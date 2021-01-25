package org.ksoap2.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public interface ServiceConnection {
    public static final int DEFAULT_BUFFER_SIZE = 262144;
    public static final int DEFAULT_TIMEOUT = 20000;

    void connect() throws IOException;

    void disconnect() throws IOException;

    InputStream getErrorStream();

    String getHost();

    String getPath();

    int getPort();

    int getResponseCode() throws IOException;

    List getResponseProperties() throws IOException;

    InputStream openInputStream() throws IOException;

    OutputStream openOutputStream() throws IOException;

    void setChunkedStreamingMode();

    void setFixedLengthStreamingMode(int i);

    void setRequestMethod(String str) throws IOException;

    void setRequestProperty(String str, String str2) throws IOException;
}
