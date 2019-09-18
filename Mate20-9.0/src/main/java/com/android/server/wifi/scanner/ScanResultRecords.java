package com.android.server.wifi.scanner;

import android.text.TextUtils;
import android.util.Log;
import com.android.server.wifi.util.NativeUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ScanResultRecords {
    private static final String TAG = "ScanResultRecords";
    private static ScanResultRecords sInstance = null;
    private final List<String> mHiLinkRecords = new ArrayList();
    private final Map<String, Map<String, ArrayList<Byte>>> mOriSsidRecords = new HashMap();
    private final Map<String, String> mPmfRecords = new HashMap();

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
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0024, code lost:
        return;
     */
    public synchronized void recordHiLink(String bssid) {
        if (TextUtils.isEmpty(bssid)) {
            Log.d(TAG, "recordHiLinkNetwork: bssid is empty.");
            return;
        }
        String record = bssid.toLowerCase(Locale.ENGLISH);
        if (!this.mHiLinkRecords.contains(record)) {
            this.mHiLinkRecords.add(record);
        }
    }

    public synchronized boolean isHiLink(String bssid) {
        if (TextUtils.isEmpty(bssid)) {
            Log.d(TAG, "isNetworkRecordedAsHiLink: bssid is empty.");
            return false;
        }
        return this.mHiLinkRecords.contains(bssid.toLowerCase(Locale.ENGLISH));
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x005c, code lost:
        return;
     */
    public synchronized void recordOriSsid(String bssid, String ssid, byte[] oriSsid) {
        if (!TextUtils.isEmpty(bssid) && !TextUtils.isEmpty(ssid)) {
            if (!isByteArrayInvalid(oriSsid)) {
                String bssidRecord = bssid.toLowerCase(Locale.ENGLISH);
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
            }
        }
        Log.d(TAG, "recordOriSsid: param is invalid.");
    }

    public synchronized ArrayList<Byte> getOriSsid(String bssid, String ssid) {
        if (!TextUtils.isEmpty(bssid)) {
            if (!TextUtils.isEmpty(ssid)) {
                String bssidRecord = bssid.toLowerCase(Locale.ENGLISH);
                String ssidRecord = NativeUtil.removeEnclosingQuotes(ssid);
                if (!this.mOriSsidRecords.containsKey(bssidRecord)) {
                    Log.d(TAG, "getOriSsid: bssid is not exist in records.");
                    return null;
                } else if (!this.mOriSsidRecords.get(bssidRecord).containsKey(ssidRecord)) {
                    Log.d(TAG, "getOriSsid: ssid is not exist in records.");
                    return null;
                } else {
                    return (ArrayList) this.mOriSsidRecords.get(bssidRecord).get(ssidRecord);
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
                this.mPmfRecords.put(bssid.toLowerCase(Locale.ENGLISH), pmfCapabilities);
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
        String bssidRecord = bssid.toLowerCase(Locale.ENGLISH);
        if (!this.mPmfRecords.containsKey(bssidRecord)) {
            Log.d(TAG, "PMF: do not match");
            return null;
        }
        return this.mPmfRecords.get(bssidRecord);
    }

    private boolean isByteArrayInvalid(byte[] bytes) {
        if (!(bytes == null || bytes.length == 0)) {
            for (byte b : bytes) {
                if (b != 0) {
                    return false;
                }
            }
        }
        return true;
    }
}
