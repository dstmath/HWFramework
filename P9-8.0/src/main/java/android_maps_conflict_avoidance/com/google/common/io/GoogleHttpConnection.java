package android_maps_conflict_avoidance.com.google.common.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface GoogleHttpConnection {
    void close() throws IOException;

    String getContentType() throws IOException;

    long getLength() throws IOException;

    int getResponseCode() throws IOException;

    DataInputStream openDataInputStream() throws IOException;

    DataOutputStream openDataOutputStream() throws IOException;

    void setConnectionProperty(String str, String str2) throws IOException;
}
