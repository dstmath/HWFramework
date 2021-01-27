package com.huawei.internal.telephony.vsim.process;

import android.os.Message;
import com.android.internal.telephony.vsim.HwVSimLog;
import com.android.internal.telephony.vsim.HwVSimModemAdapter;
import com.android.internal.telephony.vsim.HwVSimRequest;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.internal.telephony.vsim.HwVSimMtkController;

public class HwVSimMtkInitialProcessor extends HwVSimMtkProcessor {
    private static final long INITIAL_TIMEOUT = 10000;
    private static final int INVALID = -1;
    private static final String LOG_TAG = "HwVsimMtkInitialProcess";
    private boolean mIsQueryCardTypeDone = true;

    public HwVSimMtkInitialProcessor(HwVSimMtkController controller, HwVSimModemAdapter modemAdapter, HwVSimRequest request) {
        super(controller, modemAdapter, request);
        this.mController.syncSubState();
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public void onEnter() {
        logi("onEnter");
        this.mController.getHandler().sendEmptyMessageDelayed(75, 10000);
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public void onExit() {
        logi("onExit");
        this.mController.getHandler().removeMessages(75);
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public void transitionToState(int state) {
        this.mController.transitionToState(state);
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public boolean processMessage(Message msg) {
        int i = msg.what;
        if (i == 75) {
            onInitialTimeout();
            return true;
        } else if (i != 76) {
            return false;
        } else {
            checkInitialSubStateDone(msg);
            return true;
        }
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public Message obtainMessage(int what, Object obj) {
        return this.mController.obtainMessage(what, obj);
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public void doProcessException(AsyncResultEx ar, HwVSimRequest request) {
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.internal.telephony.vsim.process.HwVSimMtkProcessor
    public void logi(String content) {
        HwVSimLog.VSimLogI(LOG_TAG, content);
    }

    private void checkInitialSubStateDone(Message msg) {
        logi("checkInitSubStateDone");
        checkInitDone();
    }

    private void checkInitDone() {
        if (!this.mIsQueryCardTypeDone) {
            logi("slot switch not done!");
            return;
        }
        logi("checkInitDone");
        transitionToState(0);
    }

    private void onInitialTimeout() {
        logi("Initial time out : mIsQueryCardTypeDone : " + this.mIsQueryCardTypeDone);
        transitionToState(0);
    }
}
