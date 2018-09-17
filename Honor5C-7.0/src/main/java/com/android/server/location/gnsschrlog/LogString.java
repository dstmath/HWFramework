package com.android.server.location.gnsschrlog;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class LogString {
    static String CHARSET = null;
    static String EMPTY_STRING = null;
    private static final String LOG_TAG = "LogString";
    private int length;
    private String value;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.location.gnsschrlog.LogString.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.location.gnsschrlog.LogString.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.location.gnsschrlog.LogString.<clinit>():void");
    }

    private String getEmptyString(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(EMPTY_STRING);
        }
        return sb.toString();
    }

    public int getLength() {
        return this.length;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        if (value == null) {
            this.value = getEmptyString(this.length);
        } else {
            this.value = value;
        }
    }

    public void setByByteArray(byte[] src, int len, boolean bIsLittleEndian) {
        if (this.length != len) {
            ChrLog.chrLogE(LOG_TAG, "setByByteArray failed ,not support len = " + len);
        }
        try {
            this.value = new String(src, CHARSET);
            ChrLog.chrLogI(LOG_TAG, "setByByteArray value = " + this.value);
        } catch (UnsupportedEncodingException e) {
            ChrLog.chrLogE(LOG_TAG, "setByByteArray UnsupportedEncodingException");
        }
    }

    public LogString(int length) {
        this.length = length;
        this.value = getEmptyString(length);
    }

    public String toString() {
        return this.value.toString();
    }

    public byte[] toByteArray() {
        try {
            ByteBuffer bytebuf = ByteBuffer.wrap(new byte[this.length]);
            byte[] subValueBytes = this.value.getBytes(CHARSET);
            if (subValueBytes.length > this.length) {
                bytebuf.put(subValueBytes, 0, this.length);
                ChrLog.chrLogE(LOG_TAG, "toByteArray length error, subValueBytes.length = " + subValueBytes.length + ", length = " + this.length);
            } else {
                bytebuf.put(subValueBytes);
            }
            return bytebuf.array();
        } catch (UnsupportedEncodingException e) {
            return new byte[this.length];
        }
    }
}
