package com.google.android.mms.pdu;

import com.google.android.mms.InvalidHeaderValueException;

public class SendConf extends GenericPdu {
    public SendConf() throws InvalidHeaderValueException {
        setMessageType(129);
    }

    SendConf(PduHeaders headers) {
        super(headers);
    }

    public byte[] getMessageId() {
        return this.mPduHeaders.getTextString(139);
    }

    public void setMessageId(byte[] value) {
        this.mPduHeaders.setTextString(value, 139);
    }

    public int getResponseStatus() {
        return this.mPduHeaders.getOctet(146);
    }

    public void setResponseStatus(int value) throws InvalidHeaderValueException {
        this.mPduHeaders.setOctet(value, 146);
    }

    public byte[] getTransactionId() {
        return this.mPduHeaders.getTextString(152);
    }

    public void setTransactionId(byte[] value) {
        this.mPduHeaders.setTextString(value, 152);
    }
}
