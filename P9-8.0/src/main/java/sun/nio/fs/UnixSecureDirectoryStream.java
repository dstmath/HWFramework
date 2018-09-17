package sun.nio.fs;

import dalvik.system.CloseGuard;
import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.ClosedDirectoryStreamException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.LinkOption;
import java.nio.file.NotDirectoryException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;
import java.nio.file.SecureDirectoryStream;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

class UnixSecureDirectoryStream implements SecureDirectoryStream<Path> {
    private final int dfd;
    private final UnixDirectoryStream ds;
    private final CloseGuard guard = CloseGuard.get();

    private class BasicFileAttributeViewImpl implements BasicFileAttributeView {
        final UnixPath file;
        final boolean followLinks;

        BasicFileAttributeViewImpl(UnixPath file, boolean followLinks) {
            this.file = file;
            this.followLinks = followLinks;
        }

        int open() throws IOException {
            int oflags = UnixConstants.O_RDONLY;
            if (!this.followLinks) {
                oflags |= UnixConstants.O_NOFOLLOW;
            }
            try {
                return UnixNativeDispatcher.openat(UnixSecureDirectoryStream.this.dfd, this.file.asByteArray(), oflags, 0);
            } catch (UnixException x) {
                x.rethrowAsIOException(this.file);
                return -1;
            }
        }

        private void checkWriteAccess() {
            if (System.getSecurityManager() == null) {
                return;
            }
            if (this.file == null) {
                UnixSecureDirectoryStream.this.ds.directory().checkWrite();
            } else {
                UnixSecureDirectoryStream.this.ds.directory().resolve(this.file).checkWrite();
            }
        }

        public String name() {
            return "basic";
        }

        public BasicFileAttributes readAttributes() throws IOException {
            UnixSecureDirectoryStream.this.ds.readLock().lock();
            try {
                if (UnixSecureDirectoryStream.this.ds.isOpen()) {
                    UnixFileAttributes attrs;
                    if (System.getSecurityManager() != null) {
                        if (this.file == null) {
                            UnixSecureDirectoryStream.this.ds.directory().checkRead();
                        } else {
                            UnixSecureDirectoryStream.this.ds.directory().resolve(this.file).checkRead();
                        }
                    }
                    if (this.file == null) {
                        attrs = UnixFileAttributes.get(UnixSecureDirectoryStream.this.dfd);
                    } else {
                        attrs = UnixFileAttributes.get(UnixSecureDirectoryStream.this.dfd, this.file, this.followLinks);
                    }
                    BasicFileAttributes asBasicFileAttributes = attrs.asBasicFileAttributes();
                    return asBasicFileAttributes;
                }
                throw new ClosedDirectoryStreamException();
            } catch (UnixException x) {
                x.rethrowAsIOException(this.file);
                return null;
            } finally {
                UnixSecureDirectoryStream.this.ds.readLock().unlock();
                return null;
            }
        }

        public void setTimes(FileTime lastModifiedTime, FileTime lastAccessTime, FileTime createTime) throws IOException {
            checkWriteAccess();
            UnixSecureDirectoryStream.this.ds.readLock().lock();
            try {
                if (UnixSecureDirectoryStream.this.ds.isOpen()) {
                    int fd = this.file == null ? UnixSecureDirectoryStream.this.dfd : open();
                    if (lastModifiedTime == null || lastAccessTime == null) {
                        try {
                            UnixFileAttributes attrs = UnixFileAttributes.get(fd);
                            if (lastModifiedTime == null) {
                                lastModifiedTime = attrs.lastModifiedTime();
                            }
                            if (lastAccessTime == null) {
                                lastAccessTime = attrs.lastAccessTime();
                            }
                        } catch (UnixException x) {
                            x.rethrowAsIOException(this.file);
                        } catch (Throwable th) {
                            if (this.file != null) {
                                UnixNativeDispatcher.close(fd);
                            }
                        }
                    }
                    try {
                        UnixNativeDispatcher.futimes(fd, lastAccessTime.to(TimeUnit.MICROSECONDS), lastModifiedTime.to(TimeUnit.MICROSECONDS));
                    } catch (UnixException x2) {
                        x2.rethrowAsIOException(this.file);
                    }
                    if (this.file != null) {
                        UnixNativeDispatcher.close(fd);
                    }
                    UnixSecureDirectoryStream.this.ds.readLock().unlock();
                    return;
                }
                throw new ClosedDirectoryStreamException();
            } catch (Throwable th2) {
                UnixSecureDirectoryStream.this.ds.readLock().unlock();
            }
        }
    }

    private class PosixFileAttributeViewImpl extends BasicFileAttributeViewImpl implements PosixFileAttributeView {
        PosixFileAttributeViewImpl(UnixPath file, boolean followLinks) {
            super(file, followLinks);
        }

        private void checkWriteAndUserAccess() {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                checkWriteAccess();
                sm.checkPermission(new RuntimePermission("accessUserInformation"));
            }
        }

        public String name() {
            return "posix";
        }

        public PosixFileAttributes readAttributes() throws IOException {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                if (this.file == null) {
                    UnixSecureDirectoryStream.this.ds.directory().checkRead();
                } else {
                    UnixSecureDirectoryStream.this.ds.directory().resolve(this.file).checkRead();
                }
                sm.checkPermission(new RuntimePermission("accessUserInformation"));
            }
            UnixSecureDirectoryStream.this.ds.readLock().lock();
            try {
                if (UnixSecureDirectoryStream.this.ds.isOpen()) {
                    UnixFileAttributes attrs;
                    if (this.file == null) {
                        attrs = UnixFileAttributes.get(UnixSecureDirectoryStream.this.dfd);
                    } else {
                        attrs = UnixFileAttributes.get(UnixSecureDirectoryStream.this.dfd, this.file, this.followLinks);
                    }
                    UnixSecureDirectoryStream.this.ds.readLock().unlock();
                    return attrs;
                }
                throw new ClosedDirectoryStreamException();
            } catch (UnixException x) {
                x.rethrowAsIOException(this.file);
                UnixSecureDirectoryStream.this.ds.readLock().unlock();
                return null;
            } catch (Throwable th) {
                UnixSecureDirectoryStream.this.ds.readLock().unlock();
                throw th;
            }
        }

        public void setPermissions(Set<PosixFilePermission> perms) throws IOException {
            checkWriteAndUserAccess();
            UnixSecureDirectoryStream.this.ds.readLock().lock();
            int fd;
            try {
                if (UnixSecureDirectoryStream.this.ds.isOpen()) {
                    fd = this.file == null ? UnixSecureDirectoryStream.this.dfd : open();
                    UnixNativeDispatcher.fchmod(fd, UnixFileModeAttribute.toUnixMode(perms));
                    if (this.file != null && fd >= 0) {
                        UnixNativeDispatcher.close(fd);
                    }
                    UnixSecureDirectoryStream.this.ds.readLock().unlock();
                    return;
                }
                throw new ClosedDirectoryStreamException();
            } catch (UnixException x) {
                x.rethrowAsIOException(this.file);
                if (this.file != null && fd >= 0) {
                    UnixNativeDispatcher.close(fd);
                }
            } catch (Throwable th) {
                UnixSecureDirectoryStream.this.ds.readLock().unlock();
            }
        }

        private void setOwners(int uid, int gid) throws IOException {
            checkWriteAndUserAccess();
            UnixSecureDirectoryStream.this.ds.readLock().lock();
            int fd;
            try {
                if (UnixSecureDirectoryStream.this.ds.isOpen()) {
                    fd = this.file == null ? UnixSecureDirectoryStream.this.dfd : open();
                    UnixNativeDispatcher.fchown(fd, uid, gid);
                    if (this.file != null && fd >= 0) {
                        UnixNativeDispatcher.close(fd);
                    }
                    UnixSecureDirectoryStream.this.ds.readLock().unlock();
                    return;
                }
                throw new ClosedDirectoryStreamException();
            } catch (UnixException x) {
                x.rethrowAsIOException(this.file);
                if (this.file != null && fd >= 0) {
                    UnixNativeDispatcher.close(fd);
                }
            } catch (Throwable th) {
                UnixSecureDirectoryStream.this.ds.readLock().unlock();
            }
        }

        public UserPrincipal getOwner() throws IOException {
            return readAttributes().owner();
        }

        public void setOwner(UserPrincipal owner) throws IOException {
            if (!(owner instanceof User)) {
                throw new ProviderMismatchException();
            } else if (owner instanceof Group) {
                throw new IOException("'owner' parameter can't be a group");
            } else {
                setOwners(((User) owner).uid(), -1);
            }
        }

        public void setGroup(GroupPrincipal group) throws IOException {
            if (group instanceof Group) {
                setOwners(-1, ((Group) group).gid());
                return;
            }
            throw new ProviderMismatchException();
        }
    }

    UnixSecureDirectoryStream(UnixPath dir, long dp, int dfd, Filter<? super Path> filter) {
        this.ds = new UnixDirectoryStream(dir, dp, filter);
        this.dfd = dfd;
        if (dfd != -1) {
            this.guard.open("close");
        }
    }

    public void close() throws IOException {
        this.ds.writeLock().lock();
        try {
            if (this.ds.closeImpl()) {
                UnixNativeDispatcher.close(this.dfd);
            }
            this.ds.writeLock().unlock();
            this.guard.close();
        } catch (Throwable th) {
            this.ds.writeLock().unlock();
        }
    }

    public Iterator<Path> iterator() {
        return this.ds.iterator(this);
    }

    private UnixPath getName(Path obj) {
        if (obj == null) {
            throw new NullPointerException();
        } else if (obj instanceof UnixPath) {
            return (UnixPath) obj;
        } else {
            throw new ProviderMismatchException();
        }
    }

    public SecureDirectoryStream<Path> newDirectoryStream(Path obj, LinkOption... options) throws IOException {
        UnixPath file = getName(obj);
        UnixPath child = this.ds.directory().resolve((Path) file);
        boolean followLinks = Util.followLinks(options);
        if (System.getSecurityManager() != null) {
            child.checkRead();
        }
        this.ds.readLock().lock();
        try {
            if (this.ds.isOpen()) {
                int newdfd2 = -1;
                long ptr = 0;
                int flags = UnixConstants.O_RDONLY;
                if (!followLinks) {
                    flags |= UnixConstants.O_NOFOLLOW;
                }
                int newdfd1 = UnixNativeDispatcher.openat(this.dfd, file.asByteArray(), flags, 0);
                newdfd2 = UnixNativeDispatcher.dup(newdfd1);
                ptr = UnixNativeDispatcher.fdopendir(newdfd1);
                SecureDirectoryStream unixSecureDirectoryStream = new UnixSecureDirectoryStream(child, ptr, newdfd2, null);
                this.ds.readLock().unlock();
                return unixSecureDirectoryStream;
            }
            throw new ClosedDirectoryStreamException();
        } catch (UnixException x) {
            if (-1 != -1) {
                UnixNativeDispatcher.close(-1);
            }
            if (-1 != -1) {
                UnixNativeDispatcher.close(-1);
            }
            if (x.errno() == UnixConstants.ENOTDIR) {
                throw new NotDirectoryException(file.toString());
            }
            x.rethrowAsIOException(file);
        } catch (Throwable th) {
            this.ds.readLock().unlock();
        }
    }

    public SeekableByteChannel newByteChannel(Path obj, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
        UnixPath file = getName(obj);
        int mode = UnixFileModeAttribute.toUnixMode(UnixFileModeAttribute.ALL_READWRITE, attrs);
        String pathToCheck = this.ds.directory().resolve((Path) file).getPathForPermissionCheck();
        this.ds.readLock().lock();
        SeekableByteChannel newFileChannel;
        try {
            if (this.ds.isOpen()) {
                newFileChannel = UnixChannelFactory.newFileChannel(this.dfd, file, pathToCheck, options, mode);
                return newFileChannel;
            }
            throw new ClosedDirectoryStreamException();
        } catch (UnixException x) {
            x.rethrowAsIOException(file);
            newFileChannel = null;
            return newFileChannel;
        } finally {
            this.ds.readLock().unlock();
        }
    }

    private void implDelete(Path obj, boolean haveFlags, int flags) throws IOException {
        UnixPath file = getName(obj);
        if (System.getSecurityManager() != null) {
            this.ds.directory().resolve((Path) file).checkDelete();
        }
        this.ds.readLock().lock();
        if (this.ds.isOpen()) {
            if (!haveFlags) {
                UnixFileAttributes attrs = null;
                try {
                    attrs = UnixFileAttributes.get(this.dfd, file, false);
                } catch (UnixException x) {
                    if ((flags & 512) == 0 || !(x.errno() == UnixConstants.EEXIST || x.errno() == UnixConstants.ENOTEMPTY)) {
                        x.rethrowAsIOException(file);
                    } else {
                        throw new DirectoryNotEmptyException(null);
                    }
                } catch (UnixException x2) {
                    x2.rethrowAsIOException(file);
                } catch (Throwable th) {
                    this.ds.readLock().unlock();
                }
                flags = attrs.isDirectory() ? 512 : 0;
            }
            UnixNativeDispatcher.unlinkat(this.dfd, file.asByteArray(), flags);
            this.ds.readLock().unlock();
            return;
        }
        throw new ClosedDirectoryStreamException();
    }

    public void deleteFile(Path file) throws IOException {
        implDelete(file, true, 0);
    }

    public void deleteDirectory(Path dir) throws IOException {
        implDelete(dir, true, 512);
    }

    public void move(Path fromObj, SecureDirectoryStream<Path> dir, Path toObj) throws IOException {
        Path from = getName(fromObj);
        Path to = getName(toObj);
        if (dir == null) {
            throw new NullPointerException();
        } else if (dir instanceof UnixSecureDirectoryStream) {
            UnixSecureDirectoryStream that = (UnixSecureDirectoryStream) dir;
            if (System.getSecurityManager() != null) {
                this.ds.directory().resolve(from).checkWrite();
                that.ds.directory().resolve(to).checkWrite();
            }
            this.ds.readLock().lock();
            try {
                that.ds.readLock().lock();
                if (this.ds.isOpen() && (that.ds.isOpen() ^ 1) == 0) {
                    UnixNativeDispatcher.renameat(this.dfd, from.asByteArray(), that.dfd, to.asByteArray());
                    that.ds.readLock().unlock();
                    this.ds.readLock().unlock();
                    return;
                }
                throw new ClosedDirectoryStreamException();
            } catch (UnixException x) {
                if (x.errno() == UnixConstants.EXDEV) {
                    throw new AtomicMoveNotSupportedException(from.toString(), to.toString(), x.errorString());
                }
                x.rethrowAsIOException(from, to);
            } catch (Throwable th) {
                this.ds.readLock().unlock();
            }
        } else {
            throw new ProviderMismatchException();
        }
    }

    private <V extends FileAttributeView> V getFileAttributeViewImpl(UnixPath file, Class<V> type, boolean followLinks) {
        if (type == null) {
            throw new NullPointerException();
        }
        Class<?> c = type;
        if (type == BasicFileAttributeView.class) {
            return new BasicFileAttributeViewImpl(file, followLinks);
        }
        if (type == PosixFileAttributeView.class || type == FileOwnerAttributeView.class) {
            return new PosixFileAttributeViewImpl(file, followLinks);
        }
        return (FileAttributeView) null;
    }

    public <V extends FileAttributeView> V getFileAttributeView(Class<V> type) {
        return getFileAttributeViewImpl(null, type, false);
    }

    public <V extends FileAttributeView> V getFileAttributeView(Path obj, Class<V> type, LinkOption... options) {
        return getFileAttributeViewImpl(getName(obj), type, Util.followLinks(options));
    }

    protected void finalize() throws IOException {
        if (this.guard != null) {
            this.guard.warnIfOpen();
        }
        close();
    }
}
