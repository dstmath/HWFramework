package com.android.okhttp;

import com.android.okhttp.internal.Util;
import com.android.okhttp.okio.BufferedSink;
import com.android.okhttp.okio.ByteString;
import com.android.okhttp.okio.Okio;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public abstract class RequestBody {

    /* renamed from: com.android.okhttp.RequestBody.1 */
    static class AnonymousClass1 extends RequestBody {
        final /* synthetic */ ByteString val$content;
        final /* synthetic */ MediaType val$contentType;

        AnonymousClass1(MediaType val$contentType, ByteString val$content) {
            this.val$contentType = val$contentType;
            this.val$content = val$content;
        }

        public MediaType contentType() {
            return this.val$contentType;
        }

        public long contentLength() throws IOException {
            return (long) this.val$content.size();
        }

        public void writeTo(BufferedSink sink) throws IOException {
            sink.write(this.val$content);
        }
    }

    /* renamed from: com.android.okhttp.RequestBody.2 */
    static class AnonymousClass2 extends RequestBody {
        final /* synthetic */ int val$byteCount;
        final /* synthetic */ byte[] val$content;
        final /* synthetic */ MediaType val$contentType;
        final /* synthetic */ int val$offset;

        AnonymousClass2(MediaType val$contentType, int val$byteCount, byte[] val$content, int val$offset) {
            this.val$contentType = val$contentType;
            this.val$byteCount = val$byteCount;
            this.val$content = val$content;
            this.val$offset = val$offset;
        }

        public MediaType contentType() {
            return this.val$contentType;
        }

        public long contentLength() {
            return (long) this.val$byteCount;
        }

        public void writeTo(BufferedSink sink) throws IOException {
            sink.write(this.val$content, this.val$offset, this.val$byteCount);
        }
    }

    /* renamed from: com.android.okhttp.RequestBody.3 */
    static class AnonymousClass3 extends RequestBody {
        final /* synthetic */ MediaType val$contentType;
        final /* synthetic */ File val$file;

        AnonymousClass3(MediaType val$contentType, File val$file) {
            this.val$contentType = val$contentType;
            this.val$file = val$file;
        }

        public MediaType contentType() {
            return this.val$contentType;
        }

        public long contentLength() {
            return this.val$file.length();
        }

        public void writeTo(BufferedSink sink) throws IOException {
            Closeable closeable = null;
            try {
                closeable = Okio.source(this.val$file);
                sink.writeAll(closeable);
            } finally {
                Util.closeQuietly(closeable);
            }
        }
    }

    public abstract MediaType contentType();

    public abstract void writeTo(BufferedSink bufferedSink) throws IOException;

    public long contentLength() throws IOException {
        return -1;
    }

    public static RequestBody create(MediaType contentType, String content) {
        Charset charset = Util.UTF_8;
        if (contentType != null) {
            charset = contentType.charset();
            if (charset == null) {
                charset = Util.UTF_8;
                contentType = MediaType.parse(contentType + "; charset=utf-8");
            }
        }
        return create(contentType, content.getBytes(charset));
    }

    public static RequestBody create(MediaType contentType, ByteString content) {
        return new AnonymousClass1(contentType, content);
    }

    public static RequestBody create(MediaType contentType, byte[] content) {
        return create(contentType, content, 0, content.length);
    }

    public static RequestBody create(MediaType contentType, byte[] content, int offset, int byteCount) {
        if (content == null) {
            throw new NullPointerException("content == null");
        }
        Util.checkOffsetAndCount((long) content.length, (long) offset, (long) byteCount);
        return new AnonymousClass2(contentType, byteCount, content, offset);
    }

    public static RequestBody create(MediaType contentType, File file) {
        if (file != null) {
            return new AnonymousClass3(contentType, file);
        }
        throw new NullPointerException("content == null");
    }
}
