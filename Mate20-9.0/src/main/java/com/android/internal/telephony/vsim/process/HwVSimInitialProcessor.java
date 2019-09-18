package com.android.internal.telephony.vsim.process;

import android.os.AsyncResult;
import android.os.Message;
import com.android.internal.telephony.HwDsdsController;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.android.internal.telephony.vsim.HwVSimController;
import com.android.internal.telephony.vsim.HwVSimLog;
import com.android.internal.telephony.vsim.HwVSimModemAdapter;
import com.android.internal.telephony.vsim.HwVSimRequest;
import com.android.internal.telephony.vsim.HwVSimUtilsInner;

public class HwVSimInitialProcessor extends HwVSimProcessor {
    public static final int INVALID = -1;
    public static final String LOG_TAG = "VSimInitialProcessor";
    private static final int MAX_TRY_GET_MODEM_SUPPORT_VSIM_VERSION_TIMES = 3;
    private boolean mDSDSAutoSetModemModeDone = false;
    private boolean mGetModemSupportVSimVersionDone = false;
    private boolean mSlotSwitchInitDone;
    private int mTryGetModemSupportVSimVersionTimes;
    protected HwVSimController mVSimController;

    public HwVSimInitialProcessor(HwVSimController controller, HwVSimModemAdapter modemAdapter, HwVSimRequest request) {
        super(modemAdapter, request);
        this.mVSimController = controller;
    }

    public void onEnter() {
        logd("onEnter");
        this.mTryGetModemSupportVSimVersionTimes = 0;
        this.mVSimController.getHandler().sendEmptyMessageDelayed(75, 120000);
        this.mModemAdapter.getModemSupportVSimVersion(this, this.mVSimController.getRadioOnSubId());
        if (HwVSimUtilsInner.isPlatformRealTripple() && HwVSimUtilsInner.IS_DSDSPOWER_SUPPORT) {
            this.mDSDSAutoSetModemModeDone = !HwDsdsController.getInstance().uiccHwdsdsNeedSetActiveMode();
            logd("DSDS Auto modem mode done is " + this.mDSDSAutoSetModemModeDone);
        }
    }

    public void onExit() {
        logd("onExit");
        this.mVSimController.getHandler().removeMessages(75);
        this.mVSimController.getHandler().removeMessages(78);
    }

    public boolean processMessage(Message msg) {
        int i = msg.what;
        if (i != 5) {
            switch (i) {
                case HwVSimConstants.EVENT_GET_MODEM_SUPPORT_VSIM_VER_DONE:
                    checkGetModemSupportVSimVersionDone(msg);
                    return true;
                case HwVSimConstants.EVENT_DSDS_AUTO_SETMODEM_DONE:
                    checkDSDSAutoSetModemDone(msg);
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
        } else {
            checkSlotSwitchInitDone();
            return true;
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

    /* access modifiers changed from: protected */
    public void logd(String s) {
        HwVSimLog.VSimLogD(LOG_TAG, s);
    }

    private void checkSlotSwitchInitDone() {
        this.mSlotSwitchInitDone = true;
        checkInitDone();
    }

    private void checkGetModemSupportVSimVersionDone(Message msg) {
        logd("checkGetModemSupportVSimVersionDone");
        this.mModemAdapter.onGetModemSupportVSimVersionDone(this, (AsyncResult) msg.obj);
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
        } else if (!this.mGetModemSupportVSimVersionDone) {
            logd("get modem support vsim version not done!");
        } else {
            if (HwVSimUtilsInner.isPlatformRealTripple() && HwVSimUtilsInner.IS_DSDSPOWER_SUPPORT) {
                if (!this.mDSDSAutoSetModemModeDone) {
                    logd("dsds auto modem change not done!");
                    return;
                }
                boolean subStateNotInit = true;
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
        logd("Initial time out : mSlotSwitchInitDone : " + this.mSlotSwitchInitDone + ", mGetModemSupportVSimVersionDone " + this.mGetModemSupportVSimVersionDone + ", mDSDSAutoSetModemModeDone " + this.mDSDSAutoSetModemModeDone);
        transitionToState(0);
    }
}
