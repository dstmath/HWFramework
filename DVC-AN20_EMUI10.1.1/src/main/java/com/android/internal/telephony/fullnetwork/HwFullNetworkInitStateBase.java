package com.android.internal.telephony.fullnetwork;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.telephony.HwTelephonyManagerInner;
import android.text.TextUtils;
import com.android.internal.telephony.uicc.HwIccUtils;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.android.telephony.SubscriptionInfoEx;
import com.huawei.hwparttelephonyfullnetwork.BuildConfig;
import com.huawei.internal.telephony.CommandsInterfaceEx;

public abstract class HwFullNetworkInitStateBase extends Handler {
    protected static HwFullNetworkChipCommon mChipCommon;
    protected static final Object mLock = new Object();
    protected CommandsInterfaceEx[] mCis;
    protected Context mContext;
    protected Handler mStateHandler;

    /* access modifiers changed from: protected */
    public abstract void logd(String str);

    /* access modifiers changed from: protected */
    public abstract void loge(String str);

    public abstract void onGetIccCardStatusDone(Object obj, Integer num);

    public HwFullNetworkInitStateBase(Context c, CommandsInterfaceEx[] ci, Handler h) {
        this.mContext = c;
        this.mCis = ci;
        this.mStateHandler = h;
        mChipCommon = HwFullNetworkChipCommon.getInstance();
        for (int i = 0; i < HwFullNetworkConstantsInner.SIM_NUM; i++) {
            mChipCommon.isSimInsertedArray[i] = false;
        }
        for (int i2 = 0; i2 < this.mCis.length; i2++) {
            Integer index = Integer.valueOf(i2);
            this.mCis[i2].registerForIccStatusChanged(this, (int) HwFullNetworkConstantsInner.EVENT_ICC_STATUS_CHANGED, index);
            this.mCis[i2].registerForAvailable(this, (int) HwFullNetworkConstantsInner.EVENT_ICC_STATUS_CHANGED, index);
        }
        initDefaultDBIfNeeded();
    }

    private void initDefaultDBIfNeeded() {
        try {
            Settings.System.getInt(this.mContext.getContentResolver(), "switch_dual_card_slots");
        } catch (Settings.SettingNotFoundException e) {
            logd("Settings Exception Reading Dual Sim Switch Dual Card Slots Values");
            Settings.System.putInt(this.mContext.getContentResolver(), "switch_dual_card_slots", 0);
        }
    }

    public void handleMessage(Message msg) {
        if (msg == null) {
            loge("msg is null, return!");
            return;
        }
        Integer index = mChipCommon.getCiIndex(msg);
        if (index.intValue() < 0 || index.intValue() >= this.mCis.length) {
            loge("Invalid index : " + index + " received with event " + msg.what);
            return;
        }
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        switch (msg.what) {
            case HwFullNetworkConstantsInner.EVENT_ICC_STATUS_CHANGED:
                logd("Received EVENT_ICC_STATUS_CHANGED on index " + index);
                onIccStatusChanged(index);
                return;
            case HwFullNetworkConstantsInner.EVENT_GET_ICCID_DONE:
                logd("Received EVENT_GET_ICCID_DONE on index " + index);
                onGetIccidDone(ar, index);
                return;
            default:
                return;
        }
    }

    /* access modifiers changed from: protected */
    public void onIccStatusChanged(Integer index) {
        if (HwFullNetworkConfigInner.isCMCCDsdxEnable() || HwFullNetworkConfigInner.IS_CT_4GSWITCH_DISABLE || HwTelephonyManagerInner.getDefault().getDefaultMainSlotCarrier() != 0) {
            this.mCis[index.intValue()].getICCID(obtainMessage(HwFullNetworkConstantsInner.EVENT_GET_ICCID_DONE, index));
        }
    }

    /* access modifiers changed from: protected */
    public void onGetIccidDone(AsyncResultEx ar, Integer index) {
        if (ar == null || ar.getException() != null) {
            logd("get iccid exception, maybe card is absent. set iccid as \"\"");
            mChipCommon.mIccIds[index.intValue()] = BuildConfig.FLAVOR;
            return;
        }
        byte[] data = (byte[]) ar.getResult();
        String iccid = HwIccUtils.bcdIccidToString(data, 0, data.length);
        if (TextUtils.isEmpty(iccid) || 7 > iccid.length()) {
            logd("iccId is invalid, set it as \"\" ");
            mChipCommon.mIccIds[index.intValue()] = BuildConfig.FLAVOR;
        } else {
            mChipCommon.mIccIds[index.intValue()] = iccid.substring(0, 7);
        }
        logd("get iccid is " + SubscriptionInfoEx.givePrintableIccid(mChipCommon.mIccIds[index.intValue()]) + " on index " + index);
    }
}
