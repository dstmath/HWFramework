package gov.nist.javax.sip.stack;

import gov.nist.core.InternalErrorHandler;
import gov.nist.core.StackLogger;
import gov.nist.javax.sip.SIPConstants;
import gov.nist.javax.sip.ServerTransactionExt;
import gov.nist.javax.sip.SipProviderImpl;
import gov.nist.javax.sip.Utils;
import gov.nist.javax.sip.header.Expires;
import gov.nist.javax.sip.header.RSeq;
import gov.nist.javax.sip.header.Via;
import gov.nist.javax.sip.header.ViaList;
import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.message.SIPResponse;
import gov.nist.javax.sip.stack.SIPTransaction;
import java.io.IOException;
import java.text.ParseException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import javax.sip.Dialog;
import javax.sip.DialogState;
import javax.sip.DialogTerminatedEvent;
import javax.sip.ListeningPoint;
import javax.sip.ObjectInUseException;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.Timeout;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionState;
import javax.sip.address.Hop;
import javax.sip.message.Request;
import javax.sip.message.Response;

public class SIPServerTransaction extends SIPTransaction implements ServerRequestInterface, ServerTransaction, ServerTransactionExt {
    private SIPDialog dialog;
    private SIPServerTransaction inviteTransaction;
    protected boolean isAckSeen;
    private SIPResponse pendingReliableResponse;
    private SIPClientTransaction pendingSubscribeTransaction;
    private Semaphore provisionalResponseSem = new Semaphore(1);
    private ProvisionalResponseTask provisionalResponseTask;
    private transient ServerRequestInterface requestOf;
    private boolean retransmissionAlertEnabled;
    private RetransmissionAlertTimerTask retransmissionAlertTimerTask;
    private int rseqNumber;

    class ListenerExecutionMaxTimer extends SIPStackTimerTask {
        SIPServerTransaction serverTransaction = SIPServerTransaction.this;

        ListenerExecutionMaxTimer() {
        }

        /* access modifiers changed from: protected */
        public void runTask() {
            try {
                if (this.serverTransaction.getState() == null) {
                    this.serverTransaction.terminate();
                    SIPTransactionStack sipStack = this.serverTransaction.getSIPStack();
                    sipStack.removePendingTransaction(this.serverTransaction);
                    sipStack.removeTransaction(this.serverTransaction);
                }
            } catch (Exception ex) {
                SIPServerTransaction.this.sipStack.getStackLogger().logError("unexpected exception", ex);
            }
        }
    }

    class ProvisionalResponseTask extends SIPStackTimerTask {
        int ticks = 1;
        int ticksLeft = this.ticks;

        public ProvisionalResponseTask() {
        }

        /* access modifiers changed from: protected */
        public void runTask() {
            SIPServerTransaction serverTransaction = SIPServerTransaction.this;
            if (serverTransaction.isTerminated()) {
                cancel();
                return;
            }
            this.ticksLeft--;
            if (this.ticksLeft == -1) {
                serverTransaction.fireReliableResponseRetransmissionTimer();
                this.ticksLeft = 2 * this.ticks;
                this.ticks = this.ticksLeft;
                if (this.ticksLeft >= 64) {
                    cancel();
                    SIPServerTransaction.this.setState(SIPTransaction.TERMINATED_STATE);
                    SIPServerTransaction.this.fireTimeoutTimer();
                }
            }
        }
    }

    class RetransmissionAlertTimerTask extends SIPStackTimerTask {
        String dialogId;
        int ticks = 1;
        int ticksLeft = this.ticks;

        public RetransmissionAlertTimerTask(String dialogId2) {
        }

        /* access modifiers changed from: protected */
        public void runTask() {
            SIPServerTransaction serverTransaction = SIPServerTransaction.this;
            this.ticksLeft--;
            if (this.ticksLeft == -1) {
                serverTransaction.fireRetransmissionTimer();
                this.ticksLeft = 2 * this.ticks;
            }
        }
    }

    class SendTrying extends SIPStackTimerTask {
        protected SendTrying() {
            if (SIPServerTransaction.this.sipStack.isLoggingEnabled()) {
                StackLogger stackLogger = SIPServerTransaction.this.sipStack.getStackLogger();
                stackLogger.logDebug("scheduled timer for " + this$0);
            }
        }

        /* access modifiers changed from: protected */
        public void runTask() {
            SIPServerTransaction serverTransaction = SIPServerTransaction.this;
            TransactionState realState = serverTransaction.getRealState();
            if (realState == null || TransactionState.TRYING == realState) {
                if (SIPServerTransaction.this.sipStack.isLoggingEnabled()) {
                    StackLogger stackLogger = SIPServerTransaction.this.sipStack.getStackLogger();
                    stackLogger.logDebug(" sending Trying current state = " + serverTransaction.getRealState());
                }
                try {
                    serverTransaction.sendMessage(serverTransaction.getOriginalRequest().createResponse(100, "Trying"));
                    if (SIPServerTransaction.this.sipStack.isLoggingEnabled()) {
                        StackLogger stackLogger2 = SIPServerTransaction.this.sipStack.getStackLogger();
                        stackLogger2.logDebug(" trying sent " + serverTransaction.getRealState());
                    }
                } catch (IOException e) {
                    if (SIPServerTransaction.this.sipStack.isLoggingEnabled()) {
                        SIPServerTransaction.this.sipStack.getStackLogger().logError("IO error sending  TRYING");
                    }
                }
            }
        }
    }

    class TransactionTimer extends SIPStackTimerTask {
        public TransactionTimer() {
            if (SIPServerTransaction.this.sipStack.isLoggingEnabled()) {
                StackLogger stackLogger = SIPServerTransaction.this.sipStack.getStackLogger();
                stackLogger.logDebug("TransactionTimer() : " + this$0.getTransactionId());
            }
        }

        /* access modifiers changed from: protected */
        public void runTask() {
            if (SIPServerTransaction.this.isTerminated()) {
                try {
                    cancel();
                } catch (IllegalStateException e) {
                    if (!SIPServerTransaction.this.sipStack.isAlive()) {
                        return;
                    }
                }
                SIPServerTransaction.this.sipStack.getTimer().schedule(new SIPTransaction.LingerTimer(), 8000);
            } else {
                SIPServerTransaction.this.fireTimer();
            }
        }
    }

    private void sendResponse(SIPResponse transactionResponse) throws IOException {
        String host;
        try {
            if (isReliable()) {
                getMessageChannel().sendMessage(transactionResponse);
            } else {
                Via via = transactionResponse.getTopmostVia();
                String transport = via.getTransport();
                if (transport != null) {
                    int port = via.getRPort();
                    if (port == -1) {
                        port = via.getPort();
                    }
                    if (port == -1) {
                        if (transport.equalsIgnoreCase(ListeningPoint.TLS)) {
                            port = 5061;
                        } else {
                            port = 5060;
                        }
                    }
                    if (via.getMAddr() != null) {
                        host = via.getMAddr();
                    } else {
                        host = via.getParameter("received");
                        if (host == null) {
                            host = via.getHost();
                        }
                    }
                    Hop hop = this.sipStack.addressResolver.resolveAddress(new HopImpl(host, port, transport));
                    MessageChannel messageChannel = getSIPStack().createRawMessageChannel(getSipProvider().getListeningPoint(hop.getTransport()).getIPAddress(), getPort(), hop);
                    if (messageChannel != null) {
                        messageChannel.sendMessage(transactionResponse);
                    } else {
                        throw new IOException("Could not create a message channel for " + hop);
                    }
                } else {
                    throw new IOException("missing transport!");
                }
            }
        } finally {
            startTransactionTimer();
        }
    }

    protected SIPServerTransaction(SIPTransactionStack sipStack, MessageChannel newChannelToUse) {
        super(sipStack, newChannelToUse);
        if (sipStack.maxListenerResponseTime != -1) {
            sipStack.getTimer().schedule(new ListenerExecutionMaxTimer(), (long) (sipStack.maxListenerResponseTime * 1000));
        }
        this.rseqNumber = (int) (Math.random() * 1000.0d);
        if (sipStack.isLoggingEnabled()) {
            StackLogger stackLogger = sipStack.getStackLogger();
            stackLogger.logDebug("Creating Server Transaction" + getBranchId());
            sipStack.getStackLogger().logStackTrace();
        }
    }

    public void setRequestInterface(ServerRequestInterface newRequestOf) {
        this.requestOf = newRequestOf;
    }

    public MessageChannel getResponseChannel() {
        return this;
    }

    public boolean isMessagePartOfTransaction(SIPMessage messageToTest) {
        boolean transactionMatches;
        SIPMessage sIPMessage = messageToTest;
        String method = messageToTest.getCSeq().getMethod();
        if (method.equals("INVITE") || !isTerminated()) {
            ViaList viaHeaders = messageToTest.getViaHeaders();
            if (viaHeaders != null) {
                Via topViaHeader = (Via) viaHeaders.getFirst();
                String messageBranch = topViaHeader.getBranch();
                if (messageBranch != null && !messageBranch.toLowerCase().startsWith(SIPConstants.BRANCH_MAGIC_COOKIE_LOWER_CASE)) {
                    messageBranch = null;
                }
                boolean skipTo = false;
                if (messageBranch == null || getBranch() == null) {
                    String originalFromTag = this.fromTag;
                    String thisFromTag = messageToTest.getFrom().getTag();
                    boolean skipFrom = originalFromTag == null || thisFromTag == null;
                    String originalToTag = this.toTag;
                    String thisToTag = messageToTest.getTo().getTag();
                    if (originalToTag == null || thisToTag == null) {
                        skipTo = true;
                    }
                    boolean isResponse = sIPMessage instanceof SIPResponse;
                    if (messageToTest.getCSeq().getMethod().equalsIgnoreCase(Request.CANCEL) && !getOriginalRequest().getCSeq().getMethod().equalsIgnoreCase(Request.CANCEL)) {
                        return false;
                    }
                    if ((isResponse || getOriginalRequest().getRequestURI().equals(((SIPRequest) sIPMessage).getRequestURI())) && ((skipFrom || (originalFromTag != null && originalFromTag.equalsIgnoreCase(thisFromTag))) && (skipTo || (originalToTag != null && originalToTag.equalsIgnoreCase(thisToTag))))) {
                        if (getOriginalRequest().getCallId().getCallId().equalsIgnoreCase(messageToTest.getCallId().getCallId())) {
                            transactionMatches = false;
                            if (getOriginalRequest().getCSeq().getSeqNumber() == messageToTest.getCSeq().getSeqNumber() && ((!messageToTest.getCSeq().getMethod().equals(Request.CANCEL) || getOriginalRequest().getMethod().equals(messageToTest.getCSeq().getMethod())) && topViaHeader.equals(getOriginalRequest().getViaHeaders().getFirst()))) {
                                return true;
                            }
                            return transactionMatches;
                        }
                    }
                } else if (method.equals(Request.CANCEL)) {
                    if (getMethod().equals(Request.CANCEL) && getBranch().equalsIgnoreCase(messageBranch) && topViaHeader.getSentBy().equals(((Via) getOriginalRequest().getViaHeaders().getFirst()).getSentBy())) {
                        skipTo = true;
                    }
                    return skipTo;
                } else {
                    if (getBranch().equalsIgnoreCase(messageBranch) && topViaHeader.getSentBy().equals(((Via) getOriginalRequest().getViaHeaders().getFirst()).getSentBy())) {
                        skipTo = true;
                    }
                    return skipTo;
                }
            }
            transactionMatches = false;
            return transactionMatches;
        }
        transactionMatches = false;
        return transactionMatches;
    }

    /* access modifiers changed from: protected */
    public void map() {
        TransactionState realState = getRealState();
        if (realState == null || realState == TransactionState.TRYING) {
            if (!isInviteTransaction() || this.isMapped || this.sipStack.getTimer() == null) {
                this.isMapped = true;
            } else {
                this.isMapped = true;
                this.sipStack.getTimer().schedule(new SendTrying(), 200);
            }
        }
        this.sipStack.removePendingTransaction(this);
    }

    public boolean isTransactionMapped() {
        return this.isMapped;
    }

    /* JADX WARNING: Removed duplicated region for block: B:47:0x012f A[Catch:{ IOException -> 0x0245 }] */
    public void processRequest(SIPRequest transactionRequest, MessageChannel sourceChannel) {
        boolean toTu = false;
        if (this.sipStack.isLoggingEnabled()) {
            StackLogger stackLogger = this.sipStack.getStackLogger();
            stackLogger.logDebug("processRequest: " + transactionRequest.getFirstLine());
            StackLogger stackLogger2 = this.sipStack.getStackLogger();
            stackLogger2.logDebug("tx state = " + getRealState());
        }
        try {
            if (getRealState() == null) {
                setOriginalRequest(transactionRequest);
                setState(TransactionState.TRYING);
                toTu = true;
                setPassToListener();
                if (isInviteTransaction() && this.isMapped) {
                    sendMessage(transactionRequest.createResponse(100, "Trying"));
                }
            } else if (isInviteTransaction() && TransactionState.COMPLETED == getRealState() && transactionRequest.getMethod().equals("ACK")) {
                setState(TransactionState.CONFIRMED);
                disableRetransmissionTimer();
                if (!isReliable()) {
                    enableTimeoutTimer(this.TIMER_I);
                } else {
                    setState(TransactionState.TERMINATED);
                }
                if (this.sipStack.isNon2XXAckPassedToListener()) {
                    this.requestOf.processRequest(transactionRequest, this);
                } else {
                    if (this.sipStack.isLoggingEnabled()) {
                        StackLogger stackLogger3 = this.sipStack.getStackLogger();
                        stackLogger3.logDebug("ACK received for server Tx " + getTransactionId() + " not delivering to application!");
                    }
                    semRelease();
                }
                return;
            } else if (transactionRequest.getMethod().equals(getOriginalRequest().getMethod())) {
                if (TransactionState.PROCEEDING != getRealState()) {
                    if (TransactionState.COMPLETED != getRealState()) {
                        if (transactionRequest.getMethod().equals("ACK")) {
                            if (this.requestOf != null) {
                                this.requestOf.processRequest(transactionRequest, this);
                            } else {
                                semRelease();
                            }
                        }
                        if (this.sipStack.isLoggingEnabled()) {
                            StackLogger stackLogger4 = this.sipStack.getStackLogger();
                            stackLogger4.logDebug("completed processing retransmitted request : " + transactionRequest.getFirstLine() + this + " txState = " + getState() + " lastResponse = " + getLastResponse());
                        }
                        return;
                    }
                }
                semRelease();
                if (this.lastResponse != null) {
                    super.sendMessage(this.lastResponse);
                }
                if (this.sipStack.isLoggingEnabled()) {
                }
                return;
            }
            if (TransactionState.COMPLETED == getRealState() || TransactionState.TERMINATED == getRealState() || this.requestOf == null) {
                getSIPStack();
                if (SIPTransactionStack.isDialogCreated(getOriginalRequest().getMethod()) && getRealState() == TransactionState.TERMINATED && transactionRequest.getMethod().equals("ACK") && this.requestOf != null) {
                    SIPDialog thisDialog = this.dialog;
                    if (thisDialog != null) {
                        if (thisDialog.ackProcessed) {
                            semRelease();
                        }
                    }
                    if (thisDialog != null) {
                        thisDialog.ackReceived(transactionRequest);
                        thisDialog.ackProcessed = true;
                    }
                    this.requestOf.processRequest(transactionRequest, this);
                } else if (transactionRequest.getMethod().equals(Request.CANCEL)) {
                    if (this.sipStack.isLoggingEnabled()) {
                        this.sipStack.getStackLogger().logDebug("Too late to cancel Transaction");
                    }
                    semRelease();
                    try {
                        sendMessage(transactionRequest.createResponse(Response.OK));
                    } catch (IOException e) {
                    }
                }
                if (this.sipStack.isLoggingEnabled()) {
                    StackLogger stackLogger5 = this.sipStack.getStackLogger();
                    stackLogger5.logDebug("Dropping request " + getRealState());
                }
            }
            if (getOriginalRequest().getMethod().equals(transactionRequest.getMethod())) {
                if (toTu) {
                    this.requestOf.processRequest(transactionRequest, this);
                } else {
                    semRelease();
                }
            } else if (this.requestOf != null) {
                this.requestOf.processRequest(transactionRequest, this);
            } else {
                semRelease();
            }
        } catch (IOException e2) {
            if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logError("IOException ", e2);
            }
            semRelease();
            raiseIOExceptionEvent();
        }
    }

    public void sendMessage(SIPMessage messageToSend) throws IOException {
        SIPResponse transactionResponse;
        int statusCode;
        try {
            transactionResponse = (SIPResponse) messageToSend;
            statusCode = transactionResponse.getStatusCode();
            if (getOriginalRequest().getTopmostVia().getBranch() != null) {
                transactionResponse.getTopmostVia().setBranch(getBranch());
            } else {
                transactionResponse.getTopmostVia().removeParameter("branch");
            }
            if (!getOriginalRequest().getTopmostVia().hasPort()) {
                transactionResponse.getTopmostVia().removePort();
            }
        } catch (IOException e) {
            setState(TransactionState.TERMINATED);
            this.collectionTime = 0;
            throw e;
        } catch (ParseException ex) {
            ex.printStackTrace();
        } catch (Throwable th) {
            startTransactionTimer();
            throw th;
        }
        if (!transactionResponse.getCSeq().getMethod().equals(getOriginalRequest().getMethod())) {
            sendResponse(transactionResponse);
            startTransactionTimer();
            return;
        }
        if (getRealState() == TransactionState.TRYING) {
            if (statusCode / 100 == 1) {
                setState(TransactionState.PROCEEDING);
            } else if (200 <= statusCode && statusCode <= 699) {
                if (!isInviteTransaction()) {
                    if (!isReliable()) {
                        setState(TransactionState.COMPLETED);
                        enableTimeoutTimer(64);
                    } else {
                        setState(TransactionState.TERMINATED);
                    }
                } else if (statusCode / 100 == 2) {
                    disableRetransmissionTimer();
                    disableTimeoutTimer();
                    this.collectionTime = 64;
                    setState(TransactionState.TERMINATED);
                    if (this.dialog != null) {
                        this.dialog.setRetransmissionTicks();
                    }
                } else {
                    setState(TransactionState.COMPLETED);
                    if (!isReliable()) {
                        enableRetransmissionTimer();
                    }
                    enableTimeoutTimer(64);
                }
            }
        } else if (getRealState() == TransactionState.PROCEEDING) {
            if (isInviteTransaction()) {
                if (statusCode / 100 == 2) {
                    disableRetransmissionTimer();
                    disableTimeoutTimer();
                    this.collectionTime = 64;
                    setState(TransactionState.TERMINATED);
                    if (this.dialog != null) {
                        this.dialog.setRetransmissionTicks();
                    }
                } else if (300 <= statusCode && statusCode <= 699) {
                    setState(TransactionState.COMPLETED);
                    if (!isReliable()) {
                        enableRetransmissionTimer();
                    }
                    enableTimeoutTimer(64);
                }
            } else if (200 <= statusCode && statusCode <= 699) {
                setState(TransactionState.COMPLETED);
                if (!isReliable()) {
                    disableRetransmissionTimer();
                    enableTimeoutTimer(64);
                } else {
                    setState(TransactionState.TERMINATED);
                }
            }
        } else if (TransactionState.COMPLETED == getRealState()) {
            startTransactionTimer();
            return;
        }
        if (this.sipStack.isLoggingEnabled()) {
            StackLogger stackLogger = this.sipStack.getStackLogger();
            stackLogger.logDebug("sendMessage : tx = " + this + " getState = " + getState());
        }
        this.lastResponse = transactionResponse;
        sendResponse(transactionResponse);
        startTransactionTimer();
    }

    public String getViaHost() {
        return getMessageChannel().getViaHost();
    }

    public int getViaPort() {
        return getMessageChannel().getViaPort();
    }

    /* access modifiers changed from: protected */
    public void fireRetransmissionTimer() {
        try {
            if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logDebug("fireRetransmissionTimer() -- ");
            }
            if (isInviteTransaction() && this.lastResponse != null) {
                if (this.retransmissionAlertEnabled) {
                    if (!this.sipStack.isTransactionPendingAck(this)) {
                        SipProviderImpl sipProvider = getSipProvider();
                        sipProvider.handleEvent(new TimeoutEvent((Object) sipProvider, (ServerTransaction) this, Timeout.RETRANSMIT), this);
                        return;
                    }
                }
                if (this.lastResponse.getStatusCode() / 100 > 2 && !this.isAckSeen) {
                    super.sendMessage(this.lastResponse);
                }
            }
        } catch (IOException e) {
            if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logException(e);
            }
            raiseErrorEvent(2);
        }
    }

    /* access modifiers changed from: private */
    public void fireReliableResponseRetransmissionTimer() {
        try {
            super.sendMessage(this.pendingReliableResponse);
        } catch (IOException e) {
            if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logException(e);
            }
            setState(TransactionState.TERMINATED);
            raiseErrorEvent(2);
        }
    }

    /* access modifiers changed from: protected */
    public void fireTimeoutTimer() {
        if (this.sipStack.isLoggingEnabled()) {
            StackLogger stackLogger = this.sipStack.getStackLogger();
            stackLogger.logDebug("SIPServerTransaction.fireTimeoutTimer this = " + this + " current state = " + getRealState() + " method = " + getOriginalRequest().getMethod());
        }
        if (!getMethod().equals("INVITE") || !this.sipStack.removeTransactionPendingAck(this)) {
            SIPDialog dialog2 = this.dialog;
            getSIPStack();
            if (SIPTransactionStack.isDialogCreated(getOriginalRequest().getMethod()) && (TransactionState.CALLING == getRealState() || TransactionState.TRYING == getRealState())) {
                dialog2.setState(SIPDialog.TERMINATED_STATE);
            } else if (getOriginalRequest().getMethod().equals("BYE") && dialog2 != null && dialog2.isTerminatedOnBye()) {
                dialog2.setState(SIPDialog.TERMINATED_STATE);
            }
            if (TransactionState.COMPLETED == getRealState() && isInviteTransaction()) {
                raiseErrorEvent(1);
                setState(TransactionState.TERMINATED);
                this.sipStack.removeTransaction(this);
            } else if (TransactionState.COMPLETED == getRealState() && !isInviteTransaction()) {
                setState(TransactionState.TERMINATED);
                this.sipStack.removeTransaction(this);
            } else if (TransactionState.CONFIRMED == getRealState() && isInviteTransaction()) {
                setState(TransactionState.TERMINATED);
                this.sipStack.removeTransaction(this);
            } else if (!isInviteTransaction() && (TransactionState.COMPLETED == getRealState() || TransactionState.CONFIRMED == getRealState())) {
                setState(TransactionState.TERMINATED);
            } else if (isInviteTransaction() && TransactionState.TERMINATED == getRealState()) {
                raiseErrorEvent(1);
                if (dialog2 != null) {
                    dialog2.setState(SIPDialog.TERMINATED_STATE);
                }
            }
            return;
        }
        if (this.sipStack.isLoggingEnabled()) {
            this.sipStack.getStackLogger().logDebug("Found tx pending ACK - returning");
        }
    }

    public SIPResponse getLastResponse() {
        return this.lastResponse;
    }

    public void setOriginalRequest(SIPRequest originalRequest) {
        super.setOriginalRequest(originalRequest);
    }

    public void sendResponse(Response response) throws SipException {
        SIPResponse sipResponse = (SIPResponse) response;
        SIPDialog dialog2 = this.dialog;
        if (response != null) {
            try {
                sipResponse.checkHeaders();
                if (sipResponse.getCSeq().getMethod().equals(getMethod())) {
                    if (getMethod().equals("SUBSCRIBE") && response.getStatusCode() / 100 == 2) {
                        if (response.getHeader("Expires") != null) {
                            Expires requestExpires = (Expires) getOriginalRequest().getExpires();
                            Expires responseExpires = (Expires) response.getExpires();
                            if (requestExpires != null && responseExpires.getExpires() > requestExpires.getExpires()) {
                                throw new SipException("Response Expires time exceeds request Expires time : See RFC 3265 3.1.1");
                            }
                        } else {
                            throw new SipException("Expires header is mandatory in 2xx response of SUBSCRIBE");
                        }
                    }
                    if (sipResponse.getStatusCode() == 200 && sipResponse.getCSeq().getMethod().equals("INVITE") && sipResponse.getHeader("Contact") == null) {
                        throw new SipException("Contact Header is mandatory for the OK to the INVITE");
                    } else if (isMessagePartOfTransaction((SIPMessage) response)) {
                        try {
                            if (this.pendingReliableResponse != null && getDialog() != null && getState() != TransactionState.TERMINATED && ((SIPResponse) response).getContentTypeHeader() != null && response.getStatusCode() / 100 == 2 && ((SIPResponse) response).getContentTypeHeader().getContentType().equalsIgnoreCase("application") && ((SIPResponse) response).getContentTypeHeader().getContentSubType().equalsIgnoreCase("sdp")) {
                                try {
                                    if (!this.provisionalResponseSem.tryAcquire(1, TimeUnit.SECONDS)) {
                                        throw new SipException("cannot send response -- unacked povisional");
                                    }
                                } catch (Exception ex) {
                                    this.sipStack.getStackLogger().logError("Could not acquire PRACK sem ", ex);
                                }
                            } else if (this.pendingReliableResponse != null && sipResponse.isFinalResponse()) {
                                this.provisionalResponseTask.cancel();
                                this.provisionalResponseTask = null;
                            }
                            if (dialog2 != null) {
                                if (sipResponse.getStatusCode() / 100 == 2) {
                                    SIPTransactionStack sIPTransactionStack = this.sipStack;
                                    if (SIPTransactionStack.isDialogCreated(sipResponse.getCSeq().getMethod())) {
                                        if (dialog2.getLocalTag() == null && sipResponse.getTo().getTag() == null) {
                                            sipResponse.getTo().setTag(Utils.getInstance().generateTag());
                                        } else if (dialog2.getLocalTag() != null && sipResponse.getToTag() == null) {
                                            sipResponse.setToTag(dialog2.getLocalTag());
                                        } else if (!(dialog2.getLocalTag() == null || sipResponse.getToTag() == null)) {
                                            if (!dialog2.getLocalTag().equals(sipResponse.getToTag())) {
                                                throw new SipException("Tag mismatch dialogTag is " + dialog2.getLocalTag() + " responseTag is " + sipResponse.getToTag());
                                            }
                                        }
                                    }
                                }
                                if (!sipResponse.getCallId().getCallId().equals(dialog2.getCallId().getCallId())) {
                                    throw new SipException("Dialog mismatch!");
                                }
                            }
                            String fromTag = ((SIPRequest) getRequest()).getFrom().getTag();
                            if (!(fromTag == null || sipResponse.getFromTag() == null)) {
                                if (!sipResponse.getFromTag().equals(fromTag)) {
                                    throw new SipException("From tag of request does not match response from tag");
                                }
                            }
                            if (fromTag != null) {
                                sipResponse.getFrom().setTag(fromTag);
                            } else if (this.sipStack.isLoggingEnabled()) {
                                this.sipStack.getStackLogger().logDebug("WARNING -- Null From tag in request!!");
                            }
                            if (dialog2 != null && response.getStatusCode() != 100) {
                                dialog2.setResponseTags(sipResponse);
                                DialogState oldState = dialog2.getState();
                                dialog2.setLastResponse(this, (SIPResponse) response);
                                if (oldState == null && dialog2.getState() == DialogState.TERMINATED) {
                                    dialog2.getSipProvider().handleEvent(new DialogTerminatedEvent(dialog2.getSipProvider(), dialog2), this);
                                }
                            } else if (dialog2 == null && getMethod().equals("INVITE") && this.retransmissionAlertEnabled && this.retransmissionAlertTimerTask == null && response.getStatusCode() / 100 == 2) {
                                String dialogId = ((SIPResponse) response).getDialogId(true);
                                this.retransmissionAlertTimerTask = new RetransmissionAlertTimerTask(dialogId);
                                this.sipStack.retransmissionAlertTransactions.put(dialogId, this);
                                this.sipStack.getTimer().schedule(this.retransmissionAlertTimerTask, 0, 500);
                            }
                            sendMessage((SIPResponse) response);
                            if (dialog2 != null) {
                                dialog2.startRetransmitTimer(this, (SIPResponse) response);
                            }
                        } catch (IOException ex2) {
                            if (this.sipStack.isLoggingEnabled()) {
                                this.sipStack.getStackLogger().logException(ex2);
                            }
                            setState(TransactionState.TERMINATED);
                            raiseErrorEvent(2);
                            throw new SipException(ex2.getMessage());
                        } catch (ParseException ex1) {
                            if (this.sipStack.isLoggingEnabled()) {
                                this.sipStack.getStackLogger().logException(ex1);
                            }
                            setState(TransactionState.TERMINATED);
                            throw new SipException(ex1.getMessage());
                        }
                    } else {
                        throw new SipException("Response does not belong to this transaction.");
                    }
                } else {
                    throw new SipException("CSeq method does not match Request method of request that created the tx.");
                }
            } catch (ParseException ex3) {
                throw new SipException(ex3.getMessage());
            }
        } else {
            throw new NullPointerException("null response");
        }
    }

    /* access modifiers changed from: private */
    public TransactionState getRealState() {
        return super.getState();
    }

    public TransactionState getState() {
        if (!isInviteTransaction() || TransactionState.TRYING != super.getState()) {
            return super.getState();
        }
        return TransactionState.PROCEEDING;
    }

    public void setState(TransactionState newState) {
        if (newState == TransactionState.TERMINATED && isReliable() && !getSIPStack().cacheServerConnections) {
            this.collectionTime = 64;
        }
        super.setState(newState);
    }

    /* access modifiers changed from: protected */
    public void startTransactionTimer() {
        if (this.transactionTimerStarted.compareAndSet(false, true) && this.sipStack.getTimer() != null) {
            this.sipStack.getTimer().schedule(new TransactionTimer(), (long) this.BASE_TIMER_INTERVAL, (long) this.BASE_TIMER_INTERVAL);
        }
    }

    public boolean equals(Object other) {
        if (!other.getClass().equals(getClass())) {
            return false;
        }
        return getBranch().equalsIgnoreCase(((SIPServerTransaction) other).getBranch());
    }

    public Dialog getDialog() {
        return this.dialog;
    }

    public void setDialog(SIPDialog sipDialog, String dialogId) {
        if (this.sipStack.isLoggingEnabled()) {
            StackLogger stackLogger = this.sipStack.getStackLogger();
            stackLogger.logDebug("setDialog " + this + " dialog = " + sipDialog);
        }
        this.dialog = sipDialog;
        if (dialogId != null) {
            this.dialog.setAssigned();
        }
        if (this.retransmissionAlertEnabled && this.retransmissionAlertTimerTask != null) {
            this.retransmissionAlertTimerTask.cancel();
            if (this.retransmissionAlertTimerTask.dialogId != null) {
                this.sipStack.retransmissionAlertTransactions.remove(this.retransmissionAlertTimerTask.dialogId);
            }
            this.retransmissionAlertTimerTask = null;
        }
        this.retransmissionAlertEnabled = false;
    }

    public void terminate() throws ObjectInUseException {
        setState(TransactionState.TERMINATED);
        if (this.retransmissionAlertTimerTask != null) {
            this.retransmissionAlertTimerTask.cancel();
            if (this.retransmissionAlertTimerTask.dialogId != null) {
                this.sipStack.retransmissionAlertTransactions.remove(this.retransmissionAlertTimerTask.dialogId);
            }
            this.retransmissionAlertTimerTask = null;
        }
    }

    /* access modifiers changed from: protected */
    public void sendReliableProvisionalResponse(Response relResponse) throws SipException {
        if (this.pendingReliableResponse == null) {
            this.pendingReliableResponse = (SIPResponse) relResponse;
            RSeq rseq = (RSeq) relResponse.getHeader("RSeq");
            if (relResponse.getHeader("RSeq") == null) {
                rseq = new RSeq();
                relResponse.setHeader(rseq);
            }
            try {
                this.rseqNumber++;
                rseq.setSeqNumber((long) this.rseqNumber);
                this.lastResponse = (SIPResponse) relResponse;
                if (getDialog() != null) {
                    if (!this.provisionalResponseSem.tryAcquire(1, TimeUnit.SECONDS)) {
                        throw new SipException("Unacknowledged response");
                    }
                }
                sendMessage((SIPMessage) relResponse);
                this.provisionalResponseTask = new ProvisionalResponseTask();
                this.sipStack.getTimer().schedule(this.provisionalResponseTask, 0, 500);
            } catch (Exception ex) {
                InternalErrorHandler.handleException(ex);
            }
        } else {
            throw new SipException("Unacknowledged response");
        }
    }

    public SIPResponse getReliableProvisionalResponse() {
        return this.pendingReliableResponse;
    }

    public boolean prackRecieved() {
        if (this.pendingReliableResponse == null) {
            return false;
        }
        if (this.provisionalResponseTask != null) {
            this.provisionalResponseTask.cancel();
        }
        this.pendingReliableResponse = null;
        this.provisionalResponseSem.release();
        return true;
    }

    public void enableRetransmissionAlerts() throws SipException {
        if (getDialog() != null) {
            throw new SipException("Dialog associated with tx");
        } else if (getMethod().equals("INVITE")) {
            this.retransmissionAlertEnabled = true;
        } else {
            throw new SipException("Request Method must be INVITE");
        }
    }

    public boolean isRetransmissionAlertEnabled() {
        return this.retransmissionAlertEnabled;
    }

    public void disableRetransmissionAlerts() {
        if (this.retransmissionAlertTimerTask != null && this.retransmissionAlertEnabled) {
            this.retransmissionAlertTimerTask.cancel();
            this.retransmissionAlertEnabled = false;
            String dialogId = this.retransmissionAlertTimerTask.dialogId;
            if (dialogId != null) {
                this.sipStack.retransmissionAlertTransactions.remove(dialogId);
            }
            this.retransmissionAlertTimerTask = null;
        }
    }

    public void setAckSeen() {
        this.isAckSeen = true;
    }

    public boolean ackSeen() {
        return this.isAckSeen;
    }

    public void setMapped(boolean b) {
        this.isMapped = true;
    }

    public void setPendingSubscribe(SIPClientTransaction pendingSubscribeClientTx) {
        this.pendingSubscribeTransaction = pendingSubscribeClientTx;
    }

    public void releaseSem() {
        if (this.pendingSubscribeTransaction != null) {
            this.pendingSubscribeTransaction.releaseSem();
        } else if (this.inviteTransaction != null && getMethod().equals(Request.CANCEL)) {
            this.inviteTransaction.releaseSem();
        }
        super.releaseSem();
    }

    public void setInviteTransaction(SIPServerTransaction st) {
        this.inviteTransaction = st;
    }

    public SIPServerTransaction getCanceledInviteTransaction() {
        return this.inviteTransaction;
    }

    public void scheduleAckRemoval() throws IllegalStateException {
        if (getMethod() == null || !getMethod().equals("ACK")) {
            StringBuilder sb = new StringBuilder();
            sb.append("Method is null[");
            sb.append(getMethod() == null);
            sb.append("] or method is not ACK[");
            sb.append(getMethod());
            sb.append("]");
            throw new IllegalStateException(sb.toString());
        }
        startTransactionTimer();
    }
}
