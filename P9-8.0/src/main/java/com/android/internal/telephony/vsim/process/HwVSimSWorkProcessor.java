package com.android.internal.telephony.vsim.process;

import android.os.AsyncResult;
import android.os.Message;
import android.os.SystemProperties;
import com.android.internal.telephony.HwVSimPhoneFactory;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.android.internal.telephony.vsim.HwVSimController;
import com.android.internal.telephony.vsim.HwVSimController.WorkModeParam;
import com.android.internal.telephony.vsim.HwVSimEventReport.VSimEventInfoUtils;
import com.android.internal.telephony.vsim.HwVSimLog;
import com.android.internal.telephony.vsim.HwVSimModemAdapter;
import com.android.internal.telephony.vsim.HwVSimNvMatchController;
import com.android.internal.telephony.vsim.HwVSimRequest;
import com.android.internal.telephony.vsim.HwVSimUtilsInner;

public class HwVSimSWorkProcessor extends HwVSimEWorkProcessor {
    public static final String LOG_TAG = "HwVSimSWorkProcessor";

    public HwVSimSWorkProcessor(HwVSimController controller, HwVSimModemAdapter modemAdapter, HwVSimRequest request) {
        super(controller, modemAdapter, request);
    }

    public void onEnter() {
        super.onEnter();
    }

    public void onExit() {
        super.onExit();
    }

    public Message obtainMessage(int what, Object obj) {
        return this.mVSimController.obtainMessage(what, obj);
    }

    public void transitionToState(int state) {
        this.mVSimController.transitionToState(state);
    }

    public void doProcessException(AsyncResult ar, HwVSimRequest request) {
        doSwitchModeProcessException(ar, request);
    }

    protected void logd(String s) {
        HwVSimLog.VSimLogD(LOG_TAG, s);
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
            case 43:
                onSwitchSlotDone(msg);
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
            case HwVSimConstants.EVENT_SET_ACTIVE_MODEM_MODE_DONE /*47*/:
                onSetActiveModemModeDone(msg);
                return true;
            case HwVSimConstants.EVENT_GET_PREFERRED_NETWORK_TYPE_DONE /*48*/:
                onGetPreferredNetworkTypeDone(msg);
                return true;
            case HwVSimConstants.EVENT_SET_PREFERRED_NETWORK_TYPE_DONE /*49*/:
                onSetPreferredNetworkTypeDone(msg);
                return true;
            case 59:
                onSwitchWorkModeDone(msg);
                return true;
            case HwVSimConstants.EVENT_GET_ICC_STATUS_DONE /*79*/:
                onGetIccCardStatusDone(msg);
                return true;
            case HwVSimConstants.EVENT_SET_CDMA_MODE_SIDE_DONE /*80*/:
                onSetCdmaModeSideDone(msg);
                return true;
            case HwVSimConstants.EVENT_JUDGE_RESTART_RILD_NV_MATCH /*81*/:
                onJudgeRestartRildNvMatch(msg);
                return true;
            case HwVSimConstants.EVENT_JUDGE_RESTART_RILD_NV_MATCH_TIMEOUT /*82*/:
                onJudgeRestartRildNvMatchTimeout();
                return true;
            case HwVSimConstants.EVENT_RADIO_AVAILABLE /*83*/:
                onRadioAvailable(msg);
                return true;
            default:
                return false;
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

    protected void afterGetAllCardStateDone() {
        logd("afterGetAllCardStateDone: onGetIccCardStatusDone->switchSimSlot");
        this.mModemAdapter.switchSimSlot(this, this.mRequest);
    }

    protected void onSwitchSlotDone(Message msg) {
        logd("onSwitchSlotDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 5);
        AsyncResult ar = msg.obj;
        if (isAsyncResultValid(ar)) {
            this.mModemAdapter.onSwitchSlotDone(this, ar);
            HwVSimRequest request = ar.userObj;
            if (isCrossProcess() && this.mVSimController.getInsertedCardCount() != 0) {
                int mainSlot = request.getMainSlot();
                logd("onSwitchSlotDone, cross and insert at least one card ,update switch dual card slot -> " + mainSlot);
                HwVSimPhoneFactory.setUserSwitchDualCardSlots(mainSlot);
            }
            HwVSimModemAdapter hwVSimModemAdapter = this.mModemAdapter;
            if (HwVSimModemAdapter.IS_FAST_SWITCH_SIMSLOT && request.getIsNeedSwitchCommrilMode()) {
                setCdmaModeSide(msg);
            } else {
                cardPowerOn(msg);
            }
        }
    }

    protected void onSetCdmaModeSideDone(Message msg) {
        logd("onSetCdmaModeSideDone");
        AsyncResult ar = msg.obj;
        if (isAsyncResultValid(ar)) {
            HwVSimRequest request = ar.userObj;
            HwVSimController.getInstance().setIsWaitingSwitchCdmaModeSide(false);
            SystemProperties.set(HwVSimModemAdapter.PROPERTY_COMMRIL_MODE, request.getExpectCommrilMode().toString());
            cardPowerOn(msg);
        }
    }

    private void cardPowerOn(Message msg) {
        HwVSimRequest request = msg.obj.userObj;
        logd("cardPoweron, subId = " + request.mSubId);
        if (isSwapProcess()) {
            logd("mainSlot = " + request.getMainSlot());
        } else if (isCrossProcess()) {
            request.setMainSlot(request.getExpectSlot());
        }
        if (HwVSimUtilsInner.isPlatformRealTripple()) {
            this.mModemAdapter.cardPowerOn(this, this.mRequest, 2, 11);
            if (HwVSimUtilsInner.isPlatformNeedWaitNvMatchUnsol() && HwVSimNvMatchController.getInstance().getIfNeedRestartRildForNvMatch()) {
                this.mIsWaitingRestartRild = true;
                restartRild();
                return;
            } else if (isNeedWaitNvCfgMatchAndRestartRild()) {
                int subId;
                startListenForRildNvMatch();
                if (this.mRequest.getMainSlot() == 0) {
                    subId = 1;
                } else {
                    subId = 0;
                }
                this.mModemAdapter.cardPowerOn(this, this.mRequest.clone(), subId, 1);
                return;
            } else {
                return;
            }
        }
        this.mModemAdapter.setTeeDataReady(this, this.mRequest, 2);
    }

    protected void afterJudgeRestartRildNvMatch() {
        logd("afterJudgeRestartRildNvMatch");
        this.mIsWaitingRestartRild = false;
        this.mHasReceiveNvMatchUnsol = false;
        this.mModemAdapter.setActiveModemMode(this, this.mRequest, 2);
    }

    protected void onRadioPowerOnDone(Message msg) {
        logd("onRadioPowerOnDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 11);
        if (isAsyncResultValid(msg.obj)) {
            switchWorkModeDone();
        }
    }

    protected void onSwitchWorkModeDone(Message msg) {
        logd("onSwitchWorkModeDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 1);
        notifyResult(this.mRequest, Boolean.valueOf(true));
        transitionToState(13);
    }

    private void switchWorkModeDone() {
        logd("switchWorkModeDone");
        Message onCompleted = obtainMessage(59, this.mRequest);
        AsyncResult.forMessage(onCompleted);
        onCompleted.sendToTarget();
    }

    protected void onSetTeeDataReadyDone(Message msg) {
        logd("onSetTeeDataReadyDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 6);
        if (isAsyncResultValid(msg.obj)) {
            this.mModemAdapter.cardPowerOn(this, this.mRequest, 2, 11);
        }
    }

    protected void onCardPowerOnDone(Message msg) {
        logd("onCardPowerOnDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 7);
        AsyncResult ar = msg.obj;
        if (isAsyncResultValidNoProcessException(ar)) {
            logd("onCardPowerOnDone : subId  = " + ar.userObj.mSubId);
            if (isNeedWaitNvCfgMatchAndRestartRild() || this.mIsWaitingRestartRild) {
                logd("if dual ims support and cross process, wait for nv cfg result unsol.");
            } else {
                this.mModemAdapter.setActiveModemMode(this, this.mRequest, 2);
            }
        }
    }

    protected void onSetActiveModemModeDone(Message msg) {
        logd("onSetActiveModemModeDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 8);
        AsyncResult ar = msg.obj;
        if (isAsyncResultValid(ar)) {
            this.mModemAdapter.getPreferredNetworkType(this, this.mRequest, ar.userObj.mSubId);
        }
    }

    private void onGetPreferredNetworkTypeDone(Message msg) {
        logd("onGetPreferredNetworkTypeDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 9);
        AsyncResult ar = msg.obj;
        if (isAsyncResultValid(ar)) {
            HwVSimRequest request = ar.userObj;
            int subId = request.mSubId;
            WorkModeParam param = getWorkModeParam(request);
            if (param == null) {
                doProcessException(ar, request);
                return;
            }
            int workMode = param.workMode;
            boolean removeG = false;
            if (!HwVSimUtilsInner.isPlatformRealTripple() && workMode == 2 && this.mVSimController.hasIccCardOnM2()) {
                removeG = true;
            }
            int networkMode = modifyNetworkMode(((int[]) ar.result)[0], removeG);
            logd("set preferred network to " + networkMode);
            this.mModemAdapter.setPreferredNetworkType(this, request, subId, networkMode);
        }
    }

    protected WorkModeParam getWorkModeParam(HwVSimRequest request) {
        if (this.mVSimController == null) {
            return null;
        }
        return this.mVSimController.getWorkModeParam(request);
    }
}
