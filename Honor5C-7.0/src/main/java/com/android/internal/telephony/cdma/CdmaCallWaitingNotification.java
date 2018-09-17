package com.android.internal.telephony.cdma;

import android.telephony.Rlog;
import com.google.android.mms.pdu.PduPersister;

public class CdmaCallWaitingNotification {
    static final String LOG_TAG = "CdmaCallWaitingNotification";
    public int alertPitch;
    public int isPresent;
    public String name;
    public int namePresentation;
    public String number;
    public int numberPlan;
    public int numberPresentation;
    public int numberType;
    public int signal;
    public int signalType;

    public CdmaCallWaitingNotification() {
        this.number = null;
        this.numberPresentation = 0;
        this.name = null;
        this.namePresentation = 0;
        this.numberType = 0;
        this.numberPlan = 0;
        this.isPresent = 0;
        this.signalType = 0;
        this.alertPitch = 0;
        this.signal = 0;
    }

    public String toString() {
        return super.toString() + "Call Waiting Notification  " + " number: " + (this.number == null ? this.number : this.number.replaceAll("\\d{4}$", "****")) + " numberPresentation: " + this.numberPresentation + " name: " + this.name + " namePresentation: " + this.namePresentation + " numberType: " + this.numberType + " numberPlan: " + this.numberPlan + " isPresent: " + this.isPresent + " signalType: " + this.signalType + " alertPitch: " + this.alertPitch + " signal: " + this.signal;
    }

    public static int presentationFromCLIP(int cli) {
        switch (cli) {
            case PduPersister.LOAD_MODE_MMS_COMMON /*0*/:
                return 1;
            case PduPersister.PROC_STATUS_TRANSIENT_FAILURE /*1*/:
                return 2;
            case PduPersister.PROC_STATUS_PERMANENTLY_FAILURE /*2*/:
                return 3;
            default:
                Rlog.d(LOG_TAG, "Unexpected presentation " + cli);
                return 3;
        }
    }
}
