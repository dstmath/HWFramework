package com.huawei.android.content.pm;

import android.content.pm.IPackageInstallObserver2;
import android.os.Bundle;
import android.os.RemoteException;

public class IPackageInstallObserver2Ex {
    private IPackageInstallObserver2 mPackageInstallObserver;

    public void setPackageInstallObserver(IPackageInstallObserver2 packageInstallObserver) {
        this.mPackageInstallObserver = packageInstallObserver;
    }

    public IPackageInstallObserver2 getPackageInstallObserver() {
        return this.mPackageInstallObserver;
    }

    public void onPackageInstalled(String basePackageName, int returnCode, String msg, Bundle extras) throws RemoteException {
        this.mPackageInstallObserver.onPackageInstalled(basePackageName, returnCode, msg, extras);
    }
}
