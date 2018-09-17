package com.google.android.mms.pdu;

import com.google.android.mms.InvalidHeaderValueException;

public class RetrieveConf extends MultimediaMessagePdu {
    public RetrieveConf() throws InvalidHeaderValueException {
        setMessageType(132);
    }

    RetrieveConf(PduHeaders headers) {
        super(headers);
    }

    RetrieveConf(PduHeaders headers, PduBody body) {
        super(headers, body);
    }

    public EncodedStringValue[] getCc() {
        return this.mPduHeaders.getEncodedStringValues(130);
    }

    public void addCc(EncodedStringValue value) {
        this.mPduHeaders.appendEncodedStringValue(value, 130);
    }

    public byte[] getContentType() {
        return this.mPduHeaders.getTextString(132);
    }

    public void setContentType(byte[] value) {
        this.mPduHeaders.setTextString(value, 132);
    }

    public int getDeliveryReport() {
        return this.mPduHeaders.getOctet(134);
    }

    public void setDeliveryReport(int value) throws InvalidHeaderValueException {
        this.mPduHeaders.setOctet(value, 134);
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

    public byte[] getMessageId() {
        return this.mPduHeaders.getTextString(139);
    }

    public void setMessageId(byte[] value) {
        this.mPduHeaders.setTextString(value, 139);
    }

    public int getReadReport() {
        return this.mPduHeaders.getOctet(144);
    }

    public void setReadReport(int value) throws InvalidHeaderValueException {
        this.mPduHeaders.setOctet(value, 144);
    }

    public int getRetrieveStatus() {
        return this.mPduHeaders.getOctet(153);
    }

    public void setRetrieveStatus(int value) throws InvalidHeaderValueException {
        this.mPduHeaders.setOctet(value, 153);
    }

    public EncodedStringValue getRetrieveText() {
        return this.mPduHeaders.getEncodedStringValue(154);
    }

    public void setRetrieveText(EncodedStringValue value) {
        this.mPduHeaders.setEncodedStringValue(value, 154);
    }

    public byte[] getTransactionId() {
        return this.mPduHeaders.getTextString(152);
    }

    public void setTransactionId(byte[] value) {
        this.mPduHeaders.setTextString(value, 152);
    }
}
