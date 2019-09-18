package com.android.internal.telephony.vsim.process;

import android.os.AsyncResult;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.HwTelephonyManagerInner;
import com.android.internal.telephony.HwVSimPhoneFactory;
import com.android.internal.telephony.fullnetwork.HwFullNetworkManager;
import com.android.internal.telephony.uicc.IccCardStatus;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.android.internal.telephony.vsim.HwVSimController;
import com.android.internal.telephony.vsim.HwVSimEventReport;
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
            case 2:
                onGetSimStateDone(msg);
                return true;
            case 41:
                onRadioPowerOffDone(msg);
                return true;
            case 42:
                onCardPowerOffDone(msg);
                return true;
            case 43:
                onSwitchSlotDone(msg);
                return true;
            case HwVSimConstants.EVENT_CARD_POWER_ON_DONE:
                onCardPowerOnDone(msg);
                return true;
            case HwVSimConstants.EVENT_RADIO_POWER_ON_DONE:
                onRadioPowerOnDone(msg);
                return true;
            case HwVSimConstants.EVENT_SET_ACTIVE_MODEM_MODE_DONE:
                onSetActiveModemModeDone(msg);
                return true;
            case HwVSimConstants.EVENT_SET_PREFERRED_NETWORK_TYPE_DONE:
                onSetPreferredNetworkTypeDone(msg);
                return true;
            case HwVSimConstants.EVENT_DISABLE_VSIM_DONE:
                onDisableVSimDone();
                return true;
            case HwVSimConstants.EVENT_GET_ICC_STATUS_DONE:
                onGetIccCardStatusDone(msg);
                return true;
            case HwVSimConstants.EVENT_SET_CDMA_MODE_SIDE_DONE:
                onSetCdmaModeSideDone(msg);
                return true;
            case HwVSimConstants.EVENT_GET_ICC_STATUS_DONE_FOR_GET_CARD_COUNT:
                onGetIccCardStatusForGetCardCountDone(msg);
                return true;
            case HwVSimConstants.EVENT_ICC_STATUS_CHANGED_FOR_CARD_COUNT:
            case HwVSimConstants.EVENT_ICC_STATUS_CHANGED_FOR_CARD_COUNT_TIMEOUT:
                onIccStatusChangedForCardCount(msg);
                return true;
            default:
                return false;
        }
    }

    public void doProcessException(AsyncResult ar, HwVSimRequest request) {
        doDisableProcessException(ar, request);
    }

    /* access modifiers changed from: protected */
    public void logd(String s) {
        HwVSimLog.VSimLogD(LOG_TAG, s);
    }

    /* access modifiers changed from: protected */
    public void onCardPowerOffDone(Message msg) {
        logd("onCardPowerOffDone");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 4);
        AsyncResult ar = (AsyncResult) msg.obj;
        if (isAsyncResultValidNoProcessException(ar)) {
            HwVSimRequest request = (HwVSimRequest) ar.userObj;
            int subId = request.mSubId;
            int subCount = request.getSubCount();
            for (int i = 0; i < subCount; i++) {
                if (subId == request.getSubIdByIndex(i)) {
                    request.setCardOnOffMark(i, false);
                }
            }
            getIccCardStatus(request, subId);
        }
    }

    /* access modifiers changed from: protected */
    public void afterGetAllCardStateDone() {
        logd("afterGetAllCardStateDone -> switch sim slot.");
        int expectSlot = this.mRequest.getExpectSlot();
        logd("restore user switch dual card slot to expectSlot: " + expectSlot);
        if (expectSlot != -1) {
            HwVSimPhoneFactory.setUserSwitchDualCardSlots(expectSlot);
        }
        this.mVSimController.setVSimCurCardType(-1);
        this.mModemAdapter.switchSimSlot(this, this.mRequest);
    }

    /* access modifiers changed from: protected */
    public void onSwitchSlotDone(Message msg) {
        logd("onSwitchSlotDone");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 5);
        AsyncResult ar = (AsyncResult) msg.obj;
        if (isAsyncResultValidForRequestNotSupport(ar)) {
            this.mModemAdapter.onSwitchSlotDone(this, ar);
            this.mVSimController.clearAllMarkForCardReload();
            HwVSimRequest request = (HwVSimRequest) ar.userObj;
            int subId = request.getMainSlot();
            this.mModemAdapter.getRadioCapability();
            if (!HwVSimModemAdapter.IS_FAST_SWITCH_SIMSLOT && request.getIsNeedSwitchCommrilMode()) {
                this.mVSimController.setBlockPinFlag(true);
                this.mVSimController.setBlockPinTable(subId, true);
            }
            if (!HwVSimModemAdapter.IS_FAST_SWITCH_SIMSLOT || !request.getIsNeedSwitchCommrilMode()) {
                restoreSavedNetworkForM0(request);
            } else {
                setCdmaModeSide(msg);
            }
        }
    }

    private void onSetCdmaModeSideDone(Message msg) {
        logd("onSetCdmaModeSideDone");
        AsyncResult ar = (AsyncResult) msg.obj;
        if (isAsyncResultValid(ar)) {
            HwVSimRequest request = (HwVSimRequest) ar.userObj;
            HwVSimController.getInstance().setIsWaitingSwitchCdmaModeSide(false);
            SystemProperties.set("persist.radio.commril_mode", request.getExpectCommrilMode().toString());
            this.mModemAdapter.getRadioCapability();
            restoreSavedNetworkForM0(request);
        }
    }

    private void restoreSavedNetworkForM0(HwVSimRequest request) {
        int subId = request.getExpectSlot();
        int networkModeForM0 = getNetworkTypeOnModem0ForDWork();
        this.mVSimController.setPreferredNetworkTypeDisableFlag(1);
        this.mModemAdapter.setPreferredNetworkType(this, request, subId, networkModeForM0);
    }

    private int getNetworkTypeOnModem0ForDWork() {
        return this.mModemAdapter.restoreSavedNetworkMode(0);
    }

    private int getNetworkTypeOnModem1ForDWork(int slotInM1) {
        int networkModeForM1 = this.mModemAdapter.restoreSavedNetworkMode(1);
        if (!HwVSimUtilsInner.IS_CMCC_4GSWITCH_DISABLE || !HwFullNetworkManager.getInstance().isCMCCHybird()) {
            return networkModeForM1;
        }
        logd("onSetPreferredNetworkTypeDone, slaveSlot " + slotInM1 + " is not cmcc card, so set 3G/2G");
        if (HwTelephonyManagerInner.getDefault().isCDMASimCard(slotInM1)) {
            return 4;
        }
        return 3;
    }

    /* access modifiers changed from: protected */
    public void onSetPreferredNetworkTypeDone(Message msg) {
        logd("onSetPreferredNetworkTypeDone");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 10);
        if (!isAsyncResultValid((AsyncResult) msg.obj)) {
            if (HwVSimModemAdapter.IS_FAST_SWITCH_SIMSLOT) {
                this.mVSimController.setPreferredNetworkTypeDisableFlag(0);
            }
            return;
        }
        int flag = this.mVSimController.getPreferredNetworkTypeDisableFlag();
        logd("onSetPreferredNetworkTypeDone, flag = " + flag);
        int slotInM0 = this.mRequest.getExpectSlot();
        int slotInM1 = HwVSimUtilsInner.getAnotherSlotId(slotInM0);
        switch (flag) {
            case 1:
                this.mModemAdapter.saveNetworkTypeToDB(slotInM0, getNetworkTypeOnModem0ForDWork());
                int networkModeForM1 = getNetworkTypeOnModem1ForDWork(slotInM1);
                logd("onSetPreferredNetworkTypeDone, subId = " + slotInM1 + ", networkMode = " + networkModeForM1);
                this.mModemAdapter.setPreferredNetworkType(this, this.mRequest, slotInM1, networkModeForM1);
                this.mVSimController.setPreferredNetworkTypeDisableFlag(2);
                break;
            case 2:
                if (HwVSimUtilsInner.isDualImsSupported()) {
                    this.mModemAdapter.saveNetworkTypeToDB(slotInM1, getNetworkTypeOnModem1ForDWork(slotInM1));
                }
                cardPowerOn(msg);
                this.mVSimController.setPreferredNetworkTypeDisableFlag(0);
                break;
        }
    }

    private void cardPowerOn(Message msg) {
        HwVSimRequest request = (HwVSimRequest) ((AsyncResult) msg.obj).userObj;
        int subId = request.getMainSlot();
        if (isCrossProcess()) {
            for (int i = 0; i < HwVSimModemAdapter.PHONE_COUNT; i++) {
                int subId2 = request.getSubIdByIndex(i);
                request.setCardOnOffMark(i, true);
                HwVSimRequest cloneRequest = request.clone();
                logd("cardPowerOn:cross subId = " + subId2);
                this.mModemAdapter.cardPowerOn(this, cloneRequest, subId2, 1);
                this.mVSimController.setMarkForCardReload(subId2, true);
            }
        } else if (isSwapProcess() != 0) {
            logd("cardPowerOn:swap subId = " + subId);
            this.mModemAdapter.cardPowerOn(this, this.mRequest, subId, 1);
            this.mVSimController.setMarkForCardReload(subId, true);
        }
    }

    /* access modifiers changed from: protected */
    public void onCardPowerOnDone(Message msg) {
        logd("onCardPowerOnDone");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 7);
        AsyncResult ar = (AsyncResult) msg.obj;
        if (isAsyncResultValidNoProcessException(ar)) {
            HwVSimRequest request = (HwVSimRequest) ar.userObj;
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
            if (HwVSimUtilsInner.isPlatformTwoModems()) {
                this.mModemAdapter.registerIccStatusChangedForGetCardCount(subId, this.mHandler);
                request.setGetIccCardStatusMark(subId, true);
                this.mHandler.sendMessageDelayed(obtainMessage(87, Integer.valueOf(subId)), HwVSimConstants.WAIT_FOR_SIM_STATUS_CHANGED_UNSOL_TIMEOUT);
                if (isSwapProcess()) {
                    int anotherSubId = HwVSimUtilsInner.getAnotherSlotId(subId);
                    request.setGetIccCardStatusMark(anotherSubId, true);
                    getIccCardStatusForGetCardCount(anotherSubId);
                }
            } else if (request.isAllMarkClear()) {
                setActiveModemMode(request);
            }
        }
    }

    private void getIccCardStatusForGetCardCount(int subId) {
        setIccCardStatusRetryTimes(subId, 0);
        this.mModemAdapter.getIccCardStatusForGetCardCount(this, this.mRequest.clone(), subId);
    }

    private void onIccStatusChangedForCardCount(Message msg) {
        int subId = HwVSimUtilsInner.getCiIndex(msg).intValue();
        logd("onIccStatusChangedForCardCount, subId = " + subId);
        this.mModemAdapter.unregisterIccStatusChangedForGetCardCount(subId, this.mHandler);
        this.mHandler.removeMessages(87, Integer.valueOf(subId));
        getIccCardStatusForGetCardCount(subId);
    }

    private void onGetIccCardStatusForGetCardCountDone(Message msg) {
        AsyncResult ar = (AsyncResult) msg.obj;
        if (ar != null) {
            HwVSimRequest request = (HwVSimRequest) ar.userObj;
            if (request != null) {
                int subId = request.mSubId;
                IccCardStatus status = (IccCardStatus) ar.result;
                StringBuilder sb = new StringBuilder();
                sb.append("onGetIccCardStatusForGetCardCountDone:mCardState[");
                sb.append(subId);
                sb.append("]=");
                sb.append(status != null ? status.mCardState : " state is null");
                logd(sb.toString());
                int retryTimes = getIccCardStatusRetryTimes(subId);
                boolean isError = ar.exception != null;
                boolean isCardPresent = !isError && status != null && status.mCardState == IccCardStatus.CardState.CARDSTATE_PRESENT;
                boolean isErrorOrNotReady = isError || !(isCardPresent || (!isError && status != null && status.mCardState == IccCardStatus.CardState.CARDSTATE_ABSENT));
                logd("onGetIccCardStatusForGetCardCountDone: isError=" + isError + ", isCardPresent" + isCardPresent);
                if (isCardPresent) {
                    request.setCardCount(request.getCardCount() + 1);
                }
                logd("onGetIccCardStatusForGetCardCountDone, cardCount=" + request.getCardCount());
                if (!isErrorOrNotReady || retryTimes >= 5) {
                    request.setGetIccCardStatusMark(subId, false);
                    setIccCardStatusRetryTimes(subId, 0);
                    if (isAllMarkClear(request)) {
                        this.mVSimController.setSavedMainSlotAndCardCount(HwVSimPhoneFactory.getVSimSavedMainSlot(), request.getCardCount());
                        setActiveModemMode(request);
                    }
                } else {
                    int retryTimes2 = retryTimes + 1;
                    setIccCardStatusRetryTimes(subId, retryTimes2);
                    logd("onGetIccCardStatusForGetCardCountDone: retry getIccCardStatus,Times=" + retryTimes2);
                    this.mModemAdapter.getIccCardStatusForGetCardCount(this, request, subId);
                }
            }
        }
    }

    private void setActiveModemMode(HwVSimRequest request) {
        this.mModemAdapter.setActiveModemMode(this, request, request.getExpectSlot());
    }

    /* access modifiers changed from: protected */
    public void onSetActiveModemModeDone(Message msg) {
        logd("onSetActiveModemModeDone");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 8);
        AsyncResult ar = (AsyncResult) msg.obj;
        if (isAsyncResultValid(ar)) {
            radioPowerOn((HwVSimRequest) ar.userObj);
        }
    }

    private void radioPowerOn(HwVSimRequest request) {
        int subId = request.getMainSlot();
        if (isCrossProcess()) {
            for (int i = 0; i < HwVSimModemAdapter.PHONE_COUNT; i++) {
                int subId2 = request.getSubIdByIndex(i);
                request.setPowerOnOffMark(i, true);
                HwVSimRequest cloneRequest = request.clone();
                logd("radioPowerOn:cross subId = " + subId2);
                this.mModemAdapter.radioPowerOn(this, cloneRequest, subId2);
            }
        } else if (isSwapProcess() != 0) {
            logd("radioPowerOn:swap subId = " + subId);
            this.mModemAdapter.radioPowerOn(this, request, subId);
        }
    }

    /* access modifiers changed from: protected */
    public void onRadioPowerOnDone(Message msg) {
        logd("onRadioPowerOnDone");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 11);
        AsyncResult ar = (AsyncResult) msg.obj;
        if (isNeedWaitNvCfgMatchAndRestartRild() || isAsyncResultValid(ar)) {
            HwVSimRequest request = (HwVSimRequest) ar.userObj;
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

    private void onDisableVSimDone() {
        logd("onDisableVSimDone");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 14);
        notifyResult(this.mRequest, true);
        transitionToState(7);
    }

    private void disableVSimDone() {
        logd("disableVSimDone");
        Message onCompleted = obtainMessage(53, this.mRequest);
        AsyncResult.forMessage(onCompleted);
        onCompleted.sendToTarget();
    }

    /* access modifiers changed from: protected */
    public boolean isNeedWaitNvCfgMatchAndRestartRild() {
        return HwVSimUtilsInner.isPlatformNeedWaitNvMatchUnsol() && HwVSimController.getInstance().getInsertedCardCount() != 0;
    }
}
