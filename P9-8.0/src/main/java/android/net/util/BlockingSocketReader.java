package android.net.util;

import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import java.io.FileDescriptor;
import java.io.IOException;
import libcore.io.IoBridge;

public abstract class BlockingSocketReader {
    public static final int DEFAULT_RECV_BUF_SIZE = 2048;
    private final byte[] mPacket;
    private volatile long mPacketsReceived;
    private volatile boolean mRunning;
    private volatile FileDescriptor mSocket;
    private final Thread mThread;

    protected abstract FileDescriptor createSocket();

    public static final void closeSocket(FileDescriptor fd) {
        try {
            IoBridge.closeAndSignalBlockedThreads(fd);
        } catch (IOException e) {
        }
    }

    protected BlockingSocketReader() {
        this(2048);
    }

    protected BlockingSocketReader(int recvbufsize) {
        if (recvbufsize < 2048) {
            recvbufsize = 2048;
        }
        this.mPacket = new byte[recvbufsize];
        this.mThread = new Thread(new -$Lambda$uJqW9o1MiZ5yyXhSTG2zE-YhRn0(this));
    }

    public final boolean start() {
        if (this.mSocket != null) {
            return false;
        }
        try {
            this.mSocket = createSocket();
            if (this.mSocket == null) {
                return false;
            }
            this.mRunning = true;
            this.mThread.start();
            return true;
        } catch (Exception e) {
            logError("Failed to create socket: ", e);
            return false;
        }
    }

    public final void stop() {
        this.mRunning = false;
        closeSocket(this.mSocket);
        this.mSocket = null;
    }

    public final boolean isRunning() {
        return this.mRunning;
    }

    public final long numPacketsReceived() {
        return this.mPacketsReceived;
    }

    protected void handlePacket(byte[] recvbuf, int length) {
    }

    protected void logError(String msg, Exception e) {
    }

    protected void onExit() {
    }

    private final void mainLoop() {
        while (isRunning()) {
            try {
                int bytesRead = Os.read(this.mSocket, this.mPacket, 0, this.mPacket.length);
                if (bytesRead < 1) {
                    if (isRunning()) {
                        logError("Socket closed, exiting", null);
                    }
                    stop();
                    onExit();
                }
                this.mPacketsReceived++;
                try {
                    handlePacket(this.mPacket, bytesRead);
                } catch (Exception e) {
                    logError("Unexpected exception: ", e);
                }
            } catch (ErrnoException e2) {
                if (e2.errno != OsConstants.EINTR) {
                    if (isRunning()) {
                        logError("read error: ", e2);
                    }
                }
            } catch (IOException ioe) {
                if (isRunning()) {
                    logError("read error: ", ioe);
                }
            }
        }
        stop();
        onExit();
    }
}
