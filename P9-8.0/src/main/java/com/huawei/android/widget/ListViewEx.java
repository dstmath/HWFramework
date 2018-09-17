package com.huawei.android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class ListViewEx extends ListView {
    public ListViewEx(Context context) {
        super(context);
    }

    public ListViewEx(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ListViewEx(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public Object getScroller() {
        return super.getScrollerInner();
    }

    public void setScroller(FastScrollerEx scroller) {
        super.setScrollerInner(scroller);
    }
}
