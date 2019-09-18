package com.android.internal.telephony.vsim.process;

import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import com.android.internal.telephony.uicc.IccCardStatus;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.android.internal.telephony.vsim.HwVSimController;
import com.android.internal.telephony.vsim.HwVSimEventReport;
import com.android.internal.telephony.vsim.HwVSimModemAdapter;
import com.android.internal.telephony.vsim.HwVSimRequest;
import com.android.internal.telephony.vsim.HwVSimSlotSwitchController;
import com.android.internal.telephony.vsim.HwVSimUtilsInner;
import java.util.Arrays;

public abstract class HwVSimWorkProcessor extends HwVSimProcessor {
    static final int GET_ICC_CARD_STATUS_RETRY_TIMES = 5;
    static final String LOG_TAG = "VSimWorkProcessor";
    private int[] mGetIccCardStatusTimes;
    protected Handler mHandler;
    boolean mHasReceiveNvMatchUnsol = false;
    private boolean mInDSDSPreProcess;
    boolean mIsWaitingRestartRild;
    private boolean[] mRadioAvailableMark;
    protected HwVSimController mVSimController;

    /* access modifiers changed from: protected */
    public abstract void logd(String str);

    /* access modifiers changed from: protected */
    public abstract void onCardPowerOffDone(Message message);

    /* access modifiers changed from: protected */
    public abstract void onCardPowerOnDone(Message message);

    /* access modifiers changed from: protected */
    public abstract void onRadioPowerOnDone(Message message);

    /* access modifiers changed from: protected */
    public abstract void onSetActiveModemModeDone(Message message);

    /* access modifiers changed from: protected */
    public abstract void onSetPreferredNetworkTypeDone(Message message);

    /* access modifiers changed from: protected */
    public abstract void onSwitchSlotDone(Message message);

    HwVSimWorkProcessor(HwVSimController controller, HwVSimModemAdapter modemAdapter, HwVSimRequest request) {
        super(modemAdapter, request);
        this.mVSimController = controller;
        this.mInDSDSPreProcess = false;
        this.mIsWaitingRestartRild = false;
        if (controller != null) {
            this.mHandler = controller.getHandler();
        }
    }

    public void onEnter() {
        logd("onEnter");
        HwVSimRequest request = this.mRequest;
        if (request != null) {
            this.mModemAdapter.handleSubSwapProcess(this, request);
            if (this.mVSimController.isEnableProcess() && !this.mVSimController.isDirectProcess()) {
                this.mModemAdapter.setHwVSimPowerOn(this, request);
            }
            if (isSwapProcess()) {
                this.mInDSDSPreProcess = true;
                int slaveSlot = request.getMainSlot() == 0 ? 1 : 0;
                this.mVSimController.setProhibitSubUpdateSimNoChange(slaveSlot, true);
                this.mModemAdapter.radioPowerOff(this, request, slaveSlot);
            } else {
                if (HwVSimModemAdapter.IS_FAST_SWITCH_SIMSLOT && this.mRequest.getIsNeedSwitchCommrilMode()) {
                    this.mVSimController.setIsWaitingSwitchCdmaModeSide(true);
                }
                this.mModemAdapter.radioPowerOff(this, request);
            }
            setProcessState(HwVSimController.ProcessState.PROCESS_STATE_WORK);
        }
    }

    public void onExit() {
        logd("onExit");
    }

    public void transitionToState(int state) {
        this.mVSimController.transitionToState(state);
    }

    public Message obtainMessage(int what, Object obj) {
        return this.mVSimController.obtainMessage(what, obj);
    }

    /* access modifiers changed from: protected */
    public void onRadioPowerOffDone(Message msg) {
        logd("onRadioPowerOffDone");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 2);
        AsyncResult ar = (AsyncResult) msg.obj;
        if (isAsyncResultValidForRequestNotSupport(ar)) {
            if (this.mInDSDSPreProcess) {
                this.mInDSDSPreProcess = false;
                HwVSimRequest request = this.mRequest;
                if (request != null) {
                    this.mModemAdapter.radioPowerOff(this, request);
                    this.mModemAdapter.onRadioPowerOffSlaveModemDone(this, request);
                }
            } else {
                this.mModemAdapter.onRadioPowerOffDone(this, ar);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onGetSimStateDone(Message msg) {
        logd("onGetSimStateDone");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 3);
        AsyncResult ar = (AsyncResult) msg.obj;
        if (isAsyncResultValidForRequestNotSupport(ar)) {
            HwVSimModemAdapter.SimStateInfo ssInfo = this.mModemAdapter.onGetSimStateDone(this, ar);
            if (ssInfo != null) {
                logd("onGetSimStateDone ssInfo = " + ssInfo.toString());
            }
            HwVSimRequest request = (HwVSimRequest) ar.userObj;
            int subCount = request.getSubCount();
            int subId = request.mSubId;
            for (int i = 0; i < subCount; i++) {
                if (subId == request.getSubIdByIndex(i)) {
                    request.setSimStateMark(i, false);
                }
            }
            if (ssInfo != null) {
                this.mModemAdapter.cardPowerOff(this, request, subId, ssInfo.simIndex);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void getIccCardStatus(HwVSimRequest request, int subId) {
        logd("onCardPowerOffDone->getIccCardStatus,wait card status is absent");
        setIccCardStatusRetryTimes(subId, 0);
        this.mModemAdapter.getIccCardStatus(this, request, subId);
    }

    /* access modifiers changed from: protected */
    public void onGetIccCardStatusDone(Message msg) {
        AsyncResult ar = (AsyncResult) msg.obj;
        if (ar != null) {
            HwVSimRequest request = (HwVSimRequest) ar.userObj;
            if (request != null) {
                int subId = request.mSubId;
                IccCardStatus status = (IccCardStatus) ar.result;
                StringBuilder sb = new StringBuilder();
                sb.append("onGetIccCardStatusDone:mCardState[");
                sb.append(subId);
                sb.append("]=");
                sb.append(status != null ? status.mCardState : " state is null");
                logd(sb.toString());
                int retryTimes = getIccCardStatusRetryTimes(subId);
                if (!(ar.exception == null && (status == null || status.mCardState == IccCardStatus.CardState.CARDSTATE_ABSENT)) && retryTimes < 5) {
                    setIccCardStatusRetryTimes(subId, retryTimes + 1);
                    logd("onGetIccCardStatusDone: retry getIccCardStatus,Times=" + retryTimes);
                    this.mModemAdapter.getIccCardStatus(this, request, subId);
                } else {
                    int subCount = request.getSubCount();
                    for (int i = 0; i < subCount; i++) {
                        if (subId == request.getSubIdByIndex(i)) {
                            request.setGetIccCardStatusMark(i, false);
                        }
                    }
                    setIccCardStatusRetryTimes(subId, 0);
                    if (isAllMarkClear(request)) {
                        afterGetAllCardStateDone();
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void afterGetAllCardStateDone() {
        logd("afterGetAllCardStateDone - do nothing.");
    }

    /* access modifiers changed from: package-private */
    public void setIccCardStatusRetryTimes(int subId, int times) {
        if (this.mGetIccCardStatusTimes == null) {
            this.mGetIccCardStatusTimes = new int[HwVSimModemAdapter.MAX_SUB_COUNT];
        }
        if (subId >= 0 && subId < this.mGetIccCardStatusTimes.length) {
            this.mGetIccCardStatusTimes[subId] = times;
        }
    }

    /* access modifiers changed from: package-private */
    public int getIccCardStatusRetryTimes(int subId) {
        if (this.mGetIccCardStatusTimes == null || subId < 0 || subId >= this.mGetIccCardStatusTimes.length) {
            return 5;
        }
        return this.mGetIccCardStatusTimes[subId];
    }

    /* access modifiers changed from: package-private */
    public void setPrefNetworkForM0(AsyncResult ar, HwVSimRequest request) {
        HwVSimController.EnableParam param = getEnableParam(request);
        if (param == null) {
            doEnableProcessException(ar, request, 3);
            return;
        }
        int networkMode = acqorderToNetworkMode(param.acqorder);
        logd("set m0 preferred network to " + networkMode);
        this.mModemAdapter.setPreferredNetworkType(this, request, 2, networkMode);
    }

    /* access modifiers changed from: package-private */
    public int acqorderToNetworkMode(String acqorder) {
        return calcNetworkModeByAcqorder(acqorder);
    }

    /* access modifiers changed from: package-private */
    public void setCdmaModeSide(Message msg) {
        AsyncResult ar = (AsyncResult) msg.obj;
        if (isAsyncResultValid(ar)) {
            HwVSimRequest request = (HwVSimRequest) ar.userObj;
            HwVSimSlotSwitchController.CommrilMode expectCommrilMode = request.getExpectCommrilMode();
            int subOnM0 = request.getSlots()[0];
            if (HwVSimSlotSwitchController.CommrilMode.HISI_CG_MODE == expectCommrilMode) {
                this.mModemAdapter.setCdmaModeSide(this, this.mRequest, subOnM0, 1);
            } else if (HwVSimSlotSwitchController.CommrilMode.HISI_CGUL_MODE == expectCommrilMode) {
                this.mModemAdapter.setCdmaModeSide(this, this.mRequest, subOnM0, 0);
            } else if (HwVSimSlotSwitchController.CommrilMode.HISI_VSIM_MODE == expectCommrilMode) {
                this.mModemAdapter.setCdmaModeSide(this, this.mRequest, subOnM0, 2);
            }
        }
    }

    private int calcNetworkModeByAcqorder(String acqorder) {
        if ("0201".equals(acqorder)) {
            return 3;
        }
        if ("01".equals(acqorder)) {
            return 1;
        }
        return 9;
    }

    /* access modifiers changed from: package-private */
    public int modifyNetworkMode(int oldNetworkMode) {
        int networkMode = oldNetworkMode;
        if (oldNetworkMode == 2) {
            return 3;
        }
        if (oldNetworkMode != 12) {
            return networkMode;
        }
        return 9;
    }

    /* access modifiers changed from: package-private */
    public HwVSimController.EnableParam getEnableParam(HwVSimRequest request) {
        if (this.mVSimController == null) {
            return null;
        }
        return this.mVSimController.getEnableParam(request);
    }

    public boolean isDirectProcess() {
        return this.mVSimController != null && this.mVSimController.isDirectProcess();
    }

    /* access modifiers changed from: package-private */
    public void startListenForRildNvMatch() {
        if (isNeedWaitNvCfgMatchAndRestartRild()) {
            this.mVSimController.setOnRestartRildNvMatch(0, this.mHandler, 81, null);
            this.mVSimController.setOnRestartRildNvMatch(1, this.mHandler, 81, null);
            this.mIsWaitingRestartRild = true;
            if (this.mHandler != null) {
                this.mHandler.sendEmptyMessageDelayed(82, HwVSimConstants.WAIT_FOR_NV_CFG_MATCH_TIMEOUT);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onJudgeRestartRildNvMatch(Message msg) {
        logd("onJudgeRestartRildNvMatch");
        AsyncResult ar = (AsyncResult) msg.obj;
        if (ar != null && ar.exception == null && (ar.result instanceof int[]) && !this.mHasReceiveNvMatchUnsol) {
            int response = ((int[]) ar.result)[0];
            logd("onJudgeRestartRildNvMatch, response = " + response);
            this.mHasReceiveNvMatchUnsol = true;
            switch (response) {
                case 0:
                    removeMessageAndStopListen();
                    afterJudgeRestartRildNvMatch();
                    return;
                case 1:
                    removeMessageAndStopListen();
                    restartRild();
                    return;
                default:
                    this.mHasReceiveNvMatchUnsol = false;
                    return;
            }
        }
    }

    private void removeMessageAndStopListen() {
        if (this.mHandler != null) {
            this.mHandler.removeMessages(81);
            this.mHandler.removeMessages(82);
            this.mVSimController.unSetOnRestartRildNvMatch(0, this.mHandler);
            this.mVSimController.unSetOnRestartRildNvMatch(1, this.mHandler);
        }
    }

    /* access modifiers changed from: package-private */
    public void onJudgeRestartRildNvMatchTimeout() {
        logd("onJudgeRestartRildNvMatchTimeout");
        removeMessageAndStopListen();
        afterJudgeRestartRildNvMatch();
    }

    /* access modifiers changed from: package-private */
    public void onRadioAvailable(Message msg) {
        Integer index = HwVSimUtilsInner.getCiIndex(msg);
        logd("onRadioAvailable, index = " + index);
        if (index.intValue() < 0 || index.intValue() >= HwVSimModemAdapter.MAX_SUB_COUNT) {
            logd("onRadioAvailable: Invalid index : " + index + " received with event " + msg.what);
        } else if (!this.mIsWaitingRestartRild) {
            logd("onRadioAvailable, not waiting restart rild, return.");
        } else {
            setRadioAvailableMark(index.intValue(), true);
            if (HwVSimUtilsInner.isPlatformTwoModems()) {
                int unavailableSlotId = this.mVSimController.getSimSlotTableLastSlotId();
                logd("onRadioAvailable, [2 modems] sub " + unavailableSlotId + " is unavailable, ignore it.");
                setRadioAvailableMark(unavailableSlotId, true);
            }
            if (isAllRadioAvailable() != 0) {
                afterJudgeRestartRildNvMatch();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void afterJudgeRestartRildNvMatch() {
        logd("afterJudgeRestartRildNvMatch - do nothing.");
    }

    /* access modifiers changed from: package-private */
    public void restartRild() {
        for (int i = 0; i < HwVSimModemAdapter.MAX_SUB_COUNT; i++) {
            setRadioAvailableMark(i, false);
        }
        HwVSimSlotSwitchController.getInstance().restartRildBySubState();
    }

    private void setRadioAvailableMark(int subId, boolean available) {
        if (this.mRadioAvailableMark == null) {
            this.mRadioAvailableMark = new boolean[HwVSimModemAdapter.MAX_SUB_COUNT];
        }
        if (subId >= 0 && subId < this.mRadioAvailableMark.length) {
            this.mRadioAvailableMark[subId] = available;
        }
    }

    private boolean isAllRadioAvailable() {
        if (this.mRadioAvailableMark == null) {
            return false;
        }
        logd("isAllRadioAvailable: " + Arrays.toString(this.mRadioAvailableMark));
        for (int i = 0; i < this.mRadioAvailableMark.length; i++) {
            if (!this.mRadioAvailableMark[i]) {
                return false;
            }
        }
        return true;
    }
}
