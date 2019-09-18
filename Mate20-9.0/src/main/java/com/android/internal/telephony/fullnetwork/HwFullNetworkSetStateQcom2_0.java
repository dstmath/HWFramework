package com.android.internal.telephony.fullnetwork;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.Rlog;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.HwSubscriptionManager;
import com.android.internal.telephony.PhoneFactory;

public class HwFullNetworkSetStateQcom2_0 extends HwFullNetworkSetStateQcomMtkBase {
    private static final String LOG_TAG = "HwFullNetworkSetStateQcom2_0";

    HwFullNetworkSetStateQcom2_0(Context c, CommandsInterface[] ci, Handler h) {
        super(c, ci, h);
        this.mChipOther = HwFullNetworkChipOther.getInstance();
        logd("HwFullNetworkSetStateQcom2_0 constructor");
    }

    public void setMainSlot(int slotId, Message response) {
        int needSetCount;
        this.mChipCommon.expectedDDSsubId = slotId;
        if (!this.mChipOther.isSetDefault4GSlotNeeded(slotId)) {
            loge("setDefault4GSlot: there is no need to set the lte slot");
            this.mChipCommon.saveMainCardIccId(this.mChipCommon.getIccId(slotId));
            this.mChipCommon.isSet4GSlotInProgress = false;
            if (HwFullNetworkConfig.IS_CARD2_CDMA_SUPPORTED && this.mChipCommon.mSet4GSlotCompleteMsg == null) {
                logd("In auto set 4GSlot mode , makesure DDS slot same as 4G slot so set DDS to slot: " + slotId);
                HwTelephonyManagerInner.getDefault().setDefaultDataSlotId(slotId);
            }
            this.mChipOther.setLteServiceAbility();
            logd("set DDS to slot: " + slotId);
            HwTelephonyManagerInner.getDefault().setDefaultDataSlotId(slotId);
            HwSubscriptionManager.getInstance().setUserPrefDefaultSlotId(slotId);
            logd("needSetCount = " + needSetCount + "; mNeedSetAllowData = " + this.mChipOther.mNeedSetAllowData);
            if (needSetCount == 0 && this.mChipOther.mNeedSetAllowData) {
                this.mChipOther.mNeedSetAllowData = false;
                for (int i = 0; i < HwFullNetworkConstants.SIM_NUM; i++) {
                    PhoneFactory.resendDataAllowed(i);
                    logd("setDefault4GSlot resend data allow with slot " + i);
                }
            }
            this.mChipCommon.sendResponseToTarget(this.mChipCommon.mSet4GSlotCompleteMsg, 0);
            this.mChipCommon.mSet4GSlotCompleteMsg = null;
            this.mStateHandler.obtainMessage(HwFullNetworkConstants.EVENT_TRANS_TO_DEFAULT).sendToTarget();
        } else if (this.mCis[slotId] == null || CommandsInterface.RadioState.RADIO_UNAVAILABLE != this.mCis[slotId].getRadioState()) {
            logd("setDefault4GSlot: target slot id is: " + slotId + " response:" + response);
            sendHwSwitchSlotStartBroadcast();
            this.mChipCommon.mSet4GSlotCompleteMsg = response;
            this.mChipCommon.isSet4GSlotInProgress = true;
            this.mChipCommon.current4GSlotBackup = this.mChipCommon.getUserSwitchDualCardSlots();
            if (HwFullNetworkConfig.IS_QCOM_DUAL_LTE_STACK) {
                this.mChipOther.mNeedExchangeDB = true;
                sendSetRadioCapabilitySuccess(false);
            } else {
                int expectedMaxCapabilitySubId = this.mChipOther.getExpectedMaxCapabilitySubId(slotId);
                if (-1 != expectedMaxCapabilitySubId) {
                    logd("setDefault4GSlot:setMaxRadioCapability, expectedMaxCapabilitySubId = " + expectedMaxCapabilitySubId);
                    setRadioCapability(expectedMaxCapabilitySubId);
                } else {
                    logd("setDefault4GSlot:don't setMaxRadioCapability, response message");
                    sendSetRadioCapabilitySuccess(false);
                }
            }
        } else {
            loge("setDefault4GSlot: radio is unavailable, return with failure");
            this.mChipCommon.sendResponseToTarget(response, 2);
            this.mChipCommon.mSet4GSlotCompleteMsg = null;
            this.mStateHandler.obtainMessage(HwFullNetworkConstants.EVENT_TRANS_TO_DEFAULT).sendToTarget();
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
