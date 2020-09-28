package com.mediatek.internal.telephony;

import android.telephony.PhoneNumberUtils;
import java.util.Arrays;

public class MtkCallForwardInfo {
    public String number;
    public int reason;
    public int serviceClass;
    public int status;
    public int timeSeconds;
    public long[] timeSlot;
    public int toa;

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append(this.status == 0 ? " not active " : " active ");
        sb.append(" reason: ");
        sb.append(this.reason);
        sb.append(" serviceClass: ");
        sb.append(this.serviceClass);
        sb.append(" \"");
        sb.append(PhoneNumberUtils.stringFromStringAndTOA(this.number, this.toa));
        sb.append("\" ");
        sb.append(this.timeSeconds);
        sb.append(" seconds timeSlot: ");
        sb.append(Arrays.toString(this.timeSlot));
        return sb.toString();
    }
}
