package com.android.server.pm;

import android.content.pm.PackageParser;
import android.hardware.biometrics.fingerprint.V2_1.RequestStatus;
import com.android.server.pm.Installer;

public class PackageManagerExceptionEx extends Exception {
    private PackageManagerException managerException;

    public PackageManagerExceptionEx(String detailMessage) {
        this.managerException = new PackageManagerException(detailMessage);
    }

    public PackageManagerExceptionEx(int error, String detailMessage) {
        this.managerException = new PackageManagerException(error, detailMessage);
    }

    public PackageManagerExceptionEx(int error, String detailMessage, Throwable throwable) {
        this.managerException = new PackageManagerException(error, detailMessage, throwable);
    }

    public PackageManagerExceptionEx(Throwable e) {
        this.managerException = new PackageManagerException(e);
    }

    public static void from(PackageParser.PackageParserException e) throws PackageManagerExceptionEx {
        throw new PackageManagerExceptionEx(e.error, e.getMessage(), e.getCause());
    }

    public static void from(Installer.InstallerException e) throws PackageManagerExceptionEx {
        throw new PackageManagerExceptionEx(RequestStatus.SYS_ETIMEDOUT, e.getMessage(), e.getCause());
    }

    public PackageManagerException getManagerException() {
        return this.managerException;
    }

    public void setManagerException(PackageManagerException managerException2) {
        this.managerException = managerException2;
    }
}
