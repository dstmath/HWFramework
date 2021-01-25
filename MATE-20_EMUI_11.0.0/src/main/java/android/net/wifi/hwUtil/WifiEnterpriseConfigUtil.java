package android.net.wifi.hwUtil;

import android.util.Log;

public class WifiEnterpriseConfigUtil {
    private static final String TAG = "WifiEnterpriseConfigUtil";

    public static boolean isEapMethodValid(int subId, int eapMethod) {
        if (!isSubIdValid(subId)) {
            throw new IllegalArgumentException("Unknown subId");
        } else if (eapMethod == 4 || eapMethod == 5 || eapMethod == 6) {
            Log.d(TAG, "setEapSubId: Eap Method is " + eapMethod + ", subId is " + subId);
            return true;
        } else {
            Log.e(TAG, "Not applicable EAP method for multi cards");
            return false;
        }
    }

    private static boolean isSubIdValid(int subId) {
        if (subId == Integer.MAX_VALUE || subId == 0 || subId == 1) {
            return true;
        }
        return false;
    }
}
