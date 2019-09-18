package com.android.internal.telephony.vsim;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.SystemProperties;
import android.telephony.SubscriptionManager;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneNotifier;
import com.android.internal.telephony.ServiceStateTracker;
import com.android.internal.telephony.dataconnection.ApnSetting;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccCardApplication;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

public class VSimUtilsInner {
    public static final String DEVICE_ID_PREF = "deviceId_pending_for_vsim";
    public static final String DEVICE_SVN_PREF = "deviceSvn_pending_for_vsim";
    public static final String ESN_PREF = "esn_pending_for_vsim";
    public static final String IMEI_PREF = "imei_pending_for_vsim";
    private static final int IS_RADIO_AVAILABLE = 7;
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
    private static final boolean sIsPlatformSupportVSim = SystemProperties.getBoolean("ro.radio.vsim_support", false);

    private VSimUtilsInner() {
    }

    public static boolean isPlatformTwoModems() {
        boolean z = false;
        if (!sIsPlatformSupportVSim) {
            return false;
        }
        if (VSIM_MODEM_COUNT == 2) {
            z = true;
        }
        return z;
    }

    public static boolean isPlatformRealTripple() {
        boolean z = false;
        if (!sIsPlatformSupportVSim) {
            return false;
        }
        if (VSIM_MODEM_COUNT == 4) {
            z = true;
        }
        return z;
    }

    public static boolean isVSimSub(int sub) {
        boolean z = false;
        if (!sIsPlatformSupportVSim) {
            return false;
        }
        if (sub == 2) {
            z = true;
        }
        return z;
    }

    public static boolean isVSimOn() {
        if (!sIsPlatformSupportVSim) {
            return false;
        }
        return HwTelephonyFactory.getHwInnerVSimManager().isVSimInStatus(1, -1);
    }

    public static boolean isVSimInProcess() {
        if (!sIsPlatformSupportVSim) {
            return false;
        }
        return HwTelephonyFactory.getHwInnerVSimManager().isVSimInStatus(2, -1);
    }

    public static void registerForIccChanged(Handler h, int what, Object obj) {
        if (sIsPlatformSupportVSim) {
            HwTelephonyFactory.getHwInnerVSimManager().registerForIccChanged(h, what, obj);
        }
    }

    public static void unregisterForIccChanged(Handler h) {
        if (sIsPlatformSupportVSim) {
            HwTelephonyFactory.getHwInnerVSimManager().unregisterForIccChanged(h);
        }
    }

    public static boolean isVSimPhone(Phone phone) {
        if (!sIsPlatformSupportVSim) {
            return false;
        }
        return HwTelephonyFactory.getHwInnerVSimManager().isVSimPhone(phone);
    }

    public static IccRecords fetchVSimIccRecords(int family) {
        return HwTelephonyFactory.getHwInnerVSimManager().fetchVSimIccRecords(family);
    }

    public static UiccCardApplication getVSimUiccCardApplication(int appFamily) {
        return HwTelephonyFactory.getHwInnerVSimManager().getVSimUiccCardApplication(appFamily);
    }

    public static ArrayList<ApnSetting> createVSimApnList() {
        return HwTelephonyFactory.getHwInnerVSimManager().createVSimApnList();
    }

    public static boolean isVSimFiltrateApn(int subId, int type) {
        if (subId != 2 || type == 0 || 3 == type || 4 == type || 5 == type) {
            return false;
        }
        return true;
    }

    public static void setMarkForCardReload(int subId, boolean value) {
        if (sIsPlatformSupportVSim) {
            HwTelephonyFactory.getHwInnerVSimManager().setMarkForCardReload(subId, value);
        }
    }

    public static boolean isRadioAvailable(int subId) {
        if (!sIsPlatformSupportVSim) {
            return true;
        }
        return HwTelephonyFactory.getHwInnerVSimManager().isVSimInStatus(7, subId);
    }

    public static void makeVSimPhoneFactory(Context context, PhoneNotifier notifier, Phone[] pps, CommandsInterface[] cis) {
        if (sIsPlatformSupportVSim) {
            HwTelephonyFactory.getHwInnerVSimManager().makeVSimPhoneFactory(context, notifier, pps, cis);
        }
    }

    public static Phone getVSimPhone() {
        return HwTelephonyFactory.getHwInnerVSimManager().getVSimPhone();
    }

    public static void dumpVSimPhoneFactory(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (sIsPlatformSupportVSim) {
            HwTelephonyFactory.getHwInnerVSimManager().dumpVSimPhoneFactory(fd, pw, args);
        }
    }

    public static ServiceStateTracker makeVSimServiceStateTracker(Phone phone, CommandsInterface ci) {
        if (!sIsPlatformSupportVSim) {
            return null;
        }
        return HwTelephonyFactory.getHwInnerVSimManager().makeVSimServiceStateTracker(phone, ci);
    }

    public static boolean isVSimCauseCardReload() {
        if (!sIsPlatformSupportVSim) {
            return false;
        }
        return HwTelephonyFactory.getHwInnerVSimManager().isVSimInStatus(8, -1);
    }

    public static String getPendingDeviceInfoFromSP(String prefKey) {
        if (!sIsPlatformSupportVSim) {
            return null;
        }
        return HwTelephonyFactory.getHwInnerVSimManager().getPendingDeviceInfoFromSP(prefKey);
    }

    public static boolean isVSimEnabled() {
        if (!sIsPlatformSupportVSim) {
            return false;
        }
        return HwTelephonyFactory.getHwInnerVSimManager().isVSimInStatus(9, -1);
    }

    public static boolean needBlockPin(int subId) {
        if (!sIsPlatformSupportVSim) {
            return false;
        }
        return HwTelephonyFactory.getHwInnerVSimManager().isVSimInStatus(10, subId);
    }

    public static boolean needBlockUnReservedForVsim(int subId) {
        if (!sIsPlatformSupportVSim) {
            return false;
        }
        return HwTelephonyFactory.getHwInnerVSimManager().isVSimInStatus(11, subId);
    }

    public static void putVSimExtraForIccStateChanged(Intent intent, int subId, String value) {
        if (sIsPlatformSupportVSim) {
            HwTelephonyFactory.getHwInnerVSimManager().putVSimExtraForIccStateChanged(intent, subId, value);
        }
    }

    public static int getTopPrioritySubscriptionId() {
        if (!sIsPlatformSupportVSim) {
            return SubscriptionManager.getDefaultDataSubscriptionId();
        }
        return HwTelephonyFactory.getHwInnerVSimManager().getTopPrioritySubscriptionId();
    }
}
