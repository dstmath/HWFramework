package com.android.server.wifi.hwUtil;

import android.text.TextUtils;
import android.util.Log;
import com.android.server.wifi.util.NativeUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ScanResultRecords implements IScanResultRecords {
    public static final int ENTERPRISE_HILINK_AP = 2;
    public static final int NORMAL_AP = 0;
    public static final int NORMAL_HILINK_AP = 1;
    private static final String TAG = "ScanResultRecords";
    public static final int WIFI_CATEGORY_DEFAULT = 1;
    public static final int WIFI_CATEGORY_WIFI6 = 2;
    public static final int WIFI_CATEGORY_WIFI6_PLUS = 3;
    private static ScanResultRecords sInstance = null;
    private final Map<String, Integer> mHiLinkRecords = new HashMap();
    private final Map<String, Map<String, ArrayList<Byte>>> mOriSsidRecords = new HashMap();
    private final Map<String, String> mPmfRecords = new HashMap();
    private final Map<String, Integer> mWifiCategoryRecords = new HashMap();

    public static ScanResultRecords getDefault() {
        if (sInstance == null) {
            synchronized (ScanResultRecords.class) {
                if (sInstance == null) {
                    sInstance = new ScanResultRecords();
                }
            }
        }
        return sInstance;
    }

    public synchronized void cleanup() {
        this.mHiLinkRecords.clear();
        this.mOriSsidRecords.clear();
        this.mPmfRecords.clear();
        this.mWifiCategoryRecords.clear();
    }

    public synchronized void recordHiLinkAp(String bssid, int hiLinkApType) {
        if (TextUtils.isEmpty(bssid)) {
            Log.d(TAG, "recordHiLinkNetwork: bssid is empty.");
            return;
        }
        this.mHiLinkRecords.put(bssid.toLowerCase(Locale.ROOT), Integer.valueOf(hiLinkApType));
    }

    public synchronized int getHiLinkAp(String bssid) {
        if (TextUtils.isEmpty(bssid)) {
            Log.e(TAG, "isNetworkRecordedAsHiLink: bssid is empty.");
            return 0;
        }
        String record = bssid.toLowerCase(Locale.ROOT);
        if (!this.mHiLinkRecords.containsKey(record)) {
            Log.d(TAG, "getHiLinkAp: bssid is not match");
            return 0;
        }
        return this.mHiLinkRecords.get(record).intValue();
    }

    @Override // com.android.server.wifi.hwUtil.IScanResultRecords
    public synchronized void recordWifiCategory(String bssid, int wifiCategory) {
        if (TextUtils.isEmpty(bssid)) {
            Log.i(TAG, "recordWifiCategory: bssid is empty.");
            return;
        }
        this.mWifiCategoryRecords.put(bssid.toLowerCase(Locale.ROOT), Integer.valueOf(wifiCategory));
    }

    @Override // com.android.server.wifi.hwUtil.IScanResultRecords
    public synchronized int getWifiCategory(String bssid) {
        if (TextUtils.isEmpty(bssid)) {
            Log.e(TAG, "getWifiCategory: bssid is empty.");
            return 1;
        }
        String record = bssid.toLowerCase(Locale.ROOT);
        if (!this.mWifiCategoryRecords.containsKey(record)) {
            Log.i(TAG, "getWifiCategory: bssid is not match");
            return 1;
        }
        return this.mWifiCategoryRecords.get(record).intValue();
    }

    public synchronized void recordOriSsid(String bssid, String ssid, byte[] oriSsid) {
        if (!TextUtils.isEmpty(bssid) && !TextUtils.isEmpty(ssid)) {
            if (!isByteArrayInvalid(oriSsid)) {
                String bssidRecord = bssid.toLowerCase(Locale.ROOT);
                String ssidRecord = NativeUtil.removeEnclosingQuotes(ssid);
                ArrayList<Byte> oriSsidRecord = NativeUtil.byteArrayToArrayList(oriSsid);
                if (!this.mOriSsidRecords.containsKey(bssidRecord)) {
                    Map<String, ArrayList<Byte>> item = new HashMap<>();
                    item.put(ssidRecord, oriSsidRecord);
                    this.mOriSsidRecords.put(bssid, item);
                } else if (!this.mOriSsidRecords.get(bssidRecord).containsKey(ssidRecord)) {
                    Map<String, ArrayList<Byte>> item2 = this.mOriSsidRecords.get(bssidRecord);
                    item2.put(ssidRecord, oriSsidRecord);
                    this.mOriSsidRecords.remove(bssid);
                    this.mOriSsidRecords.put(bssidRecord, item2);
                }
                return;
            }
        }
        Log.d(TAG, "recordOriSsid: param is invalid.");
    }

    public synchronized ArrayList<Byte> getOriSsid(String bssid, String ssid) {
        if (!TextUtils.isEmpty(bssid)) {
            if (!TextUtils.isEmpty(ssid)) {
                String bssidRecord = bssid.toLowerCase(Locale.ROOT);
                String ssidRecord = NativeUtil.removeEnclosingQuotes(ssid);
                if (!this.mOriSsidRecords.containsKey(bssidRecord)) {
                    Log.d(TAG, "getOriSsid: bssid is not exist in records.");
                    return null;
                } else if (!this.mOriSsidRecords.get(bssidRecord).containsKey(ssidRecord)) {
                    Log.d(TAG, "getOriSsid: ssid is not exist in records.");
                    return null;
                } else {
                    return this.mOriSsidRecords.get(bssidRecord).get(ssidRecord);
                }
            }
        }
        Log.d(TAG, "getOriSsid: param is null");
        return null;
    }

    public synchronized ArrayList<Byte> getOriSsid(String ssid) {
        if (TextUtils.isEmpty(ssid)) {
            Log.d(TAG, "getOriSsid: param is null");
            return null;
        }
        String ssidRecord = NativeUtil.removeEnclosingQuotes(ssid);
        for (Map<String, ArrayList<Byte>> records : this.mOriSsidRecords.values()) {
            if (records.containsKey(ssidRecord)) {
                return records.get(ssidRecord);
            }
        }
        return null;
    }

    public synchronized void clearOrdSsidRecords() {
        this.mOriSsidRecords.clear();
    }

    public synchronized void recordPmf(String bssid, String pmfCapabilities) {
        if (!TextUtils.isEmpty(bssid)) {
            if (!TextUtils.isEmpty(pmfCapabilities)) {
                this.mPmfRecords.put(bssid.toLowerCase(Locale.ROOT), pmfCapabilities);
                return;
            }
        }
        Log.d(TAG, "PMF: param is null");
    }

    public synchronized String getPmf(String bssid) {
        if (TextUtils.isEmpty(bssid)) {
            Log.d(TAG, "PMF: param is null");
            return null;
        }
        String bssidRecord = bssid.toLowerCase(Locale.ROOT);
        if (!this.mPmfRecords.containsKey(bssidRecord)) {
            Log.d(TAG, "PMF: do not match");
            return null;
        }
        return this.mPmfRecords.get(bssidRecord);
    }

    private boolean isByteArrayInvalid(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return true;
        }
        for (byte b : bytes) {
            if (b != 0) {
                return false;
            }
        }
        return true;
    }
}
