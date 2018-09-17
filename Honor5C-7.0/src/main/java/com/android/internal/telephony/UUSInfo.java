package com.android.internal.telephony;

public class UUSInfo {
    public static final int UUS_DCS_IA5c = 4;
    public static final int UUS_DCS_OSIHLP = 1;
    public static final int UUS_DCS_RMCF = 3;
    public static final int UUS_DCS_USP = 0;
    public static final int UUS_DCS_X244 = 2;
    public static final int UUS_TYPE1_IMPLICIT = 0;
    public static final int UUS_TYPE1_NOT_REQUIRED = 2;
    public static final int UUS_TYPE1_REQUIRED = 1;
    public static final int UUS_TYPE2_NOT_REQUIRED = 4;
    public static final int UUS_TYPE2_REQUIRED = 3;
    public static final int UUS_TYPE3_NOT_REQUIRED = 6;
    public static final int UUS_TYPE3_REQUIRED = 5;
    private byte[] mUusData;
    private int mUusDcs;
    private int mUusType;

    public UUSInfo() {
        this.mUusType = UUS_TYPE1_IMPLICIT;
        this.mUusDcs = UUS_TYPE2_NOT_REQUIRED;
        this.mUusData = null;
    }

    public UUSInfo(int uusType, int uusDcs, byte[] uusData) {
        this.mUusType = uusType;
        this.mUusDcs = uusDcs;
        this.mUusData = uusData;
    }

    public int getDcs() {
        return this.mUusDcs;
    }

    public void setDcs(int uusDcs) {
        this.mUusDcs = uusDcs;
    }

    public int getType() {
        return this.mUusType;
    }

    public void setType(int uusType) {
        this.mUusType = uusType;
    }

    public byte[] getUserData() {
        return this.mUusData;
    }

    public void setUserData(byte[] uusData) {
        this.mUusData = uusData;
    }
}
