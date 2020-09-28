package com.huawei.internal.telephony;

import com.android.internal.telephony.SmsMessageBase;

public abstract class SmsMessageBaseEx {
    protected SmsMessageBase mSmsMessageBase;

    public void setOriginatingAddress(SmsAddressEx smsAddressEx) {
        SmsMessageBase smsMessageBase = this.mSmsMessageBase;
        if (smsMessageBase != null && smsAddressEx != null) {
            smsMessageBase.setOriginatingAddress(smsAddressEx.getSmsAddress());
        }
    }

    public void setPdu(byte[] pdu) {
        SmsMessageBase smsMessageBase = this.mSmsMessageBase;
        if (smsMessageBase != null) {
            smsMessageBase.setPdu(pdu);
        }
    }

    public byte[] getUserData() {
        SmsMessageBase smsMessageBase = this.mSmsMessageBase;
        if (smsMessageBase != null) {
            return smsMessageBase.getUserData();
        }
        return null;
    }

    public String getMessageBody() {
        SmsMessageBase smsMessageBase = this.mSmsMessageBase;
        if (smsMessageBase != null) {
            return smsMessageBase.getMessageBody();
        }
        return null;
    }

    public static abstract class SubmitPduBaseEx {
        private SmsMessageBase.SubmitPduBase mSubmitPduBase;

        protected SubmitPduBaseEx() {
        }

        /* access modifiers changed from: protected */
        public void setSubmitPduBase(SmsMessageBase.SubmitPduBase submitPduBase) {
            this.mSubmitPduBase = submitPduBase;
        }

        public SmsMessageBase.SubmitPduBase getSubmitPduBase() {
            return this.mSubmitPduBase;
        }

        public void setEncodedScAddress(byte[] encodedScAddress) {
            SmsMessageBase.SubmitPduBase submitPduBase = this.mSubmitPduBase;
            if (submitPduBase != null) {
                submitPduBase.encodedScAddress = encodedScAddress;
            }
        }

        public void setEncodedMessage(byte[] encodedMessage) {
            SmsMessageBase.SubmitPduBase submitPduBase = this.mSubmitPduBase;
            if (submitPduBase != null) {
                submitPduBase.encodedMessage = encodedMessage;
            }
        }

        public byte[] getEncodedMessage() {
            SmsMessageBase.SubmitPduBase submitPduBase = this.mSubmitPduBase;
            if (submitPduBase == null) {
                return null;
            }
            return submitPduBase.encodedMessage;
        }
    }
}
