package com.huawei.okhttp3.internal.http;

import com.huawei.okhttp3.MediaType;
import com.huawei.okhttp3.ResponseBody;
import com.huawei.okio.BufferedSource;
import javax.annotation.Nullable;

public final class RealResponseBody extends ResponseBody {
    private final long contentLength;
    @Nullable
    private final String contentTypeString;
    private final BufferedSource source;

    public RealResponseBody(@Nullable String contentTypeString2, long contentLength2, BufferedSource source2) {
        this.contentTypeString = contentTypeString2;
        this.contentLength = contentLength2;
        this.source = source2;
    }

    public MediaType contentType() {
        if (this.contentTypeString != null) {
            return MediaType.parse(this.contentTypeString);
        }
        return null;
    }

    public long contentLength() {
        return this.contentLength;
    }

    public BufferedSource source() {
        return this.source;
    }
}
