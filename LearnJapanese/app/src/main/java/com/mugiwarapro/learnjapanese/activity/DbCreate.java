package com.mugiwarapro.learnjapanese.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.mugiwarapro.learnjapanese.R;
import com.mugiwarapro.learnjapanese.model.WordDao;
import com.mugiwarapro.learnjapanese.model.WordDbHelper;
import com.mugiwarapro.learnjapanese.model.WordEntity;
import com.mugiwarapro.learnjapanese.util.ArrayUtil;
import com.mugiwarapro.learnjapanese.util.KeyboardUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class DbCreate extends Activity {

	private static final String ENCODE = "UTF-8";

    private String mDbfileName;
    private String mDataFileName;
	private String[] mFileList;
	private List<WordEntity> mData;
	private String[] mParts;
	private String[] mCategories;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_db_auto_create);
        KeyboardUtil.initHide(this);

        mDbfileName = "data";
        mDataFileName ="data.csv";
        // 終了ボタン押下時の処理
  		Button finishButton = (Button)findViewById(R.id.finish_button);
  		finishButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // アクティビティ終了
                finish();
            }
        });
    }

	@Override
	protected void onResume() {
		super.onResume();
		// csvファイル名の一覧を取得
        checkDbFileExists();
	}


	/**
	 * DBファイルがすでに存在しているか確認し，存在している場合は削除許可を求める
	 * 存在していない，もしくは削除しても良いならば，createDatabaseを呼び出す
	 */
	private void checkDbFileExists() {
		// DBファイルの存在確認
		String dbFilename = mDbfileName;
		if(!dbFilename.endsWith(".db")) {
			dbFilename += ".db";
		}

		final File dbFile = new File(MainActivity.getApplicationDir(), dbFilename);
		// 存在している場合，削除の確認
		if(dbFile.exists()) {
			// 確認ダイアログを出す
			String title = getResources().getString(R.string.confirm);
			String msg   = getResources().getString(R.string.confirm_db_delete);
			new YesNoDialog(
					DbCreate.this,
					// 削除許可の場合，DB作成へ
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							createDatabase(dbFile);
						}
					},
					null,
					title,
					msg
			).show();
		} else {
			// ファイルが存在しない場合，DB作成へ
			createDatabase(dbFile);
		}
	}

	/**
	 * DBを作成する．
	 * すでにファイルが存在している場合，削除する．
	 * @param dbFile
	 */
	private void createDatabase(File dbFile) {
		// ファイル名取得
		String csvFilename = mDataFileName;
		// データ読み込み
		// 成功したらtrueが返る
		if(readData(csvFilename)) {
			// 存在しているDBを削除
			dbFile.delete();
			// 別スレッドでDBを作成
			createDatabaseInBackground(dbFile.getName(), csvFilename);
		} else {
			// 何らかの原因でDBファイルの作成に失敗
			String msg = getResources().getString(R.string.db_create_fail);
			Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
		}
	}

	private void createDatabaseInBackground(final String dbFilename, final String csvFilename) {
		// 非同期タスクで実行
    	// UIスレッドでは検索中ダイアログを表示
    	new AsyncTask<Void, Void, Void>() {

    		private ProgressDialog progressDialog;

    		@Override
			protected void onPreExecute() {
    			// 作成中というダイアログを表示
				progressDialog = new ProgressDialog(DbCreate.this);
				progressDialog.setTitle(R.string.progress_create);
				progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				progressDialog.setCancelable(true);
				progressDialog.show();
			}

    		@Override
			protected Void doInBackground(Void... args) {
    			// データベース作成
    			WordDbHelper.initDataBase(getApplicationContext(), dbFilename);
    			// データの挿入
    			WordDao dao = new WordDao();
    			dao.insertWords(mData);
    			dao.remakeParts(mParts);
    			dao.remakeCategories(mCategories);
    			return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				progressDialog.dismiss();
				String msg = getResources().getString(R.string.db_create_success);
				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
				// 入力フィールドクリア
				}

			@Override
			protected void onCancelled() {
				progressDialog.dismiss();
				this.cancel(true);
			}

    	}.execute();
	}

	private boolean readData(String filename) {
		BufferedReader br = null;
		int lineNum = 4;
		try {
			br = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.data)));
//			br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(MainActivity.getApplicationDir(), filename)), ENCODE));
			// 1行目は読み飛ばす
			br.readLine();
			// 2行目：品詞
			mParts = br.readLine().split(",");
			// 3行目：カテゴリ
			mCategories = br.readLine().split(",");
			// 4行目以降：データ
			mData = new ArrayList<WordEntity>();
			String line;
			while((line = br.readLine()) != null) {
				// 単語データを作成する
				WordEntity word = new WordEntity();
				// csvファイルの形式：
				// spell,meaning,part,category,exen,exja
				String[] info = line.split(",");
				// 必須情報が欠けている場合，エラーを吐く
				if(info.length < 3) {
					throw new IllegalFormatException();
				}
				int id;
				for(int i=0; i<info.length; i++) {
					switch(i) {
					case 0:	// 必須：スペル
						word.setSpell(info[i]);
						break;
					case 1:	// 必須：意味
						word.setMeaning(info[i]);
						break;
					case 2:	// 必須：品詞
						id = ArrayUtil.linearSearch(mParts, info[i]);
						if(id < 0) {
							throw new IllegalFormatException();
						} else {
							word.setPartId(id);
						}
						break;
					case 3:	// 任意：カテゴリ
						id = ArrayUtil.linearSearch(mCategories, info[i]);
						if(id < 0) {
							throw new IllegalFormatException();
						} else {
							word.setCategoryId(id);
						}
						break;
					case 4:	// 任意：例文
						word.setExampleEn(info[i]);
						break;
					case 5:	// 任意：和訳
						word.setExampleJa(info[i]);
						break;
					}
				}
				// リストに追加
				mData.add(word);
				// 行数を増やす（エラー表示用）
				lineNum ++;
			}
		} catch (FileNotFoundException e) {
			Toast.makeText(getApplicationContext(), "Error: cannot find the file : " + filename, Toast.LENGTH_SHORT).show();
			return false;
		} catch (IOException e) {
			Toast.makeText(getApplicationContext(), "Error: cannot read the file : " + filename, Toast.LENGTH_SHORT).show();
			return false;
		} catch (IllegalFormatException e) {
			Toast.makeText(getApplicationContext(), "Error: illegal format : " + filename + "(line" + lineNum + ")", Toast.LENGTH_SHORT).show();
			return false;
		} finally {
			try {
				if(br != null) {
					br.close();
				}
			} catch(IOException e) {
				return false;
			}
		}

		return true;
	}

	@SuppressWarnings("serial")
	private class IllegalFormatException extends Exception {}

}
