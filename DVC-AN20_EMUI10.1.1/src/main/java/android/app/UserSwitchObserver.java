package android.app;

import android.app.IUserSwitchObserver;
import android.os.IRemoteCallback;
import android.os.RemoteException;

public class UserSwitchObserver extends IUserSwitchObserver.Stub {
    @Override // android.app.IUserSwitchObserver
    public void onUserSwitching(int newUserId, IRemoteCallback reply) throws RemoteException {
        if (reply != null) {
            reply.sendResult(null);
        }
    }

    @Override // android.app.IUserSwitchObserver
    public void onUserSwitchComplete(int newUserId) throws RemoteException {
    }

    @Override // android.app.IUserSwitchObserver
    public void onForegroundProfileSwitch(int newProfileId) throws RemoteException {
    }

    @Override // android.app.IUserSwitchObserver
    public void onLockedBootComplete(int newUserId) throws RemoteException {
    }
}
