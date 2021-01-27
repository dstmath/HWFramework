package com.huawei.android.view;

import android.view.IWindow;

public class IWindowEx {
    private IWindow mIWindow;

    public void setIWindow(IWindow iWindow) {
        this.mIWindow = iWindow;
    }

    public IWindow getIWindow() {
        return this.mIWindow;
    }
}
