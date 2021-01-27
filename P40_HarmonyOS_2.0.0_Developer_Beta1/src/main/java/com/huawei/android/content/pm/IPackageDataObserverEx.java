package com.huawei.android.content.pm;

import android.content.pm.IPackageDataObserver;
import android.os.RemoteException;

public class IPackageDataObserverEx {
    public IPackageDataObserver mIPackageDataObserver = new IPackageDataObserver.Stub() {
        /* class com.huawei.android.content.pm.IPackageDataObserverEx.AnonymousClass1 */

        public void onRemoveCompleted(String packageName, boolean isSucceeded) throws RemoteException {
            IPackageDataObserverEx.this.onRemoveCompleted(packageName, isSucceeded);
        }
    };

    public void onRemoveCompleted(String packageName, boolean isSucceeded) throws RemoteException {
    }

    public IPackageDataObserver getIPackageDataObserver() {
        return this.mIPackageDataObserver;
    }
}
