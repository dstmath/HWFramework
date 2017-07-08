package com.android.server.wifi;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.Visibility;
import android.os.SystemClock;
import android.util.Log;
import com.android.server.wifi.hotspot2.PasspointMatch;
import com.android.server.wifi.hotspot2.PasspointMatchInfo;
import com.android.server.wifi.hotspot2.omadm.PasspointManagementObjectManager;
import com.android.server.wifi.hotspot2.pps.HomeSP;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class ScanDetailCache {
    private static final boolean DBG = false;
    private static final String TAG = "ScanDetailCache";
    private WifiConfiguration mConfig;
    private ConcurrentHashMap<String, ScanDetail> mMap;
    private ConcurrentHashMap<String, PasspointMatchInfo> mPasspointMatches;

    ScanDetailCache(WifiConfiguration config) {
        this.mConfig = config;
        this.mMap = new ConcurrentHashMap(16, 0.75f, 2);
        this.mPasspointMatches = new ConcurrentHashMap(16, 0.75f, 2);
    }

    void put(ScanDetail scanDetail) {
        put(scanDetail, null, null);
    }

    void put(ScanDetail scanDetail, PasspointMatch match, HomeSP homeSp) {
        this.mMap.put(scanDetail.getBSSIDString(), scanDetail);
        if (match != null && homeSp != null) {
            this.mPasspointMatches.put(scanDetail.getBSSIDString(), new PasspointMatchInfo(match, scanDetail, homeSp));
        }
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
        return size() == 0 ? true : DBG;
    }

    ScanDetail getFirst() {
        Iterator<ScanDetail> it = this.mMap.values().iterator();
        return it.hasNext() ? (ScanDetail) it.next() : null;
    }

    Collection<String> keySet() {
        return this.mMap.keySet();
    }

    Collection<ScanDetail> values() {
        return this.mMap.values();
    }

    public void trim(int num) {
        int currentSize = this.mMap.size();
        if (currentSize > num) {
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
            for (int i = 0; i < currentSize - num; i++) {
                ScanDetail result = (ScanDetail) list.get(i);
                this.mMap.remove(result.getBSSIDString());
                this.mPasspointMatches.remove(result.getBSSIDString());
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

    public Visibility getVisibilityByPasspointMatch(long age) {
        long now_ms = System.currentTimeMillis();
        PasspointMatchInfo pmiBest24 = null;
        PasspointMatchInfo pmiBest5 = null;
        for (PasspointMatchInfo pmi : this.mPasspointMatches.values()) {
            ScanResult result;
            ScanDetail scanDetail = pmi.getScanDetail();
            if (scanDetail != null) {
                result = scanDetail.getScanResult();
                if (!(result == null || scanDetail.getSeen() == 0 || now_ms - result.seen > age)) {
                    if (result.is5GHz()) {
                        if (pmiBest5 == null || pmiBest5.compareTo(pmi) < 0) {
                            pmiBest5 = pmi;
                        }
                    } else if (result.is24GHz() && (pmiBest24 == null || pmiBest24.compareTo(pmi) < 0)) {
                        pmiBest24 = pmi;
                    }
                }
            }
        }
        Visibility status = new Visibility();
        String logMsg = "Visiblity by passpoint match returned ";
        if (pmiBest5 != null) {
            result = pmiBest5.getScanDetail().getScanResult();
            status.rssi5 = result.level;
            status.age5 = result.seen;
            status.BSSID5 = result.BSSID;
            logMsg = logMsg + "5 GHz BSSID of " + result.BSSID;
        }
        if (pmiBest24 != null) {
            result = pmiBest24.getScanDetail().getScanResult();
            status.rssi24 = result.level;
            status.age24 = result.seen;
            status.BSSID24 = result.BSSID;
            logMsg = logMsg + "2.4 GHz BSSID of " + result.BSSID;
        }
        Log.d(TAG, logMsg);
        return status;
    }

    public Visibility getVisibility(long age) {
        if (this.mConfig.isPasspoint()) {
            return getVisibilityByPasspointMatch(age);
        }
        return getVisibilityByRssi(age);
    }

    public String toString() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("Scan Cache:  ").append('\n');
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
                    ageMin = (milli / PasspointManagementObjectManager.IntervalFactor) % 60;
                    ageHour = (milli / 3600000) % 24;
                    ageDay = milli / 86400000;
                }
                String str = result.BSSID;
                int i = result.frequency;
                sbuf.append("{").append(r0).append(",").append(r0);
                StringBuilder append = sbuf.append(",");
                Object[] objArr = new Object[1];
                objArr[0] = Integer.valueOf(result.level);
                append.append(String.format("%3d", objArr));
                if (ageSec > 0 || ageMilli > 0) {
                    sbuf.append(String.format(",%4d.%02d.%02d.%02d.%03dms", new Object[]{Long.valueOf(ageDay), Long.valueOf(ageHour), Long.valueOf(ageMin), Long.valueOf(ageSec), Long.valueOf(ageMilli)}));
                }
                if (result.numIpConfigFailures > 0) {
                    sbuf.append(",ipfail=");
                    sbuf.append(result.numIpConfigFailures);
                }
                sbuf.append("} ");
            }
            sbuf.append('\n');
        }
        return sbuf.toString();
    }
}
