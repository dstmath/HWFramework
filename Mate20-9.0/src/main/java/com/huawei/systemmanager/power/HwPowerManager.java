package com.huawei.systemmanager.power;

import android.app.ActivityManager;
import android.app.HwActivitySplitterImpl;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.RemoteException;
import android.view.HwApsImpl;
import com.android.internal.view.RotationPolicy;

public class HwPowerManager {
    private static final int ERRORRET = -1;
    public static final String EXTRA_JUMPED_ACTIVITY = "huawei.intent.extra.JUMPED_ACTIVITY";

    public static void stopOtherUserInSuperMode() throws RemoteException {
        int[] runningUsers = ActivityManager.getService().getRunningUserIds();
        for (int userId : runningUsers) {
            if (userId != 0) {
                ActivityManager.getService().stopUser(userId, true, null);
            }
        }
    }

    public static int getMinimumScreenBrightnessSetting(PowerManager powerManager) {
        if (powerManager != null) {
            return powerManager.getMinimumScreenBrightnessSetting();
        }
        return -1;
    }

    public static void setNotSplit(Intent intent) {
        HwActivitySplitterImpl.setNotSplit(intent);
    }

    public static void setRotationLock(Context context, Boolean flag) {
        if (context != null) {
            RotationPolicy.setRotationLock(context, flag.booleanValue());
        }
    }

    public static void setLowResolutionMode(Context context, boolean enableLowResolutionMode) {
        if (context != null) {
            HwApsImpl.getDefault().setLowResolutionMode(context, enableLowResolutionMode);
        }
    }
}
