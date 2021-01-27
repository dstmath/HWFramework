package com.huawei.android.telecom;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telecom.ITelecomService;

public class TelecomManagerEx {
    public static final String EXTRA_CALL_TECHNOLOGY_TYPE = "android.telecom.extra.CALL_TECHNOLOGY_TYPE";
    public static final int TTY_MODE_OFF = 0;

    public static int getActiveSubscription() {
        try {
            if (getTelecomService() != null) {
                return getTelecomService().getActiveSubscription();
            }
            return -1;
        } catch (RemoteException e) {
            Log.d("TelecomManagerEx", "getActiveSubscription is error");
            return -1;
        }
    }

    private static ITelecomService getTelecomService() {
        return ITelecomService.Stub.asInterface(ServiceManager.getService("telecom"));
    }

    public static boolean setDefaultDialer(TelecomManager tManager, String packageName) {
        if (tManager == null || TextUtils.isEmpty(packageName)) {
            return false;
        }
        return tManager.setDefaultDialer(packageName);
    }

    public static int getAllPhoneAccountsCount(TelecomManager tManager) {
        if (tManager != null) {
            return tManager.getAllPhoneAccountsCount();
        }
        return 0;
    }

    public static boolean isRinging(TelecomManager telecomManager) {
        return telecomManager != null && telecomManager.isRinging();
    }

    public static void setUserSelectedOutgoingPhoneAccount(TelecomManager tManager, PhoneAccountHandle accountHandle) {
        if (tManager != null) {
            tManager.setUserSelectedOutgoingPhoneAccount(accountHandle);
        }
    }
}
