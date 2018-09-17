package com.huawei.internal.telephony;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.android.internal.telephony.ISms;
import com.android.internal.telephony.ISms.Stub;

public class ISmsEx {
    private static final String TAG = "ISmsEx";

    private static ISms getISmsServiceOrThrow() {
        ISms iccISms = Stub.asInterface(ServiceManager.getService("isms"));
        if (iccISms != null) {
            return iccISms;
        }
        throw new UnsupportedOperationException("Sms is not supported");
    }

    public static void setSingleShiftTable(int[] temp) {
        try {
            getISmsServiceOrThrow().setEnabledSingleShiftTables(temp);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException setEnabledSingleShiftTables");
        }
    }

    public static void setSmsCodingNationalCode(String code) {
        try {
            getISmsServiceOrThrow().setSmsCodingNationalCode(code);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException setSmsCodingNationalCode");
        }
    }
}
