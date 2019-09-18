package com.android.org.conscrypt;

import java.nio.ByteBuffer;

public abstract class BufferAllocator {
    private static final BufferAllocator UNPOOLED = new BufferAllocator() {
        public AllocatedBuffer allocateDirectBuffer(int capacity) {
            return AllocatedBuffer.wrap(ByteBuffer.allocateDirect(capacity));
        }
    };

    public abstract AllocatedBuffer allocateDirectBuffer(int i);

    public static BufferAllocator unpooled() {
        return UNPOOLED;
    }
}
