package com.google.android.mms.pdu;

import com.google.android.mms.InvalidHeaderValueException;

public class MultimediaMessagePdu extends GenericPdu {
    private PduBody mMessageBody;

    public MultimediaMessagePdu(PduHeaders header, PduBody body) {
        super(header);
        this.mMessageBody = body;
    }

    MultimediaMessagePdu(PduHeaders headers) {
        super(headers);
    }

    public PduBody getBody() {
        return this.mMessageBody;
    }

    public void setBody(PduBody body) {
        this.mMessageBody = body;
    }

    public EncodedStringValue getSubject() {
        return this.mPduHeaders.getEncodedStringValue(PduPart.P_SIZE);
    }

    public void setSubject(EncodedStringValue value) {
        this.mPduHeaders.setEncodedStringValue(value, PduPart.P_SIZE);
    }

    public EncodedStringValue[] getTo() {
        return this.mPduHeaders.getEncodedStringValues(PduPart.P_NAME);
    }

    public void addTo(EncodedStringValue value) {
        this.mPduHeaders.appendEncodedStringValue(value, PduPart.P_NAME);
    }

    public int getPriority() {
        return this.mPduHeaders.getOctet(PduPart.P_DEP_PATH);
    }

    public void setPriority(int value) throws InvalidHeaderValueException {
        this.mPduHeaders.setOctet(value, PduPart.P_DEP_PATH);
    }

    public long getDate() {
        return this.mPduHeaders.getLongInteger(PduPart.P_DEP_NAME);
    }

    public void setDate(long value) {
        this.mPduHeaders.setLongInteger(value, PduPart.P_DEP_NAME);
    }
}
