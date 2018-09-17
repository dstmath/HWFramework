package com.android.server.tv;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.tv.ITvRemoteProvider;
import android.media.tv.ITvRemoteServiceInput.Stub;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import android.util.Slog;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;

final class TvRemoteProviderProxy implements ServiceConnection {
    private static final boolean DEBUG = Log.isLoggable(TAG, 2);
    private static final boolean DEBUG_KEY = false;
    protected static final String SERVICE_INTERFACE = "com.android.media.tv.remoteprovider.TvRemoteProvider";
    private static final String TAG = "TvRemoteProvProxy";
    private Connection mActiveConnection;
    private boolean mBound;
    private final ComponentName mComponentName;
    private boolean mConnectionReady;
    private final Context mContext;
    private final Handler mHandler;
    private final Object mLock = new Object();
    private ProviderMethods mProviderMethods;
    private boolean mRunning;
    private final int mUid;
    private final int mUserId;

    private final class Connection implements DeathRecipient {
        private final RemoteServiceInputProvider mServiceInputProvider = new RemoteServiceInputProvider(this);
        private final ITvRemoteProvider mTvRemoteProvider;

        public Connection(ITvRemoteProvider provider) {
            this.mTvRemoteProvider = provider;
        }

        public boolean register() {
            if (TvRemoteProviderProxy.DEBUG) {
                Slog.d(TvRemoteProviderProxy.TAG, "Connection::register()");
            }
            try {
                this.mTvRemoteProvider.asBinder().linkToDeath(this, 0);
                this.mTvRemoteProvider.setRemoteServiceInputSink(this.mServiceInputProvider);
                TvRemoteProviderProxy.this.mHandler.post(new Runnable() {
                    public void run() {
                        TvRemoteProviderProxy.this.onConnectionReady(Connection.this);
                    }
                });
                return true;
            } catch (RemoteException e) {
                binderDied();
                return false;
            }
        }

        public void dispose() {
            if (TvRemoteProviderProxy.DEBUG) {
                Slog.d(TvRemoteProviderProxy.TAG, "Connection::dispose()");
            }
            this.mTvRemoteProvider.asBinder().unlinkToDeath(this, 0);
            this.mServiceInputProvider.dispose();
        }

        public void onInputBridgeConnected(IBinder token) {
            if (TvRemoteProviderProxy.DEBUG) {
                Slog.d(TvRemoteProviderProxy.TAG, this + ": onInputBridgeConnected");
            }
            try {
                this.mTvRemoteProvider.onInputBridgeConnected(token);
            } catch (RemoteException ex) {
                Slog.e(TvRemoteProviderProxy.TAG, "Failed to deliver onInputBridgeConnected. ", ex);
            }
        }

        public void binderDied() {
            TvRemoteProviderProxy.this.mHandler.post(new Runnable() {
                public void run() {
                    TvRemoteProviderProxy.this.onConnectionDied(Connection.this);
                }
            });
        }

        void openInputBridge(IBinder token, String name, int width, int height, int maxPointers) {
            synchronized (TvRemoteProviderProxy.this.mLock) {
                if (TvRemoteProviderProxy.this.mActiveConnection == this && Binder.getCallingUid() == TvRemoteProviderProxy.this.mUid) {
                    if (TvRemoteProviderProxy.DEBUG) {
                        Slog.d(TvRemoteProviderProxy.TAG, this + ": openInputBridge," + " token=" + token + ", name=" + name);
                    }
                    long idToken = Binder.clearCallingIdentity();
                    try {
                        if (TvRemoteProviderProxy.this.mProviderMethods != null) {
                            TvRemoteProviderProxy.this.mProviderMethods.openInputBridge(TvRemoteProviderProxy.this, token, name, width, height, maxPointers);
                        }
                        Binder.restoreCallingIdentity(idToken);
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(idToken);
                    }
                } else if (TvRemoteProviderProxy.DEBUG) {
                    Slog.w(TvRemoteProviderProxy.TAG, "openInputBridge, Invalid connection or incorrect uid: " + Binder.getCallingUid());
                }
            }
        }

        void closeInputBridge(IBinder token) {
            synchronized (TvRemoteProviderProxy.this.mLock) {
                if (TvRemoteProviderProxy.this.mActiveConnection == this && Binder.getCallingUid() == TvRemoteProviderProxy.this.mUid) {
                    if (TvRemoteProviderProxy.DEBUG) {
                        Slog.d(TvRemoteProviderProxy.TAG, this + ": closeInputBridge," + " token=" + token);
                    }
                    long idToken = Binder.clearCallingIdentity();
                    try {
                        if (TvRemoteProviderProxy.this.mProviderMethods != null) {
                            TvRemoteProviderProxy.this.mProviderMethods.closeInputBridge(TvRemoteProviderProxy.this, token);
                        }
                        Binder.restoreCallingIdentity(idToken);
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(idToken);
                    }
                } else if (TvRemoteProviderProxy.DEBUG) {
                    Slog.w(TvRemoteProviderProxy.TAG, "closeInputBridge, Invalid connection or incorrect uid: " + Binder.getCallingUid());
                }
            }
        }

        void clearInputBridge(IBinder token) {
            synchronized (TvRemoteProviderProxy.this.mLock) {
                if (TvRemoteProviderProxy.this.mActiveConnection == this && Binder.getCallingUid() == TvRemoteProviderProxy.this.mUid) {
                    if (TvRemoteProviderProxy.DEBUG) {
                        Slog.d(TvRemoteProviderProxy.TAG, this + ": clearInputBridge," + " token=" + token);
                    }
                    long idToken = Binder.clearCallingIdentity();
                    try {
                        if (TvRemoteProviderProxy.this.mProviderMethods != null) {
                            TvRemoteProviderProxy.this.mProviderMethods.clearInputBridge(TvRemoteProviderProxy.this, token);
                        }
                        Binder.restoreCallingIdentity(idToken);
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(idToken);
                    }
                } else if (TvRemoteProviderProxy.DEBUG) {
                    Slog.w(TvRemoteProviderProxy.TAG, "clearInputBridge, Invalid connection or incorrect uid: " + Binder.getCallingUid());
                }
            }
        }

        void sendTimestamp(IBinder token, long timestamp) {
            synchronized (TvRemoteProviderProxy.this.mLock) {
                if (TvRemoteProviderProxy.this.mActiveConnection == this && Binder.getCallingUid() == TvRemoteProviderProxy.this.mUid) {
                    long idToken = Binder.clearCallingIdentity();
                    try {
                        if (TvRemoteProviderProxy.this.mProviderMethods != null) {
                            TvRemoteProviderProxy.this.mProviderMethods.sendTimeStamp(TvRemoteProviderProxy.this, token, timestamp);
                        }
                        Binder.restoreCallingIdentity(idToken);
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(idToken);
                    }
                } else if (TvRemoteProviderProxy.DEBUG) {
                    Slog.w(TvRemoteProviderProxy.TAG, "sendTimeStamp, Invalid connection or incorrect uid: " + Binder.getCallingUid());
                }
            }
        }

        void sendKeyDown(IBinder token, int keyCode) {
            synchronized (TvRemoteProviderProxy.this.mLock) {
                if (TvRemoteProviderProxy.this.mActiveConnection == this && Binder.getCallingUid() == TvRemoteProviderProxy.this.mUid) {
                    long idToken = Binder.clearCallingIdentity();
                    try {
                        if (TvRemoteProviderProxy.this.mProviderMethods != null) {
                            TvRemoteProviderProxy.this.mProviderMethods.sendKeyDown(TvRemoteProviderProxy.this, token, keyCode);
                        }
                        Binder.restoreCallingIdentity(idToken);
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(idToken);
                    }
                } else if (TvRemoteProviderProxy.DEBUG) {
                    Slog.w(TvRemoteProviderProxy.TAG, "sendKeyDown, Invalid connection or incorrect uid: " + Binder.getCallingUid());
                }
            }
        }

        void sendKeyUp(IBinder token, int keyCode) {
            synchronized (TvRemoteProviderProxy.this.mLock) {
                if (TvRemoteProviderProxy.this.mActiveConnection == this && Binder.getCallingUid() == TvRemoteProviderProxy.this.mUid) {
                    long idToken = Binder.clearCallingIdentity();
                    try {
                        if (TvRemoteProviderProxy.this.mProviderMethods != null) {
                            TvRemoteProviderProxy.this.mProviderMethods.sendKeyUp(TvRemoteProviderProxy.this, token, keyCode);
                        }
                        Binder.restoreCallingIdentity(idToken);
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(idToken);
                    }
                } else if (TvRemoteProviderProxy.DEBUG) {
                    Slog.w(TvRemoteProviderProxy.TAG, "sendKeyUp, Invalid connection or incorrect uid: " + Binder.getCallingUid());
                }
            }
        }

        void sendPointerDown(IBinder token, int pointerId, int x, int y) {
            synchronized (TvRemoteProviderProxy.this.mLock) {
                if (TvRemoteProviderProxy.this.mActiveConnection == this && Binder.getCallingUid() == TvRemoteProviderProxy.this.mUid) {
                    long idToken = Binder.clearCallingIdentity();
                    try {
                        if (TvRemoteProviderProxy.this.mProviderMethods != null) {
                            TvRemoteProviderProxy.this.mProviderMethods.sendPointerDown(TvRemoteProviderProxy.this, token, pointerId, x, y);
                        }
                        Binder.restoreCallingIdentity(idToken);
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(idToken);
                    }
                } else if (TvRemoteProviderProxy.DEBUG) {
                    Slog.w(TvRemoteProviderProxy.TAG, "sendPointerDown, Invalid connection or incorrect uid: " + Binder.getCallingUid());
                }
            }
        }

        void sendPointerUp(IBinder token, int pointerId) {
            synchronized (TvRemoteProviderProxy.this.mLock) {
                if (TvRemoteProviderProxy.this.mActiveConnection == this && Binder.getCallingUid() == TvRemoteProviderProxy.this.mUid) {
                    long idToken = Binder.clearCallingIdentity();
                    try {
                        if (TvRemoteProviderProxy.this.mProviderMethods != null) {
                            TvRemoteProviderProxy.this.mProviderMethods.sendPointerUp(TvRemoteProviderProxy.this, token, pointerId);
                        }
                        Binder.restoreCallingIdentity(idToken);
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(idToken);
                    }
                } else if (TvRemoteProviderProxy.DEBUG) {
                    Slog.w(TvRemoteProviderProxy.TAG, "sendPointerUp, Invalid connection or incorrect uid: " + Binder.getCallingUid());
                }
            }
        }

        void sendPointerSync(IBinder token) {
            synchronized (TvRemoteProviderProxy.this.mLock) {
                if (TvRemoteProviderProxy.this.mActiveConnection == this && Binder.getCallingUid() == TvRemoteProviderProxy.this.mUid) {
                    long idToken = Binder.clearCallingIdentity();
                    try {
                        if (TvRemoteProviderProxy.this.mProviderMethods != null) {
                            TvRemoteProviderProxy.this.mProviderMethods.sendPointerSync(TvRemoteProviderProxy.this, token);
                        }
                        Binder.restoreCallingIdentity(idToken);
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(idToken);
                    }
                } else if (TvRemoteProviderProxy.DEBUG) {
                    Slog.w(TvRemoteProviderProxy.TAG, "sendPointerSync, Invalid connection or incorrect uid: " + Binder.getCallingUid());
                }
            }
        }
    }

    public interface ProviderMethods {
        void clearInputBridge(TvRemoteProviderProxy tvRemoteProviderProxy, IBinder iBinder);

        void closeInputBridge(TvRemoteProviderProxy tvRemoteProviderProxy, IBinder iBinder);

        void openInputBridge(TvRemoteProviderProxy tvRemoteProviderProxy, IBinder iBinder, String str, int i, int i2, int i3);

        void sendKeyDown(TvRemoteProviderProxy tvRemoteProviderProxy, IBinder iBinder, int i);

        void sendKeyUp(TvRemoteProviderProxy tvRemoteProviderProxy, IBinder iBinder, int i);

        void sendPointerDown(TvRemoteProviderProxy tvRemoteProviderProxy, IBinder iBinder, int i, int i2, int i3);

        void sendPointerSync(TvRemoteProviderProxy tvRemoteProviderProxy, IBinder iBinder);

        void sendPointerUp(TvRemoteProviderProxy tvRemoteProviderProxy, IBinder iBinder, int i);

        void sendTimeStamp(TvRemoteProviderProxy tvRemoteProviderProxy, IBinder iBinder, long j);
    }

    private static final class RemoteServiceInputProvider extends Stub {
        private final WeakReference<Connection> mConnectionRef;

        public RemoteServiceInputProvider(Connection connection) {
            this.mConnectionRef = new WeakReference(connection);
        }

        public void dispose() {
            this.mConnectionRef.clear();
        }

        public void openInputBridge(IBinder token, String name, int width, int height, int maxPointers) throws RemoteException {
            Connection connection = (Connection) this.mConnectionRef.get();
            if (connection != null) {
                connection.openInputBridge(token, name, width, height, maxPointers);
            }
        }

        public void closeInputBridge(IBinder token) throws RemoteException {
            Connection connection = (Connection) this.mConnectionRef.get();
            if (connection != null) {
                connection.closeInputBridge(token);
            }
        }

        public void clearInputBridge(IBinder token) throws RemoteException {
            Connection connection = (Connection) this.mConnectionRef.get();
            if (connection != null) {
                connection.clearInputBridge(token);
            }
        }

        public void sendTimestamp(IBinder token, long timestamp) throws RemoteException {
            Connection connection = (Connection) this.mConnectionRef.get();
            if (connection != null) {
                connection.sendTimestamp(token, timestamp);
            }
        }

        public void sendKeyDown(IBinder token, int keyCode) throws RemoteException {
            Connection connection = (Connection) this.mConnectionRef.get();
            if (connection != null) {
                connection.sendKeyDown(token, keyCode);
            }
        }

        public void sendKeyUp(IBinder token, int keyCode) throws RemoteException {
            Connection connection = (Connection) this.mConnectionRef.get();
            if (connection != null) {
                connection.sendKeyUp(token, keyCode);
            }
        }

        public void sendPointerDown(IBinder token, int pointerId, int x, int y) throws RemoteException {
            Connection connection = (Connection) this.mConnectionRef.get();
            if (connection != null) {
                connection.sendPointerDown(token, pointerId, x, y);
            }
        }

        public void sendPointerUp(IBinder token, int pointerId) throws RemoteException {
            Connection connection = (Connection) this.mConnectionRef.get();
            if (connection != null) {
                connection.sendPointerUp(token, pointerId);
            }
        }

        public void sendPointerSync(IBinder token) throws RemoteException {
            Connection connection = (Connection) this.mConnectionRef.get();
            if (connection != null) {
                connection.sendPointerSync(token);
            }
        }
    }

    public TvRemoteProviderProxy(Context context, ComponentName componentName, int userId, int uid) {
        this.mContext = context;
        this.mComponentName = componentName;
        this.mUserId = userId;
        this.mUid = uid;
        this.mHandler = new Handler();
    }

    public void dump(PrintWriter pw, String prefix) {
        pw.println(prefix + "Proxy");
        pw.println(prefix + "  mUserId=" + this.mUserId);
        pw.println(prefix + "  mRunning=" + this.mRunning);
        pw.println(prefix + "  mBound=" + this.mBound);
        pw.println(prefix + "  mActiveConnection=" + this.mActiveConnection);
        pw.println(prefix + "  mConnectionReady=" + this.mConnectionReady);
    }

    public void setProviderSink(ProviderMethods provider) {
        this.mProviderMethods = provider;
    }

    public boolean hasComponentName(String packageName, String className) {
        if (this.mComponentName.getPackageName().equals(packageName)) {
            return this.mComponentName.getClassName().equals(className);
        }
        return false;
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
        synchronized (this.mLock) {
            if (this.mActiveConnection == null && shouldBind()) {
                unbind();
                bind();
            }
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
        return this.mRunning;
    }

    private void bind() {
        if (!this.mBound) {
            if (DEBUG) {
                Slog.d(TAG, this + ": Binding");
            }
            Intent service = new Intent(SERVICE_INTERFACE);
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

    public void onServiceConnected(ComponentName name, IBinder service) {
        if (DEBUG) {
            Slog.d(TAG, this + ": onServiceConnected()");
        }
        if (this.mBound) {
            disconnect();
            ITvRemoteProvider provider = ITvRemoteProvider.Stub.asInterface(service);
            if (provider != null) {
                Connection connection = new Connection(provider);
                if (connection.register()) {
                    synchronized (this.mLock) {
                        this.mActiveConnection = connection;
                    }
                    if (DEBUG) {
                        Slog.d(TAG, this + ": Connected successfully.");
                        return;
                    }
                    return;
                } else if (DEBUG) {
                    Slog.d(TAG, this + ": Registration failed");
                    return;
                } else {
                    return;
                }
            }
            Slog.e(TAG, this + ": Service returned invalid remote-control provider binder");
        }
    }

    public void onServiceDisconnected(ComponentName name) {
        if (DEBUG) {
            Slog.d(TAG, this + ": Service disconnected");
        }
        disconnect();
    }

    private void onConnectionReady(Connection connection) {
        synchronized (this.mLock) {
            if (DEBUG) {
                Slog.d(TAG, "onConnectionReady");
            }
            if (this.mActiveConnection == connection) {
                if (DEBUG) {
                    Slog.d(TAG, "mConnectionReady = true");
                }
                this.mConnectionReady = true;
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

    private void disconnect() {
        synchronized (this.mLock) {
            if (this.mActiveConnection != null) {
                this.mConnectionReady = false;
                this.mActiveConnection.dispose();
                this.mActiveConnection = null;
            }
        }
    }

    public void inputBridgeConnected(IBinder token) {
        synchronized (this.mLock) {
            if (DEBUG) {
                Slog.d(TAG, this + ": inputBridgeConnected token: " + token);
            }
            if (this.mConnectionReady) {
                this.mActiveConnection.onInputBridgeConnected(token);
            }
        }
    }
}
