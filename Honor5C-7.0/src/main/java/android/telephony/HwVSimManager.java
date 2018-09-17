package android.telephony;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.HwTelephonyProperties;
import com.android.internal.telephony.IHwVSim;
import com.android.internal.telephony.IHwVSim.Stub;

public class HwVSimManager {
    private static final boolean IS_DUAL_CMCC_UNICOM_DEVICE = false;
    public static final int MAX_VSIM_MODEM_COUNT_DUAL_SIM = 2;
    public static final int MAX_VSIM_MODEM_COUNT_TRI_SIM = 3;
    public static final int NETWORK_TYPE_1xRTT = 7;
    public static final int NETWORK_TYPE_CDMA = 4;
    public static final int NETWORK_TYPE_EDGE = 2;
    public static final int NETWORK_TYPE_EHRPD = 14;
    public static final int NETWORK_TYPE_EVDO_0 = 5;
    public static final int NETWORK_TYPE_EVDO_A = 6;
    public static final int NETWORK_TYPE_EVDO_B = 12;
    public static final int NETWORK_TYPE_GPRS = 1;
    public static final int NETWORK_TYPE_GSM = 16;
    public static final int NETWORK_TYPE_HSDPA = 8;
    public static final int NETWORK_TYPE_HSPA = 10;
    public static final int NETWORK_TYPE_HSPAP = 15;
    public static final int NETWORK_TYPE_HSUPA = 9;
    public static final int NETWORK_TYPE_IDEN = 11;
    public static final int NETWORK_TYPE_LTE = 13;
    public static final int NETWORK_TYPE_TDS = 17;
    public static final int NETWORK_TYPE_TDS_HSDPA = 18;
    public static final int NETWORK_TYPE_TDS_HSUPA = 19;
    public static final int NETWORK_TYPE_UMTS = 3;
    public static final int NETWORK_TYPE_UNKNOWN = 0;
    private static final String PROP_OVERSEAS_MODE = "persist.radio.overseas_mode";
    private static final String TAG = "HwVSimManager";
    private static final int VSIM_CMCC_DEVICE = 2;
    private static final boolean VSIM_DBG = false;
    private static final int VSIM_DUAL_CMCC_UNICOM_DEVICE = 4;
    private static final int VSIM_ENABLE_RESULT_FAIL = 3;
    private static final int VSIM_FULLNETWORK_DEVICE = 5;
    private static final int VSIM_MODEM_COUNT = 0;
    private static final int VSIM_MODEM_COUNT_DEFAULT = 3;
    public static final int VSIM_OP_ENABLEVSIM = 1;
    public static final int VSIM_OP_ENABLEVSIM_FORHASH = 3;
    public static final int VSIM_OP_SETAPN = 2;
    public static final int VSIM_OP_SETAPN_FORHASH = 4;
    private static final int VSIM_TELECOM_DEVICE = 1;
    private static final int VSIM_UNICOM_DEVICE = 3;
    private static final int VSIM_UNKNOWN_DEVICE = -1;
    private static final boolean VSIM_VDBG = false;
    public static final int VSIM_WORKMODE_HIGH_SPEED = 2;
    public static final int VSIM_WORKMODE_RESERVE_SUB1 = 0;
    public static final int VSIM_WORKMODE_RESERVE_SUB2 = 1;
    private static HwVSimManager sInstance;
    private static final boolean sIsPlatformSupportVSim = false;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.telephony.HwVSimManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.telephony.HwVSimManager.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.telephony.HwVSimManager.<clinit>():void");
    }

    private HwVSimManager() {
    }

    public static HwVSimManager getDefault() {
        return sInstance;
    }

    private IHwVSim getIHwVSim() throws RemoteException {
        IHwVSim iHwVSim = Stub.asInterface(ServiceManager.getService("ihwvsim"));
        if (iHwVSim != null) {
            return iHwVSim;
        }
        throw new RemoteException("getIHwVSim null");
    }

    public boolean isPlatformSupportVsim() {
        return sIsPlatformSupportVSim;
    }

    public int maxVSimModemCount() {
        return VSIM_UNICOM_DEVICE;
    }

    public boolean hasIccCardForVSim(int slotId) {
        if (maxVSimModemCount() == VSIM_UNICOM_DEVICE && slotId == VSIM_WORKMODE_HIGH_SPEED) {
            return hasVSimIccCard();
        }
        return (slotId == 0 || slotId == VSIM_WORKMODE_RESERVE_SUB2) ? VSIM_VDBG : VSIM_VDBG;
    }

    public boolean hasVSimIccCard() {
        try {
            return getIHwVSim().hasVSimIccCard();
        } catch (RemoteException ex) {
            Rlog.e(TAG, "RemoteException ex = " + ex);
            return VSIM_VDBG;
        }
    }

    public int getSimStateForVSim(int slotIdx) {
        if (maxVSimModemCount() == VSIM_UNICOM_DEVICE && slotIdx == VSIM_WORKMODE_HIGH_SPEED) {
            return getVSimState();
        }
        int[] subId = SubscriptionManager.getSubId(slotIdx);
        if (subId == null) {
            return VSIM_WORKMODE_RESERVE_SUB2;
        }
        String prop = TelephonyManager.getTelephonyProperty(subId[VSIM_WORKMODE_RESERVE_SUB1], "gsm.sim.state", "");
        if ("ABSENT".equals(prop)) {
            return VSIM_WORKMODE_RESERVE_SUB2;
        }
        if ("PIN_REQUIRED".equals(prop)) {
            return VSIM_WORKMODE_HIGH_SPEED;
        }
        if ("PUK_REQUIRED".equals(prop)) {
            return VSIM_UNICOM_DEVICE;
        }
        if ("NETWORK_LOCKED".equals(prop)) {
            return VSIM_OP_SETAPN_FORHASH;
        }
        if ("READY".equals(prop)) {
            return VSIM_FULLNETWORK_DEVICE;
        }
        if ("CARD_IO_ERROR".equals(prop)) {
            return NETWORK_TYPE_HSDPA;
        }
        return VSIM_WORKMODE_RESERVE_SUB1;
    }

    public int getVSimState() {
        String prop = SystemProperties.get("gsm.vsim.state");
        if ("ABSENT".equals(prop)) {
            return VSIM_WORKMODE_RESERVE_SUB2;
        }
        if ("PIN_REQUIRED".equals(prop)) {
            return VSIM_WORKMODE_HIGH_SPEED;
        }
        if ("PUK_REQUIRED".equals(prop)) {
            return VSIM_UNICOM_DEVICE;
        }
        if ("NETWORK_LOCKED".equals(prop)) {
            return VSIM_OP_SETAPN_FORHASH;
        }
        if ("READY".equals(prop)) {
            return VSIM_FULLNETWORK_DEVICE;
        }
        if ("CARD_IO_ERROR".equals(prop)) {
            return NETWORK_TYPE_HSDPA;
        }
        return VSIM_WORKMODE_RESERVE_SUB1;
    }

    public int getVSimSubId() {
        try {
            return getIHwVSim().getVSimSubId();
        } catch (RemoteException ex) {
            Rlog.e(TAG, "RemoteException ex = " + ex);
            return VSIM_UNKNOWN_DEVICE;
        }
    }

    public boolean isVSimEnabled() {
        try {
            return getIHwVSim().isVSimEnabled();
        } catch (RemoteException ex) {
            Rlog.e(TAG, "RemoteException ex = " + ex);
            return VSIM_VDBG;
        }
    }

    public boolean isVSimInProcess() {
        try {
            return getIHwVSim().isVSimInProcess();
        } catch (RemoteException ex) {
            Rlog.e(TAG, "RemoteException ex = " + ex);
            return VSIM_VDBG;
        }
    }

    public boolean isVSimOn() {
        try {
            return getIHwVSim().isVSimOn();
        } catch (RemoteException ex) {
            Rlog.e(TAG, "RemoteException ex = " + ex);
            return VSIM_VDBG;
        }
    }

    public int enableVSim(int operation, String imsi, int cardtype, int apntype, String acqorder, String challenge) {
        Rlog.d(TAG, "enableVSim, operation = " + operation + ", cardtype = " + cardtype + ", apntype = " + apntype + ", acqorder: " + acqorder);
        int result = VSIM_UNICOM_DEVICE;
        try {
            result = getIHwVSim().enableVSim(operation, imsi, cardtype, apntype, acqorder, challenge);
        } catch (RemoteException ex) {
            Rlog.e(TAG, "RemoteException ex = " + ex);
        }
        Rlog.d(TAG, "enableVSim finish, result is " + result);
        return result;
    }

    public boolean disableVSim() {
        Rlog.d(TAG, "disableVSim");
        boolean result = VSIM_VDBG;
        try {
            result = getIHwVSim().disableVSim();
        } catch (RemoteException ex) {
            Rlog.e(TAG, "RemoteException ex = " + ex);
        }
        Rlog.d(TAG, "disableVSim finish, result is " + result);
        return result;
    }

    public boolean hasHardIccCardForVSim(int subId) {
        try {
            return getIHwVSim().hasHardIccCardForVSim(subId);
        } catch (RemoteException ex) {
            Rlog.e(TAG, "RemoteException ex = " + ex);
            return VSIM_VDBG;
        }
    }

    public int getSimMode(int subId) {
        try {
            return getIHwVSim().getSimMode(subId);
        } catch (RemoteException ex) {
            Rlog.e(TAG, "RemoteException ex = " + ex);
            return VSIM_UNKNOWN_DEVICE;
        }
    }

    public void recoverSimMode() {
        try {
            getIHwVSim().recoverSimMode();
        } catch (RemoteException ex) {
            Rlog.e(TAG, "RemoteException ex = " + ex);
        }
    }

    public String getRegPlmn(int subId) {
        try {
            return getIHwVSim().getRegPlmn(subId);
        } catch (RemoteException ex) {
            Rlog.e(TAG, "RemoteException ex = " + ex);
            return null;
        }
    }

    public String getTrafficData() {
        try {
            return getIHwVSim().getTrafficData();
        } catch (RemoteException ex) {
            Rlog.e(TAG, "RemoteException ex = " + ex);
            return null;
        }
    }

    public Boolean clearTrafficData() {
        try {
            return Boolean.valueOf(getIHwVSim().clearTrafficData());
        } catch (RemoteException ex) {
            Rlog.e(TAG, "RemoteException ex = " + ex);
            return Boolean.valueOf(VSIM_VDBG);
        }
    }

    public boolean dsFlowCfg(int repFlag, int threshold, int totalThreshold, int oper) {
        Rlog.d(TAG, "dsFlowCfg, repFlag = " + repFlag + ", threshold = " + threshold + ", totalThreshold = " + totalThreshold + ", oper = " + oper);
        try {
            return getIHwVSim().dsFlowCfg(repFlag, threshold, totalThreshold, oper);
        } catch (RemoteException ex) {
            Rlog.e(TAG, "RemoteException ex = " + ex);
            return VSIM_VDBG;
        }
    }

    public int getSimStateViaSysinfoEx(int subId) {
        try {
            return getIHwVSim().getSimStateViaSysinfoEx(subId);
        } catch (RemoteException ex) {
            Rlog.e(TAG, "RemoteException ex = " + ex);
            return VSIM_UNKNOWN_DEVICE;
        }
    }

    public int getCpserr(int subId) {
        try {
            return getIHwVSim().getCpserr(subId);
        } catch (RemoteException ex) {
            Rlog.e(TAG, "RemoteException ex = " + ex);
            return VSIM_WORKMODE_RESERVE_SUB1;
        }
    }

    public int scanVsimAvailableNetworks(int subId, int type) {
        try {
            return getIHwVSim().scanVsimAvailableNetworks(subId, type);
        } catch (RemoteException ex) {
            Rlog.e(TAG, "RemoteException ex = " + ex);
            return VSIM_UNKNOWN_DEVICE;
        }
    }

    public int getVsimAvailableNetworks(int subId, int type) {
        return scanVsimAvailableNetworks(subId, type);
    }

    public boolean setUserReservedSubId(int subId) {
        Rlog.d(TAG, "setUserReservedSubId subId = " + subId);
        try {
            return getIHwVSim().setUserReservedSubId(subId);
        } catch (RemoteException ex) {
            Rlog.e(TAG, "RemoteException ex = " + ex);
            return VSIM_VDBG;
        }
    }

    public int getUserReservedSubId() {
        try {
            return getIHwVSim().getUserReservedSubId();
        } catch (RemoteException ex) {
            Rlog.e(TAG, "RemoteException ex = " + ex);
            return VSIM_UNKNOWN_DEVICE;
        }
    }

    public String getDevSubMode(int subscription) {
        try {
            return getIHwVSim().getDevSubMode(subscription);
        } catch (RemoteException ex) {
            Rlog.e(TAG, "RemoteException ex = " + ex);
            return null;
        }
    }

    public String getDevSubMode() {
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            return getDevSubMode(SubscriptionManager.getDefaultSubscriptionId());
        }
        Rlog.e(TAG, "getDevSubMode not support");
        return null;
    }

    public String getPreferredNetworkTypeForVSim(int subscription) {
        try {
            return getIHwVSim().getPreferredNetworkTypeForVSim(subscription);
        } catch (RemoteException ex) {
            Rlog.e(TAG, "RemoteException ex = " + ex);
            return null;
        }
    }

    public String getPreferredNetworkTypeForVSim() {
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            int subId = getVSimSubId();
            if (subId != VSIM_UNKNOWN_DEVICE) {
                return getPreferredNetworkTypeForVSim(subId);
            }
            Rlog.e(TAG, "getPreferredNetworkTypeForVSim vsim not enabled");
            return null;
        }
        Rlog.e(TAG, "getPreferredNetworkTypeForVSim not support");
        return null;
    }

    public int getVSimCurCardType() {
        try {
            return getIHwVSim().getVSimCurCardType();
        } catch (RemoteException ex) {
            Rlog.e(TAG, "RemoteException ex = " + ex);
            return VSIM_UNKNOWN_DEVICE;
        }
    }

    public int getOperatorWithDeviceCustomed() {
        if (isFullNetworkSupported()) {
            return VSIM_FULLNETWORK_DEVICE;
        }
        if (IS_DUAL_CMCC_UNICOM_DEVICE) {
            return VSIM_OP_SETAPN_FORHASH;
        }
        if (HuaweiTelephonyConfigs.isChinaMobile()) {
            return VSIM_WORKMODE_HIGH_SPEED;
        }
        if (HuaweiTelephonyConfigs.isChinaTelecom()) {
            return VSIM_WORKMODE_RESERVE_SUB2;
        }
        if (HuaweiTelephonyConfigs.isChinaUnicom()) {
            return VSIM_UNICOM_DEVICE;
        }
        return VSIM_UNKNOWN_DEVICE;
    }

    private boolean isFullNetworkSupported() {
        return SystemProperties.getBoolean(HwTelephonyProperties.PROPERTY_FULL_NETWORK_SUPPORT, VSIM_VDBG);
    }

    public int getDeviceNetworkCountryIso() {
        return SystemProperties.getBoolean(PROP_OVERSEAS_MODE, VSIM_VDBG) ? VSIM_UNKNOWN_DEVICE : VSIM_WORKMODE_RESERVE_SUB2;
    }

    public String getVSimNetworkOperator() {
        return SystemProperties.get("gsm.operator.numeric.vsim");
    }

    public String getVSimNetworkCountryIso() {
        return SystemProperties.get("gsm.operator.iso-country.vsim");
    }

    public String getVSimNetworkOperatorName() {
        return SystemProperties.get("gsm.operator.alpha.vsim");
    }

    public int getVSimNetworkType() {
        try {
            return getIHwVSim().getVSimNetworkType();
        } catch (RemoteException ex) {
            Rlog.e(TAG, "RemoteException ex = " + ex);
            return VSIM_WORKMODE_RESERVE_SUB1;
        }
    }

    public String getVSimNetworkTypeName() {
        switch (getVSimNetworkType()) {
            case VSIM_WORKMODE_RESERVE_SUB2 /*1*/:
                return "GPRS";
            case VSIM_WORKMODE_HIGH_SPEED /*2*/:
                return "EDGE";
            case VSIM_UNICOM_DEVICE /*3*/:
                return "UMTS";
            case VSIM_OP_SETAPN_FORHASH /*4*/:
                return "CDMA";
            case VSIM_FULLNETWORK_DEVICE /*5*/:
                return "CDMA - EvDo rev. 0";
            case NETWORK_TYPE_EVDO_A /*6*/:
                return "CDMA - EvDo rev. A";
            case NETWORK_TYPE_1xRTT /*7*/:
                return "CDMA - 1xRTT";
            case NETWORK_TYPE_HSDPA /*8*/:
                return "HSDPA";
            case NETWORK_TYPE_HSUPA /*9*/:
                return "HSUPA";
            case NETWORK_TYPE_HSPA /*10*/:
                return "HSPA";
            case NETWORK_TYPE_IDEN /*11*/:
                return "iDEN";
            case NETWORK_TYPE_EVDO_B /*12*/:
                return "CDMA - EvDo rev. B";
            case NETWORK_TYPE_LTE /*13*/:
                return "LTE";
            case NETWORK_TYPE_EHRPD /*14*/:
                return "CDMA - eHRPD";
            case NETWORK_TYPE_HSPAP /*15*/:
                return "HSPA+";
            case NETWORK_TYPE_GSM /*16*/:
                return "GSM";
            case NETWORK_TYPE_TDS /*17*/:
                return "TD-SCDMA";
            case NETWORK_TYPE_TDS_HSDPA /*18*/:
                return "TDS_HSDPA";
            case NETWORK_TYPE_TDS_HSUPA /*19*/:
                return "TDS_HSUPA";
            default:
                return "UNKNOWN";
        }
    }

    public String getVSimSubscriberId() {
        try {
            return getIHwVSim().getVSimSubscriberId();
        } catch (RemoteException ex) {
            Rlog.e(TAG, "RemoteException ex = " + ex);
            return null;
        }
    }

    public boolean setVSimULOnlyMode(boolean isULOnly) {
        try {
            return getIHwVSim().setVSimULOnlyMode(isULOnly);
        } catch (RemoteException ex) {
            Rlog.e(TAG, "RemoteException ex = " + ex);
            return VSIM_VDBG;
        }
    }

    public boolean getVSimULOnlyMode() {
        try {
            return getIHwVSim().getVSimULOnlyMode();
        } catch (RemoteException ex) {
            Rlog.e(TAG, "RemoteException ex = " + ex);
            return VSIM_VDBG;
        }
    }

    public int getVSimPlatformCapability() {
        if (isPlatformSupportVsim()) {
            return VSIM_MODEM_COUNT;
        }
        return VSIM_WORKMODE_RESERVE_SUB1;
    }

    public int getVSimOccupiedSubId() {
        try {
            return getIHwVSim().getVSimOccupiedSubId();
        } catch (RemoteException ex) {
            Rlog.e(TAG, "RemoteException ex = " + ex);
            return VSIM_UNKNOWN_DEVICE;
        }
    }

    public int getPlatformSupportVSimVer(int key) {
        try {
            return getIHwVSim().getPlatformSupportVSimVer(key);
        } catch (RemoteException ex) {
            Rlog.e(TAG, "RemoteException ex = " + ex);
            return VSIM_UNKNOWN_DEVICE;
        }
    }

    public boolean switchVSimWorkMode(int workMode) {
        try {
            return getIHwVSim().switchVSimWorkMode(workMode);
        } catch (RemoteException ex) {
            Rlog.e(TAG, "RemoteException ex = " + ex);
            return VSIM_VDBG;
        }
    }

    public int enableVSim(String imsi, int cardtype, int apntype, String acqorder, String challenge) {
        return enableVSim(VSIM_WORKMODE_RESERVE_SUB2, imsi, cardtype, apntype, acqorder, challenge);
    }

    public int setApn(int cardtype, int apntype, String challenge) {
        return enableVSim(VSIM_WORKMODE_HIGH_SPEED, null, cardtype, apntype, null, challenge);
    }

    public int enableVSim(String imsi, int cardtype, int apntype, String acqorder, String tapath, int vsimloc, String challenge) {
        return enableVSim(VSIM_WORKMODE_RESERVE_SUB2, imsi, cardtype, apntype, acqorder, tapath, vsimloc, challenge);
    }

    public int setApn(String imsi, int cardtype, int apntype, String tapath, String challenge) {
        return enableVSim(VSIM_WORKMODE_HIGH_SPEED, imsi, cardtype, apntype, null, tapath, VSIM_UNKNOWN_DEVICE, challenge);
    }

    public int enableVSim(int operation, String imsi, int cardtype, int apntype, String acqorder, String tapath, int vsimloc, String challenge) {
        Rlog.d(TAG, "enableVSim, operation = " + operation + ", cardtype = " + cardtype + ", apntype = " + apntype + ", acqorder = " + acqorder + ", vsimloc = " + vsimloc + ",challenge = " + challenge);
        int result = VSIM_UNICOM_DEVICE;
        try {
            result = getIHwVSim().enableVSimV2(operation, imsi, cardtype, apntype, acqorder, tapath, vsimloc, challenge);
        } catch (RemoteException ex) {
            Rlog.e(TAG, "RemoteException ex = " + ex);
        }
        Rlog.d(TAG, "enableVSim finish, result is " + result);
        return result;
    }

    public int dialupForVSim() {
        Rlog.d(TAG, "dialupForVSim");
        int result = VSIM_UNKNOWN_DEVICE;
        try {
            result = getIHwVSim().dialupForVSim();
        } catch (RemoteException ex) {
            Rlog.e(TAG, "RemoteException ex = " + ex);
        }
        Rlog.d(TAG, "dialupForVSim finish, result is " + result);
        return result;
    }
}
