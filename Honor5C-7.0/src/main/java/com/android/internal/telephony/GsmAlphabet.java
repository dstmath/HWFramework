package com.android.internal.telephony;

import android.common.HwFrameworkFactory;
import android.content.res.Resources;
import android.telephony.Rlog;
import android.text.TextUtils;
import android.util.SparseIntArray;
import com.android.internal.R;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.os.HwBootFail;
import com.huawei.pgmng.log.LogPower;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class GsmAlphabet {
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
            int maxSingleShiftCode = GsmAlphabet.sHighestEnabledSingleShiftCode;
            this.septetCounts = new int[(maxSingleShiftCode + GsmAlphabet.UDH_SEPTET_COST_LENGTH)];
            this.unencodableCounts = new int[(maxSingleShiftCode + GsmAlphabet.UDH_SEPTET_COST_LENGTH)];
            int tableOffset = 0;
            for (int i = GsmAlphabet.UDH_SEPTET_COST_LENGTH; i <= maxSingleShiftCode; i += GsmAlphabet.UDH_SEPTET_COST_LENGTH) {
                if (GsmAlphabet.sEnabledSingleShiftTables[tableOffset] == i) {
                    tableOffset += GsmAlphabet.UDH_SEPTET_COST_LENGTH;
                } else {
                    this.septetCounts[i] = -1;
                }
            }
            if (code == GsmAlphabet.UDH_SEPTET_COST_LENGTH && maxSingleShiftCode >= GsmAlphabet.UDH_SEPTET_COST_LENGTH) {
                this.septetCounts[GsmAlphabet.UDH_SEPTET_COST_LENGTH] = -1;
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
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.GsmAlphabet.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.GsmAlphabet.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.GsmAlphabet.<clinit>():void");
    }

    private GsmAlphabet() {
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
        if (gsmChar < 0 || gsmChar >= LogPower.START_CHG_ROTATION) {
            return ' ';
        }
        return sLanguageTables[0].charAt(gsmChar);
    }

    public static char gsmExtendedToChar(int gsmChar) {
        if (gsmChar == 27 || gsmChar < 0 || gsmChar >= LogPower.START_CHG_ROTATION) {
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
        if (septetCount > MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE) {
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
                int gsmVal = (pdu[offset + byteOffset] >> shift) & LogPower.MIME_TYPE;
                if (shift > UDH_SEPTET_COST_LENGTH) {
                    gsmVal = (gsmVal & (LogPower.MIME_TYPE >> (shift - 1))) | ((pdu[(offset + byteOffset) + UDH_SEPTET_COST_LENGTH] << (8 - shift)) & LogPower.MIME_TYPE);
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
            int c = data[i] & MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE;
            if (c == MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE) {
                break;
            }
            int i2;
            if (c != 27) {
                if (prevWasEscape) {
                    char shiftChar = c < shiftTableToChar.length() ? shiftTableToChar.charAt(c) : ' ';
                    if (shiftChar != ' ') {
                        ret.append(shiftChar);
                        i2 = i;
                    } else if (c < languageTableToChar.length()) {
                        ret.append(languageTableToChar.charAt(c));
                        i2 = i;
                    } else {
                        ret.append(' ');
                        i2 = i;
                    }
                } else if (!isMbcs || c < LogPower.START_CHG_ROTATION || i + UDH_SEPTET_COST_LENGTH >= offset + length) {
                    if (c < languageTableToChar.length()) {
                        ret.append(languageTableToChar.charAt(c));
                    } else {
                        ret.append(' ');
                    }
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

    public static boolean isGsmSeptets(char c) {
        return (sCharsToGsmTables[0].get(c, -1) == -1 && sCharsToShiftTables[0].get(c, -1) == -1) ? false : true;
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
            septets = countGsmSeptetsUsingTables(s, use7bitOnly, 0, 0);
            if (septets == -1) {
                return null;
            }
            ted.codeUnitSize = UDH_SEPTET_COST_LENGTH;
            ted.codeUnitCount = septets;
            if (septets > 160) {
                ted.msgCount = (septets + LogPower.REMOVE_VIEW) / LogPower.GPU_DRAW;
                ted.codeUnitsRemaining = (ted.msgCount * LogPower.GPU_DRAW) - septets;
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
        ted.msgCount = HwBootFail.STAGE_BOOT_SUCCESS;
        ted.codeUnitSize = UDH_SEPTET_COST_LENGTH;
        int minUnencodableCount = HwBootFail.STAGE_BOOT_SUCCESS;
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

    public static synchronized void setEnabledSingleShiftTables(int[] tables) {
        synchronized (GsmAlphabet.class) {
            sEnabledSingleShiftTables = tables;
            sDisableCountryEncodingCheck = true;
            if (tables.length > 0) {
                sHighestEnabledSingleShiftCode = tables[tables.length - 1];
            } else {
                sHighestEnabledSingleShiftCode = 0;
            }
        }
    }

    public static synchronized void setEnabledLockingShiftTables(int[] tables) {
        synchronized (GsmAlphabet.class) {
            sEnabledLockingShiftTables = tables;
            sDisableCountryEncodingCheck = true;
        }
    }

    public static synchronized int[] getEnabledSingleShiftTables() {
        int[] iArr;
        synchronized (GsmAlphabet.class) {
            iArr = sEnabledSingleShiftTables;
        }
        return iArr;
    }

    public static synchronized int[] getEnabledLockingShiftTables() {
        int[] iArr;
        synchronized (GsmAlphabet.class) {
            iArr = sEnabledLockingShiftTables;
        }
        return iArr;
    }

    private static void enableCountrySpecificEncodings() {
        Resources r = Resources.getSystem();
        sEnabledSingleShiftTables = HwFrameworkFactory.getHwInnerTelephonyManager().getSingleShiftTable(r);
        sEnabledLockingShiftTables = r.getIntArray(R.array.config_sms_enabled_locking_shift_tables);
        if (sEnabledSingleShiftTables.length > 0) {
            sHighestEnabledSingleShiftCode = sEnabledSingleShiftTables[sEnabledSingleShiftTables.length - 1];
        } else {
            sHighestEnabledSingleShiftCode = 0;
        }
    }
}
