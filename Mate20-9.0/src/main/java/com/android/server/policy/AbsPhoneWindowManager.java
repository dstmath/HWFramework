package com.android.server.policy;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.graphics.Rect;
import android.os.Handler;
import android.view.DisplayCutout;
import android.view.WindowManager;
import com.android.server.policy.WindowManagerPolicy;

public abstract class AbsPhoneWindowManager {
    private static final String TAG = "AbsPhoneWindowManager";
    public String fingersense_enable = null;
    public String fingersense_letters_enable = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
    public String fingersense_screenrecord_enable = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
    public String line_gesture_enable = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
    public String navibar_enable = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;

    public WindowManagerPolicy.WindowState getNavigationBarExternal() {
        return null;
    }

    public boolean needTurnOff(int why) {
        return true;
    }

    public boolean needTurnOffWithDismissFlag(WindowManagerPolicy.WindowState appWindow) {
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

    /* access modifiers changed from: protected */
    public boolean computeNaviBarFlag() {
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

    /* access modifiers changed from: protected */
    public void setHasAcitionBar(boolean hasActionBar) {
    }

    /* access modifiers changed from: protected */
    public void enableSystemWideActions() {
    }

    /* access modifiers changed from: protected */
    public void disableSystemWideActions() {
    }

    public WindowManagerPolicy.WindowState getFocusedWindow() {
        return null;
    }

    public int getRestrictedScreenHeight() {
        return 0;
    }

    /* access modifiers changed from: protected */
    public void enableSystemWideAfterBoot(Context context) {
    }

    /* access modifiers changed from: protected */
    public void regitsterFingerObserver(ContentResolver resolver, boolean notifyForDescendents, ContentObserver observer) {
    }

    /* access modifiers changed from: protected */
    public void setFingerSenseState(ContentResolver resolver, Handler handler) {
    }

    public void waitKeyguardDismissDone(WindowManagerPolicy.KeyguardDismissDoneListener listener) {
    }

    public void cancelWaitKeyguardDismissDone() {
    }

    /* access modifiers changed from: protected */
    public void finishKeyguardDismissDone() {
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

    /* access modifiers changed from: protected */
    public void enableFingerPrintActions() {
    }

    /* access modifiers changed from: protected */
    public void disableFingerPrintActions() {
    }

    /* access modifiers changed from: protected */
    public void enableFingerPrintActionsAfterBoot(Context context) {
    }

    /* access modifiers changed from: protected */
    public void setNaviBarState() {
    }

    /* access modifiers changed from: protected */
    public void updateSplitScreenView() {
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

    /* access modifiers changed from: protected */
    public void notifyPowerkeyInteractive(boolean bool) {
    }

    public void getStableInsetsLw(int displayRotation, int displayWidth, int displayHeight, Rect outInsets, int displayId, DisplayCutout displayCutout) {
    }

    public void getNonDecorInsetsLw(int displayRotation, int displayWidth, int displayHeight, Rect outInsets, int displayId, DisplayCutout displayCutout) {
    }

    public void overrideRectForForceRotation(WindowManagerPolicy.WindowState win, Rect pf, Rect df, Rect of, Rect cf, Rect vf, Rect dcf) {
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

    public void layoutWindowLwForNotch(WindowManagerPolicy.WindowState win, WindowManager.LayoutParams attrs) {
    }

    public boolean canLayoutInDisplayCutout(WindowManagerPolicy.WindowState win) {
        return false;
    }

    public void layoutWindowForPadPCMode(WindowManagerPolicy.WindowState win, Rect pf, Rect df, Rect cf, Rect vf, int mContentBottom) {
    }

    public boolean hideNotchStatusBar(int fl) {
        return true;
    }

    public void notchStatusBarColorUpdate(int statusbarStateFlag) {
    }

    public void onPointDown() {
    }

    public void setPowerState(int powerState) {
    }

    public void pause() {
    }

    public int getDeviceNodeFD() {
        return -1;
    }

    public int getHardwareType() {
        return -1;
    }

    /* access modifiers changed from: protected */
    public void notifyFingerSense(int rotation) {
    }

    /* access modifiers changed from: protected */
    public void uploadKeyEvent(int keyEvent) {
    }

    public boolean isHwStartWindowEnabled(int type) {
        return false;
    }

    public Context addHwStartWindow(ApplicationInfo appInfo, Context overrideContext, Context context, TypedArray typedArray, int windowFlags) {
        return null;
    }

    public void setGestureNavMode(String packageName, int uid, int leftMode, int rightMode, int bottomMode) {
    }
}
