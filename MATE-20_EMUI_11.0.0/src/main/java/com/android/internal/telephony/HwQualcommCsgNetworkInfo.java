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
        String str;
        return this.mcc == 0 && this.mnc == 0 && this.bIncludePcsDigit == 0 && this.iCSGListCat == 0 && this.iCSGId == 0 && ((str = this.sCSGName) == null || str.isEmpty()) && this.iSignalStrength == 0;
    }

    public String toString() {
        return "HwQualcommCsgNetworkInfo: mcc: " + ((int) this.mcc) + ", mnc: " + ((int) this.mnc) + ", bIncludePcsDigit: " + ((int) this.bIncludePcsDigit) + ", iCSGListCat: " + this.iCSGListCat + ", iCSGId: " + this.iCSGId + ", sCSGName: " + this.sCSGName + ", iSignalStrength: " + this.iSignalStrength + " ,isSelectedFail:" + this.isSelectedFail;
    }
}
