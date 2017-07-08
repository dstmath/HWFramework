package libcore.icu;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import libcore.util.NativeAllocationRegistry;

public final class NativeConverter {
    private static final NativeAllocationRegistry registry = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: libcore.icu.NativeConverter.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: libcore.icu.NativeConverter.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: libcore.icu.NativeConverter.<clinit>():void");
    }

    public static native Charset charsetForName(String str);

    public static native void closeConverter(long j);

    public static native boolean contains(String str, String str2);

    public static native int decode(long j, byte[] bArr, int i, char[] cArr, int i2, int[] iArr, boolean z);

    public static native int encode(long j, char[] cArr, int i, byte[] bArr, int i2, int[] iArr, boolean z);

    public static native String[] getAvailableCharsetNames();

    public static native float getAveBytesPerChar(long j);

    public static native float getAveCharsPerByte(long j);

    public static native int getMaxBytesPerChar(long j);

    public static native int getMinBytesPerChar(long j);

    public static native long getNativeFinalizer();

    public static native long getNativeSize();

    public static native byte[] getSubstitutionBytes(long j);

    public static native long openConverter(String str);

    public static native void resetByteToChar(long j);

    public static native void resetCharToByte(long j);

    private static native void setCallbackDecode(long j, int i, int i2, String str);

    private static native void setCallbackEncode(long j, int i, int i2, byte[] bArr);

    public static void registerConverter(Object referrent, long converterHandle) {
        registry.registerNativeAllocation(referrent, converterHandle);
    }

    private static int translateCodingErrorAction(CodingErrorAction action) {
        if (action == CodingErrorAction.REPORT) {
            return 0;
        }
        if (action == CodingErrorAction.IGNORE) {
            return 1;
        }
        if (action == CodingErrorAction.REPLACE) {
            return 2;
        }
        throw new AssertionError();
    }

    public static void setCallbackDecode(long converterHandle, CharsetDecoder decoder) {
        setCallbackDecode(converterHandle, translateCodingErrorAction(decoder.malformedInputAction()), translateCodingErrorAction(decoder.unmappableCharacterAction()), decoder.replacement());
    }

    public static void setCallbackEncode(long converterHandle, CharsetEncoder encoder) {
        setCallbackEncode(converterHandle, translateCodingErrorAction(encoder.malformedInputAction()), translateCodingErrorAction(encoder.unmappableCharacterAction()), encoder.replacement());
    }
}
