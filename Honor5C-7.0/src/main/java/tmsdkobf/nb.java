package tmsdkobf;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/* compiled from: Unknown */
public class nb {
    private static final char[] BW = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.nb.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.nb.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.nb.<clinit>():void");
    }

    public static byte[] bG(int i) {
        return new byte[]{(byte) ((byte) ((i >> 24) & 255)), (byte) ((byte) ((i >> 16) & 255)), (byte) ((byte) ((i >> 8) & 255)), (byte) ((byte) (i & 255))};
    }

    public static byte[] cF(String str) {
        return n(str.getBytes());
    }

    public static String cG(String str) {
        byte[] cF = cF(str);
        if (cF == null) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder(cF.length * 2);
        for (byte b : cF) {
            stringBuilder.append(Integer.toHexString(b & 255).substring(0, 1));
        }
        return stringBuilder.toString();
    }

    public static byte[] n(byte[] bArr) {
        try {
            MessageDigest instance = MessageDigest.getInstance("MD5");
            instance.update(bArr);
            return instance.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String o(byte[] bArr) {
        StringBuilder stringBuilder = new StringBuilder(bArr.length * 3);
        for (byte b : bArr) {
            int i = b & 255;
            stringBuilder.append(BW[i >> 4]);
            stringBuilder.append(BW[i & 15]);
        }
        return stringBuilder.toString().toUpperCase();
    }

    public static String p(byte[] bArr) {
        return o(n(bArr));
    }
}
