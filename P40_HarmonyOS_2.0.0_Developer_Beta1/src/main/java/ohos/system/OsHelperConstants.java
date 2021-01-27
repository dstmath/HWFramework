package ohos.system;

import android.system.OsConstants;

public final class OsHelperConstants {
    public static final int AF_INET = OsConstants.AF_INET;
    public static final int AF_INET6 = OsConstants.AF_INET6;
    public static final int AF_NETLINK = OsConstants.AF_NETLINK;
    public static final int AF_PACKET = OsConstants.AF_PACKET;
    public static final int AF_UNIX = OsConstants.AF_UNIX;
    public static final int EEXIST = OsConstants.EEXIST;
    public static final int ENOENT = OsConstants.ENOENT;
    public static final int ENOSPC = OsConstants.ENOSPC;
    public static final int EXDEV = OsConstants.EXDEV;
    public static final int F_DUPFD = OsConstants.F_DUPFD;
    public static final int F_DUPFD_CLOEXEC = OsConstants.F_DUPFD_CLOEXEC;
    public static final int F_GETFD = OsConstants.F_GETFD;
    public static final int F_GETFL = OsConstants.F_GETFL;
    public static final int F_GETLK = OsConstants.F_GETLK;
    public static final int F_GETLK64 = OsConstants.F_GETLK64;
    public static final int F_GETOWN = OsConstants.F_GETOWN;
    public static final int F_OK = OsConstants.F_OK;
    public static final int F_RDLCK = OsConstants.F_RDLCK;
    public static final int F_SETFD = OsConstants.F_SETFD;
    public static final int F_SETFL = OsConstants.F_SETFL;
    public static final int F_SETLK = OsConstants.F_SETLK;
    public static final int F_SETLK64 = OsConstants.F_SETLK64;
    public static final int F_SETLKW = OsConstants.F_SETLKW;
    public static final int F_SETLKW64 = OsConstants.F_SETLKW64;
    public static final int F_SETOWN = OsConstants.F_SETOWN;
    public static final int F_UNLCK = OsConstants.F_UNLCK;
    public static final int F_WRLCK = OsConstants.F_WRLCK;
    public static final int O_APPEND = OsConstants.O_APPEND;
    public static final int O_CLOEXEC = OsConstants.O_CLOEXEC;
    public static final int O_CREAT = OsConstants.O_CREAT;
    public static final int O_DIRECT = OsConstants.O_DIRECT;
    public static final int O_DSYNC = OsConstants.O_DSYNC;
    public static final int O_EXCL = OsConstants.O_EXCL;
    public static final int O_NOCTTY = OsConstants.O_NOCTTY;
    public static final int O_NOFOLLOW = OsConstants.O_NOFOLLOW;
    public static final int O_NONBLOCK = OsConstants.O_NONBLOCK;
    public static final int O_RDONLY = OsConstants.O_RDONLY;
    public static final int O_RDWR = OsConstants.O_RDWR;
    public static final int O_SYNC = OsConstants.O_SYNC;
    public static final int O_TRUNC = OsConstants.O_TRUNC;
    public static final int O_WRONLY = OsConstants.O_WRONLY;
    public static final int PR_SET_DUMPABLE = OsConstants.PR_SET_DUMPABLE;
    public static final int R_OK = OsConstants.R_OK;
    public static final int SEEK_CUR = OsConstants.SEEK_CUR;
    public static final int SEEK_END = OsConstants.SEEK_END;
    public static final int SEEK_SET = OsConstants.SEEK_SET;
    public static final int SOCK_CLOEXEC = OsConstants.SOCK_CLOEXEC;
    public static final int SOCK_DGRAM = OsConstants.SOCK_DGRAM;
    public static final int SOCK_NONBLOCK = OsConstants.SOCK_NONBLOCK;
    public static final int SOCK_RAW = OsConstants.SOCK_RAW;
    public static final int SOCK_SEQPACKET = OsConstants.SOCK_SEQPACKET;
    public static final int SOCK_STREAM = OsConstants.SOCK_STREAM;
    public static final int SOL_SOCKET = OsConstants.SOL_SOCKET;
    public static final int SO_SNDTIMEO = OsConstants.SO_SNDTIMEO;
    public static final int S_IFDIR = OsConstants.S_IFDIR;
    public static final int S_IFMT = OsConstants.S_IFMT;
    public static final int S_IFREG = OsConstants.S_IFREG;
    public static final int S_IRGRP = OsConstants.S_IRGRP;
    public static final int S_IROTH = OsConstants.S_IROTH;
    public static final int S_IRUSR = OsConstants.S_IRUSR;
    public static final int S_IRWXG = OsConstants.S_IRWXG;
    public static final int S_IRWXO = OsConstants.S_IRWXO;
    public static final int S_IRWXU = OsConstants.S_IRWXU;
    public static final int S_ISUID = OsConstants.S_ISUID;
    public static final int S_ISVTX = OsConstants.S_ISVTX;
    public static final int S_IWGRP = OsConstants.S_IWGRP;
    public static final int S_IWOTH = OsConstants.S_IWOTH;
    public static final int S_IWUSR = OsConstants.S_IWUSR;
    public static final int S_IXGRP = OsConstants.S_IXGRP;
    public static final int S_IXOTH = OsConstants.S_IXOTH;
    public static final int S_IXUSR = OsConstants.S_IXUSR;
    public static final int W_OK = OsConstants.W_OK;
    public static final int X_OK = OsConstants.X_OK;
    public static final int _SC_CLK_TCK = OsConstants._SC_CLK_TCK;

    private OsHelperConstants() {
    }

    public static boolean isRegularFile(int i) {
        return (i & S_IFMT) == S_IFREG;
    }

    public static boolean isDir(int i) {
        return (i & S_IFMT) == S_IFDIR;
    }
}
