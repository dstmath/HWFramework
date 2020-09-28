package com.android.internal.telephony.vsim.process;

import android.os.Handler;
import android.os.Message;
import com.android.internal.telephony.HwVSimPhoneFactory;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.android.internal.telephony.vsim.HwVSimController;
import com.android.internal.telephony.vsim.HwVSimEventReport;
import com.android.internal.telephony.vsim.HwVSimLog;
import com.android.internal.telephony.vsim.HwVSimModemAdapter;
import com.android.internal.telephony.vsim.HwVSimRequest;
import com.android.internal.telephony.vsim.HwVSimUtilsInner;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.internal.telephony.uicc.IccRecordsEx;
import com.huawei.internal.telephony.uicc.UiccCardExt;

public class HwVSimDisableProcessor extends HwVSimProcessor {
    public static final String LOG_TAG = "VSimDisableProcess";
    protected Handler mHandler = this.mVSimController.getHandler();
    protected HwVSimController mVSimController;

    public HwVSimDisableProcessor(HwVSimController controller, HwVSimModemAdapter modemAdapter, HwVSimRequest request) {
        super(modemAdapter, request);
        this.mVSimController = controller;
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public void onEnter() {
        logd("onEnter");
        cmdSem_release();
        if (this.mRequest == null) {
            transitionToState(0);
            return;
        }
        this.mVSimController.setProcessAction(HwVSimController.ProcessAction.PROCESS_ACTION_DISABLE);
        HwVSimRequest request = this.mRequest;
        request.createGotCardType(HwVSimModemAdapter.PHONE_COUNT);
        request.createCardTypes(HwVSimModemAdapter.PHONE_COUNT);
        request.setGotSimSlotMark(false);
        for (int subId = 0; subId < HwVSimModemAdapter.MAX_SUB_COUNT; subId++) {
            if (this.mModemAdapter.getCiBySub(subId) != null && HwVSimUtilsInner.isRadioAvailable(subId)) {
                this.mModemAdapter.getSimSlot(this, request.clone(), subId);
            }
        }
        this.mVSimController.setOnRadioAvaliable(this.mHandler, 83, null);
        this.mVSimController.clearRecoverMarkToFalseMessage();
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public void onExit() {
        logd("onExit");
        cleanMainSlotIfDisableSucceed();
        this.mVSimController.setBlockPinFlag(false);
        this.mModemAdapter.setHwVSimPowerOff(this.mRequest);
        allowDefaultData();
        clearCardReloadMarkIfSimLoaded();
        this.mVSimController.setIsWaitingSwitchSimSlot(true);
        this.mVSimController.setSubActivationUpdate(true);
        this.mVSimController.updateSubActivation();
        this.mVSimController.unSetOnRadioAvaliable(this.mHandler);
        this.mModemAdapter.saveM0NetworkMode(-1);
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public boolean processMessage(Message msg) {
        switch (msg.what) {
            case HwVSimConstants.EVENT_GET_SIM_SLOT_DONE:
                onGetSimSlotDone(msg);
                return true;
            case 55:
            default:
                return false;
            case HwVSimConstants.EVENT_QUERY_CARD_TYPE_DONE:
                onQueryCardTypeDone(msg);
                return true;
        }
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public Message obtainMessage(int what, Object obj) {
        return this.mVSimController.obtainMessage(what, obj);
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public void transitionToState(int state) {
        this.mVSimController.transitionToState(state);
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public void doProcessException(AsyncResultEx ar, HwVSimRequest request) {
        doDisableProcessException(ar, request);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public void logd(String s) {
        HwVSimLog.VSimLogD(LOG_TAG, s);
    }

    private void onGetSimSlotDone(Message msg) {
        logd("onGetSimSlotDone");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 1);
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (isAsyncResultValidForRequestNotSupport(ar)) {
            HwVSimRequest request = (HwVSimRequest) ar.getUserObj();
            if (request.isGotSimSlot()) {
                logd("already got sim slot, just skip");
                return;
            }
            this.mModemAdapter.onGetSimSlotDone(this, ar);
            if (request.getIsVSimOnM0()) {
                this.mModemAdapter.getAllCardTypes(this, this.mRequest);
                return;
            }
            notifyResult(request, true);
            transitionToState(0);
        }
    }

    /* access modifiers changed from: protected */
    public void onQueryCardTypeDone(Message msg) {
        logd("onQueryCardTypeDone");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 1);
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (isAsyncResultValid(ar)) {
            this.mModemAdapter.onQueryCardTypeDone(this, ar);
            if (this.mRequest.isGotAllCardTypes()) {
                logd("onQueryCardTypeDone : isGotAllCardTypes");
                this.mModemAdapter.checkDisableSimCondition(this, this.mRequest);
            }
        }
    }

    private void cmdSem_release() {
        if (this.mVSimController != null) {
            this.mVSimController.cmdSem_release();
        }
    }

    private void allowDefaultData() {
        if (this.mVSimController != null) {
            this.mVSimController.allowDefaultData();
        }
    }

    private void cleanMainSlotIfDisableSucceed() {
        Object oResult;
        boolean iResult = false;
        if (!(this.mRequest == null || (oResult = this.mRequest.getResult()) == null)) {
            iResult = ((Boolean) oResult).booleanValue();
        }
        if (iResult) {
            this.mVSimController.setVSimSavedMainSlot(-1);
            HwVSimPhoneFactory.setVSimSavedNetworkMode(0, -1);
            HwVSimPhoneFactory.setVSimSavedNetworkMode(1, -1);
            return;
        }
        logd("leave saved main slot untouched");
    }

    private void clearCardReloadMarkIfSimLoaded() {
        for (int subId = 0; subId < HwVSimModemAdapter.PHONE_COUNT; subId++) {
            boolean loaded = false;
            UiccCardExt uiccCard = this.mVSimController.getUiccCard(subId);
            if (uiccCard != null) {
                IccRecordsEx records = uiccCard.getIccRecords();
                loaded = records != null && records.isLoaded();
            }
            if (loaded) {
                logd("clearCardReloadMarkIfSimLoaded, sim card loaded, clear the mark.");
                this.mVSimController.setMarkForCardReload(subId, false);
            }
        }
    }
}
