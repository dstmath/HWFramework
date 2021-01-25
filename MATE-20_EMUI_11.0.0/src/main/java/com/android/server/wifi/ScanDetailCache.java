package com.android.server.wifi;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import com.android.server.wifi.hotspot2.ANQPData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

public class ScanDetailCache {
    private static final boolean DBG = false;
    private static final String TAG = "ScanDetailCache";
    private final WifiConfiguration mConfig;
    private final HashMap<String, ScanDetail> mMap = new HashMap<>(16, 0.75f);
    private final int mMaxSize;
    private final int mTrimSize;

    ScanDetailCache(WifiConfiguration config, int maxSize, int trimSize) {
        this.mConfig = config;
        this.mMaxSize = maxSize;
        this.mTrimSize = trimSize;
    }

    /* access modifiers changed from: package-private */
    public void put(ScanDetail scanDetail) {
        if (this.mMap.size() >= this.mMaxSize) {
            trim();
        }
        this.mMap.put(scanDetail.getBSSIDString(), scanDetail);
    }

    public ScanResult getScanResult(String bssid) {
        ScanDetail scanDetail = getScanDetail(bssid);
        if (scanDetail == null) {
            return null;
        }
        return scanDetail.getScanResult();
    }

    public ScanDetail getScanDetail(String bssid) {
        return this.mMap.get(bssid);
    }

    /* access modifiers changed from: package-private */
    public void remove(String bssid) {
        this.mMap.remove(bssid);
    }

    /* access modifiers changed from: package-private */
    public int size() {
        return this.mMap.size();
    }

    /* access modifiers changed from: package-private */
    public boolean isEmpty() {
        return size() == 0;
    }

    /* access modifiers changed from: package-private */
    public Collection<String> keySet() {
        return this.mMap.keySet();
    }

    /* access modifiers changed from: package-private */
    public Collection<ScanDetail> values() {
        return this.mMap.values();
    }

    private void trim() {
        int currentSize = this.mMap.size();
        if (currentSize >= this.mTrimSize) {
            ArrayList<ScanDetail> list = new ArrayList<>(this.mMap.values());
            if (list.size() != 0) {
                Collections.sort(list, new Comparator() {
                    /* class com.android.server.wifi.ScanDetailCache.AnonymousClass1 */

                    @Override // java.util.Comparator
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
                this.mMap.remove(list.get(i).getBSSIDString());
            }
        }
    }

    private ArrayList<ScanDetail> sort() {
        ArrayList<ScanDetail> list = new ArrayList<>(this.mMap.values());
        if (list.size() != 0) {
            Collections.sort(list, new Comparator() {
                /* class com.android.server.wifi.ScanDetailCache.AnonymousClass2 */

                @Override // java.util.Comparator
                public int compare(Object o1, Object o2) {
                    ScanResult a = ((ScanDetail) o1).getScanResult();
                    ScanResult b = ((ScanDetail) o2).getScanResult();
                    if (a.seen > b.seen) {
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

    public String toString() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("Scan Cache:  ");
        sbuf.append('\n');
        ArrayList<ScanDetail> list = sort();
        long now_ms = System.currentTimeMillis();
        if (list.size() > 0) {
            Iterator<ScanDetail> it = list.iterator();
            while (it.hasNext()) {
                ScanDetail scanDetail = it.next();
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
                    ageMin = (milli / 60000) % 60;
                    ageHour = (milli / ANQPData.DATA_LIFETIME_MILLISECONDS) % 24;
                    ageDay = milli / WifiConfigManager.DELETED_EPHEMERAL_SSID_EXPIRY_MS;
                }
                sbuf.append("{");
                sbuf.append(result.BSSID);
                sbuf.append(",");
                sbuf.append(result.frequency);
                sbuf.append(",");
                sbuf.append(String.format("%3d", Integer.valueOf(result.level)));
                if (ageSec > 0 || ageMilli > 0) {
                    sbuf.append(String.format(",%4d.%02d.%02d.%02d.%03dms", Long.valueOf(ageDay), Long.valueOf(ageHour), Long.valueOf(ageMin), Long.valueOf(ageSec), Long.valueOf(ageMilli)));
                }
                sbuf.append("} ");
                list = list;
            }
            sbuf.append('\n');
        }
        return sbuf.toString();
    }
}
