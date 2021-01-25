package org.bouncycastle.mime;

import java.io.IOException;

public interface MimeMultipartContext extends MimeContext {
    MimeContext createContext(int i) throws IOException;
}
