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
        return this.mPduHeaders.getEncodedStringValue(150);
    }

    public void setSubject(EncodedStringValue value) {
        this.mPduHeaders.setEncodedStringValue(value, 150);
    }

    public EncodedStringValue[] getTo() {
        return this.mPduHeaders.getEncodedStringValues(151);
    }

    public void addTo(EncodedStringValue value) {
        this.mPduHeaders.appendEncodedStringValue(value, 151);
    }

    public int getPriority() {
        return this.mPduHeaders.getOctet(143);
    }

    public void setPriority(int value) throws InvalidHeaderValueException {
        this.mPduHeaders.setOctet(value, 143);
    }

    public long getDate() {
        return this.mPduHeaders.getLongInteger(133);
    }

    public void setDate(long value) {
        this.mPduHeaders.setLongInteger(value, 133);
    }
}
