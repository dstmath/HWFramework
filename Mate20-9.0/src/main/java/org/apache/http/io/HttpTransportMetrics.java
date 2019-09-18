package org.apache.http.io;

@Deprecated
public interface HttpTransportMetrics {
    long getBytesTransferred();

    void reset();
}
