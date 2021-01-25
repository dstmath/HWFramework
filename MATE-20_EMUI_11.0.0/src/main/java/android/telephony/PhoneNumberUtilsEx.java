package android.telephony;

import android.content.Context;

public class PhoneNumberUtilsEx {
    public static boolean isLocalEmergencyNumber(Context context, int subId, String number) {
        return PhoneNumberUtils.isLocalEmergencyNumber(context, subId, number);
    }
}
