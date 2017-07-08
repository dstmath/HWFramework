package com.android.server.media;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.IRemoteDisplayCallback.Stub;
import android.media.IRemoteDisplayProvider;
import android.media.RemoteDisplayState;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Slog;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.Objects;

final class RemoteDisplayProviderProxy implements ServiceConnection {
    private static final boolean DEBUG = false;
    private static final String TAG = "RemoteDisplayProvider";
    private Connection mActiveConnection;
    private boolean mBound;
    private final ComponentName mComponentName;
    private boolean mConnectionReady;
    private final Context mContext;
    private int mDiscoveryMode;
    private RemoteDisplayState mDisplayState;
    private Callback mDisplayStateCallback;
    private final Runnable mDisplayStateChanged;
    private final Handler mHandler;
    private boolean mRunning;
    private boolean mScheduledDisplayStateChangedCallback;
    private String mSelectedDisplayId;
    private final int mUserId;

    public interface Callback {
        void onDisplayStateChanged(RemoteDisplayProviderProxy remoteDisplayProviderProxy, RemoteDisplayState remoteDisplayState);
    }

    private final class Connection implements DeathRecipient {
        private final ProviderCallback mCallback;
        private final IRemoteDisplayProvider mProvider;

        /* renamed from: com.android.server.media.RemoteDisplayProviderProxy.Connection.3 */
        class AnonymousClass3 implements Runnable {
            final /* synthetic */ RemoteDisplayState val$state;

            AnonymousClass3(RemoteDisplayState val$state) {
                this.val$state = val$state;
            }

            public void run() {
                RemoteDisplayProviderProxy.this.onDisplayStateChanged(Connection.this, this.val$state);
            }
        }

        public Connection(IRemoteDisplayProvider provider) {
            this.mProvider = provider;
            this.mCallback = new ProviderCallback(this);
        }

        public boolean register() {
            try {
                this.mProvider.asBinder().linkToDeath(this, 0);
                this.mProvider.setCallback(this.mCallback);
                RemoteDisplayProviderProxy.this.mHandler.post(new Runnable() {
                    public void run() {
                        RemoteDisplayProviderProxy.this.onConnectionReady(Connection.this);
                    }
                });
                return true;
            } catch (RemoteException e) {
                binderDied();
                return RemoteDisplayProviderProxy.DEBUG;
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

        public void binderDied() {
            RemoteDisplayProviderProxy.this.mHandler.post(new Runnable() {
                public void run() {
                    RemoteDisplayProviderProxy.this.onConnectionDied(Connection.this);
                }
            });
        }

        void postStateChanged(RemoteDisplayState state) {
            RemoteDisplayProviderProxy.this.mHandler.post(new AnonymousClass3(state));
        }
    }

    private static final class ProviderCallback extends Stub {
        private final WeakReference<Connection> mConnectionRef;

        public ProviderCallback(Connection connection) {
            this.mConnectionRef = new WeakReference(connection);
        }

        public void dispose() {
            this.mConnectionRef.clear();
        }

        public void onStateChanged(RemoteDisplayState state) throws RemoteException {
            Connection connection = (Connection) this.mConnectionRef.get();
            if (connection != null) {
                connection.postStateChanged(state);
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.media.RemoteDisplayProviderProxy.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.media.RemoteDisplayProviderProxy.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.media.RemoteDisplayProviderProxy.<clinit>():void");
    }

    public RemoteDisplayProviderProxy(Context context, ComponentName componentName, int userId) {
        this.mDisplayStateChanged = new Runnable() {
            public void run() {
                RemoteDisplayProviderProxy.this.mScheduledDisplayStateChangedCallback = RemoteDisplayProviderProxy.DEBUG;
                if (RemoteDisplayProviderProxy.this.mDisplayStateCallback != null) {
                    RemoteDisplayProviderProxy.this.mDisplayStateCallback.onDisplayStateChanged(RemoteDisplayProviderProxy.this, RemoteDisplayProviderProxy.this.mDisplayState);
                }
            }
        };
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
        if (!Objects.equals(this.mSelectedDisplayId, id)) {
            if (this.mConnectionReady && this.mSelectedDisplayId != null) {
                this.mActiveConnection.disconnect(this.mSelectedDisplayId);
            }
            this.mSelectedDisplayId = id;
            if (this.mConnectionReady && id != null) {
                this.mActiveConnection.connect(id);
            }
            updateBinding();
        }
    }

    public void setDisplayVolume(int volume) {
        if (this.mConnectionReady && this.mSelectedDisplayId != null) {
            this.mActiveConnection.setVolume(this.mSelectedDisplayId, volume);
        }
    }

    public void adjustDisplayVolume(int delta) {
        if (this.mConnectionReady && this.mSelectedDisplayId != null) {
            this.mActiveConnection.adjustVolume(this.mSelectedDisplayId, delta);
        }
    }

    public boolean hasComponentName(String packageName, String className) {
        if (this.mComponentName.getPackageName().equals(packageName)) {
            return this.mComponentName.getClassName().equals(className);
        }
        return DEBUG;
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
            this.mRunning = DEBUG;
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
        if (!this.mRunning || (this.mDiscoveryMode == 0 && this.mSelectedDisplayId == null)) {
            return DEBUG;
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
            this.mBound = DEBUG;
            disconnect();
            this.mContext.unbindService(this);
        }
    }

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
                    return;
                } else if (DEBUG) {
                    Slog.d(TAG, this + ": Registration failed");
                    return;
                } else {
                    return;
                }
            }
            Slog.e(TAG, this + ": Service returned invalid remote display provider binder");
        }
    }

    public void onServiceDisconnected(ComponentName name) {
        if (DEBUG) {
            Slog.d(TAG, this + ": Service disconnected");
        }
        disconnect();
    }

    private void onConnectionReady(Connection connection) {
        if (this.mActiveConnection == connection) {
            this.mConnectionReady = true;
            if (this.mDiscoveryMode != 0) {
                this.mActiveConnection.setDiscoveryMode(this.mDiscoveryMode);
            }
            if (this.mSelectedDisplayId != null) {
                this.mActiveConnection.connect(this.mSelectedDisplayId);
            }
        }
    }

    private void onConnectionDied(Connection connection) {
        if (this.mActiveConnection == connection) {
            if (DEBUG) {
                Slog.d(TAG, this + ": Service connection died");
            }
            disconnect();
        }
    }

    private void onDisplayStateChanged(Connection connection, RemoteDisplayState state) {
        if (this.mActiveConnection == connection) {
            if (DEBUG) {
                Slog.d(TAG, this + ": State changed, state=" + state);
            }
            setDisplayState(state);
        }
    }

    private void disconnect() {
        if (this.mActiveConnection != null) {
            if (this.mSelectedDisplayId != null) {
                this.mActiveConnection.disconnect(this.mSelectedDisplayId);
            }
            this.mConnectionReady = DEBUG;
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

    public String toString() {
        return "Service connection " + this.mComponentName.flattenToShortString();
    }
}
