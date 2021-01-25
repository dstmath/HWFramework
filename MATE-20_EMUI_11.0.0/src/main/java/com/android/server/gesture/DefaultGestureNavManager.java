package com.android.server.gesture;

import android.annotation.SystemApi;
import android.app.IHwDockCallBack;
import android.content.Context;
import android.graphics.Point;
import android.os.Looper;
import com.android.server.policy.WindowManagerPolicyEx;
import com.huawei.android.server.WatchdogEx;
import java.io.PrintWriter;

public class DefaultGestureNavManager extends WatchdogEx.MonitorEx {
    public DefaultGestureNavManager(Context context) {
    }

    public Looper getGestureLoooper() {
        return null;
    }

    @Override // com.huawei.android.server.WatchdogEx.MonitorEx
    public void monitor() {
    }

    public void systemReady() {
    }

    public boolean isGestureNavStartedNotLocked() {
        return false;
    }

    public void onUserChanged(int newUserId) {
    }

    public void onConfigurationChanged() {
    }

    public void onMultiWindowChanged(int state) {
    }

    public void onRotationChanged(int rotation) {
    }

    public void onKeyguardShowingChanged(boolean isShowing) {
    }

    public boolean onFocusWindowChanged(WindowManagerPolicyEx.WindowStateEx lastFocus, WindowManagerPolicyEx.WindowStateEx newFocus) {
        return false;
    }

    public void onLayoutInDisplayCutoutModeChanged(WindowManagerPolicyEx.WindowStateEx win, boolean isOldUsingNotch, boolean isNewUsingNotch) {
    }

    public void onLockTaskStateChanged(int lockTaskState) {
    }

    public void setGestureNavMode(String packageName, int uid, int leftMode, int rightMode, int bottomMode) {
    }

    public void initSubScreenNavView() {
    }

    public void bringTopSubScreenNavView() {
    }

    public void destroySubScreenNavView() {
    }

    public void updateGestureNavRegion(boolean isShrink, int navId) {
    }

    public boolean isPointInExcludedRegion(Point point) {
        return false;
    }

    public boolean isKeyNavEnabled() {
        return false;
    }

    public void dump(String dumpPrefix, PrintWriter pw, String[] args) {
    }

    @SystemApi
    public boolean setDockCallBackInfo(IHwDockCallBack callBack, int type) {
        return false;
    }

    public void notifyANR(CharSequence windowTitle) {
    }
}
