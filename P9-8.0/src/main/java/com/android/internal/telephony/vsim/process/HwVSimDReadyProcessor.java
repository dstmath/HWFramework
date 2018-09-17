package com.android.internal.telephony.vsim.process;

import android.os.AsyncResult;
import android.os.Message;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.android.internal.telephony.vsim.HwVSimController;
import com.android.internal.telephony.vsim.HwVSimController.ProcessState;
import com.android.internal.telephony.vsim.HwVSimEventReport.VSimEventInfoUtils;
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
        setProcessState(ProcessState.PROCESS_STATE_READY);
        if (HwVSimUtilsInner.isPlatformRealTripple() && HwVSimUtilsInner.IS_DSDSPOWER_SUPPORT) {
            processDReadyOnDSDSRealTripplePlatform();
        } else {
            processDReady();
        }
    }

    public void onExit() {
        logd("onExit");
        setProcessState(ProcessState.PROCESS_STATE_NONE);
    }

    public boolean processMessage(Message msg) {
        switch (msg.what) {
            case 41:
                onRadioPowerOffDone(msg);
                return true;
            case 42:
                onCardPowerOffDone(msg);
                return true;
            case HwVSimConstants.EVENT_RADIO_POWER_ON_DONE /*46*/:
                onRadioPowerOnDone(msg);
                return true;
            case HwVSimConstants.EVENT_SWITCH_COMMRIL_DONE /*55*/:
                onSwitchCommrilDone(msg);
                return true;
            default:
                return false;
        }
    }

    public void doProcessException(AsyncResult ar, HwVSimRequest request) {
        doDisableProcessException(ar, request);
    }

    protected void logd(String s) {
        HwVSimLog.VSimLogD(LOG_TAG, s);
    }

    protected void onSwitchCommrilDone(Message msg) {
        logd("onSwitchCommrilDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 1);
        transitionToState(0);
    }

    protected void onCardPowerOffDone(Message msg) {
        logd("onCardPowerOffDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 4);
        AsyncResult ar = msg.obj;
        if (isAsyncResultValidForRequestNotSupport(ar)) {
            this.mVSimController.disposeCardForPinBlock(ar.userObj.mSubId);
            this.mModemAdapter.checkDisableSimCondition(this, this.mRequest);
        }
    }

    protected void onRadioPowerOffDone(Message msg) {
        logd("onRadioPowerOffDone");
        AsyncResult ar = msg.obj;
        if (isNeedWaitNvCfgMatchAndRestartRild() || (isAsyncResultValidForRequestNotSupport(ar) ^ 1) == 0) {
            HwVSimRequest request = ar.userObj;
            int subId = request.mSubId;
            logd("onRadioPowerOffDone subId = " + subId);
            this.mModemAdapter.radioPowerOn(this, request, subId);
        }
    }

    protected void onRadioPowerOnDone(Message msg) {
        logd("onRadioPowerOnDone");
        AsyncResult ar = msg.obj;
        if (isNeedWaitNvCfgMatchAndRestartRild() || (isAsyncResultValidForRequestNotSupport(ar) ^ 1) == 0) {
            processDReady();
        }
    }

    private void processDReady() {
        HwVSimModemAdapter hwVSimModemAdapter = this.mModemAdapter;
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

    protected boolean isNeedWaitNvCfgMatchAndRestartRild() {
        return HwVSimUtilsInner.isPlatformNeedWaitNvMatchUnsol() && HwVSimController.getInstance().getInsertedCardCount() != 0;
    }
}
