package com.android.internal.telephony.fullnetwork;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.HwNetworkTypeUtils;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.internal.telephony.CommandsInterfaceEx;

public abstract class HwFullNetworkSetStateBase extends Handler {
    public HwFullNetworkChipCommon mChipCommon;
    protected CommandsInterfaceEx[] mCis;
    protected Context mContext;
    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.android.internal.telephony.fullnetwork.HwFullNetworkSetStateBase.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                HwFullNetworkSetStateBase.this.loge("intent is null, return");
            } else if ("android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE".equals(intent.getAction())) {
                HwFullNetworkSetStateBase.this.logd("received ACTION_SET_RADIO_CAPABILITY_DONE");
                HwFullNetworkSetStateBase.this.setRadioCapabilityDone(intent);
            } else if ("android.intent.action.ACTION_SET_RADIO_CAPABILITY_FAILED".equals(intent.getAction())) {
                HwFullNetworkSetStateBase.this.logd("received ACTION_SET_RADIO_CAPABILITY_FAILED");
                HwFullNetworkSetStateBase.this.setRadioCapabilityFailed(intent);
                HwFullNetworkSetStateBase.this.sendHwSwitchSlotFailedBroadcast();
            } else {
                HwFullNetworkSetStateBase.this.logd("received ACTION not match.");
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

    HwFullNetworkSetStateBase(Context c, CommandsInterfaceEx[] ci, Handler h) {
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
        Message message = obtainMessage(HwFullNetworkConstantsInner.EVENT_SET_MAIN_SLOT_TIMEOUT);
        AsyncResultEx.forMessage(message, (Object) null, (Throwable) null);
        sendMessageDelayed(message, 60000);
        logd("startFastSwithSIMSlotTimer");
    }

    /* access modifiers changed from: package-private */
    public void sendHwSwitchSlotStartBroadcast() {
        Intent intent = new Intent("com.huawei.action.ACTION_HW_SWITCH_SLOT_DONE");
        intent.putExtra(HwFullNetworkConstantsInner.HW_SWITCH_SLOT_STEP, 0);
        this.mContext.sendBroadcast(intent);
    }

    /* access modifiers changed from: package-private */
    public void sendHwSwitchSlotDoneBroadcast(int mainSlotId) {
        int i = 1;
        Intent intent = new Intent("com.huawei.action.ACTION_HW_SWITCH_SLOT_DONE");
        intent.putExtra(HwFullNetworkConstantsInner.HW_SWITCH_SLOT_STEP, 1);
        int oldSlotId = this.mChipCommon.getUserSwitchDualCardSlots();
        this.mChipCommon.setUserSwitchDualCardSlots(mainSlotId);
        if (oldSlotId != mainSlotId) {
            i = 0;
        }
        intent.putExtra(HwFullNetworkConstantsInner.IF_NEED_SET_RADIO_CAP, i);
        if (HwFullNetworkConfigInner.isCMCCDsdxDisable() && oldSlotId != mainSlotId && HuaweiTelephonyConfigs.isHisiPlatform()) {
            HwNetworkTypeUtils.exchangeDualCardNetworkModeDB(this.mContext);
        }
        this.mContext.sendBroadcast(intent);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendHwSwitchSlotFailedBroadcast() {
        Intent intent = new Intent("com.huawei.action.ACTION_HW_SWITCH_SLOT_DONE");
        intent.putExtra(HwFullNetworkConstantsInner.HW_SWITCH_SLOT_STEP, -1);
        this.mContext.sendBroadcast(intent);
    }
}
