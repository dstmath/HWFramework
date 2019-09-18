package android.telephony;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.cover.CoverManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.text.TextUtils;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.HwModemCapability;
import com.android.internal.telephony.HwTelephonyIntentsInner;
import com.android.internal.telephony.HwTelephonyProperties;
import com.android.internal.telephony.IHwTelephony;
import com.android.internal.telephony.IPhoneCallback;
import huawei.android.app.admin.HwDevicePolicyManagerEx;
import java.util.Iterator;
import java.util.List;

public class HwTelephonyManagerInner {
    public static final String CARD_TYPE_SIM1 = "gsm.sim1.type";
    public static final String CARD_TYPE_SIM2 = "gsm.sim2.type";
    private static final String[] CDMA_CPLMNS = {"46003", "45502", "46012"};
    public static final int CDMA_MODE = 0;
    private static final String CHR_BROADCAST_PERMISSION = "com.huawei.android.permission.GET_CHR_DATA";
    public static final int CT_NATIONAL_ROAMING_CARD = 41;
    public static final int CU_DUAL_MODE_CARD = 42;
    private static final String DISABLE_PUSH = "disable-push";
    public static final int DUAL_MODE_CG_CARD = 40;
    public static final int DUAL_MODE_TELECOM_LTE_CARD = 43;
    public static final int DUAL_MODE_UG_CARD = 50;
    private static final int ERROR = -1;
    public static final int EXTRA_VALUE_NEW_SIM = 1;
    public static final int EXTRA_VALUE_NOCHANGE = 4;
    public static final int EXTRA_VALUE_REMOVE_SIM = 2;
    public static final int EXTRA_VALUE_REPOSITION_SIM = 3;
    public static final int EXTR_VALUE_INSERT_SAME_SIM = 5;
    private static final String GC_ICCID = "8985231";
    public static final int GSM_MODE = 1;
    private static final String HW_CUST_SW_SIMLOCK = "hw.cust.sw.simlock";
    public static final String INTENT_KEY_DETECT_STATUS = "simDetectStatus";
    public static final String INTENT_KEY_NEW_SIM_SLOT = "newSIMSlot";
    public static final String INTENT_KEY_NEW_SIM_STATUS = "newSIMStatus";
    public static final String INTENT_KEY_SIM_COUNT = "simCount";
    private static final String INVALID_MCCMNC = "00000";
    private static boolean IS_USE_RSRQ = SystemProperties.getBoolean("ro.config.lte_use_rsrq", false);
    private static final int NETWORK_MODE_UNKNOWN = -1;
    public static final int NOTIFY_CMODEM_STATUS_FAIL = -1;
    public static final int NOTIFY_CMODEM_STATUS_SUCCESS = 1;
    public static final int PHONE_EVENT_IMSA_TO_MAPCON = 4;
    public static final int PHONE_EVENT_RADIO_AVAILABLE = 1;
    public static final int PHONE_EVENT_RADIO_UNAVAILABLE = 2;
    private static final String PROP_LTETDD_ENABLED = "persist.radio.ltetdd_enabled";
    private static final String PROP_LTE_ENABLED = "persist.radio.lte_enabled";
    private static final String PROP_VALUE_C_CARD0_PLMN = "gsm.sim0.c_card.plmn";
    private static final String PROP_VALUE_C_CARD1_PLMN = "gsm.sim1.c_card.plmn";
    private static final String PROP_VALUE_C_CARD_PLMN = "gsm.sim.c_card.plmn";
    public static final int ROAM_MODE = 2;
    private static final int SERVICE_2G_OFF = 0;
    public static final int SERVICE_ABILITY_OFF = 0;
    public static final int SERVICE_ABILITY_ON = 1;
    public static final int SERVICE_TYPE_LTE = 0;
    public static final int SERVICE_TYPE_NR = 1;
    public static final int SINGLE_MODE_RUIM_CARD = 30;
    public static final int SINGLE_MODE_SIM_CARD = 10;
    public static final int SINGLE_MODE_USIM_CARD = 20;
    private static final int SUB_0 = 0;
    private static final int SUB_1 = 1;
    public static final int SUPPORT_SYSTEMAPP_GET_DEVICEID = 1;
    private static final String TAG = "HwTelephonyManagerInner";
    public static final int UNKNOWN_CARD = -1;
    private static String callingAppName = "";
    private static boolean haveCheckedAppName = false;
    private static String mDeviceIdAll = null;
    private static String mDeviceIdIMEI = null;
    private static HwTelephonyManagerInner sInstance = new HwTelephonyManagerInner();
    private final int SIGNAL_TYPE_CDMA = 3;
    private final int SIGNAL_TYPE_CDMALTE = 6;
    private final int SIGNAL_TYPE_EVDO = 4;
    private final int SIGNAL_TYPE_GSM = 1;
    private final int SIGNAL_TYPE_LTE = 5;
    private final int SIGNAL_TYPE_NR = 7;
    private final int SIGNAL_TYPE_UMTS = 2;
    private HwDevicePolicyManagerEx mDpm = new HwDevicePolicyManagerEx();

    public enum DataSettingModeType {
        MODE_LTE_OFF,
        MODE_LTETDD_ONLY,
        MODE_LTE_AND_AUTO,
        MODE_ERROR
    }

    private HwTelephonyManagerInner() {
    }

    public static HwTelephonyManagerInner getDefault() {
        return sInstance;
    }

    private IHwTelephony getIHwTelephony() throws RemoteException {
        IHwTelephony iHwTelephony = IHwTelephony.Stub.asInterface(ServiceManager.getService("phone_huawei"));
        if (iHwTelephony != null) {
            return iHwTelephony;
        }
        throw new RemoteException("getIHwTelephony return null");
    }

    public String getDemoString() {
        try {
            return getIHwTelephony().getDemoString();
        } catch (RemoteException e) {
            Rlog.e(TAG, "getDemoString RemoteException");
            return "ERROR";
        }
    }

    private int getDefaultSim() {
        return 0;
    }

    public String getMeid() {
        return getMeid(getDefaultSim());
    }

    public String getMeid(int slotId) {
        try {
            return getIHwTelephony().getMeidForSubscriber(slotId);
        } catch (RemoteException e) {
            return null;
        } catch (NullPointerException e2) {
            return null;
        }
    }

    public String getPesn() {
        return getPesn(getDefaultSim());
    }

    public String getPesn(int slotId) {
        try {
            return getIHwTelephony().getPesnForSubscriber(slotId);
        } catch (RemoteException e) {
            return null;
        } catch (NullPointerException e2) {
            return null;
        }
    }

    public String getNVESN() {
        try {
            return getIHwTelephony().getNVESN();
        } catch (RemoteException e) {
            return null;
        } catch (NullPointerException e2) {
            return null;
        }
    }

    public void closeRrc() {
        if (1000 == Binder.getCallingUid()) {
            try {
                getIHwTelephony().closeRrc();
            } catch (RemoteException | NullPointerException e) {
            }
        }
    }

    public int getSubState(long subId) {
        try {
            return getIHwTelephony().getSubState((int) subId);
        } catch (RemoteException e) {
            return 0;
        }
    }

    public void setUserPrefDataSlotId(int slotId) {
        try {
            getIHwTelephony().setUserPrefDataSlotId(slotId);
        } catch (RemoteException | NullPointerException e) {
        }
    }

    public boolean checkCdmaSlaveCardMode(int mode) {
        String commrilMode = SystemProperties.get(HwTelephonyProperties.PROPERTY_COMMRIL_MODE, "NON_MODE");
        String cg_standby_mode = SystemProperties.get(HwTelephonyProperties.PROPERTY_CG_STANDBY_MODE, "home");
        if (!isFullNetworkSupported() || !"CG_MODE".equals(commrilMode)) {
            return false;
        }
        switch (mode) {
            case 0:
                if (!"roam_gsm".equals(cg_standby_mode)) {
                    return true;
                }
                break;
            case 1:
                if ("roam_gsm".equals(cg_standby_mode)) {
                    return true;
                }
                break;
            case 2:
                if (!"home".equals(cg_standby_mode)) {
                    return true;
                }
                break;
        }
        return false;
    }

    public boolean isFullNetworkSupported() {
        return SystemProperties.getBoolean(HwTelephonyProperties.PROPERTY_FULL_NETWORK_SUPPORT, false);
    }

    public boolean isChinaTelecom(int slotId) {
        return HuaweiTelephonyConfigs.isChinaTelecom() || isCTSimCard(slotId);
    }

    public boolean isCTSimCard(int slotId) {
        boolean isCTCardType;
        boolean result;
        int cardType = getCardType(slotId);
        Rlog.d(TAG, "[isCTSimCard]: cardType = " + cardType);
        if (cardType == 30 || cardType == 41 || cardType == 43) {
            isCTCardType = true;
        } else {
            isCTCardType = false;
        }
        if (!isCTCardType || !HuaweiTelephonyConfigs.isHisiPlatform()) {
            result = isCTCardType;
        } else {
            boolean isCdmaCplmn = false;
            String cplmn = getCplmn(slotId);
            String[] strArr = CDMA_CPLMNS;
            int length = strArr.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                } else if (strArr[i].equals(cplmn)) {
                    isCdmaCplmn = true;
                    break;
                } else {
                    i++;
                }
            }
            Rlog.d(TAG, "[isCTSimCard]: hisi cdma  isCdmaCplmn = " + isCdmaCplmn);
            result = isCdmaCplmn;
            if (TextUtils.isEmpty(cplmn)) {
                try {
                    result = getIHwTelephony().isCtSimCard(slotId);
                } catch (RemoteException e) {
                    Rlog.e(TAG, "isCTSimCard RemoteException");
                }
            }
            Rlog.d(TAG, "[isCTSimCard]: hisi cdma  isCdmaCplmn according iccid = " + result);
        }
        if (result) {
            String preIccid = SystemProperties.get("gsm.sim.preiccid_" + slotId, "");
            if (GC_ICCID.equals(preIccid)) {
                result = false;
                Rlog.d(TAG, "Hongkong GC card is not CT card:" + preIccid);
            }
        }
        Rlog.d(TAG, "[isCTSimCard]: result = " + result);
        return result;
    }

    private String getCplmn(int slotId) {
        String result = "";
        if (slotId == 0) {
            result = SystemProperties.get(PROP_VALUE_C_CARD0_PLMN, "");
        } else if (slotId == 1) {
            result = SystemProperties.get(PROP_VALUE_C_CARD1_PLMN, "");
        }
        if (TextUtils.isEmpty(result)) {
            String value = SystemProperties.get(PROP_VALUE_C_CARD_PLMN, "");
            if (value != null && !"".equals(value)) {
                String[] substr = value.split(",");
                if (substr.length == 2 && Integer.parseInt(substr[1]) == slotId) {
                    result = substr[0];
                }
            }
        }
        Rlog.d(TAG, "getCplmn for Slot : " + slotId + " result is : " + result);
        return result;
    }

    public boolean isCDMASimCard(int slotId) {
        int cardType = getCardType(slotId);
        Rlog.d(TAG, "[isCDMASimCard]: cardType = " + cardType);
        if (!(cardType == 30 || cardType == 43)) {
            switch (cardType) {
                case 40:
                case 41:
                    break;
                default:
                    return false;
            }
        }
        return true;
    }

    public int getCardType(int slotId) {
        if (slotId == 0) {
            return SystemProperties.getInt(CARD_TYPE_SIM1, -1);
        }
        if (slotId == 1) {
            return SystemProperties.getInt(CARD_TYPE_SIM2, -1);
        }
        return -1;
    }

    public boolean isDomesticCard(int slotId) {
        try {
            return getIHwTelephony().isDomesticCard(slotId);
        } catch (RemoteException e) {
            return true;
        } catch (NullPointerException e2) {
            return true;
        }
    }

    public boolean isCTCdmaCardInGsmMode() {
        try {
            return getIHwTelephony().isCTCdmaCardInGsmMode();
        } catch (RemoteException e) {
            return false;
        } catch (NullPointerException e2) {
            return false;
        }
    }

    public void setDefaultMobileEnable(boolean enabled) {
        try {
            Rlog.d(TAG, "setDefaultMobileEnable to " + enabled);
            getIHwTelephony().setDefaultMobileEnable(enabled);
        } catch (RemoteException | NullPointerException e) {
        }
    }

    public void setDataEnabledWithoutPromp(boolean enabled) {
        try {
            getIHwTelephony().setDataEnabledWithoutPromp(enabled);
        } catch (RemoteException | NullPointerException e) {
        }
    }

    public void setDataRoamingEnabledWithoutPromp(boolean enabled) {
        try {
            getIHwTelephony().setDataRoamingEnabledWithoutPromp(enabled);
        } catch (RemoteException e) {
            Rlog.e(TAG, "setDataRoamingEnabledWithoutPromp RemoteException");
        }
    }

    public int getDataState(long subId) {
        if (subId >= 0) {
            try {
                if (subId < ((long) TelephonyManager.getDefault().getPhoneCount())) {
                    return getIHwTelephony().getDataStateForSubscriber((int) subId);
                }
            } catch (RemoteException e) {
                return 0;
            } catch (NullPointerException e2) {
                return 0;
            }
        }
        return 0;
    }

    @Deprecated
    public void setLteServiceAbility(int ability) {
        try {
            getIHwTelephony().setLteServiceAbility(ability);
        } catch (RemoteException e) {
            Rlog.e(TAG, "setLteServiceAbility RemoteException");
        }
    }

    @Deprecated
    public int getLteServiceAbility() {
        try {
            return getIHwTelephony().getLteServiceAbility();
        } catch (RemoteException e) {
            return 0;
        }
    }

    public boolean isDualImsSupported() {
        if (!TelephonyManager.getDefault().isMultiSimEnabled()) {
            return false;
        }
        return HwModemCapability.isCapabilitySupport(21);
    }

    public boolean isImeiBindSlotSupported() {
        if (!TelephonyManager.getDefault().isMultiSimEnabled()) {
            return false;
        }
        return HwModemCapability.isCapabilitySupport(26);
    }

    @Deprecated
    public int getLteServiceAbility(int subId) {
        try {
            return getIHwTelephony().getLteServiceAbilityForSubId(subId);
        } catch (RemoteException e) {
            Rlog.e(TAG, "getLteServiceAbility RemoteException");
            return 0;
        }
    }

    @Deprecated
    public void setLteServiceAbility(int subId, int ability) {
        try {
            getIHwTelephony().setLteServiceAbilityForSubId(subId, ability);
        } catch (RemoteException e) {
            Rlog.e(TAG, "setLteServiceAbility RemoteException");
        }
    }

    public void setServiceAbility(int subId, int type, int ability) {
        try {
            getIHwTelephony().setServiceAbilityForSubId(subId, type, ability);
        } catch (RemoteException e) {
            Rlog.e(TAG, "setXGServiceAbilityForSubId RemoteException");
        }
    }

    public int getServiceAbility(int subId, int type) {
        try {
            return getIHwTelephony().getServiceAbilityForSubId(subId, type);
        } catch (RemoteException e) {
            Rlog.e(TAG, "getXGServiceAbilityForSubId RemoteException");
            return 0;
        }
    }

    public int getNetworkModeFromDB(int subId) {
        try {
            return getIHwTelephony().getNetworkModeFromDB(subId);
        } catch (RemoteException e) {
            Rlog.e(TAG, "getNetworkModeFromDB RemoteException");
            return -1;
        }
    }

    public void saveNetworkModeToDB(int subId, int mode) {
        try {
            getIHwTelephony().saveNetworkModeToDB(subId, mode);
        } catch (RemoteException e) {
            Rlog.e(TAG, "saveNetworkModeToDB RemoteException");
        }
    }

    public void setImsRegistrationState(int subId, boolean registered) {
        try {
            getIHwTelephony().setImsRegistrationStateForSubId(subId, registered);
        } catch (RemoteException e) {
            Rlog.e(TAG, "setImsRegistrationState RemoteException");
        }
    }

    public boolean isImsRegistered(int subId) {
        try {
            return getIHwTelephony().isImsRegisteredForSubId(subId);
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean isVolteAvailable(int subId) {
        try {
            return getIHwTelephony().isVolteAvailableForSubId(subId);
        } catch (RemoteException e) {
            return false;
        } catch (NullPointerException e2) {
            return false;
        }
    }

    public boolean isVideoTelephonyAvailable(int subId) {
        try {
            return getIHwTelephony().isVideoTelephonyAvailableForSubId(subId);
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean isWifiCallingAvailable(int subId) {
        try {
            return getIHwTelephony().isWifiCallingAvailableForSubId(subId);
        } catch (RemoteException e) {
            return false;
        }
    }

    public void set2GServiceAbility(int ability) {
        try {
            getIHwTelephony().set2GServiceAbility(ability);
        } catch (RemoteException e) {
            Rlog.e(TAG, "set2GServiceAbility failed ,RemoteException");
        }
    }

    public int get2GServiceAbility() {
        try {
            return getIHwTelephony().get2GServiceAbility();
        } catch (RemoteException e) {
            return 0;
        }
    }

    public boolean isSubDeactivedByPowerOff(long sub) {
        Rlog.d(TAG, "In isSubDeactivedByPowerOff");
        try {
            return getIHwTelephony().isSubDeactivedByPowerOff(sub);
        } catch (RemoteException e) {
            Rlog.e(TAG, "isSubDeactivedByPowerOff RemoteException");
            return false;
        }
    }

    public boolean isNeedToRadioPowerOn(long sub) {
        Rlog.d(TAG, "In isNeedToRadioPowerOn");
        try {
            return getIHwTelephony().isNeedToRadioPowerOn(sub);
        } catch (RemoteException e) {
            Rlog.e(TAG, "isNeedToRadioPowerOn RemoteException");
            return true;
        }
    }

    public boolean isCardPresent(int slotId) {
        return TelephonyManager.getDefault().getSimState(slotId) != 1;
    }

    public void updateCrurrentPhone(int lteSlot) {
        try {
            getIHwTelephony().updateCrurrentPhone(lteSlot);
        } catch (RemoteException | NullPointerException e) {
        }
    }

    public void setDefaultDataSlotId(int slotId) {
        try {
            getIHwTelephony().setDefaultDataSlotId(slotId);
        } catch (RemoteException | NullPointerException e) {
        }
    }

    public int getDefault4GSlotId() {
        try {
            return getIHwTelephony().getDefault4GSlotId();
        } catch (RemoteException e) {
            return 0;
        }
    }

    public void setDefault4GSlotId(int slotId, Message msg) {
        Rlog.d(TAG, "In setDefault4GSlotId");
        try {
            getIHwTelephony().setDefault4GSlotId(slotId, msg);
        } catch (RemoteException e) {
            Rlog.e(TAG, "setDefault4GSlotId RemoteException");
        }
    }

    public boolean isSetDefault4GSlotIdEnabled() {
        Rlog.d(TAG, "In isSetDefault4GSlotIdEnabled");
        try {
            return getIHwTelephony().isSetDefault4GSlotIdEnabled();
        } catch (RemoteException e) {
            Rlog.e(TAG, "isSetDefault4GSlotIdEnabled RemoteException");
            return false;
        }
    }

    public void waitingSetDefault4GSlotDone(boolean waiting) {
        Rlog.d(TAG, "In waitingSetDefault4GSlotDone");
        try {
            getIHwTelephony().waitingSetDefault4GSlotDone(waiting);
        } catch (RemoteException ex) {
            Rlog.e(TAG, "RemoteException ex = " + ex);
        }
    }

    public String getIccATR() {
        String strATR;
        String strATR2 = SystemProperties.get("gsm.sim.hw_atr", "null");
        String strATR1 = SystemProperties.get("gsm.sim.hw_atr1", "null");
        Rlog.d(TAG, "getIccATR: [" + strATR + "]");
        return strATR;
    }

    public DataSettingModeType getDataSettingMode() {
        boolean isLteEnabled = SystemProperties.getBoolean(PROP_LTE_ENABLED, true);
        boolean isLteTddEnabled = SystemProperties.getBoolean(PROP_LTETDD_ENABLED, false);
        Rlog.d(TAG, "in getDataSettingMode isLteEnabled=" + isLteEnabled + " isLteTddEnabled=" + isLteTddEnabled);
        if (!isLteEnabled) {
            return DataSettingModeType.MODE_LTE_OFF;
        }
        if (isLteTddEnabled) {
            return DataSettingModeType.MODE_LTETDD_ONLY;
        }
        return DataSettingModeType.MODE_LTE_AND_AUTO;
    }

    private void doSetPreferredNetworkType(int nwMode) {
        Rlog.d(TAG, "[enter]doSetPreferredNetworkType nwMode:" + nwMode);
        try {
            getIHwTelephony().setPreferredNetworkType(nwMode);
        } catch (RemoteException e) {
        } catch (Exception e2) {
            Rlog.e(TAG, "doSetPreferredNetworkType failed!");
        }
    }

    private void doSetDataSettingModeFromLteAndAuto(DataSettingModeType dataMode) {
        if (AnonymousClass1.$SwitchMap$android$telephony$HwTelephonyManagerInner$DataSettingModeType[dataMode.ordinal()] != 1) {
            Rlog.e(TAG, "doSetDataSettingModeFromLteAndAuto failed! param err mode =" + dataMode);
            return;
        }
        doSetPreferredNetworkType(30);
    }

    private void doSetDataSettingModeFromLteTddOnly(DataSettingModeType dataMode) {
        if (AnonymousClass1.$SwitchMap$android$telephony$HwTelephonyManagerInner$DataSettingModeType[dataMode.ordinal()] != 2) {
            Rlog.e(TAG, "doSetDataSettingModeLteTddOnly failed! param err mode =" + dataMode);
            return;
        }
        doSetPreferredNetworkType(61);
    }

    public void setDataSettingMode(DataSettingModeType dataMode) {
        if (dataMode == DataSettingModeType.MODE_LTETDD_ONLY || dataMode == DataSettingModeType.MODE_LTE_AND_AUTO) {
            switch (dataMode) {
                case MODE_LTETDD_ONLY:
                    doSetDataSettingModeFromLteAndAuto(dataMode);
                    break;
                case MODE_LTE_AND_AUTO:
                    doSetDataSettingModeFromLteTddOnly(dataMode);
                    break;
            }
            return;
        }
        Rlog.e(TAG, "setDataSettingMode failed! param err mode =" + dataMode);
    }

    public boolean isSubDeactived(int subId) {
        return false;
    }

    public int getPreferredDataSubscription() {
        try {
            return getIHwTelephony().getPreferredDataSubscription();
        } catch (RemoteException e) {
            return -1;
        }
    }

    public int getOnDemandDataSubId() {
        try {
            return getIHwTelephony().getOnDemandDataSubId();
        } catch (RemoteException e) {
            return -1;
        }
    }

    public String getCdmaGsmImsi() {
        try {
            return getIHwTelephony().getCdmaGsmImsi();
        } catch (RemoteException e) {
            return null;
        }
    }

    public String getCdmaGsmImsiForSubId(int subId) {
        try {
            return getIHwTelephony().getCdmaGsmImsiForSubId(subId);
        } catch (RemoteException e) {
            Rlog.e(TAG, "getCdmaGsmImsiForSubId RemoteException");
            return null;
        }
    }

    public int getUiccCardType(int slotId) {
        try {
            return getIHwTelephony().getUiccCardType(slotId);
        } catch (RemoteException e) {
            return -1;
        }
    }

    public CellLocation getCellLocation(int slotId) {
        try {
            Bundle bundle = getIHwTelephony().getCellLocation(slotId);
            if (bundle != null) {
                if (!bundle.isEmpty()) {
                    CellLocation cl = CellLocation.newFromBundle(bundle, slotId);
                    if (cl == null || cl.isEmpty()) {
                        return null;
                    }
                    return cl;
                }
            }
            return null;
        } catch (RemoteException e) {
            return null;
        } catch (NullPointerException e2) {
            return null;
        }
    }

    public String getCdmaMlplVersion() {
        try {
            return getIHwTelephony().getCdmaMlplVersion();
        } catch (RemoteException e) {
            return null;
        }
    }

    public String getCdmaMsplVersion() {
        try {
            return getIHwTelephony().getCdmaMsplVersion();
        } catch (RemoteException e) {
            return null;
        }
    }

    public void printCallingAppNameInfo(boolean enable, Context context) {
        if (context != null) {
            int callingPid = Process.myPid();
            String appName = "";
            ActivityManager am = (ActivityManager) context.getSystemService("activity");
            if (am != null) {
                List<ActivityManager.RunningAppProcessInfo> appProcessList = am.getRunningAppProcesses();
                if (appProcessList != null) {
                    for (ActivityManager.RunningAppProcessInfo appProcess : appProcessList) {
                        if (appProcess.pid == callingPid) {
                            appName = appProcess.processName;
                        }
                    }
                    Rlog.d(TAG, "setDataEnabled: calling app is( " + appName + " ) setEanble( " + enable + " )");
                    triggerChrAppCloseDataSwitch(appName, enable, context);
                }
            }
        }
    }

    public boolean isAppInWhiteList(String appName) {
        if (CoverManager.HALL_STATE_RECEIVER_PHONE.equals(appName) || "system".equals(appName) || "com.android.systemui".equals(appName) || "com.android.settings".equals(appName) || "com.huawei.systemmanager".equals(appName) || "com.huawei.vassistant".equals(appName) || "com.huawei.systemmanager:service".equals(appName)) {
            return true;
        }
        return false;
    }

    public void triggerChrAppCloseDataSwitch(String appName, boolean enable, Context context) {
        if (appName != null && context != null) {
            if (!isAppInWhiteList(appName)) {
                Rlog.d(TAG, "app" + appName + " operate data switch! trigger Chr!");
                Intent apkIntent = new Intent(HwTelephonyIntentsInner.INTENT_DS_APP_CLOSE_DATA_SWITCH);
                apkIntent.putExtra("appname", appName);
                context.sendBroadcast(apkIntent, CHR_BROADCAST_PERMISSION);
            }
            TelephonyManager.getDefault().setDataEnabledProperties(appName, enable);
        }
    }

    public String getUniqueDeviceId(int scope) {
        if (scope == 0) {
            try {
                if (mDeviceIdAll == null) {
                    mDeviceIdAll = getIHwTelephony().getUniqueDeviceId(0);
                }
                return mDeviceIdAll;
            } catch (RemoteException e) {
                Rlog.e(TAG, "getUniqueDeviceId RemoteException");
                return null;
            } catch (NullPointerException e2) {
                Rlog.e(TAG, "getUniqueDeviceId NullPointerException");
                return null;
            }
        } else if (scope != 1) {
            return getIHwTelephony().getUniqueDeviceId(scope);
        } else {
            if (mDeviceIdIMEI == null) {
                mDeviceIdIMEI = getIHwTelephony().getUniqueDeviceId(1);
            }
            return mDeviceIdIMEI;
        }
    }

    public boolean isLTESupported() {
        try {
            return getIHwTelephony().isLTESupported();
        } catch (RemoteException e) {
            return true;
        } catch (NullPointerException e2) {
            return true;
        }
    }

    public int getSpecCardType(int slotId) {
        try {
            return getIHwTelephony().getSpecCardType(slotId);
        } catch (RemoteException e) {
            return -1;
        }
    }

    public boolean isCardUimLocked(int slotId) {
        try {
            return getIHwTelephony().isCardUimLocked(slotId);
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean isRadioOn(int slot) {
        try {
            return getIHwTelephony().isRadioOn(slot);
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean isPlatformSupportVsim() {
        return HwVSimManager.getDefault().isPlatformSupportVsim();
    }

    public boolean hasIccCardForVSim(int slotId) {
        return HwVSimManager.getDefault().hasIccCardForVSim(slotId);
    }

    public int getSimStateForVSim(int slotIdx) {
        return HwVSimManager.getDefault().getSimStateForVSim(slotIdx);
    }

    public int getVSimSubId() {
        return HwVSimManager.getDefault().getVSimSubId();
    }

    public int enableVSim(String imsi, int cardtype, int apntype, String acqorder, String challenge) {
        return HwVSimManager.getDefault().enableVSim(1, imsi, cardtype, apntype, acqorder, challenge);
    }

    public boolean disableVSim() {
        return HwVSimManager.getDefault().disableVSim();
    }

    public int setApn(int cardtype, int apntype, String challenge) {
        return HwVSimManager.getDefault().enableVSim(2, null, cardtype, apntype, null, challenge);
    }

    public int getSimMode(int subId) {
        return HwVSimManager.getDefault().getSimMode(subId);
    }

    public void recoverSimMode() {
        HwVSimManager.getDefault().recoverSimMode();
    }

    public String getRegPlmn(int subId) {
        return HwVSimManager.getDefault().getRegPlmn(subId);
    }

    public String getTrafficData() {
        return HwVSimManager.getDefault().getTrafficData();
    }

    public Boolean clearTrafficData() {
        return HwVSimManager.getDefault().clearTrafficData();
    }

    public int getSimStateViaSysinfoEx(int subId) {
        return HwVSimManager.getDefault().getSimStateViaSysinfoEx(subId);
    }

    public int getCpserr(int subId) {
        return HwVSimManager.getDefault().getCpserr(subId);
    }

    public int scanVsimAvailableNetworks(int subId, int type) {
        return HwVSimManager.getDefault().scanVsimAvailableNetworks(subId, type);
    }

    public boolean setUserReservedSubId(int subId) {
        return HwVSimManager.getDefault().setUserReservedSubId(subId);
    }

    public int getUserReservedSubId() {
        return HwVSimManager.getDefault().getUserReservedSubId();
    }

    public String getDevSubMode(int subscription) {
        return HwVSimManager.getDefault().getDevSubMode(subscription);
    }

    public String getPreferredNetworkTypeForVSim(int subscription) {
        return HwVSimManager.getDefault().getPreferredNetworkTypeForVSim(subscription);
    }

    public int getVSimCurCardType() {
        return HwVSimManager.getDefault().getVSimCurCardType();
    }

    public int getVSimFineState() {
        return 0;
    }

    public int getVSimCachedSubId() {
        return -1;
    }

    public boolean getWaitingSwitchBalongSlot() {
        try {
            return getIHwTelephony().getWaitingSwitchBalongSlot();
        } catch (RemoteException e) {
            return false;
        }
    }

    public String getCallingAppName(Context context) {
        if (context == null) {
            return "";
        }
        if (!haveCheckedAppName) {
            int callingPid = Process.myPid();
            ActivityManager am = (ActivityManager) context.getSystemService("activity");
            if (am == null) {
                return "";
            }
            List<ActivityManager.RunningAppProcessInfo> appProcessList = am.getRunningAppProcesses();
            if (appProcessList == null) {
                return "";
            }
            Iterator<ActivityManager.RunningAppProcessInfo> it = appProcessList.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                ActivityManager.RunningAppProcessInfo appProcess = it.next();
                if (appProcess.pid == callingPid) {
                    setCallingAppName(appProcess.processName);
                    Rlog.d(TAG, "setCallingAppName : " + appProcess.processName);
                    break;
                }
            }
            setHaveCheckedAppName(true);
        }
        return callingAppName;
    }

    private static void setCallingAppName(String name) {
        callingAppName = name;
    }

    private static void setHaveCheckedAppName(boolean value) {
        haveCheckedAppName = value;
    }

    public boolean setISMCOEX(String ATCommand) {
        try {
            Rlog.d(TAG, "setISMCOEX = " + ATCommand);
            return getIHwTelephony().setISMCOEX(ATCommand);
        } catch (RemoteException e) {
            Rlog.e(TAG, "setISMCOEX RemoteException");
            return false;
        } catch (NullPointerException e2) {
            Rlog.e(TAG, "setISMCOEX NullPointerException");
            return false;
        }
    }

    public String[] queryServiceCellBand() {
        Rlog.d(TAG, "queryServiceCellBand.");
        try {
            return getIHwTelephony().queryServiceCellBand();
        } catch (RemoteException e) {
            Rlog.e(TAG, "queryServiceCellBand RemoteException");
            return new String[0];
        } catch (NullPointerException e2) {
            Rlog.e(TAG, "queryServiceCellBand NullPointerException");
            return new String[0];
        }
    }

    public boolean registerForRadioAvailable(IPhoneCallback callback) {
        try {
            Rlog.d(TAG, "registerForRadioAvailable");
            return getIHwTelephony().registerForRadioAvailable(callback);
        } catch (RemoteException e) {
            Rlog.e(TAG, "registerForRadioAvailable RemoteException");
            return false;
        } catch (NullPointerException e2) {
            Rlog.e(TAG, "registerForRadioAvailable NullPointerException");
            return false;
        }
    }

    public boolean unregisterForRadioAvailable(IPhoneCallback callback) {
        try {
            Rlog.d(TAG, "unregisterForRadioAvailable");
            return getIHwTelephony().unregisterForRadioAvailable(callback);
        } catch (RemoteException e) {
            Rlog.e(TAG, "unregisterForRadioAvailable RemoteException");
            return false;
        } catch (NullPointerException e2) {
            Rlog.e(TAG, "unregisterForRadioAvailable NullPointerException");
            return false;
        }
    }

    public boolean registerForRadioNotAvailable(IPhoneCallback callback) {
        try {
            Rlog.d(TAG, "registerForRadioNotAvailable");
            return getIHwTelephony().registerForRadioNotAvailable(callback);
        } catch (RemoteException e) {
            Rlog.e(TAG, "registerForRadioNotAvailable RemoteException");
            return false;
        } catch (NullPointerException e2) {
            Rlog.e(TAG, "registerForRadioNotAvailable NullPointerException");
            return false;
        }
    }

    public boolean unregisterForRadioNotAvailable(IPhoneCallback callback) {
        try {
            Rlog.d(TAG, "unregisterForRadioNotAvailable");
            return getIHwTelephony().unregisterForRadioNotAvailable(callback);
        } catch (RemoteException e) {
            Rlog.e(TAG, "unregisterForRadioNotAvailable RemoteException");
            return false;
        } catch (NullPointerException e2) {
            Rlog.e(TAG, "unregisterForRadioNotAvailable NullPointerException");
            return false;
        }
    }

    public boolean registerCommonImsaToMapconInfo(IPhoneCallback callback) {
        try {
            Rlog.d(TAG, "registerCommonImsaToMapconInfo");
            return getIHwTelephony().registerCommonImsaToMapconInfo(callback);
        } catch (RemoteException e) {
            Rlog.e(TAG, "registerCommonImsaToMapconInfo RemoteException");
            return false;
        } catch (NullPointerException e2) {
            Rlog.e(TAG, "registerCommonImsaToMapconInfo NullPointerException");
            return false;
        }
    }

    public boolean unregisterCommonImsaToMapconInfo(IPhoneCallback callback) {
        try {
            Rlog.d(TAG, "unregisterCommonImsaToMapconInfo");
            return getIHwTelephony().unregisterCommonImsaToMapconInfo(callback);
        } catch (RemoteException e) {
            Rlog.e(TAG, "unregisterCommonImsaToMapconInfo RemoteException");
            return false;
        } catch (NullPointerException e2) {
            Rlog.e(TAG, "unregisterCommonImsaToMapconInfo NullPointerException");
            return false;
        }
    }

    public boolean isRadioAvailable() {
        try {
            Rlog.d(TAG, "isRadioAvailable");
            return getIHwTelephony().isRadioAvailable();
        } catch (RemoteException e) {
            Rlog.e(TAG, "isRadioAvailable RemoteException");
            return false;
        } catch (NullPointerException e2) {
            Rlog.e(TAG, "isRadioAvailable NullPointerException");
            return false;
        }
    }

    public void setImsSwitch(boolean value) {
        try {
            Rlog.d(TAG, "setImsSwitch" + value);
            getIHwTelephony().setImsSwitch(value);
        } catch (RemoteException e) {
            Rlog.e(TAG, "setImsSwitch RemoteException");
        }
    }

    public boolean getImsSwitch() {
        try {
            Rlog.d(TAG, "getImsSwitch");
            return getIHwTelephony().getImsSwitch();
        } catch (RemoteException e) {
            Rlog.e(TAG, "getImsSwitch RemoteException");
            return false;
        } catch (NullPointerException e2) {
            Rlog.e(TAG, "getImsSwitch NullPointerException");
            return false;
        }
    }

    public void setImsDomainConfig(int domainType) {
        try {
            Rlog.d(TAG, "setImsDomainConfig");
            getIHwTelephony().setImsDomainConfig(domainType);
        } catch (RemoteException e) {
            Rlog.e(TAG, "setImsDomainConfig RemoteException");
        } catch (NullPointerException e2) {
            Rlog.e(TAG, "setImsDomainConfig NullPointerException");
        }
    }

    public boolean handleMapconImsaReq(byte[] Msg) {
        try {
            Rlog.d(TAG, "handleMapconImsaReq");
            return getIHwTelephony().handleMapconImsaReq(Msg);
        } catch (RemoteException e) {
            Rlog.e(TAG, "handleMapconImsaReq RemoteException");
            return false;
        } catch (NullPointerException e2) {
            Rlog.e(TAG, "handleMapconImsaReq NullPointerException");
            return false;
        }
    }

    public int getUiccAppType() {
        try {
            Rlog.d(TAG, "getUiccAppType");
            return getIHwTelephony().getUiccAppType();
        } catch (RemoteException e) {
            Rlog.e(TAG, "getUiccAppType RemoteException");
            return 0;
        } catch (NullPointerException e2) {
            Rlog.e(TAG, "getUiccAppType NullPointerException");
            return 0;
        }
    }

    public int getImsDomain() {
        try {
            Rlog.d(TAG, "getImsDomain");
            return getIHwTelephony().getImsDomain();
        } catch (RemoteException e) {
            Rlog.e(TAG, "getImsDomain RemoteException");
            return -1;
        } catch (NullPointerException e2) {
            Rlog.e(TAG, "getImsDomain NullPointerException");
            return -1;
        }
    }

    public UiccAuthResponse handleUiccAuth(int auth_type, byte[] rand, byte[] auth) {
        try {
            Rlog.d(TAG, "handleUiccAuth");
            return getIHwTelephony().handleUiccAuth(auth_type, rand, auth);
        } catch (RemoteException e) {
            Rlog.e(TAG, "handleUiccAuth RemoteException");
            return null;
        } catch (NullPointerException e2) {
            Rlog.e(TAG, "handleUiccAuth NullPointerException");
            return null;
        }
    }

    public boolean registerForPhoneEvent(int phoneId, IPhoneCallback callback, int events) {
        try {
            Rlog.d(TAG, "registerForPhoneEvent, phoneId = " + phoneId + " events = " + events + " callback = " + callback);
            return getIHwTelephony().registerForPhoneEvent(phoneId, callback, events);
        } catch (RemoteException e) {
            Rlog.e(TAG, "registerForPhoneEvent RemoteException");
            return false;
        }
    }

    public boolean unregisterForPhoneEvent(IPhoneCallback callback) {
        try {
            Rlog.d(TAG, "unregisterForPhoneEvent, callback = " + callback);
            getIHwTelephony().unregisterForPhoneEvent(callback);
            return true;
        } catch (RemoteException e) {
            Rlog.e(TAG, "unregisterForPhoneEvent RemoteException");
            return false;
        }
    }

    public boolean isRadioAvailable(int phoneId) {
        try {
            Rlog.d(TAG, "isRadioAvailable, phoneId = " + phoneId);
            return getIHwTelephony().isRadioAvailableByPhoneId(phoneId);
        } catch (RemoteException e) {
            Rlog.e(TAG, "isRadioAvailable RemoteException");
            return false;
        }
    }

    public void setImsSwitch(int phoneId, boolean value) {
        try {
            Rlog.d(TAG, "setImsSwitch, phoneId = " + phoneId + ", value = " + value);
            getIHwTelephony().setImsSwitchByPhoneId(phoneId, value);
        } catch (RemoteException e) {
            Rlog.e(TAG, "setImsSwitch RemoteException");
        }
    }

    public boolean getImsSwitch(int phoneId) {
        try {
            Rlog.d(TAG, "getImsSwitch, phoneId = " + phoneId);
            return getIHwTelephony().getImsSwitchByPhoneId(phoneId);
        } catch (RemoteException e) {
            Rlog.e(TAG, "getImsSwitch RemoteException");
            return false;
        } catch (NullPointerException e2) {
            Rlog.e(TAG, "getImsSwitch NullPointerException");
            return false;
        }
    }

    public void setImsDomainConfig(int phoneId, int domainType) {
        try {
            Rlog.d(TAG, "setImsDomainConfig, phoneId = " + phoneId);
            getIHwTelephony().setImsDomainConfigByPhoneId(phoneId, domainType);
        } catch (RemoteException e) {
            Rlog.e(TAG, "setImsDomainConfig RemoteException");
        } catch (NullPointerException e2) {
            Rlog.e(TAG, "setImsDomainConfig NullPointerException");
        }
    }

    public boolean handleMapconImsaReq(int phoneId, byte[] Msg) {
        try {
            Rlog.d(TAG, "handleMapconImsaReq, phoneId = " + phoneId);
            return getIHwTelephony().handleMapconImsaReqByPhoneId(phoneId, Msg);
        } catch (RemoteException e) {
            Rlog.e(TAG, "handleMapconImsaReq RemoteException");
            return false;
        } catch (NullPointerException e2) {
            Rlog.e(TAG, "handleMapconImsaReq NullPointerException");
            return false;
        }
    }

    public int getUiccAppType(int phoneId) {
        try {
            Rlog.d(TAG, "getUiccAppType, phoneId = " + phoneId);
            return getIHwTelephony().getUiccAppTypeByPhoneId(phoneId);
        } catch (RemoteException e) {
            Rlog.e(TAG, "getUiccAppType RemoteException");
            return 0;
        }
    }

    public int getImsDomain(int phoneId) {
        try {
            Rlog.d(TAG, "getImsDomain, phoneId = " + phoneId);
            return getIHwTelephony().getImsDomainByPhoneId(phoneId);
        } catch (RemoteException e) {
            Rlog.e(TAG, "getImsDomain RemoteException");
            return -1;
        } catch (NullPointerException e2) {
            Rlog.e(TAG, "getImsDomain NullPointerException");
            return -1;
        }
    }

    public UiccAuthResponse handleUiccAuth(int phoneId, int auth_type, byte[] rand, byte[] auth) {
        try {
            Rlog.d(TAG, "handleUiccAuth, phoneId = " + phoneId);
            return getIHwTelephony().handleUiccAuthByPhoneId(phoneId, auth_type, rand, auth);
        } catch (RemoteException e) {
            Rlog.e(TAG, "handleUiccAuth RemoteException");
            return null;
        }
    }

    public boolean cmdForECInfo(int event, int action, byte[] buf) {
        try {
            return getIHwTelephony().cmdForECInfo(event, action, buf);
        } catch (RemoteException e) {
            Rlog.e(TAG, "cmdForECInfo RemoteException");
            return false;
        } catch (NullPointerException e2) {
            Rlog.e(TAG, "cmdForECInfo NullPointerException");
            return false;
        }
    }

    public void notifyCModemStatus(int status, PhoneCallback callback) {
        try {
            Rlog.d(TAG, "notifyCModemStatus");
            getIHwTelephony().notifyCModemStatus(status, callback.mCallbackStub);
        } catch (RemoteException e) {
            Rlog.e(TAG, "notifyCModemStatus RemoteException");
        } catch (NullPointerException e2) {
            Rlog.e(TAG, "notifyCModemStatus NullPointerException");
        }
    }

    public boolean notifyDeviceState(String device, String state, String extras) {
        try {
            Rlog.d(TAG, "notifyDeviceState, device =" + device + ", state = " + state);
            return getIHwTelephony().notifyDeviceState(device, state, extras);
        } catch (RemoteException e) {
            Rlog.e(TAG, "notifyDeviceState RemoteException");
            return false;
        } catch (NullPointerException e2) {
            Rlog.e(TAG, "notifyDeviceState NullPointerException");
            return false;
        }
    }

    public boolean isDataConnectivityDisabled(int slotId, String tag) {
        Bundle bundle = this.mDpm.getPolicy(null, tag);
        boolean allow = false;
        if (bundle != null) {
            allow = bundle.getBoolean("value");
        }
        return true == allow && 1 == slotId;
    }

    public void notifyCellularCommParaReady(int paratype, int pathtype, Message response) {
        try {
            Rlog.d(TAG, "notifyCellularCommParaReady: paratype = " + paratype + ", pathtype = " + pathtype);
            getIHwTelephony().notifyCellularCommParaReady(paratype, pathtype, response);
        } catch (RemoteException e) {
            Rlog.e(TAG, "notifyCellularCommParaReady RemoteException");
        }
    }

    public boolean isRoamingPushDisabled() {
        Bundle bundle = this.mDpm.getPolicy(null, DISABLE_PUSH);
        Boolean allow = false;
        if (bundle != null) {
            Boolean allow2 = Boolean.valueOf(bundle.getBoolean("value"));
            Rlog.d(TAG, "isRoamingPushDisabled: " + allow2);
            return allow2.booleanValue();
        }
        Rlog.d(TAG, "has not set the allow, return default false");
        return allow.booleanValue();
    }

    public boolean setPinLockEnabled(boolean enablePinLock, String password, int subId) {
        try {
            Rlog.d(TAG, "setPinLockEnabled, enablePinLock =" + enablePinLock + ", subId = " + subId);
            return getIHwTelephony().setPinLockEnabled(enablePinLock, password, subId);
        } catch (RemoteException e) {
            Rlog.e(TAG, "notifyDeviceState RemoteException");
            return false;
        }
    }

    public boolean changeSimPinCode(String oldPinCode, String newPinCode, int subId) {
        try {
            Rlog.d(TAG, "changeSimPinCode, subId = " + subId);
            return getIHwTelephony().changeSimPinCode(oldPinCode, newPinCode, subId);
        } catch (RemoteException e) {
            Rlog.e(TAG, "notifyDeviceState RemoteException");
            return false;
        }
    }

    public boolean sendPseudocellCellInfo(int type, int lac, int cid, int radioTech, String plmn, int subId) {
        try {
            Rlog.d(TAG, "sendPseudocellCellInfo, type =" + type + ", lac = " + lac + ", cid = " + cid + ", radioTech = " + radioTech + ", plmn = " + plmn + ", subId = " + subId);
            return getIHwTelephony().sendPseudocellCellInfo(type, lac, cid, radioTech, plmn, subId);
        } catch (RemoteException ex) {
            Rlog.e(TAG, "sendPseudocellCellInfo RemoteException:" + ex.getMessage());
            return false;
        }
    }

    public boolean sendLaaCmd(int cmd, String reserved, Message response) {
        try {
            Rlog.d(TAG, "sendLaaCmd: cmd = " + cmd + ", reserved = " + reserved);
            return getIHwTelephony().sendLaaCmd(cmd, reserved, response);
        } catch (RemoteException e) {
            Rlog.e(TAG, "sendLaaCmd RemoteException");
            return false;
        }
    }

    public boolean getLaaDetailedState(String reserved, Message response) {
        try {
            Rlog.d(TAG, "getLaaDetailedState: reserved = " + reserved);
            return getIHwTelephony().getLaaDetailedState(reserved, response);
        } catch (RemoteException e) {
            Rlog.e(TAG, "getLaaDetailedState RemoteException");
            return false;
        }
    }

    public void registerForCallAltSrv(int subId, IPhoneCallback callback) {
        try {
            Rlog.d(TAG, "registerForCallAltSrv");
            getIHwTelephony().registerForCallAltSrv(subId, callback);
        } catch (RemoteException e) {
            Rlog.e(TAG, "registerForCallAltSrv RemoteException");
        }
    }

    public void unregisterForCallAltSrv(int subId) {
        try {
            Rlog.d(TAG, "unregisterForCallAltSrv");
            getIHwTelephony().unregisterForCallAltSrv(subId);
        } catch (RemoteException e) {
            Rlog.e(TAG, "unregisterForCallAltSrv RemoteException");
        }
    }

    public int invokeOemRilRequestRaw(int phoneId, byte[] oemReq, byte[] oemResp) {
        try {
            Rlog.d(TAG, "invokeOemRilRequestRaw, phoneId = " + phoneId);
            return getIHwTelephony().invokeOemRilRequestRaw(phoneId, oemReq, oemResp);
        } catch (RemoteException e) {
            Rlog.e(TAG, "invokeOemRilRequestRaw RemoteException");
            return -1;
        }
    }

    public boolean isCspPlmnEnabled(int subId) {
        try {
            Rlog.d(TAG, "isCspPlmnEnabled for subId: " + subId);
            return getIHwTelephony().isCspPlmnEnabled(subId);
        } catch (RemoteException e) {
            Rlog.e(TAG, "isCspPlmnEnabled RemoteException");
            return false;
        }
    }

    public void setCallForwardingOption(int subId, int commandInterfaceCFAction, int commandInterfaceCFReason, String dialingNumber, int timerSeconds, Message response) {
        try {
            Rlog.d(TAG, "setCallForwardingOption subId:" + subId + ", commandInterfaceCFAction:" + commandInterfaceCFAction + ", commandInterfaceCFReason:" + commandInterfaceCFReason + ", timerSeconds: " + timerSeconds);
            getIHwTelephony().setCallForwardingOption(subId, commandInterfaceCFAction, commandInterfaceCFReason, dialingNumber, timerSeconds, response);
        } catch (RemoteException e) {
            Rlog.e(TAG, "setCallForwardingOption RemoteException");
        }
    }

    public void getCallForwardingOption(int subId, int commandInterfaceCFReason, Message response) {
        try {
            Rlog.d(TAG, "getCallForwardingOption subId:" + subId + ", commandInterfaceCFReason:" + commandInterfaceCFReason);
            getIHwTelephony().getCallForwardingOption(subId, commandInterfaceCFReason, response);
        } catch (RemoteException e) {
            Rlog.e(TAG, "getCallForwardingOption RemoteException");
        }
    }

    public boolean setSubscription(int subId, boolean activate, Message response) {
        try {
            Rlog.d(TAG, "setSubscription subId:" + subId + ", activate: " + activate);
            return getIHwTelephony().setSubscription(subId, activate, response);
        } catch (RemoteException e) {
            Rlog.e(TAG, "setSubscription RemoteException");
            return false;
        }
    }

    public String getImsImpu(int subId) {
        try {
            Rlog.d(TAG, "getImsImpu");
            return getIHwTelephony().getImsImpu(subId);
        } catch (RemoteException e) {
            Rlog.e(TAG, "getImsImpu RemoteException");
            return null;
        }
    }

    public String getLine1NumberFromImpu(int subId) {
        try {
            Rlog.d(TAG, "getLine1NumberFromImpu");
            return getIHwTelephony().getLine1NumberFromImpu(subId);
        } catch (RemoteException e) {
            Rlog.e(TAG, "getLine1NumberFromImpu RemoteException");
            return null;
        }
    }

    public boolean isSecondaryCardGsmOnly() {
        try {
            return getIHwTelephony().isSecondaryCardGsmOnly();
        } catch (RemoteException e) {
            Rlog.e(TAG, "isSecondaryCardGsmOnly RemoteException");
            return false;
        }
    }

    public boolean isVSimEnabled() {
        return HwVSimManager.getDefault().isVSimEnabled();
    }

    public boolean bindSimToProfile(int slotId) {
        try {
            Rlog.d(TAG, "bindSimToProfile slotId = " + slotId);
            return getIHwTelephony().bindSimToProfile(slotId);
        } catch (RemoteException e) {
            Rlog.e(TAG, "bindSimToProfile RemoteException");
            return false;
        }
    }

    public boolean setLine1Number(int subId, String alphaTag, String number, Message onComplete) {
        try {
            return getIHwTelephony().setLine1Number(subId, alphaTag, number, onComplete);
        } catch (RemoteException e) {
            Rlog.e(TAG, "setLine1Number RemoteException");
            return false;
        }
    }

    public boolean setDeepNoDisturbState(int slotId, int state) {
        try {
            Rlog.d(TAG, "setDeepNoDisturbState slotId = " + slotId + " state = " + state);
            return getIHwTelephony().setDeepNoDisturbState(slotId, state);
        } catch (RemoteException ex) {
            Rlog.e(TAG, "setDeepNoDisturbState RemoteException:" + ex);
            return false;
        }
    }

    public boolean setUplinkFreqBandwidthReportState(int slotId, int state, IPhoneCallback callback) {
        try {
            Rlog.d(TAG, "setUplinkFreqBandwidthReportState slotId = " + slotId + " state = " + state);
            return getIHwTelephony().setUplinkFreqBandwidthReportState(slotId, state, callback);
        } catch (RemoteException e) {
            Rlog.e(TAG, "setUplinkFreqBandwidthReportState RemoteExceptio");
            return false;
        }
    }

    public void informModemTetherStatusToChangeGRO(int enable, String faceName) {
        try {
            getIHwTelephony().informModemTetherStatusToChangeGRO(enable, faceName);
        } catch (RemoteException e) {
            Rlog.e(TAG, "sendUSBinformationToRIL RemoteException");
        }
    }

    public boolean sendSimMatchedOperatorInfo(int slotId, String opKey, String opName, int state, String reserveField) {
        try {
            Rlog.d(TAG, "sendSimMatchedOperatorInfo slotId = " + slotId + ", opKey = " + opKey + ", opName =" + opName + ", state = " + state + ", reserveField = " + reserveField);
            return getIHwTelephony().sendSimMatchedOperatorInfo(slotId, opKey, opName, state, reserveField);
        } catch (RemoteException e) {
            Rlog.e(TAG, "sendSimMatchedOperatorInfo RemoteException");
            return false;
        }
    }

    public boolean is4RMimoEnabled(int subId) {
        try {
            return getIHwTelephony().is4RMimoEnabled(subId);
        } catch (RemoteException ex) {
            Rlog.e(TAG, "is4RMimoEnabled RemoteException:" + ex);
            return false;
        }
    }

    public boolean getAntiFakeBaseStation(Message response) {
        try {
            Rlog.d(TAG, "getAntiFakeBaseStation");
            return getIHwTelephony().getAntiFakeBaseStation(response);
        } catch (RemoteException ex) {
            Rlog.e(TAG, "getAntiFakeBaseStation RemoteException" + ex);
            return false;
        }
    }

    public byte[] getCardTrayInfo() {
        try {
            return getIHwTelephony().getCardTrayInfo();
        } catch (RemoteException e) {
            Rlog.e(TAG, "getCardTrayInfo RemoteException");
            return new byte[0];
        }
    }

    public boolean registerForAntiFakeBaseStation(IPhoneCallback callback) {
        try {
            Rlog.d(TAG, "registerForAntiFakeBaseStation");
            return getIHwTelephony().registerForAntiFakeBaseStation(callback);
        } catch (RemoteException e) {
            Rlog.e(TAG, "registerForAntiFakeBaseStation RemoteException");
            return false;
        }
    }

    public boolean unregisterForAntiFakeBaseStation() {
        try {
            Rlog.d(TAG, "unregisterForAntiFakeBaseStation");
            return getIHwTelephony().unregisterForAntiFakeBaseStation();
        } catch (RemoteException e) {
            Rlog.e(TAG, "unregisterForAntiFakeBaseStation RemoteException");
            return false;
        }
    }

    public boolean setCsconEnabled(boolean isEnabled) {
        try {
            Rlog.d(TAG, "setCsconEnabled isEnabled = " + isEnabled);
            return getIHwTelephony().setCsconEnabled(isEnabled);
        } catch (RemoteException e) {
            Rlog.e(TAG, "setCsconEnabled RemoteException");
            return false;
        }
    }

    public int[] getCsconEnabled() {
        int[] response = {-1, -1};
        try {
            Rlog.d(TAG, "getCsconEnabled");
            return getIHwTelephony().getCsconEnabled();
        } catch (RemoteException e) {
            Rlog.e(TAG, "getCsconEnabled RemoteException");
            return response;
        }
    }

    public int getLteLevel(SignalStrength strength) {
        if (strength.isCdma()) {
            if (IS_USE_RSRQ) {
                return getLevel(6, strength.getLteRsrp(), strength.getLteRsrq(), strength.getPhoneId());
            }
            return getLevel(6, strength.getLteRsrp(), strength.getLteRssnr(), strength.getPhoneId());
        } else if (IS_USE_RSRQ) {
            return getLevel(5, strength.getLteRsrp(), strength.getLteRsrq(), strength.getPhoneId());
        } else {
            return getLevel(5, strength.getLteRsrp(), strength.getLteRssnr(), strength.getPhoneId());
        }
    }

    public int getCdmaLevel(SignalStrength strength) {
        return getLevel(3, strength.getCdmaDbm(), strength.getCdmaEcio(), strength.getPhoneId());
    }

    public int getEvdoLevel(SignalStrength strength) {
        return getLevel(4, strength.getEvdoDbm(), strength.getEvdoSnr(), strength.getPhoneId());
    }

    public int getGsmLevel(SignalStrength strength) {
        int wcdmaLevel = getLevel(2, strength.getWcdmaRscp(), strength.getWcdmaEcio(), strength.getPhoneId());
        int gsmLevel = getLevel(1, strength.getGsmSignalStrength(), 255, strength.getPhoneId());
        if (wcdmaLevel == 0) {
            return gsmLevel;
        }
        return wcdmaLevel;
    }

    public int getNrLevel(SignalStrength strength) {
        if (IS_USE_RSRQ) {
            return getLevel(7, strength.getNrRsrp(), strength.getNrRsrq(), strength.getPhoneId());
        }
        return getLevel(7, strength.getNrRsrp(), strength.getNrRssnr(), strength.getPhoneId());
    }

    private int getLevel(int type, int rssi, int ecio, int phoneId) {
        try {
            return getIHwTelephony().getLevel(type, rssi, ecio, phoneId);
        } catch (RemoteException e) {
            Rlog.e(TAG, "getLevel RemoteException");
            return 0;
        }
    }

    public int getDataRegisteredState(int subId) {
        try {
            return getIHwTelephony().getDataRegisteredState(subId);
        } catch (RemoteException e) {
            Rlog.e(TAG, "getDataRegisteredState RemoteException");
            return 1;
        }
    }

    public int getVoiceRegisteredState(int subId) {
        try {
            return getIHwTelephony().getVoiceRegisteredState(subId);
        } catch (RemoteException e) {
            Rlog.e(TAG, "getVoiceRegisteredState RemoteException");
            return 1;
        }
    }

    public boolean isNrSupported() {
        return HwModemCapability.isCapabilitySupport(29);
    }

    public boolean setTemperatureControlToModem(int level, int type, int subId, Message response) {
        try {
            return getIHwTelephony().setTemperatureControlToModem(level, type, subId, response);
        } catch (RemoteException e) {
            Rlog.e(TAG, "setTemperatureControlToModem RemoteException");
            return false;
        }
    }

    public boolean isAISCard(int subId) {
        try {
            return getIHwTelephony().isAISCard(subId);
        } catch (RemoteException e) {
            Rlog.e(TAG, "isAISCard RemoteException");
            return true;
        }
    }

    public boolean isAisCustomDisable() {
        if (INVALID_MCCMNC.equals(SystemProperties.get(HW_CUST_SW_SIMLOCK, ""))) {
            return true;
        }
        return false;
    }

    public boolean isCustomAis() {
        try {
            return getIHwTelephony().isCustomAis();
        } catch (RemoteException e) {
            Rlog.e(TAG, "isCustomAis RemoteException");
            return false;
        }
    }
}
