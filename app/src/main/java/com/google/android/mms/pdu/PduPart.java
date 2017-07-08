package com.google.android.mms.pdu;

import android.net.Uri;
import java.util.HashMap;
import java.util.Map;

public class PduPart {
    public static final String CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding";
    static final byte[] DISPOSITION_ATTACHMENT = null;
    static final byte[] DISPOSITION_FROM_DATA = null;
    static final byte[] DISPOSITION_INLINE = null;
    public static final String P_7BIT = "7bit";
    public static final String P_8BIT = "8bit";
    public static final String P_BASE64 = "base64";
    public static final String P_BINARY = "binary";
    public static final int P_CHARSET = 129;
    public static final int P_COMMENT = 155;
    public static final int P_CONTENT_DISPOSITION = 197;
    public static final int P_CONTENT_ID = 192;
    public static final int P_CONTENT_LOCATION = 142;
    public static final int P_CONTENT_TRANSFER_ENCODING = 200;
    public static final int P_CONTENT_TYPE = 145;
    public static final int P_CREATION_DATE = 147;
    public static final int P_CT_MR_TYPE = 137;
    public static final int P_DEP_COMMENT = 140;
    public static final int P_DEP_CONTENT_DISPOSITION = 174;
    public static final int P_DEP_DOMAIN = 141;
    public static final int P_DEP_FILENAME = 134;
    public static final int P_DEP_NAME = 133;
    public static final int P_DEP_PATH = 143;
    public static final int P_DEP_START = 138;
    public static final int P_DEP_START_INFO = 139;
    public static final int P_DIFFERENCES = 135;
    public static final int P_DISPOSITION_ATTACHMENT = 129;
    public static final int P_DISPOSITION_FROM_DATA = 128;
    public static final int P_DISPOSITION_INLINE = 130;
    public static final int P_DOMAIN = 156;
    public static final int P_FILENAME = 152;
    public static final int P_LEVEL = 130;
    public static final int P_MAC = 146;
    public static final int P_MAX_AGE = 142;
    public static final int P_MODIFICATION_DATE = 148;
    public static final int P_NAME = 151;
    public static final int P_PADDING = 136;
    public static final int P_PATH = 157;
    public static final int P_Q = 128;
    public static final String P_QUOTED_PRINTABLE = "quoted-printable";
    public static final int P_READ_DATE = 149;
    public static final int P_SEC = 145;
    public static final int P_SECURE = 144;
    public static final int P_SIZE = 150;
    public static final int P_START = 153;
    public static final int P_START_INFO = 154;
    public static final int P_TYPE = 131;
    private static final String TAG = "PduPart";
    private byte[] mPartData;
    private Map<Integer, Object> mPartHeader;
    private Uri mUri;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.google.android.mms.pdu.PduPart.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.google.android.mms.pdu.PduPart.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.mms.pdu.PduPart.<clinit>():void");
    }

    public PduPart() {
        this.mPartHeader = null;
        this.mUri = null;
        this.mPartData = null;
        this.mPartHeader = new HashMap();
    }

    public void setData(byte[] data) {
        if (data != null) {
            this.mPartData = new byte[data.length];
            System.arraycopy(data, 0, this.mPartData, 0, data.length);
        }
    }

    public byte[] getData() {
        if (this.mPartData == null) {
            return null;
        }
        byte[] byteArray = new byte[this.mPartData.length];
        System.arraycopy(this.mPartData, 0, byteArray, 0, this.mPartData.length);
        return byteArray;
    }

    public int getDataLength() {
        if (this.mPartData != null) {
            return this.mPartData.length;
        }
        return 0;
    }

    public void setDataUri(Uri uri) {
        this.mUri = uri;
    }

    public Uri getDataUri() {
        return this.mUri;
    }

    public void setContentId(byte[] contentId) {
        if (contentId == null || contentId.length == 0) {
            throw new IllegalArgumentException("Content-Id may not be null or empty.");
        } else if (contentId.length > 1 && ((char) contentId[0]) == '<' && ((char) contentId[contentId.length - 1]) == '>') {
            this.mPartHeader.put(Integer.valueOf(P_CONTENT_ID), contentId);
        } else {
            byte[] buffer = new byte[(contentId.length + 2)];
            buffer[0] = (byte) 60;
            buffer[buffer.length - 1] = (byte) 62;
            System.arraycopy(contentId, 0, buffer, 1, contentId.length);
            this.mPartHeader.put(Integer.valueOf(P_CONTENT_ID), buffer);
        }
    }

    public byte[] getContentId() {
        return (byte[]) this.mPartHeader.get(Integer.valueOf(P_CONTENT_ID));
    }

    public void setCharset(int charset) {
        this.mPartHeader.put(Integer.valueOf(P_DISPOSITION_ATTACHMENT), Integer.valueOf(charset));
    }

    public int getCharset() {
        Integer charset = (Integer) this.mPartHeader.get(Integer.valueOf(P_DISPOSITION_ATTACHMENT));
        if (charset == null) {
            return 0;
        }
        return charset.intValue();
    }

    public void setContentLocation(byte[] contentLocation) {
        if (contentLocation == null) {
            throw new NullPointerException("null content-location");
        }
        this.mPartHeader.put(Integer.valueOf(P_MAX_AGE), contentLocation);
    }

    public byte[] getContentLocation() {
        return (byte[]) this.mPartHeader.get(Integer.valueOf(P_MAX_AGE));
    }

    public void setContentDisposition(byte[] contentDisposition) {
        if (contentDisposition == null) {
            throw new NullPointerException("null content-disposition");
        }
        this.mPartHeader.put(Integer.valueOf(P_CONTENT_DISPOSITION), contentDisposition);
    }

    public byte[] getContentDisposition() {
        return (byte[]) this.mPartHeader.get(Integer.valueOf(P_CONTENT_DISPOSITION));
    }

    public void setContentType(byte[] contentType) {
        if (contentType == null) {
            throw new NullPointerException("null content-type");
        }
        this.mPartHeader.put(Integer.valueOf(P_SEC), contentType);
    }

    public byte[] getContentType() {
        return (byte[]) this.mPartHeader.get(Integer.valueOf(P_SEC));
    }

    public void setContentTransferEncoding(byte[] contentTransferEncoding) {
        if (contentTransferEncoding == null) {
            throw new NullPointerException("null content-transfer-encoding");
        }
        this.mPartHeader.put(Integer.valueOf(P_CONTENT_TRANSFER_ENCODING), contentTransferEncoding);
    }

    public byte[] getContentTransferEncoding() {
        return (byte[]) this.mPartHeader.get(Integer.valueOf(P_CONTENT_TRANSFER_ENCODING));
    }

    public void setName(byte[] name) {
        if (name == null) {
            throw new NullPointerException("null content-id");
        }
        this.mPartHeader.put(Integer.valueOf(P_NAME), name);
    }

    public byte[] getName() {
        return (byte[]) this.mPartHeader.get(Integer.valueOf(P_NAME));
    }

    public void setFilename(byte[] fileName) {
        if (fileName == null) {
            throw new NullPointerException("null content-id");
        }
        this.mPartHeader.put(Integer.valueOf(P_FILENAME), fileName);
    }

    public byte[] getFilename() {
        return (byte[]) this.mPartHeader.get(Integer.valueOf(P_FILENAME));
    }

    public String generateLocation() {
        byte[] location = (byte[]) this.mPartHeader.get(Integer.valueOf(P_NAME));
        if (location == null) {
            location = (byte[]) this.mPartHeader.get(Integer.valueOf(P_FILENAME));
            if (location == null) {
                location = (byte[]) this.mPartHeader.get(Integer.valueOf(P_MAX_AGE));
            }
        }
        if (location != null) {
            return new String(location);
        }
        byte[] contentId = (byte[]) this.mPartHeader.get(Integer.valueOf(P_CONTENT_ID));
        if (contentId != null) {
            return "cid:" + new String(contentId);
        }
        return "cid:null";
    }
}
