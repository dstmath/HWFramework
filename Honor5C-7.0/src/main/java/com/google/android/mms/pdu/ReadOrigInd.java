package com.google.android.mms.pdu;

import com.google.android.mms.InvalidHeaderValueException;

public class ReadOrigInd extends GenericPdu {
    public ReadOrigInd() throws InvalidHeaderValueException {
        setMessageType(PduPart.P_PADDING);
    }

    ReadOrigInd(PduHeaders headers) {
        super(headers);
    }

    public long getDate() {
        return this.mPduHeaders.getLongInteger(PduPart.P_DEP_NAME);
    }

    public void setDate(long value) {
        this.mPduHeaders.setLongInteger(value, PduPart.P_DEP_NAME);
    }

    public EncodedStringValue getFrom() {
        return this.mPduHeaders.getEncodedStringValue(PduPart.P_CT_MR_TYPE);
    }

    public void setFrom(EncodedStringValue value) {
        this.mPduHeaders.setEncodedStringValue(value, PduPart.P_CT_MR_TYPE);
    }

    public byte[] getMessageId() {
        return this.mPduHeaders.getTextString(PduPart.P_DEP_START_INFO);
    }

    public void setMessageId(byte[] value) {
        this.mPduHeaders.setTextString(value, PduPart.P_DEP_START_INFO);
    }

    public int getReadStatus() {
        return this.mPduHeaders.getOctet(PduPart.P_COMMENT);
    }

    public void setReadStatus(int value) throws InvalidHeaderValueException {
        this.mPduHeaders.setOctet(value, PduPart.P_COMMENT);
    }

    public EncodedStringValue[] getTo() {
        return this.mPduHeaders.getEncodedStringValues(PduPart.P_NAME);
    }

    public void setTo(EncodedStringValue[] value) {
        this.mPduHeaders.setEncodedStringValues(value, PduPart.P_NAME);
    }
}
