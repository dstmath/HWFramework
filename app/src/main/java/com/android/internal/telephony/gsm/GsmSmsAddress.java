package com.android.internal.telephony.gsm;

import android.telephony.PhoneNumberUtils;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.SmsAddress;
import com.android.internal.telephony.imsphone.CallFailCause;
import com.google.android.mms.pdu.PduHeaders;
import com.google.android.mms.pdu.PduPart;
import java.text.ParseException;

public class GsmSmsAddress extends SmsAddress {
    static final int OFFSET_ADDRESS_LENGTH = 0;
    static final int OFFSET_ADDRESS_VALUE = 2;
    static final int OFFSET_TOA = 1;

    public GsmSmsAddress(byte[] data, int offset, int length) throws ParseException {
        this.origBytes = new byte[length];
        System.arraycopy(data, offset, this.origBytes, OFFSET_ADDRESS_LENGTH, length);
        int addressLength = this.origBytes[OFFSET_ADDRESS_LENGTH] & PduHeaders.STORE_STATUS_ERROR_END;
        int toa = this.origBytes[OFFSET_TOA] & PduHeaders.STORE_STATUS_ERROR_END;
        this.ton = (toa >> 4) & 7;
        if ((toa & PduPart.P_Q) != PduPart.P_Q) {
            throw new ParseException("Invalid TOA - high bit must be set. toa = " + toa, offset + OFFSET_TOA);
        } else if (isAlphanumeric()) {
            this.address = GsmAlphabet.gsm7BitPackedToString(this.origBytes, OFFSET_ADDRESS_VALUE, (addressLength * 4) / 7);
        } else {
            byte lastByte = this.origBytes[length - 1];
            if ((addressLength & OFFSET_TOA) == OFFSET_TOA) {
                byte[] bArr = this.origBytes;
                int i = length - 1;
                bArr[i] = (byte) (bArr[i] | CallFailCause.CALL_BARRED);
            }
            this.address = PhoneNumberUtils.calledPartyBCDToString(this.origBytes, OFFSET_TOA, length - 1);
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
        if ((this.origBytes[OFFSET_ADDRESS_LENGTH] & PduHeaders.STORE_STATUS_ERROR_END) == 4 && isAlphanumeric()) {
            return (this.origBytes[OFFSET_TOA] & 15) == 0;
        } else {
            return false;
        }
    }

    public boolean isCphsVoiceMessageSet() {
        if (isCphsVoiceMessageIndicatorAddress() && (this.origBytes[OFFSET_ADDRESS_VALUE] & PduHeaders.STORE_STATUS_ERROR_END) == 17) {
            return true;
        }
        return false;
    }

    public boolean isCphsVoiceMessageClear() {
        if (isCphsVoiceMessageIndicatorAddress() && (this.origBytes[OFFSET_ADDRESS_VALUE] & PduHeaders.STORE_STATUS_ERROR_END) == 16) {
            return true;
        }
        return false;
    }
}
