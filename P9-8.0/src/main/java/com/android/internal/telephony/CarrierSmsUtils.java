package com.android.internal.telephony;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Binder;
import android.os.PersistableBundle;
import android.telephony.CarrierConfigManager;
import android.telephony.Rlog;

public class CarrierSmsUtils {
    private static final String CARRIER_IMS_PACKAGE_KEY = "config_ims_package_override_string";
    protected static final String TAG = CarrierSmsUtils.class.getSimpleName();
    protected static final boolean VDBG = false;

    public static String getCarrierImsPackageForIntent(Context context, Phone phone, Intent intent) {
        String carrierImsPackage = getCarrierImsPackage(context, phone);
        if (carrierImsPackage == null) {
            return null;
        }
        for (ResolveInfo info : context.getPackageManager().queryIntentServices(intent, 0)) {
            if (info.serviceInfo == null) {
                Rlog.e(TAG, "Can't get service information from " + info);
            } else if (carrierImsPackage.equals(info.serviceInfo.packageName)) {
                return carrierImsPackage;
            }
        }
        return null;
    }

    private static String getCarrierImsPackage(Context context, Phone phone) {
        CarrierConfigManager cm = (CarrierConfigManager) context.getSystemService("carrier_config");
        if (cm == null) {
            Rlog.e(TAG, "Failed to retrieve CarrierConfigManager");
            return null;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            PersistableBundle config = cm.getConfigForSubId(phone.getSubId());
            if (config == null) {
                return null;
            }
            String string = config.getString(CARRIER_IMS_PACKAGE_KEY, null);
            Binder.restoreCallingIdentity(identity);
            return string;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private CarrierSmsUtils() {
    }
}
