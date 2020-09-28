package com.android.internal.telephony.vsim.process;

import android.os.Message;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.android.internal.telephony.vsim.HwVSimController;
import com.android.internal.telephony.vsim.HwVSimEventReport;
import com.android.internal.telephony.vsim.HwVSimLog;
import com.android.internal.telephony.vsim.HwVSimModemAdapter;
import com.android.internal.telephony.vsim.HwVSimRequest;
import com.huawei.android.os.AsyncResultEx;

public class HwVSimSReadyProcessor extends HwVSimEReadyProcessor {
    public static final String LOG_TAG = "HwVSimSReadyProcessor";

    public HwVSimSReadyProcessor(HwVSimController controller, HwVSimModemAdapter modemAdapter, HwVSimRequest request) {
        super(controller, modemAdapter, request);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.vsim.process.HwVSimReadyProcessor, com.android.internal.telephony.vsim.process.HwVSimProcessor, com.android.internal.telephony.vsim.process.HwVSimEReadyProcessor
    public void logd(String s) {
        HwVSimLog.VSimLogD(LOG_TAG, s);
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimReadyProcessor, com.android.internal.telephony.vsim.process.HwVSimProcessor, com.android.internal.telephony.vsim.process.HwVSimEReadyProcessor
    public boolean processMessage(Message msg) {
        switch (msg.what) {
            case HwVSimConstants.EVENT_CARD_POWER_ON_DONE:
                onCardPowerOnDone(msg);
                return true;
            case HwVSimConstants.EVENT_RADIO_POWER_ON_DONE:
                onRadioPowerOnDone(msg);
                return true;
            case HwVSimConstants.EVENT_NETWORK_CONNECTED:
                onNetworkConnected();
                return true;
            case HwVSimConstants.EVENT_SWITCH_WORKMODE_FINISH:
                onSwitchWorkModeFinish();
                return true;
            case 65:
                onPlmnSelInfoDone(msg);
                return true;
            case HwVSimConstants.EVENT_NETWORK_CONNECT_TIMEOUT:
                onNetworkConnectTimeout();
                return true;
            default:
                return false;
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.vsim.process.HwVSimEReadyProcessor
    public void afterNetwork() {
        logd("afterNetwork");
        if (this.mHasEnterAfterNetwork) {
            logd("can not enter more than once.");
            return;
        }
        this.mHasEnterAfterNetwork = true;
        int mainSlot = this.mRequest.getMainSlot();
        int slaveSlot = mainSlot == 0 ? 1 : 0;
        HwVSimRequest slaveRequest = this.mRequest.clone();
        if (slaveRequest != null) {
            slaveRequest.setPowerOnOffMark(slaveSlot, true);
            slaveRequest.setCardOnOffMark(slaveSlot, true);
            this.mModemAdapter.cardPowerOn(this, slaveRequest, slaveSlot, 1);
        }
        int mainIndex = 1;
        if (mainSlot == 2) {
            mainIndex = 11;
        }
        HwVSimRequest mainRequest = this.mRequest.clone();
        if (mainRequest != null) {
            mainRequest.setPowerOnOffMark(mainSlot, true);
            mainRequest.setCardOnOffMark(mainSlot, true);
            this.mModemAdapter.cardPowerOn(this, mainRequest, mainSlot, mainIndex);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.vsim.process.HwVSimEReadyProcessor
    public void onCardPowerOnDone(Message msg) {
        logd("onCardPowerOnDone");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 7);
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (isAsyncResultValidNoProcessException(ar)) {
            HwVSimRequest request = (HwVSimRequest) ar.getUserObj();
            int subId = request.mSubId;
            int mainSlot = request.getMainSlot();
            request.setCardOnOffMark(subId, false);
            logd("onCardPowerOnDone subId = " + subId + " mainSlot = " + mainSlot);
            if (subId == mainSlot) {
                HwVSimController.WorkModeParam param = getWorkModeParam(this.mRequest);
                if (param == null) {
                    doSwitchModeProcessException(null, this.mRequest);
                    return;
                } else if (param.workMode != 2) {
                    request.setPowerOnOffMark(subId, false);
                    logd("m2 subId = " + subId + "set radio power off");
                    this.mVSimController.getPhoneBySub(subId).getServiceStateTracker().setDesiredPowerState(false);
                } else if (ar.getException() != null) {
                    this.mModemAdapter.radioPowerOn(null, null, subId);
                    request.setPowerOnOffMark(subId, false);
                } else {
                    this.mModemAdapter.radioPowerOn(this, request, subId);
                }
            } else if (ar.getException() != null) {
                this.mModemAdapter.radioPowerOn(null, null, subId);
                request.setPowerOnOffMark(subId, false);
            } else {
                this.mModemAdapter.radioPowerOn(this, request, subId);
            }
            if (isAllMarkClear(request)) {
                switchWorkModeFinish();
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.vsim.process.HwVSimEReadyProcessor
    public void onRadioPowerOnDone(Message msg) {
        logd("onRadioPowerOnDone");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 11);
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (isAsyncResultValidForRequestNotSupport(ar)) {
            HwVSimRequest request = (HwVSimRequest) ar.getUserObj();
            int subId = request.mSubId;
            request.setPowerOnOffMark(subId, false);
            logd("onRadioPowerOnDone subId = " + subId);
            if (isAllMarkClear(request)) {
                switchWorkModeFinish();
            }
        }
    }

    private void onSwitchWorkModeFinish() {
        logd("onSwitchWorkModeFinish");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 1);
        transitionToState(0);
    }

    private void switchWorkModeFinish() {
        logd("switchWorkModeFinish");
        if (isAllMarkClear(this.mRequest)) {
            Message onCompleted = this.mVSimController.obtainMessage(60, this.mRequest);
            AsyncResultEx.forMessage(onCompleted);
            onCompleted.sendToTarget();
        }
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimReadyProcessor, com.android.internal.telephony.vsim.process.HwVSimProcessor, com.android.internal.telephony.vsim.process.HwVSimEReadyProcessor
    public void doProcessException(AsyncResultEx ar, HwVSimRequest request) {
        doSwitchModeProcessException(ar, request);
    }

    private HwVSimController.WorkModeParam getWorkModeParam(HwVSimRequest request) {
        if (this.mVSimController == null) {
            return null;
        }
        return this.mVSimController.getWorkModeParam(request);
    }
}
