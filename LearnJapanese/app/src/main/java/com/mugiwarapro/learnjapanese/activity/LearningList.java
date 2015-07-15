package com.mugiwarapro.learnjapanese.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.mugiwarapro.learnjapanese.Config;
import com.mugiwarapro.learnjapanese.R;
import com.mugiwarapro.learnjapanese.model.Question3Activity;
import com.mugiwarapro.learnjapanese.model.Question4Activity;
import com.mugiwarapro.learnjapanese.model.VSappConst;
import com.mugiwarapro.learnjapanese.model.WordDao;
import com.mugiwarapro.learnjapanese.model.WordDbHelper;
import com.mugiwarapro.learnjapanese.model.WordEntity;
import com.mugiwarapro.learnjapanese.model.WordListBase;
import com.mugiwarapro.learnjapanese.util.DateUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class LearningList extends WordListBase<Integer> {

	private static final int REQ_CODE_LEARNING = 2;

	private List<WordEntity> mNewWords;
	private List<WordEntity> mReviseWords;
	private Thread mBackgroundThread;

	private Spinner mCategorySpinner;

	@Override
	protected void setContentView() {
		setContentView(R.layout.activity_learning_list);
	}
	@Override
	protected void initOnCreate() {
		// ビューの取得
        mCategorySpinner = (Spinner)findViewById(R.id.category_spinner);

        // スピナー用のアダプタの作成とセット
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, getCategoryList());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCategorySpinner.setAdapter(adapter);

        // スピナーの選択が変更された際の処理
        mCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        	public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
				// 選択されたカテゴリの単語を探す
				search(position);
			}
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

	}

	/**
	 * カテゴリに"全て"を加えた配列を作成
	 * @return
	 */
	private String[] getCategoryList() {
		WordDao dao = new WordDao();
		String[] catTemp = dao.getCategories();
		String[] catList = new String[catTemp.length+1];
		catList[0] = getResources().getString(R.string.all_category);
		System.arraycopy(catTemp, 0, catList, 1, catTemp.length);
		return catList;
	}

	@Override
	protected List<WordEntity> doInAsyncBackground(Integer... args) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		WordDao dao = new WordDao();
		// 共通検索条件設定
		String order = "random()";
		String limit = pref.getString(Config.LEARNING_NUM, Config.DEF_LEARNING_NUM);

		// 引数==0：全てのカテゴリ
		// 引数!=0：カテゴリ指定
		if(args[0] == 0) {
			String where = WordDbHelper.WCOL_LEVEL + " = ?";
			String[] params = {"0"};
			// 検索実行
			mNewWords = dao.searchWord(PROJECTION, where, params, order, limit);
		} else {
			String where = WordDbHelper.WCOL_LEVEL         + " = ? and "
						 + WordDbHelper.WCOL_CATEGORY_FULL + " = ?";
			String[] params = {"0", String.valueOf(args[0] - 1)};
			// 検索実行
			mNewWords = dao.searchWord(PROJECTION, where, params, order, limit);
		}

		return mNewWords;
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == REQ_CODE_LEARNING) {
			if(resultCode == Activity.RESULT_OK) {
				finish();
			}
		}
	}
}
