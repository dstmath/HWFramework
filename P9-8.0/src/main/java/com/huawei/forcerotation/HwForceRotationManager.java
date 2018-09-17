package com.huawei.forcerotation;

import android.app.ActivityThread;
import android.content.Context;
import android.graphics.Rect;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.util.HwPCUtils;
import android.util.Slog;
import com.huawei.forcerotation.IHwForceRotationManager.Stub;

public class HwForceRotationManager implements IForceRotationManager {
    private static final String FORCE_ROTATION_FEATURE_PROP_KEY = "ro.config.hw_force_rotation";
    private static final String TAG = "HwForceRotationManager";
    private static HwForceRotationManager sInstance;
    private boolean mForceRotationFeatureSupported = SystemProperties.getBoolean(FORCE_ROTATION_FEATURE_PROP_KEY, false);
    private IHwForceRotationManager mService;

    private HwForceRotationManager() {
    }

    private boolean checkForceRotationService() {
        synchronized (this) {
            if (this.mService == null) {
                this.mService = Stub.asInterface(ServiceManager.getService("forceRotationService"));
            }
        }
        if (this.mService != null) {
            return true;
        }
        Slog.i(TAG, "checkForceRotationService->service is not started yet");
        return false;
    }

    public boolean isForceRotationSupported() {
        return this.mForceRotationFeatureSupported;
    }

    public static synchronized HwForceRotationManager getDefault() {
        HwForceRotationManager hwForceRotationManager;
        synchronized (HwForceRotationManager.class) {
            if (sInstance == null) {
                sInstance = new HwForceRotationManager();
            }
            hwForceRotationManager = sInstance;
        }
        return hwForceRotationManager;
    }

    public boolean isForceRotationSwitchOpen() {
        if (!checkForceRotationService()) {
            return false;
        }
        boolean result = false;
        try {
            result = this.mService.isForceRotationSwitchOpen();
        } catch (RemoteException ex) {
            Slog.w(TAG, "isForceRotationSwitchOpen,ex:" + ex);
        }
        return result;
    }

    public boolean isForceRotationSwitchOpen(Context context) {
        if (!(context == null || context.getContentResolver() == null || System.getInt(context.getContentResolver(), "force_rotation_mode", 0) != 1)) {
            int isPcCastModeInServer;
            if (HwPCUtils.enabledInPad()) {
                isPcCastModeInServer = HwPCUtils.isPcCastModeInServer();
            } else {
                isPcCastModeInServer = 0;
            }
            if ((isPcCastModeInServer ^ 1) != 0) {
                return true;
            }
        }
        return false;
    }

    public boolean isAppInForceRotationWhiteList(String packageName) {
        if (!checkForceRotationService()) {
            return false;
        }
        boolean result = false;
        try {
            result = this.mService.isAppInForceRotationWhiteList(packageName);
        } catch (RemoteException ex) {
            Slog.w(TAG, "isAppInForceRotationWhiteList,ex:" + ex);
        }
        return result;
    }

    public boolean isAppForceLandRotatable(String packageName, IBinder aToken) {
        if (!checkForceRotationService()) {
            return false;
        }
        boolean rotatable = false;
        try {
            rotatable = this.mService.isAppForceLandRotatable(packageName, aToken);
        } catch (RemoteException ex) {
            Slog.w(TAG, "isAppForceLandRotatable,ex:" + ex);
        }
        return rotatable;
    }

    public boolean saveOrUpdateForceRotationAppInfo(String packageName, String componentName, IBinder aToken, int reqOrientation) {
        if (!checkForceRotationService()) {
            return false;
        }
        boolean saved = false;
        try {
            saved = this.mService.saveOrUpdateForceRotationAppInfo(packageName, componentName, aToken, reqOrientation);
        } catch (RemoteException ex) {
            Slog.w(TAG, "saveOrUpdateForceRotationAppInfo,ex:" + ex);
        }
        return saved;
    }

    public void showToastIfNeeded(String packageName, int pid, String processName, IBinder aToken) {
        if (checkForceRotationService()) {
            try {
                this.mService.showToastIfNeeded(packageName, pid, processName, aToken);
            } catch (RemoteException ex) {
                Slog.w(TAG, "showToastIfNeeded,ex:" + ex);
            }
        }
    }

    public void applyForceRotationLayout(IBinder aToken, Rect vf) {
        if (checkForceRotationService()) {
            try {
                this.mService.applyForceRotationLayout(aToken, vf);
            } catch (RemoteException ex) {
                Slog.w(TAG, "applyForceRotationLayout,ex:" + ex);
            }
        }
    }

    public int recalculateWidthForForceRotation(int width, int height, int logicalHeight) {
        if (!checkForceRotationService()) {
            return width;
        }
        int appwidth = width;
        try {
            appwidth = this.mService.recalculateWidthForForceRotation(width, height, logicalHeight, ActivityThread.currentPackageName());
        } catch (RemoteException ex) {
            Slog.w(TAG, "applyForceRotationLayout,ex:" + ex);
        }
        return appwidth;
    }
}
