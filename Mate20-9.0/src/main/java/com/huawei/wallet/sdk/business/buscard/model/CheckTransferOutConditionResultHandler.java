package com.huawei.wallet.sdk.business.buscard.model;

import android.os.Handler;

public class CheckTransferOutConditionResultHandler {
    /* access modifiers changed from: private */
    public CheckTransferOutConditionCallback callback;
    private Handler resultHander;

    class Task implements Runnable {
        private int resultCode;

        public Task(int resultCode2) {
            this.resultCode = resultCode2;
        }

        public void run() {
            if (CheckTransferOutConditionResultHandler.this.callback != null) {
                CheckTransferOutConditionResultHandler.this.callback.checkTransferOutConditionCallback(this.resultCode);
            }
        }
    }

    public CheckTransferOutConditionResultHandler(Handler operateResultHandler, CheckTransferOutConditionCallback callback2) {
        this.callback = callback2;
        this.resultHander = operateResultHandler;
    }

    public void handleResult(int resultCode) {
        this.resultHander.post(new Task(resultCode));
    }
}
