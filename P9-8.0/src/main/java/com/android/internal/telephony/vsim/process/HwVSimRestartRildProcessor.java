package com.android.internal.telephony.vsim.process;

import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.android.internal.telephony.vsim.HwVSimController;
import com.android.internal.telephony.vsim.HwVSimLog;
import com.android.internal.telephony.vsim.HwVSimModemAdapter;
import com.android.internal.telephony.vsim.HwVSimNvMatchController;
import com.android.internal.telephony.vsim.HwVSimRequest;
import com.android.internal.telephony.vsim.HwVSimSlotSwitchController;
import com.android.internal.telephony.vsim.HwVSimUtilsInner;
import java.util.Arrays;

public class HwVSimRestartRildProcessor extends HwVSimProcessor {
    private static final int EVENT_RESTART_RILD_TIMEOUT = 2;
    private static final int EVENT_START_LISTEN_RADIO_AVALIABLE = 1;
    private static final int LISTEN_RADIO_AVALIABLE_TIMEOUT = 100;
    private static final String LOG_TAG = "VSimRestartRildProcessor";
    private static final long RESTART_RILD_TIMEOUT = 30000;
    private Handler mHandler = this.mVSimController.getHandler();
    private boolean mIsWaitingRestartRild;
    private boolean[] mRadioAvailableMark;
    private HwVSimController mVSimController;

    public HwVSimRestartRildProcessor(HwVSimController controller, HwVSimModemAdapter modemAdapter, HwVSimRequest request) {
        super(modemAdapter, request);
        this.mVSimController = controller;
    }

    public void onEnter() {
        logd("onEnter");
        this.mVSimController.getHandler().sendEmptyMessageDelayed(2, 30000);
        this.mIsWaitingRestartRild = false;
        this.mVSimController.setOnRadioAvaliable(this.mHandler, 83, null);
        for (int i = 0; i < HwVSimModemAdapter.MAX_SUB_COUNT; i++) {
            setRadioAvailableMark(i, false);
        }
        HwVSimSlotSwitchController.getInstance().restartRildBySubState();
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1), 100);
    }

    public void onExit() {
        logd("onExit");
        this.mVSimController.getHandler().removeMessages(2);
    }

    public boolean processMessage(Message msg) {
        switch (msg.what) {
            case 1:
                this.mIsWaitingRestartRild = true;
                return true;
            case 2:
                onRestartRildFinish();
                return true;
            case HwVSimConstants.EVENT_RADIO_AVAILABLE /*83*/:
                onRadioAvailable(msg);
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

    protected void setRadioAvailableMark(int subId, boolean available) {
        if (this.mRadioAvailableMark == null) {
            this.mRadioAvailableMark = new boolean[HwVSimModemAdapter.MAX_SUB_COUNT];
        }
        if (subId >= 0 && subId < this.mRadioAvailableMark.length) {
            this.mRadioAvailableMark[subId] = available;
        }
    }

    protected boolean isAllRadioAvailable() {
        if (this.mRadioAvailableMark == null) {
            return false;
        }
        logd("isAllRadioAvailable: " + Arrays.toString(this.mRadioAvailableMark));
        for (boolean z : this.mRadioAvailableMark) {
            if (!z) {
                return false;
            }
        }
        return true;
    }

    private void onRadioAvailable(Message msg) {
        Integer index = HwVSimUtilsInner.getCiIndex(msg);
        logd("onRadioAvailable, index = " + index);
        if (index.intValue() < 0 || index.intValue() >= HwVSimModemAdapter.MAX_SUB_COUNT) {
            logd("onRadioAvailable: Invalid index : " + index + " received with event " + msg.what);
        } else if (this.mIsWaitingRestartRild) {
            setRadioAvailableMark(index.intValue(), true);
            if (isAllRadioAvailable()) {
                onRestartRildFinish();
            }
        } else {
            logd("onRadioAvailable, not waiting restart rild, return.");
        }
    }

    private void onRestartRildFinish() {
        logd("onRestartRildFinish");
        transitionToState(0);
        HwVSimNvMatchController.getInstance().storeIfNeedRestartRildForNvMatch(false);
    }
}
