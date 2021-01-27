package ohos.com.sun.xml.internal.stream.util;

import java.lang.ref.SoftReference;

public class ThreadLocalBufferAllocator {
    private static final ThreadLocal<SoftReference<BufferAllocator>> TL = new ThreadLocal<>();

    public static BufferAllocator getBufferAllocator() {
        SoftReference<BufferAllocator> softReference = TL.get();
        BufferAllocator bufferAllocator = softReference != null ? softReference.get() : null;
        if (bufferAllocator != null) {
            return bufferAllocator;
        }
        BufferAllocator bufferAllocator2 = new BufferAllocator();
        TL.set(new SoftReference<>(bufferAllocator2));
        return bufferAllocator2;
    }
}
