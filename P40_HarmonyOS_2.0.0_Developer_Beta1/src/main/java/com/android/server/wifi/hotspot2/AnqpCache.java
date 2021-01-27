package com.android.server.wifi.hotspot2;

import com.android.internal.annotations.VisibleForTesting;
import com.android.server.wifi.Clock;
import com.android.server.wifi.hotspot2.anqp.ANQPElement;
import com.android.server.wifi.hotspot2.anqp.Constants;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnqpCache {
    @VisibleForTesting
    public static final long CACHE_SWEEP_INTERVAL_MILLISECONDS = 60000;
    private final Map<ANQPNetworkKey, ANQPData> mANQPCache = new HashMap();
    private Clock mClock;
    private long mLastSweep = this.mClock.getElapsedSinceBootMillis();

    public AnqpCache(Clock clock) {
        this.mClock = clock;
    }

    public void addEntry(ANQPNetworkKey key, Map<Constants.ANQPElementType, ANQPElement> anqpElements) {
        this.mANQPCache.put(key, new ANQPData(this.mClock, anqpElements));
    }

    public ANQPData getEntry(ANQPNetworkKey key) {
        return this.mANQPCache.get(key);
    }

    public void sweep() {
        long now = this.mClock.getElapsedSinceBootMillis();
        if (now >= this.mLastSweep + 60000) {
            List<ANQPNetworkKey> expiredKeys = new ArrayList<>();
            for (Map.Entry<ANQPNetworkKey, ANQPData> entry : this.mANQPCache.entrySet()) {
                if (entry.getValue().expired(now)) {
                    expiredKeys.add(entry.getKey());
                }
            }
            for (ANQPNetworkKey key : expiredKeys) {
                this.mANQPCache.remove(key);
            }
            this.mLastSweep = now;
        }
    }

    public void dump(PrintWriter out) {
        out.println("Last sweep " + Utils.toHMS(this.mClock.getElapsedSinceBootMillis() - this.mLastSweep) + " ago.");
        for (Map.Entry<ANQPNetworkKey, ANQPData> entry : this.mANQPCache.entrySet()) {
            out.println(entry.getKey() + ": " + entry.getValue());
        }
    }
}
