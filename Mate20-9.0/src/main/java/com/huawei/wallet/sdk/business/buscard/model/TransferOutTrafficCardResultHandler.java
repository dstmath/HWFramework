package com.huawei.wallet.sdk.business.buscard.model;

import android.os.Handler;
import com.huawei.wallet.sdk.business.buscard.base.result.TransferOutTrafficCardCallback;

public class TransferOutTrafficCardResultHandler {
    /* access modifiers changed from: private */
    public TransferOutTrafficCardCallback callback;
    private Handler operateResultHandler;

    class Task implements Runnable {
        private int resultCode;

        public Task(int resultCode2) {
            this.resultCode = resultCode2;
        }

        public void run() {
            if (TransferOutTrafficCardResultHandler.this.callback != null) {
                TransferOutTrafficCardResultHandler.this.callback.transferOutCallback(this.resultCode);
            }
        }
    }

    public TransferOutTrafficCardResultHandler(Handler operateResultHandler2, TransferOutTrafficCardCallback callback2) {
        this.callback = callback2;
        this.operateResultHandler = operateResultHandler2;
    }

    public void handleResult(int resultCode) {
        this.operateResultHandler.post(new Task(resultCode));
    }
}
