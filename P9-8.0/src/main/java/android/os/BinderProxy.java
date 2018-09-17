package android.os;

import android.os.IBinder.DeathRecipient;
import android.util.Log;
import java.io.FileDescriptor;
import java.lang.ref.WeakReference;

/* compiled from: Binder */
final class BinderProxy implements IBinder {
    private long mObject;
    private long mOrgue;
    private final WeakReference mSelf = new WeakReference(this);
    volatile boolean mWarnOnBlocking = Binder.sWarnOnBlocking;

    private final native void destroy();

    public native String getInterfaceDescriptor() throws RemoteException;

    public native boolean isBinderAlive();

    public native void linkToDeath(DeathRecipient deathRecipient, int i) throws RemoteException;

    public native boolean pingBinder();

    public native boolean transactNative(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException;

    public native boolean unlinkToDeath(DeathRecipient deathRecipient, int i);

    public IInterface queryLocalInterface(String descriptor) {
        return null;
    }

    public boolean transact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        Binder.checkParcel(this, code, data, "Unreasonably large binder buffer");
        if (this.mWarnOnBlocking && (flags & 1) == 0) {
            this.mWarnOnBlocking = false;
            Log.w("Binder", "Outgoing transactions from this process must be FLAG_ONEWAY", new Throwable());
        }
        boolean tracingEnabled = Binder.isTracingEnabled();
        if (tracingEnabled) {
            Throwable tr = new Throwable();
            Binder.getTransactionTracker().addTrace(tr);
            StackTraceElement stackTraceElement = tr.getStackTrace()[1];
            Trace.traceBegin(1, stackTraceElement.getClassName() + "." + stackTraceElement.getMethodName());
        }
        try {
            if (BlockMonitor.isNeedMonitor()) {
                long startTime = SystemClock.uptimeMillis();
                boolean result = transactNative(code, data, reply, flags);
                BlockMonitor.checkBinderTime(startTime);
                if (tracingEnabled) {
                    Trace.traceEnd(1);
                }
                return result;
            }
            boolean transactNative = transactNative(code, data, reply, flags);
            return transactNative;
        } finally {
            if (tracingEnabled) {
                Trace.traceEnd(1);
            }
        }
    }

    public void dump(FileDescriptor fd, String[] args) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeFileDescriptor(fd);
        data.writeStringArray(args);
        try {
            transact(IBinder.DUMP_TRANSACTION, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public void dumpAsync(FileDescriptor fd, String[] args) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeFileDescriptor(fd);
        data.writeStringArray(args);
        try {
            transact(IBinder.DUMP_TRANSACTION, data, reply, 1);
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public void shellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeFileDescriptor(in);
        data.writeFileDescriptor(out);
        data.writeFileDescriptor(err);
        data.writeStringArray(args);
        ShellCallback.writeToParcel(callback, data);
        resultReceiver.writeToParcel(data, 0);
        try {
            transact(IBinder.SHELL_COMMAND_TRANSACTION, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    BinderProxy() {
    }

    protected void finalize() throws Throwable {
        try {
            destroy();
        } finally {
            super.finalize();
        }
    }

    private static final void sendDeathNotice(DeathRecipient recipient) {
        try {
            recipient.binderDied();
        } catch (RuntimeException exc) {
            Log.w("BinderNative", "Uncaught exception from death notification", exc);
        }
    }
}
