package java.nio.channels;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface ScatteringByteChannel extends ReadableByteChannel {
    long read(ByteBuffer[] byteBufferArr) throws IOException;

    long read(ByteBuffer[] byteBufferArr, int i, int i2) throws IOException;
}
