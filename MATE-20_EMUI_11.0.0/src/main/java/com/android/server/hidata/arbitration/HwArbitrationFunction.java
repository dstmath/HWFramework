package com.android.server.hidata.arbitration;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.Settings;
import android.telephony.HwTelephonyManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Xml;
import android.util.wifi.HwHiLog;
import com.android.server.hidata.appqoe.HwAPPQoEManager;
import com.android.server.hidata.appqoe.HwAPPQoEUtils;
import com.android.server.hidata.appqoe.HwAPPStateInfo;
import com.android.server.wifipro.WifiProCommonUtils;
import huawei.android.net.hwmplink.MpLinkCommonUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.xmlpull.v1.XmlPullParser;

public class HwArbitrationFunction {
    private static final Uri APP_CONTENT_URI = Uri.parse("content://com.huawei.systemserver.networkengine.SmartNetworkProvider/apps_settings");
    private static final String APP_PATH_SETTINGS = "/apps_settings";
    private static final String APP_SELECT_CONDITIONS = "name=?";
    private static final String CONTENT_TOTAL_SWITCH = "total_switch";
    private static final Uri CONTENT_URI = Uri.parse("content://com.huawei.systemserver.networkengine.SmartNetworkProvider/total_settings");
    private static final String CONTENT_VALUE = "value";
    private static final String COUNTRY_CODE_CHINA = "460";
    private static final String PATH_SETTINGS = "/total_settings";
    private static final String SCHEME = "content://";
    private static final String SETTINGS_AUTHORITY = "com.huawei.systemserver.networkengine.SmartNetworkProvider";
    private static final int SWITCH_DEFAULT = -1;
    private static final int SWITCH_OPEN = 1;
    private static final String TAG = (HwArbitrationDEFS.BASE_TAG + HwArbitrationFunction.class.getSimpleName());
    private static int mCurrentDataTech = 0;
    private static int mCurrentServiceState = 1;
    private static boolean mDataRoamingState = false;
    private static boolean mDataTechSuitable = false;
    private static boolean mIsDsDS3 = true;
    private static boolean mIsPvpScene = false;
    private static boolean mIsScreenOn = true;

    public static boolean isAllowMpLink(Context context, int uid, HwAPPStateInfo appInfo) {
        if (isAppLinkTurboEnabled(context, uid)) {
            HwHiLog.d(TAG, false, "LinkTurbo enabled", new Object[0]);
            return false;
        } else if (HwArbitrationManager.isExceedMaxTemperature()) {
            HwHiLog.d(TAG, false, "exceed max temperature", new Object[0]);
            return false;
        } else if (!HwAPPQoEManager.isAppStartMonitor(appInfo, context)) {
            HwHiLog.d(TAG, false, "isAppStartMonitor false", new Object[0]);
            return false;
        } else if (!HwArbitrationCommonUtils.hasSimCard(context)) {
            HwHiLog.d(TAG, false, "No Sim", new Object[0]);
            return false;
        } else if (isInAirplaneMode(context)) {
            HwHiLog.d(TAG, false, "cell in AirplaneMode", new Object[0]);
            return false;
        } else if (!isStateInService()) {
            HwHiLog.d(TAG, false, "SIM not in service", new Object[0]);
            return false;
        } else if (!HwArbitrationCommonUtils.isCellEnable(context)) {
            HwHiLog.d(TAG, false, "Cell is not enabled", new Object[0]);
            return false;
        } else if (!isDataTechSuitableForMplink()) {
            HwHiLog.d(TAG, false, "data tech not suit for mplink", new Object[0]);
            return false;
        } else if (isInVPNMode(context)) {
            HwHiLog.d(TAG, false, "cell in VPNMode", new Object[0]);
            return false;
        } else if (!MpLinkCommonUtils.isMpLinkEnabled(context)) {
            HwHiLog.d(TAG, false, "WLAN+ off", new Object[0]);
            return false;
        } else if (!isCell4Gor3G(context)) {
            HwHiLog.d(TAG, false, "not 4G or 3G", new Object[0]);
            return false;
        } else if (isDataRoaming()) {
            HwHiLog.d(TAG, false, "celluar data is roaming", new Object[0]);
            return false;
        } else if (Integer.MIN_VALUE != uid && isUidPolicyNotAllowCell(uid)) {
            HwHiLog.d(TAG, false, "uid policy is not allow cellar", new Object[0]);
            return false;
        } else if (HwArbitrationCommonUtils.isDefaultPhoneCsCalling(context)) {
            HwHiLog.d(TAG, false, "Default Sim is CS calling", new Object[0]);
            return false;
        } else {
            if (HwArbitrationCommonUtils.isVicePhoneCalling(context)) {
                HwHiLog.d(TAG, false, "Vice Sim is calling", new Object[0]);
                if (!isDsDs3()) {
                    HwHiLog.d(TAG, false, "not DSDS3.X", new Object[0]);
                    return false;
                } else if (HwArbitrationCommonUtils.DEL_DEFAULT_LINK) {
                    HwHiLog.d(TAG, false, "DEL_DEFAULT_LINK is true", new Object[0]);
                    return false;
                } else if (!isDataSubIdEqualDefaultId()) {
                    HwHiLog.d(TAG, false, "DataSubId not equal to DefaultId", new Object[0]);
                    return false;
                }
            }
            if (!isVSimEnabled()) {
                return true;
            }
            HwHiLog.d(TAG, false, "skyTone VSim is Enabled", new Object[0]);
            return false;
        }
    }

    public static boolean isAllowMpLinkInCellMonitorState(Context context) {
        if (WifiProCommonUtils.isWifiProLitePropertyEnabled(context) || !WifiProCommonUtils.isWifiProSwitchOn(context)) {
            HwAPPQoEUtils.logD(TAG, false, "lite version or wifi pro switch off", new Object[0]);
            return false;
        } else if (!HwArbitrationCommonUtils.MAINLAND_REGION) {
            HwHiLog.d(TAG, false, "not in mainland region", new Object[0]);
            return false;
        } else if (!isChina()) {
            HwHiLog.d(TAG, false, "not Chinese operator", new Object[0]);
            return false;
        } else if (!MpLinkCommonUtils.isSupportMpLink()) {
            HwHiLog.d(TAG, false, "not support mplink", new Object[0]);
            return false;
        } else if (isInVPNMode(context)) {
            HwHiLog.d(TAG, false, "cell in VPNMode", new Object[0]);
            return false;
        } else if (isDataRoaming()) {
            HwHiLog.d(TAG, false, "cellular data is roaming", new Object[0]);
            return false;
        } else if (!isDataTechSuitableForMplink()) {
            HwHiLog.d(TAG, false, "data tech not suit for mplink", new Object[0]);
            return false;
        } else if (!isVSimEnabled()) {
            return true;
        } else {
            HwHiLog.d(TAG, false, "skyTone VSim is Enabled", new Object[0]);
            return false;
        }
    }

    private static boolean isDataTechSuitableForMplink() {
        int i = mCurrentDataTech;
        return i == 3 || i == 8 || i == 9 || i == 10 || i == 15 || i == 13 || i == 19 || i == 20;
    }

    public static boolean isChina() {
        String operator = TelephonyManager.getDefault().getNetworkOperator();
        return (operator == null || operator.length() == 0 || !operator.startsWith("460")) ? false : true;
    }

    private static boolean isInAirplaneMode(Context context) {
        try {
            return Settings.Global.getInt(context.getContentResolver(), "airplane_mode_on") == 1;
        } catch (Settings.SettingNotFoundException e) {
            HwHiLog.e(TAG, false, "AirplaneMode error is: %{public}s", new Object[]{e.getMessage()});
            return false;
        }
    }

    public static boolean isInVPNMode(Context context) {
        try {
            return getSettingsSystemBoolean(context.getContentResolver(), "wifipro_network_vpn_state", false);
        } catch (Exception e) {
            HwHiLog.e(TAG, false, "Exception happened while getting VPN mode", new Object[0]);
            return false;
        }
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

    public static int getNetworkID(Context mContext, int network) {
        Network[] networks;
        NetworkCapabilities capabilities;
        int networkType = -1;
        if (network == 800) {
            networkType = getWifiNetwork(mContext);
        }
        if (network == 801) {
            networkType = getCellNetwork(mContext);
        }
        ConnectivityManager mConnectivityManager = (ConnectivityManager) mContext.getSystemService("connectivity");
        if (mConnectivityManager == null || (networks = mConnectivityManager.getAllNetworks()) == null || networks.length == 0) {
            return -1;
        }
        for (Network netItem : networks) {
            NetworkInfo netInfo = mConnectivityManager.getNetworkInfo(netItem);
            if (netInfo != null && netInfo.getType() == networkType && (capabilities = mConnectivityManager.getNetworkCapabilities(netItem)) != null && capabilities.hasCapability(12)) {
                return netItem.netId;
            }
        }
        return -1;
    }

    public static int getNetwork(Context mContext, int netId) {
        int result = -1;
        ConnectivityManager mConnectivityManager = (ConnectivityManager) mContext.getSystemService("connectivity");
        if (!(mConnectivityManager == null || mConnectivityManager.getAllNetworks() == null)) {
            Network[] networks = mConnectivityManager.getAllNetworks();
            int length = networks.length;
            for (int i = 0; i < length; i++) {
                NetworkInfo netInfo = mConnectivityManager.getNetworkInfo(networks[i]);
                Network myNetwork = networks[i];
                if (!(myNetwork == null || myNetwork.netId != netId || netInfo == null)) {
                    result = netInfo.getType();
                }
            }
        }
        if (getWifiNetwork(mContext) == result) {
            return 800;
        }
        if (getCellNetwork(mContext) == result) {
            return 801;
        }
        return 802;
    }

    public static int getWifiNetwork(Context mContext) {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService("connectivity");
        return 1;
    }

    public static int getCellNetwork(Context mContext) {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService("connectivity");
        return 0;
    }

    private static boolean isCell4Gor3G(Context mContext) {
        int networkClass = TelephonyManager.getNetworkClass(((TelephonyManager) mContext.getSystemService("phone")).getNetworkType());
        return networkClass == 2 || networkClass == 3 || networkClass == 4;
    }

    public static boolean isInLTE(Context mContext) {
        int networkType = ((TelephonyManager) mContext.getSystemService("phone")).getNetworkType();
        if (networkType == 13 || networkType == 19 || networkType == 20) {
            return true;
        }
        return false;
    }

    private static boolean isUidPolicyNotAllowCell(int uid) {
        String str;
        Object[] objArr;
        XmlPullParser parser = Xml.newPullParser();
        if (parser == null) {
            HwHiLog.e(TAG, false, "parser is null!!!", new Object[0]);
            return false;
        }
        boolean out = false;
        InputStream inStream = null;
        try {
            InputStream inStream2 = new FileInputStream(new File(HwArbitrationDEFS.UID_POLICY_FILE_PATH));
            parser.setInput(inStream2, "UTF-8");
            for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                if (eventType != 0) {
                    if (eventType == 2) {
                        String name = parser.getName();
                        if (name != null && name.equalsIgnoreCase("uid-policy")) {
                            int xmlUid = Integer.parseInt(parser.getAttributeValue(null, "uid"));
                            int xmlPolicy = Integer.parseInt(parser.getAttributeValue(null, "policy"));
                            if (xmlUid == uid && (xmlPolicy == 1 || xmlPolicy == 2 || xmlPolicy == 3)) {
                                HwArbitrationCommonUtils.logD(TAG, false, "uid: %{public}d, policy: %{public}d", Integer.valueOf(xmlUid), Integer.valueOf(xmlPolicy));
                                out = true;
                            }
                        }
                    }
                }
            }
            try {
                inStream2.close();
            } catch (IOException e3) {
                str = TAG;
                objArr = new Object[]{e3.toString()};
            }
        } catch (RuntimeException e2) {
            HwHiLog.e(TAG, false, "RuntimeException: %{public}s", new Object[]{e2.toString()});
            if (0 != 0) {
                try {
                    inStream.close();
                } catch (IOException e32) {
                    str = TAG;
                    objArr = new Object[]{e32.toString()};
                }
            }
        } catch (Exception e) {
            HwHiLog.e(TAG, false, "Exception happened in isUidPolicyNotAllowCell", new Object[0]);
            if (0 != 0) {
                try {
                    inStream.close();
                } catch (IOException e33) {
                    str = TAG;
                    objArr = new Object[]{e33.toString()};
                }
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    inStream.close();
                } catch (IOException e34) {
                    HwHiLog.e(TAG, false, "IOException: %{public}s", new Object[]{e34.toString()});
                }
            }
            throw th;
        }
        return out;
        HwHiLog.e(str, false, "IOException: %{public}s", objArr);
        return out;
    }

    public static boolean isStreamingScene(HwAPPStateInfo appInfo) {
        if (appInfo == null) {
            return false;
        }
        if (appInfo.mScenceId == 100501 || appInfo.mScenceId == 100901 || appInfo.mScenceId == 100106 || appInfo.mScenceId == 100105 || appInfo.mScenceId == 100701 || appInfo.mScenceId == 101101) {
            return true;
        }
        return false;
    }

    public static void setPvpScene(boolean isPvpScene) {
        mIsPvpScene = isPvpScene;
    }

    public static boolean isPvpScene() {
        return mIsPvpScene;
    }

    public static void setScreenState(boolean isScreenOn) {
        HwHiLog.d(TAG, false, "mIsScreenOn:%{public}s", new Object[]{String.valueOf(isScreenOn)});
        mIsScreenOn = isScreenOn;
    }

    public static boolean isScreenOn() {
        return mIsScreenOn;
    }

    public static void setDataRoamingState(boolean isDataRoaming) {
        HwHiLog.d(TAG, false, "mDataRoamingState:%{public}s", new Object[]{String.valueOf(isDataRoaming)});
        mDataRoamingState = isDataRoaming;
    }

    public static boolean isDataRoaming() {
        return mDataRoamingState;
    }

    public static void setDataTechSuitable(boolean dataTechSuitable) {
        mDataTechSuitable = dataTechSuitable;
        HwHiLog.d(TAG, false, "mDataTechSuitable:%{public}s", new Object[]{String.valueOf(mDataTechSuitable)});
    }

    public static boolean isDataTechSuitable() {
        return mDataTechSuitable;
    }

    public static void setServiceState(int currentServiceState) {
        mCurrentServiceState = currentServiceState;
        HwHiLog.d(TAG, false, "mCurrentServiceState:%{public}d", new Object[]{Integer.valueOf(mCurrentServiceState)});
    }

    public static boolean isStateInService() {
        return mCurrentServiceState == 0;
    }

    public static void setDataTech(int dataTech) {
        mCurrentDataTech = dataTech;
        HwHiLog.d(TAG, false, "mCurrentDataTech:%{public}d", new Object[]{Integer.valueOf(mCurrentDataTech)});
    }

    public static int getDataTech() {
        return mCurrentDataTech;
    }

    public static boolean isDataSubIdEqualDefaultId() {
        int defaultSubId = SubscriptionManager.getDefaultSubId();
        int defaultDataSubId = SubscriptionManager.getDefaultDataSubscriptionId();
        HwArbitrationCommonUtils.logD(TAG, false, "defaultSubId:%{public}d, defaultDataSubId:%{public}d", Integer.valueOf(defaultSubId), Integer.valueOf(defaultDataSubId));
        if (HwArbitrationCommonUtils.isSubIdValid(defaultSubId) && HwArbitrationCommonUtils.isSubIdValid(defaultDataSubId)) {
            return defaultDataSubId == defaultSubId;
        }
        HwArbitrationCommonUtils.logE(TAG, false, "invalid subId", new Object[0]);
        return false;
    }

    public static void setDsDsState(int dsDsState) {
        boolean z = true;
        if (1 != dsDsState) {
            z = false;
        }
        mIsDsDS3 = z;
    }

    public static boolean isDsDs3() {
        HwArbitrationCommonUtils.logD(TAG, false, "isDsDs3.0:%{public}s", String.valueOf(mIsDsDS3));
        return mIsDsDS3;
    }

    private static boolean isVSimEnabled() {
        return HwTelephonyManager.getDefault().isVSimEnabled();
    }

    public static boolean isInMPLink(Context context, int uid) {
        boolean result = false;
        if (HwArbitrationStateMachine.getInstance(context) != null) {
            result = HwArbitrationStateMachine.getInstance(context).isInMPLink(uid);
        }
        HwHiLog.d(TAG, false, "isInMPLink:%{public}s", new Object[]{String.valueOf(result)});
        return result;
    }

    public static boolean isAppLinkTurboEnabled(Context context, int uid) {
        if (context == null) {
            HwHiLog.e(TAG, false, "isAppLinkTurboEnabled context is null.", new Object[0]);
            return false;
        }
        String packageName = MpLinkCommonUtils.getPackageName(context, uid);
        if (packageName == null || packageName.length() == 0) {
            HwHiLog.e(TAG, false, "isAppLinkTurboEnabled pkgName is null or length is 0.", new Object[0]);
            return false;
        } else if (!isTotalLinkTurboEnabled(context)) {
            return false;
        } else {
            int appSwitch = -1;
            Cursor cursor = null;
            try {
                ContentResolver resolver = context.getContentResolver();
                if (resolver == null) {
                    HwHiLog.e(TAG, false, "isAppLinkTurboEnabled resolver is null.", new Object[0]);
                    if (0 != 0) {
                        try {
                            cursor.close();
                        } catch (SQLiteException e) {
                            HwHiLog.e(TAG, false, "isAppLinkTurboEnabled close exception", new Object[0]);
                        }
                    }
                    return false;
                }
                Cursor cursor2 = resolver.query(APP_CONTENT_URI, new String[]{"value"}, APP_SELECT_CONDITIONS, new String[]{packageName}, null);
                if (cursor2 != null && cursor2.getCount() > 0) {
                    int index = cursor2.getColumnIndex("value");
                    cursor2.moveToFirst();
                    appSwitch = cursor2.getInt(index);
                }
                if (cursor2 != null) {
                    try {
                        cursor2.close();
                    } catch (SQLiteException e2) {
                        HwHiLog.e(TAG, false, "isAppLinkTurboEnabled close exception", new Object[0]);
                    }
                }
                HwHiLog.d(TAG, false, "packageName:%{public}s, appSwitch:%{public}d", new Object[]{packageName, Integer.valueOf(appSwitch)});
                if (appSwitch == 1) {
                    return true;
                }
                return false;
            } catch (SQLiteException e3) {
                HwHiLog.e(TAG, false, "isAppLinkTurboEnabled SQLiteException", new Object[0]);
                if (0 != 0) {
                    try {
                        cursor.close();
                    } catch (SQLiteException e4) {
                        HwHiLog.e(TAG, false, "isAppLinkTurboEnabled close exception", new Object[0]);
                    }
                }
                return false;
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        cursor.close();
                    } catch (SQLiteException e5) {
                        HwHiLog.e(TAG, false, "isAppLinkTurboEnabled close exception", new Object[0]);
                    }
                }
                throw th;
            }
        }
    }

    private static boolean isTotalLinkTurboEnabled(Context context) {
        Cursor cursor = null;
        if (context == null) {
            HwHiLog.e(TAG, false, "isTotalLinkTurboEnabled context is null.", new Object[0]);
            return false;
        }
        int totalSwitch = -1;
        try {
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) {
                HwHiLog.e(TAG, false, "isTotalLinkTurboEnabled resolver is null.", new Object[0]);
                if (0 != 0) {
                    try {
                        cursor.close();
                    } catch (SQLiteException e) {
                        HwHiLog.e(TAG, false, "isTotalLinkTurboEnabled close exception", new Object[0]);
                    }
                }
                return false;
            }
            Cursor cursor2 = resolver.query(CONTENT_URI, new String[]{CONTENT_TOTAL_SWITCH}, null, null, null);
            if (cursor2 != null && cursor2.getCount() > 0) {
                int index = cursor2.getColumnIndex("value");
                cursor2.moveToFirst();
                totalSwitch = cursor2.getInt(index);
            }
            if (cursor2 != null) {
                try {
                    cursor2.close();
                } catch (SQLiteException e2) {
                    HwHiLog.e(TAG, false, "isTotalLinkTurboEnabled close exception", new Object[0]);
                }
            }
            if (totalSwitch == 1) {
                return true;
            }
            return false;
        } catch (SQLiteException e3) {
            HwHiLog.e(TAG, false, "isTotalLinkTurboEnabled SQLiteException", new Object[0]);
            if (0 != 0) {
                try {
                    cursor.close();
                } catch (SQLiteException e4) {
                    HwHiLog.e(TAG, false, "isTotalLinkTurboEnabled close exception", new Object[0]);
                }
            }
            return false;
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    cursor.close();
                } catch (SQLiteException e5) {
                    HwHiLog.e(TAG, false, "isTotalLinkTurboEnabled close exception", new Object[0]);
                }
            }
            throw th;
        }
    }
}
