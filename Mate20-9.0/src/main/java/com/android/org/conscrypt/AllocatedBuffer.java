package com.android.org.conscrypt;

import java.nio.ByteBuffer;

public abstract class AllocatedBuffer {
    public abstract ByteBuffer nioBuffer();

    public abstract AllocatedBuffer release();

    public abstract AllocatedBuffer retain();

    public static AllocatedBuffer wrap(final ByteBuffer buffer) {
        Preconditions.checkNotNull(buffer, "buffer");
        return new AllocatedBuffer() {
            public ByteBuffer nioBuffer() {
                return buffer;
            }

            public AllocatedBuffer retain() {
                return this;
            }

            public AllocatedBuffer release() {
                return this;
            }
        };
    }
}
