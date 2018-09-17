package com.huawei.internal.telephony;

import android.telephony.SmsMessage;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.GsmAlphabet.TextEncodingDetails;

public class GsmAlphabetEx {
    public static final int UDH_SEPTET_COST_CONCATENATED_MESSAGE = 6;
    public static final int UDH_SEPTET_COST_LENGTH = 1;
    public static final int UDH_SEPTET_COST_ONE_SHIFT_TABLE = 4;
    public static final int UDH_SEPTET_COST_TWO_SHIFT_TABLES = 7;

    public static int findGsmSeptetLimitIndex(String s, int start, int limit, int langTable, int langShiftTable) {
        return GsmAlphabet.findGsmSeptetLimitIndex(s, start, limit, langTable, langShiftTable);
    }

    public static int[] calculateSmsLength(CharSequence msgBody, boolean use7bitOnly) {
        TextEncodingDetails ted;
        if (SmsMessage.useCdmaFormatForMoSms()) {
            ted = com.android.internal.telephony.cdma.SmsMessage.calculateLength(msgBody, use7bitOnly);
        } else {
            ted = com.android.internal.telephony.gsm.SmsMessage.calculateLength(msgBody, use7bitOnly);
        }
        return new int[]{ted.msgCount, ted.codeUnitCount, ted.codeUnitsRemaining, ted.codeUnitSize, ted.languageTable, ted.languageShiftTable};
    }
}
