package com.huawei.android.app;

import android.app.SynchronousUserSwitchObserver;
import android.os.RemoteException;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public abstract class SynchronousUserSwitchObserverEx {
    private SynchronousUserSwitchObserver mSynchronousUserSwitchObserver = new SynchronousUserSwitchObserver() {
        /* class com.huawei.android.app.SynchronousUserSwitchObserverEx.AnonymousClass1 */

        public void onUserSwitching(int newUserId) throws RemoteException {
            SynchronousUserSwitchObserverEx.this.onUserSwitching(newUserId);
        }
    };

    public void onUserSwitching(int newUserId) throws RemoteException {
    }

    public SynchronousUserSwitchObserver getSynchronousUserSwitchObserver() {
        return this.mSynchronousUserSwitchObserver;
    }
}
