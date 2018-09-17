package com.huawei.android.widget;

import android.widget.AbsListView;
import android.widget.FastScroller;

public class FastScrollerEx extends FastScroller {
    public FastScrollerEx(AbsListView listView, int styleResId) {
        super(listView, styleResId);
    }

    public FastScrollerEx(AbsListView listView) {
        super(listView, 0);
    }
}
