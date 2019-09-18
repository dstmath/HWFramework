package com.android.internal.telephony.vsim.process;

import android.os.AsyncResult;
import android.os.Message;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.android.internal.telephony.vsim.HwVSimController;
import com.android.internal.telephony.vsim.HwVSimEventReport;
import com.android.internal.telephony.vsim.HwVSimLog;
import com.android.internal.telephony.vsim.HwVSimModemAdapter;
import com.android.internal.telephony.vsim.HwVSimRequest;
import com.android.internal.telephony.vsim.HwVSimSlotSwitchController;
import com.android.internal.telephony.vsim.HwVSimUtilsInner;

public class HwVSimEReadyProcessor extends HwVSimReadyProcessor {
    public static final String LOG_TAG = "VSimEReadyProcessor";
    boolean mHasEnterAfterNetwork = false;

    public static HwVSimEReadyProcessor create(HwVSimController controller, HwVSimModemAdapter modemAdapter, HwVSimRequest request) {
        if (controller == null || !controller.isDirectProcess()) {
            return new HwVSimEReadyProcessor(controller, modemAdapter, request);
        }
        return new HwVSimEDReadyProcessor(controller, modemAdapter, request);
    }

    HwVSimEReadyProcessor(HwVSimController controller, HwVSimModemAdapter modemAdapter, HwVSimRequest request) {
        super(controller, modemAdapter, request);
    }

    public void onEnter() {
        logd("onEnter");
        this.mHasEnterAfterNetwork = false;
        setProcessState(HwVSimController.ProcessState.PROCESS_STATE_READY);
        this.mIsM0Ready = false;
        this.mVSimController.setOnVsimRegPLMNSelInfo(this.mHandler, 65, null);
        this.mVSimController.registerNetStateReceiver();
        if (this.mHandler != null) {
            this.mHandler.sendEmptyMessageDelayed(71, 60000);
        }
    }

    public void onExit() {
        logd("onExit");
        this.mHasEnterAfterNetwork = false;
        if (this.mHandler != null) {
            this.mHandler.removeMessages(71);
        }
        this.mVSimController.unregisterNetStateReceiver();
        this.mVSimController.unSetOnVsimRegPLMNSelInfo(this.mHandler);
        setProcessState(HwVSimController.ProcessState.PROCESS_STATE_NONE);
    }

    /* access modifiers changed from: protected */
    public void logd(String s) {
        HwVSimLog.VSimLogD(LOG_TAG, s);
    }

    public boolean processMessage(Message msg) {
        boolean retVal = true;
        if (!isMessageShouldDeal(msg)) {
            return false;
        }
        switch (msg.what) {
            case HwVSimConstants.EVENT_CARD_POWER_ON_DONE:
                onCardPowerOnDone(msg);
                break;
            case HwVSimConstants.EVENT_RADIO_POWER_ON_DONE:
                onRadioPowerOnDone(msg);
                break;
            case 50:
                onNetworkConnected();
                break;
            case HwVSimConstants.EVENT_ENABLE_VSIM_FINISH:
                onEnableVSimFinish();
                break;
            case 65:
                onPlmnSelInfoDone(msg);
                break;
            case 66:
                onSetNetworkRatAndSrvdomainDone(msg);
                break;
            case HwVSimConstants.EVENT_NETWORK_CONNECT_TIMEOUT:
                onNetworkConnectTimeout();
                break;
            default:
                retVal = false;
                break;
        }
        return retVal;
    }

    public void doProcessException(AsyncResult ar, HwVSimRequest request) {
        doEnableProcessException(ar, request, 3);
    }

    /* access modifiers changed from: package-private */
    public void onNetworkConnected() {
        logd("onNetworkConnected");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 16);
        if (this.mIsM0Ready) {
            if (this.mHandler != null) {
                this.mHandler.removeMessages(71);
            }
            this.mVSimController.unregisterNetStateReceiver();
            this.mIsM0Ready = false;
            afterNetwork();
        }
    }

    /* access modifiers changed from: package-private */
    public void onNetworkConnectTimeout() {
        logd("onNetworkConnectTimeout");
        this.mVSimController.unregisterNetStateReceiver();
        if (this.mHandler != null) {
            this.mHandler.removeMessages(65);
            this.mHandler.removeMessages(50);
        }
        this.mIsM0Ready = false;
        afterNetwork();
    }

    /* access modifiers changed from: protected */
    public void afterNetwork() {
        logd("afterNetwork");
        if (this.mHasEnterAfterNetwork) {
            logd("can not enter more than once.");
            return;
        }
        this.mHasEnterAfterNetwork = true;
        HwVSimRequest request = this.mRequest;
        if (request != null) {
            if (isSwapProcess()) {
                int subId = request.getMainSlot();
                int simIndex = 1;
                if (subId == 2) {
                    simIndex = 11;
                }
                this.mModemAdapter.cardPowerOn(this, request, subId, simIndex);
                request.setSource(3);
                this.mVSimController.setMarkForCardReload(subId, true);
            } else {
                int slaveSlot = 0;
                if (isCrossProcess()) {
                    int mainSlot = request.getMainSlot();
                    if (mainSlot == 0) {
                        slaveSlot = 1;
                    }
                    this.mRequest.setSource(3);
                    HwVSimRequest slaveRequest = this.mRequest.clone();
                    if (slaveRequest != null) {
                        slaveRequest.setPowerOnOffMark(slaveSlot, true);
                        slaveRequest.setCardOnOffMark(slaveSlot, true);
                        this.mModemAdapter.cardPowerOn(this, slaveRequest, slaveSlot, 1);
                        this.mVSimController.setMarkForCardReload(slaveSlot, true);
                    }
                    if (HwVSimUtilsInner.isPlatformRealTripple()) {
                        int mainIndex = mainSlot == 2 ? 11 : 1;
                        HwVSimRequest mainRequest = this.mRequest.clone();
                        if (mainRequest != null) {
                            mainRequest.setPowerOnOffMark(mainSlot, true);
                            mainRequest.setCardOnOffMark(mainSlot, true);
                            this.mModemAdapter.cardPowerOn(this, mainRequest, mainSlot, mainIndex);
                            this.mVSimController.setMarkForCardReload(mainSlot, true);
                        }
                    }
                } else if (isDirectProcess()) {
                    int subId2 = request.getMainSlot();
                    if (!this.mModemAdapter.isNeedRadioOnM2()) {
                        logd("m2 subId = " + subId2 + "set radio power off");
                        this.mVSimController.getPhoneBySub(subId2).getServiceStateTracker().setDesiredPowerState(false);
                        enableVSimFinish();
                    } else {
                        this.mModemAdapter.setNetworkRatAndSrvdomain(this, request, subId2, calculateNetworkModeForModem2(), 0);
                    }
                }
            }
            if (HwVSimUtilsInner.isPlatformTwoModems() != 0) {
                powerOnSlaveCardOnDSDS();
            } else {
                powerOnSlaveRadioOnDSDS();
            }
        }
    }

    private int calculateNetworkModeForModem2() {
        int rat = 1;
        if (HwVSimUtilsInner.isDualImsSupported() && this.mVSimController.hasIccCardOnM2() && this.mVSimController.getCommrilMode() == HwVSimSlotSwitchController.CommrilMode.HISI_VSIM_MODE) {
            rat = 59;
        }
        logd("calculateNetworkModeForModem2, rat = " + rat);
        return rat;
    }

    /* access modifiers changed from: protected */
    public void onCardPowerOnDone(Message msg) {
        logd("onCardPowerOnDone");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 7);
        AsyncResult ar = (AsyncResult) msg.obj;
        if (isAsyncResultValidNoProcessException(ar)) {
            HwVSimRequest request = (HwVSimRequest) ar.userObj;
            int subId = request.mSubId;
            int mainSlot = request.getMainSlot();
            request.setCardOnOffMark(subId, false);
            if (ar.exception != null) {
                this.mVSimController.setMarkForCardReload(subId, false);
            }
            if (isSwapProcess()) {
                doRadioPowerOnForSwap(request);
            } else if (isCrossProcess()) {
                logd("onCardPowerOnDone: isCrossProcess subId = " + subId + " mainSlot=" + mainSlot);
                if (HwVSimUtilsInner.isVSimDsdsVersionOne()) {
                    if (!this.mVSimController.isSubOnM2(subId)) {
                        logd("onCardPowerOnDone: isCrossProcess set radio_power On subId =" + subId);
                        this.mModemAdapter.radioPowerOn(this, request, subId);
                    } else {
                        request.setPowerOnOffMark(subId, false);
                        logd("onCardPowerOnDone: isCrossProcess, m2 on subId = " + subId + ", set radio_power off");
                        this.mVSimController.getPhoneBySub(subId).getServiceStateTracker().setDesiredPowerState(false);
                        if (isAllMarkClear(request)) {
                            enableVSimFinish();
                        }
                    }
                } else if (subId != mainSlot) {
                    this.mModemAdapter.radioPowerOn(this, request, subId);
                } else if (!HwVSimUtilsInner.isPlatformRealTripple() || !HwVSimUtilsInner.IS_DSDSPOWER_SUPPORT) {
                    request.setPowerOnOffMark(subId, false);
                    logd("m2 subId = " + subId + " set radio power off");
                    this.mVSimController.getPhoneBySub(subId).getServiceStateTracker().setDesiredPowerState(false);
                    if (isAllMarkClear(request)) {
                        enableVSimFinish();
                    }
                } else {
                    this.mModemAdapter.radioPowerOn(this, request, subId);
                }
            }
        }
    }

    private void onSetNetworkRatAndSrvdomainDone(Message msg) {
        AsyncResult ar = (AsyncResult) msg.obj;
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 1);
        if (isAsyncResultValid(ar)) {
            this.mModemAdapter.onSetNetworkRatAndSrvdomainDone(this, ar);
            int subId = ((HwVSimRequest) ar.userObj).mSubId;
            logd("onSetNetworkRatAndSrvdomainDone subId = " + subId);
            this.mModemAdapter.radioPowerOn(this, this.mRequest, subId);
        }
    }

    /* access modifiers changed from: protected */
    public void onRadioPowerOnDone(Message msg) {
        logd("onRadioPowerOnDone");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 11);
        AsyncResult ar = (AsyncResult) msg.obj;
        if (isAsyncResultValidForRequestNotSupport(ar)) {
            HwVSimRequest request = (HwVSimRequest) ar.userObj;
            int subId = request.mSubId;
            request.setPowerOnOffMark(subId, false);
            logd("onRadioPowerOnDone : subId = " + subId);
            if (HwVSimUtilsInner.IS_DSDSPOWER_SUPPORT && isSwapProcess() && subId != request.getMainSlot()) {
                logd("onRadioPowerOnDone : slave slot not finish!");
            } else if (isAllMarkClear(request)) {
                enableVSimFinish();
            }
        }
    }

    private void onEnableVSimFinish() {
        logd("onEnableVSimFinish");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 13);
        transitionToState(0);
    }

    private void enableVSimFinish() {
        logd("enableVSimFinish");
        Message onCompleted = this.mVSimController.obtainMessage(57, this.mRequest);
        AsyncResult.forMessage(onCompleted);
        onCompleted.sendToTarget();
    }

    private void doRadioPowerOnForSwap(HwVSimRequest request) {
        boolean hasIccCardOnM2;
        if (HwVSimUtilsInner.isPlatformTwoModems()) {
            powerOnSlaveRadioOnDSDS();
        }
        int userReservedSubId = this.mVSimController.getUserReservedSubId();
        int subId = request.getMainSlot();
        int insertedSimCount = this.mVSimController.getInsertedSimCount();
        logd("doRadioPowerOnForSwap subId = " + subId + " userReservedSubId = " + userReservedSubId + " insertedSimCount = " + insertedSimCount + " hasIccCardOnM2 = " + hasIccCardOnM2);
        boolean noNeedRadioPowerOnModem2 = true;
        if (!HwVSimUtilsInner.isPlatformTwoModemsActual() || (insertedSimCount != 0 && ((insertedSimCount != 1 || hasIccCardOnM2) && (insertedSimCount != 2 || subId == userReservedSubId)))) {
            noNeedRadioPowerOnModem2 = false;
        }
        if (noNeedRadioPowerOnModem2) {
            logd("doRadioPowerOnForSwap no need m2 power on from subId = " + subId);
            request.setPowerOnOffMark(subId, false);
            this.mVSimController.getPhoneBySub(subId).getServiceStateTracker().setDesiredPowerState(false);
            enableVSimFinish();
            return;
        }
        this.mModemAdapter.setNetworkRatAndSrvdomain(this, this.mRequest, subId, 1, 0);
    }

    private void powerOnSlaveCardOnDSDS() {
        if (this.mRequest != null && HwVSimUtilsInner.IS_DSDSPOWER_SUPPORT && isSwapProcess()) {
            int slaveSlot = HwVSimUtilsInner.getAnotherSlotId(this.mRequest.getMainSlot());
            int slaveIndex = HwVSimUtilsInner.getSinIndexBySlotId(slaveSlot);
            logd("powerOnSlaveCardOnDSDS, slotid = " + slaveSlot);
            HwVSimRequest slaveRequest = this.mRequest.clone();
            if (slaveRequest != null) {
                slaveRequest.setSource(3);
                this.mModemAdapter.cardPowerOn(this, slaveRequest, slaveSlot, slaveIndex);
            }
        }
    }

    private void powerOnSlaveRadioOnDSDS() {
        if (this.mRequest != null && HwVSimUtilsInner.IS_DSDSPOWER_SUPPORT && isSwapProcess()) {
            int slaveSlot = this.mRequest.getMainSlot() == 0 ? 1 : 0;
            logd("powerOnSlaveRadioOnDSDS, slotid = " + slaveSlot);
            this.mVSimController.setProhibitSubUpdateSimNoChange(slaveSlot, false);
            HwVSimRequest slaveRequest = this.mRequest.clone();
            if (slaveRequest != null) {
                slaveRequest.setSource(3);
                this.mModemAdapter.radioPowerOn(this, slaveRequest, slaveSlot);
            }
        }
    }

    private boolean isMessageShouldDeal(Message msg) {
        return super.isMessageShouldDeal(msg, 3);
    }
}
