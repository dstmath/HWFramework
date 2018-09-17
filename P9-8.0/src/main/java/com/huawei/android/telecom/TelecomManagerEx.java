package com.huawei.android.telecom;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.telecom.TelecomManager;
import com.android.internal.telecom.ITelecomService;
import com.android.internal.telecom.ITelecomService.Stub;

public class TelecomManagerEx {
    public static final String EXTRA_CALL_TECHNOLOGY_TYPE = "android.telecom.extra.CALL_TECHNOLOGY_TYPE";
    public static final int TTY_MODE_OFF = 0;
    private static final ITelecomService mTelecomServiceOverride = null;

    public static int getActiveSubscription() {
        try {
            if (getTelecomService() != null) {
                return getTelecomService().getActiveSubscription();
            }
        } catch (RemoteException e) {
        }
        return -1;
    }

    private static ITelecomService getTelecomService() {
        if (mTelecomServiceOverride != null) {
            return mTelecomServiceOverride;
        }
        return Stub.asInterface(ServiceManager.getService("telecom"));
    }

    public static boolean setDefaultDialer(TelecomManager tManager, String packageName) {
        return tManager.setDefaultDialer(packageName);
    }

    public static int getAllPhoneAccountsCount(TelecomManager tManager) {
        return tManager.getAllPhoneAccountsCount();
    }

    public static boolean isRinging(TelecomManager telecomManager) {
        return telecomManager != null ? telecomManager.isRinging() : false;
    }
}
