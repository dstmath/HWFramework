package com.android.internal.telephony.cdma;

import android.annotation.UnsupportedAppUsage;
import android.telephony.Rlog;

public class CdmaCallWaitingNotification {
    static final String LOG_TAG = "CdmaCallWaitingNotification";
    public int alertPitch = 0;
    public int isPresent = 0;
    public String name = null;
    public int namePresentation = 0;
    @UnsupportedAppUsage
    public String number = null;
    public int numberPlan = 0;
    public int numberPresentation = 0;
    public int numberType = 0;
    public int signal = 0;
    public int signalType = 0;

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append("Call Waiting Notification   number: ");
        String str = this.number;
        if (str != null) {
            str = str.replaceAll("\\d{4}$", "****");
        }
        sb.append(str);
        sb.append(" numberPresentation: ");
        sb.append(this.numberPresentation);
        sb.append(" name: ");
        sb.append(this.name);
        sb.append(" namePresentation: ");
        sb.append(this.namePresentation);
        sb.append(" numberType: ");
        sb.append(this.numberType);
        sb.append(" numberPlan: ");
        sb.append(this.numberPlan);
        sb.append(" isPresent: ");
        sb.append(this.isPresent);
        sb.append(" signalType: ");
        sb.append(this.signalType);
        sb.append(" alertPitch: ");
        sb.append(this.alertPitch);
        sb.append(" signal: ");
        sb.append(this.signal);
        return sb.toString();
    }

    public static int presentationFromCLIP(int cli) {
        if (cli == 0) {
            return 1;
        }
        if (cli == 1) {
            return 2;
        }
        if (cli == 2) {
            return 3;
        }
        Rlog.d(LOG_TAG, "Unexpected presentation " + cli);
        return 3;
    }
}
