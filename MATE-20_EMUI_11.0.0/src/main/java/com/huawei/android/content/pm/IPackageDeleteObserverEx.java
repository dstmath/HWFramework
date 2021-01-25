package com.huawei.android.content.pm;

import android.content.pm.IPackageDeleteObserver;
import android.os.RemoteException;

public class IPackageDeleteObserverEx {
    private IPackageDeleteObserver mPackageDeleteObserver = new IPackageDeleteObserver.Stub() {
        /* class com.huawei.android.content.pm.IPackageDeleteObserverEx.AnonymousClass1 */

        public void packageDeleted(String packageName, int returnCode) throws RemoteException {
            IPackageDeleteObserverEx.this.packageDeleted(packageName, returnCode);
        }
    };

    public void packageDeleted(String packageName, int returnCode) throws RemoteException {
    }

    public IPackageDeleteObserver getPackageDeleteObserver() {
        return this.mPackageDeleteObserver;
    }
}
