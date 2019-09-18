package org.apache.commons.codec.binary;

import org.apache.commons.codec.BinaryDecoder;
import org.apache.commons.codec.BinaryEncoder;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.apache.http.protocol.HTTP;

@Deprecated
public class Base64 implements BinaryEncoder, BinaryDecoder {
    static final int BASELENGTH = 255;
    static final byte[] CHUNK_SEPARATOR = "\r\n".getBytes();
    static final int CHUNK_SIZE = 76;
    static final int EIGHTBIT = 8;
    static final int FOURBYTE = 4;
    static final int LOOKUPLENGTH = 64;
    static final byte PAD = 61;
    static final int SIGN = -128;
    static final int SIXTEENBIT = 16;
    static final int TWENTYFOURBITGROUP = 24;
    private static byte[] base64Alphabet = new byte[BASELENGTH];
    private static byte[] lookUpBase64Alphabet = new byte[LOOKUPLENGTH];

    static {
        int j = 0;
        for (int i = 0; i < BASELENGTH; i++) {
            base64Alphabet[i] = -1;
        }
        for (int i2 = 90; i2 >= 65; i2--) {
            base64Alphabet[i2] = (byte) (i2 - 65);
        }
        for (int i3 = 122; i3 >= 97; i3--) {
            base64Alphabet[i3] = (byte) ((i3 - 97) + 26);
        }
        for (int i4 = 57; i4 >= 48; i4--) {
            base64Alphabet[i4] = (byte) ((i4 - 48) + 52);
        }
        base64Alphabet[43] = 62;
        base64Alphabet[47] = 63;
        for (int i5 = 0; i5 <= 25; i5++) {
            lookUpBase64Alphabet[i5] = (byte) (65 + i5);
        }
        int i6 = 26;
        int j2 = 0;
        while (i6 <= 51) {
            lookUpBase64Alphabet[i6] = (byte) (97 + j2);
            i6++;
            j2++;
        }
        int i7 = 52;
        while (i7 <= 61) {
            lookUpBase64Alphabet[i7] = (byte) (48 + j);
            i7++;
            j++;
        }
        lookUpBase64Alphabet[62] = 43;
        lookUpBase64Alphabet[63] = 47;
    }

    private static boolean isBase64(byte octect) {
        if (octect != 61 && base64Alphabet[octect] == -1) {
            return false;
        }
        return true;
    }

    public static boolean isArrayByteBase64(byte[] arrayOctect) {
        if (length == 0) {
            return true;
        }
        for (byte isBase64 : discardWhitespace(arrayOctect)) {
            if (!isBase64(isBase64)) {
                return false;
            }
        }
        return true;
    }

    public static byte[] encodeBase64(byte[] binaryData) {
        return encodeBase64(binaryData, false);
    }

    public static byte[] encodeBase64Chunked(byte[] binaryData) {
        return encodeBase64(binaryData, true);
    }

    public Object decode(Object pObject) throws DecoderException {
        if (pObject instanceof byte[]) {
            return decode((byte[]) pObject);
        }
        throw new DecoderException("Parameter supplied to Base64 decode is not a byte[]");
    }

    public byte[] decode(byte[] pArray) {
        return decodeBase64(pArray);
    }

    public static byte[] encodeBase64(byte[] binaryData, boolean isChunked) {
        int encodedDataLength;
        int lengthDataBits;
        int lengthDataBits2;
        int numberTriplets;
        int numberTriplets2;
        byte[] bArr = binaryData;
        int lengthDataBits3 = bArr.length * EIGHTBIT;
        int fewerThan24bits = lengthDataBits3 % TWENTYFOURBITGROUP;
        int numberTriplets3 = lengthDataBits3 / TWENTYFOURBITGROUP;
        int nbrChunks = 0;
        if (fewerThan24bits != 0) {
            encodedDataLength = (numberTriplets3 + 1) * 4;
        } else {
            encodedDataLength = numberTriplets3 * 4;
        }
        if (isChunked) {
            nbrChunks = CHUNK_SEPARATOR.length == 0 ? 0 : (int) Math.ceil((double) (((float) encodedDataLength) / 76.0f));
            encodedDataLength += CHUNK_SEPARATOR.length * nbrChunks;
        }
        byte[] encodedData = new byte[encodedDataLength];
        int chunksSoFar = CHUNK_SIZE;
        int chunksSoFar2 = 0;
        int i = 0;
        int encodedIndex = 0;
        while (i < numberTriplets3) {
            int dataIndex = i * 3;
            byte b1 = bArr[dataIndex];
            byte b2 = bArr[dataIndex + 1];
            byte b3 = bArr[dataIndex + 2];
            byte l = (byte) (b2 & 15);
            byte k = (byte) (b1 & 3);
            byte val1 = (byte) ((b1 & Byte.MIN_VALUE) == 0 ? b1 >> 2 : (b1 >> 2) ^ 192);
            if ((b2 & Byte.MIN_VALUE) == 0) {
                lengthDataBits = lengthDataBits3;
                lengthDataBits2 = b2 >> 4;
            } else {
                lengthDataBits = lengthDataBits3;
                lengthDataBits2 = (b2 >> 4) ^ 240;
            }
            byte val2 = (byte) lengthDataBits2;
            if ((b3 & Byte.MIN_VALUE) == 0) {
                numberTriplets = numberTriplets3;
                numberTriplets2 = b3 >> 6;
            } else {
                numberTriplets = numberTriplets3;
                numberTriplets2 = (b3 >> 6) ^ 252;
            }
            byte val3 = (byte) numberTriplets2;
            encodedData[encodedIndex] = lookUpBase64Alphabet[val1];
            encodedData[encodedIndex + 1] = lookUpBase64Alphabet[val2 | (k << 4)];
            encodedData[encodedIndex + 2] = lookUpBase64Alphabet[(l << 2) | val3];
            encodedData[encodedIndex + 3] = lookUpBase64Alphabet[b3 & 63];
            byte b = val2;
            int encodedIndex2 = encodedIndex + 4;
            if (!isChunked || encodedIndex2 != chunksSoFar) {
                encodedIndex = encodedIndex2;
                chunksSoFar2 = chunksSoFar2;
                chunksSoFar = chunksSoFar;
            } else {
                int i2 = chunksSoFar;
                byte b4 = val3;
                byte b5 = val1;
                System.arraycopy(CHUNK_SEPARATOR, 0, encodedData, encodedIndex2, CHUNK_SEPARATOR.length);
                chunksSoFar2++;
                chunksSoFar = (CHUNK_SIZE * (chunksSoFar2 + 1)) + (CHUNK_SEPARATOR.length * chunksSoFar2);
                encodedIndex = encodedIndex2 + CHUNK_SEPARATOR.length;
            }
            i++;
            lengthDataBits3 = lengthDataBits;
            numberTriplets3 = numberTriplets;
        }
        int i3 = chunksSoFar;
        int i4 = numberTriplets3;
        int nextSeparatorIndex = chunksSoFar2;
        int dataIndex2 = i * 3;
        if (fewerThan24bits == EIGHTBIT) {
            byte b12 = bArr[dataIndex2];
            byte k2 = (byte) (b12 & 3);
            encodedData[encodedIndex] = lookUpBase64Alphabet[(byte) ((b12 & Byte.MIN_VALUE) == 0 ? b12 >> 2 : (b12 >> 2) ^ 192)];
            encodedData[encodedIndex + 1] = lookUpBase64Alphabet[k2 << 4];
            encodedData[encodedIndex + 2] = PAD;
            encodedData[encodedIndex + 3] = PAD;
        } else if (fewerThan24bits == 16) {
            byte b13 = bArr[dataIndex2];
            byte b22 = bArr[dataIndex2 + 1];
            byte l2 = (byte) (b22 & 15);
            byte k3 = (byte) (b13 & 3);
            byte val12 = (byte) ((b13 & Byte.MIN_VALUE) == 0 ? b13 >> 2 : (b13 >> 2) ^ 192);
            byte val22 = (byte) ((b22 & Byte.MIN_VALUE) == 0 ? b22 >> 4 : (b22 >> 4) ^ 240);
            encodedData[encodedIndex] = lookUpBase64Alphabet[val12];
            encodedData[encodedIndex + 1] = lookUpBase64Alphabet[val22 | (k3 << 4)];
            encodedData[encodedIndex + 2] = lookUpBase64Alphabet[l2 << 2];
            encodedData[encodedIndex + 3] = PAD;
        }
        if (isChunked && nextSeparatorIndex < nbrChunks) {
            System.arraycopy(CHUNK_SEPARATOR, 0, encodedData, encodedDataLength - CHUNK_SEPARATOR.length, CHUNK_SEPARATOR.length);
        }
        return encodedData;
    }

    public static byte[] decodeBase64(byte[] base64Data) {
        byte[] base64Data2 = discardNonBase64(base64Data);
        if (base64Data2.length == 0) {
            return new byte[0];
        }
        int numberQuadruple = base64Data2.length / 4;
        int encodedIndex = 0;
        int lastData = base64Data2.length;
        while (base64Data2[lastData - 1] == 61) {
            lastData--;
            if (lastData == 0) {
                return new byte[0];
            }
        }
        byte[] decodedData = new byte[(lastData - numberQuadruple)];
        for (int i = 0; i < numberQuadruple; i++) {
            int dataIndex = i * 4;
            byte marker0 = base64Data2[dataIndex + 2];
            byte marker1 = base64Data2[dataIndex + 3];
            byte b1 = base64Alphabet[base64Data2[dataIndex]];
            byte b2 = base64Alphabet[base64Data2[dataIndex + 1]];
            if (marker0 != 61 && marker1 != 61) {
                byte b3 = base64Alphabet[marker0];
                byte b4 = base64Alphabet[marker1];
                decodedData[encodedIndex] = (byte) ((b1 << 2) | (b2 >> 4));
                decodedData[encodedIndex + 1] = (byte) (((b2 & 15) << 4) | ((b3 >> 2) & 15));
                decodedData[encodedIndex + 2] = (byte) ((b3 << 6) | b4);
            } else if (marker0 == 61) {
                decodedData[encodedIndex] = (byte) ((b1 << 2) | (b2 >> 4));
            } else if (marker1 == 61) {
                byte b32 = base64Alphabet[marker0];
                decodedData[encodedIndex] = (byte) ((b1 << 2) | (b2 >> 4));
                decodedData[encodedIndex + 1] = (byte) (((b2 & 15) << 4) | ((b32 >> 2) & 15));
            }
            encodedIndex += 3;
        }
        return decodedData;
    }

    static byte[] discardWhitespace(byte[] data) {
        byte[] groomedData = new byte[data.length];
        int bytesCopied = 0;
        for (int i = 0; i < data.length; i++) {
            byte b = data[i];
            if (!(b == 13 || b == 32)) {
                switch (b) {
                    case HTTP.HT /*9*/:
                    case HTTP.LF /*10*/:
                        break;
                    default:
                        groomedData[bytesCopied] = data[i];
                        bytesCopied++;
                        break;
                }
            }
        }
        byte[] packedData = new byte[bytesCopied];
        System.arraycopy(groomedData, 0, packedData, 0, bytesCopied);
        return packedData;
    }

    static byte[] discardNonBase64(byte[] data) {
        byte[] groomedData = new byte[data.length];
        int bytesCopied = 0;
        for (int i = 0; i < data.length; i++) {
            if (isBase64(data[i])) {
                groomedData[bytesCopied] = data[i];
                bytesCopied++;
            }
        }
        byte[] packedData = new byte[bytesCopied];
        System.arraycopy(groomedData, 0, packedData, 0, bytesCopied);
        return packedData;
    }

    public Object encode(Object pObject) throws EncoderException {
        if (pObject instanceof byte[]) {
            return encode((byte[]) pObject);
        }
        throw new EncoderException("Parameter supplied to Base64 encode is not a byte[]");
    }

    public byte[] encode(byte[] pArray) {
        return encodeBase64(pArray, false);
    }
}
