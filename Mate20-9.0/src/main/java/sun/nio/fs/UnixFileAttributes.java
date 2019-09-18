package sun.nio.fs;

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

class UnixFileAttributes implements PosixFileAttributes {
    private volatile GroupPrincipal group;
    private volatile UnixFileKey key;
    private volatile UserPrincipal owner;
    private long st_atime_nsec;
    private long st_atime_sec;
    private long st_birthtime_sec;
    private long st_ctime_nsec;
    private long st_ctime_sec;
    private long st_dev;
    private int st_gid;
    private long st_ino;
    private int st_mode;
    private long st_mtime_nsec;
    private long st_mtime_sec;
    private int st_nlink;
    private long st_rdev;
    private long st_size;
    private int st_uid;

    private static class UnixAsBasicFileAttributes implements BasicFileAttributes {
        private final UnixFileAttributes attrs;

        private UnixAsBasicFileAttributes(UnixFileAttributes attrs2) {
            this.attrs = attrs2;
        }

        static UnixAsBasicFileAttributes wrap(UnixFileAttributes attrs2) {
            return new UnixAsBasicFileAttributes(attrs2);
        }

        /* access modifiers changed from: package-private */
        public UnixFileAttributes unwrap() {
            return this.attrs;
        }

        public FileTime lastModifiedTime() {
            return this.attrs.lastModifiedTime();
        }

        public FileTime lastAccessTime() {
            return this.attrs.lastAccessTime();
        }

        public FileTime creationTime() {
            return this.attrs.creationTime();
        }

        public boolean isRegularFile() {
            return this.attrs.isRegularFile();
        }

        public boolean isDirectory() {
            return this.attrs.isDirectory();
        }

        public boolean isSymbolicLink() {
            return this.attrs.isSymbolicLink();
        }

        public boolean isOther() {
            return this.attrs.isOther();
        }

        public long size() {
            return this.attrs.size();
        }

        public Object fileKey() {
            return this.attrs.fileKey();
        }
    }

    private UnixFileAttributes() {
    }

    static UnixFileAttributes get(UnixPath path, boolean followLinks) throws UnixException {
        UnixFileAttributes attrs = new UnixFileAttributes();
        if (followLinks) {
            UnixNativeDispatcher.stat(path, attrs);
        } else {
            UnixNativeDispatcher.lstat(path, attrs);
        }
        return attrs;
    }

    static UnixFileAttributes get(int fd) throws UnixException {
        UnixFileAttributes attrs = new UnixFileAttributes();
        UnixNativeDispatcher.fstat(fd, attrs);
        return attrs;
    }

    static UnixFileAttributes get(int dfd, UnixPath path, boolean followLinks) throws UnixException {
        UnixFileAttributes attrs = new UnixFileAttributes();
        UnixNativeDispatcher.fstatat(dfd, path.asByteArray(), followLinks ? 0 : 256, attrs);
        return attrs;
    }

    /* access modifiers changed from: package-private */
    public boolean isSameFile(UnixFileAttributes attrs) {
        return this.st_ino == attrs.st_ino && this.st_dev == attrs.st_dev;
    }

    /* access modifiers changed from: package-private */
    public int mode() {
        return this.st_mode;
    }

    /* access modifiers changed from: package-private */
    public long ino() {
        return this.st_ino;
    }

    /* access modifiers changed from: package-private */
    public long dev() {
        return this.st_dev;
    }

    /* access modifiers changed from: package-private */
    public long rdev() {
        return this.st_rdev;
    }

    /* access modifiers changed from: package-private */
    public int nlink() {
        return this.st_nlink;
    }

    /* access modifiers changed from: package-private */
    public int uid() {
        return this.st_uid;
    }

    /* access modifiers changed from: package-private */
    public int gid() {
        return this.st_gid;
    }

    private static FileTime toFileTime(long sec, long nsec) {
        if (nsec == 0) {
            return FileTime.from(sec, TimeUnit.SECONDS);
        }
        return FileTime.from((1000000 * sec) + (nsec / 1000), TimeUnit.MICROSECONDS);
    }

    /* access modifiers changed from: package-private */
    public FileTime ctime() {
        return toFileTime(this.st_ctime_sec, this.st_ctime_nsec);
    }

    /* access modifiers changed from: package-private */
    public boolean isDevice() {
        int type = this.st_mode & UnixConstants.S_IFMT;
        return type == UnixConstants.S_IFCHR || type == UnixConstants.S_IFBLK || type == UnixConstants.S_IFIFO;
    }

    public FileTime lastModifiedTime() {
        return toFileTime(this.st_mtime_sec, this.st_mtime_nsec);
    }

    public FileTime lastAccessTime() {
        return toFileTime(this.st_atime_sec, this.st_atime_nsec);
    }

    public FileTime creationTime() {
        if (UnixNativeDispatcher.birthtimeSupported()) {
            return FileTime.from(this.st_birthtime_sec, TimeUnit.SECONDS);
        }
        return lastModifiedTime();
    }

    public boolean isRegularFile() {
        return (this.st_mode & UnixConstants.S_IFMT) == UnixConstants.S_IFREG;
    }

    public boolean isDirectory() {
        return (this.st_mode & UnixConstants.S_IFMT) == UnixConstants.S_IFDIR;
    }

    public boolean isSymbolicLink() {
        return (this.st_mode & UnixConstants.S_IFMT) == UnixConstants.S_IFLNK;
    }

    public boolean isOther() {
        int type = this.st_mode & UnixConstants.S_IFMT;
        return (type == UnixConstants.S_IFREG || type == UnixConstants.S_IFDIR || type == UnixConstants.S_IFLNK) ? false : true;
    }

    public long size() {
        return this.st_size;
    }

    public UnixFileKey fileKey() {
        if (this.key == null) {
            synchronized (this) {
                if (this.key == null) {
                    this.key = new UnixFileKey(this.st_dev, this.st_ino);
                }
            }
        }
        return this.key;
    }

    public UserPrincipal owner() {
        if (this.owner == null) {
            synchronized (this) {
                if (this.owner == null) {
                    this.owner = UnixUserPrincipals.fromUid(this.st_uid);
                }
            }
        }
        return this.owner;
    }

    public GroupPrincipal group() {
        if (this.group == null) {
            synchronized (this) {
                if (this.group == null) {
                    this.group = UnixUserPrincipals.fromGid(this.st_gid);
                }
            }
        }
        return this.group;
    }

    public Set<PosixFilePermission> permissions() {
        int bits = this.st_mode & UnixConstants.S_IAMB;
        HashSet<PosixFilePermission> perms = new HashSet<>();
        if ((UnixConstants.S_IRUSR & bits) > 0) {
            perms.add(PosixFilePermission.OWNER_READ);
        }
        if ((UnixConstants.S_IWUSR & bits) > 0) {
            perms.add(PosixFilePermission.OWNER_WRITE);
        }
        if ((UnixConstants.S_IXUSR & bits) > 0) {
            perms.add(PosixFilePermission.OWNER_EXECUTE);
        }
        if ((UnixConstants.S_IRGRP & bits) > 0) {
            perms.add(PosixFilePermission.GROUP_READ);
        }
        if ((UnixConstants.S_IWGRP & bits) > 0) {
            perms.add(PosixFilePermission.GROUP_WRITE);
        }
        if ((UnixConstants.S_IXGRP & bits) > 0) {
            perms.add(PosixFilePermission.GROUP_EXECUTE);
        }
        if ((UnixConstants.S_IROTH & bits) > 0) {
            perms.add(PosixFilePermission.OTHERS_READ);
        }
        if ((UnixConstants.S_IWOTH & bits) > 0) {
            perms.add(PosixFilePermission.OTHERS_WRITE);
        }
        if ((UnixConstants.S_IXOTH & bits) > 0) {
            perms.add(PosixFilePermission.OTHERS_EXECUTE);
        }
        return perms;
    }

    /* access modifiers changed from: package-private */
    public BasicFileAttributes asBasicFileAttributes() {
        return UnixAsBasicFileAttributes.wrap(this);
    }

    static UnixFileAttributes toUnixFileAttributes(BasicFileAttributes attrs) {
        if (attrs instanceof UnixFileAttributes) {
            return (UnixFileAttributes) attrs;
        }
        if (attrs instanceof UnixAsBasicFileAttributes) {
            return ((UnixAsBasicFileAttributes) attrs).unwrap();
        }
        return null;
    }
}
