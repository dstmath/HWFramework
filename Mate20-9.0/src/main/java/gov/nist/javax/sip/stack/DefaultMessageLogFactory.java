package gov.nist.javax.sip.stack;

import gov.nist.javax.sip.LogRecord;
import gov.nist.javax.sip.LogRecordFactory;

public class DefaultMessageLogFactory implements LogRecordFactory {
    public LogRecord createLogRecord(String message, String source, String destination, String timeStamp, boolean isSender, String firstLine, String tid, String callId, long tsHeaderValue) {
        MessageLog messageLog = new MessageLog(message, source, destination, timeStamp, isSender, firstLine, tid, callId, tsHeaderValue);
        return messageLog;
    }

    public LogRecord createLogRecord(String message, String source, String destination, long timeStamp, boolean isSender, String firstLine, String tid, String callId, long timestampVal) {
        MessageLog messageLog = new MessageLog(message, source, destination, timeStamp, isSender, firstLine, tid, callId, timestampVal);
        return messageLog;
    }
}
