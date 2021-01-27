package com.google.android.mms.pdu;

import com.google.android.mms.InvalidHeaderValueException;

public class NotifyRespInd extends GenericPdu {
    public NotifyRespInd(int mmsVersion, byte[] transactionId, int status) throws InvalidHeaderValueException {
        setMessageType(131);
        setMmsVersion(mmsVersion);
        setTransactionId(transactionId);
        setStatus(status);
    }

    NotifyRespInd(PduHeaders headers) {
        super(headers);
    }

    public int getReportAllowed() {
        return this.mPduHeaders.getOctet(145);
    }

    public void setReportAllowed(int value) throws InvalidHeaderValueException {
        this.mPduHeaders.setOctet(value, 145);
    }

    public void setStatus(int value) throws InvalidHeaderValueException {
        this.mPduHeaders.setOctet(value, 149);
    }

    public int getStatus() {
        return this.mPduHeaders.getOctet(149);
    }

    public byte[] getTransactionId() {
        return this.mPduHeaders.getTextString(152);
    }

    public void setTransactionId(byte[] value) {
        this.mPduHeaders.setTextString(value, 152);
    }
}
