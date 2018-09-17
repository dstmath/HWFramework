package com.android.internal.widget;

import android.view.View;

public interface LockScreenWidgetCallback {
    boolean isVisible(View view);

    void requestHide(View view);

    void requestShow(View view);

    void userActivity(View view);
}
