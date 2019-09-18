package com.android.internal.telephony;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Looper;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.SubscriptionManager;
import com.android.internal.telephony.dataconnection.HwVSimNetworkFactory;
import com.android.internal.telephony.dataconnection.HwVSimTelephonyNetworkFactory;
import com.android.internal.telephony.fullnetwork.HwFullNetworkConstants;
import com.android.internal.telephony.uicc.HwVSimUiccController;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.vsim.HwVSimController;
import com.android.internal.telephony.vsim.HwVSimLog;
import com.android.internal.telephony.vsim.HwVSimModemAdapter;
import com.android.internal.telephony.vsim.HwVSimSlotSwitchController;
import com.android.internal.telephony.vsim.HwVSimUtils;
import com.android.internal.telephony.vsim.HwVSimUtilsInner;
import com.android.internal.util.IndentingPrintWriter;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Arrays;

public class HwVSimPhoneFactory {
    private static final String LOG_TAG = "VSimFactory";
    private static final String PROP_IS_VSIM_ENABLED = "persist.radio.is_vsim_enabled";
    private static final String PROP_PERSIST_RADIO_SIM_SLOT_CFG = "persist.radio.sim_slot_cfg";
    private static final String VALUE_VSIM_EANBLED_SUBID = "vsim_enabled_subid";
    private static final String VALUE_VSIM_SAVED_MAINSLOT = "vsim_saved_mainslot";
    private static final String VALUE_VSIM_SAVED_NETWORK_MODE = "vsim_saved_network_mode";
    private static final String VALUE_VSIM_USER_ENABLED = "vsim_user_enabled";
    private static final String VALUE_VSIM_USER_RESERVED_SUBID = "vsim_user_reserved_subid";
    private static final String VALUE_VSIM_USER_UNRESERVED_SUB_CARDTYPE = "vsim_user_unreserved_sub_card_type";
    static final Object mLock = new Object();
    private static Context sContext;
    private static boolean sInitiated = false;
    private static CommandsInterface sVSimCi;
    private static HwVSimController sVSimController;
    private static HwVSimNetworkFactory sVSimNetworkFactory;
    private static Phone sVSimPhone;
    private static HwVSimPhoneSwitcher sVSimPhoneSwitcher;
    private static HwVSimSlotSwitchController sVSimSlotSwitchController;
    private static HwVSimTelephonyNetworkFactory[] sVSimTelephonyNetworkFactories;
    private static HwVSimUiccController sVSimUiccController;

    private HwVSimPhoneFactory() {
        logd("HwVSimPhoneFactory");
    }

    public static void make(Context context, PhoneNotifier notifier, Phone[] phones, CommandsInterface[] cis) {
        Context context2 = context;
        Phone[] phoneArr = phones;
        CommandsInterface[] commandsInterfaceArr = cis;
        slogd("make");
        int numPhones = phoneArr.length;
        sContext = context2;
        sVSimCi = new HwVSimRIL(context2, RILConstants.PREFERRED_NETWORK_MODE, -1, 2);
        sVSimUiccController = HwVSimUiccController.make(context2, sVSimCi);
        sVSimPhone = new HwVSimPhone(context2, sVSimCi, notifier);
        int i = 0;
        Context context3 = context2;
        HwVSimPhoneSwitcher hwVSimPhoneSwitcher = new HwVSimPhoneSwitcher(PhoneFactory.MAX_ACTIVE_PHONES, numPhones, context3, Looper.myLooper(), sVSimPhone, sVSimCi, phoneArr[0], phoneArr[1], commandsInterfaceArr);
        sVSimPhoneSwitcher = hwVSimPhoneSwitcher;
        HwVSimNetworkFactory hwVSimNetworkFactory = new HwVSimNetworkFactory(sVSimPhoneSwitcher, Looper.myLooper(), context3, 2, sVSimPhone.mDcTracker);
        sVSimNetworkFactory = hwVSimNetworkFactory;
        sVSimTelephonyNetworkFactories = new HwVSimTelephonyNetworkFactory[numPhones];
        while (true) {
            int i2 = i;
            if (i2 < numPhones) {
                HwVSimTelephonyNetworkFactory[] hwVSimTelephonyNetworkFactoryArr = sVSimTelephonyNetworkFactories;
                HwVSimTelephonyNetworkFactory hwVSimTelephonyNetworkFactory = new HwVSimTelephonyNetworkFactory(sVSimPhoneSwitcher, Looper.myLooper(), context2, i2, phoneArr[i2].mDcTracker);
                hwVSimTelephonyNetworkFactoryArr[i2] = hwVSimTelephonyNetworkFactory;
                i = i2 + 1;
            } else {
                HwVSimSlotSwitchController.create(context2, sVSimCi, commandsInterfaceArr);
                sVSimSlotSwitchController = HwVSimSlotSwitchController.getInstance();
                HwVSimController.create(context2, sVSimPhone, sVSimCi, phoneArr, commandsInterfaceArr);
                sVSimController = HwVSimController.getInstance();
                HwTelephonyFactory.getHwInnerVSimManager().createHwVSimService(context2);
                sInitiated = true;
                return;
            }
        }
    }

    public static Phone getVSimPhone() {
        return sVSimPhone;
    }

    private void logd(String s) {
        HwVSimLog.VSimLogD(LOG_TAG, s);
    }

    private static void slogd(String s) {
        HwVSimLog.VSimLogD(LOG_TAG, s);
    }

    private static void sloge(String s) {
        HwVSimLog.VSimLogE(LOG_TAG, s);
    }

    public static void dump(FileDescriptor fd, PrintWriter printwriter, String[] args) {
        IndentingPrintWriter pw = new IndentingPrintWriter(printwriter, "  ");
        pw.println("VSimFactory:");
        if (!sInitiated) {
            pw.println(" not initiated");
            return;
        }
        if (HwVSimUtilsInner.isPlatformRealTripple()) {
            sVSimPhoneSwitcher.dump(fd, pw, args);
            pw.println();
            sVSimNetworkFactory.dump(fd, pw, args);
            pw.println();
        }
        Phone[] phones = PhoneFactory.getPhones();
        for (int i = 0; i < phones.length; i++) {
            pw.increaseIndent();
            if (HwVSimUtilsInner.isPlatformRealTripple()) {
                sVSimTelephonyNetworkFactories[i].dump(fd, pw, args);
            }
            pw.flush();
            pw.decreaseIndent();
            pw.println("++++++++++++++++++++++++++++++++");
        }
        pw.println("++++++++++++++++++++++++++++++++");
        sVSimUiccController.dump(fd, pw, args);
        pw.flush();
        pw.println("++++++++++++++++++++++++++++++++");
        sVSimSlotSwitchController.dump(fd, pw, args);
        pw.flush();
        pw.println("++++++++++++++++++++++++++++++++");
        sVSimController.dump(fd, pw, args);
        pw.flush();
    }

    public static void setVSimSavedMainSlot(int subId) {
        Settings.System.putInt(sContext.getContentResolver(), VALUE_VSIM_SAVED_MAINSLOT, subId);
        slogd("setVSimSavedMainSlot: " + subId);
    }

    public static int getVSimSavedMainSlot() {
        try {
            return Settings.System.getInt(sContext.getContentResolver(), VALUE_VSIM_SAVED_MAINSLOT);
        } catch (Settings.SettingNotFoundException e) {
            return -1;
        }
    }

    public static void setVSimSavedNetworkMode(int modemId, int mode) {
        ContentResolver contentResolver = sContext.getContentResolver();
        Settings.System.putInt(contentResolver, VALUE_VSIM_SAVED_NETWORK_MODE + modemId, mode);
        slogd("setVSimSavedNetworkMode: " + mode + ", for modemId:" + modemId);
    }

    public static int getVSimSavedNetworkMode(int modemId) {
        try {
            ContentResolver contentResolver = sContext.getContentResolver();
            return Settings.System.getInt(contentResolver, VALUE_VSIM_SAVED_NETWORK_MODE + modemId);
        } catch (Settings.SettingNotFoundException e) {
            slogd("getVSimSavedNetworkMode: not found");
            return -1;
        }
    }

    public static void setVSimEnabledSubId(int subId) {
        Settings.System.putInt(sContext.getContentResolver(), VALUE_VSIM_EANBLED_SUBID, subId);
        slogd("setVSimEnabledSubId: " + subId);
        if (-1 != subId) {
            setIsVsimEnabledProp(true);
        }
    }

    public static int getVSimEnabledSubId() {
        try {
            return Settings.System.getInt(sContext.getContentResolver(), VALUE_VSIM_EANBLED_SUBID);
        } catch (Settings.SettingNotFoundException e) {
            return -1;
        }
    }

    public static void setVSimUserEnabled(int value) {
        Settings.System.putInt(sContext.getContentResolver(), VALUE_VSIM_USER_ENABLED, value);
        slogd("setVSimUserEnabled: " + value);
    }

    public static int getVSimUserEnabled() {
        try {
            return Settings.System.getInt(sContext.getContentResolver(), VALUE_VSIM_USER_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            return -1;
        }
    }

    public static void setVSimUserReservedSubId(int subId) {
        if (!HwVSimUtilsInner.isPlatformRealTripple() || HwVSimUtilsInner.isVSimDsdsVersionOne()) {
            Settings.System.putInt(sContext.getContentResolver(), VALUE_VSIM_USER_RESERVED_SUBID, subId);
            slogd("setVSimUserReservedSubId: " + subId);
        }
    }

    public static int getVSimUserReservedSubId() {
        if (HwVSimUtilsInner.isPlatformRealTripple() && !HwVSimUtilsInner.isVSimDsdsVersionOne()) {
            return -1;
        }
        int userReservedSubid = -1;
        try {
            userReservedSubid = Settings.System.getInt(sContext.getContentResolver(), VALUE_VSIM_USER_RESERVED_SUBID);
        } catch (Settings.SettingNotFoundException e) {
            slogd("getVSimUserReservedSubId: not found");
        }
        return userReservedSubid;
    }

    public static void setIsVsimEnabledProp(boolean isVsimEnabled) {
        SystemProperties.set(PROP_IS_VSIM_ENABLED, isVsimEnabled ? "true" : "false");
        slogd("set persist.radio.is_vsim_enabled = " + isVsimEnabled);
    }

    public static void setUserSwitchDualCardSlots(int subscription) {
        if (sContext == null) {
            slogd("setUserSwitchDualCardSlots, sContext is null, return.");
            return;
        }
        Settings.System.putInt(sContext.getContentResolver(), "switch_dual_card_slots", subscription);
        slogd("setUserSwitchDualCardSlots: " + subscription);
        if (HwHotplugController.IS_HOTSWAP_SUPPORT) {
            updateHotPlugMainSlotIccId(subscription);
        }
    }

    private static void updateHotPlugMainSlotIccId(int subscription) {
        UiccCard card = UiccController.getInstance().getUiccCard(subscription);
        if (card != null && HwHotplugController.IS_HOTSWAP_SUPPORT) {
            HwHotplugController.getInstance().updateHotPlugMainSlotIccId(card.getIccId());
        }
    }

    public static ServiceStateTracker makeVSimServiceStateTracker(Phone phone, CommandsInterface ci) {
        return new HwVSimServiceStateTracker((GsmCdmaPhone) phone, ci);
    }

    public static boolean isVSimPhone(Phone phone) {
        return phone instanceof HwVSimPhone;
    }

    public static int getTopPrioritySubscriptionId() {
        if (sVSimPhoneSwitcher != null) {
            return sVSimPhoneSwitcher.getTopPrioritySubscriptionId();
        }
        return SubscriptionManager.getDefaultDataSubscriptionId();
    }

    public static void savePendingDeviceInfoToSP() {
        if (HwVSimUtilsInner.isPlatformTwoModems()) {
            if (sContext == null) {
                sloge("savePendingDeviceInfoToSP, sContext is null");
                return;
            }
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(sContext).edit();
            for (int i = 0; i < HwVSimModemAdapter.PHONE_COUNT; i++) {
                Phone phone = PhoneFactory.getPhone(i);
                if (sContext == null || phone == null) {
                    sloge("savePendingDeviceInfoToSP, phone is null for phone id:" + i);
                } else {
                    String imei = phone.getImei();
                    String deviceSvn = phone.getDeviceSvn();
                    String deviceId = phone.getDeviceId();
                    String meid = phone.getMeid();
                    String esn = phone.getEsn();
                    try {
                        imei = HwAESCryptoUtil.encrypt(HwFullNetworkConstants.MASTER_PASSWORD, imei);
                        deviceSvn = HwAESCryptoUtil.encrypt(HwFullNetworkConstants.MASTER_PASSWORD, deviceSvn);
                        deviceId = HwAESCryptoUtil.encrypt(HwFullNetworkConstants.MASTER_PASSWORD, deviceId);
                        meid = HwAESCryptoUtil.encrypt(HwFullNetworkConstants.MASTER_PASSWORD, meid);
                        esn = HwAESCryptoUtil.encrypt(HwFullNetworkConstants.MASTER_PASSWORD, esn);
                    } catch (Exception e) {
                        sloge("encrypt excepiton");
                    }
                    editor.putString(HwVSimUtils.IMEI_PREF + i, imei);
                    editor.putString(HwVSimUtils.DEVICE_SVN_PREF + i, deviceSvn);
                    editor.putString(HwVSimUtils.DEVICE_ID_PREF + i, deviceId);
                    editor.putString(HwVSimUtils.MEID_PREF + i, meid);
                    editor.putString(HwVSimUtils.ESN_PREF + i, esn);
                }
            }
            editor.commit();
        }
    }

    public static String getPendingDeviceInfoFromSP(String prefKey, int phoneId) {
        if (!HwVSimUtilsInner.isPlatformTwoModems()) {
            return null;
        }
        if (sContext == null) {
            sloge("getPendingDeviceInfoFromSP, sContext is null");
            return null;
        }
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(sContext);
        String result = sp.getString(prefKey + phoneId, null);
        try {
            result = HwAESCryptoUtil.decrypt(HwFullNetworkConstants.MASTER_PASSWORD, result);
        } catch (Exception e) {
            sloge("decrypt excepiton");
        }
        return result;
    }

    public static void setUnReservedSubCardType(int cardType) {
        if (HwVSimUtilsInner.isPlatformTwoModems()) {
            Settings.System.putInt(sContext.getContentResolver(), VALUE_VSIM_USER_UNRESERVED_SUB_CARDTYPE, cardType);
            slogd("setUnReservedSubCardType: " + cardType);
        }
    }

    public static int getUnReservedSubCardType() {
        if (!HwVSimUtilsInner.isPlatformTwoModems()) {
            return -1;
        }
        int cardType = Settings.System.getInt(sContext.getContentResolver(), VALUE_VSIM_USER_UNRESERVED_SUB_CARDTYPE, -1);
        slogd("getUnReservedSubCardType: " + cardType);
        return cardType;
    }

    public static void setPropPersistRadioSimSlotCfg(int[] slots) {
        if (slots != null) {
            String simSlotCfg = "";
            boolean noError = true;
            if (slots.length == 2) {
                simSlotCfg = slots[0] + "," + slots[1];
            } else if (slots.length == 3) {
                simSlotCfg = slots[0] + "," + slots[1] + "," + slots[2];
            } else {
                noError = false;
                sloge("error slots: " + Arrays.toString(slots));
            }
            if (noError) {
                SystemProperties.set(PROP_PERSIST_RADIO_SIM_SLOT_CFG, simSlotCfg);
                slogd("setPropPersistRadioSimSlotCfg: " + simSlotCfg);
            }
        }
    }

    public static HwVSimNetworkFactory getsVSimNetworkFactory() {
        return sVSimNetworkFactory;
    }
}
