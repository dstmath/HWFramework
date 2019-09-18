package com.android.server.power;

import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IHwBrightnessCallback;
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

    public int getAdjustedMaxTimeout(int oldtimeout, int maxv) {
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

    public boolean setGoogleEBS(boolean isGoogleEBS) {
        return false;
    }

    public boolean getWakeLockByUid(int uid, int wakeflag) {
        return false;
    }

    public void setLcdRatio(int ratio, boolean autoAdjust) {
    }

    public void configBrightnessRange(int ratioMin, int ratioMax, int autoLimit) {
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
    public void notifyWakeLockToIAware(int uid, int pid, String packageName, String Tag) {
    }

    /* access modifiers changed from: protected */
    public void notifyWakeLockReleaseToIAware(int uid, int pid, String packageName, String Tag) {
    }

    /* access modifiers changed from: protected */
    public int hwBrightnessSetDataInternal(String name, Bundle data) {
        return -1;
    }

    /* access modifiers changed from: protected */
    public int hwBrightnessGetDataInternal(String name, Bundle data) {
        return -1;
    }

    /* access modifiers changed from: protected */
    public int hwBrightnessRegisterCallbackInternal(IHwBrightnessCallback cb, List<String> list) {
        return -1;
    }

    /* access modifiers changed from: protected */
    public int hwBrightnessUnregisterCallbackInternal(IHwBrightnessCallback cb) {
        return -1;
    }

    /* access modifiers changed from: protected */
    public void stopWakeLockedSensor(boolean trunOffScreen) {
    }
}
