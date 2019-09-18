package gov.nist.javax.sip;

import gov.nist.core.InternalErrorHandler;
import gov.nist.core.Separators;
import gov.nist.core.StackLogger;
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
import gov.nist.javax.sip.stack.ServerRequestInterface;
import gov.nist.javax.sip.stack.ServerResponseInterface;
import java.io.IOException;
import javax.sip.ClientTransaction;
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

    public DialogFilter(SipStackImpl sipStack2) {
        this.sipStack = sipStack2;
    }

    private void sendRequestPendingResponse(SIPRequest sipRequest, SIPServerTransaction transaction) {
        SIPResponse sipResponse = sipRequest.createResponse(Response.REQUEST_PENDING);
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
            transaction.sendResponse((Response) sipResponse);
            transaction.releaseSem();
        } catch (Exception ex) {
            this.sipStack.getStackLogger().logError("Problem sending error response", ex);
            transaction.releaseSem();
            this.sipStack.removeTransaction(transaction);
        }
    }

    private void sendBadRequestResponse(SIPRequest sipRequest, SIPServerTransaction transaction, String reasonPhrase) {
        SIPResponse sipResponse = sipRequest.createResponse(Response.BAD_REQUEST);
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
            transaction.sendResponse((Response) sipResponse);
            transaction.releaseSem();
        } catch (Exception ex) {
            this.sipStack.getStackLogger().logError("Problem sending error response", ex);
            transaction.releaseSem();
            this.sipStack.removeTransaction(transaction);
        }
    }

    private void sendCallOrTransactionDoesNotExistResponse(SIPRequest sipRequest, SIPServerTransaction transaction) {
        SIPResponse sipResponse = sipRequest.createResponse(Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST);
        ServerHeader serverHeader = MessageFactoryImpl.getDefaultServerHeader();
        if (serverHeader != null) {
            sipResponse.setHeader((Header) serverHeader);
        }
        try {
            if (sipRequest.getMethod().equals("INVITE")) {
                this.sipStack.addTransactionPendingAck(transaction);
            }
            transaction.sendResponse((Response) sipResponse);
            transaction.releaseSem();
        } catch (Exception ex) {
            this.sipStack.getStackLogger().logError("Problem sending error response", ex);
            transaction.releaseSem();
            this.sipStack.removeTransaction(transaction);
        }
    }

    private void sendLoopDetectedResponse(SIPRequest sipRequest, SIPServerTransaction transaction) {
        SIPResponse sipResponse = sipRequest.createResponse(Response.LOOP_DETECTED);
        ServerHeader serverHeader = MessageFactoryImpl.getDefaultServerHeader();
        if (serverHeader != null) {
            sipResponse.setHeader((Header) serverHeader);
        }
        try {
            this.sipStack.addTransactionPendingAck(transaction);
            transaction.sendResponse((Response) sipResponse);
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
        SIPResponse sipResponse = sipRequest.createResponse(500);
        sipResponse.setReasonPhrase("Request out of order");
        if (MessageFactoryImpl.getDefaultServerHeader() != null) {
            sipResponse.setHeader((Header) MessageFactoryImpl.getDefaultServerHeader());
        }
        try {
            RetryAfter retryAfter = new RetryAfter();
            retryAfter.setRetryAfter(10);
            sipResponse.setHeader((Header) retryAfter);
            this.sipStack.addTransactionPendingAck(transaction);
            transaction.sendResponse((Response) sipResponse);
            transaction.releaseSem();
        } catch (Exception ex) {
            this.sipStack.getStackLogger().logError("Problem sending response", ex);
            transaction.releaseSem();
            this.sipStack.removeTransaction(transaction);
        }
    }

    /* JADX WARNING: type inference failed for: r0v91, types: [javax.sip.Dialog] */
    /* JADX WARNING: Multi-variable type inference failed */
    public void processRequest(SIPRequest sipRequest, MessageChannel incomingMessageChannel) {
        RequestEvent sipEvent;
        int port;
        SIPRequest sIPRequest = sipRequest;
        if (this.sipStack.isLoggingEnabled()) {
            StackLogger stackLogger = this.sipStack.getStackLogger();
            stackLogger.logDebug("PROCESSING INCOMING REQUEST " + sIPRequest + " transactionChannel = " + this.transactionChannel + " listening point = " + this.listeningPoint.getIPAddress() + Separators.COLON + this.listeningPoint.getPort());
        }
        if (this.listeningPoint == null) {
            if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logDebug("Dropping message: No listening point registered!");
            }
            return;
        }
        SipStackImpl sipStack2 = (SipStackImpl) this.transactionChannel.getSIPStack();
        SipProviderImpl sipProvider = this.listeningPoint.getProvider();
        if (sipProvider == null) {
            if (sipStack2.isLoggingEnabled()) {
                sipStack2.getStackLogger().logDebug("No provider - dropping !!");
            }
            return;
        }
        if (sipStack2 == null) {
            InternalErrorHandler.handleException("Egads! no sip stack!");
        }
        SIPServerTransaction transaction = (SIPServerTransaction) this.transactionChannel;
        if (transaction != null && sipStack2.isLoggingEnabled()) {
            StackLogger stackLogger2 = sipStack2.getStackLogger();
            stackLogger2.logDebug("transaction state = " + transaction.getState());
        }
        String dialogId = sIPRequest.getDialogId(true);
        SIPDialog dialog = sipStack2.getDialog(dialogId);
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
                if (ipAddress != null && (!ipAddress.equals(this.listeningPoint.getIPAddress()) || contactPort != this.listeningPoint.getPort())) {
                    if (sipStack2.isLoggingEnabled()) {
                        StackLogger stackLogger3 = sipStack2.getStackLogger();
                        stackLogger3.logDebug("nulling dialog -- listening point mismatch!  " + contactPort + "  lp port = " + this.listeningPoint.getPort());
                    }
                    dialog = null;
                }
            }
        }
        if (!sipProvider.isAutomaticDialogSupportEnabled() || !sipProvider.isDialogErrorsAutomaticallyHandled() || sipRequest.getToTag() != null || sipStack2.findMergedTransaction(sIPRequest) == null) {
            if (sipStack2.isLoggingEnabled()) {
                StackLogger stackLogger4 = sipStack2.getStackLogger();
                stackLogger4.logDebug("dialogId = " + dialogId);
                StackLogger stackLogger5 = sipStack2.getStackLogger();
                stackLogger5.logDebug("dialog = " + dialog);
            }
            if (!(sIPRequest.getHeader("Route") == null || transaction.getDialog() == null)) {
                RouteList routes = sipRequest.getRouteHeaders();
                SipUri uri = (SipUri) ((Route) routes.getFirst()).getAddress().getURI();
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
                        sIPRequest.removeHeader("Route");
                    } else {
                        routes.removeFirst();
                    }
                }
            }
            if (!sipRequest.getMethod().equals(Request.REFER) || dialog == null || !sipProvider.isDialogErrorsAutomaticallyHandled()) {
                if (sipRequest.getMethod().equals(Request.UPDATE)) {
                    if (sipProvider.isAutomaticDialogSupportEnabled() && dialog == null) {
                        sendCallOrTransactionDoesNotExistResponse(sIPRequest, transaction);
                        return;
                    }
                } else if (sipRequest.getMethod().equals("ACK")) {
                    if (transaction == null || !transaction.isInviteTransaction()) {
                        if (sipStack2.isLoggingEnabled()) {
                            StackLogger stackLogger6 = sipStack2.getStackLogger();
                            stackLogger6.logDebug("Processing ACK for dialog " + dialog);
                        }
                        if (dialog == null) {
                            if (sipStack2.isLoggingEnabled()) {
                                StackLogger stackLogger7 = sipStack2.getStackLogger();
                                stackLogger7.logDebug("Dialog does not exist " + sipRequest.getFirstLine() + " isServerTransaction = " + true);
                            }
                            SIPServerTransaction st = sipStack2.getRetransmissionAlertTransaction(dialogId);
                            if (st != null && st.isRetransmissionAlertEnabled()) {
                                st.disableRetransmissionAlerts();
                            }
                            SIPServerTransaction ackTransaction = sipStack2.findTransactionPendingAck(sIPRequest);
                            if (ackTransaction != null) {
                                if (sipStack2.isLoggingEnabled()) {
                                    sipStack2.getStackLogger().logDebug("Found Tx pending ACK");
                                }
                                try {
                                    ackTransaction.setAckSeen();
                                    sipStack2.removeTransaction(ackTransaction);
                                    sipStack2.removeTransactionPendingAck(ackTransaction);
                                } catch (Exception ex) {
                                    if (sipStack2.isLoggingEnabled()) {
                                        sipStack2.getStackLogger().logError("Problem terminating transaction", ex);
                                    }
                                }
                                return;
                            }
                        } else if (dialog.handleAck(transaction)) {
                            transaction.passToListener();
                            dialog.addTransaction(transaction);
                            dialog.addRoute(sIPRequest);
                            transaction.setDialog(dialog, dialogId);
                            if (sipRequest.getMethod().equals("INVITE") && sipProvider.isDialogErrorsAutomaticallyHandled()) {
                                sipStack2.putInMergeTable(transaction, sIPRequest);
                            }
                            if (sipStack2.deliverTerminatedEventForAck) {
                                try {
                                    sipStack2.addTransaction(transaction);
                                    transaction.scheduleAckRemoval();
                                } catch (IOException e) {
                                }
                            } else {
                                transaction.setMapped(true);
                            }
                        } else if (!dialog.isSequnceNumberValidation()) {
                            if (sipStack2.isLoggingEnabled()) {
                                StackLogger stackLogger8 = sipStack2.getStackLogger();
                                stackLogger8.logDebug("Dialog exists with loose dialog validation " + sipRequest.getFirstLine() + " isServerTransaction = " + true + " dialog = " + dialog.getDialogId());
                            }
                            SIPServerTransaction st2 = sipStack2.getRetransmissionAlertTransaction(dialogId);
                            if (st2 != null && st2.isRetransmissionAlertEnabled()) {
                                st2.disableRetransmissionAlerts();
                            }
                        } else {
                            if (sipStack2.isLoggingEnabled()) {
                                sipStack2.getStackLogger().logDebug("Dropping ACK - cannot find a transaction or dialog");
                            }
                            SIPServerTransaction ackTransaction2 = sipStack2.findTransactionPendingAck(sIPRequest);
                            if (ackTransaction2 != null) {
                                if (sipStack2.isLoggingEnabled()) {
                                    sipStack2.getStackLogger().logDebug("Found Tx pending ACK");
                                }
                                try {
                                    ackTransaction2.setAckSeen();
                                    sipStack2.removeTransaction(ackTransaction2);
                                    sipStack2.removeTransactionPendingAck(ackTransaction2);
                                } catch (Exception ex2) {
                                    if (sipStack2.isLoggingEnabled()) {
                                        sipStack2.getStackLogger().logError("Problem terminating transaction", ex2);
                                    }
                                }
                            }
                            return;
                        }
                    } else if (sipStack2.isLoggingEnabled()) {
                        sipStack2.getStackLogger().logDebug("Processing ACK for INVITE Tx ");
                    }
                } else if (sipRequest.getMethod().equals(Request.PRACK)) {
                    if (sipStack2.isLoggingEnabled()) {
                        StackLogger stackLogger9 = sipStack2.getStackLogger();
                        stackLogger9.logDebug("Processing PRACK for dialog " + dialog);
                    }
                    if (dialog == null && sipProvider.isAutomaticDialogSupportEnabled()) {
                        if (sipStack2.isLoggingEnabled()) {
                            StackLogger stackLogger10 = sipStack2.getStackLogger();
                            stackLogger10.logDebug("Dialog does not exist " + sipRequest.getFirstLine() + " isServerTransaction = " + true);
                        }
                        if (sipStack2.isLoggingEnabled()) {
                            sipStack2.getStackLogger().logDebug("Sending 481 for PRACK - automatic dialog support is enabled -- cant find dialog!");
                        }
                        try {
                            sipProvider.sendResponse(sIPRequest.createResponse(Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST));
                        } catch (SipException e2) {
                            SipException sipException = e2;
                            sipStack2.getStackLogger().logError("error sending response", e2);
                        }
                        if (transaction != null) {
                            sipStack2.removeTransaction(transaction);
                            transaction.releaseSem();
                        }
                        return;
                    } else if (dialog != null) {
                        if (!dialog.handlePrack(sIPRequest)) {
                            if (sipStack2.isLoggingEnabled()) {
                                sipStack2.getStackLogger().logDebug("Dropping out of sequence PRACK ");
                            }
                            if (transaction != null) {
                                sipStack2.removeTransaction(transaction);
                                transaction.releaseSem();
                            }
                            return;
                        }
                        try {
                            sipStack2.addTransaction(transaction);
                            dialog.addTransaction(transaction);
                            dialog.addRoute(sIPRequest);
                            transaction.setDialog(dialog, dialogId);
                        } catch (Exception ex3) {
                            InternalErrorHandler.handleException(ex3);
                        }
                    } else if (sipStack2.isLoggingEnabled()) {
                        sipStack2.getStackLogger().logDebug("Processing PRACK without a DIALOG -- this must be a proxy element");
                    }
                } else if (sipRequest.getMethod().equals("BYE")) {
                    if (dialog != null && !dialog.isRequestConsumable(sIPRequest)) {
                        if (sipStack2.isLoggingEnabled()) {
                            StackLogger stackLogger11 = sipStack2.getStackLogger();
                            stackLogger11.logDebug("Dropping out of sequence BYE " + dialog.getRemoteSeqNumber() + Separators.SP + sipRequest.getCSeq().getSeqNumber());
                        }
                        if (dialog.getRemoteSeqNumber() >= sipRequest.getCSeq().getSeqNumber() && transaction.getState() == TransactionState.TRYING) {
                            sendServerInternalErrorResponse(sIPRequest, transaction);
                        }
                        if (transaction != null) {
                            sipStack2.removeTransaction(transaction);
                        }
                        return;
                    } else if (dialog != null || !sipProvider.isAutomaticDialogSupportEnabled()) {
                        if (!(transaction == null || dialog == null)) {
                            try {
                                if (sipProvider == dialog.getSipProvider()) {
                                    sipStack2.addTransaction(transaction);
                                    dialog.addTransaction(transaction);
                                    transaction.setDialog(dialog, dialogId);
                                }
                            } catch (IOException ex4) {
                                InternalErrorHandler.handleException((Exception) ex4);
                            }
                        }
                        if (sipStack2.isLoggingEnabled()) {
                            StackLogger stackLogger12 = sipStack2.getStackLogger();
                            stackLogger12.logDebug("BYE Tx = " + transaction + " isMapped =" + transaction.isTransactionMapped());
                        }
                    } else {
                        SIPResponse response = sIPRequest.createResponse(Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST);
                        response.setReasonPhrase("Dialog Not Found");
                        if (sipStack2.isLoggingEnabled()) {
                            sipStack2.getStackLogger().logDebug("dropping request -- automatic dialog support enabled and dialog does not exist!");
                        }
                        try {
                            transaction.sendResponse((Response) response);
                        } catch (SipException ex5) {
                            SipException sipException2 = ex5;
                            sipStack2.getStackLogger().logError("Error in sending response", ex5);
                        }
                        if (transaction != null) {
                            sipStack2.removeTransaction(transaction);
                            transaction.releaseSem();
                        }
                        return;
                    }
                } else if (sipRequest.getMethod().equals(Request.CANCEL)) {
                    SIPServerTransaction st3 = (SIPServerTransaction) sipStack2.findCancelTransaction(sIPRequest, true);
                    if (sipStack2.isLoggingEnabled()) {
                        StackLogger stackLogger13 = sipStack2.getStackLogger();
                        stackLogger13.logDebug("Got a CANCEL, InviteServerTx = " + st3 + " cancel Server Tx ID = " + transaction + " isMapped = " + transaction.isTransactionMapped());
                    }
                    if (sipRequest.getMethod().equals(Request.CANCEL)) {
                        if (st3 != null && st3.getState() == SIPTransaction.TERMINATED_STATE) {
                            if (sipStack2.isLoggingEnabled()) {
                                sipStack2.getStackLogger().logDebug("Too late to cancel Transaction");
                            }
                            try {
                                transaction.sendResponse((Response) sIPRequest.createResponse(Response.OK));
                            } catch (Exception ex6) {
                                if (ex6.getCause() != null && (ex6.getCause() instanceof IOException)) {
                                    st3.raiseIOExceptionEvent();
                                }
                            }
                            return;
                        } else if (sipStack2.isLoggingEnabled()) {
                            StackLogger stackLogger14 = sipStack2.getStackLogger();
                            stackLogger14.logDebug("Cancel transaction = " + st3);
                        }
                    }
                    if (transaction != null && st3 != null && st3.getDialog() != null) {
                        transaction.setDialog((SIPDialog) st3.getDialog(), dialogId);
                        dialog = st3.getDialog();
                    } else if (st3 == null && sipProvider.isAutomaticDialogSupportEnabled() && transaction != null) {
                        SIPResponse response2 = sIPRequest.createResponse(Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST);
                        if (sipStack2.isLoggingEnabled()) {
                            sipStack2.getStackLogger().logDebug("dropping request -- automatic dialog support enabled and INVITE ST does not exist!");
                        }
                        try {
                            sipProvider.sendResponse(response2);
                        } catch (SipException ex7) {
                            SipException sipException3 = ex7;
                            InternalErrorHandler.handleException((Exception) ex7);
                        }
                        if (transaction != null) {
                            sipStack2.removeTransaction(transaction);
                            transaction.releaseSem();
                        }
                        return;
                    }
                    if (!(st3 == null || transaction == null)) {
                        try {
                            sipStack2.addTransaction(transaction);
                            transaction.setPassToListener();
                            transaction.setInviteTransaction(st3);
                            st3.acquireSem();
                        } catch (Exception ex8) {
                            InternalErrorHandler.handleException(ex8);
                        }
                    }
                } else if (sipRequest.getMethod().equals("INVITE")) {
                    SIPTransaction lastTransaction = dialog == null ? null : dialog.getInviteTransaction();
                    if (dialog == null || transaction == null || lastTransaction == null || sipRequest.getCSeq().getSeqNumber() <= dialog.getRemoteSeqNumber() || !(lastTransaction instanceof SIPServerTransaction) || !sipProvider.isDialogErrorsAutomaticallyHandled() || !dialog.isSequnceNumberValidation() || !lastTransaction.isInviteTransaction() || lastTransaction.getState() == TransactionState.COMPLETED || lastTransaction.getState() == TransactionState.TERMINATED || lastTransaction.getState() == TransactionState.CONFIRMED) {
                        SIPTransaction lastTransaction2 = dialog == null ? null : dialog.getLastTransaction();
                        if (dialog != null && sipProvider.isDialogErrorsAutomaticallyHandled() && lastTransaction2 != null && lastTransaction2.isInviteTransaction() && (lastTransaction2 instanceof ClientTransaction) && lastTransaction2.getLastResponse() != null && lastTransaction2.getLastResponse().getStatusCode() == 200 && !dialog.isAckSent(lastTransaction2.getLastResponse().getCSeq().getSeqNumber())) {
                            if (sipStack2.isLoggingEnabled()) {
                                sipStack2.getStackLogger().logDebug("Sending 491 response for client Dialog ACK not sent.");
                            }
                            sendRequestPendingResponse(sIPRequest, transaction);
                            return;
                        } else if (dialog != null && lastTransaction2 != null && sipProvider.isDialogErrorsAutomaticallyHandled() && lastTransaction2.isInviteTransaction() && (lastTransaction2 instanceof ServerTransaction) && !dialog.isAckSeen()) {
                            if (sipStack2.isLoggingEnabled()) {
                                sipStack2.getStackLogger().logDebug("Sending 491 response for server Dialog ACK not seen.");
                            }
                            sendRequestPendingResponse(sIPRequest, transaction);
                            return;
                        }
                    } else {
                        if (sipStack2.isLoggingEnabled()) {
                            sipStack2.getStackLogger().logDebug("Sending 500 response for out of sequence message");
                        }
                        sendServerInternalErrorResponse(sIPRequest, transaction);
                        return;
                    }
                }
            } else if (((ReferToHeader) sIPRequest.getHeader(ReferToHeader.NAME)) == null) {
                sendBadRequestResponse(sIPRequest, transaction, "Refer-To header is missing");
                return;
            } else {
                SIPTransaction lastTransaction3 = dialog.getLastTransaction();
                if (lastTransaction3 != null && sipProvider.isDialogErrorsAutomaticallyHandled()) {
                    SIPRequest lastRequest = (SIPRequest) lastTransaction3.getRequest();
                    if (lastTransaction3 instanceof SIPServerTransaction) {
                        if (!dialog.isAckSeen() && lastRequest.getMethod().equals("INVITE")) {
                            sendRequestPendingResponse(sIPRequest, transaction);
                            return;
                        }
                    } else if (lastTransaction3 instanceof SIPClientTransaction) {
                        long cseqno = lastRequest.getCSeqHeader().getSeqNumber();
                        if (lastRequest.getMethod().equals("INVITE") && !dialog.isAckSent(cseqno)) {
                            sendRequestPendingResponse(sIPRequest, transaction);
                            return;
                        }
                    }
                }
            }
            if (sipStack2.isLoggingEnabled()) {
                StackLogger stackLogger15 = sipStack2.getStackLogger();
                stackLogger15.logDebug("CHECK FOR OUT OF SEQ MESSAGE " + dialog + " transaction " + transaction);
            }
            if (dialog != null && transaction != null && !sipRequest.getMethod().equals("BYE") && !sipRequest.getMethod().equals(Request.CANCEL) && !sipRequest.getMethod().equals("ACK") && !sipRequest.getMethod().equals(Request.PRACK)) {
                if (!dialog.isRequestConsumable(sIPRequest)) {
                    if (sipStack2.isLoggingEnabled()) {
                        StackLogger stackLogger16 = sipStack2.getStackLogger();
                        stackLogger16.logDebug("Dropping out of sequence message " + dialog.getRemoteSeqNumber() + Separators.SP + sipRequest.getCSeq());
                    }
                    if (dialog.getRemoteSeqNumber() >= sipRequest.getCSeq().getSeqNumber() && sipProvider.isDialogErrorsAutomaticallyHandled() && (transaction.getState() == TransactionState.TRYING || transaction.getState() == TransactionState.PROCEEDING)) {
                        sendServerInternalErrorResponse(sIPRequest, transaction);
                    }
                    return;
                }
                try {
                    if (sipProvider == dialog.getSipProvider()) {
                        sipStack2.addTransaction(transaction);
                        dialog.addTransaction(transaction);
                        dialog.addRoute(sIPRequest);
                        transaction.setDialog(dialog, dialogId);
                    }
                } catch (IOException e3) {
                    transaction.raiseIOExceptionEvent();
                    sipStack2.removeTransaction(transaction);
                    return;
                }
            }
            if (sipStack2.isLoggingEnabled()) {
                StackLogger stackLogger17 = sipStack2.getStackLogger();
                stackLogger17.logDebug(sipRequest.getMethod() + " transaction.isMapped = " + transaction.isTransactionMapped());
            }
            if (dialog == null && sipRequest.getMethod().equals("NOTIFY")) {
                SIPClientTransaction pendingSubscribeClientTx = sipStack2.findSubscribeTransaction(sIPRequest, this.listeningPoint);
                if (sipStack2.isLoggingEnabled()) {
                    StackLogger stackLogger18 = sipStack2.getStackLogger();
                    stackLogger18.logDebug("PROCESSING NOTIFY  DIALOG == null " + pendingSubscribeClientTx);
                }
                if (sipProvider.isAutomaticDialogSupportEnabled() && pendingSubscribeClientTx == null && !sipStack2.deliverUnsolicitedNotify) {
                    try {
                        if (sipStack2.isLoggingEnabled()) {
                            sipStack2.getStackLogger().logDebug("Could not find Subscription for Notify Tx.");
                        }
                        Response errorResponse = sIPRequest.createResponse(Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST);
                        errorResponse.setReasonPhrase("Subscription does not exist");
                        sipProvider.sendResponse(errorResponse);
                        return;
                    } catch (Exception ex9) {
                        sipStack2.getStackLogger().logError("Exception while sending error response statelessly", ex9);
                        return;
                    }
                } else if (pendingSubscribeClientTx != null) {
                    transaction.setPendingSubscribe(pendingSubscribeClientTx);
                    SIPDialog subscriptionDialog = pendingSubscribeClientTx.getDefaultDialog();
                    if (subscriptionDialog == null || subscriptionDialog.getDialogId() == null || !subscriptionDialog.getDialogId().equals(dialogId)) {
                        if (subscriptionDialog == null || subscriptionDialog.getDialogId() != null) {
                            subscriptionDialog = pendingSubscribeClientTx.getDialog(dialogId);
                        } else {
                            subscriptionDialog.setDialogId(dialogId);
                        }
                        if (sipStack2.isLoggingEnabled()) {
                            StackLogger stackLogger19 = sipStack2.getStackLogger();
                            stackLogger19.logDebug("PROCESSING NOTIFY Subscribe DIALOG " + subscriptionDialog);
                        }
                        if (subscriptionDialog == null && ((sipProvider.isAutomaticDialogSupportEnabled() || pendingSubscribeClientTx.getDefaultDialog() != null) && sipStack2.isEventForked(((Event) sIPRequest.getHeader("Event")).getEventType()))) {
                            subscriptionDialog = SIPDialog.createFromNOTIFY(pendingSubscribeClientTx, transaction);
                        }
                        if (subscriptionDialog != null) {
                            transaction.setDialog(subscriptionDialog, dialogId);
                            subscriptionDialog.setState(DialogState.CONFIRMED.getValue());
                            sipStack2.putDialog(subscriptionDialog);
                            pendingSubscribeClientTx.setDialog(subscriptionDialog, dialogId);
                            if (!transaction.isTransactionMapped()) {
                                this.sipStack.mapTransaction(transaction);
                                transaction.setPassToListener();
                                try {
                                    this.sipStack.addTransaction(transaction);
                                } catch (Exception e4) {
                                }
                            }
                        }
                    } else {
                        transaction.setDialog(subscriptionDialog, dialogId);
                        SIPDialog dialog2 = subscriptionDialog;
                        if (!transaction.isTransactionMapped()) {
                            this.sipStack.mapTransaction(transaction);
                            transaction.setPassToListener();
                            try {
                                this.sipStack.addTransaction(transaction);
                            } catch (Exception e5) {
                            }
                        }
                        sipStack2.putDialog(subscriptionDialog);
                        if (pendingSubscribeClientTx != null) {
                            subscriptionDialog.addTransaction(pendingSubscribeClientTx);
                            pendingSubscribeClientTx.setDialog(subscriptionDialog, dialogId);
                        }
                    }
                    if (transaction == null || !transaction.isTransactionMapped()) {
                        sipEvent = new RequestEvent(sipProvider, null, subscriptionDialog, sIPRequest);
                    } else {
                        sipEvent = new RequestEvent(sipProvider, transaction, subscriptionDialog, sIPRequest);
                    }
                } else {
                    if (sipStack2.isLoggingEnabled()) {
                        sipStack2.getStackLogger().logDebug("could not find subscribe tx");
                    }
                    sipEvent = new RequestEvent(sipProvider, null, null, sIPRequest);
                }
            } else if (transaction == null || !transaction.isTransactionMapped()) {
                sipEvent = new RequestEvent(sipProvider, null, dialog, sIPRequest);
            } else {
                sipEvent = new RequestEvent(sipProvider, transaction, dialog, sIPRequest);
            }
            sipProvider.handleEvent(sipEvent, transaction);
            return;
        }
        sendLoopDetectedResponse(sIPRequest, transaction);
    }

    public void processResponse(SIPResponse response, MessageChannel incomingMessageChannel, SIPDialog dialog) {
        if (this.sipStack.isLoggingEnabled()) {
            StackLogger stackLogger = this.sipStack.getStackLogger();
            stackLogger.logDebug("PROCESSING INCOMING RESPONSE" + response.encodeMessage());
        }
        if (this.listeningPoint == null) {
            if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logError("Dropping message: No listening point registered!");
            }
        } else if (!this.sipStack.checkBranchId() || Utils.getInstance().responseBelongsToUs(response)) {
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
                SIPClientTransaction transaction = (SIPClientTransaction) this.transactionChannel;
                SipStackImpl sipStackImpl = sipProvider.sipStack;
                if (this.sipStack.isLoggingEnabled()) {
                    StackLogger stackLogger2 = sipStackImpl.getStackLogger();
                    stackLogger2.logDebug("Transaction = " + transaction);
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
                        StackLogger stackLogger3 = this.sipStack.getStackLogger();
                        stackLogger3.logDebug("could not find tx, handling statelessly Dialog =  " + dialog);
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
        SIPDialog sipDialog = this.sipStack.getDialog(dialogID);
        String method = sipResponse.getCSeq().getMethod();
        if (this.sipStack.isLoggingEnabled()) {
            this.sipStack.getStackLogger().logDebug("PROCESSING INCOMING RESPONSE: " + sipResponse.encodeMessage());
        }
        if (this.sipStack.checkBranchId() && !Utils.getInstance().responseBelongsToUs(sipResponse)) {
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
                SIPClientTransaction transaction = (SIPClientTransaction) this.transactionChannel;
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
                    boolean z = true;
                    boolean z2 = originalFrom == null;
                    if (sipResponse.getFrom().getTag() != null) {
                        z = false;
                    }
                    if (z ^ z2) {
                        if (this.sipStack.isLoggingEnabled()) {
                            this.sipStack.getStackLogger().logDebug("From tag mismatch -- dropping response");
                        }
                        return;
                    } else if (originalFrom != null && !originalFrom.equalsIgnoreCase(sipResponse.getFrom().getTag())) {
                        if (this.sipStack.isLoggingEnabled()) {
                            this.sipStack.getStackLogger().logDebug("From tag mismatch -- dropping response");
                        }
                        return;
                    }
                }
                SipStackImpl sipStackImpl = this.sipStack;
                if (!SipStackImpl.isDialogCreated(method) || sipResponse.getStatusCode() == 100 || sipResponse.getFrom().getTag() == null || sipResponse.getTo().getTag() == null || sipDialog != null) {
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
