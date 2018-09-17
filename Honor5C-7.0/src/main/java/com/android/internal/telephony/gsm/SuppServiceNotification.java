package com.android.internal.telephony.gsm;

import android.telephony.PhoneNumberUtils;
import java.util.Arrays;

public class SuppServiceNotification {
    public static final int MO_CODE_CALL_DEFLECTED = 8;
    public static final int MO_CODE_CALL_FORWARDED = 2;
    public static final int MO_CODE_CALL_IS_WAITING = 3;
    public static final int MO_CODE_CLIR_SUPPRESSION_REJECTED = 7;
    public static final int MO_CODE_CUG_CALL = 4;
    public static final int MO_CODE_INCOMING_CALLS_BARRED = 6;
    public static final int MO_CODE_OUTGOING_CALLS_BARRED = 5;
    public static final int MO_CODE_SOME_CF_ACTIVE = 1;
    public static final int MO_CODE_UNCONDITIONAL_CF_ACTIVE = 0;
    public static final int MT_CODE_ADDITIONAL_CALL_FORWARDED = 10;
    public static final int MT_CODE_CALL_CONNECTED_ECT = 8;
    public static final int MT_CODE_CALL_CONNECTING_ECT = 7;
    public static final int MT_CODE_CALL_ON_HOLD = 2;
    public static final int MT_CODE_CALL_RETRIEVED = 3;
    public static final int MT_CODE_CUG_CALL = 1;
    public static final int MT_CODE_DEFLECTED_CALL = 9;
    public static final int MT_CODE_FORWARDED_CALL = 0;
    public static final int MT_CODE_FORWARD_CHECK_RECEIVED = 6;
    public static final int MT_CODE_MULTI_PARTY_CALL = 4;
    public static final int MT_CODE_ON_HOLD_CALL_RELEASED = 5;
    public int code;
    public String[] history;
    public int index;
    public int notificationType;
    public String number;
    public int type;

    public String toString() {
        return super.toString() + " mobile" + (this.notificationType == 0 ? " originated " : " terminated ") + " code: " + this.code + " index: " + this.index + " history: " + PhoneNumberUtils.toLogSafePhoneNumber(Arrays.toString(this.history)) + " \"" + PhoneNumberUtils.toLogSafePhoneNumber(this.number) + "\" ";
    }
}
