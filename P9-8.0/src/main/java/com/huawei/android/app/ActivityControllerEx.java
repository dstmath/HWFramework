package com.huawei.android.app;

import android.app.ActivityManagerNative;
import android.app.IActivityController.Stub;
import android.content.Intent;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.android.os.HwTransCodeEx;

public abstract class ActivityControllerEx {
    private static final int SET_CUSTOM_ACTIVITY_CONTROLLER_TRANSACTION = 2101;
    private static final String TAG = "ActivityControllerEx";
    private InnerActivityController innerActivityController = new InnerActivityController(this, null);

    private class InnerActivityController extends Stub {
        /* synthetic */ InnerActivityController(ActivityControllerEx this$0, InnerActivityController -this1) {
            this();
        }

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

    public abstract boolean activityResuming(String str) throws RemoteException;

    public abstract boolean activityStarting(Intent intent, String str) throws RemoteException;

    public abstract boolean appCrashed(String str, int i, String str2, String str3, long j, String str4) throws RemoteException;

    public abstract int appEarlyNotResponding(String str, int i, String str2) throws RemoteException;

    public abstract int appNotResponding(String str, int i, String str2) throws RemoteException;

    public abstract int systemNotResponding(String str) throws RemoteException;

    public IBinder asBinder() {
        return this.innerActivityController.asBinder();
    }

    protected boolean setCustomActivityController(ActivityControllerEx ctrl) {
        boolean retVal = true;
        Parcel parcel = null;
        Parcel parcel2 = null;
        try {
            parcel = Parcel.obtain();
            parcel2 = Parcel.obtain();
            parcel.writeInterfaceToken(HwTransCodeEx.ACTIVITYMANAGER_DESCRIPTOR);
            if (ctrl != null) {
                parcel.writeStrongBinder(ctrl.asBinder());
            } else {
                parcel.writeStrongBinder(null);
            }
            if (!ActivityManagerNative.getDefault().asBinder().transact(SET_CUSTOM_ACTIVITY_CONTROLLER_TRANSACTION, parcel, parcel2, 0)) {
                retVal = false;
                Log.e(TAG, "Transact to activity manager failed! AcitivtyController logic invalid!");
            }
            if (parcel != null) {
                parcel.recycle();
            }
            if (parcel2 != null) {
                parcel2.recycle();
            }
        } catch (RemoteException e) {
            retVal = false;
            if (parcel != null) {
                parcel.recycle();
            }
            if (parcel2 != null) {
                parcel2.recycle();
            }
        } catch (Throwable th) {
            if (parcel != null) {
                parcel.recycle();
            }
            if (parcel2 != null) {
                parcel2.recycle();
            }
        }
        Log.i(TAG, "enableActivityController end. retVal = " + retVal);
        return retVal;
    }
}
