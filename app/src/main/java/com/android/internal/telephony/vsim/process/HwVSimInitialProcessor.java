package com.android.internal.telephony.vsim.process;

import android.os.AsyncResult;
import android.os.Message;
import com.android.internal.telephony.HwDsdsController;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.android.internal.telephony.vsim.HwVSimController;
import com.android.internal.telephony.vsim.HwVSimEventReport;
import com.android.internal.telephony.vsim.HwVSimLog;
import com.android.internal.telephony.vsim.HwVSimModemAdapter;
import com.android.internal.telephony.vsim.HwVSimRequest;
import com.android.internal.telephony.vsim.HwVSimUtilsInner;

public class HwVSimInitialProcessor extends HwVSimProcessor {
    public static final String LOG_TAG = "VSimInitialProcessor";
    private boolean mDSDSAutoSetModemModeDone;
    private boolean mGetModemSupportVSimVersionDone;
    private boolean mSlotSwitchInitDone;
    protected HwVSimController mVSimController;

    public HwVSimInitialProcessor(HwVSimController controller, HwVSimModemAdapter modemAdapter, HwVSimRequest request) {
        super(modemAdapter, request);
        this.mVSimController = controller;
        this.mGetModemSupportVSimVersionDone = false;
        this.mDSDSAutoSetModemModeDone = false;
    }

    public void onEnter() {
        boolean z = false;
        logd("onEnter");
        this.mVSimController.getHandler().sendEmptyMessageDelayed(75, HwVSimConstants.INITIAL_TIMEOUT);
        this.mModemAdapter.getModemSupportVSimVersion(this, 0);
        if (HwVSimUtilsInner.isPlatformRealTripple() && HwVSimUtilsInner.IS_DSDSPOWER_SUPPORT) {
            if (!HwDsdsController.getInstance().uiccHwdsdsNeedSetActiveMode()) {
                z = true;
            }
            this.mDSDSAutoSetModemModeDone = z;
            logd("DSDS Auto modem mode done is " + this.mDSDSAutoSetModemModeDone);
        }
    }

    public void onExit() {
        logd("onExit");
        this.mVSimController.getHandler().removeMessages(75);
    }

    public boolean processMessage(Message msg) {
        switch (msg.what) {
            case HwVSimEventReport.VSIM_CAUSE_TYPE_SWITCH_SLOT /*5*/:
                checkSlotSwitchInitDone();
                return true;
            case HwVSimConstants.EVENT_GET_MODEM_SUPPORT_VSIM_VER_DONE /*73*/:
                checkGetModemSupportVSimVersionDone(msg);
                return true;
            case HwVSimConstants.EVENT_DSDS_AUTO_SETMODEM_DONE /*74*/:
                checkDSDSAutoSetModemDone(msg);
                return true;
            case HwVSimConstants.EVENT_INITIAL_TIMEOUT /*75*/:
                onInitialTimeout();
                return true;
            case HwVSimConstants.EVENT_INITIAL_SUBSTATE_DONE /*76*/:
                checkInitialSubStateDone(msg);
                return true;
            case HwVSimConstants.EVENT_INITIAL_UPDATE_CARDTYPE /*77*/:
                checkInitialSimCardTypes(msg);
                return true;
            default:
                return false;
        }
    }

    public Message obtainMessage(int what, Object obj) {
        return this.mVSimController.obtainMessage(what, obj);
    }

    public void transitionToState(int state) {
        this.mVSimController.transitionToState(state);
    }

    public void doProcessException(AsyncResult ar, HwVSimRequest request) {
    }

    protected void logd(String s) {
        HwVSimLog.VSimLogD(LOG_TAG, s);
    }

    private void checkSlotSwitchInitDone() {
        this.mSlotSwitchInitDone = true;
        checkInitDone();
    }

    private void checkGetModemSupportVSimVersionDone(Message msg) {
        logd("checkGetModemSupportVSimVersionDone");
        this.mModemAdapter.onGetModemSupportVSimVersionDone(this, (AsyncResult) msg.obj);
        this.mGetModemSupportVSimVersionDone = true;
        checkInitDone();
    }

    private void checkDSDSAutoSetModemDone(Message msg) {
        logd("checkDSDSAutoSetModemDone");
        this.mDSDSAutoSetModemModeDone = true;
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
        if (!this.mSlotSwitchInitDone) {
            logd("slot switch not done!");
        } else if (this.mGetModemSupportVSimVersionDone) {
            if (HwVSimUtilsInner.isPlatformRealTripple() && HwVSimUtilsInner.IS_DSDSPOWER_SUPPORT) {
                if (!this.mDSDSAutoSetModemModeDone) {
                    logd("dsds auto modem change not done!");
                    return;
                } else if (this.mVSimController.getSubState(0) == -1 || this.mVSimController.getSubState(1) == -1) {
                    logd("sub state init not done!");
                    return;
                }
            }
            logd("checkInitDone");
            transitionToState(0);
        } else {
            logd("get modem support vsim version not done!");
        }
    }

    private void onInitialTimeout() {
        logd("Initial time out : mSlotSwitchInitDone : " + this.mSlotSwitchInitDone + ", mGetModemSupportVSimVersionDone " + this.mGetModemSupportVSimVersionDone + ", mDSDSAutoSetModemModeDone " + this.mDSDSAutoSetModemModeDone);
        transitionToState(0);
    }
}
