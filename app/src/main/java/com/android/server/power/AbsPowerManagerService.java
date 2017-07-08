package com.android.server.power;

import android.content.Context;
import android.os.IBinder;
import android.os.WorkSource;
import android.util.Slog;
import com.android.server.SystemService;

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

    protected boolean acquireProxyWakeLock(IBinder lock, int flags, String tag, String packageName, WorkSource ws, String historyTag, int uid, int pid) {
        return false;
    }

    protected boolean updateProxyWakeLockWorkSource(IBinder lock, WorkSource ws, String historyTag, int callingUid) {
        return false;
    }

    protected boolean releaseProxyWakeLock(IBinder lock) {
        return false;
    }

    public void forceReleaseWakeLockByPidUid(int pid, int uid) {
    }

    public void forceRestoreWakeLockByPidUid(int pid, int uid) {
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

    protected void setColorTemperatureAccordingToSetting() {
        Slog.d(TAG, "setColorTemperatureAccordingToSetting");
    }

    public int updateRgbGammaInternal(float red, float green, float blue) {
        Slog.d(TAG, "updateRgbGammaInternal");
        return 0;
    }

    public boolean isDisplayFeatureSupported(int feature) {
        return false;
    }

    protected void disableBrightnessWaitLocked(boolean dismissKeyguard) {
    }

    protected void startWakeUpReadyInternal(long eventTime, int uid, String opPackageName) {
    }

    protected void resetWaitBrightTimeoutLocked() {
    }

    protected void stopWakeUpReadyInternal(long eventTime, int uid, boolean enableBright, String opPackageName) {
    }

    protected void handleWaitBrightTimeout() {
    }

    public void startWakeUpReady(long eventTime, String opPackageName) {
    }

    public void stopWakeUpReady(long eventTime, boolean enableBright, String opPackageName) {
    }

    public boolean isSkipWakeLockUsing(int uid, String tag) {
        return false;
    }
}
