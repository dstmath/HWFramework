package com.android.internal.telephony.vsim.process;

import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import com.android.internal.telephony.vsim.HwVSimController;
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
}
