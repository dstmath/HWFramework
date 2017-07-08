package android.os;

import android.app.backup.FullBackup;
import android.os.MessageQueue.OnFileDescriptorEventListener;
import android.os.Parcelable.Creator;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.system.StructStat;
import android.util.Log;
import dalvik.system.CloseGuard;
import java.io.Closeable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramSocket;
import java.net.Socket;
import java.nio.ByteOrder;
import libcore.io.IoUtils;
import libcore.io.Memory;

public class ParcelFileDescriptor implements Parcelable, Closeable {
    public static final Creator<ParcelFileDescriptor> CREATOR = null;
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

    public static class AutoCloseInputStream extends FileInputStream {
        private final ParcelFileDescriptor mPfd;

        public AutoCloseInputStream(ParcelFileDescriptor pfd) {
            super(pfd.getFileDescriptor());
            this.mPfd = pfd;
        }

        public void close() throws IOException {
            try {
                this.mPfd.close();
            } finally {
                super.close();
            }
        }

        public int read() throws IOException {
            int result = super.read();
            if (result == -1 && this.mPfd.canDetectErrors()) {
                this.mPfd.checkError();
            }
            return result;
        }

        public int read(byte[] b) throws IOException {
            int result = super.read(b);
            if (result == -1 && this.mPfd.canDetectErrors()) {
                this.mPfd.checkError();
            }
            return result;
        }

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

        public void close() throws IOException {
            try {
                this.mPfd.close();
            } finally {
                super.close();
            }
        }
    }

    /* renamed from: android.os.ParcelFileDescriptor.2 */
    static class AnonymousClass2 implements OnFileDescriptorEventListener {
        final /* synthetic */ OnCloseListener val$listener;
        final /* synthetic */ MessageQueue val$queue;

        AnonymousClass2(MessageQueue val$queue, OnCloseListener val$listener) {
            this.val$queue = val$queue;
            this.val$listener = val$listener;
        }

        public int onFileDescriptorEvents(FileDescriptor fd, int events) {
            Status status = null;
            if ((events & ParcelFileDescriptor.MODE_WORLD_READABLE) != 0) {
                status = ParcelFileDescriptor.readCommStatus(fd, new byte[ParcelFileDescriptor.MAX_STATUS]);
            } else if ((events & 4) != 0) {
                status = new Status(-2);
            }
            if (status == null) {
                return ParcelFileDescriptor.MODE_WORLD_READABLE;
            }
            this.val$queue.removeOnFileDescriptorEventListener(fd);
            IoUtils.closeQuietly(fd);
            this.val$listener.onClose(status.asIOException());
            return 0;
        }
    }

    public static class FileDescriptorDetachedException extends IOException {
        private static final long serialVersionUID = 955542466045L;

        public FileDescriptorDetachedException() {
            super("Remote side is detached");
        }
    }

    public interface OnCloseListener {
        void onClose(IOException iOException);
    }

    private static class Status {
        public static final int DEAD = -2;
        public static final int DETACHED = 2;
        public static final int ERROR = 1;
        public static final int LEAKED = 3;
        public static final int OK = 0;
        public static final int SILENCE = -1;
        public final String msg;
        public final int status;

        public Status(int status) {
            this(status, null);
        }

        public Status(int status, String msg) {
            this.status = status;
            this.msg = msg;
        }

        public IOException asIOException() {
            switch (this.status) {
                case DEAD /*-2*/:
                    return new IOException("Remote side is dead");
                case OK /*0*/:
                    return null;
                case ERROR /*1*/:
                    return new IOException("Remote error: " + this.msg);
                case DETACHED /*2*/:
                    return new FileDescriptorDetachedException();
                case LEAKED /*3*/:
                    return new IOException("Remote side was leaked");
                default:
                    return new IOException("Unknown status: " + this.status);
            }
        }

        public String toString() {
            return "{" + this.status + ": " + this.msg + "}";
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.os.ParcelFileDescriptor.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.os.ParcelFileDescriptor.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.os.ParcelFileDescriptor.<clinit>():void");
    }

    public ParcelFileDescriptor(ParcelFileDescriptor wrapped) {
        this.mGuard = CloseGuard.get();
        this.mWrapped = wrapped;
        this.mFd = null;
        this.mCommFd = null;
        this.mClosed = true;
    }

    public ParcelFileDescriptor(FileDescriptor fd) {
        this(fd, null);
    }

    public ParcelFileDescriptor(FileDescriptor fd, FileDescriptor commChannel) {
        this.mGuard = CloseGuard.get();
        if (fd == null) {
            throw new NullPointerException("FileDescriptor must not be null");
        }
        this.mWrapped = null;
        this.mFd = fd;
        this.mCommFd = commChannel;
        this.mGuard.open("close");
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
        } else if (listener == null) {
            throw new IllegalArgumentException("Listener must not be null");
        } else {
            FileDescriptor fd = openInternal(file, mode);
            if (fd == null) {
                return null;
            }
            return fromFd(fd, handler, listener);
        }
    }

    public static ParcelFileDescriptor fromFd(FileDescriptor fd, Handler handler, OnCloseListener listener) throws IOException {
        if (handler == null) {
            throw new IllegalArgumentException("Handler must not be null");
        } else if (listener == null) {
            throw new IllegalArgumentException("Listener must not be null");
        } else {
            FileDescriptor[] comm = createCommSocketPair();
            ParcelFileDescriptor pfd = new ParcelFileDescriptor(fd, comm[0]);
            MessageQueue queue = handler.getLooper().getQueue();
            queue.addOnFileDescriptorEventListener(comm[MODE_WORLD_READABLE], MODE_WORLD_READABLE, new AnonymousClass2(queue, listener));
            return pfd;
        }
    }

    private static FileDescriptor openInternal(File file, int mode) throws FileNotFoundException {
        if ((MODE_READ_WRITE & mode) != 0) {
            return Parcel.openFileDescriptor(file.getPath(), mode);
        }
        throw new IllegalArgumentException("Must specify MODE_READ_ONLY, MODE_WRITE_ONLY, or MODE_READ_WRITE");
    }

    public static ParcelFileDescriptor dup(FileDescriptor orig) throws IOException {
        try {
            return new ParcelFileDescriptor(Os.dup(orig));
        } catch (ErrnoException e) {
            throw e.rethrowAsIOException();
        }
    }

    public ParcelFileDescriptor dup() throws IOException {
        if (this.mWrapped != null) {
            return this.mWrapped.dup();
        }
        return dup(getFileDescriptor());
    }

    public static ParcelFileDescriptor fromFd(int fd) throws IOException {
        FileDescriptor original = new FileDescriptor();
        original.setInt$(fd);
        try {
            return new ParcelFileDescriptor(Os.dup(original));
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
        if (fd != null) {
            return new ParcelFileDescriptor(fd);
        }
        return null;
    }

    public static ParcelFileDescriptor fromDatagramSocket(DatagramSocket datagramSocket) {
        FileDescriptor fd = datagramSocket.getFileDescriptor$();
        if (fd != null) {
            return new ParcelFileDescriptor(fd);
        }
        return null;
    }

    public static ParcelFileDescriptor[] createPipe() throws IOException {
        try {
            FileDescriptor[] fds = Os.pipe();
            ParcelFileDescriptor[] parcelFileDescriptorArr = new ParcelFileDescriptor[MODE_WORLD_WRITEABLE];
            parcelFileDescriptorArr[0] = new ParcelFileDescriptor(fds[0]);
            parcelFileDescriptorArr[MODE_WORLD_READABLE] = new ParcelFileDescriptor(fds[MODE_WORLD_READABLE]);
            return parcelFileDescriptorArr;
        } catch (ErrnoException e) {
            throw e.rethrowAsIOException();
        }
    }

    public static ParcelFileDescriptor[] createReliablePipe() throws IOException {
        try {
            FileDescriptor[] comm = createCommSocketPair();
            FileDescriptor[] fds = Os.pipe();
            ParcelFileDescriptor[] parcelFileDescriptorArr = new ParcelFileDescriptor[MODE_WORLD_WRITEABLE];
            parcelFileDescriptorArr[0] = new ParcelFileDescriptor(fds[0], comm[0]);
            parcelFileDescriptorArr[MODE_WORLD_READABLE] = new ParcelFileDescriptor(fds[MODE_WORLD_READABLE], comm[MODE_WORLD_READABLE]);
            return parcelFileDescriptorArr;
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
            Os.socketpair(OsConstants.AF_UNIX, type, 0, fd0, fd1);
            ParcelFileDescriptor[] parcelFileDescriptorArr = new ParcelFileDescriptor[MODE_WORLD_WRITEABLE];
            parcelFileDescriptorArr[0] = new ParcelFileDescriptor(fd0);
            parcelFileDescriptorArr[MODE_WORLD_READABLE] = new ParcelFileDescriptor(fd1);
            return parcelFileDescriptorArr;
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
            Os.socketpair(OsConstants.AF_UNIX, type, 0, fd0, fd1);
            ParcelFileDescriptor[] parcelFileDescriptorArr = new ParcelFileDescriptor[MODE_WORLD_WRITEABLE];
            parcelFileDescriptorArr[0] = new ParcelFileDescriptor(fd0, comm[0]);
            parcelFileDescriptorArr[MODE_WORLD_READABLE] = new ParcelFileDescriptor(fd1, comm[MODE_WORLD_READABLE]);
            return parcelFileDescriptorArr;
        } catch (ErrnoException e) {
            throw e.rethrowAsIOException();
        }
    }

    private static FileDescriptor[] createCommSocketPair() throws IOException {
        try {
            FileDescriptor comm1 = new FileDescriptor();
            FileDescriptor comm2 = new FileDescriptor();
            Os.socketpair(OsConstants.AF_UNIX, OsConstants.SOCK_SEQPACKET, 0, comm1, comm2);
            IoUtils.setBlocking(comm1, false);
            IoUtils.setBlocking(comm2, false);
            FileDescriptor[] fileDescriptorArr = new FileDescriptor[MODE_WORLD_WRITEABLE];
            fileDescriptorArr[0] = comm1;
            fileDescriptorArr[MODE_WORLD_READABLE] = comm2;
            return fileDescriptorArr;
        } catch (ErrnoException e) {
            throw e.rethrowAsIOException();
        }
    }

    @Deprecated
    public static ParcelFileDescriptor fromData(byte[] data, String name) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor = null;
        if (data == null) {
            return null;
        }
        MemoryFile file = new MemoryFile(name, data.length);
        if (data.length > 0) {
            file.writeBytes(data, 0, 0, data.length);
        }
        file.deactivate();
        FileDescriptor fd = file.getFileDescriptor();
        if (fd != null) {
            parcelFileDescriptor = new ParcelFileDescriptor(fd);
        }
        return parcelFileDescriptor;
    }

    public static int parseMode(String mode) {
        if (FullBackup.ROOT_TREE_TOKEN.equals(mode)) {
            return MODE_READ_ONLY;
        }
        if ("w".equals(mode) || "wt".equals(mode)) {
            return 738197504;
        }
        if ("wa".equals(mode)) {
            return 704643072;
        }
        if ("rw".equals(mode)) {
            return 939524096;
        }
        if ("rwt".equals(mode)) {
            return 1006632960;
        }
        throw new IllegalArgumentException("Bad mode '" + mode + "'");
    }

    public FileDescriptor getFileDescriptor() {
        if (this.mWrapped != null) {
            return this.mWrapped.getFileDescriptor();
        }
        return this.mFd;
    }

    public long getStatSize() {
        if (this.mWrapped != null) {
            return this.mWrapped.getStatSize();
        }
        try {
            StructStat st = Os.fstat(this.mFd);
            if (OsConstants.S_ISREG(st.st_mode) || OsConstants.S_ISLNK(st.st_mode)) {
                return st.st_size;
            }
            return -1;
        } catch (ErrnoException e) {
            Log.w(TAG, "fstat() failed: " + e);
            return -1;
        }
    }

    public long seekTo(long pos) throws IOException {
        if (this.mWrapped != null) {
            return this.mWrapped.seekTo(pos);
        }
        try {
            return Os.lseek(this.mFd, pos, OsConstants.SEEK_SET);
        } catch (ErrnoException e) {
            throw e.rethrowAsIOException();
        }
    }

    public int getFd() {
        if (this.mWrapped != null) {
            return this.mWrapped.getFd();
        }
        if (!this.mClosed) {
            return this.mFd.getInt$();
        }
        throw new IllegalStateException("Already closed");
    }

    public int detachFd() {
        if (this.mWrapped != null) {
            return this.mWrapped.detachFd();
        }
        if (this.mClosed) {
            throw new IllegalStateException("Already closed");
        }
        int fd = getFd();
        Parcel.clearFileDescriptor(this.mFd);
        writeCommStatusAndClose(MODE_WORLD_WRITEABLE, null);
        this.mClosed = true;
        this.mGuard.close();
        releaseResources();
        return fd;
    }

    public void close() throws IOException {
        if (this.mWrapped != null) {
            try {
                this.mWrapped.close();
            } finally {
                releaseResources();
            }
        } else {
            closeWithStatus(0, null);
        }
    }

    public void closeWithError(String msg) throws IOException {
        if (this.mWrapped != null) {
            try {
                this.mWrapped.closeWithError(msg);
            } finally {
                releaseResources();
            }
        } else if (msg == null) {
            throw new IllegalArgumentException("Message must not be null");
        } else {
            closeWithStatus(MODE_WORLD_READABLE, msg);
        }
    }

    private void closeWithStatus(int status, String msg) {
        if (!this.mClosed) {
            this.mClosed = true;
            this.mGuard.close();
            writeCommStatusAndClose(status, msg);
            IoUtils.closeQuietly(this.mFd);
            releaseResources();
        }
    }

    public void releaseResources() {
    }

    private byte[] getOrCreateStatusBuffer() {
        if (this.mStatusBuf == null) {
            this.mStatusBuf = new byte[MAX_STATUS];
        }
        return this.mStatusBuf;
    }

    private void writeCommStatusAndClose(int status, String msg) {
        if (this.mCommFd == null) {
            if (msg != null) {
                Log.w(TAG, "Unable to inform peer: " + msg);
            }
            return;
        }
        if (status == MODE_WORLD_WRITEABLE) {
            Log.w(TAG, "Peer expected signal when closed; unable to deliver after detach");
        }
        if (status == -1) {
            IoUtils.closeQuietly(this.mCommFd);
            this.mCommFd = null;
            return;
        }
        try {
            this.mStatus = readCommStatus(this.mCommFd, getOrCreateStatusBuffer());
            if (this.mStatus != null) {
                IoUtils.closeQuietly(this.mCommFd);
                this.mCommFd = null;
                return;
            }
            byte[] buf = getOrCreateStatusBuffer();
            Memory.pokeInt(buf, 0, status, ByteOrder.BIG_ENDIAN);
            int writePtr = 4;
            if (msg != null) {
                byte[] rawMsg = msg.getBytes();
                int len = Math.min(rawMsg.length, buf.length - 4);
                System.arraycopy(rawMsg, 0, buf, 4, len);
                writePtr = len + 4;
            }
            Os.write(this.mCommFd, buf, 0, writePtr);
            IoUtils.closeQuietly(this.mCommFd);
            this.mCommFd = null;
        } catch (ErrnoException e) {
            Log.w(TAG, "Failed to report status: " + e);
        } catch (InterruptedIOException e2) {
            Log.w(TAG, "Failed to report status: " + e2);
        } catch (Throwable th) {
            IoUtils.closeQuietly(this.mCommFd);
            this.mCommFd = null;
        }
    }

    private static Status readCommStatus(FileDescriptor comm, byte[] buf) {
        try {
            int n = Os.read(comm, buf, 0, buf.length);
            if (n == 0) {
                return new Status(-2);
            }
            int status = Memory.peekInt(buf, 0, ByteOrder.BIG_ENDIAN);
            if (status == MODE_WORLD_READABLE) {
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
        if (this.mWrapped != null) {
            return this.mWrapped.canDetectErrors();
        }
        return this.mCommFd != null;
    }

    public void checkError() throws IOException {
        if (this.mWrapped != null) {
            this.mWrapped.checkError();
            return;
        }
        if (this.mStatus == null) {
            if (this.mCommFd == null) {
                Log.w(TAG, "Peer didn't provide a comm channel; unable to check for errors");
                return;
            }
            this.mStatus = readCommStatus(this.mCommFd, getOrCreateStatusBuffer());
        }
        if (this.mStatus != null && this.mStatus.status != 0) {
            throw this.mStatus.asIOException();
        }
    }

    public String toString() {
        if (this.mWrapped != null) {
            return this.mWrapped.toString();
        }
        return "{ParcelFileDescriptor: " + this.mFd + "}";
    }

    protected void finalize() throws Throwable {
        if (this.mWrapped != null) {
            releaseResources();
        }
        if (this.mGuard != null) {
            this.mGuard.warnIfOpen();
        }
        try {
            if (!this.mClosed) {
                closeWithStatus(3, null);
            }
            super.finalize();
        } catch (Throwable th) {
            super.finalize();
        }
    }

    public int describeContents() {
        if (this.mWrapped != null) {
            return this.mWrapped.describeContents();
        }
        return MODE_WORLD_READABLE;
    }

    public void writeToParcel(Parcel out, int flags) {
        if (this.mWrapped != null) {
            try {
                this.mWrapped.writeToParcel(out, flags);
            } finally {
                releaseResources();
            }
        } else {
            if (this.mCommFd != null) {
                out.writeInt(MODE_WORLD_READABLE);
                out.writeFileDescriptor(this.mFd);
                out.writeFileDescriptor(this.mCommFd);
            } else {
                out.writeInt(0);
                out.writeFileDescriptor(this.mFd);
            }
            if ((flags & MODE_WORLD_READABLE) != 0 && !this.mClosed) {
                closeWithStatus(-1, null);
            }
        }
    }
}
