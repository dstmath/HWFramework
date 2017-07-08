package com.google.android.mms.pdu;

import android.util.Log;
import com.google.android.mms.ContentType;
import com.google.android.mms.InvalidHeaderValueException;

public class SendReq extends MultimediaMessagePdu {
    private static final String TAG = "SendReq";

    public SendReq() {
        try {
            setMessageType(PduPart.P_Q);
            setMmsVersion(18);
            setContentType(ContentType.MULTIPART_RELATED.getBytes());
            setFrom(new EncodedStringValue(PduHeaders.FROM_INSERT_ADDRESS_TOKEN_STR.getBytes()));
            setTransactionId(generateTransactionId());
        } catch (InvalidHeaderValueException e) {
            Log.e(TAG, "Unexpected InvalidHeaderValueException.", e);
            throw new RuntimeException(e);
        }
    }

    private byte[] generateTransactionId() {
        return ("T" + Long.toHexString(System.currentTimeMillis())).getBytes();
    }

    public SendReq(byte[] contentType, EncodedStringValue from, int mmsVersion, byte[] transactionId) throws InvalidHeaderValueException {
        setMessageType(PduPart.P_Q);
        setContentType(contentType);
        setFrom(from);
        setMmsVersion(mmsVersion);
        setTransactionId(transactionId);
    }

    SendReq(PduHeaders headers) {
        super(headers);
    }

    SendReq(PduHeaders headers, PduBody body) {
        super(headers, body);
    }

    public EncodedStringValue[] getBcc() {
        return this.mPduHeaders.getEncodedStringValues(PduPart.P_DISPOSITION_ATTACHMENT);
    }

    public void addBcc(EncodedStringValue value) {
        this.mPduHeaders.appendEncodedStringValue(value, PduPart.P_DISPOSITION_ATTACHMENT);
    }

    public void setBcc(EncodedStringValue[] value) {
        this.mPduHeaders.setEncodedStringValues(value, PduPart.P_DISPOSITION_ATTACHMENT);
    }

    public EncodedStringValue[] getCc() {
        return this.mPduHeaders.getEncodedStringValues(PduPart.P_LEVEL);
    }

    public void addCc(EncodedStringValue value) {
        this.mPduHeaders.appendEncodedStringValue(value, PduPart.P_LEVEL);
    }

    public void setCc(EncodedStringValue[] value) {
        this.mPduHeaders.setEncodedStringValues(value, PduPart.P_LEVEL);
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

    public long getExpiry() {
        return this.mPduHeaders.getLongInteger(PduPart.P_PADDING);
    }

    public void setExpiry(long value) {
        this.mPduHeaders.setLongInteger(value, PduPart.P_PADDING);
    }

    public long getMessageSize() {
        return this.mPduHeaders.getLongInteger(PduPart.P_MAX_AGE);
    }

    public void setMessageSize(long value) {
        this.mPduHeaders.setLongInteger(value, PduPart.P_MAX_AGE);
    }

    public byte[] getMessageClass() {
        return this.mPduHeaders.getTextString(PduPart.P_DEP_START);
    }

    public void setMessageClass(byte[] value) {
        this.mPduHeaders.setTextString(value, PduPart.P_DEP_START);
    }

    public int getReadReport() {
        return this.mPduHeaders.getOctet(PduPart.P_SECURE);
    }

    public void setReadReport(int value) throws InvalidHeaderValueException {
        this.mPduHeaders.setOctet(value, PduPart.P_SECURE);
    }

    public void setTo(EncodedStringValue[] value) {
        this.mPduHeaders.setEncodedStringValues(value, PduPart.P_NAME);
    }

    public byte[] getTransactionId() {
        return this.mPduHeaders.getTextString(PduPart.P_FILENAME);
    }

    public void setTransactionId(byte[] value) {
        this.mPduHeaders.setTextString(value, PduPart.P_FILENAME);
    }
}
