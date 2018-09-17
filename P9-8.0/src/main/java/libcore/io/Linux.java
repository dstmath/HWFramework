package libcore.io;

import android.system.ErrnoException;
import android.system.GaiException;
import android.system.StructAddrinfo;
import android.system.StructCapUserData;
import android.system.StructCapUserHeader;
import android.system.StructFlock;
import android.system.StructGroupReq;
import android.system.StructGroupSourceReq;
import android.system.StructIfaddrs;
import android.system.StructLinger;
import android.system.StructPasswd;
import android.system.StructPollfd;
import android.system.StructStat;
import android.system.StructStatVfs;
import android.system.StructTimeval;
import android.system.StructUcred;
import android.system.StructUtsname;
import android.util.MutableInt;
import android.util.MutableLong;
import dalvik.bytecode.Opcodes;
import java.io.FileDescriptor;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.NioUtils;

public final class Linux implements Os {
    private native int preadBytes(FileDescriptor fileDescriptor, Object obj, int i, int i2, long j) throws ErrnoException, InterruptedIOException;

    private native int pwriteBytes(FileDescriptor fileDescriptor, Object obj, int i, int i2, long j) throws ErrnoException, InterruptedIOException;

    private native int readBytes(FileDescriptor fileDescriptor, Object obj, int i, int i2) throws ErrnoException, InterruptedIOException;

    private native int recvfromBytes(FileDescriptor fileDescriptor, Object obj, int i, int i2, int i3, InetSocketAddress inetSocketAddress) throws ErrnoException, SocketException;

    private native int sendtoBytes(FileDescriptor fileDescriptor, Object obj, int i, int i2, int i3, InetAddress inetAddress, int i4) throws ErrnoException, SocketException;

    private native int sendtoBytes(FileDescriptor fileDescriptor, Object obj, int i, int i2, int i3, SocketAddress socketAddress) throws ErrnoException, SocketException;

    private native int umaskImpl(int i);

    private native int writeBytes(FileDescriptor fileDescriptor, Object obj, int i, int i2) throws ErrnoException, InterruptedIOException;

    public native FileDescriptor accept(FileDescriptor fileDescriptor, SocketAddress socketAddress) throws ErrnoException, SocketException;

    public native boolean access(String str, int i) throws ErrnoException;

    public native InetAddress[] android_getaddrinfo(String str, StructAddrinfo structAddrinfo, int i) throws GaiException;

    public native void bind(FileDescriptor fileDescriptor, InetAddress inetAddress, int i) throws ErrnoException, SocketException;

    public native void bind(FileDescriptor fileDescriptor, SocketAddress socketAddress) throws ErrnoException, SocketException;

    public native StructCapUserData[] capget(StructCapUserHeader structCapUserHeader) throws ErrnoException;

    public native void capset(StructCapUserHeader structCapUserHeader, StructCapUserData[] structCapUserDataArr) throws ErrnoException;

    public native void chmod(String str, int i) throws ErrnoException;

    public native void chown(String str, int i, int i2) throws ErrnoException;

    public native void close(FileDescriptor fileDescriptor) throws ErrnoException;

    public native void connect(FileDescriptor fileDescriptor, InetAddress inetAddress, int i) throws ErrnoException, SocketException;

    public native void connect(FileDescriptor fileDescriptor, SocketAddress socketAddress) throws ErrnoException, SocketException;

    public native FileDescriptor dup(FileDescriptor fileDescriptor) throws ErrnoException;

    public native FileDescriptor dup2(FileDescriptor fileDescriptor, int i) throws ErrnoException;

    public native String[] environ();

    public native void execv(String str, String[] strArr) throws ErrnoException;

    public native void execve(String str, String[] strArr, String[] strArr2) throws ErrnoException;

    public native void fchmod(FileDescriptor fileDescriptor, int i) throws ErrnoException;

    public native void fchown(FileDescriptor fileDescriptor, int i, int i2) throws ErrnoException;

    public native int fcntlFlock(FileDescriptor fileDescriptor, int i, StructFlock structFlock) throws ErrnoException, InterruptedIOException;

    public native int fcntlInt(FileDescriptor fileDescriptor, int i, int i2) throws ErrnoException;

    public native int fcntlVoid(FileDescriptor fileDescriptor, int i) throws ErrnoException;

    public native void fdatasync(FileDescriptor fileDescriptor) throws ErrnoException;

    public native StructStat fstat(FileDescriptor fileDescriptor) throws ErrnoException;

    public native StructStatVfs fstatvfs(FileDescriptor fileDescriptor) throws ErrnoException;

    public native void fsync(FileDescriptor fileDescriptor) throws ErrnoException;

    public native void ftruncate(FileDescriptor fileDescriptor, long j) throws ErrnoException;

    public native String gai_strerror(int i);

    public native int getegid();

    public native String getenv(String str);

    public native int geteuid();

    public native int getgid();

    public native StructIfaddrs[] getifaddrs() throws ErrnoException;

    public native String getnameinfo(InetAddress inetAddress, int i) throws GaiException;

    public native SocketAddress getpeername(FileDescriptor fileDescriptor) throws ErrnoException;

    public native int getpgid(int i);

    public native int getpid();

    public native int getppid();

    public native StructPasswd getpwnam(String str) throws ErrnoException;

    public native StructPasswd getpwuid(int i) throws ErrnoException;

    public native SocketAddress getsockname(FileDescriptor fileDescriptor) throws ErrnoException;

    public native int getsockoptByte(FileDescriptor fileDescriptor, int i, int i2) throws ErrnoException;

    public native InetAddress getsockoptInAddr(FileDescriptor fileDescriptor, int i, int i2) throws ErrnoException;

    public native int getsockoptInt(FileDescriptor fileDescriptor, int i, int i2) throws ErrnoException;

    public native StructLinger getsockoptLinger(FileDescriptor fileDescriptor, int i, int i2) throws ErrnoException;

    public native StructTimeval getsockoptTimeval(FileDescriptor fileDescriptor, int i, int i2) throws ErrnoException;

    public native StructUcred getsockoptUcred(FileDescriptor fileDescriptor, int i, int i2) throws ErrnoException;

    public native int gettid();

    public native int getuid();

    public native byte[] getxattr(String str, String str2) throws ErrnoException;

    public native String if_indextoname(int i);

    public native int if_nametoindex(String str);

    public native InetAddress inet_pton(int i, String str);

    public native int ioctlFlags(FileDescriptor fileDescriptor, String str) throws ErrnoException;

    public native InetAddress ioctlInetAddress(FileDescriptor fileDescriptor, int i, String str) throws ErrnoException;

    public native int ioctlInt(FileDescriptor fileDescriptor, int i, MutableInt mutableInt) throws ErrnoException;

    public native int ioctlMTU(FileDescriptor fileDescriptor, String str) throws ErrnoException;

    public native boolean isatty(FileDescriptor fileDescriptor);

    public native void kill(int i, int i2) throws ErrnoException;

    public native void lchown(String str, int i, int i2) throws ErrnoException;

    public native void link(String str, String str2) throws ErrnoException;

    public native void listen(FileDescriptor fileDescriptor, int i) throws ErrnoException;

    public native String[] listxattr(String str) throws ErrnoException;

    public native long lseek(FileDescriptor fileDescriptor, long j, int i) throws ErrnoException;

    public native StructStat lstat(String str) throws ErrnoException;

    public native void mincore(long j, long j2, byte[] bArr) throws ErrnoException;

    public native void mkdir(String str, int i) throws ErrnoException;

    public native void mkfifo(String str, int i) throws ErrnoException;

    public native void mlock(long j, long j2) throws ErrnoException;

    public native long mmap(long j, long j2, int i, int i2, FileDescriptor fileDescriptor, long j3) throws ErrnoException;

    public native void msync(long j, long j2, int i) throws ErrnoException;

    public native void munlock(long j, long j2) throws ErrnoException;

    public native void munmap(long j, long j2) throws ErrnoException;

    public native FileDescriptor open(String str, int i, int i2) throws ErrnoException;

    public native FileDescriptor[] pipe2(int i) throws ErrnoException;

    public native int poll(StructPollfd[] structPollfdArr, int i) throws ErrnoException;

    public native void posix_fallocate(FileDescriptor fileDescriptor, long j, long j2) throws ErrnoException;

    public native int prctl(int i, long j, long j2, long j3, long j4) throws ErrnoException;

    public native String readlink(String str) throws ErrnoException;

    public native int readv(FileDescriptor fileDescriptor, Object[] objArr, int[] iArr, int[] iArr2) throws ErrnoException, InterruptedIOException;

    public native String realpath(String str) throws ErrnoException;

    public native void remove(String str) throws ErrnoException;

    public native void removexattr(String str, String str2) throws ErrnoException;

    public native void rename(String str, String str2) throws ErrnoException;

    public native long sendfile(FileDescriptor fileDescriptor, FileDescriptor fileDescriptor2, MutableLong mutableLong, long j) throws ErrnoException;

    public native void setegid(int i) throws ErrnoException;

    public native void setenv(String str, String str2, boolean z) throws ErrnoException;

    public native void seteuid(int i) throws ErrnoException;

    public native void setgid(int i) throws ErrnoException;

    public native void setpgid(int i, int i2) throws ErrnoException;

    public native void setregid(int i, int i2) throws ErrnoException;

    public native void setreuid(int i, int i2) throws ErrnoException;

    public native int setsid() throws ErrnoException;

    public native void setsockoptByte(FileDescriptor fileDescriptor, int i, int i2, int i3) throws ErrnoException;

    public native void setsockoptGroupReq(FileDescriptor fileDescriptor, int i, int i2, StructGroupReq structGroupReq) throws ErrnoException;

    public native void setsockoptGroupSourceReq(FileDescriptor fileDescriptor, int i, int i2, StructGroupSourceReq structGroupSourceReq) throws ErrnoException;

    public native void setsockoptIfreq(FileDescriptor fileDescriptor, int i, int i2, String str) throws ErrnoException;

    public native void setsockoptInt(FileDescriptor fileDescriptor, int i, int i2, int i3) throws ErrnoException;

    public native void setsockoptIpMreqn(FileDescriptor fileDescriptor, int i, int i2, int i3) throws ErrnoException;

    public native void setsockoptLinger(FileDescriptor fileDescriptor, int i, int i2, StructLinger structLinger) throws ErrnoException;

    public native void setsockoptTimeval(FileDescriptor fileDescriptor, int i, int i2, StructTimeval structTimeval) throws ErrnoException;

    public native void setuid(int i) throws ErrnoException;

    public native void setxattr(String str, String str2, byte[] bArr, int i) throws ErrnoException;

    public native void shutdown(FileDescriptor fileDescriptor, int i) throws ErrnoException;

    public native FileDescriptor socket(int i, int i2, int i3) throws ErrnoException;

    public native void socketpair(int i, int i2, int i3, FileDescriptor fileDescriptor, FileDescriptor fileDescriptor2) throws ErrnoException;

    public native StructStat stat(String str) throws ErrnoException;

    public native StructStatVfs statvfs(String str) throws ErrnoException;

    public native String strerror(int i);

    public native String strsignal(int i);

    public native void symlink(String str, String str2) throws ErrnoException;

    public native long sysconf(int i);

    public native void tcdrain(FileDescriptor fileDescriptor) throws ErrnoException;

    public native void tcsendbreak(FileDescriptor fileDescriptor, int i) throws ErrnoException;

    public native StructUtsname uname();

    public native void unlink(String str) throws ErrnoException;

    public native void unsetenv(String str) throws ErrnoException;

    public native int waitpid(int i, MutableInt mutableInt, int i2) throws ErrnoException;

    public native int writev(FileDescriptor fileDescriptor, Object[] objArr, int[] iArr, int[] iArr2) throws ErrnoException, InterruptedIOException;

    Linux() {
    }

    public int pread(FileDescriptor fd, ByteBuffer buffer, long offset) throws ErrnoException, InterruptedIOException {
        int bytesRead;
        int position = buffer.position();
        if (buffer.isDirect()) {
            bytesRead = preadBytes(fd, buffer, position, buffer.remaining(), offset);
        } else {
            bytesRead = preadBytes(fd, NioUtils.unsafeArray(buffer), NioUtils.unsafeArrayOffset(buffer) + position, buffer.remaining(), offset);
        }
        maybeUpdateBufferPosition(buffer, position, bytesRead);
        return bytesRead;
    }

    public int pread(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount, long offset) throws ErrnoException, InterruptedIOException {
        return preadBytes(fd, bytes, byteOffset, byteCount, offset);
    }

    public int pwrite(FileDescriptor fd, ByteBuffer buffer, long offset) throws ErrnoException, InterruptedIOException {
        int bytesWritten;
        int position = buffer.position();
        if (buffer.isDirect()) {
            bytesWritten = pwriteBytes(fd, buffer, position, buffer.remaining(), offset);
        } else {
            bytesWritten = pwriteBytes(fd, NioUtils.unsafeArray(buffer), NioUtils.unsafeArrayOffset(buffer) + position, buffer.remaining(), offset);
        }
        maybeUpdateBufferPosition(buffer, position, bytesWritten);
        return bytesWritten;
    }

    public int pwrite(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount, long offset) throws ErrnoException, InterruptedIOException {
        return pwriteBytes(fd, bytes, byteOffset, byteCount, offset);
    }

    public int read(FileDescriptor fd, ByteBuffer buffer) throws ErrnoException, InterruptedIOException {
        int bytesRead;
        int position = buffer.position();
        if (buffer.isDirect()) {
            bytesRead = readBytes(fd, buffer, position, buffer.remaining());
        } else {
            bytesRead = readBytes(fd, NioUtils.unsafeArray(buffer), NioUtils.unsafeArrayOffset(buffer) + position, buffer.remaining());
        }
        maybeUpdateBufferPosition(buffer, position, bytesRead);
        return bytesRead;
    }

    public int read(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount) throws ErrnoException, InterruptedIOException {
        return readBytes(fd, bytes, byteOffset, byteCount);
    }

    public int recvfrom(FileDescriptor fd, ByteBuffer buffer, int flags, InetSocketAddress srcAddress) throws ErrnoException, SocketException {
        int bytesReceived;
        int position = buffer.position();
        if (buffer.isDirect()) {
            bytesReceived = recvfromBytes(fd, buffer, position, buffer.remaining(), flags, srcAddress);
        } else {
            bytesReceived = recvfromBytes(fd, NioUtils.unsafeArray(buffer), NioUtils.unsafeArrayOffset(buffer) + position, buffer.remaining(), flags, srcAddress);
        }
        maybeUpdateBufferPosition(buffer, position, bytesReceived);
        return bytesReceived;
    }

    public int recvfrom(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount, int flags, InetSocketAddress srcAddress) throws ErrnoException, SocketException {
        return recvfromBytes(fd, bytes, byteOffset, byteCount, flags, srcAddress);
    }

    public int sendto(FileDescriptor fd, ByteBuffer buffer, int flags, InetAddress inetAddress, int port) throws ErrnoException, SocketException {
        int bytesSent;
        int position = buffer.position();
        if (buffer.isDirect()) {
            bytesSent = sendtoBytes(fd, buffer, position, buffer.remaining(), flags, inetAddress, port);
        } else {
            bytesSent = sendtoBytes(fd, NioUtils.unsafeArray(buffer), NioUtils.unsafeArrayOffset(buffer) + position, buffer.remaining(), flags, inetAddress, port);
        }
        maybeUpdateBufferPosition(buffer, position, bytesSent);
        return bytesSent;
    }

    public int sendto(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount, int flags, InetAddress inetAddress, int port) throws ErrnoException, SocketException {
        return sendtoBytes(fd, bytes, byteOffset, byteCount, flags, inetAddress, port);
    }

    public int sendto(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount, int flags, SocketAddress address) throws ErrnoException, SocketException {
        return sendtoBytes(fd, bytes, byteOffset, byteCount, flags, address);
    }

    public int umask(int mask) {
        if ((mask & Opcodes.OP_CHECK_CAST_JUMBO) == mask) {
            return umaskImpl(mask);
        }
        throw new IllegalArgumentException("Invalid umask: " + mask);
    }

    public int write(FileDescriptor fd, ByteBuffer buffer) throws ErrnoException, InterruptedIOException {
        int bytesWritten;
        int position = buffer.position();
        if (buffer.isDirect()) {
            bytesWritten = writeBytes(fd, buffer, position, buffer.remaining());
        } else {
            bytesWritten = writeBytes(fd, NioUtils.unsafeArray(buffer), NioUtils.unsafeArrayOffset(buffer) + position, buffer.remaining());
        }
        maybeUpdateBufferPosition(buffer, position, bytesWritten);
        return bytesWritten;
    }

    public int write(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount) throws ErrnoException, InterruptedIOException {
        return writeBytes(fd, bytes, byteOffset, byteCount);
    }

    private static void maybeUpdateBufferPosition(ByteBuffer buffer, int originalPosition, int bytesReadOrWritten) {
        if (bytesReadOrWritten > 0) {
            buffer.position(bytesReadOrWritten + originalPosition);
        }
    }
}
