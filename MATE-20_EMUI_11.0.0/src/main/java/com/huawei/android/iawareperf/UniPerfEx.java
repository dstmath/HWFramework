package com.huawei.android.iawareperf;

import android.iawareperf.UniPerf;
import com.huawei.annotation.HwSystemApi;

public final class UniPerfEx {
    private static final Object LOCK = new Object();
    private static UniPerfEx sUniPerfEx;
    private UniPerf mUniPerf;

    private UniPerfEx(UniPerf uniPerf) {
        this.mUniPerf = uniPerf;
    }

    public static UniPerfEx getInstance() {
        UniPerfEx uniPerfEx;
        synchronized (LOCK) {
            if (sUniPerfEx == null) {
                sUniPerfEx = new UniPerfEx(UniPerf.getInstance());
            }
            uniPerfEx = sUniPerfEx;
        }
        return uniPerfEx;
    }

    public int uniPerfEvent(int cmdId, String pkgName, int... payload) {
        return this.mUniPerf.uniPerfEvent(cmdId, pkgName, payload);
    }

    @HwSystemApi
    public int uniPerfSetConfig(int cmdId, int[] uniperfTag, int[] boost) {
        return this.mUniPerf.uniPerfSetConfig(cmdId, uniperfTag, boost);
    }
}
