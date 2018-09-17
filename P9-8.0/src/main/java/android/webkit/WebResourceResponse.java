package android.webkit;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.util.Map;

public class WebResourceResponse {
    private String mEncoding;
    private boolean mImmutable;
    private InputStream mInputStream;
    private String mMimeType;
    private String mReasonPhrase;
    private Map<String, String> mResponseHeaders;
    private int mStatusCode;

    public WebResourceResponse(String mimeType, String encoding, InputStream data) {
        this.mMimeType = mimeType;
        this.mEncoding = encoding;
        setData(data);
    }

    public WebResourceResponse(String mimeType, String encoding, int statusCode, String reasonPhrase, Map<String, String> responseHeaders, InputStream data) {
        this(mimeType, encoding, data);
        setStatusCodeAndReasonPhrase(statusCode, reasonPhrase);
        setResponseHeaders(responseHeaders);
    }

    public void setMimeType(String mimeType) {
        checkImmutable();
        this.mMimeType = mimeType;
    }

    public String getMimeType() {
        return this.mMimeType;
    }

    public void setEncoding(String encoding) {
        checkImmutable();
        this.mEncoding = encoding;
    }

    public String getEncoding() {
        return this.mEncoding;
    }

    public void setStatusCodeAndReasonPhrase(int statusCode, String reasonPhrase) {
        checkImmutable();
        if (statusCode < 100) {
            throw new IllegalArgumentException("statusCode can't be less than 100.");
        } else if (statusCode > MetricsEvent.DIALOG_USER_NEED_LOCKSCREEN) {
            throw new IllegalArgumentException("statusCode can't be greater than 599.");
        } else if (statusCode > 299 && statusCode < 400) {
            throw new IllegalArgumentException("statusCode can't be in the [300, 399] range.");
        } else if (reasonPhrase == null) {
            throw new IllegalArgumentException("reasonPhrase can't be null.");
        } else if (reasonPhrase.trim().isEmpty()) {
            throw new IllegalArgumentException("reasonPhrase can't be empty.");
        } else {
            for (int i = 0; i < reasonPhrase.length(); i++) {
                if (reasonPhrase.charAt(i) > 127) {
                    throw new IllegalArgumentException("reasonPhrase can't contain non-ASCII characters.");
                }
            }
            this.mStatusCode = statusCode;
            this.mReasonPhrase = reasonPhrase;
        }
    }

    public int getStatusCode() {
        return this.mStatusCode;
    }

    public String getReasonPhrase() {
        return this.mReasonPhrase;
    }

    public void setResponseHeaders(Map<String, String> headers) {
        checkImmutable();
        this.mResponseHeaders = headers;
    }

    public Map<String, String> getResponseHeaders() {
        return this.mResponseHeaders;
    }

    public void setData(InputStream data) {
        checkImmutable();
        if (data == null || !StringBufferInputStream.class.isAssignableFrom(data.getClass())) {
            this.mInputStream = data;
            return;
        }
        throw new IllegalArgumentException("StringBufferInputStream is deprecated and must not be passed to a WebResourceResponse");
    }

    public InputStream getData() {
        return this.mInputStream;
    }

    public WebResourceResponse(boolean immutable, String mimeType, String encoding, int statusCode, String reasonPhrase, Map<String, String> responseHeaders, InputStream data) {
        this.mImmutable = immutable;
        this.mMimeType = mimeType;
        this.mEncoding = encoding;
        this.mStatusCode = statusCode;
        this.mReasonPhrase = reasonPhrase;
        this.mResponseHeaders = responseHeaders;
        this.mInputStream = data;
    }

    private void checkImmutable() {
        if (this.mImmutable) {
            throw new IllegalStateException("This WebResourceResponse instance is immutable");
        }
    }
}
