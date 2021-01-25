package com.huawei.android.view;

import android.view.IInputFilter;

public class InputFilterEx {
    private IInputFilter mIInputFilter;

    public void setIInputFilter(IInputFilter filter) {
        this.mIInputFilter = filter;
    }

    public IInputFilter getIInputFilter() {
        return this.mIInputFilter;
    }
}
