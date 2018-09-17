package com.huawei.systemmanager.rainbow.comm.request;

import android.content.Context;

public abstract class AbsRequest {
    private boolean mNeedDefaultParam = true;
    private boolean mResult = true;

    protected abstract void doRequest(Context context);

    public boolean processRequest(Context ctx) {
        if (shouldRun(ctx)) {
            doRequest(ctx);
        }
        return this.mResult;
    }

    protected boolean shouldRun(Context ctx) {
        return true;
    }

    protected void setRequestFailed() {
        this.mResult = false;
    }

    protected void setNeedDefaultParam(boolean need) {
        this.mNeedDefaultParam = need;
    }

    protected boolean isNeedDefaultParam() {
        return this.mNeedDefaultParam;
    }
}
