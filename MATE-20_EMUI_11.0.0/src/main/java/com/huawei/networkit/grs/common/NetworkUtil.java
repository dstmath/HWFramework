package com.huawei.networkit.grs.common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.LinkedList;

public class NetworkUtil {
    private static final String TAG = NetworkUtil.class.getSimpleName();
    private static final int TYPE_WIFI_P2P = 13;

    public static final class NetType {
        public static final int TYPE_2G = 2;
        public static final int TYPE_3G = 3;
        public static final int TYPE_4G = 4;
        public static final int TYPE_NO_NETWORK = -1;
        public static final int TYPE_UNKNOWN = 0;
        public static final int TYPE_WIFI = 1;
    }

    public static NetworkInfo getNetworkInfo(Context context) {
        ConnectivityManager manager;
        if (context == null || (manager = (ConnectivityManager) context.getSystemService("connectivity")) == null) {
            return null;
        }
        try {
            return manager.getActiveNetworkInfo();
        } catch (SecurityException e) {
            return null;
        }
    }

    public static int getNetworkType(Context context) {
        if (context != null) {
            return getNetworkType(getNetworkInfo(context));
        }
        return 0;
    }

    public static int getNetworkType(NetworkInfo netInfo) {
        int psType;
        if (netInfo == null || !netInfo.isConnected()) {
            return -1;
        }
        int type = netInfo.getType();
        if (1 == type || 13 == type) {
            return 1;
        }
        if (type != 0) {
            return 0;
        }
        int subtype = netInfo.getSubtype();
        switch (subtype) {
            case 1:
            case 2:
            case 4:
            case 7:
            case 11:
                psType = 2;
                break;
            case 3:
            case 5:
            case 6:
            case 8:
            case 9:
            case 10:
            case 12:
            case 14:
            case 15:
                psType = 3;
                break;
            case 13:
                psType = 4;
                break;
            default:
                psType = 0;
                break;
        }
        if (psType != 0 || Build.VERSION.SDK_INT < 25) {
            return psType;
        }
        if (subtype == 16) {
            return 2;
        }
        if (subtype != 17) {
            return 0;
        }
        return 3;
    }

    private static LinkedList<String> getDnsServerIpsFromLinkProperties(ConnectivityManager connectivityManager, NetworkInfo activeNetworkInfo) {
        LinkProperties linkProperties;
        LinkedList<String> dnsServers = new LinkedList<>();
        Network[] networks = connectivityManager.getAllNetworks();
        if (networks == null || networks.length == 0) {
            return dnsServers;
        }
        for (Network network : networks) {
            NetworkInfo networkInfo = connectivityManager.getNetworkInfo(network);
            if (!(networkInfo == null || networkInfo.getType() != activeNetworkInfo.getType() || (linkProperties = connectivityManager.getLinkProperties(network)) == null)) {
                for (InetAddress addr : linkProperties.getDnsServers()) {
                    dnsServers.add(addr.getHostAddress());
                }
            }
        }
        return dnsServers;
    }

    private static String[] getDnsServerIpsFromConnectionManager(Context context) {
        ConnectivityManager connectivityManager;
        LinkedList<String> dnsServers = new LinkedList<>();
        if (!(Build.VERSION.SDK_INT < 21 || context == null || (connectivityManager = (ConnectivityManager) context.getSystemService("connectivity")) == null)) {
            try {
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                if (activeNetworkInfo != null) {
                    dnsServers = getDnsServerIpsFromLinkProperties(connectivityManager, activeNetworkInfo);
                }
            } catch (SecurityException e) {
                String str = TAG;
                Logger.i(str, "getActiveNetworkInfo failed, exception:" + e.getClass().getSimpleName());
            }
        }
        return dnsServers.isEmpty() ? new String[0] : (String[]) dnsServers.toArray(new String[dnsServers.size()]);
    }

    public static String getDnsServerIps(Context context) {
        return Arrays.toString(getDnsServerIpsFromConnectionManager(context));
    }
}
