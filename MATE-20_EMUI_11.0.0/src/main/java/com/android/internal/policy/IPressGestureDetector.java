package com.android.internal.policy;

import android.content.res.Configuration;
import android.view.MotionEvent;

public interface IPressGestureDetector {
    boolean dispatchTouchEvent(MotionEvent motionEvent, boolean z);

    void handleBackKey();

    void handleConfigurationChanged(Configuration configuration);

    boolean isLongPressSwipe();

    void onAttached(int i);

    void onDetached();
}
