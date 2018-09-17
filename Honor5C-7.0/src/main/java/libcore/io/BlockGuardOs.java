package libcore.io;

import android.system.ErrnoException;
import android.system.OsConstants;
import android.system.StructLinger;
import android.system.StructPollfd;
import android.system.StructStat;
import android.system.StructStatVfs;
import android.util.MutableLong;
import dalvik.system.BlockGuard;
import dalvik.system.SocketTagger;
import java.io.FileDescriptor;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;

public class BlockGuardOs extends ForwardingOs {
    public BlockGuardOs(Os os) {
        super(os);
    }

    private FileDescriptor tagSocket(FileDescriptor fd) throws ErrnoException {
        try {
            SocketTagger.get().tag(fd);
            return fd;
        } catch (SocketException e) {
            throw new ErrnoException("socket", OsConstants.EINVAL, e);
        }
    }

    private void untagSocket(FileDescriptor fd) throws ErrnoException {
        try {
            SocketTagger.get().untag(fd);
        } catch (SocketException e) {
            throw new ErrnoException("socket", OsConstants.EINVAL, e);
        }
    }

    public FileDescriptor accept(FileDescriptor fd, SocketAddress peerAddress) throws ErrnoException, SocketException {
        BlockGuard.getThreadPolicy().onNetwork();
        return tagSocket(this.os.accept(fd, peerAddress));
    }

    public boolean access(String path, int mode) throws ErrnoException {
        BlockGuard.getThreadPolicy().onReadFromDisk();
        return this.os.access(path, mode);
    }

    public void chmod(String path, int mode) throws ErrnoException {
        BlockGuard.getThreadPolicy().onWriteToDisk();
        this.os.chmod(path, mode);
    }

    public void chown(String path, int uid, int gid) throws ErrnoException {
        BlockGuard.getThreadPolicy().onWriteToDisk();
        this.os.chown(path, uid, gid);
    }

    public void close(FileDescriptor fd) throws ErrnoException {
        try {
            if (fd.isSocket$()) {
                if (isLingerSocket(fd)) {
                    BlockGuard.getThreadPolicy().onNetwork();
                }
                untagSocket(fd);
            }
        } catch (ErrnoException e) {
        }
        this.os.close(fd);
    }

    private static boolean isLingerSocket(FileDescriptor fd) throws ErrnoException {
        StructLinger linger = Libcore.os.getsockoptLinger(fd, OsConstants.SOL_SOCKET, OsConstants.SO_LINGER);
        if (!linger.isOn() || linger.l_linger <= 0) {
            return false;
        }
        return true;
    }

    public void connect(FileDescriptor fd, InetAddress address, int port) throws ErrnoException, SocketException {
        BlockGuard.getThreadPolicy().onNetwork();
        this.os.connect(fd, address, port);
    }

    public void fchmod(FileDescriptor fd, int mode) throws ErrnoException {
        BlockGuard.getThreadPolicy().onWriteToDisk();
        this.os.fchmod(fd, mode);
    }

    public void fchown(FileDescriptor fd, int uid, int gid) throws ErrnoException {
        BlockGuard.getThreadPolicy().onWriteToDisk();
        this.os.fchown(fd, uid, gid);
    }

    public void fdatasync(FileDescriptor fd) throws ErrnoException {
        BlockGuard.getThreadPolicy().onWriteToDisk();
        this.os.fdatasync(fd);
    }

    public StructStat fstat(FileDescriptor fd) throws ErrnoException {
        BlockGuard.getThreadPolicy().onReadFromDisk();
        return this.os.fstat(fd);
    }

    public StructStatVfs fstatvfs(FileDescriptor fd) throws ErrnoException {
        BlockGuard.getThreadPolicy().onReadFromDisk();
        return this.os.fstatvfs(fd);
    }

    public void fsync(FileDescriptor fd) throws ErrnoException {
        BlockGuard.getThreadPolicy().onWriteToDisk();
        this.os.fsync(fd);
    }

    public void ftruncate(FileDescriptor fd, long length) throws ErrnoException {
        BlockGuard.getThreadPolicy().onWriteToDisk();
        this.os.ftruncate(fd, length);
    }

    public void lchown(String path, int uid, int gid) throws ErrnoException {
        BlockGuard.getThreadPolicy().onWriteToDisk();
        this.os.lchown(path, uid, gid);
    }

    public void link(String oldPath, String newPath) throws ErrnoException {
        BlockGuard.getThreadPolicy().onWriteToDisk();
        this.os.link(oldPath, newPath);
    }

    public long lseek(FileDescriptor fd, long offset, int whence) throws ErrnoException {
        BlockGuard.getThreadPolicy().onReadFromDisk();
        return this.os.lseek(fd, offset, whence);
    }

    public StructStat lstat(String path) throws ErrnoException {
        BlockGuard.getThreadPolicy().onReadFromDisk();
        return this.os.lstat(path);
    }

    public void mkdir(String path, int mode) throws ErrnoException {
        BlockGuard.getThreadPolicy().onWriteToDisk();
        this.os.mkdir(path, mode);
    }

    public void mkfifo(String path, int mode) throws ErrnoException {
        BlockGuard.getThreadPolicy().onWriteToDisk();
        this.os.mkfifo(path, mode);
    }

    public FileDescriptor open(String path, int flags, int mode) throws ErrnoException {
        BlockGuard.getThreadPolicy().onReadFromDisk();
        if ((OsConstants.O_ACCMODE & mode) != OsConstants.O_RDONLY) {
            BlockGuard.getThreadPolicy().onWriteToDisk();
        }
        return this.os.open(path, flags, mode);
    }

    public int poll(StructPollfd[] fds, int timeoutMs) throws ErrnoException {
        if (timeoutMs != 0) {
            BlockGuard.getThreadPolicy().onNetwork();
        }
        return this.os.poll(fds, timeoutMs);
    }

    public void posix_fallocate(FileDescriptor fd, long offset, long length) throws ErrnoException {
        BlockGuard.getThreadPolicy().onWriteToDisk();
        this.os.posix_fallocate(fd, offset, length);
    }

    public int pread(FileDescriptor fd, ByteBuffer buffer, long offset) throws ErrnoException, InterruptedIOException {
        BlockGuard.getThreadPolicy().onReadFromDisk();
        return this.os.pread(fd, buffer, offset);
    }

    public int pread(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount, long offset) throws ErrnoException, InterruptedIOException {
        BlockGuard.getThreadPolicy().onReadFromDisk();
        return this.os.pread(fd, bytes, byteOffset, byteCount, offset);
    }

    public int pwrite(FileDescriptor fd, ByteBuffer buffer, long offset) throws ErrnoException, InterruptedIOException {
        BlockGuard.getThreadPolicy().onWriteToDisk();
        return this.os.pwrite(fd, buffer, offset);
    }

    public int pwrite(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount, long offset) throws ErrnoException, InterruptedIOException {
        BlockGuard.getThreadPolicy().onWriteToDisk();
        return this.os.pwrite(fd, bytes, byteOffset, byteCount, offset);
    }

    public int read(FileDescriptor fd, ByteBuffer buffer) throws ErrnoException, InterruptedIOException {
        BlockGuard.getThreadPolicy().onReadFromDisk();
        return this.os.read(fd, buffer);
    }

    public int read(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount) throws ErrnoException, InterruptedIOException {
        BlockGuard.getThreadPolicy().onReadFromDisk();
        return this.os.read(fd, bytes, byteOffset, byteCount);
    }

    public String readlink(String path) throws ErrnoException {
        BlockGuard.getThreadPolicy().onReadFromDisk();
        return this.os.readlink(path);
    }

    public String realpath(String path) throws ErrnoException {
        BlockGuard.getThreadPolicy().onReadFromDisk();
        return this.os.realpath(path);
    }

    public int readv(FileDescriptor fd, Object[] buffers, int[] offsets, int[] byteCounts) throws ErrnoException, InterruptedIOException {
        BlockGuard.getThreadPolicy().onReadFromDisk();
        return this.os.readv(fd, buffers, offsets, byteCounts);
    }

    public int recvfrom(FileDescriptor fd, ByteBuffer buffer, int flags, InetSocketAddress srcAddress) throws ErrnoException, SocketException {
        BlockGuard.getThreadPolicy().onNetwork();
        return this.os.recvfrom(fd, buffer, flags, srcAddress);
    }

    public int recvfrom(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount, int flags, InetSocketAddress srcAddress) throws ErrnoException, SocketException {
        BlockGuard.getThreadPolicy().onNetwork();
        return this.os.recvfrom(fd, bytes, byteOffset, byteCount, flags, srcAddress);
    }

    public void remove(String path) throws ErrnoException {
        BlockGuard.getThreadPolicy().onWriteToDisk();
        this.os.remove(path);
    }

    public void rename(String oldPath, String newPath) throws ErrnoException {
        BlockGuard.getThreadPolicy().onWriteToDisk();
        this.os.rename(oldPath, newPath);
    }

    public long sendfile(FileDescriptor outFd, FileDescriptor inFd, MutableLong inOffset, long byteCount) throws ErrnoException {
        BlockGuard.getThreadPolicy().onWriteToDisk();
        return this.os.sendfile(outFd, inFd, inOffset, byteCount);
    }

    public int sendto(FileDescriptor fd, ByteBuffer buffer, int flags, InetAddress inetAddress, int port) throws ErrnoException, SocketException {
        BlockGuard.getThreadPolicy().onNetwork();
        return this.os.sendto(fd, buffer, flags, inetAddress, port);
    }

    public int sendto(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount, int flags, InetAddress inetAddress, int port) throws ErrnoException, SocketException {
        if (inetAddress != null) {
            BlockGuard.getThreadPolicy().onNetwork();
        }
        return this.os.sendto(fd, bytes, byteOffset, byteCount, flags, inetAddress, port);
    }

    public FileDescriptor socket(int domain, int type, int protocol) throws ErrnoException {
        return tagSocket(this.os.socket(domain, type, protocol));
    }

    public void socketpair(int domain, int type, int protocol, FileDescriptor fd1, FileDescriptor fd2) throws ErrnoException {
        this.os.socketpair(domain, type, protocol, fd1, fd2);
        tagSocket(fd1);
        tagSocket(fd2);
    }

    public StructStat stat(String path) throws ErrnoException {
        BlockGuard.getThreadPolicy().onReadFromDisk();
        return this.os.stat(path);
    }

    public StructStatVfs statvfs(String path) throws ErrnoException {
        BlockGuard.getThreadPolicy().onReadFromDisk();
        return this.os.statvfs(path);
    }

    public void symlink(String oldPath, String newPath) throws ErrnoException {
        BlockGuard.getThreadPolicy().onWriteToDisk();
        this.os.symlink(oldPath, newPath);
    }

    public int write(FileDescriptor fd, ByteBuffer buffer) throws ErrnoException, InterruptedIOException {
        BlockGuard.getThreadPolicy().onWriteToDisk();
        return this.os.write(fd, buffer);
    }

    public int write(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount) throws ErrnoException, InterruptedIOException {
        BlockGuard.getThreadPolicy().onWriteToDisk();
        return this.os.write(fd, bytes, byteOffset, byteCount);
    }

    public int writev(FileDescriptor fd, Object[] buffers, int[] offsets, int[] byteCounts) throws ErrnoException, InterruptedIOException {
        BlockGuard.getThreadPolicy().onWriteToDisk();
        return this.os.writev(fd, buffers, offsets, byteCounts);
    }
}
