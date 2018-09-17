package android.webkit;

import java.io.InputStream;
import java.util.Map;

@Deprecated
public final class PluginData {
    private long mContentLength;
    private Map<String, String[]> mHeaders;
    private int mStatusCode;
    private InputStream mStream;

    @Deprecated
    public PluginData(InputStream stream, long length, Map<String, String[]> headers, int code) {
        this.mStream = stream;
        this.mContentLength = length;
        this.mHeaders = headers;
        this.mStatusCode = code;
    }

    @Deprecated
    public InputStream getInputStream() {
        return this.mStream;
    }

    @Deprecated
    public long getContentLength() {
        return this.mContentLength;
    }

    @Deprecated
    public Map<String, String[]> getHeaders() {
        return this.mHeaders;
    }

    @Deprecated
    public int getStatusCode() {
        return this.mStatusCode;
    }
}
