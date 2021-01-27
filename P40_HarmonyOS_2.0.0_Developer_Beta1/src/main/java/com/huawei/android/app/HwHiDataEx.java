package com.huawei.android.app;

import android.os.RemoteException;
import android.util.Log;
import com.huawei.android.gameassist.HwHiNetworkManager;
import com.huawei.android.gameassist.IHiNetworkManager;

public class HwHiDataEx {
    private static final String TAG = "HiData_HwHiDataEx";

    public static int onOpenAccelerateResult(String acceletrateResult) {
        Log.w(TAG, "enter onOpenAccelerateResult ");
        IHiNetworkManager hiNetwork = HwHiNetworkManager.getService();
        if (hiNetwork == null) {
            return -1;
        }
        try {
            return hiNetwork.onOpenAccelerateResult(acceletrateResult);
        } catch (RemoteException e) {
            Log.w(TAG, "onOpenAccelerateResult RemoteException");
            return -1;
        }
    }

    public static int onDetectTimeDelayResult(String timeDelayResult) {
        Log.w(TAG, "enter onDetectTimeDelayResult ");
        IHiNetworkManager hiNetwork = HwHiNetworkManager.getService();
        if (hiNetwork == null) {
            return -1;
        }
        try {
            return hiNetwork.onDetectTimeDelayResult(timeDelayResult);
        } catch (RemoteException e) {
            Log.w(TAG, "onDetectTimeDelayResult RemoteException");
            return -1;
        }
    }
}
