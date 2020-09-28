package gov.nist.javax.sip.stack;

import gov.nist.core.Separators;
import gov.nist.core.ServerLogger;
import gov.nist.core.StackLogger;
import gov.nist.javax.sip.LogRecord;
import gov.nist.javax.sip.header.CallID;
import gov.nist.javax.sip.message.SIPMessage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Properties;
import javax.sip.SipStack;
import javax.sip.header.TimeStampHeader;
import javax.sip.message.Request;

public class ServerLog implements ServerLogger {
    private String auxInfo;
    private Properties configurationProperties;
    private String description;
    private boolean logContent;
    private String logFileName;
    private PrintWriter printWriter;
    private SIPTransactionStack sipStack;
    private String stackIpAddress;
    protected StackLogger stackLogger;
    protected int traceLevel = 16;

    private void setProperties(Properties configurationProperties2) {
        int ll;
        this.configurationProperties = configurationProperties2;
        this.description = configurationProperties2.getProperty("javax.sip.STACK_NAME");
        this.stackIpAddress = configurationProperties2.getProperty("javax.sip.IP_ADDRESS");
        this.logFileName = configurationProperties2.getProperty("gov.nist.javax.sip.SERVER_LOG");
        String logLevel = configurationProperties2.getProperty("gov.nist.javax.sip.TRACE_LEVEL");
        String logContent2 = configurationProperties2.getProperty("gov.nist.javax.sip.LOG_MESSAGE_CONTENT");
        this.logContent = logContent2 != null && logContent2.equals("true");
        if (logLevel != null && !logLevel.equals("LOG4J")) {
            try {
                if (logLevel.equals("DEBUG")) {
                    ll = 32;
                } else if (logLevel.equals(Request.INFO)) {
                    ll = 16;
                } else if (logLevel.equals("ERROR")) {
                    ll = 4;
                } else {
                    if (!logLevel.equals("NONE")) {
                        if (!logLevel.equals("OFF")) {
                            ll = Integer.parseInt(logLevel);
                        }
                    }
                    ll = 0;
                }
                setTraceLevel(ll);
            } catch (NumberFormatException e) {
                PrintStream printStream = System.out;
                printStream.println("ServerLog: WARNING Bad integer " + logLevel);
                System.out.println("logging dislabled ");
                setTraceLevel(0);
            }
        }
        checkLogFile();
    }

    public void setStackIpAddress(String ipAddress) {
        this.stackIpAddress = ipAddress;
    }

    @Override // gov.nist.core.ServerLogger
    public synchronized void closeLogFile() {
        if (this.printWriter != null) {
            this.printWriter.close();
            this.printWriter = null;
        }
    }

    public void checkLogFile() {
        String str = this.logFileName;
        if (str != null && this.traceLevel >= 16) {
            try {
                File logFile = new File(str);
                if (!logFile.exists()) {
                    logFile.createNewFile();
                    this.printWriter = null;
                }
                if (this.printWriter == null) {
                    this.printWriter = new PrintWriter((Writer) new FileWriter(this.logFileName, !Boolean.valueOf(this.configurationProperties.getProperty("gov.nist.javax.sip.SERVER_LOG_OVERWRITE")).booleanValue()), true);
                    PrintWriter printWriter2 = this.printWriter;
                    printWriter2.println("<!-- Use the  Trace Viewer in src/tools/tracesviewer to view this  trace  \nHere are the stack configuration properties \njavax.sip.IP_ADDRESS= " + this.configurationProperties.getProperty("javax.sip.IP_ADDRESS") + "\njavax.sip.STACK_NAME= " + this.configurationProperties.getProperty("javax.sip.STACK_NAME") + "\njavax.sip.ROUTER_PATH= " + this.configurationProperties.getProperty("javax.sip.ROUTER_PATH") + "\njavax.sip.OUTBOUND_PROXY= " + this.configurationProperties.getProperty("javax.sip.OUTBOUND_PROXY") + "\n-->");
                    PrintWriter printWriter3 = this.printWriter;
                    StringBuilder sb = new StringBuilder();
                    sb.append("<description\n logDescription=\"");
                    sb.append(this.description);
                    sb.append("\"\n name=\"");
                    sb.append(this.configurationProperties.getProperty("javax.sip.STACK_NAME"));
                    sb.append("\"\n auxInfo=\"");
                    sb.append(this.auxInfo);
                    sb.append("\"/>\n ");
                    printWriter3.println(sb.toString());
                    if (this.auxInfo != null) {
                        if (this.sipStack.isLoggingEnabled()) {
                            StackLogger stackLogger2 = this.stackLogger;
                            stackLogger2.logDebug("Here are the stack configuration properties \njavax.sip.IP_ADDRESS= " + this.configurationProperties.getProperty("javax.sip.IP_ADDRESS") + "\njavax.sip.ROUTER_PATH= " + this.configurationProperties.getProperty("javax.sip.ROUTER_PATH") + "\njavax.sip.OUTBOUND_PROXY= " + this.configurationProperties.getProperty("javax.sip.OUTBOUND_PROXY") + "\ngov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS= " + this.configurationProperties.getProperty("gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS") + "\ngov.nist.javax.sip.CACHE_SERVER_CONNECTIONS= " + this.configurationProperties.getProperty("gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS") + "\ngov.nist.javax.sip.REENTRANT_LISTENER= " + this.configurationProperties.getProperty("gov.nist.javax.sip.REENTRANT_LISTENER") + "gov.nist.javax.sip.THREAD_POOL_SIZE= " + this.configurationProperties.getProperty("gov.nist.javax.sip.THREAD_POOL_SIZE") + Separators.RETURN);
                            this.stackLogger.logDebug(" ]]> ");
                            this.stackLogger.logDebug("</debug>");
                            StackLogger stackLogger3 = this.stackLogger;
                            StringBuilder sb2 = new StringBuilder();
                            sb2.append("<description\n logDescription=\"");
                            sb2.append(this.description);
                            sb2.append("\"\n name=\"");
                            sb2.append(this.stackIpAddress);
                            sb2.append("\"\n auxInfo=\"");
                            sb2.append(this.auxInfo);
                            sb2.append("\"/>\n ");
                            stackLogger3.logDebug(sb2.toString());
                            this.stackLogger.logDebug("<debug>");
                            this.stackLogger.logDebug("<![CDATA[ ");
                        }
                    } else if (this.sipStack.isLoggingEnabled()) {
                        StackLogger stackLogger4 = this.stackLogger;
                        stackLogger4.logDebug("Here are the stack configuration properties \n" + this.configurationProperties + Separators.RETURN);
                        this.stackLogger.logDebug(" ]]>");
                        this.stackLogger.logDebug("</debug>");
                        StackLogger stackLogger5 = this.stackLogger;
                        stackLogger5.logDebug("<description\n logDescription=\"" + this.description + "\"\n name=\"" + this.stackIpAddress + "\" />\n");
                        this.stackLogger.logDebug("<debug>");
                        this.stackLogger.logDebug("<![CDATA[ ");
                    }
                }
            } catch (IOException e) {
            }
        }
    }

    public boolean needsLogging() {
        return this.logFileName != null;
    }

    public void setLogFileName(String name) {
        this.logFileName = name;
    }

    public String getLogFileName() {
        return this.logFileName;
    }

    private void logMessage(String message) {
        checkLogFile();
        PrintWriter printWriter2 = this.printWriter;
        if (printWriter2 != null) {
            printWriter2.println(message);
        }
        if (this.sipStack.isLoggingEnabled()) {
            this.stackLogger.logInfo(message);
        }
    }

    private void logMessage(String message, String from, String to, boolean sender, String callId, String firstLine, String status, String tid, long time, long timestampVal) {
        LogRecord log = this.sipStack.logRecordFactory.createLogRecord(message, from, to, time, sender, firstLine, tid, callId, timestampVal);
        if (log != null) {
            logMessage(log.toString());
        }
    }

    @Override // gov.nist.core.ServerLogger
    public void logMessage(SIPMessage message, String from, String to, boolean sender, long time) {
        checkLogFile();
        if (message.getFirstLine() != null) {
            CallID cid = (CallID) message.getCallId();
            String callId = null;
            if (cid != null) {
                callId = cid.getCallId();
            }
            String firstLine = message.getFirstLine().trim();
            String inputText = this.logContent ? message.encode() : message.encodeMessage();
            String tid = message.getTransactionId();
            TimeStampHeader tsHdr = (TimeStampHeader) message.getHeader("Timestamp");
            logMessage(inputText, from, to, sender, callId, firstLine, null, tid, time, tsHdr == null ? 0 : tsHdr.getTime());
        }
    }

    @Override // gov.nist.core.ServerLogger
    public void logMessage(SIPMessage message, String from, String to, String status, boolean sender, long time) {
        checkLogFile();
        CallID cid = (CallID) message.getCallId();
        String callId = null;
        if (cid != null) {
            callId = cid.getCallId();
        }
        String firstLine = message.getFirstLine().trim();
        String encoded = this.logContent ? message.encode() : message.encodeMessage();
        String tid = message.getTransactionId();
        TimeStampHeader tshdr = (TimeStampHeader) message.getHeader("Timestamp");
        logMessage(encoded, from, to, sender, callId, firstLine, status, tid, time, tshdr == null ? 0 : tshdr.getTime());
    }

    @Override // gov.nist.core.ServerLogger
    public void logMessage(SIPMessage message, String from, String to, String status, boolean sender) {
        logMessage(message, from, to, status, sender, System.currentTimeMillis());
    }

    @Override // gov.nist.core.ServerLogger
    public void logException(Exception ex) {
        if (this.traceLevel >= 4) {
            checkLogFile();
            ex.printStackTrace();
            PrintWriter printWriter2 = this.printWriter;
            if (printWriter2 != null) {
                ex.printStackTrace(printWriter2);
            }
        }
    }

    public void setTraceLevel(int level) {
        this.traceLevel = level;
    }

    public int getTraceLevel() {
        return this.traceLevel;
    }

    public void setAuxInfo(String auxInfo2) {
        this.auxInfo = auxInfo2;
    }

    @Override // gov.nist.core.ServerLogger
    public void setSipStack(SipStack sipStack2) {
        if (sipStack2 instanceof SIPTransactionStack) {
            this.sipStack = (SIPTransactionStack) sipStack2;
            this.stackLogger = this.sipStack.getStackLogger();
            return;
        }
        throw new IllegalArgumentException("sipStack must be a SIPTransactionStack");
    }

    @Override // gov.nist.core.ServerLogger
    public void setStackProperties(Properties stackProperties) {
        setProperties(stackProperties);
    }

    public void setLevel(int jsipLoggingLevel) {
    }
}
