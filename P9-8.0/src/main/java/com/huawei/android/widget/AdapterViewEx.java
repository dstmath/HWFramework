package com.huawei.android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;

public class AdapterViewEx extends AdapterView {
    public AdapterViewEx(Context context) {
        super(context);
    }

    public AdapterViewEx(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AdapterViewEx(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AdapterViewEx(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public Adapter getAdapter() {
        return null;
    }

    public void setAdapter(Adapter adapter) {
    }

    public View getSelectedView() {
        return null;
    }

    public void setSelection(int position) {
    }

    public boolean isRtlLocale() {
        return super.isRtlLocale();
    }
}
