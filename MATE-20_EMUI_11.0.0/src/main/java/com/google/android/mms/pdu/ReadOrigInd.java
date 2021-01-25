package com.google.android.mms.pdu;

import com.google.android.mms.InvalidHeaderValueException;

public class ReadOrigInd extends GenericPdu {
    public ReadOrigInd() throws InvalidHeaderValueException {
        setMessageType(136);
    }

    ReadOrigInd(PduHeaders headers) {
        super(headers);
    }

    public long getDate() {
        return this.mPduHeaders.getLongInteger(133);
    }

    public void setDate(long value) {
        this.mPduHeaders.setLongInteger(value, 133);
    }

    @Override // com.google.android.mms.pdu.GenericPdu
    public EncodedStringValue getFrom() {
        return this.mPduHeaders.getEncodedStringValue(137);
    }

    @Override // com.google.android.mms.pdu.GenericPdu
    public void setFrom(EncodedStringValue value) {
        this.mPduHeaders.setEncodedStringValue(value, 137);
    }

    public byte[] getMessageId() {
        return this.mPduHeaders.getTextString(139);
    }

    public void setMessageId(byte[] value) {
        this.mPduHeaders.setTextString(value, 139);
    }

    public int getReadStatus() {
        return this.mPduHeaders.getOctet(155);
    }

    public void setReadStatus(int value) throws InvalidHeaderValueException {
        this.mPduHeaders.setOctet(value, 155);
    }

    public EncodedStringValue[] getTo() {
        return this.mPduHeaders.getEncodedStringValues(151);
    }

    public void setTo(EncodedStringValue[] value) {
        this.mPduHeaders.setEncodedStringValues(value, 151);
    }
}
