package com.android.server.wifi;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.Visibility;
import android.os.SystemClock;
import com.android.server.wifi.hotspot2.ANQPData;
import com.android.server.wifi.hotspot2.AnqpCache;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class ScanDetailCache {
    private static final boolean DBG = false;
    private static final String TAG = "ScanDetailCache";
    private final WifiConfiguration mConfig;
    private final HashMap<String, ScanDetail> mMap = new HashMap(16, 0.75f);
    private final int mMaxSize;
    private final int mTrimSize;

    ScanDetailCache(WifiConfiguration config, int maxSize, int trimSize) {
        this.mConfig = config;
        this.mMaxSize = maxSize;
        this.mTrimSize = trimSize;
    }

    void put(ScanDetail scanDetail) {
        if (this.mMap.size() >= this.mMaxSize) {
            trim();
        }
        this.mMap.put(scanDetail.getBSSIDString(), scanDetail);
    }

    ScanResult get(String bssid) {
        ScanDetail scanDetail = getScanDetail(bssid);
        if (scanDetail == null) {
            return null;
        }
        return scanDetail.getScanResult();
    }

    ScanDetail getScanDetail(String bssid) {
        return (ScanDetail) this.mMap.get(bssid);
    }

    void remove(String bssid) {
        this.mMap.remove(bssid);
    }

    int size() {
        return this.mMap.size();
    }

    boolean isEmpty() {
        return size() == 0;
    }

    Collection<String> keySet() {
        return this.mMap.keySet();
    }

    Collection<ScanDetail> values() {
        return this.mMap.values();
    }

    private void trim() {
        int currentSize = this.mMap.size();
        if (currentSize >= this.mTrimSize) {
            ArrayList<ScanDetail> list = new ArrayList(this.mMap.values());
            if (list.size() != 0) {
                Collections.sort(list, new Comparator() {
                    public int compare(Object o1, Object o2) {
                        ScanDetail a = (ScanDetail) o1;
                        ScanDetail b = (ScanDetail) o2;
                        if (a.getSeen() > b.getSeen()) {
                            return 1;
                        }
                        if (a.getSeen() < b.getSeen()) {
                            return -1;
                        }
                        return a.getBSSIDString().compareTo(b.getBSSIDString());
                    }
                });
            }
            for (int i = 0; i < currentSize - this.mTrimSize; i++) {
                this.mMap.remove(((ScanDetail) list.get(i)).getBSSIDString());
            }
        }
    }

    private ArrayList<ScanDetail> sort() {
        ArrayList<ScanDetail> list = new ArrayList(this.mMap.values());
        if (list.size() != 0) {
            Collections.sort(list, new Comparator() {
                public int compare(Object o1, Object o2) {
                    ScanResult a = ((ScanDetail) o1).getScanResult();
                    ScanResult b = ((ScanDetail) o2).getScanResult();
                    if (a.numIpConfigFailures > b.numIpConfigFailures) {
                        return 1;
                    }
                    if (a.numIpConfigFailures < b.numIpConfigFailures || a.seen > b.seen) {
                        return -1;
                    }
                    if (a.seen < b.seen) {
                        return 1;
                    }
                    if (a.level > b.level) {
                        return -1;
                    }
                    if (a.level < b.level) {
                        return 1;
                    }
                    return a.BSSID.compareTo(b.BSSID);
                }
            });
        }
        return list;
    }

    public Visibility getVisibilityByRssi(long age) {
        Visibility status = new Visibility();
        long now_ms = System.currentTimeMillis();
        long now_elapsed_ms = SystemClock.elapsedRealtime();
        for (ScanDetail scanDetail : values()) {
            ScanResult result = scanDetail.getScanResult();
            if (scanDetail.getSeen() != 0) {
                if (result.is5GHz()) {
                    status.num5++;
                } else if (result.is24GHz()) {
                    status.num24++;
                }
                if (result.timestamp != 0) {
                    if (now_elapsed_ms - (result.timestamp / 1000) > age) {
                    }
                } else if (now_ms - result.seen > age) {
                }
                if (result.is5GHz()) {
                    if (result.level > status.rssi5) {
                        status.rssi5 = result.level;
                        status.age5 = result.seen;
                        status.BSSID5 = result.BSSID;
                    }
                } else if (result.is24GHz() && result.level > status.rssi24) {
                    status.rssi24 = result.level;
                    status.age24 = result.seen;
                    status.BSSID24 = result.BSSID;
                }
            }
        }
        return status;
    }

    public Visibility getVisibility(long age) {
        return getVisibilityByRssi(age);
    }

    public String toString() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("Scan Cache:  ").append(10);
        ArrayList<ScanDetail> list = sort();
        long now_ms = System.currentTimeMillis();
        if (list.size() > 0) {
            for (ScanDetail scanDetail : list) {
                ScanResult result = scanDetail.getScanResult();
                long milli = now_ms - scanDetail.getSeen();
                long ageSec = 0;
                long ageMin = 0;
                long ageHour = 0;
                long ageMilli = 0;
                long ageDay = 0;
                if (now_ms > scanDetail.getSeen() && scanDetail.getSeen() > 0) {
                    ageMilli = milli % 1000;
                    ageSec = (milli / 1000) % 60;
                    ageMin = (milli / AnqpCache.CACHE_SWEEP_INTERVAL_MILLISECONDS) % 60;
                    ageHour = (milli / ANQPData.DATA_LIFETIME_MILLISECONDS) % 24;
                    ageDay = milli / 86400000;
                }
                sbuf.append("{").append(result.BSSID).append(",").append(result.frequency);
                sbuf.append(",").append(String.format("%3d", new Object[]{Integer.valueOf(result.level)}));
                if (ageSec > 0 || ageMilli > 0) {
                    sbuf.append(String.format(",%4d.%02d.%02d.%02d.%03dms", new Object[]{Long.valueOf(ageDay), Long.valueOf(ageHour), Long.valueOf(ageMin), Long.valueOf(ageSec), Long.valueOf(ageMilli)}));
                }
                if (result.numIpConfigFailures > 0) {
                    sbuf.append(",ipfail=");
                    sbuf.append(result.numIpConfigFailures);
                }
                sbuf.append("} ");
            }
            sbuf.append(10);
        }
        return sbuf.toString();
    }
}
