package com.android.internal.http.multipart;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;
import org.apache.commons.logging.Log;
import org.apache.http.Header;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EncodingUtils;

public class MultipartEntity extends AbstractHttpEntity {
    public static final String MULTIPART_BOUNDARY = "http.method.multipart.boundary";
    private static byte[] MULTIPART_CHARS = null;
    private static final String MULTIPART_FORM_CONTENT_TYPE = "multipart/form-data";
    private static final Log log = null;
    private boolean contentConsumed;
    private byte[] multipartBoundary;
    private HttpParams params;
    protected Part[] parts;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.http.multipart.MultipartEntity.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.http.multipart.MultipartEntity.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.http.multipart.MultipartEntity.<clinit>():void");
    }

    private static byte[] generateMultipartBoundary() {
        Random rand = new Random();
        byte[] bytes = new byte[(rand.nextInt(11) + 30)];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = MULTIPART_CHARS[rand.nextInt(MULTIPART_CHARS.length)];
        }
        return bytes;
    }

    public MultipartEntity(Part[] parts, HttpParams params) {
        this.contentConsumed = false;
        if (parts == null) {
            throw new IllegalArgumentException("parts cannot be null");
        } else if (params == null) {
            throw new IllegalArgumentException("params cannot be null");
        } else {
            this.parts = parts;
            this.params = params;
        }
    }

    public MultipartEntity(Part[] parts) {
        this.contentConsumed = false;
        setContentType(MULTIPART_FORM_CONTENT_TYPE);
        if (parts == null) {
            throw new IllegalArgumentException("parts cannot be null");
        }
        this.parts = parts;
        this.params = null;
    }

    protected byte[] getMultipartBoundary() {
        if (this.multipartBoundary == null) {
            String str = null;
            if (this.params != null) {
                str = (String) this.params.getParameter(MULTIPART_BOUNDARY);
            }
            if (str != null) {
                this.multipartBoundary = EncodingUtils.getAsciiBytes(str);
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
