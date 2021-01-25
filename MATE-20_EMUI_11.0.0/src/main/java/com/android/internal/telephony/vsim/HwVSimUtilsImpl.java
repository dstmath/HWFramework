package com.android.internal.telephony.vsim;

import com.android.internal.telephony.HwVSimPhoneFactory;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.internal.telephony.IccCardConstantsEx;

public class HwVSimUtilsImpl extends DefaultHwVSimUtils {
    public static final String DEVICE_ID_PREF = "deviceId_pending_for_vsim";
    public static final String DEVICE_SVN_PREF = "deviceSvn_pending_for_vsim";
    public static final String ESN_PREF = "esn_pending_for_vsim";
    public static final String IMEI_PREF = "imei_pending_for_vsim";
    private static final String LOG_TAG = "VSimUtils";
    public static final String MEID_PREF = "meid_pending_for_vsim";
    private static final int VSIM_MODEM_COUNT = SystemPropertiesEx.getInt("ro.radio.vsim_modem_count", 3);
    private static final int VSIM_MODEM_COUNT_DEFAULT = 3;
    private static HwVSimUtilsImpl sInstance;
    protected static final boolean sIsPlatformSupportVSim = SystemPropertiesEx.getBoolean("ro.radio.vsim_support", false);

    private HwVSimUtilsImpl() {
    }

    public static synchronized HwVSimUtilsImpl getInstance() {
        HwVSimUtilsImpl hwVSimUtilsImpl;
        synchronized (HwVSimUtilsImpl.class) {
            if (sInstance == null) {
                sInstance = new HwVSimUtilsImpl();
            }
            hwVSimUtilsImpl = sInstance;
        }
        return hwVSimUtilsImpl;
    }

    public boolean isVSimSupported() {
        return true;
    }

    public boolean isVSimEnabled() {
        if (sIsPlatformSupportVSim && HwVSimController.isInstantiated()) {
            return HwVSimController.getInstance().isVSimEnabled();
        }
        return false;
    }

    public boolean isVSimOn() {
        if (sIsPlatformSupportVSim && HwVSimController.isInstantiated()) {
            return HwVSimController.getInstance().isVSimOn();
        }
        return false;
    }

    public boolean isVSimCauseCardReload() {
        if (sIsPlatformSupportVSim && HwVSimController.isInstantiated()) {
            return HwVSimController.getInstance().isVSimCauseCardReload();
        }
        return false;
    }

    public boolean isAllowALSwitch() {
        if (!sIsPlatformSupportVSim || !HwVSimController.isInstantiated()) {
            return true;
        }
        if (HwVSimPhoneFactory.getVSimEnabledSubId() != -1) {
            return false;
        }
        return !HwVSimController.getInstance().isDoingSlotSwitch();
    }

    public boolean isPlatformTwoModems() {
        return VSIM_MODEM_COUNT == 2;
    }

    public boolean isRadioAvailable(int slotId) {
        if (sIsPlatformSupportVSim && HwVSimController.isInstantiated()) {
            return HwVSimUtilsInner.isRadioAvailable(slotId);
        }
        return true;
    }

    public boolean needBlockPin(int slotId) {
        if (sIsPlatformSupportVSim && HwVSimController.isInstantiated()) {
            return HwVSimController.getInstance().needBlockPin(slotId);
        }
        return false;
    }

    public boolean isVSimInProcess() {
        if (sIsPlatformSupportVSim && HwVSimController.isInstantiated()) {
            return HwVSimController.getInstance().isVSimInProcess();
        }
        return false;
    }

    public void processHotPlug(int[] cardTypes) {
        if (sIsPlatformSupportVSim && HwVSimController.isInstantiated()) {
            HwVSimController.getInstance().processHotPlug(cardTypes);
        }
    }

    public IccCardConstantsEx.StateEx modifySimStateForVsim(int phoneId, IccCardConstantsEx.StateEx s) {
        if (sIsPlatformSupportVSim && HwVSimController.isInstantiated()) {
            return HwVSimController.getInstance().modifySimStateForVsim(phoneId, s);
        }
        return s;
    }

    public boolean isPlatformRealTripple() {
        if (sIsPlatformSupportVSim && HwVSimController.isInstantiated()) {
            return HwVSimUtilsInner.isPlatformRealTripple();
        }
        return false;
    }

    public void updateSubState(int slotId, int subState) {
        if (sIsPlatformSupportVSim && HwVSimController.isInstantiated()) {
            HwVSimController.getInstance().updateSubState(slotId, subState);
        }
    }

    public void setSubActived(int slotId) {
        if (sIsPlatformSupportVSim && HwVSimController.isInstantiated()) {
            HwVSimController.getInstance().setSubActived(slotId);
        }
    }

    public void updateSimCardTypes(int[] cardTypes) {
        if (sIsPlatformSupportVSim && HwVSimController.isInstantiated()) {
            HwVSimController.getInstance().updateSimCardTypes(cardTypes);
        }
    }

    public boolean isSubActivationUpdate() {
        if (sIsPlatformSupportVSim && HwVSimController.isInstantiated()) {
            return HwVSimController.getInstance().isSubActivationUpdate();
        }
        return false;
    }

    public void processAutoSetPowerupModemDone() {
    }

    public boolean prohibitSubUpdateSimNoChange(int slotId) {
        if (sIsPlatformSupportVSim && HwVSimController.isInstantiated() && HwVSimUtilsInner.isPlatformRealTripple() && HwVSimUtilsInner.IS_DSDSPOWER_SUPPORT) {
            return HwVSimController.getInstance().prohibitSubUpdateSimNoChange(slotId);
        }
        return false;
    }

    public boolean needBlockUnReservedForVsim(int slotId) {
        if (sIsPlatformSupportVSim && HwVSimController.isInstantiated()) {
            return HwVSimController.getInstance().needBlockUnReservedForVsim(slotId);
        }
        return false;
    }

    public boolean getIsWaitingSwitchCdmaModeSide() {
        if (sIsPlatformSupportVSim && HwVSimController.isInstantiated()) {
            return HwVSimController.getInstance().getIsWaitingSwitchCdmaModeSide();
        }
        return false;
    }

    public boolean getIsWaitingNvMatchUnsol() {
        if (sIsPlatformSupportVSim && HwVSimController.isInstantiated() && HwVSimUtilsInner.isPlatformNeedWaitNvMatchUnsol()) {
            return HwVSimController.getInstance().getIsWaitingNvMatchUnsol();
        }
        return false;
    }

    public boolean isVSimDsdsVersionOne() {
        if (sIsPlatformSupportVSim && HwVSimController.isInstantiated()) {
            return HwVSimUtilsInner.isVSimDsdsVersionOne();
        }
        return false;
    }

    public void simHotPlugOut(int slotId) {
        if (sIsPlatformSupportVSim && HwVSimController.isInstantiated()) {
            HwVSimController.getInstance().simHotPlugOut(slotId);
        }
    }

    public void simHotPlugIn(int slotId) {
        if (sIsPlatformSupportVSim && HwVSimController.isInstantiated()) {
            HwVSimController.getInstance().simHotPlugIn(slotId);
        }
    }

    public boolean isVSimSub(int sub) {
        if (sIsPlatformSupportVSim && sub == 2) {
            return true;
        }
        return false;
    }

    public void storeIfNeedRestartRildForNvMatch(boolean isNeed) {
        if (sIsPlatformSupportVSim && HwVSimController.isInstantiated()) {
            HwVSimNvMatchController.getInstance().storeIfNeedRestartRildForNvMatch(isNeed);
        }
    }

    public void restartRildIfIdle() {
        if (sIsPlatformSupportVSim && HwVSimController.isInstantiated()) {
            HwVSimNvMatchController.getInstance().restartRildIfIdle();
        }
    }

    public void setDefaultMobileEnableForVSim(boolean isEnable) {
        if (sIsPlatformSupportVSim && HwVSimController.isInstantiated()) {
            HwVSimUtilsInner.setDefaultMobileEnableForVSim(isEnable);
        }
    }
}
