package com.android.server.location;

import android.content.Context;
import android.location.Location;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings.Secure;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.location.gnsschrlog.CSegEVENT_GPS_SESSION_EVENT;
import com.android.server.location.gnsschrlog.CSubApk_Name;
import com.android.server.location.gnsschrlog.CSubBrcmPosReferenceInfo;
import com.android.server.location.gnsschrlog.CSubFixPos_status;
import com.android.server.location.gnsschrlog.CSubLosPos_Status;
import com.android.server.location.gnsschrlog.CSubResumePos_Status;
import com.android.server.location.gnsschrlog.ChrLogBaseModel;
import com.android.server.location.gnsschrlog.GnssConnectivityLogManager;
import com.android.server.rms.iaware.appmng.AwareAppMngDFX;
import com.android.server.security.securitydiagnose.HwSecDiagnoseConstant;
import com.android.server.security.trustcircle.lifecycle.LifeCycleStateMachine;
import com.android.server.security.trustcircle.utils.ByteUtil;
import com.android.server.wifipro.WifiProCommonDefs;
import huawei.com.android.server.policy.HwGlobalActionsData;
import java.util.ArrayList;
import java.util.Date;

public class GpsSessionEvent {
    private static final int ALMANAC_MASK = 1;
    private static final int DAILY_REPORT = 2;
    private static final boolean DEBUG = false;
    private static final int EPHEMERIS_MASK = 0;
    private static final int GPS_SESSION_EVENT = 73;
    private static final int GpsType = 14;
    public static final String LOCATION_MODE_BATTERY_SAVING = "BATTERY_SAVING";
    public static final String LOCATION_MODE_HIGH_ACCURACY = "HIGH_ACCURACY";
    public static final String LOCATION_MODE_OFF = "LOCATION_OFF";
    public static final String LOCATION_MODE_SENSORS_ONLY = "DEVICE_ONLY";
    private static final String TAG = "HwGpsLog_SessionEvent";
    private static final int TRIGGER_NOW = 1;
    public static final int TYPE_WIFI = 100;
    private static final int USED_FOR_FIX_MASK = 2;
    private static final boolean VERBOSE = false;
    private ArrayList mApkList;
    CSubBrcmPosReferenceInfo mBrcmReferenceInfo;
    public Context mContext;
    CSegEVENT_GPS_SESSION_EVENT mGpsSessionEvt;
    HwGnssDftGnssSessionParam mHwGnssDftGnssSessionParam;

    enum FixMode {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.location.GpsSessionEvent.FixMode.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.location.GpsSessionEvent.FixMode.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.location.GpsSessionEvent.FixMode.<clinit>():void");
        }
    }

    static class FixPos_Status extends CSubFixPos_status {
        public FixPos_Status(long time, int svCount, int usedSvCount, String svInfo) {
            this.lFixTime.setValue(time);
            this.iSvCount.setValue(svCount);
            this.iUsedSvCount.setValue(usedSvCount);
            this.strSvInfo.setValue(svInfo);
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

    static class LostPos_Status extends CSubLosPos_Status {
        public LostPos_Status(long time, int accuracy, int speed, int svCount, int usedSvCount, String svInfo) {
            this.lLosPosTime.setValue(time);
            this.iLosPosSpeed.setValue(speed);
            this.iLosPosAccuracy.setValue(accuracy);
            this.iSvCount.setValue(svCount);
            this.iUsedSvCount.setValue(usedSvCount);
            this.strSvInfo.setValue(svInfo);
        }
    }

    static class ResumePos_Status extends CSubResumePos_Status {
        public ResumePos_Status(long time, int accuracy, int speed, int svCount, int usedSvCount, String svInfo) {
            this.strSvInfo.setValue(svInfo);
            this.iSvCount.setValue(svCount);
            this.lResumePosTime.setValue(time);
            this.iResumePosSpeed.setValue(speed);
            this.iUsedSvCount.setValue(usedSvCount);
            this.iResumePosAccuracy.setValue(accuracy);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.location.GpsSessionEvent.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.location.GpsSessionEvent.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.location.GpsSessionEvent.<clinit>():void");
    }

    GpsSessionEvent(Context context) {
        this.mApkList = new ArrayList();
        this.mContext = context;
        this.mGpsSessionEvt = new CSegEVENT_GPS_SESSION_EVENT();
        this.mHwGnssDftGnssSessionParam = new HwGnssDftGnssSessionParam();
        this.mBrcmReferenceInfo = new CSubBrcmPosReferenceInfo();
        Log.d(TAG, "GpsSessionEvent , mGpsSessionEvt is :" + this.mGpsSessionEvt);
    }

    public void createNewGpsSessionEvent() {
        this.mGpsSessionEvt = new CSegEVENT_GPS_SESSION_EVENT();
        this.mBrcmReferenceInfo = new CSubBrcmPosReferenceInfo();
    }

    private static String networkStatusToString(int networkStatus) {
        String res = "UNKNOW";
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
            case GpsType /*14*/:
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
        this.mGpsSessionEvt.strNetWorkAvailable.setValue(Boolean.toString(isNetAvailable));
    }

    private void setWifiInfo() {
        WifiManager wifiManager = (WifiManager) this.mContext.getSystemService(GnssConnectivityLogManager.SUBSYS_WIFI);
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            this.mGpsSessionEvt.strWifi_Switch.setValue(Boolean.toString(wifiManager.isWifiEnabled()));
        }
    }

    private void setMobileInfo() {
        TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
        CellLocation location = tm.getCellLocation();
        this.mGpsSessionEvt.strDataCall_Switch.setValue(Boolean.toString(tm.getDataEnabled()));
        String operator = tm.getNetworkOperator();
        if (operator != null && operator.length() > 3) {
            int mcc = Integer.parseInt(operator.substring(EPHEMERIS_MASK, 3));
            int mnc = Integer.parseInt(operator.substring(3));
            if (DEBUG) {
                Log.d(TAG, "mcc : " + mcc + "mnc : " + mnc);
            }
            this.mGpsSessionEvt.iCell_Mcc.setValue(mcc);
            this.mGpsSessionEvt.iCell_Mnc.setValue(mnc);
            if (location instanceof GsmCellLocation) {
                GsmCellLocation gsmLocation = (GsmCellLocation) tm.getCellLocation();
                if (gsmLocation != null) {
                    this.mGpsSessionEvt.iCell_Lac.setValue(gsmLocation.getLac());
                }
            } else if (location instanceof CdmaCellLocation) {
                CdmaCellLocation cdmaLocation = (CdmaCellLocation) tm.getCellLocation();
                if (cdmaLocation != null) {
                    this.mGpsSessionEvt.usCellN_ID.setValue(cdmaLocation.getNetworkId());
                }
            } else {
                Log.d(TAG, "another cell location!do nothing.");
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

    public void setBrcmReferenceInfo() {
        this.mGpsSessionEvt.setCSubBrcmPosReferenceInfo(this.mBrcmReferenceInfo);
    }

    public void setBrcmPosSource(int posSource) {
        this.mBrcmReferenceInfo.ucPosSource.setValue(posSource);
    }

    public void setBrcmTimeSource(int timeSource) {
        this.mBrcmReferenceInfo.ucTimeSource.setValue(timeSource);
    }

    public void setBrcmAidingStatus(int status) {
        this.mBrcmReferenceInfo.ucAidingStatus.setValue(status);
    }

    public void setBrcmTcxoOffset(int offset) {
        this.mBrcmReferenceInfo.lTcxo_Offset.setValue(offset);
    }

    public void setBrcmAgcData(float gps, float glo, float dbs) {
        this.mBrcmReferenceInfo.strAgc_GPS.setValue(String.valueOf(gps));
        this.mBrcmReferenceInfo.strAgc_GLO.setValue(String.valueOf(glo));
        this.mBrcmReferenceInfo.strAgc_BDS.setValue(String.valueOf(dbs));
    }

    public void setBrcmRestartFlag(boolean isGpsRestarted) {
        this.mGpsSessionEvt.strIsGpsdResart.setValue(Boolean.toString(isGpsRestarted));
    }

    public void setLostPosCnt(int cnt) {
        if (DEBUG) {
            Log.d(TAG, "setLostPosCnt : " + cnt);
        }
        this.mGpsSessionEvt.iLostPosCnt.setValue(cnt);
        this.mHwGnssDftGnssSessionParam.lostPosCnt = cnt;
    }

    public void setReStartCnt(int cnt) {
        this.mGpsSessionEvt.usGpsdReStartCnt.setValue(cnt);
    }

    public void setGpsdRestartFlag(boolean isRestart) {
        if (DEBUG) {
            Log.d(TAG, "setGpsdRestartFlag : " + isRestart);
        }
        this.mGpsSessionEvt.strIsGpsdResart.setValue(Boolean.toString(isRestart));
        this.mHwGnssDftGnssSessionParam.isGpsdResart = isRestart;
    }

    public void setStopTime(long time) {
        if (DEBUG) {
            Log.d(TAG, "setStopTime : " + time);
        }
        this.mGpsSessionEvt.lStopTime.setValue(time);
        this.mHwGnssDftGnssSessionParam.stopTime = time;
    }

    public void setCommFlag(boolean issueSession) {
        if (DEBUG) {
            Log.d(TAG, "setCommFlag : " + issueSession);
        }
        this.mGpsSessionEvt.strIsIssueSession.setValue(Boolean.toString(issueSession));
    }

    public void setAppUsedParm() {
    }

    public void setAvgAcc(int avgPositionAcc) {
        if (DEBUG) {
            Log.d(TAG, "setAvgAcc : " + avgPositionAcc);
        }
        this.mGpsSessionEvt.iAvgPositionAcc.setValue(avgPositionAcc);
    }

    public void setFixLocation(Location location) {
        if (DEBUG) {
            Log.d(TAG, "setFixLocation : ACC" + location.getAccuracy() + ", SPEED" + location.getSpeed());
        }
        this.mGpsSessionEvt.iFixAccuracy.setValue((int) location.getAccuracy());
        this.mGpsSessionEvt.iFixSpeed.setValue((int) location.getSpeed());
    }

    public void setTTFF(int ttff) {
        this.mGpsSessionEvt.iTTFF.setValue(ttff);
        this.mHwGnssDftGnssSessionParam.ttff = ttff;
    }

    public void setFirstCatchSvTime(long time) {
        if (DEBUG) {
            Log.d(TAG, "setFirstCatchSvTime : " + time);
        }
        this.mGpsSessionEvt.lFirstCatchSvTime.setValue(time);
    }

    public void setDrivingAvgCn0(int cn0) {
        this.mGpsSessionEvt.ucAvgCN0When40KMPH.setValue(cn0);
    }

    public void setCatchSvTime(long time) {
        if (DEBUG) {
            Log.d(TAG, "setCatchSvTime : " + time);
        }
        this.mGpsSessionEvt.lCatchSvTime.setValue(time);
        this.mHwGnssDftGnssSessionParam.catchSvTime = time;
    }

    public void setNetworkStatus(int networkStatus) {
        this.mGpsSessionEvt.enNetworkStatus.setValue(networkStatusToString(networkStatus));
    }

    public void setProvider(String providermode) {
        if (DEBUG) {
            Log.d(TAG, "setProvider : " + providermode);
        }
        this.mGpsSessionEvt.strProviderMode.setValue(providermode);
    }

    public void setPosMode(int mode) {
        this.mGpsSessionEvt.ucPosMode.setValue(mode);
    }

    public void setStartTime(long time) {
        if (DEBUG) {
            Log.d(TAG, "set start time ,: " + time);
        }
        this.mGpsSessionEvt.lStartTime.setValue(time);
        this.mHwGnssDftGnssSessionParam.startTime = time;
    }

    public void setInjectParam(int injectParam) {
        this.mGpsSessionEvt.ucInjectAiding.setValue(injectParam);
    }

    public void setGpsApkName(String name, String version) {
        if (DEBUG) {
            Log.d(TAG, "setApkName : " + name + " , version is : " + version);
        }
        if (!this.mApkList.contains(name)) {
            this.mApkList.add(name);
            this.mGpsSessionEvt.setCSubApk_NameList(new GpsApkName(name, version));
        }
    }

    public void setFixPos_SvStatus(long time, int svCount, int usedSvCount, String svInfo) {
        if (DEBUG) {
            Log.d(TAG, "setFixPos_SvStatus ,:time  " + time + " svCount:" + svCount + " ,usedSvCount:" + usedSvCount + " ,svInfo:" + svInfo);
        }
        this.mGpsSessionEvt.setCSubFixPos_status(new FixPos_Status(time, svCount, usedSvCount, svInfo));
    }

    public void setLostPos_SvStatus(long time, int accuracy, int speed, int svCount, int usedSvCount, String svInfo) {
        this.mGpsSessionEvt.setCSubLosPos_StatusList(new LostPos_Status(time, accuracy, speed, svCount, usedSvCount, svInfo));
    }

    public void setResumePos_SvStatus(long time, int accuracy, int speed, int svCount, int usedSvCount, String svInfo) {
        this.mGpsSessionEvt.setCSubResumePos_StatusList(new ResumePos_Status(time, accuracy, speed, svCount, usedSvCount, svInfo));
    }

    private void setCommonPara() {
        Date date = new Date();
        setWifiInfo();
        setMobileInfo();
        setBrcmReferenceInfo();
        this.mGpsSessionEvt.tmTimeStamp.setValue(date);
        this.mGpsSessionEvt.strLocSetStatus.setValue(setGpsSettingStatus());
    }

    public void writeGpsSessionInfo() {
        rptGpsSessionToImonitor();
        Date date = new Date();
        ChrLogBaseModel cChrLogBaseModel = this.mGpsSessionEvt;
        setCommonPara();
        this.mApkList.clear();
        if (DEBUG) {
            Log.d(TAG, "writeGpsSessionInfo, id :73");
        }
        GnssConnectivityLogManager.getInstance().reportAbnormalEventEx(cChrLogBaseModel, GpsType, TRIGGER_NOW, GPS_SESSION_EVENT, date, TRIGGER_NOW);
    }

    private void rptGpsSessionToImonitor() {
        new HwGnssDftManager(this.mContext).sendSessionDataToImonitor(GPS_SESSION_EVENT, this.mHwGnssDftGnssSessionParam);
        this.mHwGnssDftGnssSessionParam.resetParam();
    }
}
