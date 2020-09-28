package gov.nist.javax.sip;

import gov.nist.core.StackLogger;
import gov.nist.core.ThreadAuditor;
import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.message.SIPResponse;
import gov.nist.javax.sip.stack.SIPClientTransaction;
import gov.nist.javax.sip.stack.SIPDialog;
import gov.nist.javax.sip.stack.SIPServerTransaction;
import gov.nist.javax.sip.stack.SIPTransaction;
import java.util.EventObject;
import java.util.LinkedList;
import javax.sip.DialogState;
import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.SipListener;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionState;
import javax.sip.TransactionTerminatedEvent;

/* access modifiers changed from: package-private */
public class EventScanner implements Runnable {
    private int[] eventMutex;
    private boolean isStopped;
    private LinkedList pendingEvents;
    private int refCount;
    private SipStackImpl sipStack;

    public void incrementRefcount() {
        synchronized (this.eventMutex) {
            this.refCount++;
        }
    }

    public EventScanner(SipStackImpl sipStackImpl) {
        this.pendingEvents = new LinkedList();
        this.eventMutex = new int[]{0};
        this.pendingEvents = new LinkedList();
        Thread myThread = new Thread(this);
        myThread.setDaemon(false);
        this.sipStack = sipStackImpl;
        myThread.setName("EventScannerThread");
        myThread.start();
    }

    public void addEvent(EventWrapper eventWrapper) {
        if (this.sipStack.isLoggingEnabled()) {
            StackLogger stackLogger = this.sipStack.getStackLogger();
            stackLogger.logDebug("addEvent " + eventWrapper);
        }
        synchronized (this.eventMutex) {
            this.pendingEvents.add(eventWrapper);
            this.eventMutex.notify();
        }
    }

    public void stop() {
        synchronized (this.eventMutex) {
            if (this.refCount > 0) {
                this.refCount--;
            }
            if (this.refCount == 0) {
                this.isStopped = true;
                this.eventMutex.notify();
            }
        }
    }

    public void forceStop() {
        synchronized (this.eventMutex) {
            this.isStopped = true;
            this.refCount = 0;
            this.eventMutex.notify();
        }
    }

    /* JADX INFO: finally extract failed */
    public void deliverEvent(EventWrapper eventWrapper) {
        SipListener sipListener;
        SIPDialog dialog;
        EventObject sipEvent = eventWrapper.sipEvent;
        if (this.sipStack.isLoggingEnabled()) {
            StackLogger stackLogger = this.sipStack.getStackLogger();
            stackLogger.logDebug("sipEvent = " + sipEvent + "source = " + sipEvent.getSource());
        }
        if (!(sipEvent instanceof IOExceptionEvent)) {
            sipListener = ((SipProviderImpl) sipEvent.getSource()).getSipListener();
        } else {
            sipListener = this.sipStack.getSipListener();
        }
        if (sipEvent instanceof RequestEvent) {
            try {
                SIPRequest sipRequest = (SIPRequest) ((RequestEvent) sipEvent).getRequest();
                if (this.sipStack.isLoggingEnabled()) {
                    StackLogger stackLogger2 = this.sipStack.getStackLogger();
                    stackLogger2.logDebug("deliverEvent : " + sipRequest.getFirstLine() + " transaction " + eventWrapper.transaction + " sipEvent.serverTx = " + ((RequestEvent) sipEvent).getServerTransaction());
                }
                SIPServerTransaction tx = (SIPServerTransaction) this.sipStack.findTransaction(sipRequest, true);
                if (tx == null || tx.passToListener()) {
                    if (this.sipStack.findPendingTransaction(sipRequest) != null) {
                        if (this.sipStack.isLoggingEnabled()) {
                            this.sipStack.getStackLogger().logDebug("transaction already exists!!");
                        }
                        if (this.sipStack.isLoggingEnabled()) {
                            StackLogger stackLogger3 = this.sipStack.getStackLogger();
                            stackLogger3.logDebug("Done processing Message " + ((SIPRequest) ((RequestEvent) sipEvent).getRequest()).getFirstLine());
                        }
                        if (eventWrapper.transaction != null && ((SIPServerTransaction) eventWrapper.transaction).passToListener()) {
                            ((SIPServerTransaction) eventWrapper.transaction).releaseSem();
                        }
                        if (eventWrapper.transaction != null) {
                            this.sipStack.removePendingTransaction((SIPServerTransaction) eventWrapper.transaction);
                        }
                        if (eventWrapper.transaction.getOriginalRequest().getMethod().equals("ACK")) {
                            eventWrapper.transaction.setState(TransactionState.TERMINATED);
                            return;
                        }
                        return;
                    }
                    this.sipStack.putPendingTransaction((SIPServerTransaction) eventWrapper.transaction);
                } else if (!sipRequest.getMethod().equals("ACK") || !tx.isInviteTransaction() || (tx.getLastResponse().getStatusCode() / 100 != 2 && !this.sipStack.isNon2XXAckPassedToListener())) {
                    if (this.sipStack.isLoggingEnabled()) {
                        StackLogger stackLogger4 = this.sipStack.getStackLogger();
                        stackLogger4.logDebug("transaction already exists! " + tx);
                    }
                    if (this.sipStack.isLoggingEnabled()) {
                        StackLogger stackLogger5 = this.sipStack.getStackLogger();
                        stackLogger5.logDebug("Done processing Message " + ((SIPRequest) ((RequestEvent) sipEvent).getRequest()).getFirstLine());
                    }
                    if (eventWrapper.transaction != null && ((SIPServerTransaction) eventWrapper.transaction).passToListener()) {
                        ((SIPServerTransaction) eventWrapper.transaction).releaseSem();
                    }
                    if (eventWrapper.transaction != null) {
                        this.sipStack.removePendingTransaction((SIPServerTransaction) eventWrapper.transaction);
                    }
                    if (eventWrapper.transaction.getOriginalRequest().getMethod().equals("ACK")) {
                        eventWrapper.transaction.setState(TransactionState.TERMINATED);
                        return;
                    }
                    return;
                } else if (this.sipStack.isLoggingEnabled()) {
                    this.sipStack.getStackLogger().logDebug("Detected broken client sending ACK with same branch! Passing...");
                }
                sipRequest.setTransaction(eventWrapper.transaction);
                try {
                    if (this.sipStack.isLoggingEnabled()) {
                        StackLogger stackLogger6 = this.sipStack.getStackLogger();
                        stackLogger6.logDebug("Calling listener " + sipRequest.getFirstLine());
                        StackLogger stackLogger7 = this.sipStack.getStackLogger();
                        stackLogger7.logDebug("Calling listener " + eventWrapper.transaction);
                    }
                    if (sipListener != null) {
                        sipListener.processRequest((RequestEvent) sipEvent);
                    }
                    if (this.sipStack.isLoggingEnabled()) {
                        StackLogger stackLogger8 = this.sipStack.getStackLogger();
                        stackLogger8.logDebug("Done processing Message " + sipRequest.getFirstLine());
                    }
                    if (!(eventWrapper.transaction == null || (dialog = (SIPDialog) eventWrapper.transaction.getDialog()) == null)) {
                        dialog.requestConsumed();
                    }
                } catch (Exception ex) {
                    this.sipStack.getStackLogger().logException(ex);
                }
            } finally {
                if (this.sipStack.isLoggingEnabled()) {
                    StackLogger stackLogger9 = this.sipStack.getStackLogger();
                    stackLogger9.logDebug("Done processing Message " + ((SIPRequest) ((RequestEvent) sipEvent).getRequest()).getFirstLine());
                }
                if (eventWrapper.transaction != null && ((SIPServerTransaction) eventWrapper.transaction).passToListener()) {
                    ((SIPServerTransaction) eventWrapper.transaction).releaseSem();
                }
                if (eventWrapper.transaction != null) {
                    this.sipStack.removePendingTransaction((SIPServerTransaction) eventWrapper.transaction);
                }
                if (eventWrapper.transaction.getOriginalRequest().getMethod().equals("ACK")) {
                    eventWrapper.transaction.setState(TransactionState.TERMINATED);
                }
            }
        } else if (sipEvent instanceof ResponseEvent) {
            try {
                ResponseEvent responseEvent = (ResponseEvent) sipEvent;
                SIPResponse sipResponse = (SIPResponse) responseEvent.getResponse();
                SIPDialog sipDialog = (SIPDialog) responseEvent.getDialog();
                try {
                    if (this.sipStack.isLoggingEnabled()) {
                        StackLogger stackLogger10 = this.sipStack.getStackLogger();
                        stackLogger10.logDebug("Calling listener for " + sipResponse.getFirstLine());
                    }
                    if (sipListener != null) {
                        SIPTransaction tx2 = eventWrapper.transaction;
                        if (tx2 != null) {
                            tx2.setPassToListener();
                        }
                        sipListener.processResponse((ResponseEvent) sipEvent);
                    }
                    if (sipDialog != null && ((sipDialog.getState() == null || !sipDialog.getState().equals(DialogState.TERMINATED)) && (sipResponse.getStatusCode() == 481 || sipResponse.getStatusCode() == 408))) {
                        if (this.sipStack.isLoggingEnabled()) {
                            this.sipStack.getStackLogger().logDebug("Removing dialog on 408 or 481 response");
                        }
                        sipDialog.doDeferredDelete();
                    }
                    if (sipResponse.getCSeq().getMethod().equals("INVITE") && sipDialog != null && sipResponse.getStatusCode() == 200) {
                        if (this.sipStack.isLoggingEnabled()) {
                            StackLogger stackLogger11 = this.sipStack.getStackLogger();
                            stackLogger11.logDebug("Warning! unacknowledged dialog. " + sipDialog.getState());
                        }
                        sipDialog.doDeferredDeleteIfNoAckSent(sipResponse.getCSeq().getSeqNumber());
                    }
                } catch (Exception ex2) {
                    this.sipStack.getStackLogger().logException(ex2);
                }
                SIPClientTransaction ct = (SIPClientTransaction) eventWrapper.transaction;
                if (ct != null && TransactionState.COMPLETED == ct.getState() && ct.getOriginalRequest() != null && !ct.getOriginalRequest().getMethod().equals("INVITE")) {
                    ct.clearState();
                }
                if (eventWrapper.transaction != null && eventWrapper.transaction.passToListener()) {
                    eventWrapper.transaction.releaseSem();
                }
            } catch (Throwable th) {
                if (eventWrapper.transaction != null && eventWrapper.transaction.passToListener()) {
                    eventWrapper.transaction.releaseSem();
                }
                throw th;
            }
        } else if (sipEvent instanceof TimeoutEvent) {
            if (sipListener != null) {
                try {
                    sipListener.processTimeout((TimeoutEvent) sipEvent);
                } catch (Exception ex3) {
                    this.sipStack.getStackLogger().logException(ex3);
                }
            }
        } else if (sipEvent instanceof DialogTimeoutEvent) {
            if (sipListener != null) {
                try {
                    if (sipListener instanceof SipListenerExt) {
                        ((SipListenerExt) sipListener).processDialogTimeout((DialogTimeoutEvent) sipEvent);
                    }
                } catch (Exception ex4) {
                    this.sipStack.getStackLogger().logException(ex4);
                }
            }
        } else if (sipEvent instanceof IOExceptionEvent) {
            if (sipListener != null) {
                try {
                    sipListener.processIOException((IOExceptionEvent) sipEvent);
                } catch (Exception ex5) {
                    this.sipStack.getStackLogger().logException(ex5);
                }
            }
        } else if (sipEvent instanceof TransactionTerminatedEvent) {
            try {
                if (this.sipStack.isLoggingEnabled()) {
                    this.sipStack.getStackLogger().logDebug("About to deliver transactionTerminatedEvent");
                    StackLogger stackLogger12 = this.sipStack.getStackLogger();
                    stackLogger12.logDebug("tx = " + ((TransactionTerminatedEvent) sipEvent).getClientTransaction());
                    StackLogger stackLogger13 = this.sipStack.getStackLogger();
                    stackLogger13.logDebug("tx = " + ((TransactionTerminatedEvent) sipEvent).getServerTransaction());
                }
                if (sipListener != null) {
                    sipListener.processTransactionTerminated((TransactionTerminatedEvent) sipEvent);
                }
            } catch (AbstractMethodError e) {
                if (this.sipStack.isLoggingEnabled()) {
                    this.sipStack.getStackLogger().logWarning("Unable to call sipListener.processTransactionTerminated");
                }
            } catch (Exception ex6) {
                this.sipStack.getStackLogger().logException(ex6);
            }
        } else if (!(sipEvent instanceof DialogTerminatedEvent)) {
            StackLogger stackLogger14 = this.sipStack.getStackLogger();
            stackLogger14.logFatalError("bad event" + sipEvent);
        } else if (sipListener != null) {
            try {
                sipListener.processDialogTerminated((DialogTerminatedEvent) sipEvent);
            } catch (AbstractMethodError e2) {
                if (this.sipStack.isLoggingEnabled()) {
                    this.sipStack.getStackLogger().logWarning("Unable to call sipListener.processDialogTerminated");
                }
            } catch (Exception ex7) {
                this.sipStack.getStackLogger().logException(ex7);
            }
        }
    }

    /* JADX INFO: Multiple debug info for r3v3 java.util.LinkedList: [D('ex' java.lang.InterruptedException), D('eventsToDeliver' java.util.LinkedList)] */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0034, code lost:
        if (r7.sipStack.isLoggingEnabled() == false) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0038, code lost:
        if (r7.isStopped != false) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x003a, code lost:
        r7.sipStack.getStackLogger().logFatalError("Event scanner exited abnormally");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x008a, code lost:
        r2 = r3.listIterator();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0092, code lost:
        if (r2.hasNext() == false) goto L_0x000a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0094, code lost:
        r4 = (gov.nist.javax.sip.EventWrapper) r2.next();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00a1, code lost:
        if (r7.sipStack.isLoggingEnabled() == false) goto L_0x00c9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00a3, code lost:
        r4 = r7.sipStack.getStackLogger();
        r4.logDebug("Processing " + r4 + "nevents " + r3.size());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:?, code lost:
        deliverEvent(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00cd, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00d4, code lost:
        if (r7.sipStack.isLoggingEnabled() != false) goto L_0x00d6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00d6, code lost:
        r7.sipStack.getStackLogger().logError("Unexpected exception caught while delivering event -- carrying on bravely", r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:?, code lost:
        return;
     */
    public void run() {
        boolean isLoggingEnabled;
        boolean z;
        try {
            ThreadAuditor.ThreadHandle threadHandle = this.sipStack.getThreadAuditor().addCurrentThread();
            while (true) {
                synchronized (this.eventMutex) {
                    while (this.pendingEvents.isEmpty()) {
                        if (!this.isStopped) {
                            try {
                                threadHandle.ping();
                                this.eventMutex.wait(threadHandle.getPingIntervalInMillisecs());
                            } catch (InterruptedException e) {
                                if (this.sipStack.isLoggingEnabled()) {
                                    this.sipStack.getStackLogger().logDebug("Interrupted!");
                                }
                                if (isLoggingEnabled && !z) {
                                    return;
                                }
                                return;
                            }
                        } else if (this.sipStack.isLoggingEnabled()) {
                            this.sipStack.getStackLogger().logDebug("Stopped event scanner!!");
                        }
                    }
                    LinkedList eventsToDeliver = this.pendingEvents;
                    this.pendingEvents = new LinkedList();
                }
            }
        } finally {
            if (this.sipStack.isLoggingEnabled() && !this.isStopped) {
                this.sipStack.getStackLogger().logFatalError("Event scanner exited abnormally");
            }
        }
    }
}
