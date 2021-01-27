package com.android.internal.telephony.vsim.process;

import android.os.Message;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.android.internal.telephony.vsim.HwVSimController;
import com.android.internal.telephony.vsim.HwVSimEventReport;
import com.android.internal.telephony.vsim.HwVSimModemAdapter;
import com.android.internal.telephony.vsim.HwVSimRequest;
import com.android.internal.telephony.vsim.HwVSimSlotSwitchController;
import com.android.internal.telephony.vsim.HwVSimUtilsInner;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.internal.telephony.uicc.IccCardStatusExt;
import com.huawei.internal.telephony.vsim.process.HwVSimHisiProcessor;
import java.util.Arrays;

public abstract class HwVSimWorkProcessor extends HwVSimHisiProcessor {
    static final int GET_ICC_CARD_STATUS_RETRY_TIMES = 5;
    static final String LOG_TAG = "VSimWorkProcessor";
    private int[] mGetIccCardStatusTimes;
    boolean mHasReceiveNvMatchUnsol = false;
    private boolean mInDSDSPreProcess = false;
    boolean mIsWaitingRestartRild = false;
    private boolean[] mRadioAvailableMark;

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
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
        super(controller, modemAdapter, request);
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
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
            setProcessState(HwVSimConstants.ProcessState.PROCESS_STATE_WORK);
        }
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public void onExit() {
        logd("onExit");
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public void transitionToState(int state) {
        this.mVSimController.transitionToState(state);
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public Message obtainMessage(int what, Object obj) {
        return this.mVSimController.obtainMessage(what, obj);
    }

    /* access modifiers changed from: protected */
    public void onRadioPowerOffDone(Message msg) {
        logd("onRadioPowerOffDone");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 2);
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (isAsyncResultValidForRequestNotSupport(ar)) {
            if (this.mInDSDSPreProcess) {
                this.mInDSDSPreProcess = false;
                HwVSimRequest request = this.mRequest;
                if (request != null) {
                    this.mModemAdapter.radioPowerOff(this, request);
                    this.mModemAdapter.onRadioPowerOffSlaveModemDone(this, request);
                    return;
                }
                return;
            }
            this.mModemAdapter.onRadioPowerOffDone(this, ar);
        }
    }

    /* access modifiers changed from: protected */
    public void onGetSimStateDone(Message msg) {
        logd("onGetSimStateDone");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 3);
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (isAsyncResultValidForRequestNotSupport(ar)) {
            HwVSimModemAdapter.SimStateInfo ssInfo = this.mModemAdapter.onGetSimStateDone(ar);
            if (ssInfo != null) {
                logd("onGetSimStateDone ssInfo = " + ssInfo.toString());
            }
            HwVSimRequest request = (HwVSimRequest) ar.getUserObj();
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
        HwVSimRequest request;
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (!(ar == null || (request = (HwVSimRequest) ar.getUserObj()) == null)) {
            int subId = request.mSubId;
            IccCardStatusExt status = IccCardStatusExt.from(ar.getResult());
            StringBuilder sb = new StringBuilder();
            sb.append("onGetIccCardStatusDone:mCardState[");
            sb.append(subId);
            sb.append("]=");
            sb.append(status != null ? status.getCardState() : " state is null");
            logd(sb.toString());
            int retryTimes = getIccCardStatusRetryTimes(subId);
            if (!(ar.getException() == null && (status == null || status.getCardState() == IccCardStatusExt.CardStateEx.CARDSTATE_ABSENT)) && retryTimes < 5) {
                int retryTimes2 = retryTimes + 1;
                setIccCardStatusRetryTimes(subId, retryTimes2);
                logd("onGetIccCardStatusDone: retry getIccCardStatus,Times=" + retryTimes2);
                this.mModemAdapter.getIccCardStatus(this, request, subId);
                return;
            }
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

    /* access modifiers changed from: protected */
    public void afterGetAllCardStateDone() {
        logd("afterGetAllCardStateDone - do nothing.");
    }

    /* access modifiers changed from: package-private */
    public void setIccCardStatusRetryTimes(int subId, int times) {
        if (this.mGetIccCardStatusTimes == null) {
            this.mGetIccCardStatusTimes = new int[HwVSimModemAdapter.MAX_SUB_COUNT];
        }
        if (subId >= 0) {
            int[] iArr = this.mGetIccCardStatusTimes;
            if (subId < iArr.length) {
                iArr[subId] = times;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public int getIccCardStatusRetryTimes(int subId) {
        int[] iArr = this.mGetIccCardStatusTimes;
        if (iArr == null || subId < 0 || subId >= iArr.length) {
            return 5;
        }
        return iArr[subId];
    }

    /* access modifiers changed from: package-private */
    public void setPrefNetworkForM0(AsyncResultEx ar, HwVSimRequest request) {
        HwVSimConstants.EnableParam param = getEnableParam(request);
        if (param == null) {
            doEnableProcessException(ar, request, 3);
            return;
        }
        int networkMode = acqorderToNetworkMode(param.acqOrder);
        logd("set m0 preferred network to " + networkMode);
        this.mModemAdapter.setPreferredNetworkType(this, request, 2, networkMode);
    }

    /* access modifiers changed from: package-private */
    public int acqorderToNetworkMode(String acqorder) {
        return calcNetworkModeByAcqorder(acqorder);
    }

    /* access modifiers changed from: package-private */
    public void setCdmaModeSide(Message msg) {
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (isAsyncResultValid(ar)) {
            HwVSimRequest request = (HwVSimRequest) ar.getUserObj();
            HwVSimSlotSwitchController.CommrilMode expectCommrilMode = request.getExpectCommrilMode();
            int subOnM0 = request.getSlots()[0];
            if (HwVSimSlotSwitchController.CommrilMode.HISI_CG_MODE == expectCommrilMode) {
                this.mModemAdapter.setCdmaModeSide(this, this.mRequest, subOnM0, 1);
            } else if (HwVSimSlotSwitchController.CommrilMode.HISI_CGUL_MODE == expectCommrilMode) {
                this.mModemAdapter.setCdmaModeSide(this, this.mRequest, subOnM0, 0);
            } else if (HwVSimSlotSwitchController.CommrilMode.HISI_VSIM_MODE == expectCommrilMode) {
                this.mModemAdapter.setCdmaModeSide(this, this.mRequest, subOnM0, 2);
            } else {
                request.mSubId = subOnM0;
                Message onCompleted = obtainMessage(80, request);
                AsyncResultEx.forMessage(onCompleted, (Object) null, (Throwable) null);
                onCompleted.sendToTarget();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public int modifyNetworkMode(int oldNetworkMode) {
        if (oldNetworkMode == 2) {
            return 3;
        }
        if (oldNetworkMode != 12) {
            return oldNetworkMode;
        }
        return 9;
    }

    /* access modifiers changed from: package-private */
    public HwVSimConstants.EnableParam getEnableParam(HwVSimRequest request) {
        if (this.mVSimController == null) {
            return null;
        }
        return this.mVSimController.getEnableParam(request);
    }

    @Override // com.huawei.internal.telephony.vsim.process.HwVSimHisiProcessor, com.android.internal.telephony.vsim.process.HwVSimProcessor
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
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (ar != null && ar.getException() == null && (ar.getResult() instanceof int[]) && !this.mHasReceiveNvMatchUnsol) {
            int response = ((int[]) ar.getResult())[0];
            logd("onJudgeRestartRildNvMatch, response = " + response);
            this.mHasReceiveNvMatchUnsol = true;
            if (response == 0) {
                removeMessageAndStopListen();
                afterJudgeRestartRildNvMatch();
            } else if (response != 1) {
                this.mHasReceiveNvMatchUnsol = false;
            } else {
                removeMessageAndStopListen();
                restartRild();
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
            if (isAllRadioAvailable()) {
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
        if (subId >= 0) {
            boolean[] zArr = this.mRadioAvailableMark;
            if (subId < zArr.length) {
                zArr[subId] = available;
            }
        }
    }

    private boolean isAllRadioAvailable() {
        if (this.mRadioAvailableMark == null) {
            return false;
        }
        logd("isAllRadioAvailable: " + Arrays.toString(this.mRadioAvailableMark));
        int i = 0;
        while (true) {
            boolean[] zArr = this.mRadioAvailableMark;
            if (i >= zArr.length) {
                return true;
            }
            if (!zArr[i]) {
                return false;
            }
            i++;
        }
    }
}
