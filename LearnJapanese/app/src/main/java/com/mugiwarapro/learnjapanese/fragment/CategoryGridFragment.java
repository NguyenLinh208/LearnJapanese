package com.mugiwarapro.learnjapanese.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.mugiwarapro.learnjapanese.R;
import com.mugiwarapro.learnjapanese.activity.WordListActivity;
import com.mugiwarapro.learnjapanese.adapter.GridViewAdapter;
import com.mugiwarapro.learnjapanese.model.GridViewItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by usr0200475 on 15/07/16.
 */
public class CategoryGridFragment extends Fragment implements AdapterView.OnItemClickListener {

    private List<GridViewItem> mItems;
    private GridViewAdapter mAdapter;
    // カテゴリ
    private static final String[] listCategory = {"Clothing","Direction","Nature","Culture","Daily Routines","Relationship","Weather","Office","Travel","TAT200"};



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // initialize the items list
        mItems = new ArrayList<GridViewItem>();
        Resources resources = getResources();
        for (int i = 0; i <= 9; i++) {
            mItems.add(new GridViewItem(resources.getDrawable(R.drawable.category_icon), listCategory[i]));
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate the root view of the fragment
        View fragmentView = inflater.inflate(R.layout.fragment_grid_category, container, false);

        mAdapter = new GridViewAdapter(getActivity(), mItems);

        // initialize the GridView
        GridView gridView = (GridView) fragmentView.findViewById(R.id.gridView);
        gridView.setAdapter(mAdapter);
        gridView.setOnItemClickListener(this);

        return fragmentView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        GridViewItem item = mItems.get(position);
        Toast.makeText(getActivity(), "onCLicked", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getActivity(), WordListActivity.class);
        intent.putExtra("CATEGORY",position);
        startActivity(intent);

    }

}
