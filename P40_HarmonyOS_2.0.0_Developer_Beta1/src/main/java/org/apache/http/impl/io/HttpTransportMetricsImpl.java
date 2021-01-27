package org.apache.http.impl.io;

import org.apache.http.io.HttpTransportMetrics;

@Deprecated
public class HttpTransportMetricsImpl implements HttpTransportMetrics {
    private long bytesTransferred = 0;

    @Override // org.apache.http.io.HttpTransportMetrics
    public long getBytesTransferred() {
        return this.bytesTransferred;
    }

    public void setBytesTransferred(long count) {
        this.bytesTransferred = count;
    }

    public void incrementBytesTransferred(long count) {
        this.bytesTransferred += count;
    }

    @Override // org.apache.http.io.HttpTransportMetrics
    public void reset() {
        this.bytesTransferred = 0;
    }
}
