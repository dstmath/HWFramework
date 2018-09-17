package com.android.internal.telephony.uicc;

import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.telephony.Rlog;
import android.text.format.DateFormat;
import android.util.LogException;
import com.android.internal.R;
import com.android.internal.telephony.GsmAlphabet;
import java.io.UnsupportedEncodingException;

public class IccUtils {
    static final String LOG_TAG = "IccUtils";

    public static String bcdToString(byte[] data, int offset, int length) {
        StringBuilder ret = new StringBuilder(length * 2);
        for (int i = offset; i < offset + length; i++) {
            int v = data[i] & 15;
            if (v > 9) {
                break;
            }
            ret.append((char) (v + 48));
            v = (data[i] >> 4) & 15;
            if (v != 15) {
                if (v > 9) {
                    break;
                }
                ret.append((char) (v + 48));
            }
        }
        return ret.toString();
    }

    public static String bcdPlmnToString(byte[] data, int offset) {
        if (offset + 3 > data.length) {
            return null;
        }
        String ret = bytesToHexString(new byte[]{(byte) ((data[offset + 0] << 4) | ((data[offset + 0] >> 4) & 15)), (byte) ((data[offset + 1] << 4) | (data[offset + 2] & 15)), (byte) ((data[offset + 2] & 240) | ((data[offset + 1] >> 4) & 15))});
        if (ret.endsWith("f")) {
            ret = ret.substring(0, ret.length() - 1);
        }
        return ret;
    }

    public static String bchToString(byte[] data, int offset, int length) {
        StringBuilder ret = new StringBuilder(length * 2);
        for (int i = offset; i < offset + length; i++) {
            ret.append("0123456789abcdef".charAt(data[i] & 15));
            ret.append("0123456789abcdef".charAt((data[i] >> 4) & 15));
        }
        return ret.toString();
    }

    public static String cdmaBcdToString(byte[] data, int offset, int length) {
        StringBuilder ret = new StringBuilder(length);
        int count = 0;
        int i = offset;
        while (count < length) {
            int v = data[i] & 15;
            if (v > 9) {
                v = 0;
            }
            ret.append((char) (v + 48));
            count++;
            if (count == length) {
                break;
            }
            v = (data[i] >> 4) & 15;
            if (v > 9) {
                v = 0;
            }
            ret.append((char) (v + 48));
            count++;
            i++;
        }
        return ret.toString();
    }

    public static int gsmBcdByteToInt(byte b) {
        int ret = 0;
        if ((b & 240) <= 144) {
            ret = (b >> 4) & 15;
        }
        if ((b & 15) <= 9) {
            return ret + ((b & 15) * 10);
        }
        return ret;
    }

    public static int cdmaBcdByteToInt(byte b) {
        int ret = 0;
        if ((b & 240) <= 144) {
            ret = ((b >> 4) & 15) * 10;
        }
        if ((b & 15) <= 9) {
            return ret + (b & 15);
        }
        return ret;
    }

    public static String adnStringFieldToString(byte[] data, int offset, int length) {
        if (length == 0) {
            return LogException.NO_VALUE;
        }
        if (length >= 1 && data[offset] == Byte.MIN_VALUE) {
            String str = null;
            try {
                str = new String(data, offset + 1, ((length - 1) / 2) * 2, "utf-16be");
            } catch (UnsupportedEncodingException ex) {
                Rlog.e(LOG_TAG, "implausible UnsupportedEncodingException", ex);
            }
            if (str != null) {
                int ucslen = str.length();
                while (ucslen > 0 && str.charAt(ucslen - 1) == 65535) {
                    ucslen--;
                }
                return str.substring(0, ucslen);
            }
        }
        boolean isucs2 = false;
        char base = 0;
        int len = 0;
        if (length >= 3 && data[offset] == (byte) -127) {
            len = data[offset + 1] & 255;
            if (len > length - 3) {
                len = length - 3;
            }
            base = (char) ((data[offset + 2] & 255) << 7);
            offset += 3;
            isucs2 = true;
        } else if (length >= 4 && data[offset] == (byte) -126) {
            len = data[offset + 1] & 255;
            if (len > length - 4) {
                len = length - 4;
            }
            base = (char) (((data[offset + 2] & 255) << 8) | (data[offset + 3] & 255));
            offset += 4;
            isucs2 = true;
        }
        if (isucs2) {
            StringBuilder ret = new StringBuilder();
            while (len > 0) {
                if (data[offset] < (byte) 0) {
                    ret.append((char) ((data[offset] & 127) + base));
                    offset++;
                    len--;
                }
                int count = 0;
                while (count < len && data[offset + count] >= (byte) 0) {
                    count++;
                }
                ret.append(GsmAlphabet.gsm8BitUnpackedToString(data, offset, count));
                offset += count;
                len -= count;
            }
            return ret.toString();
        }
        Resources resource = Resources.getSystem();
        String defaultCharset = LogException.NO_VALUE;
        try {
            defaultCharset = resource.getString(R.string.gsm_alphabet_default_charset);
        } catch (NotFoundException e) {
        }
        return GsmAlphabet.gsm8BitUnpackedToString(data, offset, length, defaultCharset.trim());
    }

    static int hexCharToInt(char c) {
        if (c >= '0' && c <= '9') {
            return c - 48;
        }
        if (c >= DateFormat.CAPITAL_AM_PM && c <= 'F') {
            return (c - 65) + 10;
        }
        if (c >= DateFormat.AM_PM && c <= 'f') {
            return (c - 97) + 10;
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
            ret.append("0123456789abcdef".charAt((bytes[i] >> 4) & 15));
            ret.append("0123456789abcdef".charAt(bytes[i] & 15));
        }
        return ret.toString();
    }

    public static String networkNameToString(byte[] data, int offset, int length) {
        if ((data[offset] & 128) != 128 || length < 1) {
            return LogException.NO_VALUE;
        }
        String ret;
        switch ((data[offset] >>> 4) & 7) {
            case 0:
                ret = GsmAlphabet.gsm7BitPackedToString(data, offset + 1, (((length - 1) * 8) - (data[offset] & 7)) / 7);
                break;
            case 1:
                try {
                    ret = new String(data, offset + 1, length - 1, "utf-16");
                    break;
                } catch (UnsupportedEncodingException ex) {
                    ret = LogException.NO_VALUE;
                    Rlog.e(LOG_TAG, "implausible UnsupportedEncodingException", ex);
                    break;
                }
            default:
                ret = LogException.NO_VALUE;
                break;
        }
        int i = data[offset] & 64;
        return ret;
    }

    public static Bitmap parseToBnW(byte[] data, int length) {
        int width = data[0] & 255;
        int valueIndex = 1 + 1;
        int height = data[1] & 255;
        int numOfPixels = width * height;
        int[] pixels = new int[numOfPixels];
        int bitIndex = 7;
        byte currentByte = (byte) 0;
        int pixelIndex = 0;
        while (pixelIndex < numOfPixels) {
            int valueIndex2;
            if (pixelIndex % 8 == 0) {
                valueIndex2 = valueIndex + 1;
                currentByte = data[valueIndex];
                bitIndex = 7;
            } else {
                valueIndex2 = valueIndex;
            }
            int pixelIndex2 = pixelIndex + 1;
            int bitIndex2 = bitIndex - 1;
            pixels[pixelIndex] = bitToRGB((currentByte >> bitIndex) & 1);
            bitIndex = bitIndex2;
            pixelIndex = pixelIndex2;
            valueIndex = valueIndex2;
        }
        if (pixelIndex != numOfPixels) {
            Rlog.e(LOG_TAG, "parse end and size error");
        }
        return Bitmap.createBitmap(pixels, width, height, Config.ARGB_8888);
    }

    private static int bitToRGB(int bit) {
        if (bit == 1) {
            return -1;
        }
        return -16777216;
    }

    public static Bitmap parseToRGB(byte[] data, int length, boolean transparency) {
        int[] resultArray;
        int width = data[0] & 255;
        int valueIndex = 1 + 1;
        int height = data[1] & 255;
        int valueIndex2 = valueIndex + 1;
        int bits = data[valueIndex] & 255;
        valueIndex = valueIndex2 + 1;
        int colorNumber = data[valueIndex2] & 255;
        valueIndex2 = valueIndex + 1;
        valueIndex = valueIndex2 + 1;
        int[] colorIndexArray = getCLUT(data, ((data[valueIndex] & 255) << 8) | (data[valueIndex2] & 255), colorNumber);
        if (transparency) {
            colorIndexArray[colorNumber - 1] = 0;
        }
        if (8 % bits == 0) {
            resultArray = mapTo2OrderBitColor(data, valueIndex, width * height, colorIndexArray, bits);
        } else {
            resultArray = mapToNon2OrderBitColor(data, valueIndex, width * height, colorIndexArray, bits);
        }
        return Bitmap.createBitmap(resultArray, width, height, Config.RGB_565);
    }

    private static int[] mapTo2OrderBitColor(byte[] data, int valueIndex, int length, int[] colorArray, int bits) {
        if (8 % bits != 0) {
            Rlog.e(LOG_TAG, "not event number of color");
            return mapToNon2OrderBitColor(data, valueIndex, length, colorArray, bits);
        }
        int mask = 1;
        switch (bits) {
            case 1:
                mask = 1;
                break;
            case 2:
                mask = 3;
                break;
            case 4:
                mask = 15;
                break;
            case 8:
                mask = 255;
                break;
        }
        int[] resultArray = new int[length];
        int resultIndex = 0;
        int run = 8 / bits;
        int valueIndex2 = valueIndex;
        while (resultIndex < length) {
            valueIndex = valueIndex2 + 1;
            byte tempByte = data[valueIndex2];
            int runIndex = 0;
            int resultIndex2 = resultIndex;
            while (runIndex < run) {
                resultIndex = resultIndex2 + 1;
                resultArray[resultIndex2] = colorArray[(tempByte >> (((run - runIndex) - 1) * bits)) & mask];
                runIndex++;
                resultIndex2 = resultIndex;
            }
            resultIndex = resultIndex2;
            valueIndex2 = valueIndex;
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
        int endIndex = offset + (number * 3);
        int valueIndex = offset;
        int colorIndex = 0;
        while (true) {
            int colorIndex2 = colorIndex + 1;
            int valueIndex2 = valueIndex + 1;
            valueIndex = valueIndex2 + 1;
            valueIndex2 = valueIndex + 1;
            result[colorIndex] = ((((rawData[valueIndex] & 255) << 16) | -16777216) | ((rawData[valueIndex2] & 255) << 8)) | (rawData[valueIndex] & 255);
            if (valueIndex2 >= endIndex) {
                return result;
            }
            colorIndex = colorIndex2;
            valueIndex = valueIndex2;
        }
    }

    public static String iccIdBcdToString(byte[] data, int offset, int length) {
        StringBuilder ret = new StringBuilder(length * 2);
        char[] cnum = new char[]{DateFormat.CAPITAL_AM_PM, 'B', 'C', 'D', DateFormat.DAY, 'F'};
        for (int i = offset; i < offset + length; i++) {
            int v = data[i] & 15;
            if (v > 9) {
                ret.append(cnum[v - 10]);
            } else {
                ret.append((char) (v + 48));
            }
            v = (data[i] >> 4) & 15;
            if (v > 9) {
                ret.append(cnum[v - 10]);
            } else {
                ret.append((char) (v + 48));
            }
        }
        return ret.toString();
    }
}
