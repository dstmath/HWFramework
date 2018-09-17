package tmsdkobf;

public class fm {
    private static final char[] mh = null;
    public static final byte[] mi = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.fm.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.fm.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.fm.<clinit>():void");
    }

    public static String c(byte[] bArr) {
        if (bArr == null || bArr.length == 0) {
            return null;
        }
        char[] cArr = new char[(bArr.length * 2)];
        for (int i = 0; i < bArr.length; i++) {
            byte b = bArr[i];
            cArr[(i * 2) + 1] = (char) mh[b & 15];
            cArr[(i * 2) + 0] = (char) mh[((byte) (b >>> 4)) & 15];
        }
        return new String(cArr);
    }

    public static byte a(char c) {
        if (c >= '0' && c <= '9') {
            return (byte) (c - 48);
        }
        if (c >= 'a' && c <= 'f') {
            return (byte) ((c - 97) + 10);
        }
        if (c >= 'A' && c <= 'F') {
            return (byte) ((c - 65) + 10);
        }
        return (byte) 0;
    }

    public static byte[] ac(String str) {
        if (str == null || str.equals("")) {
            return mi;
        }
        byte[] bArr = new byte[(str.length() / 2)];
        for (int i = 0; i < bArr.length; i++) {
            bArr[i] = (byte) ((byte) ((a(str.charAt(i * 2)) * 16) + a(str.charAt((i * 2) + 1))));
        }
        return bArr;
    }
}
