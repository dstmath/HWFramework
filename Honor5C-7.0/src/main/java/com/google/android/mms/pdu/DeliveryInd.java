package com.google.android.mms.pdu;

import com.google.android.mms.InvalidHeaderValueException;

public class DeliveryInd extends GenericPdu {
    public DeliveryInd() throws InvalidHeaderValueException {
        setMessageType(PduPart.P_DEP_FILENAME);
    }

    DeliveryInd(PduHeaders headers) {
        super(headers);
    }

    public long getDate() {
        return this.mPduHeaders.getLongInteger(PduPart.P_DEP_NAME);
    }

    public void setDate(long value) {
        this.mPduHeaders.setLongInteger(value, PduPart.P_DEP_NAME);
    }

    public byte[] getMessageId() {
        return this.mPduHeaders.getTextString(PduPart.P_DEP_START_INFO);
    }

    public void setMessageId(byte[] value) {
        this.mPduHeaders.setTextString(value, PduPart.P_DEP_START_INFO);
    }

    public int getStatus() {
        return this.mPduHeaders.getOctet(PduPart.P_READ_DATE);
    }

    public void setStatus(int value) throws InvalidHeaderValueException {
        this.mPduHeaders.setOctet(value, PduPart.P_READ_DATE);
    }

    public EncodedStringValue[] getTo() {
        return this.mPduHeaders.getEncodedStringValues(PduPart.P_NAME);
    }

    public void setTo(EncodedStringValue[] value) {
        this.mPduHeaders.setEncodedStringValues(value, PduPart.P_NAME);
    }
}
