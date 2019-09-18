package com.android.okhttp.internal.http;

import com.android.okhttp.Headers;
import com.android.okhttp.MediaType;
import com.android.okhttp.ResponseBody;
import com.android.okhttp.okio.BufferedSource;

public final class RealResponseBody extends ResponseBody {
    private final Headers headers;
    private final BufferedSource source;

    public RealResponseBody(Headers headers2, BufferedSource source2) {
        this.headers = headers2;
        this.source = source2;
    }

    public MediaType contentType() {
        String contentType = this.headers.get("Content-Type");
        if (contentType != null) {
            return MediaType.parse(contentType);
        }
        return null;
    }

    public long contentLength() {
        return OkHeaders.contentLength(this.headers);
    }

    public BufferedSource source() {
        return this.source;
    }
}
