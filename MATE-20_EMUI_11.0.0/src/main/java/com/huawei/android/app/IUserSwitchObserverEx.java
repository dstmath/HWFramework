package com.huawei.android.app;

import android.app.IUserSwitchObserver;
import android.os.IRemoteCallback;
import android.os.RemoteException;
import com.huawei.android.os.IRemoteCallbackEx;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class IUserSwitchObserverEx {
    private IUserSwitchObserver mIUserSwitchObserver = new IUserSwitchObserver.Stub() {
        /* class com.huawei.android.app.IUserSwitchObserverEx.AnonymousClass1 */

        public void onUserSwitching(int newUserId, IRemoteCallback reply) throws RemoteException {
            IUserSwitchObserverEx.this.onUserSwitching(newUserId, new IRemoteCallbackEx(reply));
        }

        public void onUserSwitchComplete(int newUserId) throws RemoteException {
            IUserSwitchObserverEx.this.onUserSwitchComplete(newUserId);
        }

        public void onForegroundProfileSwitch(int newProfileId) {
        }

        public void onLockedBootComplete(int newUserId) {
        }
    };

    @HwSystemApi
    public void onUserSwitchComplete(int newUserId) {
    }

    @HwSystemApi
    public void onUserSwitching(int newUserId, IRemoteCallbackEx reply) {
    }

    public IUserSwitchObserver getIUserSwitchObserver() {
        return this.mIUserSwitchObserver;
    }
}
