package com.huawei.android.app;

import android.app.UserSwitchObserver;
import android.os.Bundle;
import android.os.IRemoteCallback;
import android.os.RemoteException;

public class UserSwitchObserverEx {
    private UserSwitchObserver mUserSwitchObserver = new UserSwitchObserver() {
        /* class com.huawei.android.app.UserSwitchObserverEx.AnonymousClass1 */

        public void onUserSwitching(int newUserId, IRemoteCallback reply) throws RemoteException {
            UserSwitchObserverEx.this.onUserSwitching(newUserId, reply);
        }

        public void onUserSwitchComplete(int newUserId) throws RemoteException {
            UserSwitchObserverEx.this.onUserSwitchComplete(newUserId);
        }
    };

    public void onUserSwitching(int newUserId, IRemoteCallback reply) throws RemoteException {
        if (reply != null) {
            reply.sendResult((Bundle) null);
        }
    }

    public void onUserSwitchComplete(int newUserId) throws RemoteException {
    }

    public UserSwitchObserver getUserSwitchObserver() {
        return this.mUserSwitchObserver;
    }
}
