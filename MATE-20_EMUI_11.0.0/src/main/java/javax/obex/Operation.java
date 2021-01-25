package javax.obex;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Operation {
    void abort() throws IOException;

    void close() throws IOException;

    String getEncoding();

    int getHeaderLength();

    long getLength();

    int getMaxPacketSize();

    HeaderSet getReceivedHeader() throws IOException;

    int getResponseCode() throws IOException;

    String getType();

    void noBodyHeader();

    DataInputStream openDataInputStream() throws IOException;

    DataOutputStream openDataOutputStream() throws IOException;

    InputStream openInputStream() throws IOException;

    OutputStream openOutputStream() throws IOException;

    void sendHeaders(HeaderSet headerSet) throws IOException;
}
