package com.huawei.internal.telephony;

import android.content.Context;
import com.android.internal.telephony.TelephonyPermissions;

public class TelephonyPermissionsExt {
    public static boolean checkCallingOrSelfReadDeviceIdentifiers(Context context, int subId, String callingPackage, String message) {
        return TelephonyPermissions.checkCallingOrSelfReadDeviceIdentifiers(context, subId, callingPackage, message);
    }

    public static boolean checkCallingOrSelfReadPhoneState(Context context, int subId, String callingPackage, String message) {
        return TelephonyPermissions.checkCallingOrSelfReadPhoneState(context, subId, callingPackage, message);
    }
}
