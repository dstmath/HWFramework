package com.android.internal.telephony.vsim.process;

import android.os.Handler;
import android.os.Message;
import com.android.internal.telephony.HwVSimPhone;
import com.android.internal.telephony.HwVSimPhoneFactory;
import com.android.internal.telephony.uicc.HwVSimIccCardProxy;
import com.android.internal.telephony.vsim.HwVSimController;
import com.android.internal.telephony.vsim.HwVSimEventReport;
import com.android.internal.telephony.vsim.HwVSimLog;
import com.android.internal.telephony.vsim.HwVSimModemAdapter;
import com.android.internal.telephony.vsim.HwVSimRequest;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.internal.telephony.IccCardConstantsEx;
import com.huawei.internal.telephony.PhoneExt;

public class HwVSimSwitchModeProcessor extends HwVSimProcessor {
    public static final String LOG_TAG = "HwVSimSwitchModeProcessor";
    protected Handler mHandler = this.mVSimController.getHandler();
    protected HwVSimController mVSimController;

    public HwVSimSwitchModeProcessor(HwVSimController controller, HwVSimModemAdapter modemAdapter, HwVSimRequest request) {
        super(modemAdapter, request);
        this.mVSimController = controller;
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public void onEnter() {
        logd("onEnter");
        cmdSem_release();
        HwVSimRequest request = this.mRequest;
        if (request == null) {
            transitionToState(0);
            return;
        }
        request.createGotCardType(HwVSimModemAdapter.PHONE_COUNT);
        request.createCardTypes(HwVSimModemAdapter.PHONE_COUNT);
        this.mModemAdapter.getSimSlot(this, request, 2);
        this.mVSimController.setProcessAction(HwVSimController.ProcessAction.PROCESS_ACTION_SWITCHWORKMODE);
        this.mVSimController.setOnRadioAvaliable(this.mHandler, 83, null);
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public void onExit() {
        logd("onExit");
        noticeApkToReEnable();
        this.mVSimController.updateUserPreferences();
        this.mVSimController.unSetOnRadioAvaliable(this.mHandler);
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
        doSwitchModeProcessException(ar, request);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public void logd(String s) {
        HwVSimLog.VSimLogD(LOG_TAG, s);
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public boolean processMessage(Message msg) {
        int i = msg.what;
        if (i == 54) {
            onGetSimSlotDone(msg);
            return true;
        } else if (i != 56) {
            return false;
        } else {
            onQueryCardTypeDone(msg);
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public void onGetSimSlotDone(Message msg) {
        logd("onGetSimSlotDone");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 1);
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (isAsyncResultValid(ar)) {
            this.mModemAdapter.onGetSimSlotDone(this, ar);
            HwVSimRequest request = (HwVSimRequest) ar.getUserObj();
            if (request.getIsVSimOnM0()) {
                this.mModemAdapter.getAllCardTypes(this, this.mRequest);
                return;
            }
            notifyResult(request, false);
            transitionToState(0);
        }
    }

    private void noticeApkToReEnable() {
        PhoneExt phone = HwVSimPhoneFactory.getVSimPhone();
        logd("noticeApkToReEnable: phone = " + phone);
        HwVSimIccCardProxy vSimIccCardProxy = null;
        if (phone instanceof HwVSimPhone) {
            vSimIccCardProxy = ((HwVSimPhone) phone).getIccCard();
        }
        if (vSimIccCardProxy == null) {
            logd("icccard is null, return.");
            return;
        }
        IccCardConstantsEx.StateEx state = vSimIccCardProxy.getState();
        if (IccCardConstantsEx.StateEx.ABSENT == state) {
            vSimIccCardProxy.broadcastIccStateChangedIntent(state);
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
                this.mModemAdapter.checkSwitchModeSimCondition(this, this.mRequest);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void cmdSem_release() {
        if (this.mVSimController != null) {
            this.mVSimController.cmdSem_release();
        }
    }
}
