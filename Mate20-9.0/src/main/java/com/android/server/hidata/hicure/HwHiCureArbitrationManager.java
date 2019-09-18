package com.android.server.hidata.hicure;

import android.content.Context;

public class HwHiCureArbitrationManager {
    public static final String TAG = "HwHiCureArbitrationManager";
    private static HwHiCureArbitrationManager mHwHiCureArbitrationManager = null;
    private Context mContext = null;

    private HwHiCureArbitrationManager(Context context) {
        this.mContext = context;
        HwHiCureActivityObserver.createHwHiCureActivityObserver(context);
    }

    public static synchronized HwHiCureArbitrationManager createInstance(Context context) {
        HwHiCureArbitrationManager hwHiCureArbitrationManager;
        synchronized (HwHiCureArbitrationManager.class) {
            if (mHwHiCureArbitrationManager == null) {
                mHwHiCureArbitrationManager = new HwHiCureArbitrationManager(context);
            }
            hwHiCureArbitrationManager = mHwHiCureArbitrationManager;
        }
        return hwHiCureArbitrationManager;
    }
}
