package com.android.server.rms.iaware.srms;

import android.content.Context;
import android.os.SystemProperties;
import android.rms.iaware.AwareConstant.FeatureType;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import com.android.server.mtm.iaware.appmng.appstart.AwareAppStartupPolicy;
import com.android.server.rms.iaware.IRDataRegister;
import com.android.server.rms.iaware.feature.RFeature;

public class AppStartupFeature extends RFeature {
    private static final int MIN_VERSION = 2;
    private static final String TAG = "AppStartupFeature";
    private static boolean mBetaUser;
    private static boolean mFeature = SystemProperties.getBoolean("persist.sys.appstart.enable", false);

    static {
        boolean z = true;
        if (SystemProperties.getInt("ro.logsystem.usertype", 1) != 3) {
            z = false;
        }
        mBetaUser = z;
    }

    public AppStartupFeature(Context context, FeatureType type, IRDataRegister dataRegister) {
        super(context, type, dataRegister);
    }

    public static boolean isAppStartupEnabled() {
        return mFeature;
    }

    public static boolean isBetaUser() {
        return mBetaUser;
    }

    public boolean reportData(CollectData data) {
        return true;
    }

    public boolean enable() {
        setEnable(false);
        return false;
    }

    public boolean disable() {
        setEnable(false);
        return true;
    }

    public boolean enableFeatureEx(int realVersion) {
        if (realVersion < 2) {
            setEnable(false);
            return false;
        }
        setEnable(true);
        AwareAppStartupPolicy policy = AwareAppStartupPolicy.self();
        if (policy != null) {
            policy.initSystemUidCache();
        }
        return true;
    }

    public String getBigDataByVersion(int iawareVer, boolean forBeta, boolean clearData) {
        if (!mFeature || iawareVer < 2) {
            AwareLog.e(TAG, "bigdata is not support, mFeature=" + mFeature + ", iawareVer=" + iawareVer);
            return null;
        } else if (mBetaUser == forBeta) {
            return SRMSDumpRadar.getInstance().saveStartupBigData(forBeta, clearData, false);
        } else {
            AwareLog.i(TAG, "request bigdata is not match, betaUser=" + mBetaUser + ", forBeta=" + forBeta + ", clearData=" + clearData);
            return null;
        }
    }

    public static void setEnable(boolean on) {
        AwareLog.i(TAG, "iaware appstartup feature changed: " + mFeature + "->" + on);
        if (mFeature != on) {
            AwareAppStartupPolicy policy = AwareAppStartupPolicy.self();
            if (policy != null) {
                policy.setEnable(on);
            }
            mFeature = on;
        }
    }
}
