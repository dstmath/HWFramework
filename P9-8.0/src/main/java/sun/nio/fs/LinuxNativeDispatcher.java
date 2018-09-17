package sun.nio.fs;

class LinuxNativeDispatcher extends UnixNativeDispatcher {
    static native void endmntent(long j) throws UnixException;

    private static native int fgetxattr0(int i, long j, long j2, int i2) throws UnixException;

    static native int flistxattr(int i, long j, int i2) throws UnixException;

    private static native void fremovexattr0(int i, long j) throws UnixException;

    private static native void fsetxattr0(int i, long j, long j2, int i2) throws UnixException;

    static native int getmntent(long j, UnixMountEntry unixMountEntry) throws UnixException;

    private static native void init();

    private static native long setmntent0(long j, long j2) throws UnixException;

    private LinuxNativeDispatcher() {
    }

    static long setmntent(byte[] filename, byte[] type) throws UnixException {
        NativeBuffer pathBuffer = NativeBuffers.asNativeBuffer(filename);
        NativeBuffer typeBuffer = NativeBuffers.asNativeBuffer(type);
        try {
            long j = setmntent0(pathBuffer.address(), typeBuffer.address());
            return j;
        } finally {
            typeBuffer.release();
            pathBuffer.release();
        }
    }

    static int fgetxattr(int filedes, byte[] name, long valueAddress, int valueLen) throws UnixException {
        NativeBuffer buffer = NativeBuffers.asNativeBuffer(name);
        try {
            int fgetxattr0 = fgetxattr0(filedes, buffer.address(), valueAddress, valueLen);
            return fgetxattr0;
        } finally {
            buffer.release();
        }
    }

    static void fsetxattr(int filedes, byte[] name, long valueAddress, int valueLen) throws UnixException {
        NativeBuffer buffer = NativeBuffers.asNativeBuffer(name);
        try {
            fsetxattr0(filedes, buffer.address(), valueAddress, valueLen);
        } finally {
            buffer.release();
        }
    }

    static void fremovexattr(int filedes, byte[] name) throws UnixException {
        NativeBuffer buffer = NativeBuffers.asNativeBuffer(name);
        try {
            fremovexattr0(filedes, buffer.address());
        } finally {
            buffer.release();
        }
    }

    static {
        init();
    }
}
