package com.huawei.nb.client.callback;

import android.os.RemoteException;
import com.huawei.nb.callback.IKvSubscribeCallback;

public class KvSubscribeCallback extends IKvSubscribeCallback.Stub implements WaitableCallback<Boolean> {
    private final CallbackManager callbackManager;
    private final CallbackWaiter<Boolean> callbackWaiter = new CallbackWaiter<>(false);

    public KvSubscribeCallback(CallbackManager callbackManager2) {
        this.callbackManager = callbackManager2;
    }

    @Override // com.huawei.nb.callback.IKvSubscribeCallback
    public void onSuccess(int i) throws RemoteException {
        this.callbackWaiter.set(i, true);
    }

    @Override // com.huawei.nb.callback.IKvSubscribeCallback
    public void onFailure(int i, String str) throws RemoteException {
        this.callbackWaiter.set(i, false);
    }

    @Override // com.huawei.nb.client.callback.WaitableCallback
    public void interrupt() {
        this.callbackWaiter.interrupt();
    }

    @Override // com.huawei.nb.client.callback.WaitableCallback
    public Boolean await(int i, long j) {
        this.callbackManager.startWaiting(this);
        Boolean await = this.callbackWaiter.await(i, j);
        this.callbackManager.stopWaiting(this);
        return await;
    }
}
