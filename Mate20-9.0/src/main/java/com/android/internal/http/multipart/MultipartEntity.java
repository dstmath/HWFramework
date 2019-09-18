package com.android.internal.http.multipart;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EncodingUtils;

public class MultipartEntity extends AbstractHttpEntity {
    public static final String MULTIPART_BOUNDARY = "http.method.multipart.boundary";
    private static byte[] MULTIPART_CHARS = EncodingUtils.getAsciiBytes("-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");
    private static final String MULTIPART_FORM_CONTENT_TYPE = "multipart/form-data";
    private static final Log log = LogFactory.getLog(MultipartEntity.class);
    private boolean contentConsumed = false;
    private byte[] multipartBoundary;
    private HttpParams params;
    protected Part[] parts;

    private static byte[] generateMultipartBoundary() {
        Random rand = new Random();
        byte[] bytes = new byte[(rand.nextInt(11) + 30)];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = MULTIPART_CHARS[rand.nextInt(MULTIPART_CHARS.length)];
        }
        return bytes;
    }

    public MultipartEntity(Part[] parts2, HttpParams params2) {
        if (parts2 == null) {
            throw new IllegalArgumentException("parts cannot be null");
        } else if (params2 != null) {
            this.parts = parts2;
            this.params = params2;
        } else {
            throw new IllegalArgumentException("params cannot be null");
        }
    }

    public MultipartEntity(Part[] parts2) {
        setContentType(MULTIPART_FORM_CONTENT_TYPE);
        if (parts2 != null) {
            this.parts = parts2;
            this.params = null;
            return;
        }
        throw new IllegalArgumentException("parts cannot be null");
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v4, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v4, resolved type: java.lang.String} */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Multi-variable type inference failed */
    public byte[] getMultipartBoundary() {
        if (this.multipartBoundary == null) {
            String temp = null;
            if (this.params != null) {
                temp = this.params.getParameter(MULTIPART_BOUNDARY);
            }
            if (temp != null) {
                this.multipartBoundary = EncodingUtils.getAsciiBytes(temp);
            } else {
                this.multipartBoundary = generateMultipartBoundary();
            }
        }
        return this.multipartBoundary;
    }

    public boolean isRepeatable() {
        for (Part isRepeatable : this.parts) {
            if (!isRepeatable.isRepeatable()) {
                return false;
            }
        }
        return true;
    }

    public void writeTo(OutputStream out) throws IOException {
        Part.sendParts(out, this.parts, getMultipartBoundary());
    }

    public Header getContentType() {
        StringBuffer buffer = new StringBuffer(MULTIPART_FORM_CONTENT_TYPE);
        buffer.append("; boundary=");
        buffer.append(EncodingUtils.getAsciiString(getMultipartBoundary()));
        return new BasicHeader(HTTP.CONTENT_TYPE, buffer.toString());
    }

    public long getContentLength() {
        try {
            return Part.getLengthOfParts(this.parts, getMultipartBoundary());
        } catch (Exception e) {
            log.error("An exception occurred while getting the length of the parts", e);
            return 0;
        }
    }

    public InputStream getContent() throws IOException, IllegalStateException {
        if (isRepeatable() || !this.contentConsumed) {
            this.contentConsumed = true;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Part.sendParts(baos, this.parts, this.multipartBoundary);
            return new ByteArrayInputStream(baos.toByteArray());
        }
        throw new IllegalStateException("Content has been consumed");
    }

    public boolean isStreaming() {
        return false;
    }
}
