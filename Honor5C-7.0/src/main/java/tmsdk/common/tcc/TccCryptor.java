package tmsdk.common.tcc;

import android.content.Context;

/* compiled from: Unknown */
public class TccCryptor {
    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdk.common.tcc.TccCryptor.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdk.common.tcc.TccCryptor.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdk.common.tcc.TccCryptor.<clinit>():void");
    }

    public static int EndianSwap(int i) {
        return (((i << 24) | ((65280 & i) << 8)) | ((16711680 & i) >>> 8)) | (i >>> 24);
    }

    @Deprecated
    public static byte[] decrypt(Context context, byte[] bArr, byte[] bArr2) {
        return decrypt(bArr, bArr2);
    }

    public static native byte[] decrypt(byte[] bArr, byte[] bArr2);

    @Deprecated
    public static boolean decryptBoolean(Context context, boolean z) {
        return decryptBoolean(z);
    }

    public static boolean decryptBoolean(boolean z) {
        return !z;
    }

    public static int decryptInt(int i) {
        return i ^ -1;
    }

    @Deprecated
    public static int decryptInt(Context context, int i) {
        return decryptInt(i);
    }

    public static long decryptLong(long j) {
        return -1 ^ j;
    }

    @Deprecated
    public static long decryptLong(Context context, long j) {
        return decryptLong(j);
    }

    @Deprecated
    public static String encrypt(Context context, String str, String str2) {
        return encrypt(str, str2);
    }

    public static String encrypt(String str, String str2) {
        byte[] bArr = null;
        byte[] bytes = str.getBytes();
        if (str2 != null) {
            bArr = str2.getBytes();
        }
        return new String(encrypt(bytes, bArr));
    }

    @Deprecated
    public static byte[] encrypt(Context context, byte[] bArr, byte[] bArr2) {
        return encrypt(bArr, bArr2);
    }

    public static native byte[] encrypt(byte[] bArr, byte[] bArr2);

    @Deprecated
    public static boolean encryptBoolean(Context context, boolean z) {
        return encryptBoolean(z);
    }

    public static boolean encryptBoolean(boolean z) {
        return !z;
    }

    public static int encryptInt(int i) {
        return i ^ -1;
    }

    @Deprecated
    public static int encryptInt(Context context, int i) {
        return encryptInt(i);
    }

    public static long encryptLong(long j) {
        return -1 ^ j;
    }

    @Deprecated
    public static long encryptLong(Context context, long j) {
        return encryptLong(j);
    }

    @Deprecated
    public static byte[] makePassword(Context context, byte[] bArr) {
        return makePassword(bArr);
    }

    public static native byte[] makePassword(byte[] bArr);
}
