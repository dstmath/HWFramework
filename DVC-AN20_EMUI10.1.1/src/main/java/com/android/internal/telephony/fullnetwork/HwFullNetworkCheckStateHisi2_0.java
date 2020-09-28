package com.android.internal.telephony.fullnetwork;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.telephony.HwTelephonyManager;
import com.android.internal.telephony.HwCardTrayInfo;
import com.android.internal.telephony.vsim.HwVSimUtils;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.PhoneFactoryExt;
import com.huawei.internal.telephony.uicc.UiccCardExt;

public class HwFullNetworkCheckStateHisi2_0 extends HwFullNetworkCheckStateHisiBase {
    private static final String LOG_TAG = "HwFullNetworkCheckStateHisi2_0";

    public HwFullNetworkCheckStateHisi2_0(Context c, CommandsInterfaceEx[] ci, Handler h) {
        super(c, ci, h);
        logd("HwFullNetworkCheckStateHisi2_0 constructor");
    }

    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkCheckStateHisiBase, com.android.internal.telephony.fullnetwork.HwFullNetworkCheckStateBase
    public boolean checkIfAllCardsReady(Message msg) {
        logd("checkIfAllCardsReady");
        isCardsReady();
        if (!HwTelephonyManager.getDefault().isPlatformSupportVsim() || !HwVSimUtils.isPlatformTwoModems() || !HwVSimUtils.isVSimEnabled()) {
            try {
                PhoneExt[] phones = PhoneFactoryExt.getPhones();
                for (int i = 0; i < HwFullNetworkConstantsInner.SIM_NUM; i++) {
                    if (phones[i] == null || (phones[i] != null && phones[i].getRadioCapability() == null)) {
                        this.ready = false;
                        logd("RadioCapability is null");
                    }
                }
                if (!this.mChipHisi.mAllCardsReady && this.ready) {
                    handleNotAllCardsReady();
                }
                if (this.mChipHisi.mAllCardsReady != this.ready) {
                    this.mChipHisi.mAllCardsReady = this.ready;
                    logd("mAllCardsReady is " + this.ready);
                }
                if (!this.mChipHisi.mAllCardsReady) {
                    return false;
                }
                checkCMCCUnbind();
                checkDefaultMainSlotForMDMCarrier();
                this.mChipHisi.refreshCardState();
                checkIfAllCardsReadyForeUicc();
                logd("checkIfAllCardsReady mAutoSwitchDualCardsSlotDone = " + this.mChipHisi.mAutoSwitchDualCardsSlotDone + ", isBalongSimSynced = " + this.mChipHisi.isBalongSimSynced() + ", needFixMainSlotPosition = " + this.mChipHisi.needFixMainSlotPosition);
                if (!this.mChipHisi.mAutoSwitchDualCardsSlotDone || !this.mChipHisi.isBalongSimSynced() || this.mChipHisi.needFixMainSlotPosition) {
                    return switchDualCardsSlotIfNeeded();
                }
                return false;
            } catch (IllegalStateException e) {
                this.ready = false;
                logd("PhoneFactoryExt.getPhones is null");
                return false;
            }
        } else {
            logd("checkIfAllCardsReady()...vsim enabled on two modem platform.");
            this.mChipHisi.setWaitingSwitchBalongSlot(false);
            this.mChipHisi.mAutoSwitchDualCardsSlotDone = true;
            return false;
        }
    }

    private void checkIfAllCardsReadyForeUicc() {
        boolean isSim1Absent;
        boolean isSim2Euicc;
        boolean isSim2EuiccNotMainSlot;
        if (this.mChipCommon.isSupportEuicc()) {
            boolean isUiccChanged = uiccTypeChanged();
            if (euiccAppNumsChanged() || isUiccChanged) {
                logd("disable/enable profile or switch esim/sim to sim/esim");
                this.mChipHisi.mAutoSwitchDualCardsSlotDone = false;
                return;
            }
            if (!HwCardTrayInfo.getInstance().isCardTrayOut(0) || this.mChipHisi.isSimPresentBySubState(0)) {
                isSim1Absent = false;
            } else {
                isSim1Absent = true;
            }
            if (this.mChipHisi.mCardType == 1) {
                isSim2Euicc = true;
            } else {
                isSim2Euicc = false;
            }
            if (!isSim2Euicc || this.mChipCommon.getUserSwitchDualCardSlots() == 1) {
                isSim2EuiccNotMainSlot = false;
            } else {
                isSim2EuiccNotMainSlot = true;
            }
            if (isSim1Absent && isSim2EuiccNotMainSlot) {
                logd("sim 1 hot plug out, sim 2 is euicc and not main slot, set to false");
                this.mChipHisi.mAutoSwitchDualCardsSlotDone = false;
            }
        }
    }

    private void handleNotAllCardsReady() {
        if (hasMessages(HwFullNetworkConstantsInner.EVENT_SET_MAIN_SLOT_TIMEOUT)) {
            logd("checkIfAllCardsReady, is switching sim slot, waiting...");
        } else if ("0".equals(SystemPropertiesEx.get("gsm.nvcfg.rildrestarting", "0"))) {
            logd("send mSet4GSlotCompleteMsg to target.");
            this.mChipCommon.sendResponseToTarget(this.mChipCommon.mSet4GSlotCompleteMsg, 0);
            this.mChipCommon.mSet4GSlotCompleteMsg = null;
            setPrefNetworkIfNeeded();
        } else {
            logd("gsm.nvcfg.rildrestarting not 0");
        }
    }

    private boolean euiccAppNumsChanged() {
        UiccCardExt uiccCard = this.mChipCommon.mUiccController.getUiccCard(1);
        if (uiccCard == null) {
            logd("haven't get all UiccCards done, please wait! ");
            return false;
        } else if (uiccCard.isEuiccCard()) {
            int currenAppNums = uiccCard.getNumApplications();
            if (this.mChipHisi.mLastAppNums == currenAppNums) {
                return false;
            }
            logd("esimapp changed currennums " + currenAppNums + " lastnums " + this.mChipHisi.mLastAppNums);
            this.mChipHisi.mLastAppNums = currenAppNums;
            return true;
        } else {
            this.mChipHisi.mLastAppNums = 0;
            return false;
        }
    }

    private boolean uiccTypeChanged() {
        int currenCardType;
        UiccCardExt uiccCard = this.mChipCommon.mUiccController.getUiccCard(1);
        if (uiccCard == null) {
            logd("haven't get all UiccCards done, please wait! ");
            return false;
        }
        if (uiccCard.isEuiccCard()) {
            currenCardType = 1;
        } else {
            currenCardType = 0;
        }
        if (this.mChipHisi.mCardType == currenCardType) {
            return false;
        }
        logd("uicc type is changed " + this.mChipHisi.mCardType + " -> " + currenCardType);
        this.mChipHisi.mCardType = currenCardType;
        return true;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkCheckStateBase
    public void logd(String msg) {
        RlogEx.d(LOG_TAG, msg);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkCheckStateBase
    public void loge(String msg) {
        RlogEx.e(LOG_TAG, msg);
    }
}
