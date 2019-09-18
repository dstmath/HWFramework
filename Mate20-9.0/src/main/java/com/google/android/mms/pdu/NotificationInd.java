package com.google.android.mms.pdu;

import com.google.android.mms.InvalidHeaderValueException;

public class NotificationInd extends GenericPdu {
    public NotificationInd() throws InvalidHeaderValueException {
        setMessageType(130);
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
        return this.mPduHeaders.getTextString(131);
    }

    public void setContentLocation(byte[] value) {
        this.mPduHeaders.setTextString(value, 131);
    }

    public long getExpiry() {
        return this.mPduHeaders.getLongInteger(136);
    }

    public void setExpiry(long value) {
        this.mPduHeaders.setLongInteger(value, 136);
    }

    public EncodedStringValue getFrom() {
        return this.mPduHeaders.getEncodedStringValue(137);
    }

    public void setFrom(EncodedStringValue value) {
        this.mPduHeaders.setEncodedStringValue(value, 137);
    }

    public byte[] getMessageClass() {
        return this.mPduHeaders.getTextString(138);
    }

    public void setMessageClass(byte[] value) {
        this.mPduHeaders.setTextString(value, 138);
    }

    public long getMessageSize() {
        return this.mPduHeaders.getLongInteger(142);
    }

    public void setMessageSize(long value) {
        this.mPduHeaders.setLongInteger(value, 142);
    }

    public EncodedStringValue getSubject() {
        return this.mPduHeaders.getEncodedStringValue(150);
    }

    public void setSubject(EncodedStringValue value) {
        this.mPduHeaders.setEncodedStringValue(value, 150);
    }

    public byte[] getTransactionId() {
        return this.mPduHeaders.getTextString(152);
    }

    public void setTransactionId(byte[] value) {
        this.mPduHeaders.setTextString(value, 152);
    }

    public int getDeliveryReport() {
        return this.mPduHeaders.getOctet(134);
    }

    public void setDeliveryReport(int value) throws InvalidHeaderValueException {
        this.mPduHeaders.setOctet(value, 134);
    }
}
