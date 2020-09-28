package gov.nist.javax.sip.stack;

import gov.nist.core.InternalErrorHandler;
import gov.nist.core.NameValueList;
import gov.nist.core.StackLogger;
import gov.nist.javax.sip.ClientTransactionExt;
import gov.nist.javax.sip.SIPConstants;
import gov.nist.javax.sip.Utils;
import gov.nist.javax.sip.address.AddressImpl;
import gov.nist.javax.sip.header.Contact;
import gov.nist.javax.sip.header.RecordRoute;
import gov.nist.javax.sip.header.RecordRouteList;
import gov.nist.javax.sip.header.Route;
import gov.nist.javax.sip.header.RouteList;
import gov.nist.javax.sip.header.SIPHeader;
import gov.nist.javax.sip.header.TimeStamp;
import gov.nist.javax.sip.header.To;
import gov.nist.javax.sip.header.Via;
import gov.nist.javax.sip.header.ViaList;
import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.message.SIPResponse;
import gov.nist.javax.sip.stack.SIPTransaction;
import java.io.IOException;
import java.text.ParseException;
import java.util.ListIterator;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.DialogState;
import javax.sip.InvalidArgumentException;
import javax.sip.ObjectInUseException;
import javax.sip.SipException;
import javax.sip.Timeout;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionState;
import javax.sip.address.Hop;
import javax.sip.address.SipURI;
import javax.sip.address.URI;
import javax.sip.message.Request;

public class SIPClientTransaction extends SIPTransaction implements ServerResponseInterface, ClientTransaction, ClientTransactionExt {
    private int callingStateTimeoutCount;
    private SIPDialog defaultDialog;
    private SIPRequest lastRequest;
    private Hop nextHop;
    private boolean notifyOnRetransmit = false;
    private transient ServerResponseInterface respondTo;
    private ConcurrentHashMap<String, SIPDialog> sipDialogs;
    private boolean timeoutIfStillInCallingState = false;
    private String viaHost;
    private int viaPort;

    public class TransactionTimer extends SIPStackTimerTask {
        public TransactionTimer() {
        }

        /* access modifiers changed from: protected */
        @Override // gov.nist.javax.sip.stack.SIPStackTimerTask
        public void runTask() {
            SIPClientTransaction clientTransaction = SIPClientTransaction.this;
            SIPTransactionStack sipStack = clientTransaction.sipStack;
            if (clientTransaction.isTerminated()) {
                if (sipStack.isLoggingEnabled()) {
                    sipStack.getStackLogger().logDebug("removing  = " + clientTransaction + " isReliable " + clientTransaction.isReliable());
                }
                sipStack.removeTransaction(clientTransaction);
                try {
                    cancel();
                } catch (IllegalStateException e) {
                    if (!sipStack.isAlive()) {
                        return;
                    }
                }
                if (!sipStack.cacheClientConnections && clientTransaction.isReliable()) {
                    MessageChannel messageChannel = clientTransaction.getMessageChannel();
                    int newUseCount = messageChannel.useCount - 1;
                    messageChannel.useCount = newUseCount;
                    if (newUseCount <= 0) {
                        sipStack.getTimer().schedule(new SIPTransaction.LingerTimer(), 8000);
                    }
                } else if (sipStack.isLoggingEnabled() && clientTransaction.isReliable()) {
                    int useCount = clientTransaction.getMessageChannel().useCount;
                    if (sipStack.isLoggingEnabled()) {
                        sipStack.getStackLogger().logDebug("Client Use Count = " + useCount);
                    }
                }
            } else {
                clientTransaction.fireTimer();
            }
        }
    }

    protected SIPClientTransaction(SIPTransactionStack newSIPStack, MessageChannel newChannelToUse) {
        super(newSIPStack, newChannelToUse);
        setBranch(Utils.getInstance().generateBranchId());
        this.messageProcessor = newChannelToUse.messageProcessor;
        setEncapsulatedChannel(newChannelToUse);
        if (this.sipStack.isLoggingEnabled()) {
            StackLogger stackLogger = this.sipStack.getStackLogger();
            stackLogger.logDebug("Creating clientTransaction " + this);
            this.sipStack.getStackLogger().logStackTrace();
        }
        this.sipDialogs = new ConcurrentHashMap<>();
    }

    public void setResponseInterface(ServerResponseInterface newRespondTo) {
        if (this.sipStack.isLoggingEnabled()) {
            StackLogger stackLogger = this.sipStack.getStackLogger();
            stackLogger.logDebug("Setting response interface for " + this + " to " + newRespondTo);
            if (newRespondTo == null) {
                this.sipStack.getStackLogger().logStackTrace();
                this.sipStack.getStackLogger().logDebug("WARNING -- setting to null!");
            }
        }
        this.respondTo = newRespondTo;
    }

    public MessageChannel getRequestChannel() {
        return this;
    }

    @Override // gov.nist.javax.sip.stack.SIPTransaction
    public boolean isMessagePartOfTransaction(SIPMessage messageToTest) {
        ViaList viaHeaders = messageToTest.getViaHeaders();
        String messageBranch = ((Via) viaHeaders.getFirst()).getBranch();
        boolean transactionMatches = true;
        boolean rfc3261Compliant = getBranch() != null && messageBranch != null && getBranch().toLowerCase().startsWith(SIPConstants.BRANCH_MAGIC_COOKIE_LOWER_CASE) && messageBranch.toLowerCase().startsWith(SIPConstants.BRANCH_MAGIC_COOKIE_LOWER_CASE);
        if (TransactionState.COMPLETED == getState()) {
            if (!rfc3261Compliant) {
                return getBranch().equals(messageToTest.getTransactionId());
            }
            if (!getBranch().equalsIgnoreCase(((Via) viaHeaders.getFirst()).getBranch()) || !getMethod().equals(messageToTest.getCSeq().getMethod())) {
                transactionMatches = false;
            }
            return transactionMatches;
        } else if (isTerminated()) {
            return false;
        } else {
            if (rfc3261Compliant) {
                if (getBranch().equalsIgnoreCase(((Via) viaHeaders.getFirst()).getBranch())) {
                    return getOriginalRequest().getCSeq().getMethod().equals(messageToTest.getCSeq().getMethod());
                }
                return false;
            } else if (getBranch() != null) {
                return getBranch().equalsIgnoreCase(messageToTest.getTransactionId());
            } else {
                return getOriginalRequest().getTransactionId().equalsIgnoreCase(messageToTest.getTransactionId());
            }
        }
    }

    @Override // gov.nist.javax.sip.stack.MessageChannel, gov.nist.javax.sip.stack.SIPTransaction
    public void sendMessage(SIPMessage messageToSend) throws IOException {
        try {
            SIPRequest transactionRequest = (SIPRequest) messageToSend;
            try {
                ((Via) transactionRequest.getViaHeaders().getFirst()).setBranch(getBranch());
            } catch (ParseException e) {
            }
            if (this.sipStack.isLoggingEnabled()) {
                StackLogger stackLogger = this.sipStack.getStackLogger();
                stackLogger.logDebug("Sending Message " + messageToSend);
                StackLogger stackLogger2 = this.sipStack.getStackLogger();
                stackLogger2.logDebug("TransactionState " + getState());
            }
            if ((TransactionState.PROCEEDING == getState() || TransactionState.CALLING == getState()) && transactionRequest.getMethod().equals("ACK")) {
                if (isReliable()) {
                    setState(TransactionState.TERMINATED);
                } else {
                    setState(TransactionState.COMPLETED);
                }
                super.sendMessage(transactionRequest);
                return;
            }
            try {
                this.lastRequest = transactionRequest;
                if (getState() == null) {
                    setOriginalRequest(transactionRequest);
                    if (transactionRequest.getMethod().equals("INVITE")) {
                        setState(TransactionState.CALLING);
                    } else if (transactionRequest.getMethod().equals("ACK")) {
                        setState(TransactionState.TERMINATED);
                    } else {
                        setState(TransactionState.TRYING);
                    }
                    if (!isReliable()) {
                        enableRetransmissionTimer();
                    }
                    if (isInviteTransaction()) {
                        enableTimeoutTimer(64);
                    } else {
                        enableTimeoutTimer(64);
                    }
                }
                super.sendMessage(transactionRequest);
                this.isMapped = true;
                startTransactionTimer();
            } catch (IOException e2) {
                setState(TransactionState.TERMINATED);
                throw e2;
            }
        } finally {
            this.isMapped = true;
            startTransactionTimer();
        }
    }

    @Override // gov.nist.javax.sip.stack.ServerResponseInterface
    public synchronized void processResponse(SIPResponse transactionResponse, MessageChannel sourceChannel, SIPDialog dialog) {
        if (getState() != null) {
            if ((TransactionState.COMPLETED != getState() && TransactionState.TERMINATED != getState()) || transactionResponse.getStatusCode() / 100 != 1) {
                if (this.sipStack.isLoggingEnabled()) {
                    StackLogger stackLogger = this.sipStack.getStackLogger();
                    stackLogger.logDebug("processing " + transactionResponse.getFirstLine() + "current state = " + getState());
                    StackLogger stackLogger2 = this.sipStack.getStackLogger();
                    StringBuilder sb = new StringBuilder();
                    sb.append("dialog = ");
                    sb.append(dialog);
                    stackLogger2.logDebug(sb.toString());
                }
                this.lastResponse = transactionResponse;
                try {
                    if (isInviteTransaction()) {
                        inviteClientTransaction(transactionResponse, sourceChannel, dialog);
                    } else {
                        nonInviteClientTransaction(transactionResponse, sourceChannel, dialog);
                    }
                } catch (IOException ex) {
                    if (this.sipStack.isLoggingEnabled()) {
                        this.sipStack.getStackLogger().logException(ex);
                    }
                    setState(TransactionState.TERMINATED);
                    raiseErrorEvent(2);
                }
            }
        }
    }

    private void nonInviteClientTransaction(SIPResponse transactionResponse, MessageChannel sourceChannel, SIPDialog sipDialog) throws IOException {
        int statusCode = transactionResponse.getStatusCode();
        if (TransactionState.TRYING == getState()) {
            if (statusCode / 100 == 1) {
                setState(TransactionState.PROCEEDING);
                enableRetransmissionTimer(8);
                enableTimeoutTimer(64);
                ServerResponseInterface serverResponseInterface = this.respondTo;
                if (serverResponseInterface != null) {
                    serverResponseInterface.processResponse(transactionResponse, this, sipDialog);
                } else {
                    semRelease();
                }
            } else if (200 <= statusCode && statusCode <= 699) {
                ServerResponseInterface serverResponseInterface2 = this.respondTo;
                if (serverResponseInterface2 != null) {
                    serverResponseInterface2.processResponse(transactionResponse, this, sipDialog);
                } else {
                    semRelease();
                }
                if (!isReliable()) {
                    setState(TransactionState.COMPLETED);
                    enableTimeoutTimer(this.TIMER_K);
                    return;
                }
                setState(TransactionState.TERMINATED);
            }
        } else if (TransactionState.PROCEEDING != getState()) {
            if (this.sipStack.isLoggingEnabled()) {
                StackLogger stackLogger = this.sipStack.getStackLogger();
                stackLogger.logDebug(" Not sending response to TU! " + getState());
            }
            semRelease();
        } else if (statusCode / 100 == 1) {
            ServerResponseInterface serverResponseInterface3 = this.respondTo;
            if (serverResponseInterface3 != null) {
                serverResponseInterface3.processResponse(transactionResponse, this, sipDialog);
            } else {
                semRelease();
            }
        } else if (200 <= statusCode && statusCode <= 699) {
            ServerResponseInterface serverResponseInterface4 = this.respondTo;
            if (serverResponseInterface4 != null) {
                serverResponseInterface4.processResponse(transactionResponse, this, sipDialog);
            } else {
                semRelease();
            }
            disableRetransmissionTimer();
            disableTimeoutTimer();
            if (!isReliable()) {
                setState(TransactionState.COMPLETED);
                enableTimeoutTimer(this.TIMER_K);
                return;
            }
            setState(TransactionState.TERMINATED);
        }
    }

    private void inviteClientTransaction(SIPResponse transactionResponse, MessageChannel sourceChannel, SIPDialog dialog) throws IOException {
        int statusCode = transactionResponse.getStatusCode();
        if (TransactionState.TERMINATED == getState()) {
            boolean ackAlreadySent = false;
            if (dialog != null && dialog.isAckSeen() && dialog.getLastAckSent() != null && dialog.getLastAckSent().getCSeq().getSeqNumber() == transactionResponse.getCSeq().getSeqNumber() && transactionResponse.getFromTag().equals(dialog.getLastAckSent().getFromTag())) {
                ackAlreadySent = true;
            }
            if (dialog != null && ackAlreadySent && transactionResponse.getCSeq().getMethod().equals(dialog.getMethod())) {
                try {
                    if (this.sipStack.isLoggingEnabled()) {
                        this.sipStack.getStackLogger().logDebug("resending ACK");
                    }
                    dialog.resendAck();
                } catch (SipException e) {
                }
            }
            semRelease();
        } else if (TransactionState.CALLING == getState()) {
            if (statusCode / 100 == 2) {
                disableRetransmissionTimer();
                disableTimeoutTimer();
                setState(TransactionState.TERMINATED);
                ServerResponseInterface serverResponseInterface = this.respondTo;
                if (serverResponseInterface != null) {
                    serverResponseInterface.processResponse(transactionResponse, this, dialog);
                } else {
                    semRelease();
                }
            } else if (statusCode / 100 == 1) {
                disableRetransmissionTimer();
                disableTimeoutTimer();
                setState(TransactionState.PROCEEDING);
                ServerResponseInterface serverResponseInterface2 = this.respondTo;
                if (serverResponseInterface2 != null) {
                    serverResponseInterface2.processResponse(transactionResponse, this, dialog);
                } else {
                    semRelease();
                }
            } else if (300 <= statusCode && statusCode <= 699) {
                try {
                    sendMessage((SIPRequest) createErrorAck());
                } catch (Exception ex) {
                    this.sipStack.getStackLogger().logError("Unexpected Exception sending ACK -- sending error AcK ", ex);
                }
                ServerResponseInterface serverResponseInterface3 = this.respondTo;
                if (serverResponseInterface3 != null) {
                    serverResponseInterface3.processResponse(transactionResponse, this, dialog);
                } else {
                    semRelease();
                }
                if (getDialog() != null && ((SIPDialog) getDialog()).isBackToBackUserAgent()) {
                    ((SIPDialog) getDialog()).releaseAckSem();
                }
                if (!isReliable()) {
                    setState(TransactionState.COMPLETED);
                    enableTimeoutTimer(this.TIMER_D);
                    return;
                }
                setState(TransactionState.TERMINATED);
            }
        } else if (TransactionState.PROCEEDING == getState()) {
            if (statusCode / 100 == 1) {
                ServerResponseInterface serverResponseInterface4 = this.respondTo;
                if (serverResponseInterface4 != null) {
                    serverResponseInterface4.processResponse(transactionResponse, this, dialog);
                } else {
                    semRelease();
                }
            } else if (statusCode / 100 == 2) {
                setState(TransactionState.TERMINATED);
                ServerResponseInterface serverResponseInterface5 = this.respondTo;
                if (serverResponseInterface5 != null) {
                    serverResponseInterface5.processResponse(transactionResponse, this, dialog);
                } else {
                    semRelease();
                }
            } else if (300 <= statusCode && statusCode <= 699) {
                try {
                    sendMessage((SIPRequest) createErrorAck());
                } catch (Exception ex2) {
                    InternalErrorHandler.handleException(ex2);
                }
                if (getDialog() != null) {
                    ((SIPDialog) getDialog()).releaseAckSem();
                }
                if (!isReliable()) {
                    setState(TransactionState.COMPLETED);
                    enableTimeoutTimer(this.TIMER_D);
                } else {
                    setState(TransactionState.TERMINATED);
                }
                ServerResponseInterface serverResponseInterface6 = this.respondTo;
                if (serverResponseInterface6 != null) {
                    serverResponseInterface6.processResponse(transactionResponse, this, dialog);
                } else {
                    semRelease();
                }
            }
        } else if (TransactionState.COMPLETED == getState() && 300 <= statusCode && statusCode <= 699) {
            try {
                sendMessage((SIPRequest) createErrorAck());
            } catch (Exception ex3) {
                InternalErrorHandler.handleException(ex3);
            } catch (Throwable th) {
                semRelease();
                throw th;
            }
            semRelease();
        }
    }

    @Override // javax.sip.ClientTransaction
    public void sendRequest() throws SipException {
        SIPDialog dialog;
        SIPRequest sipRequest = getOriginalRequest();
        if (getState() == null) {
            if (this.sipStack.isLoggingEnabled()) {
                StackLogger stackLogger = this.sipStack.getStackLogger();
                stackLogger.logDebug("sendRequest() " + sipRequest);
            }
            try {
                sipRequest.checkHeaders();
                if (getMethod().equals("SUBSCRIBE") && sipRequest.getHeader("Expires") == null && this.sipStack.isLoggingEnabled()) {
                    this.sipStack.getStackLogger().logWarning("Expires header missing in outgoing subscribe -- Notifier will assume implied value on event package");
                }
                try {
                    if (getOriginalRequest().getMethod().equals(Request.CANCEL) && this.sipStack.isCancelClientTransactionChecked()) {
                        SIPClientTransaction ct = (SIPClientTransaction) this.sipStack.findCancelTransaction(getOriginalRequest(), false);
                        if (ct == null) {
                            throw new SipException("Could not find original tx to cancel. RFC 3261 9.1");
                        } else if (ct.getState() == null) {
                            throw new SipException("State is null no provisional response yet -- cannot cancel RFC 3261 9.1");
                        } else if (!ct.getMethod().equals("INVITE")) {
                            throw new SipException("Cannot cancel non-invite requests RFC 3261 9.1");
                        }
                    } else if (getOriginalRequest().getMethod().equals("BYE") || getOriginalRequest().getMethod().equals("NOTIFY")) {
                        SIPDialog dialog2 = this.sipStack.getDialog(getOriginalRequest().getDialogId(false));
                        if (getSipProvider().isAutomaticDialogSupportEnabled() && dialog2 != null) {
                            throw new SipException("Dialog is present and AutomaticDialogSupport is enabled for  the provider -- Send the Request using the Dialog.sendRequest(transaction)");
                        }
                    }
                    if (!getMethod().equals("INVITE") || (dialog = getDefaultDialog()) == null || !dialog.isBackToBackUserAgent() || dialog.takeAckSem()) {
                        this.isMapped = true;
                        sendMessage(sipRequest);
                        return;
                    }
                    throw new SipException("Failed to take ACK semaphore");
                } catch (IOException ex) {
                    setState(TransactionState.TERMINATED);
                    throw new SipException("IO Error sending request", ex);
                }
            } catch (ParseException ex2) {
                if (this.sipStack.isLoggingEnabled()) {
                    this.sipStack.getStackLogger().logError("missing required header");
                }
                throw new SipException(ex2.getMessage());
            }
        } else {
            throw new SipException("Request already sent");
        }
    }

    /* access modifiers changed from: protected */
    @Override // gov.nist.javax.sip.stack.SIPTransaction
    public void fireRetransmissionTimer() {
        try {
            if (getState() == null) {
                return;
            }
            if (this.isMapped) {
                boolean inv = isInviteTransaction();
                TransactionState s = getState();
                if (!inv || TransactionState.CALLING != s) {
                    if (inv) {
                        return;
                    }
                    if (!(TransactionState.TRYING == s || TransactionState.PROCEEDING == s)) {
                        return;
                    }
                }
                if (this.lastRequest != null) {
                    if (this.sipStack.generateTimeStampHeader && this.lastRequest.getHeader("Timestamp") != null) {
                        long milisec = System.currentTimeMillis();
                        TimeStamp timeStamp = new TimeStamp();
                        try {
                            timeStamp.setTimeStamp((float) milisec);
                        } catch (InvalidArgumentException ex) {
                            InternalErrorHandler.handleException(ex);
                        }
                        this.lastRequest.setHeader(timeStamp);
                    }
                    super.sendMessage(this.lastRequest);
                    if (this.notifyOnRetransmit) {
                        getSipProvider().handleEvent(new TimeoutEvent(getSipProvider(), this, Timeout.RETRANSMIT), this);
                    }
                    if (this.timeoutIfStillInCallingState && getState() == TransactionState.CALLING) {
                        this.callingStateTimeoutCount--;
                        if (this.callingStateTimeoutCount == 0) {
                            getSipProvider().handleEvent(new TimeoutEvent(getSipProvider(), this, Timeout.RETRANSMIT), this);
                            this.timeoutIfStillInCallingState = false;
                        }
                    }
                }
            }
        } catch (IOException e) {
            raiseIOExceptionEvent();
            raiseErrorEvent(2);
        }
    }

    /* access modifiers changed from: protected */
    @Override // gov.nist.javax.sip.stack.SIPTransaction
    public void fireTimeoutTimer() {
        SIPClientTransaction inviteTx;
        if (this.sipStack.isLoggingEnabled()) {
            StackLogger stackLogger = this.sipStack.getStackLogger();
            stackLogger.logDebug("fireTimeoutTimer " + this);
        }
        SIPDialog dialog = (SIPDialog) getDialog();
        if (TransactionState.CALLING == getState() || TransactionState.TRYING == getState() || TransactionState.PROCEEDING == getState()) {
            if (dialog != null && (dialog.getState() == null || dialog.getState() == DialogState.EARLY)) {
                getSIPStack();
                if (SIPTransactionStack.isDialogCreated(getOriginalRequest().getMethod())) {
                    dialog.delete();
                }
            } else if (dialog != null && getOriginalRequest().getMethod().equalsIgnoreCase("BYE") && dialog.isTerminatedOnBye()) {
                dialog.delete();
            }
        }
        if (TransactionState.COMPLETED != getState()) {
            raiseErrorEvent(1);
            if (getOriginalRequest().getMethod().equalsIgnoreCase(Request.CANCEL) && (inviteTx = (SIPClientTransaction) getOriginalRequest().getInviteTransaction()) != null) {
                if ((inviteTx.getState() == TransactionState.CALLING || inviteTx.getState() == TransactionState.PROCEEDING) && inviteTx.getDialog() != null) {
                    inviteTx.setState(TransactionState.TERMINATED);
                    return;
                }
                return;
            }
            return;
        }
        setState(TransactionState.TERMINATED);
    }

    @Override // javax.sip.ClientTransaction
    public Request createCancel() throws SipException {
        SIPRequest originalRequest = getOriginalRequest();
        if (originalRequest == null) {
            throw new SipException("Bad state " + getState());
        } else if (!originalRequest.getMethod().equals("INVITE")) {
            throw new SipException("Only INIVTE may be cancelled");
        } else if (!originalRequest.getMethod().equalsIgnoreCase("ACK")) {
            SIPRequest cancelRequest = originalRequest.createCancelRequest();
            cancelRequest.setInviteTransaction(this);
            return cancelRequest;
        } else {
            throw new SipException("Cannot Cancel ACK!");
        }
    }

    @Override // javax.sip.ClientTransaction
    public Request createAck() throws SipException {
        SIPRequest originalRequest = getOriginalRequest();
        if (originalRequest == null) {
            throw new SipException("bad state " + getState());
        } else if (getMethod().equalsIgnoreCase("ACK")) {
            throw new SipException("Cannot ACK an ACK!");
        } else if (this.lastResponse == null) {
            throw new SipException("bad Transaction state");
        } else if (this.lastResponse.getStatusCode() < 200) {
            if (this.sipStack.isLoggingEnabled()) {
                StackLogger stackLogger = this.sipStack.getStackLogger();
                stackLogger.logDebug("lastResponse = " + this.lastResponse);
            }
            throw new SipException("Cannot ACK a provisional response!");
        } else {
            SIPRequest ackRequest = originalRequest.createAckRequest((To) this.lastResponse.getTo());
            RecordRouteList recordRouteList = this.lastResponse.getRecordRouteHeaders();
            if (recordRouteList == null) {
                if (!(this.lastResponse.getContactHeaders() == null || this.lastResponse.getStatusCode() / 100 == 3)) {
                    ackRequest.setRequestURI((URI) ((Contact) this.lastResponse.getContactHeaders().getFirst()).getAddress().getURI().clone());
                }
                return ackRequest;
            }
            ackRequest.removeHeader("Route");
            RouteList routeList = new RouteList();
            ListIterator<RecordRoute> li = recordRouteList.listIterator(recordRouteList.size());
            while (li.hasPrevious()) {
                RecordRoute rr = li.previous();
                Route route = new Route();
                route.setAddress((AddressImpl) ((AddressImpl) rr.getAddress()).clone());
                route.setParameters((NameValueList) rr.getParameters().clone());
                routeList.add((SIPHeader) route);
            }
            Contact contact = null;
            if (this.lastResponse.getContactHeaders() != null) {
                contact = (Contact) this.lastResponse.getContactHeaders().getFirst();
            }
            if (!((SipURI) ((Route) routeList.getFirst()).getAddress().getURI()).hasLrParam()) {
                Route route2 = null;
                if (contact != null) {
                    route2 = new Route();
                    route2.setAddress((AddressImpl) ((AddressImpl) contact.getAddress()).clone());
                }
                routeList.removeFirst();
                ackRequest.setRequestURI(((Route) routeList.getFirst()).getAddress().getURI());
                if (route2 != null) {
                    routeList.add((SIPHeader) route2);
                }
                ackRequest.addHeader(routeList);
            } else if (contact != null) {
                ackRequest.setRequestURI((URI) contact.getAddress().getURI().clone());
                ackRequest.addHeader(routeList);
            }
            return ackRequest;
        }
    }

    private final Request createErrorAck() throws SipException, ParseException {
        SIPRequest originalRequest = getOriginalRequest();
        if (originalRequest == null) {
            throw new SipException("bad state " + getState());
        } else if (!getMethod().equals("INVITE")) {
            throw new SipException("Can only ACK an INVITE!");
        } else if (this.lastResponse == null) {
            throw new SipException("bad Transaction state");
        } else if (this.lastResponse.getStatusCode() >= 200) {
            return originalRequest.createErrorAck((To) this.lastResponse.getTo());
        } else {
            if (this.sipStack.isLoggingEnabled()) {
                StackLogger stackLogger = this.sipStack.getStackLogger();
                stackLogger.logDebug("lastResponse = " + this.lastResponse);
            }
            throw new SipException("Cannot ACK a provisional response!");
        }
    }

    /* access modifiers changed from: protected */
    public void setViaPort(int port) {
        this.viaPort = port;
    }

    /* access modifiers changed from: protected */
    public void setViaHost(String host) {
        this.viaHost = host;
    }

    @Override // gov.nist.javax.sip.stack.MessageChannel, gov.nist.javax.sip.stack.SIPTransaction
    public int getViaPort() {
        return this.viaPort;
    }

    @Override // gov.nist.javax.sip.stack.MessageChannel, gov.nist.javax.sip.stack.SIPTransaction
    public String getViaHost() {
        return this.viaHost;
    }

    public Via getOutgoingViaHeader() {
        return getMessageProcessor().getViaHeader();
    }

    public void clearState() {
    }

    @Override // gov.nist.javax.sip.stack.SIPTransaction
    public void setState(TransactionState newState) {
        if (newState == TransactionState.TERMINATED && isReliable() && !getSIPStack().cacheClientConnections) {
            this.collectionTime = 64;
        }
        if (super.getState() != TransactionState.COMPLETED && (newState == TransactionState.COMPLETED || newState == TransactionState.TERMINATED)) {
            this.sipStack.decrementActiveClientTransactionCount();
        }
        super.setState(newState);
    }

    /* access modifiers changed from: protected */
    @Override // gov.nist.javax.sip.stack.SIPTransaction
    public void startTransactionTimer() {
        if (this.transactionTimerStarted.compareAndSet(false, true)) {
            TimerTask myTimer = new TransactionTimer();
            if (this.sipStack.getTimer() != null) {
                this.sipStack.getTimer().schedule(myTimer, (long) this.BASE_TIMER_INTERVAL, (long) this.BASE_TIMER_INTERVAL);
            }
        }
    }

    @Override // javax.sip.Transaction
    public void terminate() throws ObjectInUseException {
        setState(TransactionState.TERMINATED);
    }

    public boolean checkFromTag(SIPResponse sipResponse) {
        String originalFromTag = ((SIPRequest) getRequest()).getFromTag();
        if (this.defaultDialog != null) {
            if ((originalFromTag == null) ^ (sipResponse.getFrom().getTag() == null)) {
                if (this.sipStack.isLoggingEnabled()) {
                    this.sipStack.getStackLogger().logDebug("From tag mismatch -- dropping response");
                }
                return false;
            } else if (originalFromTag != null && !originalFromTag.equalsIgnoreCase(sipResponse.getFrom().getTag())) {
                if (this.sipStack.isLoggingEnabled()) {
                    this.sipStack.getStackLogger().logDebug("From tag mismatch -- dropping response");
                }
                return false;
            }
        }
        return true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:41:0x00a8  */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x00b0  */
    @Override // gov.nist.javax.sip.stack.ServerResponseInterface
    public void processResponse(SIPResponse sipResponse, MessageChannel incomingChannel) {
        SIPDialog dialog;
        SIPRequest sIPRequest;
        SIPDialog dialog2 = null;
        String method = sipResponse.getCSeq().getMethod();
        String dialogId = sipResponse.getDialogId(false);
        if (!method.equals(Request.CANCEL) || (sIPRequest = this.lastRequest) == null) {
            dialog2 = getDialog(dialogId);
        } else {
            SIPClientTransaction ict = (SIPClientTransaction) sIPRequest.getInviteTransaction();
            if (ict != null) {
                dialog2 = ict.defaultDialog;
            }
        }
        if (dialog == null) {
            int code = sipResponse.getStatusCode();
            if (code > 100 && code < 300 && (sipResponse.getToTag() != null || this.sipStack.isRfc2543Supported())) {
                SIPTransactionStack sIPTransactionStack = this.sipStack;
                if (SIPTransactionStack.isDialogCreated(method)) {
                    synchronized (this) {
                        if (this.defaultDialog != null) {
                            if (sipResponse.getFromTag() != null) {
                                SIPResponse dialogResponse = this.defaultDialog.getLastResponse();
                                String defaultDialogId = this.defaultDialog.getDialogId();
                                if (dialogResponse != null) {
                                    if (!method.equals("SUBSCRIBE") || !dialogResponse.getCSeq().getMethod().equals("NOTIFY") || !defaultDialogId.equals(dialogId)) {
                                        dialog = this.sipStack.getDialog(dialogId);
                                        if (dialog == null && this.defaultDialog.isAssigned()) {
                                            dialog = this.sipStack.createDialog(this, sipResponse);
                                        }
                                        if (dialog == null) {
                                            setDialog(dialog, dialog.getDialogId());
                                        } else {
                                            this.sipStack.getStackLogger().logError("dialog is unexpectedly null", new NullPointerException());
                                        }
                                    }
                                }
                                this.defaultDialog.setLastResponse(this, sipResponse);
                                dialog = this.defaultDialog;
                                if (dialog == null) {
                                }
                            } else {
                                throw new RuntimeException("Response without from-tag");
                            }
                        } else if (this.sipStack.isAutomaticDialogSupportEnabled) {
                            dialog = this.sipStack.createDialog(this, sipResponse);
                            setDialog(dialog, dialog.getDialogId());
                        }
                    }
                }
            }
            dialog = this.defaultDialog;
        } else {
            dialog.setLastResponse(this, sipResponse);
        }
        processResponse(sipResponse, incomingChannel, dialog);
    }

    @Override // gov.nist.javax.sip.stack.SIPTransaction, javax.sip.Transaction
    public Dialog getDialog() {
        Dialog retval = null;
        if (!(this.lastResponse == null || this.lastResponse.getFromTag() == null || this.lastResponse.getToTag() == null || this.lastResponse.getStatusCode() == 100)) {
            retval = getDialog(this.lastResponse.getDialogId(false));
        }
        if (retval == null) {
            retval = this.defaultDialog;
        }
        if (this.sipStack.isLoggingEnabled()) {
            StackLogger stackLogger = this.sipStack.getStackLogger();
            stackLogger.logDebug(" sipDialogs =  " + this.sipDialogs + " default dialog " + this.defaultDialog + " retval " + retval);
        }
        return retval;
    }

    public SIPDialog getDialog(String dialogId) {
        return this.sipDialogs.get(dialogId);
    }

    @Override // gov.nist.javax.sip.stack.SIPTransaction
    public void setDialog(SIPDialog sipDialog, String dialogId) {
        if (this.sipStack.isLoggingEnabled()) {
            StackLogger stackLogger = this.sipStack.getStackLogger();
            stackLogger.logDebug("setDialog: " + dialogId + "sipDialog = " + sipDialog);
        }
        if (sipDialog == null) {
            if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logError("NULL DIALOG!!");
            }
            throw new NullPointerException("bad dialog null");
        }
        if (this.defaultDialog == null) {
            this.defaultDialog = sipDialog;
            if (getMethod().equals("INVITE") && getSIPStack().maxForkTime != 0) {
                getSIPStack().addForkedClientTransaction(this);
            }
        }
        if (dialogId != null && sipDialog.getDialogId() != null) {
            this.sipDialogs.put(dialogId, sipDialog);
        }
    }

    public SIPDialog getDefaultDialog() {
        return this.defaultDialog;
    }

    public void setNextHop(Hop hop) {
        this.nextHop = hop;
    }

    @Override // gov.nist.javax.sip.ClientTransactionExt, javax.sip.ClientTransaction
    public Hop getNextHop() {
        return this.nextHop;
    }

    @Override // gov.nist.javax.sip.ClientTransactionExt, javax.sip.ClientTransaction
    public void setNotifyOnRetransmit(boolean notifyOnRetransmit2) {
        this.notifyOnRetransmit = notifyOnRetransmit2;
    }

    public boolean isNotifyOnRetransmit() {
        return this.notifyOnRetransmit;
    }

    @Override // gov.nist.javax.sip.ClientTransactionExt, javax.sip.ClientTransaction
    public void alertIfStillInCallingStateBy(int count) {
        this.timeoutIfStillInCallingState = true;
        this.callingStateTimeoutCount = count;
    }
}
