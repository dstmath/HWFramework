package com.google.android.mms.pdu;

import com.google.android.mms.InvalidHeaderValueException;

public class SendConf extends GenericPdu {
    public SendConf() throws InvalidHeaderValueException {
        setMessageType(PduPart.P_DISPOSITION_ATTACHMENT);
    }

    SendConf(PduHeaders headers) {
        super(headers);
    }

    public byte[] getMessageId() {
        return this.mPduHeaders.getTextString(PduPart.P_DEP_START_INFO);
    }

    public void setMessageId(byte[] value) {
        this.mPduHeaders.setTextString(value, PduPart.P_DEP_START_INFO);
    }

    public int getResponseStatus() {
        return this.mPduHeaders.getOctet(PduPart.P_MAC);
    }

    public void setResponseStatus(int value) throws InvalidHeaderValueException {
        this.mPduHeaders.setOctet(value, PduPart.P_MAC);
    }

    public byte[] getTransactionId() {
        return this.mPduHeaders.getTextString(PduPart.P_FILENAME);
    }

    public void setTransactionId(byte[] value) {
        this.mPduHeaders.setTextString(value, PduPart.P_FILENAME);
    }
}
