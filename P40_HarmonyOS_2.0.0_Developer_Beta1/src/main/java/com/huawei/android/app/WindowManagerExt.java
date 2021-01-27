package com.huawei.android.app;

import android.os.Bundle;
import android.os.FreezeScreenScene;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.view.IWindowManager;
import android.view.WindowManagerGlobal;
import com.huawei.android.view.HwWindowManager;
import com.huawei.android.view.IDockedStackListenerEx;

public class WindowManagerExt {
    private static final String TAG = "WindowManagerEx";
    public static final int TRANSIT_ACTIVITY_CLOSE = 7;
    public static final int TRANSIT_KEYGUARD_GOING_AWAY = 20;
    public static final int TRANSIT_NONE = 0;
    public static final int TRANSIT_TASK_CLOSE = 9;
    public static final int TRANSIT_TASK_OPEN = 8;
    public static final int TRANSIT_TASK_TO_BACK = 11;
    public static final int TRANSIT_TASK_TO_FRONT = 10;
    public static final int TRANSIT_TRANSLUCENT_ACTIVITY_CLOSE = 25;
    public static final int TRANSIT_WALLPAPER_CLOSE = 12;
    public static final int TRANSIT_WALLPAPER_OPEN = 13;

    public static void updateFocusWindowFreezed(Boolean isGainFocus) {
        if (HwWindowManager.getService() != null) {
            try {
                HwWindowManager.getService().updateFocusWindowFreezed(isGainFocus.booleanValue());
            } catch (RemoteException e) {
                Log.e(TAG, "updateFocusWindowFreezed RemoteException");
            }
        }
    }

    public static int getDefaultDisplayRotation() {
        IWindowManager windowManager = IWindowManager.Stub.asInterface(ServiceManager.getService(FreezeScreenScene.WINDOW_PARAM));
        if (windowManager == null) {
            return -1;
        }
        try {
            return windowManager.getDefaultDisplayRotation();
        } catch (RemoteException e) {
            Log.e(TAG, "Error occured when get rotation");
            return -1;
        }
    }

    public static void registerDockedStackListener(IDockedStackListenerEx listener) throws RemoteException {
        WindowManagerGlobal.getWindowManagerService().registerDockedStackListener(listener.getDockedStackListener());
    }

    public static int getNavBarPosition(int displayId) throws RemoteException {
        return WindowManagerGlobal.getWindowManagerService().getNavBarPosition(displayId);
    }

    public static IBinder asBinder() {
        return WindowManagerGlobal.getWindowManagerService().asBinder();
    }

    public static boolean isServiceReady() {
        return WindowManagerGlobal.getWindowManagerService() != null;
    }

    public static void lockNow(Bundle options) throws RemoteException {
        WindowManagerGlobal.getWindowManagerService().lockNow(options);
    }
}
