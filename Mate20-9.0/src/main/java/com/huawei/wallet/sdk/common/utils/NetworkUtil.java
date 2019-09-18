package com.huawei.wallet.sdk.common.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.net.ssl.SSLSocket;

public final class NetworkUtil {
    private static final int ANDROID_M_CODE = 23;
    public static final int NETWORK_2G = 2;
    public static final int NETWORK_3G = 3;
    public static final int NETWORK_4G = 4;
    public static final int NETWORK_NO = -1;
    private static final int NETWORK_TYPE_GSM = 16;
    private static final int NETWORK_TYPE_IWLAN = 18;
    private static final int NETWORK_TYPE_TD_SCDMA = 17;
    public static final int NETWORK_UNKNOWN = 5;
    public static final int NETWORK_WIFI = 1;
    private static final String[] UN_SAFE_ALGORITHMS = {"TEA", "SHA0", "MD2", "MD4", "RIPEMD", "aNULL", "eNULL", "RC4", "DES", "DESX", "DES40", "RC2", "MD5", "ANON", "NULL", "TLS_EMPTY_RENEGOTIATION_INFO_SCSV"};

    private NetworkUtil() {
    }

    public static boolean isNetworkConnected(Context context) {
        NetworkInfo networkInfo = getActiveNetworkInfo(context);
        if (networkInfo == null || !networkInfo.isConnected()) {
            return false;
        }
        return true;
    }

    private static NetworkInfo getActiveNetworkInfo(Context context) {
        if (context == null) {
            return null;
        }
        if (Build.VERSION.SDK_INT < 23 || context.checkSelfPermission("android.permission.ACCESS_NETWORK_STATE") == 0) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService("connectivity");
            if (cm != null) {
                return cm.getActiveNetworkInfo();
            }
        }
        return null;
    }

    public static int getNetWorkType(Context context) {
        NetworkInfo info = getActiveNetworkInfo(context);
        if (info == null || !info.isAvailable()) {
            return -1;
        }
        int netType = info.getType();
        if (netType == 1) {
            return 1;
        }
        if (netType != 0) {
            return 5;
        }
        switch (info.getSubtype()) {
            case 1:
            case 2:
            case 4:
            case 7:
            case 11:
            case 16:
                return 2;
            case 3:
            case 5:
            case 6:
            case 8:
            case 9:
            case 10:
            case 12:
            case 14:
            case 15:
            case 17:
                return 3;
            case 13:
            case 18:
                return 4;
            default:
                String subtypeName = info.getSubtypeName();
                if (subtypeName == null) {
                    return 5;
                }
                if (subtypeName.equalsIgnoreCase("TD-SCDMA") || subtypeName.equalsIgnoreCase("WCDMA") || subtypeName.equalsIgnoreCase("CDMA2000")) {
                    return 3;
                }
                return 5;
        }
    }

    public static String getNetWorkTypeName(Context context) {
        int netWorkType = getNetWorkType(context);
        if (netWorkType == -1) {
            return "no";
        }
        switch (netWorkType) {
            case 1:
                return "wifi";
            case 2:
                return "gprs";
            case 3:
                return "3g";
            case 4:
                return "4g";
            default:
                return "unknown";
        }
    }

    public static void setEnableSafeCipherSuites(SSLSocket sslsock) {
        if (sslsock != null) {
            String[] ENABLED_CIPHERS = sslsock.getEnabledCipherSuites();
            List<String> ENABLED_CIPHERS_List = new ArrayList<>();
            Object obj = "";
            for (String string : ENABLED_CIPHERS) {
                boolean isSafeAlgorithm = true;
                String upperCaseStr = string.toUpperCase(Locale.ENGLISH);
                String[] strArr = UN_SAFE_ALGORITHMS;
                int length = strArr.length;
                int i = 0;
                while (true) {
                    if (i >= length) {
                        break;
                    } else if (upperCaseStr.contains(strArr[i])) {
                        isSafeAlgorithm = false;
                        break;
                    } else {
                        i++;
                    }
                }
                if (isSafeAlgorithm) {
                    ENABLED_CIPHERS_List.add(string);
                }
            }
            sslsock.setEnabledCipherSuites((String[]) ENABLED_CIPHERS_List.toArray(new String[ENABLED_CIPHERS_List.size()]));
        }
    }
}
