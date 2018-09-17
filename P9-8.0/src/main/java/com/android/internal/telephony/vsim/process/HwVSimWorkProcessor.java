package com.android.internal.telephony.vsim.process;

import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.HwVSimPhoneFactory;
import com.android.internal.telephony.uicc.IccCardStatus;
import com.android.internal.telephony.uicc.IccCardStatus.CardState;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.android.internal.telephony.vsim.HwVSimController;
import com.android.internal.telephony.vsim.HwVSimController.EnableParam;
import com.android.internal.telephony.vsim.HwVSimController.ProcessState;
import com.android.internal.telephony.vsim.HwVSimEventReport.VSimEventInfoUtils;
import com.android.internal.telephony.vsim.HwVSimModemAdapter;
import com.android.internal.telephony.vsim.HwVSimModemAdapter.SimStateInfo;
import com.android.internal.telephony.vsim.HwVSimRequest;
import com.android.internal.telephony.vsim.HwVSimSlotSwitchController;
import com.android.internal.telephony.vsim.HwVSimSlotSwitchController.CommrilMode;
import com.android.internal.telephony.vsim.HwVSimUtilsInner;
import java.util.Arrays;

public abstract class HwVSimWorkProcessor extends HwVSimProcessor {
    protected static final int GET_ICC_CARD_STATUS_RETRY_TIMES = 5;
    public static final String LOG_TAG = "VSimWorkProcessor";
    private static final int LTE_SERVICE_ON = 1;
    protected static final int PHONE_COUNT = TelephonyManager.getDefault().getPhoneCount();
    protected int[] mGetIccCardStatusTimes;
    protected Handler mHandler;
    protected boolean mHasReceiveNvMatchUnsol = false;
    protected boolean mInDSDSPreProcess;
    protected boolean mIsWaitingRestartRild;
    protected boolean[] mRadioAvailableMark;
    protected HwVSimController mVSimController;

    protected abstract void logd(String str);

    protected abstract void onCardPowerOffDone(Message message);

    protected abstract void onCardPowerOnDone(Message message);

    protected abstract void onRadioPowerOnDone(Message message);

    protected abstract void onSetActiveModemModeDone(Message message);

    protected abstract void onSetPreferredNetworkTypeDone(Message message);

    protected abstract void onSetTeeDataReadyDone(Message message);

    protected abstract void onSwitchSlotDone(Message message);

    public HwVSimWorkProcessor(HwVSimController controller, HwVSimModemAdapter modemAdapter, HwVSimRequest request) {
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
            if (this.mVSimController.isEnableProcess() && (this.mVSimController.isDirectProcess() ^ 1) != 0) {
                this.mModemAdapter.setHwVSimPowerOn(this, request);
            }
            if (HwVSimUtilsInner.isPlatformRealTripple() && HwVSimUtilsInner.IS_DSDSPOWER_SUPPORT && isSwapProcess()) {
                this.mInDSDSPreProcess = true;
                int slaveSlot = request.getMainSlot() == 0 ? 1 : 0;
                this.mVSimController.setProhibitSubUpdateSimNoChange(slaveSlot, true);
                this.mModemAdapter.radioPowerOff(this, request, slaveSlot);
            } else {
                HwVSimModemAdapter hwVSimModemAdapter = this.mModemAdapter;
                if (HwVSimModemAdapter.IS_FAST_SWITCH_SIMSLOT && this.mRequest.getIsNeedSwitchCommrilMode()) {
                    this.mVSimController.setIsWaitingSwitchCdmaModeSide(true);
                }
                this.mModemAdapter.radioPowerOff(this, request);
            }
            setProcessState(ProcessState.PROCESS_STATE_WORK);
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

    protected void onRadioPowerOffDone(Message msg) {
        logd("onRadioPowerOffDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 2);
        AsyncResult ar = msg.obj;
        if (isAsyncResultValidForRequestNotSupport(ar)) {
            if (this.mInDSDSPreProcess) {
                this.mInDSDSPreProcess = false;
                HwVSimRequest request = this.mRequest;
                if (request != null) {
                    this.mModemAdapter.radioPowerOff(this, request);
                } else {
                    return;
                }
            }
            this.mModemAdapter.onRadioPowerOffDone(this, ar);
        }
    }

    protected void onGetSimStateDone(Message msg) {
        logd("onGetSimStateDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 3);
        AsyncResult ar = msg.obj;
        if (isAsyncResultValidForRequestNotSupport(ar)) {
            SimStateInfo ssInfo = this.mModemAdapter.onGetSimStateDone(this, ar);
            if (ssInfo != null) {
                logd("onGetSimStateDone ssInfo index = " + ssInfo.simIndex);
                logd("onGetSimStateDone ssInfo simEnable = " + ssInfo.simEnable);
                logd("onGetSimStateDone ssInfo simSub = " + ssInfo.simSub);
                logd("onGetSimStateDone ssInfo simNetInfo = " + ssInfo.simNetInfo);
            }
            HwVSimRequest request = ar.userObj;
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

    protected void getIccCardStatus(HwVSimRequest request, int subId) {
        logd("onCardPowerOffDone->getIccCardStatus,wait card status is absent");
        setIccCardStatusRetryTimes(subId, 0);
        this.mModemAdapter.getIccCardStatus(this, request, subId);
    }

    protected void onGetIccCardStatusDone(Message msg) {
        AsyncResult ar = msg.obj;
        if (ar != null) {
            HwVSimRequest request = ar.userObj;
            if (request != null) {
                int subId = request.mSubId;
                IccCardStatus status = ar.result;
                logd("onGetIccCardStatusDone:mCardState[" + subId + "]=" + (status != null ? status.mCardState : " state is null"));
                int retryTimes = getIccCardStatusRetryTimes(subId);
                if (!(ar.exception == null && (status == null || status.mCardState == CardState.CARDSTATE_ABSENT)) && retryTimes < 5) {
                    retryTimes++;
                    setIccCardStatusRetryTimes(subId, retryTimes);
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

    protected void afterGetAllCardStateDone() {
        logd("afterGetAllCardStateDone - do nothing.");
    }

    protected void setIccCardStatusRetryTimes(int subId, int times) {
        if (this.mGetIccCardStatusTimes == null) {
            this.mGetIccCardStatusTimes = new int[HwVSimModemAdapter.MAX_SUB_COUNT];
        }
        if (subId >= 0 && subId < this.mGetIccCardStatusTimes.length) {
            this.mGetIccCardStatusTimes[subId] = times;
        }
    }

    protected int getIccCardStatusRetryTimes(int subId) {
        if (this.mGetIccCardStatusTimes == null || subId < 0 || subId >= this.mGetIccCardStatusTimes.length) {
            return 5;
        }
        return this.mGetIccCardStatusTimes[subId];
    }

    protected void setPrefNetworkForM0(AsyncResult ar, HwVSimRequest request) {
        EnableParam param = getEnableParam(request);
        if (param == null) {
            doEnableProcessException(ar, request, Integer.valueOf(3));
            return;
        }
        int networkMode = acqorderToNetworkMode(param.acqorder);
        logd("set m0 preferred network to " + networkMode);
        this.mModemAdapter.setPreferredNetworkType(this, request, 2, networkMode);
    }

    protected int acqorderToNetworkMode(String acqorder) {
        HwVSimRequest request = this.mRequest;
        if (request == null) {
            return 3;
        }
        int[] cardTypes = request.getCardTypes();
        if (cardTypes == null || cardTypes.length == 0) {
            return 3;
        }
        boolean isULOnly;
        if (isDirectProcess()) {
            isULOnly = getULOnlyProp();
        } else {
            isULOnly = checkHasIccCardOnM2(cardTypes);
            setULOnlyProp(Boolean.valueOf(isULOnly));
        }
        return calcNetworkModeByAcqorder(acqorder, isULOnly);
    }

    protected void setCdmaModeSide(Message msg) {
        AsyncResult ar = msg.obj;
        if (isAsyncResultValid(ar)) {
            HwVSimRequest request = ar.userObj;
            CommrilMode expectCommrilMode = request.getExpectCommrilMode();
            int subOnM0 = request.getSlots()[0];
            if (CommrilMode.HISI_CG_MODE == expectCommrilMode) {
                this.mModemAdapter.setCdmaModeSide(this, this.mRequest, subOnM0, 1);
            } else if (CommrilMode.HISI_CGUL_MODE == expectCommrilMode) {
                this.mModemAdapter.setCdmaModeSide(this, this.mRequest, subOnM0, 0);
            } else if (CommrilMode.HISI_VSIM_MODE == expectCommrilMode) {
                this.mModemAdapter.setCdmaModeSide(this, this.mRequest, subOnM0, 2);
            }
        }
    }

    private boolean checkHasIccCardOnM2(int[] cardTypes) {
        if (HwVSimUtilsInner.isVSimDsdsVersionOne()) {
            logd("checkHasIccCardOnM2: isVSimDsdsVersionOne , return false ");
            return false;
        }
        logd("checkHasIccCardOnM2 cardTypes = " + Arrays.toString(cardTypes));
        boolean[] isCardPresent = HwVSimUtilsInner.getCardState(cardTypes);
        int insertedCardCount = HwVSimUtilsInner.getInsertedCardCount(cardTypes);
        boolean hasIccCardOnM2 = false;
        boolean ulOnlyMode = getVSimULOnlyMode();
        if (HwVSimUtilsInner.isChinaTelecom() && HwVSimUtilsInner.isPlatformRealTripple()) {
            int userReservedSubId = getUserReservedSubId();
            int mainSlot = HwVSimPhoneFactory.getVSimSavedMainSlot();
            if (mainSlot == -1) {
                mainSlot = 0;
            }
            int slaveSlot = mainSlot == 0 ? 1 : 0;
            if (insertedCardCount != 0) {
                if (isCardPresent[mainSlot] && isCardPresent[slaveSlot] && (ulOnlyMode || slaveSlot == userReservedSubId)) {
                    hasIccCardOnM2 = true;
                } else if (!isCardPresent[mainSlot] && isCardPresent[slaveSlot]) {
                    hasIccCardOnM2 = true;
                }
            }
        } else if (ulOnlyMode && insertedCardCount == PHONE_COUNT) {
            hasIccCardOnM2 = true;
        }
        return hasIccCardOnM2;
    }

    private int calcNetworkModeByAcqorder(String acqorder, boolean isULOnly) {
        if (isULOnly) {
            if ("0201".equals(acqorder) || "02".equals(acqorder)) {
                return 2;
            }
            return 12;
        } else if ("0201".equals(acqorder)) {
            return 3;
        } else {
            if ("01".equals(acqorder)) {
                return 1;
            }
            return 9;
        }
    }

    protected int modifyNetworkMode(int oldNetworkMode, boolean removeG) {
        int networkMode = oldNetworkMode;
        if (!removeG) {
            switch (oldNetworkMode) {
                case 2:
                    networkMode = 3;
                    break;
                case 12:
                    networkMode = 9;
                    break;
            }
        }
        switch (oldNetworkMode) {
            case 3:
                networkMode = 2;
                break;
            case 9:
                networkMode = 12;
                break;
        }
        if (networkMode != oldNetworkMode) {
            setULOnlyProp(Boolean.valueOf(removeG));
        }
        return networkMode;
    }

    protected EnableParam getEnableParam(HwVSimRequest request) {
        if (this.mVSimController == null) {
            return null;
        }
        return this.mVSimController.getEnableParam(request);
    }

    protected void setULOnlyProp(Boolean isULOnly) {
        if (this.mVSimController != null) {
            this.mVSimController.setULOnlyProp(isULOnly);
        }
    }

    protected boolean getULOnlyProp() {
        if (this.mVSimController != null) {
            return this.mVSimController.getULOnlyProp();
        }
        return false;
    }

    protected boolean getVSimULOnlyMode() {
        if (this.mVSimController != null) {
            return this.mVSimController.getVSimULOnlyMode();
        }
        return false;
    }

    protected int getUserReservedSubId() {
        if (this.mVSimController != null) {
            return this.mVSimController.getUserReservedSubId();
        }
        return -1;
    }

    public boolean isDirectProcess() {
        if (this.mVSimController == null) {
            return false;
        }
        return this.mVSimController.isDirectProcess();
    }

    protected int getNetworkTypeOnModem1(int modemId) {
        int modem1NetWorkType = HwVSimPhoneFactory.getVSimSavedNetworkMode(modemId);
        logd("getNetworkTypeOnModem1 : modem1NetWorkType  = " + modem1NetWorkType);
        return modem1NetWorkType;
    }

    protected int getNetworkTypeOnModem2() {
        return 1;
    }

    protected void startListenForRildNvMatch() {
        if (isNeedWaitNvCfgMatchAndRestartRild()) {
            this.mVSimController.setOnRestartRildNvMatch(0, this.mHandler, 81, null);
            this.mVSimController.setOnRestartRildNvMatch(1, this.mHandler, 81, null);
            this.mIsWaitingRestartRild = true;
            if (this.mHandler != null) {
                this.mHandler.sendEmptyMessageDelayed(82, HwVSimConstants.WAIT_FOR_NV_CFG_MATCH_TIMEOUT);
            }
        }
    }

    protected void onJudgeRestartRildNvMatch(Message msg) {
        logd("onJudgeRestartRildNvMatch");
        AsyncResult ar = msg.obj;
        if (ar != null && ar.exception == null && (ar.result instanceof int[]) && (this.mHasReceiveNvMatchUnsol ^ 1) != 0) {
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

    protected void onJudgeRestartRildNvMatchTimeout() {
        logd("onJudgeRestartRildNvMatchTimeout");
        removeMessageAndStopListen();
        afterJudgeRestartRildNvMatch();
    }

    protected void onRadioAvailable(Message msg) {
        Integer index = HwVSimUtilsInner.getCiIndex(msg);
        logd("onRadioAvailable, index = " + index);
        if (index.intValue() < 0 || index.intValue() >= HwVSimModemAdapter.MAX_SUB_COUNT) {
            logd("onRadioAvailable: Invalid index : " + index + " received with event " + msg.what);
        } else if (this.mIsWaitingRestartRild) {
            setRadioAvailableMark(index.intValue(), true);
            if (isAllRadioAvailable()) {
                afterJudgeRestartRildNvMatch();
            }
        } else {
            logd("onRadioAvailable, not waiting restart rild, return.");
        }
    }

    protected void afterJudgeRestartRildNvMatch() {
        logd("afterJudgeRestartRildNvMatch - do nothing.");
    }

    protected void restartRild() {
        for (int i = 0; i < HwVSimModemAdapter.MAX_SUB_COUNT; i++) {
            setRadioAvailableMark(i, false);
        }
        HwVSimSlotSwitchController.getInstance().restartRildBySubState();
    }

    protected void setRadioAvailableMark(int subId, boolean available) {
        if (this.mRadioAvailableMark == null) {
            this.mRadioAvailableMark = new boolean[HwVSimModemAdapter.MAX_SUB_COUNT];
        }
        if (subId >= 0 && subId < this.mRadioAvailableMark.length) {
            this.mRadioAvailableMark[subId] = available;
        }
    }

    protected boolean isAllRadioAvailable() {
        if (this.mRadioAvailableMark == null) {
            return false;
        }
        logd("isAllRadioAvailable: " + Arrays.toString(this.mRadioAvailableMark));
        for (boolean z : this.mRadioAvailableMark) {
            if (!z) {
                return false;
            }
        }
        return true;
    }
}
