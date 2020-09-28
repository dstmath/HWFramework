package com.android.internal.telephony.fullnetwork;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.telephony.HwTelephonyManagerInner;
import com.android.internal.telephony.HwSubscriptionManager;
import com.huawei.android.telephony.RlogEx;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.PhoneFactoryExt;

public class HwFullNetworkSetStateQcom2_0 extends HwFullNetworkSetStateQcomMtkBase {
    private static final String LOG_TAG = "HwFullNetworkSetStateQcom2_0";

    HwFullNetworkSetStateQcom2_0(Context c, CommandsInterfaceEx[] ci, Handler h) {
        super(c, ci, h);
        this.mChipOther = HwFullNetworkChipOther.getInstance();
        logd("HwFullNetworkSetStateQcom2_0 constructor");
    }

    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkSetStateBase, com.android.internal.telephony.fullnetwork.HwFullNetworkSetStateQcomMtkBase
    public void setMainSlot(int slotId, Message response) {
        this.mChipCommon.expectedDDSsubId = slotId;
        if (!this.mChipOther.isSetDefault4GSlotNeeded(slotId)) {
            handleNoNeedSetDefault4GSlot(slotId);
        } else if (this.mCis[slotId] == null || this.mCis[slotId].getRadioState() != 2) {
            logd("setDefault4GSlot: target slot id is: " + slotId + " response:" + response);
            sendHwSwitchSlotStartBroadcast();
            this.mChipCommon.mSet4GSlotCompleteMsg = response;
            this.mChipCommon.isSet4GSlotInProgress = true;
            this.mChipCommon.current4GSlotBackup = this.mChipCommon.getUserSwitchDualCardSlots();
            if (HwFullNetworkConfigInner.IS_QCOM_DUAL_LTE_STACK) {
                this.mChipOther.mNeedExchangeDB = true;
                sendSetRadioCapabilitySuccess(false);
                return;
            }
            int expectedMaxCapabilitySubId = this.mChipOther.getExpectedMaxCapabilitySubId(slotId);
            if (-1 != expectedMaxCapabilitySubId) {
                logd("setDefault4GSlot:setMaxRadioCapability, expectedMaxCapabilitySubId = " + expectedMaxCapabilitySubId);
                setRadioCapability(expectedMaxCapabilitySubId);
                return;
            }
            logd("setDefault4GSlot:don't setMaxRadioCapability, response message");
            sendSetRadioCapabilitySuccess(false);
        } else {
            loge("setDefault4GSlot: radio is unavailable, return with failure");
            this.mChipCommon.sendResponseToTarget(response, 2);
            this.mChipCommon.mSet4GSlotCompleteMsg = null;
            this.mStateHandler.obtainMessage(HwFullNetworkConstantsInner.EVENT_TRANS_TO_DEFAULT).sendToTarget();
        }
    }

    private void handleNoNeedSetDefault4GSlot(int slotId) {
        loge("setDefault4GSlot: there is no need to set the lte slot");
        this.mChipCommon.saveMainCardIccId(this.mChipCommon.getIccId(slotId));
        this.mChipCommon.isSet4GSlotInProgress = false;
        if (HwFullNetworkConfigInner.IS_CARD2_CDMA_SUPPORTED && this.mChipCommon.mSet4GSlotCompleteMsg == null) {
            logd("In auto set 4GSlot mode , makesure DDS slot same as 4G slot so set DDS to slot: " + slotId);
            HwTelephonyManagerInner.getDefault().setDefaultDataSlotId(slotId);
        }
        this.mChipOther.setServiceAbility();
        logd("set DDS to slot: " + slotId);
        HwTelephonyManagerInner.getDefault().setDefaultDataSlotId(slotId);
        HwSubscriptionManager.getInstance().setUserPrefDefaultSlotId(slotId);
        int needSetCount = PhoneFactoryExt.onDataSubChange();
        logd("needSetCount = " + needSetCount + "; mNeedSetAllowData = " + this.mChipOther.mNeedSetAllowData);
        if (needSetCount == 0 && this.mChipOther.mNeedSetAllowData) {
            this.mChipOther.mNeedSetAllowData = false;
            for (int i = 0; i < HwFullNetworkConstantsInner.SIM_NUM; i++) {
                PhoneFactoryExt.resendDataAllowed(i);
                logd("setDefault4GSlot resend data allow with slot " + i);
            }
        }
        this.mChipCommon.sendResponseToTarget(this.mChipCommon.mSet4GSlotCompleteMsg, 0);
        this.mChipCommon.mSet4GSlotCompleteMsg = null;
        this.mStateHandler.obtainMessage(HwFullNetworkConstantsInner.EVENT_TRANS_TO_DEFAULT).sendToTarget();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkSetStateBase, com.android.internal.telephony.fullnetwork.HwFullNetworkSetStateQcomMtkBase
    public void logd(String msg) {
        RlogEx.d(LOG_TAG, msg);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkSetStateBase, com.android.internal.telephony.fullnetwork.HwFullNetworkSetStateQcomMtkBase
    public void loge(String msg) {
        RlogEx.e(LOG_TAG, msg);
    }
}
