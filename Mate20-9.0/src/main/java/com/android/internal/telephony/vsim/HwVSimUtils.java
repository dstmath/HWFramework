package com.android.internal.telephony.vsim;

import android.os.SystemProperties;
import com.android.internal.telephony.HwVSimPhoneFactory;
import com.android.internal.telephony.IccCardConstants;

public class HwVSimUtils {
    public static final String DEVICE_ID_PREF = "deviceId_pending_for_vsim";
    public static final String DEVICE_SVN_PREF = "deviceSvn_pending_for_vsim";
    public static final String ESN_PREF = "esn_pending_for_vsim";
    public static final String IMEI_PREF = "imei_pending_for_vsim";
    private static final String LOG_TAG = "VSimUtils";
    public static final String MEID_PREF = "meid_pending_for_vsim";
    private static final int VSIM_MODEM_COUNT = SystemProperties.getInt("ro.radio.vsim_modem_count", 3);
    private static final int VSIM_MODEM_COUNT_DEFAULT = 3;
    protected static final boolean sIsPlatformSupportVSim = SystemProperties.getBoolean("ro.radio.vsim_support", false);

    private HwVSimUtils() {
    }

    public static boolean isVSimEnabled() {
        if (sIsPlatformSupportVSim && HwVSimController.isInstantiated()) {
            return HwVSimController.getInstance().isVSimEnabled();
        }
        return false;
    }

    public static boolean isVSimOn() {
        if (sIsPlatformSupportVSim && HwVSimController.isInstantiated()) {
            return HwVSimController.getInstance().isVSimOn();
        }
        return false;
    }

    public static boolean isVSimCauseCardReload() {
        if (sIsPlatformSupportVSim && HwVSimController.isInstantiated()) {
            return HwVSimController.getInstance().isVSimCauseCardReload();
        }
        return false;
    }

    public static boolean isAllowALSwitch() {
        if (!sIsPlatformSupportVSim || !HwVSimController.isInstantiated()) {
            return true;
        }
        if (HwVSimPhoneFactory.getVSimEnabledSubId() != -1) {
            return false;
        }
        return !HwVSimController.getInstance().isDoingSlotSwitch();
    }

    public static boolean isPlatformTwoModems() {
        return VSIM_MODEM_COUNT == 2;
    }

    public static boolean isRadioAvailable(int subId) {
        if (sIsPlatformSupportVSim && HwVSimController.isInstantiated()) {
            return HwVSimUtilsInner.isRadioAvailable(subId);
        }
        return true;
    }

    public static boolean needBlockPin(int subId) {
        if (sIsPlatformSupportVSim && HwVSimController.isInstantiated()) {
            return HwVSimController.getInstance().needBlockPin(subId);
        }
        return false;
    }

    public static boolean isVSimInProcess() {
        if (sIsPlatformSupportVSim && HwVSimController.isInstantiated()) {
            return HwVSimController.getInstance().isVSimInProcess();
        }
        return false;
    }

    public static boolean needBlockPinInBoot() {
        if (sIsPlatformSupportVSim && HwVSimController.isInstantiated()) {
            return HwVSimController.getInstance().needBlockPinInBoot();
        }
        return false;
    }

    public static boolean mainSlotPinBusy() {
        if (sIsPlatformSupportVSim && HwVSimController.isInstantiated()) {
            return HwVSimController.getInstance().mainSlotPinBusy();
        }
        return false;
    }

    public static void processHotPlug(int[] cardTypes) {
        if (sIsPlatformSupportVSim && HwVSimController.isInstantiated()) {
            HwVSimController.getInstance().processHotPlug(cardTypes);
        }
    }

    public static IccCardConstants.State modifySimStateForVsim(int subId, IccCardConstants.State s) {
        if (sIsPlatformSupportVSim && HwVSimController.isInstantiated()) {
            return HwVSimController.getInstance().modifySimStateForVsim(subId, s);
        }
        return s;
    }

    public static boolean isPlatformRealTripple() {
        if (sIsPlatformSupportVSim && HwVSimController.isInstantiated()) {
            return HwVSimUtilsInner.isPlatformRealTripple();
        }
        return false;
    }

    public static void updateSubState(int subId, int subState) {
        if (sIsPlatformSupportVSim && HwVSimController.isInstantiated()) {
            HwVSimController.getInstance().updateSubState(subId, subState);
        }
    }

    public static void setSubActived(int subId) {
        if (sIsPlatformSupportVSim && HwVSimController.isInstantiated()) {
            HwVSimController.getInstance().setSubActived(subId);
        }
    }

    public static void updateSimCardTypes(int[] cardTypes) {
        if (sIsPlatformSupportVSim && HwVSimController.isInstantiated()) {
            HwVSimController.getInstance().updateSimCardTypes(cardTypes);
        }
    }

    public static boolean isSubActivationUpdate() {
        if (sIsPlatformSupportVSim && HwVSimController.isInstantiated()) {
            return HwVSimController.getInstance().isSubActivationUpdate();
        }
        return false;
    }

    public static void processAutoSetPowerupModemDone() {
        if (sIsPlatformSupportVSim && HwVSimController.isInstantiated()) {
            HwVSimController.getInstance().processAutoSetPowerupModemDone();
        }
    }

    public static boolean prohibitSubUpdateSimNoChange(int subId) {
        if (sIsPlatformSupportVSim && HwVSimController.isInstantiated() && HwVSimUtilsInner.isPlatformRealTripple() && HwVSimUtilsInner.IS_DSDSPOWER_SUPPORT) {
            return HwVSimController.getInstance().prohibitSubUpdateSimNoChange(subId);
        }
        return false;
    }

    public static boolean needBlockUnReservedForVsim(int subId) {
        if (sIsPlatformSupportVSim && HwVSimController.isInstantiated()) {
            return HwVSimController.getInstance().needBlockUnReservedForVsim(subId);
        }
        return false;
    }

    public static boolean getIsWaitingSwitchCdmaModeSide() {
        if (sIsPlatformSupportVSim && HwVSimController.isInstantiated()) {
            return HwVSimController.getInstance().getIsWaitingSwitchCdmaModeSide();
        }
        return false;
    }

    public static boolean getIsWaitingNvMatchUnsol() {
        if (sIsPlatformSupportVSim && HwVSimController.isInstantiated() && HwVSimUtilsInner.isPlatformNeedWaitNvMatchUnsol()) {
            return HwVSimController.getInstance().getIsWaitingNvMatchUnsol();
        }
        return false;
    }

    public static boolean isVSimDsdsVersionOne() {
        if (sIsPlatformSupportVSim && HwVSimController.isInstantiated()) {
            return HwVSimUtilsInner.isVSimDsdsVersionOne();
        }
        return false;
    }

    public static void simHotPlugOut(int slotId) {
        if (sIsPlatformSupportVSim && HwVSimController.isInstantiated()) {
            HwVSimController.getInstance().simHotPlugOut(slotId);
        }
    }

    public static void simHotPlugIn(int slotId) {
        if (sIsPlatformSupportVSim && HwVSimController.isInstantiated()) {
            HwVSimController.getInstance().simHotPlugIn(slotId);
        }
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
}
