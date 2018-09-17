package com.android.internal.telephony;

import android.content.Context;
import android.os.Looper;
import android.os.SystemProperties;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.telephony.SubscriptionManager;
import com.android.internal.telephony.dataconnection.HwVSimNetworkFactory;
import com.android.internal.telephony.dataconnection.HwVSimTelephonyNetworkFactory;
import com.android.internal.telephony.uicc.HwVSimUiccController;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.vsim.HwVSimController;
import com.android.internal.telephony.vsim.HwVSimLog;
import com.android.internal.telephony.vsim.HwVSimSlotSwitchController;
import com.android.internal.telephony.vsim.HwVSimUtilsInner;
import com.android.internal.util.IndentingPrintWriter;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class HwVSimPhoneFactory {
    private static final String LOG_TAG = "VSimFactory";
    private static final String PROP_IS_VSIM_ENABLED = "persist.radio.is_vsim_enabled";
    private static final String PROP_LOCK_OVERSEAS_MODE = "persist.radio.lock_overseasmode";
    private static final String PROP_OVERSEAS_MODE = "persist.radio.overseas_mode";
    private static final String PROP_VSIM_SUPPORT_GSM = "persist.radio.vsim_support_gsm";
    private static final String VALUE_VSIM_EANBLED_SUBID = "vsim_enabled_subid";
    private static final String VALUE_VSIM_OCCUPIED_SUBID = "vsim_occupied_subid";
    private static final String VALUE_VSIM_SAVED_COMMRIL_MODE = "vsim_saved_commril_mode";
    private static final String VALUE_VSIM_SAVED_MAINSLOT = "vsim_saved_mainslot";
    private static final String VALUE_VSIM_SAVED_NETWORK_MODE = "vsim_saved_network_mode";
    private static final String VALUE_VSIM_ULONLY_MODE = "vsim_ulonly_mode";
    private static final String VALUE_VSIM_USER_ENABLED = "vsim_user_enabled";
    private static final String VALUE_VSIM_USER_RESERVED_SUBID = "vsim_user_reserved_subid";
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
        slogd("make");
        int numPhones = phones.length;
        sContext = context;
        sVSimCi = new HwVSimRIL(context, RILConstants.PREFERRED_NETWORK_MODE, -1, Integer.valueOf(2));
        sVSimUiccController = HwVSimUiccController.make(context, sVSimCi);
        sVSimPhone = new HwVSimPhone(context, sVSimCi, notifier);
        if (HwVSimUtilsInner.isPlatformRealTripple()) {
            sVSimPhoneSwitcher = new HwVSimPhoneSwitcher(PhoneFactory.MAX_ACTIVE_PHONES, numPhones, context, Looper.myLooper(), sVSimPhone, sVSimCi, phones[0], phones[1], cis);
            sVSimNetworkFactory = new HwVSimNetworkFactory(sVSimPhoneSwitcher, Looper.myLooper(), context, 2, sVSimPhone.mDcTracker);
            sVSimTelephonyNetworkFactories = new HwVSimTelephonyNetworkFactory[numPhones];
            for (int i = 0; i < numPhones; i++) {
                sVSimTelephonyNetworkFactories[i] = new HwVSimTelephonyNetworkFactory(sVSimPhoneSwitcher, Looper.myLooper(), context, i, phones[i].mDcTracker);
            }
        }
        HwVSimSlotSwitchController.create(context, sVSimCi, cis);
        sVSimSlotSwitchController = HwVSimSlotSwitchController.getInstance();
        HwVSimController.create(context, sVSimPhone, sVSimCi, phones, cis);
        sVSimController = HwVSimController.getInstance();
        HwTelephonyFactory.getHwInnerVSimManager().createHwVSimService(context);
        sInitiated = true;
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

    public static void dump(FileDescriptor fd, PrintWriter printwriter, String[] args) {
        IndentingPrintWriter pw = new IndentingPrintWriter(printwriter, "  ");
        pw.println("VSimFactory:");
        if (sInitiated) {
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
            return;
        }
        pw.println(" not initiated");
    }

    public static void setVSimSavedMainSlot(int subId) {
        System.putInt(sContext.getContentResolver(), VALUE_VSIM_SAVED_MAINSLOT, subId);
        slogd("setVSimSavedMainSlot: " + subId);
    }

    public static int getVSimSavedMainSlot() {
        int subId = -1;
        try {
            return System.getInt(sContext.getContentResolver(), VALUE_VSIM_SAVED_MAINSLOT);
        } catch (SettingNotFoundException e) {
            return subId;
        }
    }

    public static void setVSimSavedCommrilMode(int mode) {
        System.putInt(sContext.getContentResolver(), VALUE_VSIM_SAVED_COMMRIL_MODE, mode);
        slogd("setVSimSavedCommrilMode: " + mode);
    }

    public static int getVSimSavedCommrilMode(int default_mode) {
        int mode = default_mode;
        try {
            return System.getInt(sContext.getContentResolver(), VALUE_VSIM_SAVED_COMMRIL_MODE);
        } catch (SettingNotFoundException e) {
            return mode;
        }
    }

    public static void setVSimSavedNetworkMode(int modemId, int mode) {
        System.putInt(sContext.getContentResolver(), VALUE_VSIM_SAVED_NETWORK_MODE + modemId, mode);
        slogd("setVSimSavedNetworkMode: " + mode + ", for modemId:" + modemId);
    }

    public static int getVSimSavedNetworkMode(int modemId) {
        int mode = -1;
        try {
            return System.getInt(sContext.getContentResolver(), VALUE_VSIM_SAVED_NETWORK_MODE + modemId);
        } catch (SettingNotFoundException e) {
            slogd("getVSimSavedNetworkMode: not found");
            return mode;
        }
    }

    public static void setVSimULOnlyMode(boolean isULOnly) {
        if (!HwVSimUtilsInner.isPlatformRealTripple()) {
            System.putInt(sContext.getContentResolver(), VALUE_VSIM_ULONLY_MODE, isULOnly ? 1 : 0);
            slogd("setVSimULOnlyMode: " + isULOnly);
        }
    }

    public static boolean getVSimULOnlyMode(boolean defValue) {
        if (HwVSimUtilsInner.isPlatformRealTripple()) {
            return false;
        }
        boolean value = defValue;
        try {
            value = System.getInt(sContext.getContentResolver(), VALUE_VSIM_ULONLY_MODE) == 1;
        } catch (SettingNotFoundException e) {
        }
        return value;
    }

    public static void setVSimEnabledSubId(int subId) {
        System.putInt(sContext.getContentResolver(), VALUE_VSIM_EANBLED_SUBID, subId);
        slogd("setVSimEnabledSubId: " + subId);
        if (-1 != subId) {
            setIsVsimEnabledProp(true);
        }
    }

    public static int getVSimEnabledSubId() {
        int enabledSubid = -1;
        try {
            return System.getInt(sContext.getContentResolver(), VALUE_VSIM_EANBLED_SUBID);
        } catch (SettingNotFoundException e) {
            return enabledSubid;
        }
    }

    public static void setVSimUserEnabled(int value) {
        System.putInt(sContext.getContentResolver(), VALUE_VSIM_USER_ENABLED, value);
        slogd("setVSimUserEnabled: " + value);
    }

    public static int getVSimUserEnabled() {
        int value = -1;
        try {
            return System.getInt(sContext.getContentResolver(), VALUE_VSIM_USER_ENABLED);
        } catch (SettingNotFoundException e) {
            return value;
        }
    }

    public static void setVSimUserReservedSubId(int subId) {
        if (!HwVSimUtilsInner.isPlatformRealTripple() || (HwVSimUtilsInner.isVSimDsdsVersionOne() ^ 1) == 0) {
            System.putInt(sContext.getContentResolver(), VALUE_VSIM_USER_RESERVED_SUBID, subId);
            slogd("setVSimUserReservedSubId: " + subId);
        }
    }

    public static int getVSimUserReservedSubId() {
        if (HwVSimUtilsInner.isPlatformRealTripple() && (HwVSimUtilsInner.isVSimDsdsVersionOne() ^ 1) != 0) {
            return -1;
        }
        int userReservedSubid = -1;
        try {
            userReservedSubid = System.getInt(sContext.getContentResolver(), VALUE_VSIM_USER_RESERVED_SUBID);
        } catch (SettingNotFoundException e) {
            slogd("getVSimUserReservedSubId: not found");
        }
        return userReservedSubid;
    }

    public static void setVSimOccupiedSubId(int subId) {
        System.putInt(sContext.getContentResolver(), VALUE_VSIM_OCCUPIED_SUBID, subId);
        slogd("setVSimOccupiedSubId: " + subId);
    }

    public static int getVSimOccupiedSubId() {
        int occupiedSubId = -1;
        try {
            return System.getInt(sContext.getContentResolver(), VALUE_VSIM_OCCUPIED_SUBID);
        } catch (SettingNotFoundException e) {
            return occupiedSubId;
        }
    }

    public static void setULOnlyProp(boolean isULOnly) {
        slogd("setULOnlyProp: " + isULOnly);
        if (!HwVSimUtilsInner.isPlatformRealTripple()) {
            SystemProperties.set(PROP_VSIM_SUPPORT_GSM, Boolean.valueOf(isULOnly ^ 1).toString());
        }
    }

    public static boolean getULOnlyProp() {
        if (HwVSimUtilsInner.isPlatformRealTripple()) {
            return false;
        }
        return Boolean.valueOf(SystemProperties.getBoolean(PROP_VSIM_SUPPORT_GSM, true)).booleanValue() ^ 1;
    }

    public static void setLockOverseasMode(boolean isLock) {
        SystemProperties.set(PROP_LOCK_OVERSEAS_MODE, isLock ? "true" : "false");
        slogd("set persist.radio.lock_overseasmode = " + isLock);
    }

    public static void setOverseasMode(boolean isOverseas) {
        SystemProperties.set(PROP_OVERSEAS_MODE, isOverseas ? "true" : "false");
        slogd("set persist.radio.overseas_mode = " + isOverseas);
    }

    public static boolean getOverseasMode() {
        boolean isOverseasMode = SystemProperties.getBoolean(PROP_OVERSEAS_MODE, false);
        slogd("get persist.radio.overseas_mode = " + isOverseasMode);
        return isOverseasMode;
    }

    public static void setIsVsimEnabledProp(boolean isVsimEnabled) {
        SystemProperties.set(PROP_IS_VSIM_ENABLED, isVsimEnabled ? "true" : "false");
        slogd("set persist.radio.is_vsim_enabled = " + isVsimEnabled);
    }

    public static boolean getIsVsimEnabledProp() {
        boolean isVsimEnabled = SystemProperties.getBoolean(PROP_IS_VSIM_ENABLED, false);
        slogd("get persist.radio.is_vsim_enabled = " + isVsimEnabled);
        return isVsimEnabled;
    }

    public static void setUserSwitchDualCardSlots(int subscription) {
        if (sContext == null) {
            slogd("setUserSwitchDualCardSlots, sContext is null, return.");
            return;
        }
        System.putInt(sContext.getContentResolver(), "switch_dual_card_slots", subscription);
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
}
