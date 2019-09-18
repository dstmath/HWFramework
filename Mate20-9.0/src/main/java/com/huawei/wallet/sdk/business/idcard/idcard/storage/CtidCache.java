package com.huawei.wallet.sdk.business.idcard.idcard.storage;

import com.huawei.wallet.sdk.common.log.LogC;

public class CtidCache {
    private static final byte[] SYNC_LOCK = new byte[0];
    private static final String TAG = "IDCard:CtidCache";
    private static volatile CtidCache mInstance;
    private String aid = "A0000003330101020063485750415901";
    private boolean supportCtid = false;

    public static CtidCache getInstance() {
        LogC.i(TAG, "getInstance executed", false);
        if (mInstance == null) {
            synchronized (SYNC_LOCK) {
                if (mInstance == null) {
                    mInstance = new CtidCache();
                }
            }
        }
        return mInstance;
    }

    public String getAid() {
        return this.aid;
    }

    public void setAid(String aid2) {
    }

    public boolean isSupportCtid() {
        return this.supportCtid;
    }

    public void setSupportCtid(boolean supportCtid2) {
        this.supportCtid = supportCtid2;
    }
}
