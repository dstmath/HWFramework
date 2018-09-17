package android.net.wifi;

import android.Manifest.permission;
import android.content.Context;
import android.net.INetworkScoreCache.Stub;
import android.net.NetworkKey;
import android.net.ScoredNetwork;
import android.os.Handler;
import android.os.Process;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.Preconditions;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WifiNetworkScoreCache extends Stub {
    private static final boolean DBG = Log.isLoggable(TAG, 3);
    public static final int INVALID_NETWORK_SCORE = -128;
    private static final String TAG = "WifiNetworkScoreCache";
    private final Object mCacheLock;
    private final Context mContext;
    @GuardedBy("mCacheLock")
    private CacheListener mListener;
    private final Map<String, ScoredNetwork> mNetworkCache;

    public static abstract class CacheListener {
        private Handler mHandler;

        public abstract void networkCacheUpdated(List<ScoredNetwork> list);

        public CacheListener(Handler handler) {
            Preconditions.checkNotNull(handler);
            this.mHandler = handler;
        }

        void post(final List<ScoredNetwork> updatedNetworks) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    CacheListener.this.networkCacheUpdated(updatedNetworks);
                }
            });
        }
    }

    public WifiNetworkScoreCache(Context context) {
        this(context, null);
    }

    public WifiNetworkScoreCache(Context context, CacheListener listener) {
        this.mCacheLock = new Object();
        this.mContext = context.getApplicationContext();
        this.mListener = listener;
        this.mNetworkCache = new HashMap();
    }

    public final void updateScores(List<ScoredNetwork> networks) {
        if (networks != null && !networks.isEmpty()) {
            if (DBG) {
                Log.d(TAG, "updateScores list size=" + networks.size());
            }
            boolean changed = false;
            synchronized (this.mNetworkCache) {
                for (ScoredNetwork network : networks) {
                    String networkKey = buildNetworkKey(network);
                    if (networkKey != null) {
                        this.mNetworkCache.put(networkKey, network);
                        changed = true;
                    } else if (DBG) {
                        Log.d(TAG, "Failed to build network key for ScoredNetwork" + network);
                    }
                }
            }
            synchronized (this.mCacheLock) {
                if (this.mListener != null && changed) {
                    this.mListener.post(networks);
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
        return getScoredNetwork(result) != null;
    }

    public boolean hasScoreCurve(ScanResult result) {
        ScoredNetwork network = getScoredNetwork(result);
        if (network == null || network.rssiCurve == null) {
            return false;
        }
        return true;
    }

    public int getNetworkScore(ScanResult result) {
        int score = INVALID_NETWORK_SCORE;
        ScoredNetwork network = getScoredNetwork(result);
        if (!(network == null || network.rssiCurve == null)) {
            score = network.rssiCurve.lookupScore(result.level);
            if (DBG) {
                Log.d(TAG, "getNetworkScore found scored network " + network.networkKey + " score " + Integer.toString(score) + " RSSI " + result.level);
            }
        }
        return score;
    }

    public boolean getMeteredHint(ScanResult result) {
        ScoredNetwork network = getScoredNetwork(result);
        return network != null ? network.meteredHint : false;
    }

    public int getNetworkScore(ScanResult result, boolean isActiveNetwork) {
        int score = INVALID_NETWORK_SCORE;
        ScoredNetwork network = getScoredNetwork(result);
        if (!(network == null || network.rssiCurve == null)) {
            score = network.rssiCurve.lookupScore(result.level, isActiveNetwork);
            if (DBG) {
                Log.d(TAG, "getNetworkScore found scored network " + network.networkKey + " score " + Integer.toString(score) + " RSSI " + result.level + " isActiveNetwork " + isActiveNetwork);
            }
        }
        return score;
    }

    public ScoredNetwork getScoredNetwork(ScanResult result) {
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

    public ScoredNetwork getScoredNetwork(NetworkKey networkKey) {
        String key = buildNetworkKey(networkKey);
        if (key == null) {
            if (DBG) {
                Log.d(TAG, "Could not build key string for Network Key: " + networkKey);
            }
            return null;
        }
        ScoredNetwork scoredNetwork;
        synchronized (this.mNetworkCache) {
            scoredNetwork = (ScoredNetwork) this.mNetworkCache.get(key);
        }
        return scoredNetwork;
    }

    private String buildNetworkKey(ScoredNetwork network) {
        if (network == null) {
            return null;
        }
        return buildNetworkKey(network.networkKey);
    }

    private String buildNetworkKey(NetworkKey networkKey) {
        if (networkKey == null || networkKey.wifiKey == null || networkKey.type != 1) {
            return null;
        }
        String key = networkKey.wifiKey.ssid;
        if (key == null) {
            return null;
        }
        if (networkKey.wifiKey.bssid != null) {
            key = key + networkKey.wifiKey.bssid;
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
        this.mContext.enforceCallingOrSelfPermission(permission.DUMP, TAG);
        writer.println(String.format("WifiNetworkScoreCache (%s/%d)", new Object[]{this.mContext.getPackageName(), Integer.valueOf(Process.myUid())}));
        writer.println("  All score curves:");
        for (ScoredNetwork score : this.mNetworkCache.values()) {
            writer.println("    " + score);
        }
        writer.println("  Current network scores:");
        for (ScanResult scanResult : ((WifiManager) this.mContext.getSystemService(Context.WIFI_SERVICE)).getScanResults()) {
            writer.println("    " + buildNetworkKey(scanResult) + ": " + getNetworkScore(scanResult));
        }
    }

    public void registerListener(CacheListener listener) {
        synchronized (this.mCacheLock) {
            this.mListener = listener;
        }
    }

    public void unregisterListener() {
        synchronized (this.mCacheLock) {
            this.mListener = null;
        }
    }
}
