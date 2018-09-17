package com.android.internal.os;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Slog;
import java.io.Closeable;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public final class TransferPipe implements Runnable, Closeable {
    static final boolean DEBUG = false;
    static final long DEFAULT_TIMEOUT = 5000;
    static final String TAG = "TransferPipe";
    String mBufferPrefix;
    boolean mComplete;
    long mEndTime;
    String mFailure;
    final ParcelFileDescriptor[] mFds;
    FileDescriptor mOutFd;
    final Thread mThread;

    interface Caller {
        void go(IInterface iInterface, FileDescriptor fileDescriptor, String str, String[] strArr) throws RemoteException;
    }

    public TransferPipe() throws IOException {
        this(null);
    }

    public TransferPipe(String bufferPrefix) throws IOException {
        this.mThread = new Thread(this, TAG);
        this.mFds = ParcelFileDescriptor.createPipe();
        this.mBufferPrefix = bufferPrefix;
    }

    ParcelFileDescriptor getReadFd() {
        return this.mFds[0];
    }

    public ParcelFileDescriptor getWriteFd() {
        return this.mFds[1];
    }

    public void setBufferPrefix(String prefix) {
        this.mBufferPrefix = prefix;
    }

    public static void dumpAsync(IBinder binder, FileDescriptor out, String[] args) throws IOException, RemoteException {
        goDump(binder, out, args);
    }

    static void go(Caller caller, IInterface iface, FileDescriptor out, String prefix, String[] args) throws IOException, RemoteException {
        go(caller, iface, out, prefix, args, 5000);
    }

    /* JADX WARNING: Removed duplicated region for block: B:23:0x0035 A:{SYNTHETIC, Splitter: B:23:0x0035} */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x0046  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x003a  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static void go(Caller caller, IInterface iface, FileDescriptor out, String prefix, String[] args, long timeout) throws IOException, RemoteException {
        Throwable th;
        Throwable th2 = null;
        if (iface.asBinder() instanceof Binder) {
            try {
                caller.go(iface, out, prefix, args);
            } catch (RemoteException e) {
            }
            return;
        }
        TransferPipe tp = null;
        try {
            TransferPipe tp2 = new TransferPipe();
            try {
                caller.go(iface, tp2.getWriteFd().getFileDescriptor(), prefix, args);
                tp2.go(out, timeout);
                if (tp2 != null) {
                    try {
                        tp2.close();
                    } catch (Throwable th3) {
                        th2 = th3;
                    }
                }
                if (th2 != null) {
                    throw th2;
                }
            } catch (Throwable th4) {
                th = th4;
                tp = tp2;
                if (tp != null) {
                    try {
                        tp.close();
                    } catch (Throwable th5) {
                        if (th2 == null) {
                            th2 = th5;
                        } else if (th2 != th5) {
                            th2.addSuppressed(th5);
                        }
                    }
                }
                if (th2 == null) {
                    throw th2;
                }
                throw th;
            }
        } catch (Throwable th6) {
            th = th6;
            if (tp != null) {
            }
            if (th2 == null) {
            }
        }
    }

    static void goDump(IBinder binder, FileDescriptor out, String[] args) throws IOException, RemoteException {
        goDump(binder, out, args, 5000);
    }

    /* JADX WARNING: Removed duplicated region for block: B:23:0x0031 A:{SYNTHETIC, Splitter: B:23:0x0031} */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x0042  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0036  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static void goDump(IBinder binder, FileDescriptor out, String[] args, long timeout) throws IOException, RemoteException {
        Throwable th;
        Throwable th2 = null;
        if (binder instanceof Binder) {
            try {
                binder.dump(out, args);
            } catch (RemoteException e) {
            }
            return;
        }
        TransferPipe tp = null;
        try {
            TransferPipe tp2 = new TransferPipe();
            try {
                binder.dumpAsync(tp2.getWriteFd().getFileDescriptor(), args);
                tp2.go(out, timeout);
                if (tp2 != null) {
                    try {
                        tp2.close();
                    } catch (Throwable th3) {
                        th2 = th3;
                    }
                }
                if (th2 != null) {
                    throw th2;
                }
            } catch (Throwable th4) {
                th = th4;
                tp = tp2;
                if (tp != null) {
                    try {
                        tp.close();
                    } catch (Throwable th5) {
                        if (th2 == null) {
                            th2 = th5;
                        } else if (th2 != th5) {
                            th2.addSuppressed(th5);
                        }
                    }
                }
                if (th2 == null) {
                    throw th2;
                }
                throw th;
            }
        } catch (Throwable th6) {
            th = th6;
            if (tp != null) {
            }
            if (th2 == null) {
            }
        }
    }

    public void go(FileDescriptor out) throws IOException {
        go(out, 5000);
    }

    public void go(FileDescriptor out, long timeout) throws IOException {
        try {
            synchronized (this) {
                this.mOutFd = out;
                this.mEndTime = SystemClock.uptimeMillis() + timeout;
                closeFd(1);
                this.mThread.start();
                while (this.mFailure == null && (this.mComplete ^ 1) != 0) {
                    long waitTime = this.mEndTime - SystemClock.uptimeMillis();
                    if (waitTime <= 0) {
                        this.mThread.interrupt();
                        throw new IOException("Timeout");
                    }
                    try {
                        wait(waitTime);
                    } catch (InterruptedException e) {
                    }
                }
                if (this.mFailure != null) {
                    throw new IOException(this.mFailure);
                }
            }
        } finally {
            kill();
        }
    }

    void closeFd(int num) {
        if (this.mFds[num] != null) {
            try {
                this.mFds[num].close();
            } catch (IOException e) {
            }
            this.mFds[num] = null;
        }
    }

    public void close() {
        kill();
    }

    public void kill() {
        synchronized (this) {
            closeFd(0);
            closeFd(1);
        }
    }

    /* JADX WARNING: Missing block: B:11:0x0029, code:
            r1 = null;
            r6 = true;
     */
    /* JADX WARNING: Missing block: B:12:0x002d, code:
            if (r12.mBufferPrefix == null) goto L_0x0035;
     */
    /* JADX WARNING: Missing block: B:13:0x002f, code:
            r1 = r12.mBufferPrefix.getBytes();
     */
    /* JADX WARNING: Missing block: B:15:?, code:
            r8 = r3.read(r0);
     */
    /* JADX WARNING: Missing block: B:16:0x0039, code:
            if (r8 <= 0) goto L_0x007e;
     */
    /* JADX WARNING: Missing block: B:17:0x003b, code:
            if (r1 != null) goto L_0x0052;
     */
    /* JADX WARNING: Missing block: B:18:0x003d, code:
            r4.write(r0, 0, r8);
     */
    /* JADX WARNING: Missing block: B:20:0x0042, code:
            r2 = move-exception;
     */
    /* JADX WARNING: Missing block: B:21:0x0043, code:
            monitor-enter(r12);
     */
    /* JADX WARNING: Missing block: B:23:?, code:
            r12.mFailure = r2.toString();
            notifyAll();
     */
    /* JADX WARNING: Missing block: B:25:0x004e, code:
            return;
     */
    /* JADX WARNING: Missing block: B:29:0x0052, code:
            r9 = 0;
            r5 = 0;
     */
    /* JADX WARNING: Missing block: B:30:0x0054, code:
            if (r5 >= r8) goto L_0x0076;
     */
    /* JADX WARNING: Missing block: B:33:0x0058, code:
            if (r0[r5] == (byte) 10) goto L_0x0073;
     */
    /* JADX WARNING: Missing block: B:34:0x005a, code:
            if (r5 <= r9) goto L_0x0061;
     */
    /* JADX WARNING: Missing block: B:35:0x005c, code:
            r4.write(r0, r9, r5 - r9);
     */
    /* JADX WARNING: Missing block: B:36:0x0061, code:
            r9 = r5;
     */
    /* JADX WARNING: Missing block: B:37:0x0062, code:
            if (r6 == false) goto L_0x0068;
     */
    /* JADX WARNING: Missing block: B:38:0x0064, code:
            r4.write(r1);
            r6 = false;
     */
    /* JADX WARNING: Missing block: B:39:0x0068, code:
            r5 = r5 + 1;
     */
    /* JADX WARNING: Missing block: B:40:0x006a, code:
            if (r5 >= r8) goto L_0x0070;
     */
    /* JADX WARNING: Missing block: B:42:0x006e, code:
            if (r0[r5] != (byte) 10) goto L_0x0068;
     */
    /* JADX WARNING: Missing block: B:43:0x0070, code:
            if (r5 >= r8) goto L_0x0073;
     */
    /* JADX WARNING: Missing block: B:44:0x0072, code:
            r6 = true;
     */
    /* JADX WARNING: Missing block: B:45:0x0073, code:
            r5 = r5 + 1;
     */
    /* JADX WARNING: Missing block: B:46:0x0076, code:
            if (r8 <= r9) goto L_0x0035;
     */
    /* JADX WARNING: Missing block: B:47:0x0078, code:
            r4.write(r0, r9, r8 - r9);
     */
    /* JADX WARNING: Missing block: B:48:0x007e, code:
            r10 = r12.mThread.isInterrupted();
     */
    /* JADX WARNING: Missing block: B:49:0x0084, code:
            monitor-enter(r12);
     */
    /* JADX WARNING: Missing block: B:52:?, code:
            r12.mComplete = true;
            notifyAll();
     */
    /* JADX WARNING: Missing block: B:53:0x008b, code:
            monitor-exit(r12);
     */
    /* JADX WARNING: Missing block: B:54:0x008c, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void run() {
        byte[] buffer = new byte[1024];
        synchronized (this) {
            ParcelFileDescriptor readFd = getReadFd();
            if (readFd == null) {
                Slog.w(TAG, "Pipe has been closed...");
                return;
            }
            FileInputStream fis = new FileInputStream(readFd.getFileDescriptor());
            FileOutputStream fos = new FileOutputStream(this.mOutFd);
        }
    }
}
