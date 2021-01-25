package com.huawei.nb.client.callback;

import android.os.RemoteException;
import com.huawei.nb.callback.IUpdateCallback;

public class UpdateCallback extends IUpdateCallback.Stub implements WaitableCallback<Integer> {
    private static final Integer INVALID_COUNT = -1;
    private final CallbackManager callbackManager;
    private final CallbackWaiter<Integer> callbackWaiter = new CallbackWaiter<>(INVALID_COUNT);

    UpdateCallback(CallbackManager callbackManager2) {
        this.callbackManager = callbackManager2;
    }

    @Override // com.huawei.nb.callback.IUpdateCallback
    public void onResult(int i, int i2) throws RemoteException {
        this.callbackWaiter.set(i, Integer.valueOf(i2));
    }

    @Override // com.huawei.nb.client.callback.WaitableCallback
    public Integer await(int i, long j) {
        this.callbackManager.startWaiting(this);
        Integer await = this.callbackWaiter.await(i, j);
        this.callbackManager.stopWaiting(this);
        return await;
    }

    @Override // com.huawei.nb.client.callback.WaitableCallback
    public void interrupt() {
        this.callbackWaiter.interrupt();
    }
}
