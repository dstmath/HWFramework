package com.android.server.location;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.provider.Settings.Secure;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.location.gnsschrlog.CSegEVENT_GPS_POS_ERROR_EVENT;
import com.android.server.location.gnsschrlog.CSubApk_Name;
import com.android.server.location.gnsschrlog.CSubNetwork_Pos_Timeout;
import com.android.server.location.gnsschrlog.ChrLogBaseModel;
import com.android.server.location.gnsschrlog.GnssChrCommonInfo;
import com.android.server.location.gnsschrlog.GnssConnectivityLogManager;
import com.android.server.rms.algorithm.utils.IAwareHabitUtils;
import com.android.server.rms.iaware.appmng.AwareAppMngDFX;
import com.android.server.security.securitydiagnose.HwSecDiagnoseConstant;
import com.android.server.security.trustcircle.lifecycle.LifeCycleStateMachine;
import com.android.server.security.trustcircle.utils.ByteUtil;
import com.android.server.wifipro.WifiProCommonDefs;
import huawei.com.android.server.policy.HwGlobalActionsData;
import java.util.ArrayList;
import java.util.Date;

public class NetworkPosErrorEvent {
    private static final int ALMANAC_MASK = 1;
    private static final int DAILY_REPORT = 2;
    private static final boolean DEBUG = false;
    private static final int EPHEMERIS_MASK = 0;
    private static final int GPS_POS_ERROR_EVENT = 72;
    public static final String LOCATION_MODE_BATTERY_SAVING = "BATTERY_SAVING";
    public static final String LOCATION_MODE_HIGH_ACCURACY = "HIGH_ACCURACY";
    public static final String LOCATION_MODE_OFF = "LOCATION_OFF";
    public static final String LOCATION_MODE_SENSORS_ONLY = "DEVICE_ONLY";
    public static final int NETWORK_POSITION_TIMEOUT = 22;
    private static final String TAG = "HwGnssLog_PosErrEvent";
    private static final int TRIGGER_NOW = 1;
    public static final int TYPE_WIFI = 100;
    private static final int USED_FOR_FIX_MASK = 2;
    private static final boolean VERBOSE = false;
    private ArrayList mApkList;
    protected GnssChrCommonInfo mChrComInfo;
    public Context mContext;
    CSegEVENT_GPS_POS_ERROR_EVENT mNetworkPosErrEvt;

    static class GpsApkName extends CSubApk_Name {
        public GpsApkName(String name, String version) {
            if (name != null) {
                this.strApkName.setValue(name);
                this.strApkVersion.setValue(version);
            }
        }
    }

    static class NetworkPosTimeoutParam extends CSubNetwork_Pos_Timeout {
        public NetworkPosTimeoutParam(int suberrorcode, boolean IsDataAvailable) {
            this.ucSubErrorCode.setValue(suberrorcode);
            this.strIsDataAvailable.setValue(Boolean.toString(IsDataAvailable));
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.location.NetworkPosErrorEvent.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.location.NetworkPosErrorEvent.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.location.NetworkPosErrorEvent.<clinit>():void");
    }

    NetworkPosErrorEvent(Context context) {
        this.mApkList = new ArrayList();
        this.mChrComInfo = new GnssChrCommonInfo();
        this.mContext = context;
        this.mNetworkPosErrEvt = new CSegEVENT_GPS_POS_ERROR_EVENT();
    }

    private static String networkStatusToString(int networkStatus) {
        String res = "NETWORK_TYPE_UNKNOWN";
        switch (networkStatus) {
            case EPHEMERIS_MASK /*0*/:
                return "NETWORK_TYPE_UNKNOWN";
            case TRIGGER_NOW /*1*/:
                return "NETWORK_TYPE_GPRS";
            case USED_FOR_FIX_MASK /*2*/:
                return "NETWORK_TYPE_EDGE";
            case WifiProCommonDefs.WIFI_SECURITY_PHISHING_FAILED /*3*/:
                return "NETWORK_TYPE_UMTS";
            case HwGlobalActionsData.FLAG_AIRPLANEMODE_TRANSITING /*4*/:
                return "NETWORK_TYPE_CDMA";
            case LifeCycleStateMachine.LOGOUT /*5*/:
                return "NETWORK_TYPE_EVDO_0";
            case LifeCycleStateMachine.DELETE_ACCOUNT /*6*/:
                return "NETWORK_TYPE_EVDO_A";
            case LifeCycleStateMachine.TIME_OUT /*7*/:
                return "NETWORK_TYPE_1xRTT";
            case ByteUtil.LONG_SIZE /*8*/:
                return "NETWORK_TYPE_HSDPA";
            case HwGnssLogHandlerMsgID.UPDATESVSTATUS /*9*/:
                return "NETWORK_TYPE_HSUPA";
            case AwareAppMngDFX.APPLICATION_STARTTYPE_COLD /*10*/:
                return "NETWORK_TYPE_HSPA";
            case AwareAppMngDFX.APPLICATION_STARTTYPE_TOTAL /*11*/:
                return "NETWORK_TYPE_IDEN";
            case HwGnssLogHandlerMsgID.UPDATESETPOSMODE /*12*/:
                return "NETWORK_TYPE_EVDO_B";
            case HwGnssLogHandlerMsgID.PERMISSIONERR /*13*/:
                return "NETWORK_TYPE_LTE";
            case IAwareHabitUtils.HABIT_PROTECT_MAX_TRAIN_COUNTS /*14*/:
                return "NETWORK_TYPE_EHRPD";
            case HwGnssLogHandlerMsgID.ADDGEOFENCESTATUS /*15*/:
                return "NETWORK_TYPE_HSPAP";
            case HwSecDiagnoseConstant.BIT_SU /*16*/:
                return "NETWORK_TYPE_GSM";
            case HwGnssLogHandlerMsgID.UPDATELOSTPOSITION /*17*/:
                return "NETWORK_TYPE_TD_SCDMA";
            case HwGnssLogHandlerMsgID.UPDATEAPKNAME /*18*/:
                return "NETWORK_TYPE_IWLAN";
            case TYPE_WIFI /*100*/:
                return "TYPE_WIFI";
            default:
                return res;
        }
    }

    public void setNetworkAvailable(boolean isNetAvailable) {
        this.mNetworkPosErrEvt.strNetWorkAvailable.setValue(Boolean.toString(isNetAvailable));
    }

    private void setScreenState() {
        this.mNetworkPosErrEvt.strScreenState.setValue(Boolean.toString(((PowerManager) this.mContext.getSystemService("power")).isScreenOn()));
    }

    private void setMobileInfo() {
        TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
        CellLocation location = tm.getCellLocation();
        this.mNetworkPosErrEvt.strDataCall_Switch.setValue(Boolean.toString(tm.getDataEnabled()));
        String operator = tm.getNetworkOperator();
        if (operator != null && operator.length() > 3) {
            int mcc = Integer.parseInt(operator.substring(EPHEMERIS_MASK, 3));
            int mnc = Integer.parseInt(operator.substring(3));
            if (DEBUG) {
                Log.d(TAG, "mcc : " + mcc + "mnc : " + mnc);
            }
            this.mNetworkPosErrEvt.iCell_Mcc.setValue(mcc);
            this.mNetworkPosErrEvt.iCell_Mnc.setValue(mnc);
            if (location instanceof GsmCellLocation) {
                GsmCellLocation gsmLocation = (GsmCellLocation) tm.getCellLocation();
                if (gsmLocation != null) {
                    int lac = gsmLocation.getLac();
                    int cellid = gsmLocation.getCid();
                    this.mNetworkPosErrEvt.iCell_Lac.setValue(lac);
                    this.mNetworkPosErrEvt.iCell_Cid.setValue(cellid);
                    if (DEBUG) {
                        Log.d(TAG, "lac : " + lac + "cellid" + cellid);
                    }
                }
            } else if (location instanceof CdmaCellLocation) {
                CdmaCellLocation cdmaLocation = (CdmaCellLocation) tm.getCellLocation();
                if (cdmaLocation != null) {
                    int cdmaLac = cdmaLocation.getNetworkId();
                    int cid = cdmaLocation.getBaseStationId();
                    int cdmaMnc = cdmaLocation.getSystemId();
                    this.mNetworkPosErrEvt.iCell_Baseid.setValue(cid);
                    this.mNetworkPosErrEvt.usCell_SID.setValue(cdmaMnc);
                    this.mNetworkPosErrEvt.usCellN_ID.setValue(cdmaLac);
                    if (DEBUG) {
                        Log.d(TAG, "cid : " + cid + "cdmaMnc : " + cdmaMnc + "cdmaLac : " + cdmaLac);
                    }
                }
            } else {
                Log.d(TAG, "another cell location!do nothing.");
            }
        }
    }

    private void setWifiInfo() {
        WifiManager wifiManager = (WifiManager) this.mContext.getSystemService(GnssConnectivityLogManager.SUBSYS_WIFI);
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            this.mNetworkPosErrEvt.strWifi_Switch.setValue(Boolean.toString(wifiManager.isWifiEnabled()));
            if (wifiInfo != null) {
                if (DEBUG) {
                    Log.d(TAG, "WIFI info : " + wifiInfo.toString() + "SSID : " + wifiInfo.getSSID() + "BSSID : " + wifiInfo.getBSSID());
                }
                this.mNetworkPosErrEvt.strWifi_Bssid.setValue(wifiInfo.getBSSID());
                this.mNetworkPosErrEvt.strWifi_Ssid.setValue(wifiInfo.getSSID());
            }
        }
    }

    private boolean getAllowedProviders(String provider) {
        return TextUtils.delimitedStringContains(Secure.getString(this.mContext.getContentResolver(), "location_providers_allowed"), ',', provider);
    }

    private String setGpsSettingStatus() {
        boolean gpsEnabled = getAllowedProviders("gps");
        boolean networkEnabled = getAllowedProviders("network");
        if (gpsEnabled && networkEnabled) {
            return LOCATION_MODE_HIGH_ACCURACY;
        }
        if (gpsEnabled) {
            return LOCATION_MODE_SENSORS_ONLY;
        }
        if (networkEnabled) {
            return LOCATION_MODE_BATTERY_SAVING;
        }
        return LOCATION_MODE_OFF;
    }

    private void setPosErrCommParam() {
        Date date = new Date();
        setWifiInfo();
        setScreenState();
        setMobileInfo();
        this.mNetworkPosErrEvt.ucCardIndex.setValue(this.mChrComInfo.getCardIndex());
        this.mNetworkPosErrEvt.tmTimeStamp.setValue(date);
        this.mNetworkPosErrEvt.strLocSetStatus.setValue(setGpsSettingStatus());
    }

    public void setNetworkPosTimeOUTInfo(int subErrorcode, boolean isNetworkAvaiable) {
        this.mNetworkPosErrEvt.setCSubNetwork_Pos_Timeout(new NetworkPosTimeoutParam(subErrorcode, isNetworkAvaiable));
    }

    public void setNetworkInfo(int networkstate) {
        this.mNetworkPosErrEvt.enNetworkStatus.setValue(networkStatusToString(networkstate));
    }

    public void setGpsApkName(String name, String version) {
        if (!this.mApkList.contains(name)) {
            this.mApkList.add(name);
            this.mNetworkPosErrEvt.setCSubApk_NameList(new GpsApkName(name, version));
        }
    }

    public void setStartTime(long time) {
        this.mNetworkPosErrEvt.lStartTime.setValue(time);
    }

    public void setProvider(String providermode) {
        this.mNetworkPosErrEvt.strProviderMode.setValue(providermode);
    }

    public void writeNetworkPosErrInfo() {
        Date date = new Date();
        this.mApkList.clear();
        setPosErrCommParam();
        this.mNetworkPosErrEvt.ucErrorCode.setValue((int) NETWORK_POSITION_TIMEOUT);
        ChrLogBaseModel cChrLogBaseModel = this.mNetworkPosErrEvt;
        Log.d(TAG, "fillPosErrInfo: 72  ,ErrorCode:22");
        GnssConnectivityLogManager.getInstance().reportAbnormalEventEx(cChrLogBaseModel, 14, TRIGGER_NOW, GPS_POS_ERROR_EVENT, date, TRIGGER_NOW);
    }
}
