package com.android.internal.telephony.vsim;

import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.HwVSimPhoneFactory;
import com.android.internal.telephony.IccCardConstants.State;

public class HwVSimUtils {
    public static final String DEVICE_ID_PREF = "deviceId_pending_for_vsim";
    public static final String DEVICE_SVN_PREF = "deviceSvn_pending_for_vsim";
    public static final String ESN_PREF = "esn_pending_for_vsim";
    public static final String IMEI_PREF = "imei_pending_for_vsim";
    private static final String LOG_TAG = "VSimUtils";
    public static final String MEID_PREF = "meid_pending_for_vsim";
    private static final int VSIM_MODEM_COUNT = 0;
    private static final int VSIM_MODEM_COUNT_DEFAULT = 3;
    protected static final boolean sIsPlatformSupportVSim = false;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.vsim.HwVSimUtils.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.vsim.HwVSimUtils.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.vsim.HwVSimUtils.<clinit>():void");
    }

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
        boolean z = false;
        if (!sIsPlatformSupportVSim || !HwVSimController.isInstantiated()) {
            return true;
        }
        if (HwVSimPhoneFactory.getVSimEnabledSubId() != -1) {
            return false;
        }
        if (!HwVSimController.getInstance().isDoingSlotSwitch()) {
            z = true;
        }
        return z;
    }

    public static boolean isPlatformTwoModems() {
        return VSIM_MODEM_COUNT == 2;
    }

    public static boolean isRadioAvailable(int subId) {
        if (!sIsPlatformSupportVSim || !HwVSimController.isInstantiated()) {
            return true;
        }
        CommandsInterface ci = HwVSimController.getInstance().getCiBySub(subId);
        if (ci != null) {
            return ci.isRadioAvailable();
        }
        return false;
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

    public static State modifySimStateForVsim(int subId, State s) {
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
}
