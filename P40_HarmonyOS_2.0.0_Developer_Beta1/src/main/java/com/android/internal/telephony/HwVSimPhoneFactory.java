package com.android.internal.telephony;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.SubscriptionManager;
import com.android.internal.telephony.dataconnection.HwVSimNetworkFactory;
import com.android.internal.telephony.dataconnection.HwVSimTelephonyNetworkFactory;
import com.android.internal.telephony.fullnetwork.HwFullNetworkManager;
import com.android.internal.telephony.uicc.HwVSimUiccController;
import com.android.internal.telephony.vsim.HwVSimController;
import com.android.internal.telephony.vsim.HwVSimLog;
import com.android.internal.telephony.vsim.HwVSimModemAdapter;
import com.android.internal.telephony.vsim.HwVSimSlotSwitchController;
import com.android.internal.telephony.vsim.HwVSimUtilsImpl;
import com.android.internal.telephony.vsim.HwVSimUtilsInner;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.hwparttelephonyvsim.BuildConfig;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.PhoneFactoryExt;
import com.huawei.internal.telephony.PhoneNotifierEx;
import com.huawei.internal.telephony.ServiceStateTrackerEx;
import com.huawei.internal.telephony.uicc.UiccCardExt;
import com.huawei.internal.telephony.uicc.UiccControllerExt;
import com.huawei.internal.telephony.vsim.HwVSimBaseController;
import com.huawei.internal.telephony.vsim.HwVSimMtkController;
import com.huawei.internal.util.IndentingPrintWriterEx;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Arrays;

public class HwVSimPhoneFactory {
    private static final String LOG_TAG = "VSimFactory";
    private static final String PROP_IS_VSIM_ENABLED = "persist.radio.is_vsim_enabled";
    private static final String PROP_PERSIST_RADIO_SIM_SLOT_CFG = "persist.radio.sim_slot_cfg";
    private static final String VALUE_VSIM_EANBLED_SUBID = "vsim_enabled_subid";
    private static final String VALUE_VSIM_SAVED_MAINSLOT = "vsim_saved_mainslot";
    private static final String VALUE_VSIM_SAVED_MOBILE_DATA = "vsim_mobile_data";
    private static final String VALUE_VSIM_SAVED_NETWORK_MODE = "vsim_saved_network_mode";
    private static final String VALUE_VSIM_USER_ENABLED = "vsim_user_enabled";
    private static final String VALUE_VSIM_USER_RESERVED_SUBID = "vsim_user_reserved_subid";
    private static final String VALUE_VSIM_USER_UNRESERVED_SUB_CARDTYPE = "vsim_user_unreserved_sub_card_type";
    static final Object mLock = new Object();
    private static Context sContext;
    private static HwVSimServiceStateTracker sHwVSimServiceStateTracker;
    private static boolean sInitiated = false;
    private static CommandsInterfaceEx sVSimCi;
    private static HwVSimController sVSimController;
    private static HwVSimNetworkFactory sVSimNetworkFactory;
    private static PhoneExt sVSimPhone;
    private static HwVSimPhoneSwitcher sVSimPhoneSwitcher;
    private static HwVSimSlotSwitchController sVSimSlotSwitchController;
    private static HwVSimTelephonyNetworkFactory[] sVSimTelephonyNetworkFactories;
    private static HwVSimUiccController sVSimUiccController;
    private static HwVSimBaseController sVsimBaseController;

    private HwVSimPhoneFactory() {
        logd("HwVSimPhoneFactory");
    }

    public static void make(Context context, PhoneNotifierEx notifier, PhoneExt[] phones, CommandsInterfaceEx[] cis) {
        slogd("make");
        if (!HwVSimMtkController.isInstantiated() && HuaweiTelephonyConfigs.isMTKPlatform()) {
            sContext = context;
            HwVSimMtkController.create(context, phones, cis);
            sVsimBaseController = HwVSimMtkController.getInstance();
        } else if (HwVSimController.isInstantiated() || !HuaweiTelephonyConfigs.isHisiPlatform()) {
            RlogEx.i(LOG_TAG, "only create hw vsim service.");
        } else {
            int numPhones = phones.length;
            sContext = context;
            sVSimCi = new HwVSimRIL(context, PhoneExt.getPreferredNetworkMode(), -1, 2);
            sVSimUiccController = HwVSimUiccController.make(context, sVSimCi);
            sVSimPhone = new HwVSimPhone(context, sVSimCi, notifier);
            sVSimPhoneSwitcher = new HwVSimPhoneSwitcher(PhoneFactoryExt.MAX_ACTIVE_PHONES, numPhones, context, Looper.myLooper(), sVSimPhone, sVSimCi, phones[0], phones[1], cis);
            sVSimNetworkFactory = new HwVSimNetworkFactory(sVSimPhoneSwitcher, Looper.myLooper(), context, 2, sVSimPhone.getDcTracker());
            sVSimTelephonyNetworkFactories = new HwVSimTelephonyNetworkFactory[numPhones];
            for (int i = 0; i < numPhones; i++) {
                sVSimTelephonyNetworkFactories[i] = new HwVSimTelephonyNetworkFactory(sVSimPhoneSwitcher, Looper.myLooper(), context, i, phones[i].getDcTracker());
            }
            HwVSimSlotSwitchController.create(context, sVSimCi, cis);
            sVSimSlotSwitchController = HwVSimSlotSwitchController.getInstance();
            HwVSimController.create(context, sVSimPhone, sVSimCi, phones, cis);
            sVSimController = HwVSimController.getInstance();
        }
        HwInnerVSimManagerImpl.getDefault().createHwVSimService(context);
        sInitiated = true;
    }

    public static PhoneExt getVSimPhone() {
        return sVSimPhone;
    }

    public static boolean isInitiated() {
        return sInitiated;
    }

    private static void slogd(String log) {
        HwVSimLog.VSimLogD(LOG_TAG, log);
    }

    private static void sloge(String log) {
        HwVSimLog.error(LOG_TAG, log);
    }

    public static void dump(FileDescriptor fd, PrintWriter printwriter, String[] args) {
        IndentingPrintWriterEx pw = new IndentingPrintWriterEx(printwriter, "  ");
        pw.println("VSimFactory:");
        if (!sInitiated) {
            pw.println(" not initiated");
        } else if (HuaweiTelephonyConfigs.isMTKPlatform()) {
            pw.println("++++++++++++++++++++++++++++++++");
            sVsimBaseController.dump(fd, pw.getPrintWriter(), args);
            pw.flush();
        } else {
            if (HwVSimUtilsInner.isPlatformRealTripple()) {
                sVSimPhoneSwitcher.dump(fd, pw.getPrintWriter(), args);
                pw.println();
                sVSimNetworkFactory.dump(fd, pw.getPrintWriter(), args);
                pw.println();
            }
            PhoneExt[] phones = PhoneFactoryExt.getPhones();
            for (int i = 0; i < phones.length; i++) {
                pw.increaseIndent();
                if (HwVSimUtilsInner.isPlatformRealTripple()) {
                    sVSimTelephonyNetworkFactories[i].dump(fd, pw.getPrintWriter(), args);
                }
                pw.flush();
                pw.decreaseIndent();
                pw.println("++++++++++++++++++++++++++++++++");
            }
            pw.println("++++++++++++++++++++++++++++++++");
            sVSimUiccController.dump(fd, pw.getPrintWriter(), args);
            pw.flush();
            pw.println("++++++++++++++++++++++++++++++++");
            sVSimSlotSwitchController.dump(fd, pw.getPrintWriter(), args);
            pw.flush();
            pw.println("++++++++++++++++++++++++++++++++");
            sVSimController.dump(fd, pw.getPrintWriter(), args);
            pw.flush();
        }
    }

    public static int getVSimSavedMainSlot() {
        try {
            return Settings.System.getInt(sContext.getContentResolver(), VALUE_VSIM_SAVED_MAINSLOT);
        } catch (Settings.SettingNotFoundException e) {
            return -1;
        }
    }

    public static void setVSimSavedMainSlot(int subId) {
        Settings.System.putInt(sContext.getContentResolver(), VALUE_VSIM_SAVED_MAINSLOT, subId);
        slogd("setVSimSavedMainSlot: " + subId);
    }

    public static void setVSimSavedNetworkMode(int modemId, int mode) {
        ContentResolver contentResolver = sContext.getContentResolver();
        Settings.System.putInt(contentResolver, VALUE_VSIM_SAVED_NETWORK_MODE + modemId, mode);
        slogd("setVSimSavedNetworkMode: " + mode + ", for modemId:" + modemId);
    }

    public static void setVSimSavedMobileData(int phoneId, int state) {
        ContentResolver contentResolver = sContext.getContentResolver();
        Settings.System.putInt(contentResolver, VALUE_VSIM_SAVED_MOBILE_DATA + phoneId, state);
        slogd("setVSimSavedMobileData: " + state + ", for phoneId:" + phoneId);
    }

    public static int getVSimSavedMobileData(int phoneId) {
        ContentResolver contentResolver = sContext.getContentResolver();
        return Settings.System.getInt(contentResolver, VALUE_VSIM_SAVED_MOBILE_DATA + phoneId, -1);
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

    public static int getVSimEnabledSubId() {
        try {
            return Settings.System.getInt(sContext.getContentResolver(), VALUE_VSIM_EANBLED_SUBID);
        } catch (Settings.SettingNotFoundException e) {
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

    public static int getVSimEnabledSubId(Context context) {
        if (context == null) {
            return -1;
        }
        try {
            return Settings.System.getInt(context.getContentResolver(), VALUE_VSIM_EANBLED_SUBID);
        } catch (Settings.SettingNotFoundException e) {
            return -1;
        }
    }

    public static int getVSimUserEnabled() {
        try {
            return Settings.System.getInt(sContext.getContentResolver(), VALUE_VSIM_USER_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            return -1;
        }
    }

    public static void setVSimUserEnabled(int value) {
        Settings.System.putInt(sContext.getContentResolver(), VALUE_VSIM_USER_ENABLED, value);
        slogd("setVSimUserEnabled: " + value);
    }

    public static int getVSimUserReservedSubId(Context context) {
        if (context == null) {
            return -1;
        }
        if (HwVSimUtilsInner.isPlatformRealTripple() && !HwVSimUtilsInner.isVSimDsdsVersionOne()) {
            return -1;
        }
        try {
            return Settings.System.getInt(context.getContentResolver(), VALUE_VSIM_USER_RESERVED_SUBID);
        } catch (Settings.SettingNotFoundException e) {
            slogd("getVsimUserReservedSubId: not found");
            return -1;
        }
    }

    public static void setVSimUserReservedSubId(Context context, int subId) {
        if (context == null) {
            return;
        }
        if (!HwVSimUtilsInner.isPlatformRealTripple() || HwVSimUtilsInner.isVSimDsdsVersionOne()) {
            Settings.System.putInt(context.getContentResolver(), VALUE_VSIM_USER_RESERVED_SUBID, subId);
            slogd("setVsimUserReservedSubId: " + subId);
        }
    }

    public static void setIsVsimEnabledProp(boolean isVsimEnabled) {
        SystemPropertiesEx.set(PROP_IS_VSIM_ENABLED, isVsimEnabled ? "true" : "false");
        slogd("set persist.radio.is_vsim_enabled = " + isVsimEnabled);
    }

    public static void setUserSwitchDualCardSlots(int subscription) {
        Context context = sContext;
        if (context == null) {
            slogd("setUserSwitchDualCardSlots, sContext is null, return.");
            return;
        }
        Settings.System.putInt(context.getContentResolver(), "switch_dual_card_slots", subscription);
        slogd("setUserSwitchDualCardSlots: " + subscription);
        if (HwHotplugController.IS_HOTSWAP_SUPPORT) {
            updateHotPlugMainSlotIccId(subscription);
        }
    }

    private static void updateHotPlugMainSlotIccId(int subscription) {
        UiccCardExt card = UiccControllerExt.getInstance().getUiccCard(subscription);
        if (card != null && HwHotplugController.IS_HOTSWAP_SUPPORT) {
            HwHotplugController.getInstance().updateHotPlugMainSlotIccId(card.getIccId());
        }
    }

    public static ServiceStateTrackerEx makeVSimServiceStateTracker(PhoneExt phone, CommandsInterfaceEx ci) {
        sHwVSimServiceStateTracker = new HwVSimServiceStateTracker(phone, ci);
        return sHwVSimServiceStateTracker;
    }

    public static int getTopPrioritySubscriptionId() {
        HwVSimPhoneSwitcher hwVSimPhoneSwitcher = sVSimPhoneSwitcher;
        if (hwVSimPhoneSwitcher != null) {
            return hwVSimPhoneSwitcher.getTopPrioritySubscriptionId();
        }
        return SubscriptionManager.getDefaultDataSubscriptionId();
    }

    public static void savePendingDeviceInfoToSP() {
        if (HwVSimUtilsInner.isPlatformTwoModems()) {
            Context context = sContext;
            if (context == null) {
                sloge("savePendingDeviceInfoToSP, sContext is null");
                return;
            }
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
            for (int i = 0; i < HwVSimModemAdapter.PHONE_COUNT; i++) {
                PhoneExt phone = PhoneFactoryExt.getPhone(i);
                if (sContext == null || phone == null) {
                    sloge("savePendingDeviceInfoToSP, phone is null for phone id:" + i);
                } else {
                    String imei = phone.getImei();
                    String deviceSvn = phone.getDeviceSvn();
                    String deviceId = phone.getDeviceId();
                    String meid = phone.getMeid();
                    String esn = phone.getEsn();
                    try {
                        imei = HwAESCryptoUtil.encrypt(HwFullNetworkManager.getInstance().getMasterPassword(), imei);
                        deviceSvn = HwAESCryptoUtil.encrypt(HwFullNetworkManager.getInstance().getMasterPassword(), deviceSvn);
                        deviceId = HwAESCryptoUtil.encrypt(HwFullNetworkManager.getInstance().getMasterPassword(), deviceId);
                        meid = HwAESCryptoUtil.encrypt(HwFullNetworkManager.getInstance().getMasterPassword(), meid);
                        esn = HwAESCryptoUtil.encrypt(HwFullNetworkManager.getInstance().getMasterPassword(), esn);
                    } catch (Exception e) {
                        sloge("encrypt excepiton");
                    }
                    editor.putString(HwVSimUtilsImpl.IMEI_PREF + i, imei);
                    editor.putString(HwVSimUtilsImpl.DEVICE_SVN_PREF + i, deviceSvn);
                    editor.putString(HwVSimUtilsImpl.DEVICE_ID_PREF + i, deviceId);
                    editor.putString(HwVSimUtilsImpl.MEID_PREF + i, meid);
                    editor.putString(HwVSimUtilsImpl.ESN_PREF + i, esn);
                }
            }
            editor.commit();
        }
    }

    public static String getPendingDeviceInfoFromSP(String prefKey, int phoneId) {
        if (!HwVSimUtilsInner.isPlatformTwoModems()) {
            return null;
        }
        Context context = sContext;
        if (context == null) {
            sloge("getPendingDeviceInfoFromSP, sContext is null");
            return null;
        }
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String result = sp.getString(prefKey + phoneId, null);
        try {
            return HwAESCryptoUtil.decrypt(HwFullNetworkManager.getInstance().getMasterPassword(), result);
        } catch (Exception e) {
            sloge("decrypt excepiton");
            return result;
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

    public static void setUnReservedSubCardType(int cardType) {
        if (HwVSimUtilsInner.isPlatformTwoModems()) {
            Settings.System.putInt(sContext.getContentResolver(), VALUE_VSIM_USER_UNRESERVED_SUB_CARDTYPE, cardType);
            slogd("setUnReservedSubCardType: " + cardType);
        }
    }

    public static void setPropPersistRadioSimSlotCfg(int[] slots) {
        if (slots != null) {
            String simSlotCfg = BuildConfig.FLAVOR;
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
                SystemPropertiesEx.set(PROP_PERSIST_RADIO_SIM_SLOT_CFG, simSlotCfg);
                slogd("setPropPersistRadioSimSlotCfg: " + simSlotCfg);
            }
        }
    }

    public static HwVSimNetworkFactory getsVSimNetworkFactory() {
        return sVSimNetworkFactory;
    }

    private void logd(String s) {
        HwVSimLog.VSimLogD(LOG_TAG, s);
    }
}
