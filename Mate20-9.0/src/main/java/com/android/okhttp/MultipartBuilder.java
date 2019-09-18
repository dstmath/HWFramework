package com.android.okhttp;

import com.android.okhttp.internal.Util;
import com.android.okhttp.okio.Buffer;
import com.android.okhttp.okio.BufferedSink;
import com.android.okhttp.okio.ByteString;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class MultipartBuilder {
    public static final MediaType ALTERNATIVE = MediaType.parse("multipart/alternative");
    /* access modifiers changed from: private */
    public static final byte[] COLONSPACE = {58, 32};
    /* access modifiers changed from: private */
    public static final byte[] CRLF = {13, 10};
    /* access modifiers changed from: private */
    public static final byte[] DASHDASH = {45, 45};
    public static final MediaType DIGEST = MediaType.parse("multipart/digest");
    public static final MediaType FORM = MediaType.parse("multipart/form-data");
    public static final MediaType MIXED = MediaType.parse("multipart/mixed");
    public static final MediaType PARALLEL = MediaType.parse("multipart/parallel");
    private final ByteString boundary;
    private final List<RequestBody> partBodies;
    private final List<Headers> partHeaders;
    private MediaType type;

    private static final class MultipartRequestBody extends RequestBody {
        private final ByteString boundary;
        private long contentLength = -1;
        private final MediaType contentType;
        private final List<RequestBody> partBodies;
        private final List<Headers> partHeaders;

        public MultipartRequestBody(MediaType type, ByteString boundary2, List<Headers> partHeaders2, List<RequestBody> partBodies2) {
            if (type != null) {
                this.boundary = boundary2;
                this.contentType = MediaType.parse(type + "; boundary=" + boundary2.utf8());
                this.partHeaders = Util.immutableList(partHeaders2);
                this.partBodies = Util.immutableList(partBodies2);
                return;
            }
            throw new NullPointerException("type == null");
        }

        public MediaType contentType() {
            return this.contentType;
        }

        public long contentLength() throws IOException {
            long result = this.contentLength;
            if (result != -1) {
                return result;
            }
            long writeOrCountBytes = writeOrCountBytes(null, true);
            this.contentLength = writeOrCountBytes;
            return writeOrCountBytes;
        }

        private long writeOrCountBytes(BufferedSink sink, boolean countBytes) throws IOException {
            long byteCount = 0;
            Buffer byteCountBuffer = null;
            if (countBytes) {
                Buffer buffer = new Buffer();
                byteCountBuffer = buffer;
                sink = buffer;
            }
            int partCount = this.partHeaders.size();
            for (int p = 0; p < partCount; p++) {
                Headers headers = this.partHeaders.get(p);
                RequestBody body = this.partBodies.get(p);
                sink.write(MultipartBuilder.DASHDASH);
                sink.write(this.boundary);
                sink.write(MultipartBuilder.CRLF);
                if (headers != null) {
                    int headerCount = headers.size();
                    for (int h = 0; h < headerCount; h++) {
                        sink.writeUtf8(headers.name(h)).write(MultipartBuilder.COLONSPACE).writeUtf8(headers.value(h)).write(MultipartBuilder.CRLF);
                    }
                }
                MediaType contentType2 = body.contentType();
                if (contentType2 != null) {
                    sink.writeUtf8("Content-Type: ").writeUtf8(contentType2.toString()).write(MultipartBuilder.CRLF);
                }
                long contentLength2 = body.contentLength();
                if (contentLength2 != -1) {
                    sink.writeUtf8("Content-Length: ").writeDecimalLong(contentLength2).write(MultipartBuilder.CRLF);
                } else if (countBytes) {
                    byteCountBuffer.clear();
                    return -1;
                }
                sink.write(MultipartBuilder.CRLF);
                if (countBytes) {
                    byteCount += contentLength2;
                } else {
                    this.partBodies.get(p).writeTo(sink);
                }
                sink.write(MultipartBuilder.CRLF);
            }
            sink.write(MultipartBuilder.DASHDASH);
            sink.write(this.boundary);
            sink.write(MultipartBuilder.DASHDASH);
            sink.write(MultipartBuilder.CRLF);
            if (countBytes) {
                byteCount += byteCountBuffer.size();
                byteCountBuffer.clear();
            }
            return byteCount;
        }

        public void writeTo(BufferedSink sink) throws IOException {
            writeOrCountBytes(sink, false);
        }
    }

    public MultipartBuilder() {
        this(UUID.randomUUID().toString());
    }

    public MultipartBuilder(String boundary2) {
        this.type = MIXED;
        this.partHeaders = new ArrayList();
        this.partBodies = new ArrayList();
        this.boundary = ByteString.encodeUtf8(boundary2);
    }

    public MultipartBuilder type(MediaType type2) {
        if (type2 == null) {
            throw new NullPointerException("type == null");
        } else if (type2.type().equals("multipart")) {
            this.type = type2;
            return this;
        } else {
            throw new IllegalArgumentException("multipart != " + type2);
        }
    }

    public MultipartBuilder addPart(RequestBody body) {
        return addPart(null, body);
    }

    public MultipartBuilder addPart(Headers headers, RequestBody body) {
        if (body == null) {
            throw new NullPointerException("body == null");
        } else if (headers != null && headers.get("Content-Type") != null) {
            throw new IllegalArgumentException("Unexpected header: Content-Type");
        } else if (headers == null || headers.get("Content-Length") == null) {
            this.partHeaders.add(headers);
            this.partBodies.add(body);
            return this;
        } else {
            throw new IllegalArgumentException("Unexpected header: Content-Length");
        }
    }

    private static StringBuilder appendQuotedString(StringBuilder target, String key) {
        target.append('\"');
        int len = key.length();
        for (int i = 0; i < len; i++) {
            char ch = key.charAt(i);
            if (ch == 10) {
                target.append("%0A");
            } else if (ch == 13) {
                target.append("%0D");
            } else if (ch != '\"') {
                target.append(ch);
            } else {
                target.append("%22");
            }
        }
        target.append('\"');
        return target;
    }

    public MultipartBuilder addFormDataPart(String name, String value) {
        return addFormDataPart(name, null, RequestBody.create((MediaType) null, value));
    }

    public MultipartBuilder addFormDataPart(String name, String filename, RequestBody value) {
        if (name != null) {
            StringBuilder disposition = new StringBuilder("form-data; name=");
            appendQuotedString(disposition, name);
            if (filename != null) {
                disposition.append("; filename=");
                appendQuotedString(disposition, filename);
            }
            return addPart(Headers.of("Content-Disposition", disposition.toString()), value);
        }
        throw new NullPointerException("name == null");
    }

    public RequestBody build() {
        if (!this.partHeaders.isEmpty()) {
            return new MultipartRequestBody(this.type, this.boundary, this.partHeaders, this.partBodies);
        }
        throw new IllegalStateException("Multipart body must have at least one part.");
    }
}
