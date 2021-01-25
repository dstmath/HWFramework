package com.android.server.rms.iaware.srms;

import android.content.Context;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import com.android.server.mtm.iaware.appmng.appstart.AwareAppStartupPolicy;
import com.android.server.rms.iaware.IRDataRegister;
import com.android.server.rms.iaware.feature.RFeature;
import com.huawei.android.os.SystemPropertiesEx;

public class AppStartupFeature extends RFeature {
    private static final int MIN_VERSION = 2;
    private static final String TAG = "AppStartupFeature";
    private static boolean sBetaUser;
    private static boolean sFeature = SystemPropertiesEx.getBoolean("persist.sys.appstart.enable", false);

    static {
        boolean z = false;
        if (SystemPropertiesEx.getInt("ro.logsystem.usertype", 1) == 3) {
            z = true;
        }
        sBetaUser = z;
    }

    public AppStartupFeature(Context context, AwareConstant.FeatureType type, IRDataRegister dataRegister) {
        super(context, type, dataRegister);
    }

    public static boolean isAppStartupEnabled() {
        return sFeature;
    }

    public static boolean isBetaUser() {
        return sBetaUser;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean reportData(CollectData data) {
        return true;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean enable() {
        setEnable(false);
        return false;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean disable() {
        setEnable(false);
        return true;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
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

    @Override // com.android.server.rms.iaware.feature.RFeature
    public String getBigDataByVersion(int iawareVer, boolean forBeta, boolean clearData) {
        if (!sFeature || iawareVer < 2) {
            AwareLog.e(TAG, "bigdata is not support, sFeature=" + sFeature + ", iawareVer=" + iawareVer);
            return null;
        } else if (sBetaUser == forBeta) {
            return SrmsDumpRadar.getInstance().saveStartupBigData(forBeta, clearData, false);
        } else {
            AwareLog.i(TAG, "request bigdata is not match, betaUser=" + sBetaUser + ", forBeta=" + forBeta + ", clearData=" + clearData);
            return null;
        }
    }

    public static void setEnable(boolean on) {
        AwareLog.i(TAG, "iaware appstartup feature changed: " + sFeature + "->" + on);
        if (sFeature != on) {
            AwareAppStartupPolicy policy = AwareAppStartupPolicy.self();
            if (policy != null) {
                policy.setEnable(on);
            }
            sFeature = on;
        }
    }
}
