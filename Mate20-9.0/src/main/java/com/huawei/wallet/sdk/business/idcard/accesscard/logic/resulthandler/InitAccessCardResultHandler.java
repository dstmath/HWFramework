package com.huawei.wallet.sdk.business.idcard.accesscard.logic.resulthandler;

import android.os.Handler;
import com.huawei.wallet.sdk.business.idcard.accesscard.logic.callback.InitAccessCardOperatorCallback;

public class InitAccessCardResultHandler {
    InitAccessCardOperatorCallback callback;
    private Handler operateResultHandler;

    class Task implements Runnable {
        private int resultCode;

        public Task(int resultCode2) {
            this.resultCode = resultCode2;
        }

        public void run() {
            if (InitAccessCardResultHandler.this.callback != null) {
                InitAccessCardResultHandler.this.callback.initCUPCardOperatorResult(this.resultCode);
            }
        }
    }

    public InitAccessCardResultHandler(Handler operateResultHandler2, InitAccessCardOperatorCallback callback2) {
        this.callback = callback2;
        this.operateResultHandler = operateResultHandler2;
    }

    public void handleResult(int resultCode) {
        this.operateResultHandler.post(new Task(resultCode));
    }
}
