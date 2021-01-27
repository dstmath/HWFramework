package com.huawei.android.emcom;

import android.emcom.EmcomManager;

public class EmcomManagerExt {
    private static volatile EmcomManagerExt sInstance;

    public static synchronized EmcomManagerExt getInstance() {
        EmcomManagerExt emcomManagerExt;
        synchronized (EmcomManagerExt.class) {
            if (sInstance == null) {
                sInstance = new EmcomManagerExt();
            }
            emcomManagerExt = sInstance;
        }
        return emcomManagerExt;
    }

    public void responseForParaUpgrade(int paratype, int pathtype, int result) {
        EmcomManager.getInstance().responseForParaUpgrade(paratype, pathtype, result);
    }
}
