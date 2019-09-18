package com.huawei.nb.client.callback;

import android.util.ArraySet;
import java.util.Iterator;
import java.util.function.Function;

public class CallbackManager {
    private final Object mLock = new Object();
    private final ArraySet<WaitableCallback> waitingCallbacks = new ArraySet<>();

    public void interruptAll() {
        synchronized (this.mLock) {
            Iterator<WaitableCallback> it = this.waitingCallbacks.iterator();
            while (it.hasNext()) {
                it.next().interrupt();
            }
            this.waitingCallbacks.clear();
        }
    }

    public void startWaiting(WaitableCallback callback) {
        if (callback != null) {
            synchronized (this.mLock) {
                this.waitingCallbacks.add(callback);
            }
        }
    }

    public void stopWaiting(WaitableCallback callback) {
        if (callback != null) {
            synchronized (this.mLock) {
                this.waitingCallbacks.remove(callback);
            }
        }
    }

    public <R> R createCallBack(Function<CallbackManager, R> function) {
        if (function == null) {
            return null;
        }
        return function.apply(this);
    }
}
