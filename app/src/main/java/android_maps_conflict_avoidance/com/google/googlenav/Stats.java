package android_maps_conflict_avoidance.com.google.googlenav;

import android_maps_conflict_avoidance.com.google.common.Config;
import android_maps_conflict_avoidance.com.google.common.Log;
import android_maps_conflict_avoidance.com.google.common.StaticUtil;
import java.io.DataInput;
import java.io.IOException;

public class Stats {
    private static Stats currentInstance;
    private int bytesDownloaded;
    private int bytesUploaded;
    private int flashCacheHits;
    private int flashCacheHitsSinceLastLog;
    private int flashCacheMisses;
    private int flashCacheMissesSinceLastLog;

    private Stats() {
        this.flashCacheHits = 0;
        this.flashCacheMisses = 0;
        this.flashCacheHitsSinceLastLog = 0;
        this.flashCacheMissesSinceLastLog = 0;
        this.bytesDownloaded = 0;
        this.bytesUploaded = 0;
    }

    public static synchronized Stats getInstance() {
        Stats stats;
        synchronized (Stats.class) {
            if (currentInstance == null) {
                currentInstance = read();
                if (currentInstance == null) {
                    currentInstance = new Stats();
                }
            }
            stats = currentInstance;
        }
        return stats;
    }

    public void flashCacheHit() {
        synchronized (this) {
            this.flashCacheHits++;
            this.flashCacheHitsSinceLastLog++;
        }
        log(false);
    }

    public void flashCacheMiss() {
        synchronized (this) {
            this.flashCacheMisses++;
            this.flashCacheMissesSinceLastLog++;
        }
        log(false);
    }

    private static Stats read() {
        IOException e;
        DataInput dis = StaticUtil.readPreferenceAsDataInput("Stats");
        if (dis == null) {
            return null;
        }
        try {
            Stats stats = new Stats();
            try {
                stats.flashCacheHits = dis.readInt();
                stats.flashCacheMisses = dis.readInt();
                stats.bytesDownloaded = dis.readInt();
                stats.bytesUploaded = dis.readInt();
                return stats;
            } catch (IOException e2) {
                e = e2;
                Log.logThrowable("STATS", e);
                Config.getInstance().getPersistentStore().deleteBlock("Stats");
                return null;
            }
        } catch (IOException e3) {
            e = e3;
            Log.logThrowable("STATS", e);
            Config.getInstance().getPersistentStore().deleteBlock("Stats");
            return null;
        }
    }

    private void log(boolean force) {
        int threshold = 0;
        if (!force) {
            threshold = 50;
        }
        synchronized (this) {
            int hits = this.flashCacheHitsSinceLastLog;
            int misses = this.flashCacheMissesSinceLastLog;
            if (hits + misses > threshold) {
                this.flashCacheHitsSinceLastLog = 0;
                this.flashCacheMissesSinceLastLog = 0;
            }
        }
        if (hits + misses > threshold) {
            StringBuffer result = new StringBuffer();
            if (hits > 0) {
                result.append("|");
                result.append("f");
                result.append("=");
                result.append(hits);
            }
            if (misses > 0) {
                result.append("|");
                result.append("m");
                result.append("=");
                result.append(misses);
            }
            result.append("|");
            Log.addEvent((short) 22, "c", result.toString());
        }
    }
}
