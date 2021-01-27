package com.huawei.internal.telephony.vsim.process;

import android.os.Handler;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.android.internal.telephony.vsim.HwVSimController;
import com.android.internal.telephony.vsim.HwVSimModemAdapter;
import com.android.internal.telephony.vsim.HwVSimRequest;
import com.android.internal.telephony.vsim.HwVSimUtilsInner;
import com.android.internal.telephony.vsim.process.HwVSimProcessor;
import com.huawei.android.os.AsyncResultEx;

public abstract class HwVSimHisiProcessor extends HwVSimProcessor {
    protected Handler mHandler = this.mVSimController.getHandler();
    protected HwVSimController mVSimController;

    public HwVSimHisiProcessor(HwVSimController controller, HwVSimModemAdapter modemAdapter, HwVSimRequest request) {
        super(modemAdapter, request);
        this.mVSimController = controller;
    }

    @Deprecated
    public void setIsVSimOn(boolean isVSimOn) {
        HwVSimController.getInstance().setIsVSimOn(isVSimOn);
    }

    public void setProcessAction(HwVSimConstants.ProcessAction action) {
        HwVSimController.getInstance().setProcessAction(action);
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public void setProcessType(HwVSimConstants.ProcessType type) {
        this.mVSimController.setProcessType(type);
    }

    public void setProcessState(HwVSimConstants.ProcessState state) {
        this.mVSimController.setProcessState(state);
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public boolean isSwapProcess() {
        return this.mVSimController.isSwapProcess();
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public boolean isCrossProcess() {
        return this.mVSimController.isCrossProcess();
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public boolean isDirectProcess() {
        return this.mVSimController.isDirectProcess();
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public boolean isEnableProcess() {
        return this.mVSimController.isEnableProcess();
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public boolean isDisableProcess() {
        return this.mVSimController.isDisableProcess();
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public boolean isSwitchModeProcess() {
        return this.mVSimController.isSwitchModeProcess();
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public boolean isWorkProcess() {
        return this.mVSimController.isWorkProcess();
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public boolean isReadyProcess() {
        return this.mVSimController.isReadyProcess();
    }

    /* access modifiers changed from: protected */
    public boolean isNeedWaitNvCfgMatchAndRestartRild() {
        return isCrossProcess() && HwVSimUtilsInner.isPlatformNeedWaitNvMatchUnsol() && this.mVSimController.getInsertedCardCount() != 0;
    }

    /* access modifiers changed from: protected */
    public void closeChipSessionWhenOpenFailOrTimeout(AsyncResultEx ar) {
        logd("closeChipSessionWhenOpenFailOrTimeout, ar = " + ar);
        doEnableProcessException(ar, this.mRequest, 11);
    }
}
