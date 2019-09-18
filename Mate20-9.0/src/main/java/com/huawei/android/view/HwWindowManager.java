package com.huawei.android.view;

import android.content.Context;
import android.os.Binder;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.util.Singleton;
import android.view.IWindowManager;
import com.huawei.android.view.IHwWindowManager;
import java.util.List;

public class HwWindowManager {
    private static final Singleton<IHwWindowManager> IWindowManagerSingleton = new Singleton<IHwWindowManager>() {
        /* access modifiers changed from: protected */
        public IHwWindowManager create() {
            try {
                IWindowManager wms = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
                if (wms == null) {
                    return null;
                }
                return IHwWindowManager.Stub.asInterface(wms.getHwInnerService());
            } catch (RemoteException e) {
                Log.e(HwWindowManager.TAG, "IHwWindowManager create() fail: " + e);
                return null;
            }
        }
    };
    public static final int NOTCH_MODE_ALWAYS = 1;
    private static final String TAG = "HwWindowManager";

    public static IHwWindowManager getService() {
        return (IHwWindowManager) IWindowManagerSingleton.get();
    }

    public static List<String> getNotchSystemApps() {
        try {
            return getService().getNotchSystemApps();
        } catch (RemoteException e) {
            Log.e(TAG, "getSystemApps failed " + e.getMessage());
            return null;
        }
    }

    public static boolean registerWMMonitorCallback(IHwWMDAMonitorCallback callback) {
        if (getService() == null) {
            return false;
        }
        long token = Binder.clearCallingIdentity();
        try {
            getService().registerWMMonitorCallback(callback);
            Binder.restoreCallingIdentity(token);
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "registerWMMonitorCallback catch RemoteException!");
            Binder.restoreCallingIdentity(token);
            return false;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
            throw th;
        }
    }

    public static List<Bundle> getVisibleWindows(int ops) {
        if (getService() == null) {
            return null;
        }
        long token = Binder.clearCallingIdentity();
        try {
            return getService().getVisibleWindows(ops);
        } catch (RemoteException e) {
            Log.e(TAG, "getVisibleWindows catch RemoteException!");
            return null;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public static int getFocusWindowWidth() {
        try {
            return getService().getFocusWindowWidth();
        } catch (RemoteException e) {
            Log.e(TAG, "getFocusWindowWidth failed " + e.getMessage());
            return 0;
        }
    }

    public static void startNotifyWindowFocusChange() {
        try {
            getService().startNotifyWindowFocusChange();
        } catch (RemoteException e) {
            Log.e(TAG, "startNotifyWindowFocusChange failed " + e.getMessage());
        }
    }

    public static void stopNotifyWindowFocusChange() {
        try {
            getService().stopNotifyWindowFocusChange();
        } catch (RemoteException e) {
            Log.e(TAG, "stopNotifyWindowFocusChange failed " + e.getMessage());
        }
    }

    public static void getCurrFocusedWinInExtDisplay(Bundle outBundle) {
        try {
            getService().getCurrFocusedWinInExtDisplay(outBundle);
        } catch (RemoteException e) {
            Log.e(TAG, "getCurrFocusedWinInExtDisplay failed " + e.getMessage());
        }
    }

    public static boolean hasLighterViewInPCCastMode() {
        try {
            return getService().hasLighterViewInPCCastMode();
        } catch (RemoteException e) {
            Log.e(TAG, "hasLighterViewInPCCastMode failed " + e.getMessage());
            return false;
        }
    }

    public static boolean shouldDropMotionEventForTouchPad(float x, float y) {
        try {
            return getService().shouldDropMotionEventForTouchPad(x, y);
        } catch (RemoteException e) {
            Log.e(TAG, "shouldDropMotionEventForTouchPad failed " + e.getMessage());
            return false;
        }
    }

    public static HwTaskSnapshotWrapper getForegroundTaskSnapshotWrapper(boolean refresh) {
        try {
            return getService().getForegroundTaskSnapshotWrapper(refresh);
        } catch (RemoteException e) {
            Log.e(TAG, "getForegroundTaskSnapshotWrapper", e);
            return null;
        }
    }

    public static void setCoverManagerState(boolean isCoverOpen) {
        try {
            getService().setCoverManagerState(isCoverOpen);
        } catch (RemoteException e) {
            Log.e(TAG, "setCoverManagerState failed " + e.getMessage());
        }
    }

    public static void freezeOrThawRotation(int rotation) {
        try {
            getService().freezeOrThawRotation(rotation);
        } catch (RemoteException e) {
            Log.e(TAG, "freezeOrThawRotation failed " + e.getMessage());
        }
    }

    public static void setGestureNavMode(Context context, int leftMode, int rightMode, int bottomMode) {
        try {
            getService().setGestureNavMode(context.getPackageName(), leftMode, rightMode, bottomMode);
        } catch (RemoteException e) {
            Log.e(TAG, "setGestureNavMode failed", e);
        }
    }
}
