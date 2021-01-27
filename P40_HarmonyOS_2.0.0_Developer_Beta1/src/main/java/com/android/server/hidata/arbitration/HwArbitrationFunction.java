package com.android.server.hidata.arbitration;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.wifi.HwHiLog;
import com.android.server.hidata.appqoe.HwAppStateInfo;

public class HwArbitrationFunction {
    private static final String COUNTRY_CODE_CHINA = "460";
    private static final String TAG = (HwArbitrationDefs.BASE_TAG + HwArbitrationFunction.class.getSimpleName());
    private static int sCurrentDataTech = 0;
    private static boolean sDataTechSuitable = false;
    private static boolean sIsPvpScene = false;
    private static boolean sIsScreenOn = true;

    public static boolean isChina() {
        String operator = TelephonyManager.getDefault().getNetworkOperator();
        return (operator == null || operator.length() == 0 || !operator.startsWith("460")) ? false : true;
    }

    public static boolean isInVpnMode(Context context) {
        return getSettingsSystemBoolean(context.getContentResolver(), HwArbitrationDefs.SETTING_SECURE_VPN_WORK_VALUE, false);
    }

    private static boolean getSettingsSystemBoolean(ContentResolver cr, String name, boolean def) {
        return Settings.System.getInt(cr, name, def ? 1 : 0) == 1;
    }

    public static int getCurrentNetwork(Context context, int UID) {
        HwArbitrationCommonUtils.logD(TAG, false, "getCurrentNetwork,UID =%{public}d", Integer.valueOf(UID));
        if (HwArbitrationStateMachine.getInstance(context) != null) {
            return HwArbitrationStateMachine.getInstance(context).getCurrentNetwork(context, UID);
        }
        return HwArbitrationCommonUtils.getActiveConnectType(context);
    }

    public static boolean isStreamingScene(HwAppStateInfo appInfo) {
        if (appInfo == null) {
            return false;
        }
        if (appInfo.mScenesId == 100501 || appInfo.mScenesId == 100901 || appInfo.mScenesId == 100106 || appInfo.mScenesId == 100105 || appInfo.mScenesId == 100701 || appInfo.mScenesId == 101101) {
            return true;
        }
        return false;
    }

    public static void setPvpScene(boolean isPvpScene) {
        sIsPvpScene = isPvpScene;
    }

    public static boolean isPvpScene() {
        return sIsPvpScene;
    }

    public static void setScreenState(boolean isScreenOn) {
        HwHiLog.d(TAG, false, "mIsScreenOn:%{public}s", new Object[]{String.valueOf(isScreenOn)});
        sIsScreenOn = isScreenOn;
    }

    public static boolean isScreenOn() {
        return sIsScreenOn;
    }

    public static void setDataTechSuitable(boolean dataTechSuitable) {
        sDataTechSuitable = dataTechSuitable;
        HwHiLog.d(TAG, false, "mDataTechSuitable:%{public}s", new Object[]{String.valueOf(sDataTechSuitable)});
    }

    public static boolean isDataTechSuitable() {
        return sDataTechSuitable;
    }

    public static void setDataTech(int dataTech) {
        sCurrentDataTech = dataTech;
        HwHiLog.d(TAG, false, "mCurrentDataTech:%{public}d", new Object[]{Integer.valueOf(sCurrentDataTech)});
    }

    public static int getDataTech() {
        return sCurrentDataTech;
    }
}
