package com.android.internal.telephony.vsim;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.SystemProperties;
import android.telephony.SubscriptionManager;
import android.telephony.data.ApnSetting;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneNotifier;
import com.android.internal.telephony.ServiceStateTracker;
import com.android.internal.telephony.uicc.IccCardStatus;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.PhoneNotifierEx;
import com.huawei.internal.telephony.uicc.IccRecordsEx;
import com.huawei.internal.telephony.uicc.UiccCardApplicationEx;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

public class VSimUtilsInner {
    public static final String DEVICE_ID_PREF = "deviceId_pending_for_vsim";
    public static final String DEVICE_SVN_PREF = "deviceSvn_pending_for_vsim";
    public static final String ESN_PREF = "esn_pending_for_vsim";
    public static final String IMEI_PREF = "imei_pending_for_vsim";
    private static final boolean IS_PLATFORM_SUPPORT_VSIM = SystemProperties.getBoolean("ro.radio.vsim_support", false);
    public static final int IS_RADIO_AVAILABLE = 7;
    private static final int IS_VSIM_CAUSE_CARD_RELOAD = 8;
    private static final int IS_VSIM_ENABLED = 9;
    private static final int IS_VSIM_IN_PROCESS = 2;
    private static final int IS_VSIM_ON = 1;
    public static final String MEID_PREF = "meid_pending_for_vsim";
    private static final int NEED_BLOCK_PIN = 10;
    private static final int NEED_BLOCK_UNRESERVED_SUBID = 11;
    private static final int VSIM_MODEM_COUNT = SystemProperties.getInt("ro.radio.vsim_modem_count", 3);
    private static final int VSIM_MODEM_COUNT_DEFAULT = 3;
    private static final int VSIM_MODEM_COUNT_DUAL = 2;
    private static final int VSIM_MODEM_COUNT_FAKE_TRIPPLE = 3;
    private static final int VSIM_MODEM_COUNT_REAL_TRIPPLE = 4;

    private VSimUtilsInner() {
    }

    public static boolean isPlatformTwoModems() {
        if (IS_PLATFORM_SUPPORT_VSIM && VSIM_MODEM_COUNT == 2) {
            return true;
        }
        return false;
    }

    public static boolean isPlatformRealTripple() {
        if (IS_PLATFORM_SUPPORT_VSIM && VSIM_MODEM_COUNT == 4) {
            return true;
        }
        return false;
    }

    public static boolean isHisiVSimSlot(int slotId) {
        return HuaweiTelephonyConfigs.isHisiPlatform() && slotId == 2;
    }

    public static boolean isVSimSlot(int slotId) {
        if (IS_PLATFORM_SUPPORT_VSIM && slotId != -1 && HwTelephonyFactory.getHwInnerVSimManager().getVSimSlot() == slotId) {
            return true;
        }
        return false;
    }

    public static boolean isVSimOn() {
        if (!IS_PLATFORM_SUPPORT_VSIM) {
            return false;
        }
        return HwTelephonyFactory.getHwInnerVSimManager().isVSimInStatus(1, -1);
    }

    public static boolean isVSimInProcess() {
        if (!IS_PLATFORM_SUPPORT_VSIM) {
            return false;
        }
        return HwTelephonyFactory.getHwInnerVSimManager().isVSimInStatus(2, -1);
    }

    public static void registerForIccChanged(Handler h, int what, Object obj) {
        if (IS_PLATFORM_SUPPORT_VSIM) {
            HwTelephonyFactory.getHwInnerVSimManager().registerForIccChanged(h, what, obj);
        }
    }

    public static void unregisterForIccChanged(Handler h) {
        if (IS_PLATFORM_SUPPORT_VSIM) {
            HwTelephonyFactory.getHwInnerVSimManager().unregisterForIccChanged(h);
        }
    }

    public static boolean isVSimPhone(Phone phone) {
        if (IS_PLATFORM_SUPPORT_VSIM && phone != null && isVSimSlot(phone.getPhoneId())) {
            return true;
        }
        return false;
    }

    public static IccRecords fetchVSimIccRecords(int family) {
        IccRecordsEx iccRecordsEx;
        if (IS_PLATFORM_SUPPORT_VSIM && (iccRecordsEx = HwTelephonyFactory.getHwInnerVSimManager().fetchVSimIccRecords(family)) != null) {
            return iccRecordsEx.getIccRecords();
        }
        return null;
    }

    public static UiccCardApplication getVSimUiccCardApplication(int appFamily) {
        UiccCardApplicationEx uiccCardApplicationEx;
        if (IS_PLATFORM_SUPPORT_VSIM && (uiccCardApplicationEx = HwTelephonyFactory.getHwInnerVSimManager().getVSimUiccCardApplication(appFamily)) != null) {
            return uiccCardApplicationEx.getUiccCardApplication();
        }
        return null;
    }

    public static ArrayList<ApnSetting> createVSimApnList() {
        return HwTelephonyFactory.getHwInnerVSimManager().createVSimApnList();
    }

    public static boolean isVSimFiltrateApn(int subId, int type) {
        if (subId != 999999 || type == 0 || 3 == type || 4 == type || 5 == type) {
            return false;
        }
        return true;
    }

    public static void setMarkForCardReload(int subId, boolean value) {
        if (IS_PLATFORM_SUPPORT_VSIM) {
            HwTelephonyFactory.getHwInnerVSimManager().setMarkForCardReload(subId, value);
        }
    }

    public static boolean isRadioAvailable(int subId) {
        if (!IS_PLATFORM_SUPPORT_VSIM) {
            return true;
        }
        return HwTelephonyFactory.getHwInnerVSimManager().isVSimInStatus(7, subId);
    }

    public static void makeVSimPhoneFactory(Context context, PhoneNotifier notifier, Phone[] pps, CommandsInterface[] cis) {
        if (IS_PLATFORM_SUPPORT_VSIM) {
            PhoneNotifierEx phoneNotifierEx = new PhoneNotifierEx();
            phoneNotifierEx.setPhoneNotifier(notifier);
            HwTelephonyFactory.getHwInnerVSimManager().makeVSimPhoneFactory(context, phoneNotifierEx, PhoneExt.getPhoneExts(pps), CommandsInterfaceEx.getCommandsInterfaceExs(cis));
        }
    }

    public static Phone getVSimPhone() {
        PhoneExt phoneExt = HwTelephonyFactory.getHwInnerVSimManager().getVSimPhone();
        if (phoneExt != null) {
            return phoneExt.getPhone();
        }
        return null;
    }

    public static void dumpVSimPhoneFactory(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (IS_PLATFORM_SUPPORT_VSIM) {
            HwTelephonyFactory.getHwInnerVSimManager().dumpVSimPhoneFactory(fd, pw, args);
        }
    }

    public static ServiceStateTracker makeVSimServiceStateTracker(Phone phone, CommandsInterface ci) {
        if (!IS_PLATFORM_SUPPORT_VSIM) {
            return null;
        }
        PhoneExt phoneExt = new PhoneExt();
        phoneExt.setPhone(phone);
        CommandsInterfaceEx commandsInterfaceEx = new CommandsInterfaceEx();
        commandsInterfaceEx.setCommandsInterface(ci);
        return HwTelephonyFactory.getHwInnerVSimManager().makeVSimServiceStateTracker(phoneExt, commandsInterfaceEx).getServiceStateTracker();
    }

    public static boolean isVSimCauseCardReload() {
        if (!IS_PLATFORM_SUPPORT_VSIM) {
            return false;
        }
        return HwTelephonyFactory.getHwInnerVSimManager().isVSimInStatus(8, -1);
    }

    public static String getPendingDeviceInfoFromSP(String prefKey) {
        if (!IS_PLATFORM_SUPPORT_VSIM) {
            return null;
        }
        return HwTelephonyFactory.getHwInnerVSimManager().getPendingDeviceInfoFromSP(prefKey);
    }

    public static boolean isVSimEnabled() {
        if (!IS_PLATFORM_SUPPORT_VSIM) {
            return false;
        }
        return HwTelephonyFactory.getHwInnerVSimManager().isVSimInStatus(9, -1);
    }

    public static boolean needBlockPin(int subId) {
        if (!IS_PLATFORM_SUPPORT_VSIM) {
            return false;
        }
        return HwTelephonyFactory.getHwInnerVSimManager().isVSimInStatus(10, subId);
    }

    public static boolean needBlockUnReservedForVsim(int subId) {
        if (!IS_PLATFORM_SUPPORT_VSIM) {
            return false;
        }
        return HwTelephonyFactory.getHwInnerVSimManager().isVSimInStatus(11, subId);
    }

    public static void putVSimExtraForIccStateChanged(Intent intent, int subId, String value) {
        if (IS_PLATFORM_SUPPORT_VSIM) {
            HwTelephonyFactory.getHwInnerVSimManager().putVSimExtraForIccStateChanged(intent, subId, value);
        }
    }

    public static int getTopPrioritySubscriptionId() {
        if (!IS_PLATFORM_SUPPORT_VSIM) {
            return SubscriptionManager.getDefaultDataSubscriptionId();
        }
        return HwTelephonyFactory.getHwInnerVSimManager().getTopPrioritySubscriptionId();
    }

    public static void disposeSSTForVSim() {
        if (IS_PLATFORM_SUPPORT_VSIM) {
            HwTelephonyFactory.getHwInnerVSimManager().disposeSSTForVSim();
        }
    }

    public static String changeSpnForVSim(String spn) {
        if (!IS_PLATFORM_SUPPORT_VSIM) {
            return spn;
        }
        return HwTelephonyFactory.getHwInnerVSimManager().changeSpnForVSim(spn);
    }

    public static int changeRuleForVSim(int rule) {
        if (!IS_PLATFORM_SUPPORT_VSIM) {
            return rule;
        }
        return HwTelephonyFactory.getHwInnerVSimManager().changeRuleForVSim(rule);
    }

    public static IccCardStatus.CardState getVSimCardState() {
        if (!IS_PLATFORM_SUPPORT_VSIM) {
            return IccCardStatus.CardState.CARDSTATE_ABSENT;
        }
        return HwTelephonyFactory.getHwInnerVSimManager().getVSimCardState().getValue();
    }

    public static int getVSimSubId() {
        return 999999;
    }

    public static int getVSimPhoneId(int subId, int defaultPhoneId) {
        return subId == getVSimSubId() ? HwTelephonyFactory.getHwInnerVSimManager().getVSimSlot() : defaultPhoneId;
    }
}
