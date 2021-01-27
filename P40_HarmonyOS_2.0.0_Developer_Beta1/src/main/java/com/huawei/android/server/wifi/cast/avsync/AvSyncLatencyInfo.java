package com.huawei.android.server.wifi.cast.avsync;

import android.text.TextUtils;
import android.util.SparseArray;
import android.util.wifi.HwHiLog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AvSyncLatencyInfo {
    private static final int INVALID_LATENCY = -1;
    public static final int SUPPORT_VERSION_MAJOR = 1;
    private static final String TAG = "AvSyncLatencyInfo";
    public static final String TAG_CAST_TYPE = "cast_type";
    public static final String TAG_LABEL = "label";
    public static final String TAG_LATENCY_APP = "latency_app";
    public static final String TAG_LATENCY_BASE = "latency_base";
    public static final String TAG_LATENCY_MS = "latency_ms";
    public static final String TAG_PKG_NAME = "pkg";
    public static final String TAG_VERSION_MAX = "version_max";
    public static final String TAG_VERSION_MIN = "version_min";
    private Map<String, AvSyncLatencyAppInfo> mLatencyAppTable = new HashMap();
    private SparseArray<Integer> mLatencyBaseTable = new SparseArray<>();

    public void addLatencyBaseInfo(int castType, int latencyMs) {
        if (latencyMs != -1) {
            this.mLatencyBaseTable.put(castType, Integer.valueOf(latencyMs));
        }
    }

    public void addLatencyAppInfo(String pkgName, AvSyncLatencyAppInfo latency) {
        if (!TextUtils.isEmpty(pkgName)) {
            this.mLatencyAppTable.put(pkgName, latency);
        }
    }

    public void clearData() {
        SparseArray<Integer> sparseArray = this.mLatencyBaseTable;
        if (sparseArray != null) {
            sparseArray.clear();
        }
        Map<String, AvSyncLatencyAppInfo> map = this.mLatencyAppTable;
        if (map != null) {
            map.clear();
        }
    }

    public int getLatency(String pkgName, int appVersionCode, int castType) {
        int latencyBase;
        AvSyncLatencyAppInfo latencyAppInfo = this.mLatencyAppTable.get(pkgName);
        if (latencyAppInfo == null || castType <= 0) {
            return 0;
        }
        int latencyApp = latencyAppInfo.getAppDetailLatency(appVersionCode);
        if (latencyApp == -1) {
            HwHiLog.i(TAG, false, "not found app latencyAppDetailList", new Object[0]);
            return 0;
        }
        int appLatencyBase = latencyAppInfo.getAppLatencyBase(castType);
        if (appLatencyBase >= 0) {
            latencyBase = appLatencyBase;
        } else {
            latencyBase = this.mLatencyBaseTable.get(castType, 0).intValue();
        }
        return latencyBase + latencyApp;
    }

    public static final class AvSyncLatencyAppInfo {
        private SparseArray<Integer> appLatencyBaseTable = new SparseArray<>();
        private List<AvSyncLatencyAppDetailInfo> appLatencyDetailList = new ArrayList();
        private String pkgName;

        public AvSyncLatencyAppInfo(String pkgName2) {
            this.pkgName = pkgName2;
        }

        public void addAppLatencyBase(int castType, int latencyMs) {
            if (latencyMs != -1) {
                this.appLatencyBaseTable.put(castType, Integer.valueOf(latencyMs));
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int getAppLatencyBase(int castType) {
            return this.appLatencyBaseTable.get(castType, -1).intValue();
        }

        public void addAppDetailLatency(int versionMin, int versionMax, int latencyMs) {
            this.appLatencyDetailList.add(new AvSyncLatencyAppDetailInfo(versionMin, versionMax, latencyMs));
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int getAppDetailLatency(int appVersionCode) {
            for (AvSyncLatencyAppDetailInfo detail : this.appLatencyDetailList) {
                if (detail.versionMin <= appVersionCode && appVersionCode <= detail.versionMax) {
                    return detail.latencyMs;
                }
            }
            return -1;
        }

        /* access modifiers changed from: private */
        public static final class AvSyncLatencyAppDetailInfo {
            private int latencyMs;
            private int versionMax;
            private int versionMin;

            private AvSyncLatencyAppDetailInfo(int versionMin2, int versionMax2, int latencyMs2) {
                this.versionMin = -1;
                this.versionMax = -1;
                this.latencyMs = -1;
                this.versionMin = versionMin2 < 0 ? Integer.MIN_VALUE : versionMin2;
                this.versionMax = versionMax2 < 0 ? Integer.MAX_VALUE : versionMax2;
                this.latencyMs = latencyMs2;
            }
        }
    }
}
