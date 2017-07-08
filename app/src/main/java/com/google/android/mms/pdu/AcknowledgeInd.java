package com.google.android.mms.pdu;

import com.google.android.mms.InvalidHeaderValueException;

public class AcknowledgeInd extends GenericPdu {
    public AcknowledgeInd(int mmsVersion, byte[] transactionId) throws InvalidHeaderValueException {
        setMessageType(PduPart.P_DEP_NAME);
        setMmsVersion(mmsVersion);
        setTransactionId(transactionId);
    }

    AcknowledgeInd(PduHeaders headers) {
        super(headers);
    }

    public int getReportAllowed() {
        return this.mPduHeaders.getOctet(PduPart.P_SEC);
    }

    public void setReportAllowed(int value) throws InvalidHeaderValueException {
        this.mPduHeaders.setOctet(value, PduPart.P_SEC);
    }

    public byte[] getTransactionId() {
        return this.mPduHeaders.getTextString(PduPart.P_FILENAME);
    }

    public void setTransactionId(byte[] value) {
        this.mPduHeaders.setTextString(value, PduPart.P_FILENAME);
    }
}
