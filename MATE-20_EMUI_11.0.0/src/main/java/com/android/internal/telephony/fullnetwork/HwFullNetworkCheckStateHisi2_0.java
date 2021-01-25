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
                boolean isNeedSwitchMainSlotForEuicc = isNeedSwitchMainSlotForEuicc();
                logd("checkIfAllCardsReady mAutoSwitchDualCardsSlotDone = " + this.mChipHisi.mAutoSwitchDualCardsSlotDone + ", isBalongSimSynced = " + this.mChipHisi.isBalongSimSynced() + ", needFixMainSlotPosition = " + this.mChipHisi.needFixMainSlotPosition + ",isNeedSwitcMainSlotForEuicc = " + isNeedSwitchMainSlotForEuicc);
                if (!this.mChipHisi.mAutoSwitchDualCardsSlotDone || !this.mChipHisi.isBalongSimSynced() || this.mChipHisi.needFixMainSlotPosition || isNeedSwitchMainSlotForEuicc) {
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

    private boolean isNeedSwitchMainSlotForEuicc() {
        if (!this.mChipCommon.isSupportEuicc()) {
            return false;
        }
        boolean isUiccChanged = uiccTypeChanged();
        if (euiccAppNumsChanged() || isUiccChanged) {
            logd("disable/enable profile or switch esim/sim to sim/esim");
            return true;
        }
        boolean isSim1Absent = HwCardTrayInfo.getInstance().isCardTrayOut(0) && !this.mChipHisi.isSimPresentBySubState(0);
        boolean isSim2EuiccNotMainSlot = (this.mChipHisi.mCardType == 1) && this.mChipCommon.getUserSwitchDualCardSlots() != 1;
        if (!isSim1Absent || !isSim2EuiccNotMainSlot) {
            return false;
        }
        logd("sim 1 hot plug out, sim 2 is euicc and not main slot, set to false");
        return true;
    }

    private void handleNotAllCardsReady() {
        if (hasMessages(402)) {
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
        }
        if (uiccCard.isEuiccCard()) {
            int currenAppNums = uiccCard.getNumApplications();
            if (this.mChipHisi.mLastAppNums != currenAppNums) {
                logd("esimapp changed currennums " + currenAppNums + " lastnums " + this.mChipHisi.mLastAppNums);
                this.mChipHisi.mLastAppNums = currenAppNums;
                return true;
            }
        } else {
            this.mChipHisi.mLastAppNums = 0;
        }
        return false;
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
        RlogEx.i(LOG_TAG, msg);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkCheckStateBase
    public void loge(String msg) {
        RlogEx.e(LOG_TAG, msg);
    }
}
