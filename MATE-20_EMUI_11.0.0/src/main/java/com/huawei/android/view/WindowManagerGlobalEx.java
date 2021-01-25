package com.huawei.android.view;

import android.graphics.Point;
import android.os.RemoteException;
import android.view.WindowManagerGlobal;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class WindowManagerGlobalEx {
    public static void getInitialDisplaySize(int displayId, Point size) throws RemoteException {
        WindowManagerGlobal.getWindowManagerService().getInitialDisplaySize(displayId, size);
    }
}
