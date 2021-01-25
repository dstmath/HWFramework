package com.huawei.hwwifiproservice;

import android.text.TextUtils;
import android.util.Log;
import com.android.server.wifi.hwUtil.StringUtilEx;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class HwDualBandBlackListManager {
    public static final int BLACKLIST_VALID_TIME = 120000;
    private static final int PERMANENT_BLACKLIST_CNT = 2;
    private static final int PERMANENT_BLACKLIST_VALID_TIME = 1200000;
    private static final String TAG = "WiFi_PRO";
    private static HwDualBandBlackListManager sHwDualBandBlackListMgr;
    private HashMap<String, DualBandBlackApInfo> mPermanentWifiBlackListApInfo = new HashMap<>();
    private volatile ArrayList<String> mPermanentWifiBlacklist = new ArrayList<>();
    private HashMap<String, DualBandBlackApInfo> mWifiBlackListApInfo = new HashMap<>();
    private volatile ArrayList<String> mWifiBlacklist = new ArrayList<>();

    private HwDualBandBlackListManager() {
    }

    public static HwDualBandBlackListManager getHwDualBandBlackListMgrInstance() {
        if (sHwDualBandBlackListMgr == null) {
            sHwDualBandBlackListMgr = new HwDualBandBlackListManager();
        }
        return sHwDualBandBlackListMgr;
    }

    public ArrayList<String> getWifiBlacklist() {
        return this.mWifiBlacklist;
    }

    public synchronized void addWifiBlacklist(String bssid, boolean needReset) {
        int expireTime;
        if (!TextUtils.isEmpty(bssid)) {
            if (!this.mWifiBlacklist.contains(bssid)) {
                Log.i("WiFi_PRO", "add dualband WifiBlacklist, bssid = " + StringUtilEx.safeDisplayBssid(bssid));
                this.mWifiBlacklist.add(bssid);
                if (this.mWifiBlackListApInfo.containsKey(bssid)) {
                    DualBandBlackApInfo apInfo = this.mWifiBlackListApInfo.get(bssid);
                    int curCounter = needReset ? 0 : apInfo.getBlackApCounter() + 1;
                    Log.i("WiFi_PRO", "addWifiBlacklist, curCounter = " + curCounter + ", needReset = " + needReset);
                    apInfo.setBlackApCounter(curCounter);
                    apInfo.setAddTime(System.currentTimeMillis());
                    if (curCounter >= 2) {
                        expireTime = PERMANENT_BLACKLIST_VALID_TIME;
                        Log.i("WiFi_PRO", "expireTime is 20 minutes");
                    } else {
                        expireTime = 120000;
                        Log.i("WiFi_PRO", "expireTime is 2 minutes");
                    }
                    apInfo.setExpireTime((long) expireTime);
                    this.mWifiBlackListApInfo.put(bssid, apInfo);
                    new Timer().schedule(new RemoveBlacklistTask(bssid), (long) expireTime);
                } else {
                    this.mWifiBlackListApInfo.put(bssid, new DualBandBlackApInfo(bssid, 0, System.currentTimeMillis(), 120000));
                    new Timer().schedule(new RemoveBlacklistTask(bssid), 120000);
                    Log.i("WiFi_PRO", "expireTime is 2 minutes");
                }
            }
        }
    }

    public synchronized void addPermanentWifiBlacklist(String ssid, String bssid) {
        WifiProDualBandApInfoRcd mRecrd;
        if (!TextUtils.isEmpty(ssid)) {
            Log.i("WiFi_PRO", "addPermanentWifiBlacklist ssid : " + StringUtilEx.safeDisplaySsid(ssid));
            if (this.mPermanentWifiBlackListApInfo.containsKey(ssid)) {
                DualBandBlackApInfo apInfo = this.mPermanentWifiBlackListApInfo.get(ssid);
                int curCounter = apInfo.getBlackApCounter() + 1;
                apInfo.setBlackApCounter(curCounter);
                apInfo.setAddTime(System.currentTimeMillis());
                this.mPermanentWifiBlackListApInfo.put(ssid, apInfo);
                Log.i("WiFi_PRO", "curCounter : " + curCounter);
                if (!(bssid == null || curCounter < 2 || (mRecrd = HwDualBandInformationManager.getInstance().getDualBandAPInfo(bssid)) == null)) {
                    mRecrd.mInBlackList = 1;
                    HwDualBandInformationManager.getInstance().updateAPInfo(mRecrd);
                    Log.i("WiFi_PRO", "removePermanentWifiBlacklist ssid :  " + StringUtilEx.safeDisplaySsid(ssid));
                    this.mPermanentWifiBlackListApInfo.remove(ssid);
                }
            } else {
                this.mPermanentWifiBlackListApInfo.put(ssid, new DualBandBlackApInfo(ssid, 1, System.currentTimeMillis(), 0));
            }
            if (!this.mPermanentWifiBlacklist.contains(ssid)) {
                this.mPermanentWifiBlacklist.add(ssid);
                Log.i("WiFi_PRO", "Add PermanentWifiBlacklist for 1200000 minutes ssid " + StringUtilEx.safeDisplaySsid(ssid));
                new Timer().schedule(new RemovePermanentBlacklistTask(ssid), 1200000);
            }
        }
    }

    public synchronized long getPermanentExpireTimeForRetry(String ssid) {
        long j = 0;
        if (TextUtils.isEmpty(ssid)) {
            return 0;
        }
        Log.i("WiFi_PRO", "getPermanentExpireTime for ssid =" + StringUtilEx.safeDisplaySsid(ssid));
        if (this.mPermanentWifiBlackListApInfo.containsKey(ssid)) {
            long expireTime = 1200000 - (System.currentTimeMillis() - this.mPermanentWifiBlackListApInfo.get(ssid).getAddTime());
            if (expireTime <= 0) {
                removePermanentWifiBlacklist(ssid);
            }
            if (expireTime > 0) {
                j = expireTime;
            }
            return j;
        }
        Log.e("WiFi_PRO", "can not find this ssid in PermanentWifiBlackList");
        removePermanentWifiBlacklist(ssid);
        return 0;
    }

    public synchronized boolean isInPermanentWifiBlacklist(String ssid) {
        if (TextUtils.isEmpty(ssid)) {
            return false;
        }
        if (!this.mPermanentWifiBlacklist.contains(ssid)) {
            return false;
        }
        Log.i("WiFi_PRO", "InPermanentWifiBlacklist ssid " + StringUtilEx.safeDisplaySsid(ssid));
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void removePermanentWifiBlacklist(String ssid) {
        if (ssid != null) {
            if (this.mPermanentWifiBlacklist.contains(ssid)) {
                Log.i("WiFi_PRO", "removePermanentWifiBlacklist ssid = " + StringUtilEx.safeDisplaySsid(ssid));
                this.mPermanentWifiBlacklist.remove(ssid);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void removeWifiBlacklist(String bssid) {
        if (bssid != null) {
            if (this.mWifiBlacklist.contains(bssid)) {
                Log.i("WiFi_PRO", "removeWifiBlacklist bssid = " + StringUtilEx.safeDisplayBssid(bssid));
                this.mWifiBlacklist.remove(bssid);
            }
        }
    }

    public synchronized boolean isInWifiBlacklist(String bssid) {
        if (TextUtils.isEmpty(bssid)) {
            return false;
        }
        if (!this.mWifiBlacklist.contains(bssid)) {
            return false;
        }
        Log.i("WiFi_PRO", "InWifiBlacklist bssid " + StringUtilEx.safeDisplayBssid(bssid));
        return true;
    }

    public synchronized void cleanBlacklist() {
        this.mWifiBlacklist.clear();
    }

    public synchronized long getExpireTimeForRetry(String bssid) {
        long j = 0;
        if (TextUtils.isEmpty(bssid)) {
            return 0;
        }
        Log.i("WiFi_PRO", "getExpiretime for bssid = " + StringUtilEx.safeDisplayBssid(bssid));
        if (this.mWifiBlackListApInfo.containsKey(bssid)) {
            DualBandBlackApInfo apInfo = this.mWifiBlackListApInfo.get(bssid);
            long expireRetryTime = apInfo.getExpireTIme() - (System.currentTimeMillis() - apInfo.getAddTime());
            if (expireRetryTime <= 0) {
                this.mWifiBlackListApInfo.remove(bssid);
                removeWifiBlacklist(bssid);
            }
            if (expireRetryTime > 0) {
                j = expireRetryTime;
            }
            return j;
        }
        Log.e("WiFi_PRO", "can not find this bssid " + StringUtilEx.safeDisplayBssid(bssid) + " in DualBandBlackList");
        removeWifiBlacklist(bssid);
        return 0;
    }

    class RemovePermanentBlacklistTask extends TimerTask {
        String id;

        public RemovePermanentBlacklistTask(String id2) {
            this.id = id2;
        }

        @Override // java.util.TimerTask, java.lang.Runnable
        public void run() {
            HwDualBandBlackListManager.this.removePermanentWifiBlacklist(this.id);
        }
    }

    /* access modifiers changed from: package-private */
    public class RemoveBlacklistTask extends TimerTask {
        String id;

        public RemoveBlacklistTask(String id2) {
            this.id = id2;
        }

        @Override // java.util.TimerTask, java.lang.Runnable
        public void run() {
            HwDualBandBlackListManager.this.removeWifiBlacklist(this.id);
        }
    }

    /* access modifiers changed from: package-private */
    public static class DualBandBlackApInfo {
        private long mAddTime;
        private String mBssid;
        private int mCounter;
        private long mExpireTime;

        public DualBandBlackApInfo(String bssid, int counter, long addTime, long expireTime) {
            this.mBssid = bssid;
            this.mCounter = counter;
            this.mAddTime = addTime;
            this.mExpireTime = expireTime;
        }

        private String getBlackApBssid() {
            return this.mBssid;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int getBlackApCounter() {
            return this.mCounter;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setBlackApCounter(int counter) {
            this.mCounter = counter;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private long getAddTime() {
            return this.mAddTime;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setAddTime(long addTime) {
            this.mAddTime = addTime;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setExpireTime(long time) {
            this.mExpireTime = time;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private long getExpireTIme() {
            return this.mExpireTime;
        }
    }
}
