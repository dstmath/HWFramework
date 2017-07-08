package com.google.android.mms.pdu;

import com.google.android.mms.InvalidHeaderValueException;

public class RetrieveConf extends MultimediaMessagePdu {
    public RetrieveConf() throws InvalidHeaderValueException {
        setMessageType(PduHeaders.STATUS_UNRECOGNIZED);
    }

    RetrieveConf(PduHeaders headers) {
        super(headers);
    }

    RetrieveConf(PduHeaders headers, PduBody body) {
        super(headers, body);
    }

    public EncodedStringValue[] getCc() {
        return this.mPduHeaders.getEncodedStringValues(PduPart.P_LEVEL);
    }

    public void addCc(EncodedStringValue value) {
        this.mPduHeaders.appendEncodedStringValue(value, PduPart.P_LEVEL);
    }

    public byte[] getContentType() {
        return this.mPduHeaders.getTextString(PduHeaders.STATUS_UNRECOGNIZED);
    }

    public void setContentType(byte[] value) {
        this.mPduHeaders.setTextString(value, PduHeaders.STATUS_UNRECOGNIZED);
    }

    public int getDeliveryReport() {
        return this.mPduHeaders.getOctet(PduPart.P_DEP_FILENAME);
    }

    public void setDeliveryReport(int value) throws InvalidHeaderValueException {
        this.mPduHeaders.setOctet(value, PduPart.P_DEP_FILENAME);
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

    public byte[] getMessageId() {
        return this.mPduHeaders.getTextString(PduPart.P_DEP_START_INFO);
    }

    public void setMessageId(byte[] value) {
        this.mPduHeaders.setTextString(value, PduPart.P_DEP_START_INFO);
    }

    public int getReadReport() {
        return this.mPduHeaders.getOctet(PduPart.P_SECURE);
    }

    public void setReadReport(int value) throws InvalidHeaderValueException {
        this.mPduHeaders.setOctet(value, PduPart.P_SECURE);
    }

    public int getRetrieveStatus() {
        return this.mPduHeaders.getOctet(PduPart.P_START);
    }

    public void setRetrieveStatus(int value) throws InvalidHeaderValueException {
        this.mPduHeaders.setOctet(value, PduPart.P_START);
    }

    public EncodedStringValue getRetrieveText() {
        return this.mPduHeaders.getEncodedStringValue(PduPart.P_START_INFO);
    }

    public void setRetrieveText(EncodedStringValue value) {
        this.mPduHeaders.setEncodedStringValue(value, PduPart.P_START_INFO);
    }

    public byte[] getTransactionId() {
        return this.mPduHeaders.getTextString(PduPart.P_FILENAME);
    }

    public void setTransactionId(byte[] value) {
        this.mPduHeaders.setTextString(value, PduPart.P_FILENAME);
    }
}
