package android.os;

import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.util.Arrays;
import libcore.io.IoBridge;
import libcore.io.IoUtils;
import libcore.io.Memory;
import libcore.io.Streams;

@Deprecated
public class FileBridge extends Thread {
    private static final int CMD_CLOSE = 3;
    private static final int CMD_FSYNC = 2;
    private static final int CMD_WRITE = 1;
    private static final int MSG_LENGTH = 8;
    private static final String TAG = "FileBridge";
    private final FileDescriptor mClient = new FileDescriptor();
    private volatile boolean mClosed;
    private final FileDescriptor mServer = new FileDescriptor();
    private FileDescriptor mTarget;

    public static class FileBridgeOutputStream extends OutputStream {
        private final FileDescriptor mClient;
        private final ParcelFileDescriptor mClientPfd;
        private final byte[] mTemp;

        public FileBridgeOutputStream(ParcelFileDescriptor clientPfd) {
            this.mTemp = new byte[8];
            this.mClientPfd = clientPfd;
            this.mClient = clientPfd.getFileDescriptor();
        }

        public FileBridgeOutputStream(FileDescriptor client) {
            this.mTemp = new byte[8];
            this.mClientPfd = null;
            this.mClient = client;
        }

        public void close() throws IOException {
            try {
                writeCommandAndBlock(3, "close()");
            } finally {
                IoBridge.closeAndSignalBlockedThreads(this.mClient);
                IoUtils.closeQuietly(this.mClientPfd);
            }
        }

        public void fsync() throws IOException {
            writeCommandAndBlock(2, "fsync()");
        }

        private void writeCommandAndBlock(int cmd, String cmdString) throws IOException {
            Memory.pokeInt(this.mTemp, 0, cmd, ByteOrder.BIG_ENDIAN);
            IoBridge.write(this.mClient, this.mTemp, 0, 8);
            if (IoBridge.read(this.mClient, this.mTemp, 0, 8) != 8 || Memory.peekInt(this.mTemp, 0, ByteOrder.BIG_ENDIAN) != cmd) {
                throw new IOException("Failed to execute " + cmdString + " across bridge");
            }
        }

        public void write(byte[] buffer, int byteOffset, int byteCount) throws IOException {
            Arrays.checkOffsetAndCount(buffer.length, byteOffset, byteCount);
            Memory.pokeInt(this.mTemp, 0, 1, ByteOrder.BIG_ENDIAN);
            Memory.pokeInt(this.mTemp, 4, byteCount, ByteOrder.BIG_ENDIAN);
            IoBridge.write(this.mClient, this.mTemp, 0, 8);
            IoBridge.write(this.mClient, buffer, byteOffset, byteCount);
        }

        public void write(int oneByte) throws IOException {
            Streams.writeSingleByte(this, oneByte);
        }
    }

    public FileBridge() {
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

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0054 A:{ExcHandler: android.system.ErrnoException (r1_0 'e' java.lang.Exception), Splitter: B:1:0x0007} */
    /* JADX WARNING: Missing block: B:12:0x0054, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:14:?, code:
            android.util.Log.wtf(TAG, "Failed during bridge", r1);
     */
    /* JADX WARNING: Missing block: B:15:0x005e, code:
            forceClose();
     */
    /* JADX WARNING: Missing block: B:36:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void run() {
        byte[] temp = new byte[8192];
        while (IoBridge.read(this.mServer, temp, 0, 8) == 8) {
            try {
                int cmd = Memory.peekInt(temp, 0, ByteOrder.BIG_ENDIAN);
                if (cmd == 1) {
                    int len = Memory.peekInt(temp, 4, ByteOrder.BIG_ENDIAN);
                    while (len > 0) {
                        int n = IoBridge.read(this.mServer, temp, 0, Math.min(temp.length, len));
                        if (n == -1) {
                            throw new IOException("Unexpected EOF; still expected " + len + " bytes");
                        }
                        IoBridge.write(this.mTarget, temp, 0, n);
                        len -= n;
                    }
                    continue;
                } else if (cmd == 2) {
                    Os.fsync(this.mTarget);
                    IoBridge.write(this.mServer, temp, 0, 8);
                } else if (cmd == 3) {
                    Os.fsync(this.mTarget);
                    Os.close(this.mTarget);
                    this.mClosed = true;
                    IoBridge.write(this.mServer, temp, 0, 8);
                    break;
                }
            } catch (Exception e) {
            } catch (Throwable th) {
                forceClose();
                throw th;
            }
        }
        forceClose();
    }
}
