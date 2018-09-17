package javax.obex;

import java.io.IOException;

public interface BaseStream {
    boolean continueOperation(boolean z, boolean z2) throws IOException;

    void ensureNotDone() throws IOException;

    void ensureOpen() throws IOException;

    void streamClosed(boolean z) throws IOException;
}
