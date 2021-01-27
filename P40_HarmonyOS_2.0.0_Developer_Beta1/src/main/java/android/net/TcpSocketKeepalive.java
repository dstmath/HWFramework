package android.net;

import android.net.SocketKeepalive;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Log;
import java.util.concurrent.Executor;

final class TcpSocketKeepalive extends SocketKeepalive {
    TcpSocketKeepalive(IConnectivityManager service, Network network, ParcelFileDescriptor pfd, Executor executor, SocketKeepalive.Callback callback) {
        super(service, network, pfd, executor, callback);
    }

    /* access modifiers changed from: package-private */
    @Override // android.net.SocketKeepalive
    public void startImpl(int intervalSec) {
        this.mExecutor.execute(new Runnable(intervalSec) {
            /* class android.net.$$Lambda$TcpSocketKeepalive$E1MP45uBTM6jPfrxAAqXFllEmAA */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                TcpSocketKeepalive.this.lambda$startImpl$0$TcpSocketKeepalive(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$startImpl$0$TcpSocketKeepalive(int intervalSec) {
        try {
            this.mService.startTcpKeepalive(this.mNetwork, this.mPfd.getFileDescriptor(), intervalSec, this.mCallback);
        } catch (RemoteException e) {
            Log.e("SocketKeepalive", "Error starting packet keepalive: ", e);
            throw e.rethrowFromSystemServer();
        }
    }

    /* access modifiers changed from: package-private */
    @Override // android.net.SocketKeepalive
    public void stopImpl() {
        this.mExecutor.execute(new Runnable() {
            /* class android.net.$$Lambda$TcpSocketKeepalive$XcFd1FxqMQjF6WWgzFIVR4hV2xk */

            @Override // java.lang.Runnable
            public final void run() {
                TcpSocketKeepalive.this.lambda$stopImpl$1$TcpSocketKeepalive();
            }
        });
    }

    public /* synthetic */ void lambda$stopImpl$1$TcpSocketKeepalive() {
        try {
            if (this.mSlot != null) {
                this.mService.stopKeepalive(this.mNetwork, this.mSlot.intValue());
            }
        } catch (RemoteException e) {
            Log.e("SocketKeepalive", "Error stopping packet keepalive: ", e);
            throw e.rethrowFromSystemServer();
        }
    }
}
