package com.android.internal.telephony.vsim.process;

import android.os.AsyncResult;
import android.os.Message;
import android.os.SystemProperties;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.android.internal.telephony.vsim.HwVSimController;
import com.android.internal.telephony.vsim.HwVSimController.EnableParam;
import com.android.internal.telephony.vsim.HwVSimEventReport.VSimEventInfoUtils;
import com.android.internal.telephony.vsim.HwVSimLog;
import com.android.internal.telephony.vsim.HwVSimModemAdapter;
import com.android.internal.telephony.vsim.HwVSimRequest;
import com.android.internal.telephony.vsim.HwVSimSlotSwitchController.CommrilMode;
import com.android.internal.telephony.vsim.HwVSimUtilsInner;

public class HwVSimEWorkProcessor extends HwVSimWorkProcessor {
    public static final String LOG_TAG = "VSimEWorkProcessor";
    private Message mMessage;

    public static HwVSimEWorkProcessor create(HwVSimController controller, HwVSimModemAdapter modemAdapter, HwVSimRequest request) {
        if (controller == null || !controller.isDirectProcess()) {
            return new HwVSimEWorkProcessor(controller, modemAdapter, request);
        }
        return new HwVSimEDWorkProcessor(controller, modemAdapter, request);
    }

    public HwVSimEWorkProcessor(HwVSimController controller, HwVSimModemAdapter modemAdapter, HwVSimRequest request) {
        super(controller, modemAdapter, request);
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
            case HwVSimConstants.EVENT_SET_PREFERRED_NETWORK_TYPE_DONE /*49*/:
                onSetPreferredNetworkTypeDone(msg);
                return true;
            case HwVSimConstants.EVENT_ENABLE_VSIM_DONE /*51*/:
                onEnableVSimDone(msg);
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

    public void doProcessException(AsyncResult ar, HwVSimRequest request) {
        doEnableProcessException(ar, request, Integer.valueOf(3));
    }

    protected void logd(String s) {
        HwVSimLog.VSimLogD(LOG_TAG, s);
    }

    protected void onSwitchSlotDone(Message msg) {
        logd("onSwitchSlotDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 5);
        AsyncResult ar = msg.obj;
        if (isAsyncResultValid(ar)) {
            this.mModemAdapter.onSwitchSlotDone(this, ar);
            this.mVSimController.clearAllMarkForCardReload();
            this.mVSimController.setBlockPinFlag(false);
            HwVSimRequest request = ar.userObj;
            HwVSimModemAdapter hwVSimModemAdapter = this.mModemAdapter;
            if (HwVSimModemAdapter.IS_FAST_SWITCH_SIMSLOT && request.getIsNeedSwitchCommrilMode()) {
                setCdmaModeSide(msg);
            } else {
                cardPowerOnModem1orWriteVSim(msg);
            }
        }
    }

    protected void onSetCdmaModeSideDone(Message msg) {
        logd("onSetCdmaModeSideDone");
        AsyncResult ar = msg.obj;
        if (isAsyncResultValid(ar)) {
            HwVSimRequest request = ar.userObj;
            HwVSimController.getInstance().setIsWaitingSwitchCdmaModeSide(false);
            CommrilMode expectCommrilMode = request.getExpectCommrilMode();
            logd("onSetCdmaModeSideDone, expectCommrilMode = " + expectCommrilMode);
            SystemProperties.set(HwVSimModemAdapter.PROPERTY_COMMRIL_MODE, expectCommrilMode.toString());
            cardPowerOnModem1orWriteVSim(msg);
        }
    }

    private void cardPowerOnModem1orWriteVSim(Message msg) {
        if (isNeedWaitNvCfgMatchAndRestartRild()) {
            logd("cardPowerOnModem1orWriteVSim");
            this.mMessage = Message.obtain(msg);
            cardPowerOnModem1andWaitForNvCfgMatch(msg);
            return;
        }
        writeVsim(msg);
    }

    private void cardPowerOnModem1andWaitForNvCfgMatch(Message msg) {
        logd("cardPowerOnModem1andWaitForNvCfgMatch");
        HwVSimRequest request = msg.obj.userObj;
        this.mModemAdapter.cardPowerOn(this, this.mRequest, request.getMainSlot(), 1);
        int subId = request.getExpectSlot();
        this.mModemAdapter.cardPowerOn(this, request.clone(), subId, 1);
        startListenForRildNvMatch();
    }

    private void writeVsim(Message msg) {
        AsyncResult ar = msg.obj;
        HwVSimRequest request = ar.userObj;
        int subId = request.mSubId;
        if (isSwapProcess()) {
            logd("mainSlot = " + request.getMainSlot());
        } else if (isCrossProcess()) {
            request.setMainSlot(request.getExpectSlot());
        }
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
            this.mModemAdapter.setTeeDataReady(this, this.mRequest, 2);
        }
    }

    protected void onCardPowerOffDone(Message msg) {
        logd("onCardPowerOffDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 4);
        AsyncResult ar = msg.obj;
        if (isAsyncResultValidForRequestNotSupport(ar)) {
            HwVSimRequest request = ar.userObj;
            int subId = request.mSubId;
            logd("onCardPowerOffDone, subId: " + subId);
            int subCount = request.getSubCount();
            for (int i = 0; i < subCount; i++) {
                if (subId == request.getSubIdByIndex(i)) {
                    request.setCardOnOffMark(i, false);
                }
            }
            getIccCardStatus(request, subId);
        }
    }

    protected void onSetTeeDataReadyDone(Message msg) {
        logd("onSetTeeDataReadyDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 6);
        AsyncResult ar = msg.obj;
        if (isAsyncResultValid(ar, Integer.valueOf(4))) {
            this.mVSimController.setVSimCurCardType(this.mVSimController.getCardTypeFromEnableParam(ar.userObj));
            this.mModemAdapter.cardPowerOn(this, this.mRequest, 2, 11);
        }
    }

    protected void onCardPowerOnDone(Message msg) {
        logd("onCardPowerOnDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 7);
        AsyncResult ar = msg.obj;
        if (isAsyncResultValidForCardPowerOn(ar)) {
            int subId = ar.userObj.mSubId;
            logd("onCardPowerOnDone : subId  = " + subId);
            if (subId == 2) {
                this.mModemAdapter.setActiveModemMode(this, this.mRequest, 2);
            }
        }
    }

    protected void afterJudgeRestartRildNvMatch() {
        logd("afterJudgeRestartRildNvMatch");
        this.mIsWaitingRestartRild = false;
        this.mHasReceiveNvMatchUnsol = false;
        writeVsim(this.mMessage);
    }

    protected void onSetActiveModemModeDone(Message msg) {
        logd("onSetActiveModemModeDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 8);
        AsyncResult ar = msg.obj;
        if (isAsyncResultValid(ar)) {
            setPrefNetworkForM0(ar, ar.userObj);
        }
    }

    protected void onSetPreferredNetworkTypeDone(Message msg) {
        logd("onSetPreferredNetworkTypeDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 10);
        AsyncResult ar = msg.obj;
        HwVSimRequest request = ar.userObj;
        if (isAsyncResultValid(ar)) {
            int flag = this.mVSimController.getPreferredNetworkTypeEnableFlag();
            logd("onSetPreferredNetworkTypeDone, flag = " + flag);
            if (!HwVSimModemAdapter.IS_FAST_SWITCH_SIMSLOT) {
                radioPowerOnForVSim();
            } else if (flag == 0) {
                this.mModemAdapter.setPreferredNetworkType(this, request, request.getMainSlot() == 0 ? 1 : 0, getNetworkTypeOnModem1(0));
                this.mVSimController.setPreferredNetworkTypeEnableFlag(1);
            } else if (1 == flag) {
                this.mModemAdapter.setPreferredNetworkType(this, request, request.getMainSlot(), getNetworkTypeOnModem2());
                this.mVSimController.setPreferredNetworkTypeEnableFlag(2);
            } else {
                radioPowerOnForVSim();
            }
            return;
        }
        HwVSimModemAdapter hwVSimModemAdapter = this.mModemAdapter;
        if (HwVSimModemAdapter.IS_FAST_SWITCH_SIMSLOT && request.getIsNeedSwitchCommrilMode()) {
            this.mVSimController.setPreferredNetworkTypeEnableFlag(0);
        }
    }

    private void radioPowerOnForVSim() {
        logd("radioPowerOnForVSim : subId  = " + 2);
        if (HwVSimUtilsInner.isPlatformRealTripple()) {
            this.mVSimController.allowData(2);
        }
        this.mModemAdapter.radioPowerOn(this, this.mRequest, 2);
        this.mVSimController.setPreferredNetworkTypeEnableFlag(0);
    }

    protected void onRadioPowerOnDone(Message msg) {
        logd("onRadioPowerOnDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 11);
        if (isAsyncResultValid(msg.obj)) {
            enableVSimDone();
        }
    }

    protected void onEnableVSimDone(Message msg) {
        logd("onEnableVSimDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 12);
        notifyResult(this.mRequest, Integer.valueOf(0));
        if (this.mVSimController.isDirectProcess()) {
            this.mModemAdapter.onEDWorkTransitionState(this);
        } else {
            transitionToState(4);
        }
    }

    protected void enableVSimDone() {
        logd("enableVSimDone");
        Message onCompleted = obtainMessage(51, this.mRequest);
        AsyncResult.forMessage(onCompleted);
        onCompleted.sendToTarget();
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
        doEnableProcessException(ar, request, Integer.valueOf(2));
        return false;
    }

    protected void afterGetAllCardStateDone() {
        logd("afterGetAllCardStateDone: onGetIccCardStatusDone->switchSimSlot");
        this.mModemAdapter.switchSimSlot(this, this.mRequest);
    }
}
