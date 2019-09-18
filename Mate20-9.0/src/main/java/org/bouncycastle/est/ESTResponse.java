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
    /* access modifiers changed from: private */
    public Long contentLength;
    private final HttpUtil.Headers headers;
    private InputStream inputStream;
    private final byte[] lineBuffer;
    private final ESTRequest originalRequest;
    /* access modifiers changed from: private */
    public long read = 0;
    private final Source source;
    private int statusCode;
    private String statusMessage;

    private class PrintingInputStream extends InputStream {
        private final InputStream src;

        private PrintingInputStream(InputStream inputStream) {
            this.src = inputStream;
        }

        public int available() throws IOException {
            return this.src.available();
        }

        public void close() throws IOException {
            this.src.close();
        }

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

    private void process() throws IOException {
        this.HttpVersion = readStringIncluding(' ');
        this.statusCode = Integer.parseInt(readStringIncluding(' '));
        this.statusMessage = readStringIncluding(10);
        while (true) {
            String readStringIncluding = readStringIncluding(10);
            if (readStringIncluding.length() <= 0) {
                break;
            }
            int indexOf = readStringIncluding.indexOf(58);
            if (indexOf > -1) {
                this.headers.add(Strings.toLowerCase(readStringIncluding.substring(0, indexOf).trim()), readStringIncluding.substring(indexOf + 1).trim());
            }
        }
        this.contentLength = getContentLength();
        if (this.statusCode == 204 || this.statusCode == 202) {
            if (this.contentLength == null) {
                this.contentLength = 0L;
            } else if (this.statusCode == 204 && this.contentLength.longValue() > 0) {
                throw new IOException("Got HTTP status 204 but Content-length > 0.");
            }
        }
        if (this.contentLength != null) {
            if (this.contentLength.equals(ZERO)) {
                this.inputStream = new InputStream() {
                    public int read() throws IOException {
                        return -1;
                    }
                };
            }
            if (this.contentLength != null) {
                if (this.contentLength.longValue() < 0) {
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
        if (this.inputStream != null) {
            this.inputStream.close();
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
    /* JADX WARNING: Removed duplicated region for block: B:10:0x002a  */
    /* JADX WARNING: Removed duplicated region for block: B:8:0x001e  */
    public String readStringIncluding(char c) throws IOException {
        int read2;
        int i = 0;
        while (true) {
            read2 = this.inputStream.read();
            int i2 = i + 1;
            this.lineBuffer[i] = (byte) read2;
            if (i2 >= this.lineBuffer.length) {
                throw new IOException("Server sent line > " + this.lineBuffer.length);
            } else if (read2 != c && read2 > -1) {
                i = i2;
            } else if (read2 == -1) {
                return new String(this.lineBuffer, 0, i2).trim();
            } else {
                throw new EOFException();
            }
        }
        if (read2 == -1) {
        }
    }

    /* access modifiers changed from: protected */
    public InputStream wrapWithCounter(final InputStream inputStream2, final Long l) {
        return new InputStream() {
            public void close() throws IOException {
                if (ESTResponse.this.contentLength != null && ESTResponse.this.contentLength.longValue() - 1 > ESTResponse.this.read) {
                    throw new IOException("Stream closed before limit fully read, Read: " + ESTResponse.this.read + " ContentLength: " + ESTResponse.this.contentLength);
                } else if (inputStream2.available() <= 0) {
                    inputStream2.close();
                } else {
                    throw new IOException("Stream closed with extra content in pipe that exceeds content length.");
                }
            }

            public int read() throws IOException {
                int read = inputStream2.read();
                if (read > -1) {
                    long unused = ESTResponse.this.read = 1 + ESTResponse.this.read;
                    if (l == null || ESTResponse.this.read < l.longValue()) {
                        return read;
                    }
                    throw new IOException("Absolute Read Limit exceeded: " + l);
                }
                return read;
            }
        };
    }
}
