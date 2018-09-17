package com.android.server.policy;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.graphics.Rect;
import android.os.Handler;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerPolicy.KeyguardDismissDoneListener;
import android.view.WindowManagerPolicy.WindowState;

public abstract class AbsPhoneWindowManager {
    private static final String TAG = "AbsPhoneWindowManager";
    public String fingersense_enable = null;
    public String fingersense_letters_enable = "";
    public boolean isNavibarHide;
    public String line_gesture_enable = "";
    public String navibar_enable = "";

    public WindowState getNavigationBarExternal() {
        return null;
    }

    public boolean needTurnOff(int why) {
        return true;
    }

    public boolean needTurnOffWithDismissFlag(WindowState appWindow) {
        return true;
    }

    public boolean needTurnOffWithDismissFlag() {
        return true;
    }

    public void setNaviImmersiveMode(boolean mode) {
    }

    public void freezeOrThawRotation(int rotation) {
    }

    public boolean isWakeKeyFun(int keyCode) {
        return false;
    }

    protected boolean computeNaviBarFlag() {
        return false;
    }

    public boolean isNaviBarMini() {
        return false;
    }

    public int getNaviBarHeightForRotationMin(int index) {
        return 0;
    }

    public int getNaviBarWidthForRotationMin(int index) {
        return 0;
    }

    public int getNaviBarHeightForRotationMax(int index) {
        return 0;
    }

    public int getNaviBarWidthForRotationMax(int index) {
        return 0;
    }

    public boolean swipeFromTop() {
        return false;
    }

    public boolean swipeFromBottom() {
        return false;
    }

    public boolean swipeFromRight() {
        return false;
    }

    public void setInputMethodWindowVisible(boolean visible) {
    }

    public void setNaviBarFlag(boolean flag) {
    }

    public void updateNavigationBar(boolean minNaviBar) {
    }

    public void swipFromTop() {
    }

    public boolean getImmersiveMode() {
        return false;
    }

    public void showHwTransientBars() {
    }

    public boolean isTopIsFullscreen() {
        return false;
    }

    public boolean okToShowTransientBar() {
        return true;
    }

    public boolean isGestureIsolated() {
        return false;
    }

    public void requestTransientStatusBars() {
    }

    protected void setHasAcitionBar(boolean hasActionBar) {
    }

    protected void enableSystemWideActions() {
    }

    protected void disableSystemWideActions() {
    }

    public WindowState getFocusedWindow() {
        return null;
    }

    public int getRestrictedScreenHeight() {
        return 0;
    }

    protected void enableSystemWideAfterBoot(Context context) {
    }

    protected void regitsterFingerObserver(ContentResolver resolver, boolean notifyForDescendents, ContentObserver observer) {
    }

    protected void setFingerSenseState(ContentResolver resolver, Handler handler) {
    }

    public void waitKeyguardDismissDone(KeyguardDismissDoneListener listener) {
    }

    public void cancelWaitKeyguardDismissDone() {
    }

    protected void finishKeyguardDismissDone() {
    }

    public void setInterceptInputForWaitBrightness(boolean intercept) {
    }

    public boolean getInterceptInputForWaitBrightness() {
        return false;
    }

    public void setNavibarAlignLeftWhenLand(boolean isLeft) {
    }

    public boolean getNavibarAlignLeftWhenLand() {
        return false;
    }

    protected void enableFingerPrintActions() {
    }

    protected void disableFingerPrintActions() {
    }

    protected void enableFingerPrintActionsAfterBoot(Context context) {
    }

    protected void setNaviBarState() {
    }

    protected void updateSplitScreenView() {
    }

    public boolean isSupportCover() {
        return false;
    }

    public boolean isSmartCoverMode() {
        return false;
    }

    public boolean isInCallActivity() {
        return false;
    }

    protected void notifyPowerkeyInteractive(boolean bool) {
    }

    public void getStableInsetsLw(int displayRotation, int displayWidth, int displayHeight, Rect outInsets, int displayId) {
    }

    public void getNonDecorInsetsLw(int displayRotation, int displayWidth, int displayHeight, Rect outInsets, int displayId) {
    }

    public void overrideRectForForceRotation(WindowState win, Rect pf, Rect df, Rect of, Rect cf, Rect vf, Rect dcf) {
    }

    public void layoutStatusBarForNotch(Rect pf, Rect df, Rect of) {
    }

    public void layoutWindowLwForNotch(WindowState win, WindowState attached, LayoutParams attrs, int fl, Rect pf, Rect df, Rect of, Rect cf) {
    }

    public void adjustWindowParamsLwForNotch(WindowState win, LayoutParams attrs) {
    }

    public boolean hideNotchStatusBar(int fl) {
        return true;
    }

    public void notchStatusBarColorUpdate(int statusbarStateFlag) {
    }

    public boolean isIntelliServiceEnabledFR(int orientatin) {
        return false;
    }

    public void setSensorRotationFR(int rotation) {
    }

    public void startIntelliServiceFR() {
    }

    public int getRotationFromRealSensorFR(int rotaion) {
        return -1;
    }

    public int getRotationFromSensorOrFaceFR(int orientation, int lastRotation) {
        return -1;
    }

    public void layoutWindowForPadPCMode(WindowState win, Rect pf, Rect df, Rect cf, Rect vf, int mContentBottom) {
    }
}
