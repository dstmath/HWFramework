package com.android.server.security.hwkeychain;

import android.os.RemoteException;
import com.huawei.common.service.IDecisionCallback;
import java.util.Map;

public abstract class DecisionCallback extends IDecisionCallback.Stub {
    protected DecisionCallback mReversed1;

    public abstract void onResult(Map map) throws RemoteException;

    public void onTimeout() {
    }

    public DecisionCallback setReversed1(DecisionCallback rev1) {
        this.mReversed1 = rev1;
        return this;
    }

    public DecisionCallback getReversed1() {
        return this.mReversed1;
    }

    public DecisionCallback clearReversed1() {
        return setReversed1(null);
    }
}
