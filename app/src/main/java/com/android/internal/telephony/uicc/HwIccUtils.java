package com.android.internal.telephony.uicc;

import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.text.TextUtils;
import com.android.internal.telephony.HwGsmAlphabet;
import com.android.internal.telephony.HwSubscriptionManager;
import com.android.internal.telephony.gsm.HwSmsMessage;
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
        if (length >= ADN_TON_AND_NPI && data[offset] == -128) {
            String str = null;
            try {
                str = new String(data, offset + ADN_TON_AND_NPI, ((length - 1) / EXT_RECORD_TYPE_ADDITIONAL_DATA) * EXT_RECORD_TYPE_ADDITIONAL_DATA, "utf-16be");
            } catch (UnsupportedEncodingException ex) {
                Rlog.e(LOG_TAG, "implausible UnsupportedEncodingException", ex);
            }
            if (str != null) {
                int ucslen = str.length();
                while (ucslen > 0) {
                    if (str.charAt(ucslen - 1) != '\uffff') {
                        break;
                    }
                    ucslen--;
                }
                return str.substring(ADN_BCD_NUMBER_LENGTH, ucslen);
            }
        }
        boolean isucs2 = false;
        char base = '\u0000';
        int len = ADN_BCD_NUMBER_LENGTH;
        if (length >= EXT_RECORD_TYPE_MASK && data[offset] == -127) {
            len = data[offset + ADN_TON_AND_NPI] & HwSubscriptionManager.SUB_INIT_STATE;
            if (len > length - 3) {
                len = length - 3;
            }
            base = (char) ((data[offset + EXT_RECORD_TYPE_ADDITIONAL_DATA] & HwSubscriptionManager.SUB_INIT_STATE) << 7);
            offset += EXT_RECORD_TYPE_MASK;
            isucs2 = true;
        } else if (length >= 4 && data[offset] == -126) {
            len = data[offset + ADN_TON_AND_NPI] & HwSubscriptionManager.SUB_INIT_STATE;
            if (len > length - 4) {
                len = length - 4;
            }
            int i = data[offset + EXT_RECORD_TYPE_ADDITIONAL_DATA] & HwSubscriptionManager.SUB_INIT_STATE;
            base = (char) ((r0 << 8) | (data[offset + EXT_RECORD_TYPE_MASK] & HwSubscriptionManager.SUB_INIT_STATE));
            offset += 4;
            isucs2 = true;
        }
        if (isucs2) {
            StringBuilder ret = new StringBuilder();
            while (len > 0) {
                if (data[offset] < null) {
                    ret.append((char) ((data[offset] & 127) + base));
                    offset += ADN_TON_AND_NPI;
                    len--;
                }
                int count = ADN_BCD_NUMBER_LENGTH;
                while (count < len && data[offset + count] >= null) {
                    count += ADN_TON_AND_NPI;
                }
                ret.append(HwGsmAlphabet.gsm8BitUnpackedToString(data, offset, count));
                offset += count;
                len -= count;
            }
            return ret.toString();
        }
        String defaultCharset = "";
        try {
            defaultCharset = Resources.getSystem().getString(17039435);
        } catch (NotFoundException e) {
        }
        return HwGsmAlphabet.gsm8BitUnpackedToString(data, offset, length, defaultCharset.trim(), true);
    }

    public static int hexCharToInt(char c) {
        if (c >= '0' && c <= '9') {
            return c - 48;
        }
        if (c >= 'A' && c <= 'F') {
            return (c - 65) + MAX_EXT_CALLED_PARTY_LENGTH;
        }
        if (c >= 'a' && c <= 'f') {
            return (c - 97) + MAX_EXT_CALLED_PARTY_LENGTH;
        }
        throw new RuntimeException("invalid hex char '" + c + "'");
    }

    public static String bcdIccidToString(byte[] data, int offset, int length) {
        StringBuilder ret = new StringBuilder(length * EXT_RECORD_TYPE_ADDITIONAL_DATA);
        char[] cnum = new char[]{'A', 'B', 'C', 'D', 'E', 'F'};
        for (int i = offset; i < offset + length; i += ADN_TON_AND_NPI) {
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
        int gsm7Len = ADN_BCD_NUMBER_LENGTH;
        int gsm7Converts = ADN_BCD_NUMBER_LENGTH;
        int ucs80Len = ADN_TON_AND_NPI;
        int ucs80Converts = ADN_BCD_NUMBER_LENGTH;
        int ucs81Len = EXT_RECORD_TYPE_MASK;
        int ucs81Converts = ADN_BCD_NUMBER_LENGTH;
        int ucs82Len = 4;
        int ucs82Converts = ADN_BCD_NUMBER_LENGTH;
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
            for (i = ADN_BCD_NUMBER_LENGTH; i < recordSize; i += ADN_TON_AND_NPI) {
                adnString[i] = (byte) -1;
            }
            return adnString;
        }
        if (mNumber.length() > 20) {
            Rlog.w(LOG_TAG, "[buildAdnString] Max length of dialing number is 20");
            mNumber = mNumber.substring(ADN_BCD_NUMBER_LENGTH, 20);
        }
        int lenTag = mAlphaTag.length();
        for (int index = ADN_BCD_NUMBER_LENGTH; index < lenTag && (useGsm7 || usePattern81 || usePattern82 || ucs80Len <= footerOffset); index += ADN_TON_AND_NPI) {
            char c = mAlphaTag.charAt(index);
            int currGsm7Length = HwGsmAlphabet.UCStoGsm7(c);
            if (currGsm7Length == -1) {
                useGsm7 = false;
            } else if (useGsm7) {
                gsm7Len += currGsm7Length;
                gsm7Converts += ADN_TON_AND_NPI;
            }
            if (usePattern81 && ucs81Len < footerOffset) {
                if (-1 == currGsm7Length) {
                    if ((32768 & c) == 32768) {
                        usePattern81 = false;
                    } else if (setPattern81) {
                        char c2 = (char) (c & 32640);
                        if (baser81 != r0) {
                            usePattern81 = false;
                        }
                    } else {
                        setPattern81 = true;
                        baser81 = (char) (c & 32640);
                    }
                }
                if (usePattern81) {
                    ucs81Converts += ADN_TON_AND_NPI;
                    if (-1 == currGsm7Length) {
                        ucs81Len += ADN_TON_AND_NPI;
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
                    ucs82Converts += ADN_TON_AND_NPI;
                    if (-1 == currGsm7Length) {
                        ucs82Len += ADN_TON_AND_NPI;
                    } else {
                        ucs82Len += currGsm7Length;
                    }
                }
            }
            if (ucs80Len < footerOffset) {
                ucs80Len += EXT_RECORD_TYPE_ADDITIONAL_DATA;
                ucs80Converts += ADN_TON_AND_NPI;
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
        int bestMode = ADN_BCD_NUMBER_LENGTH;
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
            bestMode = HwSmsMessage.SMS_TOA_UNKNOWN;
            bestLen = ucs80Len;
        }
        mAlphaTag = mAlphaTag.substring(ADN_BCD_NUMBER_LENGTH, bestConverts);
        if (bestLen > footerOffset) {
            mAlphaTag = mAlphaTag.substring(ADN_BCD_NUMBER_LENGTH, bestConverts - 1);
            bestLen -= 2;
        }
        adnString = new byte[recordSize];
        for (i = ADN_BCD_NUMBER_LENGTH; i < recordSize; i += ADN_TON_AND_NPI) {
            adnString[i] = (byte) -1;
        }
        if (mNumber.length() > 0) {
            byte[] bcdNumber = PhoneNumberUtils.numberToCalledPartyBCD(mNumber);
            Rlog.e("AdnRecord", "buildAdnString bcdNumber.length = " + bcdNumber.length);
            System.arraycopy(bcdNumber, ADN_BCD_NUMBER_LENGTH, adnString, footerOffset + ADN_TON_AND_NPI, bcdNumber.length);
            adnString[footerOffset + ADN_BCD_NUMBER_LENGTH] = (byte) bcdNumber.length;
        }
        adnString[footerOffset + ADN_CAPABILITY_ID] = (byte) -1;
        adnString[footerOffset + EXT_RECORD_LENGTH_BYTES] = (byte) -1;
        byte[] byteTag;
        switch (bestMode) {
            case ADN_BCD_NUMBER_LENGTH /*0*/:
                byteTag = HwGsmAlphabet.stringToGsm8BitPacked(mAlphaTag);
                System.arraycopy(byteTag, ADN_BCD_NUMBER_LENGTH, adnString, ADN_BCD_NUMBER_LENGTH, byteTag.length);
                break;
            case HwSmsMessage.SMS_TOA_UNKNOWN /*128*/:
                try {
                    byteTag = mAlphaTag.getBytes("UTF-16BE");
                } catch (UnsupportedEncodingException e) {
                    byteTag = new byte[ADN_BCD_NUMBER_LENGTH];
                }
                adnString[ADN_BCD_NUMBER_LENGTH] = Byte.MIN_VALUE;
                System.arraycopy(byteTag, ADN_BCD_NUMBER_LENGTH, adnString, ADN_TON_AND_NPI, byteTag.length);
                break;
            case 129:
                byteTag = HwGsmAlphabet.stringToUCS81Packed(mAlphaTag, baser81, bestLen - 2);
                adnString[ADN_BCD_NUMBER_LENGTH] = (byte) -127;
                adnString[ADN_TON_AND_NPI] = (byte) (bestLen - 3);
                System.arraycopy(byteTag, ADN_BCD_NUMBER_LENGTH, adnString, EXT_RECORD_TYPE_ADDITIONAL_DATA, byteTag.length);
                break;
            case 130:
                byteTag = HwGsmAlphabet.stringToUCS82Packed(mAlphaTag, baser82Low, bestLen - 2);
                adnString[ADN_BCD_NUMBER_LENGTH] = (byte) -126;
                adnString[ADN_TON_AND_NPI] = (byte) (bestLen - 4);
                System.arraycopy(byteTag, ADN_BCD_NUMBER_LENGTH, adnString, EXT_RECORD_TYPE_ADDITIONAL_DATA, byteTag.length);
                break;
        }
        return adnString;
    }

    public static int getAlphaTagEncodingLength(String alphaTag) {
        int gsm7Len = ADN_BCD_NUMBER_LENGTH;
        int gsm7Converts = ADN_BCD_NUMBER_LENGTH;
        int ucs80Len = ADN_TON_AND_NPI;
        int ucs80Converts = ADN_BCD_NUMBER_LENGTH;
        int ucs81Len = EXT_RECORD_TYPE_MASK;
        int ucs81Converts = ADN_BCD_NUMBER_LENGTH;
        int ucs82Len = 4;
        int ucs82Converts = ADN_BCD_NUMBER_LENGTH;
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
            return ADN_BCD_NUMBER_LENGTH;
        }
        int lenTag = alphaTag.length();
        for (int index = ADN_BCD_NUMBER_LENGTH; index < lenTag; index += ADN_TON_AND_NPI) {
            char c = alphaTag.charAt(index);
            int currGsm7Length = HwGsmAlphabet.UCStoGsm7(c);
            if (currGsm7Length == -1) {
                useGsm7 = false;
            } else if (useGsm7) {
                gsm7Len += currGsm7Length;
                gsm7Converts += ADN_TON_AND_NPI;
            }
            if (usePattern81) {
                if (-1 == currGsm7Length) {
                    if ((32768 & c) == 32768) {
                        usePattern81 = false;
                    } else if (setPattern81) {
                        char c2 = (char) (c & 32640);
                        if (baser81 != r0) {
                            usePattern81 = false;
                        }
                    } else {
                        setPattern81 = true;
                        baser81 = (char) (c & 32640);
                    }
                }
                if (usePattern81) {
                    ucs81Converts += ADN_TON_AND_NPI;
                    if (-1 == currGsm7Length) {
                        ucs81Len += ADN_TON_AND_NPI;
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
                    ucs82Converts += ADN_TON_AND_NPI;
                    if (-1 == currGsm7Length) {
                        ucs82Len += ADN_TON_AND_NPI;
                    } else {
                        ucs82Len += currGsm7Length;
                    }
                }
            }
            ucs80Len += EXT_RECORD_TYPE_ADDITIONAL_DATA;
            ucs80Converts += ADN_TON_AND_NPI;
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
        for (int valueIndex = startIndex; valueIndex < endIndex; valueIndex += ADN_TON_AND_NPI) {
            if (data[valueIndex] == null) {
                tempTotalLength += ADN_TON_AND_NPI;
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
        int i;
        if (s1 == null) {
            s1 = new String[ADN_TON_AND_NPI];
            s1[ADN_BCD_NUMBER_LENGTH] = "";
        }
        if (s2 == null) {
            s2 = new String[ADN_TON_AND_NPI];
            s2[ADN_BCD_NUMBER_LENGTH] = "";
        }
        int length = s1.length;
        for (i = ADN_BCD_NUMBER_LENGTH; i < length; i += ADN_TON_AND_NPI) {
            String str = s1[i];
            if (!TextUtils.isEmpty(str) && !Arrays.asList(s2).contains(str)) {
                return false;
            }
        }
        length = s2.length;
        for (i = ADN_BCD_NUMBER_LENGTH; i < length; i += ADN_TON_AND_NPI) {
            str = s2[i];
            if (!TextUtils.isEmpty(str) && !Arrays.asList(s1).contains(str)) {
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
        for (i = ADN_BCD_NUMBER_LENGTH; i < fileCount; i += ADN_TON_AND_NPI) {
            ref[i] = "";
        }
        for (i = ADN_BCD_NUMBER_LENGTH; i < src.length; i += ADN_TON_AND_NPI) {
            if (!TextUtils.isEmpty(src[i])) {
                for (j = ADN_BCD_NUMBER_LENGTH; j < dest.length; j += ADN_TON_AND_NPI) {
                    if (src[i].equals(dest[j])) {
                        ref[i] = src[i];
                        break;
                    }
                }
            }
        }
        for (i = ADN_BCD_NUMBER_LENGTH; i < dest.length; i += ADN_TON_AND_NPI) {
            if (!Arrays.asList(ref).contains(dest[i])) {
                for (j = ADN_BCD_NUMBER_LENGTH; j < ref.length; j += ADN_TON_AND_NPI) {
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
        if (data.length < (length + ADN_TON_AND_NPI) / EXT_RECORD_TYPE_ADDITIONAL_DATA) {
            Rlog.w(LOG_TAG, "cdmaDTMFToString data.length < length");
            length = data.length * EXT_RECORD_TYPE_ADDITIONAL_DATA;
        }
        StringBuilder ret = new StringBuilder();
        if (MDN_BYTE_LENGTH == data.length && ADN_TON_AND_NPI == (data[9] & ADN_TON_AND_NPI)) {
            ret.append('+');
        }
        int count = ADN_BCD_NUMBER_LENGTH;
        int i = offset;
        while (count < length) {
            char c = intToCdmaDTMFChar(data[i] & 15);
            if ('-' != c) {
                ret.append(c);
            }
            count += ADN_TON_AND_NPI;
            if (count == length) {
                break;
            }
            c = intToCdmaDTMFChar((data[i] >> 4) & 15);
            if ('-' != c) {
                ret.append(c);
            }
            count += ADN_TON_AND_NPI;
            i += ADN_TON_AND_NPI;
        }
        return ret.toString();
    }

    public static char intToCdmaDTMFChar(int c) {
        if (c >= 0 && c <= 9) {
            return (char) (c + 48);
        }
        if (c == MAX_EXT_CALLED_PARTY_LENGTH) {
            return '0';
        }
        if (c == MDN_BYTE_LENGTH) {
            return '*';
        }
        if (c == ADN_CAPABILITY_ID) {
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
            return MAX_EXT_CALLED_PARTY_LENGTH;
        }
        if (c == '*') {
            return MDN_BYTE_LENGTH;
        }
        if (c == '#') {
            return ADN_CAPABILITY_ID;
        }
        throw new RuntimeException("invalid char for BCD " + c);
    }

    public static byte[] stringToCdmaDTMF(String number) {
        int numberLenReal = number.length();
        int numberLenEffective = numberLenReal;
        if (numberLenReal == 0) {
            return new byte[ADN_BCD_NUMBER_LENGTH];
        }
        byte[] result = new byte[((numberLenReal + ADN_TON_AND_NPI) / EXT_RECORD_TYPE_ADDITIONAL_DATA)];
        int digitCount = ADN_BCD_NUMBER_LENGTH;
        for (int i = ADN_BCD_NUMBER_LENGTH; i < numberLenReal; i += ADN_TON_AND_NPI) {
            int i2 = digitCount >> ADN_TON_AND_NPI;
            result[i2] = (byte) (result[i2] | ((byte) ((cdmaDTMFCharToint(number.charAt(i)) & 15) << ((digitCount & ADN_TON_AND_NPI) == ADN_TON_AND_NPI ? 4 : ADN_BCD_NUMBER_LENGTH))));
            digitCount += ADN_TON_AND_NPI;
        }
        if ((digitCount & ADN_TON_AND_NPI) == ADN_TON_AND_NPI) {
            i2 = digitCount >> ADN_TON_AND_NPI;
            result[i2] = (byte) (result[i2] | 240);
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
        for (int i = ADN_BCD_NUMBER_LENGTH; i < str.length; i += ADN_TON_AND_NPI) {
            ret.append(str[i]);
        }
        String retString = ret.toString();
        Matcher m = Pattern.compile("(^[#*])(.*)([#*])(.*)([*]{2})(.*)(#)$").matcher(retString);
        if (!m.matches()) {
            m = Pattern.compile("(^[#*])(.*)([#*])(.*)(#)$").matcher(retString);
            if (!m.matches()) {
                m = Pattern.compile("(^[#*])(.*)([#*])(.*)").matcher(retString);
                if (m.matches()) {
                    ret = new StringBuilder();
                    ret.append(m.group(ADN_TON_AND_NPI));
                    ret.append(m.group(EXT_RECORD_TYPE_ADDITIONAL_DATA));
                    ret.append(m.group(EXT_RECORD_TYPE_MASK));
                    ret.append("+");
                    ret.append(m.group(4));
                } else {
                    ret = new StringBuilder();
                    ret.append('+');
                    ret.append(retString);
                }
            } else if ("".equals(m.group(EXT_RECORD_TYPE_ADDITIONAL_DATA))) {
                ret = new StringBuilder();
                ret.append(m.group(ADN_TON_AND_NPI));
                ret.append(m.group(EXT_RECORD_TYPE_MASK));
                ret.append(m.group(4));
                ret.append(m.group(5));
                ret.append("+");
            } else {
                ret = new StringBuilder();
                ret.append(m.group(ADN_TON_AND_NPI));
                ret.append(m.group(EXT_RECORD_TYPE_ADDITIONAL_DATA));
                ret.append(m.group(EXT_RECORD_TYPE_MASK));
                ret.append("+");
                ret.append(m.group(4));
                ret.append(m.group(5));
            }
        } else if ("".equals(m.group(EXT_RECORD_TYPE_ADDITIONAL_DATA))) {
            ret = new StringBuilder();
            ret.append(m.group(ADN_TON_AND_NPI));
            ret.append(m.group(EXT_RECORD_TYPE_MASK));
            ret.append(m.group(4));
            ret.append(m.group(5));
            ret.append(m.group(6));
            ret.append(m.group(7));
            ret.append("+");
        } else {
            ret = new StringBuilder();
            ret.append(m.group(ADN_TON_AND_NPI));
            ret.append(m.group(EXT_RECORD_TYPE_ADDITIONAL_DATA));
            ret.append(m.group(EXT_RECORD_TYPE_MASK));
            ret.append("+");
            ret.append(m.group(4));
            ret.append(m.group(5));
            ret.append(m.group(6));
            ret.append(m.group(7));
        }
        return ret.toString();
    }
}
