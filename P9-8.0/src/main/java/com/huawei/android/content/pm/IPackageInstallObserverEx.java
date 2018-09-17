package com.huawei.android.content.pm;

import android.content.pm.IPackageInstallObserver;
import android.content.pm.IPackageInstallObserver.Stub;
import android.os.RemoteException;

public class IPackageInstallObserverEx {
    private IPackageInstallObserver mPackageInstallObserver = new Stub() {
        public void packageInstalled(String packageName, int returnCode) throws RemoteException {
            IPackageInstallObserverEx.this.packageInstalled(packageName, returnCode);
        }
    };

    public void packageInstalled(String packageName, int returnCode) throws RemoteException {
    }

    public IPackageInstallObserver getPackageInstallObserver() {
        return this.mPackageInstallObserver;
    }
}
