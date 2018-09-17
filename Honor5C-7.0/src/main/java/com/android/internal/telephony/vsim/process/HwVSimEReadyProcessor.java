package com.android.internal.telephony.vsim.process;

import android.os.AsyncResult;
import android.os.Message;
import android.os.SystemClock;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.android.internal.telephony.vsim.HwVSimController;
import com.android.internal.telephony.vsim.HwVSimController.ProcessState;
import com.android.internal.telephony.vsim.HwVSimEventReport.VSimEventInfoUtils;
import com.android.internal.telephony.vsim.HwVSimLog;
import com.android.internal.telephony.vsim.HwVSimModemAdapter;
import com.android.internal.telephony.vsim.HwVSimRequest;
import com.android.internal.telephony.vsim.HwVSimUtilsInner;

public class HwVSimEReadyProcessor extends HwVSimReadyProcessor {
    public static final String LOG_TAG = "VSimEReadyProcessor";

    public static HwVSimEReadyProcessor create(HwVSimController controller, HwVSimModemAdapter modemAdapter, HwVSimRequest request) {
        if (controller == null || !controller.isDirectProcess()) {
            return new HwVSimEReadyProcessor(controller, modemAdapter, request);
        }
        return new HwVSimEDReadyProcessor(controller, modemAdapter, request);
    }

    public HwVSimEReadyProcessor(HwVSimController controller, HwVSimModemAdapter modemAdapter, HwVSimRequest request) {
        super(controller, modemAdapter, request);
    }

    public void onEnter() {
        logd("onEnter");
        setProcessState(ProcessState.PROCESS_STATE_READY);
        this.mIsM0Ready = false;
        this.mVSimController.setOnVsimRegPLMNSelInfo(this.mHandler, 65, null);
        this.mVSimController.registerNetStateReceiver();
        if (this.mHandler != null) {
            this.mHandler.sendEmptyMessageDelayed(71, 60000);
        }
    }

    public void onExit() {
        logd("onExit");
        if (this.mHandler != null) {
            this.mHandler.removeMessages(71);
        }
        this.mVSimController.unregisterNetStateReceiver();
        this.mVSimController.unSetOnVsimRegPLMNSelInfo(this.mHandler);
        setProcessState(ProcessState.PROCESS_STATE_NONE);
    }

    protected void logd(String s) {
        HwVSimLog.VSimLogD(LOG_TAG, s);
    }

    public boolean processMessage(Message msg) {
        switch (msg.what) {
            case HwVSimConstants.EVENT_CARD_POWER_ON_DONE /*45*/:
                onCardPowerOnDone(msg);
                return true;
            case HwVSimConstants.EVENT_RADIO_POWER_ON_DONE /*46*/:
                onRadioPowerOnDone(msg);
                return true;
            case HwVSimConstants.EVENT_NETWORK_CONNECTED /*50*/:
                onNetworkConnected(msg);
                return true;
            case HwVSimConstants.EVENT_ENABLE_VSIM_FINISH /*57*/:
                onEnableVSimFinish(msg);
                return true;
            case HwVSimConstants.EVENT_VSIM_PLMN_SELINFO /*65*/:
                onPlmnSelInfoDone(msg);
                return true;
            case HwVSimConstants.EVENT_SET_NETWORK_RAT_AND_SRVDOMAIN_DONE /*66*/:
                onSetNetworkRatAndSrvdomainDone(msg);
                return true;
            case HwVSimConstants.EVENT_NETWORK_CONNECT_TIMEOUT /*71*/:
                onNetworkConnectTimeout(msg);
                return true;
            default:
                return false;
        }
    }

    public void doProcessException(AsyncResult ar, HwVSimRequest request) {
        doEnableProcessException(ar, request, Integer.valueOf(3));
    }

    protected void onPlmnSelInfoDone(Message msg) {
        logd("onPlmnSelInfoDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 15);
        AsyncResult ar = msg.obj;
        int flag = -1;
        int result = -1;
        if (ar.exception == null) {
            int[] response = ar.result;
            if (response.length != 0) {
                flag = response[0];
                result = response[1];
            }
        }
        logd("PLMNSELEINFO: " + flag + " " + result);
        if (flag == 1) {
            this.mIsM0Ready = true;
            if (result == 0) {
                if (this.mVSimController.isNetworkConnected() && this.mHandler != null) {
                    this.mHandler.obtainMessage(50).sendToTarget();
                }
                VSimEventInfoUtils.setPsRegTime(this.mVSimController.mEventInfo, SystemClock.elapsedRealtime() - this.mVSimController.mVSimEnterTime);
                return;
            } else if (this.mHandler != null) {
                this.mHandler.removeMessages(71);
                this.mHandler.obtainMessage(71).sendToTarget();
                return;
            } else {
                return;
            }
        }
        this.mIsM0Ready = false;
    }

    protected void onNetworkConnected(Message msg) {
        logd("onNetworkConnected");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 16);
        if (this.mIsM0Ready) {
            if (this.mHandler != null) {
                this.mHandler.removeMessages(71);
            }
            this.mVSimController.unregisterNetStateReceiver();
            this.mIsM0Ready = false;
            afterNetwork();
        }
    }

    protected void onNetworkConnectTimeout(Message msg) {
        logd("onNetworkConnectTimeout");
        this.mVSimController.unregisterNetStateReceiver();
        if (this.mHandler != null) {
            this.mHandler.removeMessages(65);
            this.mHandler.removeMessages(50);
        }
        this.mIsM0Ready = false;
        afterNetwork();
    }

    protected void afterNetwork() {
        int slaveSlot = 0;
        logd("afterNetwork");
        HwVSimRequest request = this.mRequest;
        if (request != null) {
            int subId;
            if (isSwapProcess()) {
                subId = request.getMainSlot();
                int simIndex = 1;
                if (subId == 2) {
                    simIndex = 11;
                }
                this.mModemAdapter.cardPowerOn(this, request, subId, simIndex);
                this.mVSimController.setMarkForCardReload(subId, true);
            } else if (isCrossProcess()) {
                int mainSlot = request.getMainSlot();
                if (mainSlot == 0) {
                    slaveSlot = 1;
                }
                int slaveIndex = 1;
                if (slaveSlot == 2) {
                    slaveIndex = 11;
                }
                HwVSimRequest slaveRequst = this.mRequest.clone();
                slaveRequst.setPowerOnOffMark(slaveSlot, true);
                slaveRequst.setCardOnOffMark(slaveSlot, true);
                this.mModemAdapter.cardPowerOn(this, slaveRequst, slaveSlot, slaveIndex);
                this.mVSimController.setMarkForCardReload(slaveSlot, true);
                int mainIndex = 1;
                if (mainSlot == 2) {
                    mainIndex = 11;
                }
                HwVSimRequest mainRequest = this.mRequest.clone();
                mainRequest.setPowerOnOffMark(mainSlot, true);
                mainRequest.setCardOnOffMark(mainSlot, true);
                this.mModemAdapter.cardPowerOn(this, mainRequest, mainSlot, mainIndex);
                this.mVSimController.setMarkForCardReload(mainSlot, true);
            } else if (isDirectProcess()) {
                subId = request.getMainSlot();
                if (this.mModemAdapter.isNeedRadioOnM2()) {
                    this.mModemAdapter.setNetworkRatAndSrvdomain(this, request, subId, 1, 0);
                } else {
                    logd("m2 subId = " + subId + "set radio power off");
                    this.mVSimController.getPhoneBySub(subId).getServiceStateTracker().setDesiredPowerState(false);
                    enableVSimFinish();
                }
            }
            powerOnSlaveRadioOnDSDS();
        }
    }

    protected void onCardPowerOnDone(Message msg) {
        logd("onCardPowerOnDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 7);
        AsyncResult ar = msg.obj;
        if (isAsyncResultValidNoProcessException(ar)) {
            HwVSimRequest request = ar.userObj;
            int subId = request.mSubId;
            int mainSlot = request.getMainSlot();
            request.setCardOnOffMark(subId, false);
            if (ar.exception != null) {
                this.mVSimController.setMarkForCardReload(subId, false);
            }
            if (isSwapProcess()) {
                doRadioPowerOnForSwap(request);
            } else if (isCrossProcess()) {
                logd("onCardPowerOnDone subId = " + subId);
                if (subId != mainSlot) {
                    this.mModemAdapter.radioPowerOn(this, request, subId);
                } else if (HwVSimUtilsInner.isPlatformRealTripple() && HwVSimUtilsInner.IS_DSDSPOWER_SUPPORT) {
                    this.mModemAdapter.radioPowerOn(this, request, subId);
                } else {
                    request.setPowerOnOffMark(subId, false);
                    logd("m2 subId = " + subId + " set radio power off");
                    this.mVSimController.getPhoneBySub(subId).getServiceStateTracker().setDesiredPowerState(false);
                    if (isAllMarkClear(request)) {
                        enableVSimFinish();
                    }
                }
            }
        }
    }

    protected void onSetNetworkRatAndSrvdomainDone(Message msg) {
        AsyncResult ar = msg.obj;
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 1);
        if (isAsyncResultValid(ar)) {
            this.mModemAdapter.onSetNetworkRatAndSrvdomainDone(this, ar);
            int subId = ar.userObj.mSubId;
            logd("onSetNetworkRatAndSrvdomainDone subId = " + subId);
            this.mModemAdapter.radioPowerOn(this, this.mRequest, subId);
        }
    }

    protected void onRadioPowerOnDone(Message msg) {
        logd("onRadioPowerOnDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 11);
        AsyncResult ar = msg.obj;
        if (isAsyncResultValidForRequestNotSupport(ar)) {
            HwVSimRequest request = ar.userObj;
            int subId = request.mSubId;
            request.setPowerOnOffMark(subId, false);
            logd("onRadioPowerOnDone : subId = " + subId);
            if (HwVSimUtilsInner.isPlatformRealTripple() && HwVSimUtilsInner.IS_DSDSPOWER_SUPPORT && isSwapProcess() && subId != request.getMainSlot()) {
                logd("onRadioPowerOnDone : slave slot not finish!");
            } else if (isAllMarkClear(request)) {
                enableVSimFinish();
            }
        }
    }

    protected void onEnableVSimFinish(Message msg) {
        logd("onEnableVSimFinish");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 13);
        transitionToState(0);
    }

    protected void enableVSimFinish() {
        logd("enableVSimFinish");
        Message onCompleted = this.mVSimController.obtainMessage(57, this.mRequest);
        AsyncResult.forMessage(onCompleted);
        onCompleted.sendToTarget();
    }

    private void doRadioPowerOnForSwap(HwVSimRequest request) {
        int userReservedSubId = this.mVSimController.getUserReservedSubId();
        int subId = request.getMainSlot();
        boolean ulOnlyMode = this.mVSimController.getVSimULOnlyMode();
        int insertedSimCount = this.mVSimController.getInsertedSimCount();
        boolean hasIccCardOnM2 = this.mVSimController.hasIccCardOnM2();
        logd("doRadioPowerOnForSwap subId = " + subId + " userReservedSubId = " + userReservedSubId + " ulOnlyMode = " + ulOnlyMode + " insertedSimCount = " + insertedSimCount + " hasIccCardOnM2 = " + hasIccCardOnM2);
        if (HwVSimUtilsInner.isPlatformRealTripple() || (insertedSimCount != 0 && ((insertedSimCount != 1 || hasIccCardOnM2) && (insertedSimCount != 2 || ulOnlyMode || subId == userReservedSubId)))) {
            this.mModemAdapter.setNetworkRatAndSrvdomain(this, this.mRequest, subId, 1, 0);
            return;
        }
        logd("doCardPowerOnForSwap no need m2 power on from subId = " + subId);
        request.setPowerOnOffMark(subId, false);
        this.mVSimController.getPhoneBySub(subId).getServiceStateTracker().setDesiredPowerState(false);
        enableVSimFinish();
    }

    private void powerOnSlaveRadioOnDSDS() {
        if (this.mRequest != null && HwVSimUtilsInner.isPlatformRealTripple() && HwVSimUtilsInner.IS_DSDSPOWER_SUPPORT && isSwapProcess()) {
            int slaveSlot = this.mRequest.getMainSlot() == 0 ? 1 : 0;
            this.mVSimController.setProhibitSubUpdateSimNoChange(slaveSlot, false);
            this.mModemAdapter.radioPowerOn(this, this.mRequest.clone(), slaveSlot);
        }
    }
}
