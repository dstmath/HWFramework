package com.huawei.nb.coordinator;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.huawei.odmf.model.api.Attribute;

public class NetWorkStateUtil {
    private static final int NETWORK_CLASS_2_G = 1;
    private static final int NETWORK_CLASS_3_G = 2;
    private static final int NETWORK_CLASS_4_G = 3;
    public static final int TYPE_2G = 2;
    public static final int TYPE_3G4G = 3;
    public static final int TYPE_UNKNOWN = 0;
    public static final int TYPE_WIFI = 1;
    public static final long UPLOAD_ON_2G_FLAGS = 4;
    public static final long UPLOAD_ON_3G_FLAGS = 2;
    public static final long UPLOAD_ON_WIFI_FLAGS = 1;

    public static int getCurrentNetWorkType(Context context) {
        return convertType(getActiveNetworkInfo(context));
    }

    public static NetworkInfo getActiveNetworkInfo(Context context) {
        ConnectivityManager mConnMgr = (ConnectivityManager) context.getSystemService("connectivity");
        if (mConnMgr != null) {
            return mConnMgr.getActiveNetworkInfo();
        }
        return null;
    }

    public static int convertType(NetworkInfo netInfo) {
        if (netInfo == null || !netInfo.isConnected()) {
            return 0;
        }
        int netType = netInfo.getType();
        if (1 == netType) {
            return 1;
        }
        if (netType == 0) {
            switch (getNetworkClass(netInfo.getSubtype())) {
                case 0:
                case 1:
                    return 2;
                case 2:
                case 3:
                    return 3;
                default:
                    return 2;
            }
        } else if (9 == netType) {
            return 1;
        } else {
            if (6 == netType) {
                return 3;
            }
            return 0;
        }
    }

    private static int getNetworkClass(int networkType) {
        switch (networkType) {
            case 1:
            case 2:
            case 4:
            case 7:
            case Attribute.BYTE /*11*/:
                return 1;
            case 3:
            case 5:
            case 6:
            case 8:
            case Attribute.DATE /*9*/:
            case Attribute.TIME /*10*/:
            case Attribute.CALENDAR /*12*/:
            case Attribute.CHARACTER /*14*/:
            case 15:
                return 2;
            case Attribute.TIMESTAMP /*13*/:
                return 3;
            default:
                return 0;
        }
    }

    public static boolean isAllowAction(Context context, long flags) {
        switch (getCurrentNetWorkType(context)) {
            case 1:
                if (isContainWifiFlag(flags)) {
                    return true;
                }
                return false;
            case 2:
                if (isContain2GFlag(flags)) {
                    return true;
                }
                return false;
            case 3:
                if (isContain3GFlag(flags)) {
                    return true;
                }
                return false;
            default:
                return false;
        }
    }

    private static boolean isContainWifiFlag(long flag) {
        if ((1 & flag) != 0) {
            return true;
        }
        return false;
    }

    private static boolean isContain3GFlag(long flag) {
        if ((2 & flag) != 0) {
            return true;
        }
        return false;
    }

    private static boolean isContain2GFlag(long flag) {
        if ((4 & flag) != 0) {
            return true;
        }
        return false;
    }
}
