package com.android.server.wm;

import android.content.Context;

public interface IHwWindowState {
    void updateInputMethodGlobalScale(Context context, WindowState windowState);
}
