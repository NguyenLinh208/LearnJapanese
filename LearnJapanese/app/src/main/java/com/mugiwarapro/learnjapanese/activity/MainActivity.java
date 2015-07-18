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
import com.mugiwarapro.learnjapanese.fragment.AlertDialogFragment;
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
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawTitle;
    private CharSequence mTitle;

    // slide menu items
    private String[] navMenuTitles;
    private TypedArray navMenuIcons;
    private ArrayList<NavigationDrawerItem> navDrawerItems;
    private NavigationDrawerAdapter adapter;

    //Check database file
    private static File mFileDir;
    public static File getApplicationDir() {
        return mFileDir;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AlertDialogFragment dialogFragment = new AlertDialogFragment();

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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.drawable.ic_drawer,
                R.string.app_name,
                R.string.app_name
        ){
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(mDrawTitle);
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
        checkTTSinstalled();
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

    /**
     * DBファイルがすでに存在しているか確認し，存在している場合は削除許可を求める
     * 存在していない，もしくは削除しても良いならば，createDatabaseを呼び出す
     */
    private void checkDbFileExists() {
        // DBファイルの存在確認
        String dbFilename = "data.db";
        if(!dbFilename.endsWith(".db")) {
            dbFilename += ".db";
        }

        final File dbFile = new File(MainActivity.getApplicationDir(), dbFilename);
        // 存在している場合，削除の確認
        if(dbFile.exists()) {
        } else {
            // ファイルが存在しない場合，DB作成へ
            FragmentManager fragmentManager = getFragmentManager();
            AlertDialogFragment dialogFragment = new AlertDialogFragment();
            dialogFragment.show(fragmentManager,"Dialog");
        }
    }


}

