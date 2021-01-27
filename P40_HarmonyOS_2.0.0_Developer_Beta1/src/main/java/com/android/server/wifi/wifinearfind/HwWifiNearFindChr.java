package com.android.server.wifi.wifinearfind;

import android.text.TextUtils;
import android.util.Log;
import huawei.hiview.HiEvent;
import huawei.hiview.HiView;
import java.util.concurrent.ConcurrentHashMap;

public class HwWifiNearFindChr {
    private static final int EVENT_ID_DEVICE_INFO = 909002105;
    private static final int EVENT_ID_STATISTICAL_INFO = 909001015;
    private static final String KEY_COUNT = "COUNT";
    public static final String KEY_COUNT0 = "COUNT0";
    public static final String KEY_COUNT1 = "COUNT1";
    public static final String KEY_COUNT2 = "COUNT2";
    public static final String KEY_COUNT3 = "COUNT3";
    public static final String KEY_COUNT4 = "COUNT4";
    public static final String KEY_COUNT5 = "COUNT5";
    public static final String KEY_COUNT6 = "COUNT6";
    private static final String KEY_NAME = "NAME";
    private static final String KEY_REPORT_DEVICE_COUNT = "REPORTDEVICECOUNT";
    private static final String KEY_RESULT = "RESULT";
    private static final String KEY_SCAN_COUNT = "SCANCOUNT";
    private static final String KEY_SCAN_DEVICE_COUNT = "SCANDEVICECOUNT";
    private static final Object LOCK_OBJECT = new Object();
    private static final String TAG = HwWifiNearFindChr.class.getSimpleName();
    private static final int UNKNOWN_RESULT = 2;
    private static volatile HwWifiNearFindChr sInstance;
    private int mFindScanCount = 0;
    private ConcurrentHashMap<String, Integer> mReportDeviceMap = new ConcurrentHashMap<>();
    private String mReportSsid = "";
    private ConcurrentHashMap<String, Integer> mScanDeviceMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Integer> mScanTimeMap = new ConcurrentHashMap<>();
    private int mTotalScanCount = 0;

    private HwWifiNearFindChr() {
    }

    public static HwWifiNearFindChr getInstance() {
        if (sInstance == null) {
            synchronized (LOCK_OBJECT) {
                if (sInstance == null) {
                    sInstance = new HwWifiNearFindChr();
                }
            }
        }
        return sInstance;
    }

    public void updateReportSsid(String currentSsid) {
        this.mReportSsid = currentSsid;
    }

    public void updateFindScanCount() {
        this.mFindScanCount++;
    }

    public void clearFindScanCount() {
        this.mFindScanCount = 0;
    }

    public void updateTotalScanCount() {
        this.mTotalScanCount++;
    }

    public void updateScanTimeMap(String key, int count) {
        ConcurrentHashMap<String, Integer> concurrentHashMap = this.mScanTimeMap;
        if (concurrentHashMap == null) {
            Log.e(TAG, "updateScanTimeMap, mScanTimeMap is null");
        } else {
            concurrentHashMap.merge(key, Integer.valueOf(count), $$Lambda$HwWifiNearFindChr$Q2DAb4xlCHvDdJqJscY6eIBhw.INSTANCE);
        }
    }

    public void updateScanDeviceMap(String key, int count) {
        ConcurrentHashMap<String, Integer> concurrentHashMap = this.mScanDeviceMap;
        if (concurrentHashMap == null) {
            Log.e(TAG, "updateScanDeviceMap: mScanDeviceMap is null");
        } else {
            concurrentHashMap.merge(key, Integer.valueOf(count), $$Lambda$HwWifiNearFindChr$ji6i8l4LhKIRlbBroFCpR5z3hAw.INSTANCE);
        }
    }

    public void updateReportDeviceMap(String key, int count) {
        ConcurrentHashMap<String, Integer> concurrentHashMap = this.mReportDeviceMap;
        if (concurrentHashMap == null) {
            Log.e(TAG, "updateReportDeviceMap: mReportDeviceMap is null");
        } else {
            concurrentHashMap.merge(key, Integer.valueOf(count), $$Lambda$HwWifiNearFindChr$tyZk6fprtRjGYUFgoa85ADjj8.INSTANCE);
        }
    }

    public void reportDeviceInfoChr() {
        if (TextUtils.isEmpty(this.mReportSsid) || this.mFindScanCount < 0) {
            Log.e(TAG, "reportDeviceInfoChr, mReportSsid is null or mFindScanCount < 0");
            return;
        }
        HiEvent event = new HiEvent((int) EVENT_ID_DEVICE_INFO);
        event.putString(KEY_NAME, this.mReportSsid).putInt(KEY_COUNT, this.mFindScanCount).putInt(KEY_RESULT, 2);
        HiView.report(event);
        String str = TAG;
        Log.d(str, "reportDeviceInfoChr, name = " + this.mReportSsid + ", count = " + this.mFindScanCount + ", result = 2");
        this.mReportSsid = "";
        this.mFindScanCount = 0;
    }

    public void reportScanInfoChr() {
        if (this.mScanTimeMap == null || this.mScanDeviceMap == null || this.mReportDeviceMap == null) {
            Log.e(TAG, "reportScanInfoChr, mScanTimeMap or mScanDeviceMap or mReportDeviceMap is null");
            return;
        }
        HiEvent event = new HiEvent((int) EVENT_ID_STATISTICAL_INFO);
        event.putInt(KEY_COUNT0, this.mScanTimeMap.getOrDefault(KEY_COUNT0, 0).intValue()).putInt(KEY_COUNT1, this.mScanTimeMap.getOrDefault(KEY_COUNT1, 0).intValue()).putInt(KEY_COUNT2, this.mScanTimeMap.getOrDefault(KEY_COUNT2, 0).intValue()).putInt(KEY_COUNT3, this.mScanTimeMap.getOrDefault(KEY_COUNT3, 0).intValue()).putInt(KEY_COUNT4, this.mScanTimeMap.getOrDefault(KEY_COUNT4, 0).intValue()).putInt(KEY_COUNT5, this.mScanTimeMap.getOrDefault(KEY_COUNT5, 0).intValue()).putInt(KEY_COUNT6, this.mScanTimeMap.getOrDefault(KEY_COUNT6, 0).intValue()).putInt(KEY_SCAN_DEVICE_COUNT, this.mScanDeviceMap.size()).putInt(KEY_REPORT_DEVICE_COUNT, this.mReportDeviceMap.size()).putInt(KEY_SCAN_COUNT, this.mTotalScanCount);
        HiView.report(event);
        String str = TAG;
        Log.d(str, "reportScanInfoChr, count0 = " + this.mScanTimeMap.getOrDefault(KEY_COUNT0, 0) + ", count1 = " + this.mScanTimeMap.getOrDefault(KEY_COUNT1, 0) + ", count2 = " + this.mScanTimeMap.getOrDefault(KEY_COUNT2, 0) + ", count3 = " + this.mScanTimeMap.getOrDefault(KEY_COUNT3, 0) + ", count4 = " + this.mScanTimeMap.getOrDefault(KEY_COUNT4, 0) + ", count5 = " + this.mScanTimeMap.getOrDefault(KEY_COUNT5, 0) + ", count6 = " + this.mScanTimeMap.getOrDefault(KEY_COUNT6, 0) + ", scanDeviceCount = " + this.mScanDeviceMap.size() + ", reportDeviceCount = " + this.mReportDeviceMap.size() + ", scanCount = " + this.mTotalScanCount);
        this.mTotalScanCount = 0;
        this.mScanTimeMap.clear();
        this.mScanDeviceMap.clear();
        this.mReportDeviceMap.clear();
    }
}
