package com.android.internal.telephony.vsim.process;

import android.os.Message;
import com.android.internal.telephony.vsim.HwVSimController;
import com.android.internal.telephony.vsim.HwVSimEventReport;
import com.android.internal.telephony.vsim.HwVSimLog;
import com.android.internal.telephony.vsim.HwVSimModemAdapter;
import com.android.internal.telephony.vsim.HwVSimRequest;
import com.huawei.android.os.AsyncResultEx;

public class HwVSimEDWorkProcessor extends HwVSimEWorkProcessor {
    public static final String LOG_TAG = "VSimEDWorkProcessor";

    HwVSimEDWorkProcessor(HwVSimController controller, HwVSimModemAdapter modemAdapter, HwVSimRequest request) {
        super(controller, modemAdapter, request);
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimEWorkProcessor, com.android.internal.telephony.vsim.process.HwVSimWorkProcessor, com.android.internal.telephony.vsim.process.HwVSimProcessor
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

    @Override // com.android.internal.telephony.vsim.process.HwVSimWorkProcessor, com.android.internal.telephony.vsim.process.HwVSimProcessor
    public void onExit() {
        logd("onExit");
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimEWorkProcessor, com.android.internal.telephony.vsim.process.HwVSimProcessor
    public void doProcessException(AsyncResultEx ar, HwVSimRequest request) {
        doEnableProcessException(ar, request, 3);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.vsim.process.HwVSimEWorkProcessor, com.android.internal.telephony.vsim.process.HwVSimWorkProcessor, com.android.internal.telephony.vsim.process.HwVSimProcessor
    public void logd(String s) {
        HwVSimLog.VSimLogD(LOG_TAG, s);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.vsim.process.HwVSimWorkProcessor
    public void onRadioPowerOffDone(Message msg) {
        logd("onRadioPowerOffDone");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 2);
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (isAsyncResultValidForRequestNotSupport(ar)) {
            int subId = ((HwVSimRequest) ar.getUserObj()).mSubId;
            if (subId == 2) {
                this.mModemAdapter.getSimState(this, this.mRequest, subId);
            } else {
                this.mModemAdapter.radioPowerOff(this, this.mRequest, 2);
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.vsim.process.HwVSimWorkProcessor
    public void onGetSimStateDone(Message msg) {
        logd("onGetSimStateDone");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 3);
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (isAsyncResultValidForRequestNotSupport(ar)) {
            HwVSimModemAdapter.SimStateInfo ssInfo = this.mModemAdapter.onGetSimStateDone(ar);
            if (ssInfo != null) {
                logd("onGetSimStateDone ssInfo = " + ssInfo.toString());
            }
            int subId = ((HwVSimRequest) ar.getUserObj()).mSubId;
            if (ssInfo != null) {
                this.mModemAdapter.cardPowerOff(this, this.mRequest, subId, ssInfo.simIndex);
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.vsim.process.HwVSimEWorkProcessor, com.android.internal.telephony.vsim.process.HwVSimWorkProcessor
    public void onCardPowerOffDone(Message msg) {
        logd("onCardPowerOffDone");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 4);
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (isAsyncResultValid(ar)) {
            HwVSimRequest request = (HwVSimRequest) ar.getUserObj();
            Object arg = request.getArgument();
            HwVSimController.EnableParam param = null;
            if (arg != null) {
                param = (HwVSimController.EnableParam) arg;
            }
            if (param == null) {
                doProcessException(ar, request);
            } else {
                writeVSimOpenSession(ar, request, param);
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.vsim.process.HwVSimEWorkProcessor, com.android.internal.telephony.vsim.process.HwVSimWorkProcessor
    public void onCardPowerOnDone(Message msg) {
        logd("onCardPowerOnDone");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 7);
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (isAsyncResultValidForCardPowerOn(ar)) {
            HwVSimRequest request = (HwVSimRequest) ar.getUserObj();
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
    @Override // com.android.internal.telephony.vsim.process.HwVSimEWorkProcessor
    public boolean isAsyncResultValidForCardPowerOn(AsyncResultEx ar) {
        if (ar == null) {
            doProcessException(null, null);
            return false;
        }
        HwVSimRequest request = (HwVSimRequest) ar.getUserObj();
        if (request == null) {
            return false;
        }
        if (ar.getException() == null || request.mSubId != 2) {
            return true;
        }
        doProcessException(ar, request);
        return false;
    }
}
