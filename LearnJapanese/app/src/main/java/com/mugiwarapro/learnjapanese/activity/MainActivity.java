package com.mugiwarapro.learnjapanese.activity;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.mugiwarapro.learnjapanese.adapter.NavigationDrawerAdapter;
import com.mugiwarapro.learnjapanese.fragment.CategoryGridFragment;
import com.mugiwarapro.learnjapanese.fragment.HomeFragment;
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
import com.mugiwarapro.learnjapanese.model.NavigationDrawerItem;
import com.mugiwarapro.learnjapanese.model.WordDao;
import com.mugiwarapro.learnjapanese.model.WordDbHelper;
import com.mugiwarapro.learnjapanese.model.WordEntity;
import com.mugiwarapro.learnjapanese.util.ArrayUtil;
import com.mugiwarapro.learnjapanese.util.DateUtil;

public class MainActivity extends ActionBarActivity {

    private static final int REQ_CODE_TTS = 1;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawList;
    private List<WordEntity> mData;
    private String[] mParts;
    private String[] mCategories;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawTitle;
    private CharSequence mTitle;

    // slide menu items
    private String[] navMenuTitles;
    private TypedArray navMenuIcons;
    private ArrayList<NavigationDrawerItem> navDrawerItems;
    private NavigationDrawerAdapter adapter;

    private static File mFileDir;

    public static File getApplicationDir() {
        return mFileDir;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTitle = mDrawTitle = getTitle();

        //Load slide menu Item
        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);
        navMenuIcons = getResources().obtainTypedArray(R.array.nav_drawer_icons);

        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mDrawList = (ListView)findViewById(R.id.list_slidermenu);

        //List item of NavigationDrawer
        navDrawerItems = new ArrayList<NavigationDrawerItem>();

        // adding nav drawer items to array
        // Home
        navDrawerItems.add(new NavigationDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1)));
        // Setting
        navDrawerItems.add(new NavigationDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1)));
        // Category
        navDrawerItems.add(new NavigationDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2, -1)));
        navMenuIcons.recycle();
        adapter = new NavigationDrawerAdapter(getApplicationContext(),navDrawerItems);
        mDrawList.setAdapter(adapter);
        mDrawList.setOnItemClickListener(new SlideMenuClickListener());

        //Show actionbar
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.drawable.ic_drawer, //nav menu toggle icon
                R.string.app_name,    // nav drawer open - description for accessibility
                R.string.app_name     // nav drawer close - description for accessibility
        ){
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);
                // calling onPrepareOptionsMenu() to show action bar icons
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(mDrawTitle);
                // calling onPrepareOptionsMenu() to hide action bar icons
                invalidateOptionsMenu();
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            displayView(0);
        }

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

    /**
     * Slide menu item click listener
     * */
    private class SlideMenuClickListener implements
            ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            displayView(position);
        }
    }

    private void displayView(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new HomeFragment();
                break;
            case 1:
                fragment = new SettingFragment();
                break;
            case 2:
                fragment = new CategoryGridFragment();
                break;
            default:
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, fragment).commit();

            // update selected item and title, then close the drawer
            mDrawList.setItemChecked(position, true);
            mDrawList.setSelection(position);
            setTitle(navMenuTitles[position]);
            mDrawerLayout.closeDrawer(mDrawList);
        } else {
            // error in creating fragment
            Log.e("MainActivity", "Error in creating fragment");
        }

        Log.v("MenuTitle", navMenuTitles[0]);

    }
    /**
     * Slide menu item click listener
     * */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /***
     * Called when invalidateOptionsMenu() is triggered
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // if nav drawer is opened, hide the action items
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawList);
        menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
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

