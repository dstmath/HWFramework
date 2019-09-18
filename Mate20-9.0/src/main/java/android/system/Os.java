package android.system;

import java.io.FileDescriptor;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import libcore.io.Libcore;

public final class Os {
    private Os() {
    }

    public static FileDescriptor accept(FileDescriptor fd, InetSocketAddress peerAddress) throws ErrnoException, SocketException {
        return Libcore.os.accept(fd, peerAddress);
    }

    public static FileDescriptor accept(FileDescriptor fd, SocketAddress peerAddress) throws ErrnoException, SocketException {
        return Libcore.os.accept(fd, peerAddress);
    }

    public static boolean access(String path, int mode) throws ErrnoException {
        return Libcore.os.access(path, mode);
    }

    public static InetAddress[] android_getaddrinfo(String node, StructAddrinfo hints, int netId) throws GaiException {
        return Libcore.os.android_getaddrinfo(node, hints, netId);
    }

    public static void bind(FileDescriptor fd, InetAddress address, int port) throws ErrnoException, SocketException {
        Libcore.os.bind(fd, address, port);
    }

    public static void bind(FileDescriptor fd, SocketAddress address) throws ErrnoException, SocketException {
        Libcore.os.bind(fd, address);
    }

    public static StructCapUserData[] capget(StructCapUserHeader hdr) throws ErrnoException {
        return Libcore.os.capget(hdr);
    }

    public static void capset(StructCapUserHeader hdr, StructCapUserData[] data) throws ErrnoException {
        Libcore.os.capset(hdr, data);
    }

    public static void chmod(String path, int mode) throws ErrnoException {
        Libcore.os.chmod(path, mode);
    }

    public static void chown(String path, int uid, int gid) throws ErrnoException {
        Libcore.os.chown(path, uid, gid);
    }

    public static void close(FileDescriptor fd) throws ErrnoException {
        Libcore.os.close(fd);
    }

    public static void connect(FileDescriptor fd, InetAddress address, int port) throws ErrnoException, SocketException {
        Libcore.os.connect(fd, address, port);
    }

    public static void connect(FileDescriptor fd, SocketAddress address) throws ErrnoException, SocketException {
        Libcore.os.connect(fd, address);
    }

    public static FileDescriptor dup(FileDescriptor oldFd) throws ErrnoException {
        return Libcore.os.dup(oldFd);
    }

    public static FileDescriptor dup2(FileDescriptor oldFd, int newFd) throws ErrnoException {
        return Libcore.os.dup2(oldFd, newFd);
    }

    public static String[] environ() {
        return Libcore.os.environ();
    }

    public static void execv(String filename, String[] argv) throws ErrnoException {
        Libcore.os.execv(filename, argv);
    }

    public static void execve(String filename, String[] argv, String[] envp) throws ErrnoException {
        Libcore.os.execve(filename, argv, envp);
    }

    public static void fchmod(FileDescriptor fd, int mode) throws ErrnoException {
        Libcore.os.fchmod(fd, mode);
    }

    public static void fchown(FileDescriptor fd, int uid, int gid) throws ErrnoException {
        Libcore.os.fchown(fd, uid, gid);
    }

    public static int fcntlFlock(FileDescriptor fd, int cmd, StructFlock arg) throws ErrnoException, InterruptedIOException {
        return Libcore.os.fcntlFlock(fd, cmd, arg);
    }

    public static int fcntlInt(FileDescriptor fd, int cmd, int arg) throws ErrnoException {
        return Libcore.os.fcntlInt(fd, cmd, arg);
    }

    public static int fcntlVoid(FileDescriptor fd, int cmd) throws ErrnoException {
        return Libcore.os.fcntlVoid(fd, cmd);
    }

    public static void fdatasync(FileDescriptor fd) throws ErrnoException {
        Libcore.os.fdatasync(fd);
    }

    public static StructStat fstat(FileDescriptor fd) throws ErrnoException {
        return Libcore.os.fstat(fd);
    }

    public static StructStatVfs fstatvfs(FileDescriptor fd) throws ErrnoException {
        return Libcore.os.fstatvfs(fd);
    }

    public static void fsync(FileDescriptor fd) throws ErrnoException {
        Libcore.os.fsync(fd);
    }

    public static void ftruncate(FileDescriptor fd, long length) throws ErrnoException {
        Libcore.os.ftruncate(fd, length);
    }

    public static String gai_strerror(int error) {
        return Libcore.os.gai_strerror(error);
    }

    public static int getegid() {
        return Libcore.os.getegid();
    }

    public static int geteuid() {
        return Libcore.os.geteuid();
    }

    public static int getgid() {
        return Libcore.os.getgid();
    }

    public static String getenv(String name) {
        return Libcore.os.getenv(name);
    }

    public static StructIfaddrs[] getifaddrs() throws ErrnoException {
        return Libcore.os.getifaddrs();
    }

    public static String getnameinfo(InetAddress address, int flags) throws GaiException {
        return Libcore.os.getnameinfo(address, flags);
    }

    public static SocketAddress getpeername(FileDescriptor fd) throws ErrnoException {
        return Libcore.os.getpeername(fd);
    }

    public static int getpgid(int pid) throws ErrnoException {
        return Libcore.os.getpgid(pid);
    }

    public static int getpid() {
        return Libcore.os.getpid();
    }

    public static int getppid() {
        return Libcore.os.getppid();
    }

    public static StructPasswd getpwnam(String name) throws ErrnoException {
        return Libcore.os.getpwnam(name);
    }

    public static StructPasswd getpwuid(int uid) throws ErrnoException {
        return Libcore.os.getpwuid(uid);
    }

    public static StructRlimit getrlimit(int resource) throws ErrnoException {
        return Libcore.os.getrlimit(resource);
    }

    public static SocketAddress getsockname(FileDescriptor fd) throws ErrnoException {
        return Libcore.os.getsockname(fd);
    }

    public static int getsockoptByte(FileDescriptor fd, int level, int option) throws ErrnoException {
        return Libcore.os.getsockoptByte(fd, level, option);
    }

    public static InetAddress getsockoptInAddr(FileDescriptor fd, int level, int option) throws ErrnoException {
        return Libcore.os.getsockoptInAddr(fd, level, option);
    }

    public static int getsockoptInt(FileDescriptor fd, int level, int option) throws ErrnoException {
        return Libcore.os.getsockoptInt(fd, level, option);
    }

    public static StructLinger getsockoptLinger(FileDescriptor fd, int level, int option) throws ErrnoException {
        return Libcore.os.getsockoptLinger(fd, level, option);
    }

    public static StructTimeval getsockoptTimeval(FileDescriptor fd, int level, int option) throws ErrnoException {
        return Libcore.os.getsockoptTimeval(fd, level, option);
    }

    public static StructUcred getsockoptUcred(FileDescriptor fd, int level, int option) throws ErrnoException {
        return Libcore.os.getsockoptUcred(fd, level, option);
    }

    public static int gettid() {
        return Libcore.os.gettid();
    }

    public static int getuid() {
        return Libcore.os.getuid();
    }

    public static byte[] getxattr(String path, String name) throws ErrnoException {
        return Libcore.os.getxattr(path, name);
    }

    public static String if_indextoname(int index) {
        return Libcore.os.if_indextoname(index);
    }

    public static int if_nametoindex(String name) {
        return Libcore.os.if_nametoindex(name);
    }

    public static InetAddress inet_pton(int family, String address) {
        return Libcore.os.inet_pton(family, address);
    }

    public static InetAddress ioctlInetAddress(FileDescriptor fd, int cmd, String interfaceName) throws ErrnoException {
        return Libcore.os.ioctlInetAddress(fd, cmd, interfaceName);
    }

    public static int ioctlInt(FileDescriptor fd, int cmd, Int32Ref arg) throws ErrnoException {
        return Libcore.os.ioctlInt(fd, cmd, arg);
    }

    public static boolean isatty(FileDescriptor fd) {
        return Libcore.os.isatty(fd);
    }

    public static void kill(int pid, int signal) throws ErrnoException {
        Libcore.os.kill(pid, signal);
    }

    public static void lchown(String path, int uid, int gid) throws ErrnoException {
        Libcore.os.lchown(path, uid, gid);
    }

    public static void link(String oldPath, String newPath) throws ErrnoException {
        Libcore.os.link(oldPath, newPath);
    }

    public static void listen(FileDescriptor fd, int backlog) throws ErrnoException {
        Libcore.os.listen(fd, backlog);
    }

    public static String[] listxattr(String path) throws ErrnoException {
        return Libcore.os.listxattr(path);
    }

    public static long lseek(FileDescriptor fd, long offset, int whence) throws ErrnoException {
        return Libcore.os.lseek(fd, offset, whence);
    }

    public static StructStat lstat(String path) throws ErrnoException {
        return Libcore.os.lstat(path);
    }

    public static void mincore(long address, long byteCount, byte[] vector) throws ErrnoException {
        Libcore.os.mincore(address, byteCount, vector);
    }

    public static void mkdir(String path, int mode) throws ErrnoException {
        Libcore.os.mkdir(path, mode);
    }

    public static void mkfifo(String path, int mode) throws ErrnoException {
        Libcore.os.mkfifo(path, mode);
    }

    public static void mlock(long address, long byteCount) throws ErrnoException {
        Libcore.os.mlock(address, byteCount);
    }

    public static long mmap(long address, long byteCount, int prot, int flags, FileDescriptor fd, long offset) throws ErrnoException {
        return Libcore.os.mmap(address, byteCount, prot, flags, fd, offset);
    }

    public static void msync(long address, long byteCount, int flags) throws ErrnoException {
        Libcore.os.msync(address, byteCount, flags);
    }

    public static void munlock(long address, long byteCount) throws ErrnoException {
        Libcore.os.munlock(address, byteCount);
    }

    public static void munmap(long address, long byteCount) throws ErrnoException {
        Libcore.os.munmap(address, byteCount);
    }

    public static FileDescriptor open(String path, int flags, int mode) throws ErrnoException {
        return Libcore.os.open(path, flags, mode);
    }

    public static FileDescriptor[] pipe() throws ErrnoException {
        return Libcore.os.pipe2(0);
    }

    public static FileDescriptor[] pipe2(int flags) throws ErrnoException {
        return Libcore.os.pipe2(flags);
    }

    public static int poll(StructPollfd[] fds, int timeoutMs) throws ErrnoException {
        return Libcore.os.poll(fds, timeoutMs);
    }

    public static void posix_fallocate(FileDescriptor fd, long offset, long length) throws ErrnoException {
        Libcore.os.posix_fallocate(fd, offset, length);
    }

    public static int prctl(int option, long arg2, long arg3, long arg4, long arg5) throws ErrnoException {
        return Libcore.os.prctl(option, arg2, arg3, arg4, arg5);
    }

    public static int pread(FileDescriptor fd, ByteBuffer buffer, long offset) throws ErrnoException, InterruptedIOException {
        return Libcore.os.pread(fd, buffer, offset);
    }

    public static int pread(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount, long offset) throws ErrnoException, InterruptedIOException {
        return Libcore.os.pread(fd, bytes, byteOffset, byteCount, offset);
    }

    public static int pwrite(FileDescriptor fd, ByteBuffer buffer, long offset) throws ErrnoException, InterruptedIOException {
        return Libcore.os.pwrite(fd, buffer, offset);
    }

    public static int pwrite(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount, long offset) throws ErrnoException, InterruptedIOException {
        return Libcore.os.pwrite(fd, bytes, byteOffset, byteCount, offset);
    }

    public static int read(FileDescriptor fd, ByteBuffer buffer) throws ErrnoException, InterruptedIOException {
        return Libcore.os.read(fd, buffer);
    }

    public static int read(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount) throws ErrnoException, InterruptedIOException {
        return Libcore.os.read(fd, bytes, byteOffset, byteCount);
    }

    public static String readlink(String path) throws ErrnoException {
        return Libcore.os.readlink(path);
    }

    public static String realpath(String path) throws ErrnoException {
        return Libcore.os.realpath(path);
    }

    public static int readv(FileDescriptor fd, Object[] buffers, int[] offsets, int[] byteCounts) throws ErrnoException, InterruptedIOException {
        return Libcore.os.readv(fd, buffers, offsets, byteCounts);
    }

    public static int recvfrom(FileDescriptor fd, ByteBuffer buffer, int flags, InetSocketAddress srcAddress) throws ErrnoException, SocketException {
        return Libcore.os.recvfrom(fd, buffer, flags, srcAddress);
    }

    public static int recvfrom(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount, int flags, InetSocketAddress srcAddress) throws ErrnoException, SocketException {
        return Libcore.os.recvfrom(fd, bytes, byteOffset, byteCount, flags, srcAddress);
    }

    public static void remove(String path) throws ErrnoException {
        Libcore.os.remove(path);
    }

    public static void removexattr(String path, String name) throws ErrnoException {
        Libcore.os.removexattr(path, name);
    }

    public static void rename(String oldPath, String newPath) throws ErrnoException {
        Libcore.os.rename(oldPath, newPath);
    }

    public static long sendfile(FileDescriptor outFd, FileDescriptor inFd, Int64Ref offset, long byteCount) throws ErrnoException {
        return Libcore.os.sendfile(outFd, inFd, offset, byteCount);
    }

    public static int sendto(FileDescriptor fd, ByteBuffer buffer, int flags, InetAddress inetAddress, int port) throws ErrnoException, SocketException {
        return Libcore.os.sendto(fd, buffer, flags, inetAddress, port);
    }

    public static int sendto(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount, int flags, InetAddress inetAddress, int port) throws ErrnoException, SocketException {
        return Libcore.os.sendto(fd, bytes, byteOffset, byteCount, flags, inetAddress, port);
    }

    public static int sendto(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount, int flags, SocketAddress address) throws ErrnoException, SocketException {
        return Libcore.os.sendto(fd, bytes, byteOffset, byteCount, flags, address);
    }

    public static void setegid(int egid) throws ErrnoException {
        Libcore.os.setegid(egid);
    }

    public static void setenv(String name, String value, boolean overwrite) throws ErrnoException {
        Libcore.os.setenv(name, value, overwrite);
    }

    public static void seteuid(int euid) throws ErrnoException {
        Libcore.os.seteuid(euid);
    }

    public static void setgid(int gid) throws ErrnoException {
        Libcore.os.setgid(gid);
    }

    public static void setpgid(int pid, int pgid) throws ErrnoException {
        Libcore.os.setpgid(pid, pgid);
    }

    public static void setregid(int rgid, int egid) throws ErrnoException {
        Libcore.os.setregid(rgid, egid);
    }

    public static void setreuid(int ruid, int euid) throws ErrnoException {
        Libcore.os.setreuid(ruid, euid);
    }

    public static int setsid() throws ErrnoException {
        return Libcore.os.setsid();
    }

    public static void setsockoptByte(FileDescriptor fd, int level, int option, int value) throws ErrnoException {
        Libcore.os.setsockoptByte(fd, level, option, value);
    }

    public static void setsockoptIfreq(FileDescriptor fd, int level, int option, String value) throws ErrnoException {
        Libcore.os.setsockoptIfreq(fd, level, option, value);
    }

    public static void setsockoptInt(FileDescriptor fd, int level, int option, int value) throws ErrnoException {
        Libcore.os.setsockoptInt(fd, level, option, value);
    }

    public static void setsockoptIpMreqn(FileDescriptor fd, int level, int option, int value) throws ErrnoException {
        Libcore.os.setsockoptIpMreqn(fd, level, option, value);
    }

    public static void setsockoptGroupReq(FileDescriptor fd, int level, int option, StructGroupReq value) throws ErrnoException {
        Libcore.os.setsockoptGroupReq(fd, level, option, value);
    }

    public static void setsockoptLinger(FileDescriptor fd, int level, int option, StructLinger value) throws ErrnoException {
        Libcore.os.setsockoptLinger(fd, level, option, value);
    }

    public static void setsockoptTimeval(FileDescriptor fd, int level, int option, StructTimeval value) throws ErrnoException {
        Libcore.os.setsockoptTimeval(fd, level, option, value);
    }

    public static void setuid(int uid) throws ErrnoException {
        Libcore.os.setuid(uid);
    }

    public static void setxattr(String path, String name, byte[] value, int flags) throws ErrnoException {
        Libcore.os.setxattr(path, name, value, flags);
    }

    public static void shutdown(FileDescriptor fd, int how) throws ErrnoException {
        Libcore.os.shutdown(fd, how);
    }

    public static FileDescriptor socket(int domain, int type, int protocol) throws ErrnoException {
        return Libcore.os.socket(domain, type, protocol);
    }

    public static void socketpair(int domain, int type, int protocol, FileDescriptor fd1, FileDescriptor fd2) throws ErrnoException {
        Libcore.os.socketpair(domain, type, protocol, fd1, fd2);
    }

    public static long splice(FileDescriptor fdIn, Int64Ref offIn, FileDescriptor fdOut, Int64Ref offOut, long len, int flags) throws ErrnoException {
        return Libcore.os.splice(fdIn, offIn, fdOut, offOut, len, flags);
    }

    public static StructStat stat(String path) throws ErrnoException {
        return Libcore.os.stat(path);
    }

    public static StructStatVfs statvfs(String path) throws ErrnoException {
        return Libcore.os.statvfs(path);
    }

    public static String strerror(int errno) {
        return Libcore.os.strerror(errno);
    }

    public static String strsignal(int signal) {
        return Libcore.os.strsignal(signal);
    }

    public static void symlink(String oldPath, String newPath) throws ErrnoException {
        Libcore.os.symlink(oldPath, newPath);
    }

    public static long sysconf(int name) {
        return Libcore.os.sysconf(name);
    }

    public static void tcdrain(FileDescriptor fd) throws ErrnoException {
        Libcore.os.tcdrain(fd);
    }

    public static void tcsendbreak(FileDescriptor fd, int duration) throws ErrnoException {
        Libcore.os.tcsendbreak(fd, duration);
    }

    public static int umask(int mask) {
        return Libcore.os.umask(mask);
    }

    public static StructUtsname uname() {
        return Libcore.os.uname();
    }

    public static void unlink(String pathname) throws ErrnoException {
        Libcore.os.unlink(pathname);
    }

    public static void unsetenv(String name) throws ErrnoException {
        Libcore.os.unsetenv(name);
    }

    public static int waitpid(int pid, Int32Ref status, int options) throws ErrnoException {
        return Libcore.os.waitpid(pid, status, options);
    }

    public static int write(FileDescriptor fd, ByteBuffer buffer) throws ErrnoException, InterruptedIOException {
        return Libcore.os.write(fd, buffer);
    }

    public static int write(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount) throws ErrnoException, InterruptedIOException {
        return Libcore.os.write(fd, bytes, byteOffset, byteCount);
    }

    public static int writev(FileDescriptor fd, Object[] buffers, int[] offsets, int[] byteCounts) throws ErrnoException, InterruptedIOException {
        return Libcore.os.writev(fd, buffers, offsets, byteCounts);
    }
}
