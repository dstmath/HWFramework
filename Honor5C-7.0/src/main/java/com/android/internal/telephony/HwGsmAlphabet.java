package com.android.internal.telephony;

import android.content.res.Resources;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.text.TextUtils;
import android.util.SparseIntArray;
import com.android.internal.telephony.gsm.HwSmsMessage;
import com.android.internal.telephony.vsim.HwVSimEventReport;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class HwGsmAlphabet {
    public static final byte GSM_EXTENDED_ESCAPE = (byte) 27;
    private static final String TAG = "GSM";
    public static final int UDH_SEPTET_COST_CONCATENATED_MESSAGE = 6;
    public static final int UDH_SEPTET_COST_LENGTH = 1;
    public static final int UDH_SEPTET_COST_ONE_SHIFT_TABLE = 4;
    public static final int UDH_SEPTET_COST_TWO_SHIFT_TABLES = 7;
    private static final SparseIntArray[] sCharsToGsmTables = null;
    private static final SparseIntArray[] sCharsToShiftTables = null;
    private static boolean sDisableCountryEncodingCheck;
    private static int[] sEnabledLockingShiftTables;
    private static int[] sEnabledSingleShiftTables;
    private static int sHighestEnabledSingleShiftCode;
    private static final String[] sLanguageShiftTables = null;
    private static final String[] sLanguageTables = null;

    private static class LanguagePairCount {
        final int languageCode;
        final int[] septetCounts;
        final int[] unencodableCounts;

        LanguagePairCount(int code) {
            this.languageCode = code;
            int maxSingleShiftCode = HwGsmAlphabet.sHighestEnabledSingleShiftCode;
            this.septetCounts = new int[(maxSingleShiftCode + HwGsmAlphabet.UDH_SEPTET_COST_LENGTH)];
            this.unencodableCounts = new int[(maxSingleShiftCode + HwGsmAlphabet.UDH_SEPTET_COST_LENGTH)];
            int tableOffset = 0;
            for (int i = HwGsmAlphabet.UDH_SEPTET_COST_LENGTH; i <= maxSingleShiftCode; i += HwGsmAlphabet.UDH_SEPTET_COST_LENGTH) {
                if (HwGsmAlphabet.sEnabledSingleShiftTables[tableOffset] == i) {
                    tableOffset += HwGsmAlphabet.UDH_SEPTET_COST_LENGTH;
                } else {
                    this.septetCounts[i] = -1;
                }
            }
            if (code == HwGsmAlphabet.UDH_SEPTET_COST_LENGTH && maxSingleShiftCode >= HwGsmAlphabet.UDH_SEPTET_COST_LENGTH) {
                this.septetCounts[HwGsmAlphabet.UDH_SEPTET_COST_LENGTH] = -1;
            } else if (code == 3 && maxSingleShiftCode >= 2) {
                this.septetCounts[2] = -1;
            }
        }
    }

    public static class TextEncodingDetails {
        public int codeUnitCount;
        public int codeUnitSize;
        public int codeUnitsRemaining;
        public int languageShiftTable;
        public int languageTable;
        public int msgCount;

        public String toString() {
            return "TextEncodingDetails { msgCount=" + this.msgCount + ", codeUnitCount=" + this.codeUnitCount + ", codeUnitsRemaining=" + this.codeUnitsRemaining + ", codeUnitSize=" + this.codeUnitSize + ", languageTable=" + this.languageTable + ", languageShiftTable=" + this.languageShiftTable + " }";
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.HwGsmAlphabet.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.HwGsmAlphabet.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.HwGsmAlphabet.<clinit>():void");
    }

    private HwGsmAlphabet() {
    }

    public static int charToGsm(char c) {
        try {
            return charToGsm(c, false);
        } catch (EncodeException e) {
            return sCharsToGsmTables[0].get(32, 32);
        }
    }

    public static int charToGsm(char c, boolean throwException) throws EncodeException {
        int ret = sCharsToGsmTables[0].get(c, -1);
        if (ret != -1) {
            return ret;
        }
        if (sCharsToShiftTables[0].get(c, -1) != -1) {
            return 27;
        }
        if (!throwException) {
            return sCharsToGsmTables[0].get(32, 32);
        }
        throw new EncodeException(c);
    }

    public static int charToGsmExtended(char c) {
        int ret = sCharsToShiftTables[0].get(c, -1);
        if (ret == -1) {
            return sCharsToGsmTables[0].get(32, 32);
        }
        return ret;
    }

    public static char gsmToChar(int gsmChar) {
        if (gsmChar < 0 || gsmChar >= HwSmsMessage.SMS_TOA_UNKNOWN) {
            return ' ';
        }
        return sLanguageTables[0].charAt(gsmChar);
    }

    public static char gsmExtendedToChar(int gsmChar) {
        if (gsmChar == 27 || gsmChar < 0 || gsmChar >= HwSmsMessage.SMS_TOA_UNKNOWN) {
            return ' ';
        }
        char c = sLanguageShiftTables[0].charAt(gsmChar);
        if (c == ' ') {
            return sLanguageTables[0].charAt(gsmChar);
        }
        return c;
    }

    public static byte[] stringToGsm7BitPackedWithHeader(String data, byte[] header) throws EncodeException {
        return stringToGsm7BitPackedWithHeader(data, header, 0, 0);
    }

    public static byte[] stringToGsm7BitPackedWithHeader(String data, byte[] header, int languageTable, int languageShiftTable) throws EncodeException {
        if (header == null || header.length == 0) {
            return stringToGsm7BitPacked(data, languageTable, languageShiftTable);
        }
        byte[] ret = stringToGsm7BitPacked(data, (((header.length + UDH_SEPTET_COST_LENGTH) * 8) + UDH_SEPTET_COST_CONCATENATED_MESSAGE) / UDH_SEPTET_COST_TWO_SHIFT_TABLES, true, languageTable, languageShiftTable);
        ret[UDH_SEPTET_COST_LENGTH] = (byte) header.length;
        System.arraycopy(header, 0, ret, 2, header.length);
        return ret;
    }

    public static byte[] stringToGsm7BitPacked(String data) throws EncodeException {
        return stringToGsm7BitPacked(data, 0, true, 0, 0);
    }

    public static byte[] stringToGsm7BitPacked(String data, int languageTable, int languageShiftTable) throws EncodeException {
        return stringToGsm7BitPacked(data, 0, true, languageTable, languageShiftTable);
    }

    public static byte[] stringToGsm7BitPacked(String data, int startingSeptetOffset, boolean throwException, int languageTable, int languageShiftTable) throws EncodeException {
        int dataLen = data.length();
        int septetCount = countGsmSeptetsUsingTables(data, !throwException, languageTable, languageShiftTable);
        if (septetCount == -1) {
            throw new EncodeException("countGsmSeptetsUsingTables(): unencodable char");
        }
        septetCount += startingSeptetOffset;
        if (septetCount > HwSubscriptionManager.SUB_INIT_STATE) {
            throw new EncodeException("Payload cannot exceed 255 septets");
        }
        byte[] ret = new byte[((((septetCount * UDH_SEPTET_COST_TWO_SHIFT_TABLES) + UDH_SEPTET_COST_TWO_SHIFT_TABLES) / 8) + UDH_SEPTET_COST_LENGTH)];
        SparseIntArray charToLanguageTable = sCharsToGsmTables[languageTable];
        SparseIntArray charToShiftTable = sCharsToShiftTables[languageShiftTable];
        int i = 0;
        int septets = startingSeptetOffset;
        int bitOffset = startingSeptetOffset * UDH_SEPTET_COST_TWO_SHIFT_TABLES;
        while (i < dataLen && septets < septetCount) {
            char c = data.charAt(i);
            int v = charToLanguageTable.get(c, -1);
            if (v == -1) {
                v = charToShiftTable.get(c, -1);
                if (v != -1) {
                    packSmsChar(ret, bitOffset, 27);
                    bitOffset += UDH_SEPTET_COST_TWO_SHIFT_TABLES;
                    septets += UDH_SEPTET_COST_LENGTH;
                } else if (throwException) {
                    throw new EncodeException("stringToGsm7BitPacked(): unencodable char");
                } else {
                    v = charToLanguageTable.get(32, 32);
                }
            }
            packSmsChar(ret, bitOffset, v);
            septets += UDH_SEPTET_COST_LENGTH;
            i += UDH_SEPTET_COST_LENGTH;
            bitOffset += UDH_SEPTET_COST_TWO_SHIFT_TABLES;
        }
        ret[0] = (byte) septetCount;
        return ret;
    }

    private static void packSmsChar(byte[] packedChars, int bitOffset, int value) {
        int shift = bitOffset % 8;
        int byteOffset = (bitOffset / 8) + UDH_SEPTET_COST_LENGTH;
        packedChars[byteOffset] = (byte) (packedChars[byteOffset] | (value << shift));
        if (shift > UDH_SEPTET_COST_LENGTH) {
            packedChars[byteOffset + UDH_SEPTET_COST_LENGTH] = (byte) (value >> (8 - shift));
        }
    }

    public static String gsm7BitPackedToString(byte[] pdu, int offset, int lengthSeptets) {
        return gsm7BitPackedToString(pdu, offset, lengthSeptets, 0, 0, 0);
    }

    public static String gsm7BitPackedToString(byte[] pdu, int offset, int lengthSeptets, int numPaddingBits, int languageTable, int shiftTable) {
        StringBuilder ret = new StringBuilder(lengthSeptets);
        if (languageTable < 0 || languageTable > sLanguageTables.length) {
            Rlog.w(TAG, "unknown language table " + languageTable + ", using default");
            languageTable = 0;
        }
        if (shiftTable < 0 || shiftTable > sLanguageShiftTables.length) {
            Rlog.w(TAG, "unknown single shift table " + shiftTable + ", using default");
            shiftTable = 0;
        }
        boolean prevCharWasEscape = false;
        try {
            String languageTableToChar = sLanguageTables[languageTable];
            String shiftTableToChar = sLanguageShiftTables[shiftTable];
            if (languageTableToChar.isEmpty()) {
                Rlog.w(TAG, "no language table for code " + languageTable + ", using default");
                languageTableToChar = sLanguageTables[0];
            }
            if (shiftTableToChar.isEmpty()) {
                Rlog.w(TAG, "no single shift table for code " + shiftTable + ", using default");
                shiftTableToChar = sLanguageShiftTables[0];
            }
            for (int i = 0; i < lengthSeptets; i += UDH_SEPTET_COST_LENGTH) {
                int bitOffset = (i * UDH_SEPTET_COST_TWO_SHIFT_TABLES) + numPaddingBits;
                int byteOffset = bitOffset / 8;
                int shift = bitOffset % 8;
                int gsmVal = (pdu[offset + byteOffset] >> shift) & 127;
                if (shift > UDH_SEPTET_COST_LENGTH) {
                    gsmVal = (gsmVal & (127 >> (shift - 1))) | ((pdu[(offset + byteOffset) + UDH_SEPTET_COST_LENGTH] << (8 - shift)) & 127);
                }
                if (prevCharWasEscape) {
                    if (gsmVal == 27) {
                        ret.append(' ');
                    } else {
                        char c = shiftTableToChar.charAt(gsmVal);
                        if (c == ' ') {
                            ret.append(languageTableToChar.charAt(gsmVal));
                        } else {
                            ret.append(c);
                        }
                    }
                    prevCharWasEscape = false;
                } else if (gsmVal == 27) {
                    prevCharWasEscape = true;
                } else {
                    ret.append(languageTableToChar.charAt(gsmVal));
                }
            }
            return ret.toString();
        } catch (RuntimeException ex) {
            Rlog.e(TAG, "Error GSM 7 bit packed: ", ex);
            return null;
        }
    }

    public static String gsm8BitUnpackedToString(byte[] data, int offset, int length) {
        return gsm8BitUnpackedToString(data, offset, length, "");
    }

    public static String gsm8BitUnpackedToString(byte[] data, int offset, int length, String characterset) {
        boolean isMbcs = false;
        Charset charset = null;
        ByteBuffer mbcsBuffer = null;
        if (!TextUtils.isEmpty(characterset)) {
            if (!characterset.equalsIgnoreCase("us-ascii") && Charset.isSupported(characterset)) {
                isMbcs = true;
                charset = Charset.forName(characterset);
                mbcsBuffer = ByteBuffer.allocate(2);
            }
        }
        String languageTableToChar = sLanguageTables[0];
        String shiftTableToChar = sLanguageShiftTables[0];
        StringBuilder ret = new StringBuilder(length);
        boolean prevWasEscape = false;
        int i = offset;
        while (i < offset + length) {
            int c = data[i] & HwSubscriptionManager.SUB_INIT_STATE;
            if (c == HwSubscriptionManager.SUB_INIT_STATE) {
                break;
            }
            int i2;
            if (c != 27) {
                if (prevWasEscape) {
                    char shiftChar = shiftTableToChar.charAt(c);
                    if (shiftChar == ' ') {
                        ret.append(languageTableToChar.charAt(c));
                        i2 = i;
                    } else {
                        ret.append(shiftChar);
                        i2 = i;
                    }
                } else if (!isMbcs || c < HwSmsMessage.SMS_TOA_UNKNOWN || i + UDH_SEPTET_COST_LENGTH >= offset + length) {
                    ret.append(languageTableToChar.charAt(c));
                    i2 = i;
                } else {
                    mbcsBuffer.clear();
                    i2 = i + UDH_SEPTET_COST_LENGTH;
                    mbcsBuffer.put(data, i, 2);
                    mbcsBuffer.flip();
                    ret.append(charset.decode(mbcsBuffer).toString());
                }
                prevWasEscape = false;
            } else if (prevWasEscape) {
                ret.append(' ');
                prevWasEscape = false;
                i2 = i;
            } else {
                prevWasEscape = true;
                i2 = i;
            }
            i = i2 + UDH_SEPTET_COST_LENGTH;
        }
        return ret.toString();
    }

    public static String gsm8BitUnpackedToString(byte[] data, int offset, int length, boolean needConvertCharacter) {
        return gsm8BitUnpackedToString(data, offset, length, "", needConvertCharacter);
    }

    public static String gsm8BitUnpackedToString(byte[] data, int offset, int length, String characterset, boolean needConvertCharacter) {
        boolean isMbcs = false;
        Charset charset = null;
        ByteBuffer mbcsBuffer = null;
        if (!(TextUtils.isEmpty(characterset) || characterset.equalsIgnoreCase("us-ascii") || !Charset.isSupported(characterset))) {
            isMbcs = true;
            charset = Charset.forName(characterset);
            mbcsBuffer = ByteBuffer.allocate(2);
        }
        StringBuilder ret = new StringBuilder(length);
        boolean prevWasEscape = false;
        int i = offset;
        while (i < offset + length) {
            int c = data[i] & HwSubscriptionManager.SUB_INIT_STATE;
            if (c == HwSubscriptionManager.SUB_INIT_STATE) {
                break;
            }
            int i2;
            if (c != 27) {
                if (needConvertCharacter) {
                    if (95 == c) {
                        c = 17;
                    }
                    if (64 == c) {
                        c = 0;
                    }
                }
                if (prevWasEscape) {
                    ret.append(gsmExtendedToChar(c));
                    i2 = i;
                } else if (!isMbcs || c < HwSmsMessage.SMS_TOA_UNKNOWN || i + UDH_SEPTET_COST_LENGTH >= offset + length) {
                    ret.append(gsmToChar(c));
                    i2 = i;
                } else {
                    mbcsBuffer.clear();
                    i2 = i + UDH_SEPTET_COST_LENGTH;
                    mbcsBuffer.put(data, i, 2);
                    mbcsBuffer.flip();
                    ret.append(charset.decode(mbcsBuffer).toString());
                }
                prevWasEscape = false;
            } else if (prevWasEscape) {
                ret.append(' ');
                prevWasEscape = false;
                i2 = i;
            } else {
                prevWasEscape = true;
                i2 = i;
            }
            i = i2 + UDH_SEPTET_COST_LENGTH;
        }
        return ret.toString();
    }

    public static byte[] stringToGsm8BitPacked(String s) {
        byte[] ret = new byte[countGsmSeptetsUsingTables(s, true, 0, 0)];
        stringToGsm8BitUnpackedField(s, ret, 0, ret.length);
        return ret;
    }

    public static void stringToGsm8BitUnpackedField(String s, byte[] dest, int offset, int length) {
        int outByteIndex = offset;
        SparseIntArray charToLanguageTable = sCharsToGsmTables[0];
        SparseIntArray charToShiftTable = sCharsToShiftTables[0];
        int sz = s.length();
        int outByteIndex2 = outByteIndex;
        for (int i = 0; i < sz && outByteIndex2 - offset < length; i += UDH_SEPTET_COST_LENGTH) {
            char c = s.charAt(i);
            int v = charToLanguageTable.get(c, -1);
            if (v == -1) {
                v = charToShiftTable.get(c, -1);
                if (v == -1) {
                    v = charToLanguageTable.get(32, 32);
                    outByteIndex = outByteIndex2;
                } else if ((outByteIndex2 + UDH_SEPTET_COST_LENGTH) - offset >= length) {
                    break;
                } else {
                    outByteIndex = outByteIndex2 + UDH_SEPTET_COST_LENGTH;
                    dest[outByteIndex2] = GSM_EXTENDED_ESCAPE;
                }
            } else {
                outByteIndex = outByteIndex2;
            }
            outByteIndex2 = outByteIndex + UDH_SEPTET_COST_LENGTH;
            dest[outByteIndex] = (byte) v;
        }
        while (outByteIndex2 - offset < length) {
            outByteIndex = outByteIndex2 + UDH_SEPTET_COST_LENGTH;
            dest[outByteIndex2] = (byte) -1;
            outByteIndex2 = outByteIndex;
        }
    }

    public static int UCStoGsm7(char c) {
        if (sCharsToGsmTables[0].get(c, -1) != -1) {
            return UDH_SEPTET_COST_LENGTH;
        }
        if (sCharsToShiftTables[0].get(c, -1) != -1) {
            return 2;
        }
        return -1;
    }

    public static int charToGsm7bit(char c) {
        int ret = sCharsToGsmTables[0].get(c, -1);
        if (-1 != ret) {
            return ret;
        }
        if (-1 == sCharsToShiftTables[0].get(c, -1)) {
            return -1;
        }
        return 27;
    }

    public static byte[] stringToUCS81Packed(String s, char baser81, int septets) {
        byte[] dest = new byte[septets];
        dest[0] = (byte) ((baser81 & 32640) >> UDH_SEPTET_COST_TWO_SHIFT_TABLES);
        int i = 0;
        int sz = s.length();
        int outByteIndex = UDH_SEPTET_COST_LENGTH;
        while (i < sz) {
            int outByteIndex2;
            char c = s.charAt(i);
            int v = charToGsm7bit(c);
            if (-1 == v) {
                outByteIndex2 = outByteIndex + UDH_SEPTET_COST_LENGTH;
                dest[outByteIndex] = (byte) ((c & 127) | HwSmsMessage.SMS_TOA_UNKNOWN);
            } else if (v == 27) {
                outByteIndex2 = outByteIndex + UDH_SEPTET_COST_LENGTH;
                dest[outByteIndex] = GSM_EXTENDED_ESCAPE;
                outByteIndex = outByteIndex2 + UDH_SEPTET_COST_LENGTH;
                dest[outByteIndex2] = (byte) GsmAlphabet.charToGsmExtended(c);
                outByteIndex2 = outByteIndex;
            } else {
                outByteIndex2 = outByteIndex + UDH_SEPTET_COST_LENGTH;
                dest[outByteIndex] = (byte) v;
            }
            i += UDH_SEPTET_COST_LENGTH;
            outByteIndex = outByteIndex2;
        }
        return dest;
    }

    public static byte[] stringToUCS82Packed(String s, char baser82Low, int septets) {
        byte[] dest = new byte[septets];
        dest[0] = (byte) ((baser82Low >> 8) & HwSubscriptionManager.SUB_INIT_STATE);
        dest[UDH_SEPTET_COST_LENGTH] = (byte) (baser82Low & HwSubscriptionManager.SUB_INIT_STATE);
        int i = 0;
        int sz = s.length();
        int outByteIndex = 2;
        while (i < sz) {
            int outByteIndex2;
            char c = s.charAt(i);
            int v = charToGsm7bit(c);
            if (-1 == v) {
                outByteIndex2 = outByteIndex + UDH_SEPTET_COST_LENGTH;
                dest[outByteIndex] = (byte) ((((c & HwSubscriptionManager.SUB_INIT_STATE) - (baser82Low & HwSubscriptionManager.SUB_INIT_STATE)) & 127) | HwSmsMessage.SMS_TOA_UNKNOWN);
            } else if (v == 27) {
                outByteIndex2 = outByteIndex + UDH_SEPTET_COST_LENGTH;
                dest[outByteIndex] = GSM_EXTENDED_ESCAPE;
                outByteIndex = outByteIndex2 + UDH_SEPTET_COST_LENGTH;
                dest[outByteIndex2] = (byte) GsmAlphabet.charToGsmExtended(c);
                outByteIndex2 = outByteIndex;
            } else {
                outByteIndex2 = outByteIndex + UDH_SEPTET_COST_LENGTH;
                dest[outByteIndex] = (byte) v;
            }
            i += UDH_SEPTET_COST_LENGTH;
            outByteIndex = outByteIndex2;
        }
        return dest;
    }

    public static int countGsmSeptets(char c) {
        try {
            return countGsmSeptets(c, false);
        } catch (EncodeException e) {
            return 0;
        }
    }

    public static int countGsmSeptets(char c, boolean throwsException) throws EncodeException {
        if (sCharsToGsmTables[0].get(c, -1) != -1) {
            return UDH_SEPTET_COST_LENGTH;
        }
        if (sCharsToShiftTables[0].get(c, -1) != -1) {
            return 2;
        }
        if (!throwsException) {
            return UDH_SEPTET_COST_LENGTH;
        }
        throw new EncodeException(c);
    }

    public static int countGsmSeptetsUsingTables(CharSequence s, boolean use7bitOnly, int languageTable, int languageShiftTable) {
        int count = 0;
        int sz = s.length();
        SparseIntArray charToLanguageTable = sCharsToGsmTables[languageTable];
        SparseIntArray charToShiftTable = sCharsToShiftTables[languageShiftTable];
        for (int i = 0; i < sz; i += UDH_SEPTET_COST_LENGTH) {
            char c = s.charAt(i);
            if (c == '\u001b') {
                Rlog.w(TAG, "countGsmSeptets() string contains Escape character, skipping.");
            } else if (charToLanguageTable.get(c, -1) != -1) {
                count += UDH_SEPTET_COST_LENGTH;
            } else if (charToShiftTable.get(c, -1) != -1) {
                count += 2;
            } else if (!use7bitOnly) {
                return -1;
            } else {
                count += UDH_SEPTET_COST_LENGTH;
            }
        }
        return count;
    }

    public static TextEncodingDetails countGsmSeptets(CharSequence s, boolean use7bitOnly) {
        if (!sDisableCountryEncodingCheck) {
            enableCountrySpecificEncodings();
        }
        TextEncodingDetails ted;
        int septets;
        if (sEnabledSingleShiftTables.length + sEnabledLockingShiftTables.length == 0) {
            ted = new TextEncodingDetails();
            septets = GsmAlphabet.countGsmSeptetsUsingTables(s, use7bitOnly, 0, 0);
            if (septets == -1) {
                return null;
            }
            ted.codeUnitSize = UDH_SEPTET_COST_LENGTH;
            ted.codeUnitCount = septets;
            if (septets > 160) {
                ted.msgCount = (septets + 152) / 153;
                ted.codeUnitsRemaining = (ted.msgCount * 153) - septets;
            } else {
                ted.msgCount = UDH_SEPTET_COST_LENGTH;
                ted.codeUnitsRemaining = 160 - septets;
            }
            ted.codeUnitSize = UDH_SEPTET_COST_LENGTH;
            return ted;
        }
        int i;
        int maxSingleShiftCode = sHighestEnabledSingleShiftCode;
        List<LanguagePairCount> lpcList = new ArrayList(sEnabledLockingShiftTables.length + UDH_SEPTET_COST_LENGTH);
        lpcList.add(new LanguagePairCount(0));
        int[] iArr = sEnabledLockingShiftTables;
        int length = iArr.length;
        for (i = 0; i < length; i += UDH_SEPTET_COST_LENGTH) {
            int i2 = iArr[i];
            if (!(i2 == 0 || sLanguageTables[i2].isEmpty())) {
                lpcList.add(new LanguagePairCount(i2));
            }
        }
        int sz = s.length();
        for (i2 = 0; i2 < sz && !lpcList.isEmpty(); i2 += UDH_SEPTET_COST_LENGTH) {
            char c = s.charAt(i2);
            if (c == '\u001b') {
                Rlog.w(TAG, "countGsmSeptets() string contains Escape character, ignoring!");
            } else {
                for (LanguagePairCount lpc : lpcList) {
                    int table;
                    int[] iArr2;
                    if (sCharsToGsmTables[lpc.languageCode].get(c, -1) == -1) {
                        for (table = 0; table <= maxSingleShiftCode; table += UDH_SEPTET_COST_LENGTH) {
                            if (lpc.septetCounts[table] != -1) {
                                if (sCharsToShiftTables[table].get(c, -1) != -1) {
                                    iArr2 = lpc.septetCounts;
                                    iArr2[table] = iArr2[table] + 2;
                                } else if (use7bitOnly) {
                                    iArr2 = lpc.septetCounts;
                                    iArr2[table] = iArr2[table] + UDH_SEPTET_COST_LENGTH;
                                    iArr2 = lpc.unencodableCounts;
                                    iArr2[table] = iArr2[table] + UDH_SEPTET_COST_LENGTH;
                                } else {
                                    lpc.septetCounts[table] = -1;
                                }
                            }
                        }
                    } else {
                        for (table = 0; table <= maxSingleShiftCode; table += UDH_SEPTET_COST_LENGTH) {
                            if (lpc.septetCounts[table] != -1) {
                                iArr2 = lpc.septetCounts;
                                iArr2[table] = iArr2[table] + UDH_SEPTET_COST_LENGTH;
                            }
                        }
                    }
                }
            }
        }
        ted = new TextEncodingDetails();
        ted.msgCount = Integer.MAX_VALUE;
        ted.codeUnitSize = UDH_SEPTET_COST_LENGTH;
        int minUnencodableCount = Integer.MAX_VALUE;
        for (LanguagePairCount lpc2 : lpcList) {
            int shiftTable = 0;
            while (shiftTable <= maxSingleShiftCode) {
                septets = lpc2.septetCounts[shiftTable];
                if (septets != -1) {
                    int udhLength;
                    int msgCount;
                    int septetsRemaining;
                    if (lpc2.languageCode != 0 && shiftTable != 0) {
                        udhLength = 8;
                    } else if (lpc2.languageCode == 0 && shiftTable == 0) {
                        udhLength = 0;
                    } else {
                        udhLength = 5;
                    }
                    if (septets + udhLength > 160) {
                        if (udhLength == 0) {
                            udhLength = UDH_SEPTET_COST_LENGTH;
                        }
                        int septetsPerMessage = 160 - (udhLength + UDH_SEPTET_COST_CONCATENATED_MESSAGE);
                        msgCount = ((septets + septetsPerMessage) - 1) / septetsPerMessage;
                        septetsRemaining = (msgCount * septetsPerMessage) - septets;
                    } else {
                        msgCount = UDH_SEPTET_COST_LENGTH;
                        septetsRemaining = (160 - udhLength) - septets;
                    }
                    int unencodableCount = lpc2.unencodableCounts[shiftTable];
                    if (!use7bitOnly || unencodableCount <= minUnencodableCount) {
                        if (!use7bitOnly || unencodableCount >= minUnencodableCount) {
                            i = ted.msgCount;
                            if (msgCount >= r0) {
                                i = ted.msgCount;
                                if (msgCount == r0) {
                                    i = ted.codeUnitsRemaining;
                                    if (septetsRemaining > r0) {
                                    }
                                }
                            }
                        }
                        minUnencodableCount = unencodableCount;
                        ted.msgCount = msgCount;
                        ted.codeUnitCount = septets;
                        ted.codeUnitsRemaining = septetsRemaining;
                        ted.languageTable = lpc2.languageCode;
                        ted.languageShiftTable = shiftTable;
                    }
                }
                shiftTable += UDH_SEPTET_COST_LENGTH;
            }
        }
        i = ted.msgCount;
        if (r0 == Integer.MAX_VALUE) {
            return null;
        }
        return ted;
    }

    public static int findGsmSeptetLimitIndex(String s, int start, int limit, int langTable, int langShiftTable) {
        int accumulator = 0;
        int size = s.length();
        SparseIntArray charToLangTable = sCharsToGsmTables[langTable];
        SparseIntArray charToLangShiftTable = sCharsToShiftTables[langShiftTable];
        for (int i = start; i < size; i += UDH_SEPTET_COST_LENGTH) {
            if (charToLangTable.get(s.charAt(i), -1) != -1) {
                accumulator += UDH_SEPTET_COST_LENGTH;
            } else if (charToLangShiftTable.get(s.charAt(i), -1) == -1) {
                accumulator += UDH_SEPTET_COST_LENGTH;
            } else {
                accumulator += 2;
            }
            if (accumulator > limit) {
                return i;
            }
        }
        return size;
    }

    static synchronized void setEnabledSingleShiftTables(int[] tables) {
        synchronized (HwGsmAlphabet.class) {
            sEnabledSingleShiftTables = tables;
            sDisableCountryEncodingCheck = true;
            if (tables.length > 0) {
                sHighestEnabledSingleShiftCode = tables[tables.length - 1];
            } else {
                sHighestEnabledSingleShiftCode = 0;
            }
        }
    }

    static synchronized void setEnabledLockingShiftTables(int[] tables) {
        synchronized (HwGsmAlphabet.class) {
            sEnabledLockingShiftTables = tables;
            sDisableCountryEncodingCheck = true;
        }
    }

    static synchronized int[] getEnabledSingleShiftTables() {
        int[] iArr;
        synchronized (HwGsmAlphabet.class) {
            iArr = sEnabledSingleShiftTables;
        }
        return iArr;
    }

    static synchronized int[] getEnabledLockingShiftTables() {
        int[] iArr;
        synchronized (HwGsmAlphabet.class) {
            iArr = sEnabledLockingShiftTables;
        }
        return iArr;
    }

    private static void enableCountrySpecificEncodings() {
        Resources r = Resources.getSystem();
        sEnabledSingleShiftTables = getSingleShiftTable(r);
        sEnabledLockingShiftTables = r.getIntArray(17236017);
        if (sEnabledSingleShiftTables.length > 0) {
            sHighestEnabledSingleShiftCode = sEnabledSingleShiftTables[sEnabledSingleShiftTables.length - 1];
        } else {
            sHighestEnabledSingleShiftCode = 0;
        }
    }

    private static int[] getSingleShiftTable(Resources r) {
        int[] temp = new int[UDH_SEPTET_COST_LENGTH];
        if (SystemProperties.getInt("ro.config.smsCoding_National", 0) != 0) {
            temp[0] = SystemProperties.getInt("ro.config.smsCoding_National", 0);
        } else if (SystemProperties.getInt("gsm.sms.coding.national", 0) == 0) {
            return r.getIntArray(17236016);
        } else {
            temp[0] = SystemProperties.getInt("gsm.sms.coding.national", 0);
        }
        return temp;
    }

    public static char util_UnicodeToGsm7DefaultExtended(char c) {
        switch (c) {
            case HwVSimEventReport.VSIM_PROCESS_TYPE_DB /*12*/:
                return '\n';
            case '[':
                return '<';
            case '\\':
                return '/';
            case ']':
                return '>';
            case '^':
                return '\u0014';
            case '{':
                return '(';
            case '|':
                return '@';
            case '}':
                return ')';
            case '~':
                return '=';
            case '\u20ac':
                return 'e';
            default:
                return c;
        }
    }

    public static char ussd_7bit_ucs2_to_gsm_char_default(char c) {
        switch (c) {
            case '\u00c0':
            case '\u00c1':
            case '\u00c2':
            case '\u00c3':
            case '\u00c4':
                return 'A';
            case '\u00c8':
            case '\u00c9':
            case '\u00ca':
            case '\u00cb':
                return 'E';
            case '\u00cc':
            case '\u00cd':
            case '\u00ce':
            case '\u00cf':
                return 'I';
            case '\u00d0':
                return 'D';
            case '\u00d1':
                return '\u00e4';
            case '\u00d2':
            case '\u00d3':
            case '\u00d4':
            case '\u00d5':
                return 'O';
            case '\u00d9':
            case '\u00da':
            case '\u00db':
            case '\u00dc':
                return 'U';
            case '\u00dd':
            case '\u00de':
                return 'Y';
            case '\u00e0':
            case '\u00e1':
            case '\u00e2':
            case '\u00e3':
                return 'a';
            case '\u00e7':
                return 'c';
            case '\u00e8':
            case '\u00e9':
            case '\u00ea':
            case '\u00eb':
            case '\u00f0':
                return 'e';
            case '\u00ed':
            case '\u00ee':
            case '\u00ef':
                return 'i';
            case '\u00f2':
            case '\u00f3':
            case '\u00f4':
            case '\u00f5':
                return 'o';
            case '\u00fa':
            case '\u00fb':
            case '\u00fc':
                return 'u';
            case '\u00fd':
            case '\u00fe':
                return 'y';
            default:
                return c;
        }
    }
}
