package com.android.internal.telephony.vsim.process;

import android.os.AsyncResult;
import android.os.Message;
import com.android.internal.telephony.vsim.HwVSimController;
import com.android.internal.telephony.vsim.HwVSimEventReport;
import com.android.internal.telephony.vsim.HwVSimLog;
import com.android.internal.telephony.vsim.HwVSimModemAdapter;
import com.android.internal.telephony.vsim.HwVSimRequest;

public class HwVSimEDWorkProcessor extends HwVSimEWorkProcessor {
    public static final String LOG_TAG = "VSimEDWorkProcessor";

    HwVSimEDWorkProcessor(HwVSimController controller, HwVSimModemAdapter modemAdapter, HwVSimRequest request) {
        super(controller, modemAdapter, request);
    }

    public void onEnter() {
        logd("onEnter");
        HwVSimRequest request = this.mRequest;
        if (request != null) {
            int subId = this.mModemAdapter.getPoffSubForEDWork(request);
            logd("onEnter subId = " + subId);
            if (subId != -1) {
                this.mModemAdapter.radioPowerOff(this, request, subId);
                setProcessState(HwVSimController.ProcessState.PROCESS_STATE_WORK);
            }
        }
    }

    public void onExit() {
        logd("onExit");
    }

    public void doProcessException(AsyncResult ar, HwVSimRequest request) {
        doEnableProcessException(ar, request, 3);
    }

    /* access modifiers changed from: protected */
    public void logd(String s) {
        HwVSimLog.VSimLogD(LOG_TAG, s);
    }

    /* access modifiers changed from: protected */
    public void onRadioPowerOffDone(Message msg) {
        logd("onRadioPowerOffDone");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 2);
        AsyncResult ar = (AsyncResult) msg.obj;
        if (isAsyncResultValidForRequestNotSupport(ar)) {
            int subId = ((HwVSimRequest) ar.userObj).mSubId;
            if (subId == 2) {
                this.mModemAdapter.getSimState(this, this.mRequest, subId);
            } else {
                this.mModemAdapter.radioPowerOff(this, this.mRequest, 2);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onGetSimStateDone(Message msg) {
        logd("onGetSimStateDone");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 3);
        AsyncResult ar = (AsyncResult) msg.obj;
        if (isAsyncResultValidForRequestNotSupport(ar)) {
            HwVSimModemAdapter.SimStateInfo ssInfo = this.mModemAdapter.onGetSimStateDone(this, ar);
            if (ssInfo != null) {
                logd("onGetSimStateDone ssInfo = " + ssInfo.toString());
            }
            int subId = ((HwVSimRequest) ar.userObj).mSubId;
            if (ssInfo != null) {
                this.mModemAdapter.cardPowerOff(this, this.mRequest, subId, ssInfo.simIndex);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onCardPowerOffDone(Message msg) {
        logd("onCardPowerOffDone");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 4);
        AsyncResult ar = (AsyncResult) msg.obj;
        if (isAsyncResultValid(ar)) {
            HwVSimRequest request = (HwVSimRequest) ar.userObj;
            Object arg = request.getArgument();
            HwVSimController.EnableParam param = null;
            if (arg != null) {
                param = (HwVSimController.EnableParam) arg;
            }
            if (param == null) {
                doProcessException(ar, request);
                return;
            }
            int result = this.mVSimController.writeVsimToTA(param.imsi, param.cardType, param.apnType, param.challenge, param.taPath, param.vsimLoc, 0);
            if (result != 0) {
                doEnableProcessException(ar, request, Integer.valueOf(result));
                return;
            }
            this.mVSimController.setVSimCurCardType(this.mVSimController.getCardTypeFromEnableParam(request));
            this.mModemAdapter.cardPowerOn(this, this.mRequest, 2, 11);
        }
    }

    /* access modifiers changed from: protected */
    public void onCardPowerOnDone(Message msg) {
        logd("onCardPowerOnDone");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 7);
        AsyncResult ar = (AsyncResult) msg.obj;
        if (isAsyncResultValidForCardPowerOn(ar)) {
            HwVSimRequest request = (HwVSimRequest) ar.userObj;
            int subId = request.mSubId;
            HwVSimController.EnableParam param = getEnableParam(request);
            if (param == null) {
                doEnableProcessException(ar, request, 3);
                return;
            }
            this.mVSimController.allowData(subId);
            int networkMode = acqorderToNetworkMode(param.acqorder);
            logd("set preferred network to " + networkMode);
            this.mModemAdapter.setPreferredNetworkType(this, this.mRequest, subId, networkMode);
        }
    }

    /* access modifiers changed from: protected */
    public boolean isAsyncResultValidForCardPowerOn(AsyncResult ar) {
        if (ar == null) {
            doProcessException(null, null);
            return false;
        }
        HwVSimRequest request = (HwVSimRequest) ar.userObj;
        if (request == null) {
            return false;
        }
        if (ar.exception == null || request.mSubId != 2) {
            return true;
        }
        doProcessException(ar, request);
        return false;
    }
}
