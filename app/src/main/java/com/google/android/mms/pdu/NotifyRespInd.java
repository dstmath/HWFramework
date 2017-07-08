package com.google.android.mms.pdu;

import com.google.android.mms.InvalidHeaderValueException;

public class NotifyRespInd extends GenericPdu {
    public NotifyRespInd(int mmsVersion, byte[] transactionId, int status) throws InvalidHeaderValueException {
        setMessageType(PduPart.P_TYPE);
        setMmsVersion(mmsVersion);
        setTransactionId(transactionId);
        setStatus(status);
    }

    NotifyRespInd(PduHeaders headers) {
        super(headers);
    }

    public int getReportAllowed() {
        return this.mPduHeaders.getOctet(PduPart.P_SEC);
    }

    public void setReportAllowed(int value) throws InvalidHeaderValueException {
        this.mPduHeaders.setOctet(value, PduPart.P_SEC);
    }

    public void setStatus(int value) throws InvalidHeaderValueException {
        this.mPduHeaders.setOctet(value, PduPart.P_READ_DATE);
    }

    public int getStatus() {
        return this.mPduHeaders.getOctet(PduPart.P_READ_DATE);
    }

    public byte[] getTransactionId() {
        return this.mPduHeaders.getTextString(PduPart.P_FILENAME);
    }

    public void setTransactionId(byte[] value) {
        this.mPduHeaders.setTextString(value, PduPart.P_FILENAME);
    }
}
