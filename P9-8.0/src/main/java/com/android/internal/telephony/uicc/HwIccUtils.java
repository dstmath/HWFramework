package com.android.internal.telephony.uicc;

import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.text.TextUtils;
import com.android.internal.telephony.HwGsmAlphabet;
import com.android.internal.telephony.HwSubscriptionManager;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HwIccUtils {
    static final int ADN_BCD_NUMBER_LENGTH = 0;
    static final int ADN_CAPABILITY_ID = 12;
    static final int ADN_DIALING_NUMBER_END = 11;
    static final int ADN_DIALING_NUMBER_START = 2;
    static final int ADN_EXTENSION_ID = 13;
    static final int ADN_TON_AND_NPI = 1;
    static final int EXT_RECORD_LENGTH_BYTES = 13;
    static final int EXT_RECORD_TYPE_ADDITIONAL_DATA = 2;
    static final int EXT_RECORD_TYPE_MASK = 3;
    static final int FOOTER_SIZE_BYTES = 14;
    private static final String LOG_TAG = "HwIccUtils";
    static final int MAX_EXT_CALLED_PARTY_LENGTH = 10;
    static final int MAX_NUMBER_SIZE_BYTES = 11;
    static final int MDN_BYTE_LENGTH = 11;

    public static String adnStringFieldToStringForSTK(byte[] data, int offset, int length) {
        if (length == 0) {
            return "";
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
            len = data[offset + 1] & HwSubscriptionManager.SUB_INIT_STATE;
            if (len > length - 3) {
                len = length - 3;
            }
            base = (char) ((data[offset + 2] & HwSubscriptionManager.SUB_INIT_STATE) << 7);
            offset += 3;
            isucs2 = true;
        } else if (length >= 4 && data[offset] == (byte) -126) {
            len = data[offset + 1] & HwSubscriptionManager.SUB_INIT_STATE;
            if (len > length - 4) {
                len = length - 4;
            }
            base = (char) (((data[offset + 2] & HwSubscriptionManager.SUB_INIT_STATE) << 8) | (data[offset + 3] & HwSubscriptionManager.SUB_INIT_STATE));
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
                ret.append(HwGsmAlphabet.gsm8BitUnpackedToString(data, offset, count));
                offset += count;
                len -= count;
            }
            return ret.toString();
        }
        String defaultCharset = "";
        try {
            defaultCharset = Resources.getSystem().getString(17040097);
        } catch (NotFoundException e) {
        }
        return HwGsmAlphabet.gsm8BitUnpackedToString(data, offset, length, defaultCharset.trim(), true);
    }

    public static int hexCharToInt(char c) {
        if (c >= '0' && c <= '9') {
            return c - 48;
        }
        if (c >= 'A' && c <= 'F') {
            return (c - 65) + 10;
        }
        if (c >= 'a' && c <= 'f') {
            return (c - 97) + 10;
        }
        throw new RuntimeException("invalid hex char '" + c + "'");
    }

    public static byte[] hexStringToBcd(String s) {
        if (s == null) {
            return new byte[0];
        }
        int sz = s.length();
        byte[] ret = new byte[(sz / 2)];
        for (int i = 0; i < sz; i += 2) {
            ret[i / 2] = (byte) ((hexCharToInt(s.charAt(i + 1)) << 4) | hexCharToInt(s.charAt(i)));
        }
        return ret;
    }

    public static String bcdIccidToString(byte[] data, int offset, int length) {
        StringBuilder ret = new StringBuilder(length * 2);
        char[] cnum = new char[]{'A', 'B', 'C', 'D', 'E', 'F'};
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

    public static byte[] buildAdnStringHw(int recordSize, String mAlphaTag, String mNumber) {
        int footerOffset = recordSize - 14;
        int gsm7Len = 0;
        int gsm7Converts = 0;
        int ucs80Len = 1;
        int ucs80Converts = 0;
        int ucs81Len = 3;
        int ucs81Converts = 0;
        int ucs82Len = 4;
        int ucs82Converts = 0;
        char baser81 = ' ';
        char baser82Low = ' ';
        char baser82High = ' ';
        boolean useGsm7 = true;
        boolean usePattern81 = true;
        boolean setPattern81 = false;
        boolean usePattern82 = true;
        boolean setPattern82 = false;
        byte[] adnString;
        int i;
        if (mNumber == null || mAlphaTag == null) {
            Rlog.w(LOG_TAG, "[buildAdnString] Empty alpha tag or number");
            adnString = new byte[recordSize];
            for (i = 0; i < recordSize; i++) {
                adnString[i] = (byte) -1;
            }
            return adnString;
        }
        if (mNumber.length() > 20) {
            Rlog.w(LOG_TAG, "[buildAdnString] Max length of dialing number is 20");
            mNumber = mNumber.substring(0, 20);
        }
        int lenTag = mAlphaTag.length();
        for (int index = 0; index < lenTag && (useGsm7 || usePattern81 || usePattern82 || ucs80Len <= footerOffset); index++) {
            char c = mAlphaTag.charAt(index);
            int currGsm7Length = HwGsmAlphabet.UCStoGsm7(c);
            if (currGsm7Length == -1) {
                useGsm7 = false;
            } else if (useGsm7) {
                gsm7Len += currGsm7Length;
                gsm7Converts++;
            }
            if (usePattern81 && ucs81Len < footerOffset) {
                if (-1 == currGsm7Length) {
                    if ((32768 & c) == 32768) {
                        usePattern81 = false;
                    } else if (!setPattern81) {
                        setPattern81 = true;
                        baser81 = (char) (c & 32640);
                    } else if (baser81 != ((char) (c & 32640))) {
                        usePattern81 = false;
                    }
                }
                if (usePattern81) {
                    ucs81Converts++;
                    if (-1 == currGsm7Length) {
                        ucs81Len++;
                    } else {
                        ucs81Len += currGsm7Length;
                    }
                }
            }
            if (usePattern82 && ucs82Len < footerOffset) {
                if (-1 == currGsm7Length) {
                    if (setPattern82) {
                        if (baser82Low > c) {
                            baser82Low = c;
                        } else if (baser82High < c) {
                            baser82High = c;
                        }
                        if (baser82High - baser82Low > 127) {
                            usePattern82 = false;
                        }
                    } else {
                        setPattern82 = true;
                        baser82Low = c;
                        baser82High = c;
                    }
                }
                if (usePattern82) {
                    ucs82Converts++;
                    if (-1 == currGsm7Length) {
                        ucs82Len++;
                    } else {
                        ucs82Len += currGsm7Length;
                    }
                }
            }
            if (ucs80Len < footerOffset) {
                ucs80Len += 2;
                ucs80Converts++;
            }
            if (useGsm7) {
                if (gsm7Len >= footerOffset) {
                    break;
                }
            } else if (usePattern81) {
                if (ucs81Len >= footerOffset) {
                    break;
                }
            } else if (usePattern82) {
                if (ucs82Len >= footerOffset) {
                    break;
                }
            } else if (ucs80Len >= footerOffset) {
                break;
            }
        }
        int bestConverts = gsm7Converts;
        int bestMode = 0;
        int bestLen = gsm7Len;
        if (gsm7Converts < ucs81Converts) {
            bestConverts = ucs81Converts;
            bestMode = 129;
            bestLen = ucs81Len;
        }
        if (bestConverts < ucs82Converts) {
            bestConverts = ucs82Converts;
            bestMode = 130;
            bestLen = ucs82Len;
        }
        if (bestConverts < ucs80Converts) {
            bestConverts = ucs80Converts;
            bestMode = 128;
            bestLen = ucs80Len;
        }
        mAlphaTag = mAlphaTag.substring(0, bestConverts);
        if (bestLen > footerOffset) {
            mAlphaTag = mAlphaTag.substring(0, bestConverts - 1);
            bestLen -= 2;
        }
        adnString = new byte[recordSize];
        for (i = 0; i < recordSize; i++) {
            adnString[i] = (byte) -1;
        }
        if (mNumber.length() > 0) {
            byte[] bcdNumber = PhoneNumberUtils.numberToCalledPartyBCD(mNumber);
            Rlog.e("AdnRecord", "buildAdnString bcdNumber.length = " + bcdNumber.length);
            System.arraycopy(bcdNumber, 0, adnString, footerOffset + 1, bcdNumber.length);
            adnString[footerOffset + 0] = (byte) bcdNumber.length;
        }
        adnString[footerOffset + 12] = (byte) -1;
        adnString[footerOffset + 13] = (byte) -1;
        byte[] byteTag;
        switch (bestMode) {
            case 0:
                byteTag = HwGsmAlphabet.stringToGsm8BitPacked(mAlphaTag);
                System.arraycopy(byteTag, 0, adnString, 0, byteTag.length);
                break;
            case 128:
                try {
                    byteTag = mAlphaTag.getBytes("UTF-16BE");
                } catch (UnsupportedEncodingException e) {
                    byteTag = new byte[0];
                }
                adnString[0] = Byte.MIN_VALUE;
                System.arraycopy(byteTag, 0, adnString, 1, byteTag.length);
                break;
            case 129:
                byteTag = HwGsmAlphabet.stringToUCS81Packed(mAlphaTag, baser81, bestLen - 2);
                adnString[0] = (byte) -127;
                adnString[1] = (byte) (bestLen - 3);
                System.arraycopy(byteTag, 0, adnString, 2, byteTag.length);
                break;
            case 130:
                byteTag = HwGsmAlphabet.stringToUCS82Packed(mAlphaTag, baser82Low, bestLen - 2);
                adnString[0] = (byte) -126;
                adnString[1] = (byte) (bestLen - 4);
                System.arraycopy(byteTag, 0, adnString, 2, byteTag.length);
                break;
        }
        return adnString;
    }

    public static int getAlphaTagEncodingLength(String alphaTag) {
        int gsm7Len = 0;
        int gsm7Converts = 0;
        int ucs80Len = 1;
        int ucs80Converts = 0;
        int ucs81Len = 3;
        int ucs81Converts = 0;
        int ucs82Len = 4;
        int ucs82Converts = 0;
        char baser81 = ' ';
        char baser82Low = ' ';
        char baser82High = ' ';
        boolean useGsm7 = true;
        boolean usePattern81 = true;
        boolean setPattern81 = false;
        boolean usePattern82 = true;
        boolean setPattern82 = false;
        if (alphaTag == null) {
            Rlog.w(LOG_TAG, "[getAlphaTagEncodingLength] Empty alpha tag");
            return 0;
        }
        int lenTag = alphaTag.length();
        for (int index = 0; index < lenTag; index++) {
            char c = alphaTag.charAt(index);
            int currGsm7Length = HwGsmAlphabet.UCStoGsm7(c);
            if (currGsm7Length == -1) {
                useGsm7 = false;
            } else if (useGsm7) {
                gsm7Len += currGsm7Length;
                gsm7Converts++;
            }
            if (usePattern81) {
                if (-1 == currGsm7Length) {
                    if ((32768 & c) == 32768) {
                        usePattern81 = false;
                    } else if (!setPattern81) {
                        setPattern81 = true;
                        baser81 = (char) (c & 32640);
                    } else if (baser81 != ((char) (c & 32640))) {
                        usePattern81 = false;
                    }
                }
                if (usePattern81) {
                    ucs81Converts++;
                    if (-1 == currGsm7Length) {
                        ucs81Len++;
                    } else {
                        ucs81Len += currGsm7Length;
                    }
                }
            }
            if (usePattern82) {
                if (-1 == currGsm7Length) {
                    if (setPattern82) {
                        if (baser82Low > c) {
                            baser82Low = c;
                        } else if (baser82High < c) {
                            baser82High = c;
                        }
                        if (baser82High - baser82Low > 127) {
                            usePattern82 = false;
                        }
                    } else {
                        setPattern82 = true;
                        baser82Low = c;
                        baser82High = c;
                    }
                }
                if (usePattern82) {
                    ucs82Converts++;
                    if (-1 == currGsm7Length) {
                        ucs82Len++;
                    } else {
                        ucs82Len += currGsm7Length;
                    }
                }
            }
            ucs80Len += 2;
            ucs80Converts++;
        }
        int bestConverts = gsm7Converts;
        int bestLen = gsm7Len;
        if (gsm7Converts < ucs81Converts) {
            bestConverts = ucs81Converts;
            bestLen = ucs81Len;
        }
        if (bestConverts < ucs82Converts) {
            bestConverts = ucs82Converts;
            bestLen = ucs82Len;
        }
        if (bestConverts < ucs80Converts) {
            bestLen = ucs80Len;
        }
        return bestLen;
    }

    public static boolean equalAdn(AdnRecord first, AdnRecord second) {
        if (first.mEfid == second.mEfid && first.mRecordNumber == second.mRecordNumber) {
            return true;
        }
        return false;
    }

    public static boolean isContainZeros(byte[] data, int length, int totalLength, int curIndex) {
        int startIndex = totalLength + curIndex;
        int endIndex = data.length;
        int tempTotalLength = totalLength;
        if (totalLength >= length || startIndex > endIndex) {
            return false;
        }
        for (int valueIndex = startIndex; valueIndex < endIndex; valueIndex++) {
            if (data[valueIndex] == (byte) 0) {
                tempTotalLength++;
            }
        }
        if (tempTotalLength == length) {
            return true;
        }
        return false;
    }

    public static boolean arrayCompareNullEqualsEmpty(String[] s1, String[] s2) {
        if (s1 == s2) {
            return true;
        }
        if (s1 == null) {
            s1 = new String[]{""};
        }
        if (s2 == null) {
            s2 = new String[]{""};
        }
        for (String str : s1) {
            if (!TextUtils.isEmpty(str) && !Arrays.asList(s2).contains(str)) {
                return false;
            }
        }
        for (String str2 : s2) {
            if (!TextUtils.isEmpty(str2) && !Arrays.asList(s1).contains(str2)) {
                return false;
            }
        }
        return true;
    }

    public static String[] updateAnrEmailArrayHelper(String[] dest, String[] src, int fileCount) {
        if (fileCount == 0) {
            return null;
        }
        if (dest == null || src == null) {
            return dest;
        }
        int i;
        int j;
        String[] ref = new String[fileCount];
        for (i = 0; i < fileCount; i++) {
            ref[i] = "";
        }
        for (i = 0; i < src.length; i++) {
            if (!TextUtils.isEmpty(src[i])) {
                for (Object equals : dest) {
                    if (src[i].equals(equals)) {
                        ref[i] = src[i];
                        break;
                    }
                }
            }
        }
        for (i = 0; i < dest.length; i++) {
            if (!Arrays.asList(ref).contains(dest[i])) {
                for (j = 0; j < ref.length; j++) {
                    if (TextUtils.isEmpty(ref[j])) {
                        ref[j] = dest[i];
                        break;
                    }
                }
            }
        }
        return ref;
    }

    public static String cdmaDTMFToString(byte[] data, int offset, int length) {
        if (data == null) {
            return null;
        }
        if (data.length < (length + 1) / 2) {
            Rlog.w(LOG_TAG, "cdmaDTMFToString data.length < length");
            length = data.length * 2;
        }
        StringBuilder ret = new StringBuilder();
        if (11 == data.length && 1 == (data[9] & 1)) {
            ret.append('+');
        }
        int count = 0;
        int i = offset;
        while (count < length) {
            char c = intToCdmaDTMFChar(data[i] & 15);
            if ('-' != c) {
                ret.append(c);
            }
            count++;
            if (count == length) {
                break;
            }
            c = intToCdmaDTMFChar((data[i] >> 4) & 15);
            if ('-' != c) {
                ret.append(c);
            }
            count++;
            i++;
        }
        return ret.toString();
    }

    public static char intToCdmaDTMFChar(int c) {
        if (c >= 0 && c <= 9) {
            return (char) (c + 48);
        }
        if (c == 10) {
            return '0';
        }
        if (c == 11) {
            return '*';
        }
        if (c == 12) {
            return '#';
        }
        Rlog.w(LOG_TAG, "intToCdmaDTMFChar invalid char " + ((char) (c + 48)));
        return '-';
    }

    public static int cdmaDTMFCharToint(char c) {
        if (c > '0' && c <= '9') {
            return c - 48;
        }
        if (c == '0') {
            return 10;
        }
        if (c == '*') {
            return 11;
        }
        if (c == '#') {
            return 12;
        }
        throw new RuntimeException("invalid char for BCD " + c);
    }

    public static byte[] stringToCdmaDTMF(String number) {
        int numberLenReal = number.length();
        int numberLenEffective = numberLenReal;
        if (numberLenReal == 0) {
            return new byte[0];
        }
        int i;
        byte[] result = new byte[((numberLenReal + 1) / 2)];
        int digitCount = 0;
        for (int i2 = 0; i2 < numberLenReal; i2++) {
            i = digitCount >> 1;
            result[i] = (byte) (result[i] | ((byte) ((cdmaDTMFCharToint(number.charAt(i2)) & 15) << ((digitCount & 1) == 1 ? 4 : 0))));
            digitCount++;
        }
        if ((digitCount & 1) == 1) {
            i = digitCount >> 1;
            result[i] = (byte) (result[i] | 240);
        }
        return result;
    }

    public static String prependPlusInLongAdnNumber(String Number) {
        if (Number == null || Number.length() == 0) {
            return Number;
        }
        if (!(Number.indexOf(43) != -1)) {
            return Number;
        }
        String[] str = Number.split("\\+");
        StringBuilder ret = new StringBuilder();
        for (String append : str) {
            ret.append(append);
        }
        String retString = ret.toString();
        Matcher m = Pattern.compile("(^[#*])(.*)([#*])(.*)([*]{2})(.*)(#)$").matcher(retString);
        if (!m.matches()) {
            m = Pattern.compile("(^[#*])(.*)([#*])(.*)(#)$").matcher(retString);
            if (!m.matches()) {
                m = Pattern.compile("(^[#*])(.*)([#*])(.*)").matcher(retString);
                if (m.matches()) {
                    ret = new StringBuilder();
                    ret.append(m.group(1));
                    ret.append(m.group(2));
                    ret.append(m.group(3));
                    ret.append("+");
                    ret.append(m.group(4));
                } else {
                    ret = new StringBuilder();
                    ret.append('+');
                    ret.append(retString);
                }
            } else if ("".equals(m.group(2))) {
                ret = new StringBuilder();
                ret.append(m.group(1));
                ret.append(m.group(3));
                ret.append(m.group(4));
                ret.append(m.group(5));
                ret.append("+");
            } else {
                ret = new StringBuilder();
                ret.append(m.group(1));
                ret.append(m.group(2));
                ret.append(m.group(3));
                ret.append("+");
                ret.append(m.group(4));
                ret.append(m.group(5));
            }
        } else if ("".equals(m.group(2))) {
            ret = new StringBuilder();
            ret.append(m.group(1));
            ret.append(m.group(3));
            ret.append(m.group(4));
            ret.append(m.group(5));
            ret.append(m.group(6));
            ret.append(m.group(7));
            ret.append("+");
        } else {
            ret = new StringBuilder();
            ret.append(m.group(1));
            ret.append(m.group(2));
            ret.append(m.group(3));
            ret.append("+");
            ret.append(m.group(4));
            ret.append(m.group(5));
            ret.append(m.group(6));
            ret.append(m.group(7));
        }
        return ret.toString();
    }
}
