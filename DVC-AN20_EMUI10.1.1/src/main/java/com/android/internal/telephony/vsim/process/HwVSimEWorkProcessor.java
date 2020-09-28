package com.android.internal.telephony.vsim.process;

import android.os.Message;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.android.internal.telephony.vsim.HwVSimController;
import com.android.internal.telephony.vsim.HwVSimEventReport;
import com.android.internal.telephony.vsim.HwVSimLog;
import com.android.internal.telephony.vsim.HwVSimModemAdapter;
import com.android.internal.telephony.vsim.HwVSimRequest;
import com.android.internal.telephony.vsim.HwVSimSlotSwitchController;
import com.android.internal.telephony.vsim.HwVSimUtilsInner;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.android.os.SystemPropertiesEx;

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

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor, com.android.internal.telephony.vsim.process.HwVSimWorkProcessor
    public void onEnter() {
        super.onEnter();
        if (!this.mVSimController.isDirectProcess() && HwVSimUtilsInner.isPlatformNeedWaitNvMatchUnsol()) {
            this.mVSimController.setIsWaitingNvMatchUnsol(true);
        }
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public boolean processMessage(Message msg) {
        boolean retVal = true;
        if (!isMessageShouldDeal(msg)) {
            return false;
        }
        switch (msg.what) {
            case 2:
                onGetSimStateDone(msg);
                break;
            case HwVSimConstants.EVENT_RADIO_POWER_OFF_DONE:
                onRadioPowerOffDone(msg);
                break;
            case HwVSimConstants.EVENT_CARD_POWER_OFF_DONE:
                onCardPowerOffDone(msg);
                break;
            case HwVSimConstants.EVENT_SWITCH_SLOT_DONE:
                onSwitchSlotDone(msg);
                break;
            case HwVSimConstants.EVENT_CARD_POWER_ON_DONE:
                onCardPowerOnDone(msg);
                break;
            case HwVSimConstants.EVENT_RADIO_POWER_ON_DONE:
                onRadioPowerOnDone(msg);
                break;
            case HwVSimConstants.EVENT_SET_ACTIVE_MODEM_MODE_DONE:
                onSetActiveModemModeDone(msg);
                break;
            case HwVSimConstants.EVENT_SET_PREFERRED_NETWORK_TYPE_DONE:
                onSetPreferredNetworkTypeDone(msg);
                break;
            case 51:
                onEnableVSimDone(msg);
                break;
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
            case HwVSimConstants.EVENT_SEND_OPEN_SESSION_CONFIG_DONE:
                onSendOpenSessionConfigDone(msg);
                break;
            case HwVSimConstants.EVENT_SEND_OPEN_SESSION_CONFIG_TIMEOUT:
                onSendOpenSessionConfigTimeout(msg);
                break;
            case HwVSimConstants.EVENT_SEND_VSIM_DATA_TO_MODEM_DONE:
                onSendVsimDataToModemDone(msg);
                break;
            case HwVSimConstants.EVENT_SEND_VSIM_DATA_TO_MODEM_TIMEOUT:
                onSendVsimDataToModemTimeout(msg);
                break;
            default:
                retVal = false;
                break;
        }
        return retVal;
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public void doProcessException(AsyncResultEx ar, HwVSimRequest request) {
        doEnableProcessException(ar, request, 3);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor, com.android.internal.telephony.vsim.process.HwVSimWorkProcessor
    public void logd(String s) {
        HwVSimLog.VSimLogD(LOG_TAG, s);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.vsim.process.HwVSimWorkProcessor
    public void onSwitchSlotDone(Message msg) {
        logd("onSwitchSlotDone");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 5);
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (isAsyncResultValid(ar)) {
            this.mModemAdapter.onSwitchSlotDone(this, ar);
            this.mVSimController.clearAllMarkForCardReload();
            this.mVSimController.setBlockPinFlag(false);
            HwVSimRequest request = (HwVSimRequest) ar.getUserObj();
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
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (isAsyncResultValid(ar)) {
            HwVSimController.getInstance().setIsWaitingSwitchCdmaModeSide(false);
            HwVSimSlotSwitchController.CommrilMode expectCommrilMode = ((HwVSimRequest) ar.getUserObj()).getExpectCommrilMode();
            logd("onSetCdmaModeSideDone, expectCommrilMode = " + expectCommrilMode);
            SystemPropertiesEx.set(HwVSimModemAdapter.PROPERTY_COMMRIL_MODE, expectCommrilMode.toString());
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
        HwVSimRequest request = (HwVSimRequest) AsyncResultEx.from(msg.obj).getUserObj();
        int subId = request.getMainSlot();
        request.setSource(2);
        this.mModemAdapter.cardPowerOn(this, request, subId, 1);
        int subId2 = request.getExpectSlot();
        this.mModemAdapter.cardPowerOn(this, request.clone(), subId2, 1);
        startListenForRildNvMatch();
    }

    private void writeVsim(Message msg) {
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        HwVSimRequest request = (HwVSimRequest) ar.getUserObj();
        int i = request.mSubId;
        if (isSwapProcess()) {
            logd("mainSlot = " + request.getMainSlot());
        } else if (isCrossProcess()) {
            request.setMainSlot(request.getExpectSlot());
        }
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

    /* access modifiers changed from: package-private */
    public void writeVSimOpenSession(AsyncResultEx ar, HwVSimRequest request, HwVSimController.EnableParam param) {
        if (param.supportVsimCa == 1) {
            this.mModemAdapter.openChipSession(this, request, 2);
            this.mHandler.sendEmptyMessageDelayed(89, 5000);
            return;
        }
        writeVsim(ar, request, param);
    }

    private void onSendOpenSessionConfigDone(Message msg) {
        logd("onSendOpenSessionConfigDone");
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (ar == null) {
            logd("onSendOpenSessionConfigDone : ar null");
            return;
        }
        this.mHandler.removeMessages(89);
        HwVSimRequest request = (HwVSimRequest) ar.getUserObj();
        if (ar.getException() != null) {
            if (isRequestNotSupport(ar.getException())) {
                logd("request not support, just skip");
            } else {
                logd("open fail, try to close session");
                closeChipSessionWhenOpenFailOrTimeout(ar);
                return;
            }
        }
        writeVsim(ar, request, (HwVSimController.EnableParam) request.getArgument());
    }

    private void onSendOpenSessionConfigTimeout(Message msg) {
        logd("onSendOpenSessionConfigTimeout");
        closeChipSessionWhenOpenFailOrTimeout(AsyncResultEx.from(msg.obj));
    }

    private void writeVsim(AsyncResultEx ar, HwVSimRequest request, HwVSimController.EnableParam param) {
        int result;
        if (param.supportVsimCa == 1) {
            result = this.mVSimController.writeVsimToTA(51, param);
            this.mVSimController.setIsTaOpen(true);
        } else {
            result = this.mVSimController.writeVsimToTA(1, param);
        }
        if (result != 0) {
            doEnableProcessException(ar, request, Integer.valueOf(result));
            this.mVSimController.closeTaSafely(request);
        } else if (param.supportVsimCa == 1) {
            this.mHandler.sendEmptyMessageDelayed(92, 5000);
            this.mModemAdapter.sendVsimDataToModem(this, request, 2);
        } else {
            cardPowerOnVsim(request);
        }
    }

    private void onSendVsimDataToModemDone(Message msg) {
        logd("onSendVsimDataToModemDone");
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (ar == null) {
            logd("onSendVsimDataToModemDone, ar null");
            return;
        }
        this.mHandler.removeMessages(92);
        HwVSimRequest request = (HwVSimRequest) ar.getUserObj();
        if (ar.getException() != null) {
            if (isRequestNotSupport(ar.getException())) {
                logd("request not support, just skip");
            } else {
                logd("send fail, try to close session");
                closeChipSessionWhenOpenFailOrTimeout(ar);
                return;
            }
        }
        int result = this.mVSimController.writeVsimToTA(4, (HwVSimController.EnableParam) request.getArgument());
        if (result != 0) {
            doEnableProcessException(ar, request, Integer.valueOf(result));
            this.mVSimController.closeTaSafely(request);
            return;
        }
        closeChipSessionWhenSuccess();
        cardPowerOnVsim(request);
    }

    private void onSendVsimDataToModemTimeout(Message msg) {
        logd("onSendVsimDataToModemTimeout");
        closeChipSessionWhenOpenFailOrTimeout(AsyncResultEx.from(msg.obj));
    }

    private void cardPowerOnVsim(HwVSimRequest request) {
        this.mVSimController.setVSimCurCardType(this.mVSimController.getCardTypeFromEnableParam(request));
        this.mModemAdapter.cardPowerOn(this, this.mRequest, 2, 11);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.vsim.process.HwVSimWorkProcessor
    public void onCardPowerOffDone(Message msg) {
        logd("onCardPowerOffDone");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 4);
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (isAsyncResultValidForRequestNotSupport(ar)) {
            HwVSimRequest request = (HwVSimRequest) ar.getUserObj();
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
    @Override // com.android.internal.telephony.vsim.process.HwVSimWorkProcessor
    public void onCardPowerOnDone(Message msg) {
        logd("onCardPowerOnDone");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 7);
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (isAsyncResultValidForCardPowerOn(ar)) {
            int subId = ((HwVSimRequest) ar.getUserObj()).mSubId;
            logd("onCardPowerOnDone : subId  = " + subId);
            if (subId == 2) {
                if (ar.getException() == null) {
                    this.mVSimController.setVSimOnSuccess(true);
                }
                this.mModemAdapter.setActiveModemMode(this, this.mRequest, 2);
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.vsim.process.HwVSimWorkProcessor
    public void afterJudgeRestartRildNvMatch() {
        logd("afterJudgeRestartRildNvMatch");
        this.mIsWaitingRestartRild = false;
        this.mHasReceiveNvMatchUnsol = false;
        this.mVSimController.setIsWaitingNvMatchUnsol(false);
        writeVsim(this.mMessage);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.vsim.process.HwVSimWorkProcessor
    public void onSetActiveModemModeDone(Message msg) {
        logd("onSetActiveModemModeDone");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 8);
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (isAsyncResultValid(ar)) {
            setPrefNetworkForM0(ar, (HwVSimRequest) ar.getUserObj());
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.vsim.process.HwVSimWorkProcessor
    public void onSetPreferredNetworkTypeDone(Message msg) {
        int slotIdInModem1 = 0;
        logd("onSetPreferredNetworkTypeDone");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 10);
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        HwVSimRequest request = (HwVSimRequest) ar.getUserObj();
        if (isAsyncResultValid(ar)) {
            int flag = this.mVSimController.getPreferredNetworkTypeEnableFlag();
            logd("onSetPreferredNetworkTypeDone, flag = " + flag);
            int slotIdInModem2 = request.getMainSlot();
            if (slotIdInModem2 == 0) {
                slotIdInModem1 = 1;
            }
            if (!HwVSimModemAdapter.IS_FAST_SWITCH_SIMSLOT) {
                radioPowerOnForVSim();
            } else if (flag == 0) {
                this.mModemAdapter.setPreferredNetworkType(this, request, slotIdInModem1, getNetworkTypeOnModem1ForEWork());
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
                    int networkTypeInModem1 = getNetworkTypeOnModem1ForEWork();
                    if (networkTypeInModem1 == 65) {
                        networkTypeInModem1 = 9;
                    }
                    this.mModemAdapter.saveNetworkTypeToDB(slotIdInModem1, networkTypeInModem1);
                }
            }
        } else if (HwVSimModemAdapter.IS_FAST_SWITCH_SIMSLOT && request.getIsNeedSwitchCommrilMode()) {
            this.mVSimController.setPreferredNetworkTypeEnableFlag(0);
        }
    }

    private void radioPowerOnForVSim() {
        logd("radioPowerOnForVSim : subId  = " + 2);
        this.mVSimController.allowData(2);
        this.mModemAdapter.radioPowerOn(this, this.mRequest, 2);
        this.mVSimController.setPreferredNetworkTypeEnableFlag(0);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.vsim.process.HwVSimWorkProcessor
    public void onRadioPowerOnDone(Message msg) {
        logd("onRadioPowerOnDone");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 11);
        if (isAsyncResultValid(AsyncResultEx.from(msg.obj))) {
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
        AsyncResultEx.forMessage(onCompleted);
        onCompleted.sendToTarget();
    }

    /* access modifiers changed from: protected */
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
        doEnableProcessException(ar, request, 2);
        return false;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.vsim.process.HwVSimWorkProcessor
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

    /* access modifiers changed from: package-private */
    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public void closeChipSessionWhenOpenFailOrTimeout(AsyncResultEx ar) {
        super.closeChipSessionWhenOpenFailOrTimeout(ar);
        this.mVSimController.closeTaSafely(this.mRequest);
    }

    private void closeChipSessionWhenSuccess() {
        this.mModemAdapter.closeChipSession(2);
        this.mVSimController.closeTaSafely(this.mRequest);
    }
}
