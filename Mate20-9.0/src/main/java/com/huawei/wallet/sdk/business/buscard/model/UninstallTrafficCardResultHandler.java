package com.huawei.wallet.sdk.business.buscard.model;

import android.os.Handler;
import com.huawei.wallet.sdk.business.buscard.base.result.UninstallTrafficCardCallback;

public class UninstallTrafficCardResultHandler {
    /* access modifiers changed from: private */
    public UninstallTrafficCardCallback callback;
    private Handler operateResultHandler;

    class Task implements Runnable {
        private int resultCode;

        public Task(int resultCode2) {
            this.resultCode = resultCode2;
        }

        public void run() {
            if (UninstallTrafficCardResultHandler.this.callback != null) {
                UninstallTrafficCardResultHandler.this.callback.uninstallTrafficCardCallback(this.resultCode);
            }
        }
    }

    public UninstallTrafficCardResultHandler(Handler operateResultHandler2, UninstallTrafficCardCallback callback2) {
        this.callback = callback2;
        this.operateResultHandler = operateResultHandler2;
    }

    public void handleResult(int resultCode) {
        this.operateResultHandler.post(new Task(resultCode));
    }
}
