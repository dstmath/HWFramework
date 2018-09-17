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

public class ForwardingOs implements Os {
    protected final Os os;

    public ForwardingOs(Os os) {
        this.os = os;
    }

    public FileDescriptor accept(FileDescriptor fd, SocketAddress peerAddress) throws ErrnoException, SocketException {
        return this.os.accept(fd, peerAddress);
    }

    public boolean access(String path, int mode) throws ErrnoException {
        return this.os.access(path, mode);
    }

    public InetAddress[] android_getaddrinfo(String node, StructAddrinfo hints, int netId) throws GaiException {
        return this.os.android_getaddrinfo(node, hints, netId);
    }

    public void bind(FileDescriptor fd, InetAddress address, int port) throws ErrnoException, SocketException {
        this.os.bind(fd, address, port);
    }

    public void bind(FileDescriptor fd, SocketAddress address) throws ErrnoException, SocketException {
        this.os.bind(fd, address);
    }

    public StructCapUserData[] capget(StructCapUserHeader hdr) throws ErrnoException {
        return this.os.capget(hdr);
    }

    public void capset(StructCapUserHeader hdr, StructCapUserData[] data) throws ErrnoException {
        this.os.capset(hdr, data);
    }

    public void chmod(String path, int mode) throws ErrnoException {
        this.os.chmod(path, mode);
    }

    public void chown(String path, int uid, int gid) throws ErrnoException {
        this.os.chown(path, uid, gid);
    }

    public void close(FileDescriptor fd) throws ErrnoException {
        this.os.close(fd);
    }

    public void connect(FileDescriptor fd, InetAddress address, int port) throws ErrnoException, SocketException {
        this.os.connect(fd, address, port);
    }

    public void connect(FileDescriptor fd, SocketAddress address) throws ErrnoException, SocketException {
        this.os.connect(fd, address);
    }

    public FileDescriptor dup(FileDescriptor oldFd) throws ErrnoException {
        return this.os.dup(oldFd);
    }

    public FileDescriptor dup2(FileDescriptor oldFd, int newFd) throws ErrnoException {
        return this.os.dup2(oldFd, newFd);
    }

    public String[] environ() {
        return this.os.environ();
    }

    public void execv(String filename, String[] argv) throws ErrnoException {
        this.os.execv(filename, argv);
    }

    public void execve(String filename, String[] argv, String[] envp) throws ErrnoException {
        this.os.execve(filename, argv, envp);
    }

    public void fchmod(FileDescriptor fd, int mode) throws ErrnoException {
        this.os.fchmod(fd, mode);
    }

    public void fchown(FileDescriptor fd, int uid, int gid) throws ErrnoException {
        this.os.fchown(fd, uid, gid);
    }

    public int fcntlFlock(FileDescriptor fd, int cmd, StructFlock arg) throws ErrnoException, InterruptedIOException {
        return this.os.fcntlFlock(fd, cmd, arg);
    }

    public int fcntlInt(FileDescriptor fd, int cmd, int arg) throws ErrnoException {
        return this.os.fcntlInt(fd, cmd, arg);
    }

    public int fcntlVoid(FileDescriptor fd, int cmd) throws ErrnoException {
        return this.os.fcntlVoid(fd, cmd);
    }

    public void fdatasync(FileDescriptor fd) throws ErrnoException {
        this.os.fdatasync(fd);
    }

    public StructStat fstat(FileDescriptor fd) throws ErrnoException {
        return this.os.fstat(fd);
    }

    public StructStatVfs fstatvfs(FileDescriptor fd) throws ErrnoException {
        return this.os.fstatvfs(fd);
    }

    public void fsync(FileDescriptor fd) throws ErrnoException {
        this.os.fsync(fd);
    }

    public void ftruncate(FileDescriptor fd, long length) throws ErrnoException {
        this.os.ftruncate(fd, length);
    }

    public String gai_strerror(int error) {
        return this.os.gai_strerror(error);
    }

    public int getegid() {
        return this.os.getegid();
    }

    public int geteuid() {
        return this.os.geteuid();
    }

    public int getgid() {
        return this.os.getgid();
    }

    public String getenv(String name) {
        return this.os.getenv(name);
    }

    public String getnameinfo(InetAddress address, int flags) throws GaiException {
        return this.os.getnameinfo(address, flags);
    }

    public SocketAddress getpeername(FileDescriptor fd) throws ErrnoException {
        return this.os.getpeername(fd);
    }

    public int getpgid(int pid) throws ErrnoException {
        return this.os.getpgid(pid);
    }

    public int getpid() {
        return this.os.getpid();
    }

    public int getppid() {
        return this.os.getppid();
    }

    public StructPasswd getpwnam(String name) throws ErrnoException {
        return this.os.getpwnam(name);
    }

    public StructPasswd getpwuid(int uid) throws ErrnoException {
        return this.os.getpwuid(uid);
    }

    public SocketAddress getsockname(FileDescriptor fd) throws ErrnoException {
        return this.os.getsockname(fd);
    }

    public int getsockoptByte(FileDescriptor fd, int level, int option) throws ErrnoException {
        return this.os.getsockoptByte(fd, level, option);
    }

    public InetAddress getsockoptInAddr(FileDescriptor fd, int level, int option) throws ErrnoException {
        return this.os.getsockoptInAddr(fd, level, option);
    }

    public int getsockoptInt(FileDescriptor fd, int level, int option) throws ErrnoException {
        return this.os.getsockoptInt(fd, level, option);
    }

    public StructLinger getsockoptLinger(FileDescriptor fd, int level, int option) throws ErrnoException {
        return this.os.getsockoptLinger(fd, level, option);
    }

    public StructTimeval getsockoptTimeval(FileDescriptor fd, int level, int option) throws ErrnoException {
        return this.os.getsockoptTimeval(fd, level, option);
    }

    public StructUcred getsockoptUcred(FileDescriptor fd, int level, int option) throws ErrnoException {
        return this.os.getsockoptUcred(fd, level, option);
    }

    public int gettid() {
        return this.os.gettid();
    }

    public int getuid() {
        return this.os.getuid();
    }

    public byte[] getxattr(String path, String name) throws ErrnoException {
        return this.os.getxattr(path, name);
    }

    public StructIfaddrs[] getifaddrs() throws ErrnoException {
        return this.os.getifaddrs();
    }

    public String if_indextoname(int index) {
        return this.os.if_indextoname(index);
    }

    public int if_nametoindex(String name) {
        return this.os.if_nametoindex(name);
    }

    public InetAddress inet_pton(int family, String address) {
        return this.os.inet_pton(family, address);
    }

    public int ioctlFlags(FileDescriptor fd, String interfaceName) throws ErrnoException {
        return this.os.ioctlFlags(fd, interfaceName);
    }

    public InetAddress ioctlInetAddress(FileDescriptor fd, int cmd, String interfaceName) throws ErrnoException {
        return this.os.ioctlInetAddress(fd, cmd, interfaceName);
    }

    public int ioctlInt(FileDescriptor fd, int cmd, MutableInt arg) throws ErrnoException {
        return this.os.ioctlInt(fd, cmd, arg);
    }

    public int ioctlMTU(FileDescriptor fd, String interfaceName) throws ErrnoException {
        return this.os.ioctlMTU(fd, interfaceName);
    }

    public boolean isatty(FileDescriptor fd) {
        return this.os.isatty(fd);
    }

    public void kill(int pid, int signal) throws ErrnoException {
        this.os.kill(pid, signal);
    }

    public void lchown(String path, int uid, int gid) throws ErrnoException {
        this.os.lchown(path, uid, gid);
    }

    public void link(String oldPath, String newPath) throws ErrnoException {
        this.os.link(oldPath, newPath);
    }

    public void listen(FileDescriptor fd, int backlog) throws ErrnoException {
        this.os.listen(fd, backlog);
    }

    public String[] listxattr(String path) throws ErrnoException {
        return this.os.listxattr(path);
    }

    public long lseek(FileDescriptor fd, long offset, int whence) throws ErrnoException {
        return this.os.lseek(fd, offset, whence);
    }

    public StructStat lstat(String path) throws ErrnoException {
        return this.os.lstat(path);
    }

    public void mincore(long address, long byteCount, byte[] vector) throws ErrnoException {
        this.os.mincore(address, byteCount, vector);
    }

    public void mkdir(String path, int mode) throws ErrnoException {
        this.os.mkdir(path, mode);
    }

    public void mkfifo(String path, int mode) throws ErrnoException {
        this.os.mkfifo(path, mode);
    }

    public void mlock(long address, long byteCount) throws ErrnoException {
        this.os.mlock(address, byteCount);
    }

    public long mmap(long address, long byteCount, int prot, int flags, FileDescriptor fd, long offset) throws ErrnoException {
        return this.os.mmap(address, byteCount, prot, flags, fd, offset);
    }

    public void msync(long address, long byteCount, int flags) throws ErrnoException {
        this.os.msync(address, byteCount, flags);
    }

    public void munlock(long address, long byteCount) throws ErrnoException {
        this.os.munlock(address, byteCount);
    }

    public void munmap(long address, long byteCount) throws ErrnoException {
        this.os.munmap(address, byteCount);
    }

    public FileDescriptor open(String path, int flags, int mode) throws ErrnoException {
        return this.os.open(path, flags, mode);
    }

    public FileDescriptor[] pipe2(int flags) throws ErrnoException {
        return this.os.pipe2(flags);
    }

    public int poll(StructPollfd[] fds, int timeoutMs) throws ErrnoException {
        return this.os.poll(fds, timeoutMs);
    }

    public void posix_fallocate(FileDescriptor fd, long offset, long length) throws ErrnoException {
        this.os.posix_fallocate(fd, offset, length);
    }

    public int prctl(int option, long arg2, long arg3, long arg4, long arg5) throws ErrnoException {
        return this.os.prctl(option, arg2, arg3, arg4, arg5);
    }

    public int pread(FileDescriptor fd, ByteBuffer buffer, long offset) throws ErrnoException, InterruptedIOException {
        return this.os.pread(fd, buffer, offset);
    }

    public int pread(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount, long offset) throws ErrnoException, InterruptedIOException {
        return this.os.pread(fd, bytes, byteOffset, byteCount, offset);
    }

    public int pwrite(FileDescriptor fd, ByteBuffer buffer, long offset) throws ErrnoException, InterruptedIOException {
        return this.os.pwrite(fd, buffer, offset);
    }

    public int pwrite(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount, long offset) throws ErrnoException, InterruptedIOException {
        return this.os.pwrite(fd, bytes, byteOffset, byteCount, offset);
    }

    public int read(FileDescriptor fd, ByteBuffer buffer) throws ErrnoException, InterruptedIOException {
        return this.os.read(fd, buffer);
    }

    public int read(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount) throws ErrnoException, InterruptedIOException {
        return this.os.read(fd, bytes, byteOffset, byteCount);
    }

    public String readlink(String path) throws ErrnoException {
        return this.os.readlink(path);
    }

    public String realpath(String path) throws ErrnoException {
        return this.os.realpath(path);
    }

    public int readv(FileDescriptor fd, Object[] buffers, int[] offsets, int[] byteCounts) throws ErrnoException, InterruptedIOException {
        return this.os.readv(fd, buffers, offsets, byteCounts);
    }

    public int recvfrom(FileDescriptor fd, ByteBuffer buffer, int flags, InetSocketAddress srcAddress) throws ErrnoException, SocketException {
        return this.os.recvfrom(fd, buffer, flags, srcAddress);
    }

    public int recvfrom(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount, int flags, InetSocketAddress srcAddress) throws ErrnoException, SocketException {
        return this.os.recvfrom(fd, bytes, byteOffset, byteCount, flags, srcAddress);
    }

    public void remove(String path) throws ErrnoException {
        this.os.remove(path);
    }

    public void removexattr(String path, String name) throws ErrnoException {
        this.os.removexattr(path, name);
    }

    public void rename(String oldPath, String newPath) throws ErrnoException {
        this.os.rename(oldPath, newPath);
    }

    public long sendfile(FileDescriptor outFd, FileDescriptor inFd, MutableLong inOffset, long byteCount) throws ErrnoException {
        return this.os.sendfile(outFd, inFd, inOffset, byteCount);
    }

    public int sendto(FileDescriptor fd, ByteBuffer buffer, int flags, InetAddress inetAddress, int port) throws ErrnoException, SocketException {
        return this.os.sendto(fd, buffer, flags, inetAddress, port);
    }

    public int sendto(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount, int flags, InetAddress inetAddress, int port) throws ErrnoException, SocketException {
        return this.os.sendto(fd, bytes, byteOffset, byteCount, flags, inetAddress, port);
    }

    public int sendto(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount, int flags, SocketAddress address) throws ErrnoException, SocketException {
        return this.os.sendto(fd, bytes, byteOffset, byteCount, flags, address);
    }

    public void setegid(int egid) throws ErrnoException {
        this.os.setegid(egid);
    }

    public void setenv(String name, String value, boolean overwrite) throws ErrnoException {
        this.os.setenv(name, value, overwrite);
    }

    public void seteuid(int euid) throws ErrnoException {
        this.os.seteuid(euid);
    }

    public void setgid(int gid) throws ErrnoException {
        this.os.setgid(gid);
    }

    public void setpgid(int pid, int pgid) throws ErrnoException {
        this.os.setpgid(pid, pgid);
    }

    public void setregid(int rgid, int egid) throws ErrnoException {
        this.os.setregid(rgid, egid);
    }

    public void setreuid(int ruid, int euid) throws ErrnoException {
        this.os.setreuid(ruid, euid);
    }

    public int setsid() throws ErrnoException {
        return this.os.setsid();
    }

    public void setsockoptByte(FileDescriptor fd, int level, int option, int value) throws ErrnoException {
        this.os.setsockoptByte(fd, level, option, value);
    }

    public void setsockoptIfreq(FileDescriptor fd, int level, int option, String value) throws ErrnoException {
        this.os.setsockoptIfreq(fd, level, option, value);
    }

    public void setsockoptInt(FileDescriptor fd, int level, int option, int value) throws ErrnoException {
        this.os.setsockoptInt(fd, level, option, value);
    }

    public void setsockoptIpMreqn(FileDescriptor fd, int level, int option, int value) throws ErrnoException {
        this.os.setsockoptIpMreqn(fd, level, option, value);
    }

    public void setsockoptGroupReq(FileDescriptor fd, int level, int option, StructGroupReq value) throws ErrnoException {
        this.os.setsockoptGroupReq(fd, level, option, value);
    }

    public void setsockoptGroupSourceReq(FileDescriptor fd, int level, int option, StructGroupSourceReq value) throws ErrnoException {
        this.os.setsockoptGroupSourceReq(fd, level, option, value);
    }

    public void setsockoptLinger(FileDescriptor fd, int level, int option, StructLinger value) throws ErrnoException {
        this.os.setsockoptLinger(fd, level, option, value);
    }

    public void setsockoptTimeval(FileDescriptor fd, int level, int option, StructTimeval value) throws ErrnoException {
        this.os.setsockoptTimeval(fd, level, option, value);
    }

    public void setuid(int uid) throws ErrnoException {
        this.os.setuid(uid);
    }

    public void setxattr(String path, String name, byte[] value, int flags) throws ErrnoException {
        this.os.setxattr(path, name, value, flags);
    }

    public void shutdown(FileDescriptor fd, int how) throws ErrnoException {
        this.os.shutdown(fd, how);
    }

    public FileDescriptor socket(int domain, int type, int protocol) throws ErrnoException {
        return this.os.socket(domain, type, protocol);
    }

    public void socketpair(int domain, int type, int protocol, FileDescriptor fd1, FileDescriptor fd2) throws ErrnoException {
        this.os.socketpair(domain, type, protocol, fd1, fd2);
    }

    public StructStat stat(String path) throws ErrnoException {
        return this.os.stat(path);
    }

    public StructStatVfs statvfs(String path) throws ErrnoException {
        return this.os.statvfs(path);
    }

    public String strerror(int errno) {
        return this.os.strerror(errno);
    }

    public String strsignal(int signal) {
        return this.os.strsignal(signal);
    }

    public void symlink(String oldPath, String newPath) throws ErrnoException {
        this.os.symlink(oldPath, newPath);
    }

    public long sysconf(int name) {
        return this.os.sysconf(name);
    }

    public void tcdrain(FileDescriptor fd) throws ErrnoException {
        this.os.tcdrain(fd);
    }

    public void tcsendbreak(FileDescriptor fd, int duration) throws ErrnoException {
        this.os.tcsendbreak(fd, duration);
    }

    public int umask(int mask) {
        return this.os.umask(mask);
    }

    public StructUtsname uname() {
        return this.os.uname();
    }

    public void unlink(String pathname) throws ErrnoException {
        this.os.unlink(pathname);
    }

    public void unsetenv(String name) throws ErrnoException {
        this.os.unsetenv(name);
    }

    public int waitpid(int pid, MutableInt status, int options) throws ErrnoException {
        return this.os.waitpid(pid, status, options);
    }

    public int write(FileDescriptor fd, ByteBuffer buffer) throws ErrnoException, InterruptedIOException {
        return this.os.write(fd, buffer);
    }

    public int write(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount) throws ErrnoException, InterruptedIOException {
        return this.os.write(fd, bytes, byteOffset, byteCount);
    }

    public int writev(FileDescriptor fd, Object[] buffers, int[] offsets, int[] byteCounts) throws ErrnoException, InterruptedIOException {
        return this.os.writev(fd, buffers, offsets, byteCounts);
    }
}
