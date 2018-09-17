package com.android.internal.telephony.vsim.process;

import android.os.AsyncResult;
import android.os.Message;
import com.android.internal.telephony.vsim.HwVSimController;
import com.android.internal.telephony.vsim.HwVSimController.EnableParam;
import com.android.internal.telephony.vsim.HwVSimController.ProcessState;
import com.android.internal.telephony.vsim.HwVSimEventReport.VSimEventInfoUtils;
import com.android.internal.telephony.vsim.HwVSimLog;
import com.android.internal.telephony.vsim.HwVSimModemAdapter;
import com.android.internal.telephony.vsim.HwVSimModemAdapter.SimStateInfo;
import com.android.internal.telephony.vsim.HwVSimRequest;
import com.android.internal.telephony.vsim.HwVSimUtilsInner;

public class HwVSimEDWorkProcessor extends HwVSimEWorkProcessor {
    public static final String LOG_TAG = "VSimEDWorkProcessor";

    public HwVSimEDWorkProcessor(HwVSimController controller, HwVSimModemAdapter modemAdapter, HwVSimRequest request) {
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
                setProcessState(ProcessState.PROCESS_STATE_WORK);
            }
        }
    }

    public void onExit() {
        logd("onExit");
    }

    public void doProcessException(AsyncResult ar, HwVSimRequest request) {
        doEnableProcessException(ar, request, Integer.valueOf(3));
    }

    protected void logd(String s) {
        HwVSimLog.VSimLogD(LOG_TAG, s);
    }

    protected void onRadioPowerOffDone(Message msg) {
        logd("onRadioPowerOffDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 2);
        AsyncResult ar = msg.obj;
        if (isAsyncResultValidForRequestNotSupport(ar)) {
            int subId = ar.userObj.mSubId;
            if (subId == 2) {
                this.mModemAdapter.getSimState(this, this.mRequest, subId);
            } else {
                this.mModemAdapter.radioPowerOff(this, this.mRequest, 2);
            }
        }
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
            int subId = ar.userObj.mSubId;
            if (ssInfo != null) {
                this.mModemAdapter.cardPowerOff(this, this.mRequest, subId, ssInfo.simIndex);
            }
        }
    }

    protected void onCardPowerOffDone(Message msg) {
        logd("onCardPowerOffDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 4);
        AsyncResult ar = msg.obj;
        if (isAsyncResultValid(ar)) {
            HwVSimRequest request = ar.userObj;
            EnableParam arg = request.getArgument();
            EnableParam param = null;
            if (arg != null) {
                param = arg;
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
            if (HwVSimUtilsInner.isPlatformRealTripple()) {
                this.mVSimController.setVSimCurCardType(this.mVSimController.getCardTypeFromEnableParam(request));
                this.mModemAdapter.cardPowerOn(this, this.mRequest, 2, 11);
            } else {
                int subId = request.mSubId;
                this.mModemAdapter.setTeeDataReady(this, this.mRequest, subId);
            }
        }
    }

    protected void onCardPowerOnDone(Message msg) {
        logd("onCardPowerOnDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 7);
        AsyncResult ar = msg.obj;
        if (isAsyncResultValidForCardPowerOn(ar)) {
            HwVSimRequest request = ar.userObj;
            int subId = request.mSubId;
            EnableParam param = getEnableParam(request);
            if (param == null) {
                doEnableProcessException(ar, request, Integer.valueOf(3));
                return;
            }
            if (HwVSimUtilsInner.isPlatformRealTripple()) {
                this.mVSimController.allowData(subId);
            }
            int networkMode = acqorderToNetworkMode(param.acqorder);
            logd("set preferred network to " + networkMode);
            this.mModemAdapter.setPreferredNetworkType(this, this.mRequest, subId, networkMode);
        }
    }

    protected boolean isAsyncResultValidForCardPowerOn(AsyncResult ar) {
        if (ar == null) {
            doProcessException(null, null);
            return false;
        }
        HwVSimRequest request = ar.userObj;
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
