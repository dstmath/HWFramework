package com.huawei.net;

import android.net.NetworkInfo;

public class NetworkInfoEx {
    private NetworkInfoEx() {
    }

    public static NetworkInfo makeNetworkInfo(int type, int subtype, String typeName, String subtypeName) {
        return new NetworkInfo(type, subtype, typeName, subtypeName);
    }

    public static NetworkInfo makeNetworkInfo(NetworkInfo source) {
        return new NetworkInfo(source);
    }

    public static void setIsAvailable(NetworkInfo ni, boolean isAvailable) {
        if (ni != null) {
            ni.setIsAvailable(isAvailable);
        }
    }

    public static void setDetailedState(NetworkInfo ni, NetworkInfo.DetailedState detailedState, String reason, String extraInfo) {
        if (ni != null) {
            ni.setDetailedState(detailedState, reason, extraInfo);
        }
    }
}
