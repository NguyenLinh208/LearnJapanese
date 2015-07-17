package com.mugiwarapro.learnjapanese.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mugiwarapro.learnjapanese.R;
import com.mugiwarapro.learnjapanese.model.NavigationDrawerItem;

import java.util.ArrayList;

/**
 * Created by usr0200475 on 15/07/17.
 */
public class NavigationDrawerAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<NavigationDrawerItem> navigationDrawerItems;

    public NavigationDrawerAdapter(Context context, ArrayList<NavigationDrawerItem> navigationDrawerItems) {
        this.context = context;
        this.navigationDrawerItems = navigationDrawerItems;
    }

    @Override
    public int getCount() {
        return navigationDrawerItems.size();
    }

    @Override
    public Object getItem(int position) {
        return navigationDrawerItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.draw_list_item,null);
        }

        ImageView imgIcon = (ImageView) convertView.findViewById(R.id.icon);
        TextView textTitle = (TextView) convertView.findViewById(R.id.title);
        TextView textCount = (TextView) convertView.findViewById(R.id.counter);

        imgIcon.setImageResource(navigationDrawerItems.get(position).getIcon());
        textTitle.setText(navigationDrawerItems.get(position).getTitle());

        if(navigationDrawerItems.get(position).getCounterVisibility()){
            textCount.setText(navigationDrawerItems.get(position).getCount());
        } else {
            // hide the counter view
            textCount.setVisibility(View.GONE);
        }

        return convertView;
    }
}
