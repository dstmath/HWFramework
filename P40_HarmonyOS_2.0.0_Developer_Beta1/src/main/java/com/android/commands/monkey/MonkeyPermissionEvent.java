package com.android.commands.monkey;

import android.app.ActivityManager;
import android.app.IActivityManager;
import android.content.pm.IPackageManager;
import android.content.pm.PermissionInfo;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.view.IWindowManager;

public class MonkeyPermissionEvent extends MonkeyEvent {
    private PermissionInfo mPermissionInfo;
    private String mPkg;

    public MonkeyPermissionEvent(String pkg, PermissionInfo permissionInfo) {
        super(7);
        this.mPkg = pkg;
        this.mPermissionInfo = permissionInfo;
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x002e: APUT  (r9v1 java.lang.Object[]), (0 ??[int, short, byte, char]), (r10v0 java.lang.String) */
    @Override // com.android.commands.monkey.MonkeyEvent
    public int injectEvent(IWindowManager iwm, IActivityManager iam, int verbose) {
        IPackageManager pm = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
        int currentUser = ActivityManager.getCurrentUser();
        try {
            boolean grant = pm.checkPermission(this.mPermissionInfo.name, this.mPkg, currentUser) == -1;
            Logger logger = Logger.out;
            Object[] objArr = new Object[3];
            objArr[0] = grant ? "grant" : "revoke";
            objArr[1] = this.mPermissionInfo.name;
            objArr[2] = this.mPkg;
            logger.println(String.format(":Permission %s %s to package %s", objArr));
            if (grant) {
                pm.grantRuntimePermission(this.mPkg, this.mPermissionInfo.name, currentUser);
            } else {
                pm.revokeRuntimePermission(this.mPkg, this.mPermissionInfo.name, currentUser);
            }
            return 1;
        } catch (RemoteException e) {
            return -1;
        }
    }
}
