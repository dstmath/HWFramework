package com.android.server.media;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.IRemoteDisplayCallback;
import android.media.IRemoteDisplayProvider;
import android.media.RemoteDisplayState;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import android.util.Slog;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.Objects;

/* access modifiers changed from: package-private */
public final class RemoteDisplayProviderProxy implements ServiceConnection {
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final String TAG = "RemoteDisplayProvider";
    private Connection mActiveConnection;
    private boolean mBound;
    private final ComponentName mComponentName;
    private boolean mConnectionReady;
    private final Context mContext;
    private int mDiscoveryMode;
    private RemoteDisplayState mDisplayState;
    private Callback mDisplayStateCallback;
    private final Runnable mDisplayStateChanged = new Runnable() {
        /* class com.android.server.media.RemoteDisplayProviderProxy.AnonymousClass1 */

        @Override // java.lang.Runnable
        public void run() {
            RemoteDisplayProviderProxy.this.mScheduledDisplayStateChangedCallback = false;
            if (RemoteDisplayProviderProxy.this.mDisplayStateCallback != null) {
                Callback callback = RemoteDisplayProviderProxy.this.mDisplayStateCallback;
                RemoteDisplayProviderProxy remoteDisplayProviderProxy = RemoteDisplayProviderProxy.this;
                callback.onDisplayStateChanged(remoteDisplayProviderProxy, remoteDisplayProviderProxy.mDisplayState);
            }
        }
    };
    private final Handler mHandler;
    private boolean mRunning;
    private boolean mScheduledDisplayStateChangedCallback;
    private String mSelectedDisplayId;
    private final int mUserId;

    public interface Callback {
        void onDisplayStateChanged(RemoteDisplayProviderProxy remoteDisplayProviderProxy, RemoteDisplayState remoteDisplayState);
    }

    public RemoteDisplayProviderProxy(Context context, ComponentName componentName, int userId) {
        this.mContext = context;
        this.mComponentName = componentName;
        this.mUserId = userId;
        this.mHandler = new Handler();
    }

    public void dump(PrintWriter pw, String prefix) {
        pw.println(prefix + "Proxy");
        pw.println(prefix + "  mUserId=" + this.mUserId);
        pw.println(prefix + "  mRunning=" + this.mRunning);
        pw.println(prefix + "  mBound=" + this.mBound);
        pw.println(prefix + "  mActiveConnection=" + this.mActiveConnection);
        pw.println(prefix + "  mConnectionReady=" + this.mConnectionReady);
        pw.println(prefix + "  mDiscoveryMode=" + this.mDiscoveryMode);
        pw.println(prefix + "  mSelectedDisplayId=" + this.mSelectedDisplayId);
        pw.println(prefix + "  mDisplayState=" + this.mDisplayState);
    }

    public void setCallback(Callback callback) {
        this.mDisplayStateCallback = callback;
    }

    public RemoteDisplayState getDisplayState() {
        return this.mDisplayState;
    }

    public void setDiscoveryMode(int mode) {
        if (this.mDiscoveryMode != mode) {
            this.mDiscoveryMode = mode;
            if (this.mConnectionReady) {
                this.mActiveConnection.setDiscoveryMode(mode);
            }
            updateBinding();
        }
    }

    public void setSelectedDisplay(String id) {
        String str;
        if (!Objects.equals(this.mSelectedDisplayId, id)) {
            if (this.mConnectionReady && (str = this.mSelectedDisplayId) != null) {
                this.mActiveConnection.disconnect(str);
            }
            this.mSelectedDisplayId = id;
            if (this.mConnectionReady && id != null) {
                this.mActiveConnection.connect(id);
            }
            updateBinding();
        }
    }

    public void setDisplayVolume(int volume) {
        String str;
        if (this.mConnectionReady && (str = this.mSelectedDisplayId) != null) {
            this.mActiveConnection.setVolume(str, volume);
        }
    }

    public void adjustDisplayVolume(int delta) {
        String str;
        if (this.mConnectionReady && (str = this.mSelectedDisplayId) != null) {
            this.mActiveConnection.adjustVolume(str, delta);
        }
    }

    public boolean hasComponentName(String packageName, String className) {
        return this.mComponentName.getPackageName().equals(packageName) && this.mComponentName.getClassName().equals(className);
    }

    public String getFlattenedComponentName() {
        return this.mComponentName.flattenToShortString();
    }

    public void start() {
        if (!this.mRunning) {
            if (DEBUG) {
                Slog.d(TAG, this + ": Starting");
            }
            this.mRunning = true;
            updateBinding();
        }
    }

    public void stop() {
        if (this.mRunning) {
            if (DEBUG) {
                Slog.d(TAG, this + ": Stopping");
            }
            this.mRunning = false;
            updateBinding();
        }
    }

    public void rebindIfDisconnected() {
        if (this.mActiveConnection == null && shouldBind()) {
            unbind();
            bind();
        }
    }

    private void updateBinding() {
        if (shouldBind()) {
            bind();
        } else {
            unbind();
        }
    }

    private boolean shouldBind() {
        if (!this.mRunning) {
            return false;
        }
        if (this.mDiscoveryMode == 0 && this.mSelectedDisplayId == null) {
            return false;
        }
        return true;
    }

    private void bind() {
        if (!this.mBound) {
            if (DEBUG) {
                Slog.d(TAG, this + ": Binding");
            }
            Intent service = new Intent("com.android.media.remotedisplay.RemoteDisplayProvider");
            service.setComponent(this.mComponentName);
            try {
                this.mBound = this.mContext.bindServiceAsUser(service, this, 67108865, new UserHandle(this.mUserId));
                if (!this.mBound && DEBUG) {
                    Slog.d(TAG, this + ": Bind failed");
                }
            } catch (SecurityException ex) {
                if (DEBUG) {
                    Slog.d(TAG, this + ": Bind failed", ex);
                }
            }
        }
    }

    private void unbind() {
        if (this.mBound) {
            if (DEBUG) {
                Slog.d(TAG, this + ": Unbinding");
            }
            this.mBound = false;
            disconnect();
            this.mContext.unbindService(this);
        }
    }

    @Override // android.content.ServiceConnection
    public void onServiceConnected(ComponentName name, IBinder service) {
        if (DEBUG) {
            Slog.d(TAG, this + ": Connected");
        }
        if (this.mBound) {
            disconnect();
            IRemoteDisplayProvider provider = IRemoteDisplayProvider.Stub.asInterface(service);
            if (provider != null) {
                Connection connection = new Connection(provider);
                if (connection.register()) {
                    this.mActiveConnection = connection;
                } else if (DEBUG) {
                    Slog.d(TAG, this + ": Registration failed");
                }
            } else {
                Slog.e(TAG, this + ": Service returned invalid remote display provider binder");
            }
        }
    }

    @Override // android.content.ServiceConnection
    public void onServiceDisconnected(ComponentName name) {
        if (DEBUG) {
            Slog.d(TAG, this + ": Service disconnected");
        }
        disconnect();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onConnectionReady(Connection connection) {
        Connection connection2 = this.mActiveConnection;
        if (connection2 == connection) {
            this.mConnectionReady = true;
            int i = this.mDiscoveryMode;
            if (i != 0) {
                connection2.setDiscoveryMode(i);
            }
            String str = this.mSelectedDisplayId;
            if (str != null) {
                this.mActiveConnection.connect(str);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onConnectionDied(Connection connection) {
        if (this.mActiveConnection == connection) {
            if (DEBUG) {
                Slog.d(TAG, this + ": Service connection died");
            }
            disconnect();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onDisplayStateChanged(Connection connection, RemoteDisplayState state) {
        if (this.mActiveConnection == connection) {
            if (DEBUG) {
                Slog.d(TAG, this + ": State changed, state=" + state);
            }
            setDisplayState(state);
        }
    }

    private void disconnect() {
        Connection connection = this.mActiveConnection;
        if (connection != null) {
            String str = this.mSelectedDisplayId;
            if (str != null) {
                connection.disconnect(str);
            }
            this.mConnectionReady = false;
            this.mActiveConnection.dispose();
            this.mActiveConnection = null;
            setDisplayState(null);
        }
    }

    private void setDisplayState(RemoteDisplayState state) {
        if (!Objects.equals(this.mDisplayState, state)) {
            this.mDisplayState = state;
            if (!this.mScheduledDisplayStateChangedCallback) {
                this.mScheduledDisplayStateChangedCallback = true;
                this.mHandler.post(this.mDisplayStateChanged);
            }
        }
    }

    @Override // java.lang.Object
    public String toString() {
        return "Service connection " + this.mComponentName.flattenToShortString();
    }

    /* access modifiers changed from: private */
    public final class Connection implements IBinder.DeathRecipient {
        private final ProviderCallback mCallback = new ProviderCallback(this);
        private final IRemoteDisplayProvider mProvider;

        public Connection(IRemoteDisplayProvider provider) {
            this.mProvider = provider;
        }

        public boolean register() {
            try {
                this.mProvider.asBinder().linkToDeath(this, 0);
                this.mProvider.setCallback(this.mCallback);
                RemoteDisplayProviderProxy.this.mHandler.post(new Runnable() {
                    /* class com.android.server.media.RemoteDisplayProviderProxy.Connection.AnonymousClass1 */

                    @Override // java.lang.Runnable
                    public void run() {
                        RemoteDisplayProviderProxy.this.onConnectionReady(Connection.this);
                    }
                });
                return true;
            } catch (RemoteException e) {
                binderDied();
                return false;
            }
        }

        public void dispose() {
            this.mProvider.asBinder().unlinkToDeath(this, 0);
            this.mCallback.dispose();
        }

        public void setDiscoveryMode(int mode) {
            try {
                this.mProvider.setDiscoveryMode(mode);
            } catch (RemoteException ex) {
                Slog.e(RemoteDisplayProviderProxy.TAG, "Failed to deliver request to set discovery mode.", ex);
            }
        }

        public void connect(String id) {
            try {
                this.mProvider.connect(id);
            } catch (RemoteException ex) {
                Slog.e(RemoteDisplayProviderProxy.TAG, "Failed to deliver request to connect to display.", ex);
            }
        }

        public void disconnect(String id) {
            try {
                this.mProvider.disconnect(id);
            } catch (RemoteException ex) {
                Slog.e(RemoteDisplayProviderProxy.TAG, "Failed to deliver request to disconnect from display.", ex);
            }
        }

        public void setVolume(String id, int volume) {
            try {
                this.mProvider.setVolume(id, volume);
            } catch (RemoteException ex) {
                Slog.e(RemoteDisplayProviderProxy.TAG, "Failed to deliver request to set display volume.", ex);
            }
        }

        public void adjustVolume(String id, int volume) {
            try {
                this.mProvider.adjustVolume(id, volume);
            } catch (RemoteException ex) {
                Slog.e(RemoteDisplayProviderProxy.TAG, "Failed to deliver request to adjust display volume.", ex);
            }
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            RemoteDisplayProviderProxy.this.mHandler.post(new Runnable() {
                /* class com.android.server.media.RemoteDisplayProviderProxy.Connection.AnonymousClass2 */

                @Override // java.lang.Runnable
                public void run() {
                    RemoteDisplayProviderProxy.this.onConnectionDied(Connection.this);
                }
            });
        }

        /* access modifiers changed from: package-private */
        public void postStateChanged(final RemoteDisplayState state) {
            RemoteDisplayProviderProxy.this.mHandler.post(new Runnable() {
                /* class com.android.server.media.RemoteDisplayProviderProxy.Connection.AnonymousClass3 */

                @Override // java.lang.Runnable
                public void run() {
                    RemoteDisplayProviderProxy.this.onDisplayStateChanged(Connection.this, state);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    public static final class ProviderCallback extends IRemoteDisplayCallback.Stub {
        private final WeakReference<Connection> mConnectionRef;

        public ProviderCallback(Connection connection) {
            this.mConnectionRef = new WeakReference<>(connection);
        }

        public void dispose() {
            this.mConnectionRef.clear();
        }

        public void onStateChanged(RemoteDisplayState state) throws RemoteException {
            Connection connection = this.mConnectionRef.get();
            if (connection != null) {
                connection.postStateChanged(state);
            }
        }
    }
}
