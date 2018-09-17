package com.android.server.wifi.scanner;

import android.text.TextUtils;
import android.util.Log;
import com.android.server.wifi.util.NativeUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScanResultRecords {
    private static final String TAG = "ScanResultRecords";
    private static ScanResultRecords sInstance = null;
    private final List<String> mHiLinkRecords = new ArrayList();
    private final Map<String, Map<String, ArrayList<Byte>>> mOriSsidRecords = new HashMap();

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
    }

    /* JADX WARNING: Missing block: B:12:0x0024, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void recordHiLink(String bssid) {
        if (TextUtils.isEmpty(bssid)) {
            Log.d(TAG, "recordHiLinkNetwork: bssid is empty.");
            return;
        }
        String record = bssid.toLowerCase();
        if (!this.mHiLinkRecords.contains(record)) {
            this.mHiLinkRecords.add(record);
        }
    }

    public synchronized boolean isHiLink(String bssid) {
        if (TextUtils.isEmpty(bssid)) {
            Log.d(TAG, "isNetworkRecordedAsHiLink: bssid is empty.");
            return false;
        }
        return this.mHiLinkRecords.contains(bssid.toLowerCase());
    }

    /* JADX WARNING: Missing block: B:16:0x0040, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void recordOriSsid(String bssid, String ssid, byte[] oriSsid) {
        if (TextUtils.isEmpty(bssid) || TextUtils.isEmpty(ssid) || isByteArrayInvalid(oriSsid)) {
            Log.d(TAG, "recordOriSsid: param is invalid.");
            return;
        }
        String bssidRecord = bssid.toLowerCase();
        String ssidRecord = NativeUtil.removeEnclosingQuotes(ssid);
        ArrayList<Byte> oriSsidRecord = NativeUtil.byteArrayToArrayList(oriSsid);
        Map<String, ArrayList<Byte>> item;
        if (!this.mOriSsidRecords.containsKey(bssidRecord)) {
            item = new HashMap();
            item.put(ssidRecord, oriSsidRecord);
            this.mOriSsidRecords.put(bssid, item);
        } else if (!((Map) this.mOriSsidRecords.get(bssidRecord)).containsKey(ssidRecord)) {
            item = (Map) this.mOriSsidRecords.get(bssidRecord);
            item.put(ssidRecord, oriSsidRecord);
            this.mOriSsidRecords.remove(bssid);
            this.mOriSsidRecords.put(bssidRecord, item);
        }
    }

    public synchronized ArrayList<Byte> getOriSsid(String bssid, String ssid) {
        if (TextUtils.isEmpty(bssid) || TextUtils.isEmpty(ssid)) {
            Log.d(TAG, "getOriSsid: param is null");
            return null;
        }
        String bssidRecord = bssid.toLowerCase();
        String ssidRecord = NativeUtil.removeEnclosingQuotes(ssid);
        if (!this.mOriSsidRecords.containsKey(bssidRecord)) {
            Log.d(TAG, "getOriSsid: bssid is not exist in records.");
            return null;
        } else if (((Map) this.mOriSsidRecords.get(bssidRecord)).containsKey(ssidRecord)) {
            return (ArrayList) ((Map) this.mOriSsidRecords.get(bssidRecord)).get(ssidRecord);
        } else {
            Log.d(TAG, "getOriSsid: ssid is not exist in records.");
            return null;
        }
    }

    public synchronized void clearOrdSsidRecords() {
        this.mOriSsidRecords.clear();
    }

    private boolean isByteArrayInvalid(byte[] bytes) {
        if (!(bytes == null || bytes.length == 0)) {
            for (byte b : bytes) {
                if (b != (byte) 0) {
                    return false;
                }
            }
        }
        return true;
    }
}
