package com.huawei.android.app;

import android.app.IActivityController;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

public abstract class ActivityControllerEx {
    private static final String TAG = "ActivityControllerEx";
    private InnerActivityController innerActivityController = new InnerActivityController();

    public abstract boolean activityResuming(String str) throws RemoteException;

    public abstract boolean activityStarting(Intent intent, String str) throws RemoteException;

    public abstract boolean appCrashed(String str, int i, String str2, String str3, long j, String str4) throws RemoteException;

    public abstract int appEarlyNotResponding(String str, int i, String str2) throws RemoteException;

    public abstract int appNotResponding(String str, int i, String str2) throws RemoteException;

    public abstract int systemNotResponding(String str) throws RemoteException;

    /* access modifiers changed from: private */
    public class InnerActivityController extends IActivityController.Stub {
        private InnerActivityController() {
        }

        public boolean activityResuming(String pkgName) throws RemoteException {
            return ActivityControllerEx.this.activityResuming(pkgName);
        }

        public boolean activityStarting(Intent pIntent, String pkgName) throws RemoteException {
            return ActivityControllerEx.this.activityStarting(pIntent, pkgName);
        }

        public boolean appCrashed(String processName, int pid, String shortMsg, String longMsg, long timeMillis, String stackTrace) throws RemoteException {
            return ActivityControllerEx.this.appCrashed(processName, pid, shortMsg, longMsg, timeMillis, stackTrace);
        }

        public int appEarlyNotResponding(String processName, int pid, String annotation) throws RemoteException {
            return ActivityControllerEx.this.appEarlyNotResponding(processName, pid, annotation);
        }

        public int appNotResponding(String processName, int pid, String processStats) throws RemoteException {
            return ActivityControllerEx.this.appNotResponding(processName, pid, processStats);
        }

        public int systemNotResponding(String msg) throws RemoteException {
            return ActivityControllerEx.this.systemNotResponding(msg);
        }
    }

    public IBinder asBinder() {
        return this.innerActivityController.asBinder();
    }

    /* access modifiers changed from: protected */
    public boolean setCustomActivityController(ActivityControllerEx ctrl) {
        if (ctrl != null) {
            return HwActivityTaskManager.setCustomActivityController(IActivityController.Stub.asInterface(ctrl.asBinder()));
        }
        return HwActivityTaskManager.setCustomActivityController((IActivityController) null);
    }
}
