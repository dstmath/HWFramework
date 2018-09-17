package com.android.internal.telephony.vsim.process;

import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.uicc.IccCardStatus.CardState;
import com.android.internal.telephony.uicc.IccCardStatusUtils;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.android.internal.telephony.vsim.HwVSimController;
import com.android.internal.telephony.vsim.HwVSimController.EnableParam;
import com.android.internal.telephony.vsim.HwVSimController.ProcessAction;
import com.android.internal.telephony.vsim.HwVSimEventReport.VSimEventInfoUtils;
import com.android.internal.telephony.vsim.HwVSimLog;
import com.android.internal.telephony.vsim.HwVSimModemAdapter;
import com.android.internal.telephony.vsim.HwVSimModemAdapter.SimStateInfo;
import com.android.internal.telephony.vsim.HwVSimRequest;
import com.android.internal.telephony.vsim.HwVSimSlotSwitchController;
import com.android.internal.telephony.vsim.HwVSimUtilsInner;

public class HwVSimEnableProcessor extends HwVSimProcessor {
    public static final String LOG_TAG = "VSimEnableProcessor";
    protected Handler mHandler;
    private CardState mVSimCardState;
    protected HwVSimController mVSimController;

    public HwVSimEnableProcessor(HwVSimController controller, HwVSimModemAdapter modemAdapter, HwVSimRequest request) {
        super(modemAdapter, request);
        this.mVSimCardState = CardState.CARDSTATE_ABSENT;
        this.mVSimController = controller;
        this.mHandler = this.mVSimController.getHandler();
    }

    public void onEnter() {
        logd("onEnter");
        cmdSem_release();
        HwVSimRequest request = this.mRequest;
        if (request == null) {
            transitionToState(0);
            return;
        }
        EnableParam param = getEnableParam(request);
        if (param == null) {
            notifyResult(request, Integer.valueOf(3));
            transitionToState(0);
            return;
        }
        if (param.operation == 5) {
            this.mVSimController.setProcessAction(ProcessAction.PROCESS_ACTION_ENABLE_OFFLINE);
        } else {
            this.mVSimController.setProcessAction(ProcessAction.PROCESS_ACTION_ENABLE);
        }
        this.mModemAdapter.doEnableStateEnter(this, request);
        this.mVSimController.registerForVSimIccChanged(this.mHandler, 3, Integer.valueOf(2));
        request.createGotCardType(HwVSimModemAdapter.PHONE_COUNT);
        request.createCardTypes(HwVSimModemAdapter.PHONE_COUNT);
        this.mVSimController.updateUiccCardCount();
        request.setGotSimSlotMark(false);
        for (int i = 0; i < HwVSimModemAdapter.MAX_SUB_COUNT; i++) {
            int subId = i;
            CommandsInterface ci = this.mModemAdapter.getCiBySub(subId);
            if (ci != null && ci.isRadioAvailable()) {
                this.mModemAdapter.getSimSlot(this, request.clone(), subId);
            }
        }
    }

    public void onExit() {
        logd("onExit");
        this.mVSimController.unregisterForVSimIccChanged(this.mHandler);
        this.mVSimController.setBlockPinFlag(false);
        this.mVSimController.clearProhibitSubUpdateSimNoChange();
    }

    public boolean processMessage(Message msg) {
        switch (msg.what) {
            case HwVSimUtilsInner.STATE_EB /*2*/:
                onGetSimStateDone(msg);
                return true;
            case HwVSimSlotSwitchController.CARD_TYPE_DUAL_MODE /*3*/:
                onIccChanged(msg);
                return true;
            case HwVSimConstants.EVENT_GET_SIM_SLOT_DONE /*54*/:
                onGetSimSlotDone(msg);
                return true;
            case HwVSimConstants.EVENT_SWITCH_COMMRIL_DONE /*55*/:
                onSwitchCommrilDone(msg);
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
        doEnableProcessException(ar, request, Integer.valueOf(3));
    }

    protected void logd(String s) {
        HwVSimLog.VSimLogD(LOG_TAG, s);
    }

    protected void onGetSimSlotDone(Message msg) {
        logd("onGetSimSlotDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 1);
        AsyncResult ar = msg.obj;
        if (ar != null && ar.exception != null && isRequestNotSupport(ar.exception)) {
            logd("request not support, just skip");
        } else if (isAsyncResultValidForRequestNotSupport(ar)) {
            HwVSimRequest request = ar.userObj;
            if (request.isGotSimSlot()) {
                logd("already got sim slot, just skip");
                return;
            }
            this.mModemAdapter.onGetSimSlotDone(this, ar);
            boolean isVSimOnM0 = request.getIsVSimOnM0();
            logd("onGetSimSlotDone isVSimOnM0 = " + isVSimOnM0);
            if (isVSimOnM0) {
                this.mModemAdapter.getSimState(this, request, 2);
            } else {
                this.mVSimController.setIsVSimOn(false);
                this.mModemAdapter.checkVSimCondition(this, this.mRequest);
            }
        }
    }

    protected void onGetSimStateDone(Message msg) {
        boolean z = true;
        logd("onGetSimStateDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 3);
        AsyncResult ar = msg.obj;
        if (isAsyncResultValid(ar)) {
            SimStateInfo ssInfo = this.mModemAdapter.onGetSimStateDone(this, ar);
            if (ssInfo != null) {
                logd("onGetSimStateDone ssInfo index = " + ssInfo.simIndex);
                logd("onGetSimStateDone ssInfo simEnable = " + ssInfo.simEnable);
                logd("onGetSimStateDone ssInfo simSub = " + ssInfo.simSub);
                logd("onGetSimStateDone ssInfo simNetInfo = " + ssInfo.simNetInfo);
            }
            if (ssInfo != null) {
                if (ssInfo.simIndex != 11) {
                    logd("warning, slot switched yet vsim not loaded");
                }
                if (ssInfo.simSub != 2) {
                    logd("warning, wrong sim sub");
                }
                HwVSimController hwVSimController = this.mVSimController;
                if (ssInfo.simNetInfo != 1) {
                    z = false;
                }
                hwVSimController.setIsVSimOn(z);
            }
            this.mModemAdapter.checkVSimCondition(this, this.mRequest);
        }
    }

    protected void onQueryCardTypeDone(Message msg) {
        logd("onQueryCardTypeDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 1);
        AsyncResult ar = msg.obj;
        if (isAsyncResultValid(ar)) {
            this.mModemAdapter.onQueryCardTypeDone(this, ar);
            if (this.mRequest.isGotAllCardTypes()) {
                this.mModemAdapter.checkEnableSimCondition(this, this.mRequest);
            }
        }
    }

    protected void onSwitchCommrilDone(Message msg) {
        logd("onSwitchCommrilDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 1);
        AsyncResult ar = msg.obj;
        if (isAsyncResultValid(ar)) {
            this.mModemAdapter.onSwitchCommrilDone(this, ar);
            transitionToState(3);
        }
    }

    protected void onIccChanged(Message msg) {
        logd("onIccChanged");
        if (msg.obj == null) {
            logd("onIccChanged : ar null");
        } else {
            updateIccAvailability(HwVSimUtilsInner.getCiIndex(msg).intValue());
        }
    }

    private void updateIccAvailability(int subId) {
        logd("updateIccAvailability, subId = " + subId);
        if (subId != 2) {
            logd("only deal with VSIM");
            return;
        }
        UiccCard newCard = this.mVSimController.getUiccCard(subId);
        if (newCard == null) {
            logd("new card null");
            return;
        }
        CardState newState = newCard.getCardState();
        CardState oldState = this.mVSimCardState;
        this.mVSimCardState = newState;
        logd("Slot[" + subId + "]: New Card State = " + newState + " " + "Old Card State = " + oldState);
        if (!IccCardStatusUtils.isCardPresent(oldState) && IccCardStatusUtils.isCardPresent(newState)) {
            logd("vsim inserted");
            switchDDS();
        }
    }

    private void switchDDS() {
        if (this.mVSimController != null) {
            this.mVSimController.switchDDS();
        }
    }

    protected EnableParam getEnableParam(HwVSimRequest request) {
        if (this.mVSimController == null) {
            return null;
        }
        return this.mVSimController.getEnableParam(request);
    }

    protected void cmdSem_release() {
        if (this.mVSimController != null) {
            this.mVSimController.cmdSem_release();
        }
    }
}
