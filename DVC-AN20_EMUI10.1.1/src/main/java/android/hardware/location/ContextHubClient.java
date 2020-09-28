package android.hardware.location;

import android.annotation.SystemApi;
import android.os.RemoteException;
import com.android.internal.util.Preconditions;
import dalvik.system.CloseGuard;
import java.io.Closeable;
import java.util.concurrent.atomic.AtomicBoolean;

@SystemApi
public class ContextHubClient implements Closeable {
    private final ContextHubInfo mAttachedHub;
    private IContextHubClient mClientProxy = null;
    private final CloseGuard mCloseGuard;
    private final AtomicBoolean mIsClosed = new AtomicBoolean(false);
    private final boolean mPersistent;

    ContextHubClient(ContextHubInfo hubInfo, boolean persistent) {
        this.mAttachedHub = hubInfo;
        this.mPersistent = persistent;
        if (this.mPersistent) {
            this.mCloseGuard = null;
            return;
        }
        this.mCloseGuard = CloseGuard.get();
        this.mCloseGuard.open("close");
    }

    /* access modifiers changed from: package-private */
    public void setClientProxy(IContextHubClient clientProxy) {
        Preconditions.checkNotNull(clientProxy, "IContextHubClient cannot be null");
        if (this.mClientProxy == null) {
            this.mClientProxy = clientProxy;
            return;
        }
        throw new IllegalStateException("Cannot change client proxy multiple times");
    }

    public ContextHubInfo getAttachedHub() {
        return this.mAttachedHub;
    }

    @Override // java.io.Closeable, java.lang.AutoCloseable
    public void close() {
        if (!this.mIsClosed.getAndSet(true)) {
            CloseGuard closeGuard = this.mCloseGuard;
            if (closeGuard != null) {
                closeGuard.close();
            }
            try {
                this.mClientProxy.close();
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public int sendMessageToNanoApp(NanoAppMessage message) {
        Preconditions.checkNotNull(message, "NanoAppMessage cannot be null");
        try {
            return this.mClientProxy.sendMessageToNanoApp(message);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public void finalize() throws Throwable {
        try {
            if (this.mCloseGuard != null) {
                this.mCloseGuard.warnIfOpen();
            }
            if (!this.mPersistent) {
                close();
            }
        } finally {
            super.finalize();
        }
    }
}
