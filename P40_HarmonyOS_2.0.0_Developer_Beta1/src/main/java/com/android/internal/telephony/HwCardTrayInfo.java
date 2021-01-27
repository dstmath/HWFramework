package com.android.internal.telephony;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.telephony.HwTelephonyManagerInner;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.internal.telephony.CommandsInterfaceEx;

public class HwCardTrayInfo extends Handler {
    private static final String ACTION_HW_HOT_PLUG_STATE_CHANGED = "com.huawei.intent.action.HOT_PLUG_STATE_CHANGED";
    private static final int CARDTRAY_HOTPLUG_INFO_LEN = 4;
    private static final int CARDTRAY_OUT_SLOT = 0;
    private static final String DEVICE_PROPER_CONFIG = "ro.ril.esim_type";
    private static final int DEVICE_TYPE = SystemPropertiesEx.getInt(DEVICE_PROPER_CONFIG, -1);
    private static final int DEVICE_TYPE_DUAL_SIM_AND_ESIM = 3;
    private static final int DEVICE_TYPE_INVALID = -1;
    private static final String DSDS_CARD_MANAGER_PACKAGES_NAME = "com.huawei.dsdscardmanager";
    private static final String ESIM_EANBLED_SUBID = "esim_switch_enabled_subid";
    private static final int ESIM_PSIM_SWITCH_DEFAULT = -1;
    private static final String ESIM_PSIM_SWITCH_FLAG_FOR_SIM = "esim_psim_switch_for_sim";
    private static final String ESIM_PSIM_SWITCH_FLAG_FOR_TELEPHONY = "esim_psim_switch_for_telephony";
    private static final int ESIM_SWITCH_DISABLED = 0;
    private static final int EVENT_SIM_HOTPLUG = 10;
    private static final int INVALID_SLOT_1_HOT_PLUG_STATE = -1;
    private static final int PHONE_COUNT = TelephonyManagerEx.getDefault().getPhoneCount();
    private static final String SECOND_CARD_HOT_PLUG_STATE_FOR_ESIM = "second_card_hot_plug_state_for_esim";
    private static final int SLOT0 = 0;
    private static final int SLOT1 = 1;
    private static final int SLOT_1_HOT_PLUG_IN_FOR_ESIM = 6;
    private static final String TAG = "HwCardTrayInfo";
    private static final Object mLock = new Object();
    private static HwCardTrayInfo sInstance;
    private Context mContext;
    private HotplugState[] mHotplugState = new HotplugState[PHONE_COUNT];
    private boolean mQueryDone = false;
    private int mSlot1HotPlugStateForEsim = -1;

    /* access modifiers changed from: private */
    public enum HotplugState {
        STATE_PLUG_OUT,
        STATE_PLUG_IN
    }

    private HwCardTrayInfo(CommandsInterfaceEx[] ci, Context context) {
        if (ci != null) {
            for (int i = 0; i < ci.length; i++) {
                ci[i].registerForSimHotPlug(this, (int) EVENT_SIM_HOTPLUG, Integer.valueOf(i));
            }
        }
        this.mContext = context;
    }

    public static HwCardTrayInfo make(CommandsInterfaceEx[] ci, Context context) {
        HwCardTrayInfo hwCardTrayInfo;
        synchronized (mLock) {
            if (sInstance == null) {
                sInstance = new HwCardTrayInfo(ci, context);
                hwCardTrayInfo = sInstance;
            } else {
                throw new RuntimeException("HwCardTrayInfo.make() should only be called once");
            }
        }
        return hwCardTrayInfo;
    }

    public static HwCardTrayInfo getInstance() {
        HwCardTrayInfo hwCardTrayInfo;
        synchronized (mLock) {
            if (sInstance != null) {
                hwCardTrayInfo = sInstance;
            } else {
                throw new RuntimeException("HwCardTrayInfo.getInstance can't be called before make()");
            }
        }
        return hwCardTrayInfo;
    }

    private static void log(String string) {
        RlogEx.i(TAG, string);
    }

    @Override // android.os.Handler
    public void handleMessage(Message msg) {
        if (msg.what != EVENT_SIM_HOTPLUG) {
            loge("Unknown Event " + msg.what);
            return;
        }
        onSimHotPlug(msg);
    }

    private void onSimHotPlug(Message msg) {
        Object result;
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (ar != null && (result = ar.getResult()) != null && (result instanceof int[]) && ((int[]) result).length > 0) {
            Integer slotId = (Integer) ar.getUserObj();
            if (!isValidIndex(slotId.intValue())) {
                loge("onSimHotPlug, invalid slot:" + slotId);
                return;
            }
            if (HotplugState.STATE_PLUG_IN.ordinal() == ((int[]) result)[0]) {
                this.mHotplugState[slotId.intValue()] = HotplugState.STATE_PLUG_IN;
            } else if (HotplugState.STATE_PLUG_OUT.ordinal() == ((int[]) result)[0]) {
                clearEsimSwitchStatus(this.mContext);
                this.mHotplugState[slotId.intValue()] = HotplugState.STATE_PLUG_OUT;
            }
            if (DEVICE_TYPE == 3) {
                if (slotId.intValue() == 1) {
                    this.mSlot1HotPlugStateForEsim = ((int[]) result)[0];
                    sendBroadcastForEsim(this.mContext, this.mSlot1HotPlugStateForEsim);
                } else if (slotId.intValue() != 0) {
                    log("not need update slot1 hot plug state.");
                } else if (HotplugState.STATE_PLUG_OUT.ordinal() == ((int[]) result)[0]) {
                    this.mSlot1HotPlugStateForEsim = 0;
                    sendBroadcastForEsim(this.mContext, this.mSlot1HotPlugStateForEsim);
                }
            }
            log("onSimHotPlug, mHotplugState[" + slotId + "]:" + this.mHotplugState[slotId.intValue()] + ", mSlot1HotPlugStateForEsim" + this.mSlot1HotPlugStateForEsim);
        }
    }

    private void sendBroadcastForEsim(Context context, int slot1HotPlugStateForEsim) {
        if (context != null) {
            Intent intent = new Intent(ACTION_HW_HOT_PLUG_STATE_CHANGED);
            intent.setPackage(DSDS_CARD_MANAGER_PACKAGES_NAME);
            intent.putExtra(SECOND_CARD_HOT_PLUG_STATE_FOR_ESIM, this.mSlot1HotPlugStateForEsim);
            context.sendBroadcast(intent, "android.permission.READ_PHONE_STATE");
            log("sendBroadcastForEsim slot1HotPlugStateForEsim=" + slot1HotPlugStateForEsim);
        }
    }

    private boolean isValidIndex(int index) {
        return index >= 0 && index < PHONE_COUNT;
    }

    private void setCardTrayHotplugState(byte[] cardTrayInfo) {
        if (cardTrayInfo != null && cardTrayInfo.length >= 4) {
            for (int i = 0; i < PHONE_COUNT; i++) {
                if (HotplugState.STATE_PLUG_IN.ordinal() == cardTrayInfo[(i * 2) + 1]) {
                    this.mHotplugState[i] = HotplugState.STATE_PLUG_IN;
                } else if (HotplugState.STATE_PLUG_OUT.ordinal() == cardTrayInfo[(i * 2) + 1]) {
                    this.mHotplugState[i] = HotplugState.STATE_PLUG_OUT;
                }
                log("setCardTrayHotplugState, mHotplugState[" + i + "]:" + this.mHotplugState[i]);
            }
        }
    }

    public synchronized boolean isCardTrayOut(int slotId) {
        boolean z = false;
        if (!this.mQueryDone) {
            log("isCardTrayOut, first query for card tray info.");
            byte[] cardTrayInfo = HwTelephonyManagerInner.getDefault().getCardTrayInfo();
            if (cardTrayInfo != null && cardTrayInfo.length >= 4) {
                this.mQueryDone = true;
                setCardTrayHotplugState(cardTrayInfo);
                if (isValidIndex(slotId)) {
                    if (cardTrayInfo[(slotId * 2) + 1] == 0) {
                        z = true;
                    }
                    return z;
                }
            }
        } else if (isValidIndex(slotId)) {
            if (this.mHotplugState[slotId].ordinal() == 0) {
                z = true;
            }
            return z;
        }
        return false;
    }

    public int getSlot1HotPlugStateForEsim() {
        return this.mSlot1HotPlugStateForEsim;
    }

    private void loge(String string) {
        RlogEx.e(TAG, string);
    }

    private void clearEsimSwitchStatus(Context context) {
        if (context != null) {
            log("clearEsimSwitchStatus");
            Settings.System.putInt(context.getContentResolver(), ESIM_EANBLED_SUBID, 0);
            Settings.System.putInt(context.getContentResolver(), ESIM_PSIM_SWITCH_FLAG_FOR_SIM, -1);
            Settings.System.putInt(context.getContentResolver(), ESIM_PSIM_SWITCH_FLAG_FOR_TELEPHONY, -1);
        }
    }
}
