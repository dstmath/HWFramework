package com.android.server.rms.iaware.feature;

import android.content.Context;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import com.android.server.rms.iaware.IRDataRegister;
import com.android.server.rms.iaware.appmng.AwareAppPreloadResourceManager;

public class PreloadResourceFeature extends RFeature {
    private static final int BASE_VERSION = 3;
    private static final String TAG = "PreloadResourceFeature";

    public PreloadResourceFeature(Context context, AwareConstant.FeatureType type, IRDataRegister dataRegister) {
        super(context, type, dataRegister);
    }

    public boolean enable() {
        return false;
    }

    public boolean enableFeatureEx(int realVersion) {
        if (realVersion < 3) {
            AwareLog.i(TAG, "enableFeatureEx failed, realVersion: " + realVersion + ", baseVersion: " + 3);
            return false;
        }
        AwareAppPreloadResourceManager.getInstance().enable();
        AwareLog.d(TAG, "enableFeatureEx realVersion=" + realVersion);
        return true;
    }

    public boolean disable() {
        AwareAppPreloadResourceManager.getInstance().disable();
        AwareLog.d(TAG, "PreloadResourceFeature  disable");
        return true;
    }

    public boolean reportData(CollectData data) {
        return false;
    }
}
