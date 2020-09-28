package com.huawei.internal.telephony.cdma.sms;

import com.android.internal.telephony.cdma.sms.CdmaSmsAddress;
import com.huawei.internal.telephony.SmsAddressEx;

public class CdmaSmsAddressEx extends SmsAddressEx {
    public static final int DIGIT_MODE_4BIT_DTMF = 0;
    public static final int DIGIT_MODE_8BIT_CHAR = 1;
    public static final int NUMBERING_PLAN_ISDN_TELEPHONY = 1;
    public static final int NUMBERING_PLAN_UNKNOWN = 0;
    public static final int NUMBER_MODE_DATA_NETWORK = 1;
    public static final int NUMBER_MODE_NOT_DATA_NETWORK = 0;
    public static final int TON_INTERNATIONAL_OR_IP = 1;
    public static final int TON_NATIONAL_OR_EMAIL = 2;
    public static final int TON_UNKNOWN = 0;
    private CdmaSmsAddress mCdmaSmsAddress = new CdmaSmsAddress();

    public CdmaSmsAddressEx() {
        this.mSmsAddress = this.mCdmaSmsAddress;
    }

    public void setCdmaSmsAddressEx(CdmaSmsAddress cdmaSmsAddressEx) {
        this.mCdmaSmsAddress = cdmaSmsAddressEx;
        this.mSmsAddress = this.mCdmaSmsAddress;
    }

    public CdmaSmsAddress getCdmaSmsAddress() {
        return this.mCdmaSmsAddress;
    }

    public void setDigitMode(int digitMode) {
        CdmaSmsAddress cdmaSmsAddress = this.mCdmaSmsAddress;
        if (cdmaSmsAddress != null) {
            cdmaSmsAddress.digitMode = digitMode;
        }
    }

    public void setTon(int ton) {
        CdmaSmsAddress cdmaSmsAddress = this.mCdmaSmsAddress;
        if (cdmaSmsAddress != null) {
            cdmaSmsAddress.ton = ton;
        }
    }

    public void setNumberMode(int numberMode) {
        CdmaSmsAddress cdmaSmsAddress = this.mCdmaSmsAddress;
        if (cdmaSmsAddress != null) {
            cdmaSmsAddress.numberMode = numberMode;
        }
    }

    public void setNumberPlan(int numberPlan) {
        CdmaSmsAddress cdmaSmsAddress = this.mCdmaSmsAddress;
        if (cdmaSmsAddress != null) {
            cdmaSmsAddress.numberPlan = numberPlan;
        }
    }

    public void setOrigBytes(byte[] origBytes) {
        CdmaSmsAddress cdmaSmsAddress = this.mCdmaSmsAddress;
        if (cdmaSmsAddress != null) {
            cdmaSmsAddress.origBytes = origBytes;
        }
    }

    public void setOrigBytes(byte value, int byteIndex) {
        CdmaSmsAddress cdmaSmsAddress = this.mCdmaSmsAddress;
        if (cdmaSmsAddress != null && cdmaSmsAddress.origBytes != null) {
            this.mCdmaSmsAddress.origBytes[byteIndex] = value;
        }
    }

    public void setNumberOfDigits(int numberOfDigits) {
        CdmaSmsAddress cdmaSmsAddress = this.mCdmaSmsAddress;
        if (cdmaSmsAddress != null) {
            cdmaSmsAddress.numberOfDigits = numberOfDigits;
        }
    }

    public int getDigitMode() {
        CdmaSmsAddress cdmaSmsAddress = this.mCdmaSmsAddress;
        if (cdmaSmsAddress != null) {
            return cdmaSmsAddress.digitMode;
        }
        return 0;
    }

    public int getTon() {
        CdmaSmsAddress cdmaSmsAddress = this.mCdmaSmsAddress;
        if (cdmaSmsAddress != null) {
            return cdmaSmsAddress.ton;
        }
        return 0;
    }

    public byte[] getOrigBytes() {
        CdmaSmsAddress cdmaSmsAddress = this.mCdmaSmsAddress;
        if (cdmaSmsAddress != null) {
            return cdmaSmsAddress.origBytes;
        }
        return null;
    }

    public int getNumberMode() {
        CdmaSmsAddress cdmaSmsAddress = this.mCdmaSmsAddress;
        if (cdmaSmsAddress != null) {
            return cdmaSmsAddress.numberMode;
        }
        return 0;
    }

    public int getNumberOfDigits() {
        CdmaSmsAddress cdmaSmsAddress = this.mCdmaSmsAddress;
        if (cdmaSmsAddress != null) {
            return cdmaSmsAddress.numberOfDigits;
        }
        return 0;
    }

    public static String filterNumericSugarHw(String address) {
        return CdmaSmsAddress.filterNumericSugarHw(address);
    }

    public static byte[] parseToDtmf(String address) {
        return CdmaSmsAddress.parseToDtmf(address);
    }

    public static String filterWhitespaceHw(String address) {
        return CdmaSmsAddress.filterWhitespaceHw(address);
    }

    public static CdmaSmsAddressEx parse(String address) {
        CdmaSmsAddressEx cdmaSmsAddressEx = new CdmaSmsAddressEx();
        cdmaSmsAddressEx.setCdmaSmsAddressEx(CdmaSmsAddress.parse(address));
        return cdmaSmsAddressEx;
    }

    public int getNumberPlan() {
        return this.mCdmaSmsAddress.numberPlan;
    }
}
