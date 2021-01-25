package com.huawei.nb.client.callback;

import android.os.RemoteException;
import com.huawei.nb.callback.IAIFetchCallback;
import com.huawei.nb.container.ObjectContainer;
import java.util.Collections;
import java.util.List;

public class AIFetchCallback extends IAIFetchCallback.Stub implements WaitableCallback<List> {
    private final CallbackManager callbackManager;
    private final CallbackWaiter<List> callbackWaiter = new CallbackWaiter<>(Collections.EMPTY_LIST);

    public AIFetchCallback(CallbackManager callbackManager2) {
        this.callbackManager = callbackManager2;
    }

    @Override // com.huawei.nb.callback.IAIFetchCallback
    public void onResult(int i, ObjectContainer objectContainer) throws RemoteException {
        this.callbackWaiter.set(i, objectContainer == null ? Collections.EMPTY_LIST : objectContainer.get());
    }

    @Override // com.huawei.nb.client.callback.WaitableCallback
    public List await(int i, long j) {
        this.callbackManager.startWaiting(this);
        List await = this.callbackWaiter.await(i, j);
        this.callbackManager.stopWaiting(this);
        return await;
    }

    @Override // com.huawei.nb.client.callback.WaitableCallback
    public void interrupt() {
        this.callbackWaiter.interrupt();
    }
}
