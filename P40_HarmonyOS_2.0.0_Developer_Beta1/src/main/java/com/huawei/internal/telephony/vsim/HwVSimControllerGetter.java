package com.huawei.internal.telephony.vsim;

import android.os.Bundle;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.android.internal.telephony.vsim.HwVSimController;
import com.android.internal.telephony.vsim.HwVSimEventReport;
import com.android.internal.telephony.vsim.HwVSimLog;
import com.android.internal.telephony.vsim.HwVSimSlotSwitchController;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.IccCardConstantsEx;
import com.huawei.internal.telephony.PhoneExt;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public final class HwVSimControllerGetter {
    private static final DefaultHwVSimBaseController DEFAULT_CONTROLLER = new DefaultHwVSimBaseController();
    private static final Object LOCK = new Object();
    private static final String TAG = "HwVSimControllerGetter";
    private static volatile HwVSimControllerGetter sInstance = null;
    private final boolean isHisiPlatform = HuaweiTelephonyConfigs.isHisiPlatform();
    private final boolean isMTKPlatform = HuaweiTelephonyConfigs.isMTKPlatform();
    private final boolean isPlatformSupportVSim = SystemPropertiesEx.getBoolean("ro.radio.vsim_support", false);

    private HwVSimControllerGetter() {
        slogI("get controller, isMTKPlatform:" + this.isMTKPlatform + " ,isHisiPlatform: " + this.isHisiPlatform + ", isPlatformSupportVSim:" + this.isPlatformSupportVSim);
    }

    private static HwVSimControllerGetter instance() {
        HwVSimControllerGetter hwVSimControllerGetter;
        synchronized (LOCK) {
            if (sInstance == null) {
                sInstance = new HwVSimControllerGetter();
            }
            hwVSimControllerGetter = sInstance;
        }
        return hwVSimControllerGetter;
    }

    public static HwVSimBaseController get() {
        return instance().getController();
    }

    public static boolean valid(HwVSimBaseController controller) {
        return (controller == null || controller == DEFAULT_CONTROLLER) ? false : true;
    }

    private HwVSimBaseController getController() {
        if (!this.isPlatformSupportVSim) {
            return DEFAULT_CONTROLLER;
        }
        if (!HwVSimBaseController.isInstantiated()) {
            return DEFAULT_CONTROLLER;
        }
        if (this.isHisiPlatform) {
            return HwVSimController.getInstance();
        }
        if (this.isMTKPlatform) {
            return HwVSimMtkController.getInstance();
        }
        slogW("getController, unknown Platform");
        return DEFAULT_CONTROLLER;
    }

    private static void slogI(String msg) {
        HwVSimLog.info(TAG, msg);
    }

    /* access modifiers changed from: private */
    public static void slogW(String msg) {
        HwVSimLog.warning(TAG, msg);
    }

    /* access modifiers changed from: private */
    public static class DefaultHwVSimBaseController extends HwVSimBaseController {
        DefaultHwVSimBaseController() {
            super(null, null, null);
        }

        @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
        public void broadcastVsimServiceReady() {
            HwVSimControllerGetter.slogW("No implement broadcastVsimServiceReady");
        }

        @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
        public int getVsimSlotId() {
            HwVSimControllerGetter.slogW("No implement getVsimSlotId");
            return -1;
        }

        @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
        public boolean isVSimEnabled() {
            HwVSimControllerGetter.slogW("No implement isVSimEnabled");
            return false;
        }

        @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
        public boolean isVSimOn() {
            HwVSimControllerGetter.slogW("No implement isVSimOn");
            return false;
        }

        @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
        public void setIsVSimOn(boolean isVSimOn) {
            HwVSimControllerGetter.slogW("No implement setIsVSimOn");
        }

        @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            HwVSimControllerGetter.slogW("No implement dump");
        }

        @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
        public String getSpn() {
            HwVSimControllerGetter.slogW("No implement getSpn");
            return null;
        }

        @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
        public int getRule() {
            HwVSimControllerGetter.slogW("No implement getRule");
            return super.getRule();
        }

        @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
        public boolean isVSimCauseCardReload() {
            HwVSimControllerGetter.slogW("No implement isVSimCauseCardReload");
            return false;
        }

        @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
        public void setMarkForCardReload(int slotId, boolean value) {
            HwVSimControllerGetter.slogW("No implement setMarkForCardReload");
        }

        @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
        public boolean needBlockPin(int slotId) {
            HwVSimControllerGetter.slogW("No implement needBlockPin");
            return false;
        }

        @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
        public boolean needBlockUnReservedForVsim(int slotId) {
            HwVSimControllerGetter.slogW("No implement needBlockUnReservedForVsim");
            return false;
        }

        @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
        public String getPendingDeviceInfoFromSP(String prefKey) {
            HwVSimControllerGetter.slogW("No implement getPendingDeviceInfoFromSP");
            return null;
        }

        @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
        public CommandsInterfaceEx getCiBySub(int slotId) {
            HwVSimControllerGetter.slogW("No implement getCiBySub, slotId:" + slotId);
            return null;
        }

        @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
        public PhoneExt getPhoneBySub(int subId) {
            HwVSimControllerGetter.slogW("No implement getPhoneBySub, subId:" + subId);
            return null;
        }

        @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
        public int getSimSlotTableLastSlotId() {
            HwVSimControllerGetter.slogW("No implement getSimSlotTableLastSlotId");
            return -1;
        }

        @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
        public int getSubState(int slotId) {
            HwVSimControllerGetter.slogW("No implement getSubState slotId =" + slotId);
            return 0;
        }

        @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
        public int getCardPresentNumeric(boolean[] isCardPresent) {
            HwVSimControllerGetter.slogW("No implement getCardPresentNumeric");
            return 0;
        }

        @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
        public HwVSimEventReport.VSimEventInfo getVSimEventInfo() {
            HwVSimControllerGetter.slogW("No implement getVSimEventInfo");
            return null;
        }

        @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
        public boolean isDoingSlotSwitch() {
            HwVSimControllerGetter.slogW("No implement isDoingSlotSwitch");
            return true;
        }

        @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
        public void processHotPlug(int[] cardTypes) {
            HwVSimControllerGetter.slogW("No implement processHotPlug");
        }

        @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
        public IccCardConstantsEx.StateEx modifySimStateForVsim(int phoneId, IccCardConstantsEx.StateEx state) {
            HwVSimControllerGetter.slogW("No implement modifySimStateForVsim");
            return state;
        }

        @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
        public void updateSubState(int slotId, int subState) {
            HwVSimControllerGetter.slogW("No implement updateSubState");
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
        public boolean isProcessInit() {
            HwVSimControllerGetter.slogW("No implement isProcessInit");
            return false;
        }

        @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
        public boolean getIsSessionOpen() {
            HwVSimControllerGetter.slogW("No implement getIsSessionOpen");
            return false;
        }

        @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
        public void setIsSessionOpen(boolean isOpen) {
            HwVSimControllerGetter.slogW("No implement setIsSessionOpen:" + isOpen);
        }

        @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
        public void setSubActived(int slotId) {
            HwVSimControllerGetter.slogW("No implement setSubActived");
        }

        @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
        public void updateSimCardTypes(int[] cardTypes) {
            HwVSimControllerGetter.slogW("No implement updateSimCardTypes");
        }

        @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
        public boolean isSubActivationUpdate() {
            HwVSimControllerGetter.slogW("No implement isSubActivationUpdate");
            return false;
        }

        @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
        public boolean prohibitSubUpdateSimNoChange(int slotId) {
            HwVSimControllerGetter.slogW("No implement prohibitSubUpdateSimNoChange slotId:" + slotId);
            return false;
        }

        @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
        public boolean getIsWaitingSwitchCdmaModeSide() {
            HwVSimControllerGetter.slogW("No implement getIsWaitingSwitchCdmaModeSide");
            return false;
        }

        @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
        public boolean getIsWaitingNvMatchUnsol() {
            HwVSimControllerGetter.slogW("No implement getIsWaitingNvMatchUnsol");
            return false;
        }

        @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
        public void simHotPlugOut(int slotId) {
            HwVSimControllerGetter.slogW("No implement simHotPlugOut , slotId = " + slotId);
        }

        @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
        public void simHotPlugIn(int slotId) {
            HwVSimControllerGetter.slogW("No implement simHotPlugIn , slotId = " + slotId);
        }

        @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
        public HwVSimSlotSwitchController.CommrilMode getCommrilMode() {
            HwVSimControllerGetter.slogW("No implement getCommRilMode");
            return null;
        }

        @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
        public HwVSimSlotSwitchController.CommrilMode getExpectCommrilMode(int mainSlot, int[] cardType) {
            HwVSimControllerGetter.slogW("No implement getExpectCommrilMode");
            return null;
        }

        @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
        public boolean isVSimInProcess() {
            HwVSimControllerGetter.slogW("No implement isVSimInProcess");
            return false;
        }

        @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
        public int enableVSim(int operation, Bundle bundle) {
            HwVSimControllerGetter.slogW("No implement enableVSim");
            return 3;
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
        public int enableVSim(HwVSimConstants.EnableParam param) {
            HwVSimControllerGetter.slogW("No implement enableVSim");
            return 3;
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
        public int setApn(HwVSimConstants.ApnParams params) {
            HwVSimControllerGetter.slogW("No implement setApn");
            return 3;
        }

        @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
        public boolean disableVSim() {
            HwVSimControllerGetter.slogW("No implement disableVSim");
            return false;
        }

        @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
        public String getTrafficData() {
            HwVSimControllerGetter.slogW("No implement getTrafficData");
            return null;
        }

        @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
        public boolean clearTrafficData() {
            HwVSimControllerGetter.slogW("No implement clearTrafficData");
            return false;
        }

        @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
        public boolean dsFlowCfg(int repFlag, int threshold, int totalThreshold, int oper) {
            HwVSimControllerGetter.slogW("No implement dsFlowCfg");
            return false;
        }

        @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
        public int getSimStateViaSysinfoEx(int slotId) {
            HwVSimControllerGetter.slogW("No implement getSimStateViaSysinfoEx, slotId:" + slotId);
            return -1;
        }

        @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
        public int scanVsimAvailableNetworks(int slotId, int type) {
            HwVSimControllerGetter.slogW("No implement scanVsimAvailableNetworks, slotId:" + slotId);
            return -1;
        }

        @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
        public String getDevSubMode(int slotId) {
            HwVSimControllerGetter.slogW("No implement getDevSubMode, slotId:" + slotId);
            return null;
        }

        @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
        public String getPreferredNetworkTypeForVsim(int subscription) {
            HwVSimControllerGetter.slogW("No implement getPreferredNetworkTypeForVSim");
            return null;
        }

        @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
        public boolean switchVsimWorkMode(int workMode) {
            HwVSimControllerGetter.slogW("No implement switchVSimWorkMode:" + workMode);
            return false;
        }

        @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
        public int dialupForVSim() {
            HwVSimControllerGetter.slogW("No implement dialupForVSim");
            return -1;
        }

        @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
        public boolean hasVSimIccCard() {
            HwVSimControllerGetter.slogW("No implement hasVSimIccCard");
            return false;
        }

        @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
        public void disposeCard(int index) {
            HwVSimControllerGetter.slogW("No implement disposeCard. index:" + index);
        }

        @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
        public void broadcastQueryResults(AsyncResultEx ar) {
            HwVSimControllerGetter.slogW("No implement broadcastQueryResults");
        }

        @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
        public void broadcastVSimCardType() {
            HwVSimControllerGetter.slogW("No implement broadcastVSimCardType");
        }
    }
}
