package com.android.internal.telephony.vsim.process;

import android.os.Message;
import android.telephony.HwTelephonyManagerInner;
import com.android.internal.telephony.HwVSimPhoneFactory;
import com.android.internal.telephony.fullnetwork.HwFullNetworkManager;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.android.internal.telephony.vsim.HwVSimController;
import com.android.internal.telephony.vsim.HwVSimEventReport;
import com.android.internal.telephony.vsim.HwVSimLog;
import com.android.internal.telephony.vsim.HwVSimModemAdapter;
import com.android.internal.telephony.vsim.HwVSimRequest;
import com.android.internal.telephony.vsim.HwVSimUtilsInner;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.internal.telephony.uicc.IccCardStatusExt;

public class HwVSimDWorkProcessor extends HwVSimWorkProcessor {
    public static final String LOG_TAG = "VSimDWorkProcessor";

    public HwVSimDWorkProcessor(HwVSimController controller, HwVSimModemAdapter modemAdapter, HwVSimRequest request) {
        super(controller, modemAdapter, request);
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public boolean processMessage(Message msg) {
        switch (msg.what) {
            case 2:
                onGetSimStateDone(msg);
                return true;
            case HwVSimConstants.EVENT_RADIO_POWER_OFF_DONE:
                onRadioPowerOffDone(msg);
                return true;
            case HwVSimConstants.EVENT_CARD_POWER_OFF_DONE:
                onCardPowerOffDone(msg);
                return true;
            case HwVSimConstants.EVENT_SWITCH_SLOT_DONE:
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

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public void doProcessException(AsyncResultEx ar, HwVSimRequest request) {
        doDisableProcessException(ar, request);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor, com.android.internal.telephony.vsim.process.HwVSimWorkProcessor
    public void logd(String s) {
        HwVSimLog.VSimLogD(LOG_TAG, s);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.vsim.process.HwVSimWorkProcessor
    public void onCardPowerOffDone(Message msg) {
        logd("onCardPowerOffDone");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 4);
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (isAsyncResultValidNoProcessException(ar)) {
            HwVSimRequest request = (HwVSimRequest) ar.getUserObj();
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
    @Override // com.android.internal.telephony.vsim.process.HwVSimWorkProcessor
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
    @Override // com.android.internal.telephony.vsim.process.HwVSimWorkProcessor
    public void onSwitchSlotDone(Message msg) {
        logd("onSwitchSlotDone");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 5);
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (isAsyncResultValidForRequestNotSupport(ar)) {
            this.mModemAdapter.onSwitchSlotDone(this, ar);
            this.mVSimController.clearAllMarkForCardReload();
            HwVSimRequest request = (HwVSimRequest) ar.getUserObj();
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
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (isAsyncResultValid(ar)) {
            HwVSimRequest request = (HwVSimRequest) ar.getUserObj();
            HwVSimController.getInstance().setIsWaitingSwitchCdmaModeSide(false);
            SystemPropertiesEx.set(HwVSimModemAdapter.PROPERTY_COMMRIL_MODE, request.getExpectCommrilMode().toString());
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

    private int getNetworkTypeOnModem1ForDWork(int slotInM1, int networkModeForM0) {
        logd("getNetworkTypeOnModem1ForDWork, sslotInM1:" + slotInM1 + ", networkModeForM0:" + networkModeForM0);
        int networkModeForM1 = this.mModemAdapter.restoreSavedNetworkMode(1);
        if (HwFullNetworkManager.getInstance().isCMCCDsdxDisable() && HwFullNetworkManager.getInstance().isCMCCHybird()) {
            logd("onSetPreferredNetworkTypeDone, slaveSlot " + slotInM1 + " is not cmcc card, so set 3G/2G");
            networkModeForM1 = HwTelephonyManagerInner.getDefault().isCDMASimCard(slotInM1) ? 4 : 3;
        }
        logd("getNetworkTypeOnModem1ForDWork, networkModeForM1:" + networkModeForM1);
        if (!HwVSimUtilsInner.isNrServiceAbilityOn(networkModeForM0) || networkModeForM1 != 9) {
            return networkModeForM1;
        }
        return 65;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.vsim.process.HwVSimWorkProcessor
    public void onSetPreferredNetworkTypeDone(Message msg) {
        logd("onSetPreferredNetworkTypeDone");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 10);
        if (isAsyncResultValid(AsyncResultEx.from(msg.obj))) {
            int flag = this.mVSimController.getPreferredNetworkTypeDisableFlag();
            logd("onSetPreferredNetworkTypeDone, flag = " + flag);
            int slotInM0 = this.mRequest.getExpectSlot();
            int slotInM1 = HwVSimUtilsInner.getAnotherSlotId(slotInM0);
            int networkModeForM0 = getNetworkTypeOnModem0ForDWork();
            switch (flag) {
                case 1:
                    this.mModemAdapter.saveNetworkTypeToDB(slotInM0, networkModeForM0);
                    int networkModeForM1 = getNetworkTypeOnModem1ForDWork(slotInM1, networkModeForM0);
                    logd("onSetPreferredNetworkTypeDone, subId = " + slotInM1 + ", networkMode = " + networkModeForM1);
                    this.mModemAdapter.setPreferredNetworkType(this, this.mRequest, slotInM1, networkModeForM1);
                    this.mVSimController.setPreferredNetworkTypeDisableFlag(2);
                    return;
                case 2:
                    if (HwVSimUtilsInner.isDualImsSupported()) {
                        int networkModeForM12 = getNetworkTypeOnModem1ForDWork(slotInM1, networkModeForM0);
                        if (networkModeForM12 == 65) {
                            logd("onSetPreferredNetworkTypeDone, set 9 to db for m1.");
                            networkModeForM12 = 9;
                        }
                        this.mModemAdapter.saveNetworkTypeToDB(slotInM1, networkModeForM12);
                    }
                    cardPowerOn(msg);
                    this.mVSimController.setPreferredNetworkTypeDisableFlag(0);
                    return;
                default:
                    return;
            }
        } else if (HwVSimModemAdapter.IS_FAST_SWITCH_SIMSLOT) {
            this.mVSimController.setPreferredNetworkTypeDisableFlag(0);
        }
    }

    private void cardPowerOn(Message msg) {
        HwVSimRequest request = (HwVSimRequest) AsyncResultEx.from(msg.obj).getUserObj();
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
        } else if (isSwapProcess()) {
            logd("cardPowerOn:swap subId = " + subId);
            this.mModemAdapter.cardPowerOn(this, this.mRequest, subId, 1);
            this.mVSimController.setMarkForCardReload(subId, true);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.vsim.process.HwVSimWorkProcessor
    public void onCardPowerOnDone(Message msg) {
        logd("onCardPowerOnDone");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 7);
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (isAsyncResultValidNoProcessException(ar)) {
            HwVSimRequest request = (HwVSimRequest) ar.getUserObj();
            int subId = request.mSubId;
            int subCount = request.getSubCount();
            for (int i = 0; i < subCount; i++) {
                if (subId == request.getSubIdByIndex(i)) {
                    request.setCardOnOffMark(i, false);
                }
            }
            if (ar.getException() != null) {
                this.mVSimController.setMarkForCardReload(subId, false);
            }
            if (HwVSimUtilsInner.isPlatformTwoModems()) {
                this.mModemAdapter.registerIccStatusChangedForGetCardCount(subId, this.mHandler);
                request.setGetIccCardStatusMark(subId, true);
                this.mHandler.sendMessageDelayed(obtainMessage(87, Integer.valueOf(subId)), 5000);
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
        HwVSimRequest request;
        boolean isError;
        boolean isCardPresent;
        boolean isCardAbsent;
        boolean isCardReady;
        boolean isErrorOrNotReady = true;
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (ar != null && (request = (HwVSimRequest) ar.getUserObj()) != null) {
            int subId = request.mSubId;
            IccCardStatusExt status = IccCardStatusExt.from(ar.getResult());
            logd("onGetIccCardStatusForGetCardCountDone:mCardState[" + subId + "]=" + (status != null ? status.getCardState() : " state is null"));
            int retryTimes = getIccCardStatusRetryTimes(subId);
            if (ar.getException() != null) {
                isError = true;
            } else {
                isError = false;
            }
            if (isError || status == null || status.getCardState() != IccCardStatusExt.CardStateEx.CARDSTATE_PRESENT) {
                isCardPresent = false;
            } else {
                isCardPresent = true;
            }
            if (isError || status == null || status.getCardState() != IccCardStatusExt.CardStateEx.CARDSTATE_ABSENT) {
                isCardAbsent = false;
            } else {
                isCardAbsent = true;
            }
            if (isCardPresent || isCardAbsent) {
                isCardReady = true;
            } else {
                isCardReady = false;
            }
            if (!isError && isCardReady) {
                isErrorOrNotReady = false;
            }
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
                    return;
                }
                return;
            }
            int retryTimes2 = retryTimes + 1;
            setIccCardStatusRetryTimes(subId, retryTimes2);
            logd("onGetIccCardStatusForGetCardCountDone: retry getIccCardStatus,Times=" + retryTimes2);
            this.mModemAdapter.getIccCardStatusForGetCardCount(this, request, subId);
        }
    }

    private void setActiveModemMode(HwVSimRequest request) {
        this.mModemAdapter.setActiveModemMode(this, request, request.getExpectSlot());
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.vsim.process.HwVSimWorkProcessor
    public void onSetActiveModemModeDone(Message msg) {
        logd("onSetActiveModemModeDone");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 8);
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (isAsyncResultValid(ar)) {
            radioPowerOn((HwVSimRequest) ar.getUserObj());
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
        } else if (isSwapProcess()) {
            logd("radioPowerOn:swap subId = " + subId);
            this.mModemAdapter.radioPowerOn(this, request, subId);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.vsim.process.HwVSimWorkProcessor
    public void onRadioPowerOnDone(Message msg) {
        logd("onRadioPowerOnDone");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 11);
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (isNeedWaitNvCfgMatchAndRestartRild() || isAsyncResultValid(ar)) {
            HwVSimRequest request = (HwVSimRequest) ar.getUserObj();
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
        AsyncResultEx.forMessage(onCompleted);
        onCompleted.sendToTarget();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public boolean isNeedWaitNvCfgMatchAndRestartRild() {
        return HwVSimUtilsInner.isPlatformNeedWaitNvMatchUnsol() && HwVSimController.getInstance().getInsertedCardCount() != 0;
    }
}
