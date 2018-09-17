package com.android.server.wifi;

import java.io.IOException;

public class IMSIParameter {
    private final String mImsi;
    private final boolean mPrefix;

    public IMSIParameter(String imsi, boolean prefix) {
        this.mImsi = imsi;
        this.mPrefix = prefix;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public IMSIParameter(String imsi) throws IOException {
        if (imsi == null || imsi.length() == 0) {
            throw new IOException("Bad IMSI: '" + imsi + "'");
        }
        char stopChar = '\u0000';
        int nonDigit = 0;
        while (nonDigit < imsi.length()) {
            stopChar = imsi.charAt(nonDigit);
            if (stopChar >= '0' && stopChar <= '9') {
                nonDigit++;
            }
        }
        if (nonDigit == imsi.length()) {
            this.mImsi = imsi;
            this.mPrefix = false;
        } else if (nonDigit == imsi.length() - 1 && r1 == '*') {
            this.mImsi = imsi.substring(0, nonDigit);
            this.mPrefix = true;
        } else {
            throw new IOException("Bad IMSI: '" + imsi + "'");
        }
    }

    public boolean matches(String fullIMSI) {
        if (!this.mPrefix) {
            return this.mImsi.equals(fullIMSI);
        }
        return this.mImsi.regionMatches(false, 0, fullIMSI, 0, this.mImsi.length());
    }

    public boolean matchesMccMnc(String mccMnc) {
        if (this.mPrefix) {
            return this.mImsi.regionMatches(false, 0, mccMnc, 0, this.mImsi.length());
        }
        return this.mImsi.regionMatches(false, 0, mccMnc, 0, mccMnc.length());
    }

    public boolean isPrefix() {
        return this.mPrefix;
    }

    public String getImsi() {
        return this.mImsi;
    }

    public int prefixLength() {
        return this.mImsi.length();
    }

    public boolean equals(Object thatObject) {
        boolean z = false;
        if (this == thatObject) {
            return true;
        }
        if (thatObject == null || getClass() != thatObject.getClass()) {
            return false;
        }
        IMSIParameter that = (IMSIParameter) thatObject;
        if (this.mPrefix == that.mPrefix) {
            z = this.mImsi.equals(that.mImsi);
        }
        return z;
    }

    public int hashCode() {
        int result;
        int i = 0;
        if (this.mImsi != null) {
            result = this.mImsi.hashCode();
        } else {
            result = 0;
        }
        int i2 = result * 31;
        if (this.mPrefix) {
            i = 1;
        }
        return i2 + i;
    }

    public String toString() {
        if (this.mPrefix) {
            return this.mImsi + '*';
        }
        return this.mImsi;
    }
}
