package com.android.server.wifi;

import android.text.TextUtils;

public class IMSIParameter {
    private static final int MAX_IMSI_LENGTH = 15;
    public static final int MCC_MNC_LENGTH = 6;
    private final String mImsi;
    private final boolean mPrefix;

    public IMSIParameter(String imsi, boolean prefix) {
        this.mImsi = imsi;
        this.mPrefix = prefix;
    }

    public static IMSIParameter build(String imsi) {
        if (TextUtils.isEmpty(imsi) || imsi.length() > 15) {
            return null;
        }
        char stopChar = 0;
        int nonDigit = 0;
        while (nonDigit < imsi.length() && (stopChar = imsi.charAt(nonDigit)) >= '0' && stopChar <= '9') {
            nonDigit++;
        }
        if (nonDigit == imsi.length()) {
            return new IMSIParameter(imsi, false);
        }
        if (nonDigit == imsi.length() - 1 && stopChar == '*') {
            return new IMSIParameter(imsi.substring(0, nonDigit), true);
        }
        return null;
    }

    public boolean matchesImsi(String fullIMSI) {
        if (fullIMSI == null) {
            return false;
        }
        if (!this.mPrefix) {
            return this.mImsi.equals(fullIMSI);
        }
        String str = this.mImsi;
        return str.regionMatches(false, 0, fullIMSI, 0, str.length());
    }

    public boolean matchesMccMnc(String mccMnc) {
        if (mccMnc == null || mccMnc.length() != 6) {
            return false;
        }
        int checkLength = 6;
        if (this.mPrefix && this.mImsi.length() < 6) {
            checkLength = this.mImsi.length();
        }
        return this.mImsi.regionMatches(false, 0, mccMnc, 0, checkLength);
    }

    public boolean equals(Object thatObject) {
        if (this == thatObject) {
            return true;
        }
        if (!(thatObject instanceof IMSIParameter)) {
            return false;
        }
        IMSIParameter that = (IMSIParameter) thatObject;
        if (this.mPrefix != that.mPrefix || !TextUtils.equals(this.mImsi, that.mImsi)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        String str = this.mImsi;
        return ((str != null ? str.hashCode() : 0) * 31) + (this.mPrefix ? 1 : 0);
    }

    public String toString() {
        if (!this.mPrefix) {
            return this.mImsi;
        }
        return this.mImsi + '*';
    }
}
