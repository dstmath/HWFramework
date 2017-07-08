package com.android.server.wifi;

import android.content.Context;
import android.net.INetworkScoreCache.Stub;
import android.net.ScoredNetwork;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class WifiNetworkScoreCache extends Stub {
    private static final boolean DBG = false;
    public static final int INVALID_NETWORK_SCORE = -128;
    private static final String TAG = "WifiNetworkScoreCache";
    private final Context mContext;
    private final Map<String, ScoredNetwork> mNetworkCache;

    public WifiNetworkScoreCache(Context context) {
        this.mContext = context;
        this.mNetworkCache = new HashMap();
    }

    public final void updateScores(List<ScoredNetwork> networks) {
        if (networks != null) {
            Log.e(TAG, "updateScores list size=" + networks.size());
            synchronized (this.mNetworkCache) {
                for (ScoredNetwork network : networks) {
                    String networkKey = buildNetworkKey(network);
                    if (networkKey != null) {
                        this.mNetworkCache.put(networkKey, network);
                    }
                }
            }
        }
    }

    public final void clearScores() {
        synchronized (this.mNetworkCache) {
            this.mNetworkCache.clear();
        }
    }

    public boolean isScoredNetwork(ScanResult result) {
        return getScoredNetwork(result) != null ? true : DBG;
    }

    public boolean hasScoreCurve(ScanResult result) {
        ScoredNetwork network = getScoredNetwork(result);
        if (network == null || network.rssiCurve == null) {
            return DBG;
        }
        return true;
    }

    public int getNetworkScore(ScanResult result) {
        ScoredNetwork network = getScoredNetwork(result);
        if (network == null || network.rssiCurve == null) {
            return INVALID_NETWORK_SCORE;
        }
        return network.rssiCurve.lookupScore(result.level);
    }

    public boolean getMeteredHint(ScanResult result) {
        ScoredNetwork network = getScoredNetwork(result);
        return network != null ? network.meteredHint : DBG;
    }

    public int getNetworkScore(ScanResult result, boolean isActiveNetwork) {
        ScoredNetwork network = getScoredNetwork(result);
        if (network == null || network.rssiCurve == null) {
            return INVALID_NETWORK_SCORE;
        }
        return network.rssiCurve.lookupScore(result.level, isActiveNetwork);
    }

    private ScoredNetwork getScoredNetwork(ScanResult result) {
        String key = buildNetworkKey(result);
        if (key == null) {
            return null;
        }
        ScoredNetwork network;
        synchronized (this.mNetworkCache) {
            network = (ScoredNetwork) this.mNetworkCache.get(key);
        }
        return network;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private String buildNetworkKey(ScoredNetwork network) {
        if (network == null || network.networkKey == null || network.networkKey.wifiKey == null || network.networkKey.type != 1) {
            return null;
        }
        String key = network.networkKey.wifiKey.ssid;
        if (key == null) {
            return null;
        }
        if (network.networkKey.wifiKey.bssid != null) {
            key = key + network.networkKey.wifiKey.bssid;
        }
        return key;
    }

    private String buildNetworkKey(ScanResult result) {
        if (result == null || result.SSID == null) {
            return null;
        }
        StringBuilder key = new StringBuilder("\"");
        key.append(result.SSID);
        key.append("\"");
        if (result.BSSID != null) {
            key.append(result.BSSID);
        }
        return key.toString();
    }

    protected final void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.DUMP", TAG);
        writer.println(TAG);
        writer.println("  All score curves:");
        for (Entry<String, ScoredNetwork> entry : this.mNetworkCache.entrySet()) {
            ScoredNetwork scoredNetwork = (ScoredNetwork) entry.getValue();
            writer.println("    " + ((String) entry.getKey()) + ": " + scoredNetwork.rssiCurve + ", meteredHint=" + scoredNetwork.meteredHint);
        }
        writer.println("  Current network scores:");
        for (ScanResult scanResult : ((WifiManager) this.mContext.getSystemService("wifi")).getScanResults()) {
            writer.println("    " + buildNetworkKey(scanResult) + ": " + getNetworkScore(scanResult));
        }
    }
}
