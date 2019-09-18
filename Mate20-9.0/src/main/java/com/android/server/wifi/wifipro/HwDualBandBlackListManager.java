package com.android.server.wifi.wifipro;

import android.text.TextUtils;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class HwDualBandBlackListManager {
    public static final int BLACKLIST_VALID_TIME = 300000;
    private static final int PERMANENT_BLACKLIST_VALID_TIME = 1200000;
    private static final String TAG = "WiFi_PRO";
    private static HwDualBandBlackListManager mHwDualBandBlackListMgr;
    private HashMap<String, DualBandBlackApInfo> mPermanentWifiBlackListApInfo = new HashMap<>();
    private volatile ArrayList<String> mPermanentWifiBlacklist = new ArrayList<>();
    private HashMap<String, DualBandBlackApInfo> mWifiBlackListApInfo = new HashMap<>();
    private volatile ArrayList<String> mWifiBlacklist = new ArrayList<>();

    static class DualBandBlackApInfo {
        private long mAddTime;
        private int mCounter;
        private long mExpireTime;
        private String mSsid;

        public DualBandBlackApInfo(String ssid, int counter, long addTime, long expireTime) {
            this.mSsid = ssid;
            this.mCounter = counter;
            this.mAddTime = addTime;
            this.mExpireTime = expireTime;
        }

        /* access modifiers changed from: private */
        public String getBlackApSsid() {
            return this.mSsid;
        }

        /* access modifiers changed from: private */
        public int getBlackApCounter() {
            return this.mCounter;
        }

        /* access modifiers changed from: private */
        public void setBlackApCounter(int counter) {
            this.mCounter = counter;
        }

        /* access modifiers changed from: private */
        public long getAddTime() {
            return this.mAddTime;
        }

        /* access modifiers changed from: private */
        public void setAddTime(long addTime) {
            this.mAddTime = addTime;
        }

        /* access modifiers changed from: private */
        public void setExpireTime(long time) {
            this.mExpireTime = time;
        }

        /* access modifiers changed from: private */
        public long getExpireTIme() {
            return this.mExpireTime;
        }
    }

    class RemoveBlacklistTask extends TimerTask {
        String id;

        public RemoveBlacklistTask(String id2) {
            this.id = id2;
        }

        public void run() {
            HwDualBandBlackListManager.this.removeWifiBlacklist(this.id);
        }
    }

    class RemovePermanentBlacklistTask extends TimerTask {
        String id;

        public RemovePermanentBlacklistTask(String id2) {
            this.id = id2;
        }

        public void run() {
            HwDualBandBlackListManager.this.removePermanentWifiBlacklist(this.id);
        }
    }

    private HwDualBandBlackListManager() {
    }

    public static HwDualBandBlackListManager getHwDualBandBlackListMgrInstance() {
        if (mHwDualBandBlackListMgr == null) {
            mHwDualBandBlackListMgr = new HwDualBandBlackListManager();
        }
        return mHwDualBandBlackListMgr;
    }

    public ArrayList<String> getWifiBlacklist() {
        return this.mWifiBlacklist;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x00ae, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x00b0, code lost:
        return;
     */
    public synchronized void addWifiBlacklist(String ssid, boolean needReset) {
        if (!TextUtils.isEmpty(ssid)) {
            if (!this.mWifiBlacklist.contains(ssid)) {
                Log.d("WiFi_PRO", "addWifiBlacklist + ssid = " + ssid);
                this.mWifiBlacklist.add(ssid);
                if (this.mWifiBlackListApInfo.containsKey(ssid)) {
                    DualBandBlackApInfo apInfo = this.mWifiBlackListApInfo.get(ssid);
                    int curCounter = needReset ? 0 : apInfo.getBlackApCounter() + 1;
                    int expireTime = 300000 * (curCounter + 1);
                    apInfo.setBlackApCounter(curCounter);
                    apInfo.setAddTime(System.currentTimeMillis());
                    apInfo.setExpireTime((long) expireTime);
                    this.mWifiBlackListApInfo.put(ssid, apInfo);
                    new Timer().schedule(new RemoveBlacklistTask(ssid), (long) expireTime);
                } else {
                    DualBandBlackApInfo dualBandBlackApInfo = new DualBandBlackApInfo(ssid, 0, System.currentTimeMillis(), 300000);
                    DualBandBlackApInfo newApInfo = dualBandBlackApInfo;
                    this.mWifiBlackListApInfo.put(ssid, newApInfo);
                    Log.d("WiFi_PRO", "Add new DualBandBlackApInfo : " + newApInfo.getBlackApSsid());
                    new Timer().schedule(new RemoveBlacklistTask(ssid), 300000);
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x00c0, code lost:
        return;
     */
    public synchronized void addPermanentWifiBlacklist(String ssid, String bssid) {
        if (!TextUtils.isEmpty(ssid)) {
            Log.d("WiFi_PRO", "addPermanentWifiBlacklist ssid : " + ssid);
            if (this.mPermanentWifiBlackListApInfo.containsKey(ssid)) {
                DualBandBlackApInfo apInfo = this.mPermanentWifiBlackListApInfo.get(ssid);
                int curCounter = apInfo.getBlackApCounter() + 1;
                apInfo.setBlackApCounter(curCounter);
                apInfo.setAddTime(System.currentTimeMillis());
                this.mPermanentWifiBlackListApInfo.put(ssid, apInfo);
                Log.d("WiFi_PRO", "curCounter : " + curCounter);
                if (bssid != null && curCounter >= 2) {
                    WifiProDualBandApInfoRcd mRecrd = HwDualBandInformationManager.getInstance().getDualBandAPInfo(bssid);
                    if (mRecrd != null) {
                        mRecrd.isInBlackList = 1;
                        HwDualBandInformationManager.getInstance().updateAPInfo(mRecrd);
                        Log.d("WiFi_PRO", "removePermanentWifiBlacklist ssid :  " + ssid);
                        this.mPermanentWifiBlackListApInfo.remove(ssid);
                    }
                }
            } else {
                DualBandBlackApInfo newApInfo = new DualBandBlackApInfo(ssid, 1, System.currentTimeMillis(), 0);
                this.mPermanentWifiBlackListApInfo.put(ssid, newApInfo);
            }
            if (!this.mPermanentWifiBlacklist.contains(ssid)) {
                this.mPermanentWifiBlacklist.add(ssid);
                new Timer().schedule(new RemovePermanentBlacklistTask(ssid), 1200000);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x004c, code lost:
        return r1;
     */
    public synchronized long getPermanentExpireTimeForRetry(String ssid) {
        long j = 0;
        if (TextUtils.isEmpty(ssid)) {
            return 0;
        }
        Log.d("WiFi_PRO", "getPermanentExpireTime for ssid =" + ssid);
        if (this.mPermanentWifiBlackListApInfo.containsKey(ssid)) {
            long expireTime = 1200000 - (System.currentTimeMillis() - this.mPermanentWifiBlackListApInfo.get(ssid).getAddTime());
            if (expireTime <= 0) {
                removePermanentWifiBlacklist(ssid);
            }
            if (expireTime > 0) {
                j = expireTime;
            }
        } else {
            Log.e("WiFi_PRO", "can not find this ssid in PermanentWifiBlackList");
            removePermanentWifiBlacklist(ssid);
            return 0;
        }
    }

    public synchronized boolean isInPermanentWifiBlacklist(String ssid) {
        if (TextUtils.isEmpty(ssid)) {
            return false;
        }
        return this.mPermanentWifiBlacklist.contains(ssid);
    }

    /* Debug info: failed to restart local var, previous not found, register: 3 */
    /* access modifiers changed from: private */
    public synchronized void removePermanentWifiBlacklist(String ssid) {
        if (ssid != null) {
            if (this.mPermanentWifiBlacklist.contains(ssid)) {
                Log.d("WiFi_PRO", "removePermanentWifiBlacklist ssid = " + ssid);
                this.mPermanentWifiBlacklist.remove(ssid);
            }
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 3 */
    /* access modifiers changed from: private */
    public synchronized void removeWifiBlacklist(String ssid) {
        if (ssid != null) {
            if (this.mWifiBlacklist.contains(ssid)) {
                Log.d("WiFi_PRO", "removeWifiBlacklist ssid = " + ssid);
                this.mWifiBlacklist.remove(ssid);
            }
        }
    }

    public synchronized boolean isInWifiBlacklist(String ssid) {
        if (TextUtils.isEmpty(ssid)) {
            return false;
        }
        return this.mWifiBlacklist.contains(ssid);
    }

    public synchronized void cleanBlacklist() {
        this.mWifiBlacklist.clear();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0053, code lost:
        return r1;
     */
    public synchronized long getExpireTimeForRetry(String ssid) {
        long j = 0;
        if (TextUtils.isEmpty(ssid)) {
            return 0;
        }
        Log.d("WiFi_PRO", "getExpiretime for ssid =" + ssid);
        if (this.mWifiBlackListApInfo.containsKey(ssid)) {
            DualBandBlackApInfo apInfo = this.mWifiBlackListApInfo.get(ssid);
            long expireRetryTime = apInfo.getExpireTIme() - (System.currentTimeMillis() - apInfo.getAddTime());
            if (expireRetryTime <= 0) {
                this.mWifiBlackListApInfo.remove(ssid);
                removeWifiBlacklist(ssid);
            }
            if (expireRetryTime > 0) {
                j = expireRetryTime;
            }
        } else {
            Log.e("WiFi_PRO", "can not find this ssid in DualBandBlackList");
            removeWifiBlacklist(ssid);
            return 0;
        }
    }
}
