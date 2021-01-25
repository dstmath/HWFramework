package com.android.internal.telephony.vsim.process;

import android.os.Message;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.android.internal.telephony.vsim.HwVSimController;
import com.android.internal.telephony.vsim.HwVSimLog;
import com.android.internal.telephony.vsim.HwVSimModemAdapter;
import com.android.internal.telephony.vsim.HwVSimRequest;
import com.android.internal.telephony.vsim.HwVSimUtilsInner;
import com.huawei.android.os.AsyncResultEx;

public class HwVSimInitialProcessor extends HwVSimProcessor {
    private static final int INVALID = -1;
    private static final String LOG_TAG = "VSimInitialProcessor";
    private boolean mIsSlotSwitchInitDone;
    private HwVSimController mVsimController;

    public HwVSimInitialProcessor(HwVSimController controller, HwVSimModemAdapter modemAdapter, HwVSimRequest request) {
        super(modemAdapter, request);
        this.mVsimController = controller;
        this.mVsimController.syncSubState();
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public void onEnter() {
        logd("onEnter");
        this.mVsimController.getHandler().sendEmptyMessageDelayed(75, HwVSimConstants.INITIAL_TIMEOUT);
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public void onExit() {
        logd("onExit");
        this.mVsimController.getHandler().removeMessages(75);
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
        return this.mVsimController.obtainMessage(what, obj);
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public void transitionToState(int state) {
        this.mVsimController.transitionToState(state);
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
        int[] simCardTypes = this.mVsimController.getSimCardTypes();
        for (int i = 0; i < simCardTypes.length; i++) {
            if (simCardTypes[i] == 0) {
                this.mVsimController.updateSubState(i, 1);
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
            if ((this.mVsimController.getSubState(0) != -1 || this.mVsimController.needBlockUnReservedForVsim(0)) && (this.mVsimController.getSubState(1) != -1 || this.mVsimController.needBlockUnReservedForVsim(1))) {
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
