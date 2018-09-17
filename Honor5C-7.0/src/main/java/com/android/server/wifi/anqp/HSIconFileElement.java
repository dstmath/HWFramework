package com.android.server.wifi.anqp;

import com.android.server.wifi.anqp.Constants.ANQPElementType;
import java.net.ProtocolException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class HSIconFileElement extends ANQPElement {
    private final byte[] mIconData;
    private final StatusCode mStatusCode;
    private final String mType;

    public enum StatusCode {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.anqp.HSIconFileElement.StatusCode.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.anqp.HSIconFileElement.StatusCode.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.anqp.HSIconFileElement.StatusCode.<clinit>():void");
        }
    }

    public HSIconFileElement(ANQPElementType infoID, ByteBuffer payload) throws ProtocolException {
        super(infoID);
        if (payload.remaining() < 4) {
            throw new ProtocolException("Truncated icon file: " + payload.remaining());
        }
        int statusID = payload.get() & Constants.BYTE_MASK;
        this.mStatusCode = statusID < StatusCode.values().length ? StatusCode.values()[statusID] : null;
        this.mType = Constants.getPrefixedString(payload, 1, StandardCharsets.US_ASCII);
        this.mIconData = new byte[(payload.getShort() & Constants.SHORT_MASK)];
        payload.get(this.mIconData);
    }

    public StatusCode getStatusCode() {
        return this.mStatusCode;
    }

    public String getType() {
        return this.mType;
    }

    public byte[] getIconData() {
        return this.mIconData;
    }

    public String toString() {
        return "HSIconFile{statusCode=" + this.mStatusCode + ", type='" + this.mType + '\'' + ", iconData=" + this.mIconData.length + " bytes }";
    }
}
