package java.security.cert;

import java.io.IOException;
import java.io.OutputStream;

public interface Extension {
    void encode(OutputStream outputStream) throws IOException;

    String getId();

    byte[] getValue();

    boolean isCritical();
}
