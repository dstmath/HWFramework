package com.google.protobuf;

import android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback;
import com.android.server.wifi.WifiConfigManager;
import java.lang.reflect.Field;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.logging.Level;
import java.util.logging.Logger;
import sun.misc.Unsafe;

/* access modifiers changed from: package-private */
public final class Utf8 {
    private static final long ASCII_MASK_LONG = -9187201950435737472L;
    public static final int COMPLETE = 0;
    public static final int MALFORMED = -1;
    static final int MAX_BYTES_PER_CHAR = 3;
    private static final int UNSAFE_COUNT_ASCII_THRESHOLD = 16;
    private static final Logger logger = Logger.getLogger(Utf8.class.getName());
    private static final Processor processor = (UnsafeProcessor.isAvailable() ? new UnsafeProcessor() : new SafeProcessor());

    public static boolean isValidUtf8(byte[] bytes) {
        return processor.isValidUtf8(bytes, 0, bytes.length);
    }

    public static boolean isValidUtf8(byte[] bytes, int index, int limit) {
        return processor.isValidUtf8(bytes, index, limit);
    }

    public static int partialIsValidUtf8(int state, byte[] bytes, int index, int limit) {
        return processor.partialIsValidUtf8(state, bytes, index, limit);
    }

    /* access modifiers changed from: private */
    public static int incompleteStateFor(int byte1) {
        if (byte1 > -12) {
            return -1;
        }
        return byte1;
    }

    /* access modifiers changed from: private */
    public static int incompleteStateFor(int byte1, int byte2) {
        if (byte1 > -12 || byte2 > -65) {
            return -1;
        }
        return (byte2 << 8) ^ byte1;
    }

    /* access modifiers changed from: private */
    public static int incompleteStateFor(int byte1, int byte2, int byte3) {
        if (byte1 > -12 || byte2 > -65 || byte3 > -65) {
            return -1;
        }
        return ((byte2 << 8) ^ byte1) ^ (byte3 << 16);
    }

    /* access modifiers changed from: private */
    public static int incompleteStateFor(byte[] bytes, int index, int limit) {
        byte b = bytes[index - 1];
        int i = limit - index;
        if (i == 0) {
            return incompleteStateFor(b);
        }
        if (i == 1) {
            return incompleteStateFor(b, bytes[index]);
        }
        if (i == 2) {
            return incompleteStateFor(b, bytes[index], bytes[index + 1]);
        }
        throw new AssertionError();
    }

    /* access modifiers changed from: private */
    public static int incompleteStateFor(ByteBuffer buffer, int byte1, int index, int remaining) {
        if (remaining == 0) {
            return incompleteStateFor(byte1);
        }
        if (remaining == 1) {
            return incompleteStateFor(byte1, buffer.get(index));
        }
        if (remaining == 2) {
            return incompleteStateFor(byte1, buffer.get(index), buffer.get(index + 1));
        }
        throw new AssertionError();
    }

    /* access modifiers changed from: package-private */
    public static class UnpairedSurrogateException extends IllegalArgumentException {
        private UnpairedSurrogateException(int index, int length) {
            super("Unpaired surrogate at index " + index + " of " + length);
        }
    }

    static int encodedLength(CharSequence sequence) {
        int utf16Length = sequence.length();
        int utf8Length = utf16Length;
        int i = 0;
        while (i < utf16Length && sequence.charAt(i) < 128) {
            i++;
        }
        while (true) {
            if (i < utf16Length) {
                char c = sequence.charAt(i);
                if (c >= 2048) {
                    utf8Length += encodedLengthGeneral(sequence, i);
                    break;
                }
                utf8Length += (127 - c) >>> 31;
                i++;
            } else {
                break;
            }
        }
        if (utf8Length >= utf16Length) {
            return utf8Length;
        }
        throw new IllegalArgumentException("UTF-8 length does not fit in int: " + (((long) utf8Length) + 4294967296L));
    }

    private static int encodedLengthGeneral(CharSequence sequence, int start) {
        int utf16Length = sequence.length();
        int utf8Length = 0;
        int i = start;
        while (i < utf16Length) {
            char c = sequence.charAt(i);
            if (c < 2048) {
                utf8Length += (127 - c) >>> 31;
            } else {
                utf8Length += 2;
                if (55296 <= c && c <= 57343) {
                    if (Character.codePointAt(sequence, i) >= 65536) {
                        i++;
                    } else {
                        throw new UnpairedSurrogateException(i, utf16Length);
                    }
                }
            }
            i++;
        }
        return utf8Length;
    }

    static int encode(CharSequence in, byte[] out, int offset, int length) {
        return processor.encodeUtf8(in, out, offset, length);
    }

    static boolean isValidUtf8(ByteBuffer buffer) {
        return processor.isValidUtf8(buffer, buffer.position(), buffer.remaining());
    }

    static int partialIsValidUtf8(int state, ByteBuffer buffer, int index, int limit) {
        return processor.partialIsValidUtf8(state, buffer, index, limit);
    }

    static void encodeUtf8(CharSequence in, ByteBuffer out) {
        processor.encodeUtf8(in, out);
    }

    /* access modifiers changed from: private */
    public static int estimateConsecutiveAscii(ByteBuffer buffer, int index, int limit) {
        int i = index;
        int lim = limit - 7;
        while (i < lim && (buffer.getLong(i) & ASCII_MASK_LONG) == 0) {
            i += 8;
        }
        return i - index;
    }

    /* access modifiers changed from: package-private */
    public static abstract class Processor {
        /* access modifiers changed from: package-private */
        public abstract int encodeUtf8(CharSequence charSequence, byte[] bArr, int i, int i2);

        /* access modifiers changed from: package-private */
        public abstract void encodeUtf8Direct(CharSequence charSequence, ByteBuffer byteBuffer);

        /* access modifiers changed from: package-private */
        public abstract int partialIsValidUtf8(int i, byte[] bArr, int i2, int i3);

        /* access modifiers changed from: package-private */
        public abstract int partialIsValidUtf8Direct(int i, ByteBuffer byteBuffer, int i2, int i3);

        Processor() {
        }

        /* access modifiers changed from: package-private */
        public final boolean isValidUtf8(byte[] bytes, int index, int limit) {
            return partialIsValidUtf8(0, bytes, index, limit) == 0;
        }

        /* access modifiers changed from: package-private */
        public final boolean isValidUtf8(ByteBuffer buffer, int index, int limit) {
            return partialIsValidUtf8(0, buffer, index, limit) == 0;
        }

        /* access modifiers changed from: package-private */
        public final int partialIsValidUtf8(int state, ByteBuffer buffer, int index, int limit) {
            if (buffer.hasArray()) {
                int offset = buffer.arrayOffset();
                return partialIsValidUtf8(state, buffer.array(), offset + index, offset + limit);
            } else if (buffer.isDirect()) {
                return partialIsValidUtf8Direct(state, buffer, index, limit);
            } else {
                return partialIsValidUtf8Default(state, buffer, index, limit);
            }
        }

        /* access modifiers changed from: package-private */
        public final int partialIsValidUtf8Default(int state, ByteBuffer buffer, int index, int limit) {
            int index2;
            if (state == 0) {
                index2 = index;
            } else if (index >= limit) {
                return state;
            } else {
                byte byte1 = (byte) state;
                if (byte1 < -32) {
                    if (byte1 >= -62) {
                        index2 = index + 1;
                        if (buffer.get(index) > -65) {
                        }
                    }
                    return -1;
                } else if (byte1 < -16) {
                    byte byte2 = (byte) (~(state >> 8));
                    if (byte2 == 0) {
                        int index3 = index + 1;
                        byte2 = buffer.get(index);
                        if (index3 >= limit) {
                            return Utf8.incompleteStateFor(byte1, byte2);
                        }
                        index = index3;
                    }
                    if (byte2 <= -65 && ((byte1 != -32 || byte2 >= -96) && (byte1 != -19 || byte2 < -96))) {
                        index2 = index + 1;
                        if (buffer.get(index) > -65) {
                        }
                    }
                    return -1;
                } else {
                    byte byte22 = (byte) (~(state >> 8));
                    byte byte3 = 0;
                    if (byte22 == 0) {
                        int index4 = index + 1;
                        byte22 = buffer.get(index);
                        if (index4 >= limit) {
                            return Utf8.incompleteStateFor(byte1, byte22);
                        }
                        index = index4;
                    } else {
                        byte3 = (byte) (state >> 16);
                    }
                    if (byte3 == 0) {
                        int index5 = index + 1;
                        byte3 = buffer.get(index);
                        if (index5 >= limit) {
                            return Utf8.incompleteStateFor(byte1, byte22, byte3);
                        }
                        index = index5;
                    }
                    if (byte22 <= -65 && (((byte1 << 28) + (byte22 + 112)) >> 30) == 0 && byte3 <= -65) {
                        int index6 = index + 1;
                        if (buffer.get(index) <= -65) {
                            index2 = index6;
                        }
                    }
                    return -1;
                }
            }
            return partialIsValidUtf8(buffer, index2, limit);
        }

        private static int partialIsValidUtf8(ByteBuffer buffer, int index, int limit) {
            int index2 = index + Utf8.estimateConsecutiveAscii(buffer, index, limit);
            while (index2 < limit) {
                int index3 = index2 + 1;
                int byte1 = buffer.get(index2);
                if (byte1 >= 0) {
                    index2 = index3;
                } else if (byte1 < -32) {
                    if (index3 >= limit) {
                        return byte1;
                    }
                    if (byte1 < -62 || buffer.get(index3) > -65) {
                        return -1;
                    }
                    index2 = index3 + 1;
                } else if (byte1 < -16) {
                    if (index3 >= limit - 1) {
                        return Utf8.incompleteStateFor(buffer, byte1, index3, limit - index3);
                    }
                    int index4 = index3 + 1;
                    byte byte2 = buffer.get(index3);
                    if (byte2 > -65 || ((byte1 == -32 && byte2 < -96) || ((byte1 == -19 && byte2 >= -96) || buffer.get(index4) > -65))) {
                        return -1;
                    }
                    index2 = index4 + 1;
                } else if (index3 >= limit - 2) {
                    return Utf8.incompleteStateFor(buffer, byte1, index3, limit - index3);
                } else {
                    int index5 = index3 + 1;
                    int byte22 = buffer.get(index3);
                    if (byte22 <= -65 && (((byte1 << 28) + (byte22 + ISupplicantStaIfaceCallback.StatusCode.FILS_AUTHENTICATION_FAILURE)) >> 30) == 0) {
                        int index6 = index5 + 1;
                        if (buffer.get(index5) <= -65) {
                            index2 = index6 + 1;
                            if (buffer.get(index6) > -65) {
                            }
                        }
                    }
                    return -1;
                }
            }
            return 0;
        }

        /* access modifiers changed from: package-private */
        public final void encodeUtf8(CharSequence in, ByteBuffer out) {
            if (out.hasArray()) {
                int offset = out.arrayOffset();
                out.position(Utf8.encode(in, out.array(), out.position() + offset, out.remaining()) - offset);
            } else if (out.isDirect()) {
                encodeUtf8Direct(in, out);
            } else {
                encodeUtf8Default(in, out);
            }
        }

        /* access modifiers changed from: package-private */
        public final void encodeUtf8Default(CharSequence in, ByteBuffer out) {
            int inLength = in.length();
            int outIx = out.position();
            int inIx = 0;
            while (inIx < inLength) {
                try {
                    char c = in.charAt(inIx);
                    if (c >= 128) {
                        break;
                    }
                    out.put(outIx + inIx, (byte) c);
                    inIx++;
                } catch (IndexOutOfBoundsException e) {
                    throw new ArrayIndexOutOfBoundsException("Failed writing " + in.charAt(inIx) + " at index " + (out.position() + Math.max(inIx, (outIx - out.position()) + 1)));
                }
            }
            if (inIx == inLength) {
                out.position(outIx + inIx);
                return;
            }
            int outIx2 = outIx + inIx;
            while (inIx < inLength) {
                char c2 = in.charAt(inIx);
                if (c2 < 128) {
                    out.put(outIx2, (byte) c2);
                } else if (c2 < 2048) {
                    int outIx3 = outIx2 + 1;
                    try {
                        out.put(outIx2, (byte) ((c2 >>> 6) | WifiConfigManager.SCAN_CACHE_ENTRIES_MAX_SIZE));
                        out.put(outIx3, (byte) ((c2 & '?') | 128));
                        outIx2 = outIx3;
                    } catch (IndexOutOfBoundsException e2) {
                        outIx = outIx3;
                        throw new ArrayIndexOutOfBoundsException("Failed writing " + in.charAt(inIx) + " at index " + (out.position() + Math.max(inIx, (outIx - out.position()) + 1)));
                    }
                } else if (c2 < 55296 || 57343 < c2) {
                    int outIx4 = outIx2 + 1;
                    out.put(outIx2, (byte) ((c2 >>> '\f') | 224));
                    outIx2 = outIx4 + 1;
                    out.put(outIx4, (byte) (((c2 >>> 6) & 63) | 128));
                    out.put(outIx2, (byte) ((c2 & '?') | 128));
                } else {
                    if (inIx + 1 != inLength) {
                        inIx++;
                        char low = in.charAt(inIx);
                        if (Character.isSurrogatePair(c2, low)) {
                            int codePoint = Character.toCodePoint(c2, low);
                            int outIx5 = outIx2 + 1;
                            try {
                                out.put(outIx2, (byte) ((codePoint >>> 18) | 240));
                                int outIx6 = outIx5 + 1;
                                out.put(outIx5, (byte) (((codePoint >>> 12) & 63) | 128));
                                int outIx7 = outIx6 + 1;
                                out.put(outIx6, (byte) (((codePoint >>> 6) & 63) | 128));
                                out.put(outIx7, (byte) ((codePoint & 63) | 128));
                                outIx2 = outIx7;
                            } catch (IndexOutOfBoundsException e3) {
                                outIx = outIx5;
                                throw new ArrayIndexOutOfBoundsException("Failed writing " + in.charAt(inIx) + " at index " + (out.position() + Math.max(inIx, (outIx - out.position()) + 1)));
                            }
                        }
                    }
                    throw new UnpairedSurrogateException(inIx, inLength);
                }
                inIx++;
                outIx2++;
            }
            out.position(outIx2);
        }
    }

    static final class SafeProcessor extends Processor {
        SafeProcessor() {
        }

        /* access modifiers changed from: package-private */
        @Override // com.google.protobuf.Utf8.Processor
        public int partialIsValidUtf8(int state, byte[] bytes, int index, int limit) {
            int index2;
            if (state == 0) {
                index2 = index;
            } else if (index >= limit) {
                return state;
            } else {
                int byte1 = (byte) state;
                if (byte1 < -32) {
                    if (byte1 >= -62) {
                        index2 = index + 1;
                        if (bytes[index] > -65) {
                        }
                    }
                    return -1;
                } else if (byte1 < -16) {
                    byte b = (byte) (~(state >> 8));
                    if (b == 0) {
                        int index3 = index + 1;
                        b = bytes[index];
                        if (index3 >= limit) {
                            return Utf8.incompleteStateFor(byte1, b);
                        }
                        index = index3;
                    }
                    if (b <= -65 && ((byte1 != -32 || b >= -96) && (byte1 != -19 || b < -96))) {
                        index2 = index + 1;
                        if (bytes[index] > -65) {
                        }
                    }
                    return -1;
                } else {
                    byte b2 = (byte) (~(state >> 8));
                    byte b3 = 0;
                    if (b2 == 0) {
                        int index4 = index + 1;
                        b2 = bytes[index];
                        if (index4 >= limit) {
                            return Utf8.incompleteStateFor(byte1, b2);
                        }
                        index = index4;
                    } else {
                        b3 = (byte) (state >> 16);
                    }
                    if (b3 == 0) {
                        int index5 = index + 1;
                        b3 = bytes[index];
                        if (index5 >= limit) {
                            return Utf8.incompleteStateFor(byte1, b2, b3);
                        }
                        index = index5;
                    }
                    if (b2 <= -65 && (((byte1 << 28) + (b2 + 112)) >> 30) == 0 && b3 <= -65) {
                        int index6 = index + 1;
                        if (bytes[index] <= -65) {
                            index2 = index6;
                        }
                    }
                    return -1;
                }
            }
            return partialIsValidUtf8(bytes, index2, limit);
        }

        /* access modifiers changed from: package-private */
        @Override // com.google.protobuf.Utf8.Processor
        public int partialIsValidUtf8Direct(int state, ByteBuffer buffer, int index, int limit) {
            return partialIsValidUtf8Default(state, buffer, index, limit);
        }

        /* access modifiers changed from: package-private */
        @Override // com.google.protobuf.Utf8.Processor
        public int encodeUtf8(CharSequence in, byte[] out, int offset, int length) {
            char c;
            int utf16Length = in.length();
            int i = 0;
            int limit = offset + length;
            while (i < utf16Length && i + offset < limit && (c = in.charAt(i)) < 128) {
                out[offset + i] = (byte) c;
                i++;
            }
            if (i == utf16Length) {
                return offset + utf16Length;
            }
            int j = offset + i;
            while (i < utf16Length) {
                char c2 = in.charAt(i);
                if (c2 < 128 && j < limit) {
                    out[j] = (byte) c2;
                    j++;
                } else if (c2 < 2048 && j <= limit - 2) {
                    int j2 = j + 1;
                    out[j] = (byte) ((c2 >>> 6) | 960);
                    j = j2 + 1;
                    out[j2] = (byte) ((c2 & '?') | 128);
                } else if ((c2 < 55296 || 57343 < c2) && j <= limit - 3) {
                    int j3 = j + 1;
                    out[j] = (byte) ((c2 >>> '\f') | 480);
                    int j4 = j3 + 1;
                    out[j3] = (byte) (((c2 >>> 6) & 63) | 128);
                    out[j4] = (byte) ((c2 & '?') | 128);
                    j = j4 + 1;
                } else if (j <= limit - 4) {
                    if (i + 1 != in.length()) {
                        i++;
                        char low = in.charAt(i);
                        if (Character.isSurrogatePair(c2, low)) {
                            int codePoint = Character.toCodePoint(c2, low);
                            int j5 = j + 1;
                            out[j] = (byte) ((codePoint >>> 18) | 240);
                            int j6 = j5 + 1;
                            out[j5] = (byte) (((codePoint >>> 12) & 63) | 128);
                            int j7 = j6 + 1;
                            out[j6] = (byte) (((codePoint >>> 6) & 63) | 128);
                            j = j7 + 1;
                            out[j7] = (byte) ((codePoint & 63) | 128);
                        }
                    }
                    throw new UnpairedSurrogateException(i - 1, utf16Length);
                } else if (55296 > c2 || c2 > 57343 || (i + 1 != in.length() && Character.isSurrogatePair(c2, in.charAt(i + 1)))) {
                    throw new ArrayIndexOutOfBoundsException("Failed writing " + c2 + " at index " + j);
                } else {
                    throw new UnpairedSurrogateException(i, utf16Length);
                }
                i++;
            }
            return j;
        }

        /* access modifiers changed from: package-private */
        @Override // com.google.protobuf.Utf8.Processor
        public void encodeUtf8Direct(CharSequence in, ByteBuffer out) {
            encodeUtf8Default(in, out);
        }

        private static int partialIsValidUtf8(byte[] bytes, int index, int limit) {
            while (index < limit && bytes[index] >= 0) {
                index++;
            }
            if (index >= limit) {
                return 0;
            }
            return partialIsValidUtf8NonAscii(bytes, index, limit);
        }

        /* JADX INFO: Multiple debug info for r7v2 byte: [D('index' int), D('byte1' int)] */
        /* JADX INFO: Multiple debug info for r0v2 byte: [D('index' int), D('byte2' int)] */
        /* JADX INFO: Multiple debug info for r0v8 byte: [D('index' int), D('byte2' int)] */
        private static int partialIsValidUtf8NonAscii(byte[] bytes, int index, int limit) {
            while (index < limit) {
                int index2 = index + 1;
                byte b = bytes[index];
                if (b >= 0) {
                    index = index2;
                } else if (b < -32) {
                    if (index2 >= limit) {
                        return b;
                    }
                    if (b >= -62) {
                        index = index2 + 1;
                        if (bytes[index2] > -65) {
                        }
                    }
                    return -1;
                } else if (b < -16) {
                    if (index2 >= limit - 1) {
                        return Utf8.incompleteStateFor(bytes, index2, limit);
                    }
                    int index3 = index2 + 1;
                    byte b2 = bytes[index2];
                    if (b2 <= -65 && ((b != -32 || b2 >= -96) && (b != -19 || b2 < -96))) {
                        index = index3 + 1;
                        if (bytes[index3] > -65) {
                        }
                    }
                    return -1;
                } else if (index2 >= limit - 2) {
                    return Utf8.incompleteStateFor(bytes, index2, limit);
                } else {
                    int index4 = index2 + 1;
                    byte b3 = bytes[index2];
                    if (b3 <= -65 && (((b << 28) + (b3 + 112)) >> 30) == 0) {
                        int index5 = index4 + 1;
                        if (bytes[index4] <= -65) {
                            index = index5 + 1;
                            if (bytes[index5] > -65) {
                            }
                        }
                    }
                    return -1;
                }
            }
            return 0;
        }
    }

    /* access modifiers changed from: package-private */
    public static final class UnsafeProcessor extends Processor {
        private static final int ARRAY_BASE_OFFSET = byteArrayBaseOffset();
        private static final boolean AVAILABLE = (BUFFER_ADDRESS_OFFSET != -1 && ARRAY_BASE_OFFSET % 8 == 0);
        private static final long BUFFER_ADDRESS_OFFSET = fieldOffset(field(Buffer.class, "address"));
        private static final Unsafe UNSAFE = getUnsafe();

        UnsafeProcessor() {
        }

        static boolean isAvailable() {
            return AVAILABLE;
        }

        /* access modifiers changed from: package-private */
        @Override // com.google.protobuf.Utf8.Processor
        public int partialIsValidUtf8(int state, byte[] bytes, int index, int limit) {
            long offset;
            long offset2;
            long offset3;
            if ((index | limit | (bytes.length - limit)) >= 0) {
                int i = ARRAY_BASE_OFFSET;
                long offset4 = (long) (i + index);
                long offsetLimit = (long) (i + limit);
                if (state == 0) {
                    offset = offset4;
                } else if (offset4 >= offsetLimit) {
                    return state;
                } else {
                    int byte1 = (byte) state;
                    if (byte1 < -32) {
                        if (byte1 >= -62) {
                            offset = 1 + offset4;
                            if (UNSAFE.getByte(bytes, offset4) > -65) {
                            }
                        }
                        return -1;
                    } else if (byte1 < -16) {
                        int byte2 = (byte) (~(state >> 8));
                        if (byte2 == 0) {
                            offset3 = offset4 + 1;
                            byte2 = UNSAFE.getByte(bytes, offset4);
                            if (offset3 >= offsetLimit) {
                                return Utf8.incompleteStateFor(byte1, byte2);
                            }
                        } else {
                            offset3 = offset4;
                        }
                        if (byte2 <= -65 && ((byte1 != -32 || byte2 >= -96) && (byte1 != -19 || byte2 < -96))) {
                            offset = 1 + offset3;
                            if (UNSAFE.getByte(bytes, offset3) > -65) {
                            }
                        }
                        return -1;
                    } else {
                        int byte22 = (byte) (~(state >> 8));
                        int byte3 = 0;
                        if (byte22 == 0) {
                            offset2 = offset4 + 1;
                            byte22 = UNSAFE.getByte(bytes, offset4);
                            if (offset2 >= offsetLimit) {
                                return Utf8.incompleteStateFor(byte1, byte22);
                            }
                        } else {
                            byte3 = (byte) (state >> 16);
                            offset2 = offset4;
                        }
                        if (byte3 == 0) {
                            long offset5 = offset2 + 1;
                            byte3 = UNSAFE.getByte(bytes, offset2);
                            if (offset5 >= offsetLimit) {
                                return Utf8.incompleteStateFor(byte1, byte22, byte3);
                            }
                            offset2 = offset5;
                        }
                        if (byte22 <= -65 && (((byte1 << 28) + (byte22 + ISupplicantStaIfaceCallback.StatusCode.FILS_AUTHENTICATION_FAILURE)) >> 30) == 0 && byte3 <= -65) {
                            offset = 1 + offset2;
                            if (UNSAFE.getByte(bytes, offset2) > -65) {
                            }
                        }
                        return -1;
                    }
                }
                return partialIsValidUtf8(bytes, offset, (int) (offsetLimit - offset));
            }
            throw new ArrayIndexOutOfBoundsException(String.format("Array length=%d, index=%d, limit=%d", Integer.valueOf(bytes.length), Integer.valueOf(index), Integer.valueOf(limit)));
        }

        /* access modifiers changed from: package-private */
        @Override // com.google.protobuf.Utf8.Processor
        public int partialIsValidUtf8Direct(int state, ByteBuffer buffer, int index, int limit) {
            long address;
            long address2;
            long address3;
            if ((index | limit | (buffer.limit() - limit)) >= 0) {
                long address4 = addressOffset(buffer) + ((long) index);
                long addressLimit = ((long) (limit - index)) + address4;
                if (state == 0) {
                    address = address4;
                } else if (address4 >= addressLimit) {
                    return state;
                } else {
                    int byte1 = (byte) state;
                    if (byte1 < -32) {
                        if (byte1 >= -62) {
                            address = 1 + address4;
                            if (UNSAFE.getByte(address4) > -65) {
                            }
                        }
                        return -1;
                    } else if (byte1 < -16) {
                        int byte2 = (byte) (~(state >> 8));
                        if (byte2 == 0) {
                            address3 = address4 + 1;
                            byte2 = UNSAFE.getByte(address4);
                            if (address3 >= addressLimit) {
                                return Utf8.incompleteStateFor(byte1, byte2);
                            }
                        } else {
                            address3 = address4;
                        }
                        if (byte2 <= -65 && ((byte1 != -32 || byte2 >= -96) && (byte1 != -19 || byte2 < -96))) {
                            address = 1 + address3;
                            if (UNSAFE.getByte(address3) > -65) {
                            }
                        }
                        return -1;
                    } else {
                        int byte22 = (byte) (~(state >> 8));
                        int byte3 = 0;
                        if (byte22 == 0) {
                            address2 = address4 + 1;
                            byte22 = UNSAFE.getByte(address4);
                            if (address2 >= addressLimit) {
                                return Utf8.incompleteStateFor(byte1, byte22);
                            }
                        } else {
                            byte3 = (byte) (state >> 16);
                            address2 = address4;
                        }
                        if (byte3 == 0) {
                            long address5 = address2 + 1;
                            byte3 = UNSAFE.getByte(address2);
                            if (address5 >= addressLimit) {
                                return Utf8.incompleteStateFor(byte1, byte22, byte3);
                            }
                            address2 = address5;
                        }
                        if (byte22 <= -65 && (((byte1 << 28) + (byte22 + ISupplicantStaIfaceCallback.StatusCode.FILS_AUTHENTICATION_FAILURE)) >> 30) == 0 && byte3 <= -65) {
                            address = 1 + address2;
                            if (UNSAFE.getByte(address2) > -65) {
                            }
                        }
                        return -1;
                    }
                }
                return partialIsValidUtf8(address, (int) (addressLimit - address));
            }
            throw new ArrayIndexOutOfBoundsException(String.format("buffer limit=%d, index=%d, limit=%d", Integer.valueOf(buffer.limit()), Integer.valueOf(index), Integer.valueOf(limit)));
        }

        /* JADX INFO: Multiple debug info for r6v10 long: [D('outLimit' long), D('outIx' long)] */
        /* access modifiers changed from: package-private */
        @Override // com.google.protobuf.Utf8.Processor
        public int encodeUtf8(CharSequence in, byte[] out, int offset, int length) {
            char c;
            long j;
            long j2;
            long outLimit;
            String str;
            String str2;
            char c2;
            long outIx = (long) (ARRAY_BASE_OFFSET + offset);
            long outLimit2 = ((long) length) + outIx;
            int inLimit = in.length();
            String str3 = " at index ";
            String str4 = "Failed writing ";
            if (inLimit > length || out.length - length < offset) {
                throw new ArrayIndexOutOfBoundsException(str4 + in.charAt(inLimit - 1) + str3 + (offset + length));
            }
            int inIx = 0;
            while (true) {
                c = 128;
                j = 1;
                if (inIx < inLimit) {
                    char c3 = in.charAt(inIx);
                    if (c3 >= 128) {
                        break;
                    }
                    UNSAFE.putByte(out, outIx, (byte) c3);
                    inIx++;
                    outIx = 1 + outIx;
                } else {
                    break;
                }
            }
            if (inIx == inLimit) {
                return (int) (outIx - ((long) ARRAY_BASE_OFFSET));
            }
            while (inIx < inLimit) {
                char c4 = in.charAt(inIx);
                if (c4 < c && outIx < outLimit2) {
                    UNSAFE.putByte(out, outIx, (byte) c4);
                    str2 = str3;
                    outIx += j;
                    j2 = 1;
                    outLimit = outLimit2;
                    str = str4;
                    c2 = 128;
                } else if (c4 >= 2048 || outIx > outLimit2 - 2) {
                    if (c4 >= 55296 && 57343 >= c4) {
                        str2 = str3;
                        str = str4;
                    } else if (outIx <= outLimit2 - 3) {
                        str2 = str3;
                        str = str4;
                        long outIx2 = outIx + 1;
                        UNSAFE.putByte(out, outIx, (byte) ((c4 >>> '\f') | 480));
                        long outIx3 = outIx2 + 1;
                        UNSAFE.putByte(out, outIx2, (byte) (((c4 >>> 6) & 63) | 128));
                        UNSAFE.putByte(out, outIx3, (byte) ((c4 & '?') | 128));
                        outLimit = outLimit2;
                        outIx = outIx3 + 1;
                        c2 = 128;
                        j2 = 1;
                    } else {
                        str2 = str3;
                        str = str4;
                    }
                    if (outIx <= outLimit2 - 4) {
                        if (inIx + 1 != inLimit) {
                            inIx++;
                            char low = in.charAt(inIx);
                            if (Character.isSurrogatePair(c4, low)) {
                                int codePoint = Character.toCodePoint(c4, low);
                                outLimit = outLimit2;
                                long outIx4 = outIx + 1;
                                UNSAFE.putByte(out, outIx, (byte) ((codePoint >>> 18) | 240));
                                long outIx5 = outIx4 + 1;
                                UNSAFE.putByte(out, outIx4, (byte) (((codePoint >>> 12) & 63) | 128));
                                long outIx6 = outIx5 + 1;
                                c2 = 128;
                                UNSAFE.putByte(out, outIx5, (byte) (((codePoint >>> 6) & 63) | 128));
                                j2 = 1;
                                UNSAFE.putByte(out, outIx6, (byte) ((codePoint & 63) | 128));
                                outIx = outIx6 + 1;
                            }
                        }
                        throw new UnpairedSurrogateException(inIx - 1, inLimit);
                    } else if (55296 > c4 || c4 > 57343 || (inIx + 1 != inLimit && Character.isSurrogatePair(c4, in.charAt(inIx + 1)))) {
                        throw new ArrayIndexOutOfBoundsException(str + c4 + str2 + outIx);
                    } else {
                        throw new UnpairedSurrogateException(inIx, inLimit);
                    }
                } else {
                    long outIx7 = outIx + 1;
                    UNSAFE.putByte(out, outIx, (byte) ((c4 >>> 6) | 960));
                    UNSAFE.putByte(out, outIx7, (byte) ((c4 & '?') | 128));
                    str2 = str3;
                    outIx = outIx7 + 1;
                    j2 = 1;
                    outLimit = outLimit2;
                    str = str4;
                    c2 = 128;
                }
                inIx++;
                c = c2;
                str3 = str2;
                str4 = str;
                outLimit2 = outLimit;
                j = j2;
            }
            return (int) (outIx - ((long) ARRAY_BASE_OFFSET));
        }

        /* JADX INFO: Multiple debug info for r6v9 long: [D('outLimit' long), D('outIx' long)] */
        /* access modifiers changed from: package-private */
        @Override // com.google.protobuf.Utf8.Processor
        public void encodeUtf8Direct(CharSequence in, ByteBuffer out) {
            char c;
            long j;
            long outIx;
            long outLimit;
            long outIx2;
            char c2;
            long address = addressOffset(out);
            long outIx3 = ((long) out.position()) + address;
            long outLimit2 = ((long) out.limit()) + address;
            int inLimit = in.length();
            if (((long) inLimit) <= outLimit2 - outIx3) {
                int inIx = 0;
                while (true) {
                    c = 128;
                    j = 1;
                    if (inIx < inLimit) {
                        char c3 = in.charAt(inIx);
                        if (c3 >= 128) {
                            break;
                        }
                        UNSAFE.putByte(outIx3, (byte) c3);
                        inIx++;
                        outIx3 = 1 + outIx3;
                    } else {
                        break;
                    }
                }
                if (inIx == inLimit) {
                    out.position((int) (outIx3 - address));
                    return;
                }
                while (inIx < inLimit) {
                    char c4 = in.charAt(inIx);
                    if (c4 < c && outIx3 < outLimit2) {
                        UNSAFE.putByte(outIx3, (byte) c4);
                        outLimit = outLimit2;
                        outIx3 += j;
                        c2 = 128;
                        outIx = 1;
                        outIx2 = address;
                    } else if (c4 >= 2048 || outIx3 > outLimit2 - 2) {
                        outIx2 = address;
                        if ((c4 < 55296 || 57343 < c4) && outIx3 <= outLimit2 - 3) {
                            long outIx4 = outIx3 + 1;
                            UNSAFE.putByte(outIx3, (byte) ((c4 >>> '\f') | 480));
                            long outIx5 = outIx4 + 1;
                            UNSAFE.putByte(outIx4, (byte) (((c4 >>> 6) & 63) | 128));
                            UNSAFE.putByte(outIx5, (byte) ((c4 & '?') | 128));
                            outLimit = outLimit2;
                            outIx3 = outIx5 + 1;
                            c2 = 128;
                            outIx = 1;
                        } else if (outIx3 <= outLimit2 - 4) {
                            if (inIx + 1 != inLimit) {
                                inIx++;
                                char low = in.charAt(inIx);
                                if (Character.isSurrogatePair(c4, low)) {
                                    int codePoint = Character.toCodePoint(c4, low);
                                    outLimit = outLimit2;
                                    long outIx6 = outIx3 + 1;
                                    UNSAFE.putByte(outIx3, (byte) ((codePoint >>> 18) | 240));
                                    long outIx7 = outIx6 + 1;
                                    UNSAFE.putByte(outIx6, (byte) (((codePoint >>> 12) & 63) | 128));
                                    long outIx8 = outIx7 + 1;
                                    c2 = 128;
                                    UNSAFE.putByte(outIx7, (byte) (((codePoint >>> 6) & 63) | 128));
                                    outIx = 1;
                                    outIx3 = outIx8 + 1;
                                    UNSAFE.putByte(outIx8, (byte) ((codePoint & 63) | 128));
                                }
                            }
                            throw new UnpairedSurrogateException(inIx - 1, inLimit);
                        } else if (55296 > c4 || c4 > 57343 || (inIx + 1 != inLimit && Character.isSurrogatePair(c4, in.charAt(inIx + 1)))) {
                            throw new ArrayIndexOutOfBoundsException("Failed writing " + c4 + " at index " + outIx3);
                        } else {
                            throw new UnpairedSurrogateException(inIx, inLimit);
                        }
                    } else {
                        outIx2 = address;
                        long outIx9 = outIx3 + 1;
                        UNSAFE.putByte(outIx3, (byte) ((c4 >>> 6) | 960));
                        outIx3 = outIx9 + 1;
                        UNSAFE.putByte(outIx9, (byte) ((c4 & '?') | 128));
                        outLimit = outLimit2;
                        c2 = 128;
                        outIx = 1;
                    }
                    inIx++;
                    c = c2;
                    address = outIx2;
                    outLimit2 = outLimit;
                    j = outIx;
                }
                out.position((int) (outIx3 - address));
                return;
            }
            throw new ArrayIndexOutOfBoundsException("Failed writing " + in.charAt(inLimit - 1) + " at index " + out.limit());
        }

        private static int unsafeEstimateConsecutiveAscii(byte[] bytes, long offset, int maxChars) {
            if (maxChars < 16) {
                return 0;
            }
            int unaligned = ((int) offset) & 7;
            int j = unaligned;
            while (j > 0) {
                long offset2 = 1 + offset;
                if (UNSAFE.getByte(bytes, offset) < 0) {
                    return unaligned - j;
                }
                j--;
                offset = offset2;
            }
            int remaining = maxChars - unaligned;
            while (remaining >= 8 && (UNSAFE.getLong(bytes, offset) & Utf8.ASCII_MASK_LONG) == 0) {
                offset += 8;
                remaining -= 8;
            }
            return maxChars - remaining;
        }

        private static int unsafeEstimateConsecutiveAscii(long address, int maxChars) {
            if (maxChars < 16) {
                return 0;
            }
            int unaligned = ((int) address) & 7;
            int j = unaligned;
            while (j > 0) {
                long address2 = 1 + address;
                if (UNSAFE.getByte(address) < 0) {
                    return unaligned - j;
                }
                j--;
                address = address2;
            }
            int remaining = maxChars - unaligned;
            while (remaining >= 8 && (UNSAFE.getLong(address) & Utf8.ASCII_MASK_LONG) == 0) {
                address += 8;
                remaining -= 8;
            }
            return maxChars - remaining;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:39:0x0073, code lost:
            return -1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:54:0x00ab, code lost:
            return -1;
         */
        private static int partialIsValidUtf8(byte[] bytes, long offset, int remaining) {
            int skipped = unsafeEstimateConsecutiveAscii(bytes, offset, remaining);
            int remaining2 = remaining - skipped;
            long offset2 = offset + ((long) skipped);
            while (true) {
                int byte1 = 0;
                while (true) {
                    if (remaining2 <= 0) {
                        break;
                    }
                    long offset3 = offset2 + 1;
                    int i = UNSAFE.getByte(bytes, offset2);
                    byte1 = i;
                    if (i < 0) {
                        offset2 = offset3;
                        break;
                    }
                    remaining2--;
                    offset2 = offset3;
                }
                if (remaining2 != 0) {
                    int remaining3 = remaining2 - 1;
                    if (byte1 >= -32) {
                        if (byte1 >= -16) {
                            if (remaining3 >= 3) {
                                remaining2 = remaining3 - 3;
                                long offset4 = offset2 + 1;
                                int byte2 = UNSAFE.getByte(bytes, offset2);
                                if (byte2 > -65 || (((byte1 << 28) + (byte2 + ISupplicantStaIfaceCallback.StatusCode.FILS_AUTHENTICATION_FAILURE)) >> 30) != 0) {
                                    break;
                                }
                                long offset5 = offset4 + 1;
                                if (UNSAFE.getByte(bytes, offset4) > -65) {
                                    break;
                                }
                                long offset6 = offset5 + 1;
                                if (UNSAFE.getByte(bytes, offset5) > -65) {
                                    break;
                                }
                                offset2 = offset6;
                            } else {
                                return unsafeIncompleteStateFor(bytes, byte1, offset2, remaining3);
                            }
                        } else if (remaining3 < 2) {
                            return unsafeIncompleteStateFor(bytes, byte1, offset2, remaining3);
                        } else {
                            remaining2 = remaining3 - 2;
                            long offset7 = offset2 + 1;
                            int byte22 = UNSAFE.getByte(bytes, offset2);
                            if (byte22 > -65 || ((byte1 == -32 && byte22 < -96) || (byte1 == -19 && byte22 >= -96))) {
                                break;
                            }
                            long offset8 = 1 + offset7;
                            if (UNSAFE.getByte(bytes, offset7) > -65) {
                                break;
                            }
                            offset2 = offset8;
                        }
                    } else if (remaining3 != 0) {
                        remaining2 = remaining3 - 1;
                        if (byte1 < -62) {
                            break;
                        }
                        long offset9 = 1 + offset2;
                        if (UNSAFE.getByte(bytes, offset2) > -65) {
                            break;
                        }
                        offset2 = offset9;
                    } else {
                        return byte1;
                    }
                } else {
                    return 0;
                }
            }
            return -1;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:39:0x0072, code lost:
            return -1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:54:0x00a9, code lost:
            return -1;
         */
        private static int partialIsValidUtf8(long address, int remaining) {
            int skipped = unsafeEstimateConsecutiveAscii(address, remaining);
            long address2 = address + ((long) skipped);
            int remaining2 = remaining - skipped;
            while (true) {
                int byte1 = 0;
                while (true) {
                    if (remaining2 <= 0) {
                        break;
                    }
                    long address3 = address2 + 1;
                    int i = UNSAFE.getByte(address2);
                    byte1 = i;
                    if (i < 0) {
                        address2 = address3;
                        break;
                    }
                    remaining2--;
                    address2 = address3;
                }
                if (remaining2 != 0) {
                    int remaining3 = remaining2 - 1;
                    if (byte1 >= -32) {
                        if (byte1 >= -16) {
                            if (remaining3 >= 3) {
                                remaining2 = remaining3 - 3;
                                long address4 = address2 + 1;
                                byte byte2 = UNSAFE.getByte(address2);
                                if (byte2 > -65 || (((byte1 << 28) + (byte2 + 112)) >> 30) != 0) {
                                    break;
                                }
                                long address5 = address4 + 1;
                                if (UNSAFE.getByte(address4) > -65) {
                                    break;
                                }
                                long address6 = address5 + 1;
                                if (UNSAFE.getByte(address5) > -65) {
                                    break;
                                }
                                address2 = address6;
                            } else {
                                return unsafeIncompleteStateFor(address2, byte1, remaining3);
                            }
                        } else if (remaining3 < 2) {
                            return unsafeIncompleteStateFor(address2, byte1, remaining3);
                        } else {
                            remaining2 = remaining3 - 2;
                            long address7 = address2 + 1;
                            byte byte22 = UNSAFE.getByte(address2);
                            if (byte22 > -65 || ((byte1 == -32 && byte22 < -96) || (byte1 == -19 && byte22 >= -96))) {
                                break;
                            }
                            long address8 = 1 + address7;
                            if (UNSAFE.getByte(address7) > -65) {
                                break;
                            }
                            address2 = address8;
                        }
                    } else if (remaining3 != 0) {
                        remaining2 = remaining3 - 1;
                        if (byte1 < -62) {
                            break;
                        }
                        long address9 = 1 + address2;
                        if (UNSAFE.getByte(address2) > -65) {
                            break;
                        }
                        address2 = address9;
                    } else {
                        return byte1;
                    }
                } else {
                    return 0;
                }
            }
            return -1;
        }

        private static int unsafeIncompleteStateFor(byte[] bytes, int byte1, long offset, int remaining) {
            if (remaining == 0) {
                return Utf8.incompleteStateFor(byte1);
            }
            if (remaining == 1) {
                return Utf8.incompleteStateFor(byte1, UNSAFE.getByte(bytes, offset));
            }
            if (remaining == 2) {
                return Utf8.incompleteStateFor(byte1, UNSAFE.getByte(bytes, offset), UNSAFE.getByte(bytes, 1 + offset));
            }
            throw new AssertionError();
        }

        private static int unsafeIncompleteStateFor(long address, int byte1, int remaining) {
            if (remaining == 0) {
                return Utf8.incompleteStateFor(byte1);
            }
            if (remaining == 1) {
                return Utf8.incompleteStateFor(byte1, UNSAFE.getByte(address));
            }
            if (remaining == 2) {
                return Utf8.incompleteStateFor(byte1, UNSAFE.getByte(address), UNSAFE.getByte(1 + address));
            }
            throw new AssertionError();
        }

        /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0025: APUT  (r4v1 java.lang.Object[]), (2 ??[int, float, short, byte, char]), (r5v1 java.lang.String) */
        private static Field field(Class<?> clazz, String fieldName) {
            Field field;
            try {
                field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
            } catch (Throwable th) {
                field = null;
            }
            Logger logger = Utf8.logger;
            Level level = Level.FINEST;
            Object[] objArr = new Object[3];
            objArr[0] = clazz.getName();
            objArr[1] = fieldName;
            objArr[2] = field != null ? "available" : "unavailable";
            logger.log(level, "{0}.{1}: {2}", objArr);
            return field;
        }

        private static long fieldOffset(Field field) {
            Unsafe unsafe;
            if (field == null || (unsafe = UNSAFE) == null) {
                return -1;
            }
            return unsafe.objectFieldOffset(field);
        }

        private static <T> int byteArrayBaseOffset() {
            Unsafe unsafe = UNSAFE;
            if (unsafe == null) {
                return -1;
            }
            return unsafe.arrayBaseOffset(byte[].class);
        }

        private static long addressOffset(ByteBuffer buffer) {
            return UNSAFE.getLong(buffer, BUFFER_ADDRESS_OFFSET);
        }

        private static Unsafe getUnsafe() {
            Unsafe unsafe = null;
            try {
                unsafe = (Unsafe) AccessController.doPrivileged(new PrivilegedExceptionAction<Unsafe>() {
                    /* class com.google.protobuf.Utf8.UnsafeProcessor.AnonymousClass1 */

                    @Override // java.security.PrivilegedExceptionAction
                    public Unsafe run() throws Exception {
                        UnsafeProcessor.checkRequiredMethods(Unsafe.class);
                        Field[] declaredFields = Unsafe.class.getDeclaredFields();
                        for (Field f : declaredFields) {
                            f.setAccessible(true);
                            Object x = f.get(null);
                            if (Unsafe.class.isInstance(x)) {
                                return (Unsafe) Unsafe.class.cast(x);
                            }
                        }
                        return null;
                    }
                });
            } catch (Throwable th) {
            }
            Utf8.logger.log(Level.FINEST, "sun.misc.Unsafe: {}", unsafe != null ? "available" : "unavailable");
            return unsafe;
        }

        /* access modifiers changed from: private */
        public static void checkRequiredMethods(Class<Unsafe> clazz) throws NoSuchMethodException, SecurityException {
            clazz.getMethod("arrayBaseOffset", Class.class);
            clazz.getMethod("getByte", Object.class, Long.TYPE);
            clazz.getMethod("putByte", Object.class, Long.TYPE, Byte.TYPE);
            clazz.getMethod("getLong", Object.class, Long.TYPE);
            clazz.getMethod("objectFieldOffset", Field.class);
            clazz.getMethod("getByte", Long.TYPE);
            clazz.getMethod("getLong", Object.class, Long.TYPE);
            clazz.getMethod("putByte", Long.TYPE, Byte.TYPE);
            clazz.getMethod("getLong", Long.TYPE);
        }
    }

    private Utf8() {
    }
}
