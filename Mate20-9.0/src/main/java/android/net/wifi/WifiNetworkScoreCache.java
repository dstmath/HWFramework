package android.net.wifi;

import android.content.Context;
import android.net.INetworkScoreCache;
import android.net.NetworkKey;
import android.net.ScoredNetwork;
import android.os.Handler;
import android.os.Process;
import android.util.Log;
import android.util.LruCache;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.Preconditions;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

public class WifiNetworkScoreCache extends INetworkScoreCache.Stub {
    private static final boolean DBG = Log.isLoggable(TAG, 3);
    private static final int DEFAULT_MAX_CACHE_SIZE = 100;
    public static final int INVALID_NETWORK_SCORE = -128;
    private static final String TAG = "WifiNetworkScoreCache";
    @GuardedBy("mLock")
    private final LruCache<String, ScoredNetwork> mCache;
    private final Context mContext;
    @GuardedBy("mLock")
    private CacheListener mListener;
    private final Object mLock;

    public static abstract class CacheListener {
        private Handler mHandler;

        public abstract void networkCacheUpdated(List<ScoredNetwork> list);

        public CacheListener(Handler handler) {
            Preconditions.checkNotNull(handler);
            this.mHandler = handler;
        }

        /* access modifiers changed from: package-private */
        public void post(final List<ScoredNetwork> updatedNetworks) {
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
        this(context, listener, 100);
    }

    public WifiNetworkScoreCache(Context context, CacheListener listener, int maxCacheSize) {
        this.mLock = new Object();
        this.mContext = context.getApplicationContext();
        this.mListener = listener;
        this.mCache = new LruCache<>(maxCacheSize);
    }

    public final void updateScores(List<ScoredNetwork> networks) {
        if (networks != null && !networks.isEmpty()) {
            if (DBG) {
                Log.d(TAG, "updateScores list size=" + networks.size());
            }
            boolean changed = false;
            synchronized (this.mLock) {
                for (ScoredNetwork network : networks) {
                    String networkKey = buildNetworkKey(network);
                    if (networkKey != null) {
                        this.mCache.put(networkKey, network);
                        changed = true;
                    } else if (DBG) {
                        Log.d(TAG, "Failed to build network key for ScoredNetwork" + network);
                    }
                }
                if (this.mListener != null && changed) {
                    this.mListener.post(networks);
                }
            }
        }
    }

    public final void clearScores() {
        synchronized (this.mLock) {
            this.mCache.evictAll();
        }
    }

    public boolean isScoredNetwork(ScanResult result) {
        return getScoredNetwork(result) != null;
    }

    public boolean hasScoreCurve(ScanResult result) {
        ScoredNetwork network = getScoredNetwork(result);
        return (network == null || network.rssiCurve == null) ? false : true;
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
        return network != null && network.meteredHint;
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
        ScoredNetwork network;
        String key = buildNetworkKey(result);
        if (key == null) {
            return null;
        }
        synchronized (this.mLock) {
            network = this.mCache.get(key);
        }
        return network;
    }

    public ScoredNetwork getScoredNetwork(NetworkKey networkKey) {
        ScoredNetwork scoredNetwork;
        String key = buildNetworkKey(networkKey);
        if (key == null) {
            if (DBG) {
                Log.d(TAG, "Could not build key string for Network Key: " + networkKey);
            }
            return null;
        }
        synchronized (this.mLock) {
            scoredNetwork = this.mCache.get(key);
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

    /* access modifiers changed from: protected */
    public final void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.DUMP", TAG);
        writer.println(String.format("WifiNetworkScoreCache (%s/%d)", new Object[]{this.mContext.getPackageName(), Integer.valueOf(Process.myUid())}));
        writer.println("  All score curves:");
        synchronized (this.mLock) {
            Iterator<ScoredNetwork> it = this.mCache.snapshot().values().iterator();
            while (it.hasNext()) {
                writer.println("    " + it.next());
            }
            writer.println("  Network scores for latest ScanResults:");
            for (ScanResult scanResult : ((WifiManager) this.mContext.getSystemService("wifi")).getScanResults()) {
                writer.println("    " + buildNetworkKey(scanResult) + ": " + getNetworkScore(scanResult));
            }
        }
    }

    public void registerListener(CacheListener listener) {
        synchronized (this.mLock) {
            this.mListener = listener;
        }
    }

    public void unregisterListener() {
        synchronized (this.mLock) {
            this.mListener = null;
        }
    }
}
