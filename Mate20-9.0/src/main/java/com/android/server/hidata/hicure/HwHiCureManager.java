package com.android.server.hidata.hicure;

import android.content.Context;

public class HwHiCureManager {
    public static final String TAG = "HwHiCureManager";
    private static HwHiCureManager mHiCureManager = null;
    public DnsHiCureEngine mDnsHiCureEngine = null;

    private HwHiCureManager(Context context) {
        modelInit(context);
    }

    public static synchronized HwHiCureManager createInstance(Context context) {
        HwHiCureManager hwHiCureManager;
        synchronized (HwHiCureManager.class) {
            if (mHiCureManager == null) {
                mHiCureManager = new HwHiCureManager(context);
            }
            hwHiCureManager = mHiCureManager;
        }
        return hwHiCureManager;
    }

    private void modelInit(Context context) {
        this.mDnsHiCureEngine = DnsHiCureEngine.getInstance(context);
    }
}
