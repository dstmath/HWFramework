package android.os;

import android.annotation.UnsupportedAppUsage;
import android.os.MessageQueue;
import android.os.Parcelable;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.system.StructStat;
import android.util.Log;
import dalvik.system.CloseGuard;
import dalvik.system.VMRuntime;
import java.io.Closeable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.UncheckedIOException;
import java.net.DatagramSocket;
import java.net.Socket;
import java.nio.ByteOrder;
import libcore.io.IoUtils;
import libcore.io.Memory;

public class ParcelFileDescriptor implements Parcelable, Closeable {
    public static final Parcelable.Creator<ParcelFileDescriptor> CREATOR = new Parcelable.Creator<ParcelFileDescriptor>() {
        /* class android.os.ParcelFileDescriptor.AnonymousClass2 */

        @Override // android.os.Parcelable.Creator
        public ParcelFileDescriptor createFromParcel(Parcel in) {
            int hasCommChannel = in.readInt();
            FileDescriptor fd = in.readRawFileDescriptor();
            FileDescriptor commChannel = null;
            if (hasCommChannel != 0) {
                commChannel = in.readRawFileDescriptor();
            }
            return new ParcelFileDescriptor(fd, commChannel);
        }

        @Override // android.os.Parcelable.Creator
        public ParcelFileDescriptor[] newArray(int size) {
            return new ParcelFileDescriptor[size];
        }
    };
    private static final int MAX_STATUS = 1024;
    public static final int MODE_APPEND = 33554432;
    public static final int MODE_CREATE = 134217728;
    public static final int MODE_READ_ONLY = 268435456;
    public static final int MODE_READ_WRITE = 805306368;
    public static final int MODE_TRUNCATE = 67108864;
    @Deprecated
    public static final int MODE_WORLD_READABLE = 1;
    @Deprecated
    public static final int MODE_WORLD_WRITEABLE = 2;
    public static final int MODE_WRITE_ONLY = 536870912;
    private static final String TAG = "ParcelFileDescriptor";
    private volatile boolean mClosed;
    private FileDescriptor mCommFd;
    private final FileDescriptor mFd;
    private final CloseGuard mGuard;
    private Status mStatus;
    private byte[] mStatusBuf;
    private final ParcelFileDescriptor mWrapped;

    public interface OnCloseListener {
        void onClose(IOException iOException);
    }

    public ParcelFileDescriptor(ParcelFileDescriptor wrapped) {
        this.mGuard = CloseGuard.get();
        this.mWrapped = wrapped;
        this.mFd = null;
        this.mCommFd = null;
        this.mClosed = true;
    }

    @UnsupportedAppUsage
    public ParcelFileDescriptor(FileDescriptor fd) {
        this(fd, null);
    }

    public ParcelFileDescriptor(FileDescriptor fd, FileDescriptor commChannel) {
        this.mGuard = CloseGuard.get();
        if (fd != null) {
            this.mWrapped = null;
            this.mFd = fd;
            IoUtils.setFdOwner(this.mFd, this);
            this.mCommFd = commChannel;
            FileDescriptor fileDescriptor = this.mCommFd;
            if (fileDescriptor != null) {
                IoUtils.setFdOwner(fileDescriptor, this);
            }
            this.mGuard.open("close");
            return;
        }
        throw new NullPointerException("FileDescriptor must not be null");
    }

    public static ParcelFileDescriptor open(File file, int mode) throws FileNotFoundException {
        FileDescriptor fd = openInternal(file, mode);
        if (fd == null) {
            return null;
        }
        return new ParcelFileDescriptor(fd);
    }

    public static ParcelFileDescriptor open(File file, int mode, Handler handler, OnCloseListener listener) throws IOException {
        if (handler == null) {
            throw new IllegalArgumentException("Handler must not be null");
        } else if (listener != null) {
            FileDescriptor fd = openInternal(file, mode);
            if (fd == null) {
                return null;
            }
            return fromFd(fd, handler, listener);
        } else {
            throw new IllegalArgumentException("Listener must not be null");
        }
    }

    public static ParcelFileDescriptor fromPfd(ParcelFileDescriptor pfd, Handler handler, OnCloseListener listener) throws IOException {
        FileDescriptor original = new FileDescriptor();
        original.setInt$(pfd.detachFd());
        return fromFd(original, handler, listener);
    }

    public static ParcelFileDescriptor fromFd(FileDescriptor fd, Handler handler, final OnCloseListener listener) throws IOException {
        if (handler == null) {
            throw new IllegalArgumentException("Handler must not be null");
        } else if (listener != null) {
            FileDescriptor[] comm = createCommSocketPair();
            ParcelFileDescriptor pfd = new ParcelFileDescriptor(fd, comm[0]);
            final MessageQueue queue = handler.getLooper().getQueue();
            queue.addOnFileDescriptorEventListener(comm[1], 1, new MessageQueue.OnFileDescriptorEventListener() {
                /* class android.os.ParcelFileDescriptor.AnonymousClass1 */

                @Override // android.os.MessageQueue.OnFileDescriptorEventListener
                public int onFileDescriptorEvents(FileDescriptor fd, int events) {
                    Status status = null;
                    if ((events & 1) != 0) {
                        status = ParcelFileDescriptor.readCommStatus(fd, new byte[1024]);
                    } else if ((events & 4) != 0) {
                        status = new Status(-2);
                    }
                    if (status == null) {
                        return 1;
                    }
                    MessageQueue.this.removeOnFileDescriptorEventListener(fd);
                    IoUtils.closeQuietly(fd);
                    listener.onClose(status.asIOException());
                    return 0;
                }
            });
            return pfd;
        } else {
            throw new IllegalArgumentException("Listener must not be null");
        }
    }

    private static FileDescriptor openInternal(File file, int mode) throws FileNotFoundException {
        int flags = FileUtils.translateModePfdToPosix(mode) | ifAtLeastQ(OsConstants.O_CLOEXEC);
        int realMode = OsConstants.S_IRWXU | OsConstants.S_IRWXG;
        if ((mode & 1) != 0) {
            realMode |= OsConstants.S_IROTH;
        }
        if ((mode & 2) != 0) {
            realMode |= OsConstants.S_IWOTH;
        }
        try {
            return Os.open(file.getPath(), flags, realMode);
        } catch (ErrnoException e) {
            throw new FileNotFoundException(e.getMessage());
        }
    }

    public static ParcelFileDescriptor dup(FileDescriptor orig) throws IOException {
        try {
            FileDescriptor fd = new FileDescriptor();
            fd.setInt$(Os.fcntlInt(orig, isAtLeastQ() ? OsConstants.F_DUPFD_CLOEXEC : OsConstants.F_DUPFD, 0));
            return new ParcelFileDescriptor(fd);
        } catch (ErrnoException e) {
            throw e.rethrowAsIOException();
        }
    }

    public ParcelFileDescriptor dup() throws IOException {
        ParcelFileDescriptor parcelFileDescriptor = this.mWrapped;
        if (parcelFileDescriptor != null) {
            return parcelFileDescriptor.dup();
        }
        return dup(getFileDescriptor());
    }

    public static ParcelFileDescriptor fromFd(int fd) throws IOException {
        FileDescriptor original = new FileDescriptor();
        original.setInt$(fd);
        try {
            FileDescriptor dup = new FileDescriptor();
            dup.setInt$(Os.fcntlInt(original, isAtLeastQ() ? OsConstants.F_DUPFD_CLOEXEC : OsConstants.F_DUPFD, 0));
            return new ParcelFileDescriptor(dup);
        } catch (ErrnoException e) {
            throw e.rethrowAsIOException();
        }
    }

    public static ParcelFileDescriptor adoptFd(int fd) {
        FileDescriptor fdesc = new FileDescriptor();
        fdesc.setInt$(fd);
        return new ParcelFileDescriptor(fdesc);
    }

    public static ParcelFileDescriptor fromSocket(Socket socket) {
        FileDescriptor fd = socket.getFileDescriptor$();
        if (fd == null) {
            return null;
        }
        try {
            return dup(fd);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static ParcelFileDescriptor fromDatagramSocket(DatagramSocket datagramSocket) {
        FileDescriptor fd = datagramSocket.getFileDescriptor$();
        if (fd == null) {
            return null;
        }
        try {
            return dup(fd);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static ParcelFileDescriptor[] createPipe() throws IOException {
        try {
            FileDescriptor[] fds = Os.pipe2(ifAtLeastQ(OsConstants.O_CLOEXEC));
            return new ParcelFileDescriptor[]{new ParcelFileDescriptor(fds[0]), new ParcelFileDescriptor(fds[1])};
        } catch (ErrnoException e) {
            throw e.rethrowAsIOException();
        }
    }

    public static ParcelFileDescriptor[] createReliablePipe() throws IOException {
        try {
            FileDescriptor[] comm = createCommSocketPair();
            FileDescriptor[] fds = Os.pipe2(ifAtLeastQ(OsConstants.O_CLOEXEC));
            return new ParcelFileDescriptor[]{new ParcelFileDescriptor(fds[0], comm[0]), new ParcelFileDescriptor(fds[1], comm[1])};
        } catch (ErrnoException e) {
            throw e.rethrowAsIOException();
        }
    }

    public static ParcelFileDescriptor[] createSocketPair() throws IOException {
        return createSocketPair(OsConstants.SOCK_STREAM);
    }

    public static ParcelFileDescriptor[] createSocketPair(int type) throws IOException {
        try {
            FileDescriptor fd0 = new FileDescriptor();
            FileDescriptor fd1 = new FileDescriptor();
            Os.socketpair(OsConstants.AF_UNIX, ifAtLeastQ(OsConstants.SOCK_CLOEXEC) | type, 0, fd0, fd1);
            return new ParcelFileDescriptor[]{new ParcelFileDescriptor(fd0), new ParcelFileDescriptor(fd1)};
        } catch (ErrnoException e) {
            throw e.rethrowAsIOException();
        }
    }

    public static ParcelFileDescriptor[] createReliableSocketPair() throws IOException {
        return createReliableSocketPair(OsConstants.SOCK_STREAM);
    }

    public static ParcelFileDescriptor[] createReliableSocketPair(int type) throws IOException {
        try {
            FileDescriptor[] comm = createCommSocketPair();
            FileDescriptor fd0 = new FileDescriptor();
            FileDescriptor fd1 = new FileDescriptor();
            Os.socketpair(OsConstants.AF_UNIX, ifAtLeastQ(OsConstants.SOCK_CLOEXEC) | type, 0, fd0, fd1);
            return new ParcelFileDescriptor[]{new ParcelFileDescriptor(fd0, comm[0]), new ParcelFileDescriptor(fd1, comm[1])};
        } catch (ErrnoException e) {
            throw e.rethrowAsIOException();
        }
    }

    private static FileDescriptor[] createCommSocketPair() throws IOException {
        try {
            FileDescriptor comm1 = new FileDescriptor();
            FileDescriptor comm2 = new FileDescriptor();
            Os.socketpair(OsConstants.AF_UNIX, OsConstants.SOCK_SEQPACKET | ifAtLeastQ(OsConstants.SOCK_CLOEXEC), 0, comm1, comm2);
            IoUtils.setBlocking(comm1, false);
            IoUtils.setBlocking(comm2, false);
            return new FileDescriptor[]{comm1, comm2};
        } catch (ErrnoException e) {
            throw e.rethrowAsIOException();
        }
    }

    @UnsupportedAppUsage
    @Deprecated
    public static ParcelFileDescriptor fromData(byte[] data, String name) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor = null;
        if (data == null) {
            return null;
        }
        MemoryFile file = new MemoryFile(name, data.length);
        try {
            if (data.length > 0) {
                file.writeBytes(data, 0, 0, data.length);
            }
            file.deactivate();
            FileDescriptor fd = file.getFileDescriptor();
            if (fd != null) {
                parcelFileDescriptor = dup(fd);
            }
            return parcelFileDescriptor;
        } finally {
            file.close();
        }
    }

    public static int parseMode(String mode) {
        return FileUtils.translateModePosixToPfd(FileUtils.translateModeStringToPosix(mode));
    }

    public static File getFile(FileDescriptor fd) throws IOException {
        try {
            String path = Os.readlink("/proc/self/fd/" + fd.getInt$());
            if (OsConstants.S_ISREG(Os.stat(path).st_mode)) {
                return new File(path);
            }
            throw new IOException("Not a regular file: " + path);
        } catch (ErrnoException e) {
            throw e.rethrowAsIOException();
        }
    }

    public FileDescriptor getFileDescriptor() {
        ParcelFileDescriptor parcelFileDescriptor = this.mWrapped;
        if (parcelFileDescriptor != null) {
            return parcelFileDescriptor.getFileDescriptor();
        }
        return this.mFd;
    }

    public long getStatSize() {
        ParcelFileDescriptor parcelFileDescriptor = this.mWrapped;
        if (parcelFileDescriptor != null) {
            return parcelFileDescriptor.getStatSize();
        }
        try {
            StructStat st = Os.fstat(this.mFd);
            if (!OsConstants.S_ISREG(st.st_mode)) {
                if (!OsConstants.S_ISLNK(st.st_mode)) {
                    return -1;
                }
            }
            return st.st_size;
        } catch (ErrnoException e) {
            Log.w(TAG, "fstat() failed: " + e);
            return -1;
        }
    }

    @UnsupportedAppUsage
    public long seekTo(long pos) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor = this.mWrapped;
        if (parcelFileDescriptor != null) {
            return parcelFileDescriptor.seekTo(pos);
        }
        try {
            return Os.lseek(this.mFd, pos, OsConstants.SEEK_SET);
        } catch (ErrnoException e) {
            throw e.rethrowAsIOException();
        }
    }

    public int getFd() {
        ParcelFileDescriptor parcelFileDescriptor = this.mWrapped;
        if (parcelFileDescriptor != null) {
            return parcelFileDescriptor.getFd();
        }
        if (!this.mClosed) {
            return this.mFd.getInt$();
        }
        throw new IllegalStateException("Already closed");
    }

    public int detachFd() {
        ParcelFileDescriptor parcelFileDescriptor = this.mWrapped;
        if (parcelFileDescriptor != null) {
            return parcelFileDescriptor.detachFd();
        }
        if (!this.mClosed) {
            int fd = IoUtils.acquireRawFd(this.mFd);
            writeCommStatusAndClose(2, null);
            this.mClosed = true;
            this.mGuard.close();
            releaseResources();
            return fd;
        }
        throw new IllegalStateException("Already closed");
    }

    @Override // java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        ParcelFileDescriptor parcelFileDescriptor = this.mWrapped;
        if (parcelFileDescriptor != null) {
            try {
                parcelFileDescriptor.close();
            } finally {
                releaseResources();
            }
        } else {
            closeWithStatus(0, null);
        }
    }

    public void closeWithError(String msg) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor = this.mWrapped;
        if (parcelFileDescriptor != null) {
            try {
                parcelFileDescriptor.closeWithError(msg);
            } finally {
                releaseResources();
            }
        } else if (msg != null) {
            closeWithStatus(1, msg);
        } else {
            throw new IllegalArgumentException("Message must not be null");
        }
    }

    private void closeWithStatus(int status, String msg) {
        if (!this.mClosed) {
            this.mClosed = true;
            CloseGuard closeGuard = this.mGuard;
            if (closeGuard != null) {
                closeGuard.close();
            }
            writeCommStatusAndClose(status, msg);
            IoUtils.closeQuietly(this.mFd);
            releaseResources();
        }
    }

    public void releaseResources() {
    }

    private byte[] getOrCreateStatusBuffer() {
        if (this.mStatusBuf == null) {
            this.mStatusBuf = new byte[1024];
        }
        return this.mStatusBuf;
    }

    private void writeCommStatusAndClose(int status, String msg) {
        if (this.mCommFd != null) {
            if (status == 2) {
                Log.w(TAG, "Peer expected signal when closed; unable to deliver after detach");
            }
            if (status == -1) {
                IoUtils.closeQuietly(this.mCommFd);
                this.mCommFd = null;
                return;
            }
            try {
                this.mStatus = readCommStatus(this.mCommFd, getOrCreateStatusBuffer());
                if (this.mStatus == null) {
                    try {
                        byte[] buf = getOrCreateStatusBuffer();
                        Memory.pokeInt(buf, 0, status, ByteOrder.BIG_ENDIAN);
                        int writePtr = 0 + 4;
                        if (msg != null) {
                            byte[] rawMsg = msg.getBytes();
                            int len = Math.min(rawMsg.length, buf.length - writePtr);
                            System.arraycopy(rawMsg, 0, buf, writePtr, len);
                            writePtr += len;
                        }
                        Os.write(this.mCommFd, buf, 0, writePtr);
                    } catch (ErrnoException e) {
                        Log.w(TAG, "Failed to report status: " + e);
                    } catch (InterruptedIOException e2) {
                        Log.w(TAG, "Failed to report status: " + e2);
                    }
                    IoUtils.closeQuietly(this.mCommFd);
                    this.mCommFd = null;
                }
            } finally {
                IoUtils.closeQuietly(this.mCommFd);
                this.mCommFd = null;
            }
        } else if (msg != null) {
            Log.w(TAG, "Unable to inform peer: " + msg);
        }
    }

    /* access modifiers changed from: private */
    public static Status readCommStatus(FileDescriptor comm, byte[] buf) {
        try {
            int n = Os.read(comm, buf, 0, buf.length);
            if (n == 0) {
                return new Status(-2);
            }
            int status = Memory.peekInt(buf, 0, ByteOrder.BIG_ENDIAN);
            if (status == 1) {
                return new Status(status, new String(buf, 4, n - 4));
            }
            return new Status(status);
        } catch (ErrnoException e) {
            if (e.errno == OsConstants.EAGAIN) {
                return null;
            }
            Log.d(TAG, "Failed to read status; assuming dead: " + e);
            return new Status(-2);
        } catch (InterruptedIOException e2) {
            Log.d(TAG, "Failed to read status; assuming dead: " + e2);
            return new Status(-2);
        }
    }

    public boolean canDetectErrors() {
        ParcelFileDescriptor parcelFileDescriptor = this.mWrapped;
        if (parcelFileDescriptor != null) {
            return parcelFileDescriptor.canDetectErrors();
        }
        return this.mCommFd != null;
    }

    public void checkError() throws IOException {
        ParcelFileDescriptor parcelFileDescriptor = this.mWrapped;
        if (parcelFileDescriptor != null) {
            parcelFileDescriptor.checkError();
            return;
        }
        if (this.mStatus == null) {
            FileDescriptor fileDescriptor = this.mCommFd;
            if (fileDescriptor == null) {
                Log.w(TAG, "Peer didn't provide a comm channel; unable to check for errors");
                return;
            }
            this.mStatus = readCommStatus(fileDescriptor, getOrCreateStatusBuffer());
        }
        Status status = this.mStatus;
        if (status != null && status.status != 0) {
            throw this.mStatus.asIOException();
        }
    }

    public static class AutoCloseInputStream extends FileInputStream {
        private final ParcelFileDescriptor mPfd;

        public AutoCloseInputStream(ParcelFileDescriptor pfd) {
            super(pfd.getFileDescriptor());
            this.mPfd = pfd;
        }

        @Override // java.io.FileInputStream, java.io.Closeable, java.lang.AutoCloseable, java.io.InputStream
        public void close() throws IOException {
            try {
                super.close();
            } finally {
                this.mPfd.close();
            }
        }

        @Override // java.io.FileInputStream, java.io.InputStream
        public int read() throws IOException {
            int result = super.read();
            if (result == -1 && this.mPfd.canDetectErrors()) {
                this.mPfd.checkError();
            }
            return result;
        }

        @Override // java.io.FileInputStream, java.io.InputStream
        public int read(byte[] b) throws IOException {
            int result = super.read(b);
            if (result == -1 && this.mPfd.canDetectErrors()) {
                this.mPfd.checkError();
            }
            return result;
        }

        @Override // java.io.FileInputStream, java.io.InputStream
        public int read(byte[] b, int off, int len) throws IOException {
            int result = super.read(b, off, len);
            if (result == -1 && this.mPfd.canDetectErrors()) {
                this.mPfd.checkError();
            }
            return result;
        }
    }

    public static class AutoCloseOutputStream extends FileOutputStream {
        private final ParcelFileDescriptor mPfd;

        public AutoCloseOutputStream(ParcelFileDescriptor pfd) {
            super(pfd.getFileDescriptor());
            this.mPfd = pfd;
        }

        @Override // java.io.FileOutputStream, java.io.OutputStream, java.io.Closeable, java.lang.AutoCloseable
        public void close() throws IOException {
            try {
                super.close();
            } finally {
                this.mPfd.close();
            }
        }
    }

    @Override // java.lang.Object
    public String toString() {
        ParcelFileDescriptor parcelFileDescriptor = this.mWrapped;
        if (parcelFileDescriptor != null) {
            return parcelFileDescriptor.toString();
        }
        return "{ParcelFileDescriptor: " + this.mFd + "}";
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public void finalize() throws Throwable {
        if (this.mWrapped != null) {
            releaseResources();
        }
        CloseGuard closeGuard = this.mGuard;
        if (closeGuard != null) {
            closeGuard.warnIfOpen();
        }
        try {
            if (!this.mClosed) {
                closeWithStatus(3, null);
            }
        } finally {
            super.finalize();
        }
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        ParcelFileDescriptor parcelFileDescriptor = this.mWrapped;
        if (parcelFileDescriptor != null) {
            return parcelFileDescriptor.describeContents();
        }
        return 1;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        ParcelFileDescriptor parcelFileDescriptor = this.mWrapped;
        if (parcelFileDescriptor != null) {
            try {
                parcelFileDescriptor.writeToParcel(out, flags);
            } finally {
                releaseResources();
            }
        } else {
            if (this.mCommFd != null) {
                out.writeInt(1);
                out.writeFileDescriptor(this.mFd);
                out.writeFileDescriptor(this.mCommFd);
            } else {
                out.writeInt(0);
                out.writeFileDescriptor(this.mFd);
            }
            if ((flags & 1) != 0 && !this.mClosed) {
                closeWithStatus(-1, null);
            }
        }
    }

    public static class FileDescriptorDetachedException extends IOException {
        private static final long serialVersionUID = 955542466045L;

        public FileDescriptorDetachedException() {
            super("Remote side is detached");
        }
    }

    /* access modifiers changed from: private */
    public static class Status {
        public static final int DEAD = -2;
        public static final int DETACHED = 2;
        public static final int ERROR = 1;
        public static final int LEAKED = 3;
        public static final int OK = 0;
        public static final int SILENCE = -1;
        public final String msg;
        public final int status;

        public Status(int status2) {
            this(status2, null);
        }

        public Status(int status2, String msg2) {
            this.status = status2;
            this.msg = msg2;
        }

        public IOException asIOException() {
            int i = this.status;
            if (i == -2) {
                return new IOException("Remote side is dead");
            }
            if (i == 0) {
                return null;
            }
            if (i == 1) {
                return new IOException("Remote error: " + this.msg);
            } else if (i == 2) {
                return new FileDescriptorDetachedException();
            } else {
                if (i == 3) {
                    return new IOException("Remote side was leaked");
                }
                return new IOException("Unknown status: " + this.status);
            }
        }

        public String toString() {
            return "{" + this.status + ": " + this.msg + "}";
        }
    }

    private static boolean isAtLeastQ() {
        return VMRuntime.getRuntime().getTargetSdkVersion() >= 29;
    }

    private static int ifAtLeastQ(int value) {
        if (isAtLeastQ()) {
            return value;
        }
        return 0;
    }
}
