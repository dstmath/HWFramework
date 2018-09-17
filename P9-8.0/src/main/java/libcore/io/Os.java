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
import java.io.FileDescriptor;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;

public interface Os {
    FileDescriptor accept(FileDescriptor fileDescriptor, SocketAddress socketAddress) throws ErrnoException, SocketException;

    boolean access(String str, int i) throws ErrnoException;

    InetAddress[] android_getaddrinfo(String str, StructAddrinfo structAddrinfo, int i) throws GaiException;

    void bind(FileDescriptor fileDescriptor, InetAddress inetAddress, int i) throws ErrnoException, SocketException;

    void bind(FileDescriptor fileDescriptor, SocketAddress socketAddress) throws ErrnoException, SocketException;

    StructCapUserData[] capget(StructCapUserHeader structCapUserHeader) throws ErrnoException;

    void capset(StructCapUserHeader structCapUserHeader, StructCapUserData[] structCapUserDataArr) throws ErrnoException;

    void chmod(String str, int i) throws ErrnoException;

    void chown(String str, int i, int i2) throws ErrnoException;

    void close(FileDescriptor fileDescriptor) throws ErrnoException;

    void connect(FileDescriptor fileDescriptor, InetAddress inetAddress, int i) throws ErrnoException, SocketException;

    void connect(FileDescriptor fileDescriptor, SocketAddress socketAddress) throws ErrnoException, SocketException;

    FileDescriptor dup(FileDescriptor fileDescriptor) throws ErrnoException;

    FileDescriptor dup2(FileDescriptor fileDescriptor, int i) throws ErrnoException;

    String[] environ();

    void execv(String str, String[] strArr) throws ErrnoException;

    void execve(String str, String[] strArr, String[] strArr2) throws ErrnoException;

    void fchmod(FileDescriptor fileDescriptor, int i) throws ErrnoException;

    void fchown(FileDescriptor fileDescriptor, int i, int i2) throws ErrnoException;

    int fcntlFlock(FileDescriptor fileDescriptor, int i, StructFlock structFlock) throws ErrnoException, InterruptedIOException;

    int fcntlInt(FileDescriptor fileDescriptor, int i, int i2) throws ErrnoException;

    int fcntlVoid(FileDescriptor fileDescriptor, int i) throws ErrnoException;

    void fdatasync(FileDescriptor fileDescriptor) throws ErrnoException;

    StructStat fstat(FileDescriptor fileDescriptor) throws ErrnoException;

    StructStatVfs fstatvfs(FileDescriptor fileDescriptor) throws ErrnoException;

    void fsync(FileDescriptor fileDescriptor) throws ErrnoException;

    void ftruncate(FileDescriptor fileDescriptor, long j) throws ErrnoException;

    String gai_strerror(int i);

    int getegid();

    String getenv(String str);

    int geteuid();

    int getgid();

    StructIfaddrs[] getifaddrs() throws ErrnoException;

    String getnameinfo(InetAddress inetAddress, int i) throws GaiException;

    SocketAddress getpeername(FileDescriptor fileDescriptor) throws ErrnoException;

    int getpgid(int i) throws ErrnoException;

    int getpid();

    int getppid();

    StructPasswd getpwnam(String str) throws ErrnoException;

    StructPasswd getpwuid(int i) throws ErrnoException;

    SocketAddress getsockname(FileDescriptor fileDescriptor) throws ErrnoException;

    int getsockoptByte(FileDescriptor fileDescriptor, int i, int i2) throws ErrnoException;

    InetAddress getsockoptInAddr(FileDescriptor fileDescriptor, int i, int i2) throws ErrnoException;

    int getsockoptInt(FileDescriptor fileDescriptor, int i, int i2) throws ErrnoException;

    StructLinger getsockoptLinger(FileDescriptor fileDescriptor, int i, int i2) throws ErrnoException;

    StructTimeval getsockoptTimeval(FileDescriptor fileDescriptor, int i, int i2) throws ErrnoException;

    StructUcred getsockoptUcred(FileDescriptor fileDescriptor, int i, int i2) throws ErrnoException;

    int gettid();

    int getuid();

    byte[] getxattr(String str, String str2) throws ErrnoException;

    String if_indextoname(int i);

    int if_nametoindex(String str);

    InetAddress inet_pton(int i, String str);

    int ioctlFlags(FileDescriptor fileDescriptor, String str) throws ErrnoException;

    InetAddress ioctlInetAddress(FileDescriptor fileDescriptor, int i, String str) throws ErrnoException;

    int ioctlInt(FileDescriptor fileDescriptor, int i, MutableInt mutableInt) throws ErrnoException;

    int ioctlMTU(FileDescriptor fileDescriptor, String str) throws ErrnoException;

    boolean isatty(FileDescriptor fileDescriptor);

    void kill(int i, int i2) throws ErrnoException;

    void lchown(String str, int i, int i2) throws ErrnoException;

    void link(String str, String str2) throws ErrnoException;

    void listen(FileDescriptor fileDescriptor, int i) throws ErrnoException;

    String[] listxattr(String str) throws ErrnoException;

    long lseek(FileDescriptor fileDescriptor, long j, int i) throws ErrnoException;

    StructStat lstat(String str) throws ErrnoException;

    void mincore(long j, long j2, byte[] bArr) throws ErrnoException;

    void mkdir(String str, int i) throws ErrnoException;

    void mkfifo(String str, int i) throws ErrnoException;

    void mlock(long j, long j2) throws ErrnoException;

    long mmap(long j, long j2, int i, int i2, FileDescriptor fileDescriptor, long j3) throws ErrnoException;

    void msync(long j, long j2, int i) throws ErrnoException;

    void munlock(long j, long j2) throws ErrnoException;

    void munmap(long j, long j2) throws ErrnoException;

    FileDescriptor open(String str, int i, int i2) throws ErrnoException;

    FileDescriptor[] pipe2(int i) throws ErrnoException;

    int poll(StructPollfd[] structPollfdArr, int i) throws ErrnoException;

    void posix_fallocate(FileDescriptor fileDescriptor, long j, long j2) throws ErrnoException;

    int prctl(int i, long j, long j2, long j3, long j4) throws ErrnoException;

    int pread(FileDescriptor fileDescriptor, ByteBuffer byteBuffer, long j) throws ErrnoException, InterruptedIOException;

    int pread(FileDescriptor fileDescriptor, byte[] bArr, int i, int i2, long j) throws ErrnoException, InterruptedIOException;

    int pwrite(FileDescriptor fileDescriptor, ByteBuffer byteBuffer, long j) throws ErrnoException, InterruptedIOException;

    int pwrite(FileDescriptor fileDescriptor, byte[] bArr, int i, int i2, long j) throws ErrnoException, InterruptedIOException;

    int read(FileDescriptor fileDescriptor, ByteBuffer byteBuffer) throws ErrnoException, InterruptedIOException;

    int read(FileDescriptor fileDescriptor, byte[] bArr, int i, int i2) throws ErrnoException, InterruptedIOException;

    String readlink(String str) throws ErrnoException;

    int readv(FileDescriptor fileDescriptor, Object[] objArr, int[] iArr, int[] iArr2) throws ErrnoException, InterruptedIOException;

    String realpath(String str) throws ErrnoException;

    int recvfrom(FileDescriptor fileDescriptor, ByteBuffer byteBuffer, int i, InetSocketAddress inetSocketAddress) throws ErrnoException, SocketException;

    int recvfrom(FileDescriptor fileDescriptor, byte[] bArr, int i, int i2, int i3, InetSocketAddress inetSocketAddress) throws ErrnoException, SocketException;

    void remove(String str) throws ErrnoException;

    void removexattr(String str, String str2) throws ErrnoException;

    void rename(String str, String str2) throws ErrnoException;

    long sendfile(FileDescriptor fileDescriptor, FileDescriptor fileDescriptor2, MutableLong mutableLong, long j) throws ErrnoException;

    int sendto(FileDescriptor fileDescriptor, ByteBuffer byteBuffer, int i, InetAddress inetAddress, int i2) throws ErrnoException, SocketException;

    int sendto(FileDescriptor fileDescriptor, byte[] bArr, int i, int i2, int i3, InetAddress inetAddress, int i4) throws ErrnoException, SocketException;

    int sendto(FileDescriptor fileDescriptor, byte[] bArr, int i, int i2, int i3, SocketAddress socketAddress) throws ErrnoException, SocketException;

    void setegid(int i) throws ErrnoException;

    void setenv(String str, String str2, boolean z) throws ErrnoException;

    void seteuid(int i) throws ErrnoException;

    void setgid(int i) throws ErrnoException;

    void setpgid(int i, int i2) throws ErrnoException;

    void setregid(int i, int i2) throws ErrnoException;

    void setreuid(int i, int i2) throws ErrnoException;

    int setsid() throws ErrnoException;

    void setsockoptByte(FileDescriptor fileDescriptor, int i, int i2, int i3) throws ErrnoException;

    void setsockoptGroupReq(FileDescriptor fileDescriptor, int i, int i2, StructGroupReq structGroupReq) throws ErrnoException;

    void setsockoptGroupSourceReq(FileDescriptor fileDescriptor, int i, int i2, StructGroupSourceReq structGroupSourceReq) throws ErrnoException;

    void setsockoptIfreq(FileDescriptor fileDescriptor, int i, int i2, String str) throws ErrnoException;

    void setsockoptInt(FileDescriptor fileDescriptor, int i, int i2, int i3) throws ErrnoException;

    void setsockoptIpMreqn(FileDescriptor fileDescriptor, int i, int i2, int i3) throws ErrnoException;

    void setsockoptLinger(FileDescriptor fileDescriptor, int i, int i2, StructLinger structLinger) throws ErrnoException;

    void setsockoptTimeval(FileDescriptor fileDescriptor, int i, int i2, StructTimeval structTimeval) throws ErrnoException;

    void setuid(int i) throws ErrnoException;

    void setxattr(String str, String str2, byte[] bArr, int i) throws ErrnoException;

    void shutdown(FileDescriptor fileDescriptor, int i) throws ErrnoException;

    FileDescriptor socket(int i, int i2, int i3) throws ErrnoException;

    void socketpair(int i, int i2, int i3, FileDescriptor fileDescriptor, FileDescriptor fileDescriptor2) throws ErrnoException;

    StructStat stat(String str) throws ErrnoException;

    StructStatVfs statvfs(String str) throws ErrnoException;

    String strerror(int i);

    String strsignal(int i);

    void symlink(String str, String str2) throws ErrnoException;

    long sysconf(int i);

    void tcdrain(FileDescriptor fileDescriptor) throws ErrnoException;

    void tcsendbreak(FileDescriptor fileDescriptor, int i) throws ErrnoException;

    int umask(int i);

    StructUtsname uname();

    void unlink(String str) throws ErrnoException;

    void unsetenv(String str) throws ErrnoException;

    int waitpid(int i, MutableInt mutableInt, int i2) throws ErrnoException;

    int write(FileDescriptor fileDescriptor, ByteBuffer byteBuffer) throws ErrnoException, InterruptedIOException;

    int write(FileDescriptor fileDescriptor, byte[] bArr, int i, int i2) throws ErrnoException, InterruptedIOException;

    int writev(FileDescriptor fileDescriptor, Object[] objArr, int[] iArr, int[] iArr2) throws ErrnoException, InterruptedIOException;
}
