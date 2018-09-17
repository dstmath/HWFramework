package com.android.internal.telephony;

public class HwQualcommCsgNetworkInfo {
    public byte bIncludePcsDigit;
    public int iCSGId;
    public int iCSGListCat;
    public int iSignalStrength;
    public boolean isSelectedFail = false;
    public short mcc;
    public short mnc;
    public String sCSGName;

    public boolean isEmpty() {
        if (this.mcc == (short) 0 && this.mnc == (short) 0 && this.bIncludePcsDigit == (byte) 0 && this.iCSGListCat == 0 && this.iCSGId == 0) {
            return (this.sCSGName == null || this.sCSGName.isEmpty()) && this.iSignalStrength == 0;
        } else {
            return false;
        }
    }

    public String toString() {
        return "HwQualcommCsgNetworkInfo: mcc: " + this.mcc + ", mnc: " + this.mnc + ", bIncludePcsDigit: " + this.bIncludePcsDigit + ", iCSGListCat: " + this.iCSGListCat + ", iCSGId: " + this.iCSGId + ", sCSGName: " + this.sCSGName + ", iSignalStrength: " + this.iSignalStrength + " ,isSelectedFail:" + this.isSelectedFail;
    }
}
