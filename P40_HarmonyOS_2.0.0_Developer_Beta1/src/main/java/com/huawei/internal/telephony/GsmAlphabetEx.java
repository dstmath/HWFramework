package com.huawei.internal.telephony;

import android.telephony.SmsMessage;
import com.android.internal.telephony.EncodeException;
import com.android.internal.telephony.GsmAlphabet;
import com.huawei.annotation.HwSystemApi;

public class GsmAlphabetEx {
    public static final int UDH_SEPTET_COST_CONCATENATED_MESSAGE = 6;
    public static final int UDH_SEPTET_COST_LENGTH = 1;
    public static final int UDH_SEPTET_COST_ONE_SHIFT_TABLE = 4;
    public static final int UDH_SEPTET_COST_TWO_SHIFT_TABLES = 7;

    public static int findGsmSeptetLimitIndex(String s, int start, int limit, int langTable, int langShiftTable) {
        return GsmAlphabet.findGsmSeptetLimitIndex(s, start, limit, langTable, langShiftTable);
    }

    public static int[] calculateSmsLength(CharSequence msgBody, boolean use7bitOnly) {
        GsmAlphabet.TextEncodingDetails ted;
        if (SmsMessage.useCdmaFormatForMoSmsEx()) {
            ted = com.android.internal.telephony.cdma.SmsMessage.calculateLength(msgBody, use7bitOnly);
        } else {
            ted = com.android.internal.telephony.gsm.SmsMessage.calculateLength(msgBody, use7bitOnly);
        }
        return new int[]{ted.msgCount, ted.codeUnitCount, ted.codeUnitsRemaining, ted.codeUnitSize, ted.languageTable, ted.languageShiftTable};
    }

    @HwSystemApi
    public static byte[] stringToGsm7BitPackedWithHeader(String message, byte[] udh) throws EncodeExceptionEx {
        try {
            return GsmAlphabet.stringToGsm7BitPackedWithHeader(message, udh);
        } catch (EncodeException ex) {
            throw new EncodeExceptionEx(ex.getMessage(), ex.getError());
        }
    }

    @HwSystemApi
    public static byte[] stringToGsm8BitPacked(String s) {
        return GsmAlphabet.stringToGsm8BitPacked(s);
    }

    @HwSystemApi
    public static String gsm8BitUnpackedToString(byte[] data, int offset, int length) {
        return GsmAlphabet.gsm8BitUnpackedToString(data, offset, length);
    }

    @HwSystemApi
    public static int countGsmSeptetsUsingTables(CharSequence s, boolean use7bitOnly, int languageTable, int languageShiftTable) {
        return GsmAlphabet.countGsmSeptetsUsingTables(s, use7bitOnly, languageTable, languageShiftTable);
    }

    @HwSystemApi
    public static int charToGsmExtended(char c) {
        return GsmAlphabet.charToGsmExtended(c);
    }

    @HwSystemApi
    public static synchronized void setEnabledSingleShiftTables(int[] tables) {
        synchronized (GsmAlphabetEx.class) {
            GsmAlphabet.setEnabledSingleShiftTables(tables);
        }
    }
}
