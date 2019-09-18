package com.android.internal.telephony.fullnetwork;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.telephony.HwTelephonyManagerInner;
import android.text.TextUtils;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.HwSubscriptionManager;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.vsim.HwVSimConstants;

public abstract class HwFullNetworkDefaultStateBase extends Handler {
    protected HwFullNetworkChipCommon mChipCommon;
    protected CommandsInterface[] mCis;
    protected Context mContext;
    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                HwFullNetworkDefaultStateBase.this.loge("intent is null, return");
                return;
            }
            if ("com.huawei.intent.action.ACTION_SUBSCRIPTION_SET_UICC_RESULT".equals(intent.getAction())) {
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

    public HwFullNetworkDefaultStateBase(Context c, CommandsInterface[] ci, Handler h) {
        this.mContext = c;
        this.mCis = ci;
        this.mStateHandler = h;
        this.mChipCommon = HwFullNetworkChipCommon.getInstance();
        for (int i = 0; i < this.mCis.length; i++) {
            Integer index = Integer.valueOf(i);
            this.mCis[i].registerForNotAvailable(this, HwFullNetworkConstants.EVENT_RADIO_UNAVAILABLE, index);
            this.mCis[i].registerForAvailable(this, HwFullNetworkConstants.EVENT_RADIO_AVAILABLE, index);
        }
        IntentFilter filter = new IntentFilter("com.huawei.devicepolicy.action.POLICY_CHANGED");
        if (HwFullNetworkConfig.IS_CMCC_4GSWITCH_DISABLE || HwFullNetworkConfig.IS_CT_4GSWITCH_DISABLE) {
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
        int i = msg.what;
        if (i != 1010) {
            switch (i) {
                case HwFullNetworkConstants.EVENT_RADIO_UNAVAILABLE:
                    logd("Received EVENT_RADIO_UNAVAILABLE on index " + index);
                    onRadioUnavailable(index);
                    break;
                case HwFullNetworkConstants.EVENT_RADIO_AVAILABLE:
                    logd("Received EVENT_RADIO_AVAILABLE on index " + index);
                    onRadioAvailable(index);
                    break;
            }
        } else {
            logd("Received EVENT_VOICE_CALL_ENDED on index " + index);
            onVoiceCallEnded(index);
        }
    }

    /* access modifiers changed from: protected */
    public void onRadioAvailable(Integer index) {
        if (HwFullNetworkConstants.SIM_NUM == 2 && !this.mChipCommon.isVoiceCallEndedRegistered && !HwFullNetworkConfig.IS_CHINA_TELECOM) {
            for (Phone phone : PhoneFactory.getPhones()) {
                if (phone != null) {
                    logd("registerForVoiceCallEnded for phone " + phone.getPhoneId());
                    phone.getCallTracker().registerForVoiceCallEnded(this, HwFullNetworkConstants.EVENT_VOICE_CALL_ENDED, Integer.valueOf(phone.getPhoneId()));
                }
            }
            this.mChipCommon.isVoiceCallEndedRegistered = true;
        }
    }

    /* access modifiers changed from: protected */
    public void onVoiceCallEnded(Integer index) {
        logd("onVoiceCallEnded");
        if (!(SubscriptionController.getInstance().getSubState(index.intValue() == 0 ? 1 : 0) == 1 || SubscriptionController.getInstance().getSubState(index.intValue()) != 1 || index.intValue() == this.mChipCommon.getUserSwitchDualCardSlots())) {
            Message msg = this.mStateHandler.obtainMessage(HwFullNetworkConstants.EVENT_SET_MAIN_SLOT);
            msg.arg1 = index.intValue();
            this.mStateHandler.sendMessage(msg);
        }
        if (index.intValue() == 1 && SubscriptionController.getInstance().getSubState(index.intValue()) == 1 && HwTelephonyManagerInner.getDefault().isDataConnectivityDisabled(1, "disable-sub")) {
            HwSubscriptionManager.getInstance().setSubscription(index.intValue(), false, null);
        }
    }

    public void processMdmPolicyChanged(Intent intent) {
        String action_tag = intent.getStringExtra("action_tag");
        if (!TextUtils.isEmpty(action_tag) && action_tag.equals("action_disable_data_4G")) {
            int targetId = intent.getIntExtra(HwVSimConstants.EXTRA_NETWORK_SCAN_SUBID, -1);
            this.mChipCommon.isSet4GSlotInProgress = false;
            boolean dataState = intent.getBooleanExtra("dataState", false);
            boolean isSub0Active = SubscriptionController.getInstance().getSubState(0) == 1;
            if (this.mChipCommon.isCardPresent(0) && this.mChipCommon.isCardPresent(1) && dataState && isSub0Active) {
                Message msg = this.mStateHandler.obtainMessage(HwFullNetworkConstants.EVENT_SET_MAIN_SLOT);
                msg.arg1 = targetId;
                this.mStateHandler.sendMessage(msg);
            }
        }
    }

    public void forceSetDefault4GSlotForCMCC(int cmccSlotId) {
        logd("forceSetDefault4GSlotForCMCC cmccSlotId:" + cmccSlotId);
        this.mStateHandler.obtainMessage(HwFullNetworkConstants.EVENT_FORCE_CHECK_MAIN_SLOT_FOR_CMCC, Integer.valueOf(cmccSlotId)).sendToTarget();
    }
}
