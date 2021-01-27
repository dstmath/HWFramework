package com.android.server.jankshield;

import android.content.Context;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.util.Log;
import huawei.android.jankshield.IJankShield;
import huawei.android.jankshield.JankAppInfo;
import huawei.android.jankshield.JankCheckPerfBug;
import huawei.android.jankshield.JankProductInfo;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class JankShieldService extends IJankShield.Stub {
    public static final int MY_PID = Process.myPid();
    private static final String TAG = "JankShield";
    protected Context mContext = null;
    IPackageManager pm = null;

    public JankShieldService(Context context) {
        this.mContext = context;
    }

    public boolean getState(String name) throws RemoteException {
        if (checkCallingPermission("android.permission.DUMP") == 0) {
            return false;
        }
        String msg = "Permission Denied: can't getState from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid();
        Log.w(TAG, msg);
        throw new SecurityException(msg);
    }

    public JankCheckPerfBug checkPerfBug() {
        if (checkCallingPermission("android.permission.DUMP") == 0) {
            JankCheckPerfBug jankCheckBug = new JankCheckPerfBug();
            jankCheckBug.checkPerfBug(this.mContext);
            return jankCheckBug;
        }
        String msg = "Permission Denied: can't checkPerfBug from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid();
        Log.w(TAG, msg);
        throw new SecurityException(msg);
    }

    public JankAppInfo getJankAppInfo(String packageName) {
        if (checkCallingPermission("android.permission.DUMP") == 0) {
            JankAppInfo jankAppInfo = new JankAppInfo();
            PackageManager packageManager = this.mContext.getPackageManager();
            if (packageManager == null) {
                Log.w(TAG, "packageManager == null");
                return null;
            }
            try {
                PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
                if (packageInfo == null) {
                    return null;
                }
                jankAppInfo.packageName = packageInfo.packageName;
                jankAppInfo.versionCode = packageInfo.versionCode;
                jankAppInfo.versionName = packageInfo.versionName;
                jankAppInfo.coreApp = packageInfo.coreApp;
                jankAppInfo.flags = -1;
                jankAppInfo.systemApp = false;
                if (packageInfo.applicationInfo != null) {
                    jankAppInfo.flags = packageInfo.applicationInfo.flags;
                    if ((packageInfo.applicationInfo.flags & 1) != 0) {
                        jankAppInfo.systemApp = true;
                    }
                }
                return jankAppInfo;
            } catch (PackageManager.NameNotFoundException e) {
                Log.w(TAG, "Could not find packageinfo");
                return null;
            }
        } else {
            String msg = "Permission Denied: can't getJankAppInfo from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid();
            Log.w(TAG, msg);
            throw new SecurityException(msg);
        }
    }

    public JankProductInfo getJankProductInfo() {
        if (checkCallingPermission("android.permission.DUMP") == 0) {
            return new JankProductInfo();
        }
        String msg = "Permission Denied: can't getJankAppInfo from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid();
        Log.w(TAG, msg);
        throw new SecurityException(msg);
    }

    /* access modifiers changed from: package-private */
    public int checkCallingPermission(String permission) {
        return checkPermission(permission, Binder.getCallingPid(), UserHandle.getAppId(Binder.getCallingUid()));
    }

    public int checkPermission(String permission, int pid, int uid) {
        if (permission == null) {
            return -1;
        }
        if (pid == MY_PID || uid == 1000 || uid == 0) {
            return 0;
        }
        if (UserHandle.isIsolated(uid)) {
            return -1;
        }
        try {
            if (this.pm == null) {
                this.pm = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
            }
            if (this.pm != null) {
                return this.pm.checkUidPermission(permission, uid);
            }
            return -1;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (checkCallingPermission("android.permission.DUMP") != 0) {
            pw.println("Permission Denied: can't dump jankshield service from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
            return;
        }
        pw.println("JankShield is working ...");
    }
}
