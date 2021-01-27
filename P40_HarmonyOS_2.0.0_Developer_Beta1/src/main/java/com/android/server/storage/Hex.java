package com.android.server.storage;

import com.android.server.display.HwUibcReceiver;

@Deprecated
public class Hex implements BinaryEncoder, BinaryDecoder {
    private static final char[] DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static byte[] decodeHex(char[] data) throws DecoderException {
        int len = data.length;
        if ((len & 1) == 0) {
            byte[] out = new byte[(len >> 1)];
            int i = 0;
            int j = 0;
            while (j < len) {
                int j2 = j + 1;
                j = j2 + 1;
                out[i] = (byte) (((toDigit(data[j], j) << 4) | toDigit(data[j2], j2)) & 255);
                i++;
            }
            return out;
        }
        throw new DecoderException("Odd number of characters.");
    }

    protected static int toDigit(char ch, int index) throws DecoderException {
        int digit = Character.digit(ch, 16);
        if (digit != -1) {
            return digit;
        }
        throw new DecoderException("Illegal hexadecimal charcter " + ch + " at index " + index);
    }

    public static char[] encodeHex(byte[] data) {
        int l = data.length;
        char[] out = new char[(l << 1)];
        int j = 0;
        for (int i = 0; i < l; i++) {
            int j2 = j + 1;
            char[] cArr = DIGITS;
            out[j] = cArr[(data[i] & 240) >>> 4];
            j = j2 + 1;
            out[j2] = cArr[data[i] & HwUibcReceiver.CurrentPacket.INPUT_MASK];
        }
        return out;
    }

    @Override // com.android.server.storage.BinaryDecoder
    public byte[] decode(byte[] array) throws DecoderException {
        return decodeHex(new String(array).toCharArray());
    }

    @Override // com.android.server.storage.Decoder
    public Object decode(Object object) throws DecoderException {
        try {
            return decodeHex(object instanceof String ? ((String) object).toCharArray() : (char[]) object);
        } catch (ClassCastException e) {
            throw new DecoderException(e.getMessage());
        }
    }

    @Override // com.android.server.storage.BinaryEncoder
    public byte[] encode(byte[] array) {
        return new String(encodeHex(array)).getBytes();
    }

    @Override // com.android.server.storage.Encoder
    public Object encode(Object object) throws EncoderException {
        try {
            return encodeHex(object instanceof String ? ((String) object).getBytes() : (byte[]) object);
        } catch (ClassCastException e) {
            throw new EncoderException(e.getMessage());
        }
    }
}
