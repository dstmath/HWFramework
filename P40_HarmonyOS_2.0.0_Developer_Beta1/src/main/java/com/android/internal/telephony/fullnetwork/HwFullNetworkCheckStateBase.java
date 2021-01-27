package com.android.internal.telephony.fullnetwork;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.telephony.HwTelephonyManager;
import android.telephony.HwTelephonyManagerInner;
import com.huawei.internal.telephony.CommandsInterfaceEx;

public abstract class HwFullNetworkCheckStateBase extends Handler {
    public int defaultMainSlot = 0;
    protected Handler mCheckStateHandler;
    public HwFullNetworkChipCommon mChipCommon = null;
    protected CommandsInterfaceEx[] mCis;
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

    public HwFullNetworkCheckStateBase(Context c, CommandsInterfaceEx[] ci, Handler h) {
        this.mCis = ci;
        this.mCheckStateHandler = h;
        this.mChipCommon = HwFullNetworkChipCommon.getInstance();
        this.mOperatorBase = HwFullNetworkOperatorFactory.getOperatorBase();
        logd("HwFullNetworkCheckStateBase constructor");
    }

    public boolean judgeDefaultMainSlotForMDM() {
        boolean isSub0Active = HwTelephonyManager.getDefault().getSubState(0) == 1;
        logd("judgeDefaultMainSlotForMDM isSub0Active = " + isSub0Active);
        this.defaultMainSlot = this.mChipCommon.getUserSwitchDualCardSlots();
        boolean isBothCardsPresent = this.mChipCommon.isCardPresent(0) && this.mChipCommon.isCardPresent(1);
        if (HwTelephonyManagerInner.getDefault().isDataConnectivityDisabled(1, "disable-data") && isBothCardsPresent && isSub0Active) {
            this.defaultMainSlot = 0;
            logd("disable-data  defaultMainSlot= " + this.defaultMainSlot);
            return true;
        } else if (!HwTelephonyManagerInner.getDefault().isDataConnectivityDisabled(1, "disable-sub") || !this.mChipCommon.isCardPresent(0)) {
            logd("not default main slot.");
            return false;
        } else {
            this.defaultMainSlot = 0;
            logd("disable-sub  defaultMainSlot= " + this.defaultMainSlot);
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public void checkCMCCUnbind() {
        if (HwFullNetworkConfigInner.IS_CMCC_UNBIND) {
            if (this.mChipCommon.isCMCCUnbindBySNDate()) {
                logd("checkCMCCUnbind: SNDate > configDate,unbind,return");
                return;
            }
            String cmccUnbindFlag = this.mChipCommon.getCMCCUnbindFlag();
            logd("checkCMCCUnbind: getCMCCUnbindFlag:" + cmccUnbindFlag);
            if (HwFullNetworkConstantsInner.CMCC_UNBIND_FLAG_INVALID.equals(cmccUnbindFlag)) {
                boolean isHybird = this.mChipCommon.isCMCCHybird();
                boolean isMainSoltCMCC = this.mChipCommon.getCMCCCardSlotId() == this.mChipCommon.getUserSwitchDualCardSlots();
                if (!isHybird || isMainSoltCMCC) {
                    cmccUnbindFlag = HwFullNetworkConstantsInner.CMCC_UNBIND_FLAG_BIND;
                } else {
                    cmccUnbindFlag = HwFullNetworkConstantsInner.CMCC_UNBIND_FLAG_UNBIND;
                }
                this.mChipCommon.setCMCCUnbindFlag(cmccUnbindFlag);
                logd("checkCMCCUnbind: setCMCCUnbindFlag:" + cmccUnbindFlag);
            }
            if (!HwFullNetworkConstantsInner.CMCC_UNBIND_FLAG_UNBIND.equals(cmccUnbindFlag)) {
                if (HwFullNetworkConstantsInner.CMCC_UNBIND_FLAG_BIND.equals(cmccUnbindFlag)) {
                    setCMCCHybirdBind();
                } else {
                    logd("checkCMCCUnbind: flag is unknown");
                }
            }
        }
    }

    private void setCMCCHybirdBind() {
        logd("setCMCCHybirdBind");
        this.mOperatorBase = HwFullNetworkOperatorFactory.getOperatorCMCC();
        HwFullNetworkConfigInner.setCMCCDsdxEnable(true);
    }

    public void checkDefaultMainSlotForMDMCarrier() {
        if (HwTelephonyManagerInner.getDefault().getDefaultMainSlotCarrier() != 0) {
            if (HwTelephonyManagerInner.getDefault().getDefaultMainSlotCarrier() == 1) {
                this.mOperatorBase = HwFullNetworkOperatorFactory.getOperatorCMCCMDMCarrier();
                logd("checkDefaultMainSlotForMDMCarrier CMCC");
            } else if (HwTelephonyManagerInner.getDefault().getDefaultMainSlotCarrier() == 2) {
                this.mOperatorBase = HwFullNetworkOperatorFactory.getOperatorCTMDMCarrier();
                logd("checkDefaultMainSlotForMDMCarrier CT");
            } else {
                logd("checkDefaultMainSlotForMDMCarrier invaild value");
            }
        }
    }
}
