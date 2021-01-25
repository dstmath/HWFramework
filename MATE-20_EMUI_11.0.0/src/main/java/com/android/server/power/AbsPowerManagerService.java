package com.android.server.power;

import android.content.Context;
import android.os.IBinder;
import android.os.WorkSource;
import android.util.Slog;
import com.android.server.SystemService;
import java.util.List;

public abstract class AbsPowerManagerService extends SystemService {
    public static final int MIN_COVER_SCREEN_OFF_TIMEOUT = 10000;
    private static final String TAG = "AbsPowerManagerService";

    public AbsPowerManagerService(Context context) {
        super(context);
    }

    public int getAdjustedMaxTimeout() {
        return 0;
    }

    public void proxyWakeLockByPidUid(int pid, int uid, boolean proxy) {
    }

    /* access modifiers changed from: protected */
    public boolean acquireProxyWakeLock(IBinder lock, int flags, String tag, String packageName, WorkSource ws, String historyTag, int uid, int pid) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean updateProxyWakeLockWorkSource(IBinder lock, WorkSource ws, String historyTag, int callingUid) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean releaseProxyWakeLock(IBinder lock) {
        return false;
    }

    public void forceReleaseWakeLockByPidUid(int pid, int uid) {
    }

    public void forceRestoreWakeLockByPidUid(int pid, int uid) {
    }

    public boolean proxyedWakeLock(int subType, List<String> list) {
        return false;
    }

    public boolean getWakeLockByUid(int uid, int wakeflag) {
        return false;
    }

    public boolean isAppWakeLockFilterTag(int flags, String packageName, WorkSource ws) {
        return false;
    }

    public int setColorTemperatureInternal(int colorTemper) {
        Slog.d(TAG, "setColorTemperatureInternal");
        return 0;
    }

    /* access modifiers changed from: protected */
    public void setColorTemperatureAccordingToSetting() {
        Slog.d(TAG, "setColorTemperatureAccordingToSetting");
    }

    public int updateRgbGammaInternal(float red, float green, float blue) {
        Slog.d(TAG, "updateRgbGammaInternal");
        return 0;
    }

    public boolean isDisplayFeatureSupported(int feature) {
        return false;
    }

    /* access modifiers changed from: protected */
    public void disableBrightnessWaitLocked(boolean dismissKeyguard) {
    }

    /* access modifiers changed from: protected */
    public void startWakeUpReadyInternal(long eventTime, int uid, String opPackageName) {
    }

    /* access modifiers changed from: protected */
    public void resetWaitBrightTimeoutLocked() {
    }

    /* access modifiers changed from: protected */
    public void stopWakeUpReadyInternal(long eventTime, int uid, boolean enableBright, String opPackageName) {
    }

    /* access modifiers changed from: protected */
    public void handleWaitBrightTimeout() {
    }

    public void startWakeUpReady(long eventTime, String opPackageName) {
    }

    public void stopWakeUpReady(long eventTime, boolean enableBright, String opPackageName) {
    }

    /* access modifiers changed from: protected */
    public void setAuthSucceededInternal() {
    }

    public boolean isSkipWakeLockUsing(int uid, String tag) {
        return false;
    }

    /* access modifiers changed from: protected */
    public void notifyWakeupResult(boolean isWakenupThisTime) {
    }

    /* access modifiers changed from: protected */
    public void stopPickupTrunOff() {
    }

    /* access modifiers changed from: protected */
    public void startIntelliService() {
    }

    /* access modifiers changed from: protected */
    public void stopIntelliService() {
    }

    /* access modifiers changed from: protected */
    public int registerFaceDetect() {
        return -1;
    }

    /* access modifiers changed from: protected */
    public int unregisterFaceDetect() {
        return -1;
    }

    /* access modifiers changed from: protected */
    public int getDisplayPanelTypeInternal() {
        return -1;
    }

    /* access modifiers changed from: protected */
    public void stopWakeLockedSensor(boolean trunOffScreen) {
    }

    /* access modifiers changed from: protected */
    public void setMirrorLinkForVdrive() {
    }

    /* access modifiers changed from: protected */
    public boolean isVdriveHeldWakeLock() {
        return false;
    }

    /* access modifiers changed from: protected */
    public void setMirrorLinkPowerStatusInternal(boolean status) {
    }

    /* access modifiers changed from: protected */
    public void bedTimeLog(boolean keepAwake, boolean stayOn, boolean screenBrightnessBoostInProgress) {
    }

    /* access modifiers changed from: protected */
    public boolean isPhoneHeldWakeLock() {
        return false;
    }

    /* access modifiers changed from: protected */
    public void updateEyePoetectColorTemperature(int lastScreenState, int newScreenState) {
    }

    /* access modifiers changed from: protected */
    public boolean needFaceDetectLocked(long nextTimeout, long now, boolean startNoChangeLights) {
        return false;
    }
}
