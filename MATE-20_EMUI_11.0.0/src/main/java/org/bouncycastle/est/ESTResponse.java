package org.bouncycastle.est;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import org.bouncycastle.est.HttpUtil;
import org.bouncycastle.util.Properties;
import org.bouncycastle.util.Strings;

public class ESTResponse {
    private static final Long ZERO = 0L;
    private String HttpVersion;
    private Long absoluteReadLimit;
    private Long contentLength;
    private final HttpUtil.Headers headers;
    private InputStream inputStream;
    private final byte[] lineBuffer;
    private final ESTRequest originalRequest;
    private long read = 0;
    private final Source source;
    private int statusCode;
    private String statusMessage;

    private class PrintingInputStream extends InputStream {
        private final InputStream src;

        private PrintingInputStream(InputStream inputStream) {
            this.src = inputStream;
        }

        @Override // java.io.InputStream
        public int available() throws IOException {
            return this.src.available();
        }

        @Override // java.io.InputStream, java.io.Closeable, java.lang.AutoCloseable
        public void close() throws IOException {
            this.src.close();
        }

        @Override // java.io.InputStream
        public int read() throws IOException {
            int read = this.src.read();
            System.out.print(String.valueOf((char) read));
            return read;
        }
    }

    public ESTResponse(ESTRequest eSTRequest, Source source2) throws IOException {
        this.originalRequest = eSTRequest;
        this.source = source2;
        if (source2 instanceof LimitedSource) {
            this.absoluteReadLimit = ((LimitedSource) source2).getAbsoluteReadLimit();
        }
        Set<String> asKeySet = Properties.asKeySet("org.bouncycastle.debug.est");
        this.inputStream = (asKeySet.contains("input") || asKeySet.contains("all")) ? new PrintingInputStream(source2.getInputStream()) : source2.getInputStream();
        this.headers = new HttpUtil.Headers();
        this.lineBuffer = new byte[1024];
        process();
    }

    static /* synthetic */ long access$108(ESTResponse eSTResponse) {
        long j = eSTResponse.read;
        eSTResponse.read = 1 + j;
        return j;
    }

    private void process() throws IOException {
        this.HttpVersion = readStringIncluding(' ');
        this.statusCode = Integer.parseInt(readStringIncluding(' '));
        this.statusMessage = readStringIncluding('\n');
        while (true) {
            String readStringIncluding = readStringIncluding('\n');
            if (readStringIncluding.length() <= 0) {
                break;
            }
            int indexOf = readStringIncluding.indexOf(58);
            if (indexOf > -1) {
                this.headers.add(Strings.toLowerCase(readStringIncluding.substring(0, indexOf).trim()), readStringIncluding.substring(indexOf + 1).trim());
            }
        }
        this.contentLength = getContentLength();
        int i = this.statusCode;
        if (i == 204 || i == 202) {
            Long l = this.contentLength;
            if (l == null) {
                this.contentLength = 0L;
            } else if (this.statusCode == 204 && l.longValue() > 0) {
                throw new IOException("Got HTTP status 204 but Content-length > 0.");
            }
        }
        Long l2 = this.contentLength;
        if (l2 != null) {
            if (l2.equals(ZERO)) {
                this.inputStream = new InputStream() {
                    /* class org.bouncycastle.est.ESTResponse.AnonymousClass1 */

                    @Override // java.io.InputStream
                    public int read() throws IOException {
                        return -1;
                    }
                };
            }
            Long l3 = this.contentLength;
            if (l3 != null) {
                if (l3.longValue() < 0) {
                    throw new IOException("Server returned negative content length: " + this.absoluteReadLimit);
                } else if (this.absoluteReadLimit != null && this.contentLength.longValue() >= this.absoluteReadLimit.longValue()) {
                    throw new IOException("Content length longer than absolute read limit: " + this.absoluteReadLimit + " Content-Length: " + this.contentLength);
                }
            }
            this.inputStream = wrapWithCounter(this.inputStream, this.absoluteReadLimit);
            if ("base64".equalsIgnoreCase(getHeader("content-transfer-encoding"))) {
                this.inputStream = new CTEBase64InputStream(this.inputStream, getContentLength());
                return;
            }
            return;
        }
        throw new IOException("No Content-length header.");
    }

    public void close() throws IOException {
        InputStream inputStream2 = this.inputStream;
        if (inputStream2 != null) {
            inputStream2.close();
        }
        this.source.close();
    }

    public Long getContentLength() {
        String firstValue = this.headers.getFirstValue("Content-Length");
        if (firstValue == null) {
            return null;
        }
        try {
            return Long.valueOf(Long.parseLong(firstValue));
        } catch (RuntimeException e) {
            throw new RuntimeException("Content Length: '" + firstValue + "' invalid. " + e.getMessage());
        }
    }

    public String getHeader(String str) {
        return this.headers.getFirstValue(str);
    }

    public HttpUtil.Headers getHeaders() {
        return this.headers;
    }

    public String getHttpVersion() {
        return this.HttpVersion;
    }

    public InputStream getInputStream() {
        return this.inputStream;
    }

    public ESTRequest getOriginalRequest() {
        return this.originalRequest;
    }

    public Source getSource() {
        return this.source;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public String getStatusMessage() {
        return this.statusMessage;
    }

    /* access modifiers changed from: protected */
    public String readStringIncluding(char c) throws IOException {
        int read2;
        int i;
        int i2 = 0;
        while (true) {
            read2 = this.inputStream.read();
            byte[] bArr = this.lineBuffer;
            i = i2 + 1;
            bArr[i2] = (byte) read2;
            if (i >= bArr.length) {
                throw new IOException("Server sent line > " + this.lineBuffer.length);
            } else if (read2 == c || read2 <= -1) {
                break;
            } else {
                i2 = i;
            }
        }
        if (read2 != -1) {
            return new String(this.lineBuffer, 0, i).trim();
        }
        throw new EOFException();
    }

    /* access modifiers changed from: protected */
    public InputStream wrapWithCounter(final InputStream inputStream2, final Long l) {
        return new InputStream() {
            /* class org.bouncycastle.est.ESTResponse.AnonymousClass2 */

            @Override // java.io.InputStream, java.io.Closeable, java.lang.AutoCloseable
            public void close() throws IOException {
                if (ESTResponse.this.contentLength != null && ESTResponse.this.contentLength.longValue() - 1 > ESTResponse.this.read) {
                    throw new IOException("Stream closed before limit fully read, Read: " + ESTResponse.this.read + " ContentLength: " + ESTResponse.this.contentLength);
                } else if (inputStream2.available() <= 0) {
                    inputStream2.close();
                } else {
                    throw new IOException("Stream closed with extra content in pipe that exceeds content length.");
                }
            }

            @Override // java.io.InputStream
            public int read() throws IOException {
                int read = inputStream2.read();
                if (read > -1) {
                    ESTResponse.access$108(ESTResponse.this);
                    if (l != null && ESTResponse.this.read >= l.longValue()) {
                        throw new IOException("Absolute Read Limit exceeded: " + l);
                    }
                }
                return read;
            }
        };
    }
}
