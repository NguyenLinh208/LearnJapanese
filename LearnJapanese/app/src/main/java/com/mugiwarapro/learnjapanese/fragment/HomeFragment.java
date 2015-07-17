package com.mugiwarapro.learnjapanese.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.mugiwarapro.learnjapanese.Config;
import com.mugiwarapro.learnjapanese.R;
import com.mugiwarapro.learnjapanese.activity.LearningListActivity;

/**
 * Created by usr0200475 on 15/07/13.
 */
public class HomeFragment extends Fragment {


    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static HomeFragment newInstance(int sectionNumber) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Button settingButton = (Button)rootView.findViewById(R.id.setting_button);
        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                Toast.makeText(getActivity(), "onClicked", Toast.LENGTH_SHORT).show();
                pref.edit().putString(Config.DIALOG_MODE, "0").commit();
                intent = new Intent(getActivity(), LearningListActivity.class);
                startActivity(intent);
            }
        });
        return rootView;
    }


}
