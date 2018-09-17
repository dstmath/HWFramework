package com.huawei.android.iawareperf;

import android.iawareperf.UniPerf;

public final class UniPerfEx {
    private static UniPerfEx mUniPerfEx;
    private static final Object sLock = new Object();
    private UniPerf sUniPerf;

    private UniPerfEx(UniPerf uniPerf) {
        this.sUniPerf = uniPerf;
    }

    public static UniPerfEx getInstance() {
        UniPerfEx uniPerfEx;
        synchronized (sLock) {
            if (mUniPerfEx == null) {
                mUniPerfEx = new UniPerfEx(UniPerf.getInstance());
            }
            uniPerfEx = mUniPerfEx;
        }
        return uniPerfEx;
    }

    public int uniPerfEvent(int cmdId, String pkgName, int... payload) {
        return this.sUniPerf.uniPerfEvent(cmdId, pkgName, payload);
    }
}
