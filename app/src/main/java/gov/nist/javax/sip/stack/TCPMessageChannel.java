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
import org.ccil.cowan.tagsoup.HTMLModels;

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
        if (!(msgString.startsWith("SIP/") || msgString.startsWith("ACK "))) {
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
        ServerRequestInterface sipServerRequest;
        ServerResponseInterface sipServerResponse;
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
                if (v.hasParameter(Via.RPORT) || !hop.getHost().equals(this.peerAddress.getHostAddress())) {
                    v.setParameter(Via.RECEIVED, this.peerAddress.getHostAddress());
                }
                v.setParameter(Via.RPORT, Integer.toString(this.peerPort));
            } catch (Exception ex) {
                InternalErrorHandler.handleException(ex, this.sipStack.getStackLogger());
            } catch (Throwable th) {
                if (sipServerRequest instanceof SIPTransaction) {
                    if (!((SIPServerTransaction) sipServerRequest).passToListener()) {
                        ((SIPTransaction) sipServerRequest).releaseSem();
                    }
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
                if (sipServerRequest instanceof SIPTransaction) {
                    if (!((SIPServerTransaction) sipServerRequest).passToListener()) {
                        ((SIPTransaction) sipServerRequest).releaseSem();
                    }
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
                if (sipServerResponse != null) {
                    if (sipServerResponse instanceof SIPClientTransaction) {
                        if (!((SIPClientTransaction) sipServerResponse).checkFromTag(sipResponse)) {
                            if (this.sipStack.isLoggingEnabled()) {
                                this.sipStack.getStackLogger().logError("Dropping response message with invalid tag >>> " + sipResponse);
                            }
                            if (sipServerResponse instanceof SIPTransaction) {
                                if (!((SIPTransaction) sipServerResponse).passToListener()) {
                                    ((SIPTransaction) sipServerResponse).releaseSem();
                                }
                            }
                            return;
                        }
                    }
                    sipServerResponse.processResponse(sipResponse, this);
                    if (sipServerResponse instanceof SIPTransaction) {
                        if (!((SIPTransaction) sipServerResponse).passToListener()) {
                            ((SIPTransaction) sipServerResponse).releaseSem();
                        }
                    }
                } else {
                    this.sipStack.getStackLogger().logWarning("Application is blocked -- could not acquire semaphore -- dropping response");
                }
            } catch (ParseException e) {
                if (this.sipStack.isLoggingEnabled()) {
                    this.sipStack.getStackLogger().logError("Dropping Badly formatted response message >>> " + sipResponse);
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
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
                byte[] msg = new byte[HTMLModels.M_INLINE];
                int nbytes = this.myClientInputStream.read(msg, 0, HTMLModels.M_INLINE);
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
                InternalErrorHandler.handleException(ex, this.sipStack.getStackLogger());
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
        if (this.isCached && !this.isRunning) {
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
