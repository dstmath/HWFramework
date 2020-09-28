package com.huawei.android.net;

import android.content.Context;
import android.net.IConnectivityManager;
import android.net.INetworkStatsService;
import android.net.LinkProperties;
import android.net.NetworkState;
import android.net.TrafficStats;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Slog;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class HwTrafficStats {
    private static final String TAG = "HwTrafficStats";
    private static final int UNSUPPORTED = -1;
    private static volatile IConnectivityManager sConnectivityManager;
    private static volatile INetworkStatsService sStatsService;

    private HwTrafficStats() {
    }

    private static INetworkStatsService getStatsService() {
        if (sStatsService == null) {
            synchronized (HwTrafficStats.class) {
                if (sStatsService == null) {
                    sStatsService = INetworkStatsService.Stub.asInterface(ServiceManager.getService(Context.NETWORK_STATS_SERVICE));
                }
            }
        }
        return sStatsService;
    }

    private static IConnectivityManager getConnectivityManagerService() {
        if (sConnectivityManager == null) {
            synchronized (HwTrafficStats.class) {
                if (sConnectivityManager == null) {
                    sConnectivityManager = IConnectivityManager.Stub.asInterface(ServiceManager.getService("connectivity"));
                }
            }
        }
        return sConnectivityManager;
    }

    private static long addIfSupported(long stat) {
        if (stat == -1) {
            return 0;
        }
        return stat;
    }

    public static HashMap<String, Long> getMobileTxBytes() {
        return getMobileTxByteInterval();
    }

    public static HashMap<String, Long> getMobileRxBytes() {
        return getMobileRxByteInterval();
    }

    private static HashMap<String, Long> getMobileTxByteInterval() {
        long start = System.currentTimeMillis();
        NetworkState[] networkStates = getNetworkStates();
        if (networkStates == null) {
            Slog.i(TAG, "networkStates is null, so return.");
            return new HashMap<>(0);
        }
        Slog.i(TAG, "network states size = " + networkStates.length);
        HashMap<String, Long> txBytesMap = new HashMap<>(networkStates.length);
        for (NetworkState networkState : networkStates) {
            if (networkState != null) {
                LinkProperties linkProperties = networkState.linkProperties;
                String subscriberId = networkState.subscriberId;
                if (!(subscriberId == null || linkProperties == null)) {
                    long total = addIfSupported(TrafficStats.getTxBytes(linkProperties.getInterfaceName()));
                    if (txBytesMap.containsKey(subscriberId)) {
                        txBytesMap.put(subscriberId, 0L);
                    } else {
                        txBytesMap.put(subscriberId, Long.valueOf(total));
                    }
                }
            }
        }
        Iterator<Map.Entry<String, Long>> iterator = txBytesMap.entrySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getValue().longValue() == 0) {
                Slog.i(TAG, "multiple active networks.");
                iterator.remove();
            }
        }
        long totalEnd = System.currentTimeMillis();
        Slog.i(TAG, "method call cost = " + (totalEnd - start));
        return txBytesMap;
    }

    private static HashMap<String, Long> getMobileRxByteInterval() {
        long start = System.currentTimeMillis();
        NetworkState[] networkStates = getNetworkStates();
        if (networkStates == null) {
            return new HashMap<>(0);
        }
        Slog.i(TAG, "network states size = " + networkStates.length);
        HashMap<String, Long> rxBytesMap = new HashMap<>(networkStates.length);
        for (NetworkState networkState : networkStates) {
            if (networkState != null) {
                LinkProperties linkProperties = networkState.linkProperties;
                String subscriberId = networkState.subscriberId;
                if (!(subscriberId == null || linkProperties == null)) {
                    long total = addIfSupported(TrafficStats.getRxBytes(linkProperties.getInterfaceName()));
                    if (rxBytesMap.containsKey(subscriberId)) {
                        rxBytesMap.put(subscriberId, 0L);
                    } else {
                        rxBytesMap.put(subscriberId, Long.valueOf(total));
                    }
                }
            }
        }
        Iterator<Map.Entry<String, Long>> iterator = rxBytesMap.entrySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getValue().longValue() == 0) {
                Slog.i(TAG, "multiple active networks.");
                iterator.remove();
            }
        }
        long totalEnd = System.currentTimeMillis();
        Slog.i(TAG, "method call cost = " + (totalEnd - start));
        return rxBytesMap;
    }

    private static NetworkState[] getNetworkStates() {
        try {
            return getConnectivityManagerService().getAllNetworkState();
        } catch (RemoteException e) {
            Slog.e(TAG, "Can not get network state.");
            return null;
        }
    }
}
