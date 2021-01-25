package com.huawei.okhttp3;

import com.huawei.okhttp3.internal.Util;
import com.huawei.okio.Buffer;
import com.huawei.okio.BufferedSource;
import com.huawei.okio.ByteString;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import javax.annotation.Nullable;

public abstract class ResponseBody implements Closeable {
    @Nullable
    private Reader reader;

    public abstract long contentLength();

    @Nullable
    public abstract MediaType contentType();

    public abstract BufferedSource source();

    public final InputStream byteStream() {
        return source().inputStream();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x004c, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x004d, code lost:
        if (r2 != null) goto L_0x004f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x004f, code lost:
        $closeResource(r3, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0052, code lost:
        throw r4;
     */
    public final byte[] bytes() throws IOException {
        long contentLength = contentLength();
        if (contentLength <= 2147483647L) {
            BufferedSource source = source();
            byte[] bytes = source.readByteArray();
            $closeResource(null, source);
            if (contentLength == -1 || contentLength == ((long) bytes.length)) {
                return bytes;
            }
            throw new IOException("Content-Length (" + contentLength + ") and stream length (" + bytes.length + ") disagree");
        }
        throw new IOException("Cannot buffer entire body for content length: " + contentLength);
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                x0.addSuppressed(th);
            }
        } else {
            x1.close();
        }
    }

    public final Reader charStream() {
        Reader r = this.reader;
        if (r != null) {
            return r;
        }
        BomAwareReader bomAwareReader = new BomAwareReader(source(), charset());
        this.reader = bomAwareReader;
        return bomAwareReader;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001a, code lost:
        $closeResource(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x001d, code lost:
        throw r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0017, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0018, code lost:
        if (r0 != null) goto L_0x001a;
     */
    public final String string() throws IOException {
        BufferedSource source = source();
        String readString = source.readString(Util.bomAwareCharset(source, charset()));
        $closeResource(null, source);
        return readString;
    }

    private Charset charset() {
        MediaType contentType = contentType();
        Charset charset = StandardCharsets.UTF_8;
        return contentType != null ? contentType.charset(charset) : charset;
    }

    @Override // java.io.Closeable, java.lang.AutoCloseable
    public void close() {
        Util.closeQuietly(source());
    }

    public static ResponseBody create(@Nullable MediaType contentType, String content) {
        Charset charset = StandardCharsets.UTF_8;
        if (contentType != null && (charset = contentType.charset()) == null) {
            charset = StandardCharsets.UTF_8;
            contentType = MediaType.parse(contentType + "; charset=utf-8");
        }
        Buffer buffer = new Buffer().writeString(content, charset);
        return create(contentType, buffer.size(), buffer);
    }

    public static ResponseBody create(@Nullable MediaType contentType, byte[] content) {
        return create(contentType, (long) content.length, new Buffer().write(content));
    }

    public static ResponseBody create(@Nullable MediaType contentType, ByteString content) {
        return create(contentType, (long) content.size(), new Buffer().write(content));
    }

    public static ResponseBody create(@Nullable final MediaType contentType, final long contentLength, final BufferedSource content) {
        if (content != null) {
            return new ResponseBody() {
                /* class com.huawei.okhttp3.ResponseBody.AnonymousClass1 */

                @Override // com.huawei.okhttp3.ResponseBody
                @Nullable
                public MediaType contentType() {
                    return MediaType.this;
                }

                @Override // com.huawei.okhttp3.ResponseBody
                public long contentLength() {
                    return contentLength;
                }

                @Override // com.huawei.okhttp3.ResponseBody
                public BufferedSource source() {
                    return content;
                }
            };
        }
        throw new NullPointerException("source == null");
    }

    static final class BomAwareReader extends Reader {
        private final Charset charset;
        private boolean closed;
        @Nullable
        private Reader delegate;
        private final BufferedSource source;

        BomAwareReader(BufferedSource source2, Charset charset2) {
            this.source = source2;
            this.charset = charset2;
        }

        @Override // java.io.Reader
        public int read(char[] cbuf, int off, int len) throws IOException {
            if (!this.closed) {
                Reader delegate2 = this.delegate;
                if (delegate2 == null) {
                    InputStreamReader inputStreamReader = new InputStreamReader(this.source.inputStream(), Util.bomAwareCharset(this.source, this.charset));
                    this.delegate = inputStreamReader;
                    delegate2 = inputStreamReader;
                }
                return delegate2.read(cbuf, off, len);
            }
            throw new IOException("Stream closed");
        }

        @Override // java.io.Reader, java.io.Closeable, java.lang.AutoCloseable
        public void close() throws IOException {
            this.closed = true;
            Reader reader = this.delegate;
            if (reader != null) {
                reader.close();
            } else {
                this.source.close();
            }
        }
    }
}
