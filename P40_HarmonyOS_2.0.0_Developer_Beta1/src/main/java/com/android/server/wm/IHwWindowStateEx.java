package com.android.server.wm;

import android.graphics.Rect;
import android.view.WindowManager;
import android.view.animation.Animation;

public interface IHwWindowStateEx {
    Rect adjustImePosForFreeform(Rect rect, Rect rect2);

    int adjustTopForFreeform(Rect rect, Rect rect2, int i);

    int calculateInputMethodWindowHeight(int i, int i2, int i3, int i4, int i5);

    void createMagicWindowDimmer();

    void destoryMagicWindowDimmer();

    void initializeHwAnim(Animation animation, int i, int i2, int i3);

    boolean isInHideCaptionList();

    boolean isInHwFreeFormWorkspace();

    boolean isNeedMoveAnimation(WindowState windowState);

    boolean isPopUpIme(int i, boolean z, WindowManager.LayoutParams layoutParams);

    void setInputMethodWindowTop(int i);

    void stopMagicWindowDimmer();

    boolean updateMagicWindowDimmer();
}
