package com.android.server.hidata.arbitration;

import android.content.ContentResolver;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.telephony.HwTelephonyManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Xml;
import com.android.server.hidata.appqoe.HwAPPStateInfo;
import huawei.android.net.hwmplink.MpLinkCommonUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.xmlpull.v1.XmlPullParser;

public class HwArbitrationFunction {
    private static final String COUNTRY_CODE_CHINA = "460";
    private static final String TAG = (HwArbitrationDEFS.BASE_TAG + HwArbitrationFunction.class.getSimpleName());
    private static int mCurrentDataTech = 0;
    private static int mCurrentServiceState = 1;
    private static boolean mDataRoamingState = false;
    private static boolean mDataTechSuitable = false;
    private static boolean mIsDsDS3 = true;
    private static boolean mIsPvpScene = false;
    private static boolean mIsScreenOn = true;

    public static boolean isAllowMpLink(Context context, int uid) {
        if (!HwArbitrationCommonUtils.MAINLAND_REGION) {
            Log.d(TAG, "region is not CN");
            return false;
        } else if (!HwArbitrationCommonUtils.hasSimCard(context)) {
            Log.d(TAG, "No Sim");
            return false;
        } else if (isInAirplaneMode(context)) {
            Log.d(TAG, "cell in AirplaneMode");
            return false;
        } else if (!isStateInService()) {
            Log.d(TAG, "SIM not in service");
            return false;
        } else if (!isChina()) {
            Log.d(TAG, "not China operator");
            return false;
        } else if (!HwArbitrationCommonUtils.isCellEnable(context)) {
            Log.d(TAG, "Cell is not enabled");
            return false;
        } else if (isInVPNMode(context)) {
            Log.d(TAG, "cell in VPNMode");
            return false;
        } else if (!MpLinkCommonUtils.isMpLinkEnabled(context)) {
            Log.d(TAG, "WLAN+ off");
            return false;
        } else if (!isCell4Gor3G(context)) {
            Log.d(TAG, "not 4G or 3G");
            return false;
        } else if (isDataRoaming()) {
            Log.d(TAG, "celluar data is roaming");
            return false;
        } else if (Integer.MIN_VALUE != uid && isUidPolicyNotAllowCell(uid)) {
            Log.d(TAG, "uid policy is not allow cellar");
            return false;
        } else if (HwArbitrationCommonUtils.isDefaultPhoneCSCalling(context)) {
            Log.d(TAG, "Default Sim is CS calling");
            return false;
        } else {
            if (HwArbitrationCommonUtils.isVicePhoneCalling(context)) {
                Log.d(TAG, "Vice Sim is calling");
                if (!isDsDs3()) {
                    Log.d(TAG, "not DSDS3.X");
                    return false;
                } else if (HwArbitrationCommonUtils.DEL_DEFAULT_LINK) {
                    Log.d(TAG, "DEL_DEFAULT_LINK is true");
                    return false;
                } else if (!isDataSubIdEqualDefaultId()) {
                    Log.d(TAG, "DataSubId not equal to DefaultId");
                    return false;
                }
            }
            if (!isVSimEnabled()) {
                return true;
            }
            Log.d(TAG, "skyTone VSim is Enabled");
            return false;
        }
    }

    private static boolean isChina() {
        String operator = TelephonyManager.getDefault().getNetworkOperator();
        return (operator == null || operator.length() == 0 || !operator.startsWith("460")) ? false : true;
    }

    private static boolean isInAirplaneMode(Context context) {
        boolean z = false;
        try {
            if (Settings.Global.getInt(context.getContentResolver(), "airplane_mode_on") == 1) {
                z = true;
            }
            return z;
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, "AirplaneMode error is: " + e.toString());
            return false;
        }
    }

    public static boolean isInVPNMode(Context context) {
        try {
            return getSettingsSystemBoolean(context.getContentResolver(), "wifipro_network_vpn_state", false);
        } catch (Exception e) {
            String str = TAG;
            Log.e(str, "VPN Mode error is: " + e.toString());
            return false;
        }
    }

    private static boolean getSettingsSystemBoolean(ContentResolver cr, String name, boolean def) {
        return Settings.System.getInt(cr, name, def) == 1;
    }

    public static int getCurrentNetwork(Context context, int UID) {
        String str = TAG;
        HwArbitrationCommonUtils.logD(str, "getCurrentNetwork,UID =" + UID);
        if (HwArbitrationStateMachine.getInstance(context) != null) {
            return HwArbitrationStateMachine.getInstance(context).getCurrentNetwork(context, UID);
        }
        return HwArbitrationCommonUtils.getActiveConnectType(context);
    }

    public static int getNetworkID(Context mContext, int network) {
        int networkType = -1;
        if (network == 800) {
            networkType = getWifiNetwork(mContext);
        }
        if (network == 801) {
            networkType = getCellNetwork(mContext);
        }
        ConnectivityManager mConnectivityManager = (ConnectivityManager) mContext.getSystemService("connectivity");
        if (!(mConnectivityManager == null || mConnectivityManager.getAllNetworks() == null)) {
            Network[] networks = mConnectivityManager.getAllNetworks();
            int length = networks.length;
            for (int i = 0; i < length; i++) {
                NetworkInfo netInfo = mConnectivityManager.getNetworkInfo(networks[i]);
                if (netInfo != null && netInfo.getType() == networkType) {
                    Network myNetwork = networks[i];
                    if (myNetwork != null) {
                        return myNetwork.netId;
                    }
                }
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
        mContext.getSystemService("connectivity");
        return 1;
    }

    public static int getCellNetwork(Context mContext) {
        mContext.getSystemService("connectivity");
        return 0;
    }

    private static boolean isCell4Gor3G(Context mContext) {
        int networkClass = TelephonyManager.getNetworkClass(((TelephonyManager) mContext.getSystemService("phone")).getNetworkType());
        return networkClass == 2 || networkClass == 3;
    }

    public static boolean isInLTE(Context mContext) {
        int networkType = ((TelephonyManager) mContext.getSystemService("phone")).getNetworkType();
        if (networkType == 13 || networkType == 19) {
            return true;
        }
        return false;
    }

    private static boolean isUidPolicyNotAllowCell(int uid) {
        String str;
        StringBuilder sb;
        XmlPullParser parser = Xml.newPullParser();
        if (parser == null) {
            Log.e(TAG, "parser is null!!!");
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
                                HwArbitrationCommonUtils.logD(TAG, "uid: " + xmlUid + ", policy: " + xmlPolicy);
                                out = true;
                            }
                        }
                    }
                }
            }
            try {
                inStream2.close();
            } catch (IOException e) {
                e3 = e;
                str = TAG;
                sb = new StringBuilder();
            }
        } catch (RuntimeException e2) {
            Log.e(TAG, "RuntimeException: " + e2.toString());
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e3) {
                    e3 = e3;
                    str = TAG;
                    sb = new StringBuilder();
                }
            }
        } catch (Exception e4) {
            Log.e(TAG, "Exception: " + e4.toString());
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e5) {
                    e3 = e5;
                    str = TAG;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e32) {
                    Log.e(TAG, "IOException: " + e32.toString());
                }
            }
            throw th;
        }
        return out;
        sb.append("IOException: ");
        sb.append(e3.toString());
        Log.e(str, sb.toString());
        return out;
    }

    public static boolean isStreamingScene(HwAPPStateInfo appInfo) {
        if (appInfo == null) {
            return false;
        }
        if (appInfo.mScenceId == 100501 || appInfo.mScenceId == 100106 || appInfo.mScenceId == 100105 || appInfo.mScenceId == 100701) {
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
        String str = TAG;
        Log.d(str, "mIsScreenOn:" + isScreenOn);
        mIsScreenOn = isScreenOn;
    }

    public static boolean isScreenOn() {
        return mIsScreenOn;
    }

    public static void setDataRoamingState(boolean isDataRoaming) {
        String str = TAG;
        Log.d(str, "mDataRoamingState:" + isDataRoaming);
        mDataRoamingState = isDataRoaming;
    }

    public static boolean isDataRoaming() {
        return mDataRoamingState;
    }

    public static void setDataTechSuitable(boolean dataTechSuitable) {
        mDataTechSuitable = dataTechSuitable;
        String str = TAG;
        Log.d(str, "mDataTechSuitable:" + mDataTechSuitable);
    }

    public static boolean isDataTechSuitable() {
        return mDataTechSuitable;
    }

    public static void setServiceState(int currentServiceState) {
        mCurrentServiceState = currentServiceState;
        String str = TAG;
        Log.d(str, "mCurrentServiceState:" + mCurrentServiceState);
    }

    public static boolean isStateInService() {
        return mCurrentServiceState == 0;
    }

    public static void setDataTech(int dataTech) {
        mCurrentDataTech = dataTech;
        String str = TAG;
        Log.d(str, "mCurrentDataTech:" + mCurrentDataTech);
    }

    public static int getDataTech() {
        return mCurrentDataTech;
    }

    public static boolean isDataSubIdEqualDefaultId() {
        int defaultSubId = SubscriptionManager.getDefaultSubId();
        int defaultDataSubId = SubscriptionManager.getDefaultDataSubscriptionId();
        String str = TAG;
        HwArbitrationCommonUtils.logD(str, "defaultSubId:" + defaultSubId + ", defaultDataSubId:" + defaultDataSubId);
        boolean z = false;
        if (!HwArbitrationCommonUtils.isSlotIdValid(defaultSubId) || !HwArbitrationCommonUtils.isSlotIdValid(defaultDataSubId)) {
            HwArbitrationCommonUtils.logE(TAG, "invalid subId");
            return false;
        }
        if (defaultDataSubId == defaultSubId) {
            z = true;
        }
        return z;
    }

    public static void setDsDsState(int dsDsState) {
        boolean z = true;
        if (1 != dsDsState) {
            z = false;
        }
        mIsDsDS3 = z;
    }

    public static boolean isDsDs3() {
        String str = TAG;
        HwArbitrationCommonUtils.logD(str, "isDsDs3.0:" + mIsDsDS3);
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
        String str = TAG;
        Log.d(str, "isInMPLink:" + result);
        return result;
    }
}
