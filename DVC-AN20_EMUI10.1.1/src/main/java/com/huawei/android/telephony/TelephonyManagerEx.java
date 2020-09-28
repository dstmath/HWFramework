package com.huawei.android.telephony;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Message;
import android.os.ServiceManager;
import android.provider.Settings;
import android.telecom.PhoneAccount;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.IHwTelephony;
import com.android.internal.telephony.IPhoneCallback;
import com.android.internal.telephony.Phone;
import com.huawei.android.util.NoExtAPIException;
import com.huawei.annotation.HwSystemApi;
import java.util.List;

public class TelephonyManagerEx {
    @HwSystemApi
    public static final String ACTION_PHONE_STATE_CHANGED = "android.intent.action.PHONE_STATE";
    public static final int ANTENNA_BOTH = 2;
    public static final int ANTENNA_DOWN = 0;
    public static final int ANTENNA_UP = 1;
    public static final int ANT_SWITCH_ASDIV_CONFIG_LOWER = 0;
    public static final int ANT_SWITCH_ASDIV_CONFIG_UPPER = 1;
    public static final int BIGPOWER_NO = 0;
    public static final int BIGPOWER_YES = 1;
    @HwSystemApi
    public static final int CARRIER_PRIVILEGE_STATUS_HAS_ACCESS = 1;
    @HwSystemApi
    public static final int CARRIER_PRIVILEGE_STATUS_RULES_NOT_LOADED = -1;
    public static final String KEY1 = "key1";
    public static final int LTE_SWITCH_OFF = 0;
    public static final int LTE_SWITCH_ON = 1;
    @HwSystemApi
    public static final int NETWORK_CLASS_2_G = 1;
    @HwSystemApi
    public static final int NETWORK_CLASS_3_G = 2;
    @HwSystemApi
    public static final int NETWORK_CLASS_4_G = 3;
    @HwSystemApi
    public static final int NETWORK_CLASS_5_G = 4;
    @HwSystemApi
    public static final int NETWORK_CLASS_UNKNOWN = 0;
    public static final int NETWORK_TYPE_DCHSPAP = 30;
    public static final int NETWORK_TYPE_LTE_CA = 19;
    public static final int NETWORK_TYPE_NR = 20;
    @HwSystemApi
    public static final int RADIO_POWER_ON = 1;
    @HwSystemApi
    public static final int RADIO_POWER_UNAVAILABLE = 2;
    @HwSystemApi
    public static final int SCOPE_ALL = 0;
    @HwSystemApi
    public static final int SCOPE_IMEI = 1;
    public static final int SIM_STATE_ABSENT = 1;
    public static final int SIM_STATE_CARD_IO_ERROR = 6;
    public static final int SIM_STATE_DEACTIVED = 8;
    public static final int SIM_STATE_LOADED = 10;
    public static final int SIM_STATE_NETWORK_LOCKED = 4;
    public static final int SIM_STATE_NOT_READY = 7;
    public static final int SIM_STATE_PIN_REQUIRED = 2;
    public static final int SIM_STATE_PUK_REQUIRED = 3;
    public static final int SIM_STATE_READY = 5;
    public static final int SIM_STATE_UNKNOWN = 0;
    public static final int TYPE_ANTENNA = 2;
    public static final int TYPE_BAND = 1;
    public static final int TYPE_BIGPOWER = 4;
    public static final int UNKNOWN = -1;

    @HwSystemApi
    public enum MultiSimVariantsExt {
        DSDS,
        DSDA,
        TSTS,
        UNKNOWN
    }

    @HwSystemApi
    public static boolean getDataEnabled(TelephonyManager telephonyManager) {
        if (telephonyManager == null) {
            return false;
        }
        return telephonyManager.getDataEnabled();
    }

    @HwSystemApi
    public static int checkCarrierPrivilegesForPackageAnyPhone(String pkgName) {
        return TelephonyManager.getDefault().checkCarrierPrivilegesForPackageAnyPhone(pkgName);
    }

    @HwSystemApi
    public static List<String> getPackagesWithCarrierPrivileges() {
        return TelephonyManager.getDefault().getPackagesWithCarrierPrivileges();
    }

    @HwSystemApi
    public static boolean switchSlots(int[] physicalSlots) {
        return TelephonyManager.getDefault().switchSlots(physicalSlots);
    }

    @HwSystemApi
    public static void setDataEnabledProperties(String appName, boolean enable) {
        TelephonyManager.getDefault().setDataEnabledProperties(appName, enable);
    }

    @HwSystemApi
    public static String getSimOperatorNameForPhone(int slotId) {
        return TelephonyManager.getDefault().getSimOperatorNameForPhone(slotId);
    }

    @HwSystemApi
    public static TelephonyManager getDefault() {
        return TelephonyManager.getDefault();
    }

    public static void setDataEnabled(TelephonyManager tm, boolean enable) {
        if (tm != null) {
            tm.setDataEnabled(enable);
        }
    }

    @HwSystemApi
    public static void setDataEnabled(TelephonyManager tm, int subId, boolean enable) {
        if (tm != null) {
            tm.setDataEnabled(subId, enable);
        }
    }

    public static boolean isMultiSimEnabled(TelephonyManager object) {
        if (object == null) {
            return false;
        }
        return object.isMultiSimEnabled();
    }

    public static int getPhoneType(TelephonyManager object) {
        if (object == null) {
            return 0;
        }
        return object.getPhoneType();
    }

    public static String getPesn(TelephonyManager object) {
        throw new NoExtAPIException("method not supported.");
    }

    public static boolean isVirtualNet() {
        throw new NoExtAPIException("method not supported.");
    }

    public static String getApnFilter() {
        throw new NoExtAPIException("method not supported.");
    }

    public static boolean isCTCdmaCardInGsmMode() {
        return HwTelephonyManagerInner.getDefault().isCTCdmaCardInGsmMode();
    }

    public static String getIccCardType() {
        return null;
    }

    public static boolean isMTKPlatform() {
        return false;
    }

    public static int getDefault4GSlotId() {
        return HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
    }

    public static void setDefault4GSlotId(int slotId, Message msg) {
        HwTelephonyManagerInner.getDefault().setDefault4GSlotId(slotId, msg);
    }

    public static boolean isSetDefault4GSlotIdEnabled() {
        return HwTelephonyManagerInner.getDefault().isSetDefault4GSlotIdEnabled();
    }

    public static void waitingSetDefault4GSlotDone(boolean waiting) {
        HwTelephonyManagerInner.getDefault().waitingSetDefault4GSlotDone(waiting);
    }

    public static String getCdmaMlplVersion() {
        return HwTelephonyManagerInner.getDefault().getCdmaMlplVersion();
    }

    public static String getCdmaMsplVersion() {
        return HwTelephonyManagerInner.getDefault().getCdmaMsplVersion();
    }

    public static boolean checkCdmaSlaveCardMode(int mode) {
        return HwTelephonyManagerInner.getDefault().checkCdmaSlaveCardMode(mode);
    }

    public static boolean isCTSimCard(int slotId) {
        return HwTelephonyManagerInner.getDefault().isCTSimCard(slotId);
    }

    public static boolean isSubDeactivedByPowerOff(Phone phone) {
        if (phone == null) {
            return false;
        }
        return HwTelephonyManagerInner.getDefault().isSubDeactivedByPowerOff((long) phone.getSubId());
    }

    public static boolean isNeedToRadioPowerOn(Phone phone) {
        if (phone == null) {
            return true;
        }
        return HwTelephonyManagerInner.getDefault().isNeedToRadioPowerOn((long) phone.getSubId());
    }

    public static void setLteServiceAbility(int ability) {
        HwTelephonyManagerInner.getDefault().setLteServiceAbility(ability);
    }

    public static int getLteServiceAbility() {
        return HwTelephonyManagerInner.getDefault().getLteServiceAbility();
    }

    @HwSystemApi
    public static boolean registerForWirelessState(int type, int slotId, IPhoneCallback callback) {
        try {
            return IHwTelephony.Stub.asInterface(ServiceManager.getService("phone_huawei")).registerForWirelessState(type, slotId, callback);
        } catch (Exception e) {
            return false;
        }
    }

    @HwSystemApi
    public static boolean unregisterForWirelessState(int type, int slotId, IPhoneCallback callback) {
        try {
            IHwTelephony.Stub.asInterface(ServiceManager.getService("phone_huawei")).unregisterForWirelessState(type, slotId, callback);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean setMaxTxPower(int type, int power) {
        try {
            IHwTelephony.Stub.asInterface(ServiceManager.getService("phone_huawei")).setMaxTxPower(type, power);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static TelephonyManager from(Context context) {
        return TelephonyManager.from(context);
    }

    public static String getSubscriberId(TelephonyManager tm, int subId) {
        if (tm == null) {
            return null;
        }
        return tm.getSubscriberId(subId);
    }

    @HwSystemApi
    public static String getSimCountryIsoForPhone(TelephonyManager tm, int subId) {
        if (tm == null) {
            return null;
        }
        return tm.getSimCountryIsoForPhone(subId);
    }

    @HwSystemApi
    public static String getIccAuthentication(TelephonyManager tm, int subId, int appType, int authType, String data) {
        if (tm == null) {
            return null;
        }
        return tm.getIccAuthentication(subId, appType, authType, data);
    }

    @HwSystemApi
    public static ServiceState getServiceStateForSubscriber(int subId) {
        return TelephonyManager.getDefault().getServiceStateForSubscriber(subId);
    }

    @HwSystemApi
    public static String getSimOperatorNumericForPhone(int phoneId) {
        return TelephonyManager.getDefault().getSimOperatorNumericForPhone(phoneId);
    }

    @HwSystemApi
    public static int getDataNetworkType(TelephonyManager tm, int subId) {
        if (tm == null) {
            return 0;
        }
        return tm.getDataNetworkType(subId);
    }

    @HwSystemApi
    public static String getMsisdn(TelephonyManager tm, int subId) {
        if (tm == null) {
            return null;
        }
        return tm.getMsisdn(subId);
    }

    @HwSystemApi
    public static int getNetworkType(int subId) {
        return TelephonyManager.getDefault().getNetworkType(subId);
    }

    @HwSystemApi
    public static String getSimSerialNumber(TelephonyManager tm, int subId) {
        if (tm == null) {
            return null;
        }
        return tm.getSimSerialNumber(subId);
    }

    @HwSystemApi
    public static void setNetworkSelectionModeAutomatic(int subId) {
        TelephonyManager.getDefault().setNetworkSelectionModeAutomatic(subId);
    }

    @HwSystemApi
    public static int[] supplyPinReportResultForSubscriber(int subId, String pin) {
        return TelephonyManager.getDefault().supplyPinReportResultForSubscriber(subId, pin);
    }

    @HwSystemApi
    public static int[] supplyPukReportResultForSubscriber(int subId, String puk, String pin) {
        return TelephonyManager.getDefault().supplyPukReportResultForSubscriber(subId, puk, pin);
    }

    @HwSystemApi
    public static int getPreferredNetworkType(int subId) {
        return TelephonyManager.getDefault().getPreferredNetworkType(subId);
    }

    @HwSystemApi
    public static boolean setPreferredNetworkType(int subId, int networkType) {
        return TelephonyManager.getDefault().setPreferredNetworkType(subId, networkType);
    }

    @HwSystemApi
    public static String getNetworkOperatorName(int subId) {
        return TelephonyManager.getDefault().getNetworkOperatorName(subId);
    }

    @HwSystemApi
    public static boolean isNetworkRoaming(int subId) {
        return TelephonyManager.getDefault().isNetworkRoaming(subId);
    }

    @HwSystemApi
    public static boolean isNetworkRoaming(TelephonyManager tm, int subId) {
        if (tm == null) {
            return false;
        }
        return tm.isNetworkRoaming(subId);
    }

    @HwSystemApi
    public static int getLteOnCdmaMode(int slotId) {
        return TelephonyManager.getDefault().getLteOnCdmaMode(slotId);
    }

    @HwSystemApi
    public static String getLine1Number(int subId) {
        return TelephonyManager.getDefault().getLine1Number(subId);
    }

    @HwSystemApi
    public static String getLine1Number(Context context, int subId) {
        return TelephonyManager.from(context).getLine1Number(subId);
    }

    @HwSystemApi
    public static String getLine1AlphaTag(int subId) {
        return TelephonyManager.getDefault().getLine1AlphaTag(subId);
    }

    @HwSystemApi
    public static String getLine1AlphaTag(Context context, int subId) {
        return TelephonyManager.from(context).getLine1AlphaTag(subId);
    }

    @HwSystemApi
    public static String getVoiceMailNumber(int subId) {
        return TelephonyManager.getDefault().getVoiceMailNumber(subId);
    }

    @HwSystemApi
    public static String getVoiceMailNumber(Context context, int subId) {
        return TelephonyManager.from(context).getVoiceMailNumber(subId);
    }

    @HwSystemApi
    public static int getVoiceMessageCount(int subId) {
        return TelephonyManager.getDefault().getVoiceMessageCount(subId);
    }

    @HwSystemApi
    public static String getVoiceMailAlphaTag(int subId) {
        return TelephonyManager.getDefault().getVoiceMailAlphaTag(subId);
    }

    @HwSystemApi
    public static String getVoiceMailAlphaTag(Context context, int subId) {
        return TelephonyManager.from(context).getVoiceMailAlphaTag(subId);
    }

    @HwSystemApi
    public static int getCdmaEriIconIndex(int subId) {
        return TelephonyManager.getDefault().getCdmaEriIconIndex(subId);
    }

    @HwSystemApi
    public static int getCdmaEriIconIndex(Context context, int subId) {
        return TelephonyManager.from(context).getCdmaEriIconIndex(subId);
    }

    @HwSystemApi
    public static int getCdmaEriIconMode(int subId) {
        return TelephonyManager.getDefault().getCdmaEriIconMode(subId);
    }

    @HwSystemApi
    public static int getCdmaEriIconMode(Context context, int subId) {
        return TelephonyManager.from(context).getCdmaEriIconMode(subId);
    }

    @HwSystemApi
    public static String getCdmaEriText(int subId) {
        return TelephonyManager.getDefault().getCdmaEriText(subId);
    }

    @HwSystemApi
    public static String getCdmaEriText(Context context, int subId) {
        return TelephonyManager.from(context).getCdmaEriText(subId);
    }

    public static boolean endCall(TelephonyManager telephonyManager) {
        if (telephonyManager == null) {
            return false;
        }
        return telephonyManager.endCall();
    }

    public static boolean isIdle(TelephonyManager telephonyManager) {
        if (telephonyManager == null) {
            return true;
        }
        return telephonyManager.isIdle();
    }

    public static int getLteOnCdmaMode(TelephonyManager telephonyManager) {
        if (telephonyManager == null) {
            return -1;
        }
        return telephonyManager.getLteOnCdmaMode();
    }

    public static int getCurrentPhoneType(TelephonyManager telephonyManager, int subId) {
        if (telephonyManager == null) {
            return 0;
        }
        return telephonyManager.getCurrentPhoneType(subId);
    }

    public static int getCurrentPhoneType(TelephonyManager telephonyManager) {
        if (telephonyManager == null) {
            return 0;
        }
        return telephonyManager.getCurrentPhoneType();
    }

    public static boolean handlePinMmi(TelephonyManager telephonyManager, String dialString) {
        if (telephonyManager != null) {
            return telephonyManager.handlePinMmi(dialString);
        }
        return false;
    }

    public static boolean isOffhook(TelephonyManager telephonyManager) {
        if (telephonyManager != null) {
            return telephonyManager.isOffhook();
        }
        return false;
    }

    public static String getNetworkOperator(TelephonyManager telephonyManager, int subId) {
        if (telephonyManager != null) {
            return telephonyManager.getNetworkOperator(subId);
        }
        return null;
    }

    public static int getNetworkTypeDchspap() {
        return 30;
    }

    public static int getNetworkTypeLteCa() {
        return 19;
    }

    public static boolean isRadioOn(TelephonyManager tm) {
        if (tm != null) {
            return tm.isRadioOn();
        }
        return false;
    }

    public static int invokeOemRilRequestRaw(TelephonyManager tm, byte[] oemReq, byte[] oemResp) {
        if (tm != null) {
            return tm.invokeOemRilRequestRaw(oemReq, oemResp);
        }
        return -1;
    }

    public static int getIntAtIndex(ContentResolver cr, String name, int index) throws Settings.SettingNotFoundException {
        return TelephonyManager.getIntAtIndex(cr, name, index);
    }

    @HwSystemApi
    public static boolean putIntAtIndex(ContentResolver cr, String name, int index, int value) {
        return TelephonyManager.putIntAtIndex(cr, name, index, value);
    }

    public static String getGroupIdLevel1(TelephonyManager tm, int subId) {
        if (tm != null) {
            return tm.getGroupIdLevel1(subId);
        }
        return null;
    }

    public static void setDataEnabledProperties(TelephonyManager tm, String appName, boolean enable) {
        if (tm != null) {
            tm.setDataEnabledProperties(appName, enable);
        }
    }

    @HwSystemApi
    public static boolean isMultiSimEnabled() {
        return TelephonyManager.getDefault().isMultiSimEnabled();
    }

    @HwSystemApi
    public static String getIsimIst() {
        return TelephonyManager.getDefault().getIsimIst();
    }

    @HwSystemApi
    public static void enableIms(TelephonyManager tm, int slotId) {
        if (tm != null) {
            tm.enableIms(slotId);
        }
    }

    @HwSystemApi
    public static void disableIms(TelephonyManager tm, int slotId) {
        if (tm != null) {
            tm.disableIms(slotId);
        }
    }

    @HwSystemApi
    public static int getCurrentPhoneType() {
        return TelephonyManager.getDefault().getCurrentPhoneType();
    }

    @HwSystemApi
    public static int getSubIdForPhoneAccount(PhoneAccount phoneAccount) {
        return TelephonyManager.getDefault().getSubIdForPhoneAccount(phoneAccount);
    }

    @HwSystemApi
    public static boolean isVideoCallingEnabled() {
        return TelephonyManager.getDefault().isVideoCallingEnabled();
    }

    @HwSystemApi
    public static boolean isImsRegistered() {
        return TelephonyManager.getDefault().isImsRegistered();
    }

    @HwSystemApi
    public static int getNetworkClass(int networkType) {
        return TelephonyManager.getNetworkClass(networkType);
    }

    @HwSystemApi
    public static boolean isWifiCallingAvailable() {
        return TelephonyManager.getDefault().isWifiCallingAvailable();
    }

    @HwSystemApi
    public static String getSimOperatorNumeric() {
        return TelephonyManager.getDefault().getSimOperatorNumeric();
    }

    /* renamed from: com.huawei.android.telephony.TelephonyManagerEx$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$android$telephony$TelephonyManager$MultiSimVariants = new int[TelephonyManager.MultiSimVariants.values().length];

        static {
            try {
                $SwitchMap$android$telephony$TelephonyManager$MultiSimVariants[TelephonyManager.MultiSimVariants.DSDS.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$android$telephony$TelephonyManager$MultiSimVariants[TelephonyManager.MultiSimVariants.DSDA.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$android$telephony$TelephonyManager$MultiSimVariants[TelephonyManager.MultiSimVariants.TSTS.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }

    @HwSystemApi
    public static MultiSimVariantsExt getMultiSimConfiguration() {
        int i = AnonymousClass1.$SwitchMap$android$telephony$TelephonyManager$MultiSimVariants[TelephonyManager.getDefault().getMultiSimConfiguration().ordinal()];
        if (i == 1) {
            return MultiSimVariantsExt.DSDS;
        }
        if (i == 2) {
            return MultiSimVariantsExt.DSDA;
        }
        if (i != 3) {
            return MultiSimVariantsExt.UNKNOWN;
        }
        return MultiSimVariantsExt.TSTS;
    }

    @HwSystemApi
    public static int getCurrentPhoneType(int subId) {
        return TelephonyManager.getDefault().getCurrentPhoneType(subId);
    }

    @HwSystemApi
    public static String getTelephonyProperty(int phoneId, String property, String defaultVal) {
        return TelephonyManager.getTelephonyProperty(phoneId, property, defaultVal);
    }

    @HwSystemApi
    public static int getCallState(TelephonyManager telephonyManager, int subid) {
        if (telephonyManager != null) {
            return telephonyManager.getCallState(subid);
        }
        return 0;
    }

    @HwSystemApi
    public static boolean hasIccCard(int slotIndex) {
        return TelephonyManager.getDefault().hasIccCard(slotIndex);
    }

    @HwSystemApi
    public static String getNetworkCountryIsoForPhone(int phoneId) {
        return TelephonyManager.getDefault().getNetworkCountryIsoForPhone(phoneId);
    }

    @HwSystemApi
    public static void setTelephonyProperty(int phoneId, String property, String value) {
        TelephonyManager.getDefault();
        TelephonyManager.setTelephonyProperty(phoneId, property, value);
    }

    @HwSystemApi
    public static int getCurrentPhoneTypeForSlot(int slotIndex) {
        return TelephonyManager.getDefault().getCurrentPhoneTypeForSlot(slotIndex);
    }
}
