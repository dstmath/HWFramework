package com.huawei.android.app;

import android.app.UserSwitchObserver;
import android.os.IRemoteCallback;
import android.os.RemoteException;
import com.huawei.android.os.IRemoteCallbackExt;

public class UserSwitchObserverBridge extends UserSwitchObserver {
    private UserSwitchObserverExt mUserSwitchObserverExt;

    public void setUserSwitchObserverExt(UserSwitchObserverExt userSwitchObserverExt) {
        this.mUserSwitchObserverExt = userSwitchObserverExt;
    }

    public void onUserSwitching(int newUserId, IRemoteCallback reply) throws RemoteException {
        UserSwitchObserverExt userSwitchObserverExt = this.mUserSwitchObserverExt;
        if (userSwitchObserverExt == null) {
            return;
        }
        if (reply != null) {
            IRemoteCallbackExt iRemoteCallbackExt = new IRemoteCallbackExt();
            iRemoteCallbackExt.setIRemoteCallback(reply);
            this.mUserSwitchObserverExt.onUserSwitching(newUserId, iRemoteCallbackExt);
            return;
        }
        userSwitchObserverExt.onUserSwitching(newUserId, null);
    }

    public void onUserSwitchComplete(int newUserId) throws RemoteException {
        UserSwitchObserverExt userSwitchObserverExt = this.mUserSwitchObserverExt;
        if (userSwitchObserverExt != null) {
            userSwitchObserverExt.onUserSwitchComplete(newUserId);
        }
    }
}
