package libcore.icu;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import libcore.util.NativeAllocationRegistry;

public final class NativeConverter {
    private static final NativeAllocationRegistry registry = new NativeAllocationRegistry(NativeConverter.class.getClassLoader(), getNativeFinalizer(), getNativeSize());

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
