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

    public static RequestBody create(final MediaType contentType, final ByteString content) {
        return new RequestBody() {
            public MediaType contentType() {
                return contentType;
            }

            public long contentLength() throws IOException {
                return (long) content.size();
            }

            public void writeTo(BufferedSink sink) throws IOException {
                sink.write(content);
            }
        };
    }

    public static RequestBody create(MediaType contentType, byte[] content) {
        return create(contentType, content, 0, content.length);
    }

    public static RequestBody create(final MediaType contentType, final byte[] content, final int offset, final int byteCount) {
        if (content == null) {
            throw new NullPointerException("content == null");
        }
        Util.checkOffsetAndCount((long) content.length, (long) offset, (long) byteCount);
        return new RequestBody() {
            public MediaType contentType() {
                return contentType;
            }

            public long contentLength() {
                return (long) byteCount;
            }

            public void writeTo(BufferedSink sink) throws IOException {
                sink.write(content, offset, byteCount);
            }
        };
    }

    public static RequestBody create(final MediaType contentType, final File file) {
        if (file != null) {
            return new RequestBody() {
                public MediaType contentType() {
                    return contentType;
                }

                public long contentLength() {
                    return file.length();
                }

                public void writeTo(BufferedSink sink) throws IOException {
                    Closeable closeable = null;
                    try {
                        closeable = Okio.source(file);
                        sink.writeAll(closeable);
                    } finally {
                        Util.closeQuietly(closeable);
                    }
                }
            };
        }
        throw new NullPointerException("content == null");
    }
}
