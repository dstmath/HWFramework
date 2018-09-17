package sun.nio.fs;

class UnixNativeDispatcher {
    private static final int SUPPORTS_BIRTHTIME = 65536;
    private static final int SUPPORTS_FUTIMES = 4;
    private static final int SUPPORTS_OPENAT = 2;
    private static final int capabilities = init();

    private static native void access0(long j, int i) throws UnixException;

    private static native void chmod0(long j, int i) throws UnixException;

    private static native void chown0(long j, int i, int i2) throws UnixException;

    static native void close(int i);

    static native void closedir(long j) throws UnixException;

    static native int dup(int i) throws UnixException;

    static native void fchmod(int i, int i2) throws UnixException;

    static native void fchown(int i, int i2, int i3) throws UnixException;

    static native void fclose(long j) throws UnixException;

    static native long fdopendir(int i) throws UnixException;

    private static native long fopen0(long j, long j2) throws UnixException;

    static native long fpathconf(int i, int i2) throws UnixException;

    static native void fstat(int i, UnixFileAttributes unixFileAttributes) throws UnixException;

    private static native void fstatat0(int i, long j, int i2, UnixFileAttributes unixFileAttributes) throws UnixException;

    static native void futimes(int i, long j, long j2) throws UnixException;

    static native byte[] getcwd();

    static native byte[] getgrgid(int i) throws UnixException;

    private static native int getgrnam0(long j) throws UnixException;

    private static native int getpwnam0(long j) throws UnixException;

    static native byte[] getpwuid(int i) throws UnixException;

    private static native int init();

    private static native void lchown0(long j, int i, int i2) throws UnixException;

    private static native void link0(long j, long j2) throws UnixException;

    private static native void lstat0(long j, UnixFileAttributes unixFileAttributes) throws UnixException;

    private static native void mkdir0(long j, int i) throws UnixException;

    private static native void mknod0(long j, int i, long j2) throws UnixException;

    private static native int open0(long j, int i, int i2) throws UnixException;

    private static native int openat0(int i, long j, int i2, int i3) throws UnixException;

    private static native long opendir0(long j) throws UnixException;

    private static native long pathconf0(long j, int i) throws UnixException;

    static native int read(int i, long j, int i2) throws UnixException;

    static native byte[] readdir(long j) throws UnixException;

    private static native byte[] readlink0(long j) throws UnixException;

    private static native byte[] realpath0(long j) throws UnixException;

    private static native void rename0(long j, long j2) throws UnixException;

    private static native void renameat0(int i, long j, int i2, long j2) throws UnixException;

    private static native void rmdir0(long j) throws UnixException;

    private static native void stat0(long j, UnixFileAttributes unixFileAttributes) throws UnixException;

    private static native void statvfs0(long j, UnixFileStoreAttributes unixFileStoreAttributes) throws UnixException;

    static native byte[] strerror(int i);

    private static native void symlink0(long j, long j2) throws UnixException;

    private static native void unlink0(long j) throws UnixException;

    private static native void unlinkat0(int i, long j, int i2) throws UnixException;

    private static native void utimes0(long j, long j2, long j3) throws UnixException;

    static native int write(int i, long j, int i2) throws UnixException;

    protected UnixNativeDispatcher() {
    }

    private static NativeBuffer copyToNativeBuffer(UnixPath path) {
        byte[] cstr = path.getByteArrayForSysCalls();
        int size = cstr.length + 1;
        NativeBuffer buffer = NativeBuffers.getNativeBufferFromCache(size);
        if (buffer == null) {
            buffer = NativeBuffers.allocNativeBuffer(size);
        } else if (buffer.owner() == path) {
            return buffer;
        }
        NativeBuffers.copyCStringToNativeBuffer(cstr, buffer);
        buffer.setOwner(path);
        return buffer;
    }

    static int open(UnixPath path, int flags, int mode) throws UnixException {
        NativeBuffer buffer = copyToNativeBuffer(path);
        try {
            int open0 = open0(buffer.address(), flags, mode);
            return open0;
        } finally {
            buffer.release();
        }
    }

    static int openat(int dfd, byte[] path, int flags, int mode) throws UnixException {
        NativeBuffer buffer = NativeBuffers.asNativeBuffer(path);
        try {
            int openat0 = openat0(dfd, buffer.address(), flags, mode);
            return openat0;
        } finally {
            buffer.release();
        }
    }

    static long fopen(UnixPath filename, String mode) throws UnixException {
        NativeBuffer pathBuffer = copyToNativeBuffer(filename);
        NativeBuffer modeBuffer = NativeBuffers.asNativeBuffer(Util.toBytes(mode));
        try {
            long fopen0 = fopen0(pathBuffer.address(), modeBuffer.address());
            return fopen0;
        } finally {
            modeBuffer.release();
            pathBuffer.release();
        }
    }

    static void link(UnixPath existing, UnixPath newfile) throws UnixException {
        NativeBuffer existingBuffer = copyToNativeBuffer(existing);
        NativeBuffer newBuffer = copyToNativeBuffer(newfile);
        try {
            link0(existingBuffer.address(), newBuffer.address());
        } finally {
            newBuffer.release();
            existingBuffer.release();
        }
    }

    static void unlink(UnixPath path) throws UnixException {
        NativeBuffer buffer = copyToNativeBuffer(path);
        try {
            unlink0(buffer.address());
        } finally {
            buffer.release();
        }
    }

    static void unlinkat(int dfd, byte[] path, int flag) throws UnixException {
        NativeBuffer buffer = NativeBuffers.asNativeBuffer(path);
        try {
            unlinkat0(dfd, buffer.address(), flag);
        } finally {
            buffer.release();
        }
    }

    static void mknod(UnixPath path, int mode, long dev) throws UnixException {
        NativeBuffer buffer = copyToNativeBuffer(path);
        try {
            mknod0(buffer.address(), mode, dev);
        } finally {
            buffer.release();
        }
    }

    static void rename(UnixPath from, UnixPath to) throws UnixException {
        NativeBuffer fromBuffer = copyToNativeBuffer(from);
        NativeBuffer toBuffer = copyToNativeBuffer(to);
        try {
            rename0(fromBuffer.address(), toBuffer.address());
        } finally {
            toBuffer.release();
            fromBuffer.release();
        }
    }

    static void renameat(int fromfd, byte[] from, int tofd, byte[] to) throws UnixException {
        NativeBuffer fromBuffer = NativeBuffers.asNativeBuffer(from);
        NativeBuffer toBuffer = NativeBuffers.asNativeBuffer(to);
        try {
            renameat0(fromfd, fromBuffer.address(), tofd, toBuffer.address());
        } finally {
            toBuffer.release();
            fromBuffer.release();
        }
    }

    static void mkdir(UnixPath path, int mode) throws UnixException {
        NativeBuffer buffer = copyToNativeBuffer(path);
        try {
            mkdir0(buffer.address(), mode);
        } finally {
            buffer.release();
        }
    }

    static void rmdir(UnixPath path) throws UnixException {
        NativeBuffer buffer = copyToNativeBuffer(path);
        try {
            rmdir0(buffer.address());
        } finally {
            buffer.release();
        }
    }

    static byte[] readlink(UnixPath path) throws UnixException {
        NativeBuffer buffer = copyToNativeBuffer(path);
        try {
            byte[] readlink0 = readlink0(buffer.address());
            return readlink0;
        } finally {
            buffer.release();
        }
    }

    static byte[] realpath(UnixPath path) throws UnixException {
        NativeBuffer buffer = copyToNativeBuffer(path);
        try {
            byte[] realpath0 = realpath0(buffer.address());
            return realpath0;
        } finally {
            buffer.release();
        }
    }

    static void symlink(byte[] name1, UnixPath name2) throws UnixException {
        NativeBuffer targetBuffer = NativeBuffers.asNativeBuffer(name1);
        NativeBuffer linkBuffer = copyToNativeBuffer(name2);
        try {
            symlink0(targetBuffer.address(), linkBuffer.address());
        } finally {
            linkBuffer.release();
            targetBuffer.release();
        }
    }

    static void stat(UnixPath path, UnixFileAttributes attrs) throws UnixException {
        NativeBuffer buffer = copyToNativeBuffer(path);
        try {
            stat0(buffer.address(), attrs);
        } finally {
            buffer.release();
        }
    }

    static void lstat(UnixPath path, UnixFileAttributes attrs) throws UnixException {
        NativeBuffer buffer = copyToNativeBuffer(path);
        try {
            lstat0(buffer.address(), attrs);
        } finally {
            buffer.release();
        }
    }

    static void fstatat(int dfd, byte[] path, int flag, UnixFileAttributes attrs) throws UnixException {
        NativeBuffer buffer = NativeBuffers.asNativeBuffer(path);
        try {
            fstatat0(dfd, buffer.address(), flag, attrs);
        } finally {
            buffer.release();
        }
    }

    static void chown(UnixPath path, int uid, int gid) throws UnixException {
        NativeBuffer buffer = copyToNativeBuffer(path);
        try {
            chown0(buffer.address(), uid, gid);
        } finally {
            buffer.release();
        }
    }

    static void lchown(UnixPath path, int uid, int gid) throws UnixException {
        NativeBuffer buffer = copyToNativeBuffer(path);
        try {
            lchown0(buffer.address(), uid, gid);
        } finally {
            buffer.release();
        }
    }

    static void chmod(UnixPath path, int mode) throws UnixException {
        NativeBuffer buffer = copyToNativeBuffer(path);
        try {
            chmod0(buffer.address(), mode);
        } finally {
            buffer.release();
        }
    }

    static void utimes(UnixPath path, long times0, long times1) throws UnixException {
        NativeBuffer buffer = copyToNativeBuffer(path);
        try {
            utimes0(buffer.address(), times0, times1);
        } finally {
            buffer.release();
        }
    }

    static long opendir(UnixPath path) throws UnixException {
        NativeBuffer buffer = copyToNativeBuffer(path);
        try {
            long opendir0 = opendir0(buffer.address());
            return opendir0;
        } finally {
            buffer.release();
        }
    }

    static void access(UnixPath path, int amode) throws UnixException {
        NativeBuffer buffer = copyToNativeBuffer(path);
        try {
            access0(buffer.address(), amode);
        } finally {
            buffer.release();
        }
    }

    static int getpwnam(String name) throws UnixException {
        NativeBuffer buffer = NativeBuffers.asNativeBuffer(Util.toBytes(name));
        try {
            int i = getpwnam0(buffer.address());
            return i;
        } finally {
            buffer.release();
        }
    }

    static int getgrnam(String name) throws UnixException {
        NativeBuffer buffer = NativeBuffers.asNativeBuffer(Util.toBytes(name));
        try {
            int i = getgrnam0(buffer.address());
            return i;
        } finally {
            buffer.release();
        }
    }

    static void statvfs(UnixPath path, UnixFileStoreAttributes attrs) throws UnixException {
        NativeBuffer buffer = copyToNativeBuffer(path);
        try {
            statvfs0(buffer.address(), attrs);
        } finally {
            buffer.release();
        }
    }

    static long pathconf(UnixPath path, int name) throws UnixException {
        NativeBuffer buffer = copyToNativeBuffer(path);
        try {
            long pathconf0 = pathconf0(buffer.address(), name);
            return pathconf0;
        } finally {
            buffer.release();
        }
    }

    static boolean openatSupported() {
        return (capabilities & 2) != 0;
    }

    static boolean futimesSupported() {
        return (capabilities & 4) != 0;
    }

    static boolean birthtimeSupported() {
        return (capabilities & 65536) != 0;
    }
}
