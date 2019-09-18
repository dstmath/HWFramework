package com.huawei.nb.client.callback;

import android.os.RemoteException;
import com.huawei.nb.callback.IDeleteCallback;

public class DeleteCallback extends IDeleteCallback.Stub implements WaitableCallback<Integer> {
    private static final Integer INVALID_COUNT = -1;
    private final CallbackManager callbackManager;
    private final CallbackWaiter<Integer> callbackWaiter = new CallbackWaiter<>(INVALID_COUNT);

    DeleteCallback(CallbackManager callbackManager2) {
        this.callbackManager = callbackManager2;
    }

    public void onResult(int transactionId, int count) throws RemoteException {
        this.callbackWaiter.set(transactionId, Integer.valueOf(count));
    }

    public Integer await(int transactionId, long timeout) {
        this.callbackManager.startWaiting(this);
        Integer result = this.callbackWaiter.await(transactionId, timeout);
        this.callbackManager.stopWaiting(this);
        return result;
    }

    public void interrupt() {
        this.callbackWaiter.interrupt();
    }
}
