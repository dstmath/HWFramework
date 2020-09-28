package com.google.android.mms.pdu;

import com.google.android.mms.InvalidHeaderValueException;

public class ReadRecInd extends GenericPdu {
    public ReadRecInd(EncodedStringValue from, byte[] messageId, int mmsVersion, int readStatus, EncodedStringValue[] to) throws InvalidHeaderValueException {
        setMessageType(135);
        setFrom(from);
        setMessageId(messageId);
        setMmsVersion(mmsVersion);
        setTo(to);
        setReadStatus(readStatus);
    }

    ReadRecInd(PduHeaders headers) {
        super(headers);
    }

    public long getDate() {
        return this.mPduHeaders.getLongInteger(133);
    }

    public void setDate(long value) {
        this.mPduHeaders.setLongInteger(value, 133);
    }

    public byte[] getMessageId() {
        return this.mPduHeaders.getTextString(139);
    }

    public void setMessageId(byte[] value) {
        this.mPduHeaders.setTextString(value, 139);
    }

    public EncodedStringValue[] getTo() {
        return this.mPduHeaders.getEncodedStringValues(151);
    }

    public void setTo(EncodedStringValue[] value) {
        this.mPduHeaders.setEncodedStringValues(value, 151);
    }

    public int getReadStatus() {
        return this.mPduHeaders.getOctet(155);
    }

    public void setReadStatus(int value) throws InvalidHeaderValueException {
        this.mPduHeaders.setOctet(value, 155);
    }
}
