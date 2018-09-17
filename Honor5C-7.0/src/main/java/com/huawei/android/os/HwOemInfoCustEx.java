package com.huawei.android.os;

public class HwOemInfoCustEx {
    public static final int firstTimeLength = 16;
    public static final int firstTimeType = 37;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.android.os.HwOemInfoCustEx.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.android.os.HwOemInfoCustEx.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.android.os.HwOemInfoCustEx.<clinit>():void");
    }

    public static final native boolean native_bTAddressIsNull();

    public static final native byte[] native_getByteArrayFromOeminfo(int i, int i2);

    private static final native int native_getFastbootStatus();

    public static final native String native_getStringFromOeminfo(int i, int i2);

    public static final native int native_nffwriteImeiToOeminfo(String str);

    public static final native int native_writeByteArrayToOeminfo(int i, int i2, byte[] bArr);

    public static final native int native_writeStringToOeminfo(int i, int i2, String str);

    public static final int nffwriteImeiToOeminfo(String imeiStr) {
        return native_nffwriteImeiToOeminfo(imeiStr);
    }

    public static final boolean bTAddressIsNull() {
        return native_bTAddressIsNull();
    }

    public static final int writeStringToOeminfo(int type, int sizeOf, String mStr) {
        return native_writeStringToOeminfo(type, sizeOf, mStr);
    }

    public static final int writeByteArrayToOeminfo(int type, int sizeOf, byte[] mByteArray) {
        return native_writeByteArrayToOeminfo(type, sizeOf, mByteArray);
    }

    public static final String getStringFromOeminfo(int type, int sizeOf) {
        return native_getStringFromOeminfo(type, sizeOf);
    }

    public static final byte[] getByteArrayFromOeminfo(int type, int sizeOf) {
        return native_getByteArrayFromOeminfo(type, sizeOf);
    }

    public static final int writeFirstStartTime(String mStr) {
        return native_writeStringToOeminfo(firstTimeType, firstTimeLength, mStr);
    }

    public static final String getFirstStartTime() {
        return native_getStringFromOeminfo(firstTimeType, firstTimeLength);
    }

    public static final int getFastbootStatus() {
        return native_getFastbootStatus();
    }
}
