package gov.nist.javax.sip;

import gov.nist.core.InternalErrorHandler;
import gov.nist.core.Separators;
import gov.nist.core.StackLogger;
import gov.nist.javax.sip.DialogTimeoutEvent;
import gov.nist.javax.sip.address.ParameterNames;
import gov.nist.javax.sip.address.RouterExt;
import gov.nist.javax.sip.header.CallID;
import gov.nist.javax.sip.header.Via;
import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.message.SIPResponse;
import gov.nist.javax.sip.stack.HopImpl;
import gov.nist.javax.sip.stack.MessageChannel;
import gov.nist.javax.sip.stack.SIPClientTransaction;
import gov.nist.javax.sip.stack.SIPDialog;
import gov.nist.javax.sip.stack.SIPDialogErrorEvent;
import gov.nist.javax.sip.stack.SIPDialogEventListener;
import gov.nist.javax.sip.stack.SIPServerTransaction;
import gov.nist.javax.sip.stack.SIPTransaction;
import gov.nist.javax.sip.stack.SIPTransactionErrorEvent;
import gov.nist.javax.sip.stack.SIPTransactionEventListener;
import java.io.IOException;
import java.text.ParseException;
import java.util.EventObject;
import java.util.Iterator;
import java.util.TooManyListenersException;
import java.util.concurrent.ConcurrentHashMap;
import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.DialogState;
import javax.sip.InvalidArgumentException;
import javax.sip.ListeningPoint;
import javax.sip.ObjectInUseException;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import javax.sip.Timeout;
import javax.sip.TimeoutEvent;
import javax.sip.Transaction;
import javax.sip.TransactionAlreadyExistsException;
import javax.sip.TransactionState;
import javax.sip.TransactionUnavailableException;
import javax.sip.address.Hop;
import javax.sip.header.CallIdHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

public class SipProviderImpl implements SipProvider, SipProviderExt, SIPTransactionEventListener, SIPDialogEventListener {
    private String IN6_ADDR_ANY = "::0";
    private String IN_ADDR_ANY = "0.0.0.0";
    private String address;
    private boolean automaticDialogSupportEnabled;
    private boolean dialogErrorsAutomaticallyHandled = true;
    private EventScanner eventScanner;
    private ConcurrentHashMap listeningPoints;
    private int port;
    private SipListener sipListener;
    protected SipStackImpl sipStack;

    private SipProviderImpl() {
    }

    /* access modifiers changed from: protected */
    public void stop() {
        if (this.sipStack.isLoggingEnabled()) {
            this.sipStack.getStackLogger().logDebug("Exiting provider");
        }
        for (ListeningPointImpl listeningPoint : this.listeningPoints.values()) {
            listeningPoint.removeSipProvider();
        }
        this.eventScanner.stop();
    }

    public ListeningPoint getListeningPoint(String transport) {
        if (transport != null) {
            return (ListeningPoint) this.listeningPoints.get(transport.toUpperCase());
        }
        throw new NullPointerException("Null transport param");
    }

    public void handleEvent(EventObject sipEvent, SIPTransaction transaction) {
        if (this.sipStack.isLoggingEnabled()) {
            StackLogger stackLogger = this.sipStack.getStackLogger();
            stackLogger.logDebug("handleEvent " + sipEvent + "currentTransaction = " + transaction + "this.sipListener = " + getSipListener() + "sipEvent.source = " + sipEvent.getSource());
            if (sipEvent instanceof RequestEvent) {
                Dialog dialog = ((RequestEvent) sipEvent).getDialog();
                if (this.sipStack.isLoggingEnabled()) {
                    StackLogger stackLogger2 = this.sipStack.getStackLogger();
                    stackLogger2.logDebug("Dialog = " + dialog);
                }
            } else if (sipEvent instanceof ResponseEvent) {
                Dialog dialog2 = ((ResponseEvent) sipEvent).getDialog();
                if (this.sipStack.isLoggingEnabled()) {
                    StackLogger stackLogger3 = this.sipStack.getStackLogger();
                    stackLogger3.logDebug("Dialog = " + dialog2);
                }
            }
            this.sipStack.getStackLogger().logStackTrace();
        }
        EventWrapper eventWrapper = new EventWrapper(sipEvent, transaction);
        if (!this.sipStack.reEntrantListener) {
            this.eventScanner.addEvent(eventWrapper);
        } else {
            this.eventScanner.deliverEvent(eventWrapper);
        }
    }

    protected SipProviderImpl(SipStackImpl sipStack2) {
        this.eventScanner = sipStack2.getEventScanner();
        this.sipStack = sipStack2;
        this.eventScanner.incrementRefcount();
        this.listeningPoints = new ConcurrentHashMap();
        this.automaticDialogSupportEnabled = this.sipStack.isAutomaticDialogSupportEnabled();
        this.dialogErrorsAutomaticallyHandled = this.sipStack.isAutomaticDialogErrorHandlingEnabled();
    }

    /* access modifiers changed from: protected */
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public void addSipListener(SipListener sipListener2) throws TooManyListenersException {
        if (this.sipStack.sipListener == null) {
            this.sipStack.sipListener = sipListener2;
        } else if (this.sipStack.sipListener != sipListener2) {
            throw new TooManyListenersException("Stack already has a listener. Only one listener per stack allowed");
        }
        if (this.sipStack.isLoggingEnabled()) {
            StackLogger stackLogger = this.sipStack.getStackLogger();
            stackLogger.logDebug("add SipListener " + sipListener2);
        }
        this.sipListener = sipListener2;
    }

    public ListeningPoint getListeningPoint() {
        if (this.listeningPoints.size() > 0) {
            return (ListeningPoint) this.listeningPoints.values().iterator().next();
        }
        return null;
    }

    public CallIdHeader getNewCallId() {
        String callId = Utils.getInstance().generateCallIdentifier(getListeningPoint().getIPAddress());
        CallID callid = new CallID();
        try {
            callid.setCallId(callId);
        } catch (ParseException e) {
        }
        return callid;
    }

    public ClientTransaction getNewClientTransaction(Request request) throws TransactionUnavailableException {
        if (request == null) {
            throw new NullPointerException("null request");
        } else if (this.sipStack.isAlive()) {
            SIPRequest sipRequest = (SIPRequest) request;
            if (sipRequest.getTransaction() != null) {
                throw new TransactionUnavailableException("Transaction already assigned to request");
            } else if (!sipRequest.getMethod().equals("ACK")) {
                if (sipRequest.getTopmostVia() == null) {
                    request.setHeader(((ListeningPointImpl) getListeningPoint(ParameterNames.UDP)).getViaHeader());
                }
                try {
                    sipRequest.checkHeaders();
                    if (sipRequest.getTopmostVia().getBranch() == null || !sipRequest.getTopmostVia().getBranch().startsWith(SIPConstants.BRANCH_MAGIC_COOKIE) || this.sipStack.findTransaction((SIPRequest) request, false) == null) {
                        if (request.getMethod().equalsIgnoreCase(Request.CANCEL)) {
                            SIPClientTransaction ct = (SIPClientTransaction) this.sipStack.findCancelTransaction((SIPRequest) request, false);
                            if (ct != null) {
                                SIPClientTransaction createClientTransaction = this.sipStack.createClientTransaction((SIPRequest) request, ct.getMessageChannel());
                                createClientTransaction.addEventListener(this);
                                this.sipStack.addTransaction(createClientTransaction);
                                if (ct.getDialog() != null) {
                                    createClientTransaction.setDialog((SIPDialog) ct.getDialog(), sipRequest.getDialogId(false));
                                }
                                return createClientTransaction;
                            }
                        }
                        if (this.sipStack.isLoggingEnabled()) {
                            StackLogger stackLogger = this.sipStack.getStackLogger();
                            stackLogger.logDebug("could not find existing transaction for " + ((SIPRequest) request).getFirstLine() + " creating a new one ");
                        }
                        try {
                            Hop hop = this.sipStack.getNextHop((SIPRequest) request);
                            if (hop != null) {
                                String transport = hop.getTransport();
                                ListeningPointImpl listeningPoint = (ListeningPointImpl) getListeningPoint(transport);
                                SIPDialog dialog = this.sipStack.getDialog(sipRequest.getDialogId(false));
                                if (dialog != null && dialog.getState() == DialogState.TERMINATED) {
                                    this.sipStack.removeDialog(dialog);
                                }
                                try {
                                    if (sipRequest.getTopmostVia().getBranch() == null || !sipRequest.getTopmostVia().getBranch().startsWith(SIPConstants.BRANCH_MAGIC_COOKIE) || this.sipStack.checkBranchId()) {
                                        sipRequest.getTopmostVia().setBranch(Utils.getInstance().generateBranchId());
                                    }
                                    Via topmostVia = sipRequest.getTopmostVia();
                                    if (topmostVia.getTransport() == null) {
                                        topmostVia.setTransport(transport);
                                    }
                                    if (topmostVia.getPort() == -1) {
                                        topmostVia.setPort(listeningPoint.getPort());
                                    }
                                    String branchId = sipRequest.getTopmostVia().getBranch();
                                    SIPClientTransaction ct2 = (SIPClientTransaction) this.sipStack.createMessageChannel(sipRequest, listeningPoint.getMessageProcessor(), hop);
                                    if (ct2 != null) {
                                        ct2.setNextHop(hop);
                                        ct2.setOriginalRequest(sipRequest);
                                        ct2.setBranch(branchId);
                                        SipStackImpl sipStackImpl = this.sipStack;
                                        if (SipStackImpl.isDialogCreated(request.getMethod())) {
                                            if (dialog != null) {
                                                ct2.setDialog(dialog, sipRequest.getDialogId(false));
                                            } else if (isAutomaticDialogSupportEnabled()) {
                                                ct2.setDialog(this.sipStack.createDialog(ct2), sipRequest.getDialogId(false));
                                            }
                                        } else if (dialog != null) {
                                            ct2.setDialog(dialog, sipRequest.getDialogId(false));
                                        }
                                        ct2.addEventListener(this);
                                        return ct2;
                                    }
                                    throw new TransactionUnavailableException("Cound not create tx");
                                } catch (IOException ex) {
                                    throw new TransactionUnavailableException("Could not resolve next hop or listening point unavailable! ", ex);
                                } catch (ParseException ex2) {
                                    InternalErrorHandler.handleException((Exception) ex2);
                                    throw new TransactionUnavailableException("Unexpected Exception FIXME! ", ex2);
                                } catch (InvalidArgumentException ex3) {
                                    InternalErrorHandler.handleException((Exception) ex3);
                                    throw new TransactionUnavailableException("Unexpected Exception FIXME! ", ex3);
                                }
                            } else {
                                throw new TransactionUnavailableException("Cannot resolve next hop -- transaction unavailable");
                            }
                        } catch (SipException ex4) {
                            throw new TransactionUnavailableException("Cannot resolve next hop -- transaction unavailable", ex4);
                        }
                    } else {
                        throw new TransactionUnavailableException("Transaction already exists!");
                    }
                } catch (ParseException ex5) {
                    throw new TransactionUnavailableException(ex5.getMessage(), ex5);
                }
            } else {
                throw new TransactionUnavailableException("Cannot create client transaction for  ACK");
            }
        } else {
            throw new TransactionUnavailableException("Stack is stopped");
        }
    }

    public ServerTransaction getNewServerTransaction(Request request) throws TransactionAlreadyExistsException, TransactionUnavailableException {
        SIPServerTransaction transaction;
        if (this.sipStack.isAlive()) {
            SIPRequest sipRequest = (SIPRequest) request;
            try {
                sipRequest.checkHeaders();
                if (request.getMethod().equals("ACK")) {
                    if (this.sipStack.isLoggingEnabled()) {
                        this.sipStack.getStackLogger().logError("Creating server transaction for ACK -- makes no sense!");
                    }
                    throw new TransactionUnavailableException("Cannot create Server transaction for ACK ");
                } else if (sipRequest.getMethod().equals("NOTIFY") && sipRequest.getFromTag() != null && sipRequest.getToTag() == null && this.sipStack.findSubscribeTransaction(sipRequest, (ListeningPointImpl) getListeningPoint()) == null && !this.sipStack.deliverUnsolicitedNotify) {
                    throw new TransactionUnavailableException("Cannot find matching Subscription (and gov.nist.javax.sip.DELIVER_UNSOLICITED_NOTIFY not set)");
                } else if (this.sipStack.acquireSem()) {
                    try {
                        SipStackImpl sipStackImpl = this.sipStack;
                        if (SipStackImpl.isDialogCreated(sipRequest.getMethod())) {
                            if (this.sipStack.findTransaction((SIPRequest) request, true) == null) {
                                transaction = (SIPServerTransaction) ((SIPRequest) request).getTransaction();
                                if (transaction != null) {
                                    if (transaction.getOriginalRequest() == null) {
                                        transaction.setOriginalRequest(sipRequest);
                                    }
                                    this.sipStack.addTransaction(transaction);
                                    transaction.addEventListener(this);
                                    if (isAutomaticDialogSupportEnabled()) {
                                        SIPDialog dialog = this.sipStack.getDialog(sipRequest.getDialogId(true));
                                        if (dialog == null) {
                                            dialog = this.sipStack.createDialog(transaction);
                                        }
                                        transaction.setDialog(dialog, sipRequest.getDialogId(true));
                                        if (sipRequest.getMethod().equals("INVITE") && isDialogErrorsAutomaticallyHandled()) {
                                            this.sipStack.putInMergeTable(transaction, sipRequest);
                                        }
                                        dialog.addRoute(sipRequest);
                                        if (!(dialog.getRemoteTag() == null || dialog.getLocalTag() == null)) {
                                            this.sipStack.putDialog(dialog);
                                        }
                                    }
                                } else {
                                    throw new TransactionUnavailableException("Transaction not available");
                                }
                            } else {
                                throw new TransactionAlreadyExistsException("server transaction already exists!");
                            }
                        } else if (isAutomaticDialogSupportEnabled()) {
                            if (((SIPServerTransaction) this.sipStack.findTransaction((SIPRequest) request, true)) == null) {
                                transaction = (SIPServerTransaction) ((SIPRequest) request).getTransaction();
                                if (transaction != null) {
                                    if (transaction.getOriginalRequest() == null) {
                                        transaction.setOriginalRequest(sipRequest);
                                    }
                                    this.sipStack.addTransaction(transaction);
                                    SIPDialog dialog2 = this.sipStack.getDialog(sipRequest.getDialogId(true));
                                    if (dialog2 != null) {
                                        dialog2.addTransaction(transaction);
                                        dialog2.addRoute(sipRequest);
                                        transaction.setDialog(dialog2, sipRequest.getDialogId(true));
                                    }
                                } else {
                                    throw new TransactionUnavailableException("Transaction not available!");
                                }
                            } else {
                                throw new TransactionAlreadyExistsException("Transaction exists! ");
                            }
                        } else if (((SIPServerTransaction) this.sipStack.findTransaction((SIPRequest) request, true)) == null) {
                            SIPServerTransaction transaction2 = (SIPServerTransaction) ((SIPRequest) request).getTransaction();
                            if (transaction2 != null) {
                                if (transaction2.getOriginalRequest() == null) {
                                    transaction2.setOriginalRequest(sipRequest);
                                }
                                this.sipStack.mapTransaction(transaction2);
                                SIPDialog dialog3 = this.sipStack.getDialog(sipRequest.getDialogId(true));
                                if (dialog3 != null) {
                                    dialog3.addTransaction(transaction2);
                                    dialog3.addRoute(sipRequest);
                                    transaction2.setDialog(dialog3, sipRequest.getDialogId(true));
                                }
                                this.sipStack.releaseSem();
                                return transaction2;
                            }
                            SIPServerTransaction transaction3 = this.sipStack.createServerTransaction((MessageChannel) sipRequest.getMessageChannel());
                            if (transaction3 != null) {
                                transaction3.setOriginalRequest(sipRequest);
                                this.sipStack.mapTransaction(transaction3);
                                SIPDialog dialog4 = this.sipStack.getDialog(sipRequest.getDialogId(true));
                                if (dialog4 != null) {
                                    dialog4.addTransaction(transaction3);
                                    dialog4.addRoute(sipRequest);
                                    transaction3.setDialog(dialog4, sipRequest.getDialogId(true));
                                }
                                this.sipStack.releaseSem();
                                return transaction3;
                            }
                            throw new TransactionUnavailableException("Transaction unavailable -- too many servrer transactions");
                        } else {
                            throw new TransactionAlreadyExistsException("Transaction exists! ");
                        }
                        this.sipStack.releaseSem();
                        return transaction;
                    } catch (IOException e) {
                        throw new TransactionUnavailableException("Could not send back provisional response!");
                    } catch (IOException e2) {
                        throw new TransactionUnavailableException("Error sending provisional response");
                    } catch (Throwable th) {
                        this.sipStack.releaseSem();
                        throw th;
                    }
                } else {
                    throw new TransactionUnavailableException("Transaction not available -- could not acquire stack lock");
                }
            } catch (ParseException ex) {
                throw new TransactionUnavailableException(ex.getMessage(), ex);
            }
        } else {
            throw new TransactionUnavailableException("Stack is stopped");
        }
    }

    public SipStack getSipStack() {
        return this.sipStack;
    }

    public void removeSipListener(SipListener sipListener2) {
        if (sipListener2 == getSipListener()) {
            this.sipListener = null;
        }
        boolean found = false;
        Iterator<SipProviderImpl> it = this.sipStack.getSipProviders();
        while (it.hasNext()) {
            if (it.next().getSipListener() != null) {
                found = true;
            }
        }
        if (!found) {
            this.sipStack.sipListener = null;
        }
    }

    public void sendRequest(Request request) throws SipException {
        StringBuilder sb;
        StackLogger stackLogger;
        if (this.sipStack.isAlive()) {
            if (((SIPRequest) request).getRequestLine() != null && request.getMethod().equals("ACK")) {
                Dialog dialog = this.sipStack.getDialog(((SIPRequest) request).getDialogId(false));
                if (!(dialog == null || dialog.getState() == null || !this.sipStack.isLoggingEnabled())) {
                    StackLogger stackLogger2 = this.sipStack.getStackLogger();
                    stackLogger2.logWarning("Dialog exists -- you may want to use Dialog.sendAck() " + dialog.getState());
                }
            }
            Hop hop = this.sipStack.getRouter((SIPRequest) request).getNextHop(request);
            if (hop != null) {
                SIPRequest sipRequest = (SIPRequest) request;
                if (sipRequest.isNullRequest() || sipRequest.getTopmostVia() != null) {
                    try {
                        if (!sipRequest.isNullRequest()) {
                            Via via = sipRequest.getTopmostVia();
                            String branch = via.getBranch();
                            if (branch == null || branch.length() == 0) {
                                via.setBranch(sipRequest.getTransactionId());
                            }
                        }
                        MessageChannel messageChannel = null;
                        if (this.listeningPoints.containsKey(hop.getTransport().toUpperCase())) {
                            messageChannel = this.sipStack.createRawMessageChannel(getListeningPoint(hop.getTransport()).getIPAddress(), getListeningPoint(hop.getTransport()).getPort(), hop);
                        }
                        if (messageChannel != null) {
                            messageChannel.sendMessage(sipRequest, hop);
                            if (this.sipStack.isLoggingEnabled()) {
                                stackLogger = this.sipStack.getStackLogger();
                                sb = new StringBuilder();
                                sb.append("done sending ");
                                sb.append(request.getMethod());
                                sb.append(" to hop ");
                                sb.append(hop);
                                stackLogger.logDebug(sb.toString());
                                return;
                            }
                            return;
                        }
                        throw new SipException("Could not create a message channel for " + hop.toString());
                    } catch (IOException ex) {
                        if (this.sipStack.isLoggingEnabled()) {
                            this.sipStack.getStackLogger().logException(ex);
                        }
                        throw new SipException("IO Exception occured while Sending Request", ex);
                    } catch (ParseException ex1) {
                        InternalErrorHandler.handleException((Exception) ex1);
                        if (this.sipStack.isLoggingEnabled()) {
                            stackLogger = this.sipStack.getStackLogger();
                            sb = new StringBuilder();
                        }
                    } catch (Throwable th) {
                        if (this.sipStack.isLoggingEnabled()) {
                            StackLogger stackLogger3 = this.sipStack.getStackLogger();
                            stackLogger3.logDebug("done sending " + request.getMethod() + " to hop " + hop);
                        }
                        throw th;
                    }
                } else {
                    throw new SipException("Invalid SipRequest -- no via header!");
                }
            } else {
                throw new SipException("could not determine next hop!");
            }
        } else {
            throw new SipException("Stack is stopped.");
        }
    }

    public void sendResponse(Response response) throws SipException {
        if (this.sipStack.isAlive()) {
            SIPResponse sipResponse = (SIPResponse) response;
            Via via = sipResponse.getTopmostVia();
            if (via != null) {
                SIPServerTransaction st = (SIPServerTransaction) this.sipStack.findTransaction((SIPMessage) response, true);
                if (st == null || st.getState() == TransactionState.TERMINATED || !isAutomaticDialogSupportEnabled()) {
                    String transport = via.getTransport();
                    String host = via.getReceived();
                    if (host == null) {
                        host = via.getHost();
                    }
                    int port2 = via.getRPort();
                    if (port2 == -1) {
                        port2 = via.getPort();
                        if (port2 == -1) {
                            if (transport.equalsIgnoreCase(ListeningPoint.TLS)) {
                                port2 = 5061;
                            } else {
                                port2 = 5060;
                            }
                        }
                    }
                    if (host.indexOf(Separators.COLON) > 0 && host.indexOf("[") < 0) {
                        host = "[" + host + "]";
                    }
                    Hop hop = this.sipStack.getAddressResolver().resolveAddress(new HopImpl(host, port2, transport));
                    try {
                        ListeningPointImpl listeningPoint = (ListeningPointImpl) getListeningPoint(transport);
                        if (listeningPoint != null) {
                            this.sipStack.createRawMessageChannel(getListeningPoint(hop.getTransport()).getIPAddress(), listeningPoint.port, hop).sendMessage(sipResponse);
                            return;
                        }
                        throw new SipException("whoopsa daisy! no listening point found for transport " + transport);
                    } catch (IOException ex) {
                        throw new SipException(ex.getMessage());
                    }
                } else {
                    throw new SipException("Transaction exists -- cannot send response statelessly");
                }
            } else {
                throw new SipException("No via header in response!");
            }
        } else {
            throw new SipException("Stack is stopped");
        }
    }

    public synchronized void setListeningPoint(ListeningPoint listeningPoint) {
        if (listeningPoint != null) {
            ListeningPointImpl lp = (ListeningPointImpl) listeningPoint;
            lp.sipProvider = this;
            String transport = lp.getTransport().toUpperCase();
            this.address = listeningPoint.getIPAddress();
            this.port = listeningPoint.getPort();
            this.listeningPoints.clear();
            this.listeningPoints.put(transport, listeningPoint);
        } else {
            throw new NullPointerException("Null listening point");
        }
    }

    public Dialog getNewDialog(Transaction transaction) throws SipException {
        SIPDialog dialog;
        if (transaction == null) {
            throw new NullPointerException("Null transaction!");
        } else if (!this.sipStack.isAlive()) {
            throw new SipException("Stack is stopped.");
        } else if (!isAutomaticDialogSupportEnabled()) {
            SipStackImpl sipStackImpl = this.sipStack;
            if (SipStackImpl.isDialogCreated(transaction.getRequest().getMethod())) {
                SIPTransaction sipTransaction = (SIPTransaction) transaction;
                if (transaction instanceof ServerTransaction) {
                    SIPServerTransaction st = (SIPServerTransaction) transaction;
                    Response response = st.getLastResponse();
                    if (response == null || response.getStatusCode() == 100) {
                        SIPRequest sipRequest = (SIPRequest) transaction.getRequest();
                        dialog = this.sipStack.getDialog(sipRequest.getDialogId(true));
                        if (dialog == null) {
                            dialog = this.sipStack.createDialog((SIPTransaction) transaction);
                            dialog.addTransaction(sipTransaction);
                            dialog.addRoute(sipRequest);
                            sipTransaction.setDialog(dialog, null);
                        } else {
                            sipTransaction.setDialog(dialog, sipRequest.getDialogId(true));
                        }
                        if (sipRequest.getMethod().equals("INVITE") && isDialogErrorsAutomaticallyHandled()) {
                            this.sipStack.putInMergeTable(st, sipRequest);
                        }
                    } else {
                        throw new SipException("Cannot set dialog after response has been sent");
                    }
                } else {
                    SIPClientTransaction sipClientTx = (SIPClientTransaction) transaction;
                    if (sipClientTx.getLastResponse() == null) {
                        if (this.sipStack.getDialog(((SIPRequest) sipClientTx.getRequest()).getDialogId(false)) == null) {
                            dialog = this.sipStack.createDialog(sipTransaction);
                            sipClientTx.setDialog(dialog, null);
                        } else {
                            throw new SipException("Dialog already exists!");
                        }
                    } else {
                        throw new SipException("Cannot call this method after response is received!");
                    }
                }
                dialog.addEventListener(this);
                return dialog;
            }
            throw new SipException("Dialog cannot be created for this method " + transaction.getRequest().getMethod());
        } else {
            throw new SipException(" Error - AUTOMATIC_DIALOG_SUPPORT is on");
        }
    }

    public void transactionErrorEvent(SIPTransactionErrorEvent transactionErrorEvent) {
        TimeoutEvent ev;
        TimeoutEvent ev2;
        TimeoutEvent ev3;
        SIPTransaction transaction = (SIPTransaction) transactionErrorEvent.getSource();
        if (transactionErrorEvent.getErrorID() == 2) {
            if (this.sipStack.isLoggingEnabled()) {
                StackLogger stackLogger = this.sipStack.getStackLogger();
                stackLogger.logDebug("TransportError occured on " + transaction);
            }
            Object errorObject = transactionErrorEvent.getSource();
            Timeout timeout = Timeout.TRANSACTION;
            if (errorObject instanceof SIPServerTransaction) {
                ev3 = new TimeoutEvent((Object) this, (ServerTransaction) errorObject, timeout);
            } else {
                Hop hop = ((SIPClientTransaction) errorObject).getNextHop();
                if (this.sipStack.getRouter() instanceof RouterExt) {
                    ((RouterExt) this.sipStack.getRouter()).transactionTimeout(hop);
                }
                ev3 = new TimeoutEvent((Object) this, (ClientTransaction) errorObject, timeout);
            }
            handleEvent(ev3, (SIPTransaction) errorObject);
        } else if (transactionErrorEvent.getErrorID() == 1) {
            Object errorObject2 = transactionErrorEvent.getSource();
            Timeout timeout2 = Timeout.TRANSACTION;
            if (errorObject2 instanceof SIPServerTransaction) {
                ev2 = new TimeoutEvent((Object) this, (ServerTransaction) errorObject2, timeout2);
            } else {
                Hop hop2 = ((SIPClientTransaction) errorObject2).getNextHop();
                if (this.sipStack.getRouter() instanceof RouterExt) {
                    ((RouterExt) this.sipStack.getRouter()).transactionTimeout(hop2);
                }
                ev2 = new TimeoutEvent((Object) this, (ClientTransaction) errorObject2, timeout2);
            }
            handleEvent(ev2, (SIPTransaction) errorObject2);
        } else if (transactionErrorEvent.getErrorID() == 3) {
            Object errorObject3 = transactionErrorEvent.getSource();
            if (((Transaction) errorObject3).getDialog() != null) {
                InternalErrorHandler.handleException("Unexpected event !", this.sipStack.getStackLogger());
            }
            Timeout timeout3 = Timeout.RETRANSMIT;
            if (errorObject3 instanceof SIPServerTransaction) {
                ev = new TimeoutEvent((Object) this, (ServerTransaction) errorObject3, timeout3);
            } else {
                ev = new TimeoutEvent((Object) this, (ClientTransaction) errorObject3, timeout3);
            }
            handleEvent(ev, (SIPTransaction) errorObject3);
        }
    }

    public synchronized void dialogErrorEvent(SIPDialogErrorEvent dialogErrorEvent) {
        SIPDialog sipDialog = (SIPDialog) dialogErrorEvent.getSource();
        DialogTimeoutEvent.Reason reason = DialogTimeoutEvent.Reason.AckNotReceived;
        if (dialogErrorEvent.getErrorID() == 2) {
            reason = DialogTimeoutEvent.Reason.AckNotSent;
        } else if (dialogErrorEvent.getErrorID() == 3) {
            reason = DialogTimeoutEvent.Reason.ReInviteTimeout;
        }
        if (this.sipStack.isLoggingEnabled()) {
            StackLogger stackLogger = this.sipStack.getStackLogger();
            stackLogger.logDebug("Dialog TimeoutError occured on " + sipDialog);
        }
        handleEvent(new DialogTimeoutEvent(this, sipDialog, reason), null);
    }

    public synchronized ListeningPoint[] getListeningPoints() {
        ListeningPoint[] retval;
        retval = new ListeningPointImpl[this.listeningPoints.size()];
        this.listeningPoints.values().toArray(retval);
        return retval;
    }

    public synchronized void addListeningPoint(ListeningPoint listeningPoint) throws ObjectInUseException {
        ListeningPointImpl lp = (ListeningPointImpl) listeningPoint;
        if (lp.sipProvider != null) {
            if (lp.sipProvider != this) {
                throw new ObjectInUseException("Listening point assigned to another provider");
            }
        }
        String transport = lp.getTransport().toUpperCase();
        if (this.listeningPoints.isEmpty()) {
            this.address = listeningPoint.getIPAddress();
            this.port = listeningPoint.getPort();
        } else if (!this.address.equals(listeningPoint.getIPAddress()) || this.port != listeningPoint.getPort()) {
            throw new ObjectInUseException("Provider already has different IP Address associated");
        }
        if (this.listeningPoints.containsKey(transport)) {
            if (this.listeningPoints.get(transport) != listeningPoint) {
                throw new ObjectInUseException("Listening point already assigned for transport!");
            }
        }
        lp.sipProvider = this;
        this.listeningPoints.put(transport, lp);
    }

    public synchronized void removeListeningPoint(ListeningPoint listeningPoint) throws ObjectInUseException {
        ListeningPointImpl lp = (ListeningPointImpl) listeningPoint;
        if (!lp.messageProcessor.inUse()) {
            this.listeningPoints.remove(lp.getTransport().toUpperCase());
        } else {
            throw new ObjectInUseException("Object is in use");
        }
    }

    public synchronized void removeListeningPoints() {
        Iterator it = this.listeningPoints.values().iterator();
        while (it.hasNext()) {
            ((ListeningPointImpl) it.next()).messageProcessor.stop();
            it.remove();
        }
    }

    public void setAutomaticDialogSupportEnabled(boolean automaticDialogSupportEnabled2) {
        this.automaticDialogSupportEnabled = automaticDialogSupportEnabled2;
        if (this.automaticDialogSupportEnabled) {
            this.dialogErrorsAutomaticallyHandled = true;
        }
    }

    public boolean isAutomaticDialogSupportEnabled() {
        return this.automaticDialogSupportEnabled;
    }

    public void setDialogErrorsAutomaticallyHandled() {
        this.dialogErrorsAutomaticallyHandled = true;
    }

    public boolean isDialogErrorsAutomaticallyHandled() {
        return this.dialogErrorsAutomaticallyHandled;
    }

    public SipListener getSipListener() {
        return this.sipListener;
    }
}
