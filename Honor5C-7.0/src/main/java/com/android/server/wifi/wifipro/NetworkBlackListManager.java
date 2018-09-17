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
    private volatile ArrayList<String> mCellBlacklist;
    private volatile ArrayList<String> mWifiBlacklist;

    class removeBlacklistTask extends TimerTask {
        String id;
        int type;

        public removeBlacklistTask(String id, int type) {
            this.type = type;
            this.id = id;
        }

        public void run() {
            if (NetworkBlackListManager.TYPE_WIFI == this.type) {
                NetworkBlackListManager.this.removeWifiBlacklist(this.id);
            } else if (NetworkBlackListManager.TYPE_CELL == this.type) {
                NetworkBlackListManager.this.removeCellBlacklist(this.id);
            }
        }
    }

    private NetworkBlackListManager(Context context) {
        this.mWifiBlacklist = new ArrayList();
        this.mCellBlacklist = new ArrayList();
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

    public synchronized void addWifiBlacklist(String bssid) {
        if (!TextUtils.isEmpty(bssid)) {
            Log.w(TAG, "addWifiBlacklist + id = " + bssid);
            this.mWifiBlacklist.add(bssid);
            new Timer().schedule(new removeBlacklistTask(bssid, TYPE_WIFI), 90000);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized boolean containedInWifiBlacklists(String targetSsid) {
        if (TextUtils.isEmpty(targetSsid)) {
            return false;
        }
        int i = 0;
        while (true) {
            if (i >= this.mWifiBlacklist.size()) {
                return false;
            }
            if (((String) this.mWifiBlacklist.get(i)).equals("\"" + targetSsid + "\"")) {
                return true;
            }
            i += TYPE_WIFI;
        }
    }

    public synchronized void addCellBlacklist(String cellid) {
        if (!TextUtils.isEmpty(cellid)) {
            Log.w(TAG, "addCellBlacklist + id = " + cellid);
            this.mCellBlacklist.add(cellid);
            new Timer().schedule(new removeBlacklistTask(cellid, TYPE_CELL), 90000);
        }
    }

    private synchronized void removeWifiBlacklist(String bssid) {
        if (this.mWifiBlacklist.contains(bssid)) {
            Log.w(TAG, "removeBlacklistTask + id = " + bssid);
            this.mWifiBlacklist.remove(bssid);
        }
    }

    private synchronized void removeCellBlacklist(String cellid) {
        if (this.mCellBlacklist.contains(cellid)) {
            Log.w(TAG, "removeBlacklistTask + id = " + cellid);
            this.mCellBlacklist.remove(cellid);
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

    public synchronized void cleanBlacklist() {
        this.mCellBlacklist.clear();
        this.mWifiBlacklist.clear();
    }
}
