package com.huawei.ace.runtime;

import android.app.ActivityManager;
import android.app.IActivityManager;
import android.os.RemoteException;

public class VipThreadUtility {
    private static final int MODE_ADD = 0;
    private static final int MODE_SET = 1;
    private static final String TAG = "VipThreadUtility";

    public static boolean setHmThreadToRtg(int i, int i2) {
        if (i <= 0) {
            return false;
        }
        if (i2 == 1) {
            try {
                IActivityManager service = ActivityManager.getService();
                service.setHmThreadToRtg("mode:set;tids:" + i);
            } catch (RemoteException unused) {
                ALog.e(TAG, "setHmThreadToRtg failed");
                return false;
            }
        } else {
            IActivityManager service2 = ActivityManager.getService();
            service2.setHmThreadToRtg("mode:add;tids:" + i);
        }
        return true;
    }
}
