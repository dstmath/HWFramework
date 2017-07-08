package gov.nist.javax.sip.stack;

import gov.nist.core.Separators;
import gov.nist.javax.sip.LogRecord;

class MessageLog implements LogRecord {
    private String callId;
    private String destination;
    private String firstLine;
    private boolean isSender;
    private String message;
    private String source;
    private String tid;
    private long timeStamp;
    private long timeStampHeaderValue;

    public boolean equals(Object other) {
        boolean z = false;
        if (!(other instanceof MessageLog)) {
            return false;
        }
        MessageLog otherLog = (MessageLog) other;
        if (otherLog.message.equals(this.message) && otherLog.timeStamp == this.timeStamp) {
            z = true;
        }
        return z;
    }

    public MessageLog(String message, String source, String destination, String timeStamp, boolean isSender, String firstLine, String tid, String callId, long timeStampHeaderValue) {
        if (message == null || message.equals("")) {
            throw new IllegalArgumentException("null msg");
        }
        this.message = message;
        this.source = source;
        this.destination = destination;
        try {
            long ts = Long.parseLong(timeStamp);
            if (ts < 0) {
                throw new IllegalArgumentException("Bad time stamp ");
            }
            this.timeStamp = ts;
            this.isSender = isSender;
            this.firstLine = firstLine;
            this.tid = tid;
            this.callId = callId;
            this.timeStampHeaderValue = timeStampHeaderValue;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Bad number format " + timeStamp);
        }
    }

    public MessageLog(String message, String source, String destination, long timeStamp, boolean isSender, String firstLine, String tid, String callId, long timestampVal) {
        if (message == null || message.equals("")) {
            throw new IllegalArgumentException("null msg");
        }
        this.message = message;
        this.source = source;
        this.destination = destination;
        if (timeStamp < 0) {
            throw new IllegalArgumentException("negative ts");
        }
        this.timeStamp = timeStamp;
        this.isSender = isSender;
        this.firstLine = firstLine;
        this.tid = tid;
        this.callId = callId;
        this.timeStampHeaderValue = timestampVal;
    }

    public String toString() {
        String str;
        StringBuilder append = new StringBuilder().append("<message\nfrom=\"").append(this.source).append("\" \nto=\"").append(this.destination).append("\" \ntime=\"").append(this.timeStamp).append(Separators.DOUBLE_QUOTE);
        if (this.timeStampHeaderValue != 0) {
            str = "\ntimeStamp = \"" + this.timeStampHeaderValue + Separators.DOUBLE_QUOTE;
        } else {
            str = "";
        }
        return (((append.append(str).append("\nisSender=\"").append(this.isSender).append("\" \ntransactionId=\"").append(this.tid).append("\" \ncallId=\"").append(this.callId).append("\" \nfirstLine=\"").append(this.firstLine.trim()).append(Separators.DOUBLE_QUOTE).append(" \n>\n").toString() + "<![CDATA[") + this.message) + "]]>\n") + "</message>\n";
    }
}
