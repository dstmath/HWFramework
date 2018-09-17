package android.os;

import android.os.IBinder.DeathRecipient;
import android.util.Log;
import com.android.internal.util.FastPrintWriter;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import libcore.io.IoUtils;

public class Binder implements IBinder {
    private static final boolean CHECK_PARCEL_SIZE = false;
    private static final boolean FIND_POTENTIAL_LEAKS = false;
    public static boolean LOG_RUNTIME_EXCEPTION = false;
    static final String TAG = "Binder";
    private static String sDumpDisabled;
    private static boolean sTracingEnabled;
    private static TransactionTracker sTransactionTracker;
    private String mDescriptor;
    private long mObject;
    private IInterface mOwner;

    /* renamed from: android.os.Binder.1 */
    class AnonymousClass1 extends Thread {
        final /* synthetic */ String[] val$args;
        final /* synthetic */ FileDescriptor val$fd;
        final /* synthetic */ PrintWriter val$pw;

        AnonymousClass1(String $anonymous0, PrintWriter val$pw, FileDescriptor val$fd, String[] val$args) {
            this.val$pw = val$pw;
            this.val$fd = val$fd;
            this.val$args = val$args;
            super($anonymous0);
        }

        public void run() {
            try {
                Binder.this.dump(this.val$fd, this.val$pw, this.val$args);
            } finally {
                this.val$pw.flush();
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.os.Binder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.os.Binder.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.os.Binder.<clinit>():void");
    }

    public static final native void blockUntilThreadAvailable();

    public static final native long clearCallingIdentity();

    private final native void destroy();

    public static final native void flushPendingCommands();

    public static final native int getCallingPid();

    public static final native int getCallingUid();

    public static final native int getThreadStrictModePolicy();

    private final native void init();

    public static final native void joinThreadPool();

    public static final native void restoreCallingIdentity(long j);

    public static final native void setThreadStrictModePolicy(int i);

    public static void enableTracing() {
        sTracingEnabled = true;
    }

    public static void disableTracing() {
        sTracingEnabled = FIND_POTENTIAL_LEAKS;
    }

    public static boolean isTracingEnabled() {
        return sTracingEnabled;
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

    public static final UserHandle getCallingUserHandle() {
        return UserHandle.of(UserHandle.getUserId(getCallingUid()));
    }

    public static final boolean isProxy(IInterface iface) {
        return iface.asBinder() != iface ? true : FIND_POTENTIAL_LEAKS;
    }

    public Binder() {
        init();
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
        if (this.mDescriptor.equals(descriptor)) {
            return this.mOwner;
        }
        return null;
    }

    public static void setDumpDisabled(String msg) {
        synchronized (Binder.class) {
            sDumpDisabled = msg;
        }
    }

    protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        FileDescriptor fileDescriptor = null;
        if (code == IBinder.INTERFACE_TRANSACTION) {
            reply.writeString(getInterfaceDescriptor());
            return true;
        } else if (code == IBinder.DUMP_TRANSACTION) {
            ParcelFileDescriptor fd = data.readFileDescriptor();
            args = data.readStringArray();
            if (fd != null) {
                try {
                    dump(fd.getFileDescriptor(), args);
                } finally {
                    IoUtils.closeQuietly(fd);
                }
            }
            if (reply != null) {
                reply.writeNoException();
            } else {
                StrictMode.clearGatheredViolations();
            }
            return true;
        } else if (code != IBinder.SHELL_COMMAND_TRANSACTION) {
            return FIND_POTENTIAL_LEAKS;
        } else {
            ParcelFileDescriptor in = data.readFileDescriptor();
            ParcelFileDescriptor out = data.readFileDescriptor();
            ParcelFileDescriptor err = data.readFileDescriptor();
            args = data.readStringArray();
            ResultReceiver resultReceiver = (ResultReceiver) ResultReceiver.CREATOR.createFromParcel(data);
            if (out != null) {
                if (in != null) {
                    try {
                        fileDescriptor = in.getFileDescriptor();
                    } catch (Throwable th) {
                        IoUtils.closeQuietly(in);
                        IoUtils.closeQuietly(out);
                        IoUtils.closeQuietly(err);
                        if (reply != null) {
                            reply.writeNoException();
                        } else {
                            StrictMode.clearGatheredViolations();
                        }
                    }
                }
                shellCommand(fileDescriptor, out.getFileDescriptor(), err != null ? err.getFileDescriptor() : out.getFileDescriptor(), args, resultReceiver);
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

    public void dump(FileDescriptor fd, String[] args) {
        PrintWriter pw = new FastPrintWriter(new FileOutputStream(fd));
        try {
            doDump(fd, pw, args);
        } finally {
            pw.flush();
        }
    }

    void doDump(FileDescriptor fd, PrintWriter pw, String[] args) {
        synchronized (Binder.class) {
            String disabled = sDumpDisabled;
        }
        if (disabled == null) {
            try {
                dump(fd, pw, args);
                return;
            } catch (SecurityException e) {
                pw.println("Security exception: " + e.getMessage());
                throw e;
            } catch (Throwable e2) {
                pw.println();
                pw.println("Exception occurred while dumping:");
                e2.printStackTrace(pw);
                return;
            }
        }
        pw.println(sDumpDisabled);
    }

    public void dumpAsync(FileDescriptor fd, String[] args) {
        new AnonymousClass1("Binder.dumpAsync", new FastPrintWriter(new FileOutputStream(fd)), fd, args).start();
    }

    protected void dump(FileDescriptor fd, PrintWriter fout, String[] args) {
    }

    public void shellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ResultReceiver resultReceiver) throws RemoteException {
        onShellCommand(in, out, err, args, resultReceiver);
    }

    public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ResultReceiver resultReceiver) throws RemoteException {
        if (err == null) {
            err = out;
        }
        PrintWriter pw = new FastPrintWriter(new FileOutputStream(err));
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

    public void linkToDeath(DeathRecipient recipient, int flags) {
    }

    public boolean unlinkToDeath(DeathRecipient recipient, int flags) {
        return true;
    }

    protected void finalize() throws Throwable {
        try {
            destroy();
        } finally {
            super.finalize();
        }
    }

    static void checkParcel(IBinder obj, int code, Parcel parcel, String msg) {
    }

    private boolean execTransact(int code, long dataObj, long replyObj, int flags) {
        boolean onTransact;
        Parcel data = Parcel.obtain(dataObj);
        Parcel reply = Parcel.obtain(replyObj);
        try {
            onTransact = onTransact(code, data, reply, flags);
        } catch (Exception e) {
            if (LOG_RUNTIME_EXCEPTION) {
                Log.w(TAG, "Caught a RuntimeException from the binder stub implementation.", e);
            }
            if ((flags & 1) == 0) {
                reply.setDataPosition(0);
                reply.writeException(e);
            } else if (e instanceof RemoteException) {
                Log.w(TAG, "Binder call failed.", e);
            } else {
                Log.w(TAG, "Caught a RuntimeException from the binder stub implementation.", e);
            }
            onTransact = true;
        } catch (OutOfMemoryError e2) {
            Log.e(TAG, "Caught an OutOfMemoryError from the binder stub implementation.", e2);
            RuntimeException re = new RuntimeException("Out of memory", e2);
            reply.setDataPosition(0);
            reply.writeException(re);
            onTransact = true;
        }
        checkParcel(this, code, reply, "Unreasonably large binder reply buffer");
        reply.recycle();
        data.recycle();
        StrictMode.clearGatheredViolations();
        return onTransact;
    }
}
