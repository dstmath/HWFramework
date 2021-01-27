package com.huawei.internal.telephony.cdma.sms;

import com.android.internal.telephony.cdma.sms.SmsEnvelope;

public class SmsEnvelopeEx {
    public static final int TELESERVICEID_CT_AUTO_REG_NOTIFICATION = 65005;
    public static final int TELESERVICE_CT_MMS_NOTIFICATION = 65002;
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

    public void setOrigAddress(CdmaSmsAddressEx origAddress) {
        SmsEnvelope smsEnvelope = this.mSmsEnvelope;
        if (smsEnvelope != null) {
            smsEnvelope.origAddress = origAddress.getCdmaSmsAddress();
        }
    }

    public void setCauseCode(byte causeCode) {
        SmsEnvelope smsEnvelope = this.mSmsEnvelope;
        if (smsEnvelope != null) {
            smsEnvelope.causeCode = causeCode;
        }
    }

    public byte[] getBearerData() {
        SmsEnvelope smsEnvelope = this.mSmsEnvelope;
        if (smsEnvelope != null) {
            return smsEnvelope.bearerData;
        }
        return null;
    }

    public void setBearerData(byte[] bearerData) {
        SmsEnvelope smsEnvelope = this.mSmsEnvelope;
        if (smsEnvelope != null) {
            smsEnvelope.bearerData = bearerData;
        }
    }

    public byte getReplySeqNo() {
        SmsEnvelope smsEnvelope = this.mSmsEnvelope;
        if (smsEnvelope != null) {
            return smsEnvelope.replySeqNo;
        }
        return 0;
    }

    public void setReplySeqNo(byte replySeqNo) {
        SmsEnvelope smsEnvelope = this.mSmsEnvelope;
        if (smsEnvelope != null) {
            smsEnvelope.replySeqNo = replySeqNo;
        }
    }

    public byte getErrorClass() {
        SmsEnvelope smsEnvelope = this.mSmsEnvelope;
        if (smsEnvelope != null) {
            return smsEnvelope.errorClass;
        }
        return 0;
    }

    public void setErrorClass(byte errorClass) {
        SmsEnvelope smsEnvelope = this.mSmsEnvelope;
        if (smsEnvelope != null) {
            smsEnvelope.errorClass = errorClass;
        }
    }

    public SmsEnvelope getSmsEnvelope() {
        return this.mSmsEnvelope;
    }

    public void setSmsEnvelope(SmsEnvelope smsEnvelope) {
        this.mSmsEnvelope = smsEnvelope;
    }
}
