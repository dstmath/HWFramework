package gov.nist.javax.sip;

import gov.nist.core.InternalErrorHandler;
import gov.nist.core.Separators;
import gov.nist.javax.sip.address.ParameterNames;
import gov.nist.javax.sip.address.SipUri;
import gov.nist.javax.sip.header.Contact;
import gov.nist.javax.sip.header.Event;
import gov.nist.javax.sip.header.RetryAfter;
import gov.nist.javax.sip.header.Route;
import gov.nist.javax.sip.header.RouteList;
import gov.nist.javax.sip.message.MessageFactoryImpl;
import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.message.SIPResponse;
import gov.nist.javax.sip.stack.MessageChannel;
import gov.nist.javax.sip.stack.SIPClientTransaction;
import gov.nist.javax.sip.stack.SIPDialog;
import gov.nist.javax.sip.stack.SIPServerTransaction;
import gov.nist.javax.sip.stack.SIPTransaction;
import gov.nist.javax.sip.stack.SIPTransactionStack;
import gov.nist.javax.sip.stack.ServerRequestInterface;
import gov.nist.javax.sip.stack.ServerResponseInterface;
import java.io.IOException;
import java.util.EventObject;
import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.DialogState;
import javax.sip.ListeningPoint;
import javax.sip.RequestEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.TransactionState;
import javax.sip.header.Header;
import javax.sip.header.ReferToHeader;
import javax.sip.header.ServerHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

class DialogFilter implements ServerRequestInterface, ServerResponseInterface {
    protected ListeningPointImpl listeningPoint;
    private SipStackImpl sipStack;
    protected SIPTransaction transactionChannel;

    public DialogFilter(SipStackImpl sipStack) {
        this.sipStack = sipStack;
    }

    private void sendRequestPendingResponse(SIPRequest sipRequest, SIPServerTransaction transaction) {
        Response sipResponse = sipRequest.createResponse(Response.REQUEST_PENDING);
        ServerHeader serverHeader = MessageFactoryImpl.getDefaultServerHeader();
        if (serverHeader != null) {
            sipResponse.setHeader((Header) serverHeader);
        }
        try {
            RetryAfter retryAfter = new RetryAfter();
            retryAfter.setRetryAfter(1);
            sipResponse.setHeader((Header) retryAfter);
            if (sipRequest.getMethod().equals("INVITE")) {
                this.sipStack.addTransactionPendingAck(transaction);
            }
            transaction.sendResponse(sipResponse);
            transaction.releaseSem();
        } catch (Exception ex) {
            this.sipStack.getStackLogger().logError("Problem sending error response", ex);
            transaction.releaseSem();
            this.sipStack.removeTransaction(transaction);
        }
    }

    private void sendBadRequestResponse(SIPRequest sipRequest, SIPServerTransaction transaction, String reasonPhrase) {
        Response sipResponse = sipRequest.createResponse(Response.BAD_REQUEST);
        if (reasonPhrase != null) {
            sipResponse.setReasonPhrase(reasonPhrase);
        }
        ServerHeader serverHeader = MessageFactoryImpl.getDefaultServerHeader();
        if (serverHeader != null) {
            sipResponse.setHeader((Header) serverHeader);
        }
        try {
            if (sipRequest.getMethod().equals("INVITE")) {
                this.sipStack.addTransactionPendingAck(transaction);
            }
            transaction.sendResponse(sipResponse);
            transaction.releaseSem();
        } catch (Exception ex) {
            this.sipStack.getStackLogger().logError("Problem sending error response", ex);
            transaction.releaseSem();
            this.sipStack.removeTransaction(transaction);
        }
    }

    private void sendCallOrTransactionDoesNotExistResponse(SIPRequest sipRequest, SIPServerTransaction transaction) {
        Response sipResponse = sipRequest.createResponse(Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST);
        ServerHeader serverHeader = MessageFactoryImpl.getDefaultServerHeader();
        if (serverHeader != null) {
            sipResponse.setHeader((Header) serverHeader);
        }
        try {
            if (sipRequest.getMethod().equals("INVITE")) {
                this.sipStack.addTransactionPendingAck(transaction);
            }
            transaction.sendResponse(sipResponse);
            transaction.releaseSem();
        } catch (Exception ex) {
            this.sipStack.getStackLogger().logError("Problem sending error response", ex);
            transaction.releaseSem();
            this.sipStack.removeTransaction(transaction);
        }
    }

    private void sendLoopDetectedResponse(SIPRequest sipRequest, SIPServerTransaction transaction) {
        Response sipResponse = sipRequest.createResponse(Response.LOOP_DETECTED);
        ServerHeader serverHeader = MessageFactoryImpl.getDefaultServerHeader();
        if (serverHeader != null) {
            sipResponse.setHeader((Header) serverHeader);
        }
        try {
            this.sipStack.addTransactionPendingAck(transaction);
            transaction.sendResponse(sipResponse);
            transaction.releaseSem();
        } catch (Exception ex) {
            this.sipStack.getStackLogger().logError("Problem sending error response", ex);
            transaction.releaseSem();
            this.sipStack.removeTransaction(transaction);
        }
    }

    private void sendServerInternalErrorResponse(SIPRequest sipRequest, SIPServerTransaction transaction) {
        if (this.sipStack.isLoggingEnabled()) {
            this.sipStack.getStackLogger().logDebug("Sending 500 response for out of sequence message");
        }
        Response sipResponse = sipRequest.createResponse(500);
        sipResponse.setReasonPhrase("Request out of order");
        if (MessageFactoryImpl.getDefaultServerHeader() != null) {
            sipResponse.setHeader((Header) MessageFactoryImpl.getDefaultServerHeader());
        }
        try {
            RetryAfter retryAfter = new RetryAfter();
            retryAfter.setRetryAfter(10);
            sipResponse.setHeader((Header) retryAfter);
            this.sipStack.addTransactionPendingAck(transaction);
            transaction.sendResponse(sipResponse);
            transaction.releaseSem();
        } catch (Exception ex) {
            this.sipStack.getStackLogger().logError("Problem sending response", ex);
            transaction.releaseSem();
            this.sipStack.removeTransaction(transaction);
        }
    }

    public void processRequest(SIPRequest sipRequest, MessageChannel incomingMessageChannel) {
        if (this.sipStack.isLoggingEnabled()) {
            this.sipStack.getStackLogger().logDebug("PROCESSING INCOMING REQUEST " + sipRequest + " transactionChannel = " + this.transactionChannel + " listening point = " + this.listeningPoint.getIPAddress() + Separators.COLON + this.listeningPoint.getPort());
        }
        if (this.listeningPoint == null) {
            if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logDebug("Dropping message: No listening point registered!");
            }
            return;
        }
        SipStackImpl sipStack = (SipStackImpl) this.transactionChannel.getSIPStack();
        SipProviderImpl sipProvider = this.listeningPoint.getProvider();
        if (sipProvider == null) {
            if (sipStack.isLoggingEnabled()) {
                sipStack.getStackLogger().logDebug("No provider - dropping !!");
            }
            return;
        }
        if (sipStack == null) {
            InternalErrorHandler.handleException("Egads! no sip stack!");
        }
        SIPTransaction transaction = (SIPServerTransaction) this.transactionChannel;
        if (transaction != null && sipStack.isLoggingEnabled()) {
            sipStack.getStackLogger().logDebug("transaction state = " + transaction.getState());
        }
        String dialogId = sipRequest.getDialogId(true);
        Dialog dialog = sipStack.getDialog(dialogId);
        if (!(dialog == null || sipProvider == dialog.getSipProvider())) {
            Contact contact = dialog.getMyContactHeader();
            if (contact != null) {
                SipUri contactUri = (SipUri) contact.getAddress().getURI();
                String ipAddress = contactUri.getHost();
                int contactPort = contactUri.getPort();
                String contactTransport = contactUri.getTransportParam();
                if (contactTransport == null) {
                    contactTransport = ParameterNames.UDP;
                }
                if (contactPort == -1) {
                    if (contactTransport.equals(ParameterNames.UDP) || contactTransport.equals(ParameterNames.TCP)) {
                        contactPort = 5060;
                    } else {
                        contactPort = 5061;
                    }
                }
                if (!(ipAddress == null || (ipAddress.equals(this.listeningPoint.getIPAddress()) && contactPort == this.listeningPoint.getPort()))) {
                    if (sipStack.isLoggingEnabled()) {
                        sipStack.getStackLogger().logDebug("nulling dialog -- listening point mismatch!  " + contactPort + "  lp port = " + this.listeningPoint.getPort());
                    }
                    dialog = null;
                }
            }
        }
        if (sipProvider.isAutomaticDialogSupportEnabled() && sipProvider.isDialogErrorsAutomaticallyHandled() && sipRequest.getToTag() == null && sipStack.findMergedTransaction(sipRequest) != null) {
            sendLoopDetectedResponse(sipRequest, transaction);
            return;
        }
        if (sipStack.isLoggingEnabled()) {
            sipStack.getStackLogger().logDebug("dialogId = " + dialogId);
            sipStack.getStackLogger().logDebug("dialog = " + dialog);
        }
        if (!(sipRequest.getHeader("Route") == null || transaction.getDialog() == null)) {
            RouteList routes = sipRequest.getRouteHeaders();
            SipUri uri = (SipUri) ((Route) routes.getFirst()).getAddress().getURI();
            int port;
            if (uri.getHostPort().hasPort()) {
                port = uri.getHostPort().getPort();
            } else if (this.listeningPoint.getTransport().equalsIgnoreCase(ListeningPoint.TLS)) {
                port = 5061;
            } else {
                port = 5060;
            }
            String host = uri.getHost();
            if ((host.equals(this.listeningPoint.getIPAddress()) || host.equalsIgnoreCase(this.listeningPoint.getSentBy())) && port == this.listeningPoint.getPort()) {
                if (routes.size() == 1) {
                    sipRequest.removeHeader("Route");
                } else {
                    routes.removeFirst();
                }
            }
        }
        SIPTransaction lastTransaction;
        SIPServerTransaction st;
        SIPResponse response;
        if (sipRequest.getMethod().equals(Request.REFER) && dialog != null && sipProvider.isDialogErrorsAutomaticallyHandled()) {
            if (((ReferToHeader) sipRequest.getHeader(ReferToHeader.NAME)) == null) {
                sendBadRequestResponse(sipRequest, transaction, "Refer-To header is missing");
                return;
            }
            lastTransaction = dialog.getLastTransaction();
            if (lastTransaction != null && sipProvider.isDialogErrorsAutomaticallyHandled()) {
                SIPRequest lastRequest = (SIPRequest) lastTransaction.getRequest();
                if (lastTransaction instanceof SIPServerTransaction) {
                    if (!dialog.isAckSeen() && lastRequest.getMethod().equals("INVITE")) {
                        sendRequestPendingResponse(sipRequest, transaction);
                        return;
                    }
                } else if (lastTransaction instanceof SIPClientTransaction) {
                    long cseqno = lastRequest.getCSeqHeader().getSeqNumber();
                    if (lastRequest.getMethod().equals("INVITE") && (dialog.isAckSent(cseqno) ^ 1) != 0) {
                        sendRequestPendingResponse(sipRequest, transaction);
                        return;
                    }
                }
            }
        } else if (sipRequest.getMethod().equals(Request.UPDATE)) {
            if (sipProvider.isAutomaticDialogSupportEnabled() && dialog == null) {
                sendCallOrTransactionDoesNotExistResponse(sipRequest, transaction);
                return;
            }
        } else if (sipRequest.getMethod().equals("ACK")) {
            if (transaction == null || !transaction.isInviteTransaction()) {
                if (sipStack.isLoggingEnabled()) {
                    sipStack.getStackLogger().logDebug("Processing ACK for dialog " + dialog);
                }
                SIPServerTransaction ackTransaction;
                if (dialog == null) {
                    if (sipStack.isLoggingEnabled()) {
                        sipStack.getStackLogger().logDebug("Dialog does not exist " + sipRequest.getFirstLine() + " isServerTransaction = " + true);
                    }
                    st = sipStack.getRetransmissionAlertTransaction(dialogId);
                    if (st != null && st.isRetransmissionAlertEnabled()) {
                        st.disableRetransmissionAlerts();
                    }
                    ackTransaction = sipStack.findTransactionPendingAck(sipRequest);
                    if (ackTransaction != null) {
                        if (sipStack.isLoggingEnabled()) {
                            sipStack.getStackLogger().logDebug("Found Tx pending ACK");
                        }
                        try {
                            ackTransaction.setAckSeen();
                            sipStack.removeTransaction(ackTransaction);
                            sipStack.removeTransactionPendingAck(ackTransaction);
                        } catch (Exception ex) {
                            if (sipStack.isLoggingEnabled()) {
                                sipStack.getStackLogger().logError("Problem terminating transaction", ex);
                            }
                        }
                        return;
                    }
                } else if (dialog.handleAck(transaction)) {
                    transaction.passToListener();
                    dialog.addTransaction(transaction);
                    dialog.addRoute(sipRequest);
                    transaction.setDialog(dialog, dialogId);
                    if (sipRequest.getMethod().equals("INVITE") && sipProvider.isDialogErrorsAutomaticallyHandled()) {
                        sipStack.putInMergeTable(transaction, sipRequest);
                    }
                    if (sipStack.deliverTerminatedEventForAck) {
                        try {
                            sipStack.addTransaction((SIPServerTransaction) transaction);
                            transaction.scheduleAckRemoval();
                        } catch (IOException e) {
                        }
                    } else {
                        transaction.setMapped(true);
                    }
                } else if (dialog.isSequnceNumberValidation()) {
                    if (sipStack.isLoggingEnabled()) {
                        sipStack.getStackLogger().logDebug("Dropping ACK - cannot find a transaction or dialog");
                    }
                    ackTransaction = sipStack.findTransactionPendingAck(sipRequest);
                    if (ackTransaction != null) {
                        if (sipStack.isLoggingEnabled()) {
                            sipStack.getStackLogger().logDebug("Found Tx pending ACK");
                        }
                        try {
                            ackTransaction.setAckSeen();
                            sipStack.removeTransaction(ackTransaction);
                            sipStack.removeTransactionPendingAck(ackTransaction);
                        } catch (Exception ex2) {
                            if (sipStack.isLoggingEnabled()) {
                                sipStack.getStackLogger().logError("Problem terminating transaction", ex2);
                            }
                        }
                    }
                    return;
                } else {
                    if (sipStack.isLoggingEnabled()) {
                        sipStack.getStackLogger().logDebug("Dialog exists with loose dialog validation " + sipRequest.getFirstLine() + " isServerTransaction = " + true + " dialog = " + dialog.getDialogId());
                    }
                    st = sipStack.getRetransmissionAlertTransaction(dialogId);
                    if (st != null && st.isRetransmissionAlertEnabled()) {
                        st.disableRetransmissionAlerts();
                    }
                }
            } else if (sipStack.isLoggingEnabled()) {
                sipStack.getStackLogger().logDebug("Processing ACK for INVITE Tx ");
            }
        } else if (sipRequest.getMethod().equals(Request.PRACK)) {
            if (sipStack.isLoggingEnabled()) {
                sipStack.getStackLogger().logDebug("Processing PRACK for dialog " + dialog);
            }
            if (dialog == null && sipProvider.isAutomaticDialogSupportEnabled()) {
                if (sipStack.isLoggingEnabled()) {
                    sipStack.getStackLogger().logDebug("Dialog does not exist " + sipRequest.getFirstLine() + " isServerTransaction = " + true);
                }
                if (sipStack.isLoggingEnabled()) {
                    sipStack.getStackLogger().logDebug("Sending 481 for PRACK - automatic dialog support is enabled -- cant find dialog!");
                }
                try {
                    sipProvider.sendResponse(sipRequest.createResponse(Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST));
                } catch (SipException e2) {
                    sipStack.getStackLogger().logError("error sending response", e2);
                }
                if (transaction != null) {
                    sipStack.removeTransaction(transaction);
                    transaction.releaseSem();
                }
                return;
            } else if (dialog != null) {
                if (dialog.handlePrack(sipRequest)) {
                    try {
                        sipStack.addTransaction((SIPServerTransaction) transaction);
                        dialog.addTransaction(transaction);
                        dialog.addRoute(sipRequest);
                        transaction.setDialog(dialog, dialogId);
                    } catch (Exception ex22) {
                        InternalErrorHandler.handleException(ex22);
                    }
                } else {
                    if (sipStack.isLoggingEnabled()) {
                        sipStack.getStackLogger().logDebug("Dropping out of sequence PRACK ");
                    }
                    if (transaction != null) {
                        sipStack.removeTransaction(transaction);
                        transaction.releaseSem();
                    }
                    return;
                }
            } else if (sipStack.isLoggingEnabled()) {
                sipStack.getStackLogger().logDebug("Processing PRACK without a DIALOG -- this must be a proxy element");
            }
        } else if (sipRequest.getMethod().equals("BYE")) {
            if (dialog != null && (dialog.isRequestConsumable(sipRequest) ^ 1) != 0) {
                if (sipStack.isLoggingEnabled()) {
                    sipStack.getStackLogger().logDebug("Dropping out of sequence BYE " + dialog.getRemoteSeqNumber() + Separators.SP + sipRequest.getCSeq().getSeqNumber());
                }
                if (dialog.getRemoteSeqNumber() >= sipRequest.getCSeq().getSeqNumber() && transaction.getState() == TransactionState.TRYING) {
                    sendServerInternalErrorResponse(sipRequest, transaction);
                }
                if (transaction != null) {
                    sipStack.removeTransaction(transaction);
                }
                return;
            } else if (dialog == null && sipProvider.isAutomaticDialogSupportEnabled()) {
                response = sipRequest.createResponse(Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST);
                response.setReasonPhrase("Dialog Not Found");
                if (sipStack.isLoggingEnabled()) {
                    sipStack.getStackLogger().logDebug("dropping request -- automatic dialog support enabled and dialog does not exist!");
                }
                try {
                    transaction.sendResponse((Response) response);
                } catch (Exception ex3) {
                    sipStack.getStackLogger().logError("Error in sending response", ex3);
                }
                if (transaction != null) {
                    sipStack.removeTransaction(transaction);
                    transaction.releaseSem();
                }
                return;
            } else {
                if (!(transaction == null || dialog == null)) {
                    try {
                        if (sipProvider == dialog.getSipProvider()) {
                            sipStack.addTransaction((SIPServerTransaction) transaction);
                            dialog.addTransaction(transaction);
                            transaction.setDialog(dialog, dialogId);
                        }
                    } catch (Exception ex4) {
                        InternalErrorHandler.handleException(ex4);
                    }
                }
                if (sipStack.isLoggingEnabled()) {
                    sipStack.getStackLogger().logDebug("BYE Tx = " + transaction + " isMapped =" + transaction.isTransactionMapped());
                }
            }
        } else if (sipRequest.getMethod().equals(Request.CANCEL)) {
            st = (SIPServerTransaction) sipStack.findCancelTransaction(sipRequest, true);
            if (sipStack.isLoggingEnabled()) {
                sipStack.getStackLogger().logDebug("Got a CANCEL, InviteServerTx = " + st + " cancel Server Tx ID = " + transaction + " isMapped = " + transaction.isTransactionMapped());
            }
            if (sipRequest.getMethod().equals(Request.CANCEL)) {
                if (st != null && st.getState() == SIPTransaction.TERMINATED_STATE) {
                    if (sipStack.isLoggingEnabled()) {
                        sipStack.getStackLogger().logDebug("Too late to cancel Transaction");
                    }
                    try {
                        transaction.sendResponse((Response) sipRequest.createResponse(Response.OK));
                    } catch (Exception ex222) {
                        if (ex222.getCause() != null && (ex222.getCause() instanceof IOException)) {
                            st.raiseIOExceptionEvent();
                        }
                    }
                    return;
                } else if (sipStack.isLoggingEnabled()) {
                    sipStack.getStackLogger().logDebug("Cancel transaction = " + st);
                }
            }
            if (transaction != null && st != null && st.getDialog() != null) {
                transaction.setDialog((SIPDialog) st.getDialog(), dialogId);
                dialog = (SIPDialog) st.getDialog();
            } else if (st == null && sipProvider.isAutomaticDialogSupportEnabled() && transaction != null) {
                response = sipRequest.createResponse(Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST);
                if (sipStack.isLoggingEnabled()) {
                    sipStack.getStackLogger().logDebug("dropping request -- automatic dialog support enabled and INVITE ST does not exist!");
                }
                try {
                    sipProvider.sendResponse(response);
                } catch (Exception ex32) {
                    InternalErrorHandler.handleException(ex32);
                }
                if (transaction != null) {
                    sipStack.removeTransaction(transaction);
                    transaction.releaseSem();
                }
                return;
            }
            if (!(st == null || transaction == null)) {
                try {
                    sipStack.addTransaction((SIPServerTransaction) transaction);
                    transaction.setPassToListener();
                    transaction.setInviteTransaction(st);
                    st.acquireSem();
                } catch (Exception ex2222) {
                    InternalErrorHandler.handleException(ex2222);
                }
            }
        } else if (sipRequest.getMethod().equals("INVITE")) {
            if (dialog == null) {
                lastTransaction = null;
            } else {
                lastTransaction = dialog.getInviteTransaction();
            }
            if (dialog == null || transaction == null || r24 == null || sipRequest.getCSeq().getSeqNumber() <= dialog.getRemoteSeqNumber() || !(r24 instanceof SIPServerTransaction) || !sipProvider.isDialogErrorsAutomaticallyHandled() || !dialog.isSequnceNumberValidation() || !r24.isInviteTransaction() || r24.getState() == TransactionState.COMPLETED || r24.getState() == TransactionState.TERMINATED || r24.getState() == TransactionState.CONFIRMED) {
                lastTransaction = dialog == null ? null : dialog.getLastTransaction();
                if (dialog != null && sipProvider.isDialogErrorsAutomaticallyHandled() && lastTransaction != null && lastTransaction.isInviteTransaction() && (lastTransaction instanceof ClientTransaction) && lastTransaction.getLastResponse() != null && lastTransaction.getLastResponse().getStatusCode() == 200 && (dialog.isAckSent(lastTransaction.getLastResponse().getCSeq().getSeqNumber()) ^ 1) != 0) {
                    if (sipStack.isLoggingEnabled()) {
                        sipStack.getStackLogger().logDebug("Sending 491 response for client Dialog ACK not sent.");
                    }
                    sendRequestPendingResponse(sipRequest, transaction);
                    return;
                } else if (dialog != null && lastTransaction != null && sipProvider.isDialogErrorsAutomaticallyHandled() && lastTransaction.isInviteTransaction() && (lastTransaction instanceof ServerTransaction) && (dialog.isAckSeen() ^ 1) != 0) {
                    if (sipStack.isLoggingEnabled()) {
                        sipStack.getStackLogger().logDebug("Sending 491 response for server Dialog ACK not seen.");
                    }
                    sendRequestPendingResponse(sipRequest, transaction);
                    return;
                }
            }
            if (sipStack.isLoggingEnabled()) {
                sipStack.getStackLogger().logDebug("Sending 500 response for out of sequence message");
            }
            sendServerInternalErrorResponse(sipRequest, transaction);
            return;
        }
        if (sipStack.isLoggingEnabled()) {
            sipStack.getStackLogger().logDebug("CHECK FOR OUT OF SEQ MESSAGE " + dialog + " transaction " + transaction);
        }
        if (!(dialog == null || transaction == null || (sipRequest.getMethod().equals("BYE") ^ 1) == 0 || (sipRequest.getMethod().equals(Request.CANCEL) ^ 1) == 0 || (sipRequest.getMethod().equals("ACK") ^ 1) == 0 || (sipRequest.getMethod().equals(Request.PRACK) ^ 1) == 0)) {
            if (dialog.isRequestConsumable(sipRequest)) {
                try {
                    if (sipProvider == dialog.getSipProvider()) {
                        sipStack.addTransaction((SIPServerTransaction) transaction);
                        dialog.addTransaction(transaction);
                        dialog.addRoute(sipRequest);
                        transaction.setDialog(dialog, dialogId);
                    }
                } catch (IOException e3) {
                    transaction.raiseIOExceptionEvent();
                    sipStack.removeTransaction(transaction);
                    return;
                }
            }
            if (sipStack.isLoggingEnabled()) {
                sipStack.getStackLogger().logDebug("Dropping out of sequence message " + dialog.getRemoteSeqNumber() + Separators.SP + sipRequest.getCSeq());
            }
            if (dialog.getRemoteSeqNumber() >= sipRequest.getCSeq().getSeqNumber() && sipProvider.isDialogErrorsAutomaticallyHandled() && (transaction.getState() == TransactionState.TRYING || transaction.getState() == TransactionState.PROCEEDING)) {
                sendServerInternalErrorResponse(sipRequest, transaction);
            }
            return;
        }
        if (sipStack.isLoggingEnabled()) {
            sipStack.getStackLogger().logDebug(sipRequest.getMethod() + " transaction.isMapped = " + transaction.isTransactionMapped());
        }
        EventObject requestEvent;
        if (dialog == null && sipRequest.getMethod().equals("NOTIFY")) {
            SIPTransaction pendingSubscribeClientTx = sipStack.findSubscribeTransaction(sipRequest, this.listeningPoint);
            if (sipStack.isLoggingEnabled()) {
                sipStack.getStackLogger().logDebug("PROCESSING NOTIFY  DIALOG == null " + pendingSubscribeClientTx);
            }
            if (sipProvider.isAutomaticDialogSupportEnabled() && pendingSubscribeClientTx == null && (sipStack.deliverUnsolicitedNotify ^ 1) != 0) {
                try {
                    if (sipStack.isLoggingEnabled()) {
                        sipStack.getStackLogger().logDebug("Could not find Subscription for Notify Tx.");
                    }
                    Response errorResponse = sipRequest.createResponse(Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST);
                    errorResponse.setReasonPhrase("Subscription does not exist");
                    sipProvider.sendResponse(errorResponse);
                    return;
                } catch (Exception ex22222) {
                    sipStack.getStackLogger().logError("Exception while sending error response statelessly", ex22222);
                    return;
                }
            } else if (pendingSubscribeClientTx != null) {
                transaction.setPendingSubscribe(pendingSubscribeClientTx);
                SIPDialog subscriptionDialog = pendingSubscribeClientTx.getDefaultDialog();
                if (subscriptionDialog == null || subscriptionDialog.getDialogId() == null || (subscriptionDialog.getDialogId().equals(dialogId) ^ 1) != 0) {
                    if (subscriptionDialog == null || subscriptionDialog.getDialogId() != null) {
                        subscriptionDialog = pendingSubscribeClientTx.getDialog(dialogId);
                    } else {
                        subscriptionDialog.setDialogId(dialogId);
                    }
                    if (sipStack.isLoggingEnabled()) {
                        sipStack.getStackLogger().logDebug("PROCESSING NOTIFY Subscribe DIALOG " + subscriptionDialog);
                    }
                    if (subscriptionDialog == null && ((sipProvider.isAutomaticDialogSupportEnabled() || pendingSubscribeClientTx.getDefaultDialog() != null) && sipStack.isEventForked(((Event) sipRequest.getHeader("Event")).getEventType()))) {
                        subscriptionDialog = SIPDialog.createFromNOTIFY(pendingSubscribeClientTx, transaction);
                    }
                    if (subscriptionDialog != null) {
                        transaction.setDialog(subscriptionDialog, dialogId);
                        subscriptionDialog.setState(DialogState.CONFIRMED.getValue());
                        sipStack.putDialog(subscriptionDialog);
                        pendingSubscribeClientTx.setDialog(subscriptionDialog, dialogId);
                        if (!transaction.isTransactionMapped()) {
                            this.sipStack.mapTransaction(transaction);
                            transaction.setPassToListener();
                            try {
                                this.sipStack.addTransaction((SIPServerTransaction) transaction);
                            } catch (Exception e4) {
                            }
                        }
                    }
                } else {
                    transaction.setDialog(subscriptionDialog, dialogId);
                    SIPDialog sIPDialog = subscriptionDialog;
                    if (!transaction.isTransactionMapped()) {
                        this.sipStack.mapTransaction(transaction);
                        transaction.setPassToListener();
                        try {
                            this.sipStack.addTransaction((SIPServerTransaction) transaction);
                        } catch (Exception e5) {
                        }
                    }
                    sipStack.putDialog(subscriptionDialog);
                    if (pendingSubscribeClientTx != null) {
                        subscriptionDialog.addTransaction(pendingSubscribeClientTx);
                        pendingSubscribeClientTx.setDialog(subscriptionDialog, dialogId);
                    }
                }
                if (transaction == null || !transaction.isTransactionMapped()) {
                    requestEvent = new RequestEvent(sipProvider, null, subscriptionDialog, sipRequest);
                } else {
                    requestEvent = new RequestEvent(sipProvider, transaction, subscriptionDialog, sipRequest);
                }
            } else {
                if (sipStack.isLoggingEnabled()) {
                    sipStack.getStackLogger().logDebug("could not find subscribe tx");
                }
                requestEvent = new RequestEvent(sipProvider, null, null, sipRequest);
            }
        } else if (transaction == null || !transaction.isTransactionMapped()) {
            requestEvent = new RequestEvent(sipProvider, null, dialog, sipRequest);
        } else {
            requestEvent = new RequestEvent(sipProvider, transaction, dialog, sipRequest);
        }
        sipProvider.handleEvent(sipEvent, transaction);
    }

    public void processResponse(SIPResponse response, MessageChannel incomingMessageChannel, SIPDialog dialog) {
        if (this.sipStack.isLoggingEnabled()) {
            this.sipStack.getStackLogger().logDebug("PROCESSING INCOMING RESPONSE" + response.encodeMessage());
        }
        if (this.listeningPoint == null) {
            if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logError("Dropping message: No listening point registered!");
            }
        } else if (!this.sipStack.checkBranchId() || (Utils.getInstance().responseBelongsToUs(response) ^ 1) == 0) {
            SipProviderImpl sipProvider = this.listeningPoint.getProvider();
            if (sipProvider == null) {
                if (this.sipStack.isLoggingEnabled()) {
                    this.sipStack.getStackLogger().logError("Dropping message:  no provider");
                }
            } else if (sipProvider.getSipListener() == null) {
                if (this.sipStack.isLoggingEnabled()) {
                    this.sipStack.getStackLogger().logError("No listener -- dropping response!");
                }
            } else {
                SIPClientTransaction transaction = this.transactionChannel;
                SipStackImpl sipStackImpl = sipProvider.sipStack;
                if (this.sipStack.isLoggingEnabled()) {
                    sipStackImpl.getStackLogger().logDebug("Transaction = " + transaction);
                }
                if (transaction == null) {
                    if (dialog != null) {
                        if (response.getStatusCode() / 100 != 2) {
                            if (this.sipStack.isLoggingEnabled()) {
                                this.sipStack.getStackLogger().logDebug("Response is not a final response and dialog is found for response -- dropping response!");
                            }
                            return;
                        } else if (dialog.getState() == DialogState.TERMINATED) {
                            if (this.sipStack.isLoggingEnabled()) {
                                this.sipStack.getStackLogger().logDebug("Dialog is terminated -- dropping response!");
                            }
                            return;
                        } else {
                            boolean ackAlreadySent = false;
                            if (dialog.isAckSeen() && dialog.getLastAckSent() != null && dialog.getLastAckSent().getCSeq().getSeqNumber() == response.getCSeq().getSeqNumber()) {
                                ackAlreadySent = true;
                            }
                            if (ackAlreadySent && response.getCSeq().getMethod().equals(dialog.getMethod())) {
                                try {
                                    if (this.sipStack.isLoggingEnabled()) {
                                        this.sipStack.getStackLogger().logDebug("Retransmission of OK detected: Resending last ACK");
                                    }
                                    dialog.resendAck();
                                    return;
                                } catch (SipException ex) {
                                    this.sipStack.getStackLogger().logError("could not resend ack", ex);
                                }
                            }
                        }
                    }
                    if (this.sipStack.isLoggingEnabled()) {
                        this.sipStack.getStackLogger().logDebug("could not find tx, handling statelessly Dialog =  " + dialog);
                    }
                    ResponseEventExt sipEvent = new ResponseEventExt(sipProvider, transaction, dialog, response);
                    if (response.getCSeqHeader().getMethod().equals("INVITE")) {
                        sipEvent.setOriginalTransaction(this.sipStack.getForkedTransaction(response.getTransactionId()));
                    }
                    sipProvider.handleEvent(sipEvent, transaction);
                    return;
                }
                ResponseEventExt responseEvent = new ResponseEventExt(sipProvider, transaction, dialog, response);
                if (response.getCSeqHeader().getMethod().equals("INVITE")) {
                    responseEvent.setOriginalTransaction(this.sipStack.getForkedTransaction(response.getTransactionId()));
                }
                if (!(dialog == null || response.getStatusCode() == 100)) {
                    dialog.setLastResponse(transaction, response);
                    transaction.setDialog(dialog, dialog.getDialogId());
                }
                sipProvider.handleEvent(responseEvent, transaction);
            }
        } else {
            if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logError("Dropping response - topmost VIA header does not originate from this stack");
            }
        }
    }

    public String getProcessingInfo() {
        return null;
    }

    public void processResponse(SIPResponse sipResponse, MessageChannel incomingChannel) {
        String dialogID = sipResponse.getDialogId(false);
        Dialog sipDialog = this.sipStack.getDialog(dialogID);
        String method = sipResponse.getCSeq().getMethod();
        if (this.sipStack.isLoggingEnabled()) {
            this.sipStack.getStackLogger().logDebug("PROCESSING INCOMING RESPONSE: " + sipResponse.encodeMessage());
        }
        if (this.sipStack.checkBranchId() && (Utils.getInstance().responseBelongsToUs(sipResponse) ^ 1) != 0) {
            if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logError("Detected stray response -- dropping");
            }
        } else if (this.listeningPoint == null) {
            if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logDebug("Dropping message: No listening point registered!");
            }
        } else {
            SipProviderImpl sipProvider = this.listeningPoint.getProvider();
            if (sipProvider == null) {
                if (this.sipStack.isLoggingEnabled()) {
                    this.sipStack.getStackLogger().logDebug("Dropping message:  no provider");
                }
            } else if (sipProvider.getSipListener() == null) {
                if (this.sipStack.isLoggingEnabled()) {
                    this.sipStack.getStackLogger().logDebug("Dropping message:  no sipListener registered!");
                }
            } else {
                SIPClientTransaction transaction = this.transactionChannel;
                if (sipDialog == null && transaction != null) {
                    sipDialog = transaction.getDialog(dialogID);
                    if (sipDialog != null && sipDialog.getState() == DialogState.TERMINATED) {
                        sipDialog = null;
                    }
                }
                if (this.sipStack.isLoggingEnabled()) {
                    this.sipStack.getStackLogger().logDebug("Transaction = " + transaction + " sipDialog = " + sipDialog);
                }
                if (this.transactionChannel != null) {
                    String originalFrom = ((SIPRequest) this.transactionChannel.getRequest()).getFromTag();
                    if (((originalFrom == null ? 1 : 0) ^ (sipResponse.getFrom().getTag() == null ? 1 : 0)) != 0) {
                        if (this.sipStack.isLoggingEnabled()) {
                            this.sipStack.getStackLogger().logDebug("From tag mismatch -- dropping response");
                        }
                        return;
                    } else if (!(originalFrom == null || (originalFrom.equalsIgnoreCase(sipResponse.getFrom().getTag()) ^ 1) == 0)) {
                        if (this.sipStack.isLoggingEnabled()) {
                            this.sipStack.getStackLogger().logDebug("From tag mismatch -- dropping response");
                        }
                        return;
                    }
                }
                SipStackImpl sipStackImpl = this.sipStack;
                if (!SIPTransactionStack.isDialogCreated(method) || sipResponse.getStatusCode() == 100 || sipResponse.getFrom().getTag() == null || sipResponse.getTo().getTag() == null || sipDialog != null) {
                    if (!(sipDialog == null || transaction != null || sipDialog.getState() == DialogState.TERMINATED)) {
                        if (sipResponse.getStatusCode() / 100 != 2) {
                            if (this.sipStack.isLoggingEnabled()) {
                                this.sipStack.getStackLogger().logDebug("status code != 200 ; statusCode = " + sipResponse.getStatusCode());
                            }
                        } else if (sipDialog.getState() == DialogState.TERMINATED) {
                            if (this.sipStack.isLoggingEnabled()) {
                                this.sipStack.getStackLogger().logDebug("Dialog is terminated -- dropping response!");
                            }
                            if (sipResponse.getStatusCode() / 100 == 2 && sipResponse.getCSeq().getMethod().equals("INVITE")) {
                                try {
                                    sipDialog.sendAck(sipDialog.createAck(sipResponse.getCSeq().getSeqNumber()));
                                } catch (Exception ex) {
                                    this.sipStack.getStackLogger().logError("Error creating ack", ex);
                                }
                            }
                            return;
                        } else {
                            boolean ackAlreadySent = false;
                            if (sipDialog.isAckSeen() && sipDialog.getLastAckSent() != null && sipDialog.getLastAckSent().getCSeq().getSeqNumber() == sipResponse.getCSeq().getSeqNumber() && sipResponse.getDialogId(false).equals(sipDialog.getLastAckSent().getDialogId(false))) {
                                ackAlreadySent = true;
                            }
                            if (ackAlreadySent && sipResponse.getCSeq().getMethod().equals(sipDialog.getMethod())) {
                                try {
                                    if (this.sipStack.isLoggingEnabled()) {
                                        this.sipStack.getStackLogger().logDebug("resending ACK");
                                    }
                                    sipDialog.resendAck();
                                    return;
                                } catch (SipException e) {
                                }
                            }
                        }
                    }
                } else if (sipProvider.isAutomaticDialogSupportEnabled()) {
                    if (this.transactionChannel == null) {
                        sipDialog = this.sipStack.createDialog(sipProvider, sipResponse);
                    } else if (sipDialog == null) {
                        sipDialog = this.sipStack.createDialog((SIPClientTransaction) this.transactionChannel, sipResponse);
                        this.transactionChannel.setDialog(sipDialog, sipResponse.getDialogId(false));
                    }
                }
                if (this.sipStack.isLoggingEnabled()) {
                    this.sipStack.getStackLogger().logDebug("sending response to TU for processing ");
                }
                if (!(sipDialog == null || sipResponse.getStatusCode() == 100 || sipResponse.getTo().getTag() == null)) {
                    sipDialog.setLastResponse(transaction, sipResponse);
                }
                ResponseEventExt responseEvent = new ResponseEventExt(sipProvider, transaction, sipDialog, sipResponse);
                if (sipResponse.getCSeq().getMethod().equals("INVITE")) {
                    responseEvent.setOriginalTransaction(this.sipStack.getForkedTransaction(sipResponse.getTransactionId()));
                }
                sipProvider.handleEvent(responseEvent, transaction);
            }
        }
    }
}
