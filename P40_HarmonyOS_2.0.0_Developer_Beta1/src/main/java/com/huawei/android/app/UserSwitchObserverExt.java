package com.huawei.android.app;

import android.app.UserSwitchObserver;
import android.os.RemoteException;
import com.huawei.android.os.IRemoteCallbackExt;

public class UserSwitchObserverExt {
    private UserSwitchObserverBridge mBridge = new UserSwitchObserverBridge();

    public UserSwitchObserverExt() {
        this.mBridge.setUserSwitchObserverExt(this);
    }

    public void onUserSwitching(int newUserId, IRemoteCallbackExt reply) throws RemoteException {
    }

    public void onUserSwitchComplete(int newUserId) throws RemoteException {
    }

    public UserSwitchObserver getUserSwitchObserver() {
        return this.mBridge;
    }
}
