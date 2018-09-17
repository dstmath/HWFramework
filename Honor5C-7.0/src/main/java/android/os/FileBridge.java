package android.os;

import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.util.Log;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.util.Arrays;
import libcore.io.IoBridge;
import libcore.io.IoUtils;
import libcore.io.Memory;
import libcore.io.Streams;

public class FileBridge extends Thread {
    private static final int CMD_CLOSE = 3;
    private static final int CMD_FSYNC = 2;
    private static final int CMD_WRITE = 1;
    private static final int MSG_LENGTH = 8;
    private static final String TAG = "FileBridge";
    private final FileDescriptor mClient;
    private volatile boolean mClosed;
    private final FileDescriptor mServer;
    private FileDescriptor mTarget;

    public static class FileBridgeOutputStream extends OutputStream {
        private final FileDescriptor mClient;
        private final ParcelFileDescriptor mClientPfd;
        private final byte[] mTemp;

        public FileBridgeOutputStream(ParcelFileDescriptor clientPfd) {
            this.mTemp = new byte[FileBridge.MSG_LENGTH];
            this.mClientPfd = clientPfd;
            this.mClient = clientPfd.getFileDescriptor();
        }

        public FileBridgeOutputStream(FileDescriptor client) {
            this.mTemp = new byte[FileBridge.MSG_LENGTH];
            this.mClientPfd = null;
            this.mClient = client;
        }

        public void close() throws IOException {
            try {
                writeCommandAndBlock(FileBridge.CMD_CLOSE, "close()");
            } finally {
                IoBridge.closeAndSignalBlockedThreads(this.mClient);
                IoUtils.closeQuietly(this.mClientPfd);
            }
        }

        public void fsync() throws IOException {
            writeCommandAndBlock(FileBridge.CMD_FSYNC, "fsync()");
        }

        private void writeCommandAndBlock(int cmd, String cmdString) throws IOException {
            Memory.pokeInt(this.mTemp, 0, cmd, ByteOrder.BIG_ENDIAN);
            IoBridge.write(this.mClient, this.mTemp, 0, FileBridge.MSG_LENGTH);
            if (IoBridge.read(this.mClient, this.mTemp, 0, FileBridge.MSG_LENGTH) != FileBridge.MSG_LENGTH || Memory.peekInt(this.mTemp, 0, ByteOrder.BIG_ENDIAN) != cmd) {
                throw new IOException("Failed to execute " + cmdString + " across bridge");
            }
        }

        public void write(byte[] buffer, int byteOffset, int byteCount) throws IOException {
            Arrays.checkOffsetAndCount(buffer.length, byteOffset, byteCount);
            Memory.pokeInt(this.mTemp, 0, FileBridge.CMD_WRITE, ByteOrder.BIG_ENDIAN);
            Memory.pokeInt(this.mTemp, 4, byteCount, ByteOrder.BIG_ENDIAN);
            IoBridge.write(this.mClient, this.mTemp, 0, FileBridge.MSG_LENGTH);
            IoBridge.write(this.mClient, buffer, byteOffset, byteCount);
        }

        public void write(int oneByte) throws IOException {
            Streams.writeSingleByte(this, oneByte);
        }
    }

    public FileBridge() {
        this.mServer = new FileDescriptor();
        this.mClient = new FileDescriptor();
        try {
            Os.socketpair(OsConstants.AF_UNIX, OsConstants.SOCK_STREAM, 0, this.mServer, this.mClient);
        } catch (ErrnoException e) {
            throw new RuntimeException("Failed to create bridge");
        }
    }

    public boolean isClosed() {
        return this.mClosed;
    }

    public void forceClose() {
        IoUtils.closeQuietly(this.mTarget);
        IoUtils.closeQuietly(this.mServer);
        IoUtils.closeQuietly(this.mClient);
        this.mClosed = true;
    }

    public void setTargetFile(FileDescriptor target) {
        this.mTarget = target;
    }

    public FileDescriptor getClientSocket() {
        return this.mClient;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void run() {
        byte[] temp = new byte[Process.PROC_OUT_LONG];
        while (IoBridge.read(this.mServer, temp, 0, MSG_LENGTH) == MSG_LENGTH) {
            int cmd = Memory.peekInt(temp, 0, ByteOrder.BIG_ENDIAN);
            if (cmd == CMD_WRITE) {
                int len = Memory.peekInt(temp, 4, ByteOrder.BIG_ENDIAN);
                while (len > 0) {
                    int n = IoBridge.read(this.mServer, temp, 0, Math.min(temp.length, len));
                    if (n == -1) {
                        throw new IOException("Unexpected EOF; still expected " + len + " bytes");
                    }
                    try {
                        IoBridge.write(this.mTarget, temp, 0, n);
                        len -= n;
                    } catch (Exception e) {
                        Log.wtf(TAG, "Failed during bridge", e);
                        return;
                    } catch (Throwable th) {
                        forceClose();
                    }
                }
                continue;
            } else if (cmd == CMD_FSYNC) {
                Os.fsync(this.mTarget);
                IoBridge.write(this.mServer, temp, 0, MSG_LENGTH);
            } else if (cmd == CMD_CLOSE) {
                Os.fsync(this.mTarget);
                Os.close(this.mTarget);
                this.mClosed = true;
                IoBridge.write(this.mServer, temp, 0, MSG_LENGTH);
                break;
            }
        }
        forceClose();
    }
}
