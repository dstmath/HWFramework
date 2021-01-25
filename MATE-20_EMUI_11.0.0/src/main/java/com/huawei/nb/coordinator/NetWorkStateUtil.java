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

    private static int getNetworkClass(int i) {
        switch (i) {
            case 1:
            case 2:
            case 4:
            case 7:
            case Attribute.BYTE /* 11 */:
                return 1;
            case 3:
            case 5:
            case 6:
            case 8:
            case Attribute.DATE /* 9 */:
            case Attribute.TIME /* 10 */:
            case Attribute.CALENDAR /* 12 */:
            case Attribute.CHARACTER /* 14 */:
            case 15:
                return 2;
            case Attribute.TIMESTAMP /* 13 */:
                return 3;
            default:
                return 0;
        }
    }

    private static boolean isContain2GFlag(long j) {
        return (j & 4) != 0;
    }

    private static boolean isContain3GFlag(long j) {
        return (j & 2) != 0;
    }

    private static boolean isContainWifiFlag(long j) {
        return (j & 1) != 0;
    }

    private NetWorkStateUtil() {
    }

    public static int getCurrentNetWorkType(Context context) {
        return convertType(getActiveNetworkInfo(context));
    }

    public static NetworkInfo getActiveNetworkInfo(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        if (connectivityManager != null) {
            return connectivityManager.getActiveNetworkInfo();
        }
        return null;
    }

    public static int convertType(NetworkInfo networkInfo) {
        if (networkInfo != null && networkInfo.isConnected()) {
            int type = networkInfo.getType();
            if (type != 1) {
                if (type == 0) {
                    int networkClass = getNetworkClass(networkInfo.getSubtype());
                    return (networkClass == 0 || networkClass == 1 || !(networkClass == 2 || networkClass == 3)) ? 2 : 3;
                } else if (type != 9) {
                    if (type == 6) {
                        return 3;
                    }
                }
            }
            return 1;
        }
        return 0;
    }

    public static boolean isAllowAction(Context context, long j) {
        int currentNetWorkType = getCurrentNetWorkType(context);
        if (currentNetWorkType != 0) {
            if (currentNetWorkType != 1) {
                if (currentNetWorkType != 2) {
                    if (currentNetWorkType == 3 && isContain3GFlag(j)) {
                        return true;
                    }
                } else if (isContain2GFlag(j)) {
                    return true;
                }
            } else if (isContainWifiFlag(j)) {
                return true;
            }
        }
        return false;
    }
}
