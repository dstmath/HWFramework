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
    static final byte PAD = (byte) 61;
    static final int SIGN = -128;
    static final int SIXTEENBIT = 16;
    static final int TWENTYFOURBITGROUP = 24;
    private static byte[] base64Alphabet = new byte[BASELENGTH];
    private static byte[] lookUpBase64Alphabet = new byte[LOOKUPLENGTH];

    static {
        int i;
        for (i = 0; i < BASELENGTH; i++) {
            base64Alphabet[i] = (byte) -1;
        }
        for (i = 90; i >= 65; i--) {
            base64Alphabet[i] = (byte) (i - 65);
        }
        for (i = 122; i >= 97; i--) {
            base64Alphabet[i] = (byte) ((i - 97) + 26);
        }
        for (i = 57; i >= 48; i--) {
            base64Alphabet[i] = (byte) ((i - 48) + 52);
        }
        base64Alphabet[43] = (byte) 62;
        base64Alphabet[47] = (byte) 63;
        for (i = 0; i <= 25; i++) {
            lookUpBase64Alphabet[i] = (byte) (i + 65);
        }
        i = 26;
        int j = 0;
        while (i <= 51) {
            lookUpBase64Alphabet[i] = (byte) (j + 97);
            i++;
            j++;
        }
        i = 52;
        j = 0;
        while (i <= 61) {
            lookUpBase64Alphabet[i] = (byte) (j + 48);
            i++;
            j++;
        }
        lookUpBase64Alphabet[62] = (byte) 43;
        lookUpBase64Alphabet[63] = (byte) 47;
    }

    private static boolean isBase64(byte octect) {
        if (octect != PAD && base64Alphabet[octect] == (byte) -1) {
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
        int dataIndex;
        byte b1;
        byte b2;
        byte l;
        byte k;
        byte val1;
        byte val2;
        int lengthDataBits = binaryData.length * EIGHTBIT;
        int fewerThan24bits = lengthDataBits % TWENTYFOURBITGROUP;
        int numberTriplets = lengthDataBits / TWENTYFOURBITGROUP;
        int nbrChunks = 0;
        if (fewerThan24bits != 0) {
            encodedDataLength = (numberTriplets + 1) * 4;
        } else {
            encodedDataLength = numberTriplets * 4;
        }
        if (isChunked) {
            if (CHUNK_SEPARATOR.length == 0) {
                nbrChunks = 0;
            } else {
                nbrChunks = (int) Math.ceil((double) (((float) encodedDataLength) / 76.0f));
            }
            encodedDataLength += CHUNK_SEPARATOR.length * nbrChunks;
        }
        byte[] encodedData = new byte[encodedDataLength];
        int encodedIndex = 0;
        int nextSeparatorIndex = CHUNK_SIZE;
        int chunksSoFar = 0;
        int i = 0;
        while (i < numberTriplets) {
            dataIndex = i * 3;
            b1 = binaryData[dataIndex];
            b2 = binaryData[dataIndex + 1];
            byte b3 = binaryData[dataIndex + 2];
            l = (byte) (b2 & 15);
            k = (byte) (b1 & 3);
            val1 = (b1 & SIGN) == 0 ? (byte) (b1 >> 2) : (byte) ((b1 >> 2) ^ 192);
            val2 = (b2 & SIGN) == 0 ? (byte) (b2 >> 4) : (byte) ((b2 >> 4) ^ 240);
            byte val3 = (b3 & SIGN) == 0 ? (byte) (b3 >> 6) : (byte) ((b3 >> 6) ^ 252);
            encodedData[encodedIndex] = lookUpBase64Alphabet[val1];
            encodedData[encodedIndex + 1] = lookUpBase64Alphabet[(k << 4) | val2];
            encodedData[encodedIndex + 2] = lookUpBase64Alphabet[(l << 2) | val3];
            encodedData[encodedIndex + 3] = lookUpBase64Alphabet[b3 & 63];
            encodedIndex += 4;
            if (isChunked && encodedIndex == nextSeparatorIndex) {
                System.arraycopy(CHUNK_SEPARATOR, 0, encodedData, encodedIndex, CHUNK_SEPARATOR.length);
                chunksSoFar++;
                nextSeparatorIndex = ((chunksSoFar + 1) * CHUNK_SIZE) + (CHUNK_SEPARATOR.length * chunksSoFar);
                encodedIndex += CHUNK_SEPARATOR.length;
            }
            i++;
        }
        dataIndex = i * 3;
        if (fewerThan24bits == EIGHTBIT) {
            b1 = binaryData[dataIndex];
            k = (byte) (b1 & 3);
            encodedData[encodedIndex] = lookUpBase64Alphabet[(b1 & SIGN) == 0 ? (byte) (b1 >> 2) : (byte) ((b1 >> 2) ^ 192)];
            encodedData[encodedIndex + 1] = lookUpBase64Alphabet[k << 4];
            encodedData[encodedIndex + 2] = PAD;
            encodedData[encodedIndex + 3] = PAD;
        } else if (fewerThan24bits == 16) {
            b1 = binaryData[dataIndex];
            b2 = binaryData[dataIndex + 1];
            l = (byte) (b2 & 15);
            k = (byte) (b1 & 3);
            val1 = (b1 & SIGN) == 0 ? (byte) (b1 >> 2) : (byte) ((b1 >> 2) ^ 192);
            val2 = (b2 & SIGN) == 0 ? (byte) (b2 >> 4) : (byte) ((b2 >> 4) ^ 240);
            encodedData[encodedIndex] = lookUpBase64Alphabet[val1];
            encodedData[encodedIndex + 1] = lookUpBase64Alphabet[(k << 4) | val2];
            encodedData[encodedIndex + 2] = lookUpBase64Alphabet[l << 2];
            encodedData[encodedIndex + 3] = PAD;
        }
        if (isChunked && chunksSoFar < nbrChunks) {
            System.arraycopy(CHUNK_SEPARATOR, 0, encodedData, encodedDataLength - CHUNK_SEPARATOR.length, CHUNK_SEPARATOR.length);
        }
        return encodedData;
    }

    public static byte[] decodeBase64(byte[] base64Data) {
        base64Data = discardNonBase64(base64Data);
        if (base64Data.length == 0) {
            return new byte[0];
        }
        int numberQuadruple = base64Data.length / 4;
        int encodedIndex = 0;
        int lastData = base64Data.length;
        while (base64Data[lastData - 1] == PAD) {
            lastData--;
            if (lastData == 0) {
                return new byte[0];
            }
        }
        byte[] decodedData = new byte[(lastData - numberQuadruple)];
        for (int i = 0; i < numberQuadruple; i++) {
            int dataIndex = i * 4;
            byte marker0 = base64Data[dataIndex + 2];
            byte marker1 = base64Data[dataIndex + 3];
            byte b1 = base64Alphabet[base64Data[dataIndex]];
            byte b2 = base64Alphabet[base64Data[dataIndex + 1]];
            byte b3;
            if (marker0 != PAD && marker1 != PAD) {
                b3 = base64Alphabet[marker0];
                byte b4 = base64Alphabet[marker1];
                decodedData[encodedIndex] = (byte) ((b1 << 2) | (b2 >> 4));
                decodedData[encodedIndex + 1] = (byte) (((b2 & 15) << 4) | ((b3 >> 2) & 15));
                decodedData[encodedIndex + 2] = (byte) ((b3 << 6) | b4);
            } else if (marker0 == PAD) {
                decodedData[encodedIndex] = (byte) ((b1 << 2) | (b2 >> 4));
            } else if (marker1 == PAD) {
                b3 = base64Alphabet[marker0];
                decodedData[encodedIndex] = (byte) ((b1 << 2) | (b2 >> 4));
                decodedData[encodedIndex + 1] = (byte) (((b2 & 15) << 4) | ((b3 >> 2) & 15));
            }
            encodedIndex += 3;
        }
        return decodedData;
    }

    static byte[] discardWhitespace(byte[] data) {
        byte[] groomedData = new byte[data.length];
        int bytesCopied = 0;
        for (int i = 0; i < data.length; i++) {
            switch (data[i]) {
                case HTTP.HT /*9*/:
                case HTTP.LF /*10*/:
                case HTTP.CR /*13*/:
                case HTTP.SP /*32*/:
                    break;
                default:
                    int bytesCopied2 = bytesCopied + 1;
                    groomedData[bytesCopied] = data[i];
                    bytesCopied = bytesCopied2;
                    break;
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
                int bytesCopied2 = bytesCopied + 1;
                groomedData[bytesCopied] = data[i];
                bytesCopied = bytesCopied2;
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
