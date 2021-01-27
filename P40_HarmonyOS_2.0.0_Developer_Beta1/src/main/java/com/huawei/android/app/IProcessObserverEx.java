package com.huawei.android.app;

import android.app.IProcessObserver;
import android.os.RemoteException;

public class IProcessObserverEx {
    private IProcessObserver mIProcessObserver = new IProcessObserver.Stub() {
        /* class com.huawei.android.app.IProcessObserverEx.AnonymousClass1 */

        public void onForegroundActivitiesChanged(int i, int i1, boolean b) throws RemoteException {
            IProcessObserverEx.this.onForegroundActivitiesChanged(i, i1, b);
        }

        public void onProcessDied(int i, int i1) throws RemoteException {
            IProcessObserverEx.this.onProcessDied(i, i1);
        }

        public void onForegroundServicesChanged(int pid, int uid, int serviceTypes) {
        }
    };

    public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
    }

    public void onProcessDied(int i, int i1) {
    }

    public IProcessObserver getIProcessObserver() {
        return this.mIProcessObserver;
    }
}
