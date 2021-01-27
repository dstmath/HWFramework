package com.google.android.mms.pdu;

import android.util.Log;
import com.google.android.mms.ContentType;
import com.google.android.mms.InvalidHeaderValueException;

public class SendReq extends MultimediaMessagePdu {
    private static final String TAG = "SendReq";

    public SendReq() {
        try {
            setMessageType(128);
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
        setMessageType(128);
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
        return this.mPduHeaders.getEncodedStringValues(129);
    }

    public void addBcc(EncodedStringValue value) {
        this.mPduHeaders.appendEncodedStringValue(value, 129);
    }

    public void setBcc(EncodedStringValue[] value) {
        this.mPduHeaders.setEncodedStringValues(value, 129);
    }

    public EncodedStringValue[] getCc() {
        return this.mPduHeaders.getEncodedStringValues(130);
    }

    public void addCc(EncodedStringValue value) {
        this.mPduHeaders.appendEncodedStringValue(value, 130);
    }

    public void setCc(EncodedStringValue[] value) {
        this.mPduHeaders.setEncodedStringValues(value, 130);
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

    public long getExpiry() {
        return this.mPduHeaders.getLongInteger(136);
    }

    public void setExpiry(long value) {
        this.mPduHeaders.setLongInteger(value, 136);
    }

    public long getMessageSize() {
        return this.mPduHeaders.getLongInteger(142);
    }

    public void setMessageSize(long value) {
        this.mPduHeaders.setLongInteger(value, 142);
    }

    public byte[] getMessageClass() {
        return this.mPduHeaders.getTextString(138);
    }

    public void setMessageClass(byte[] value) {
        this.mPduHeaders.setTextString(value, 138);
    }

    public int getReadReport() {
        return this.mPduHeaders.getOctet(144);
    }

    public void setReadReport(int value) throws InvalidHeaderValueException {
        this.mPduHeaders.setOctet(value, 144);
    }

    public void setTo(EncodedStringValue[] value) {
        this.mPduHeaders.setEncodedStringValues(value, 151);
    }

    public byte[] getTransactionId() {
        return this.mPduHeaders.getTextString(152);
    }

    public void setTransactionId(byte[] value) {
        this.mPduHeaders.setTextString(value, 152);
    }
}
