package com.google.android.mms.pdu;

import com.google.android.mms.InvalidHeaderValueException;

public class NotificationInd extends GenericPdu {
    public NotificationInd() throws InvalidHeaderValueException {
        setMessageType(PduPart.P_LEVEL);
    }

    NotificationInd(PduHeaders headers) {
        super(headers);
    }

    public int getContentClass() {
        return this.mPduHeaders.getOctet(PduHeaders.CONTENT_CLASS);
    }

    public void setContentClass(int value) throws InvalidHeaderValueException {
        this.mPduHeaders.setOctet(value, PduHeaders.CONTENT_CLASS);
    }

    public byte[] getContentLocation() {
        return this.mPduHeaders.getTextString(PduPart.P_TYPE);
    }

    public void setContentLocation(byte[] value) {
        this.mPduHeaders.setTextString(value, PduPart.P_TYPE);
    }

    public long getExpiry() {
        return this.mPduHeaders.getLongInteger(PduPart.P_PADDING);
    }

    public void setExpiry(long value) {
        this.mPduHeaders.setLongInteger(value, PduPart.P_PADDING);
    }

    public EncodedStringValue getFrom() {
        return this.mPduHeaders.getEncodedStringValue(PduPart.P_CT_MR_TYPE);
    }

    public void setFrom(EncodedStringValue value) {
        this.mPduHeaders.setEncodedStringValue(value, PduPart.P_CT_MR_TYPE);
    }

    public byte[] getMessageClass() {
        return this.mPduHeaders.getTextString(PduPart.P_DEP_START);
    }

    public void setMessageClass(byte[] value) {
        this.mPduHeaders.setTextString(value, PduPart.P_DEP_START);
    }

    public long getMessageSize() {
        return this.mPduHeaders.getLongInteger(PduPart.P_MAX_AGE);
    }

    public void setMessageSize(long value) {
        this.mPduHeaders.setLongInteger(value, PduPart.P_MAX_AGE);
    }

    public EncodedStringValue getSubject() {
        return this.mPduHeaders.getEncodedStringValue(PduPart.P_SIZE);
    }

    public void setSubject(EncodedStringValue value) {
        this.mPduHeaders.setEncodedStringValue(value, PduPart.P_SIZE);
    }

    public byte[] getTransactionId() {
        return this.mPduHeaders.getTextString(PduPart.P_FILENAME);
    }

    public void setTransactionId(byte[] value) {
        this.mPduHeaders.setTextString(value, PduPart.P_FILENAME);
    }

    public int getDeliveryReport() {
        return this.mPduHeaders.getOctet(PduPart.P_DEP_FILENAME);
    }

    public void setDeliveryReport(int value) throws InvalidHeaderValueException {
        this.mPduHeaders.setOctet(value, PduPart.P_DEP_FILENAME);
    }
}
