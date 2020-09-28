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
    public static final int INVALID = -1;
    public static final String LOG_TAG = "VSimInitialProcessor";
    private static final int MAX_TRY_GET_MODEM_SUPPORT_VSIM_VERSION_TIMES = 3;
    private boolean mGetModemSupportVSimVersionDone = false;
    private boolean mSlotSwitchInitDone;
    private int mTryGetModemSupportVSimVersionTimes;
    protected HwVSimController mVSimController;

    public HwVSimInitialProcessor(HwVSimController controller, HwVSimModemAdapter modemAdapter, HwVSimRequest request) {
        super(modemAdapter, request);
        this.mVSimController = controller;
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public void onEnter() {
        logd("onEnter");
        this.mTryGetModemSupportVSimVersionTimes = 0;
        this.mVSimController.getHandler().sendEmptyMessageDelayed(75, 120000);
        this.mModemAdapter.getModemSupportVSimVersion(this, this.mVSimController.getRadioOnSubId());
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public void onExit() {
        logd("onExit");
        this.mVSimController.getHandler().removeMessages(75);
        this.mVSimController.getHandler().removeMessages(78);
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public boolean processMessage(Message msg) {
        switch (msg.what) {
            case 5:
                checkSlotSwitchInitDone();
                return true;
            case HwVSimConstants.EVENT_GET_MODEM_SUPPORT_VSIM_VER_DONE:
                checkGetModemSupportVSimVersionDone(msg);
                return true;
            case HwVSimConstants.EVENT_INITIAL_TIMEOUT:
                onInitialTimeout();
                return true;
            case HwVSimConstants.EVENT_INITIAL_SUBSTATE_DONE:
                checkInitialSubStateDone(msg);
                return true;
            case HwVSimConstants.EVENT_INITIAL_UPDATE_CARDTYPE:
                checkInitialSimCardTypes(msg);
                return true;
            case HwVSimConstants.EVENT_INITIAL_GET_MODEM_SUPPORT_VSIM_VER:
                this.mModemAdapter.getModemSupportVSimVersion(this, this.mVSimController.getRadioOnSubId());
                return true;
            default:
                return false;
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
        this.mSlotSwitchInitDone = true;
        checkInitDone();
    }

    private void checkGetModemSupportVSimVersionDone(Message msg) {
        logd("checkGetModemSupportVSimVersionDone");
        this.mModemAdapter.onGetModemSupportVSimVersionDone(this, AsyncResultEx.from(msg.obj));
        if (this.mVSimController.getPlatformSupportVSimVer(0) == -1) {
            int i = this.mTryGetModemSupportVSimVersionTimes;
            this.mTryGetModemSupportVSimVersionTimes = i + 1;
            if (i < 3) {
                logd("retry to getModemSupportVSimVersion");
                this.mVSimController.getHandler().sendEmptyMessageDelayed(78, HwVSimConstants.GET_MODEM_SUPPORT_VERSION_INTERVAL);
                checkInitDone();
            }
        }
        this.mGetModemSupportVSimVersionDone = true;
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
        boolean subStateNotInit = true;
        if (!this.mSlotSwitchInitDone) {
            logd("slot switch not done!");
        } else if (!this.mGetModemSupportVSimVersionDone) {
            logd("get modem support vsim version not done!");
        } else {
            if (HwVSimUtilsInner.isPlatformRealTripple() && HwVSimUtilsInner.IS_DSDSPOWER_SUPPORT) {
                if ((-1 != this.mVSimController.getSubState(0) || this.mVSimController.needBlockUnReservedForVsim(0)) && (-1 != this.mVSimController.getSubState(1) || this.mVSimController.needBlockUnReservedForVsim(1))) {
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
    }

    private void onInitialTimeout() {
        logd("Initial time out : mSlotSwitchInitDone : " + this.mSlotSwitchInitDone + ", mGetModemSupportVSimVersionDone " + this.mGetModemSupportVSimVersionDone);
        transitionToState(0);
    }
}
