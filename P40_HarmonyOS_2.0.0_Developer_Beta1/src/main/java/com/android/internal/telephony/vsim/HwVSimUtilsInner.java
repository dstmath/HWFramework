package com.android.internal.telephony.vsim;

import android.content.Context;
import android.os.Message;
import android.telephony.HwTelephonyManagerInner;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.HwVSimPhoneFactory;
import com.android.internal.telephony.fullnetwork.HwFullNetworkManager;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.internal.telephony.CommandExceptionEx;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.vsim.HwVSimBaseController;
import com.huawei.internal.telephony.vsim.HwVSimControllerGetter;

public class HwVSimUtilsInner {
    public static final int ACTIVE_MODEM_MODE_DUAL = 1;
    public static final int ACTIVE_MODEM_MODE_SINGLE = 0;
    public static final int DISABLE = 0;
    public static final int DOMAIN_CS_ONLY = 0;
    public static final int ENABLE = 1;
    private static final int INVALID = -1;
    public static final boolean IS_DSDSPOWER_SUPPORT = SystemPropertiesEx.getBoolean("ro.config.hw_dsdspowerup", false);
    private static final String LOG_TAG = "VSimUtilsInner";
    private static final int LTE_SERVICE_OFF = 0;
    private static final int LTE_SERVICE_ON = 1;
    public static final int NETWORK_MODE_CDMA_GSM = 59;
    private static final String PROPERTY_DYNAMIC_START_STOP = "persist.hw_mc.telephony.vsim_dynamic_start_stop";
    private static final String PROPERTY_VSIM_DSDS_VERSION = "ro.radio.vsim_dsds_version";
    public static final int SIM = 1;
    public static final int STATE_ON = 1;
    public static final int UE_OPERATION_MODE_DATA_CENTRIC = 1;
    public static final int UE_OPERATION_MODE_VOICE_CENTRIC = 0;
    public static final int VSIM = 11;
    private static final int VSIM_DSDS_VERSION_DEFAULT = 0;
    private static final int VSIM_DSDS_VERSION_ONE = 1;
    private static final int VSIM_DSDS_VERSION_PROP = SystemPropertiesEx.getInt(PROPERTY_VSIM_DSDS_VERSION, 0);
    private static final int VSIM_MODEM_COUNT = SystemPropertiesEx.getInt("ro.radio.vsim_modem_count", 3);
    private static final int VSIM_MODEM_COUNT_DEFAULT = 3;
    private static final int VSIM_MODEM_COUNT_DUAL = 2;
    private static final int VSIM_MODEM_COUNT_FAKE_TRIPPLE = 3;
    private static final int VSIM_MODEM_COUNT_REAL_TRIPPLE = 4;
    public static final int VSIM_NETWORK_MODE_UNKNOWN = -1;
    public static final int VSIM_SIM_STATE_INVALID = -1;
    public static final int VSIM_SUB_INVALID = -1;
    private static final boolean VSIM_SUPPORT_DYNAMIC_START_STOP = SystemPropertiesEx.getBoolean(PROPERTY_DYNAMIC_START_STOP, true);

    private static void slogi(String log) {
        HwVSimLog.info(LOG_TAG, log);
    }

    private static void sloge(String log) {
        HwVSimLog.error(LOG_TAG, log);
    }

    public static int getInsertedCardCount(int[] cardTypes) {
        if (cardTypes == null) {
            sloge("getInsertedCardCount, cardTypes is null !");
            return -1;
        } else if (cardTypes.length == 0) {
            slogi("getInsertedCardCount, cardTypes.length == 0 !");
            return -1;
        } else {
            int insertedCardCount = 0;
            for (int i : cardTypes) {
                if (i != 0) {
                    insertedCardCount++;
                }
            }
            return insertedCardCount;
        }
    }

    public static boolean[] getCardState(int[] cardTypes) {
        if (cardTypes == null) {
            sloge("getCardState, cardTypes is null !");
            return null;
        } else if (cardTypes.length == 0) {
            slogi("getCardState, cardTypes.length == 0 !");
            return null;
        } else {
            boolean[] isCardPresents = new boolean[cardTypes.length];
            for (int i = 0; i < cardTypes.length; i++) {
                isCardPresents[i] = cardTypes[i] != 0;
            }
            return isCardPresents;
        }
    }

    public static CommandsInterfaceEx getCiBySub(int slotId, CommandsInterfaceEx vsimCi, CommandsInterfaceEx[] cis) {
        if (slotId < 0 || slotId > cis.length) {
            return null;
        }
        if (slotId == cis.length) {
            return vsimCi;
        }
        return cis[slotId];
    }

    public static PhoneExt getPhoneBySub(int slotId, PhoneExt vsimPhone, PhoneExt[] phones) {
        if (slotId < 0 || slotId > 2) {
            return null;
        }
        if (slotId == 2) {
            return vsimPhone;
        }
        return phones[slotId];
    }

    public static Integer getCiIndex(Message msg) {
        if (msg == null) {
            return 0;
        }
        if (msg.obj instanceof Integer) {
            return (Integer) msg.obj;
        }
        if (msg.obj == null) {
            return 0;
        }
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (ar.getUserObj() == null || !(ar.getUserObj() instanceof Integer)) {
            return 0;
        }
        return (Integer) ar.getUserObj();
    }

    public static boolean isRequestNotSupport(Throwable ex) {
        return CommandExceptionEx.isSpecificError(ex, CommandExceptionEx.Error.REQUEST_NOT_SUPPORTED);
    }

    public static boolean isChinaTelecom() {
        return HuaweiTelephonyConfigs.isChinaTelecom();
    }

    public static boolean isFullNetworkSupported() {
        return SystemPropertiesEx.getBoolean("ro.config.full_network_support", false);
    }

    public static int getAnotherSlotId(int subId) {
        return subId == 0 ? 1 : 0;
    }

    public static int getSinIndexBySlotId(int subId) {
        return subId == 2 ? 11 : 1;
    }

    public static boolean isValidSlotId(int subId) {
        return subId >= 0 && subId < HwVSimModemAdapter.PHONE_COUNT;
    }

    public static boolean isPlatformTwoModems() {
        return VSIM_MODEM_COUNT == 2;
    }

    public static boolean isPlatformRealTripple() {
        return VSIM_MODEM_COUNT == 4;
    }

    public static boolean isVSimDsdsVersionOne() {
        return VSIM_DSDS_VERSION_PROP == 1;
    }

    public static boolean isPlatformTwoModemsActual() {
        return isPlatformTwoModems() || (isPlatformRealTripple() && isVSimDsdsVersionOne());
    }

    public static boolean isDualImsSupported() {
        return HwTelephonyManagerInner.getDefault().isDualImsSupported();
    }

    public static boolean isPlatformNeedWaitNvMatchUnsol() {
        return HwTelephonyManagerInner.getDefault().isDualImsSupported();
    }

    public static int[] createSimSlotsTable(int m0, int m1, int m2) {
        int[] slots = new int[HwVSimModemAdapter.MAX_SUB_COUNT];
        slots[0] = m0;
        slots[1] = m1;
        slots[2] = m2;
        return slots;
    }

    public static boolean isRadioAvailable(int slotId) {
        HwVSimBaseController controller = HwVSimControllerGetter.get();
        if (!HwVSimControllerGetter.valid(controller)) {
            return true;
        }
        if (!HuaweiTelephonyConfigs.isHisiPlatform() || !HwVSimUtilsImpl.getInstance().isPlatformTwoModems()) {
            CommandsInterfaceEx ci = controller.getCiBySub(slotId);
            if (ci == null || !ci.isRadioAvailable()) {
                return false;
            }
            return true;
        } else if (controller.isVSimEnabled()) {
            if (controller.getSimSlotTableLastSlotId() != slotId) {
                return true;
            }
            return false;
        } else if (slotId != 2) {
            return true;
        } else {
            return false;
        }
    }

    public static int getNetworkTypeInModem1ForCmcc(int defaultNetworkType) {
        if (!HwFullNetworkManager.getInstance().isCMCCDsdxDisable()) {
            return defaultNetworkType;
        }
        if (isDualImsSupported()) {
            return 9;
        }
        return 3;
    }

    public static boolean isDualImsSwitchOpened() {
        return SystemPropertiesEx.getInt("persist.radio.dualltecap", 0) == 1;
    }

    public static void setDefaultMobileEnableForVSim(boolean isEnabled) {
        PhoneExt vsimPhone = HwVSimPhoneFactory.getVSimPhone();
        if (vsimPhone != null && vsimPhone.getDcTracker() != null) {
            vsimPhone.getDcTracker().setEnabledPublic(0, isEnabled);
            slogi("setDefaultMobileEnable to " + isEnabled + " for vsimPhone");
        }
    }

    public static boolean isNrServiceAbilityOn(int networkType) {
        return networkType >= 64 && networkType <= 69;
    }

    public static boolean isVsimEnabledByDatabase(Context context) {
        return HwVSimPhoneFactory.getVSimEnabledSubId(context) != -1;
    }

    public static boolean isSupportedVsimDynamicStartStop() {
        return VSIM_SUPPORT_DYNAMIC_START_STOP;
    }

    public static boolean isMtkPlatformTwoModems() {
        return HuaweiTelephonyConfigs.isMTKPlatform() && VSIM_MODEM_COUNT == 2;
    }

    public static int convertSlotId(int slotId) {
        if (slotId == 0) {
            return 1;
        }
        if (slotId == 1) {
            return 2;
        }
        return 3;
    }
}
