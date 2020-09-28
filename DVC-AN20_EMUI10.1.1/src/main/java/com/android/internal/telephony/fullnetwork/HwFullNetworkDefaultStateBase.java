package com.android.internal.telephony.fullnetwork;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.telephony.HwTelephonyManager;
import android.telephony.HwTelephonyManagerInner;
import android.text.TextUtils;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.PhoneFactoryExt;

public abstract class HwFullNetworkDefaultStateBase extends Handler {
    protected HwFullNetworkChipCommon mChipCommon;
    protected CommandsInterfaceEx[] mCis;
    protected Context mContext;
    private boolean mNeedToSetMainSlotByBroadcast = false;
    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.android.internal.telephony.fullnetwork.HwFullNetworkDefaultStateBase.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                HwFullNetworkDefaultStateBase.this.loge("intent is null, return");
            } else if ("com.huawei.intent.action.ACTION_SUBSCRIPTION_SET_UICC_RESULT".equals(intent.getAction())) {
                HwFullNetworkDefaultStateBase.this.logd("received ACTION_SUBSCRIPTION_SET_UICC_RESULT");
                HwFullNetworkDefaultStateBase.this.processSubSetUiccResult(intent);
            } else if ("com.huawei.devicepolicy.action.POLICY_CHANGED".equals(intent.getAction())) {
                HwFullNetworkDefaultStateBase.this.logd("received ACTION_MDM_POLICY_CHANGED");
                HwFullNetworkDefaultStateBase.this.processMdmPolicyChanged(intent);
            } else if ("android.intent.action.PRE_BOOT_COMPLETED".equals(intent.getAction())) {
                HwFullNetworkDefaultStateBase.this.logd("received ACTION_PRE_BOOT_COMPLETED");
                HwFullNetworkDefaultStateBase.this.processPreBootCompleted();
            } else if ("android.intent.action.SERVICE_STATE".equals(intent.getAction())) {
                HwFullNetworkDefaultStateBase.this.logd("received ACTION_SERVICE_STATE_CHANGED");
                HwFullNetworkDefaultStateBase.this.onServiceStateChangedForCMCC(intent);
            } else if ("com.huawei.action.ACTION_HW_SWITCH_SLOT_DONE".equals(intent.getAction())) {
                HwFullNetworkDefaultStateBase.this.logd("received ACTION_HW_SWITCH_SLOT_DONE, mNeedToSetMainSlotByBroadcast:" + HwFullNetworkDefaultStateBase.this.mNeedToSetMainSlotByBroadcast);
                HwFullNetworkDefaultStateBase.this.onSwitchSlotDone(intent);
            } else {
                HwFullNetworkDefaultStateBase.this.logd("received ACTION not match");
            }
        }
    };
    protected Handler mStateHandler;

    /* access modifiers changed from: protected */
    public abstract void logd(String str);

    /* access modifiers changed from: protected */
    public abstract void loge(String str);

    /* access modifiers changed from: protected */
    public abstract void onRadioUnavailable(Integer num);

    /* access modifiers changed from: protected */
    public abstract void onServiceStateChangedForCMCC(Intent intent);

    /* access modifiers changed from: protected */
    public abstract void processPreBootCompleted();

    /* access modifiers changed from: protected */
    public abstract void processSubSetUiccResult(Intent intent);

    /* access modifiers changed from: protected */
    public abstract void setLteServiceAbilityForQCOM(int i, int i2, int i3);

    /* access modifiers changed from: protected */
    public abstract void setMainSlot(int i, Message message);

    /* access modifiers changed from: protected */
    public abstract void setServiceAbilityForQCOM(int i, int i2, int i3, int i4);

    public HwFullNetworkDefaultStateBase(Context c, CommandsInterfaceEx[] ci, Handler h) {
        this.mContext = c;
        this.mCis = ci;
        this.mStateHandler = h;
        this.mChipCommon = HwFullNetworkChipCommon.getInstance();
        for (int i = 0; i < this.mCis.length; i++) {
            Integer index = Integer.valueOf(i);
            this.mCis[i].registerForNotAvailable(this, (int) HwFullNetworkConstantsInner.EVENT_RADIO_UNAVAILABLE, index);
            this.mCis[i].registerForAvailable(this, (int) HwFullNetworkConstantsInner.EVENT_RADIO_AVAILABLE, index);
            this.mChipCommon.mSubscriptionStatus[i] = -1;
        }
        IntentFilter filter = new IntentFilter("com.huawei.devicepolicy.action.POLICY_CHANGED");
        if (HwFullNetworkConfigInner.isCMCCDsdxDisable() || HwFullNetworkConfigInner.IS_CT_4GSWITCH_DISABLE || HwTelephonyManagerInner.getDefault().getDefaultMainSlotCarrier() != 0) {
            filter.addAction("android.intent.action.PRE_BOOT_COMPLETED");
        }
        filter.addAction("android.intent.action.SERVICE_STATE");
        this.mContext.registerReceiver(this.mReceiver, filter);
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
            case HwFullNetworkConstantsInner.EVENT_RADIO_UNAVAILABLE:
                logd("Received EVENT_RADIO_UNAVAILABLE on index " + index);
                onRadioUnavailable(index);
                return;
            case HwFullNetworkConstantsInner.EVENT_RADIO_AVAILABLE:
                logd("Received EVENT_RADIO_AVAILABLE on index " + index);
                onRadioAvailable(index);
                return;
            case HwFullNetworkConstantsInner.EVENT_VOICE_CALL_ENDED:
                logd("Received EVENT_VOICE_CALL_ENDED on index " + index);
                onVoiceCallEnded(index);
                return;
            default:
                return;
        }
    }

    /* access modifiers changed from: protected */
    public void onRadioAvailable(Integer index) {
        if (!(HwFullNetworkConstantsInner.SIM_NUM != 2 || this.mChipCommon.isVoiceCallEndedRegistered || HwFullNetworkConfigInner.IS_CHINA_TELECOM)) {
            PhoneExt[] phones = PhoneFactoryExt.getPhones();
            for (PhoneExt phoneExt : phones) {
                if (phoneExt != null) {
                    logd("registerForVoiceCallEnded for PhoneExt " + phoneExt.getPhoneId());
                    phoneExt.registerForVoiceCallEnded(this, (int) HwFullNetworkConstantsInner.EVENT_VOICE_CALL_ENDED, Integer.valueOf(phoneExt.getPhoneId()));
                }
            }
            this.mChipCommon.isVoiceCallEndedRegistered = true;
        }
    }

    /* access modifiers changed from: protected */
    public void onVoiceCallEnded(Integer index) {
        int otherSub;
        boolean isTlCmccHybird;
        boolean isCtHybird;
        logd("onVoiceCallEnded");
        if (index.intValue() == 0) {
            otherSub = 1;
        } else {
            otherSub = 0;
        }
        if (!(HwTelephonyManager.getDefault().getSubState((long) otherSub) == 1 || HwTelephonyManager.getDefault().getSubState((long) index.intValue()) != 1 || index.intValue() == this.mChipCommon.getUserSwitchDualCardSlots())) {
            this.mStateHandler.sendMessage(this.mStateHandler.obtainMessage(HwFullNetworkConstantsInner.EVENT_SET_MAIN_SLOT, index.intValue(), -1));
        }
        int cmccSlotId = this.mChipCommon.getCMCCCardSlotId();
        if ((HwFullNetworkConfigInner.isCMCCDsdxDisable() || HwTelephonyManagerInner.getDefault().getDefaultMainSlotCarrier() == 1) && this.mChipCommon.isCMCCHybird()) {
            isTlCmccHybird = true;
        } else {
            isTlCmccHybird = false;
        }
        if (isTlCmccHybird && HwTelephonyManager.getDefault().getSubState((long) cmccSlotId) == 1 && cmccSlotId != this.mChipCommon.getUserSwitchDualCardSlots()) {
            this.mStateHandler.sendMessage(this.mStateHandler.obtainMessage(HwFullNetworkConstantsInner.EVENT_SET_MAIN_SLOT, cmccSlotId, -1));
        }
        int ctSlotId = this.mChipCommon.getCTCardSlotId();
        if (HwTelephonyManagerInner.getDefault().getDefaultMainSlotCarrier() != 2 || !this.mChipCommon.isCTHybird()) {
            isCtHybird = false;
        } else {
            isCtHybird = true;
        }
        if (isCtHybird && HwTelephonyManager.getDefault().getSubState((long) ctSlotId) == 1 && ctSlotId != this.mChipCommon.getUserSwitchDualCardSlots()) {
            this.mStateHandler.sendMessage(this.mStateHandler.obtainMessage(HwFullNetworkConstantsInner.EVENT_SET_MAIN_SLOT, ctSlotId, -1));
        }
        if (index.intValue() == 1 && HwTelephonyManager.getDefault().getSubState((long) index.intValue()) == 1 && HwTelephonyManagerInner.getDefault().isDataConnectivityDisabled(1, "disable-sub")) {
            HwTelephonyManager.getDefault().setSubscription(index.intValue(), false, (Message) null);
        }
    }

    public void processMdmPolicyChanged(Intent intent) {
        boolean isSub0Active;
        boolean isBothCardsPresent;
        if (intent != null) {
            String actionTag = intent.getStringExtra("action_tag");
            if (!TextUtils.isEmpty(actionTag) && actionTag.equals("action_disable_data_4G")) {
                int targetId = intent.getIntExtra("subId", -1);
                this.mChipCommon.isSet4GSlotInProgress = false;
                boolean dataState = intent.getBooleanExtra("dataState", false);
                if (HwTelephonyManager.getDefault().getSubState(0) == 1) {
                    isSub0Active = true;
                } else {
                    isSub0Active = false;
                }
                if (!this.mChipCommon.isCardPresent(0) || !this.mChipCommon.isCardPresent(1)) {
                    isBothCardsPresent = false;
                } else {
                    isBothCardsPresent = true;
                }
                if (isBothCardsPresent && dataState && isSub0Active) {
                    Message msg = this.mStateHandler.obtainMessage(HwFullNetworkConstantsInner.EVENT_SET_MAIN_SLOT);
                    msg.arg1 = targetId;
                    this.mStateHandler.sendMessage(msg);
                }
            }
            if ("action_default_main_slot_carrier".equals(actionTag)) {
                processMdmCarrierPolicyChanged(intent);
            }
        }
    }

    private void processMdmCarrierPolicyChanged(Intent intent) {
        boolean isAllCardsActive = true;
        String carrier = intent.getStringExtra("carrier");
        boolean isCMCCHybird = this.mChipCommon.isCMCCHybird();
        boolean isCTHybird = this.mChipCommon.isCTHybird();
        int userDefaultMainSlot = this.mChipCommon.getUserSwitchDualCardSlots();
        int defaultMainSlotMDMCarrier = userDefaultMainSlot;
        logd("processMdmPolicyChanged userDefaultMainSlot = " + userDefaultMainSlot);
        if ("cmcc".equals(carrier) && isCMCCHybird) {
            defaultMainSlotMDMCarrier = HwFullNetworkOperatorFactory.getOperatorCMCCMDMCarrier().getDefaultMainSlot(true);
            logd("processMdmPolicyChanged defaultMainSlotMDMCarrier CMCC = " + defaultMainSlotMDMCarrier);
        } else if (!"ct".equals(carrier) || !isCTHybird) {
            logd("processMdmPolicyChanged invaild value");
        } else {
            defaultMainSlotMDMCarrier = HwFullNetworkOperatorFactory.getOperatorCTMDMCarrier().getDefaultMainSlot(true);
            logd("processMdmPolicyChanged defaultMainSlotMDMCarrier CT = " + defaultMainSlotMDMCarrier);
        }
        if (!(HwTelephonyManager.getDefault().getSubState(0) == 1 && HwTelephonyManager.getDefault().getSubState(1) == 1)) {
            isAllCardsActive = false;
        }
        if (isAllCardsActive && defaultMainSlotMDMCarrier != userDefaultMainSlot && !this.mChipCommon.getWaitingSwitchBalongSlot()) {
            logd("processMdmPolicyChanged set main slotId:" + defaultMainSlotMDMCarrier);
            Message msg = this.mStateHandler.obtainMessage(HwFullNetworkConstantsInner.EVENT_SET_MAIN_SLOT);
            msg.arg1 = defaultMainSlotMDMCarrier;
            this.mStateHandler.sendMessage(msg);
        }
    }

    public void forceSetDefault4GSlotForCMCC(int cmccSlotId) {
        logd("forceSetDefault4GSlotForCMCC cmccSlotId:" + cmccSlotId);
        this.mStateHandler.obtainMessage(HwFullNetworkConstantsInner.EVENT_FORCE_CHECK_MAIN_SLOT_FOR_CMCC, Integer.valueOf(cmccSlotId)).sendToTarget();
    }

    /* access modifiers changed from: protected */
    public void handleSetUiccSubscriptionDone(Intent intent) {
        if (intent != null) {
            int slotId = intent.getIntExtra("phone", -1);
            int subStatus = intent.getIntExtra("newSubState", -1);
            if (slotId >= this.mChipCommon.mSubscriptionStatus.length || slotId < 0) {
                logd("Invalid slotId:" + slotId);
                return;
            }
            this.mChipCommon.mSubscriptionStatus[slotId] = subStatus;
            boolean isReady = true;
            for (int i = 0; i < this.mChipCommon.mSubscriptionStatus.length; i++) {
                if (this.mChipCommon.mSubscriptionStatus[i] == -1) {
                    isReady = false;
                }
            }
            if (isReady) {
                setMainSlotWithActivationStatusCorrect();
            }
        }
    }

    private void setMainSlotWithActivationStatusCorrect() {
        int otherSlot;
        int mainSlot = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
        if (mainSlot == 0) {
            otherSlot = 1;
        } else {
            otherSlot = 0;
        }
        logd("Try to correct main slot. mainSlot = " + mainSlot + ", otherSlot = " + otherSlot + ", mainSubStatus = " + this.mChipCommon.mSubscriptionStatus[mainSlot] + ", othersubState = " + this.mChipCommon.mSubscriptionStatus[otherSlot]);
        if (this.mChipCommon.mSubscriptionStatus[mainSlot] != 0 || this.mChipCommon.mSubscriptionStatus[otherSlot] != 1) {
            clearSubStatus();
        } else if (!HwFullNetworkManagerImpl.getInstance().get4GSlotInProgress()) {
            setMainSlot(otherSlot, null);
            this.mNeedToSetMainSlotByBroadcast = false;
            clearSubStatus();
        } else {
            this.mNeedToSetMainSlotByBroadcast = true;
        }
    }

    private void clearSubStatus() {
        for (int i = 0; i < this.mChipCommon.mSubscriptionStatus.length; i++) {
            this.mChipCommon.mSubscriptionStatus[i] = -1;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onSwitchSlotDone(Intent intent) {
        boolean isSwitchDone = true;
        if (intent.getIntExtra(HwFullNetworkConstantsInner.HW_SWITCH_SLOT_STEP, -1) != 1) {
            isSwitchDone = false;
        }
        if (isSwitchDone && this.mNeedToSetMainSlotByBroadcast) {
            setMainSlotWithActivationStatusCorrect();
        }
    }
}
