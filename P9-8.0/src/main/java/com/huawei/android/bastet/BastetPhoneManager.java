package com.huawei.android.bastet;

import android.util.Log;

public class BastetPhoneManager extends BastetManager {
    private static final String TAG = "BastetPhoneManager";
    private static BastetPhoneManager sInstance = new BastetPhoneManager();

    public static synchronized BastetPhoneManager getInstance() {
        BastetPhoneManager bastetPhoneManager;
        synchronized (BastetPhoneManager.class) {
            if (sInstance == null) {
                sInstance = new BastetPhoneManager();
            }
            bastetPhoneManager = sInstance;
        }
        return bastetPhoneManager;
    }

    public int configBstBlackList(int action, String[] blacklist, int[] option) throws Exception {
        if (this.mIBastetManager == null) {
            return -1;
        }
        Log.d(TAG, "configBstBlackList");
        return 0;
    }

    public int deleteBstBlackListNum(String[] blacklist) throws Exception {
        if (this.mIBastetManager == null) {
            return -1;
        }
        Log.d(TAG, "deleteBstBlackListNum");
        return 0;
    }

    public int setBstBarredRule(int rule) throws Exception {
        if (this.mIBastetManager == null) {
            return -1;
        }
        Log.d(TAG, "setBstBarredRule");
        return 0;
    }

    public int setBstBarredSwitch(int enable_flag) throws Exception {
        if (this.mIBastetManager == null) {
            return -1;
        }
        Log.d(TAG, "setBstBarredSwitch");
        return 0;
    }

    protected void handleProxyMessage(int proxyId, int err, int ext) {
        Log.d(TAG, "handleProxyMessage");
    }

    protected void onBastetDied() {
        synchronized (this) {
            if (sInstance != null) {
                sInstance = null;
            }
        }
        Log.d(TAG, "bastetd died");
    }
}
