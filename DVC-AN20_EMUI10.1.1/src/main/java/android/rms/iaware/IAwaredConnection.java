package android.rms.iaware;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.SystemClock;
import android.system.ErrnoException;
import android.system.Int32Ref;
import android.system.Os;
import android.system.OsConstants;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.OutputStream;

public class IAwaredConnection {
    private static final long FLUSH_TIMEOUT = 2000;
    private static final String LOCAL_SOCKET_NAME = "iawared";
    private static final int PENGING_MAX_SLEEP = 10;
    private static final int PENGING_MID_SLEEP = 5;
    private static final int PENGING_MIN_SLEEP = 1;
    private static final int PENGING_VALUE_MAX = 5000;
    private static final int PENGING_VALUE_MIN = 1000;
    private static final String TAG = "IAwaredConnection";
    private static IAwaredConnection sInstance = null;
    private static final Object sLock = new Object();
    private OutputStream mOutStream = null;
    private LocalSocket mSock = null;

    private IAwaredConnection() {
    }

    public static IAwaredConnection getInstance() {
        IAwaredConnection iAwaredConnection;
        synchronized (sLock) {
            if (sInstance == null) {
                sInstance = new IAwaredConnection();
            }
            iAwaredConnection = sInstance;
        }
        return iAwaredConnection;
    }

    public synchronized void create() {
        if (!createImpl()) {
            AwareLog.e(TAG, "Failed to create connection");
        }
    }

    public synchronized void destroy() {
        destroyImpl();
    }

    public boolean sendPacket(byte[] msg) {
        if (msg != null && msg.length != 0) {
            return sendPacket(msg, 0, msg.length);
        }
        AwareLog.e(TAG, "Parameter check failed");
        return false;
    }

    public synchronized boolean sendPacket(byte[] msg, int offset, int count) {
        if (msg != null && offset >= 0 && count > 0) {
            if (offset <= msg.length - count) {
                if (!createImpl()) {
                    AwareLog.e(TAG, "Failed to create connection");
                    return false;
                }
                try {
                    this.mOutStream.write(msg, offset, count);
                    flush(FLUSH_TIMEOUT);
                    return true;
                } catch (IOException e) {
                    AwareLog.e(TAG, "Failed to write output stream, IOException");
                    destroyImpl();
                    return false;
                }
            }
        }
        AwareLog.e(TAG, "Parameter check failed");
        return false;
    }

    private boolean createImpl() {
        if (this.mSock != null) {
            return true;
        }
        try {
            this.mSock = new LocalSocket(3);
            this.mSock.connect(new LocalSocketAddress(LOCAL_SOCKET_NAME, LocalSocketAddress.Namespace.RESERVED));
            this.mOutStream = this.mSock.getOutputStream();
            return true;
        } catch (IOException e) {
            AwareLog.e(TAG, "Failed to create connection, IOException");
            destroyImpl();
            return false;
        } catch (SecurityException e2) {
            AwareLog.e(TAG, "Failed to create connection, SecurityException");
            destroyImpl();
            return false;
        }
    }

    private void destroyImpl() {
        OutputStream outputStream = this.mOutStream;
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e) {
                AwareLog.e(TAG, "Failed to close output stream, IOException");
            } catch (Throwable th) {
                this.mOutStream = null;
                throw th;
            }
            this.mOutStream = null;
        }
        LocalSocket localSocket = this.mSock;
        if (localSocket != null) {
            try {
                localSocket.close();
            } catch (IOException e2) {
                AwareLog.e(TAG, "Failed to close local socket, IOException");
            } catch (Throwable th2) {
                this.mSock = null;
                throw th2;
            }
            this.mSock = null;
        }
    }

    private void flush(long millis) throws IOException {
        FileDescriptor myFd = this.mSock.getFileDescriptor();
        if (myFd != null) {
            long start = SystemClock.uptimeMillis();
            Int32Ref pending = new Int32Ref(0);
            while (true) {
                try {
                    Os.ioctlInt(myFd, OsConstants.TIOCOUTQ, pending);
                    if (pending.value > 0) {
                        if (SystemClock.uptimeMillis() - start < millis) {
                            int left = pending.value;
                            if (left <= 1000) {
                                try {
                                    Thread.sleep(1);
                                } catch (InterruptedException e) {
                                    return;
                                }
                            } else if (left <= PENGING_VALUE_MAX) {
                                Thread.sleep(5);
                            } else {
                                Thread.sleep(10);
                            }
                        } else {
                            AwareLog.e(TAG, "Socket flush timed out !!!");
                            throw new IOException("flush timeout");
                        }
                    } else {
                        return;
                    }
                } catch (ErrnoException e2) {
                    throw e2.rethrowAsIOException();
                }
            }
        } else {
            throw new IOException("socket closed");
        }
    }
}
