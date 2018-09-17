package org.apache.commons.codec.binary;

import org.apache.commons.codec.BinaryDecoder;
import org.apache.commons.codec.BinaryEncoder;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;

@Deprecated
public class BinaryCodec implements BinaryDecoder, BinaryEncoder {
    private static final int[] BITS = null;
    private static final int BIT_0 = 1;
    private static final int BIT_1 = 2;
    private static final int BIT_2 = 4;
    private static final int BIT_3 = 8;
    private static final int BIT_4 = 16;
    private static final int BIT_5 = 32;
    private static final int BIT_6 = 64;
    private static final int BIT_7 = 128;
    private static final byte[] EMPTY_BYTE_ARRAY = null;
    private static final char[] EMPTY_CHAR_ARRAY = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: org.apache.commons.codec.binary.BinaryCodec.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: org.apache.commons.codec.binary.BinaryCodec.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: org.apache.commons.codec.binary.BinaryCodec.<clinit>():void");
    }

    public byte[] encode(byte[] raw) {
        return toAsciiBytes(raw);
    }

    public Object encode(Object raw) throws EncoderException {
        if (raw instanceof byte[]) {
            return toAsciiChars((byte[]) raw);
        }
        throw new EncoderException("argument not a byte array");
    }

    public Object decode(Object ascii) throws DecoderException {
        if (ascii == null) {
            return EMPTY_BYTE_ARRAY;
        }
        if (ascii instanceof byte[]) {
            return fromAscii((byte[]) ascii);
        }
        if (ascii instanceof char[]) {
            return fromAscii((char[]) ascii);
        }
        if (ascii instanceof String) {
            return fromAscii(((String) ascii).toCharArray());
        }
        throw new DecoderException("argument not a byte array");
    }

    public byte[] decode(byte[] ascii) {
        return fromAscii(ascii);
    }

    public byte[] toByteArray(String ascii) {
        if (ascii == null) {
            return EMPTY_BYTE_ARRAY;
        }
        return fromAscii(ascii.toCharArray());
    }

    public static byte[] fromAscii(char[] ascii) {
        if (ascii == null || ascii.length == 0) {
            return EMPTY_BYTE_ARRAY;
        }
        byte[] l_raw = new byte[(ascii.length >> 3)];
        int ii = 0;
        int jj = ascii.length - 1;
        while (ii < l_raw.length) {
            for (int bits = 0; bits < BITS.length; bits += BIT_0) {
                if (ascii[jj - bits] == '1') {
                    l_raw[ii] = (byte) (l_raw[ii] | BITS[bits]);
                }
            }
            ii += BIT_0;
            jj -= 8;
        }
        return l_raw;
    }

    public static byte[] fromAscii(byte[] ascii) {
        if (ascii == null || ascii.length == 0) {
            return EMPTY_BYTE_ARRAY;
        }
        byte[] l_raw = new byte[(ascii.length >> 3)];
        int ii = 0;
        int jj = ascii.length - 1;
        while (ii < l_raw.length) {
            for (int bits = 0; bits < BITS.length; bits += BIT_0) {
                if (ascii[jj - bits] == 49) {
                    l_raw[ii] = (byte) (l_raw[ii] | BITS[bits]);
                }
            }
            ii += BIT_0;
            jj -= 8;
        }
        return l_raw;
    }

    public static byte[] toAsciiBytes(byte[] raw) {
        if (raw == null || raw.length == 0) {
            return EMPTY_BYTE_ARRAY;
        }
        byte[] l_ascii = new byte[(raw.length << 3)];
        int ii = 0;
        int jj = l_ascii.length - 1;
        while (ii < raw.length) {
            for (int bits = 0; bits < BITS.length; bits += BIT_0) {
                if ((raw[ii] & BITS[bits]) == 0) {
                    l_ascii[jj - bits] = (byte) 48;
                } else {
                    l_ascii[jj - bits] = (byte) 49;
                }
            }
            ii += BIT_0;
            jj -= 8;
        }
        return l_ascii;
    }

    public static char[] toAsciiChars(byte[] raw) {
        if (raw == null || raw.length == 0) {
            return EMPTY_CHAR_ARRAY;
        }
        char[] l_ascii = new char[(raw.length << 3)];
        int ii = 0;
        int jj = l_ascii.length - 1;
        while (ii < raw.length) {
            for (int bits = 0; bits < BITS.length; bits += BIT_0) {
                if ((raw[ii] & BITS[bits]) == 0) {
                    l_ascii[jj - bits] = '0';
                } else {
                    l_ascii[jj - bits] = '1';
                }
            }
            ii += BIT_0;
            jj -= 8;
        }
        return l_ascii;
    }

    public static String toAsciiString(byte[] raw) {
        return new String(toAsciiChars(raw));
    }
}
