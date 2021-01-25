package com.huawei.server.wm;

import android.graphics.Rect;
import android.os.Handler;
import android.view.Display;
import android.view.MotionEvent;
import android.view.WindowManager;
import com.android.server.wm.DisplayFrames;
import com.android.server.wm.WindowFrames;
import com.android.server.wm.WindowState;
import java.io.PrintWriter;

public interface IHwDisplayPolicyEx {
    public static final int INVALID_WIDTH = -1;

    void addPointerEvent(MotionEvent motionEvent);

    void beginLayoutForPC(DisplayFrames displayFrames);

    boolean canUpdateDisplayFrames(WindowState windowState, WindowManager.LayoutParams layoutParams, int i);

    boolean computeNaviBarFlag();

    void dumpPC(String str, PrintWriter printWriter);

    int focusChangedLw(WindowState windowState, WindowState windowState2);

    boolean focusChangedLwForPC(WindowState windowState);

    boolean getImmersiveMode();

    int getInputMethodRightForHwMultiDisplay(int i, int i2);

    boolean getNaviBarFlag();

    int getNaviBarHeightForRotationMax(int i);

    int getNaviBarHeightForRotationMin(int i);

    int getNaviBarWidthForRotationMax(int i);

    int getNaviBarWidthForRotationMin(int i);

    WindowState getNavigationBarExternal();

    int getNonDecorDisplayHeight(int i, int i2);

    int getNonDecorDisplayWidthForExtraDisplay(int i, int i2);

    boolean getNonDecorInsetsForPC(Rect rect, int i);

    boolean getStableInsetsForPC(Rect rect, int i);

    void initialNavigationSize(Display display, int i, int i2, int i3);

    boolean isAppNeedExpand(String str);

    boolean isGestureIsolated(WindowState windowState, WindowState windowState2);

    boolean isNaviBarMini();

    boolean isNeedExceptDisplaySide(WindowManager.LayoutParams layoutParams, WindowState windowState, int i);

    boolean layoutWindowForPCNavigationBar(WindowState windowState);

    void layoutWindowLw(WindowState windowState, WindowState windowState2, WindowState windowState3, boolean z);

    void onConfigurationChanged();

    void onLockTaskStateChangedLw(int i);

    void onPointDown();

    int prepareAddWindowForPC(WindowState windowState, WindowManager.LayoutParams layoutParams);

    void registerExternalPointerEventListener();

    void removeWindowForPC(WindowState windowState);

    void setInputMethodWindowVisible(boolean z);

    void setNaviBarFlag(boolean z);

    void setNaviImmersiveMode(boolean z);

    void showTopBar(Handler handler, int i);

    boolean swipeFromBottom();

    boolean swipeFromLeft();

    boolean swipeFromRight();

    void swipeFromTop();

    void systemReadyEx();

    void unRegisterExternalPointerEventListener();

    void updateDisplayFrames(WindowState windowState, DisplayFrames displayFrames, int i, Rect rect, int i2);

    void updateNavigationBar(boolean z);

    void updateWindowDisplayFrame(WindowState windowState, int i, Rect rect);

    void updateWindowFramesForPC(WindowFrames windowFrames, Rect rect, Rect rect2, Rect rect3, Rect rect4, boolean z);
}
