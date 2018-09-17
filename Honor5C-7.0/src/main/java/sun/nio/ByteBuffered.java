package sun.nio;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface ByteBuffered {
    ByteBuffer getByteBuffer() throws IOException;
}
