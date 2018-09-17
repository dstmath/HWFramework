package gov.nist.javax.sip.stack;

import gov.nist.core.InternalErrorHandler;
import gov.nist.core.Separators;
import gov.nist.javax.sip.header.CSeq;
import gov.nist.javax.sip.header.CallID;
import gov.nist.javax.sip.header.From;
import gov.nist.javax.sip.header.RequestLine;
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
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.ParseException;
import java.util.TimerTask;
import javax.sip.ListeningPoint;
import javax.sip.address.Hop;
import javax.sip.message.Response;

public class TCPMessageChannel extends MessageChannel implements SIPMessageListener, Runnable, RawMessageChannel {
    protected boolean isCached;
    protected boolean isRunning;
    protected String key;
    protected String myAddress;
    protected InputStream myClientInputStream;
    protected OutputStream myClientOutputStream;
    private PipelinedMsgParser myParser;
    protected int myPort;
    private Socket mySock;
    private Thread mythread;
    protected InetAddress peerAddress;
    protected int peerPort;
    protected String peerProtocol;
    protected SIPTransactionStack sipStack;
    private TCPMessageProcessor tcpMessageProcessor;

    protected TCPMessageChannel(SIPTransactionStack sipStack) {
        this.sipStack = sipStack;
    }

    protected TCPMessageChannel(Socket sock, SIPTransactionStack sipStack, TCPMessageProcessor msgProcessor) throws IOException {
        if (sipStack.isLoggingEnabled()) {
            sipStack.getStackLogger().logDebug("creating new TCPMessageChannel ");
            sipStack.getStackLogger().logStackTrace();
        }
        this.mySock = sock;
        this.peerAddress = this.mySock.getInetAddress();
        this.myAddress = msgProcessor.getIpAddress().getHostAddress();
        this.myClientInputStream = this.mySock.getInputStream();
        this.myClientOutputStream = this.mySock.getOutputStream();
        this.mythread = new Thread(this);
        this.mythread.setDaemon(true);
        this.mythread.setName("TCPMessageChannelThread");
        this.sipStack = sipStack;
        this.peerPort = this.mySock.getPort();
        this.tcpMessageProcessor = msgProcessor;
        this.myPort = this.tcpMessageProcessor.getPort();
        this.messageProcessor = msgProcessor;
        this.mythread.start();
    }

    protected TCPMessageChannel(InetAddress inetAddr, int port, SIPTransactionStack sipStack, TCPMessageProcessor messageProcessor) throws IOException {
        if (sipStack.isLoggingEnabled()) {
            sipStack.getStackLogger().logDebug("creating new TCPMessageChannel ");
            sipStack.getStackLogger().logStackTrace();
        }
        this.peerAddress = inetAddr;
        this.peerPort = port;
        this.myPort = messageProcessor.getPort();
        this.peerProtocol = ListeningPoint.TCP;
        this.sipStack = sipStack;
        this.tcpMessageProcessor = messageProcessor;
        this.myAddress = messageProcessor.getIpAddress().getHostAddress();
        this.key = MessageChannel.getKey(this.peerAddress, this.peerPort, ListeningPoint.TCP);
        this.messageProcessor = messageProcessor;
    }

    public boolean isReliable() {
        return true;
    }

    public void close() {
        try {
            if (this.mySock != null) {
                this.mySock.close();
                this.mySock = null;
            }
            if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logDebug("Closing message Channel " + this);
            }
        } catch (IOException ex) {
            if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logDebug("Error closing socket " + ex);
            }
        }
    }

    public SIPTransactionStack getSIPStack() {
        return this.sipStack;
    }

    public String getTransport() {
        return ListeningPoint.TCP;
    }

    public String getPeerAddress() {
        if (this.peerAddress != null) {
            return this.peerAddress.getHostAddress();
        }
        return getHost();
    }

    protected InetAddress getPeerInetAddress() {
        return this.peerAddress;
    }

    public String getPeerProtocol() {
        return this.peerProtocol;
    }

    private void sendMessage(byte[] msg, boolean retry) throws IOException {
        Socket sock = this.sipStack.ioHandler.sendBytes(this.messageProcessor.getIpAddress(), this.peerAddress, this.peerPort, this.peerProtocol, msg, retry, this);
        if (sock != this.mySock && sock != null) {
            try {
                if (this.mySock != null) {
                    this.mySock.close();
                }
            } catch (IOException e) {
            }
            this.mySock = sock;
            this.myClientInputStream = this.mySock.getInputStream();
            this.myClientOutputStream = this.mySock.getOutputStream();
            Thread thread = new Thread(this);
            thread.setDaemon(true);
            thread.setName("TCPMessageChannelThread");
            thread.start();
        }
    }

    public void sendMessage(SIPMessage sipMessage) throws IOException {
        byte[] msg = sipMessage.encodeAsBytes(getTransport());
        long time = System.currentTimeMillis();
        sendMessage(msg, true);
        if (this.sipStack.getStackLogger().isLoggingEnabled(16)) {
            logMessage(sipMessage, this.peerAddress, this.peerPort, time);
        }
    }

    public void sendMessage(byte[] message, InetAddress receiverAddress, int receiverPort, boolean retry) throws IOException {
        if (message == null || receiverAddress == null) {
            throw new IllegalArgumentException("Null argument");
        }
        Socket sock = this.sipStack.ioHandler.sendBytes(this.messageProcessor.getIpAddress(), receiverAddress, receiverPort, ListeningPoint.TCP, message, retry, this);
        if (sock != this.mySock && sock != null) {
            if (this.mySock != null) {
                this.sipStack.getTimer().schedule(new TimerTask() {
                    public boolean cancel() {
                        try {
                            TCPMessageChannel.this.mySock.close();
                            super.cancel();
                        } catch (IOException e) {
                        }
                        return true;
                    }

                    public void run() {
                        try {
                            TCPMessageChannel.this.mySock.close();
                        } catch (IOException e) {
                        }
                    }
                }, 8000);
            }
            this.mySock = sock;
            this.myClientInputStream = this.mySock.getInputStream();
            this.myClientOutputStream = this.mySock.getOutputStream();
            Thread mythread = new Thread(this);
            mythread.setDaemon(true);
            mythread.setName("TCPMessageChannelThread");
            mythread.start();
        }
    }

    public void handleException(ParseException ex, SIPMessage sipMessage, Class hdrClass, String header, String message) throws ParseException {
        if (this.sipStack.isLoggingEnabled()) {
            this.sipStack.getStackLogger().logException(ex);
        }
        if (hdrClass == null || !(hdrClass.equals(From.class) || hdrClass.equals(To.class) || hdrClass.equals(CSeq.class) || hdrClass.equals(Via.class) || hdrClass.equals(CallID.class) || hdrClass.equals(RequestLine.class) || hdrClass.equals(StatusLine.class))) {
            sipMessage.addUnparsed(header);
            return;
        }
        if (this.sipStack.isLoggingEnabled()) {
            this.sipStack.getStackLogger().logDebug("Encountered Bad Message \n" + sipMessage.toString());
        }
        String msgString = sipMessage.toString();
        if (!(msgString.startsWith("SIP/") || (msgString.startsWith("ACK ") ^ 1) == 0)) {
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
        ServerResponseInterface sipServerResponse;
        ServerRequestInterface sipServerRequest;
        if (sipMessage.getFrom() == null || sipMessage.getTo() == null || sipMessage.getCallId() == null || sipMessage.getCSeq() == null || sipMessage.getViaHeaders() == null) {
            String badmsg = sipMessage.encode();
            if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logDebug(">>> Dropped Bad Msg");
                this.sipStack.getStackLogger().logDebug(badmsg);
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
                if (v.hasParameter("rport") || (hop.getHost().equals(this.peerAddress.getHostAddress()) ^ 1) != 0) {
                    v.setParameter("received", this.peerAddress.getHostAddress());
                }
                v.setParameter("rport", Integer.toString(this.peerPort));
            } catch (Exception ex) {
                InternalErrorHandler.handleException(ex, this.sipStack.getStackLogger());
            } catch (Throwable th) {
                if ((sipServerRequest instanceof SIPTransaction) && !((SIPServerTransaction) sipServerRequest).passToListener()) {
                    ((SIPTransaction) sipServerRequest).releaseSem();
                }
            }
            if (!this.isCached) {
                ((TCPMessageProcessor) this.messageProcessor).cacheMessageChannel(this);
                this.isCached = true;
                this.sipStack.ioHandler.putSocket(IOHandler.makeKey(this.mySock.getInetAddress(), ((InetSocketAddress) this.mySock.getRemoteSocketAddress()).getPort()), this.mySock);
            }
        }
        long receptionTime = System.currentTimeMillis();
        if (sipMessage instanceof SIPRequest) {
            SIPRequest sipRequest = (SIPRequest) sipMessage;
            if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logDebug("----Processing Message---");
            }
            if (this.sipStack.getStackLogger().isLoggingEnabled(16)) {
                this.sipStack.serverLogger.logMessage(sipMessage, getPeerHostPort().toString(), getMessageProcessor().getIpAddress().getHostAddress() + Separators.COLON + getMessageProcessor().getPort(), false, receptionTime);
            }
            if (this.sipStack.getMaxMessageSize() > 0) {
                if ((sipRequest.getContentLength() == null ? 0 : sipRequest.getContentLength().getContentLength()) + sipRequest.getSize() > this.sipStack.getMaxMessageSize()) {
                    sendMessage(sipRequest.createResponse(Response.MESSAGE_TOO_LARGE).encodeAsBytes(getTransport()), false);
                    throw new Exception("Message size exceeded");
                }
            }
            sipServerRequest = this.sipStack.newSIPServerRequest(sipRequest, this);
            if (sipServerRequest != null) {
                sipServerRequest.processRequest(sipRequest, this);
                if ((sipServerRequest instanceof SIPTransaction) && !((SIPServerTransaction) sipServerRequest).passToListener()) {
                    ((SIPTransaction) sipServerRequest).releaseSem();
                }
            } else if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logWarning("Dropping request -- could not acquire semaphore in 10 sec");
            }
        } else {
            SIPResponse sipResponse = (SIPResponse) sipMessage;
            try {
                sipResponse.checkHeaders();
                if (this.sipStack.getMaxMessageSize() > 0) {
                    if ((sipResponse.getContentLength() == null ? 0 : sipResponse.getContentLength().getContentLength()) + sipResponse.getSize() > this.sipStack.getMaxMessageSize()) {
                        if (this.sipStack.isLoggingEnabled()) {
                            this.sipStack.getStackLogger().logDebug("Message size exceeded");
                        }
                        return;
                    }
                }
                sipServerResponse = this.sipStack.newSIPServerResponse(sipResponse, this);
                if (sipServerResponse == null) {
                    this.sipStack.getStackLogger().logWarning("Application is blocked -- could not acquire semaphore -- dropping response");
                } else if (!(sipServerResponse instanceof SIPClientTransaction) || (((SIPClientTransaction) sipServerResponse).checkFromTag(sipResponse) ^ 1) == 0) {
                    sipServerResponse.processResponse(sipResponse, this);
                    if ((sipServerResponse instanceof SIPTransaction) && (((SIPTransaction) sipServerResponse).passToListener() ^ 1) != 0) {
                        ((SIPTransaction) sipServerResponse).releaseSem();
                    }
                } else {
                    if (this.sipStack.isLoggingEnabled()) {
                        this.sipStack.getStackLogger().logError("Dropping response message with invalid tag >>> " + sipResponse);
                    }
                    if ((sipServerResponse instanceof SIPTransaction) && (((SIPTransaction) sipServerResponse).passToListener() ^ 1) != 0) {
                        ((SIPTransaction) sipServerResponse).releaseSem();
                    }
                }
            } catch (ParseException e) {
                if (this.sipStack.isLoggingEnabled()) {
                    this.sipStack.getStackLogger().logError("Dropping Badly formatted response message >>> " + sipResponse);
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:45:0x00f0 A:{ExcHandler: java.lang.Exception (r3_0 'ex' java.lang.Exception), Splitter: B:1:0x0033} */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing block: B:26:0x0089, code:
            r2 = move-exception;
     */
    /* JADX WARNING: Missing block: B:28:?, code:
            r5.write("\r\n\r\n".getBytes("UTF-8"));
     */
    /* JADX WARNING: Missing block: B:31:0x009d, code:
            if (r14.sipStack.isLoggingEnabled() != false) goto L_0x009f;
     */
    /* JADX WARNING: Missing block: B:32:0x009f, code:
            r14.sipStack.getStackLogger().logDebug("IOException  closing sock " + r2);
     */
    /* JADX WARNING: Missing block: B:35:0x00c0, code:
            if (r14.sipStack.maxConnections != -1) goto L_0x00c2;
     */
    /* JADX WARNING: Missing block: B:37:0x00c4, code:
            monitor-enter(r14.tcpMessageProcessor);
     */
    /* JADX WARNING: Missing block: B:39:?, code:
            r9 = r14.tcpMessageProcessor;
            r9.nConnections--;
            r14.tcpMessageProcessor.notify();
     */
    /* JADX WARNING: Missing block: B:42:0x00d3, code:
            r14.mySock.close();
            r5.close();
     */
    /* JADX WARNING: Missing block: B:43:0x00db, code:
            r14.isRunning = false;
            r14.tcpMessageProcessor.remove(r14);
            r9 = r14.tcpMessageProcessor;
            r9.useCount--;
            r14.myParser.close();
     */
    /* JADX WARNING: Missing block: B:44:0x00ef, code:
            return;
     */
    /* JADX WARNING: Missing block: B:45:0x00f0, code:
            r3 = move-exception;
     */
    /* JADX WARNING: Missing block: B:47:?, code:
            gov.nist.core.InternalErrorHandler.handleException(r3, r14.sipStack.getStackLogger());
     */
    /* JADX WARNING: Missing block: B:50:0x00fd, code:
            r14.isRunning = false;
            r14.tcpMessageProcessor.remove(r14);
            r10 = r14.tcpMessageProcessor;
            r10.useCount--;
            r14.myParser.close();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void run() {
        Pipeline hispipe = new Pipeline(this.myClientInputStream, this.sipStack.readTimeout, this.sipStack.getTimer());
        this.myParser = new PipelinedMsgParser(this, hispipe, this.sipStack.getMaxMessageSize());
        this.myParser.processInput();
        TCPMessageProcessor tCPMessageProcessor = this.tcpMessageProcessor;
        tCPMessageProcessor.useCount++;
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
                    synchronized (this.tcpMessageProcessor) {
                        tCPMessageProcessor = this.tcpMessageProcessor;
                        tCPMessageProcessor.nConnections--;
                        this.tcpMessageProcessor.notify();
                        break;
                    }
                }
                hispipe.write(msg, 0, nbytes);
            } catch (IOException e) {
            } catch (Exception ex) {
            }
        }
        hispipe.close();
        this.mySock.close();
        this.isRunning = false;
        this.tcpMessageProcessor.remove(this);
        tCPMessageProcessor = this.tcpMessageProcessor;
        tCPMessageProcessor.useCount--;
        this.myParser.close();
    }

    protected void uncache() {
        if (this.isCached && (this.isRunning ^ 1) != 0) {
            this.tcpMessageProcessor.remove(this);
        }
    }

    public boolean equals(Object other) {
        if (!getClass().equals(other.getClass())) {
            return false;
        }
        if (this.mySock != ((TCPMessageChannel) other).mySock) {
            return false;
        }
        return true;
    }

    public String getKey() {
        if (this.key != null) {
            return this.key;
        }
        this.key = MessageChannel.getKey(this.peerAddress, this.peerPort, ListeningPoint.TCP);
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
        return false;
    }
}
