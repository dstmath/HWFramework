package com.android.internal.telephony.fullnetwork;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.Rlog;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.HwDsdsController;
import com.android.internal.telephony.vsim.HwVSimUtils;

public class HwFullNetworkCheckStateHisi1_0 extends HwFullNetworkCheckStateHisiBase {
    private static final String LOG_TAG = "HwFullNetworkCheckStateHisi1_0";

    public HwFullNetworkCheckStateHisi1_0(Context c, CommandsInterface[] ci, Handler h) {
        super(c, ci, h);
        logd("HwFullNetworkCheckStateHisi1_0 constructor");
    }

    public boolean checkIfAllCardsReady(Message msg) {
        logd("checkIfAllCardsReady");
        isCardsReady();
        if (HwFullNetworkConfig.IS_HISI_DSDX) {
            logd("HwFullNetwork not getCdmaModeDone");
            this.ready = false;
        }
        if (!HwVSimUtils.isPlatformTwoModems() || (!HwVSimUtils.isVSimEnabled() && !HwVSimUtils.isVSimCauseCardReload())) {
            if (!this.mChipHisi.mAllCardsReady && this.ready) {
                if (hasMessages(HwFullNetworkConstants.EVENT_SET_MAIN_SLOT_TIMEOUT)) {
                    logd("checkIfAllCardsReady, is switching sim slot, waiting...");
                } else if ("0".equals(SystemProperties.get("gsm.nvcfg.rildrestarting", "0"))) {
                    logd("send mSet4GSlotCompleteMsg to target.");
                    this.mChipCommon.sendResponseToTarget(this.mChipCommon.mSet4GSlotCompleteMsg, 0);
                    this.mChipCommon.mSet4GSlotCompleteMsg = null;
                    setPrefNetworkIfNeeded();
                } else {
                    logd("gsm.nvcfg.rildrestarting not 0");
                }
            }
            if (this.mChipHisi.mAllCardsReady != this.ready) {
                this.mChipHisi.mAllCardsReady = this.ready;
                logd("mAllCardsReady is " + this.ready);
            }
            if (!this.mChipHisi.mAllCardsReady) {
                return false;
            }
            this.mChipHisi.refreshCardState();
            logd("checkIfAllCardsReady mAutoSwitchDualCardsSlotDone = " + this.mChipHisi.mAutoSwitchDualCardsSlotDone + " ; mCommrilRestartRild= " + this.mChipHisi.mCommrilRestartRild);
            boolean isNeedSetMainSlot = false;
            if (!this.mChipHisi.mAutoSwitchDualCardsSlotDone || !this.mChipHisi.isBalongSimSynced()) {
                logd("switchDualCardsSlotIfNeeded!");
                isNeedSetMainSlot = switchDualCardsSlotIfNeeded();
            } else if (this.mChipHisi.mCommrilRestartRild && !this.mChipCommon.isSet4GSlotInProgress) {
                logd("mCommrilRestartRild is true");
                this.mChipHisi.setCommrilRestartRild(false);
                this.mChipHisi.mAutoSwitchDualCardsSlotDone = true;
                this.mChipHisi.setWaitingSwitchBalongSlot(true);
                this.defaultMainSlot = this.mChipCommon.getUserSwitchDualCardSlots();
                isNeedSetMainSlot = true;
            }
            if (HwDsdsController.IS_DSDSPOWER_SUPPORT) {
                if (HwFullNetworkConfig.IS_FULL_NETWORK_SUPPORTED_IN_HISI) {
                    logd("Need to switch commril, so wait for radio on to SetActiveMode");
                } else {
                    HwDsdsController.getInstance().custHwdsdsSetActiveModeIfNeeded(this.mChipCommon.mUiccController.getUiccCards());
                }
            }
            return isNeedSetMainSlot;
        }
        logd("checkIfAllCardsReady()...vsim enabled or card reloading on two modem platform.");
        this.mChipHisi.setWaitingSwitchBalongSlot(false);
        this.mChipHisi.mAutoSwitchDualCardsSlotDone = true;
        return false;
    }

    /* access modifiers changed from: protected */
    public void logd(String msg) {
        Rlog.d(LOG_TAG, msg);
    }

    /* access modifiers changed from: protected */
    public void loge(String msg) {
        Rlog.e(LOG_TAG, msg);
    }
}
