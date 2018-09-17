package com.android.internal.os;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Slog;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.microedition.khronos.opengles.GL10;

public final class TransferPipe implements Runnable {
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
        this.mThread = new Thread(this, TAG);
        this.mFds = ParcelFileDescriptor.createPipe();
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

    static void go(Caller caller, IInterface iface, FileDescriptor out, String prefix, String[] args) throws IOException, RemoteException {
        go(caller, iface, out, prefix, args, DEFAULT_TIMEOUT);
    }

    static void go(Caller caller, IInterface iface, FileDescriptor out, String prefix, String[] args, long timeout) throws IOException, RemoteException {
        if (iface.asBinder() instanceof Binder) {
            try {
                caller.go(iface, out, prefix, args);
            } catch (RemoteException e) {
            }
            return;
        }
        TransferPipe tp = new TransferPipe();
        try {
            caller.go(iface, tp.getWriteFd().getFileDescriptor(), prefix, args);
            tp.go(out, timeout);
        } finally {
            tp.kill();
        }
    }

    static void goDump(IBinder binder, FileDescriptor out, String[] args) throws IOException, RemoteException {
        goDump(binder, out, args, DEFAULT_TIMEOUT);
    }

    static void goDump(IBinder binder, FileDescriptor out, String[] args, long timeout) throws IOException, RemoteException {
        if (binder instanceof Binder) {
            try {
                binder.dump(out, args);
            } catch (RemoteException e) {
            }
            return;
        }
        TransferPipe tp = new TransferPipe();
        try {
            binder.dumpAsync(tp.getWriteFd().getFileDescriptor(), args);
            tp.go(out, timeout);
        } finally {
            tp.kill();
        }
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

    public void kill() {
        synchronized (this) {
            closeFd(0);
            closeFd(1);
        }
    }

    public void run() {
        byte[] buffer = new byte[GL10.GL_STENCIL_BUFFER_BIT];
        synchronized (this) {
            ParcelFileDescriptor readFd = getReadFd();
            if (readFd == null) {
                Slog.w(TAG, "Pipe has been closed...");
                return;
            }
            FileInputStream fis = new FileInputStream(readFd.getFileDescriptor());
            FileOutputStream fos = new FileOutputStream(this.mOutFd);
            byte[] bufferPrefix = null;
            boolean needPrefix = true;
            if (this.mBufferPrefix != null) {
                bufferPrefix = this.mBufferPrefix.getBytes();
            }
            while (true) {
                try {
                    int size = fis.read(buffer);
                    if (size <= 0) {
                        break;
                    } else if (bufferPrefix == null) {
                        fos.write(buffer, 0, size);
                    } else {
                        int start = 0;
                        int i = 0;
                        while (i < size) {
                            if (buffer[i] != (byte) 10) {
                                if (i > start) {
                                    fos.write(buffer, start, i - start);
                                }
                                start = i;
                                if (needPrefix) {
                                    fos.write(bufferPrefix);
                                    needPrefix = DEBUG;
                                }
                                do {
                                    i++;
                                    if (i >= size) {
                                        break;
                                    }
                                } while (buffer[i] != (byte) 10);
                                if (i < size) {
                                    needPrefix = true;
                                }
                            }
                            i++;
                        }
                        if (size > start) {
                            fos.write(buffer, start, size - start);
                        }
                    }
                } catch (IOException e) {
                    synchronized (this) {
                    }
                    this.mFailure = e.toString();
                    notifyAll();
                    return;
                }
            }
            if (this.mThread.isInterrupted()) {
                synchronized (this) {
                }
            }
            this.mComplete = true;
            notifyAll();
        }
    }
}
