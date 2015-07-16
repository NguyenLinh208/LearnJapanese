package com.mugiwarapro.learnjapanese.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mugiwarapro.learnjapanese.R;
import com.mugiwarapro.learnjapanese.model.GridViewItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by usr0200475 on 15/07/04.
 */
public class GridViewAdapter extends BaseAdapter {
    private Context mContext;
    private List<GridViewItem> mItems;

    public GridViewAdapter(Context context, List<GridViewItem> items) {
        mContext = context;
        mItems = items;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;


        if(convertView == null) {
            // inflate the GridView item layout
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.gridview_item, parent, false);

            // initialize the view holder
            viewHolder = new ViewHolder();
            viewHolder.ivIcon = (ImageView) convertView.findViewById(R.id.ivIcon);
            viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
            convertView.setTag(viewHolder);
        } else {
            // recycle the already inflated view
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // update the item view
        GridViewItem item = mItems.get(position);
        viewHolder.ivIcon.setImageDrawable(item.icon);
        viewHolder.tvTitle.setText(item.title);

//
//        int toiletPositionNumber = position;
//        if (position >= 4) {
//            toiletPositionNumber = position - position / 4 - 1;
//        }
//        int color = 0x00FFFFFF; // Transparent
//        if ( position%4 == 0) {
//            color = 0x00FFFFFF;
//        } else  if (listToilets.get(toiletPositionNumber).isStatus()) {
//            color = Color.GRAY; // Opaque Blue
//        }
//
//        convertView.setBackgroundColor(color);

        return convertView;
    }

    /**
     * The view holder design pattern prevents using findViewById()
     * repeatedly in the getView() method of the adapter.
     */
    private static class ViewHolder {
        ImageView ivIcon;
        TextView tvTitle;
    }
}