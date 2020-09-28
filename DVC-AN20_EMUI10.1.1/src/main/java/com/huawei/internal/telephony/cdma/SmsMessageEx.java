package com.huawei.internal.telephony.cdma;

import com.android.internal.telephony.cdma.SmsMessage;
import com.android.internal.telephony.cdma.sms.CdmaSmsAddress;
import com.huawei.internal.telephony.SmsMessageBaseEx;
import com.huawei.internal.telephony.cdma.sms.CdmaSmsAddressEx;
import com.huawei.internal.telephony.cdma.sms.SmsEnvelopeEx;

public class SmsMessageEx extends SmsMessageBaseEx {
    private SmsMessage mSmsMessage = new SmsMessage();

    public SmsMessageEx() {
        this.mSmsMessageBase = this.mSmsMessage;
    }

    public void setSmsMessage(SmsMessage smsMessage) {
        this.mSmsMessage = smsMessage;
        this.mSmsMessageBase = this.mSmsMessage;
    }

    public SmsMessage getSmsMessage() {
        return this.mSmsMessage;
    }

    public void setSmsEnvelope(SmsEnvelopeEx smsEnvelope) {
        SmsMessage smsMessage = this.mSmsMessage;
        if (smsMessage != null && smsEnvelope != null) {
            smsMessage.setEnvelopeHw(smsEnvelope.getSmsEnvelope());
        }
    }

    public void setStatus(int status) {
        SmsMessage smsMessage = this.mSmsMessage;
        if (smsMessage != null) {
            smsMessage.setStatusHw(status);
        }
    }

    public void parseSms() {
        SmsMessage smsMessage = this.mSmsMessage;
        if (smsMessage != null) {
            smsMessage.parseSms();
        }
    }

    public int getStatus() {
        SmsMessage smsMessage = this.mSmsMessage;
        if (smsMessage != null) {
            return smsMessage.getStatus();
        }
        return 0;
    }

    public static int getNextMessageId() {
        return SmsMessage.getNextMessageId();
    }

    public static CdmaSmsAddressEx parseAddrForSMSMO(String destAddrStr) {
        CdmaSmsAddressEx cdmaSmsAddressEx = new CdmaSmsAddressEx();
        CdmaSmsAddress destAddr = SmsMessage.parseAddrForSMSMO(destAddrStr);
        if (destAddr != null) {
            cdmaSmsAddressEx.setCdmaSmsAddressEx(destAddr);
        }
        return cdmaSmsAddressEx;
    }

    public static class SubmitPduEx extends SmsMessageBaseEx.SubmitPduBaseEx {
        private SmsMessage.SubmitPdu mSubmitPdu = new SmsMessage.SubmitPdu();

        public SubmitPduEx() {
            setSubmitPduBase(this.mSubmitPdu);
        }

        @Override // com.huawei.internal.telephony.SmsMessageBaseEx.SubmitPduBaseEx
        public byte[] getEncodedMessage() {
            return this.mSubmitPdu.encodedMessage;
        }

        @Override // com.huawei.internal.telephony.SmsMessageBaseEx.SubmitPduBaseEx
        public void setEncodedMessage(byte[] encodedMessage) {
            this.mSubmitPdu.encodedMessage = encodedMessage;
        }

        @Override // com.huawei.internal.telephony.SmsMessageBaseEx.SubmitPduBaseEx
        public void setEncodedScAddress(byte[] encodedScAddress) {
            this.mSubmitPdu.encodedScAddress = encodedScAddress;
        }
    }
}
