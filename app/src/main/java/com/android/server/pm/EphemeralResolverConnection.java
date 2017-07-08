package com.android.server.pm;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.EphemeralResolveInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IRemoteCallback;
import android.os.IRemoteCallback.Stub;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.TimedRemoteCaller;
import com.android.internal.app.IEphemeralResolver;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.TimeoutException;

final class EphemeralResolverConnection {
    private static final long BIND_SERVICE_TIMEOUT_MS = 0;
    private final Context mContext;
    private final GetEphemeralResolveInfoCaller mGetEphemeralResolveInfoCaller;
    private final Intent mIntent;
    private final Object mLock;
    private IEphemeralResolver mRemoteInstance;
    private final ServiceConnection mServiceConnection;

    private static final class GetEphemeralResolveInfoCaller extends TimedRemoteCaller<List<EphemeralResolveInfo>> {
        private final IRemoteCallback mCallback;

        public GetEphemeralResolveInfoCaller() {
            super(5000);
            this.mCallback = new Stub() {
                public void sendResult(Bundle data) throws RemoteException {
                    GetEphemeralResolveInfoCaller.this.onRemoteMethodResult(data.getParcelableArrayList("com.android.internal.app.RESOLVE_INFO"), data.getInt("com.android.internal.app.SEQUENCE", -1));
                }
            };
        }

        public List<EphemeralResolveInfo> getEphemeralResolveInfoList(IEphemeralResolver target, int hashPrefix) throws RemoteException, TimeoutException {
            int sequence = onBeforeRemoteCall();
            target.getEphemeralResolveInfoList(this.mCallback, hashPrefix, sequence);
            return (List) getResultTimed(sequence);
        }
    }

    private final class MyServiceConnection implements ServiceConnection {
        private MyServiceConnection() {
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            synchronized (EphemeralResolverConnection.this.mLock) {
                EphemeralResolverConnection.this.mRemoteInstance = IEphemeralResolver.Stub.asInterface(service);
                EphemeralResolverConnection.this.mLock.notifyAll();
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            synchronized (EphemeralResolverConnection.this.mLock) {
                EphemeralResolverConnection.this.mRemoteInstance = null;
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.pm.EphemeralResolverConnection.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.pm.EphemeralResolverConnection.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.pm.EphemeralResolverConnection.<clinit>():void");
    }

    public EphemeralResolverConnection(Context context, ComponentName componentName) {
        this.mLock = new Object();
        this.mGetEphemeralResolveInfoCaller = new GetEphemeralResolveInfoCaller();
        this.mServiceConnection = new MyServiceConnection();
        this.mContext = context;
        this.mIntent = new Intent().setComponent(componentName);
    }

    public final List<EphemeralResolveInfo> getEphemeralResolveInfoList(int hashPrefix) {
        Object obj;
        throwIfCalledOnMainThread();
        try {
            List<EphemeralResolveInfo> ephemeralResolveInfoList = this.mGetEphemeralResolveInfoCaller.getEphemeralResolveInfoList(getRemoteInstanceLazy(), hashPrefix);
            synchronized (this.mLock) {
                this.mLock.notifyAll();
            }
            return ephemeralResolveInfoList;
        } catch (RemoteException e) {
            obj = this.mLock;
            synchronized (obj) {
            }
            this.mLock.notifyAll();
            return null;
        } catch (TimeoutException e2) {
            obj = this.mLock;
            synchronized (obj) {
            }
            this.mLock.notifyAll();
            return null;
        } catch (Throwable th) {
            synchronized (this.mLock) {
            }
            this.mLock.notifyAll();
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String prefix) {
        synchronized (this.mLock) {
            pw.append(prefix).append("bound=").append(this.mRemoteInstance != null ? "true" : "false").println();
            pw.flush();
            try {
                getRemoteInstanceLazy().asBinder().dump(fd, new String[]{prefix});
            } catch (TimeoutException e) {
            } catch (RemoteException e2) {
            }
        }
    }

    private IEphemeralResolver getRemoteInstanceLazy() throws TimeoutException {
        synchronized (this.mLock) {
            if (this.mRemoteInstance != null) {
                IEphemeralResolver iEphemeralResolver = this.mRemoteInstance;
                return iEphemeralResolver;
            }
            bindLocked();
            iEphemeralResolver = this.mRemoteInstance;
            return iEphemeralResolver;
        }
    }

    private void bindLocked() throws TimeoutException {
        if (this.mRemoteInstance == null) {
            this.mContext.bindServiceAsUser(this.mIntent, this.mServiceConnection, 67108865, UserHandle.SYSTEM);
            long startMillis = SystemClock.uptimeMillis();
            while (this.mRemoteInstance == null) {
                long remainingMillis = BIND_SERVICE_TIMEOUT_MS - (SystemClock.uptimeMillis() - startMillis);
                if (remainingMillis <= 0) {
                    throw new TimeoutException("Didn't bind to resolver in time.");
                }
                try {
                    this.mLock.wait(remainingMillis);
                } catch (InterruptedException e) {
                }
            }
            this.mLock.notifyAll();
        }
    }

    private void throwIfCalledOnMainThread() {
        if (Thread.currentThread() == this.mContext.getMainLooper().getThread()) {
            throw new RuntimeException("Cannot invoke on the main thread");
        }
    }
}
