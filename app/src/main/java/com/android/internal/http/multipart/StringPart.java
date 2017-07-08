package com.android.internal.http.multipart;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.commons.logging.Log;
import org.apache.http.util.EncodingUtils;

public class StringPart extends PartBase {
    public static final String DEFAULT_CHARSET = "US-ASCII";
    public static final String DEFAULT_CONTENT_TYPE = "text/plain";
    public static final String DEFAULT_TRANSFER_ENCODING = "8bit";
    private static final Log LOG = null;
    private byte[] content;
    private String value;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.http.multipart.StringPart.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.http.multipart.StringPart.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.http.multipart.StringPart.<clinit>():void");
    }

    public StringPart(String name, String value, String charset) {
        String str = DEFAULT_CONTENT_TYPE;
        if (charset == null) {
            charset = DEFAULT_CHARSET;
        }
        super(name, str, charset, DEFAULT_TRANSFER_ENCODING);
        if (value == null) {
            throw new IllegalArgumentException("Value may not be null");
        } else if (value.indexOf(0) != -1) {
            throw new IllegalArgumentException("NULs may not be present in string parts");
        } else {
            this.value = value;
        }
    }

    public StringPart(String name, String value) {
        this(name, value, null);
    }

    private byte[] getContent() {
        if (this.content == null) {
            this.content = EncodingUtils.getBytes(this.value, getCharSet());
        }
        return this.content;
    }

    protected void sendData(OutputStream out) throws IOException {
        LOG.trace("enter sendData(OutputStream)");
        out.write(getContent());
    }

    protected long lengthOfData() {
        LOG.trace("enter lengthOfData()");
        return (long) getContent().length;
    }

    public void setCharSet(String charSet) {
        super.setCharSet(charSet);
        this.content = null;
    }
}
