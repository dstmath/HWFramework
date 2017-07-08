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
import com.android.server.location.gnsschrlog.CSubBrcm_Assert_Info;
import com.android.server.location.gnsschrlog.CSubData_Delivery_Delay;
import com.android.server.location.gnsschrlog.CSubFirst_Fix_Time_Out;
import com.android.server.location.gnsschrlog.CSubLos_pos_param;
import com.android.server.location.gnsschrlog.CSubNtp_Data_Param;
import com.android.server.location.gnsschrlog.ChrLogBaseModel;
import com.android.server.location.gnsschrlog.GnssChrCommonInfo;
import com.android.server.location.gnsschrlog.GnssConnectivityLogManager;
import java.util.ArrayList;
import java.util.Date;

public class GpsPosErrorEvent {
    public static final int AGPS_CONN_FAILED = 18;
    public static final int AGPS_TIMEOUT = 14;
    private static final int ALMANAC_MASK = 1;
    private static final int DAILY_REPORT = 2;
    public static final int DATA_DELIVERY_DELAY = 17;
    private static final boolean DEBUG = false;
    private static final int EPHEMERIS_MASK = 0;
    public static final int GPSD_NOT_RECOVERY_FAILED = 21;
    public static final int GPS_ADD_BATCHING_FAILED = 10;
    public static final int GPS_ADD_GEOFENCE_FAILED = 9;
    public static final int GPS_BRCM_ASSERT = 30;
    public static final int GPS_CLOSE_GPS_SWITCH_FAILED = 8;
    public static final int GPS_DAILY_CNT_REPORT_FAILD = 25;
    public static final int GPS_INIT_FAILED = 24;
    public static final int GPS_IN_DOOR_FAILED = 20;
    public static final int GPS_LOCAL_DATA_ERR = 29;
    public static final int GPS_LOST_POSITION_FAILED = 11;
    public static final int GPS_LOST_POSITION_UNSURE_FAILED = 23;
    public static final int GPS_LOW_SIGNAL_FAILED = 19;
    public static final int GPS_NTP_DLOAD_FAILED = 4;
    public static final int GPS_NTP_WRONG = 26;
    public static final int GPS_OPEN_GPS_SWITCH_FAILED = 7;
    public static final int GPS_PERMISSION_DENIED = 6;
    private static final int GPS_POS_ERROR_EVENT = 72;
    public static final int GPS_POS_START_FAILED = 1;
    public static final int GPS_POS_STOP_FAILED = 2;
    public static final int GPS_SET_POS_MODE_FAILED = 5;
    public static final int GPS_SUPL_DATA_ERR = 28;
    public static final int GPS_WAKE_LOCK_NOT_RELEASE_FAILED = 12;
    public static final int GPS_XTRA_DATA_ERR = 27;
    public static final int GPS_XTRA_DLOAD_FAILED = 3;
    public static final int HOTSTART_TIMEOUT = 15;
    public static final int LOCATIONPROVIDER_BIND_FAIL = 31;
    public static final String LOCATION_MODE_BATTERY_SAVING = "BATTERY_SAVING";
    public static final String LOCATION_MODE_HIGH_ACCURACY = "HIGH_ACCURACY";
    public static final String LOCATION_MODE_OFF = "LOCATION_OFF";
    public static final String LOCATION_MODE_SENSORS_ONLY = "DEVICE_ONLY";
    public static final int NAVIGATION_ABORT = 16;
    public static final int NETWORK_POSITION_TIMEOUT = 22;
    public static final int STANDALONE_TIMEOUT = 13;
    private static final String TAG = "HwGnssLog_PosErrEvent";
    private static final int TRIGGER_NOW = 1;
    public static final int TYPE_WIFI = 100;
    public static final int UNKNOWN_ISSUE = 0;
    private static final int USED_FOR_FIX_MASK = 2;
    private static final boolean VERBOSE = false;
    private ArrayList mApkList;
    protected GnssChrCommonInfo mChrComInfo;
    public Context mContext;
    CSegEVENT_GPS_POS_ERROR_EVENT mGpsPosErrEvt;

    static class DataDeliveryDelayInfo extends CSubData_Delivery_Delay {
        public DataDeliveryDelayInfo(int difftime) {
            this.iDelayTime.setValue(difftime);
        }
    }

    static class FirstFixTimeoutStatus extends CSubFirst_Fix_Time_Out {
        public FirstFixTimeoutStatus(int svCount, int usedSvCount, String svInfo, int injectParam) {
            this.lTime.setValue(System.currentTimeMillis());
            this.iSvCount.setValue(svCount);
            this.iUsedSvCount.setValue(usedSvCount);
            this.strSvInfo.setValue(svInfo);
            this.ucInjectAiding.setValue(injectParam);
        }
    }

    static class GpsApkName extends CSubApk_Name {
        public GpsApkName(String name, String version) {
            if (name != null) {
                this.strApkName.setValue(name);
                this.strApkVersion.setValue(version);
            }
        }
    }

    static class LostPosParam extends CSubLos_pos_param {
        public LostPosParam(long time, int accuracy, int speed, int svCount, int usedSvCount, String svInfo) {
            this.lTime.setValue(time);
            this.iSpeed.setValue(speed);
            this.iAccuracy.setValue(accuracy);
            this.iSvCount.setValue(svCount);
            this.iUsedSvCount.setValue(usedSvCount);
            this.strSvInfo.setValue(svInfo);
        }
    }

    static class NtpDataParam extends CSubNtp_Data_Param {
        public NtpDataParam(long ntptime, long realtime, String ntpIpAddr) {
            this.lReal_Time.setValue(realtime);
            this.lNtp_Time.setValue(ntptime);
            this.strNtp_IpAddr.setValue(ntpIpAddr);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.location.GpsPosErrorEvent.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.location.GpsPosErrorEvent.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.location.GpsPosErrorEvent.<clinit>():void");
    }

    GpsPosErrorEvent(Context context) {
        this.mApkList = new ArrayList();
        this.mChrComInfo = new GnssChrCommonInfo();
        this.mContext = context;
        this.mGpsPosErrEvt = new CSegEVENT_GPS_POS_ERROR_EVENT();
    }

    public void createNewGpsPosErrorEvent() {
        this.mApkList.clear();
        this.mGpsPosErrEvt = new CSegEVENT_GPS_POS_ERROR_EVENT();
    }

    private static String networkStatusToString(int networkStatus) {
        String res = "NETWORK_TYPE_UNKNOWN";
        switch (networkStatus) {
            case UNKNOWN_ISSUE /*0*/:
                return "NETWORK_TYPE_UNKNOWN";
            case TRIGGER_NOW /*1*/:
                return "NETWORK_TYPE_GPRS";
            case USED_FOR_FIX_MASK /*2*/:
                return "NETWORK_TYPE_EDGE";
            case GPS_XTRA_DLOAD_FAILED /*3*/:
                return "NETWORK_TYPE_UMTS";
            case GPS_NTP_DLOAD_FAILED /*4*/:
                return "NETWORK_TYPE_CDMA";
            case GPS_SET_POS_MODE_FAILED /*5*/:
                return "NETWORK_TYPE_EVDO_0";
            case GPS_PERMISSION_DENIED /*6*/:
                return "NETWORK_TYPE_EVDO_A";
            case GPS_OPEN_GPS_SWITCH_FAILED /*7*/:
                return "NETWORK_TYPE_1xRTT";
            case GPS_CLOSE_GPS_SWITCH_FAILED /*8*/:
                return "NETWORK_TYPE_HSDPA";
            case GPS_ADD_GEOFENCE_FAILED /*9*/:
                return "NETWORK_TYPE_HSUPA";
            case GPS_ADD_BATCHING_FAILED /*10*/:
                return "NETWORK_TYPE_HSPA";
            case GPS_LOST_POSITION_FAILED /*11*/:
                return "NETWORK_TYPE_IDEN";
            case GPS_WAKE_LOCK_NOT_RELEASE_FAILED /*12*/:
                return "NETWORK_TYPE_EVDO_B";
            case STANDALONE_TIMEOUT /*13*/:
                return "NETWORK_TYPE_LTE";
            case AGPS_TIMEOUT /*14*/:
                return "NETWORK_TYPE_EHRPD";
            case HOTSTART_TIMEOUT /*15*/:
                return "NETWORK_TYPE_HSPAP";
            case NAVIGATION_ABORT /*16*/:
                return "NETWORK_TYPE_GSM";
            case DATA_DELIVERY_DELAY /*17*/:
                return "NETWORK_TYPE_TD_SCDMA";
            case AGPS_CONN_FAILED /*18*/:
                return "NETWORK_TYPE_IWLAN";
            case TYPE_WIFI /*100*/:
                return "TYPE_WIFI";
            default:
                return res;
        }
    }

    private void setScreenState() {
        this.mGpsPosErrEvt.strScreenState.setValue(Boolean.toString(((PowerManager) this.mContext.getSystemService("power")).isScreenOn()));
    }

    private void setWifiInfo() {
        WifiManager wifiManager = (WifiManager) this.mContext.getSystemService(GnssConnectivityLogManager.SUBSYS_WIFI);
        if (wifiManager != null) {
            boolean wifiState = wifiManager.isWifiEnabled();
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            this.mGpsPosErrEvt.strWifi_Switch.setValue(Boolean.toString(wifiState));
            if (wifiInfo != null) {
                if (DEBUG) {
                    Log.d(TAG, "SSID = " + wifiInfo.getSSID() + ",,,,,,,,BSSID = " + wifiInfo.getBSSID());
                }
                this.mGpsPosErrEvt.strWifi_Bssid.setValue(wifiInfo.getBSSID());
                this.mGpsPosErrEvt.strWifi_Ssid.setValue(wifiInfo.getSSID());
            }
        }
    }

    private void setMobileInfo() {
        TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
        CellLocation location = tm.getCellLocation();
        this.mGpsPosErrEvt.strDataCall_Switch.setValue(Boolean.toString(tm.getDataEnabled()));
        String operator = tm.getNetworkOperator();
        if (operator != null && operator.length() > GPS_XTRA_DLOAD_FAILED) {
            int mcc = Integer.parseInt(operator.substring(UNKNOWN_ISSUE, GPS_XTRA_DLOAD_FAILED));
            int mnc = Integer.parseInt(operator.substring(GPS_XTRA_DLOAD_FAILED));
            if (DEBUG) {
                Log.d(TAG, "mcc : " + mcc + "mnc : " + mnc);
            }
            this.mGpsPosErrEvt.iCell_Mcc.setValue(mcc);
            this.mGpsPosErrEvt.iCell_Mnc.setValue(mnc);
            if (location instanceof GsmCellLocation) {
                GsmCellLocation gsmLocation = (GsmCellLocation) tm.getCellLocation();
                if (gsmLocation != null) {
                    int lac = gsmLocation.getLac();
                    int cellid = gsmLocation.getCid();
                    this.mGpsPosErrEvt.iCell_Lac.setValue(lac);
                    this.mGpsPosErrEvt.iCell_Cid.setValue(cellid);
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
                    this.mGpsPosErrEvt.iCell_Baseid.setValue(cid);
                    this.mGpsPosErrEvt.usCell_SID.setValue(cdmaMnc);
                    this.mGpsPosErrEvt.usCellN_ID.setValue(cdmaLac);
                    if (DEBUG) {
                        Log.d(TAG, "cid : " + cid + "cdmaMnc : " + cdmaMnc + "cdmaLac : " + cdmaLac);
                    }
                }
            } else {
                Log.d(TAG, "another cell location!do nothing.");
            }
        }
    }

    public void setNetworkAvailable(boolean isNetAvailable) {
        this.mGpsPosErrEvt.strNetWorkAvailable.setValue(Boolean.toString(isNetAvailable));
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
        setScreenState();
        setWifiInfo();
        setMobileInfo();
        HwGnssCommParam hwGnssCommParam = new HwGnssCommParam(this.mContext);
        this.mGpsPosErrEvt.enScreen_Orientation.setValue(hwGnssCommParam.getScreenOrientation());
        this.mGpsPosErrEvt.ucBT_Switch.setValue(hwGnssCommParam.getBtSwitchState());
        this.mGpsPosErrEvt.ucNFC_Switch.setValue(hwGnssCommParam.getNfcSwitchState());
        this.mGpsPosErrEvt.ucUSB_State.setValue(hwGnssCommParam.getUsbConnectState());
        this.mGpsPosErrEvt.ucCardIndex.setValue(this.mChrComInfo.getCardIndex());
        this.mGpsPosErrEvt.tmTimeStamp.setValue(date);
        this.mGpsPosErrEvt.strLocSetStatus.setValue(setGpsSettingStatus());
    }

    public void setFirstFixTimeOutStatus(int svCount, int usedSvCount, String svInfo, int injectParam) {
        this.mGpsPosErrEvt.setCSubFirst_Fix_Time_Out(new FirstFixTimeoutStatus(svCount, usedSvCount, svInfo, injectParam));
    }

    public void setDataDeliveryDelay(int difftime) {
        this.mGpsPosErrEvt.setCSubData_Delivery_Delay(new DataDeliveryDelayInfo(difftime));
    }

    public void setLostPos_SvStatus(long time, int accuracy, int speed, int svCount, int usedSvCount, String svInfo) {
        this.mGpsPosErrEvt.setCSubLos_pos_param(new LostPosParam(time, accuracy, speed, svCount, usedSvCount, svInfo));
    }

    public void setBrcmAssertInfo(String assertInfo) {
        CSubBrcm_Assert_Info brcminfo = new CSubBrcm_Assert_Info();
        brcminfo.strAssertInfo.setValue(assertInfo);
        this.mGpsPosErrEvt.setCSubBrcm_Assert_Info(brcminfo);
    }

    public void setNetworkInfo(int networkstate) {
        this.mGpsPosErrEvt.enNetworkStatus.setValue(networkStatusToString(networkstate));
    }

    public void setNtpErrTime(long ntptime, long realtime, String ntpIpAddr) {
        this.mGpsPosErrEvt.setCSubNtp_Data_Param(new NtpDataParam(ntptime, realtime, ntpIpAddr));
    }

    public void setGpsApkName(String name, String version) {
        if (!this.mApkList.contains(name)) {
            this.mApkList.add(name);
            this.mGpsPosErrEvt.setCSubApk_NameList(new GpsApkName(name, version));
        }
    }

    public void setStartTime(long time) {
        this.mGpsPosErrEvt.lStartTime.setValue(time);
    }

    public void setProvider(String providermode) {
        this.mGpsPosErrEvt.strProviderMode.setValue(providermode);
    }

    public void setPosMode(int mode) {
        this.mGpsPosErrEvt.ucPosMode.setValue(mode);
    }

    public void writePosErrInfo(int errorcode) {
        Date date = new Date();
        setPosErrCommParam();
        this.mGpsPosErrEvt.ucErrorCode.setValue(errorcode);
        ChrLogBaseModel cChrLogBaseModel = this.mGpsPosErrEvt;
        Log.d(TAG, "writePosErrInfo: 72 ,ErrorCode:" + errorcode);
        GnssConnectivityLogManager.getInstance().reportAbnormalEventEx(cChrLogBaseModel, AGPS_TIMEOUT, TRIGGER_NOW, GPS_POS_ERROR_EVENT, date, TRIGGER_NOW, errorcode);
    }
}
