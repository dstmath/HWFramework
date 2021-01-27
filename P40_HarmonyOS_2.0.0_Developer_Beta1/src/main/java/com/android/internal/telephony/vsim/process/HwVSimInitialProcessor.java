package com.android.internal.telephony.vsim.process;

import android.os.Message;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.android.internal.telephony.vsim.HwVSimController;
import com.android.internal.telephony.vsim.HwVSimLog;
import com.android.internal.telephony.vsim.HwVSimModemAdapter;
import com.android.internal.telephony.vsim.HwVSimRequest;
import com.android.internal.telephony.vsim.HwVSimUtilsInner;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.internal.telephony.vsim.process.HwVSimHisiProcessor;

public class HwVSimInitialProcessor extends HwVSimHisiProcessor {
    private static final int INVALID = -1;
    private static final String LOG_TAG = "VSimInitialProcessor";
    private boolean mIsSlotSwitchInitDone;

    public HwVSimInitialProcessor(HwVSimController controller, HwVSimModemAdapter modemAdapter, HwVSimRequest request) {
        super(controller, modemAdapter, request);
        this.mVSimController.syncSubState();
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public void onEnter() {
        logd("onEnter");
        this.mHandler.sendEmptyMessageDelayed(75, HwVSimConstants.INITIAL_TIMEOUT);
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public void onExit() {
        logd("onExit");
        this.mHandler.removeMessages(75);
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public boolean processMessage(Message msg) {
        int i = msg.what;
        if (i != 5) {
            switch (i) {
                case HwVSimConstants.EVENT_INITIAL_TIMEOUT /* 75 */:
                    onInitialTimeout();
                    return true;
                case HwVSimConstants.EVENT_INITIAL_SUBSTATE_DONE /* 76 */:
                    checkInitialSubStateDone(msg);
                    return true;
                case HwVSimConstants.EVENT_INITIAL_UPDATE_CARDTYPE /* 77 */:
                    checkInitialSimCardTypes(msg);
                    return true;
                default:
                    return false;
            }
        } else {
            checkSlotSwitchInitDone();
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
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public void logd(String s) {
        HwVSimLog.VSimLogD(LOG_TAG, s);
    }

    private void checkSlotSwitchInitDone() {
        this.mIsSlotSwitchInitDone = true;
        checkInitDone();
    }

    private void checkInitialSubStateDone(Message msg) {
        logd("checkInitSubStateDone");
        checkInitDone();
    }

    private void checkInitialSimCardTypes(Message msg) {
        logd("checkInitialSimCardTypes");
        int[] simCardTypes = this.mVSimController.getSimCardTypes();
        for (int i = 0; i < simCardTypes.length; i++) {
            if (simCardTypes[i] == 0) {
                this.mVSimController.updateSubState(i, 1);
            }
        }
        checkInitDone();
    }

    private void checkInitDone() {
        if (!this.mIsSlotSwitchInitDone) {
            logd("slot switch not done!");
            return;
        }
        if (HwVSimUtilsInner.isPlatformRealTripple() && HwVSimUtilsInner.IS_DSDSPOWER_SUPPORT) {
            boolean subStateNotInit = true;
            if ((this.mVSimController.getSubState(0) != -1 || this.mVSimController.needBlockUnReservedForVsim(0)) && (this.mVSimController.getSubState(1) != -1 || this.mVSimController.needBlockUnReservedForVsim(1))) {
                subStateNotInit = false;
            }
            if (subStateNotInit) {
                logd("sub state init not done!");
                return;
            }
        }
        logd("checkInitDone");
        transitionToState(0);
    }

    private void onInitialTimeout() {
        logd("Initial time out : mIsSlotSwitchInitDone : " + this.mIsSlotSwitchInitDone);
        transitionToState(0);
    }
}
