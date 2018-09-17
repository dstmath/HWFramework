package com.android.commands.monkey;

import android.app.IActivityManager;
import android.content.pm.IPackageManager;
import android.content.pm.IPackageManager.Stub;
import android.content.pm.PermissionInfo;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.view.IWindowManager;

public class MonkeyPermissionEvent extends MonkeyEvent {
    private PermissionInfo mPermissionInfo;
    private String mPkg;

    public MonkeyPermissionEvent(String pkg, PermissionInfo permissionInfo) {
        super(7);
        this.mPkg = pkg;
        this.mPermissionInfo = permissionInfo;
    }

    public int injectEvent(IWindowManager iwm, IActivityManager iam, int verbose) {
        IPackageManager pm = Stub.asInterface(ServiceManager.getService("package"));
        try {
            boolean grant = pm.checkPermission(this.mPermissionInfo.name, this.mPkg, UserHandle.myUserId()) == -1;
            Logger logger = Logger.out;
            String str = ":Permission %s %s to package %s";
            Object[] objArr = new Object[3];
            objArr[0] = grant ? "grant" : "revoke";
            objArr[1] = this.mPermissionInfo.name;
            objArr[2] = this.mPkg;
            logger.println(String.format(str, objArr));
            if (grant) {
                pm.grantRuntimePermission(this.mPkg, this.mPermissionInfo.name, UserHandle.myUserId());
            } else {
                pm.revokeRuntimePermission(this.mPkg, this.mPermissionInfo.name, UserHandle.myUserId());
            }
            return 1;
        } catch (RemoteException e) {
            return -1;
        }
    }
}
