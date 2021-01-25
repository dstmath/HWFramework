package com.huawei.systemmanager.mms;

public class HwMmsInfo {
    private long expiry;
    private String from;
    private long messageSize;
    private int messageType;
    private String subject;

    public HwMmsInfo(String from2, String subject2, long messageSize2, long expiry2, int messageType2) {
        this.from = from2;
        this.subject = subject2;
        this.messageSize = messageSize2;
        this.expiry = expiry2;
        this.messageType = messageType2;
    }

    public String getFrom() {
        return this.from;
    }

    public String getSubject() {
        return this.subject;
    }

    public long getMessageSize() {
        return this.messageSize;
    }

    public long getExpiry() {
        return this.expiry;
    }

    public boolean isNotificationMsg() {
        return this.messageType == 130;
    }
}
