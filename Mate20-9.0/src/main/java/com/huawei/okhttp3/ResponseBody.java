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
import javax.annotation.Nullable;

public abstract class ResponseBody implements Closeable {
    private Reader reader;

    static final class BomAwareReader extends Reader {
        private final Charset charset;
        private boolean closed;
        private Reader delegate;
        private final BufferedSource source;

        BomAwareReader(BufferedSource source2, Charset charset2) {
            this.source = source2;
            this.charset = charset2;
        }

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

        public void close() throws IOException {
            this.closed = true;
            if (this.delegate != null) {
                this.delegate.close();
            } else {
                this.source.close();
            }
        }
    }

    public abstract long contentLength();

    @Nullable
    public abstract MediaType contentType();

    public abstract BufferedSource source();

    public final InputStream byteStream() {
        return source().inputStream();
    }

    /* JADX INFO: finally extract failed */
    public final byte[] bytes() throws IOException {
        long contentLength = contentLength();
        if (contentLength <= 2147483647L) {
            BufferedSource source = source();
            try {
                byte[] bytes = source.readByteArray();
                Util.closeQuietly((Closeable) source);
                if (contentLength == -1 || contentLength == ((long) bytes.length)) {
                    return bytes;
                }
                throw new IOException("Content-Length (" + contentLength + ") and stream length (" + bytes.length + ") disagree");
            } catch (Throwable th) {
                Util.closeQuietly((Closeable) source);
                throw th;
            }
        } else {
            throw new IOException("Cannot buffer entire body for content length: " + contentLength);
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

    public final String string() throws IOException {
        BufferedSource source = source();
        try {
            return source.readString(Util.bomAwareCharset(source, charset()));
        } finally {
            Util.closeQuietly((Closeable) source);
        }
    }

    private Charset charset() {
        MediaType contentType = contentType();
        return contentType != null ? contentType.charset(Util.UTF_8) : Util.UTF_8;
    }

    public void close() {
        Util.closeQuietly((Closeable) source());
    }

    public static ResponseBody create(@Nullable MediaType contentType, String content) {
        Charset charset = Util.UTF_8;
        if (contentType != null) {
            charset = contentType.charset();
            if (charset == null) {
                charset = Util.UTF_8;
                contentType = MediaType.parse(contentType + "; charset=utf-8");
            }
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
                @Nullable
                public MediaType contentType() {
                    return MediaType.this;
                }

                public long contentLength() {
                    return contentLength;
                }

                public BufferedSource source() {
                    return content;
                }
            };
        }
        throw new NullPointerException("source == null");
    }
}
