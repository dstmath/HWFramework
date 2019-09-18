package com.android.internal.telephony.vsim.process;

import android.os.AsyncResult;
import android.os.Message;
import com.android.internal.telephony.vsim.HwVSimController;
import com.android.internal.telephony.vsim.HwVSimEventReport;
import com.android.internal.telephony.vsim.HwVSimLog;
import com.android.internal.telephony.vsim.HwVSimModemAdapter;
import com.android.internal.telephony.vsim.HwVSimRequest;
import com.android.internal.telephony.vsim.HwVSimUtilsInner;

public class HwVSimDReadyProcessor extends HwVSimReadyProcessor {
    public static final String LOG_TAG = "VSimDReadyProcessor";

    public HwVSimDReadyProcessor(HwVSimController controller, HwVSimModemAdapter modemAdapter, HwVSimRequest request) {
        super(controller, modemAdapter, request);
    }

    public void onEnter() {
        logd("onEnter");
        setProcessState(HwVSimController.ProcessState.PROCESS_STATE_READY);
        if (HwVSimUtilsInner.IS_DSDSPOWER_SUPPORT) {
            processDReadyOnDSDSRealTripplePlatform();
        } else {
            processDReady();
        }
    }

    public void onExit() {
        logd("onExit");
        setProcessState(HwVSimController.ProcessState.PROCESS_STATE_NONE);
    }

    public boolean processMessage(Message msg) {
        int i = msg.what;
        if (i == 46) {
            onRadioPowerOnDone(msg);
            return true;
        } else if (i != 55) {
            switch (i) {
                case 41:
                    onRadioPowerOffDone(msg);
                    return true;
                case 42:
                    onCardPowerOffDone(msg);
                    return true;
                default:
                    return false;
            }
        } else {
            onSwitchCommrilDone();
            return true;
        }
    }

    public void doProcessException(AsyncResult ar, HwVSimRequest request) {
        doDisableProcessException(ar, request);
    }

    /* access modifiers changed from: protected */
    public void logd(String s) {
        HwVSimLog.VSimLogD(LOG_TAG, s);
    }

    private void onSwitchCommrilDone() {
        logd("onSwitchCommrilDone");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 1);
        transitionToState(0);
    }

    private void onCardPowerOffDone(Message msg) {
        logd("onCardPowerOffDone");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 4);
        AsyncResult ar = (AsyncResult) msg.obj;
        if (isAsyncResultValidForRequestNotSupport(ar)) {
            this.mVSimController.disposeCardForPinBlock(((HwVSimRequest) ar.userObj).mSubId);
            this.mModemAdapter.checkDisableSimCondition(this, this.mRequest);
        }
    }

    /* access modifiers changed from: protected */
    public void onRadioPowerOffDone(Message msg) {
        logd("onRadioPowerOffDone");
        AsyncResult ar = (AsyncResult) msg.obj;
        if (isNeedWaitNvCfgMatchAndRestartRild() || isAsyncResultValidForRequestNotSupport(ar)) {
            HwVSimRequest request = (HwVSimRequest) ar.userObj;
            int subId = request.mSubId;
            logd("onRadioPowerOffDone subId = " + subId);
            this.mModemAdapter.radioPowerOn(this, request, subId);
        }
    }

    private void onRadioPowerOnDone(Message msg) {
        logd("onRadioPowerOnDone");
        AsyncResult ar = (AsyncResult) msg.obj;
        if (isNeedWaitNvCfgMatchAndRestartRild() || isAsyncResultValidForRequestNotSupport(ar)) {
            processDReady();
        }
    }

    private void processDReady() {
        if (HwVSimModemAdapter.IS_FAST_SWITCH_SIMSLOT || this.mRequest == null || !this.mRequest.getIsNeedSwitchCommrilMode() || !this.mVSimController.isPinNeedBlock(this.mRequest.getMainSlot())) {
            this.mModemAdapter.checkDisableSimCondition(this, this.mRequest);
        } else {
            this.mModemAdapter.cardPowerOff(this, this.mRequest, this.mRequest.getMainSlot(), 1);
        }
    }

    private void processDReadyOnDSDSRealTripplePlatform() {
        if (this.mRequest != null) {
            int slaveSlot = this.mRequest.getExpectSlot() == 0 ? 1 : 0;
            if (this.mVSimController.getSubState(slaveSlot) == 0) {
                processDReady();
            } else {
                this.mModemAdapter.radioPowerOff(this, this.mRequest, slaveSlot);
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean isNeedWaitNvCfgMatchAndRestartRild() {
        return HwVSimUtilsInner.isPlatformNeedWaitNvMatchUnsol() && HwVSimController.getInstance().getInsertedCardCount() != 0;
    }
}
