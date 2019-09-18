package com.android.internal.telephony.vsim.process;

import android.os.AsyncResult;
import android.os.Message;
import android.os.SystemProperties;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.android.internal.telephony.vsim.HwVSimController;
import com.android.internal.telephony.vsim.HwVSimEventReport;
import com.android.internal.telephony.vsim.HwVSimLog;
import com.android.internal.telephony.vsim.HwVSimModemAdapter;
import com.android.internal.telephony.vsim.HwVSimRequest;
import com.android.internal.telephony.vsim.HwVSimSlotSwitchController;
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

    public void onEnter() {
        super.onEnter();
        if (!this.mVSimController.isDirectProcess() && HwVSimUtilsInner.isPlatformNeedWaitNvMatchUnsol()) {
            this.mVSimController.setIsWaitingNvMatchUnsol(true);
        }
    }

    public boolean processMessage(Message msg) {
        boolean retVal = true;
        if (!isMessageShouldDeal(msg)) {
            return false;
        }
        int i = msg.what;
        if (i == 2) {
            onGetSimStateDone(msg);
        } else if (i == 49) {
            onSetPreferredNetworkTypeDone(msg);
        } else if (i != 51) {
            switch (i) {
                case 41:
                    onRadioPowerOffDone(msg);
                    break;
                case 42:
                    onCardPowerOffDone(msg);
                    break;
                case 43:
                    onSwitchSlotDone(msg);
                    break;
                default:
                    switch (i) {
                        case HwVSimConstants.EVENT_CARD_POWER_ON_DONE:
                            onCardPowerOnDone(msg);
                            break;
                        case HwVSimConstants.EVENT_RADIO_POWER_ON_DONE:
                            onRadioPowerOnDone(msg);
                            break;
                        case HwVSimConstants.EVENT_SET_ACTIVE_MODEM_MODE_DONE:
                            onSetActiveModemModeDone(msg);
                            break;
                        default:
                            switch (i) {
                                case HwVSimConstants.EVENT_GET_ICC_STATUS_DONE:
                                    onGetIccCardStatusDone(msg);
                                    break;
                                case HwVSimConstants.EVENT_SET_CDMA_MODE_SIDE_DONE:
                                    onSetCdmaModeSideDone(msg);
                                    break;
                                case HwVSimConstants.EVENT_JUDGE_RESTART_RILD_NV_MATCH:
                                    onJudgeRestartRildNvMatch(msg);
                                    break;
                                case HwVSimConstants.EVENT_JUDGE_RESTART_RILD_NV_MATCH_TIMEOUT:
                                    onJudgeRestartRildNvMatchTimeout();
                                    break;
                                case HwVSimConstants.EVENT_RADIO_AVAILABLE:
                                    onRadioAvailable(msg);
                                    break;
                                default:
                                    retVal = false;
                                    break;
                            }
                    }
            }
        } else {
            onEnableVSimDone(msg);
        }
        return retVal;
    }

    public void doProcessException(AsyncResult ar, HwVSimRequest request) {
        doEnableProcessException(ar, request, 3);
    }

    /* access modifiers changed from: protected */
    public void logd(String s) {
        HwVSimLog.VSimLogD(LOG_TAG, s);
    }

    /* access modifiers changed from: protected */
    public void onSwitchSlotDone(Message msg) {
        logd("onSwitchSlotDone");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 5);
        AsyncResult ar = (AsyncResult) msg.obj;
        if (isAsyncResultValid(ar)) {
            this.mModemAdapter.onSwitchSlotDone(this, ar);
            this.mVSimController.clearAllMarkForCardReload();
            this.mVSimController.setBlockPinFlag(false);
            HwVSimRequest request = (HwVSimRequest) ar.userObj;
            if (!HwVSimModemAdapter.IS_FAST_SWITCH_SIMSLOT || !request.getIsNeedSwitchCommrilMode()) {
                cardPowerOnModem1orWriteVSim(msg);
            } else {
                setCdmaModeSide(msg);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onSetCdmaModeSideDone(Message msg) {
        logd("onSetCdmaModeSideDone");
        AsyncResult ar = (AsyncResult) msg.obj;
        if (isAsyncResultValid(ar)) {
            HwVSimController.getInstance().setIsWaitingSwitchCdmaModeSide(false);
            HwVSimSlotSwitchController.CommrilMode expectCommrilMode = ((HwVSimRequest) ar.userObj).getExpectCommrilMode();
            logd("onSetCdmaModeSideDone, expectCommrilMode = " + expectCommrilMode);
            SystemProperties.set("persist.radio.commril_mode", expectCommrilMode.toString());
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
        HwVSimRequest request = (HwVSimRequest) ((AsyncResult) msg.obj).userObj;
        int subId = request.getMainSlot();
        request.setSource(2);
        this.mModemAdapter.cardPowerOn(this, request, subId, 1);
        int subId2 = request.getExpectSlot();
        this.mModemAdapter.cardPowerOn(this, request.clone(), subId2, 1);
        startListenForRildNvMatch();
    }

    private void writeVsim(Message msg) {
        AsyncResult ar = (AsyncResult) msg.obj;
        HwVSimRequest request = (HwVSimRequest) ar.userObj;
        int i = request.mSubId;
        if (isSwapProcess()) {
            int mainSlot = request.getMainSlot();
            logd("mainSlot = " + mainSlot);
        } else if (isCrossProcess() != 0) {
            request.setMainSlot(request.getExpectSlot());
        }
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

    /* access modifiers changed from: protected */
    public void onCardPowerOffDone(Message msg) {
        logd("onCardPowerOffDone");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 4);
        AsyncResult ar = (AsyncResult) msg.obj;
        if (isAsyncResultValidForRequestNotSupport(ar)) {
            HwVSimRequest request = (HwVSimRequest) ar.userObj;
            int subId = request.mSubId;
            logd("onCardPowerOffDone, subId: " + subId);
            int subCount = request.getSubCount();
            for (int i = 0; i < subCount; i++) {
                if (subId == request.getSubIdByIndex(i)) {
                    request.setCardOnOffMark(i, false);
                }
            }
            this.mModemAdapter.onCardPowerOffDoneInEWork(this, subId);
            getIccCardStatus(request, subId);
        }
    }

    /* access modifiers changed from: protected */
    public void onCardPowerOnDone(Message msg) {
        logd("onCardPowerOnDone");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 7);
        AsyncResult ar = (AsyncResult) msg.obj;
        if (isAsyncResultValidForCardPowerOn(ar)) {
            int subId = ((HwVSimRequest) ar.userObj).mSubId;
            logd("onCardPowerOnDone : subId  = " + subId);
            if (subId == 2) {
                this.mModemAdapter.setActiveModemMode(this, this.mRequest, 2);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void afterJudgeRestartRildNvMatch() {
        logd("afterJudgeRestartRildNvMatch");
        this.mIsWaitingRestartRild = false;
        this.mHasReceiveNvMatchUnsol = false;
        this.mVSimController.setIsWaitingNvMatchUnsol(false);
        writeVsim(this.mMessage);
    }

    /* access modifiers changed from: protected */
    public void onSetActiveModemModeDone(Message msg) {
        logd("onSetActiveModemModeDone");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 8);
        AsyncResult ar = (AsyncResult) msg.obj;
        if (isAsyncResultValid(ar)) {
            setPrefNetworkForM0(ar, (HwVSimRequest) ar.userObj);
        }
    }

    /* access modifiers changed from: protected */
    public void onSetPreferredNetworkTypeDone(Message msg) {
        logd("onSetPreferredNetworkTypeDone");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 10);
        AsyncResult ar = (AsyncResult) msg.obj;
        HwVSimRequest request = (HwVSimRequest) ar.userObj;
        int slotIdInModem1 = 0;
        if (!isAsyncResultValid(ar)) {
            if (HwVSimModemAdapter.IS_FAST_SWITCH_SIMSLOT && request.getIsNeedSwitchCommrilMode()) {
                this.mVSimController.setPreferredNetworkTypeEnableFlag(0);
            }
            return;
        }
        int flag = this.mVSimController.getPreferredNetworkTypeEnableFlag();
        logd("onSetPreferredNetworkTypeDone, flag = " + flag);
        int slotIdInModem2 = request.getMainSlot();
        if (slotIdInModem2 == 0) {
            slotIdInModem1 = 1;
        }
        if (!HwVSimModemAdapter.IS_FAST_SWITCH_SIMSLOT) {
            radioPowerOnForVSim();
        } else if (flag == 0) {
            this.mModemAdapter.setPreferredNetworkType(this, request, slotIdInModem1, HwVSimUtilsInner.getNetworkTypeInModem1ForCmcc(getNetworkTypeOnModem1ForEWork()));
            this.mVSimController.setPreferredNetworkTypeEnableFlag(1);
        } else if (!HwVSimUtilsInner.isPlatformRealTripple() || 1 != flag) {
            radioPowerOnForVSim();
            if (HwVSimUtilsInner.isPlatformRealTripple() && HwVSimUtilsInner.isDualImsSupported()) {
                this.mModemAdapter.saveNetworkTypeToDB(slotIdInModem2, getNetworkTypeOnModem2());
            }
        } else {
            this.mModemAdapter.setPreferredNetworkType(this, request, slotIdInModem2, getNetworkTypeOnModem2());
            this.mVSimController.setPreferredNetworkTypeEnableFlag(2);
            if (HwVSimUtilsInner.isDualImsSupported()) {
                this.mModemAdapter.saveNetworkTypeToDB(slotIdInModem1, HwVSimUtilsInner.getNetworkTypeInModem1ForCmcc(getNetworkTypeOnModem1ForEWork()));
            }
        }
    }

    private void radioPowerOnForVSim() {
        logd("radioPowerOnForVSim : subId  = " + 2);
        this.mVSimController.allowData(2);
        this.mModemAdapter.radioPowerOn(this, this.mRequest, 2);
        this.mVSimController.setPreferredNetworkTypeEnableFlag(0);
    }

    /* access modifiers changed from: protected */
    public void onRadioPowerOnDone(Message msg) {
        logd("onRadioPowerOnDone");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 11);
        if (isAsyncResultValid((AsyncResult) msg.obj)) {
            enableVSimDone();
        }
    }

    /* access modifiers changed from: protected */
    public void onEnableVSimDone(Message msg) {
        logd("onEnableVSimDone");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 12);
        notifyResult(this.mRequest, 0);
        if (this.mVSimController.isDirectProcess()) {
            this.mModemAdapter.onEDWorkTransitionState(this);
        } else {
            transitionToState(4);
        }
    }

    /* access modifiers changed from: protected */
    public void enableVSimDone() {
        logd("enableVSimDone");
        Message onCompleted = obtainMessage(51, this.mRequest);
        AsyncResult.forMessage(onCompleted);
        onCompleted.sendToTarget();
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
        doEnableProcessException(ar, request, 2);
        return false;
    }

    /* access modifiers changed from: protected */
    public void afterGetAllCardStateDone() {
        logd("afterGetAllCardStateDone: onGetIccCardStatusDone->switchSimSlot");
        this.mModemAdapter.switchSimSlot(this, this.mRequest);
    }

    private boolean isMessageShouldDeal(Message msg) {
        return super.isMessageShouldDeal(msg, 2);
    }

    private int getNetworkTypeOnModem1ForEWork() {
        return this.mModemAdapter.getAllAbilityNetworkTypeOnModem1(true);
    }

    private int getNetworkTypeOnModem2() {
        return 1;
    }
}
