package com.android.server.wifi;

import android.content.Context;
import android.text.TextUtils;
import android.util.SparseArray;

class HwCHRWifiUIDWebSpeed extends HwCHRWifiSpeedBaseChecker {
    private static final String TAG = "HwCHRWifiUIDWebSpeed";
    public static final String WEB_UID = "UID";
    private String appName;
    private Context mContext;
    private SparseArray<String> mPackageTables = null;

    public HwCHRWifiUIDWebSpeed(Context context) {
        this.counters.add(new HwCHRWifiHorizontalCounter(WEB_UID));
        this.counters.add(new HwCHRWifiHorizontalCounter(HwCHRWifiSpeedBaseChecker.WEB_SENDSEGS));
        this.counters.add(new HwCHRWifiHorizontalCounter(HwCHRWifiSpeedBaseChecker.WEB_RESENDSEGS));
        this.counters.add(new HwCHRWifiHorizontalCounter(HwCHRWifiSpeedBaseChecker.WEB_RECVSEGS));
        this.counters.add(new HwCHRWifiHorizontalCounter(HwCHRWifiSpeedBaseChecker.WEB_ERRSEGS));
        this.counters.add(new HwCHRWifiHorizontalCounter(HwCHRWifiSpeedBaseChecker.WEB_OUTRSTS));
        this.counters.add(new HwCHRWifiHorizontalCounter(HwCHRWifiSpeedBaseChecker.WEB_ESTRSTS));
        this.counters.add(new HwCHRWifiHorizontalCounter(HwCHRWifiSpeedBaseChecker.WEB_RTT_DURATION));
        this.counters.add(new HwCHRWifiHorizontalCounter(HwCHRWifiSpeedBaseChecker.WEB_RTT_PACKETS));
        this.counters.add(new HwCHRWifiHorizontalCounter(HwCHRWifiSpeedBaseChecker.WEB_DUP_ACKS));
        this.mPackageTables = new SparseArray(50);
        this.mContext = context;
    }

    public void parserValue(String cols, String line) {
        int listSize = this.counters.size();
        for (int i = 0; i < listSize; i++) {
            ((HwCHRWifiCounterInfo) this.counters.get(i)).parserValue(line, cols);
        }
        if (!this.mNeedChecked) {
            this.appName = getPackageName((int) getUID());
        }
        this.age = 2;
    }

    private String getPackageName(int uid) {
        if (uid == -1) {
            return "total";
        }
        int keyIdx = this.mPackageTables.indexOfKey(uid);
        if (keyIdx >= 0) {
            return (String) this.mPackageTables.valueAt(keyIdx);
        }
        String name = this.mContext.getPackageManager().getNameForUid(uid);
        if (TextUtils.isEmpty(name)) {
            name = "unknown:" + uid;
        }
        this.mPackageTables.put(uid, name);
        return name;
    }

    public boolean isSameUID(String line) {
        return line.startsWith(getCounterDetaByTab(WEB_UID) + "\t");
    }

    public long getUID() {
        return getCounterDetaByTab(WEB_UID);
    }

    public String toString() {
        return "HwCHRWifiUIDWebSpeed [appName=" + this.appName + HwCHRWifiCPUUsage.COL_SEP + getSpeedInfo() + "]";
    }
}
