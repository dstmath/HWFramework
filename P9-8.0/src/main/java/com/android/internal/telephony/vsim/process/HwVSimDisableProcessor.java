package com.android.internal.telephony.vsim.process;

import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.HwVSimPhoneFactory;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.android.internal.telephony.vsim.HwVSimController;
import com.android.internal.telephony.vsim.HwVSimController.ProcessAction;
import com.android.internal.telephony.vsim.HwVSimEventReport.VSimEventInfoUtils;
import com.android.internal.telephony.vsim.HwVSimLog;
import com.android.internal.telephony.vsim.HwVSimModemAdapter;
import com.android.internal.telephony.vsim.HwVSimRequest;

public class HwVSimDisableProcessor extends HwVSimProcessor {
    public static final String LOG_TAG = "VSimDisableProcess";
    protected Handler mHandler = this.mVSimController.getHandler();
    protected HwVSimController mVSimController;

    public HwVSimDisableProcessor(HwVSimController controller, HwVSimModemAdapter modemAdapter, HwVSimRequest request) {
        super(modemAdapter, request);
        this.mVSimController = controller;
    }

    public void onEnter() {
        logd("onEnter");
        cmdSem_release();
        if (this.mRequest == null) {
            transitionToState(0);
            return;
        }
        this.mVSimController.setProcessAction(ProcessAction.PROCESS_ACTION_DISABLE);
        HwVSimRequest request = this.mRequest;
        request.createGotCardType(HwVSimModemAdapter.PHONE_COUNT);
        request.createCardTypes(HwVSimModemAdapter.PHONE_COUNT);
        request.setGotSimSlotMark(false);
        for (int i = 0; i < HwVSimModemAdapter.MAX_SUB_COUNT; i++) {
            int subId = i;
            CommandsInterface ci = this.mModemAdapter.getCiBySub(subId);
            if (ci != null && (ci.isRadioAvailable() ^ 1) == 0) {
                this.mModemAdapter.getSimSlot(this, request.clone(), subId);
            }
        }
        this.mVSimController.setOnRadioAvaliable(this.mHandler, 83, null);
    }

    public void onExit() {
        logd("onExit");
        cleanMainSlotIfDisableSucceed();
        this.mVSimController.setBlockPinFlag(false);
        this.mModemAdapter.setHwVSimPowerOff(this, this.mRequest);
        allowDefaultData();
        this.mVSimController.setSubActivationUpdate(true);
        this.mVSimController.updateSubActivation();
        this.mVSimController.unSetOnRadioAvaliable(this.mHandler);
        HwVSimPhoneFactory.setVSimSavedNetworkMode(0, -1);
        HwVSimPhoneFactory.setVSimSavedNetworkMode(1, -1);
        this.mVSimController.delaymIsVSimCauseCardReloadRecover();
    }

    public boolean processMessage(Message msg) {
        switch (msg.what) {
            case 54:
                onGetSimSlotDone(msg);
                return true;
            case HwVSimConstants.EVENT_QUERY_CARD_TYPE_DONE /*56*/:
                onQueryCardTypeDone(msg);
                return true;
            default:
                return false;
        }
    }

    public Message obtainMessage(int what, Object obj) {
        return this.mVSimController.obtainMessage(what, obj);
    }

    public void transitionToState(int state) {
        this.mVSimController.transitionToState(state);
    }

    public void doProcessException(AsyncResult ar, HwVSimRequest request) {
        doDisableProcessException(ar, request);
    }

    protected void logd(String s) {
        HwVSimLog.VSimLogD(LOG_TAG, s);
    }

    protected void onGetSimSlotDone(Message msg) {
        logd("onGetSimSlotDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 1);
        AsyncResult ar = msg.obj;
        if (isAsyncResultValidForRequestNotSupport(ar)) {
            HwVSimRequest request = ar.userObj;
            if (request.isGotSimSlot()) {
                logd("already got sim slot, just skip");
                return;
            }
            this.mModemAdapter.onGetSimSlotDone(this, ar);
            if (request.getIsVSimOnM0()) {
                this.mModemAdapter.getAllCardTypes(this, this.mRequest);
            } else {
                notifyResult(request, Boolean.valueOf(true));
                transitionToState(0);
            }
        }
    }

    protected void onQueryCardTypeDone(Message msg) {
        logd("onQueryCardTypeDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 1);
        AsyncResult ar = msg.obj;
        if (isAsyncResultValid(ar)) {
            this.mModemAdapter.onQueryCardTypeDone(this, ar);
            if (this.mRequest.isGotAllCardTypes()) {
                logd("onQueryCardTypeDone : isGotAllCardTypes");
                this.mModemAdapter.checkDisableSimCondition(this, this.mRequest);
            }
        }
    }

    protected void cmdSem_release() {
        if (this.mVSimController != null) {
            this.mVSimController.cmdSem_release();
        }
    }

    private void allowDefaultData() {
        if (this.mVSimController != null) {
            this.mVSimController.allowDefaultData();
        }
    }

    private void cleanMainSlotIfDisableSucceed() {
        boolean iResult = false;
        if (this.mRequest != null) {
            Object oResult = this.mRequest.getResult();
            if (oResult != null) {
                iResult = ((Boolean) oResult).booleanValue();
            }
        }
        if (iResult) {
            this.mVSimController.setVSimSavedMainSlot(-1);
        } else {
            logd("leave saved main slot untouched");
        }
    }
}
