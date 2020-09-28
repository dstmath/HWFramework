package com.huawei.internal.telephony.cdma.sms;

import com.android.internal.telephony.cdma.sms.SmsEnvelope;

public class SmsEnvelopeEx {
    public static final int TELESERVICE_WMT = 4098;
    private SmsEnvelope mSmsEnvelope = new SmsEnvelope();

    public void setMessageType(int messageType) {
        SmsEnvelope smsEnvelope = this.mSmsEnvelope;
        if (smsEnvelope != null) {
            smsEnvelope.messageType = messageType;
        }
    }

    public void setTeleService(int teleService) {
        SmsEnvelope smsEnvelope = this.mSmsEnvelope;
        if (smsEnvelope != null) {
            smsEnvelope.teleService = teleService;
        }
    }

    public void setServiceCategory(int serviceCategory) {
        SmsEnvelope smsEnvelope = this.mSmsEnvelope;
        if (smsEnvelope != null) {
            smsEnvelope.serviceCategory = serviceCategory;
        }
    }

    public void setBearerReply(int bearerReply) {
        SmsEnvelope smsEnvelope = this.mSmsEnvelope;
        if (smsEnvelope != null) {
            smsEnvelope.bearerReply = bearerReply;
        }
    }

    public void setBearerData(byte[] bearerData) {
        SmsEnvelope smsEnvelope = this.mSmsEnvelope;
        if (smsEnvelope != null) {
            smsEnvelope.bearerData = bearerData;
        }
    }

    public void setOrigAddress(CdmaSmsAddressEx origAddress) {
        SmsEnvelope smsEnvelope = this.mSmsEnvelope;
        if (smsEnvelope != null) {
            smsEnvelope.origAddress = origAddress.getCdmaSmsAddress();
        }
    }

    public void setReplySeqNo(byte replySeqNo) {
        SmsEnvelope smsEnvelope = this.mSmsEnvelope;
        if (smsEnvelope != null) {
            smsEnvelope.replySeqNo = replySeqNo;
        }
    }

    public void setErrorClass(byte errorClass) {
        SmsEnvelope smsEnvelope = this.mSmsEnvelope;
        if (smsEnvelope != null) {
            smsEnvelope.errorClass = errorClass;
        }
    }

    public void setCauseCode(byte causeCode) {
        SmsEnvelope smsEnvelope = this.mSmsEnvelope;
        if (smsEnvelope != null) {
            smsEnvelope.causeCode = causeCode;
        }
    }

    public void setSmsEnvelope(SmsEnvelope smsEnvelope) {
        this.mSmsEnvelope = smsEnvelope;
    }

    public byte[] getBearerData() {
        SmsEnvelope smsEnvelope = this.mSmsEnvelope;
        if (smsEnvelope != null) {
            return smsEnvelope.bearerData;
        }
        return null;
    }

    public byte getReplySeqNo() {
        SmsEnvelope smsEnvelope = this.mSmsEnvelope;
        if (smsEnvelope != null) {
            return smsEnvelope.replySeqNo;
        }
        return 0;
    }

    public byte getErrorClass() {
        SmsEnvelope smsEnvelope = this.mSmsEnvelope;
        if (smsEnvelope != null) {
            return smsEnvelope.errorClass;
        }
        return 0;
    }

    public SmsEnvelope getSmsEnvelope() {
        return this.mSmsEnvelope;
    }
}
