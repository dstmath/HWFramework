package com.android.internal.telephony.vsim;

import com.huawei.internal.telephony.IccCardConstantsEx;

public class DefaultHwVSimUtils {
    private static DefaultHwVSimUtils sInstance = new DefaultHwVSimUtils();

    public static DefaultHwVSimUtils getInstance() {
        if (sInstance == null) {
            sInstance = new DefaultHwVSimUtils();
        }
        return sInstance;
    }

    public boolean getIsWaitingSwitchCdmaModeSide() {
        return false;
    }

    public boolean getIsWaitingNvMatchUnsol() {
        return false;
    }

    public boolean isVSimEnabled() {
        return false;
    }

    public boolean isVSimOn() {
        return false;
    }

    public boolean isVSimInProcess() {
        return false;
    }

    public boolean isAllowALSwitch() {
        return true;
    }

    public boolean isVSimCauseCardReload() {
        return false;
    }

    public boolean isPlatformRealTripple() {
        return false;
    }

    public boolean isPlatformTwoModems() {
        return false;
    }

    public boolean isVSimDsdsVersionOne() {
        return false;
    }

    public boolean isSubActivationUpdate() {
        return false;
    }

    public boolean isRadioAvailable(int slotId) {
        return true;
    }

    public boolean isVSimSub(int slotId) {
        return false;
    }

    public IccCardConstantsEx.StateEx modifySimStateForVsim(int phoneId, IccCardConstantsEx.StateEx s) {
        return s;
    }

    public boolean needBlockUnReservedForVsim(int slotId) {
        return false;
    }

    public void processAutoSetPowerupModemDone() {
    }

    public void processHotPlug(int[] cardTypes) {
    }

    public boolean prohibitSubUpdateSimNoChange(int slotId) {
        return false;
    }

    public void restartRildIfIdle() {
    }

    public void simHotPlugOut(int slotId) {
    }

    public void simHotPlugIn(int slotId) {
    }

    public void storeIfNeedRestartRildForNvMatch(boolean isNeed) {
    }

    public void setSubActived(int slotId) {
    }

    public void updateSimCardTypes(int[] cardTypes) {
    }

    public void updateSubState(int slotId, int subState) {
    }

    public void setDefaultMobileEnableForVSim(boolean enable) {
    }
}
