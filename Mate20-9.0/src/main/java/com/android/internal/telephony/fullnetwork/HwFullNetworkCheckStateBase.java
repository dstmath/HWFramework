package com.android.internal.telephony.fullnetwork;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.telephony.HwTelephonyManagerInner;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.SubscriptionController;

public abstract class HwFullNetworkCheckStateBase extends Handler {
    public int defaultMainSlot = 0;
    protected Handler mCheckStateHandler;
    public HwFullNetworkChipCommon mChipCommon = null;
    protected CommandsInterface[] mCis;
    public HwFullNetworkOperatorBase mOperatorBase = null;

    /* access modifiers changed from: protected */
    public abstract boolean checkIfAllCardsReady(Message message);

    /* access modifiers changed from: protected */
    public abstract void checkNetworkType();

    /* access modifiers changed from: protected */
    public abstract int getDefaultMainSlot();

    /* access modifiers changed from: protected */
    public abstract boolean judgeSetDefault4GSlotForCMCC(int i);

    /* access modifiers changed from: protected */
    public abstract void logd(String str);

    /* access modifiers changed from: protected */
    public abstract void loge(String str);

    public HwFullNetworkCheckStateBase(Context c, CommandsInterface[] ci, Handler h) {
        this.mCis = ci;
        this.mCheckStateHandler = h;
        this.mChipCommon = HwFullNetworkChipCommon.getInstance();
        this.mOperatorBase = HwFullNetworkOperatorFactory.getOperatorBase();
        logd("HwFullNetworkCheckStateBase constructor");
    }

    public boolean judgeDefaultMainSlotForMDM() {
        boolean isSub0Active = SubscriptionController.getInstance().getSubState(0) == 1;
        logd("judgeDefaultMainSlotForMDM isSub0Active = " + isSub0Active);
        this.defaultMainSlot = this.mChipCommon.getUserSwitchDualCardSlots();
        if (HwTelephonyManagerInner.getDefault().isDataConnectivityDisabled(1, "disable-data") && this.mChipCommon.isCardPresent(0) && this.mChipCommon.isCardPresent(1) && isSub0Active) {
            this.defaultMainSlot = 0;
            logd("disable-data  defaultMainSlot= " + this.defaultMainSlot);
            return true;
        } else if (!HwTelephonyManagerInner.getDefault().isDataConnectivityDisabled(1, "disable-sub") || !this.mChipCommon.isCardPresent(0)) {
            return false;
        } else {
            this.defaultMainSlot = 0;
            logd("disable-sub  defaultMainSlot= " + this.defaultMainSlot);
            return true;
        }
    }
}
