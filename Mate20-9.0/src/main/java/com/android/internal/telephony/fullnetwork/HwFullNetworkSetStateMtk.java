package com.android.internal.telephony.fullnetwork;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.HwSubscriptionManager;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.ProxyController;
import com.android.internal.telephony.RadioCapability;

public class HwFullNetworkSetStateMtk extends HwFullNetworkSetStateQcomMtkBase {
    private static final String LOG_TAG = "HwFullNetworkSetStateMtk";
    private static final String MAIN_STACK = "modem_sys1_ps0";

    HwFullNetworkSetStateMtk(Context c, CommandsInterface[] ci, Handler h) {
        super(c, ci, h);
        this.mChipOther = HwFullNetworkChipOther.getInstance();
        logd("HwFullNetworkSetStateMtk constructor");
    }

    public void setMainSlot(int slotId, Message response) {
        int needSetCount;
        this.mChipCommon.expectedDDSsubId = slotId;
        if (!isNeedSetRadioCapability(slotId)) {
            loge("setDefault4GSlot: there is no need to set the lte slot");
            this.mChipCommon.saveMainCardIccId(this.mChipCommon.getIccId(slotId));
            this.mChipCommon.isSet4GSlotInProgress = false;
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
            this.mChipOther.mNeedExchangeDB = true;
            sendHwSwitchSlotStartBroadcast();
            this.mChipCommon.mSet4GSlotCompleteMsg = response;
            this.mChipCommon.isSet4GSlotInProgress = true;
            this.mChipCommon.current4GSlotBackup = this.mChipCommon.getUserSwitchDualCardSlots();
            logd("setDefault4GSlot:setMaxRadioCapability, slotId = " + slotId);
            ProxyController.getInstance().setRadioCapability(slotId, slotId);
        } else {
            loge("setDefault4GSlot: radio is unavailable, return with failure");
            this.mChipCommon.sendResponseToTarget(response, 2);
            this.mChipCommon.mSet4GSlotCompleteMsg = null;
            this.mStateHandler.obtainMessage(HwFullNetworkConstants.EVENT_TRANS_TO_DEFAULT).sendToTarget();
        }
    }

    private boolean isNeedSetRadioCapability(int expectedMainSlotId) {
        Phone[] mPhones = PhoneFactory.getPhones();
        if (mPhones == null) {
            logd("isNeedSetRadioCapability: mPhones is null");
            return false;
        }
        if (SubscriptionManager.isValidSlotIndex(expectedMainSlotId) && mPhones[expectedMainSlotId] != null) {
            RadioCapability expectedMainSlotRC = mPhones[expectedMainSlotId].getRadioCapability();
            if (expectedMainSlotRC != null && !MAIN_STACK.equals(expectedMainSlotRC.getLogicalModemUuid())) {
                logd("isNeedSetRadioCapability: need switch LogicalModemUuid for expectedMainSlotId");
                return true;
            }
        }
        logd("isNeedSetRadioCapability: do not need set radio capability again, now main stack is already ok");
        return false;
    }

    /* access modifiers changed from: protected */
    public void setRadioCapabilityDone(Intent intent) {
        sendSetRadioCapabilitySuccess(!this.mChipCommon.isDualImsSwitchOpened());
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
