package com.android.internal.telephony.vsim;

import com.android.internal.telephony.HwVSimPhoneFactory;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.internal.telephony.IccCardConstantsEx;
import com.huawei.internal.telephony.vsim.HwVSimBaseController;
import com.huawei.internal.telephony.vsim.HwVSimControllerGetter;

public class HwVSimUtilsImpl extends DefaultHwVSimUtils {
    public static final String DEVICE_ID_PREF = "deviceId_pending_for_vsim";
    public static final String DEVICE_SVN_PREF = "deviceSvn_pending_for_vsim";
    public static final String ESN_PREF = "esn_pending_for_vsim";
    public static final String IMEI_PREF = "imei_pending_for_vsim";
    private static final boolean IS_PLATFORM_SUPPORT_V_SIM = SystemPropertiesEx.getBoolean("ro.radio.vsim_support", false);
    private static final String LOG_TAG = "VSimUtils";
    public static final String MEID_PREF = "meid_pending_for_vsim";
    private static final int VSIM_MODEM_COUNT = SystemPropertiesEx.getInt("ro.radio.vsim_modem_count", 3);
    private static final int VSIM_MODEM_COUNT_DEFAULT = 3;
    private static HwVSimUtilsImpl sInstance;

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
        if (!IS_PLATFORM_SUPPORT_V_SIM) {
            return false;
        }
        return HwVSimControllerGetter.get().isVSimEnabled();
    }

    public boolean isVSimOn() {
        if (!IS_PLATFORM_SUPPORT_V_SIM) {
            return false;
        }
        return HwVSimControllerGetter.get().isVSimOn();
    }

    public boolean isVSimCauseCardReload() {
        if (!IS_PLATFORM_SUPPORT_V_SIM) {
            return false;
        }
        return HwVSimControllerGetter.get().isVSimCauseCardReload();
    }

    public boolean isAllowALSwitch() {
        if (!IS_PLATFORM_SUPPORT_V_SIM) {
            return true;
        }
        HwVSimBaseController controller = HwVSimControllerGetter.get();
        if (!HwVSimControllerGetter.valid(controller)) {
            return true;
        }
        if (HwVSimPhoneFactory.getVSimEnabledSubId() != -1) {
            return false;
        }
        return true ^ controller.isDoingSlotSwitch();
    }

    public boolean isPlatformTwoModems() {
        return VSIM_MODEM_COUNT == 2;
    }

    public boolean isRadioAvailable(int slotId) {
        if (!IS_PLATFORM_SUPPORT_V_SIM) {
            return true;
        }
        return HwVSimUtilsInner.isRadioAvailable(slotId);
    }

    public boolean needBlockPin(int slotId) {
        if (!IS_PLATFORM_SUPPORT_V_SIM) {
            return false;
        }
        return HwVSimControllerGetter.get().needBlockPin(slotId);
    }

    public boolean isVSimInProcess() {
        if (!IS_PLATFORM_SUPPORT_V_SIM) {
            return false;
        }
        return HwVSimControllerGetter.get().isVSimInProcess();
    }

    public void processHotPlug(int[] cardTypes) {
        if (IS_PLATFORM_SUPPORT_V_SIM) {
            HwVSimControllerGetter.get().processHotPlug(cardTypes);
        }
    }

    public IccCardConstantsEx.StateEx modifySimStateForVsim(int phoneId, IccCardConstantsEx.StateEx state) {
        if (!IS_PLATFORM_SUPPORT_V_SIM) {
            return state;
        }
        return HwVSimControllerGetter.get().modifySimStateForVsim(phoneId, state);
    }

    public boolean isPlatformRealTripple() {
        if (IS_PLATFORM_SUPPORT_V_SIM && HwVSimBaseController.isInstantiated()) {
            return HwVSimUtilsInner.isPlatformRealTripple();
        }
        return false;
    }

    public void updateSubState(int slotId, int subState) {
        if (IS_PLATFORM_SUPPORT_V_SIM) {
            HwVSimControllerGetter.get().updateSubState(slotId, subState);
        }
    }

    public void setSubActived(int slotId) {
        if (IS_PLATFORM_SUPPORT_V_SIM) {
            HwVSimControllerGetter.get().setSubActived(slotId);
        }
    }

    public void updateSimCardTypes(int[] cardTypes) {
        if (IS_PLATFORM_SUPPORT_V_SIM) {
            HwVSimControllerGetter.get().updateSimCardTypes(cardTypes);
        }
    }

    public boolean isSubActivationUpdate() {
        if (!IS_PLATFORM_SUPPORT_V_SIM) {
            return false;
        }
        HwVSimBaseController controller = HwVSimControllerGetter.get();
        if (!HwVSimControllerGetter.valid(controller)) {
            return false;
        }
        return controller.isSubActivationUpdate();
    }

    public void processAutoSetPowerupModemDone() {
    }

    public boolean prohibitSubUpdateSimNoChange(int slotId) {
        if (!IS_PLATFORM_SUPPORT_V_SIM) {
            return false;
        }
        HwVSimBaseController controller = HwVSimControllerGetter.get();
        if (HwVSimControllerGetter.valid(controller) && HwVSimUtilsInner.isPlatformRealTripple() && HwVSimUtilsInner.IS_DSDSPOWER_SUPPORT) {
            return controller.prohibitSubUpdateSimNoChange(slotId);
        }
        return false;
    }

    public boolean needBlockUnReservedForVsim(int slotId) {
        if (IS_PLATFORM_SUPPORT_V_SIM && HwVSimControllerGetter.valid(HwVSimControllerGetter.get())) {
            return HwVSimControllerGetter.get().needBlockUnReservedForVsim(slotId);
        }
        return false;
    }

    public boolean getIsWaitingSwitchCdmaModeSide() {
        if (!IS_PLATFORM_SUPPORT_V_SIM) {
            return false;
        }
        HwVSimBaseController controller = HwVSimControllerGetter.get();
        if (!HwVSimControllerGetter.valid(controller)) {
            return false;
        }
        return controller.getIsWaitingSwitchCdmaModeSide();
    }

    public boolean getIsWaitingNvMatchUnsol() {
        if (!IS_PLATFORM_SUPPORT_V_SIM) {
            return false;
        }
        HwVSimBaseController controller = HwVSimControllerGetter.get();
        if (HwVSimControllerGetter.valid(controller) && HwVSimUtilsInner.isPlatformNeedWaitNvMatchUnsol()) {
            return controller.getIsWaitingNvMatchUnsol();
        }
        return false;
    }

    public boolean isVSimDsdsVersionOne() {
        if (IS_PLATFORM_SUPPORT_V_SIM && HwVSimBaseController.isInstantiated()) {
            return HwVSimUtilsInner.isVSimDsdsVersionOne();
        }
        return false;
    }

    public void simHotPlugOut(int slotId) {
        if (IS_PLATFORM_SUPPORT_V_SIM) {
            HwVSimControllerGetter.get().simHotPlugOut(slotId);
        }
    }

    public void simHotPlugIn(int slotId) {
        if (IS_PLATFORM_SUPPORT_V_SIM) {
            HwVSimControllerGetter.get().simHotPlugIn(slotId);
        }
    }

    public boolean isVSimSub(int sub) {
        if (IS_PLATFORM_SUPPORT_V_SIM && sub == 2) {
            return true;
        }
        return false;
    }

    public void storeIfNeedRestartRildForNvMatch(boolean isNeed) {
        if (IS_PLATFORM_SUPPORT_V_SIM && HwVSimController.isInstantiated()) {
            HwVSimNvMatchController.getInstance().storeIfNeedRestartRildForNvMatch(isNeed);
        }
    }

    public void restartRildIfIdle() {
        if (IS_PLATFORM_SUPPORT_V_SIM && HwVSimController.isInstantiated()) {
            HwVSimNvMatchController.getInstance().restartRildIfIdle();
        }
    }

    public void setDefaultMobileEnableForVSim(boolean isEnable) {
        if (IS_PLATFORM_SUPPORT_V_SIM && HwVSimController.isInstantiated()) {
            HwVSimUtilsInner.setDefaultMobileEnableForVSim(isEnable);
        }
    }
}
