package com.huawei.internal.telephony.vsim.process;

import android.os.Message;
import android.telephony.HwTelephonyManager;
import android.telephony.HwTelephonyManagerInner;
import com.android.internal.telephony.HwVSimPhoneFactory;
import com.android.internal.telephony.fullnetwork.HwFullNetworkManager;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.android.internal.telephony.vsim.HwVSimEventReport;
import com.android.internal.telephony.vsim.HwVSimLog;
import com.android.internal.telephony.vsim.HwVSimModemAdapter;
import com.android.internal.telephony.vsim.HwVSimRequest;
import com.android.internal.telephony.vsim.HwVSimUtilsInner;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.internal.telephony.uicc.IccCardStatusExt;
import com.huawei.internal.telephony.vsim.HwVSimControllerGetter;
import com.huawei.internal.telephony.vsim.HwVSimMtkController;
import com.huawei.internal.telephony.vsim.HwVSimMtkDualModem;
import java.util.Arrays;
import java.util.Optional;

public class HwVSimMtkDisableProcessor extends HwVSimMtkProcessor {
    private static final int GET_ICC_CARD_STATUS_RETRY_TIMES = 5;
    private static final String LOG_TAG = "HwVSimMtkDisableProcessor";
    private int[] mGetIccCardStatusTimes;
    private IccCardStatusExt.CardStateEx[] mIccCardStatus;

    public HwVSimMtkDisableProcessor(HwVSimMtkController controller, HwVSimModemAdapter modemAdapter, HwVSimRequest request) {
        super(controller, modemAdapter, request);
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public void onEnter() {
        logi("onEnter");
        cmdSemRelease();
        if (this.mRequest == null) {
            transitionToState(0);
            return;
        }
        mockGetSimSlot(this.mRequest);
        if (!this.mRequest.getIsVSimOnM0()) {
            logi("vsim is already closed, return true.");
            notifyResult(this.mRequest, true);
            transitionToState(0);
            return;
        }
        this.mController.setProcessAction(HwVSimConstants.ProcessAction.PROCESS_ACTION_DISABLE);
        this.mRequest.createGotCardType(HwVSimModemAdapter.PHONE_COUNT);
        this.mRequest.createCardTypes(HwVSimModemAdapter.PHONE_COUNT);
        if (this.mController.cardTypeValid()) {
            System.arraycopy(this.mController.getCardTypes(), 0, this.mRequest.getCardTypes(), 0, HwVSimModemAdapter.PHONE_COUNT);
            logi("request cardTypes:" + Arrays.toString(this.mRequest.getCardTypes()));
            for (int i = 0; i < HwVSimModemAdapter.PHONE_COUNT; i++) {
                this.mRequest.setGotCardType(i, true);
            }
            this.mModemAdapter.checkDisableSimCondition(this, this.mRequest);
            restoreSavedNetwork(this.mRequest);
            return;
        }
        this.mModemAdapter.getAllCardTypes(this, this.mRequest);
    }

    private void mockGetSimSlot(HwVSimRequest request) {
        if (this.mModemAdapter instanceof HwVSimMtkDualModem) {
            int mainSlot = HwTelephonyManager.getDefault().getDefault4GSlotId();
            int secondarySlot = HwVSimUtilsInner.getAnotherSlotId(mainSlot);
            int vSimSlotId = HwVSimPhoneFactory.getVSimEnabledSubId();
            boolean isVSimOnModem0 = HwVSimControllerGetter.get().isVSimOn();
            int[] slots = {mainSlot, secondarySlot};
            int[] responseSlots = new int[HwVSimModemAdapter.MAX_SUB_COUNT];
            if (HwVSimPhoneFactory.getVSimSavedMainSlot() == -1) {
                logi("mockGetSimSlot, vsim saved main slot is invalid.");
                isVSimOnModem0 = false;
            }
            if (isVSimOnModem0) {
                responseSlots[0] = 2;
                responseSlots[1] = HwVSimUtilsInner.getAnotherSlotId(vSimSlotId);
                responseSlots[2] = vSimSlotId;
                mainSlot = vSimSlotId;
            } else {
                responseSlots[0] = slots[0];
                responseSlots[1] = slots[1];
                responseSlots[2] = 2;
            }
            ((HwVSimMtkDualModem) this.mModemAdapter).afterGetSimSlot(request, mainSlot, isVSimOnModem0, slots, responseSlots);
        }
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public void onExit() {
        logi("onExit");
        cleanMainSlotIfDisableSucceed();
    }

    private void cleanMainSlotIfDisableSucceed() {
        boolean iResult = false;
        if (this.mRequest != null) {
            Object oResult = this.mRequest.getResult();
            if (oResult instanceof Boolean) {
                iResult = ((Boolean) oResult).booleanValue();
            }
        }
        if (iResult) {
            this.mController.setVSimSavedMainSlot(-1);
            HwVSimPhoneFactory.setVSimSavedNetworkMode(0, -1);
            HwVSimPhoneFactory.setVSimSavedNetworkMode(1, -1);
            return;
        }
        logd("leave saved main slot untouched");
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public void transitionToState(int state) {
        this.mController.transitionToState(state);
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public boolean processMessage(Message msg) {
        int i = msg.what;
        if (i == 49) {
            onSetPreferredNetworkTypeDone(msg);
            return true;
        } else if (i == 56) {
            onQueryCardTypeDone(msg);
            return true;
        } else if (i == 100) {
            onNotifyPlugOutDone(msg);
            return true;
        } else if (i == 102) {
            onSetMainSlotDone(msg);
            return true;
        } else if (i == 97) {
            onNotifyPlugInDone(msg);
            return true;
        } else if (i != 98) {
            switch (i) {
                case HwVSimConstants.EVENT_GET_ICC_STATUS_DONE_FOR_GET_CARD_COUNT /* 85 */:
                    onGetIccCardStatusForGetCardCountDone(msg);
                    return true;
                case HwVSimConstants.EVENT_ICC_STATUS_CHANGED_FOR_CARD_COUNT /* 86 */:
                case HwVSimConstants.EVENT_ICC_STATUS_CHANGED_FOR_CARD_COUNT_TIMEOUT /* 87 */:
                    onIccStatusChangedForCardCount(msg);
                    return true;
                default:
                    return false;
            }
        } else {
            onDisableVSimExternalDone(msg);
            return true;
        }
    }

    private void onQueryCardTypeDone(Message msg) {
        logi("onQueryCardTypeDone");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mController.getVSimEventInfo(), 1);
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (isAsyncResultValid(ar)) {
            this.mModemAdapter.onQueryCardTypeDone(this, ar);
            if (this.mRequest.isGotAllCardTypes()) {
                this.mModemAdapter.checkDisableSimCondition(this, this.mRequest);
                restoreSavedNetwork(this.mRequest);
            }
        }
    }

    private void restoreSavedNetwork(HwVSimRequest request) {
        int subId = request.getExpectSlot();
        int networkTypeModem0 = getNetworkTypeOnModem0();
        this.mController.setPreferredNetworkTypeDisableFlag(1);
        this.mModemAdapter.setPreferredNetworkType(this, request, subId, networkTypeModem0);
        logi("restoreSavedNetwork request slotInM0 = " + subId + ", networkTypeModem0 = " + networkTypeModem0);
    }

    private void onSetPreferredNetworkTypeDone(Message msg) {
        logi("onSetPreferredNetworkTypeDone");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mController.getVSimEventInfo(), 10);
        if (isAsyncResultValid(AsyncResultEx.from(msg.obj))) {
            int flag = this.mController.getPreferredNetworkTypeDisableFlag();
            int slotInM0 = this.mRequest.getExpectSlot();
            int slotInM1 = HwVSimUtilsInner.getAnotherSlotId(slotInM0);
            int networkTypeModem0 = getNetworkTypeOnModem0();
            int networkTypeModem1 = getNetworkTypeOnModem1();
            if (flag == 1) {
                this.mModemAdapter.saveNetworkTypeToDB(slotInM0, networkTypeModem0);
                logi("onSetPreferredNetworkTypeDone main slotInM0 = " + slotInM0 + ", networkModeM0 = " + networkTypeModem0);
                logi("onSetPreferredNetworkTypeDone request slotInM1 = " + slotInM1 + ", networkModeM1 = " + networkTypeModem1);
                this.mModemAdapter.setPreferredNetworkType(this, this.mRequest, slotInM1, networkTypeModem1);
                this.mController.setPreferredNetworkTypeDisableFlag(2);
            } else if (flag == 2) {
                logi("onSetPreferredNetworkTypeDone slave subId = " + slotInM1 + ", networkMode = " + networkTypeModem1);
                this.mModemAdapter.saveNetworkTypeToDB(slotInM1, networkTypeModem1);
                this.mController.setPreferredNetworkTypeDisableFlag(0);
                cmdPlugOut(msg);
            }
        }
    }

    private int getNetworkTypeOnModem0() {
        return this.mModemAdapter.restoreSavedNetworkMode(0);
    }

    private int getNetworkTypeOnModem1() {
        return this.mModemAdapter.restoreSavedNetworkMode(1);
    }

    private void cmdPlugOut(Message msg) {
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (!isAsyncResultValid(ar)) {
            logi("cmdPlugOut cancel with exception");
            return;
        }
        HwVSimRequest hwVSimRequest = (HwVSimRequest) ar.getUserObj();
        int slotId = this.mController.getVsimSlotId();
        logi("cmdPlugOut slotId:" + slotId);
        if (this.mController.sendVsimEvent(slotId, 3, null, obtainMessage(100, this.mRequest)) == -1) {
            logi("cmdPlugOut fail, ExternalSimManagerEx null");
            notifyResult(this.mRequest, false);
            transitionToState(0);
        }
    }

    private void onNotifyPlugOutDone(Message msg) {
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        logi("PlugOutDone -> CmdDisableVSim");
        this.mController.handleMessageDone(msg.arg1);
        cmdDisableVSim(ar);
    }

    private void cmdDisableVSim(AsyncResultEx ar) {
        if (!isAsyncResultValid(ar)) {
            logi("cmdDisableVSim cancel with exception");
            return;
        }
        this.mController.setIsVSimOn(false);
        HwVSimRequest hwVSimRequest = (HwVSimRequest) ar.getUserObj();
        int slotId = this.mController.getVsimSlotId();
        logi("cmdDisableVSim slotId:" + slotId);
        if (this.mController.sendVsimEvent(slotId, 2, null, obtainMessage(98, this.mRequest)) == -1) {
            logi("onCmdDisableVSim fail, ExternalSimManagerEx null");
            notifyResult(this.mRequest, false);
            transitionToState(0);
        }
    }

    private void onDisableVSimExternalDone(Message msg) {
        logi("onDisableVSimExternalDone");
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        this.mController.handleMessageDone(msg.arg1);
        if (isAsyncResultValid(ar)) {
            this.mController.setVSimCurCardType(-1);
            Message response = obtainMessage(97, (HwVSimRequest) ar.getUserObj());
            int slotId = this.mController.getVsimSlotId();
            logi("onDisableVSimExternalDone slotId:" + slotId);
            if (this.mController.sendVsimEvent(slotId, 4, null, response) == -1) {
                logi("onDisableVSimExternalDone fail, ExternalSimManagerEx null");
                notifyResult(this.mRequest, false);
                transitionToState(0);
            }
        }
    }

    private void onNotifyPlugInDone(Message msg) {
        logi("onNotifyPlugInDone");
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        this.mController.handleMessageDone(msg.arg1);
        if (isAsyncResultValid(ar)) {
            this.mController.restoreSavedMobileDataEnableState();
            queryIccStatus();
        }
    }

    private void queryIccStatus() {
        int[] subs = new int[HwVSimModemAdapter.PHONE_COUNT];
        int index = 0;
        int[] simSlotTable = this.mController.getSimSlotTable();
        for (int slot : simSlotTable) {
            if (slot != 2 && index < HwVSimModemAdapter.PHONE_COUNT) {
                subs[index] = slot;
                index++;
            }
        }
        logi("queryIccStatus subs:" + Arrays.toString(subs));
        this.mRequest.setSubs(subs);
        this.mRequest.createGetIccCardStatusMark();
        int slotId = this.mRequest.getExpectSlot();
        this.mModemAdapter.registerIccStatusChangedForGetCardCount(slotId, this.mHandler);
        this.mRequest.setGetIccCardStatusMark(slotId, true);
        this.mHandler.sendMessageDelayed(obtainMessage(87, Integer.valueOf(slotId)), 5000);
        int anotherSubId = HwVSimUtilsInner.getAnotherSlotId(slotId);
        this.mModemAdapter.registerIccStatusChangedForGetCardCount(anotherSubId, this.mHandler);
        this.mRequest.setGetIccCardStatusMark(anotherSubId, true);
        this.mHandler.sendMessageDelayed(obtainMessage(87, Integer.valueOf(anotherSubId)), 5000);
    }

    private void onIccStatusChangedForCardCount(Message msg) {
        int subId = HwVSimUtilsInner.getCiIndex(msg).intValue();
        logi("onIccStatusChangedForCardCount, subId = " + subId);
        this.mModemAdapter.unregisterIccStatusChangedForGetCardCount(subId, this.mHandler);
        this.mHandler.removeMessages(87, Integer.valueOf(subId));
        getIccCardStatusForGetCardCount(subId);
    }

    private void getIccCardStatusForGetCardCount(int subId) {
        setIccCardStatusRetryTimes(subId, 0);
        this.mModemAdapter.getIccCardStatusForGetCardCount(this, this.mRequest.clone(), subId);
    }

    private void onGetIccCardStatusForGetCardCountDone(Message msg) {
        HwVSimRequest request;
        logd("onGetIccCardStatusForGetCardCountDone");
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (ar != null && (request = (HwVSimRequest) ar.getUserObj()) != null) {
            int slotId = request.mSubId;
            IccCardStatusExt status = IccCardStatusExt.from(ar.getResult());
            StringBuilder sb = new StringBuilder();
            sb.append("onGetIccCardStatusForGetCardCountDone:mCardState[");
            sb.append(slotId);
            sb.append("]=");
            sb.append(status != null ? status.getCardState() : " state is null");
            logi(sb.toString());
            int retryTimes = getIccCardStatusRetryTimes(slotId);
            boolean isError = ar.getException() != null;
            boolean isCardPresent = !isError && status != null && status.getCardState() == IccCardStatusExt.CardStateEx.CARDSTATE_PRESENT;
            boolean isErrorOrNotReady = isError || !(isCardPresent || (!isError && status != null && status.getCardState() == IccCardStatusExt.CardStateEx.CARDSTATE_ABSENT));
            logi("onGetIccCardStatusForGetCardCountDone: isError=" + isError + ", isCardPresent" + isCardPresent);
            if (isCardPresent) {
                request.setCardCount(request.getCardCount() + 1);
                setIccCardStatus(slotId, IccCardStatusExt.CardStateEx.CARDSTATE_PRESENT);
            } else {
                setIccCardStatus(slotId, IccCardStatusExt.CardStateEx.CARDSTATE_ABSENT);
            }
            logi("onGetIccCardStatusForGetCardCountDone, cardCount=" + request.getCardCount());
            if (!isErrorOrNotReady || retryTimes >= 5) {
                request.setGetIccCardStatusMark(slotId, false);
                setIccCardStatusRetryTimes(slotId, 0);
                if (request.isGetIccCardStatusDone()) {
                    setMainSlot(HwVSimPhoneFactory.getVSimSavedMainSlot(), request.getCardCount());
                    return;
                }
                return;
            }
            int retryTimes2 = retryTimes + 1;
            setIccCardStatusRetryTimes(slotId, retryTimes2);
            logi("onGetIccCardStatusForGetCardCountDone: retry getIccCardStatus,Times=" + retryTimes2);
            this.mModemAdapter.getIccCardStatusForGetCardCount(this, request, slotId);
        }
    }

    private void setIccCardStatusRetryTimes(int subId, int times) {
        if (this.mGetIccCardStatusTimes == null) {
            this.mGetIccCardStatusTimes = new int[HwVSimModemAdapter.PHONE_COUNT];
        }
        if (subId >= 0) {
            int[] iArr = this.mGetIccCardStatusTimes;
            if (subId < iArr.length) {
                iArr[subId] = times;
            }
        }
    }

    private void setIccCardStatus(int slotId, IccCardStatusExt.CardStateEx status) {
        if (this.mIccCardStatus == null) {
            this.mIccCardStatus = new IccCardStatusExt.CardStateEx[HwVSimModemAdapter.PHONE_COUNT];
        }
        if (slotId >= 0 && slotId < this.mIccCardStatus.length) {
            logi("setIccCardStatus, slotId = " + slotId + ", status = " + status);
            this.mIccCardStatus[slotId] = status;
        }
    }

    private int getIccCardStatusRetryTimes(int subId) {
        int[] iArr = this.mGetIccCardStatusTimes;
        if (iArr == null || subId < 0 || subId >= iArr.length) {
            return 5;
        }
        return iArr[subId];
    }

    private IccCardStatusExt.CardStateEx getIccCardStatus(int slotId) {
        IccCardStatusExt.CardStateEx[] cardStateExArr = this.mIccCardStatus;
        if (cardStateExArr == null || slotId < 0 || slotId >= cardStateExArr.length) {
            return IccCardStatusExt.CardStateEx.CARDSTATE_ABSENT;
        }
        return cardStateExArr[slotId];
    }

    private void setMainSlot(int savedMainSlot, int cardCount) {
        logi("setMainSlot saveMainSlot:" + savedMainSlot + ",cardCount:" + cardCount);
        HwFullNetworkManager.getInstance().setMainSlot(getAllowDataSlotId(savedMainSlot, cardCount), this.mHandler.obtainMessage(HwVSimConstants.EVENT_SET_MAIN_SLOT_DONE, this.mRequest));
    }

    private int getAllowDataSlotId(int savedMainSlot, int cardCount) {
        int default4GSlotId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
        int allowDataSlotId = savedMainSlot;
        int anotherSlotId = HwVSimUtilsInner.getAnotherSlotId(savedMainSlot);
        if (savedMainSlot == -1 || cardCount == 0) {
            allowDataSlotId = default4GSlotId;
        } else if (getIccCardStatus(savedMainSlot) == IccCardStatusExt.CardStateEx.CARDSTATE_ABSENT && getIccCardStatus(anotherSlotId) != IccCardStatusExt.CardStateEx.CARDSTATE_ABSENT) {
            logi("getAllowDataSlotId saved main slot is absent, change to another one.");
            allowDataSlotId = anotherSlotId;
        }
        logi("getAllowDataSlotId cardCount:" + cardCount + ", savedMainSlot:" + savedMainSlot + ", default4GSlotId:" + default4GSlotId + ", allowDataSlotId:" + allowDataSlotId);
        return allowDataSlotId;
    }

    private void onSetMainSlotDone(Message msg) {
        logi("onSetMainSlotDone -> finish ");
        if (isAsyncResultValid(AsyncResultEx.from(msg.obj))) {
            setDefaultDataSlotForMainSlot();
            HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mController.getVSimEventInfo(), 14);
            notifyResult(this.mRequest, true);
            transitionToState(0);
        }
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public Message obtainMessage(int what, Object obj) {
        return this.mController.obtainMessage(what, obj);
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public void doProcessException(AsyncResultEx ar, HwVSimRequest request) {
        logi("doProcessException to default state");
        doDisableProcessException(ar, request);
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.internal.telephony.vsim.process.HwVSimMtkProcessor
    public void logi(String content) {
        HwVSimLog.info(LOG_TAG, content);
    }

    private void cmdSemRelease() {
        Optional.ofNullable(this.mController).ifPresent($$Lambda$RJTKzqB1tTKFhpagP2yncGM8agg.INSTANCE);
    }
}
