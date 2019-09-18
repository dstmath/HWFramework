package com.android.internal.telephony.fullnetwork;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.telephony.SubscriptionInfo;
import android.text.TextUtils;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.uicc.HwIccUtils;

public abstract class HwFullNetworkInitStateBase extends Handler {
    protected static HwFullNetworkChipCommon mChipCommon;
    protected static final Object mLock = new Object();
    protected CommandsInterface[] mCis;
    protected Context mContext;
    protected Handler mStateHandler;

    /* access modifiers changed from: protected */
    public abstract void logd(String str);

    /* access modifiers changed from: protected */
    public abstract void loge(String str);

    public abstract void onGetIccCardStatusDone(AsyncResult asyncResult, Integer num);

    public HwFullNetworkInitStateBase(Context c, CommandsInterface[] ci, Handler h) {
        this.mContext = c;
        this.mCis = ci;
        this.mStateHandler = h;
        mChipCommon = HwFullNetworkChipCommon.getInstance();
        for (int i = 0; i < HwFullNetworkConstants.SIM_NUM; i++) {
            mChipCommon.isSimInsertedArray[i] = false;
        }
        for (int i2 = 0; i2 < this.mCis.length; i2++) {
            Integer index = Integer.valueOf(i2);
            this.mCis[i2].registerForIccStatusChanged(this, 1001, index);
            this.mCis[i2].registerForAvailable(this, 1001, index);
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

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v13, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v6, resolved type: android.os.AsyncResult} */
    /* JADX WARNING: Multi-variable type inference failed */
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
        AsyncResult ar = null;
        if (msg.obj != null && (msg.obj instanceof AsyncResult)) {
            ar = msg.obj;
        }
        int i = msg.what;
        if (i == 1001) {
            logd("Received EVENT_ICC_STATUS_CHANGED on index " + index);
            onIccStatusChanged(index);
        } else if (i == 1009) {
            logd("Received EVENT_GET_ICCID_DONE on index " + index);
            onGetIccidDone(ar, index);
        }
    }

    /* access modifiers changed from: protected */
    public void onIccStatusChanged(Integer index) {
        if (HwFullNetworkConfig.IS_CMCC_4G_DSDX_ENABLE || HwFullNetworkConfig.IS_CT_4GSWITCH_DISABLE) {
            this.mCis[index.intValue()].getICCID(obtainMessage(HwFullNetworkConstants.EVENT_GET_ICCID_DONE, index));
        }
    }

    /* access modifiers changed from: protected */
    public void onGetIccidDone(AsyncResult ar, Integer index) {
        if (ar == null || ar.exception != null) {
            logd("get iccid exception, maybe card is absent. set iccid as \"\"");
            mChipCommon.mIccIds[index.intValue()] = "";
            return;
        }
        byte[] data = (byte[]) ar.result;
        String iccid = HwIccUtils.bcdIccidToString(data, 0, data.length);
        if (TextUtils.isEmpty(iccid) || 7 > iccid.length()) {
            logd("iccId is invalid, set it as \"\" ");
            mChipCommon.mIccIds[index.intValue()] = "";
        } else {
            mChipCommon.mIccIds[index.intValue()] = iccid.substring(0, 7);
        }
        logd("get iccid is " + SubscriptionInfo.givePrintableIccid(mChipCommon.mIccIds[index.intValue()]) + " on index " + index);
    }
}
