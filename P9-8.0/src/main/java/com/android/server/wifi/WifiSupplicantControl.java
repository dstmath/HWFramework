package com.android.server.wifi;

import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.LocalLog;
import android.util.Log;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class WifiSupplicantControl extends AbsWifiSupplicantControl {
    protected static final boolean HWFLOW;
    private static boolean HWLOGW_E = true;
    public static final String ID_STRING_KEY_CONFIG_KEY = "configKey";
    public static final String ID_STRING_KEY_CREATOR_UID = "creatorUid";
    public static final String ID_STRING_KEY_FQDN = "fqdn";
    public static final String ID_STRING_VAR_NAME = "id_str";
    public static final int STORED_VALUE_FOR_REQUIRE_PMF = 2;
    public static final String SUPPLICANT_CONFIG_FILE = "/data/misc/wifi/wpa_supplicant.conf";
    public static final String SUPPLICANT_CONFIG_FILE_BACKUP = "/data/misc/wifi/wpa_supplicant.conf.tmp";
    private static final String TAG = "WifiSupplicantControl";
    private static boolean VDBG = HWFLOW;
    private static boolean VVDBG = HWFLOW;
    private int mFrameworkNetworkId = -1;
    private final LocalLog mLocalLog;
    private int mSupplicantNetworkId = -1;
    private boolean mSystemSupportsFastBssTransition = false;
    private final TelephonyManager mTelephonyManager;
    private boolean mVerboseLoggingEnabled = false;
    private final WifiNative mWifiNative;

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        HWFLOW = isLoggable;
    }

    WifiSupplicantControl(TelephonyManager telephonyManager, WifiNative wifiNative, LocalLog localLog) {
        this.mTelephonyManager = telephonyManager;
        this.mWifiNative = wifiNative;
        this.mLocalLog = localLog;
    }

    private static String makeString(BitSet set, String[] strings) {
        return makeStringWithException(set, strings, null);
    }

    private static String makeStringWithException(BitSet set, String[] strings, String exception) {
        StringBuilder result = new StringBuilder();
        BitSet trimmedSet = set.get(0, strings.length);
        List<String> valueSet = new ArrayList();
        for (int bit = trimmedSet.nextSetBit(0); bit >= 0; bit = trimmedSet.nextSetBit(bit + 1)) {
            String currentName = strings[bit];
            if (exception == null || !currentName.equals(exception)) {
                valueSet.add(currentName.replace('_', '-'));
            } else {
                valueSet.add(currentName);
            }
        }
        return TextUtils.join(" ", valueSet);
    }
}
