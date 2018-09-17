package com.android.internal.telephony.vsim.process;

import android.os.AsyncResult;
import android.os.Message;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.android.internal.telephony.vsim.HwVSimController;
import com.android.internal.telephony.vsim.HwVSimController.ProcessAction;
import com.android.internal.telephony.vsim.HwVSimEventReport.VSimEventInfoUtils;
import com.android.internal.telephony.vsim.HwVSimLog;
import com.android.internal.telephony.vsim.HwVSimModemAdapter;
import com.android.internal.telephony.vsim.HwVSimRequest;
import com.android.internal.telephony.vsim.HwVSimUtilsInner;

public class HwVSimReconnectProcessor extends HwVSimProcessor {
    public static final String LOG_TAG = "HwVSimReconnectProcessor";
    protected HwVSimController mVSimController;

    public HwVSimReconnectProcessor(HwVSimController controller, HwVSimModemAdapter modemAdapter, HwVSimRequest request) {
        super(modemAdapter, request);
        this.mVSimController = controller;
    }

    public void onEnter() {
        logd("onEnter");
    }

    public void onExit() {
        logd("onExit");
        setProcessAction(ProcessAction.PROCESS_ACTION_NONE);
    }

    public boolean processMessage(Message msg) {
        switch (msg.what) {
            case 54:
                onGetSimSlotDone(msg);
                return true;
            case HwVSimConstants.EVENT_RADIO_STATE_CHANGED_ON /*62*/:
                onRadioStateChangedOn(msg);
                return true;
            default:
                return false;
        }
    }

    protected void onRadioStateChangedOn(Message msg) {
        if (!HwVSimUtilsInner.isPlatformRealTripple()) {
            logd("onRadioStateChangedOn isReconnectProcess:" + this.mVSimController.isReconnectProcess());
            if (!this.mVSimController.isReconnectProcess()) {
                VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 1);
                this.mVSimController.updateUiccCardCount();
                setProcessAction(ProcessAction.PROCESS_ACTION_RECONNECT);
                this.mModemAdapter.getSimSlot(this, new HwVSimRequest(null, 2), 2);
            }
        }
    }

    public Message obtainMessage(int what, Object obj) {
        return this.mVSimController.obtainMessage(what, obj);
    }

    public void transitionToState(int state) {
        this.mVSimController.transitionToState(state);
    }

    public void doProcessException(AsyncResult ar, HwVSimRequest request) {
        doReconnectProcessException(ar, request);
    }

    protected void logd(String s) {
        HwVSimLog.VSimLogD(LOG_TAG, s);
    }

    protected void onGetSimSlotDone(Message msg) {
        logd("onGetSimSlotDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 1);
        AsyncResult ar = msg.obj;
        if (isAsyncResultValidForRequestNotSupport(ar)) {
            this.mModemAdapter.onReconnectGetSimSlotDone(this, ar);
        } else {
            doProcessException(ar, null);
        }
    }
}
