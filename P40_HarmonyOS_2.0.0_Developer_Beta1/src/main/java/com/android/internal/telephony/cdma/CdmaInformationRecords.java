package com.android.internal.telephony.cdma;

import android.os.Parcel;

public final class CdmaInformationRecords {
    public static final int RIL_CDMA_CALLED_PARTY_NUMBER_INFO_REC = 1;
    public static final int RIL_CDMA_CALLING_PARTY_NUMBER_INFO_REC = 2;
    public static final int RIL_CDMA_CONNECTED_NUMBER_INFO_REC = 3;
    public static final int RIL_CDMA_DISPLAY_INFO_REC = 0;
    public static final int RIL_CDMA_EXTENDED_DISPLAY_INFO_REC = 7;
    public static final int RIL_CDMA_LINE_CONTROL_INFO_REC = 6;
    public static final int RIL_CDMA_REDIRECTING_NUMBER_INFO_REC = 5;
    public static final int RIL_CDMA_SIGNAL_INFO_REC = 4;
    public static final int RIL_CDMA_T53_AUDIO_CONTROL_INFO_REC = 10;
    public static final int RIL_CDMA_T53_CLIR_INFO_REC = 8;
    public static final int RIL_CDMA_T53_RELEASE_INFO_REC = 9;
    public Object record;

    public CdmaInformationRecords(CdmaDisplayInfoRec obj) {
        this.record = obj;
    }

    public CdmaInformationRecords(CdmaNumberInfoRec obj) {
        this.record = obj;
    }

    public CdmaInformationRecords(CdmaSignalInfoRec obj) {
        this.record = obj;
    }

    public CdmaInformationRecords(CdmaRedirectingNumberInfoRec obj) {
        this.record = obj;
    }

    public CdmaInformationRecords(CdmaLineControlInfoRec obj) {
        this.record = obj;
    }

    public CdmaInformationRecords(CdmaT53ClirInfoRec obj) {
        this.record = obj;
    }

    public CdmaInformationRecords(CdmaT53AudioControlInfoRec obj) {
        this.record = obj;
    }

    public CdmaInformationRecords(Parcel p) {
        int id = p.readInt();
        switch (id) {
            case 0:
            case 7:
                this.record = new CdmaDisplayInfoRec(id, p.readString());
                return;
            case 1:
            case 2:
            case 3:
                this.record = new CdmaNumberInfoRec(id, p.readString(), p.readInt(), p.readInt(), p.readInt(), p.readInt());
                return;
            case 4:
                this.record = new CdmaSignalInfoRec(p.readInt(), p.readInt(), p.readInt(), p.readInt());
                return;
            case 5:
                this.record = new CdmaRedirectingNumberInfoRec(p.readString(), p.readInt(), p.readInt(), p.readInt(), p.readInt(), p.readInt());
                return;
            case 6:
                this.record = new CdmaLineControlInfoRec(p.readInt(), p.readInt(), p.readInt(), p.readInt());
                return;
            case 8:
                this.record = new CdmaT53ClirInfoRec(p.readInt());
                return;
            case 9:
            default:
                throw new RuntimeException("RIL_UNSOL_CDMA_INFO_REC: unsupported record. Got " + idToString(id) + " ");
            case 10:
                this.record = new CdmaT53AudioControlInfoRec(p.readInt(), p.readInt());
                return;
        }
    }

    public static String idToString(int id) {
        switch (id) {
            case 0:
                return "RIL_CDMA_DISPLAY_INFO_REC";
            case 1:
                return "RIL_CDMA_CALLED_PARTY_NUMBER_INFO_REC";
            case 2:
                return "RIL_CDMA_CALLING_PARTY_NUMBER_INFO_REC";
            case 3:
                return "RIL_CDMA_CONNECTED_NUMBER_INFO_REC";
            case 4:
                return "RIL_CDMA_SIGNAL_INFO_REC";
            case 5:
                return "RIL_CDMA_REDIRECTING_NUMBER_INFO_REC";
            case 6:
                return "RIL_CDMA_LINE_CONTROL_INFO_REC";
            case 7:
                return "RIL_CDMA_EXTENDED_DISPLAY_INFO_REC";
            case 8:
                return "RIL_CDMA_T53_CLIR_INFO_REC";
            case 9:
                return "RIL_CDMA_T53_RELEASE_INFO_REC";
            case 10:
                return "RIL_CDMA_T53_AUDIO_CONTROL_INFO_REC";
            default:
                return "<unknown record>";
        }
    }

    public static class CdmaSignalInfoRec {
        public int alertPitch;
        public boolean isPresent;
        public int signal;
        public int signalType;

        public CdmaSignalInfoRec() {
        }

        public CdmaSignalInfoRec(int isPresent2, int signalType2, int alertPitch2, int signal2) {
            this.isPresent = isPresent2 != 0;
            this.signalType = signalType2;
            this.alertPitch = alertPitch2;
            this.signal = signal2;
        }

        public String toString() {
            return "CdmaSignalInfo: { isPresent: " + this.isPresent + ", signalType: " + this.signalType + ", alertPitch: " + this.alertPitch + ", signal: " + this.signal + " }";
        }
    }

    public static class CdmaDisplayInfoRec {
        public String alpha;
        public int id;

        public CdmaDisplayInfoRec(int id2, String alpha2) {
            this.id = id2;
            this.alpha = alpha2;
        }

        public String toString() {
            return "CdmaDisplayInfoRec: { id: " + CdmaInformationRecords.idToString(this.id) + ", alpha: " + this.alpha + " }";
        }
    }

    public static class CdmaNumberInfoRec {
        public int id;
        public String number;
        public byte numberPlan;
        public byte numberType;
        public byte pi;
        public byte si;

        public CdmaNumberInfoRec(int id2, String number2, int numberType2, int numberPlan2, int pi2, int si2) {
            this.number = number2;
            this.numberType = (byte) numberType2;
            this.numberPlan = (byte) numberPlan2;
            this.pi = (byte) pi2;
            this.si = (byte) si2;
        }

        public String toString() {
            return "CdmaNumberInfoRec: { id: " + CdmaInformationRecords.idToString(this.id) + ", number: <MASKED>, numberType: " + ((int) this.numberType) + ", numberPlan: " + ((int) this.numberPlan) + ", pi: " + ((int) this.pi) + ", si: " + ((int) this.si) + " }";
        }
    }

    public static class CdmaRedirectingNumberInfoRec {
        public static final int REASON_CALLED_DTE_OUT_OF_ORDER = 9;
        public static final int REASON_CALL_FORWARDING_BUSY = 1;
        public static final int REASON_CALL_FORWARDING_BY_THE_CALLED_DTE = 10;
        public static final int REASON_CALL_FORWARDING_NO_REPLY = 2;
        public static final int REASON_CALL_FORWARDING_UNCONDITIONAL = 15;
        public static final int REASON_UNKNOWN = 0;
        public CdmaNumberInfoRec numberInfoRec;
        public int redirectingReason;

        public CdmaRedirectingNumberInfoRec(String number, int numberType, int numberPlan, int pi, int si, int reason) {
            this.numberInfoRec = new CdmaNumberInfoRec(5, number, numberType, numberPlan, pi, si);
            this.redirectingReason = reason;
        }

        public String toString() {
            return "CdmaNumberInfoRec: { numberInfoRec: " + this.numberInfoRec + ", redirectingReason: " + this.redirectingReason + " }";
        }
    }

    public static class CdmaLineControlInfoRec {
        public byte lineCtrlPolarityIncluded;
        public byte lineCtrlPowerDenial;
        public byte lineCtrlReverse;
        public byte lineCtrlToggle;

        public CdmaLineControlInfoRec(int lineCtrlPolarityIncluded2, int lineCtrlToggle2, int lineCtrlReverse2, int lineCtrlPowerDenial2) {
            this.lineCtrlPolarityIncluded = (byte) lineCtrlPolarityIncluded2;
            this.lineCtrlToggle = (byte) lineCtrlToggle2;
            this.lineCtrlReverse = (byte) lineCtrlReverse2;
            this.lineCtrlPowerDenial = (byte) lineCtrlPowerDenial2;
        }

        public String toString() {
            return "CdmaLineControlInfoRec: { lineCtrlPolarityIncluded: " + ((int) this.lineCtrlPolarityIncluded) + " lineCtrlToggle: " + ((int) this.lineCtrlToggle) + " lineCtrlReverse: " + ((int) this.lineCtrlReverse) + " lineCtrlPowerDenial: " + ((int) this.lineCtrlPowerDenial) + " }";
        }
    }

    public static class CdmaT53ClirInfoRec {
        public byte cause;

        public CdmaT53ClirInfoRec(int cause2) {
            this.cause = (byte) cause2;
        }

        public String toString() {
            return "CdmaT53ClirInfoRec: { cause: " + ((int) this.cause) + " }";
        }
    }

    public static class CdmaT53AudioControlInfoRec {
        public byte downlink;
        public byte uplink;

        public CdmaT53AudioControlInfoRec(int uplink2, int downlink2) {
            this.uplink = (byte) uplink2;
            this.downlink = (byte) downlink2;
        }

        public String toString() {
            return "CdmaT53AudioControlInfoRec: { uplink: " + ((int) this.uplink) + " downlink: " + ((int) this.downlink) + " }";
        }
    }
}
