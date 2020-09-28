package com.android.internal.telephony;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.TelephonyManager;
import com.huawei.android.telephony.RlogEx;
import com.huawei.internal.telephony.CommandsInterfaceEx;

public class HwCardTrayInfo extends Handler {
    private static final int CARDTRAY_HOTPLUG_INFO_LEN = 4;
    private static final int CARDTRAY_OUT_SLOT = 0;
    private static final String ESIM_EANBLED_SUBID = "esim_switch_enabled_subid";
    private static final int ESIM_SWITCH_DISABLED = 0;
    private static final int EVENT_SIM_HOTPLUG = 10;
    private static final int PHONE_COUNT = TelephonyManager.getDefault().getPhoneCount();
    private static final String TAG = "HwCardTrayInfo";
    private static final Object mLock = new Object();
    private static HwCardTrayInfo sInstance;
    private Context mContext;
    private HotplugState[] mHotplugState = new HotplugState[PHONE_COUNT];
    private boolean mQueryDone = false;

    /* access modifiers changed from: private */
    public enum HotplugState {
        STATE_PLUG_OUT,
        STATE_PLUG_IN
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

    private HwCardTrayInfo(CommandsInterfaceEx[] ci, Context context) {
        if (ci != null) {
            for (int i = 0; i < ci.length; i++) {
                ci[i].registerForSimHotPlug(this, (int) EVENT_SIM_HOTPLUG, Integer.valueOf(i));
            }
        }
        this.mContext = context;
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

    public void handleMessage(Message msg) {
        if (msg.what != EVENT_SIM_HOTPLUG) {
            loge("Unknown Event " + msg.what);
            return;
        }
        onSimHotPlug(msg);
    }

    private void onSimHotPlug(Message msg) {
        AsyncResult ar = (AsyncResult) msg.obj;
        if (ar != null && ar.result != null && (ar.result instanceof int[]) && ((int[]) ar.result).length > 0) {
            Integer slotId = (Integer) ar.userObj;
            if (!isValidIndex(slotId.intValue())) {
                loge("onSimHotPlug, invalid slot:" + slotId);
                return;
            }
            if (HotplugState.STATE_PLUG_IN.ordinal() == ((int[]) ar.result)[0]) {
                this.mHotplugState[slotId.intValue()] = HotplugState.STATE_PLUG_IN;
            } else if (HotplugState.STATE_PLUG_OUT.ordinal() == ((int[]) ar.result)[0]) {
                clearEsimSwitchStatus(this.mContext);
                this.mHotplugState[slotId.intValue()] = HotplugState.STATE_PLUG_OUT;
            }
            log("onSimHotPlug, mHotplugState[" + slotId + "]:" + this.mHotplugState[slotId.intValue()]);
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

    private static void log(String string) {
        RlogEx.i(TAG, string);
    }

    private void loge(String string) {
        RlogEx.e(TAG, string);
    }

    private void clearEsimSwitchStatus(Context context) {
        if (context != null) {
            log("clearEsimSwitchStatus");
            Settings.System.putInt(context.getContentResolver(), ESIM_EANBLED_SUBID, 0);
        }
    }
}
