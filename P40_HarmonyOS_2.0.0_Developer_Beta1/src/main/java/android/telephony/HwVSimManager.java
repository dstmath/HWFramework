package android.telephony;

import android.os.Bundle;
import android.os.RemoteException;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.HwBaseInnerSmsManagerImpl;
import com.android.internal.telephony.HwTelephonyProperties;
import com.android.internal.telephony.IHwVSim;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.SubscriptionManagerEx;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.hwparttelephony.BuildConfig;
import com.huawei.internal.telephony.vsim.IGetVsimServiceCallback;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class HwVSimManager {
    private static final int GETMODE_RESULT_SIM = 0;
    private static final int GETMODE_RESULT_VSIM = 1;
    private static final int INVALID = -1;
    private static final boolean IS_DUAL_CMCC_UNICOM_DEVICE = SystemPropertiesEx.getBoolean("ro.hwpp.dualcu", false);
    private static final boolean IS_PLATFORM_SUPPORT_V_SIM = SystemPropertiesEx.getBoolean("ro.radio.vsim_support", false);
    private static final boolean IS_VSIM_DBG = false;
    private static final boolean IS_VSIM_VDBG = false;
    private static final int MAX_VSIM_MODEM_COUNT_DUAL_SIM = 2;
    private static final int MAX_VSIM_MODEM_COUNT_TRI_SIM = 3;
    private static final int MAX_WAIT_TIME_SECONDS = 5;
    private static final int NETWORK_TYPE_DCHSPAP = 30;
    private static final int NETWORK_TYPE_LTE_CA = 19;
    private static final String PROPERTY_DYNAMIC_START_STOP = "persist.hw_mc.telephony.vsim_dynamic_start_stop";
    private static final String PROPERTY_VSIM_DSDS_VERSION = "ro.radio.vsim_dsds_version";
    private static final String PROP_OVERSEAS_MODE = "persist.radio.overseas_mode";
    private static final String TAG = "HwVSimManager";
    private static final int VSIM_CAPABILITY_DUAL_CARDS = 2;
    private static final int VSIM_CMCC_DEVICE = 2;
    private static final int VSIM_DSDS_VERSION_DEFAULT = 0;
    private static final int VSIM_DSDS_VERSION_ONE = 1;
    private static final int VSIM_DSDS_VERSION_PROP = SystemPropertiesEx.getInt(PROPERTY_VSIM_DSDS_VERSION, 0);
    private static final int VSIM_DUAL_CMCC_UNICOM_DEVICE = 4;
    private static final int VSIM_ENABLE_RESULT_FAIL = 3;
    private static final int VSIM_FULLNETWORK_DEVICE = 5;
    private static final int VSIM_MODEM_COUNT = SystemPropertiesEx.getInt("ro.radio.vsim_modem_count", 3);
    private static final int VSIM_MODEM_COUNT_DEFAULT = 3;
    private static final int VSIM_MODEM_COUNT_DUAL_NEW = 5;
    private static final int VSIM_MODEM_COUNT_REAL_TRIPPLE = 4;
    public static final int VSIM_OP_ENABLEVSIM = 1;
    private static final int VSIM_OP_ENABLEVSIM_SETSPN_AND_RULE = 7;
    public static final int VSIM_OP_SETAPN = 2;
    private static final int VSIM_OP_SWITCH_HARD_SIM = 6;
    private static final boolean VSIM_SUPPORT_DYNAMIC_START_STOP = SystemPropertiesEx.getBoolean(PROPERTY_DYNAMIC_START_STOP, true);
    private static final int VSIM_TELECOM_DEVICE = 1;
    private static final int VSIM_UNICOM_DEVICE = 3;
    private static final int VSIM_UNKNOWN_DEVICE = -1;
    private static HwVSimManager sInstance = new HwVSimManager();

    private HwVSimManager() {
    }

    public static HwVSimManager getDefault() {
        return sInstance;
    }

    private static boolean isVsimDsdsVersionOne() {
        return VSIM_DSDS_VERSION_PROP == 1;
    }

    private IHwVSim getIHwVSim() throws RemoteException {
        IHwVSim iHwVSim = IHwVSim.Stub.asInterface(ServiceManagerEx.getService("ihwvsim"));
        if (iHwVSim != null) {
            return iHwVSim;
        }
        throw new RemoteException("getIHwVSim null");
    }

    private IHwVSim blockingGetVsimServiceIfNull() {
        if (!IS_PLATFORM_SUPPORT_V_SIM) {
            return null;
        }
        IHwVSim hwVsimService = IHwVSim.Stub.asInterface(ServiceManagerEx.getService("ihwvsim"));
        if (hwVsimService != null) {
            return hwVsimService;
        }
        if (!VSIM_SUPPORT_DYNAMIC_START_STOP) {
            return null;
        }
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<IHwVSim> resultRef = new AtomicReference<>();
        HwTelephonyManagerInner.getDefault().blockingGetVsimService(new IGetVsimServiceCallback.Stub() {
            /* class android.telephony.HwVSimManager.AnonymousClass1 */

            @Override // com.huawei.internal.telephony.vsim.IGetVsimServiceCallback
            public void onComplete(IHwVSim vsimService) {
                resultRef.set(vsimService);
                latch.countDown();
            }
        });
        return (IHwVSim) awaitResult(latch, resultRef);
    }

    private static <T> T awaitResult(CountDownLatch latch, AtomicReference<T> resultRef) {
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return resultRef.get();
    }

    public boolean isPlatformSupportVsim() {
        return IS_PLATFORM_SUPPORT_V_SIM;
    }

    public int maxVSimModemCount() {
        return 3;
    }

    public boolean hasIccCardForVSim(int slotId) {
        if (maxVSimModemCount() == 3 && slotId == 2) {
            return hasVSimIccCard();
        }
        return (slotId == 0 || slotId == 1) ? false : false;
    }

    public boolean hasVSimIccCard() {
        try {
            return getIHwVSim().hasVSimIccCard();
        } catch (RemoteException ex) {
            printExLogIfSupportVsim(ex);
            return false;
        }
    }

    public int getSimStateForVSim(int slotIdx) {
        if (maxVSimModemCount() == 3 && slotIdx == 2) {
            return getVSimState();
        }
        if (SubscriptionManagerEx.getSubId(slotIdx) == null) {
            return 1;
        }
        String prop = TelephonyManagerEx.getTelephonyProperty(slotIdx, "gsm.sim.state", BuildConfig.FLAVOR);
        if ("ABSENT".equals(prop)) {
            return 1;
        }
        if ("PIN_REQUIRED".equals(prop)) {
            return 2;
        }
        if ("PUK_REQUIRED".equals(prop)) {
            return 3;
        }
        if ("NETWORK_LOCKED".equals(prop)) {
            return 4;
        }
        if ("READY".equals(prop)) {
            return 5;
        }
        if ("CARD_IO_ERROR".equals(prop)) {
            return 8;
        }
        return 0;
    }

    public int getVSimState() {
        String prop = SystemPropertiesEx.get("gsm.vsim.state");
        if ("ABSENT".equals(prop)) {
            return 1;
        }
        if ("PIN_REQUIRED".equals(prop)) {
            return 2;
        }
        if ("PUK_REQUIRED".equals(prop)) {
            return 3;
        }
        if ("NETWORK_LOCKED".equals(prop)) {
            return 4;
        }
        if ("READY".equals(prop)) {
            return 5;
        }
        if ("CARD_IO_ERROR".equals(prop)) {
            return 8;
        }
        return 0;
    }

    public int getVSimSubId() {
        try {
            return getIHwVSim().getVSimSubId();
        } catch (RemoteException ex) {
            printExLogIfSupportVsim(ex);
            return -1;
        }
    }

    public boolean isVSimEnabled() {
        try {
            return getIHwVSim().isVSimEnabled();
        } catch (RemoteException ex) {
            printExLogIfSupportVsim(ex);
            return false;
        }
    }

    public boolean isVSimInProcess() {
        try {
            return getIHwVSim().isVSimInProcess();
        } catch (RemoteException ex) {
            printExLogIfSupportVsim(ex);
            return false;
        }
    }

    public boolean isVSimOn() {
        try {
            return getIHwVSim().isVSimOn();
        } catch (RemoteException ex) {
            printExLogIfSupportVsim(ex);
            return false;
        }
    }

    public boolean disableVSim() {
        RlogEx.i(TAG, "disableVSim");
        if (!HwTelephonyManagerInner.getDefault().isVsimEnabledByDatabase()) {
            RlogEx.i(TAG, "disableVSim, vsim is closed, return true.");
            return true;
        }
        boolean result = false;
        try {
            IHwVSim vsimService = blockingGetVsimServiceIfNull();
            if (vsimService == null) {
                RlogEx.i(TAG, "disableVSim service is null, return");
                return false;
            }
            result = vsimService.disableVSim();
            RlogEx.i(TAG, "disableVSim finish, result is " + result);
            return result;
        } catch (RemoteException ex) {
            printExLogIfSupportVsim(ex);
        }
    }

    public int getSimMode(int slotId) {
        if (!IS_PLATFORM_SUPPORT_V_SIM) {
            return -1;
        }
        if (slotId != 2) {
            return 0;
        }
        return 1;
    }

    public String getTrafficData() {
        try {
            return getIHwVSim().getTrafficData();
        } catch (RemoteException ex) {
            printExLogIfSupportVsim(ex);
            return null;
        }
    }

    public Boolean clearTrafficData() {
        try {
            IHwVSim vsimService = blockingGetVsimServiceIfNull();
            if (vsimService != null) {
                return Boolean.valueOf(vsimService.clearTrafficData());
            }
            RlogEx.i(TAG, "clearTrafficData service is null, return");
            return false;
        } catch (RemoteException ex) {
            printExLogIfSupportVsim(ex);
            return false;
        }
    }

    public boolean dsFlowCfg(int repFlag, int threshold, int totalThreshold, int oper) {
        RlogEx.i(TAG, "dsFlowCfg, repFlag = " + repFlag + ", threshold = " + threshold + ", totalThreshold = " + totalThreshold + ", oper = " + oper);
        try {
            return getIHwVSim().dsFlowCfg(repFlag, threshold, totalThreshold, oper);
        } catch (RemoteException ex) {
            printExLogIfSupportVsim(ex);
            return false;
        }
    }

    public int getSimStateViaSysinfoEx(int subId) {
        try {
            return getIHwVSim().getSimStateViaSysinfoEx(subId);
        } catch (RemoteException ex) {
            printExLogIfSupportVsim(ex);
            return -1;
        }
    }

    public int scanVsimAvailableNetworks(int subId, int type) {
        try {
            return getIHwVSim().scanVsimAvailableNetworks(subId, type);
        } catch (RemoteException ex) {
            printExLogIfSupportVsim(ex);
            return -1;
        }
    }

    public int getVsimAvailableNetworks(int subId, int type) {
        return scanVsimAvailableNetworks(subId, type);
    }

    public String getDevSubMode(int subscription) {
        try {
            return getIHwVSim().getDevSubMode(subscription);
        } catch (RemoteException ex) {
            printExLogIfSupportVsim(ex);
            return null;
        }
    }

    public String getDevSubMode() {
        if (TelephonyManagerEx.isMultiSimEnabled()) {
            return getDevSubMode(SubscriptionManagerEx.getPhoneId(SubscriptionManager.getDefaultSubscriptionId()));
        }
        RlogEx.e(TAG, "getDevSubMode not support");
        return null;
    }

    public String getPreferredNetworkTypeForVSim(int subscription) {
        try {
            return getIHwVSim().getPreferredNetworkTypeForVSim(subscription);
        } catch (RemoteException ex) {
            printExLogIfSupportVsim(ex);
            return null;
        }
    }

    public String getPreferredNetworkTypeForVSim() {
        if (TelephonyManagerEx.isMultiSimEnabled()) {
            int subId = getVSimSubId();
            if (subId != -1) {
                return getPreferredNetworkTypeForVSim(subId);
            }
            RlogEx.e(TAG, "getPreferredNetworkTypeForVSim vsim not enabled");
            return null;
        }
        RlogEx.e(TAG, "getPreferredNetworkTypeForVSim not support");
        return null;
    }

    public int getVSimCurCardType() {
        try {
            return getIHwVSim().getVSimCurCardType();
        } catch (RemoteException ex) {
            printExLogIfSupportVsim(ex);
            return -1;
        }
    }

    public int getOperatorWithDeviceCustomed() {
        if (isFullNetworkSupported()) {
            return 5;
        }
        if (IS_DUAL_CMCC_UNICOM_DEVICE) {
            return 4;
        }
        if (HuaweiTelephonyConfigs.isChinaMobile()) {
            return 2;
        }
        if (HuaweiTelephonyConfigs.isChinaTelecom()) {
            return 1;
        }
        if (HuaweiTelephonyConfigs.isChinaUnicom()) {
            return 3;
        }
        return -1;
    }

    private boolean isFullNetworkSupported() {
        return SystemPropertiesEx.getBoolean(HwTelephonyProperties.PROPERTY_FULL_NETWORK_SUPPORT, false);
    }

    public int getDeviceNetworkCountryIso() {
        return SystemPropertiesEx.getBoolean(PROP_OVERSEAS_MODE, false) ? -1 : 1;
    }

    public String getVSimNetworkOperator() {
        return SystemPropertiesEx.get("gsm.operator.numeric.vsim");
    }

    public String getVSimNetworkCountryIso() {
        return SystemPropertiesEx.get("gsm.operator.iso-country.vsim");
    }

    public String getVSimNetworkOperatorName() {
        return SystemPropertiesEx.get("gsm.operator.alpha.vsim");
    }

    public int getVSimNetworkType() {
        try {
            return getIHwVSim().getVSimNetworkType();
        } catch (RemoteException ex) {
            printExLogIfSupportVsim(ex);
            return 0;
        }
    }

    public String getVSimNetworkTypeName() {
        int vSimNetworkType = getVSimNetworkType();
        if (vSimNetworkType == 30) {
            return "DC-HSPA+";
        }
        switch (vSimNetworkType) {
            case 1:
                return "GPRS";
            case 2:
                return "EDGE";
            case 3:
                return "UMTS";
            case 4:
                return "CDMA";
            case 5:
                return "CDMA - EvDo rev. 0";
            case 6:
                return "CDMA - EvDo rev. A";
            case 7:
                return "CDMA - 1xRTT";
            case 8:
                return "HSDPA";
            case 9:
                return "HSUPA";
            case 10:
                return "HSPA";
            case 11:
                return "iDEN";
            case 12:
                return "CDMA - EvDo rev. B";
            case 13:
                return "LTE";
            case 14:
                return "CDMA - eHRPD";
            case 15:
                return "HSPA+";
            case HwBaseInnerSmsManagerImpl.SMS_GW_VP_RELATIVE_FORMAT /* 16 */:
                return "GSM";
            case 17:
                return "TD-SCDMA";
            case 18:
                return "IWLAN";
            case NETWORK_TYPE_LTE_CA /* 19 */:
                return "LTE-CA";
            case 20:
                return "NR";
            default:
                return "UNKNOWN";
        }
    }

    public String getVSimSubscriberId() {
        try {
            return getIHwVSim().getVSimSubscriberId();
        } catch (RemoteException ex) {
            printExLogIfSupportVsim(ex);
            return null;
        }
    }

    public int getVSimPlatformCapability() {
        if (!isPlatformSupportVsim()) {
            return 0;
        }
        if (isVsimDsdsVersionOne()) {
            return 2;
        }
        int i = VSIM_MODEM_COUNT;
        if (i == 2) {
            return 5;
        }
        return i;
    }

    public int getVSimOccupiedSubId() {
        try {
            return getIHwVSim().getVSimOccupiedSubId();
        } catch (RemoteException ex) {
            printExLogIfSupportVsim(ex);
            return -1;
        }
    }

    public boolean switchVSimWorkMode(int workMode) {
        try {
            return getIHwVSim().switchVSimWorkMode(workMode);
        } catch (RemoteException ex) {
            printExLogIfSupportVsim(ex);
            return false;
        }
    }

    public int enableVSim(String imsi, int cardtype, int apntype, String acqorder, String challenge) {
        return enableVSim(1, imsi, cardtype, apntype, acqorder, challenge);
    }

    public int enableVSim(String imsi, int cardtype, int apntype, String acqorder, String tapath, int vsimloc, String challenge) {
        return enableVSim(1, imsi, cardtype, apntype, acqorder, tapath, vsimloc, challenge);
    }

    public int enableVSim(int operation, String imsi, int cardtype, int apntype, String acqorder, String challenge) {
        RlogEx.i(TAG, "enableVSim V1 operation = " + operation + ", cardtype = " + cardtype + ", apntype = " + apntype + ", acqOrder: " + acqorder);
        int result = 3;
        try {
            IHwVSim vsimService = blockingGetVsimServiceIfNull();
            if (vsimService == null) {
                RlogEx.i(TAG, "enableVSim V1 service is null, return");
                return 3;
            }
            result = vsimService.enableVSim(operation, imsi, cardtype, apntype, acqorder, challenge);
            RlogEx.i(TAG, "enableVSim V1 finish, result is " + result);
            return result;
        } catch (RemoteException ex) {
            printExLogIfSupportVsim(ex);
        }
    }

    public int enableVSim(int operation, String imsi, int cardtype, int apntype, String acqorder, String tapath, int vsimloc, String challenge) {
        String str;
        int result;
        RemoteException ex;
        RlogEx.i(TAG, "enableVSim V2 , operation = " + operation + ", cardtype = " + cardtype + ", apntype = " + apntype + ", acqOrder = " + acqorder + ", vsimloc = " + vsimloc + ",challenge = " + challenge);
        try {
            IHwVSim vsimService = blockingGetVsimServiceIfNull();
            if (vsimService == null) {
                RlogEx.i(TAG, "enableVSim V2 service is null, return");
                return 3;
            }
            str = TAG;
            try {
                result = vsimService.enableVSimV2(operation, imsi, cardtype, apntype, acqorder, tapath, vsimloc, challenge);
            } catch (RemoteException e) {
                ex = e;
                printExLogIfSupportVsim(ex);
                result = 3;
                RlogEx.i(str, "enableVSim V2 finish, result is " + result);
                return result;
            }
            RlogEx.i(str, "enableVSim V2 finish, result is " + result);
            return result;
        } catch (RemoteException e2) {
            ex = e2;
            str = TAG;
            printExLogIfSupportVsim(ex);
            result = 3;
            RlogEx.i(str, "enableVSim V2 finish, result is " + result);
            return result;
        }
    }

    public int enableVSim(int operation, Bundle bundle) {
        RlogEx.i(TAG, "enableVSim V3 , operation = " + operation);
        int result = 3;
        try {
            IHwVSim vsimService = blockingGetVsimServiceIfNull();
            if (vsimService == null) {
                RlogEx.i(TAG, "enableVSim V3 service is null, return");
                return 3;
            }
            result = vsimService.enableVSimV3(operation, bundle);
            RlogEx.i(TAG, "enableVSim V3 finish, result is " + result);
            return result;
        } catch (RemoteException ex) {
            printExLogIfSupportVsim(ex);
        }
    }

    public int setApn(String imsi, int cardtype, int apntype, String tapath, String challenge) {
        return enableVSim(2, imsi, cardtype, apntype, null, tapath, -1, challenge);
    }

    public int setApn(int cardtype, int apntype, String challenge) {
        return enableVSim(2, null, cardtype, apntype, null, challenge);
    }

    public boolean isSupportVSimByOperation(int operation) {
        boolean isSupport = true;
        if (!(operation == 1 || operation == 2)) {
            if (operation == 6) {
                if (VSIM_MODEM_COUNT != 4 || !HwTelephonyManagerInner.getDefault().isDualImsSupported()) {
                    isSupport = false;
                }
                return isSupport;
            } else if (operation != 7) {
                return false;
            }
        }
        return true;
    }

    public int dialupForVSim() {
        RlogEx.i(TAG, "dialupForVSim");
        int result = -1;
        try {
            result = getIHwVSim().dialupForVSim();
        } catch (RemoteException ex) {
            printExLogIfSupportVsim(ex);
        }
        RlogEx.i(TAG, "dialupForVSim finish, result is " + result);
        return result;
    }

    private void printExLogIfSupportVsim(RemoteException ex) {
        if (IS_PLATFORM_SUPPORT_V_SIM) {
            RlogEx.e(TAG, "RemoteException ex = " + ex);
        }
    }
}
