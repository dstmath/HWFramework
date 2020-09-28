package com.huawei.android.app;

import android.os.RemoteException;
import android.util.Log;
import com.huawei.android.view.HwWindowManager;

public class WindowManagerExt {
    private static final String TAG = "WindowManagerEx";

    public static void updateFocusWindowFreezed(Boolean isGainFocus) {
        if (HwWindowManager.getService() != null) {
            try {
                HwWindowManager.getService().updateFocusWindowFreezed(isGainFocus.booleanValue());
            } catch (RemoteException e) {
                Log.e(TAG, "updateFocusWindowFreezed RemoteException");
            }
        }
    }
}
