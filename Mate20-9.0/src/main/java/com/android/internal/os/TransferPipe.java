package com.android.internal.os;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Slog;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import libcore.io.IoUtils;

public class TransferPipe implements Runnable, Closeable {
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
        this(bufferPrefix, TAG);
    }

    protected TransferPipe(String bufferPrefix, String threadName) throws IOException {
        this.mThread = new Thread(this, threadName);
        this.mFds = ParcelFileDescriptor.createPipe();
        this.mBufferPrefix = bufferPrefix;
    }

    /* access modifiers changed from: package-private */
    public ParcelFileDescriptor getReadFd() {
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

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x004c, code lost:
        r7 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x004d, code lost:
        r8 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0051, code lost:
        r8 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0052, code lost:
        r9 = r8;
        r8 = r7;
        r7 = r9;
     */
    public static byte[] dumpAsync(IBinder binder, String... args) throws IOException, RemoteException {
        ByteArrayOutputStream combinedBuffer;
        Throwable th;
        Throwable th2;
        ParcelFileDescriptor[] pipe = ParcelFileDescriptor.createPipe();
        try {
            dumpAsync(binder, pipe[1].getFileDescriptor(), args);
            pipe[1].close();
            pipe[1] = null;
            byte[] buffer = new byte[4096];
            combinedBuffer = new ByteArrayOutputStream();
            FileInputStream is = new FileInputStream(pipe[0].getFileDescriptor());
            while (true) {
                int numRead = is.read(buffer);
                if (numRead == -1) {
                    $closeResource(null, is);
                    byte[] byteArray = combinedBuffer.toByteArray();
                    $closeResource(null, combinedBuffer);
                    pipe[0].close();
                    IoUtils.closeQuietly(pipe[1]);
                    return byteArray;
                }
                combinedBuffer.write(buffer, 0, numRead);
            }
            $closeResource(th, is);
            throw th2;
        } catch (Throwable th3) {
            pipe[0].close();
            IoUtils.closeQuietly(pipe[1]);
            throw th3;
        }
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                x0.addSuppressed(th);
            }
        } else {
            x1.close();
        }
    }

    static void go(Caller caller, IInterface iface, FileDescriptor out, String prefix, String[] args) throws IOException, RemoteException {
        go(caller, iface, out, prefix, args, DEFAULT_TIMEOUT);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0026, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x002a, code lost:
        $closeResource(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x002d, code lost:
        throw r2;
     */
    static void go(Caller caller, IInterface iface, FileDescriptor out, String prefix, String[] args, long timeout) throws IOException, RemoteException {
        if (iface.asBinder() instanceof Binder) {
            try {
                caller.go(iface, out, prefix, args);
            } catch (RemoteException e) {
            }
            return;
        }
        TransferPipe tp = new TransferPipe();
        caller.go(iface, tp.getWriteFd().getFileDescriptor(), prefix, args);
        tp.go(out, timeout);
        $closeResource(null, tp);
    }

    static void goDump(IBinder binder, FileDescriptor out, String[] args) throws IOException, RemoteException {
        goDump(binder, out, args, DEFAULT_TIMEOUT);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0022, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0026, code lost:
        $closeResource(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0029, code lost:
        throw r2;
     */
    static void goDump(IBinder binder, FileDescriptor out, String[] args, long timeout) throws IOException, RemoteException {
        if (binder instanceof Binder) {
            try {
                binder.dump(out, args);
            } catch (RemoteException e) {
            }
            return;
        }
        TransferPipe tp = new TransferPipe();
        binder.dumpAsync(tp.getWriteFd().getFileDescriptor(), args);
        tp.go(out, timeout);
        $closeResource(null, tp);
    }

    public void go(FileDescriptor out) throws IOException {
        go(out, DEFAULT_TIMEOUT);
    }

    public void go(FileDescriptor out, long timeout) throws IOException {
        try {
            synchronized (this) {
                this.mOutFd = out;
                this.mEndTime = SystemClock.uptimeMillis() + timeout;
                closeFd(1);
                this.mThread.start();
                while (this.mFailure == null && !this.mComplete) {
                    long waitTime = this.mEndTime - SystemClock.uptimeMillis();
                    if (waitTime > 0) {
                        try {
                            wait(waitTime);
                        } catch (InterruptedException e) {
                        }
                    } else {
                        this.mThread.interrupt();
                        throw new IOException("Timeout");
                    }
                }
                if (this.mFailure != null) {
                    throw new IOException(this.mFailure);
                }
            }
            kill();
        } catch (Throwable th) {
            kill();
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public void closeFd(int num) {
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

    /* access modifiers changed from: protected */
    public OutputStream getNewOutputStream() {
        return new FileOutputStream(this.mOutFd);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0023, code lost:
        r3 = null;
        r4 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0027, code lost:
        if (r11.mBufferPrefix == null) goto L_0x002f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0029, code lost:
        r3 = r11.mBufferPrefix.getBytes();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:?, code lost:
        r5 = r2.read(r0);
        r6 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0035, code lost:
        if (r5 <= 0) goto L_0x006a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0037, code lost:
        r5 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0038, code lost:
        if (r3 != null) goto L_0x003e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x003a, code lost:
        r1.write(r0, 0, r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x003e, code lost:
        r8 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0040, code lost:
        if (r5 >= r6) goto L_0x0062;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0046, code lost:
        if (r0[r5] == 10) goto L_0x0060;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0048, code lost:
        if (r5 <= r8) goto L_0x004f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x004a, code lost:
        r1.write(r0, r8, r5 - r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x004f, code lost:
        r8 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0050, code lost:
        if (r4 == false) goto L_0x0056;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0052, code lost:
        r1.write(r3);
        r4 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0056, code lost:
        r5 = r5 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0057, code lost:
        if (r5 >= r6) goto L_0x005d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x005b, code lost:
        if (r0[r5] != 10) goto L_0x0056;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x005d, code lost:
        if (r5 >= r6) goto L_0x0060;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x005f, code lost:
        r4 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0060, code lost:
        r5 = r5 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0062, code lost:
        if (r6 <= r8) goto L_0x0069;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0064, code lost:
        r1.write(r0, r8, r6 - r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x006a, code lost:
        r11.mThread.isInterrupted();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0071, code lost:
        monitor-enter(r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:?, code lost:
        r11.mComplete = true;
        notifyAll();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x0077, code lost:
        monitor-exit(r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x0078, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x007c, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x007d, code lost:
        monitor-enter(r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:?, code lost:
        r11.mFailure = r5.toString();
        notifyAll();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x0088, code lost:
        return;
     */
    public void run() {
        byte[] buffer = new byte[1024];
        synchronized (this) {
            ParcelFileDescriptor readFd = getReadFd();
            if (readFd == null) {
                Slog.w(TAG, "Pipe has been closed...");
            } else {
                FileInputStream fis = new FileInputStream(readFd.getFileDescriptor());
                OutputStream fos = getNewOutputStream();
            }
        }
    }
}
