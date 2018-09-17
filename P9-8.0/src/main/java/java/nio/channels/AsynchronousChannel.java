package java.nio.channels;

import java.io.IOException;

public interface AsynchronousChannel extends Channel {
    void close() throws IOException;
}
