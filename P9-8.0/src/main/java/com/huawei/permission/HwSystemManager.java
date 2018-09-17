package com.huawei.permission;

import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.hsm.permission.StubController;

public class HwSystemManager {
    private static final String TAG = "HwSystemManagerEx";

    public static Bundle callHsmService(String method, Bundle params) {
        try {
            IHoldService service = StubController.getHoldService();
            if (service != null) {
                return service.callHsmService(method, params);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "failed to callHsmService");
        }
        return null;
    }
}
