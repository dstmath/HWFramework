package gov.nist.javax.sip.stack;

import gov.nist.core.InternalErrorHandler;
import gov.nist.core.Separators;
import gov.nist.javax.sip.SIPConstants;
import gov.nist.javax.sip.SipProviderImpl;
import gov.nist.javax.sip.TransactionExt;
import gov.nist.javax.sip.header.CallID;
import gov.nist.javax.sip.header.Event;
import gov.nist.javax.sip.header.From;
import gov.nist.javax.sip.header.To;
import gov.nist.javax.sip.header.Via;
import gov.nist.javax.sip.header.ViaList;
import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.message.SIPResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.security.cert.Certificate;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.sip.Dialog;
import javax.sip.IOExceptionEvent;
import javax.sip.ServerTransaction;
import javax.sip.Transaction;
import javax.sip.TransactionState;
import javax.sip.message.Request;
import javax.sip.message.Response;

public abstract class SIPTransaction extends MessageChannel implements Transaction, TransactionExt {
    public static final TransactionState CALLING_STATE = TransactionState.CALLING;
    public static final TransactionState COMPLETED_STATE = TransactionState.COMPLETED;
    public static final TransactionState CONFIRMED_STATE = TransactionState.CONFIRMED;
    public static final TransactionState INITIAL_STATE = null;
    protected static final int MAXIMUM_RETRANSMISSION_TICK_COUNT = 8;
    public static final TransactionState PROCEEDING_STATE = TransactionState.PROCEEDING;
    protected static final int T1 = 1;
    public static final TransactionState TERMINATED_STATE = TransactionState.TERMINATED;
    protected static final int TIMER_A = 1;
    protected static final int TIMER_B = 64;
    protected static final int TIMER_F = 64;
    protected static final int TIMER_H = 64;
    protected static final int TIMER_J = 64;
    public static final TransactionState TRYING_STATE = TransactionState.TRYING;
    protected int BASE_TIMER_INTERVAL = 500;
    protected int T2 = (4000 / this.BASE_TIMER_INTERVAL);
    protected int T4 = (5000 / this.BASE_TIMER_INTERVAL);
    protected int TIMER_D = (32000 / this.BASE_TIMER_INTERVAL);
    protected int TIMER_I = this.T4;
    protected int TIMER_K = this.T4;
    protected transient Object applicationData;
    public long auditTag = 0;
    private String branch;
    private long cSeq;
    protected CallID callId;
    protected int collectionTime;
    private TransactionState currentState;
    private transient MessageChannel encapsulatedChannel;
    protected Event event;
    private transient Set<SIPTransactionEventListener> eventListeners;
    protected From from;
    protected String fromTag;
    protected boolean isMapped;
    protected boolean isSemaphoreAquired;
    protected SIPResponse lastResponse;
    private String method;
    protected SIPRequest originalRequest;
    protected String peerAddress;
    protected InetAddress peerInetAddress;
    protected InetAddress peerPacketSourceAddress;
    protected int peerPacketSourcePort;
    protected int peerPort;
    protected String peerProtocol;
    private transient int retransmissionTimerLastTickCount;
    private transient int retransmissionTimerTicksLeft;
    private Semaphore semaphore;
    protected transient SIPTransactionStack sipStack;
    private boolean terminatedEventDelivered;
    protected int timeoutTimerTicksLeft;
    protected To to;
    protected boolean toListener;
    protected String toTag;
    protected String transactionId;
    protected AtomicBoolean transactionTimerStarted = new AtomicBoolean(false);

    class LingerTimer extends SIPStackTimerTask {
        public LingerTimer() {
            SIPTransaction sipTransaction = SIPTransaction.this;
            if (SIPTransaction.this.sipStack.isLoggingEnabled()) {
                SIPTransaction.this.sipStack.getStackLogger().logDebug("LingerTimer : " + SIPTransaction.this.getTransactionId());
            }
        }

        protected void runTask() {
            SIPTransaction transaction = SIPTransaction.this;
            SIPTransactionStack sipStack = transaction.getSIPStack();
            if (sipStack.isLoggingEnabled()) {
                sipStack.getStackLogger().logDebug("LingerTimer: run() : " + SIPTransaction.this.getTransactionId());
            }
            if (transaction instanceof SIPClientTransaction) {
                sipStack.removeTransaction(transaction);
                transaction.close();
            } else if (transaction instanceof ServerTransaction) {
                if (sipStack.isLoggingEnabled()) {
                    sipStack.getStackLogger().logDebug("removing" + transaction);
                }
                sipStack.removeTransaction(transaction);
                if (!sipStack.cacheServerConnections) {
                    MessageChannel -get0 = transaction.encapsulatedChannel;
                    int i = -get0.useCount - 1;
                    -get0.useCount = i;
                    if (i <= 0) {
                        transaction.close();
                        return;
                    }
                }
                if (sipStack.isLoggingEnabled() && (sipStack.cacheServerConnections ^ 1) != 0 && transaction.isReliable()) {
                    sipStack.getStackLogger().logDebug("Use Count = " + transaction.encapsulatedChannel.useCount);
                }
            }
        }
    }

    protected abstract void fireRetransmissionTimer();

    protected abstract void fireTimeoutTimer();

    public abstract Dialog getDialog();

    public abstract boolean isMessagePartOfTransaction(SIPMessage sIPMessage);

    public abstract void setDialog(SIPDialog sIPDialog, String str);

    protected abstract void startTransactionTimer();

    public String getBranchId() {
        return this.branch;
    }

    protected SIPTransaction(SIPTransactionStack newParentStack, MessageChannel newEncapsulatedChannel) {
        this.sipStack = newParentStack;
        this.semaphore = new Semaphore(1, true);
        this.encapsulatedChannel = newEncapsulatedChannel;
        this.peerPort = newEncapsulatedChannel.getPeerPort();
        this.peerAddress = newEncapsulatedChannel.getPeerAddress();
        this.peerInetAddress = newEncapsulatedChannel.getPeerInetAddress();
        this.peerPacketSourcePort = newEncapsulatedChannel.getPeerPacketSourcePort();
        this.peerPacketSourceAddress = newEncapsulatedChannel.getPeerPacketSourceAddress();
        this.peerProtocol = newEncapsulatedChannel.getPeerProtocol();
        if (isReliable()) {
            MessageChannel messageChannel = this.encapsulatedChannel;
            messageChannel.useCount++;
            if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logDebug("use count for encapsulated channel" + this + Separators.SP + this.encapsulatedChannel.useCount);
            }
        }
        this.currentState = null;
        disableRetransmissionTimer();
        disableTimeoutTimer();
        this.eventListeners = Collections.synchronizedSet(new HashSet());
        addEventListener(newParentStack);
    }

    public void setOriginalRequest(SIPRequest newOriginalRequest) {
        if (!(this.originalRequest == null || (this.originalRequest.getTransactionId().equals(newOriginalRequest.getTransactionId()) ^ 1) == 0)) {
            this.sipStack.removeTransactionHash(this);
        }
        this.originalRequest = newOriginalRequest;
        this.method = newOriginalRequest.getMethod();
        this.from = (From) newOriginalRequest.getFrom();
        this.to = (To) newOriginalRequest.getTo();
        this.toTag = this.to.getTag();
        this.fromTag = this.from.getTag();
        this.callId = (CallID) newOriginalRequest.getCallId();
        this.cSeq = newOriginalRequest.getCSeq().getSeqNumber();
        this.event = (Event) newOriginalRequest.getHeader("Event");
        this.transactionId = newOriginalRequest.getTransactionId();
        this.originalRequest.setTransaction(this);
        String newBranch = ((Via) newOriginalRequest.getViaHeaders().getFirst()).getBranch();
        if (newBranch != null) {
            if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logDebug("Setting Branch id : " + newBranch);
            }
            setBranch(newBranch);
            return;
        }
        if (this.sipStack.isLoggingEnabled()) {
            this.sipStack.getStackLogger().logDebug("Branch id is null - compute TID!" + newOriginalRequest.encode());
        }
        setBranch(newOriginalRequest.getTransactionId());
    }

    public SIPRequest getOriginalRequest() {
        return this.originalRequest;
    }

    public Request getRequest() {
        return this.originalRequest;
    }

    public final boolean isInviteTransaction() {
        return getMethod().equals("INVITE");
    }

    public final boolean isCancelTransaction() {
        return getMethod().equals(Request.CANCEL);
    }

    public final boolean isByeTransaction() {
        return getMethod().equals("BYE");
    }

    public MessageChannel getMessageChannel() {
        return this.encapsulatedChannel;
    }

    public final void setBranch(String newBranch) {
        this.branch = newBranch;
    }

    public final String getBranch() {
        if (this.branch == null) {
            this.branch = getOriginalRequest().getTopmostVia().getBranch();
        }
        return this.branch;
    }

    public final String getMethod() {
        return this.method;
    }

    public final long getCSeq() {
        return this.cSeq;
    }

    public void setState(TransactionState newState) {
        Object newState2;
        if (!(this.currentState != TransactionState.COMPLETED || newState2 == TransactionState.TERMINATED || newState2 == TransactionState.CONFIRMED)) {
            newState2 = TransactionState.COMPLETED;
        }
        if (this.currentState == TransactionState.CONFIRMED && newState != TransactionState.TERMINATED) {
            newState2 = TransactionState.CONFIRMED;
        }
        if (this.currentState != TransactionState.TERMINATED) {
            this.currentState = newState2;
        } else {
            newState2 = this.currentState;
        }
        if (this.sipStack.isLoggingEnabled()) {
            this.sipStack.getStackLogger().logDebug("Transaction:setState " + newState2 + Separators.SP + this + " branchID = " + getBranch() + " isClient = " + (this instanceof SIPClientTransaction));
            this.sipStack.getStackLogger().logStackTrace();
        }
    }

    public TransactionState getState() {
        return this.currentState;
    }

    protected final void enableRetransmissionTimer() {
        enableRetransmissionTimer(1);
    }

    protected final void enableRetransmissionTimer(int tickCount) {
        if (isInviteTransaction() && (this instanceof SIPClientTransaction)) {
            this.retransmissionTimerTicksLeft = tickCount;
        } else {
            this.retransmissionTimerTicksLeft = Math.min(tickCount, 8);
        }
        this.retransmissionTimerLastTickCount = this.retransmissionTimerTicksLeft;
    }

    protected final void disableRetransmissionTimer() {
        this.retransmissionTimerTicksLeft = -1;
    }

    protected final void enableTimeoutTimer(int tickCount) {
        if (this.sipStack.isLoggingEnabled()) {
            this.sipStack.getStackLogger().logDebug("enableTimeoutTimer " + this + " tickCount " + tickCount + " currentTickCount = " + this.timeoutTimerTicksLeft);
        }
        this.timeoutTimerTicksLeft = tickCount;
    }

    protected final void disableTimeoutTimer() {
        this.timeoutTimerTicksLeft = -1;
    }

    final void fireTimer() {
        int i;
        if (this.timeoutTimerTicksLeft != -1) {
            i = this.timeoutTimerTicksLeft - 1;
            this.timeoutTimerTicksLeft = i;
            if (i == 0) {
                fireTimeoutTimer();
            }
        }
        if (this.retransmissionTimerTicksLeft != -1) {
            i = this.retransmissionTimerTicksLeft - 1;
            this.retransmissionTimerTicksLeft = i;
            if (i == 0) {
                enableRetransmissionTimer(this.retransmissionTimerLastTickCount * 2);
                fireRetransmissionTimer();
            }
        }
    }

    public final boolean isTerminated() {
        return getState() == TERMINATED_STATE;
    }

    public String getHost() {
        return this.encapsulatedChannel.getHost();
    }

    public String getKey() {
        return this.encapsulatedChannel.getKey();
    }

    public int getPort() {
        return this.encapsulatedChannel.getPort();
    }

    public SIPTransactionStack getSIPStack() {
        return this.sipStack;
    }

    public String getPeerAddress() {
        return this.peerAddress;
    }

    public int getPeerPort() {
        return this.peerPort;
    }

    public int getPeerPacketSourcePort() {
        return this.peerPacketSourcePort;
    }

    public InetAddress getPeerPacketSourceAddress() {
        return this.peerPacketSourceAddress;
    }

    protected InetAddress getPeerInetAddress() {
        return this.peerInetAddress;
    }

    protected String getPeerProtocol() {
        return this.peerProtocol;
    }

    public String getTransport() {
        return this.encapsulatedChannel.getTransport();
    }

    public boolean isReliable() {
        return this.encapsulatedChannel.isReliable();
    }

    public Via getViaHeader() {
        Via channelViaHeader = super.getViaHeader();
        try {
            channelViaHeader.setBranch(this.branch);
        } catch (ParseException e) {
        }
        return channelViaHeader;
    }

    public void sendMessage(SIPMessage messageToSend) throws IOException {
        try {
            this.encapsulatedChannel.sendMessage(messageToSend, this.peerInetAddress, this.peerPort);
        } finally {
            startTransactionTimer();
        }
    }

    protected void sendMessage(byte[] messageBytes, InetAddress receiverAddress, int receiverPort, boolean retry) throws IOException {
        throw new IOException("Cannot send unparsed message through Transaction Channel!");
    }

    public void addEventListener(SIPTransactionEventListener newListener) {
        this.eventListeners.add(newListener);
    }

    public void removeEventListener(SIPTransactionEventListener oldListener) {
        this.eventListeners.remove(oldListener);
    }

    protected void raiseErrorEvent(int errorEventID) {
        SIPTransactionErrorEvent newErrorEvent = new SIPTransactionErrorEvent(this, errorEventID);
        synchronized (this.eventListeners) {
            for (SIPTransactionEventListener nextListener : this.eventListeners) {
                nextListener.transactionErrorEvent(newErrorEvent);
            }
        }
        if (errorEventID != 3) {
            this.eventListeners.clear();
            setState(TransactionState.TERMINATED);
            if ((this instanceof SIPServerTransaction) && isByeTransaction() && getDialog() != null) {
                ((SIPDialog) getDialog()).setState(SIPDialog.TERMINATED_STATE);
            }
        }
    }

    protected boolean isServerTransaction() {
        return this instanceof SIPServerTransaction;
    }

    public int getRetransmitTimer() {
        return 500;
    }

    public String getViaHost() {
        return getViaHeader().getHost();
    }

    public SIPResponse getLastResponse() {
        return this.lastResponse;
    }

    public Response getResponse() {
        return this.lastResponse;
    }

    public String getTransactionId() {
        return this.transactionId;
    }

    public int hashCode() {
        if (this.transactionId == null) {
            return -1;
        }
        return this.transactionId.hashCode();
    }

    public int getViaPort() {
        return getViaHeader().getPort();
    }

    public boolean doesCancelMatchTransaction(SIPRequest requestToTest) {
        boolean transactionMatches = false;
        if (getOriginalRequest() == null || getOriginalRequest().getMethod().equals(Request.CANCEL)) {
            return false;
        }
        ViaList viaHeaders = requestToTest.getViaHeaders();
        if (viaHeaders != null) {
            Via topViaHeader = (Via) viaHeaders.getFirst();
            String messageBranch = topViaHeader.getBranch();
            if (!(messageBranch == null || messageBranch.toLowerCase().startsWith(SIPConstants.BRANCH_MAGIC_COOKIE_LOWER_CASE))) {
                messageBranch = null;
            }
            if (messageBranch == null || getBranch() == null) {
                if (this.sipStack.isLoggingEnabled()) {
                    this.sipStack.getStackLogger().logDebug("testing against " + getOriginalRequest());
                }
                if (getOriginalRequest().getRequestURI().equals(requestToTest.getRequestURI()) && getOriginalRequest().getTo().equals(requestToTest.getTo()) && getOriginalRequest().getFrom().equals(requestToTest.getFrom()) && getOriginalRequest().getCallId().getCallId().equals(requestToTest.getCallId().getCallId()) && getOriginalRequest().getCSeq().getSeqNumber() == requestToTest.getCSeq().getSeqNumber() && topViaHeader.equals(getOriginalRequest().getViaHeaders().getFirst())) {
                    transactionMatches = true;
                }
            } else if (getBranch().equalsIgnoreCase(messageBranch) && topViaHeader.getSentBy().equals(((Via) getOriginalRequest().getViaHeaders().getFirst()).getSentBy())) {
                transactionMatches = true;
                if (this.sipStack.isLoggingEnabled()) {
                    this.sipStack.getStackLogger().logDebug("returning  true");
                }
            }
        }
        if (transactionMatches) {
            setPassToListener();
        }
        return transactionMatches;
    }

    public void setRetransmitTimer(int retransmitTimer) {
        if (retransmitTimer <= 0) {
            throw new IllegalArgumentException("Retransmit timer must be positive!");
        } else if (this.transactionTimerStarted.get()) {
            throw new IllegalStateException("Transaction timer is already started");
        } else {
            this.BASE_TIMER_INTERVAL = retransmitTimer;
            this.T4 = 5000 / this.BASE_TIMER_INTERVAL;
            this.T2 = 4000 / this.BASE_TIMER_INTERVAL;
            this.TIMER_I = this.T4;
            this.TIMER_K = this.T4;
            this.TIMER_D = 32000 / this.BASE_TIMER_INTERVAL;
        }
    }

    public void close() {
        this.encapsulatedChannel.close();
        if (this.sipStack.isLoggingEnabled()) {
            this.sipStack.getStackLogger().logDebug("Closing " + this.encapsulatedChannel);
        }
    }

    public boolean isSecure() {
        return this.encapsulatedChannel.isSecure();
    }

    public MessageProcessor getMessageProcessor() {
        return this.encapsulatedChannel.getMessageProcessor();
    }

    public void setApplicationData(Object applicationData) {
        this.applicationData = applicationData;
    }

    public Object getApplicationData() {
        return this.applicationData;
    }

    public void setEncapsulatedChannel(MessageChannel messageChannel) {
        this.encapsulatedChannel = messageChannel;
        this.peerInetAddress = messageChannel.getPeerInetAddress();
        this.peerPort = messageChannel.getPeerPort();
    }

    public SipProviderImpl getSipProvider() {
        return getMessageProcessor().getListeningPoint().getProvider();
    }

    public void raiseIOExceptionEvent() {
        setState(TransactionState.TERMINATED);
        getSipProvider().handleEvent(new IOExceptionEvent(this, getPeerAddress(), getPeerPort(), getTransport()), this);
    }

    public boolean acquireSem() {
        try {
            if (this.sipStack.getStackLogger().isLoggingEnabled()) {
                this.sipStack.getStackLogger().logDebug("acquireSem [[[[" + this);
                this.sipStack.getStackLogger().logStackTrace();
            }
            boolean retval = this.semaphore.tryAcquire(1000, TimeUnit.MILLISECONDS);
            if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logDebug("acquireSem() returning : " + retval);
            }
            this.isSemaphoreAquired = retval;
            return retval;
        } catch (Exception ex) {
            this.sipStack.getStackLogger().logError("Unexpected exception acquiring sem", ex);
            InternalErrorHandler.handleException(ex);
            this.isSemaphoreAquired = false;
            return false;
        } catch (Throwable th) {
            this.isSemaphoreAquired = false;
            throw th;
        }
    }

    public void releaseSem() {
        try {
            this.toListener = false;
            semRelease();
        } catch (Exception ex) {
            this.sipStack.getStackLogger().logError("Unexpected exception releasing sem", ex);
        }
    }

    protected void semRelease() {
        try {
            if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logDebug("semRelease ]]]]" + this);
                this.sipStack.getStackLogger().logStackTrace();
            }
            this.isSemaphoreAquired = false;
            this.semaphore.release();
        } catch (Exception ex) {
            this.sipStack.getStackLogger().logError("Unexpected exception releasing sem", ex);
        }
    }

    public boolean passToListener() {
        return this.toListener;
    }

    public void setPassToListener() {
        if (this.sipStack.isLoggingEnabled()) {
            this.sipStack.getStackLogger().logDebug("setPassToListener()");
        }
        this.toListener = true;
    }

    protected synchronized boolean testAndSetTransactionTerminatedEvent() {
        boolean retval;
        retval = this.terminatedEventDelivered ^ 1;
        this.terminatedEventDelivered = true;
        return retval;
    }

    public String getCipherSuite() throws UnsupportedOperationException {
        if (!(getMessageChannel() instanceof TLSMessageChannel)) {
            throw new UnsupportedOperationException("Not a TLS channel");
        } else if (((TLSMessageChannel) getMessageChannel()).getHandshakeCompletedListener() == null || ((TLSMessageChannel) getMessageChannel()).getHandshakeCompletedListener().getHandshakeCompletedEvent() == null) {
            return null;
        } else {
            return ((TLSMessageChannel) getMessageChannel()).getHandshakeCompletedListener().getHandshakeCompletedEvent().getCipherSuite();
        }
    }

    public Certificate[] getLocalCertificates() throws UnsupportedOperationException {
        if (!(getMessageChannel() instanceof TLSMessageChannel)) {
            throw new UnsupportedOperationException("Not a TLS channel");
        } else if (((TLSMessageChannel) getMessageChannel()).getHandshakeCompletedListener() == null || ((TLSMessageChannel) getMessageChannel()).getHandshakeCompletedListener().getHandshakeCompletedEvent() == null) {
            return null;
        } else {
            return ((TLSMessageChannel) getMessageChannel()).getHandshakeCompletedListener().getHandshakeCompletedEvent().getLocalCertificates();
        }
    }

    public Certificate[] getPeerCertificates() throws SSLPeerUnverifiedException {
        if (!(getMessageChannel() instanceof TLSMessageChannel)) {
            throw new UnsupportedOperationException("Not a TLS channel");
        } else if (((TLSMessageChannel) getMessageChannel()).getHandshakeCompletedListener() == null || ((TLSMessageChannel) getMessageChannel()).getHandshakeCompletedListener().getHandshakeCompletedEvent() == null) {
            return null;
        } else {
            return ((TLSMessageChannel) getMessageChannel()).getHandshakeCompletedListener().getHandshakeCompletedEvent().getPeerCertificates();
        }
    }
}
