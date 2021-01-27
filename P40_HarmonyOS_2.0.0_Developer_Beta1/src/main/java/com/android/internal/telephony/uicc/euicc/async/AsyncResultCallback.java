package com.android.internal.telephony.uicc.euicc.async;

import android.telephony.Rlog;

public abstract class AsyncResultCallback<Result> {
    private static final String LOG_TAG = "AsyncResultCallback";

    public abstract void onResult(Result result);

    public void onException(Throwable e) {
        Rlog.e(LOG_TAG, "Error in onException", e);
    }
}
