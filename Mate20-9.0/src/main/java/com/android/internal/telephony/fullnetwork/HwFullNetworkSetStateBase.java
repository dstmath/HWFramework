package com.android.internal.telephony.fullnetwork;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.HwNetworkTypeUtils;

public abstract class HwFullNetworkSetStateBase extends Handler {
    public HwFullNetworkChipCommon mChipCommon;
    protected CommandsInterface[] mCis;
    protected Context mContext;
    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                HwFullNetworkSetStateBase.this.loge("intent is null, return");
                return;
            }
            if ("android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE".equals(intent.getAction())) {
                HwFullNetworkSetStateBase.this.logd("received ACTION_SET_RADIO_CAPABILITY_DONE");
                HwFullNetworkSetStateBase.this.setRadioCapabilityDone(intent);
            } else if ("android.intent.action.ACTION_SET_RADIO_CAPABILITY_FAILED".equals(intent.getAction())) {
                HwFullNetworkSetStateBase.this.logd("received ACTION_SET_RADIO_CAPABILITY_FAILED");
                HwFullNetworkSetStateBase.this.setRadioCapabilityFailed(intent);
                HwFullNetworkSetStateBase.this.sendHwSwitchSlotFailedBroadcast();
            }
        }
    };
    protected Handler mStateHandler;

    /* access modifiers changed from: protected */
    public abstract void logd(String str);

    /* access modifiers changed from: protected */
    public abstract void loge(String str);

    public abstract void setMainSlot(int i, Message message);

    public abstract void setMainSlotDone(Message message, int i);

    /* access modifiers changed from: protected */
    public abstract void setRadioCapabilityDone(Intent intent);

    /* access modifiers changed from: protected */
    public abstract void setRadioCapabilityFailed(Intent intent);

    HwFullNetworkSetStateBase(Context c, CommandsInterface[] ci, Handler h) {
        this.mContext = c;
        this.mCis = ci;
        this.mStateHandler = h;
        this.mChipCommon = HwFullNetworkChipCommon.getInstance();
        IntentFilter filter = new IntentFilter("android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE");
        filter.addAction("android.intent.action.ACTION_SET_RADIO_CAPABILITY_FAILED");
        this.mContext.registerReceiver(this.mReceiver, filter);
    }

    /* access modifiers changed from: package-private */
    public void startFastSwithSIMSlotTimer() {
        Message message = obtainMessage(HwFullNetworkConstants.EVENT_SET_MAIN_SLOT_TIMEOUT);
        AsyncResult.forMessage(message, null, null);
        sendMessageDelayed(message, 60000);
        logd("startFastSwithSIMSlotTimer");
    }

    /* access modifiers changed from: package-private */
    public void sendHwSwitchSlotStartBroadcast() {
        Intent intent = new Intent("com.huawei.action.ACTION_HW_SWITCH_SLOT_DONE");
        intent.putExtra(HwFullNetworkConstants.HW_SWITCH_SLOT_STEP, 0);
        this.mContext.sendBroadcast(intent);
    }

    /* access modifiers changed from: package-private */
    public void sendHwSwitchSlotDoneBroadcast(int mainSlotId) {
        Intent intent = new Intent("com.huawei.action.ACTION_HW_SWITCH_SLOT_DONE");
        int i = 1;
        intent.putExtra(HwFullNetworkConstants.HW_SWITCH_SLOT_STEP, 1);
        int oldSlotId = this.mChipCommon.getUserSwitchDualCardSlots();
        this.mChipCommon.setUserSwitchDualCardSlots(mainSlotId);
        if (oldSlotId != mainSlotId) {
            i = 0;
        }
        intent.putExtra(HwFullNetworkConstants.IF_NEED_SET_RADIO_CAP, i);
        if (HwFullNetworkConfig.IS_CMCC_4GSWITCH_DISABLE && oldSlotId != mainSlotId) {
            HwNetworkTypeUtils.exchangeDualCardNetworkModeDB(this.mContext);
        }
        this.mContext.sendBroadcast(intent);
    }

    /* access modifiers changed from: private */
    public void sendHwSwitchSlotFailedBroadcast() {
        Intent intent = new Intent("com.huawei.action.ACTION_HW_SWITCH_SLOT_DONE");
        intent.putExtra(HwFullNetworkConstants.HW_SWITCH_SLOT_STEP, -1);
        this.mContext.sendBroadcast(intent);
    }
}
