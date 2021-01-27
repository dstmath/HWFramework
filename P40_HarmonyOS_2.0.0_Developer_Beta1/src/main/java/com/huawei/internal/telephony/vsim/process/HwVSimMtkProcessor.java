package com.huawei.internal.telephony.vsim.process;

import android.os.Handler;
import android.telephony.HwTelephonyManagerInner;
import com.android.internal.telephony.HwSubscriptionManager;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.android.internal.telephony.vsim.HwVSimModemAdapter;
import com.android.internal.telephony.vsim.HwVSimRequest;
import com.android.internal.telephony.vsim.process.HwVSimProcessor;
import com.huawei.internal.telephony.vsim.HwVSimMtkController;

public abstract class HwVSimMtkProcessor extends HwVSimProcessor {
    protected HwVSimMtkController mController;
    protected Handler mHandler = this.mController.getHandler();

    /* access modifiers changed from: protected */
    public abstract void logi(String str);

    public HwVSimMtkProcessor(HwVSimMtkController controller, HwVSimModemAdapter modemAdapter, HwVSimRequest request) {
        super(modemAdapter, request);
        this.mController = controller;
    }

    public void setProcessAction(HwVSimConstants.ProcessAction action) {
        this.mController.setProcessAction(action);
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public void setProcessType(HwVSimConstants.ProcessType type) {
        this.mController.setProcessType(type);
    }

    public void setProcessState(HwVSimConstants.ProcessState state) {
        this.mController.setProcessState(state);
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public boolean isEnableProcess() {
        return false;
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public boolean isDisableProcess() {
        return false;
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public boolean isWorkProcess() {
        return false;
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public boolean isReadyProcess() {
        return false;
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public boolean isSwapProcess() {
        return false;
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public boolean isCrossProcess() {
        return false;
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public boolean isDirectProcess() {
        return false;
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public boolean isSwitchModeProcess() {
        return false;
    }

    public void setDefaultDataSlotForMainSlot() {
        if (HwSubscriptionManager.getInstance() != null) {
            int default4GSlotId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
            logi("setDefaultDataSlotForMainSlot, main slot is " + default4GSlotId);
            HwSubscriptionManager.getInstance().setUserPrefDataSlotId(default4GSlotId);
            HwSubscriptionManager.getInstance().setDefaultDataSubIdToDbBySlotId(default4GSlotId);
            return;
        }
        loge("setDefaultDataSlot, HwSubscriptionManager is null!!");
    }
}
