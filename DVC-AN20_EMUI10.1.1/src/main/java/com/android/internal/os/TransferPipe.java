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

    /* access modifiers changed from: package-private */
    public interface Caller {
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

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x004e, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x004f, code lost:
        $closeResource(r3, r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0052, code lost:
        throw r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0055, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0056, code lost:
        $closeResource(r3, r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0059, code lost:
        throw r6;
     */
    public static byte[] dumpAsync(IBinder binder, String... args) throws IOException, RemoteException {
        ParcelFileDescriptor[] pipe = ParcelFileDescriptor.createPipe();
        try {
            dumpAsync(binder, pipe[1].getFileDescriptor(), args);
            pipe[1].close();
            pipe[1] = null;
            byte[] buffer = new byte[4096];
            ByteArrayOutputStream combinedBuffer = new ByteArrayOutputStream();
            FileInputStream is = new FileInputStream(pipe[0].getFileDescriptor());
            while (true) {
                int numRead = is.read(buffer);
                if (numRead == -1) {
                    $closeResource(null, is);
                    byte[] byteArray = combinedBuffer.toByteArray();
                    $closeResource(null, combinedBuffer);
                    return byteArray;
                }
                combinedBuffer.write(buffer, 0, numRead);
            }
        } finally {
            pipe[0].close();
            IoUtils.closeQuietly(pipe[1]);
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
        go(caller, iface, out, prefix, args, 5000);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0028, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0029, code lost:
        $closeResource(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x002c, code lost:
        throw r2;
     */
    static void go(Caller caller, IInterface iface, FileDescriptor out, String prefix, String[] args, long timeout) throws IOException, RemoteException {
        if (iface.asBinder() instanceof Binder) {
            try {
                caller.go(iface, out, prefix, args);
            } catch (RemoteException e) {
            }
        } else {
            TransferPipe tp = new TransferPipe();
            caller.go(iface, tp.getWriteFd().getFileDescriptor(), prefix, args);
            tp.go(out, timeout);
            $closeResource(null, tp);
        }
    }

    static void goDump(IBinder binder, FileDescriptor out, String[] args) throws IOException, RemoteException {
        goDump(binder, out, args, 5000);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0024, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0025, code lost:
        $closeResource(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0028, code lost:
        throw r2;
     */
    static void goDump(IBinder binder, FileDescriptor out, String[] args, long timeout) throws IOException, RemoteException {
        if (binder instanceof Binder) {
            try {
                binder.dump(out, args);
            } catch (RemoteException e) {
            }
        } else {
            TransferPipe tp = new TransferPipe();
            binder.dumpAsync(tp.getWriteFd().getFileDescriptor(), args);
            tp.go(out, timeout);
            $closeResource(null, tp);
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
        } finally {
            kill();
        }
    }

    /* access modifiers changed from: package-private */
    public void closeFd(int num) {
        ParcelFileDescriptor[] parcelFileDescriptorArr = this.mFds;
        if (parcelFileDescriptorArr[num] != null) {
            try {
                parcelFileDescriptorArr[num].close();
            } catch (IOException e) {
            }
            this.mFds[num] = null;
        }
    }

    @Override // java.io.Closeable, java.lang.AutoCloseable
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
        r5 = r11.mBufferPrefix;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0027, code lost:
        if (r5 == null) goto L_0x002d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0029, code lost:
        r3 = r5.getBytes();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:?, code lost:
        r5 = r2.read(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0033, code lost:
        if (r5 <= 0) goto L_0x0068;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0035, code lost:
        if (r3 != null) goto L_0x003c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0037, code lost:
        r3.write(r0, 0, r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x003c, code lost:
        r5 = 0;
        r8 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x003e, code lost:
        if (r8 >= r5) goto L_0x0060;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0044, code lost:
        if (r0[r8] == 10) goto L_0x005e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0046, code lost:
        if (r8 <= r5) goto L_0x004d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0048, code lost:
        r3.write(r0, r5, r8 - r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x004d, code lost:
        r5 = r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x004e, code lost:
        if (r4 == false) goto L_0x0054;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0050, code lost:
        r3.write(r3);
        r4 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0054, code lost:
        r8 = r8 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0055, code lost:
        if (r8 >= r5) goto L_0x005b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0059, code lost:
        if (r0[r8] != 10) goto L_0x0054;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x005b, code lost:
        if (r8 >= r5) goto L_0x005e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x005d, code lost:
        r4 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x005e, code lost:
        r8 = r8 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0060, code lost:
        if (r5 <= r5) goto L_0x0067;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0062, code lost:
        r3.write(r0, r5, r5 - r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0068, code lost:
        r11.mThread.isInterrupted();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x006e, code lost:
        monitor-enter(r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:?, code lost:
        r11.mComplete = true;
        notifyAll();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x0074, code lost:
        monitor-exit(r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x0075, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x0079, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x007a, code lost:
        monitor-enter(r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:?, code lost:
        r11.mFailure = r5.toString();
        notifyAll();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x0085, code lost:
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
