package android.telephony;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.text.TextUtils;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.HwModemCapability;
import com.android.internal.telephony.HwTelephonyIntentsInner;
import com.android.internal.telephony.HwTelephonyProperties;
import com.android.internal.telephony.IHwTelephony;
import com.android.internal.telephony.IHwTelephonyInner;
import com.android.internal.telephony.IPhoneCallback;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.CellLocationEx;
import com.huawei.android.telephony.CellSignalStrengthCdmaEx;
import com.huawei.android.telephony.CellSignalStrengthGsmEx;
import com.huawei.android.telephony.CellSignalStrengthLteEx;
import com.huawei.android.telephony.CellSignalStrengthNrEx;
import com.huawei.android.telephony.CellSignalStrengthWcdmaEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.SignalStrengthEx;
import com.huawei.android.telephony.SubscriptionManagerEx;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.hwparttelephony.BuildConfig;
import com.huawei.internal.telephony.HwCommonPhoneCallback;
import com.huawei.internal.telephony.IHwCommonPhoneCallback;
import com.huawei.internal.telephony.vsim.IGetVsimServiceCallback;
import huawei.android.app.admin.HwDevicePolicyManagerEx;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class HwTelephonyManagerInner {
    public static final String CARD_TYPE_SIM1 = "gsm.sim1.type";
    public static final String CARD_TYPE_SIM2 = "gsm.sim2.type";
    private static final String[] CDMA_CPLMNS = {"46003", "45502", "46012"};
    public static final int CDMA_MODE = 0;
    private static final String CHR_BROADCAST_PERMISSION = "com.huawei.android.permission.GET_CHR_DATA";
    private static final String[] CT_CPLMNS = {"46003", "46005", "46011", "46012", "47008", "45502", "45507"};
    public static final int CT_NATIONAL_ROAMING_CARD = 41;
    public static final int CU_DUAL_MODE_CARD = 42;
    private static final String DEFAULT_MAIN_SLOT_CARRIER = "default_main_slot_carrier";
    public static final int DEFAULT_MAIN_SLOT_CARRIER_CMCC = 1;
    public static final int DEFAULT_MAIN_SLOT_CARRIER_CT = 2;
    public static final int DEFAULT_MAIN_SLOT_CARRIER_UNKNOWN = 0;
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
    private static final int INVALID = -1;
    private static final String INVALID_MCCMNC = "00000";
    private static boolean IS_USE_RSRQ = SystemPropertiesEx.getBoolean("ro.config.lte_use_rsrq", false);
    private static final int MODEM_CAP_SUPPORT_DUAL_NR = 31;
    private static final int NETWORK_MODE_UNKNOWN = -1;
    public static final int NOTIFY_CMODEM_STATUS_FAIL = -1;
    public static final int NOTIFY_CMODEM_STATUS_SUCCESS = 1;
    private static final int OPTION_UNKNOWN = 0;
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
    private static final int SIGNAL_TYPE_CDMA = 3;
    private static final int SIGNAL_TYPE_CDMALTE = 6;
    private static final int SIGNAL_TYPE_EVDO = 4;
    private static final int SIGNAL_TYPE_GSM = 1;
    private static final int SIGNAL_TYPE_LTE = 5;
    private static final int SIGNAL_TYPE_NR = 7;
    private static final int SIGNAL_TYPE_UMTS = 2;
    public static final int SINGLE_MODE_RUIM_CARD = 30;
    public static final int SINGLE_MODE_SIM_CARD = 10;
    public static final int SINGLE_MODE_USIM_CARD = 20;
    private static final int SUB_0 = 0;
    private static final int SUB_1 = 1;
    public static final int SUPPORT_SYSTEMAPP_GET_DEVICEID = 1;
    public static final int SWITCH_SERVICE_TYPE_NR = 0;
    private static final String TAG = "HwTelephonyManagerInner";
    public static final String TAG_MDM_CARRIER_CMCC = "cmcc";
    public static final String TAG_MDM_CARRIER_CT = "ct";
    private static final int TWO_SIM_COUNT = 2;
    public static final int UNKNOWN_CARD = -1;
    private static String callingAppName = BuildConfig.FLAVOR;
    private static boolean haveCheckedAppName = false;
    private static String mDeviceIdAll = null;
    private static String mDeviceIdIMEI = null;
    private static HwTelephonyManagerInner sInstance = new HwTelephonyManagerInner();
    private HwDevicePolicyManagerEx mDpm = new HwDevicePolicyManagerEx();

    public enum DataSettingModeType {
        MODE_LTE_OFF,
        MODE_LTETDD_ONLY,
        MODE_LTE_AND_AUTO,
        MODE_ERROR
    }

    static {
        Arrays.sort(CT_CPLMNS);
    }

    private HwTelephonyManagerInner() {
    }

    public static HwTelephonyManagerInner getDefault() {
        return sInstance;
    }

    private static void setCallingAppName(String name) {
        callingAppName = name;
    }

    private static void setHaveCheckedAppName(boolean value) {
        haveCheckedAppName = value;
    }

    private IHwTelephony getIHwTelephony() throws RemoteException {
        IHwTelephony iHwTelephony = IHwTelephony.Stub.asInterface(ServiceManagerEx.getService("phone_huawei"));
        if (iHwTelephony != null) {
            return iHwTelephony;
        }
        throw new RemoteException("getIHwTelephony return null");
    }

    private IHwTelephonyInner getIHwTelephonyInner() {
        try {
            return IHwTelephonyInner.Stub.asInterface(getIHwTelephony().getHwInnerService());
        } catch (RemoteException e) {
            RlogEx.e(TAG, "RemoteException error");
            return null;
        } catch (RuntimeException e2) {
            RlogEx.e(TAG, "RuntimeException error");
            return null;
        }
    }

    public String getDemoString() {
        try {
            return getIHwTelephony().getDemoString();
        } catch (RemoteException e) {
            RlogEx.e(TAG, "getDemoString RemoteException");
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

    public int getSubState(long slotId) {
        try {
            return getIHwTelephony().getSubState((int) slotId);
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
        String commrilMode = SystemPropertiesEx.get(HwTelephonyProperties.PROPERTY_COMMRIL_MODE, "NON_MODE");
        String cg_standby_mode = SystemPropertiesEx.get(HwTelephonyProperties.PROPERTY_CG_STANDBY_MODE, "home");
        if (!isFullNetworkSupported() || !"CG_MODE".equals(commrilMode)) {
            return false;
        }
        if (mode != 0) {
            if (mode != 1) {
                if (mode == 2 && !"home".equals(cg_standby_mode)) {
                    return true;
                }
            } else if ("roam_gsm".equals(cg_standby_mode)) {
                return true;
            }
        } else if (!"roam_gsm".equals(cg_standby_mode)) {
            return true;
        }
        return false;
    }

    public boolean isFullNetworkSupported() {
        return SystemPropertiesEx.getBoolean(HwTelephonyProperties.PROPERTY_FULL_NETWORK_SUPPORT, false);
    }

    public boolean isChinaTelecom(int slotId) {
        return HuaweiTelephonyConfigs.isChinaTelecom() || isCTSimCard(slotId);
    }

    public boolean isCTSimCard(int slotId) {
        boolean isCTCardType;
        boolean result;
        String cplmn;
        int cardType = getCardType(slotId);
        RlogEx.i(TAG, "[isCTSimCard]: cardType = " + cardType);
        if (cardType == 30 || cardType == 41 || cardType == 43) {
            isCTCardType = true;
        } else {
            isCTCardType = false;
        }
        if (!isCTCardType || !HuaweiTelephonyConfigs.isHisiPlatform()) {
            result = isCTCardType;
        } else {
            boolean isCdmaCplmn = false;
            String cplmn2 = getCplmn(slotId);
            String[] strArr = CDMA_CPLMNS;
            int length = strArr.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                } else if (strArr[i].equals(cplmn2)) {
                    isCdmaCplmn = true;
                    break;
                } else {
                    i++;
                }
            }
            RlogEx.i(TAG, "[isCTSimCard]: hisi cdma  isCdmaCplmn = " + isCdmaCplmn);
            result = isCdmaCplmn;
            if (TextUtils.isEmpty(cplmn2)) {
                try {
                    result = getIHwTelephony().isCtSimCard(slotId);
                } catch (RemoteException e) {
                    RlogEx.e(TAG, "isCTSimCard RemoteException");
                }
            }
            RlogEx.i(TAG, "[isCTSimCard]: hisi cdma  isCdmaCplmn according iccid = " + result);
        }
        if (!result && (cplmn = getSimOperator(slotId)) != null && Arrays.binarySearch(CT_CPLMNS, cplmn) >= 0) {
            result = true;
        }
        if (result) {
            String preIccid = SystemPropertiesEx.get("gsm.sim.preiccid_" + slotId, BuildConfig.FLAVOR);
            if (GC_ICCID.equals(preIccid)) {
                result = false;
                RlogEx.i(TAG, "Hongkong GC card is not CT card:" + preIccid);
            }
        }
        RlogEx.i(TAG, "[isCTSimCard]: result = " + result);
        return result;
    }

    private String getCplmn(int slotId) {
        String result = BuildConfig.FLAVOR;
        if (slotId == 0) {
            result = SystemPropertiesEx.get(PROP_VALUE_C_CARD0_PLMN, BuildConfig.FLAVOR);
        } else if (slotId == 1) {
            result = SystemPropertiesEx.get(PROP_VALUE_C_CARD1_PLMN, BuildConfig.FLAVOR);
        }
        if (!TextUtils.isEmpty(result)) {
            RlogEx.e(TAG, "result not null getCplmn for Slot : " + slotId + " result is : " + result);
            return result;
        }
        String value = SystemPropertiesEx.get(PROP_VALUE_C_CARD_PLMN, BuildConfig.FLAVOR);
        if (TextUtils.isEmpty(value)) {
            RlogEx.e(TAG, "PROP_VALUE_C_CARD_PLMN is null, getCplmn for Slot : " + slotId + " result is : " + result);
            return result;
        }
        String[] substr = value.split(",");
        if (substr.length == 2) {
            try {
                if (Integer.parseInt(substr[1]) == slotId) {
                    result = substr[0];
                }
            } catch (NumberFormatException e) {
                RlogEx.e(TAG, "getCplmn NumberFormatException.");
            }
        }
        RlogEx.i(TAG, "getCplmn for Slot : " + slotId + " result is : " + result);
        return result;
    }

    public boolean isCDMASimCard(int slotId) {
        int cardType = getCardType(slotId);
        RlogEx.i(TAG, "[isCDMASimCard]: cardType = " + cardType);
        if (cardType == 30 || cardType == 43 || cardType == 40 || cardType == 41) {
            return true;
        }
        return false;
    }

    public int getCardType(int slotId) {
        if (slotId == 0) {
            return SystemPropertiesEx.getInt("gsm.sim1.type", -1);
        }
        if (slotId == 1) {
            return SystemPropertiesEx.getInt("gsm.sim2.type", -1);
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
            RlogEx.i(TAG, "setDefaultMobileEnable to " + enabled);
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
            RlogEx.e(TAG, "setDataRoamingEnabledWithoutPromp RemoteException");
        }
    }

    public int getDataState(long slotId) {
        if (slotId >= 0) {
            try {
                if (slotId < ((long) TelephonyManagerEx.getDefault().getPhoneCount())) {
                    return getIHwTelephony().getDataStateForSubscriber((int) slotId);
                }
            } catch (RemoteException e) {
                return 0;
            } catch (NullPointerException e2) {
                return 0;
            }
        }
        return 0;
    }

    public int getLteServiceAbility() {
        try {
            return getIHwTelephony().getLteServiceAbility();
        } catch (RemoteException e) {
            return 0;
        }
    }

    public void setLteServiceAbility(int ability) {
        try {
            getIHwTelephony().setLteServiceAbility(ability);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "setLteServiceAbility RemoteException");
        }
    }

    public boolean isDualImsSupported() {
        if (!TelephonyManagerEx.isMultiSimEnabled()) {
            return false;
        }
        return HwModemCapability.isCapabilitySupport(21);
    }

    public boolean isImeiBindSlotSupported() {
        if (!TelephonyManagerEx.isMultiSimEnabled()) {
            return false;
        }
        return HwModemCapability.isCapabilitySupport(26);
    }

    public int getLteServiceAbility(int slotId) {
        try {
            return getIHwTelephony().getLteServiceAbilityForSlotId(slotId);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "getLteServiceAbility RemoteException");
            return 0;
        }
    }

    public void setLteServiceAbility(int slotId, int ability) {
        try {
            getIHwTelephony().setLteServiceAbilityForSlotId(slotId, ability);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "setLteServiceAbility RemoteException");
        }
    }

    public void setServiceAbility(int slotId, int type, int ability) {
        try {
            getIHwTelephony().setServiceAbilityForSlotId(slotId, type, ability);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "setXGServiceAbilityForSlotId RemoteException");
        }
    }

    public int getServiceAbility(int slotId, int type) {
        try {
            return getIHwTelephony().getServiceAbilityForSlotId(slotId, type);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "getXGServiceAbilityForSlotId RemoteException");
            return 0;
        }
    }

    public void setSwitchState(int type, int ability) {
        IHwTelephonyInner service = getIHwTelephonyInner();
        if (service == null) {
            RlogEx.e(TAG, "service is null");
            return;
        }
        try {
            service.setSwitchState(type, ability);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "setSwitchState RemoteException");
        }
    }

    public boolean getSwitchState(int type) {
        IHwTelephonyInner service = getIHwTelephonyInner();
        if (service == null) {
            RlogEx.e(TAG, "service is null");
            return false;
        }
        try {
            return service.getSwitchState(type);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "getSwitchState RemoteException");
            return false;
        }
    }

    public int getNetworkModeFromDB(int slotId) {
        try {
            return getIHwTelephony().getNetworkModeFromDB(slotId);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "getNetworkModeFromDB RemoteException");
            return -1;
        }
    }

    public void saveNetworkModeToDB(int slotId, int mode) {
        try {
            getIHwTelephony().saveNetworkModeToDB(slotId, mode);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "saveNetworkModeToDB RemoteException");
        }
    }

    public void setImsRegistrationState(int slotId, boolean registered) {
        try {
            getIHwTelephony().setImsRegistrationStateForSubId(slotId, registered);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "setImsRegistrationState RemoteException");
        }
    }

    public boolean isImsRegistered(int slotId) {
        try {
            return getIHwTelephony().isImsRegisteredForSubId(slotId);
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean isVolteAvailable(int slotId) {
        try {
            return getIHwTelephony().isVolteAvailableForSubId(slotId);
        } catch (RemoteException e) {
            return false;
        } catch (NullPointerException e2) {
            return false;
        }
    }

    public boolean isVideoTelephonyAvailable(int slotId) {
        try {
            return getIHwTelephony().isVideoTelephonyAvailableForSubId(slotId);
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean isWifiCallingAvailable(int slotId) {
        try {
            return getIHwTelephony().isWifiCallingAvailableForSubId(slotId);
        } catch (RemoteException e) {
            return false;
        }
    }

    public int get2GServiceAbility() {
        try {
            return getIHwTelephony().get2GServiceAbility();
        } catch (RemoteException e) {
            return 0;
        }
    }

    public void set2GServiceAbility(int ability) {
        try {
            getIHwTelephony().set2GServiceAbility(ability);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "set2GServiceAbility failed ,RemoteException");
        }
    }

    public boolean isSubDeactivedByPowerOff(long sub) {
        RlogEx.i(TAG, "In isSubDeactivedByPowerOff");
        try {
            return getIHwTelephony().isSubDeactivedByPowerOff(sub);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "isSubDeactivedByPowerOff RemoteException");
            return false;
        }
    }

    public boolean isNeedToRadioPowerOn(long sub) {
        RlogEx.i(TAG, "In isNeedToRadioPowerOn");
        try {
            return getIHwTelephony().isNeedToRadioPowerOn(sub);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "isNeedToRadioPowerOn RemoteException");
            return true;
        }
    }

    public boolean isCardPresent(int slotId) {
        return TelephonyManagerEx.getDefault().getSimState(slotId) != 1;
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
        RlogEx.i(TAG, "In setDefault4GSlotId");
        try {
            getIHwTelephony().setDefault4GSlotId(slotId, msg);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "setDefault4GSlotId RemoteException");
        }
    }

    public boolean isSetDefault4GSlotIdEnabled() {
        boolean isEnabled = false;
        try {
            isEnabled = getIHwTelephony().isSetDefault4GSlotIdEnabled();
        } catch (RemoteException e) {
            RlogEx.e(TAG, "isSetDefault4GSlotIdEnabled RemoteException");
        }
        RlogEx.i(TAG, "In isSetDefault4GSlotIdEnabled:" + isEnabled);
        return isEnabled;
    }

    public void waitingSetDefault4GSlotDone(boolean waiting) {
        RlogEx.i(TAG, "In waitingSetDefault4GSlotDone");
        try {
            getIHwTelephony().waitingSetDefault4GSlotDone(waiting);
        } catch (RemoteException ex) {
            RlogEx.e(TAG, "RemoteException ex = " + ex);
        }
    }

    public String getIccATR() {
        String strATR = SystemPropertiesEx.get("gsm.sim.hw_atr", "null") + "," + SystemPropertiesEx.get("gsm.sim.hw_atr1", "null");
        RlogEx.i(TAG, "getIccATR: [" + strATR + "]");
        return strATR;
    }

    public DataSettingModeType getDataSettingMode() {
        boolean isLteEnabled = SystemPropertiesEx.getBoolean(PROP_LTE_ENABLED, true);
        boolean isLteTddEnabled = SystemPropertiesEx.getBoolean(PROP_LTETDD_ENABLED, false);
        RlogEx.i(TAG, "in getDataSettingMode isLteEnabled=" + isLteEnabled + " isLteTddEnabled=" + isLteTddEnabled);
        if (!isLteEnabled) {
            return DataSettingModeType.MODE_LTE_OFF;
        }
        if (isLteTddEnabled) {
            return DataSettingModeType.MODE_LTETDD_ONLY;
        }
        return DataSettingModeType.MODE_LTE_AND_AUTO;
    }

    public void setDataSettingMode(DataSettingModeType dataMode) {
        if (dataMode == DataSettingModeType.MODE_LTETDD_ONLY || dataMode == DataSettingModeType.MODE_LTE_AND_AUTO) {
            int i = AnonymousClass1.$SwitchMap$android$telephony$HwTelephonyManagerInner$DataSettingModeType[dataMode.ordinal()];
            if (i == 1) {
                doSetDataSettingModeFromLteTddOnly(dataMode);
            } else if (i == 2) {
                doSetDataSettingModeFromLteAndAuto(dataMode);
            }
        } else {
            RlogEx.e(TAG, "setDataSettingMode failed! param err mode =" + dataMode);
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: android.telephony.HwTelephonyManagerInner$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$android$telephony$HwTelephonyManagerInner$DataSettingModeType = new int[DataSettingModeType.values().length];

        static {
            try {
                $SwitchMap$android$telephony$HwTelephonyManagerInner$DataSettingModeType[DataSettingModeType.MODE_LTE_AND_AUTO.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$android$telephony$HwTelephonyManagerInner$DataSettingModeType[DataSettingModeType.MODE_LTETDD_ONLY.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
        }
    }

    private void doSetPreferredNetworkType(int nwMode) {
        RlogEx.i(TAG, "[enter]doSetPreferredNetworkType nwMode:" + nwMode);
        try {
            getIHwTelephony().setPreferredNetworkType(nwMode);
        } catch (RemoteException e) {
        } catch (Exception e2) {
            RlogEx.e(TAG, "doSetPreferredNetworkType failed!");
        }
    }

    private void doSetDataSettingModeFromLteAndAuto(DataSettingModeType dataMode) {
        if (AnonymousClass1.$SwitchMap$android$telephony$HwTelephonyManagerInner$DataSettingModeType[dataMode.ordinal()] != 2) {
            RlogEx.e(TAG, "doSetDataSettingModeFromLteAndAuto failed! param err mode =" + dataMode);
            return;
        }
        doSetPreferredNetworkType(30);
    }

    private void doSetDataSettingModeFromLteTddOnly(DataSettingModeType dataMode) {
        if (AnonymousClass1.$SwitchMap$android$telephony$HwTelephonyManagerInner$DataSettingModeType[dataMode.ordinal()] != 1) {
            RlogEx.e(TAG, "doSetDataSettingModeLteTddOnly failed! param err mode =" + dataMode);
            return;
        }
        doSetPreferredNetworkType(61);
    }

    public boolean isSubDeactived(int slotId) {
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

    public String getCdmaGsmImsiForSubId(int slotId) {
        try {
            return getIHwTelephony().getCdmaGsmImsiForSubId(slotId);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "getCdmaGsmImsiForSubId RemoteException");
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
                    CellLocation cl = CellLocationEx.newFromBundle(bundle, slotId);
                    if (cl == null || CellLocationEx.isEmpty(cl)) {
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
        List<ActivityManager.RunningAppProcessInfo> appProcessList;
        if (context != null) {
            int callingPid = Process.myPid();
            String appName = BuildConfig.FLAVOR;
            ActivityManager am = (ActivityManager) context.getSystemService("activity");
            if (!(am == null || (appProcessList = am.getRunningAppProcesses()) == null)) {
                for (ActivityManager.RunningAppProcessInfo appProcess : appProcessList) {
                    if (appProcess.pid == callingPid) {
                        appName = appProcess.processName;
                    }
                }
                RlogEx.i(TAG, "setDataEnabled: calling app is( " + appName + " ) setEanble( " + enable + " )");
                triggerChrAppCloseDataSwitch(appName, enable, context);
            }
        }
    }

    public boolean isAppInWhiteList(String appName) {
        if ("com.android.phone".equals(appName) || "system".equals(appName) || "com.android.systemui".equals(appName) || "com.android.settings".equals(appName) || "com.huawei.systemmanager".equals(appName) || "com.huawei.vassistant".equals(appName) || "com.huawei.systemmanager:service".equals(appName)) {
            return true;
        }
        return "com.huawei.hiassistantoversea".equals(appName);
    }

    public void triggerChrAppCloseDataSwitch(String appName, boolean enable, Context context) {
        if (appName != null && context != null) {
            if (!isAppInWhiteList(appName)) {
                RlogEx.i(TAG, "app" + appName + " operate data switch! trigger Chr!");
                Intent apkIntent = new Intent(HwTelephonyIntentsInner.INTENT_DS_APP_CLOSE_DATA_SWITCH);
                apkIntent.putExtra("appname", appName);
                context.sendBroadcast(apkIntent, CHR_BROADCAST_PERMISSION);
            }
            TelephonyManagerEx.setDataEnabledProperties(appName, enable);
        }
    }

    public String getUniqueDeviceId(int scope, String callingPackageName) {
        if (scope == 0) {
            try {
                if (mDeviceIdAll == null) {
                    mDeviceIdAll = getIHwTelephony().getUniqueDeviceId(0, callingPackageName);
                }
                return mDeviceIdAll;
            } catch (IllegalArgumentException e) {
                RlogEx.e(TAG, "getUniqueDeviceId IllegalArgumentException");
                return null;
            } catch (RemoteException e2) {
                RlogEx.e(TAG, "getUniqueDeviceId RemoteException");
                return null;
            }
        } else if (scope != 1) {
            return getIHwTelephony().getUniqueDeviceId(scope, callingPackageName);
        } else {
            if (mDeviceIdIMEI == null) {
                mDeviceIdIMEI = getIHwTelephony().getUniqueDeviceId(1, callingPackageName);
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

    public int getSimMode(int slotId) {
        return HwVSimManager.getDefault().getSimMode(slotId);
    }

    public void recoverSimMode() {
    }

    public String getTrafficData() {
        return HwVSimManager.getDefault().getTrafficData();
    }

    public Boolean clearTrafficData() {
        return HwVSimManager.getDefault().clearTrafficData();
    }

    public int getSimStateViaSysinfoEx(int slotId) {
        return HwVSimManager.getDefault().getSimStateViaSysinfoEx(slotId);
    }

    public int getCpserr(int slotId) {
        return 0;
    }

    public int scanVsimAvailableNetworks(int slotId, int type) {
        return HwVSimManager.getDefault().scanVsimAvailableNetworks(slotId, type);
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
        List<ActivityManager.RunningAppProcessInfo> appProcessList;
        if (context == null) {
            return BuildConfig.FLAVOR;
        }
        if (!haveCheckedAppName) {
            int callingPid = Process.myPid();
            ActivityManager am = (ActivityManager) context.getSystemService("activity");
            if (am == null || (appProcessList = am.getRunningAppProcesses()) == null) {
                return BuildConfig.FLAVOR;
            }
            Iterator<ActivityManager.RunningAppProcessInfo> it = appProcessList.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                ActivityManager.RunningAppProcessInfo appProcess = it.next();
                if (appProcess.pid == callingPid) {
                    setCallingAppName(appProcess.processName);
                    RlogEx.i(TAG, "setCallingAppName : " + appProcess.processName);
                    break;
                }
            }
            setHaveCheckedAppName(true);
        }
        return callingAppName;
    }

    public boolean setISMCOEX(String ATCommand) {
        try {
            RlogEx.i(TAG, "setISMCOEX = " + ATCommand);
            return getIHwTelephony().setISMCOEX(ATCommand);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "setISMCOEX RemoteException");
            return false;
        } catch (NullPointerException e2) {
            RlogEx.e(TAG, "setISMCOEX NullPointerException");
            return false;
        }
    }

    public String[] queryServiceCellBand() {
        RlogEx.i(TAG, "queryServiceCellBand.");
        try {
            return getIHwTelephony().queryServiceCellBand();
        } catch (RemoteException e) {
            RlogEx.e(TAG, "queryServiceCellBand RemoteException");
            return new String[0];
        } catch (NullPointerException e2) {
            RlogEx.e(TAG, "queryServiceCellBand NullPointerException");
            return new String[0];
        }
    }

    public boolean registerForRadioAvailable(IPhoneCallback callback) {
        try {
            RlogEx.i(TAG, "registerForRadioAvailable");
            if (getIHwTelephony() != null) {
                return getIHwTelephony().registerForRadioAvailable(callback);
            }
            return false;
        } catch (RemoteException e) {
            RlogEx.e(TAG, "registerForRadioAvailable RemoteException");
            return false;
        }
    }

    public boolean unregisterForRadioAvailable(IPhoneCallback callback) {
        try {
            RlogEx.i(TAG, "unregisterForRadioAvailable");
            return getIHwTelephony().unregisterForRadioAvailable(callback);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "unregisterForRadioAvailable RemoteException");
            return false;
        }
    }

    public boolean registerForRadioNotAvailable(IPhoneCallback callback) {
        try {
            RlogEx.i(TAG, "registerForRadioNotAvailable");
            return getIHwTelephony().registerForRadioNotAvailable(callback);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "registerForRadioNotAvailable RemoteException");
            return false;
        }
    }

    public boolean unregisterForRadioNotAvailable(IPhoneCallback callback) {
        try {
            RlogEx.i(TAG, "unregisterForRadioNotAvailable");
            return getIHwTelephony().unregisterForRadioNotAvailable(callback);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "unregisterForRadioNotAvailable RemoteException");
            return false;
        }
    }

    public boolean registerCommonImsaToMapconInfo(IPhoneCallback callback) {
        try {
            RlogEx.i(TAG, "registerCommonImsaToMapconInfo");
            return getIHwTelephony().registerCommonImsaToMapconInfo(callback);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "registerCommonImsaToMapconInfo RemoteException");
            return false;
        }
    }

    public boolean unregisterCommonImsaToMapconInfo(IPhoneCallback callback) {
        try {
            RlogEx.i(TAG, "unregisterCommonImsaToMapconInfo");
            return getIHwTelephony().unregisterCommonImsaToMapconInfo(callback);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "unregisterCommonImsaToMapconInfo RemoteException");
            return false;
        }
    }

    public boolean isRadioAvailable() {
        try {
            RlogEx.i(TAG, "isRadioAvailable");
            return getIHwTelephony().isRadioAvailable();
        } catch (RemoteException e) {
            RlogEx.e(TAG, "isRadioAvailable RemoteException");
            return false;
        } catch (NullPointerException e2) {
            RlogEx.e(TAG, "isRadioAvailable NullPointerException");
            return false;
        }
    }

    public void setNrSwitch(int phoneId, boolean value) {
        try {
            RlogEx.i(TAG, "setNrSwitch phoneId = " + phoneId + ", value = " + value);
            getIHwTelephony().setNrSwitch(phoneId, value);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "setNrSwitch RemoteException");
        }
    }

    public boolean getImsSwitch() {
        try {
            RlogEx.i(TAG, "getImsSwitch");
            return getIHwTelephony().getImsSwitch();
        } catch (RemoteException e) {
            RlogEx.e(TAG, "getImsSwitch RemoteException");
            return false;
        } catch (NullPointerException e2) {
            RlogEx.e(TAG, "getImsSwitch NullPointerException");
            return false;
        }
    }

    public void setImsSwitch(boolean value) {
        try {
            RlogEx.i(TAG, "setImsSwitch" + value);
            getIHwTelephony().setImsSwitch(value);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "setImsSwitch RemoteException");
        }
    }

    public void setImsDomainConfig(int domainType) {
        try {
            RlogEx.i(TAG, "setImsDomainConfig");
            getIHwTelephony().setImsDomainConfig(domainType);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "setImsDomainConfig RemoteException");
        } catch (NullPointerException e2) {
            RlogEx.e(TAG, "setImsDomainConfig NullPointerException");
        }
    }

    public boolean handleMapconImsaReq(byte[] Msg) {
        try {
            RlogEx.i(TAG, "handleMapconImsaReq");
            return getIHwTelephony().handleMapconImsaReq(Msg);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "handleMapconImsaReq RemoteException");
            return false;
        } catch (NullPointerException e2) {
            RlogEx.e(TAG, "handleMapconImsaReq NullPointerException");
            return false;
        }
    }

    public int getUiccAppType() {
        try {
            RlogEx.i(TAG, "getUiccAppType");
            return getIHwTelephony().getUiccAppType();
        } catch (RemoteException e) {
            RlogEx.e(TAG, "getUiccAppType RemoteException");
            return 0;
        } catch (NullPointerException e2) {
            RlogEx.e(TAG, "getUiccAppType NullPointerException");
            return 0;
        }
    }

    public int getImsDomain() {
        try {
            RlogEx.i(TAG, "getImsDomain");
            return getIHwTelephony().getImsDomain();
        } catch (RemoteException e) {
            RlogEx.e(TAG, "getImsDomain RemoteException");
            return -1;
        } catch (NullPointerException e2) {
            RlogEx.e(TAG, "getImsDomain NullPointerException");
            return -1;
        }
    }

    public UiccAuthResponse handleUiccAuth(int auth_type, byte[] rand, byte[] auth) {
        try {
            RlogEx.i(TAG, "handleUiccAuth");
            return getIHwTelephony().handleUiccAuth(auth_type, rand, auth);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "handleUiccAuth RemoteException");
            return null;
        } catch (NullPointerException e2) {
            RlogEx.e(TAG, "handleUiccAuth NullPointerException");
            return null;
        }
    }

    public boolean registerForPhoneEvent(int phoneId, IPhoneCallback callback, int events) {
        try {
            RlogEx.i(TAG, "registerForPhoneEvent, phoneId = " + phoneId + " events = " + events + " callback = " + callback);
            return getIHwTelephony().registerForPhoneEvent(phoneId, callback, events);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "registerForPhoneEvent RemoteException");
            return false;
        }
    }

    public boolean unregisterForPhoneEvent(IPhoneCallback callback) {
        try {
            RlogEx.i(TAG, "unregisterForPhoneEvent, callback = " + callback);
            getIHwTelephony().unregisterForPhoneEvent(callback);
            return true;
        } catch (RemoteException e) {
            RlogEx.e(TAG, "unregisterForPhoneEvent RemoteException");
            return false;
        }
    }

    public boolean isRadioAvailable(int phoneId) {
        try {
            RlogEx.i(TAG, "isRadioAvailable, phoneId = " + phoneId);
            return getIHwTelephony().isRadioAvailableByPhoneId(phoneId);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "isRadioAvailable RemoteException");
            return false;
        }
    }

    public void setImsSwitch(int phoneId, boolean value) {
        try {
            RlogEx.i(TAG, "setImsSwitch, phoneId = " + phoneId + ", value = " + value);
            getIHwTelephony().setImsSwitchByPhoneId(phoneId, value);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "setImsSwitch RemoteException");
        }
    }

    public boolean getImsSwitch(int phoneId) {
        try {
            RlogEx.i(TAG, "getImsSwitch, phoneId = " + phoneId);
            return getIHwTelephony().getImsSwitchByPhoneId(phoneId);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "getImsSwitch RemoteException");
            return false;
        } catch (NullPointerException e2) {
            RlogEx.e(TAG, "getImsSwitch NullPointerException");
            return false;
        }
    }

    public void setImsDomainConfig(int phoneId, int domainType) {
        try {
            RlogEx.i(TAG, "setImsDomainConfig, phoneId = " + phoneId);
            getIHwTelephony().setImsDomainConfigByPhoneId(phoneId, domainType);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "setImsDomainConfig RemoteException");
        } catch (NullPointerException e2) {
            RlogEx.e(TAG, "setImsDomainConfig NullPointerException");
        }
    }

    public boolean handleMapconImsaReq(int phoneId, byte[] msg) {
        try {
            RlogEx.i(TAG, "handleMapconImsaReq, phoneId = " + phoneId);
            return getIHwTelephony().handleMapconImsaReqByPhoneId(phoneId, msg);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "handleMapconImsaReq RemoteException");
            return false;
        } catch (NullPointerException e2) {
            RlogEx.e(TAG, "handleMapconImsaReq NullPointerException");
            return false;
        }
    }

    public int getUiccAppType(int phoneId) {
        try {
            RlogEx.i(TAG, "getUiccAppType, phoneId = " + phoneId);
            return getIHwTelephony().getUiccAppTypeByPhoneId(phoneId);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "getUiccAppType RemoteException");
            return 0;
        }
    }

    public int getImsDomain(int phoneId) {
        try {
            RlogEx.i(TAG, "getImsDomain, phoneId = " + phoneId);
            return getIHwTelephony().getImsDomainByPhoneId(phoneId);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "getImsDomain RemoteException");
            return -1;
        } catch (NullPointerException e2) {
            RlogEx.e(TAG, "getImsDomain NullPointerException");
            return -1;
        }
    }

    public UiccAuthResponse handleUiccAuth(int phoneId, int authType, byte[] rand, byte[] auth) {
        try {
            RlogEx.i(TAG, "handleUiccAuth, phoneId = " + phoneId);
            return getIHwTelephony().handleUiccAuthByPhoneId(phoneId, authType, rand, auth);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "handleUiccAuth RemoteException");
            return null;
        }
    }

    public boolean cmdForECInfo(int event, int action, byte[] buf) {
        try {
            return getIHwTelephony().cmdForECInfo(event, action, buf);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "cmdForECInfo RemoteException");
            return false;
        } catch (NullPointerException e2) {
            RlogEx.e(TAG, "cmdForECInfo NullPointerException");
            return false;
        }
    }

    public void notifyCModemStatus(int status, PhoneCallback callback) {
        try {
            RlogEx.i(TAG, "notifyCModemStatus");
            getIHwTelephony().notifyCModemStatus(status, callback.mCallbackStub);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "notifyCModemStatus RemoteException");
        } catch (NullPointerException e2) {
            RlogEx.e(TAG, "notifyCModemStatus NullPointerException");
        }
    }

    public boolean notifyDeviceState(String device, String state, String extras) {
        try {
            RlogEx.i(TAG, "notifyDeviceState, device =" + device + ", state = " + state);
            return getIHwTelephony().notifyDeviceState(device, state, extras);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "notifyDeviceState RemoteException");
            return false;
        } catch (NullPointerException e2) {
            RlogEx.e(TAG, "notifyDeviceState NullPointerException");
            return false;
        }
    }

    public boolean isDataConnectivityDisabled(int slotId, String tag) {
        Bundle bundle = this.mDpm.getPolicy((ComponentName) null, tag);
        boolean allow = false;
        if (bundle != null) {
            allow = bundle.getBoolean("value");
        }
        return true == allow && 1 == slotId;
    }

    public void notifyCellularCommParaReady(int paratype, int pathtype, Message response) {
        try {
            RlogEx.i(TAG, "notifyCellularCommParaReady: paratype = " + paratype + ", pathtype = " + pathtype);
            getIHwTelephony().notifyCellularCommParaReady(paratype, pathtype, response);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "notifyCellularCommParaReady RemoteException");
        }
    }

    public boolean isRoamingPushDisabled() {
        Bundle bundle = this.mDpm.getPolicy((ComponentName) null, DISABLE_PUSH);
        Boolean allow = false;
        if (bundle != null) {
            Boolean allow2 = Boolean.valueOf(bundle.getBoolean("value"));
            RlogEx.i(TAG, "isRoamingPushDisabled: " + allow2);
            return allow2.booleanValue();
        }
        RlogEx.i(TAG, "has not set the allow, return default false");
        return allow.booleanValue();
    }

    public int getDefaultMainSlotCarrier() {
        Bundle bundle = this.mDpm.getPolicy((ComponentName) null, DEFAULT_MAIN_SLOT_CARRIER);
        if (bundle != null) {
            String carrier = bundle.getString("value");
            RlogEx.i(TAG, "getDefaultMainSlotCarrier: " + carrier);
            if (TAG_MDM_CARRIER_CMCC.equals(carrier)) {
                return 1;
            }
            if (TAG_MDM_CARRIER_CT.equals(carrier)) {
                return 2;
            }
            return 0;
        }
        RlogEx.i(TAG, "has not set the default main slot carrier, return");
        return 0;
    }

    public boolean setPinLockEnabled(boolean enablePinLock, String password, int slotId) {
        try {
            RlogEx.i(TAG, "setPinLockEnabled, enablePinLock =" + enablePinLock + ", slotId = " + slotId);
            return getIHwTelephony().setPinLockEnabled(enablePinLock, password, slotId);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "notifyDeviceState RemoteException");
            return false;
        }
    }

    public boolean changeSimPinCode(String oldPinCode, String newPinCode, int slotId) {
        try {
            RlogEx.i(TAG, "changeSimPinCode, slotId = " + slotId);
            return getIHwTelephony().changeSimPinCode(oldPinCode, newPinCode, slotId);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "notifyDeviceState RemoteException");
            return false;
        }
    }

    public boolean sendPseudocellCellInfo(int type, int lac, int cid, int radioTech, String plmn, int slotId) {
        try {
            RlogEx.i(TAG, "sendPseudocellCellInfo, type =" + type + ", lac = " + lac + ", cid = " + cid + ", radioTech = " + radioTech + ", plmn = " + plmn + ", slotId = " + slotId);
            return getIHwTelephony().sendPseudocellCellInfo(type, lac, cid, radioTech, plmn, slotId);
        } catch (RemoteException ex) {
            RlogEx.e(TAG, "sendPseudocellCellInfo RemoteException:" + ex.getMessage());
            return false;
        }
    }

    public boolean sendLaaCmd(int cmd, String reserved, Message response) {
        try {
            RlogEx.i(TAG, "sendLaaCmd: cmd = " + cmd + ", reserved = " + reserved);
            return getIHwTelephony().sendLaaCmd(cmd, reserved, response);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "sendLaaCmd RemoteException");
            return false;
        }
    }

    public boolean getLaaDetailedState(String reserved, Message response) {
        try {
            RlogEx.i(TAG, "getLaaDetailedState: reserved = " + reserved);
            return getIHwTelephony().getLaaDetailedState(reserved, response);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "getLaaDetailedState RemoteException");
            return false;
        }
    }

    public void registerForCallAltSrv(int slotId, IPhoneCallback callback) {
        try {
            RlogEx.i(TAG, "registerForCallAltSrv");
            getIHwTelephony().registerForCallAltSrv(slotId, callback);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "registerForCallAltSrv RemoteException");
        }
    }

    public void unregisterForCallAltSrv(int slotId) {
        try {
            RlogEx.i(TAG, "unregisterForCallAltSrv");
            getIHwTelephony().unregisterForCallAltSrv(slotId);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "unregisterForCallAltSrv RemoteException");
        }
    }

    public int invokeOemRilRequestRaw(int phoneId, byte[] oemReq, byte[] oemResp) {
        try {
            RlogEx.i(TAG, "invokeOemRilRequestRaw, phoneId = " + phoneId);
            return getIHwTelephony().invokeOemRilRequestRaw(phoneId, oemReq, oemResp);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "invokeOemRilRequestRaw RemoteException");
            return -1;
        }
    }

    public boolean isCspPlmnEnabled(int slotId) {
        try {
            RlogEx.i(TAG, "isCspPlmnEnabled for subId: " + slotId);
            return getIHwTelephony().isCspPlmnEnabled(slotId);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "isCspPlmnEnabled RemoteException");
            return false;
        }
    }

    public void setCallForwardingOption(int slotId, int commandInterfaceCFAction, int commandInterfaceCFReason, String dialingNumber, int timerSeconds, Message response) {
        try {
            RlogEx.i(TAG, "setCallForwardingOption subId:" + slotId + ", action:" + commandInterfaceCFAction + ", reason:" + commandInterfaceCFReason + ", timerSeconds: " + timerSeconds);
            getIHwTelephony().setCallForwardingOption(slotId, commandInterfaceCFAction, commandInterfaceCFReason, dialingNumber, timerSeconds, response);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "setCallForwardingOption RemoteException");
        }
    }

    public void getCallForwardingOption(int slotId, int commandInterfaceCFReason, Message response) {
        try {
            RlogEx.i(TAG, "getCallForwardingOption subId:" + slotId + ", reason:" + commandInterfaceCFReason);
            getIHwTelephony().getCallForwardingOption(slotId, commandInterfaceCFReason, response);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "getCallForwardingOption RemoteException");
        }
    }

    public boolean setSubscription(int slotId, boolean activate, Message response) {
        try {
            RlogEx.i(TAG, "setSubscription slotId:" + slotId + ", activate: " + activate);
            return getIHwTelephony().setSubscription(slotId, activate, response);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "setSubscription RemoteException");
            return false;
        }
    }

    public String getImsImpu(int slotId) {
        try {
            RlogEx.i(TAG, "getImsImpu");
            return getIHwTelephony().getImsImpu(slotId);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "getImsImpu RemoteException");
            return null;
        }
    }

    public String getLine1NumberFromImpu(int slotId) {
        try {
            RlogEx.i(TAG, "getLine1NumberFromImpu");
            return getIHwTelephony().getLine1NumberFromImpu(slotId);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "getLine1NumberFromImpu RemoteException");
            return null;
        }
    }

    public boolean isSecondaryCardGsmOnly() {
        try {
            return getIHwTelephony().isSecondaryCardGsmOnly();
        } catch (RemoteException e) {
            RlogEx.e(TAG, "isSecondaryCardGsmOnly RemoteException");
            return false;
        }
    }

    public boolean isVSimEnabled() {
        return HwVSimManager.getDefault().isVSimEnabled();
    }

    public boolean bindSimToProfile(int slotId) {
        try {
            RlogEx.i(TAG, "bindSimToProfile slotId = " + slotId);
            return getIHwTelephony().bindSimToProfile(slotId);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "bindSimToProfile RemoteException");
            return false;
        }
    }

    public boolean setLine1Number(int slotId, String alphaTag, String number, Message onComplete) {
        try {
            return getIHwTelephony().setLine1Number(slotId, alphaTag, number, onComplete);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "setLine1Number RemoteException");
            return false;
        }
    }

    public boolean setDeepNoDisturbState(int slotId, int state) {
        try {
            RlogEx.i(TAG, "setDeepNoDisturbState slotId = " + slotId + " state = " + state);
            return getIHwTelephony().setDeepNoDisturbState(slotId, state);
        } catch (RemoteException ex) {
            RlogEx.e(TAG, "setDeepNoDisturbState RemoteException:" + ex);
            return false;
        }
    }

    public boolean setUplinkFreqBandwidthReportState(int slotId, int state, IPhoneCallback callback) {
        try {
            RlogEx.i(TAG, "setUplinkFreqBandwidthReportState slotId = " + slotId + " state = " + state);
            return getIHwTelephony().setUplinkFreqBandwidthReportState(slotId, state, callback);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "setUplinkFreqBandwidthReportState RemoteExceptio");
            return false;
        }
    }

    public void informModemTetherStatusToChangeGRO(int enable, String faceName) {
        try {
            getIHwTelephony().informModemTetherStatusToChangeGRO(enable, faceName);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "sendUSBinformationToRIL RemoteException");
        }
    }

    public boolean sendSimMatchedOperatorInfo(int slotId, String opKey, String opName, int state, String reserveField) {
        try {
            RlogEx.i(TAG, "sendSimMatchedOperatorInfo slotId = " + slotId + ", opKey = " + opKey + ", opName =" + opName + ", state = " + state + ", reserveField = " + reserveField);
            return getIHwTelephony().sendSimMatchedOperatorInfo(slotId, opKey, opName, state, reserveField);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "sendSimMatchedOperatorInfo RemoteException");
            return false;
        }
    }

    public boolean[] getMobilePhysicsLayerStatus(int slotId) {
        try {
            return getIHwTelephony().getMobilePhysicsLayerStatus(slotId);
        } catch (RemoteException ex) {
            RlogEx.e(TAG, "is4RMimoEnabled RemoteException:" + ex);
            return null;
        }
    }

    public boolean getAntiFakeBaseStation(Message response) {
        try {
            RlogEx.i(TAG, "getAntiFakeBaseStation");
            return getIHwTelephony().getAntiFakeBaseStation(response);
        } catch (RemoteException ex) {
            RlogEx.e(TAG, "getAntiFakeBaseStation RemoteException" + ex);
            return false;
        }
    }

    public byte[] getCardTrayInfo() {
        try {
            return getIHwTelephony().getCardTrayInfo();
        } catch (RemoteException e) {
            RlogEx.e(TAG, "getCardTrayInfo RemoteException");
            return new byte[0];
        }
    }

    public boolean registerForAntiFakeBaseStation(IPhoneCallback callback) {
        try {
            RlogEx.i(TAG, "registerForAntiFakeBaseStation");
            return getIHwTelephony().registerForAntiFakeBaseStation(callback);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "registerForAntiFakeBaseStation RemoteException");
            return false;
        }
    }

    public boolean unregisterForAntiFakeBaseStation() {
        try {
            RlogEx.i(TAG, "unregisterForAntiFakeBaseStation");
            return getIHwTelephony().unregisterForAntiFakeBaseStation();
        } catch (RemoteException e) {
            RlogEx.e(TAG, "unregisterForAntiFakeBaseStation RemoteException");
            return false;
        }
    }

    public boolean setCsconEnabled(boolean isEnabled) {
        try {
            RlogEx.i(TAG, "setCsconEnabled isEnabled = " + isEnabled);
            return getIHwTelephony().setCsconEnabled(isEnabled);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "setCsconEnabled RemoteException");
            return false;
        }
    }

    public int[] getCsconEnabled() {
        int[] response = {-1, -1};
        try {
            RlogEx.i(TAG, "getCsconEnabled");
            if (HuaweiTelephonyConfigs.isMTKPlatform()) {
                return getIHwTelephony().getCsconEnabled();
            }
        } catch (RemoteException e) {
            RlogEx.e(TAG, "getCsconEnabled RemoteException");
        }
        return response;
    }

    private int getLevel(int type, int rssi, int ecio, int phoneId) {
        try {
            return getIHwTelephony().getLevel(type, rssi, ecio, phoneId);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "getLteLevel RemoteException");
            return 0;
        }
    }

    public int getLevelHw(CellSignalStrength signalStrength) {
        if (signalStrength instanceof CellSignalStrengthLte) {
            return getLteLevel((CellSignalStrengthLte) signalStrength);
        }
        if (signalStrength instanceof CellSignalStrengthWcdma) {
            return getWcdmaLevel((CellSignalStrengthWcdma) signalStrength);
        }
        if (signalStrength instanceof CellSignalStrengthGsm) {
            return getGsmLevel((CellSignalStrengthGsm) signalStrength);
        }
        if (signalStrength instanceof CellSignalStrengthNr) {
            return getNrLevel((CellSignalStrengthNr) signalStrength);
        }
        RlogEx.i(TAG, "Unsupported CellSignalStrength=" + signalStrength);
        return 0;
    }

    public int getLteLevel(CellSignalStrengthLte signalStrength) {
        if (signalStrength == null) {
            return 0;
        }
        if (CellSignalStrengthLteEx.isCdma(signalStrength)) {
            if (IS_USE_RSRQ) {
                return getLevel(6, signalStrength.getRsrp(), signalStrength.getRsrq(), CellSignalStrengthLteEx.getPhoneId(signalStrength));
            }
            return getLevel(6, signalStrength.getRsrp(), signalStrength.getRssnr(), CellSignalStrengthLteEx.getPhoneId(signalStrength));
        } else if (IS_USE_RSRQ) {
            return getLevel(5, signalStrength.getRsrp(), signalStrength.getRsrq(), CellSignalStrengthLteEx.getPhoneId(signalStrength));
        } else {
            return getLevel(5, signalStrength.getRsrp(), signalStrength.getRssnr(), CellSignalStrengthLteEx.getPhoneId(signalStrength));
        }
    }

    public int getNrLevel(CellSignalStrengthNr strength) {
        if (strength == null) {
            return 0;
        }
        CellSignalStrengthNrEx cellSignalStrengthNrEx = new CellSignalStrengthNrEx();
        cellSignalStrengthNrEx.setCellSignalStrengthNr(strength);
        int phoneId = cellSignalStrengthNrEx.getPhoneId();
        if (IS_USE_RSRQ) {
            return getLevel(7, strength.getSsRsrp(), strength.getSsRsrq(), phoneId);
        }
        return getLevel(7, strength.getSsRsrp(), strength.getSsSinr(), phoneId);
    }

    public int getGsmLevel(CellSignalStrengthGsm signalStrength) {
        if (signalStrength == null) {
            return 0;
        }
        return getLevel(1, CellSignalStrengthGsmEx.getRssi(signalStrength), 255, CellSignalStrengthGsmEx.getPhoneId(signalStrength));
    }

    public int getWcdmaLevel(CellSignalStrengthWcdma signalStrength) {
        if (signalStrength == null) {
            return 0;
        }
        return getLevel(2, CellSignalStrengthWcdmaEx.getRscp(signalStrength), CellSignalStrengthWcdmaEx.getEcio(signalStrength), CellSignalStrengthWcdmaEx.getPhoneId(signalStrength));
    }

    public int getCdmaLevel(CellSignalStrengthCdma signalStrength) {
        if (signalStrength == null) {
            return 0;
        }
        return getLevel(3, signalStrength.getCdmaDbm(), signalStrength.getCdmaEcio(), CellSignalStrengthCdmaEx.getPhoneId(signalStrength));
    }

    public int getEvdoLevel(CellSignalStrengthCdma signalStrength) {
        if (signalStrength == null) {
            return 0;
        }
        return getLevel(4, signalStrength.getEvdoDbm(), signalStrength.getEvdoSnr(), CellSignalStrengthCdmaEx.getPhoneId(signalStrength));
    }

    public String getSubscriberId(TelephonyManager tm, int slotId) {
        int[] subIds = SubscriptionManagerEx.getSubId(slotId);
        if (tm == null || subIds == null || subIds.length <= 0) {
            return null;
        }
        return TelephonyManagerEx.getSubscriberId(tm, subIds[0]);
    }

    public String getSimCountryIso(TelephonyManager telephonyManager, int slotId) {
        if (telephonyManager != null) {
            return TelephonyManagerEx.getSimCountryIsoForPhone(telephonyManager, slotId);
        }
        return BuildConfig.FLAVOR;
    }

    public String getIccAuthentication(TelephonyManager telephonyManager, int slotId, int appType, int authType, String data) {
        int[] subIds = SubscriptionManagerEx.getSubId(slotId);
        if (telephonyManager == null || subIds == null || subIds.length <= 0) {
            return null;
        }
        return TelephonyManagerEx.getIccAuthentication(telephonyManager, subIds[0], appType, authType, data);
    }

    public ServiceState getServiceStateForSubscriber(int slotId) {
        int[] subIds = SubscriptionManagerEx.getSubId(slotId);
        if (subIds == null || subIds.length <= 0) {
            return null;
        }
        return TelephonyManagerEx.getServiceStateForSubscriber(subIds[0]);
    }

    public String getSimOperator(int slotId) {
        return TelephonyManagerEx.getSimOperatorNumericForPhone(slotId);
    }

    public int getDataNetworkType(TelephonyManager telephonyManager, int slotId) {
        int[] subIds = SubscriptionManagerEx.getSubId(slotId);
        if (telephonyManager == null || subIds == null || subIds.length <= 0) {
            return 0;
        }
        return TelephonyManagerEx.getDataNetworkType(telephonyManager, subIds[0]);
    }

    public String getMsisdn(TelephonyManager telephonyManager, int slotId) {
        int[] subIds = SubscriptionManagerEx.getSubId(slotId);
        if (telephonyManager == null || subIds == null || subIds.length <= 0) {
            return null;
        }
        return TelephonyManagerEx.getMsisdn(telephonyManager, subIds[0]);
    }

    public int getNetworkType(int slotId) {
        int[] subIds = SubscriptionManagerEx.getSubId(slotId);
        if (subIds == null || subIds.length <= 0) {
            return 0;
        }
        return TelephonyManagerEx.getNetworkType(subIds[0]);
    }

    public String getSimSerialNumber(TelephonyManager telephonyManager, int slotId) {
        int[] subIds = SubscriptionManagerEx.getSubId(slotId);
        if (telephonyManager == null || subIds == null || subIds.length <= 0) {
            return null;
        }
        return TelephonyManagerEx.getSimSerialNumber(telephonyManager, subIds[0]);
    }

    public String getSimOperatorName(int slotId) {
        return TelephonyManagerEx.getSimOperatorNameForPhone(slotId);
    }

    public void setNetworkSelectionModeAutomatic(int slotId) {
        int[] subIds = SubscriptionManagerEx.getSubId(slotId);
        if (subIds != null && subIds.length > 0) {
            TelephonyManagerEx.setNetworkSelectionModeAutomatic(subIds[0]);
        }
    }

    public int[] supplyPinReportResultForSubscriber(int slotId, String pin) {
        int[] subIds = SubscriptionManagerEx.getSubId(slotId);
        if (subIds == null || subIds.length <= 0) {
            return new int[0];
        }
        return TelephonyManagerEx.supplyPinReportResultForSubscriber(subIds[0], pin);
    }

    public int[] supplyPukReportResultForSubscriber(int slotId, String puk, String pin) {
        int[] subIds = SubscriptionManagerEx.getSubId(slotId);
        if (subIds == null || subIds.length <= 0) {
            return new int[0];
        }
        return TelephonyManagerEx.supplyPukReportResultForSubscriber(subIds[0], puk, pin);
    }

    public int getPreferredNetworkType(int slotId) {
        int[] subIds = SubscriptionManagerEx.getSubId(slotId);
        if (subIds == null || subIds.length <= 0) {
            return -1;
        }
        return TelephonyManagerEx.getPreferredNetworkType(subIds[0]);
    }

    public boolean setPreferredNetworkType(int slotId, int networkType) {
        int[] subIds = SubscriptionManagerEx.getSubId(slotId);
        if (subIds == null || subIds.length <= 0) {
            return false;
        }
        return TelephonyManagerEx.setPreferredNetworkType(subIds[0], networkType);
    }

    public int getDataRegisteredState(int phoneId) {
        try {
            return getIHwTelephony().getDataRegisteredState(phoneId);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "getDataRegisteredState RemoteException");
            return 1;
        }
    }

    public int getVoiceRegisteredState(int phoneId) {
        try {
            return getIHwTelephony().getVoiceRegisteredState(phoneId);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "getVoiceRegisteredState RemoteException");
            return 1;
        }
    }

    public String getNetworkOperatorName(int slotId) {
        int[] subIds = SubscriptionManagerEx.getSubId(slotId);
        if (subIds == null || subIds.length <= 0) {
            return BuildConfig.FLAVOR;
        }
        return TelephonyManagerEx.getNetworkOperatorName(subIds[0]);
    }

    public boolean isNetworkRoaming(int slotId) {
        int[] subIds = SubscriptionManagerEx.getSubId(slotId);
        if (subIds == null || subIds.length <= 0) {
            return false;
        }
        return TelephonyManagerEx.isNetworkRoaming(subIds[0]);
    }

    public int getLteOnCdmaMode(int slotId) {
        int[] subIds = SubscriptionManagerEx.getSubId(slotId);
        if (subIds == null || subIds.length <= 0) {
            return -1;
        }
        return TelephonyManagerEx.getLteOnCdmaMode(subIds[0]);
    }

    public String getLine1Number(Context context, int slotId) {
        int[] subIds = SubscriptionManagerEx.getSubId(slotId);
        if (subIds == null || subIds.length <= 0) {
            return null;
        }
        int subscription = subIds[0];
        if (context == null) {
            return TelephonyManagerEx.getLine1Number(subscription);
        }
        return TelephonyManagerEx.getLine1Number(context, subscription);
    }

    public String getLine1AlphaTag(Context context, int slotId) {
        int[] subIds = SubscriptionManagerEx.getSubId(slotId);
        if (subIds == null || subIds.length <= 0) {
            return null;
        }
        int subscription = subIds[0];
        if (context == null) {
            return TelephonyManagerEx.getLine1AlphaTag(subscription);
        }
        return TelephonyManagerEx.getLine1AlphaTag(context, subscription);
    }

    public String getVoiceMailNumber(Context context, int slotId) {
        int[] subIds = SubscriptionManagerEx.getSubId(slotId);
        if (subIds == null || subIds.length <= 0) {
            return null;
        }
        int subscription = subIds[0];
        if (context == null) {
            return TelephonyManagerEx.getVoiceMailNumber(subscription);
        }
        return TelephonyManagerEx.getVoiceMailNumber(context, subscription);
    }

    public int getVoiceMessageCount(int slotId) {
        int[] subIds = SubscriptionManagerEx.getSubId(slotId);
        if (subIds == null || subIds.length <= 0) {
            return 0;
        }
        return TelephonyManagerEx.getVoiceMessageCount(subIds[0]);
    }

    public String getVoiceMailAlphaTag(Context context, int slotId) {
        int[] subIds = SubscriptionManagerEx.getSubId(slotId);
        if (subIds == null || subIds.length <= 0) {
            return null;
        }
        int subscription = subIds[0];
        if (context == null) {
            return TelephonyManagerEx.getVoiceMailAlphaTag(subscription);
        }
        return TelephonyManagerEx.getVoiceMailAlphaTag(context, subscription);
    }

    public int getCdmaEriIconIndex(Context context, int slotId) {
        int[] subIds = SubscriptionManagerEx.getSubId(slotId);
        if (subIds == null || subIds.length <= 0) {
            return -1;
        }
        int subscription = subIds[0];
        if (context == null) {
            return TelephonyManagerEx.getCdmaEriIconIndex(subscription);
        }
        return TelephonyManagerEx.getCdmaEriIconIndex(context, subscription);
    }

    public int getCdmaEriIconMode(Context context, int slotId) {
        int[] subIds = SubscriptionManagerEx.getSubId(slotId);
        if (subIds == null || subIds.length <= 0) {
            return -1;
        }
        int subscription = subIds[0];
        if (context == null) {
            return TelephonyManagerEx.getCdmaEriIconMode(subscription);
        }
        return TelephonyManagerEx.getCdmaEriIconMode(context, subscription);
    }

    public String getCdmaEriText(Context context, int slotId) {
        int[] subIds = SubscriptionManagerEx.getSubId(slotId);
        if (subIds == null || subIds.length <= 0) {
            return null;
        }
        int subscription = subIds[0];
        if (context == null) {
            return TelephonyManagerEx.getCdmaEriText(subscription);
        }
        return TelephonyManagerEx.getCdmaEriText(context, subscription);
    }

    public boolean setTemperatureControlToModem(int level, int type, int subId, Message response) {
        try {
            return getIHwTelephony().setTemperatureControlToModem(level, type, subId, response);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "setTemperatureControlToModem RemoteException");
            return false;
        }
    }

    public String blockingGetIccATR(int index) {
        try {
            return getIHwTelephony().blockingGetIccATR(index);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "getIccATR RemoteException");
            return null;
        }
    }

    public boolean isNrSupported() {
        return HwModemCapability.isCapabilitySupport(29);
    }

    public boolean isDualNrSupported() {
        return getModemMaxCapability(1) >= 4;
    }

    public int getModemMaxCapability(int modemId) {
        if (modemId == 0) {
            return HwModemCapability.isCapabilitySupport(29) ? 4 : 3;
        }
        if (modemId != 1 || !TelephonyManagerEx.isMultiSimEnabled()) {
            return -1;
        }
        if (HwModemCapability.isCapabilitySupport((int) MODEM_CAP_SUPPORT_DUAL_NR)) {
            return 4;
        }
        if (HwModemCapability.isCapabilitySupport(21)) {
            return 3;
        }
        return 2;
    }

    public boolean isEuicc(int slotId) {
        try {
            return getIHwTelephony().isEuicc(slotId);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "isEuicc RemoteException");
            return false;
        }
    }

    public boolean setNrOptionMode(int mode, Message msg) {
        try {
            return getIHwTelephony().setNrOptionMode(mode, msg);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "setNrOptionMode RemoteException");
            return false;
        }
    }

    public boolean setNrOptionModeForSlotId(int slotId, int mode, Message msg) {
        IHwTelephonyInner service = getIHwTelephonyInner();
        if (service == null) {
            RlogEx.e(TAG, "service is null");
            return false;
        }
        try {
            return service.setNrOptionModeForSlotId(slotId, mode, msg);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "setNrOptionModeForSlotId RemoteException");
            return false;
        }
    }

    public int getNrOptionMode() {
        try {
            return getIHwTelephony().getNrOptionMode();
        } catch (RemoteException e) {
            RlogEx.e(TAG, "getNrOptionMode RemoteException");
            return 0;
        }
    }

    public int getNrOptionModeForSlotId(int slotId) {
        IHwTelephonyInner service = getIHwTelephonyInner();
        if (service == null) {
            RlogEx.e(TAG, "service is null");
            return 0;
        }
        try {
            return service.getNrOptionModeForSlotId(slotId);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "getNrOptionModeForSlotId RemoteException");
            return 0;
        }
    }

    public boolean isNsaState(int phoneId) {
        try {
            return getIHwTelephony().isNsaState(phoneId);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "isNsaState RemoteException");
            return false;
        }
    }

    public NrCellSsbId getNrCellSsbId(int slotId) {
        try {
            return getIHwTelephony().getNrCellSsbId(slotId);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "getNrCellSsbId RemoteException");
            return null;
        }
    }

    public boolean isAISCard(int slotId) {
        try {
            return getIHwTelephony().isAISCard(slotId);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "isAISCard RemoteException");
            return true;
        }
    }

    public boolean isAisCustomDisable() {
        return INVALID_MCCMNC.equals(SystemPropertiesEx.get(HW_CUST_SW_SIMLOCK, BuildConfig.FLAVOR));
    }

    public boolean isCustomAis() {
        try {
            return getIHwTelephony().isCustomAis();
        } catch (RemoteException e) {
            RlogEx.e(TAG, "isCustomAis RemoteException");
            return false;
        }
    }

    public boolean isBlockNonAisSlot(int slotId) {
        return isCustomAis() && !isAISCard(slotId) && !isAisCustomDisable();
    }

    public boolean isSmartCard(int slotId) {
        IHwTelephonyInner service = getIHwTelephonyInner();
        if (service == null) {
            RlogEx.e(TAG, "isSmartCard return invalid");
            return false;
        }
        try {
            return service.isSmartCard(slotId);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "isSmartCard RemoteException");
            return false;
        }
    }

    public boolean isSmartCustomDisable() {
        return INVALID_MCCMNC.equals(SystemPropertiesEx.get(HW_CUST_SW_SIMLOCK, BuildConfig.FLAVOR));
    }

    public boolean isCustomSmart() {
        IHwTelephonyInner service = getIHwTelephonyInner();
        if (service == null) {
            RlogEx.e(TAG, "isCustomSmart return invalid");
            return false;
        }
        try {
            return service.isCustomSmart();
        } catch (RemoteException e) {
            RlogEx.e(TAG, "isCustomSmart RemoteException");
            return false;
        }
    }

    public boolean isBlockNonSmartSlot(int slotId) {
        return isCustomSmart() && !isSmartCard(slotId) && !isSmartCustomDisable();
    }

    public boolean isBlockNonCustomSlot(int slotId, int customBlockType) {
        IHwTelephonyInner service = getIHwTelephonyInner();
        if (service == null) {
            RlogEx.e(TAG, "isBlockNonCustomSlot return invalid");
            return false;
        }
        try {
            return service.isBlockNonCustomSlot(slotId, customBlockType);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "isCustom RemoteException");
            return false;
        }
    }

    public boolean isNrSlicesSupported() {
        return isNrSupported() && HwModemCapability.isCapabilitySupport(30);
    }

    public String getCTOperator(int slotId, String operator) {
        try {
            return getIHwTelephony().getCTOperator(slotId, operator);
        } catch (RemoteException e) {
            return operator;
        }
    }

    public int getLevelForSa(SignalStrength signalStrength) {
        IHwTelephonyInner service = getIHwTelephonyInner();
        if (service == null) {
            return 0;
        }
        try {
            return service.getLevelForSa(SignalStrengthEx.getPhoneId(signalStrength), SignalStrengthEx.getNrLevel(signalStrength), SignalStrengthEx.getPrimaryLevelHw(signalStrength));
        } catch (RemoteException e) {
            RlogEx.e(TAG, "getLevelForSa RemoteException");
            return 0;
        }
    }

    public int getRrcConnectionState(int slotId) {
        IHwTelephonyInner service = getIHwTelephonyInner();
        if (service == null) {
            return -1;
        }
        try {
            return service.getRrcConnectionState(slotId);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "getRrcConnectionState RemoteException");
            return -1;
        }
    }

    public int getPlatformSupportVsimVer(int what) {
        IHwTelephonyInner service = getIHwTelephonyInner();
        if (service == null) {
            RlogEx.e(TAG, "getPlatformSupportVsimVer return invalid");
            return -1;
        }
        try {
            return service.getPlatformSupportVsimVer(what);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "getPlatformSupportVsimVer RemoteException");
            return -1;
        }
    }

    public String getRegPlmn(int slotId) {
        IHwTelephonyInner service = getIHwTelephonyInner();
        if (service == null) {
            RlogEx.e(TAG, "getRegPlmn return invalid");
            return null;
        }
        try {
            return service.getRegPlmn(slotId);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "getRegPlmn RemoteException");
            return null;
        }
    }

    public int getUserReservedSubId() {
        IHwTelephonyInner service = getIHwTelephonyInner();
        if (service == null) {
            RlogEx.e(TAG, "getUserReservedSubId return invalid");
            return -1;
        }
        try {
            return service.getVsimUserReservedSubId();
        } catch (RemoteException e) {
            RlogEx.e(TAG, "getUserReservedSubId RemoteException");
            return -1;
        }
    }

    public boolean setUserReservedSubId(int slotId) {
        IHwTelephonyInner service = getIHwTelephonyInner();
        if (service == null) {
            RlogEx.e(TAG, "setUserReservedSubId return invalid");
            return false;
        }
        try {
            return service.setVsimUserReservedSubId(slotId);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "setUserReservedSubId RemoteException");
            return false;
        }
    }

    public void blockingGetVsimService(IGetVsimServiceCallback callback) {
        IHwTelephonyInner service = getIHwTelephonyInner();
        if (service == null) {
            RlogEx.e(TAG, "blockingGetVsimService return invalid");
            return;
        }
        try {
            service.blockingGetVsimService(callback);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "blockingGetVsimService RemoteException");
        }
    }

    public boolean isVsimEnabledByDatabase() {
        IHwTelephonyInner service = getIHwTelephonyInner();
        if (service == null) {
            RlogEx.e(TAG, "isVsimEnabledByDatabase return invalid");
            return false;
        }
        try {
            return service.isVsimEnabledByDatabase();
        } catch (RemoteException e) {
            RlogEx.e(TAG, "isVsimEnabledByDatabase RemoteException");
            return false;
        }
    }

    public void setSimPowerStateForSlot(int slotIndex, int state, Message msg) {
        IHwTelephonyInner service = getIHwTelephonyInner();
        if (service == null) {
            RlogEx.e(TAG, "setSimPowerStateForSlot return invalid");
            return;
        }
        try {
            service.setSimPowerStateForSlot(slotIndex, state, msg);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "Error calling ITelephony#setSimPowerStateForSlot RemoteException");
        }
    }

    public boolean registerForRadioStateChanged(HwCommonPhoneCallback callback) {
        IHwTelephonyInner service = getIHwTelephonyInner();
        if (service == null || callback == null) {
            RlogEx.e(TAG, "registerForRadioStateChanged return invalid");
            return false;
        }
        try {
            Object obj = callback.getHwCommonPhoneCallback();
            if (obj instanceof IHwCommonPhoneCallback) {
                return service.registerForRadioStateChanged((IHwCommonPhoneCallback) obj);
            }
        } catch (RemoteException e) {
            RlogEx.e(TAG, "Error calling ITelephony#registerForRadioStateChanged RemoteException");
        }
        return false;
    }

    public boolean unregisterForRadioStateChanged(HwCommonPhoneCallback callback) {
        IHwTelephonyInner service = getIHwTelephonyInner();
        if (service == null || callback == null) {
            RlogEx.e(TAG, "unregisterForRadioStateChanged return invalid");
            return false;
        }
        try {
            Object obj = callback.getHwCommonPhoneCallback();
            if (obj instanceof IHwCommonPhoneCallback) {
                return service.unregisterForRadioStateChanged((IHwCommonPhoneCallback) obj);
            }
        } catch (RemoteException e) {
            RlogEx.e(TAG, "Error calling ITelephony#unregisterForRadioStateChanged RemoteException");
        }
        return false;
    }

    public int[] getSimFactoryCapability() {
        String capability = SystemPropertiesEx.get("ril.hw_mc.test_sims", "1,1");
        int[] result = {1, 1};
        if (TextUtils.isEmpty(capability)) {
            return result;
        }
        String[] cap = capability.split(",");
        if (cap.length != 2) {
            return result;
        }
        for (int i = 0; i < cap.length; i++) {
            try {
                result[i] = Integer.parseInt(cap[i]);
            } catch (NumberFormatException e) {
                RlogEx.e(TAG, "getSimFactoryCapability NumberFormatException check prop");
            }
        }
        return result;
    }

    public int getNetworkMode(int phoneId) {
        IHwTelephonyInner service = getIHwTelephonyInner();
        if (service == null) {
            RlogEx.e(TAG, "service is null");
            return 0;
        }
        try {
            return service.getNetworkMode(phoneId);
        } catch (RemoteException e) {
            RlogEx.e(TAG, "getNetworkMode RemoteException");
            return 0;
        }
    }
}
