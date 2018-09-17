package com.android.server.wifi.wifipro;

import android.text.TextUtils;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class HwDualBandBlackListManager {
    public static final int BLACKLIST_VALID_TIME = 300000;
    private static final String TAG = "WiFi_PRO";
    private static HwDualBandBlackListManager mHwDualBandBlackListMgr;
    private HashMap<String, DualBandBlackApInfo> mWifiBlackListApInfo = new HashMap();
    private volatile ArrayList<String> mWifiBlacklist = new ArrayList();

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

        private String getBlackApSsid() {
            return this.mSsid;
        }

        private int getBlackApCounter() {
            return this.mCounter;
        }

        private void setBlackApCounter(int counter) {
            this.mCounter = counter;
        }

        private long getAddTime() {
            return this.mAddTime;
        }

        private void setAddTime(long addTime) {
            this.mAddTime = addTime;
        }

        private void setExpireTime(long time) {
            this.mExpireTime = time;
        }

        private long getExpireTIme() {
            return this.mExpireTime;
        }
    }

    class RemoveBlacklistTask extends TimerTask {
        String id;

        public RemoveBlacklistTask(String id) {
            this.id = id;
        }

        public void run() {
            HwDualBandBlackListManager.this.removeWifiBlacklist(this.id);
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

    /* JADX WARNING: Missing block: B:7:0x0010, code:
            return;
     */
    /* JADX WARNING: Missing block: B:16:0x006c, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void addWifiBlacklist(String ssid, boolean needReset) {
        if (!TextUtils.isEmpty(ssid) && !this.mWifiBlacklist.contains(ssid)) {
            Log.w("WiFi_PRO", "addWifiBlacklist + ssid = " + ssid);
            this.mWifiBlacklist.add(ssid);
            if (this.mWifiBlackListApInfo.containsKey(ssid)) {
                DualBandBlackApInfo apInfo = (DualBandBlackApInfo) this.mWifiBlackListApInfo.get(ssid);
                int curCounter = needReset ? 0 : apInfo.getBlackApCounter() + 1;
                int expireTime = 300000 * (curCounter + 1);
                apInfo.setBlackApCounter(curCounter);
                apInfo.setAddTime(System.currentTimeMillis());
                apInfo.setExpireTime((long) expireTime);
                this.mWifiBlackListApInfo.put(ssid, apInfo);
                new Timer().schedule(new RemoveBlacklistTask(ssid), (long) expireTime);
            } else {
                DualBandBlackApInfo newApInfo = new DualBandBlackApInfo(ssid, 0, System.currentTimeMillis(), 300000);
                this.mWifiBlackListApInfo.put(ssid, newApInfo);
                Log.d("WiFi_PRO", "Add new DualBandBlackApInfo : " + newApInfo.getBlackApSsid());
                new Timer().schedule(new RemoveBlacklistTask(ssid), 300000);
            }
        }
    }

    private synchronized void removeWifiBlacklist(String ssid) {
        if (this.mWifiBlacklist.contains(ssid)) {
            Log.w("WiFi_PRO", "removeWifiBlacklist + id = " + ssid);
            this.mWifiBlacklist.remove(ssid);
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

    public synchronized long getExpireTimeForRetry(String ssid) {
        Log.d("WiFi_PRO", "getExpiretime for ssid =" + ssid);
        if (this.mWifiBlackListApInfo.containsKey(ssid)) {
            DualBandBlackApInfo apInfo = (DualBandBlackApInfo) this.mWifiBlackListApInfo.get(ssid);
            return apInfo.getExpireTIme() - (System.currentTimeMillis() - apInfo.getAddTime());
        }
        Log.e("WiFi_PRO", "can not find this ssid in DualBandBlackList");
        return 0;
    }
}
