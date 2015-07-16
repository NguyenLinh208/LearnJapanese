package com.mugiwarapro.learnjapanese.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;
import android.widget.Toast;

import com.mugiwarapro.learnjapanese.fragment.HomeFragment;
import com.mugiwarapro.learnjapanese.fragment.NavigationDrawerFragment;
import com.mugiwarapro.learnjapanese.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.mugiwarapro.learnjapanese.Config;
import com.mugiwarapro.learnjapanese.fragment.SettingFragment;
import com.mugiwarapro.learnjapanese.model.WordDao;
import com.mugiwarapro.learnjapanese.model.WordDbHelper;
import com.mugiwarapro.learnjapanese.model.WordEntity;
import com.mugiwarapro.learnjapanese.util.ArrayUtil;
import com.mugiwarapro.learnjapanese.util.DateUtil;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private static final int REQ_CODE_TTS = 1;

    private List<WordEntity> mData;
    private String[] mParts;
    private String[] mCategories;

    private static File mFileDir;

    public static File getApplicationDir() {
        return mFileDir;
    }

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));


        // ディレクトリの取得
        // 他のクラスから一括で利用される
        // SDカードが利用可能ならSDカード，そうでなければ通常のディレクトリ
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            mFileDir = getExternalFilesDir(null);
        } else {
            mFileDir = getFilesDir();
        }
        checkDbFileExists();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        FragmentManager fragmentManager = getFragmentManager();
        switch (position) {
            case 0:
                fragmentManager.beginTransaction()
                        .replace(R.id.container, HomeFragment.newInstance(position))
                        .commit();
                break;
            case 1:
                fragmentManager.beginTransaction()
                        .replace(R.id.container, SettingFragment.newInstance(position))
                        .commit();
                break;
            case 2:
                startActivity(new Intent(this, WordListActivity.class));
                return;

        }
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.home);
                break;
            case 2:
                mTitle = getString(R.string.learn);
                break;
            case 3:
                mTitle = getString(R.string.list_category);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * TTSのインストール状態確認
     */
    private void checkTTSinstalled() {
        Intent intent = new Intent();
        intent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(intent, REQ_CODE_TTS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQ_CODE_TTS) {
            if(resultCode != TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                showTTSinstallationDialog();
            }
        }
    }

    private void showTTSinstallationDialog() {
        // 確認は初回のみ
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if(!pref.getBoolean(Config.TTS_CHECKED, false)) {
            // 確認ダイアログを出す
            String msg = getResources().getString(R.string.confirm_tts_install);
            new YesNoDialog(
                    MainActivity.this,
                    // "はい"選択時の処理
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent install = new Intent();
                            install.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                            startActivity(install);
                        }
                    },
                    null,
                    msg
            ).show();
            // チェック済み情報を保存する
            pref.edit().putBoolean(Config.TTS_CHECKED, true).commit();
        }
    }



    private void checkDbFileExists() {
        // DBファイルの存在確認
        String dbFilename = "data";
        if(!dbFilename.endsWith(".db")) {
            dbFilename += ".db";
        }

        final File dbFile = new File(MainActivity.getApplicationDir(), dbFilename);
        // 存在している場合，削除の確認
        if(dbFile.exists()) {
            Toast.makeText(MainActivity.this,"Load thành công Data ", Toast.LENGTH_SHORT);
        } else {
            // ファイルが存在しない場合，DB作成へ
            createDatabase();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // データベース読み込み
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String dbName = pref.getString(Config.DB_USING, WordDbHelper.DEF_DB_NAME);
        WordDbHelper.initDataBase(getApplicationContext(), dbName);

        // 経過日数読み込み
        DateUtil.setPassedDays(pref.getInt(Config.PASSED_DAYS, 0));
    }

    @Override
    protected void onPause() {
        super.onPause();

        // 経過日数保存
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        pref.edit().putInt(Config.PASSED_DAYS, DateUtil.getPassedDays()).commit();
    }


    private void createDatabase() {
        // ファイル名取得
        String csvFilename = "data.csv";
        // データ読み込み
        // 成功したらtrueが返る
        if(readData()) {
            // 存在しているDBを削除csv
            // 別スレッドでDBを作成
            createDatabaseInBackground("data");
        } else {
            // 何らかの原因でDBファイルの作成に失敗
            String msg = getResources().getString(R.string.db_create_fail);
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
        }
    }

    private void createDatabaseInBackground(final String dbFilename) {
        // 非同期タスクで実行
        // UIスレッドでは検索中ダイアログを表示
        new AsyncTask<Void, Void, Void>() {

            private ProgressDialog progressDialog;

            @Override
            protected void onPreExecute() {
                // 作成中というダイアログを表示
                progressDialog = new ProgressDialog(MainActivity.this);
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
                // DB作成後にcsvファイルを削除する場合，削除処理
                // トーストを表示
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

    private boolean readData() {
        BufferedReader br = null;
        int lineNum = 4;
        try {
            br = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.data)));
            //	br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(MainActivity.getApplicationDir(), filename)), ENCODE));
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
            Toast.makeText(getApplicationContext(), "Error: cannot find the file : ", Toast.LENGTH_SHORT).show();
            return false;
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Error: cannot read the file : " , Toast.LENGTH_SHORT).show();
            return false;
        } catch (IllegalFormatException e) {
            Toast.makeText(getApplicationContext(), "Error: illegal format : " + "(line" + lineNum + ")", Toast.LENGTH_SHORT).show();
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

