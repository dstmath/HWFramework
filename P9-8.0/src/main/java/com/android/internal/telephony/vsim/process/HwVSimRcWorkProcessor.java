package com.android.internal.telephony.vsim.process;

import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.android.internal.telephony.vsim.HwVSimController;
import com.android.internal.telephony.vsim.HwVSimController.ProcessState;
import com.android.internal.telephony.vsim.HwVSimEventReport.VSimEventInfoUtils;
import com.android.internal.telephony.vsim.HwVSimLog;
import com.android.internal.telephony.vsim.HwVSimModemAdapter;
import com.android.internal.telephony.vsim.HwVSimModemAdapter.SimStateInfo;
import com.android.internal.telephony.vsim.HwVSimRequest;
import com.android.internal.telephony.vsim.HwVSimUtilsInner;

public class HwVSimRcWorkProcessor extends HwVSimWorkProcessor {
    public static final String LOG_TAG = "HwVSimRcWorkProcessor";
    private Handler mHandler = null;
    private boolean mIsRdOffM0Done = false;
    private boolean mIsRdOffM2Done = false;

    public HwVSimRcWorkProcessor(HwVSimController controller, HwVSimModemAdapter modemAdapter, HwVSimRequest request) {
        super(controller, modemAdapter, request);
        this.mHandler = controller.getHandler();
    }

    public void onEnter() {
        logd("onEnter");
        setProcessState(ProcessState.PROCESS_STATE_WORK);
    }

    public boolean processMessage(Message msg) {
        switch (msg.what) {
            case 2:
                onGetSimStateDone(msg);
                return true;
            case 41:
                onRadioPowerOffDone(msg);
                return true;
            case 42:
                onCardPowerOffDone(msg);
                return true;
            case HwVSimConstants.EVENT_SET_TEE_DATA_READY_DONE /*44*/:
                onSetTeeDataReadyDone(msg);
                return true;
            case HwVSimConstants.EVENT_CARD_POWER_ON_DONE /*45*/:
                onCardPowerOnDone(msg);
                return true;
            case HwVSimConstants.EVENT_RADIO_POWER_ON_DONE /*46*/:
                onRadioPowerOnDone(msg);
                return true;
            default:
                return false;
        }
    }

    public void doProcessException(AsyncResult ar, HwVSimRequest request) {
        doReconnectProcessException(ar, request);
    }

    protected void logd(String s) {
        HwVSimLog.VSimLogD(LOG_TAG, s);
    }

    protected void onRadioPowerOffDone(Message msg) {
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 2);
        AsyncResult ar = msg.obj;
        if (isAsyncResultValidForRequestNotSupport(ar)) {
            HwVSimRequest request = ar.userObj;
            int subId = request.mSubId;
            logd("onRadioPowerOffDone subId = " + subId);
            if (subId == 2) {
                this.mIsRdOffM0Done = true;
            } else {
                this.mIsRdOffM2Done = true;
            }
            if (this.mIsRdOffM0Done && this.mIsRdOffM2Done) {
                request.mSubId = 2;
                this.mModemAdapter.getSimState(this, request, 2);
            }
            return;
        }
        logd("onRadioPowerOffDone exception");
        doProcessException(ar, null);
    }

    protected void onGetSimStateDone(Message msg) {
        logd("onGetSimStateDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 3);
        AsyncResult ar = msg.obj;
        if (isAsyncResultValidForRequestNotSupport(ar)) {
            SimStateInfo ssInfo = this.mModemAdapter.onGetSimStateDone(this, ar);
            if (ssInfo != null) {
                logd("onGetSimStateDone ssInfo index = " + ssInfo.simIndex);
                logd("onGetSimStateDone ssInfo simEnable = " + ssInfo.simEnable);
                logd("onGetSimStateDone ssInfo simSub = " + ssInfo.simSub);
                logd("onGetSimStateDone ssInfo simNetInfo = " + ssInfo.simNetInfo);
            }
            HwVSimRequest request = ar.userObj;
            int subId = request.mSubId;
            logd("onGetSimStateDone subId = " + subId);
            if (ssInfo != null) {
                this.mModemAdapter.cardPowerOff(this, request, subId, ssInfo.simIndex);
            }
            return;
        }
        logd("onGetSimStateDone exception");
        doProcessException(ar, null);
    }

    protected void onCardPowerOffDone(Message msg) {
        logd("onCardPowerOffDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 4);
        AsyncResult ar = msg.obj;
        if (isAsyncResultValidForRequestNotSupport(ar)) {
            HwVSimRequest request = ar.userObj;
            int subId = request.mSubId;
            logd("onCardPowerOffDone subId:" + subId);
            if (HwVSimUtilsInner.isPlatformRealTripple()) {
                int simIndex = 1;
                if (subId == 2) {
                    simIndex = 11;
                }
                this.mModemAdapter.cardPowerOn(this, request, subId, simIndex);
            } else {
                this.mModemAdapter.setTeeDataReady(this, request, 2);
            }
            return;
        }
        logd("onGetSimStateDone exception");
        doProcessException(ar, null);
    }

    protected void onSetTeeDataReadyDone(Message msg) {
        logd("onSetTeeDataReadyDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 6);
        AsyncResult ar = msg.obj;
        if (isAsyncResultValidForRequestNotSupport(ar)) {
            int simIndex = 1;
            HwVSimRequest request = ar.userObj;
            int subId = request.mSubId;
            if (subId == 2) {
                simIndex = 11;
            }
            logd("onSetTeeDataReadyDone  subId:" + subId + " simIndex:" + simIndex);
            this.mModemAdapter.cardPowerOn(this, request, subId, simIndex);
            return;
        }
        logd("onGetSimStateDone exception");
        doProcessException(ar, null);
    }

    protected void onCardPowerOnDone(Message msg) {
        logd("onCardPowerOnDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 7);
        AsyncResult ar = msg.obj;
        if (isAsyncResultValidForRequestNotSupport(ar)) {
            HwVSimRequest request = ar.userObj;
            logd("onCardPowerOnDone : subId  = " + request.mSubId);
            this.mModemAdapter.radioPowerOn(this, request, 2);
            return;
        }
        logd("onGetSimStateDone exception");
        doProcessException(ar, null);
    }

    protected void onRadioPowerOnDone(Message msg) {
        logd("onRadioPowerOnDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 11);
        AsyncResult ar = msg.obj;
        if (isAsyncResultValidForRequestNotSupport(ar)) {
            HwVSimRequest request = ar.userObj;
            logd("onRadioPowerOnDone request: " + request);
            logd("onRadioPowerOnDone: subId: " + request.mSubId);
            if (2 != request.mSubId) {
                transitionToState(0);
                return;
            }
            transitionToState(10);
            this.mHandler.sendMessage(this.mHandler.obtainMessage(64, request));
            return;
        }
        logd("onGetSimStateDone exception");
        doProcessException(ar, null);
    }

    protected void onSetActiveModemModeDone(Message msg) {
    }

    protected void onSetPreferredNetworkTypeDone(Message msg) {
    }

    protected void onSwitchSlotDone(Message msg) {
    }
}
