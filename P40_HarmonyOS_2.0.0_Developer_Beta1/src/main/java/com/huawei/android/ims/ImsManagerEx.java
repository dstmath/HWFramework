package com.huawei.android.ims;

import android.content.Context;
import com.android.ims.ImsManager;

public class ImsManagerEx {
    public static boolean isServiceAvailable(Context ctx, int sub) {
        ImsManager imsManager = ImsManager.getInstance(ctx, sub);
        if (imsManager != null) {
            return imsManager.isServiceAvailable();
        }
        return false;
    }

    public static int getRegistrationTech(Context ctx, int sub) {
        ImsManager imsManager = ImsManager.getInstance(ctx, sub);
        if (imsManager != null) {
            return imsManager.getRegistrationTech();
        }
        return -1;
    }
}
