package com.android.internal.telephony.gsm;

import android.telephony.PhoneNumberUtils;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.SmsAddress;
import java.text.ParseException;

public class GsmSmsAddress extends SmsAddress {
    static final int OFFSET_ADDRESS_LENGTH = 0;
    static final int OFFSET_ADDRESS_VALUE = 2;
    static final int OFFSET_TOA = 1;

    public GsmSmsAddress(byte[] data, int offset, int length) throws ParseException {
        this.origBytes = new byte[length];
        System.arraycopy(data, offset, this.origBytes, 0, length);
        int addressLength = this.origBytes[0] & 255;
        int toa = this.origBytes[1] & 255;
        this.ton = (toa >> 4) & 7;
        if ((toa & 128) != 128) {
            throw new ParseException("Invalid TOA - high bit must be set. toa = " + toa, offset + 1);
        } else if (isAlphanumeric()) {
            this.address = GsmAlphabet.gsm7BitPackedToString(this.origBytes, 2, (addressLength * 4) / 7);
        } else {
            byte lastByte = this.origBytes[length - 1];
            if ((addressLength & 1) == 1) {
                byte[] bArr = this.origBytes;
                int i = length - 1;
                bArr[i] = (byte) (bArr[i] | 240);
            }
            this.address = PhoneNumberUtils.calledPartyBCDToString(this.origBytes, 1, length - 1);
            this.origBytes[length - 1] = lastByte;
        }
    }

    public String getAddressString() {
        return this.address;
    }

    public boolean isAlphanumeric() {
        return this.ton == 5;
    }

    public boolean isNetworkSpecific() {
        return this.ton == 3;
    }

    public boolean isCphsVoiceMessageIndicatorAddress() {
        if ((this.origBytes[0] & 255) == 4 && isAlphanumeric()) {
            return (this.origBytes[1] & 15) == 0;
        } else {
            return false;
        }
    }

    public boolean isCphsVoiceMessageSet() {
        if (isCphsVoiceMessageIndicatorAddress() && (this.origBytes[2] & 255) == 17) {
            return true;
        }
        return false;
    }

    public boolean isCphsVoiceMessageClear() {
        if (isCphsVoiceMessageIndicatorAddress() && (this.origBytes[2] & 255) == 16) {
            return true;
        }
        return false;
    }
}
