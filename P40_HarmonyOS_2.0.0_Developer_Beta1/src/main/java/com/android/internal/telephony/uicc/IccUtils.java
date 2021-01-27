package com.android.internal.telephony.uicc;

import android.app.backup.FullBackup;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.wifi.WifiEnterpriseConfig;
import android.telephony.Rlog;
import android.text.format.DateFormat;
import com.android.internal.R;
import com.android.internal.midi.MidiConstants;
import com.android.internal.telephony.GsmAlphabet;
import java.io.UnsupportedEncodingException;

public class IccUtils {
    private static final char[] HEX_CHARS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', DateFormat.CAPITAL_AM_PM, 'B', 'C', 'D', DateFormat.DAY, 'F'};
    static final String LOG_TAG = "IccUtils";

    public static String bcdToString(byte[] data, int offset, int length) {
        int v;
        StringBuilder ret = new StringBuilder(length * 2);
        int i = offset;
        while (i < offset + length && (v = data[i] & MidiConstants.STATUS_CHANNEL_MASK) <= 9) {
            ret.append((char) (v + 48));
            int v2 = (data[i] >> 4) & 15;
            if (v2 != 15) {
                if (v2 > 9) {
                    break;
                }
                ret.append((char) (v2 + 48));
            }
            i++;
        }
        return ret.toString();
    }

    public static String bcdToString(byte[] data) {
        return bcdToString(data, 0, data.length);
    }

    public static byte[] bcdToBytes(String bcd) {
        byte[] output = new byte[((bcd.length() + 1) / 2)];
        bcdToBytes(bcd, output);
        return output;
    }

    public static void bcdToBytes(String bcd, byte[] bytes) {
        bcdToBytes(bcd, bytes, 0);
    }

    public static void bcdToBytes(String bcd, byte[] bytes, int offset) {
        if (bcd.length() % 2 != 0) {
            bcd = bcd + WifiEnterpriseConfig.ENGINE_DISABLE;
        }
        int size = Math.min((bytes.length - offset) * 2, bcd.length());
        int i = 0;
        int j = offset;
        while (i + 1 < size) {
            bytes[j] = (byte) ((charToByte(bcd.charAt(i + 1)) << 4) | charToByte(bcd.charAt(i)));
            i += 2;
            j++;
        }
    }

    public static String bcdPlmnToString(byte[] data, int offset) {
        if (offset + 3 > data.length) {
            return null;
        }
        String ret = bytesToHexString(new byte[]{(byte) ((data[offset + 0] << 4) | ((data[offset + 0] >> 4) & 15)), (byte) ((data[offset + 1] << 4) | (data[offset + 2] & MidiConstants.STATUS_CHANNEL_MASK)), (byte) ((data[offset + 2] & 240) | ((data[offset + 1] >> 4) & 15))});
        if (ret.contains(FullBackup.FILES_TREE_TOKEN) || ret.contains("F")) {
            return ret.replaceAll("(?i)f", "");
        }
        return ret;
    }

    public static void stringToBcdPlmn(String plmn, byte[] data, int offset) {
        char digit6 = plmn.length() > 5 ? plmn.charAt(5) : 'F';
        data[offset] = (byte) ((charToByte(plmn.charAt(1)) << 4) | charToByte(plmn.charAt(0)));
        data[offset + 1] = (byte) ((charToByte(digit6) << 4) | charToByte(plmn.charAt(2)));
        data[offset + 2] = (byte) ((charToByte(plmn.charAt(4)) << 4) | charToByte(plmn.charAt(3)));
    }

    public static String bchToString(byte[] data, int offset, int length) {
        StringBuilder ret = new StringBuilder(length * 2);
        for (int i = offset; i < offset + length; i++) {
            ret.append(HEX_CHARS[data[i] & MidiConstants.STATUS_CHANNEL_MASK]);
            ret.append(HEX_CHARS[(data[i] >> 4) & 15]);
        }
        return ret.toString();
    }

    public static String cdmaBcdToString(byte[] data, int offset, int length) {
        StringBuilder ret = new StringBuilder(length);
        int count = 0;
        int i = offset;
        while (count < length) {
            int v = data[i] & MidiConstants.STATUS_CHANNEL_MASK;
            if (v > 9) {
                v = 0;
            }
            ret.append((char) (v + 48));
            int count2 = count + 1;
            if (count2 == length) {
                break;
            }
            int v2 = (data[i] >> 4) & 15;
            if (v2 > 9) {
                v2 = 0;
            }
            ret.append((char) (v2 + 48));
            count = count2 + 1;
            i++;
        }
        return ret.toString();
    }

    public static int gsmBcdByteToInt(byte b) {
        int ret = 0;
        if ((b & 240) <= 144) {
            ret = (b >> 4) & 15;
        }
        if ((b & MidiConstants.STATUS_CHANNEL_MASK) <= 9) {
            return ret + ((b & MidiConstants.STATUS_CHANNEL_MASK) * 10);
        }
        return ret;
    }

    public static int cdmaBcdByteToInt(byte b) {
        int ret = 0;
        if ((b & 240) <= 144) {
            ret = ((b >> 4) & 15) * 10;
        }
        if ((b & MidiConstants.STATUS_CHANNEL_MASK) <= 9) {
            return ret + (b & MidiConstants.STATUS_CHANNEL_MASK);
        }
        return ret;
    }

    public static String adnStringFieldToString(byte[] data, int offset, int length) {
        if (length == 0) {
            return "";
        }
        if (length >= 1 && data[offset] == Byte.MIN_VALUE) {
            String ret = null;
            try {
                ret = new String(data, offset + 1, ((length - 1) / 2) * 2, "utf-16be");
            } catch (UnsupportedEncodingException ex) {
                Rlog.e(LOG_TAG, "implausible UnsupportedEncodingException", ex);
            }
            if (ret != null) {
                int ucslen = ret.length();
                while (ucslen > 0 && ret.charAt(ucslen - 1) == 65535) {
                    ucslen--;
                }
                return ret.substring(0, ucslen);
            }
        }
        boolean isucs2 = false;
        char base = 0;
        int len = 0;
        if (length >= 3 && data[offset] == -127) {
            len = data[offset + 1] & 255;
            if (len > length - 3) {
                len = length - 3;
            }
            base = (char) ((data[offset + 2] & 255) << 7);
            offset += 3;
            isucs2 = true;
        } else if (length >= 4 && data[offset] == -126) {
            len = data[offset + 1] & 255;
            if (len > length - 4) {
                len = length - 4;
            }
            base = (char) (((data[offset + 2] & 255) << 8) | (data[offset + 3] & 255));
            offset += 4;
            isucs2 = true;
        }
        if (isucs2) {
            StringBuilder ret2 = new StringBuilder();
            while (len > 0) {
                if (data[offset] < 0) {
                    ret2.append((char) ((data[offset] & Byte.MAX_VALUE) + base));
                    offset++;
                    len--;
                }
                int count = 0;
                while (count < len && data[offset + count] >= 0) {
                    count++;
                }
                ret2.append(GsmAlphabet.gsm8BitUnpackedToString(data, offset, count));
                offset += count;
                len -= count;
            }
            return ret2.toString();
        }
        String defaultCharset = "";
        try {
            defaultCharset = Resources.getSystem().getString(R.string.gsm_alphabet_default_charset);
        } catch (Resources.NotFoundException e) {
        }
        return GsmAlphabet.gsm8BitUnpackedToString(data, offset, length, defaultCharset.trim());
    }

    public static int hexCharToInt(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        if (c >= 'A' && c <= 'F') {
            return (c - 'A') + 10;
        }
        if (c >= 'a' && c <= 'f') {
            return (c - 'a') + 10;
        }
        throw new RuntimeException("invalid hex char '" + c + "'");
    }

    public static byte[] hexStringToBytes(String s) {
        if (s == null) {
            return null;
        }
        int sz = s.length();
        byte[] ret = new byte[(sz / 2)];
        for (int i = 0; i < sz; i += 2) {
            ret[i / 2] = (byte) ((hexCharToInt(s.charAt(i)) << 4) | hexCharToInt(s.charAt(i + 1)));
        }
        return ret;
    }

    public static String bytesToHexString(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        StringBuilder ret = new StringBuilder(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++) {
            ret.append(HEX_CHARS[(bytes[i] >> 4) & 15]);
            ret.append(HEX_CHARS[bytes[i] & MidiConstants.STATUS_CHANNEL_MASK]);
        }
        return ret.toString();
    }

    public static String networkNameToString(byte[] data, int offset, int length) {
        String ret;
        if (data.length <= offset) {
            return null;
        }
        if ((data[offset] & 128) != 128 || length < 1) {
            return "";
        }
        int i = (data[offset] >>> 4) & 7;
        if (i == 0) {
            int i2 = offset + 1;
            ret = GsmAlphabet.gsm7BitPackedToString(data, i2, (((length - 1) * 8) - (data[offset] & 7)) / 7);
        } else if (i != 1) {
            ret = "";
        } else {
            try {
                ret = new String(data, offset + 1, length - 1, "utf-16");
            } catch (UnsupportedEncodingException ex) {
                Rlog.e(LOG_TAG, "implausible UnsupportedEncodingException", ex);
                ret = "";
            }
        }
        byte b = data[offset];
        return ret;
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX INFO: Multiple debug info for r8v4 int: [D('valueIndex' int), D('pixelIndex' int)] */
    /* JADX INFO: Multiple debug info for r2v3 byte: [D('currentByte' byte), D('valueIndex' int)] */
    public static Bitmap parseToBnW(byte[] data, int length) {
        int valueIndex = 0 + 1;
        int width = data[0] & 255;
        int valueIndex2 = valueIndex + 1;
        int height = data[valueIndex] & 255;
        int numOfPixels = width * height;
        int[] pixels = new int[numOfPixels];
        int pixelIndex = 0;
        byte bitIndex = 7;
        byte currentByte = 0;
        while (pixelIndex < numOfPixels) {
            if (pixelIndex % 8 == 0) {
                bitIndex = 7;
                currentByte = data[valueIndex2];
                valueIndex2++;
            }
            pixels[pixelIndex] = bitToRGB((currentByte >> bitIndex) & 1);
            pixelIndex++;
            bitIndex--;
        }
        if (pixelIndex != numOfPixels) {
            Rlog.e(LOG_TAG, "parse end and size error");
        }
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888);
    }

    private static int bitToRGB(int bit) {
        if (bit == 1) {
            return -1;
        }
        return -16777216;
    }

    public static Bitmap parseToRGB(byte[] data, int length, boolean transparency) {
        int[] resultArray;
        int valueIndex = 0 + 1;
        int width = data[0] & 255;
        int valueIndex2 = valueIndex + 1;
        int height = data[valueIndex] & 255;
        int valueIndex3 = valueIndex2 + 1;
        int bits = data[valueIndex2] & 255;
        int valueIndex4 = valueIndex3 + 1;
        int colorNumber = data[valueIndex3] & 255;
        int valueIndex5 = valueIndex4 + 1;
        int valueIndex6 = valueIndex5 + 1;
        int[] colorIndexArray = getCLUT(data, ((data[valueIndex4] & 255) << 8) | (data[valueIndex5] & 255), colorNumber);
        if (true == transparency) {
            colorIndexArray[colorNumber - 1] = 0;
        }
        if (8 % bits == 0) {
            resultArray = mapTo2OrderBitColor(data, valueIndex6, width * height, colorIndexArray, bits);
        } else {
            resultArray = mapToNon2OrderBitColor(data, valueIndex6, width * height, colorIndexArray, bits);
        }
        return Bitmap.createBitmap(resultArray, width, height, Bitmap.Config.RGB_565);
    }

    /* JADX INFO: Multiple debug info for r11v2 byte: [D('valueIndex' int), D('tempByte' byte)] */
    private static int[] mapTo2OrderBitColor(byte[] data, int valueIndex, int length, int[] colorArray, int bits) {
        if (8 % bits != 0) {
            Rlog.e(LOG_TAG, "not event number of color");
            return mapToNon2OrderBitColor(data, valueIndex, length, colorArray, bits);
        }
        int mask = 1;
        if (bits == 1) {
            mask = 1;
        } else if (bits == 2) {
            mask = 3;
        } else if (bits == 4) {
            mask = 15;
        } else if (bits == 8) {
            mask = 255;
        }
        int[] resultArray = new int[length];
        int resultIndex = 0;
        int run = 8 / bits;
        while (resultIndex < length) {
            int valueIndex2 = valueIndex + 1;
            byte tempByte = data[valueIndex];
            int runIndex = 0;
            while (runIndex < run) {
                resultArray[resultIndex] = colorArray[(tempByte >> (((run - runIndex) - 1) * bits)) & mask];
                runIndex++;
                resultIndex++;
            }
            valueIndex = valueIndex2;
        }
        return resultArray;
    }

    private static int[] mapToNon2OrderBitColor(byte[] data, int valueIndex, int length, int[] colorArray, int bits) {
        if (8 % bits != 0) {
            return new int[length];
        }
        Rlog.e(LOG_TAG, "not odd number of color");
        return mapTo2OrderBitColor(data, valueIndex, length, colorArray, bits);
    }

    private static int[] getCLUT(byte[] rawData, int offset, int number) {
        if (rawData == null) {
            return null;
        }
        int[] result = new int[number];
        int endIndex = (number * 3) + offset;
        int valueIndex = offset;
        int colorIndex = 0;
        while (true) {
            int colorIndex2 = colorIndex + 1;
            int valueIndex2 = valueIndex + 1;
            int valueIndex3 = valueIndex2 + 1;
            int i = ((rawData[valueIndex] & 255) << 16) | -16777216 | ((rawData[valueIndex2] & 255) << 8);
            int valueIndex4 = valueIndex3 + 1;
            result[colorIndex] = i | (rawData[valueIndex3] & 255);
            if (valueIndex4 >= endIndex) {
                return result;
            }
            colorIndex = colorIndex2;
            valueIndex = valueIndex4;
        }
    }

    public static String getDecimalSubstring(String iccId) {
        if (iccId == null) {
            return null;
        }
        int position = 0;
        while (position < iccId.length() && Character.isDigit(iccId.charAt(position))) {
            position++;
        }
        return iccId.substring(0, position);
    }

    public static int bytesToInt(byte[] src, int offset, int length) {
        if (length > 4) {
            throw new IllegalArgumentException("length must be <= 4 (only 32-bit integer supported): " + length);
        } else if (offset < 0 || length < 0 || offset + length > src.length) {
            throw new IndexOutOfBoundsException("Out of the bounds: src=[" + src.length + "], offset=" + offset + ", length=" + length);
        } else {
            int result = 0;
            for (int i = 0; i < length; i++) {
                result = (result << 8) | (src[offset + i] & 255);
            }
            if (result >= 0) {
                return result;
            }
            throw new IllegalArgumentException("src cannot be parsed as a positive integer: " + result);
        }
    }

    public static long bytesToRawLong(byte[] src, int offset, int length) {
        if (length > 8) {
            throw new IllegalArgumentException("length must be <= 8 (only 64-bit long supported): " + length);
        } else if (offset < 0 || length < 0 || offset + length > src.length) {
            throw new IndexOutOfBoundsException("Out of the bounds: src=[" + src.length + "], offset=" + offset + ", length=" + length);
        } else {
            long result = 0;
            for (int i = 0; i < length; i++) {
                result = (result << 8) | ((long) (src[offset + i] & 255));
            }
            return result;
        }
    }

    public static byte[] unsignedIntToBytes(int value) {
        if (value >= 0) {
            byte[] bytes = new byte[byteNumForUnsignedInt(value)];
            unsignedIntToBytes(value, bytes, 0);
            return bytes;
        }
        throw new IllegalArgumentException("value must be 0 or positive: " + value);
    }

    public static byte[] signedIntToBytes(int value) {
        if (value >= 0) {
            byte[] bytes = new byte[byteNumForSignedInt(value)];
            signedIntToBytes(value, bytes, 0);
            return bytes;
        }
        throw new IllegalArgumentException("value must be 0 or positive: " + value);
    }

    public static int unsignedIntToBytes(int value, byte[] dest, int offset) {
        return intToBytes(value, dest, offset, false);
    }

    public static int signedIntToBytes(int value, byte[] dest, int offset) {
        return intToBytes(value, dest, offset, true);
    }

    public static int byteNumForUnsignedInt(int value) {
        return byteNumForInt(value, false);
    }

    public static int byteNumForSignedInt(int value) {
        return byteNumForInt(value, true);
    }

    private static int intToBytes(int value, byte[] dest, int offset, boolean signed) {
        int l = byteNumForInt(value, signed);
        if (offset < 0 || offset + l > dest.length) {
            throw new IndexOutOfBoundsException("Not enough space to write. Required bytes: " + l);
        }
        int i = l - 1;
        int v = value;
        while (i >= 0) {
            dest[offset + i] = (byte) (v & 255);
            i--;
            v >>>= 8;
        }
        return l;
    }

    private static int byteNumForInt(int value, boolean signed) {
        if (value < 0) {
            throw new IllegalArgumentException("value must be 0 or positive: " + value);
        } else if (signed) {
            if (value <= 127) {
                return 1;
            }
            if (value <= 32767) {
                return 2;
            }
            if (value <= 8388607) {
                return 3;
            }
            return 4;
        } else if (value <= 255) {
            return 1;
        } else {
            if (value <= 65535) {
                return 2;
            }
            if (value <= 16777215) {
                return 3;
            }
            return 4;
        }
    }

    public static byte countTrailingZeros(byte b) {
        if (b == 0) {
            return 8;
        }
        int v = b & 255;
        byte c = 7;
        if ((v & 15) != 0) {
            c = (byte) (7 - 4);
        }
        if ((v & 51) != 0) {
            c = (byte) (c - 2);
        }
        if ((v & 85) != 0) {
            return (byte) (c - 1);
        }
        return c;
    }

    public static String byteToHex(byte b) {
        char[] cArr = HEX_CHARS;
        return new String(new char[]{cArr[(b & 255) >>> 4], cArr[b & MidiConstants.STATUS_CHANNEL_MASK]});
    }

    public static String stripTrailingFs(String s) {
        if (s == null) {
            return null;
        }
        return s.replaceAll("(?i)f*$", "");
    }

    private static byte charToByte(char c) {
        if (c >= '0' && c <= '9') {
            return (byte) (c - '0');
        }
        if (c >= 'A' && c <= 'F') {
            return (byte) (c - '7');
        }
        if (c < 'a' || c > 'f') {
            return 0;
        }
        return (byte) (c - 'W');
    }
}
