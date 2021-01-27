package com.huawei.server.security.hwkeychain;

import android.os.RemoteException;
import com.huawei.common.service.IDecisionCallback;
import java.util.Map;

public abstract class DecisionCallback extends IDecisionCallback.Stub {
    protected DecisionCallback mReversed;

    @Override // com.huawei.common.service.IDecisionCallback
    public abstract void onResult(Map map) throws RemoteException;

    public void onTimeout() {
    }

    public DecisionCallback setReversed(DecisionCallback rev) {
        this.mReversed = rev;
        return this;
    }

    public DecisionCallback getReversed() {
        return this.mReversed;
    }

    public DecisionCallback clearReversed() {
        return setReversed(null);
    }
}
