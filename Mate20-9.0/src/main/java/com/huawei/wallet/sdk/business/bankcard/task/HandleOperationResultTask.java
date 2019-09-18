package com.huawei.wallet.sdk.business.bankcard.task;

import android.os.Handler;
import com.huawei.wallet.sdk.business.bankcard.api.HandleCardOperateResultCallback;

public class HandleOperationResultTask implements Runnable {
    private Handler mExecuteHandler;
    private HandleCardOperateResultCallback mResultCallback;
    private int resultCode;

    public HandleOperationResultTask(Handler handler, HandleCardOperateResultCallback callback) {
        this.mExecuteHandler = handler;
        this.mResultCallback = callback;
    }

    public void notifyOperateResult(int code) {
        this.resultCode = code;
        this.mExecuteHandler.post(this);
    }

    public void run() {
        if (this.mResultCallback != null) {
            this.mResultCallback.operateResultCallback(this.resultCode);
        }
    }
}
