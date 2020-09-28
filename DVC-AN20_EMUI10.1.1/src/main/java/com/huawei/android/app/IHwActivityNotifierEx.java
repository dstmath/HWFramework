package com.huawei.android.app;

import android.app.IHwActivityNotifier;
import android.os.Bundle;
import android.os.RemoteException;

public class IHwActivityNotifierEx {
    private IHwActivityNotifier mHwActivityNotifier = new IHwActivityNotifier.Stub() {
        /* class com.huawei.android.app.IHwActivityNotifierEx.AnonymousClass1 */

        public void call(Bundle extras) throws RemoteException {
            IHwActivityNotifierEx.this.call(extras);
        }
    };

    public void call(Bundle extras) {
    }

    public IHwActivityNotifier getHwActivityNotifier() {
        return this.mHwActivityNotifier;
    }
}
