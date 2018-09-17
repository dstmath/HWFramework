package com.android.internal.telephony.vsim.process;

import android.os.AsyncResult;
import android.os.Message;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.android.internal.telephony.vsim.HwVSimController;
import com.android.internal.telephony.vsim.HwVSimEventReport.VSimEventInfoUtils;
import com.android.internal.telephony.vsim.HwVSimLog;
import com.android.internal.telephony.vsim.HwVSimModemAdapter;
import com.android.internal.telephony.vsim.HwVSimRequest;
import com.android.internal.telephony.vsim.HwVSimUtilsInner;

public class HwVSimDWorkProcessor extends HwVSimWorkProcessor {
    public static final String LOG_TAG = "VSimDWorkProcessor";

    public HwVSimDWorkProcessor(HwVSimController controller, HwVSimModemAdapter modemAdapter, HwVSimRequest request) {
        super(controller, modemAdapter, request);
    }

    public boolean processMessage(Message msg) {
        switch (msg.what) {
            case HwVSimUtilsInner.STATE_EB /*2*/:
                onGetSimStateDone(msg);
                return true;
            case HwVSimConstants.EVENT_RADIO_POWER_OFF_DONE /*41*/:
                onRadioPowerOffDone(msg);
                return true;
            case HwVSimConstants.EVENT_CARD_POWER_OFF_DONE /*42*/:
                onCardPowerOffDone(msg);
                return true;
            case HwVSimConstants.EVENT_SWITCH_SLOT_DONE /*43*/:
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
            case HwVSimConstants.EVENT_DISABLE_VSIM_DONE /*53*/:
                onDisableVSimDone(msg);
                return true;
            case HwVSimConstants.EVENT_GET_ICC_STATUS_DONE /*79*/:
                onGetIccCardStatusDone(msg);
                return true;
            default:
                return false;
        }
    }

    public void doProcessException(AsyncResult ar, HwVSimRequest request) {
        doDisableProcessException(ar, request);
    }

    protected void logd(String s) {
        HwVSimLog.VSimLogD(LOG_TAG, s);
    }

    protected void onCardPowerOffDone(Message msg) {
        logd("onCardPowerOffDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 4);
        AsyncResult ar = msg.obj;
        if (isAsyncResultValidForRequestNotSupport(ar)) {
            HwVSimRequest request = ar.userObj;
            int subId = request.mSubId;
            int subCount = request.getSubCount();
            for (int i = 0; i < subCount; i++) {
                if (subId == request.getSubIdByIndex(i)) {
                    request.setCardOnOffMark(i, false);
                }
            }
            if (subId != 2) {
                logd("dispose uicc card: " + subId);
                this.mVSimController.disposeCard(subId);
            }
            getIccCardStatus(request, subId);
        }
    }

    protected void afterGetAllCardStateDone() {
        if (isCrossProcess()) {
            int savedMainSlot = this.mVSimController.getVSimSavedMainSlot();
            logd("restore user switch dual card slot to savedMainSlot: " + savedMainSlot);
            if (savedMainSlot != -1) {
                this.mVSimController.setUserSwitchDualCardSlots(savedMainSlot);
            }
        }
        logd("onCardPowerOffDone:isAllMarkClear");
        this.mVSimController.setVSimCurCardType(-1);
        if (HwVSimUtilsInner.isPlatformRealTripple()) {
            this.mModemAdapter.setActiveModemMode(this, this.mRequest, 2);
            return;
        }
        if (this.mVSimController.clearVsimToTA() != 0) {
            logd("warning, clear vsim to TA not success");
        }
        this.mModemAdapter.setTeeDataReady(this, this.mRequest, 2);
    }

    protected void onSetTeeDataReadyDone(Message msg) {
        logd("onSetTeeDataReadyDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 6);
        AsyncResult ar = msg.obj;
        if (isAsyncResultValid(ar)) {
            this.mModemAdapter.setActiveModemMode(this, this.mRequest, ar.userObj.mSubId);
        }
    }

    protected void onSetActiveModemModeDone(Message msg) {
        logd("onSetActiveModemModeDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 8);
        AsyncResult ar = msg.obj;
        if (isAsyncResultValid(ar)) {
            int networkMode = this.mModemAdapter.restoreSavedNetworkMode(this);
            this.mModemAdapter.setPreferredNetworkType(this, this.mRequest, ar.userObj.mSubId, networkMode);
        }
    }

    protected void onSetPreferredNetworkTypeDone(Message msg) {
        logd("onSetPreferredNetworkTypeDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 10);
        if (isAsyncResultValid(msg.obj)) {
            this.mModemAdapter.switchSimSlot(this, this.mRequest);
        }
    }

    protected void onSwitchSlotDone(Message msg) {
        logd("onSwitchSlotDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 5);
        AsyncResult ar = msg.obj;
        if (isAsyncResultValidForRequestNotSupport(ar) && this.mModemAdapter.isDoneAllSwitchSlot(this, ar)) {
            this.mModemAdapter.onSwitchSlotDone(this, ar);
            this.mVSimController.clearAllMarkForCardReload();
            HwVSimRequest request = ar.userObj;
            int subId = request.getMainSlot();
            if (request.getIsNeedSwitchCommrilMode()) {
                this.mVSimController.setBlockPinFlag(true);
                this.mVSimController.setBlockPinTable(subId, true);
            }
            if (isCrossProcess()) {
                for (int i = 0; i < HwVSimModemAdapter.PHONE_COUNT; i++) {
                    subId = request.getSubIdByIndex(i);
                    request.setCardOnOffMark(i, true);
                    request.setPowerOnOffMark(i, true);
                    HwVSimRequest cloneRequest = request.clone();
                    logd("onSwitchSlotDone:cross subId = " + subId);
                    this.mModemAdapter.cardPowerOn(this, cloneRequest, subId, 1);
                    this.mVSimController.setMarkForCardReload(subId, true);
                }
            } else if (isSwapProcess()) {
                logd("onSwitchSlotDone:swap subId = " + subId);
                this.mModemAdapter.cardPowerOn(this, this.mRequest, subId, 1);
                this.mVSimController.setMarkForCardReload(subId, true);
            }
        }
    }

    protected void onCardPowerOnDone(Message msg) {
        logd("onCardPowerOnDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 7);
        AsyncResult ar = msg.obj;
        if (isAsyncResultValidNoProcessException(ar)) {
            HwVSimRequest request = ar.userObj;
            int subId = request.mSubId;
            int subCount = request.getSubCount();
            for (int i = 0; i < subCount; i++) {
                if (subId == request.getSubIdByIndex(i)) {
                    request.setCardOnOffMark(i, false);
                }
            }
            if (ar.exception != null) {
                this.mVSimController.setMarkForCardReload(subId, false);
            }
            this.mModemAdapter.radioPowerOn(this, request, subId);
        }
    }

    protected void onRadioPowerOnDone(Message msg) {
        logd("onRadioPowerOnDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 11);
        AsyncResult ar = msg.obj;
        if (isAsyncResultValid(ar)) {
            HwVSimRequest request = ar.userObj;
            int subId = request.mSubId;
            int subCount = request.getSubCount();
            for (int i = 0; i < subCount; i++) {
                if (subId == request.getSubIdByIndex(i)) {
                    request.setPowerOnOffMark(i, false);
                }
            }
            if (isAllMarkClear(request)) {
                logd("onRadioPowerOnDone:isAllMarkClear");
                disableVSimDone();
            }
        }
    }

    protected void onDisableVSimDone(Message msg) {
        logd("onDisableVSimDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 14);
        notifyResult(this.mRequest, Boolean.valueOf(true));
        transitionToState(7);
    }

    protected void disableVSimDone() {
        logd("disableVSimDone");
        Message onCompleted = obtainMessage(53, this.mRequest);
        AsyncResult.forMessage(onCompleted);
        onCompleted.sendToTarget();
    }
}
