package com.android.internal.telephony.fullnetwork;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.telephony.HwTelephonyManager;
import android.telephony.HwTelephonyManagerInner;
import com.android.internal.telephony.HwSubscriptionManager;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.SubscriptionManagerEx;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.PhoneFactoryExt;
import com.huawei.internal.telephony.ProxyControllerEx;
import com.huawei.internal.telephony.RadioAccessFamilyEx;

public class HwFullNetworkSetStateQcomMtkBase extends HwFullNetworkSetStateBase {
    private static final String LOG_TAG = "HwFullNetworkSetStateQcomMtkBase";
    private static final int MESSAGE_PENDING_DELAY = 500;
    private static final int RETRY_MAX_TIME = 20;
    protected boolean isSwitchSlot = true;
    protected HwFullNetworkChipOther mChipOther = null;
    private int retryCount = 0;
    private boolean updateUserDefaultFlag = false;

    HwFullNetworkSetStateQcomMtkBase(Context c, CommandsInterfaceEx[] ci, Handler h) {
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
        switch (msg.what) {
            case HwFullNetworkConstantsInner.EVENT_SET_MAIN_SLOT_DONE:
                logd("Received EVENT_SET_MAIN_SLOT_DONE on index " + index);
                setMainSlotDone(msg, index.intValue());
                return;
            case HwFullNetworkConstantsInner.EVENT_SET_PREF_NETWORK_TIMEOUT:
                logd("EVENT_SET_PREF_NETWORK_TIMEOUT");
                this.mChipCommon.sendResponseToTarget(this.mChipCommon.mSet4GSlotCompleteMsg, 2);
                this.mChipCommon.mSet4GSlotCompleteMsg = null;
                this.mChipCommon.isSet4GSlotInProgress = false;
                this.updateUserDefaultFlag = false;
                this.retryCount = 20;
                return;
            case HwFullNetworkConstantsInner.MSG_RETRY_SET_DEFAULT_LTESLOT:
                logd("MSG_RETRY_SET_DEFAULT_LTESLOT");
                setPrefNetworkTypeAndStartTimer(msg.arg1);
                return;
            default:
                logd("Unknown msg:" + msg.what);
                return;
        }
    }

    private void setPrefNetworkTypeAndStartTimer(int slotId) {
        startSetPrefNetworkTimer();
        ProxyControllerEx.getInstance().retrySetRadioCapabilities();
    }

    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkSetStateBase
    public void setMainSlot(int slotId, Message response) {
    }

    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkSetStateBase
    public void setMainSlotDone(Message response, int index) {
        if (hasMessages(HwFullNetworkConstantsInner.EVENT_SET_PREF_NETWORK_TIMEOUT)) {
            removeMessages(HwFullNetworkConstantsInner.EVENT_SET_PREF_NETWORK_TIMEOUT);
        }
        AsyncResultEx ar = AsyncResultEx.from(response.obj);
        if (ar == null || ar.getException() != null) {
            this.mChipOther.refreshCardState();
            loge("EVENT_SET_MAIN_SLOT_DONE failed ,response GENERIC_FAILURE");
            if (!this.mChipCommon.isSimInsertedArray[this.mChipCommon.default4GSlot]) {
                this.retryCount = 20;
                logd("current app destoryed, this error don't retry and set retryCount to max value");
            }
            loge("handleMessage: EVENT_SET_PREF_NETWORK_DONE failed for slot: " + index + "with retryCount = " + this.retryCount);
            if (this.retryCount < 20) {
                this.retryCount++;
                sendMessageDelayed(obtainMessage(HwFullNetworkConstantsInner.MSG_RETRY_SET_DEFAULT_LTESLOT, index, 0, 0), 500);
                return;
            }
            this.mChipCommon.sendResponseToTarget(this.mChipCommon.mSet4GSlotCompleteMsg, 2);
            this.retryCount = 0;
        } else {
            handleSetMainSlotResponseSuccess(index);
        }
        this.mChipCommon.mSet4GSlotCompleteMsg = null;
        this.mChipCommon.isSet4GSlotInProgress = false;
        this.updateUserDefaultFlag = false;
        this.mStateHandler.obtainMessage(HwFullNetworkConstantsInner.EVENT_TRANS_TO_DEFAULT).sendToTarget();
    }

    private void handleSetMainSlotResponseSuccess(int index) {
        HwTelephonyManagerInner.getDefault().updateCrurrentPhone(index);
        this.mChipCommon.saveMainCardIccId(this.mChipCommon.getIccId(index));
        this.mChipCommon.sendResponseToTarget(this.mChipCommon.mSet4GSlotCompleteMsg, 0);
        if (this.mChipCommon.mSet4GSlotCompleteMsg == null) {
            logd("slience set network mode done, prepare to set DDS for slot " + index);
            HwTelephonyManagerInner.getDefault().setDefaultDataSlotId(index);
            if (PhoneFactoryExt.onDataSubChange() == 0) {
                for (int i = 0; i < HwFullNetworkConstantsInner.SIM_NUM; i++) {
                    PhoneFactoryExt.resendDataAllowed(i);
                    logd("EVENT_SET_PREF_NETWORK_DONE resend data allow with slot " + i);
                }
            }
        }
        if (this.updateUserDefaultFlag) {
            Settings.Global.putInt(this.mContext.getContentResolver(), HwFullNetworkConstantsInner.USER_DEFAULT_SUBSCRIPTION, index);
        }
        HwSubscriptionManager.getInstance().setUserPrefDefaultSlotId(index);
        this.retryCount = 0;
    }

    /* access modifiers changed from: protected */
    public void setRadioCapability(int ddsSlotId) {
        int raf;
        ProxyControllerEx proxyController = ProxyControllerEx.getInstance();
        PhoneExt[] phones = null;
        try {
            phones = PhoneFactoryExt.getPhones();
        } catch (IllegalStateException e) {
            logd("getPhones IllegalStateException.");
        } catch (Exception e2) {
            logd("getPhones exception.");
        }
        if (SubscriptionManagerEx.isValidSlotIndex(ddsSlotId) && phones != null) {
            RadioAccessFamilyEx[] rafs = new RadioAccessFamilyEx[phones.length];
            boolean atLeastOneMatch = false;
            for (int phoneId = 0; phoneId < phones.length; phoneId++) {
                PhoneExt phoneExt = phones[phoneId];
                if (phoneId == ddsSlotId) {
                    raf = proxyController.getMaxRafSupported();
                    atLeastOneMatch = true;
                } else {
                    raf = proxyController.getMinRafSupported();
                }
                logd("[setMaxRadioCapability] phoneId=" + phoneId + " ddsSlotId=" + ddsSlotId + " raf=" + raf);
                rafs[phoneId] = new RadioAccessFamilyEx(phoneId, raf);
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
        Message response = obtainMessage(HwFullNetworkConstantsInner.EVENT_SET_MAIN_SLOT_DONE, Integer.valueOf(this.mChipCommon.expectedDDSsubId));
        AsyncResultEx.forMessage(response, (Object) null, (Throwable) null);
        response.sendToTarget();
        sendHwSwitchSlotDoneBroadcast(this.mChipCommon.expectedDDSsubId);
        for (int slotId = 0; slotId < HwFullNetworkConstantsInner.SIM_NUM; slotId++) {
            if (this.mChipOther.mSetUiccSubscriptionResult[slotId] != -1) {
                boolean active = this.mChipOther.mSetUiccSubscriptionResult[slotId] == 1;
                logd("sendSetRadioCapabilitySuccess,setSubscription: slotId = " + slotId + ", activate = " + active);
                HwTelephonyManager.getDefault().setSubscription(slotId, active, (Message) null);
            }
        }
        boolean isCmccSwithchDisableNeedExchange = this.mChipOther.mNeedExchangeDB && HwFullNetworkConfigInner.isCMCCDsdxDisable();
        if (HwFullNetworkConfigInner.IS_QCOM_DUAL_LTE_STACK && (!this.mChipCommon.isDualImsSwitchOpened() || isCmccSwithchDisableNeedExchange || HwTelephonyManager.getDefault().isNrSupported())) {
            this.mChipOther.mNeedSetLteServiceAbility = true;
            this.mChipOther.mNeedExchangeDB = false;
            exchangeNetworkTypeInDB();
        } else if (needChangeNetworkTypeInDB) {
            exchangeNetworkTypeInDB();
        } else {
            logd("no need ChangeNetwork Type In DB.");
        }
        this.mChipOther.setServiceAbility();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkSetStateBase
    public void setRadioCapabilityDone(Intent intent) {
        sendSetRadioCapabilitySuccess(true);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkSetStateBase
    public void setRadioCapabilityFailed(Intent intent) {
        Message response = obtainMessage(HwFullNetworkConstantsInner.EVENT_SET_MAIN_SLOT_DONE, Integer.valueOf(this.mChipCommon.expectedDDSsubId));
        AsyncResultEx.forMessage(response, (Object) null, new Exception());
        response.sendToTarget();
    }

    private void startSetPrefNetworkTimer() {
        Message message = obtainMessage(HwFullNetworkConstantsInner.EVENT_SET_PREF_NETWORK_TIMEOUT);
        AsyncResultEx.forMessage(message, (Object) null, (Throwable) null);
        sendMessageDelayed(message, 60000);
        logd("startSetPrefNetworkTimer!");
    }

    private void exchangeNetworkTypeInDB() {
        if (!this.isSwitchSlot) {
            logd("exchangeNetworkTypeInDB not need switch");
            return;
        }
        int previousNetworkTypeSub0 = this.mChipOther.getNetworkTypeFromDB(0);
        int previousNetworkTypeSub1 = this.mChipOther.getNetworkTypeFromDB(1);
        logd("exchangeNetworkTypeInDB PREFERRED_NETWORK_MODE:" + previousNetworkTypeSub0 + "," + previousNetworkTypeSub1 + "->" + previousNetworkTypeSub1 + "," + previousNetworkTypeSub0);
        this.mChipOther.setNetworkTypeToDB(0, previousNetworkTypeSub1);
        this.mChipOther.setNetworkTypeToDB(1, previousNetworkTypeSub0);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkSetStateBase
    public void logd(String msg) {
        RlogEx.d(LOG_TAG, msg);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkSetStateBase
    public void loge(String msg) {
        RlogEx.e(LOG_TAG, msg);
    }
}
