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

    @Override // com.huawei.okhttp3.ResponseBody
    public MediaType contentType() {
        String str = this.contentTypeString;
        if (str != null) {
            return MediaType.parse(str);
        }
        return null;
    }

    @Override // com.huawei.okhttp3.ResponseBody
    public long contentLength() {
        return this.contentLength;
    }

    @Override // com.huawei.okhttp3.ResponseBody
    public BufferedSource source() {
        return this.source;
    }
}
