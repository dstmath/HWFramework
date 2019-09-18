package com.android.server.rms.iaware.feature;

import android.content.Context;
import android.os.SystemProperties;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import com.android.server.rms.iaware.IRDataRegister;

public class StartWindowFeature extends RFeature {
    private static final int FEATURE_MIN_VERSION = 3;
    private static final String TAG = "StartWindowFeature";
    private static boolean mFeature = false;
    private static final boolean mRotateOptEnabled = SystemProperties.getBoolean("persist.sys.aware.rotate.enable", true);
    private static final boolean mStartWindowEnabled = SystemProperties.getBoolean("persist.sys.aware.stwin.enable", true);

    public StartWindowFeature(Context context, AwareConstant.FeatureType featureType, IRDataRegister dataRegister) {
        super(context, featureType, dataRegister);
    }

    public boolean enable() {
        AwareLog.i(TAG, "StartWindowFeature is a iaware3.0 feature, don't allow enable!");
        return false;
    }

    public boolean enableFeatureEx(int realVersion) {
        if (realVersion < 3) {
            AwareLog.i(TAG, "the min version of StartWindowFeature is 3, but current version is " + realVersion + ", don't allow enable!");
            return false;
        }
        AwareLog.i(TAG, "StartWindowFeature enabled, mFeature=" + mFeature);
        setEnable(true);
        return true;
    }

    public boolean disable() {
        AwareLog.i(TAG, "StartWindowFeature disabled, mFeature=" + mFeature);
        setEnable(false);
        return true;
    }

    public boolean reportData(CollectData data) {
        return false;
    }

    private static void setEnable(boolean enable) {
        mFeature = enable;
    }

    public static boolean isStartWindowEnable() {
        return mFeature && mStartWindowEnabled;
    }

    public static boolean isRotateOptEnabled() {
        return mFeature && mRotateOptEnabled;
    }
}
