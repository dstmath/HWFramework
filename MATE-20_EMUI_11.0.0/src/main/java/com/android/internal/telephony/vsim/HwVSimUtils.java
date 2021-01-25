package com.android.internal.telephony.vsim;

import android.util.Log;
import com.android.internal.telephony.HwPartOptTelephonyFactory;
import com.huawei.internal.telephony.IccCardConstantsEx;

public class HwVSimUtils {
    private static final String TAG = "HwVSimUtils";
    private static DefaultHwVSimUtils sHwVSimUtils = HwPartOptTelephonyFactory.getTelephonyFactory().getVSimFactory().getHwVSimUtils();
    private static HwVSimUtils sInstance;

    static {
        Log.d(TAG, "add " + sHwVSimUtils.getClass().getCanonicalName() + " to memory");
    }

    private HwVSimUtils() {
    }

    public static HwVSimUtils getInstance() {
        if (sInstance == null) {
            sInstance = new HwVSimUtils();
        }
        return sInstance;
    }

    public static boolean isVSimSupported() {
        return sHwVSimUtils.isVSimSupported();
    }

    public static boolean getIsWaitingSwitchCdmaModeSide() {
        return sHwVSimUtils.getIsWaitingSwitchCdmaModeSide();
    }

    public static boolean getIsWaitingNvMatchUnsol() {
        return sHwVSimUtils.getIsWaitingNvMatchUnsol();
    }

    public static boolean isVSimEnabled() {
        return sHwVSimUtils.isVSimEnabled();
    }

    public static boolean isVSimOn() {
        return sHwVSimUtils.isVSimOn();
    }

    public static boolean isVSimInProcess() {
        return sHwVSimUtils.isVSimInProcess();
    }

    public static boolean isAllowALSwitch() {
        return sHwVSimUtils.isAllowALSwitch();
    }

    public static boolean isVSimCauseCardReload() {
        return sHwVSimUtils.isVSimCauseCardReload();
    }

    public static boolean isPlatformRealTripple() {
        return sHwVSimUtils.isPlatformRealTripple();
    }

    public static boolean isPlatformTwoModems() {
        return sHwVSimUtils.isPlatformTwoModems();
    }

    public static boolean isVSimDsdsVersionOne() {
        return sHwVSimUtils.isVSimDsdsVersionOne();
    }

    public static boolean isSubActivationUpdate() {
        return sHwVSimUtils.isSubActivationUpdate();
    }

    public static boolean isRadioAvailable(int slotId) {
        return sHwVSimUtils.isRadioAvailable(slotId);
    }

    public static boolean isVSimSub(int slotId) {
        return sHwVSimUtils.isVSimSub(slotId);
    }

    public static IccCardConstantsEx.StateEx modifySimStateForVsim(int phoneId, IccCardConstantsEx.StateEx s) {
        return sHwVSimUtils.modifySimStateForVsim(phoneId, s);
    }

    public static boolean needBlockUnReservedForVsim(int slotId) {
        return sHwVSimUtils.needBlockUnReservedForVsim(slotId);
    }

    public static void processAutoSetPowerupModemDone() {
        sHwVSimUtils.processAutoSetPowerupModemDone();
    }

    public static void processHotPlug(int[] cardTypes) {
        sHwVSimUtils.processHotPlug(cardTypes);
    }

    public static boolean prohibitSubUpdateSimNoChange(int slotId) {
        return sHwVSimUtils.prohibitSubUpdateSimNoChange(slotId);
    }

    public static void restartRildIfIdle() {
        sHwVSimUtils.restartRildIfIdle();
    }

    public static void simHotPlugOut(int slotId) {
        sHwVSimUtils.simHotPlugOut(slotId);
    }

    public static void simHotPlugIn(int slotId) {
        sHwVSimUtils.simHotPlugIn(slotId);
    }

    public static void storeIfNeedRestartRildForNvMatch(boolean isNeed) {
        sHwVSimUtils.storeIfNeedRestartRildForNvMatch(isNeed);
    }

    public static void setSubActived(int slotId) {
        sHwVSimUtils.setSubActived(slotId);
    }

    public static void updateSimCardTypes(int[] cardTypes) {
        sHwVSimUtils.updateSimCardTypes(cardTypes);
    }

    public static void updateSubState(int slotId, int subState) {
        sHwVSimUtils.updateSubState(slotId, subState);
    }

    public static void setDefaultMobileEnableForVSim(boolean isEnabled) {
        sHwVSimUtils.setDefaultMobileEnableForVSim(isEnabled);
    }
}
