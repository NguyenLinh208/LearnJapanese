package com.mugiwarapro.learnjapanese.model;

import android.graphics.drawable.Drawable;

/**
 * Created by usr0200475 on 15/07/04.
 */
public class GridViewItem {
    public Drawable icon;       // the drawable for the ListView item ImageView
    public String title;        // the text for the GridView item title
    public GridViewItem(Drawable icon, String title) {
        this.icon = icon;
        this.title = title;
    }
}
