package sun.nio.fs;

import android.system.OsConstants;

class UnixConstants {
    static final int AT_REMOVEDIR = 512;
    static final int AT_SYMLINK_NOFOLLOW = 256;
    static final int EACCES = OsConstants.EACCES;
    static final int EAGAIN = OsConstants.EAGAIN;
    static final int EEXIST = OsConstants.EEXIST;
    static final int EINVAL = OsConstants.EINVAL;
    static final int EISDIR = OsConstants.EISDIR;
    static final int ELOOP = OsConstants.ELOOP;
    static final int EMFILE = OsConstants.EMFILE;
    static final int ENODATA = OsConstants.ENODATA;
    static final int ENOENT = OsConstants.ENOENT;
    static final int ENOSPC = OsConstants.ENOSPC;
    static final int ENOSYS = OsConstants.ENOSYS;
    static final int ENOTDIR = OsConstants.ENOTDIR;
    static final int ENOTEMPTY = OsConstants.ENOTEMPTY;
    static final int ERANGE = OsConstants.ERANGE;
    static final int EROFS = OsConstants.EROFS;
    static final int EXDEV = OsConstants.EXDEV;
    static final int F_OK = OsConstants.F_OK;
    static final int O_APPEND = OsConstants.O_APPEND;
    static final int O_CREAT = OsConstants.O_CREAT;
    static final int O_DSYNC = OsConstants.O_DSYNC;
    static final int O_EXCL = OsConstants.O_EXCL;
    static final int O_NOFOLLOW = OsConstants.O_NOFOLLOW;
    static final int O_RDONLY = OsConstants.O_RDONLY;
    static final int O_RDWR = OsConstants.O_RDWR;
    static final int O_SYNC = OsConstants.O_SYNC;
    static final int O_TRUNC = OsConstants.O_TRUNC;
    static final int O_WRONLY = OsConstants.O_WRONLY;
    static final int R_OK = OsConstants.R_OK;
    static final int S_IAMB = get_S_IAMB();
    static final int S_IFBLK = OsConstants.S_IFBLK;
    static final int S_IFCHR = OsConstants.S_IFCHR;
    static final int S_IFDIR = OsConstants.S_IFDIR;
    static final int S_IFIFO = OsConstants.S_IFIFO;
    static final int S_IFLNK = OsConstants.S_IFLNK;
    static final int S_IFMT = OsConstants.S_IFMT;
    static final int S_IFREG = OsConstants.S_IFREG;
    static final int S_IRGRP = OsConstants.S_IRGRP;
    static final int S_IROTH = OsConstants.S_IROTH;
    static final int S_IRUSR = OsConstants.S_IRUSR;
    static final int S_IWGRP = OsConstants.S_IWGRP;
    static final int S_IWOTH = OsConstants.S_IWOTH;
    static final int S_IWUSR = OsConstants.S_IWUSR;
    static final int S_IXGRP = OsConstants.S_IXGRP;
    static final int S_IXOTH = OsConstants.S_IXOTH;
    static final int S_IXUSR = OsConstants.S_IXUSR;
    static final int W_OK = OsConstants.W_OK;
    static final int X_OK = OsConstants.X_OK;

    private UnixConstants() {
    }

    private static int get_S_IAMB() {
        return (((((((OsConstants.S_IRUSR | OsConstants.S_IWUSR) | OsConstants.S_IXUSR) | OsConstants.S_IRGRP) | OsConstants.S_IWGRP) | OsConstants.S_IXGRP) | OsConstants.S_IROTH) | OsConstants.S_IWOTH) | OsConstants.S_IXOTH;
    }
}
