package com.huawei.android.view;

import android.graphics.Point;
import android.graphics.Region;
import android.os.RemoteException;
import android.view.WindowManagerGlobal;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class WindowManagerGlobalEx {
    public static void getInitialDisplaySize(int displayId, Point size) throws RemoteException {
        WindowManagerGlobal.getWindowManagerService().getInitialDisplaySize(displayId, size);
    }

    public static void registerSystemGestureExclusionListener(ISystemGestureExclusionListenerEx listenerEx, int displayId) throws RemoteException {
        WindowManagerGlobal.getWindowManagerService().registerSystemGestureExclusionListener(listenerEx.getISystemGestureExclusionListener(), displayId);
    }

    public static void unregisterSystemGestureExclusionListener(ISystemGestureExclusionListenerEx listenerEx, int displayId) throws RemoteException {
        WindowManagerGlobal.getWindowManagerService().unregisterSystemGestureExclusionListener(listenerEx.getISystemGestureExclusionListener(), displayId);
    }

    public static void updateTapExcludeRegion(IWindowEx windowEx, int regionId, Region region) throws RemoteException {
        WindowManagerGlobal.getWindowSession().updateTapExcludeRegion(windowEx.getIWindow(), regionId, region);
    }
}
