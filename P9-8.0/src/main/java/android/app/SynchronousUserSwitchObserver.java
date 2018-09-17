package android.app;

import android.os.IRemoteCallback;
import android.os.RemoteException;

public abstract class SynchronousUserSwitchObserver extends UserSwitchObserver {
    public abstract void onUserSwitching(int i) throws RemoteException;

    public final void onUserSwitching(int newUserId, IRemoteCallback reply) throws RemoteException {
        try {
            onUserSwitching(newUserId);
        } finally {
            if (reply != null) {
                reply.sendResult(null);
            }
        }
    }
}
