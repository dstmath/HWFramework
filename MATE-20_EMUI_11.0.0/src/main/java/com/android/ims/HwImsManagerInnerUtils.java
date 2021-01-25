package com.android.ims;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.telephony.ims.aidl.IImsMmTelFeature;
import com.huawei.android.telephony.RlogEx;
import com.huawei.annotation.HwSystemApi;
import com.huawei.ims.ImsManagerExt;
import com.huawei.ims.MmTelFeatureConnectionEx;
import com.huawei.internal.telephony.PhoneConstantsEx;

@HwSystemApi
public class HwImsManagerInnerUtils {
    private static final String TAG = "HwImsManagerInner";

    public static MmTelFeatureConnectionEx getServiceProxy(ImsManagerExt imsManager, Context context, int subId) {
        MmTelFeatureConnectionEx serviceProxy = new MmTelFeatureConnectionEx(context, subId);
        TelephonyManager tm = (TelephonyManager) context.getSystemService(PhoneConstantsEx.PHONE_KEY);
        if (tm == null) {
            log("create: TelephonyManager is null!");
            return serviceProxy;
        }
        IImsMmTelFeature binder = tm.getImsMmTelFeatureAndListen(subId, serviceProxy.getListener());
        if (binder != null) {
            serviceProxy.setBinder(binder.asBinder());
            serviceProxy.getFeatureState();
        } else {
            log("create: binder is null! Slot Id: " + subId);
        }
        return serviceProxy;
    }

    private static void log(String s) {
        RlogEx.d(TAG, s);
    }
}
