package com.android.server.wifi.hotspot2;

import android.util.Log;
import com.android.server.wifi.Clock;
import com.android.server.wifi.anqp.ANQPElement;
import com.android.server.wifi.anqp.Constants.ANQPElementType;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class AnqpCache {
    private static final long CACHE_RECHECK = 60000;
    private static final boolean DBG = false;
    private static final boolean STANDARD_ESS = true;
    private final HashMap<CacheKey, ANQPData> mANQPCache;
    private Clock mClock;
    private long mLastSweep;

    private static class CacheKey {
        private final long mBSSID;
        private final long mHESSID;
        private final String mSSID;

        private CacheKey(String ssid, long bssid, long hessid) {
            this.mSSID = ssid;
            this.mBSSID = bssid;
            this.mHESSID = hessid;
        }

        private static CacheKey buildKey(NetworkDetail network, boolean standardESS) {
            String ssid;
            long bssid;
            long hessid;
            if (((long) network.getAnqpDomainID()) == 0 || (network.getHESSID() == 0 && !standardESS)) {
                ssid = network.getSSID();
                bssid = network.getBSSID();
                hessid = 0;
            } else if (network.getHESSID() == 0 || network.getAnqpDomainID() <= 0) {
                ssid = network.getSSID();
                bssid = 0;
                hessid = 0;
            } else {
                ssid = null;
                bssid = 0;
                hessid = network.getHESSID();
            }
            return new CacheKey(ssid, bssid, hessid);
        }

        public int hashCode() {
            if (this.mHESSID != 0) {
                return (int) (((this.mHESSID >>> 32) * 31) + this.mHESSID);
            }
            if (this.mBSSID != 0) {
                return (int) (((((long) (this.mSSID.hashCode() * 31)) + (this.mBSSID >>> 32)) * 31) + this.mBSSID);
            }
            return this.mSSID.hashCode();
        }

        public boolean equals(Object thatObject) {
            boolean z = AnqpCache.STANDARD_ESS;
            if (thatObject == this) {
                return AnqpCache.STANDARD_ESS;
            }
            if (thatObject == null || thatObject.getClass() != CacheKey.class) {
                return AnqpCache.DBG;
            }
            CacheKey that = (CacheKey) thatObject;
            if (Utils.compare(that.mSSID, this.mSSID) != 0 || that.mBSSID != this.mBSSID) {
                z = AnqpCache.DBG;
            } else if (that.mHESSID != this.mHESSID) {
                z = AnqpCache.DBG;
            }
            return z;
        }

        public String toString() {
            if (this.mHESSID != 0) {
                return "HESSID:" + NetworkDetail.toMACString(this.mHESSID);
            }
            if (this.mBSSID != 0) {
                return NetworkDetail.toMACString(this.mBSSID) + ":<" + Utils.toUnicodeEscapedString(this.mSSID) + ">";
            }
            return '<' + Utils.toUnicodeEscapedString(this.mSSID) + '>';
        }
    }

    public AnqpCache(Clock clock) {
        this.mClock = clock;
        this.mANQPCache = new HashMap();
        this.mLastSweep = this.mClock.currentTimeMillis();
    }

    public List<ANQPElementType> initiate(NetworkDetail network, List<ANQPElementType> querySet) {
        CacheKey key = CacheKey.buildKey(network, STANDARD_ESS);
        synchronized (this.mANQPCache) {
            ANQPData data = (ANQPData) this.mANQPCache.get(key);
            if (data == null || data.expired()) {
                this.mANQPCache.put(key, new ANQPData(this.mClock, network, data));
                return querySet;
            }
            List<ANQPElementType> newList = data.disjoint(querySet);
            Log.d(Utils.hs2LogTag(getClass()), String.format("New ANQP elements for BSSID %012x: %s", new Object[]{Long.valueOf(network.getBSSID()), newList}));
            return newList;
        }
    }

    public void update(NetworkDetail network, Map<ANQPElementType, ANQPElement> anqpElements) {
        CacheKey key = CacheKey.buildKey(network, STANDARD_ESS);
        synchronized (this.mANQPCache) {
            ANQPData data = (ANQPData) this.mANQPCache.get(key);
            if (data == null || !data.hasData()) {
                this.mANQPCache.put(key, new ANQPData(this.mClock, network, (Map) anqpElements));
            } else {
                data.merge(anqpElements);
            }
        }
    }

    public ANQPData getEntry(NetworkDetail network) {
        CacheKey key = CacheKey.buildKey(network, STANDARD_ESS);
        synchronized (this.mANQPCache) {
            ANQPData data = (ANQPData) this.mANQPCache.get(key);
        }
        if (data == null || !data.isValid(network)) {
            return null;
        }
        return data;
    }

    public void clear(boolean all, boolean debug) {
        long now = this.mClock.currentTimeMillis();
        synchronized (this.mANQPCache) {
            if (all) {
                this.mANQPCache.clear();
                this.mLastSweep = now;
            } else if (now > this.mLastSweep + CACHE_RECHECK) {
                List<CacheKey> retirees = new ArrayList();
                for (Entry<CacheKey, ANQPData> entry : this.mANQPCache.entrySet()) {
                    if (((ANQPData) entry.getValue()).expired(now)) {
                        retirees.add((CacheKey) entry.getKey());
                    }
                }
                for (CacheKey key : retirees) {
                    this.mANQPCache.remove(key);
                    if (debug) {
                        Log.d(Utils.hs2LogTag(getClass()), "Retired " + key);
                    }
                }
                this.mLastSweep = now;
            }
        }
    }

    public void dump(PrintWriter out) {
        out.println("Last sweep " + Utils.toHMS(this.mClock.currentTimeMillis() - this.mLastSweep) + " ago.");
        for (ANQPData anqpData : this.mANQPCache.values()) {
            out.println(anqpData.toString(DBG));
        }
    }
}
