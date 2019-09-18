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

    public MessageLog(String message2, String source2, String destination2, String timeStamp2, boolean isSender2, String firstLine2, String tid2, String callId2, long timeStampHeaderValue2) {
        if (message2 == null || message2.equals("")) {
            throw new IllegalArgumentException("null msg");
        }
        this.message = message2;
        this.source = source2;
        this.destination = destination2;
        try {
            long ts = Long.parseLong(timeStamp2);
            if (ts >= 0) {
                this.timeStamp = ts;
                this.isSender = isSender2;
                this.firstLine = firstLine2;
                this.tid = tid2;
                this.callId = callId2;
                this.timeStampHeaderValue = timeStampHeaderValue2;
                return;
            }
            throw new IllegalArgumentException("Bad time stamp ");
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Bad number format " + timeStamp2);
        }
    }

    public MessageLog(String message2, String source2, String destination2, long timeStamp2, boolean isSender2, String firstLine2, String tid2, String callId2, long timestampVal) {
        if (message2 == null || message2.equals("")) {
            throw new IllegalArgumentException("null msg");
        }
        this.message = message2;
        this.source = source2;
        this.destination = destination2;
        if (timeStamp2 >= 0) {
            this.timeStamp = timeStamp2;
            this.isSender = isSender2;
            this.firstLine = firstLine2;
            this.tid = tid2;
            this.callId = callId2;
            this.timeStampHeaderValue = timestampVal;
            return;
        }
        throw new IllegalArgumentException("negative ts");
    }

    public String toString() {
        String str;
        StringBuilder sb = new StringBuilder();
        sb.append("<message\nfrom=\"");
        sb.append(this.source);
        sb.append("\" \nto=\"");
        sb.append(this.destination);
        sb.append("\" \ntime=\"");
        sb.append(this.timeStamp);
        sb.append(Separators.DOUBLE_QUOTE);
        if (this.timeStampHeaderValue != 0) {
            str = "\ntimeStamp = \"" + this.timeStampHeaderValue + Separators.DOUBLE_QUOTE;
        } else {
            str = "";
        }
        sb.append(str);
        sb.append("\nisSender=\"");
        sb.append(this.isSender);
        sb.append("\" \ntransactionId=\"");
        sb.append(this.tid);
        sb.append("\" \ncallId=\"");
        sb.append(this.callId);
        sb.append("\" \nfirstLine=\"");
        sb.append(this.firstLine.trim());
        sb.append("\" \n>\n");
        String log = sb.toString();
        String log2 = log + "<![CDATA[";
        String log3 = log2 + this.message;
        String log4 = log3 + "]]>\n";
        return log4 + "</message>\n";
    }
}
