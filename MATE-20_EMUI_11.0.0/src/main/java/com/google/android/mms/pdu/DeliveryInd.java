package com.google.android.mms.pdu;

import com.google.android.mms.InvalidHeaderValueException;

public class DeliveryInd extends GenericPdu {
    public DeliveryInd() throws InvalidHeaderValueException {
        setMessageType(134);
    }

    DeliveryInd(PduHeaders headers) {
        super(headers);
    }

    public long getDate() {
        return this.mPduHeaders.getLongInteger(133);
    }

    public void setDate(long value) {
        this.mPduHeaders.setLongInteger(value, 133);
    }

    public byte[] getMessageId() {
        return this.mPduHeaders.getTextString(139);
    }

    public void setMessageId(byte[] value) {
        this.mPduHeaders.setTextString(value, 139);
    }

    public int getStatus() {
        return this.mPduHeaders.getOctet(149);
    }

    public void setStatus(int value) throws InvalidHeaderValueException {
        this.mPduHeaders.setOctet(value, 149);
    }

    public EncodedStringValue[] getTo() {
        return this.mPduHeaders.getEncodedStringValues(151);
    }

    public void setTo(EncodedStringValue[] value) {
        this.mPduHeaders.setEncodedStringValues(value, 151);
    }
}
