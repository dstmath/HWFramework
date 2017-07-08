package com.google.android.mms.pdu;

import com.google.android.mms.InvalidHeaderValueException;

public class GenericPdu {
    PduHeaders mPduHeaders;

    public GenericPdu() {
        this.mPduHeaders = null;
        this.mPduHeaders = new PduHeaders();
    }

    GenericPdu(PduHeaders headers) {
        this.mPduHeaders = null;
        this.mPduHeaders = headers;
    }

    PduHeaders getPduHeaders() {
        return this.mPduHeaders;
    }

    public int getMessageType() {
        return this.mPduHeaders.getOctet(PduPart.P_DEP_COMMENT);
    }

    public void setMessageType(int value) throws InvalidHeaderValueException {
        this.mPduHeaders.setOctet(value, PduPart.P_DEP_COMMENT);
    }

    public int getMmsVersion() {
        return this.mPduHeaders.getOctet(PduPart.P_DEP_DOMAIN);
    }

    public void setMmsVersion(int value) throws InvalidHeaderValueException {
        this.mPduHeaders.setOctet(value, PduPart.P_DEP_DOMAIN);
    }

    public EncodedStringValue getFrom() {
        return this.mPduHeaders.getEncodedStringValue(PduPart.P_CT_MR_TYPE);
    }

    public void setFrom(EncodedStringValue value) {
        this.mPduHeaders.setEncodedStringValue(value, PduPart.P_CT_MR_TYPE);
    }
}
