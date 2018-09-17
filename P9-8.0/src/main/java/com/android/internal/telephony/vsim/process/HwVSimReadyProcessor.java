package com.android.internal.telephony.vsim.process;

import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import com.android.internal.telephony.vsim.HwVSimController;
import com.android.internal.telephony.vsim.HwVSimEventReport.VSimEventInfoUtils;
import com.android.internal.telephony.vsim.HwVSimLog;
import com.android.internal.telephony.vsim.HwVSimModemAdapter;
import com.android.internal.telephony.vsim.HwVSimRequest;

public abstract class HwVSimReadyProcessor extends HwVSimProcessor {
    public static final String LOG_TAG = "VSimReadyProcessor";
    protected static final long NETWORK_CONNECT_TIMEOUT = 60000;
    protected Handler mHandler;
    protected boolean mIsM0Ready;
    protected HwVSimController mVSimController;

    public abstract void doProcessException(AsyncResult asyncResult, HwVSimRequest hwVSimRequest);

    public abstract boolean processMessage(Message message);

    public HwVSimReadyProcessor(HwVSimController controller, HwVSimModemAdapter modemAdapter, HwVSimRequest request) {
        super(modemAdapter, request);
        this.mVSimController = controller;
        if (controller != null) {
            this.mHandler = controller.getHandler();
        }
    }

    public void onEnter() {
        logd("onEnter");
    }

    public void onExit() {
        logd("onExit");
    }

    public void transitionToState(int state) {
        this.mVSimController.transitionToState(state);
    }

    public Message obtainMessage(int what, Object obj) {
        return this.mVSimController.obtainMessage(what, obj);
    }

    protected void logd(String s) {
        HwVSimLog.VSimLogD(LOG_TAG, s);
    }

    protected void onPlmnSelInfoDone(Message msg) {
        logd("onPlmnSelInfoDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 15);
        AsyncResult ar = msg.obj;
        int flag = -1;
        int result = -1;
        if (ar.exception == null) {
            int[] response = ar.result;
            if (response.length != 0) {
                flag = response[0];
                result = response[1];
            }
        }
        logd("PLMNSELEINFO: " + flag + " " + result);
        if (flag == 1) {
            this.mIsM0Ready = true;
            if (result == 0) {
                if (this.mVSimController.isNetworkConnected() && this.mHandler != null) {
                    this.mHandler.obtainMessage(50).sendToTarget();
                }
                VSimEventInfoUtils.setPsRegTime(this.mVSimController.mEventInfo, SystemClock.elapsedRealtime() - this.mVSimController.mVSimEnterTime);
                return;
            } else if (this.mHandler != null) {
                this.mHandler.removeMessages(71);
                this.mHandler.obtainMessage(71).sendToTarget();
                return;
            } else {
                return;
            }
        }
        this.mIsM0Ready = false;
    }
}
