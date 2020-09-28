package android.os;

import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.os.IBinder;
import android.provider.SettingsStringUtil;
import android.util.ExceptionUtils;
import android.util.Log;
import com.android.internal.os.BinderInternal;
import com.android.internal.util.FastPrintWriter;
import com.android.internal.util.FunctionalUtils;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import libcore.io.IoUtils;
import libcore.util.NativeAllocationRegistry;

public class Binder implements IBinder {
    private static final boolean BINDER_DISPLAY_DETAILS = SystemProperties.getBoolean("persist.binder.display_details", false);
    public static final boolean CHECK_PARCEL_SIZE = false;
    private static final boolean FIND_POTENTIAL_LEAKS = false;
    public static boolean LOG_RUNTIME_EXCEPTION = false;
    private static final int NATIVE_ALLOCATION_SIZE = 500;
    static final String TAG = "Binder";
    public static final int UNSET_WORKSOURCE = -1;
    private static volatile String sDumpDisabled = null;
    private static BinderInternal.Observer sObserver = null;
    private static volatile boolean sTracingEnabled = false;
    private static volatile TransactionTracker sTransactionTracker = null;
    static volatile boolean sWarnOnBlocking = false;
    private static volatile BinderInternal.WorkSourceProvider sWorkSourceProvider = $$Lambda$Binder$IYUHVkWouPK_9CG2s8VwyWBt5_I.INSTANCE;
    private String mDescriptor;
    @UnsupportedAppUsage
    private final long mObject;
    private IInterface mOwner;

    @SystemApi
    public interface ProxyTransactListener {
        void onTransactEnded(Object obj);

        Object onTransactStarted(IBinder iBinder, int i);
    }

    public static final native void blockUntilThreadAvailable();

    public static final native long clearCallingIdentity();

    public static final native long clearCallingWorkSource();

    public static final native void flushPendingCommands();

    public static final native int getCallingPid();

    public static final native int getCallingUid();

    public static final native int getCallingWorkSourceUid();

    private static native long getFinalizer();

    private static native long getNativeBBinderHolder();

    /* access modifiers changed from: private */
    public static native long getNativeFinalizer();

    public static final native int getThreadStrictModePolicy();

    public static final native boolean isHandlingTransaction();

    public static final native void restoreCallingIdentity(long j);

    public static final native void restoreCallingWorkSource(long j);

    public static final native long setCallingWorkSourceUid(int i);

    public static final native void setThreadStrictModePolicy(int i);

    private static class NoImagePreloadHolder {
        public static final NativeAllocationRegistry sRegistry = new NativeAllocationRegistry(Binder.class.getClassLoader(), Binder.getNativeFinalizer(), 500);

        private NoImagePreloadHolder() {
        }
    }

    public static void enableTracing() {
        sTracingEnabled = true;
    }

    public static void disableTracing() {
        sTracingEnabled = false;
    }

    public static boolean isTracingEnabled() {
        return sTracingEnabled || BINDER_DISPLAY_DETAILS;
    }

    public static synchronized TransactionTracker getTransactionTracker() {
        TransactionTracker transactionTracker;
        synchronized (Binder.class) {
            if (sTransactionTracker == null) {
                sTransactionTracker = new TransactionTracker();
            }
            transactionTracker = sTransactionTracker;
        }
        return transactionTracker;
    }

    public static void setObserver(BinderInternal.Observer observer) {
        sObserver = observer;
    }

    public static void setWarnOnBlocking(boolean warnOnBlocking) {
        sWarnOnBlocking = warnOnBlocking;
    }

    public static IBinder allowBlocking(IBinder binder) {
        try {
            if (binder instanceof BinderProxy) {
                ((BinderProxy) binder).mWarnOnBlocking = false;
            } else if (!(binder == null || binder.getInterfaceDescriptor() == null || binder.queryLocalInterface(binder.getInterfaceDescriptor()) != null)) {
                Log.w(TAG, "Unable to allow blocking on interface " + binder);
            }
        } catch (RemoteException e) {
        }
        return binder;
    }

    public static IBinder defaultBlocking(IBinder binder) {
        if (binder instanceof BinderProxy) {
            ((BinderProxy) binder).mWarnOnBlocking = sWarnOnBlocking;
        }
        return binder;
    }

    public static void copyAllowBlocking(IBinder fromBinder, IBinder toBinder) {
        if ((fromBinder instanceof BinderProxy) && (toBinder instanceof BinderProxy)) {
            ((BinderProxy) toBinder).mWarnOnBlocking = ((BinderProxy) fromBinder).mWarnOnBlocking;
        }
    }

    public static final int getCallingUidOrThrow() {
        if (isHandlingTransaction()) {
            return getCallingUid();
        }
        throw new IllegalStateException("Thread is not in a binder transcation");
    }

    public static final UserHandle getCallingUserHandle() {
        return UserHandle.of(UserHandle.getUserId(getCallingUid()));
    }

    public static final void withCleanCallingIdentity(FunctionalUtils.ThrowingRunnable action) {
        long callingIdentity = clearCallingIdentity();
        try {
            action.runOrThrow();
            restoreCallingIdentity(callingIdentity);
            if (0 != 0) {
                throw ExceptionUtils.propagate(null);
            }
        } catch (Throwable throwable) {
            restoreCallingIdentity(callingIdentity);
            throw ExceptionUtils.propagate(throwable);
        }
    }

    public static final <T> T withCleanCallingIdentity(FunctionalUtils.ThrowingSupplier<T> action) {
        long callingIdentity = clearCallingIdentity();
        try {
            T orThrow = action.getOrThrow();
            restoreCallingIdentity(callingIdentity);
            if (0 == 0) {
                return orThrow;
            }
            throw ExceptionUtils.propagate(null);
        } catch (Throwable throwable) {
            restoreCallingIdentity(callingIdentity);
            throw ExceptionUtils.propagate(throwable);
        }
    }

    public static final void joinThreadPool() {
        BinderInternal.joinThreadPool();
    }

    public static final boolean isProxy(IInterface iface) {
        return iface.asBinder() != iface;
    }

    public Binder() {
        this(null);
    }

    public Binder(String descriptor) {
        this.mObject = getNativeBBinderHolder();
        NoImagePreloadHolder.sRegistry.registerNativeAllocation(this, this.mObject);
        this.mDescriptor = descriptor;
    }

    public void attachInterface(IInterface owner, String descriptor) {
        this.mOwner = owner;
        this.mDescriptor = descriptor;
    }

    @Override // android.os.IBinder
    public String getInterfaceDescriptor() {
        return this.mDescriptor;
    }

    @Override // android.os.IBinder
    public boolean pingBinder() {
        return true;
    }

    @Override // android.os.IBinder
    public boolean isBinderAlive() {
        return true;
    }

    @Override // android.os.IBinder
    public IInterface queryLocalInterface(String descriptor) {
        String str = this.mDescriptor;
        if (str == null || !str.equals(descriptor)) {
            return null;
        }
        return this.mOwner;
    }

    public static void setDumpDisabled(String msg) {
        sDumpDisabled = msg;
    }

    public static class PropagateWorkSourceTransactListener implements ProxyTransactListener {
        @Override // android.os.Binder.ProxyTransactListener
        public Object onTransactStarted(IBinder binder, int transactionCode) {
            int uid = ThreadLocalWorkSource.getUid();
            if (uid != -1) {
                return Long.valueOf(Binder.setCallingWorkSourceUid(uid));
            }
            return null;
        }

        @Override // android.os.Binder.ProxyTransactListener
        public void onTransactEnded(Object session) {
            if (session != null) {
                Binder.restoreCallingWorkSource(((Long) session).longValue());
            }
        }
    }

    @SystemApi
    public static void setProxyTransactListener(ProxyTransactListener listener) {
        BinderProxy.setTransactListener(listener);
    }

    /* access modifiers changed from: protected */
    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        FileDescriptor fileDescriptor;
        if (code == 1598968902) {
            reply.writeString(getInterfaceDescriptor());
            return true;
        } else if (code == 1598311760) {
            ParcelFileDescriptor fd = data.readFileDescriptor();
            String[] args = data.readStringArray();
            if (fd != null) {
                try {
                    try {
                        dump(fd.getFileDescriptor(), args);
                        IoUtils.closeQuietly(fd);
                    } catch (Throwable th) {
                        th = th;
                        IoUtils.closeQuietly(fd);
                        throw th;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    IoUtils.closeQuietly(fd);
                    throw th;
                }
            }
            if (reply != null) {
                reply.writeNoException();
            } else {
                StrictMode.clearGatheredViolations();
            }
            return true;
        } else if (code != 1598246212) {
            return false;
        } else {
            ParcelFileDescriptor in = data.readFileDescriptor();
            ParcelFileDescriptor out = data.readFileDescriptor();
            ParcelFileDescriptor err = data.readFileDescriptor();
            String[] args2 = data.readStringArray();
            ShellCallback shellCallback = ShellCallback.CREATOR.createFromParcel(data);
            ResultReceiver resultReceiver = ResultReceiver.CREATOR.createFromParcel(data);
            if (out != null) {
                if (in != null) {
                    try {
                        fileDescriptor = in.getFileDescriptor();
                    } catch (Throwable th3) {
                        IoUtils.closeQuietly(in);
                        IoUtils.closeQuietly(out);
                        IoUtils.closeQuietly(err);
                        if (reply != null) {
                            reply.writeNoException();
                        } else {
                            StrictMode.clearGatheredViolations();
                        }
                        throw th3;
                    }
                } else {
                    fileDescriptor = null;
                }
                shellCommand(fileDescriptor, out.getFileDescriptor(), err != null ? err.getFileDescriptor() : out.getFileDescriptor(), args2, shellCallback, resultReceiver);
            }
            IoUtils.closeQuietly(in);
            IoUtils.closeQuietly(out);
            IoUtils.closeQuietly(err);
            if (reply != null) {
                reply.writeNoException();
            } else {
                StrictMode.clearGatheredViolations();
            }
            return true;
        }
    }

    public String getTransactionName(int transactionCode) {
        return null;
    }

    @Override // android.os.IBinder
    public void dump(FileDescriptor fd, String[] args) {
        PrintWriter pw = new FastPrintWriter(new FileOutputStream(fd));
        try {
            doDump(fd, pw, args);
        } finally {
            pw.flush();
        }
    }

    /* access modifiers changed from: package-private */
    public void doDump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (sDumpDisabled == null) {
            try {
                dump(fd, pw, args);
            } catch (SecurityException e) {
                pw.println("Security exception: " + e.getMessage());
                throw e;
            } catch (Throwable e2) {
                pw.println();
                pw.println("Exception occurred while dumping:");
                e2.printStackTrace(pw);
            }
        } else {
            pw.println(sDumpDisabled);
        }
    }

    @Override // android.os.IBinder
    public void dumpAsync(final FileDescriptor fd, final String[] args) {
        final PrintWriter pw = new FastPrintWriter(new FileOutputStream(fd));
        new Thread("Binder.dumpAsync") {
            /* class android.os.Binder.AnonymousClass1 */

            public void run() {
                try {
                    Binder.this.dump(fd, pw, args);
                } finally {
                    pw.flush();
                }
            }
        }.start();
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter fout, String[] args) {
    }

    @Override // android.os.IBinder
    public void shellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) throws RemoteException {
        onShellCommand(in, out, err, args, callback, resultReceiver);
    }

    public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) throws RemoteException {
        PrintWriter pw = new FastPrintWriter(new FileOutputStream(err != null ? err : out));
        pw.println("No shell command implementation.");
        pw.flush();
        resultReceiver.send(0, null);
    }

    @Override // android.os.IBinder
    public final boolean transact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (data != null) {
            data.setDataPosition(0);
        }
        boolean r = onTransact(code, data, reply, flags);
        if (reply != null) {
            reply.setDataPosition(0);
        }
        return r;
    }

    @Override // android.os.IBinder
    public void linkToDeath(IBinder.DeathRecipient recipient, int flags) {
    }

    @Override // android.os.IBinder
    public boolean unlinkToDeath(IBinder.DeathRecipient recipient, int flags) {
        return true;
    }

    static void checkParcel(IBinder obj, int code, Parcel parcel, String msg) {
    }

    public static void setWorkSourceProvider(BinderInternal.WorkSourceProvider workSourceProvider) {
        if (workSourceProvider != null) {
            sWorkSourceProvider = workSourceProvider;
            return;
        }
        throw new IllegalArgumentException("workSourceProvider cannot be null");
    }

    @UnsupportedAppUsage
    private boolean execTransact(int code, long dataObj, long replyObj, int flags) {
        int callingUid = getCallingUid();
        long origWorkSource = ThreadLocalWorkSource.setUid(callingUid);
        try {
            return execTransactInternal(code, dataObj, replyObj, flags, callingUid);
        } finally {
            ThreadLocalWorkSource.restore(origWorkSource);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0054, code lost:
        if (r4 != null) goto L_0x0056;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0056, code lost:
        r4.callEnded(r0, r6.dataSize(), r7.dataSize(), android.os.Binder.sWorkSourceProvider.resolveWorkSourceUid(r6.readCallingWorkSourceUid()));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00a1, code lost:
        if (r4 != null) goto L_0x0056;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00a4, code lost:
        checkParcel(r14, r15, r7, "Unreasonably large binder reply buffer");
        r7.recycle();
        r6.recycle();
        android.os.StrictMode.clearGatheredViolations();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00b2, code lost:
        return r0;
     */
    private boolean execTransactInternal(int code, long dataObj, long replyObj, int flags, int callingUid) {
        boolean res;
        BinderInternal.Observer observer = sObserver;
        BinderInternal.CallSession callSession = observer != null ? observer.callStarted(this, code, -1) : null;
        Parcel data = Parcel.obtain(dataObj);
        Parcel reply = Parcel.obtain(replyObj);
        boolean tracingEnabled = isTracingEnabled();
        if (tracingEnabled) {
            try {
                String transactionName = getTransactionName(code);
                StringBuilder sb = new StringBuilder();
                sb.append(getClass().getName());
                sb.append(SettingsStringUtil.DELIMITER);
                sb.append(transactionName != null ? transactionName : Integer.valueOf(code));
                Trace.traceBegin(1, sb.toString());
            } catch (RemoteException | RuntimeException e) {
                if (observer != null) {
                    observer.callThrewException(callSession, e);
                }
                if (LOG_RUNTIME_EXCEPTION) {
                    Log.w(TAG, "Caught a RuntimeException from the binder stub implementation.", e);
                }
                if ((flags & 1) == 0) {
                    reply.setDataSize(0);
                    reply.setDataPosition(0);
                    reply.writeException(e);
                } else if (e instanceof RemoteException) {
                    Log.w(TAG, "Binder call failed.", e);
                } else {
                    Log.w(TAG, "Caught a RuntimeException from the binder stub implementation.", e);
                }
                res = true;
                if (tracingEnabled) {
                    Trace.traceEnd(1);
                }
            } catch (Throwable th) {
                if (tracingEnabled) {
                    Trace.traceEnd(1);
                }
                if (observer != null) {
                    observer.callEnded(callSession, data.dataSize(), reply.dataSize(), sWorkSourceProvider.resolveWorkSourceUid(data.readCallingWorkSourceUid()));
                }
                throw th;
            }
        }
        res = onTransact(code, data, reply, flags);
        if (tracingEnabled) {
            Trace.traceEnd(1);
        }
    }
}
