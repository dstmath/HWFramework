package org.apache.http.conn;

import java.io.IOException;

@Deprecated
public interface ConnectionReleaseTrigger {
    void abortConnection() throws IOException;

    void releaseConnection() throws IOException;
}
