package com.android.server.location;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import com.android.server.location.gnsschrlog.GnssConnectivityLogManager;
import com.android.server.rms.algorithm.utils.IAwareHabitUtils;
import com.android.server.rms.iaware.appmng.AwareAppMngDFX;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.security.securitydiagnose.HwSecDiagnoseConstant;
import com.android.server.security.trustcircle.lifecycle.LifeCycleStateMachine;
import com.android.server.security.trustcircle.utils.ByteUtil;
import com.android.server.wifipro.WifiProCommonDefs;
import com.android.server.wifipro.WifiProCommonUtils;
import huawei.com.android.server.policy.HwGlobalActionsData;

public class HwGnssNetWorkStatus {
    private static final boolean DEBUG = false;
    private static final String TAG = "HwGnssLog_networkStatus";
    private static final int TYPE_WIFI = 100;
    private static final boolean VERBOSE = false;
    private int mCdmaBSid;
    private int mCdmaNid;
    private int mCdmaSid;
    private int mCellid;
    private Context mContext;
    private boolean mDataEnable;
    private int mLac;
    private int mMcc;
    private int mMnc;
    private String mNetworkType;
    private String mWifiBssid;
    private String mWifiSsid;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.location.HwGnssNetWorkStatus.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.location.HwGnssNetWorkStatus.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.location.HwGnssNetWorkStatus.<clinit>():void");
    }

    public HwGnssNetWorkStatus(Context context) {
        this.mNetworkType = AppHibernateCst.INVALID_PKG;
        this.mWifiBssid = AppHibernateCst.INVALID_PKG;
        this.mWifiSsid = AppHibernateCst.INVALID_PKG;
        this.mContext = context;
    }

    private static String networkStatusToString(int networkStatus) {
        String res = "NETWORK_TYPE_UNKNOWN";
        switch (networkStatus) {
            case WifiProCommonUtils.HISTORY_ITEM_NO_INTERNET /*0*/:
                return "NETWORK_TYPE_UNKNOWN";
            case HwGlobalActionsData.FLAG_AIRPLANEMODE_ON /*1*/:
                return "NETWORK_TYPE_GPRS";
            case HwGlobalActionsData.FLAG_AIRPLANEMODE_OFF /*2*/:
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

    public void triggerNetworkRelatedStatus() {
        setMobileInfo();
        setWifiInfo();
        setNetworkType();
    }

    private void setNetworkType() {
        int networkType = 0;
        NetworkInfo networkInfo = ((ConnectivityManager) this.mContext.getSystemService("connectivity")).getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            if (networkInfo.getType() == 1) {
                networkType = TYPE_WIFI;
            } else if (networkInfo.getType() == 0) {
                networkType = networkInfo.getSubtype();
            }
        }
        this.mNetworkType = networkStatusToString(networkType);
    }

    private void setMobileInfo() {
        TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
        CellLocation location = tm.getCellLocation();
        this.mDataEnable = tm.getDataEnabled();
        String operator = tm.getNetworkOperator();
        if (operator != null && operator.length() > 3) {
            this.mMcc = Integer.parseInt(operator.substring(0, 3));
            this.mMnc = Integer.parseInt(operator.substring(3));
            if (DEBUG) {
                Log.d(TAG, "mcc : " + this.mMcc + "mnc : " + this.mMnc);
            }
            if (location instanceof GsmCellLocation) {
                GsmCellLocation gsmLocation = (GsmCellLocation) tm.getCellLocation();
                if (gsmLocation != null) {
                    this.mLac = gsmLocation.getLac();
                    this.mCellid = gsmLocation.getCid();
                }
            } else if (location instanceof CdmaCellLocation) {
                CdmaCellLocation cdmaLocation = (CdmaCellLocation) tm.getCellLocation();
                if (cdmaLocation != null) {
                    this.mCdmaNid = cdmaLocation.getNetworkId();
                    this.mCdmaBSid = cdmaLocation.getBaseStationId();
                    this.mCdmaSid = cdmaLocation.getSystemId();
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
            if (wifiInfo != null) {
                this.mWifiBssid = wifiInfo.getBSSID();
                this.mWifiSsid = wifiInfo.getSSID();
            }
        }
    }

    public String getNetworkType() {
        return this.mNetworkType;
    }

    public boolean getDateEnableStatue() {
        return this.mDataEnable;
    }

    public int getMcc() {
        return this.mMcc;
    }

    public int getMnc() {
        return this.mMnc;
    }

    public int getLac() {
        return this.mLac;
    }

    public int getCellid() {
        return this.mCellid;
    }

    public int getCdmaSid() {
        return this.mCdmaSid;
    }

    public int getCdmaNid() {
        return this.mCdmaNid;
    }

    public int getCdmaBSid() {
        return this.mCdmaBSid;
    }

    public String getWifiBssid() {
        return this.mWifiBssid;
    }

    public String getWifiSsid() {
        return this.mWifiSsid;
    }
}
