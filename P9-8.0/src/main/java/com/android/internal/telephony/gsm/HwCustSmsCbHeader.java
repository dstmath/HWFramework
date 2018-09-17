package com.android.internal.telephony.gsm;

public class HwCustSmsCbHeader {
    private final int OTHER_TYPE = -2;

    public boolean isShowCbsSettingForSBM() {
        return false;
    }

    public boolean isEtwsMessageForSBM(int rev_channle) {
        return false;
    }

    public int getEtwsTypeForSBM(int rev_channle) {
        return -2;
    }
}
