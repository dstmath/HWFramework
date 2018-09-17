package com.android.server.wifi.hotspot2;

import com.android.server.wifi.Clock;
import com.android.server.wifi.hotspot2.anqp.ANQPElement;
import com.android.server.wifi.hotspot2.anqp.Constants.ANQPElementType;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class AnqpCache {
    public static final long CACHE_SWEEP_INTERVAL_MILLISECONDS = 60000;
    private final Map<ANQPNetworkKey, ANQPData> mANQPCache = new HashMap();
    private Clock mClock;
    private long mLastSweep = this.mClock.getElapsedSinceBootMillis();

    public AnqpCache(Clock clock) {
        this.mClock = clock;
    }

    public void addEntry(ANQPNetworkKey key, Map<ANQPElementType, ANQPElement> anqpElements) {
        this.mANQPCache.put(key, new ANQPData(this.mClock, anqpElements));
    }

    public ANQPData getEntry(ANQPNetworkKey key) {
        return (ANQPData) this.mANQPCache.get(key);
    }

    public void sweep() {
        long now = this.mClock.getElapsedSinceBootMillis();
        if (now >= this.mLastSweep + CACHE_SWEEP_INTERVAL_MILLISECONDS) {
            List<ANQPNetworkKey> expiredKeys = new ArrayList();
            for (Entry<ANQPNetworkKey, ANQPData> entry : this.mANQPCache.entrySet()) {
                if (((ANQPData) entry.getValue()).expired(now)) {
                    expiredKeys.add((ANQPNetworkKey) entry.getKey());
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
        for (Entry<ANQPNetworkKey, ANQPData> entry : this.mANQPCache.entrySet()) {
            out.println(entry.getKey() + ": " + entry.getValue());
        }
    }
}
