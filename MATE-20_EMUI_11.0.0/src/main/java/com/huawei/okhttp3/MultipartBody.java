package com.huawei.okhttp3;

import com.huawei.okhttp3.Headers;
import com.huawei.okhttp3.internal.Util;
import com.huawei.okio.Buffer;
import com.huawei.okio.BufferedSink;
import com.huawei.okio.ByteString;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

public final class MultipartBody extends RequestBody {
    public static final MediaType ALTERNATIVE = MediaType.get("multipart/alternative");
    private static final byte[] COLONSPACE = {58, 32};
    private static final byte[] CRLF = {13, 10};
    private static final byte[] DASHDASH = {45, 45};
    public static final MediaType DIGEST = MediaType.get("multipart/digest");
    public static final MediaType FORM = MediaType.get("multipart/form-data");
    public static final MediaType MIXED = MediaType.get("multipart/mixed");
    public static final MediaType PARALLEL = MediaType.get("multipart/parallel");
    private final ByteString boundary;
    private long contentLength = -1;
    private final MediaType contentType;
    private final MediaType originalType;
    private final List<Part> parts;

    MultipartBody(ByteString boundary2, MediaType type, List<Part> parts2) {
        this.boundary = boundary2;
        this.originalType = type;
        this.contentType = MediaType.get(type + "; boundary=" + boundary2.utf8());
        this.parts = Util.immutableList(parts2);
    }

    public MediaType type() {
        return this.originalType;
    }

    public String boundary() {
        return this.boundary.utf8();
    }

    public int size() {
        return this.parts.size();
    }

    public List<Part> parts() {
        return this.parts;
    }

    public Part part(int index) {
        return this.parts.get(index);
    }

    @Override // com.huawei.okhttp3.RequestBody
    public MediaType contentType() {
        return this.contentType;
    }

    @Override // com.huawei.okhttp3.RequestBody
    public long contentLength() throws IOException {
        long result = this.contentLength;
        if (result != -1) {
            return result;
        }
        long writeOrCountBytes = writeOrCountBytes(null, true);
        this.contentLength = writeOrCountBytes;
        return writeOrCountBytes;
    }

    @Override // com.huawei.okhttp3.RequestBody
    public void writeTo(BufferedSink sink) throws IOException {
        writeOrCountBytes(sink, false);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r3v0, resolved type: com.huawei.okio.Buffer */
    /* JADX DEBUG: Multi-variable search result rejected for r3v1, resolved type: com.huawei.okio.Buffer */
    /* JADX DEBUG: Multi-variable search result rejected for r3v2, resolved type: com.huawei.okio.Buffer */
    /* JADX WARN: Multi-variable type inference failed */
    private long writeOrCountBytes(@Nullable BufferedSink sink, boolean countBytes) throws IOException {
        BufferedSink sink2;
        long byteCount = 0;
        Buffer byteCountBuffer = 0;
        if (countBytes) {
            sink2 = new Buffer();
            byteCountBuffer = sink2;
        } else {
            sink2 = sink;
        }
        int partCount = this.parts.size();
        for (int p = 0; p < partCount; p++) {
            Part part = this.parts.get(p);
            Headers headers = part.headers;
            RequestBody body = part.body;
            sink2.write(DASHDASH);
            sink2.write(this.boundary);
            sink2.write(CRLF);
            if (headers != null) {
                int headerCount = headers.size();
                for (int h = 0; h < headerCount; h++) {
                    sink2.writeUtf8(headers.name(h)).write(COLONSPACE).writeUtf8(headers.value(h)).write(CRLF);
                }
            }
            MediaType contentType2 = body.contentType();
            if (contentType2 != null) {
                sink2.writeUtf8("Content-Type: ").writeUtf8(contentType2.toString()).write(CRLF);
            }
            long contentLength2 = body.contentLength();
            if (contentLength2 != -1) {
                sink2.writeUtf8("Content-Length: ").writeDecimalLong(contentLength2).write(CRLF);
            } else if (countBytes) {
                byteCountBuffer.clear();
                return -1;
            }
            sink2.write(CRLF);
            if (countBytes) {
                byteCount += contentLength2;
            } else {
                body.writeTo(sink2);
            }
            sink2.write(CRLF);
        }
        sink2.write(DASHDASH);
        sink2.write(this.boundary);
        sink2.write(DASHDASH);
        sink2.write(CRLF);
        if (!countBytes) {
            return byteCount;
        }
        long byteCount2 = byteCount + byteCountBuffer.size();
        byteCountBuffer.clear();
        return byteCount2;
    }

    static void appendQuotedString(StringBuilder target, String key) {
        target.append('\"');
        int len = key.length();
        for (int i = 0; i < len; i++) {
            char ch = key.charAt(i);
            if (ch == '\n') {
                target.append("%0A");
            } else if (ch == '\r') {
                target.append("%0D");
            } else if (ch != '\"') {
                target.append(ch);
            } else {
                target.append("%22");
            }
        }
        target.append('\"');
    }

    public static final class Part {
        final RequestBody body;
        @Nullable
        final Headers headers;

        public static Part create(RequestBody body2) {
            return create(null, body2);
        }

        public static Part create(@Nullable Headers headers2, RequestBody body2) {
            if (body2 == null) {
                throw new NullPointerException("body == null");
            } else if (headers2 != null && headers2.get("Content-Type") != null) {
                throw new IllegalArgumentException("Unexpected header: Content-Type");
            } else if (headers2 == null || headers2.get("Content-Length") == null) {
                return new Part(headers2, body2);
            } else {
                throw new IllegalArgumentException("Unexpected header: Content-Length");
            }
        }

        public static Part createFormData(String name, String value) {
            return createFormData(name, null, RequestBody.create((MediaType) null, value));
        }

        public static Part createFormData(String name, @Nullable String filename, RequestBody body2) {
            if (name != null) {
                StringBuilder disposition = new StringBuilder("form-data; name=");
                MultipartBody.appendQuotedString(disposition, name);
                if (filename != null) {
                    disposition.append("; filename=");
                    MultipartBody.appendQuotedString(disposition, filename);
                }
                return create(new Headers.Builder().addUnsafeNonAscii("Content-Disposition", disposition.toString()).build(), body2);
            }
            throw new NullPointerException("name == null");
        }

        private Part(@Nullable Headers headers2, RequestBody body2) {
            this.headers = headers2;
            this.body = body2;
        }

        @Nullable
        public Headers headers() {
            return this.headers;
        }

        public RequestBody body() {
            return this.body;
        }
    }

    public static final class Builder {
        private final ByteString boundary;
        private final List<Part> parts;
        private MediaType type;

        public Builder() {
            this(UUID.randomUUID().toString());
        }

        public Builder(String boundary2) {
            this.type = MultipartBody.MIXED;
            this.parts = new ArrayList();
            this.boundary = ByteString.encodeUtf8(boundary2);
        }

        public Builder setType(MediaType type2) {
            if (type2 == null) {
                throw new NullPointerException("type == null");
            } else if (type2.type().equals("multipart")) {
                this.type = type2;
                return this;
            } else {
                throw new IllegalArgumentException("multipart != " + type2);
            }
        }

        public Builder addPart(RequestBody body) {
            return addPart(Part.create(body));
        }

        public Builder addPart(@Nullable Headers headers, RequestBody body) {
            return addPart(Part.create(headers, body));
        }

        public Builder addFormDataPart(String name, String value) {
            return addPart(Part.createFormData(name, value));
        }

        public Builder addFormDataPart(String name, @Nullable String filename, RequestBody body) {
            return addPart(Part.createFormData(name, filename, body));
        }

        public Builder addPart(Part part) {
            if (part != null) {
                this.parts.add(part);
                return this;
            }
            throw new NullPointerException("part == null");
        }

        public MultipartBody build() {
            if (!this.parts.isEmpty()) {
                return new MultipartBody(this.boundary, this.type, this.parts);
            }
            throw new IllegalStateException("Multipart body must have at least one part.");
        }
    }
}
