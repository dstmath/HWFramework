package android.os;

import android.os.IBinder;
import android.provider.SettingsStringUtil;
import android.util.ExceptionUtils;
import android.util.Log;
import com.android.internal.os.BinderCallsStats;
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
    private static volatile String sDumpDisabled = null;
    private static volatile boolean sTracingEnabled = false;
    private static volatile TransactionTracker sTransactionTracker = null;
    static volatile boolean sWarnOnBlocking = false;
    private String mDescriptor;
    private final long mObject = getNativeBBinderHolder();
    private IInterface mOwner;

    private static class NoImagePreloadHolder {
        public static final NativeAllocationRegistry sRegistry;

        private NoImagePreloadHolder() {
        }

        static {
            NativeAllocationRegistry nativeAllocationRegistry = new NativeAllocationRegistry(Binder.class.getClassLoader(), Binder.getNativeFinalizer(), 500);
            sRegistry = nativeAllocationRegistry;
        }
    }

    public static final native void blockUntilThreadAvailable();

    public static final native long clearCallingIdentity();

    public static final native void flushPendingCommands();

    public static final native int getCallingPid();

    public static final native int getCallingUid();

    private static native long getFinalizer();

    private static native long getNativeBBinderHolder();

    /* access modifiers changed from: private */
    public static native long getNativeFinalizer();

    public static final native int getThreadStrictModePolicy();

    public static final native void restoreCallingIdentity(long j);

    public static final native void setThreadStrictModePolicy(int i);

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
        } catch (RemoteException ignored) {
            Log.e(TAG, "Unable to allow blocking on interface " + binder, ignored);
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
        } catch (Throwable th) {
            restoreCallingIdentity(callingIdentity);
            if (0 != 0) {
                throw ExceptionUtils.propagate(null);
            }
            throw th;
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
        } catch (Throwable th) {
            restoreCallingIdentity(callingIdentity);
            if (0 != 0) {
                throw ExceptionUtils.propagate(null);
            }
            throw th;
        }
    }

    public static final void joinThreadPool() {
        BinderInternal.joinThreadPool();
    }

    public static final boolean isProxy(IInterface iface) {
        return iface.asBinder() != iface;
    }

    public Binder() {
        NoImagePreloadHolder.sRegistry.registerNativeAllocation(this, this.mObject);
    }

    public void attachInterface(IInterface owner, String descriptor) {
        this.mOwner = owner;
        this.mDescriptor = descriptor;
    }

    public String getInterfaceDescriptor() {
        return this.mDescriptor;
    }

    public boolean pingBinder() {
        return true;
    }

    public boolean isBinderAlive() {
        return true;
    }

    public IInterface queryLocalInterface(String descriptor) {
        if (this.mDescriptor == null || !this.mDescriptor.equals(descriptor)) {
            return null;
        }
        return this.mOwner;
    }

    public static void setDumpDisabled(String msg) {
        sDumpDisabled = msg;
    }

    /* access modifiers changed from: protected */
    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        FileDescriptor fileDescriptor;
        int i = code;
        Parcel parcel = data;
        Parcel parcel2 = reply;
        if (i == 1598968902) {
            parcel2.writeString(getInterfaceDescriptor());
            return true;
        } else if (i == 1598311760) {
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
            if (parcel2 != null) {
                reply.writeNoException();
            } else {
                StrictMode.clearGatheredViolations();
            }
            return true;
        } else {
            if (i != 1598246212) {
                return false;
            }
            ParcelFileDescriptor in = data.readFileDescriptor();
            ParcelFileDescriptor out = data.readFileDescriptor();
            ParcelFileDescriptor err = data.readFileDescriptor();
            String[] args2 = data.readStringArray();
            ShellCallback shellCallback = ShellCallback.CREATOR.createFromParcel(parcel);
            ResultReceiver resultReceiver = ResultReceiver.CREATOR.createFromParcel(parcel);
            if (out != null) {
                if (in != null) {
                    try {
                        fileDescriptor = in.getFileDescriptor();
                    } catch (Throwable th3) {
                        IoUtils.closeQuietly(in);
                        IoUtils.closeQuietly(out);
                        IoUtils.closeQuietly(err);
                        if (parcel2 != null) {
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
            if (parcel2 != null) {
                reply.writeNoException();
            } else {
                StrictMode.clearGatheredViolations();
            }
            return true;
        }
    }

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

    public void dumpAsync(FileDescriptor fd, String[] args) {
        final PrintWriter pw = new FastPrintWriter(new FileOutputStream(fd));
        final FileDescriptor fileDescriptor = fd;
        final String[] strArr = args;
        AnonymousClass1 r1 = new Thread("Binder.dumpAsync") {
            public void run() {
                try {
                    Binder.this.dump(fileDescriptor, pw, strArr);
                } finally {
                    pw.flush();
                }
            }
        };
        r1.start();
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter fout, String[] args) {
    }

    public void shellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) throws RemoteException {
        onShellCommand(in, out, err, args, callback, resultReceiver);
    }

    public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) throws RemoteException {
        PrintWriter pw = new FastPrintWriter(new FileOutputStream(err != null ? err : out));
        pw.println("No shell command implementation.");
        pw.flush();
        resultReceiver.send(0, null);
    }

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

    public void linkToDeath(IBinder.DeathRecipient recipient, int flags) {
    }

    public boolean unlinkToDeath(IBinder.DeathRecipient recipient, int flags) {
        return true;
    }

    static void checkParcel(IBinder obj, int code, Parcel parcel, String msg) {
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0047, code lost:
        android.os.Trace.traceEnd(1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0077, code lost:
        if (r8 == false) goto L_0x007a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x007a, code lost:
        checkParcel(r1, r2, r7, "Unreasonably large binder reply buffer");
        r7.recycle();
        r6.recycle();
        android.os.StrictMode.clearGatheredViolations();
        r4.callEnded(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x008c, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0045, code lost:
        if (r8 != false) goto L_0x0047;
     */
    private boolean execTransact(int code, long dataObj, long replyObj, int flags) {
        boolean res;
        int i = code;
        int i2 = flags;
        BinderCallsStats binderCallsStats = BinderCallsStats.getInstance();
        BinderCallsStats.CallSession callSession = binderCallsStats.callStarted(this, i);
        Parcel data = Parcel.obtain(dataObj);
        Parcel reply = Parcel.obtain(replyObj);
        boolean tracingEnabled = isTracingEnabled();
        if (tracingEnabled) {
            try {
                Trace.traceBegin(1, getClass().getName() + SettingsStringUtil.DELIMITER + i);
            } catch (RemoteException | RuntimeException e) {
                if (LOG_RUNTIME_EXCEPTION) {
                    Log.w(TAG, "Caught a RuntimeException from the binder stub implementation.", e);
                }
                if ((i2 & 1) == 0) {
                    reply.setDataPosition(0);
                    reply.writeException(e);
                } else if (e instanceof RemoteException) {
                    Log.w(TAG, "Binder call failed.", e);
                } else {
                    Log.w(TAG, "Caught a RuntimeException from the binder stub implementation.", e);
                }
                res = true;
            } catch (Throwable th) {
                if (tracingEnabled) {
                    Trace.traceEnd(1);
                }
                throw th;
            }
        }
        res = onTransact(i, data, reply, i2);
    }
}
