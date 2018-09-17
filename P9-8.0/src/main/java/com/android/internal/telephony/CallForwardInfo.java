package com.android.internal.telephony;

public class CallForwardInfo {
    public int endHour;
    public int endMinute;
    public String number;
    public int reason;
    public int serviceClass;
    public int startHour;
    public int startMinute;
    public int status;
    public int timeSeconds;
    public int toa;

    public String toString() {
        return super.toString() + (this.status == 0 ? " not active " : " active ") + " reason: " + this.reason + " serviceClass: " + this.serviceClass + this.timeSeconds + " seconds" + ", startHour=" + this.startHour + ", startMinute=" + this.startMinute + ", endHour=" + this.endHour + ", endMinute=" + this.endMinute;
    }
}
