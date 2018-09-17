package com.android.okhttp.internal.framed;

public enum ErrorCode {
    ;
    
    public final int httpCode;
    public final int spdyGoAwayCode;
    public final int spdyRstCode;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.okhttp.internal.framed.ErrorCode.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.okhttp.internal.framed.ErrorCode.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.okhttp.internal.framed.ErrorCode.<clinit>():void");
    }

    private ErrorCode(int httpCode, int spdyRstCode, int spdyGoAwayCode) {
        this.httpCode = httpCode;
        this.spdyRstCode = spdyRstCode;
        this.spdyGoAwayCode = spdyGoAwayCode;
    }

    public static ErrorCode fromSpdy3Rst(int code) {
        for (ErrorCode errorCode : values()) {
            if (errorCode.spdyRstCode == code) {
                return errorCode;
            }
        }
        return null;
    }

    public static ErrorCode fromHttp2(int code) {
        for (ErrorCode errorCode : values()) {
            if (errorCode.httpCode == code) {
                return errorCode;
            }
        }
        return null;
    }

    public static ErrorCode fromSpdyGoAway(int code) {
        for (ErrorCode errorCode : values()) {
            if (errorCode.spdyGoAwayCode == code) {
                return errorCode;
            }
        }
        return null;
    }
}
