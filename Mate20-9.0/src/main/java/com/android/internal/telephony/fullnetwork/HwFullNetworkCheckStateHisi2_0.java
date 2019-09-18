package com.android.internal.telephony.fullnetwork;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.Rlog;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.vsim.HwVSimUtils;

public class HwFullNetworkCheckStateHisi2_0 extends HwFullNetworkCheckStateHisiBase {
    private static final String LOG_TAG = "HwFullNetworkCheckStateHisi2_0";

    public HwFullNetworkCheckStateHisi2_0(Context c, CommandsInterface[] ci, Handler h) {
        super(c, ci, h);
        logd("HwFullNetworkCheckStateHisi2_0 constructor");
    }

    public boolean checkIfAllCardsReady(Message msg) {
        logd("checkIfAllCardsReady");
        isCardsReady();
        if (!HwVSimUtils.isPlatformTwoModems() || !HwVSimUtils.isVSimEnabled()) {
            try {
                Phone[] phones = PhoneFactory.getPhones();
                for (int i = 0; i < HwFullNetworkConstants.SIM_NUM; i++) {
                    if (phones[i] == null || (phones[i] != null && phones[i].getRadioCapability() == null)) {
                        this.ready = false;
                        logd("RadioCapability is null");
                    }
                }
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
                logd("checkIfAllCardsReady mAutoSwitchDualCardsSlotDone = " + this.mChipHisi.mAutoSwitchDualCardsSlotDone + ", isBalongSimSynced = " + this.mChipHisi.isBalongSimSynced() + ", needFixMainSlotPosition = " + this.mChipHisi.needFixMainSlotPosition);
                boolean isNeedSetMainSlot = false;
                if (!this.mChipHisi.mAutoSwitchDualCardsSlotDone || !this.mChipHisi.isBalongSimSynced() || this.mChipHisi.needFixMainSlotPosition) {
                    isNeedSetMainSlot = switchDualCardsSlotIfNeeded();
                }
                return isNeedSetMainSlot;
            } catch (IllegalStateException e) {
                this.ready = false;
                logd("PhoneFactory.getPhones is null");
                return false;
            }
        } else {
            logd("checkIfAllCardsReady()...vsim enabled on two modem platform.");
            this.mChipHisi.setWaitingSwitchBalongSlot(false);
            this.mChipHisi.mAutoSwitchDualCardsSlotDone = true;
            return false;
        }
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
