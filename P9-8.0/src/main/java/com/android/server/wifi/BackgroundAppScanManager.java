package com.android.server.wifi;

import android.util.ArraySet;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class BackgroundAppScanManager {
    private static final int BLACK_LIST_TYPE_WIFI = 6;
    private static String TAG = "BackgroundAppScanManager";
    private static final int WHITE_LIST_TYPE_WIFI = 2;
    private static BackgroundAppScanManager mBackgroundAppScanManager;
    private ArrayList<BlacklistListener> mBlacklistListenerList = new ArrayList();
    private ArraySet<String> mPkgBlackList = new ArraySet();
    private ArraySet<String> mPkgWhiteList = new ArraySet();
    private ArrayList<WhitelistListener> mWhitelistListenerList = new ArrayList();

    private BackgroundAppScanManager() {
    }

    public static BackgroundAppScanManager getInstance() {
        if (mBackgroundAppScanManager == null) {
            mBackgroundAppScanManager = new BackgroundAppScanManager();
        }
        return mBackgroundAppScanManager;
    }

    public void registerWhiteListChangeListener(WhitelistListener whitelistListener) {
        this.mWhitelistListenerList.add(whitelistListener);
    }

    public void registerBlackListChangeListener(BlacklistListener blacklistListener) {
        this.mBlacklistListenerList.add(blacklistListener);
    }

    public void refreshPackageWhitelist(int type, List<String> pkgList) {
        if (2 == type) {
            this.mPkgWhiteList.clear();
            if (pkgList != null) {
                this.mPkgWhiteList.addAll(pkgList);
            }
            Log.d(TAG, "refreshPackageWhitelist pkgs:" + this.mPkgWhiteList);
            for (WhitelistListener whitelistListener : this.mWhitelistListenerList) {
                whitelistListener.onWhitelistChange(pkgList);
            }
        } else if (6 == type) {
            this.mPkgBlackList.clear();
            if (pkgList != null) {
                this.mPkgBlackList.addAll(pkgList);
            }
            Log.d(TAG, "refreshWifiScanBlacklist pkgs:" + this.mPkgWhiteList);
            for (BlacklistListener blacklistListener : this.mBlacklistListenerList) {
                blacklistListener.onBlacklistChange(pkgList);
            }
        }
    }

    public ArraySet<String> getPackageWhiteList() {
        return this.mPkgWhiteList;
    }

    public ArraySet<String> getPackagBlackList() {
        return this.mPkgBlackList;
    }
}
