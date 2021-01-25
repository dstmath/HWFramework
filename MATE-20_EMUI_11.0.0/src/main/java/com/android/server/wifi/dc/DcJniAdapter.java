package com.android.server.wifi.dc;

import android.text.TextUtils;
import android.util.wifi.HwHiLog;

public class DcJniAdapter {
    private static final String TAG = "DcJniAdapter";
    private static DcJniAdapter sDcJniAdapter = null;

    private native boolean nativeStartOrStopHiP2p(String str, String str2, boolean z);

    private DcJniAdapter() {
    }

    public static synchronized DcJniAdapter getInstance() {
        DcJniAdapter dcJniAdapter;
        synchronized (DcJniAdapter.class) {
            if (sDcJniAdapter == null) {
                sDcJniAdapter = new DcJniAdapter();
            }
            dcJniAdapter = sDcJniAdapter;
        }
        return dcJniAdapter;
    }

    public synchronized boolean startOrStopHiP2p(String masterIfac, String slaveIfac, boolean isEnable) {
        if (!TextUtils.isEmpty(masterIfac)) {
            if (!TextUtils.isEmpty(slaveIfac)) {
                return nativeStartOrStopHiP2p(masterIfac, slaveIfac, isEnable);
            }
        }
        HwHiLog.e(TAG, false, "masterIfac or slaveIfac is null", new Object[0]);
        return false;
    }
}
