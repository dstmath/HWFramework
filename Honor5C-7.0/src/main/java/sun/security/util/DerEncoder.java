package sun.security.util;

import java.io.IOException;
import java.io.OutputStream;

public interface DerEncoder {
    void derEncode(OutputStream outputStream) throws IOException;
}
