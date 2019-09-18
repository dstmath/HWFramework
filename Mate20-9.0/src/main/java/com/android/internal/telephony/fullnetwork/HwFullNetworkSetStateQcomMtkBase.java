package com.android.internal.telephony.fullnetwork;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.RadioAccessFamily;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.HwSubscriptionManager;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.ProxyController;

public class HwFullNetworkSetStateQcomMtkBase extends HwFullNetworkSetStateBase {
    private static final String LOG_TAG = "HwFullNetworkSetStateQcomMtkBase";
    private static final int MESSAGE_PENDING_DELAY = 500;
    private static final int RETRY_MAX_TIME = 20;
    protected HwFullNetworkChipOther mChipOther = null;
    private int retryCount = 0;
    private boolean updateUserDefaultFlag = false;

    HwFullNetworkSetStateQcomMtkBase(Context c, CommandsInterface[] ci, Handler h) {
        super(c, ci, h);
        logd("HwFullNetworkSetStateQcomMtkBase constructor");
    }

    public void handleMessage(Message msg) {
        if (msg == null || msg.obj == null) {
            loge("msg or msg.obj is null, return!");
            return;
        }
        Integer index = this.mChipCommon.getCiIndex(msg);
        if (index.intValue() < 0 || index.intValue() >= this.mCis.length) {
            loge("Invalid index : " + index + " received with event " + msg.what);
            return;
        }
        int i = msg.what;
        if (i != 401) {
            switch (i) {
                case HwFullNetworkConstants.EVENT_SET_PREF_NETWORK_TIMEOUT:
                    logd("EVENT_SET_PREF_NETWORK_TIMEOUT");
                    this.mChipCommon.sendResponseToTarget(this.mChipCommon.mSet4GSlotCompleteMsg, 2);
                    this.mChipCommon.mSet4GSlotCompleteMsg = null;
                    this.mChipCommon.isSet4GSlotInProgress = false;
                    this.updateUserDefaultFlag = false;
                    this.retryCount = 20;
                    break;
                case HwFullNetworkConstants.MSG_RETRY_SET_DEFAULT_LTESLOT:
                    logd("MSG_RETRY_SET_DEFAULT_LTESLOT");
                    setPrefNetworkTypeAndStartTimer(msg.arg1);
                    break;
                default:
                    logd("Unknown msg:" + msg.what);
                    break;
            }
        } else {
            logd("Received EVENT_SET_MAIN_SLOT_DONE on index " + index);
            setMainSlotDone(msg, index.intValue());
        }
    }

    private void setPrefNetworkTypeAndStartTimer(int slotId) {
        startSetPrefNetworkTimer();
        ProxyController.getInstance().retrySetRadioCapabilities();
    }

    public void setMainSlot(int slotId, Message response) {
    }

    public void setMainSlotDone(Message response, int index) {
        if (hasMessages(HwFullNetworkConstants.EVENT_SET_PREF_NETWORK_TIMEOUT)) {
            removeMessages(HwFullNetworkConstants.EVENT_SET_PREF_NETWORK_TIMEOUT);
        }
        AsyncResult ar = (AsyncResult) response.obj;
        if (ar == null || ar.exception != null) {
            this.mChipOther.refreshCardState();
            loge("EVENT_SET_MAIN_SLOT_DONE failed ,response GENERIC_FAILURE");
            if (!this.mChipCommon.isSimInsertedArray[this.mChipCommon.default4GSlot]) {
                this.retryCount = 20;
                logd("current app destoryed, this error don't retry and set retryCount to max value");
            }
            loge("handleMessage: EVENT_SET_PREF_NETWORK_DONE failed for slot: " + index + "with retryCount = " + this.retryCount);
            if (this.retryCount < 20) {
                this.retryCount++;
                sendMessageDelayed(obtainMessage(HwFullNetworkConstants.MSG_RETRY_SET_DEFAULT_LTESLOT, index, 0, 0), 500);
                return;
            }
            this.mChipCommon.sendResponseToTarget(this.mChipCommon.mSet4GSlotCompleteMsg, 2);
            this.retryCount = 0;
        } else {
            HwTelephonyManagerInner.getDefault().updateCrurrentPhone(index);
            this.mChipCommon.saveMainCardIccId(this.mChipCommon.getIccId(index));
            this.mChipCommon.sendResponseToTarget(this.mChipCommon.mSet4GSlotCompleteMsg, 0);
            if (this.mChipCommon.mSet4GSlotCompleteMsg == null) {
                logd("slience set network mode done, prepare to set DDS for slot " + index);
                HwTelephonyManagerInner.getDefault().setDefaultDataSlotId(index);
                if (PhoneFactory.onDataSubChange(0, null) == 0) {
                    for (int i = 0; i < HwFullNetworkConstants.SIM_NUM; i++) {
                        PhoneFactory.resendDataAllowed(i);
                        logd("EVENT_SET_PREF_NETWORK_DONE resend data allow with slot " + i);
                    }
                }
            }
            if (this.updateUserDefaultFlag != 0) {
                Settings.Global.putInt(this.mContext.getContentResolver(), HwFullNetworkConstants.USER_DEFAULT_SUBSCRIPTION, index);
            }
            HwSubscriptionManager.getInstance().setUserPrefDefaultSlotId(index);
            this.retryCount = 0;
        }
        this.mChipCommon.mSet4GSlotCompleteMsg = null;
        this.mChipCommon.isSet4GSlotInProgress = false;
        this.updateUserDefaultFlag = false;
        this.mStateHandler.obtainMessage(HwFullNetworkConstants.EVENT_TRANS_TO_DEFAULT).sendToTarget();
    }

    /* access modifiers changed from: protected */
    public void setRadioCapability(int ddsSubId) {
        int raf;
        ProxyController proxyController = ProxyController.getInstance();
        Phone[] phones = null;
        try {
            phones = PhoneFactory.getPhones();
        } catch (Exception ex) {
            logd("getPhones exception:" + ex.getMessage());
        }
        if (SubscriptionManager.isValidSubscriptionId(ddsSubId) && phones != null) {
            RadioAccessFamily[] rafs = new RadioAccessFamily[phones.length];
            boolean atLeastOneMatch = false;
            for (int phoneId = 0; phoneId < phones.length; phoneId++) {
                int id = phones[phoneId].getSubId();
                if (id == ddsSubId) {
                    raf = proxyController.getMaxRafSupported();
                    atLeastOneMatch = true;
                } else {
                    raf = proxyController.getMinRafSupported();
                }
                logd("[setMaxRadioCapability] phoneId=" + phoneId + " subId=" + id + " raf=" + raf);
                rafs[phoneId] = new RadioAccessFamily(phoneId, raf);
            }
            if (atLeastOneMatch) {
                proxyController.setRadioCapability(rafs);
                startSetPrefNetworkTimer();
                return;
            }
            logd("[setMaxRadioCapability] no valid subId's found - not updating.");
        }
    }

    /* access modifiers changed from: protected */
    public void sendSetRadioCapabilitySuccess(boolean needChangeNetworkTypeInDB) {
        logd("sendSetRadioCapabilitySuccess,needChangeNetworkTypeInDB:" + needChangeNetworkTypeInDB);
        Message response = obtainMessage(HwFullNetworkConstants.EVENT_SET_MAIN_SLOT_DONE, Integer.valueOf(this.mChipCommon.expectedDDSsubId));
        AsyncResult.forMessage(response, null, null);
        response.sendToTarget();
        sendHwSwitchSlotDoneBroadcast(this.mChipCommon.expectedDDSsubId);
        int slotId = 0;
        while (true) {
            boolean active = true;
            if (slotId >= HwFullNetworkConstants.SIM_NUM) {
                break;
            }
            if (-1 != this.mChipOther.mSetUiccSubscriptionResult[slotId]) {
                if (this.mChipOther.mSetUiccSubscriptionResult[slotId] != 1) {
                    active = false;
                }
                logd("sendSetRadioCapabilitySuccess,setSubscription: slotId = " + slotId + ", activate = " + active);
                HwSubscriptionManager.getInstance().setSubscription(slotId, active, null);
            }
            slotId++;
        }
        if (HwFullNetworkConfig.IS_QCOM_DUAL_LTE_STACK && (!this.mChipCommon.isDualImsSwitchOpened() || (this.mChipOther.mNeedExchangeDB && HwFullNetworkConfig.IS_CMCC_4GSWITCH_DISABLE))) {
            this.mChipOther.mNeedSetLteServiceAbility = true;
            this.mChipOther.mNeedExchangeDB = false;
            exchangeNetworkTypeInDB();
        } else if (needChangeNetworkTypeInDB) {
            exchangeNetworkTypeInDB();
        }
        this.mChipOther.setLteServiceAbility();
    }

    /* access modifiers changed from: protected */
    public void setRadioCapabilityDone(Intent intent) {
        sendSetRadioCapabilitySuccess(true);
    }

    /* access modifiers changed from: protected */
    public void setRadioCapabilityFailed(Intent intent) {
        Message response = obtainMessage(HwFullNetworkConstants.EVENT_SET_MAIN_SLOT_DONE, Integer.valueOf(this.mChipCommon.expectedDDSsubId));
        AsyncResult.forMessage(response, null, new Exception());
        response.sendToTarget();
    }

    private void startSetPrefNetworkTimer() {
        Message message = obtainMessage(HwFullNetworkConstants.EVENT_SET_PREF_NETWORK_TIMEOUT);
        AsyncResult.forMessage(message, null, null);
        sendMessageDelayed(message, 60000);
        logd("startSetPrefNetworkTimer!");
    }

    private void exchangeNetworkTypeInDB() {
        int previousNetworkTypeSub0 = this.mChipOther.getNetworkTypeFromDB(0);
        int previousNetworkTypeSub1 = this.mChipOther.getNetworkTypeFromDB(1);
        logd("exchangeNetworkTypeInDB PREFERRED_NETWORK_MODE:" + previousNetworkTypeSub0 + "," + previousNetworkTypeSub1 + "->" + previousNetworkTypeSub1 + "," + previousNetworkTypeSub0);
        this.mChipOther.setNetworkTypeToDB(0, previousNetworkTypeSub1);
        this.mChipOther.setNetworkTypeToDB(1, previousNetworkTypeSub0);
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
