package sun.nio.fs;

import sun.misc.Unsafe;

class NativeBuffers {
    static final /* synthetic */ boolean -assertionsDisabled = (NativeBuffers.class.desiredAssertionStatus() ^ 1);
    private static final int TEMP_BUF_POOL_SIZE = 3;
    private static ThreadLocal<NativeBuffer[]> threadLocal = new ThreadLocal();
    private static final Unsafe unsafe = Unsafe.getUnsafe();

    private NativeBuffers() {
    }

    static NativeBuffer allocNativeBuffer(int size) {
        if (size < 2048) {
            size = 2048;
        }
        return new NativeBuffer(size);
    }

    static NativeBuffer getNativeBufferFromCache(int size) {
        NativeBuffer[] buffers = (NativeBuffer[]) threadLocal.get();
        if (buffers != null) {
            int i = 0;
            while (i < 3) {
                NativeBuffer buffer = buffers[i];
                if (buffer == null || buffer.size() < size) {
                    i++;
                } else {
                    buffers[i] = null;
                    return buffer;
                }
            }
        }
        return null;
    }

    static NativeBuffer getNativeBuffer(int size) {
        NativeBuffer buffer = getNativeBufferFromCache(size);
        if (buffer == null) {
            return allocNativeBuffer(size);
        }
        buffer.setOwner(null);
        return buffer;
    }

    static void releaseNativeBuffer(NativeBuffer buffer) {
        NativeBuffer[] buffers = (NativeBuffer[]) threadLocal.get();
        if (buffers == null) {
            buffers = new NativeBuffer[3];
            buffers[0] = buffer;
            threadLocal.set(buffers);
            return;
        }
        int i;
        for (i = 0; i < 3; i++) {
            if (buffers[i] == null) {
                buffers[i] = buffer;
                return;
            }
        }
        for (i = 0; i < 3; i++) {
            NativeBuffer existing = buffers[i];
            if (existing.size() < buffer.size()) {
                existing.cleaner().clean();
                buffers[i] = buffer;
                return;
            }
        }
        buffer.cleaner().clean();
    }

    static void copyCStringToNativeBuffer(byte[] cstr, NativeBuffer buffer) {
        long len = (long) cstr.length;
        if (-assertionsDisabled || ((long) buffer.size()) >= 1 + len) {
            for (int i = 0; ((long) i) < len; i++) {
                unsafe.putByte(buffer.address() + ((long) i), cstr[i]);
            }
            unsafe.putByte(buffer.address() + len, (byte) 0);
            return;
        }
        throw new AssertionError();
    }

    static NativeBuffer asNativeBuffer(byte[] cstr) {
        NativeBuffer buffer = getNativeBuffer(cstr.length + 1);
        copyCStringToNativeBuffer(cstr, buffer);
        return buffer;
    }
}
