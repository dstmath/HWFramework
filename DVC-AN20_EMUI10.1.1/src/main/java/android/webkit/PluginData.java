package android.webkit;

import android.annotation.UnsupportedAppUsage;
import java.io.InputStream;
import java.util.Map;

@Deprecated
public final class PluginData {
    private long mContentLength;
    private Map<String, String[]> mHeaders;
    private int mStatusCode;
    private InputStream mStream;

    @UnsupportedAppUsage
    @Deprecated
    public PluginData(InputStream stream, long length, Map<String, String[]> headers, int code) {
        this.mStream = stream;
        this.mContentLength = length;
        this.mHeaders = headers;
        this.mStatusCode = code;
    }

    @UnsupportedAppUsage
    @Deprecated
    public InputStream getInputStream() {
        return this.mStream;
    }

    @UnsupportedAppUsage
    @Deprecated
    public long getContentLength() {
        return this.mContentLength;
    }

    @UnsupportedAppUsage
    @Deprecated
    public Map<String, String[]> getHeaders() {
        return this.mHeaders;
    }

    @UnsupportedAppUsage
    @Deprecated
    public int getStatusCode() {
        return this.mStatusCode;
    }
}
