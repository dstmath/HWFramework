package com.huawei.android.app;

import android.app.UserSwitchObserver;
import android.os.IRemoteCallback;
import android.os.RemoteException;

public class UserSwitchObserverEx {
    private UserSwitchObserver mUserSwitchObserver = new UserSwitchObserver() {
        public void onUserSwitching(int newUserId, IRemoteCallback reply) throws RemoteException {
            UserSwitchObserverEx.this.onUserSwitching(newUserId, reply);
        }

        public void onUserSwitchComplete(int newUserId) throws RemoteException {
            UserSwitchObserverEx.this.onUserSwitchComplete(newUserId);
        }
    };

    public void onUserSwitching(int newUserId, IRemoteCallback reply) throws RemoteException {
    }

    public void onUserSwitchComplete(int newUserId) throws RemoteException {
    }

    public UserSwitchObserver getUserSwitchObserver() {
        return this.mUserSwitchObserver;
    }
}
