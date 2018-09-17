package com.android.server.wifi.wifipro;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class NetworkBlackListManager {
    public static final int BLACKLIST_VALID_TIME = 90000;
    private static final String TAG = "WiFi_PRO";
    private static final int TYPE_CELL = 2;
    private static final int TYPE_WIFI = 1;
    private static NetworkBlackListManager mNetworkBlackListManager;
    private volatile ArrayList<String> mAbnormalWifiBlacklist = new ArrayList();
    private volatile ArrayList<String> mCellBlacklist = new ArrayList();
    private volatile ArrayList<String> mWifiBlacklist = new ArrayList();

    class removeBlacklistTask extends TimerTask {
        String id;
        int type;

        public removeBlacklistTask(String id, int type) {
            this.type = type;
            this.id = id;
        }

        public void run() {
            if (1 == this.type) {
                NetworkBlackListManager.this.removeWifiBlacklist(this.id);
            } else if (2 == this.type) {
                NetworkBlackListManager.this.removeCellBlacklist(this.id);
            }
        }
    }

    private NetworkBlackListManager(Context context) {
    }

    public static NetworkBlackListManager getNetworkBlackListManagerInstance(Context context) {
        if (mNetworkBlackListManager == null) {
            mNetworkBlackListManager = new NetworkBlackListManager(context);
        }
        return mNetworkBlackListManager;
    }

    public ArrayList<String> getWifiBlacklist() {
        return this.mWifiBlacklist;
    }

    public ArrayList<String> getCellBlacklist() {
        return this.mCellBlacklist;
    }

    public ArrayList<String> getAbnormalWifiBlacklist() {
        return this.mAbnormalWifiBlacklist;
    }

    public synchronized void addWifiBlacklist(String bssid) {
        if (!TextUtils.isEmpty(bssid)) {
            this.mWifiBlacklist.add(bssid);
            new Timer().schedule(new removeBlacklistTask(bssid, 1), 90000);
        }
    }

    public synchronized boolean containedInWifiBlacklists(String targetSsid) {
        if (TextUtils.isEmpty(targetSsid)) {
            return false;
        }
        for (int i = 0; i < this.mWifiBlacklist.size(); i++) {
            if (((String) this.mWifiBlacklist.get(i)).equals("\"" + targetSsid + "\"")) {
                return true;
            }
        }
        return false;
    }

    public synchronized void addCellBlacklist(String cellid) {
        if (!TextUtils.isEmpty(cellid)) {
            Log.w("WiFi_PRO", "addCellBlacklist + id = " + cellid);
            this.mCellBlacklist.add(cellid);
            new Timer().schedule(new removeBlacklistTask(cellid, 2), 90000);
        }
    }

    public synchronized void addAbnormalWifiBlacklist(String bssid) {
        if (!TextUtils.isEmpty(bssid)) {
            this.mAbnormalWifiBlacklist.add(bssid);
        }
    }

    private synchronized void removeWifiBlacklist(String bssid) {
        if (this.mWifiBlacklist.contains(bssid)) {
            this.mWifiBlacklist.remove(bssid);
        }
    }

    private synchronized void removeCellBlacklist(String cellid) {
        if (this.mCellBlacklist.contains(cellid)) {
            Log.w("WiFi_PRO", "removeBlacklistTask + id = " + cellid);
            this.mCellBlacklist.remove(cellid);
        }
    }

    public synchronized void removeAbnormalWifiBlacklist(String bssid) {
        if (this.mAbnormalWifiBlacklist.contains(bssid)) {
            this.mAbnormalWifiBlacklist.remove(bssid);
        }
    }

    public synchronized boolean isInWifiBlacklist(String bssid) {
        if (TextUtils.isEmpty(bssid)) {
            return false;
        }
        return this.mWifiBlacklist.contains(bssid);
    }

    public synchronized boolean isInCellBlacklist(String cellid) {
        if (TextUtils.isEmpty(cellid)) {
            return false;
        }
        return this.mCellBlacklist.contains(cellid);
    }

    public synchronized boolean isInAbnormalWifiBlacklist(String bssid) {
        if (TextUtils.isEmpty(bssid)) {
            return false;
        }
        return this.mAbnormalWifiBlacklist.contains(bssid);
    }

    public synchronized void cleanAbnormalWifiBlacklist() {
        this.mAbnormalWifiBlacklist.clear();
    }

    public synchronized void cleanBlacklist() {
        this.mCellBlacklist.clear();
        this.mWifiBlacklist.clear();
    }
}
