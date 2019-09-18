package com.huawei.wallet.sdk.business.idcard.walletbase.whitecard;

import android.os.Handler;
import com.huawei.wallet.sdk.common.apdu.base.BaseCallback;
import com.huawei.wallet.sdk.common.http.errorcode.ErrorInfo;

public class BaseResultHandler {
    /* access modifiers changed from: private */
    public BaseCallback mCallback;
    private Handler mUIHandler;

    class Task implements Runnable {
        ErrorInfo errorInfo;
        int resultCode;

        public Task(int resultCode2, ErrorInfo errorInfo2) {
            this.resultCode = resultCode2;
            this.errorInfo = errorInfo2;
        }

        public void run() {
            if (this.resultCode == 0) {
                BaseResultHandler.this.mCallback.onSuccess(this.resultCode);
            } else {
                BaseResultHandler.this.mCallback.onFail(this.resultCode, this.errorInfo);
            }
        }
    }

    public BaseResultHandler(Handler mUIHandler2, BaseCallback callback) {
        this.mUIHandler = mUIHandler2;
        this.mCallback = callback;
    }

    public void handleResult(int resultCode, ErrorInfo errorInfo) {
        this.mUIHandler.post(new Task(resultCode, errorInfo));
    }
}
