package android.app;

import android.app.IUserSwitchObserver.Stub;
import android.os.IRemoteCallback;
import android.os.RemoteException;

public abstract class SynchronousUserSwitchObserver extends Stub {
    public abstract void onUserSwitching(int i) throws RemoteException;

    public final void onUserSwitching(int newUserId, IRemoteCallback reply) throws RemoteException {
        try {
            onUserSwitching(newUserId);
            if (reply != null) {
                reply.sendResult(null);
            }
        } catch (Throwable th) {
            if (reply != null) {
                reply.sendResult(null);
            }
        }
    }
}
