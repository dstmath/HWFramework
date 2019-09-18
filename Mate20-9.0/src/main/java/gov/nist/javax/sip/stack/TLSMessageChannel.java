package gov.nist.javax.sip.stack;

import gov.nist.core.InternalErrorHandler;
import gov.nist.core.Separators;
import gov.nist.core.ServerLogger;
import gov.nist.core.StackLogger;
import gov.nist.javax.sip.address.ParameterNames;
import gov.nist.javax.sip.header.CSeq;
import gov.nist.javax.sip.header.CallID;
import gov.nist.javax.sip.header.From;
import gov.nist.javax.sip.header.RequestLine;
import gov.nist.javax.sip.header.RetryAfter;
import gov.nist.javax.sip.header.StatusLine;
import gov.nist.javax.sip.header.To;
import gov.nist.javax.sip.header.Via;
import gov.nist.javax.sip.header.ViaList;
import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.message.SIPResponse;
import gov.nist.javax.sip.parser.Pipeline;
import gov.nist.javax.sip.parser.PipelinedMsgParser;
import gov.nist.javax.sip.parser.SIPMessageListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.text.ParseException;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLSocket;
import javax.sip.ListeningPoint;
import javax.sip.address.Hop;
import javax.sip.header.Header;
import javax.sip.message.Response;

public final class TLSMessageChannel extends MessageChannel implements SIPMessageListener, Runnable, RawMessageChannel {
    private HandshakeCompletedListener handshakeCompletedListener;
    protected boolean isCached;
    protected boolean isRunning;
    private String key;
    private String myAddress;
    private InputStream myClientInputStream;
    private PipelinedMsgParser myParser;
    private int myPort;
    private Socket mySock;
    private Thread mythread;
    private InetAddress peerAddress;
    private int peerPort;
    private String peerProtocol;
    private SIPTransactionStack sipStack;
    private TLSMessageProcessor tlsMessageProcessor;

    protected TLSMessageChannel(Socket sock, SIPTransactionStack sipStack2, TLSMessageProcessor msgProcessor) throws IOException {
        if (sipStack2.isLoggingEnabled()) {
            sipStack2.getStackLogger().logDebug("creating new TLSMessageChannel (incoming)");
            sipStack2.getStackLogger().logStackTrace();
        }
        this.mySock = (SSLSocket) sock;
        if (sock instanceof SSLSocket) {
            SSLSocket sslSock = (SSLSocket) sock;
            sslSock.setNeedClientAuth(true);
            this.handshakeCompletedListener = new HandshakeCompletedListenerImpl(this);
            sslSock.addHandshakeCompletedListener(this.handshakeCompletedListener);
            sslSock.startHandshake();
        }
        this.peerAddress = this.mySock.getInetAddress();
        this.myAddress = msgProcessor.getIpAddress().getHostAddress();
        this.myClientInputStream = this.mySock.getInputStream();
        this.mythread = new Thread(this);
        this.mythread.setDaemon(true);
        this.mythread.setName("TLSMessageChannelThread");
        this.sipStack = sipStack2;
        this.tlsMessageProcessor = msgProcessor;
        this.myPort = this.tlsMessageProcessor.getPort();
        this.peerPort = this.mySock.getPort();
        this.messageProcessor = msgProcessor;
        this.mythread.start();
    }

    protected TLSMessageChannel(InetAddress inetAddr, int port, SIPTransactionStack sipStack2, TLSMessageProcessor messageProcessor) throws IOException {
        if (sipStack2.isLoggingEnabled()) {
            sipStack2.getStackLogger().logDebug("creating new TLSMessageChannel (outgoing)");
            sipStack2.getStackLogger().logStackTrace();
        }
        this.peerAddress = inetAddr;
        this.peerPort = port;
        this.myPort = messageProcessor.getPort();
        this.peerProtocol = ListeningPoint.TLS;
        this.sipStack = sipStack2;
        this.tlsMessageProcessor = messageProcessor;
        this.myAddress = messageProcessor.getIpAddress().getHostAddress();
        this.key = MessageChannel.getKey(this.peerAddress, this.peerPort, ListeningPoint.TLS);
        this.messageProcessor = messageProcessor;
    }

    public boolean isReliable() {
        return true;
    }

    public void close() {
        try {
            if (this.mySock != null) {
                this.mySock.close();
            }
            if (this.sipStack.isLoggingEnabled()) {
                StackLogger stackLogger = this.sipStack.getStackLogger();
                stackLogger.logDebug("Closing message Channel " + this);
            }
        } catch (IOException ex) {
            if (this.sipStack.isLoggingEnabled()) {
                StackLogger stackLogger2 = this.sipStack.getStackLogger();
                stackLogger2.logDebug("Error closing socket " + ex);
            }
        }
    }

    public SIPTransactionStack getSIPStack() {
        return this.sipStack;
    }

    public String getTransport() {
        return ParameterNames.TLS;
    }

    public String getPeerAddress() {
        if (this.peerAddress != null) {
            return this.peerAddress.getHostAddress();
        }
        return getHost();
    }

    /* access modifiers changed from: protected */
    public InetAddress getPeerInetAddress() {
        return this.peerAddress;
    }

    public String getPeerProtocol() {
        return this.peerProtocol;
    }

    private void sendMessage(byte[] msg, boolean retry) throws IOException {
        Socket sock = this.sipStack.ioHandler.sendBytes(getMessageProcessor().getIpAddress(), this.peerAddress, this.peerPort, this.peerProtocol, msg, retry, this);
        if (sock != this.mySock && sock != null) {
            try {
                if (this.mySock != null) {
                    this.mySock.close();
                }
            } catch (IOException e) {
            }
            this.mySock = sock;
            this.myClientInputStream = this.mySock.getInputStream();
            Thread thread = new Thread(this);
            thread.setDaemon(true);
            thread.setName("TLSMessageChannelThread");
            thread.start();
        }
    }

    public void sendMessage(SIPMessage sipMessage) throws IOException {
        byte[] msg = sipMessage.encodeAsBytes(getTransport());
        long time = System.currentTimeMillis();
        sendMessage(msg, sipMessage instanceof SIPRequest);
        if (this.sipStack.getStackLogger().isLoggingEnabled(16)) {
            logMessage(sipMessage, this.peerAddress, this.peerPort, time);
        }
    }

    public void sendMessage(byte[] message, InetAddress receiverAddress, int receiverPort, boolean retry) throws IOException {
        if (message == null || receiverAddress == null) {
            throw new IllegalArgumentException("Null argument");
        }
        Socket sock = this.sipStack.ioHandler.sendBytes(this.messageProcessor.getIpAddress(), receiverAddress, receiverPort, ListeningPoint.TLS, message, retry, this);
        if (sock != this.mySock && sock != null) {
            try {
                if (this.mySock != null) {
                    this.mySock.close();
                }
            } catch (IOException e) {
            }
            this.mySock = sock;
            this.myClientInputStream = this.mySock.getInputStream();
            Thread mythread2 = new Thread(this);
            mythread2.setDaemon(true);
            mythread2.setName("TLSMessageChannelThread");
            mythread2.start();
        }
    }

    public void handleException(ParseException ex, SIPMessage sipMessage, Class hdrClass, String header, String message) throws ParseException {
        if (this.sipStack.isLoggingEnabled()) {
            this.sipStack.getStackLogger().logException(ex);
        }
        if (hdrClass == null || (!hdrClass.equals(From.class) && !hdrClass.equals(To.class) && !hdrClass.equals(CSeq.class) && !hdrClass.equals(Via.class) && !hdrClass.equals(CallID.class) && !hdrClass.equals(RequestLine.class) && !hdrClass.equals(StatusLine.class))) {
            sipMessage.addUnparsed(header);
            return;
        }
        if (this.sipStack.isLoggingEnabled()) {
            StackLogger stackLogger = this.sipStack.getStackLogger();
            stackLogger.logDebug("Encountered bad message \n" + message);
        }
        String msgString = sipMessage.toString();
        if (!msgString.startsWith("SIP/") && !msgString.startsWith("ACK ")) {
            String badReqRes = createBadReqRes(msgString, ex);
            if (badReqRes != null) {
                if (this.sipStack.isLoggingEnabled()) {
                    this.sipStack.getStackLogger().logDebug("Sending automatic 400 Bad Request:");
                    this.sipStack.getStackLogger().logDebug(badReqRes);
                }
                try {
                    sendMessage(badReqRes.getBytes(), getPeerInetAddress(), getPeerPort(), false);
                } catch (IOException e) {
                    this.sipStack.getStackLogger().logException(e);
                }
            } else if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logDebug("Could not formulate automatic 400 Bad Request");
            }
        }
        throw ex;
    }

    public void processMessage(SIPMessage sipMessage) throws Exception {
        if (sipMessage.getFrom() == null || sipMessage.getTo() == null || sipMessage.getCallId() == null || sipMessage.getCSeq() == null || sipMessage.getViaHeaders() == null) {
            String badmsg = sipMessage.encode();
            if (this.sipStack.isLoggingEnabled()) {
                StackLogger stackLogger = this.sipStack.getStackLogger();
                stackLogger.logError("bad message " + badmsg);
                this.sipStack.getStackLogger().logError(">>> Dropped Bad Msg");
            }
            return;
        }
        ViaList viaList = sipMessage.getViaHeaders();
        if (sipMessage instanceof SIPRequest) {
            Via v = (Via) viaList.getFirst();
            Hop hop = this.sipStack.addressResolver.resolveAddress(v.getHop());
            this.peerProtocol = v.getTransport();
            try {
                this.peerAddress = this.mySock.getInetAddress();
                if (v.hasParameter("rport") || !hop.getHost().equals(this.peerAddress.getHostAddress())) {
                    v.setParameter("received", this.peerAddress.getHostAddress());
                }
                v.setParameter("rport", Integer.toString(this.peerPort));
            } catch (ParseException ex) {
                InternalErrorHandler.handleException((Exception) ex);
            }
            if (!this.isCached) {
                ((TLSMessageProcessor) this.messageProcessor).cacheMessageChannel(this);
                this.isCached = true;
                this.sipStack.ioHandler.putSocket(IOHandler.makeKey(this.mySock.getInetAddress(), this.peerPort), this.mySock);
            }
        }
        long receptionTime = System.currentTimeMillis();
        int i = 0;
        if (sipMessage instanceof SIPRequest) {
            SIPRequest sipRequest = (SIPRequest) sipMessage;
            if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logDebug("----Processing Message---");
            }
            if (this.sipStack.getStackLogger().isLoggingEnabled(16)) {
                ServerLogger serverLogger = this.sipStack.serverLogger;
                String hostPort = getPeerHostPort().toString();
                serverLogger.logMessage(sipMessage, hostPort, this.messageProcessor.getIpAddress().getHostAddress() + Separators.COLON + this.messageProcessor.getPort(), false, receptionTime);
            }
            if (this.sipStack.getMaxMessageSize() > 0) {
                if (sipRequest.getSize() + (sipRequest.getContentLength() == null ? 0 : sipRequest.getContentLength().getContentLength()) > this.sipStack.getMaxMessageSize()) {
                    sendMessage(sipRequest.createResponse(Response.MESSAGE_TOO_LARGE).encodeAsBytes(getTransport()), false);
                    throw new Exception("Message size exceeded");
                }
            }
            ServerRequestInterface sipServerRequest = this.sipStack.newSIPServerRequest(sipRequest, this);
            if (sipServerRequest != null) {
                try {
                    sipServerRequest.processRequest(sipRequest, this);
                } finally {
                    if ((sipServerRequest instanceof SIPTransaction) && !((SIPServerTransaction) sipServerRequest).passToListener()) {
                        ((SIPTransaction) sipServerRequest).releaseSem();
                    }
                }
            } else {
                SIPResponse response = sipRequest.createResponse(Response.SERVICE_UNAVAILABLE);
                RetryAfter retryAfter = new RetryAfter();
                try {
                    retryAfter.setRetryAfter((int) (10.0d * Math.random()));
                    response.setHeader((Header) retryAfter);
                    sendMessage(response);
                } catch (Exception e) {
                }
                if (this.sipStack.isLoggingEnabled()) {
                    this.sipStack.getStackLogger().logWarning("Dropping message -- could not acquire semaphore");
                }
            }
        } else {
            SIPResponse sipResponse = (SIPResponse) sipMessage;
            try {
                sipResponse.checkHeaders();
                if (this.sipStack.getMaxMessageSize() > 0) {
                    int size = sipResponse.getSize();
                    if (sipResponse.getContentLength() != null) {
                        i = sipResponse.getContentLength().getContentLength();
                    }
                    if (size + i > this.sipStack.getMaxMessageSize()) {
                        if (this.sipStack.isLoggingEnabled()) {
                            this.sipStack.getStackLogger().logDebug("Message size exceeded");
                        }
                        return;
                    }
                }
                ServerResponseInterface sipServerResponse = this.sipStack.newSIPServerResponse(sipResponse, this);
                if (sipServerResponse != null) {
                    try {
                        if (!(sipServerResponse instanceof SIPClientTransaction) || ((SIPClientTransaction) sipServerResponse).checkFromTag(sipResponse)) {
                            sipServerResponse.processResponse(sipResponse, this);
                            if ((sipServerResponse instanceof SIPTransaction) && !((SIPTransaction) sipServerResponse).passToListener()) {
                                ((SIPTransaction) sipServerResponse).releaseSem();
                            }
                        } else {
                            if (this.sipStack.isLoggingEnabled()) {
                                StackLogger stackLogger2 = this.sipStack.getStackLogger();
                                stackLogger2.logError("Dropping response message with invalid tag >>> " + sipResponse);
                            }
                        }
                    } finally {
                        if ((sipServerResponse instanceof SIPTransaction) && !((SIPTransaction) sipServerResponse).passToListener()) {
                            ((SIPTransaction) sipServerResponse).releaseSem();
                        }
                    }
                } else {
                    this.sipStack.getStackLogger().logWarning("Could not get semaphore... dropping response");
                }
            } catch (ParseException e2) {
                if (this.sipStack.isLoggingEnabled()) {
                    StackLogger stackLogger3 = this.sipStack.getStackLogger();
                    stackLogger3.logError("Dropping Badly formatted response message >>> " + sipResponse);
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0084, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0086, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:?, code lost:
        gov.nist.core.InternalErrorHandler.handleException(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x008b, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:?, code lost:
        r0.write("\r\n\r\n".getBytes("UTF-8"));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x009f, code lost:
        if (r9.sipStack.isLoggingEnabled() != false) goto L_0x00a1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00a1, code lost:
        r9.sipStack.getStackLogger().logDebug("IOException  closing sock " + r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00bf, code lost:
        if (r9.sipStack.maxConnections != -1) goto L_0x00c1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00c3, code lost:
        monitor-enter(r9.tlsMessageProcessor);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:?, code lost:
        r9.tlsMessageProcessor.nConnections--;
        r9.tlsMessageProcessor.notify();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x00d5, code lost:
        r9.mySock.close();
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00e0, code lost:
        r9.isRunning = false;
        r9.tlsMessageProcessor.remove(r9);
        r9.tlsMessageProcessor.useCount--;
        r9.myParser.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00f3, code lost:
        throw r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00f5, code lost:
        r9.isRunning = false;
        r9.tlsMessageProcessor.remove(r9);
        r9.tlsMessageProcessor.useCount--;
        r9.myParser.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x0108, code lost:
        return;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0086 A[ExcHandler: Exception (r2v12 'ex' java.lang.Exception A[CUSTOM_DECLARE]), Splitter:B:2:0x0033] */
    public void run() {
        Pipeline hispipe = new Pipeline(this.myClientInputStream, this.sipStack.readTimeout, this.sipStack.getTimer());
        this.myParser = new PipelinedMsgParser(this, hispipe, this.sipStack.getMaxMessageSize());
        this.myParser.processInput();
        this.tlsMessageProcessor.useCount++;
        this.isRunning = true;
        while (true) {
            try {
                byte[] msg = new byte[4096];
                int nbytes = this.myClientInputStream.read(msg, 0, 4096);
                if (nbytes == -1) {
                    hispipe.write("\r\n\r\n".getBytes("UTF-8"));
                    if (this.sipStack.maxConnections == -1) {
                        break;
                    }
                    synchronized (this.tlsMessageProcessor) {
                        this.tlsMessageProcessor.nConnections--;
                        this.tlsMessageProcessor.notify();
                    }
                    break;
                }
                hispipe.write(msg, 0, nbytes);
            } catch (IOException e) {
            } catch (Exception ex) {
            }
        }
        hispipe.close();
        this.mySock.close();
        this.isRunning = false;
        this.tlsMessageProcessor.remove(this);
        this.tlsMessageProcessor.useCount--;
        this.myParser.close();
    }

    /* access modifiers changed from: protected */
    public void uncache() {
        if (this.isCached && !this.isRunning) {
            this.tlsMessageProcessor.remove(this);
        }
    }

    public boolean equals(Object other) {
        if (getClass().equals(other.getClass()) && this.mySock == ((TLSMessageChannel) other).mySock) {
            return true;
        }
        return false;
    }

    public String getKey() {
        if (this.key != null) {
            return this.key;
        }
        this.key = MessageChannel.getKey(this.peerAddress, this.peerPort, ListeningPoint.TLS);
        return this.key;
    }

    public String getViaHost() {
        return this.myAddress;
    }

    public int getViaPort() {
        return this.myPort;
    }

    public int getPeerPort() {
        return this.peerPort;
    }

    public int getPeerPacketSourcePort() {
        return this.peerPort;
    }

    public InetAddress getPeerPacketSourceAddress() {
        return this.peerAddress;
    }

    public boolean isSecure() {
        return true;
    }

    public void setHandshakeCompletedListener(HandshakeCompletedListener handshakeCompletedListenerImpl) {
        this.handshakeCompletedListener = handshakeCompletedListenerImpl;
    }

    public HandshakeCompletedListenerImpl getHandshakeCompletedListener() {
        return (HandshakeCompletedListenerImpl) this.handshakeCompletedListener;
    }
}
