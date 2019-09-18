package com.huawei.wallet.sdk.business.idcard.accesscard.logic.resulthandler;

import android.os.Handler;
import com.huawei.wallet.sdk.business.idcard.accesscard.logic.callback.NullifyCardResultCallback;

public class HandleNullifyResultHandler {
    /* access modifiers changed from: private */
    public NullifyCardResultCallback mCallback;
    private Handler mUIHandler;

    class Task implements Runnable {
        int resultCode;

        public Task(int resultCode2) {
            this.resultCode = resultCode2;
        }

        public void run() {
            HandleNullifyResultHandler.this.mCallback.nullifyResultCallback(this.resultCode);
        }
    }

    public HandleNullifyResultHandler(Handler mUIHandler2, NullifyCardResultCallback callback) {
        this.mUIHandler = mUIHandler2;
        this.mCallback = callback;
    }

    public void handleResult(int resultCode) {
        this.mUIHandler.post(new Task(resultCode));
    }
}
