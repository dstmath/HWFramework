package com.android.internal.telephony.vsim.process;

import android.os.Message;
import com.android.internal.telephony.HwVSimPhoneFactory;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.android.internal.telephony.vsim.HwVSimController;
import com.android.internal.telephony.vsim.HwVSimEventReport;
import com.android.internal.telephony.vsim.HwVSimLog;
import com.android.internal.telephony.vsim.HwVSimModemAdapter;
import com.android.internal.telephony.vsim.HwVSimNvMatchController;
import com.android.internal.telephony.vsim.HwVSimRequest;
import com.android.internal.telephony.vsim.HwVSimUtilsInner;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.android.os.SystemPropertiesEx;

public class HwVSimSWorkProcessor extends HwVSimEWorkProcessor {
    public static final String LOG_TAG = "HwVSimSWorkProcessor";

    public HwVSimSWorkProcessor(HwVSimController controller, HwVSimModemAdapter modemAdapter, HwVSimRequest request) {
        super(controller, modemAdapter, request);
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimEWorkProcessor, com.android.internal.telephony.vsim.process.HwVSimWorkProcessor, com.android.internal.telephony.vsim.process.HwVSimProcessor
    public void onEnter() {
        super.onEnter();
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimWorkProcessor, com.android.internal.telephony.vsim.process.HwVSimProcessor
    public void onExit() {
        super.onExit();
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimWorkProcessor, com.android.internal.telephony.vsim.process.HwVSimProcessor
    public Message obtainMessage(int what, Object obj) {
        return this.mVSimController.obtainMessage(what, obj);
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimWorkProcessor, com.android.internal.telephony.vsim.process.HwVSimProcessor
    public void transitionToState(int state) {
        this.mVSimController.transitionToState(state);
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimEWorkProcessor, com.android.internal.telephony.vsim.process.HwVSimProcessor
    public void doProcessException(AsyncResultEx ar, HwVSimRequest request) {
        doSwitchModeProcessException(ar, request);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.vsim.process.HwVSimEWorkProcessor, com.android.internal.telephony.vsim.process.HwVSimWorkProcessor, com.android.internal.telephony.vsim.process.HwVSimProcessor
    public void logd(String s) {
        HwVSimLog.VSimLogD(LOG_TAG, s);
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimEWorkProcessor, com.android.internal.telephony.vsim.process.HwVSimProcessor
    public boolean processMessage(Message msg) {
        int i = msg.what;
        if (i == 2) {
            onGetSimStateDone(msg);
            return true;
        } else if (i != 59) {
            switch (i) {
                case HwVSimConstants.EVENT_RADIO_POWER_OFF_DONE /* 41 */:
                    onRadioPowerOffDone(msg);
                    return true;
                case HwVSimConstants.EVENT_CARD_POWER_OFF_DONE /* 42 */:
                    onCardPowerOffDone(msg);
                    return true;
                case HwVSimConstants.EVENT_SWITCH_SLOT_DONE /* 43 */:
                    onSwitchSlotDone(msg);
                    return true;
                default:
                    switch (i) {
                        case HwVSimConstants.EVENT_CARD_POWER_ON_DONE /* 45 */:
                            onCardPowerOnDone(msg);
                            return true;
                        case HwVSimConstants.EVENT_RADIO_POWER_ON_DONE /* 46 */:
                            onRadioPowerOnDone(msg);
                            return true;
                        case HwVSimConstants.EVENT_SET_ACTIVE_MODEM_MODE_DONE /* 47 */:
                            onSetActiveModemModeDone(msg);
                            return true;
                        case HwVSimConstants.EVENT_GET_PREFERRED_NETWORK_TYPE_DONE /* 48 */:
                            onGetPreferredNetworkTypeDone(msg);
                            return true;
                        case HwVSimConstants.EVENT_SET_PREFERRED_NETWORK_TYPE_DONE /* 49 */:
                            onSetPreferredNetworkTypeDone(msg);
                            return true;
                        default:
                            switch (i) {
                                case HwVSimConstants.EVENT_GET_ICC_STATUS_DONE /* 79 */:
                                    onGetIccCardStatusDone(msg);
                                    return true;
                                case HwVSimConstants.EVENT_SET_CDMA_MODE_SIDE_DONE /* 80 */:
                                    onSetCdmaModeSideDone(msg);
                                    return true;
                                case HwVSimConstants.EVENT_JUDGE_RESTART_RILD_NV_MATCH /* 81 */:
                                    onJudgeRestartRildNvMatch(msg);
                                    return true;
                                case HwVSimConstants.EVENT_JUDGE_RESTART_RILD_NV_MATCH_TIMEOUT /* 82 */:
                                    onJudgeRestartRildNvMatchTimeout();
                                    return true;
                                case HwVSimConstants.EVENT_RADIO_AVAILABLE /* 83 */:
                                    onRadioAvailable(msg);
                                    return true;
                                default:
                                    return false;
                            }
                    }
            }
        } else {
            onSwitchWorkModeDone();
            return true;
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

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.vsim.process.HwVSimEWorkProcessor, com.android.internal.telephony.vsim.process.HwVSimWorkProcessor
    public void afterGetAllCardStateDone() {
        logd("afterGetAllCardStateDone: onGetIccCardStatusDone->switchSimSlot");
        this.mModemAdapter.switchSimSlot(this, this.mRequest);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.vsim.process.HwVSimEWorkProcessor, com.android.internal.telephony.vsim.process.HwVSimWorkProcessor
    public void onSwitchSlotDone(Message msg) {
        logd("onSwitchSlotDone");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 5);
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (isAsyncResultValid(ar)) {
            this.mModemAdapter.onSwitchSlotDone(this, ar);
            HwVSimRequest request = (HwVSimRequest) ar.getUserObj();
            if (isCrossProcess() && this.mVSimController.getInsertedCardCount() != 0) {
                int mainSlot = request.getMainSlot();
                logd("onSwitchSlotDone, cross and insert at least one card ,update switch dual card slot -> " + mainSlot);
                HwVSimPhoneFactory.setUserSwitchDualCardSlots(mainSlot);
            }
            if (!HwVSimModemAdapter.IS_FAST_SWITCH_SIMSLOT || !request.getIsNeedSwitchCommrilMode()) {
                cardPowerOn(msg);
            } else {
                setCdmaModeSide(msg);
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.vsim.process.HwVSimEWorkProcessor
    public void onSetCdmaModeSideDone(Message msg) {
        logd("onSetCdmaModeSideDone");
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (isAsyncResultValid(ar)) {
            HwVSimController.getInstance().setIsWaitingSwitchCdmaModeSide(false);
            SystemPropertiesEx.set(HwVSimModemAdapter.PROPERTY_COMMRIL_MODE, ((HwVSimRequest) ar.getUserObj()).getExpectCommrilMode().toString());
            cardPowerOn(msg);
        }
    }

    private void cardPowerOn(Message msg) {
        HwVSimRequest request = (HwVSimRequest) AsyncResultEx.from(msg.obj).getUserObj();
        int subId = request.mSubId;
        logd("cardPoweron, subId = " + subId);
        if (isSwapProcess()) {
            int mainSlot = request.getMainSlot();
            logd("mainSlot = " + mainSlot);
        } else if (isCrossProcess()) {
            request.setMainSlot(request.getExpectSlot());
        }
        this.mModemAdapter.cardPowerOn(this, this.mRequest, 2, 11);
        int subId2 = 1;
        if (HwVSimUtilsInner.isPlatformNeedWaitNvMatchUnsol() && HwVSimNvMatchController.getInstance().getIfNeedRestartRildForNvMatch()) {
            this.mIsWaitingRestartRild = true;
            restartRild();
        } else if (isNeedWaitNvCfgMatchAndRestartRild()) {
            startListenForRildNvMatch();
            if (this.mRequest.getMainSlot() != 0) {
                subId2 = 0;
            }
            this.mModemAdapter.cardPowerOn(this, this.mRequest.clone(), subId2, 1);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.vsim.process.HwVSimEWorkProcessor, com.android.internal.telephony.vsim.process.HwVSimWorkProcessor
    public void afterJudgeRestartRildNvMatch() {
        logd("afterJudgeRestartRildNvMatch");
        this.mIsWaitingRestartRild = false;
        this.mHasReceiveNvMatchUnsol = false;
        this.mVSimController.setIsWaitingNvMatchUnsol(false);
        this.mModemAdapter.setActiveModemMode(this, this.mRequest, 2);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.vsim.process.HwVSimEWorkProcessor, com.android.internal.telephony.vsim.process.HwVSimWorkProcessor
    public void onRadioPowerOnDone(Message msg) {
        logd("onRadioPowerOnDone");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 11);
        if (isAsyncResultValid(AsyncResultEx.from(msg.obj))) {
            switchWorkModeDone();
        }
    }

    private void onSwitchWorkModeDone() {
        logd("onSwitchWorkModeDone");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 1);
        notifyResult(this.mRequest, true);
        transitionToState(13);
    }

    private void switchWorkModeDone() {
        logd("switchWorkModeDone");
        Message onCompleted = obtainMessage(59, this.mRequest);
        AsyncResultEx.forMessage(onCompleted);
        onCompleted.sendToTarget();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.vsim.process.HwVSimEWorkProcessor, com.android.internal.telephony.vsim.process.HwVSimWorkProcessor
    public void onCardPowerOnDone(Message msg) {
        logd("onCardPowerOnDone");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 7);
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (isAsyncResultValidNoProcessException(ar)) {
            logd("onCardPowerOnDone : subId  = " + ((HwVSimRequest) ar.getUserObj()).mSubId);
            if (isNeedWaitNvCfgMatchAndRestartRild() || this.mIsWaitingRestartRild) {
                logd("if dual ims support and cross process, wait for nv cfg result unsol.");
            } else {
                this.mModemAdapter.setActiveModemMode(this, this.mRequest, 2);
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.vsim.process.HwVSimEWorkProcessor, com.android.internal.telephony.vsim.process.HwVSimWorkProcessor
    public void onSetActiveModemModeDone(Message msg) {
        logd("onSetActiveModemModeDone");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 8);
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (isAsyncResultValid(ar)) {
            this.mModemAdapter.getPreferredNetworkType(this, this.mRequest, ((HwVSimRequest) ar.getUserObj()).mSubId);
        }
    }

    private void onGetPreferredNetworkTypeDone(Message msg) {
        logd("onGetPreferredNetworkTypeDone");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 9);
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (isAsyncResultValid(ar)) {
            HwVSimRequest request = (HwVSimRequest) ar.getUserObj();
            int subId = request.mSubId;
            int networkMode = modifyNetworkMode(((int[]) ar.getResult())[0]);
            logd("set preferred network to " + networkMode);
            this.mModemAdapter.setPreferredNetworkType(this, request, subId, networkMode);
        }
    }
}
