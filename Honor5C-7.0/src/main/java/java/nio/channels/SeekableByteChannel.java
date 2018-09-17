package java.nio.channels;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface SeekableByteChannel extends ByteChannel {
    long position() throws IOException;

    SeekableByteChannel position(long j) throws IOException;

    int read(ByteBuffer byteBuffer) throws IOException;

    long size() throws IOException;

    SeekableByteChannel truncate(long j) throws IOException;

    int write(ByteBuffer byteBuffer) throws IOException;
}
