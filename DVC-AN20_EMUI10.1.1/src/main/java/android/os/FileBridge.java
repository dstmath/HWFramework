package android.os;

import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.util.Log;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;
import libcore.io.IoBridge;
import libcore.io.IoUtils;
import libcore.io.Memory;
import libcore.io.Streams;
import libcore.util.ArrayUtils;

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

    public void run() {
        byte[] temp = new byte[8192];
        while (true) {
            try {
                if (IoBridge.read(this.mServer, temp, 0, 8) != 8) {
                    break;
                }
                int cmd = Memory.peekInt(temp, 0, ByteOrder.BIG_ENDIAN);
                if (cmd == 1) {
                    int len = Memory.peekInt(temp, 4, ByteOrder.BIG_ENDIAN);
                    while (len > 0) {
                        int n = IoBridge.read(this.mServer, temp, 0, Math.min(temp.length, len));
                        if (n != -1) {
                            IoBridge.write(this.mTarget, temp, 0, n);
                            len -= n;
                        } else {
                            throw new IOException("Unexpected EOF; still expected " + len + " bytes");
                        }
                    }
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
            } catch (ErrnoException | IOException e) {
                Log.wtf(TAG, "Failed during bridge", e);
            } catch (Throwable th) {
                forceClose();
                throw th;
            }
        }
        forceClose();
    }

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

        @Override // java.io.OutputStream, java.io.Closeable, java.lang.AutoCloseable
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

        @Override // java.io.OutputStream
        public void write(byte[] buffer, int byteOffset, int byteCount) throws IOException {
            ArrayUtils.throwsIfOutOfBounds(buffer.length, byteOffset, byteCount);
            Memory.pokeInt(this.mTemp, 0, 1, ByteOrder.BIG_ENDIAN);
            Memory.pokeInt(this.mTemp, 4, byteCount, ByteOrder.BIG_ENDIAN);
            IoBridge.write(this.mClient, this.mTemp, 0, 8);
            IoBridge.write(this.mClient, buffer, byteOffset, byteCount);
        }

        @Override // java.io.OutputStream
        public void write(int oneByte) throws IOException {
            Streams.writeSingleByte(this, oneByte);
        }
    }
}
