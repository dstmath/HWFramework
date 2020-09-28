package gov.nist.javax.sip.stack;

import gov.nist.core.InternalErrorHandler;
import gov.nist.core.NameValueList;
import gov.nist.core.Separators;
import gov.nist.core.StackLogger;
import gov.nist.javax.sip.DialogExt;
import gov.nist.javax.sip.ListeningPointImpl;
import gov.nist.javax.sip.SipListenerExt;
import gov.nist.javax.sip.SipProviderImpl;
import gov.nist.javax.sip.Utils;
import gov.nist.javax.sip.address.SipUri;
import gov.nist.javax.sip.header.Authorization;
import gov.nist.javax.sip.header.CSeq;
import gov.nist.javax.sip.header.Contact;
import gov.nist.javax.sip.header.ContactList;
import gov.nist.javax.sip.header.From;
import gov.nist.javax.sip.header.MaxForwards;
import gov.nist.javax.sip.header.RAck;
import gov.nist.javax.sip.header.RSeq;
import gov.nist.javax.sip.header.Reason;
import gov.nist.javax.sip.header.RecordRoute;
import gov.nist.javax.sip.header.RecordRouteList;
import gov.nist.javax.sip.header.Require;
import gov.nist.javax.sip.header.Route;
import gov.nist.javax.sip.header.RouteList;
import gov.nist.javax.sip.header.SIPHeader;
import gov.nist.javax.sip.header.TimeStamp;
import gov.nist.javax.sip.header.To;
import gov.nist.javax.sip.header.Via;
import gov.nist.javax.sip.message.MessageFactoryImpl;
import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.message.SIPResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.net.InetAddress;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.DialogDoesNotExistException;
import javax.sip.DialogState;
import javax.sip.IOExceptionEvent;
import javax.sip.InvalidArgumentException;
import javax.sip.ListeningPoint;
import javax.sip.ObjectInUseException;
import javax.sip.SipException;
import javax.sip.Transaction;
import javax.sip.TransactionDoesNotExistException;
import javax.sip.TransactionState;
import javax.sip.address.Address;
import javax.sip.address.Hop;
import javax.sip.address.SipURI;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.EventHeader;
import javax.sip.header.Header;
import javax.sip.header.OptionTag;
import javax.sip.header.ReasonHeader;
import javax.sip.header.RequireHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;
import org.ccil.cowan.tagsoup.HTMLModels;

public class SIPDialog implements Dialog, DialogExt {
    public static final int CONFIRMED_STATE = DialogState._CONFIRMED;
    private static final int DIALOG_LINGER_TIME = 8;
    public static final int EARLY_STATE = DialogState._EARLY;
    public static final int NULL_STATE = -1;
    public static final int TERMINATED_STATE = DialogState._TERMINATED;
    private static final long serialVersionUID = -1429794423085204069L;
    private transient int ackLine;
    protected transient boolean ackProcessed;
    protected transient boolean ackSeen;
    private transient Semaphore ackSem;
    private transient Object applicationData;
    public transient long auditTag;
    private transient boolean byeSent;
    protected CallIdHeader callIdHeader;
    protected Contact contactHeader;
    private transient DialogDeleteIfNoAckSentTask dialogDeleteIfNoAckSentTask;
    private transient DialogDeleteTask dialogDeleteTask;
    private String dialogId;
    private int dialogState;
    private transient boolean dialogTerminatedEventDelivered;
    private transient String earlyDialogId;
    private EventHeader eventHeader;
    private transient Set<SIPDialogEventListener> eventListeners;
    private transient SIPTransaction firstTransaction;
    protected String firstTransactionId;
    protected boolean firstTransactionIsServerTransaction;
    protected String firstTransactionMethod;
    protected int firstTransactionPort;
    protected boolean firstTransactionSecure;
    protected boolean firstTransactionSeen;
    private transient long highestSequenceNumberAcknowledged;
    protected String hisTag;
    private transient boolean isAcknowledged;
    private transient boolean isAssigned;
    private boolean isBackToBackUserAgent;
    private SIPRequest lastAckReceived;
    private transient SIPRequest lastAckSent;
    private transient long lastInviteOkReceived;
    private SIPResponse lastResponse;
    private transient SIPTransaction lastTransaction;
    protected Address localParty;
    private long localSequenceNumber;
    private String method;
    protected String myTag;
    protected transient Long nextSeqno;
    private long originalLocalSequenceNumber;
    private transient SIPRequest originalRequest;
    private transient int prevRetransmissionTicks;
    private boolean reInviteFlag;
    private transient int reInviteWaitTime;
    protected Address remoteParty;
    private long remoteSequenceNumber;
    private Address remoteTarget;
    private transient int retransmissionTicksLeft;
    private RouteList routeList;
    private boolean sequenceNumberValidation;
    private boolean serverTransactionFlag;
    private transient SipProviderImpl sipProvider;
    private transient SIPTransactionStack sipStack;
    private transient String stackTrace;
    private boolean terminateOnBye;
    protected transient DialogTimerTask timerTask;
    private Semaphore timerTaskLock;

    public class ReInviteSender implements Runnable, Serializable {
        private static final long serialVersionUID = 1019346148741070635L;
        ClientTransaction ctx;

        public void terminate() {
            try {
                this.ctx.terminate();
                Thread.currentThread().interrupt();
            } catch (ObjectInUseException e) {
                SIPDialog.this.sipStack.getStackLogger().logError("unexpected error", e);
            }
        }

        public ReInviteSender(ClientTransaction ctx2) {
            this.ctx = ctx2;
        }

        public void run() {
            long timeToWait = 0;
            try {
                long startTime = System.currentTimeMillis();
                if (!SIPDialog.this.takeAckSem()) {
                    if (SIPDialog.this.sipStack.isLoggingEnabled()) {
                        SIPDialog.this.sipStack.getStackLogger().logError("Could not send re-INVITE time out ClientTransaction");
                    }
                    ((SIPClientTransaction) this.ctx).fireTimeoutTimer();
                    if (SIPDialog.this.sipProvider.getSipListener() == null || !(SIPDialog.this.sipProvider.getSipListener() instanceof SipListenerExt)) {
                        Request byeRequest = SIPDialog.this.createRequest("BYE");
                        if (MessageFactoryImpl.getDefaultUserAgentHeader() != null) {
                            byeRequest.addHeader(MessageFactoryImpl.getDefaultUserAgentHeader());
                        }
                        ReasonHeader reasonHeader = new Reason();
                        reasonHeader.setCause(HTMLModels.M_HEAD);
                        reasonHeader.setText("Timed out waiting to re-INVITE");
                        byeRequest.addHeader(reasonHeader);
                        SIPDialog.this.sendRequest(SIPDialog.this.getSipProvider().getNewClientTransaction(byeRequest));
                        this.ctx = null;
                        return;
                    }
                    SIPDialog.this.raiseErrorEvent(3);
                }
                if (SIPDialog.this.getState() != DialogState.TERMINATED) {
                    timeToWait = System.currentTimeMillis() - startTime;
                }
                if (timeToWait != 0) {
                    try {
                        Thread.sleep((long) SIPDialog.this.reInviteWaitTime);
                    } catch (InterruptedException e) {
                        if (SIPDialog.this.sipStack.isLoggingEnabled()) {
                            SIPDialog.this.sipStack.getStackLogger().logDebug("Interrupted sleep");
                        }
                        this.ctx = null;
                        return;
                    }
                }
                if (SIPDialog.this.getState() != DialogState.TERMINATED) {
                    SIPDialog.this.sendRequest(this.ctx, true);
                }
                if (SIPDialog.this.sipStack.isLoggingEnabled()) {
                    SIPDialog.this.sipStack.getStackLogger().logDebug("re-INVITE successfully sent");
                }
            } catch (Exception ex) {
                SIPDialog.this.sipStack.getStackLogger().logError("Error sending re-INVITE", ex);
            } catch (Throwable th) {
                this.ctx = null;
                throw th;
            }
            this.ctx = null;
        }
    }

    /* access modifiers changed from: package-private */
    public class LingerTimer extends SIPStackTimerTask implements Serializable {
        public LingerTimer() {
        }

        /* access modifiers changed from: protected */
        @Override // gov.nist.javax.sip.stack.SIPStackTimerTask
        public void runTask() {
            SIPDialog dialog = SIPDialog.this;
            if (SIPDialog.this.eventListeners != null) {
                SIPDialog.this.eventListeners.clear();
            }
            SIPDialog.this.timerTaskLock = null;
            SIPDialog.this.sipStack.removeDialog(dialog);
        }
    }

    /* access modifiers changed from: package-private */
    public class DialogTimerTask extends SIPStackTimerTask implements Serializable {
        int nRetransmissions = 0;
        SIPServerTransaction transaction;

        public DialogTimerTask(SIPServerTransaction transaction2) {
            this.transaction = transaction2;
        }

        /* access modifiers changed from: protected */
        @Override // gov.nist.javax.sip.stack.SIPStackTimerTask
        public void runTask() {
            StackLogger stackLogger;
            StringBuilder sb;
            SIPDialog dialog = SIPDialog.this;
            if (SIPDialog.this.sipStack.isLoggingEnabled()) {
                SIPDialog.this.sipStack.getStackLogger().logDebug("Running dialog timer");
            }
            this.nRetransmissions++;
            SIPServerTransaction transaction2 = this.transaction;
            if (this.nRetransmissions > 64) {
                if (SIPDialog.this.sipProvider.getSipListener() == null || !(SIPDialog.this.sipProvider.getSipListener() instanceof SipListenerExt)) {
                    dialog.delete();
                } else {
                    SIPDialog.this.raiseErrorEvent(1);
                }
                if (!(transaction2 == null || transaction2.getState() == TransactionState.TERMINATED)) {
                    transaction2.raiseErrorEvent(1);
                }
            } else if (!dialog.ackSeen && transaction2 != null) {
                SIPResponse response = transaction2.getLastResponse();
                if (response.getStatusCode() == 200) {
                    try {
                        if (dialog.toRetransmitFinalResponse(transaction2.T2)) {
                            transaction2.sendMessage(response);
                        }
                        SIPTransactionStack stack = dialog.sipStack;
                        if (stack.isLoggingEnabled()) {
                            stackLogger = stack.getStackLogger();
                            sb = new StringBuilder();
                            sb.append("resend 200 response from ");
                            sb.append(dialog);
                            stackLogger.logDebug(sb.toString());
                        }
                    } catch (IOException e) {
                        SIPDialog.this.raiseIOException(transaction2.getPeerAddress(), transaction2.getPeerPort(), transaction2.getPeerProtocol());
                        SIPTransactionStack stack2 = dialog.sipStack;
                        if (stack2.isLoggingEnabled()) {
                            stackLogger = stack2.getStackLogger();
                            sb = new StringBuilder();
                        }
                    } catch (Throwable th) {
                        SIPTransactionStack stack3 = dialog.sipStack;
                        if (stack3.isLoggingEnabled()) {
                            stack3.getStackLogger().logDebug("resend 200 response from " + dialog);
                        }
                        transaction2.fireTimer();
                        throw th;
                    }
                    transaction2.fireTimer();
                }
            }
            if (dialog.isAckSeen() || dialog.dialogState == SIPDialog.TERMINATED_STATE) {
                this.transaction = null;
                cancel();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class DialogDeleteTask extends SIPStackTimerTask implements Serializable {
        DialogDeleteTask() {
        }

        /* access modifiers changed from: protected */
        @Override // gov.nist.javax.sip.stack.SIPStackTimerTask
        public void runTask() {
            SIPDialog.this.delete();
        }
    }

    /* access modifiers changed from: package-private */
    public class DialogDeleteIfNoAckSentTask extends SIPStackTimerTask implements Serializable {
        private long seqno;

        public DialogDeleteIfNoAckSentTask(long seqno2) {
            this.seqno = seqno2;
        }

        /* access modifiers changed from: protected */
        @Override // gov.nist.javax.sip.stack.SIPStackTimerTask
        public void runTask() {
            if (SIPDialog.this.highestSequenceNumberAcknowledged < this.seqno) {
                SIPDialog.this.dialogDeleteIfNoAckSentTask = null;
                if (!SIPDialog.this.isBackToBackUserAgent) {
                    if (SIPDialog.this.sipStack.isLoggingEnabled()) {
                        SIPDialog.this.sipStack.getStackLogger().logError("ACK Was not sent. killing dialog");
                    }
                    if (SIPDialog.this.sipProvider.getSipListener() instanceof SipListenerExt) {
                        SIPDialog.this.raiseErrorEvent(2);
                    } else {
                        SIPDialog.this.delete();
                    }
                } else {
                    if (SIPDialog.this.sipStack.isLoggingEnabled()) {
                        SIPDialog.this.sipStack.getStackLogger().logError("ACK Was not sent. Sending BYE");
                    }
                    if (SIPDialog.this.sipProvider.getSipListener() instanceof SipListenerExt) {
                        SIPDialog.this.raiseErrorEvent(2);
                        return;
                    }
                    try {
                        Request byeRequest = SIPDialog.this.createRequest("BYE");
                        if (MessageFactoryImpl.getDefaultUserAgentHeader() != null) {
                            byeRequest.addHeader(MessageFactoryImpl.getDefaultUserAgentHeader());
                        }
                        ReasonHeader reasonHeader = new Reason();
                        reasonHeader.setProtocol("SIP");
                        reasonHeader.setCause(1025);
                        reasonHeader.setText("Timed out waiting to send ACK");
                        byeRequest.addHeader(reasonHeader);
                        SIPDialog.this.sendRequest(SIPDialog.this.getSipProvider().getNewClientTransaction(byeRequest));
                    } catch (Exception e) {
                        SIPDialog.this.delete();
                    }
                }
            }
        }
    }

    private SIPDialog(SipProviderImpl provider) {
        this.auditTag = 0;
        this.ackSem = new Semaphore(1);
        this.reInviteWaitTime = 100;
        this.highestSequenceNumberAcknowledged = -1;
        this.sequenceNumberValidation = true;
        this.timerTaskLock = new Semaphore(1);
        this.firstTransactionPort = 5060;
        this.terminateOnBye = true;
        this.routeList = new RouteList();
        this.dialogState = -1;
        this.localSequenceNumber = 0;
        this.remoteSequenceNumber = -1;
        this.sipProvider = provider;
        this.eventListeners = new CopyOnWriteArraySet();
    }

    private void recordStackTrace() {
        StringWriter stringWriter = new StringWriter();
        new Exception().printStackTrace(new PrintWriter(stringWriter));
        this.stackTrace = stringWriter.getBuffer().toString();
    }

    public SIPDialog(SIPTransaction transaction) {
        this(transaction.getSipProvider());
        SIPRequest sipRequest = (SIPRequest) transaction.getRequest();
        this.callIdHeader = sipRequest.getCallId();
        this.earlyDialogId = sipRequest.getDialogId(false);
        this.sipStack = transaction.sipStack;
        this.sipProvider = transaction.getSipProvider();
        if (this.sipProvider != null) {
            addTransaction(transaction);
            if (this.sipStack.isLoggingEnabled()) {
                StackLogger stackLogger = this.sipStack.getStackLogger();
                stackLogger.logDebug("Creating a dialog : " + this);
                StackLogger stackLogger2 = this.sipStack.getStackLogger();
                stackLogger2.logDebug("provider port = " + this.sipProvider.getListeningPoint().getPort());
                this.sipStack.getStackLogger().logStackTrace();
            }
            this.isBackToBackUserAgent = this.sipStack.isBackToBackUserAgent;
            addEventListener(this.sipStack);
            return;
        }
        throw new NullPointerException("Null Provider!");
    }

    public SIPDialog(SIPClientTransaction transaction, SIPResponse sipResponse) {
        this(transaction);
        if (sipResponse != null) {
            setLastResponse(transaction, sipResponse);
            this.isBackToBackUserAgent = this.sipStack.isBackToBackUserAgent;
            return;
        }
        throw new NullPointerException("Null SipResponse");
    }

    public SIPDialog(SipProviderImpl sipProvider2, SIPResponse sipResponse) {
        this(sipProvider2);
        this.sipStack = (SIPTransactionStack) sipProvider2.getSipStack();
        setLastResponse(null, sipResponse);
        this.localSequenceNumber = sipResponse.getCSeq().getSeqNumber();
        this.originalLocalSequenceNumber = this.localSequenceNumber;
        this.myTag = sipResponse.getFrom().getTag();
        this.hisTag = sipResponse.getTo().getTag();
        this.localParty = sipResponse.getFrom().getAddress();
        this.remoteParty = sipResponse.getTo().getAddress();
        this.method = sipResponse.getCSeq().getMethod();
        this.callIdHeader = sipResponse.getCallId();
        this.serverTransactionFlag = false;
        if (this.sipStack.isLoggingEnabled()) {
            StackLogger stackLogger = this.sipStack.getStackLogger();
            stackLogger.logDebug("Creating a dialog : " + this);
            this.sipStack.getStackLogger().logStackTrace();
        }
        this.isBackToBackUserAgent = this.sipStack.isBackToBackUserAgent;
        addEventListener(this.sipStack);
    }

    private void printRouteList() {
        if (this.sipStack.isLoggingEnabled()) {
            StackLogger stackLogger = this.sipStack.getStackLogger();
            stackLogger.logDebug("this : " + this);
            StackLogger stackLogger2 = this.sipStack.getStackLogger();
            stackLogger2.logDebug("printRouteList : " + this.routeList.encode());
        }
    }

    private boolean isClientDialog() {
        return ((SIPTransaction) getFirstTransaction()) instanceof SIPClientTransaction;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void raiseIOException(String host, int port, String protocol) {
        this.sipProvider.handleEvent(new IOExceptionEvent(this, host, port, protocol), null);
        setState(TERMINATED_STATE);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void raiseErrorEvent(int dialogTimeoutError) {
        SIPDialogErrorEvent newErrorEvent = new SIPDialogErrorEvent(this, dialogTimeoutError);
        synchronized (this.eventListeners) {
            for (SIPDialogEventListener nextListener : this.eventListeners) {
                nextListener.dialogErrorEvent(newErrorEvent);
            }
        }
        this.eventListeners.clear();
        if (!(dialogTimeoutError == 2 || dialogTimeoutError == 1 || dialogTimeoutError == 3)) {
            delete();
        }
        stopTimer();
    }

    private void setRemoteParty(SIPMessage sipMessage) {
        if (!isServer()) {
            this.remoteParty = sipMessage.getTo().getAddress();
        } else {
            this.remoteParty = sipMessage.getFrom().getAddress();
        }
        if (this.sipStack.isLoggingEnabled()) {
            StackLogger stackLogger = this.sipStack.getStackLogger();
            stackLogger.logDebug("settingRemoteParty " + this.remoteParty);
        }
    }

    /*  JADX ERROR: StackOverflowError in pass: MarkFinallyVisitor
        java.lang.StackOverflowError
        	at jadx.core.dex.nodes.InsnNode.isSame(InsnNode.java:303)
        	at jadx.core.dex.instructions.InvokeNode.isSame(InvokeNode.java:77)
        	at jadx.core.dex.visitors.MarkFinallyVisitor.sameInsns(MarkFinallyVisitor.java:451)
        	at jadx.core.dex.visitors.MarkFinallyVisitor.compareBlocks(MarkFinallyVisitor.java:436)
        	at jadx.core.dex.visitors.MarkFinallyVisitor.checkBlocksTree(MarkFinallyVisitor.java:408)
        	at jadx.core.dex.visitors.MarkFinallyVisitor.checkBlocksTree(MarkFinallyVisitor.java:411)
        */
    private void addRoute(gov.nist.javax.sip.header.RecordRouteList r8) {
        /*
            r7 = this;
            java.lang.String r0 = "NON LR route in Route set detected for dialog : "
            boolean r1 = r7.isClientDialog()     // Catch:{ all -> 0x00e9 }
            r2 = 1
            if (r1 == 0) goto L_0x004f
            gov.nist.javax.sip.header.RouteList r1 = new gov.nist.javax.sip.header.RouteList     // Catch:{ all -> 0x00e9 }
            r1.<init>()     // Catch:{ all -> 0x00e9 }
            r7.routeList = r1     // Catch:{ all -> 0x00e9 }
            int r1 = r8.size()     // Catch:{ all -> 0x00e9 }
            java.util.ListIterator r1 = r8.listIterator(r1)     // Catch:{ all -> 0x00e9 }
        L_0x0019:
            boolean r3 = r1.hasPrevious()     // Catch:{ all -> 0x00e9 }
            if (r3 == 0) goto L_0x004e
            java.lang.Object r3 = r1.previous()     // Catch:{ all -> 0x00e9 }
            gov.nist.javax.sip.header.RecordRoute r3 = (gov.nist.javax.sip.header.RecordRoute) r3     // Catch:{ all -> 0x00e9 }
            if (r2 == 0) goto L_0x004d
            gov.nist.javax.sip.header.Route r4 = new gov.nist.javax.sip.header.Route     // Catch:{ all -> 0x00e9 }
            r4.<init>()     // Catch:{ all -> 0x00e9 }
            javax.sip.address.Address r5 = r3.getAddress()     // Catch:{ all -> 0x00e9 }
            gov.nist.javax.sip.address.AddressImpl r5 = (gov.nist.javax.sip.address.AddressImpl) r5     // Catch:{ all -> 0x00e9 }
            java.lang.Object r5 = r5.clone()     // Catch:{ all -> 0x00e9 }
            gov.nist.javax.sip.address.AddressImpl r5 = (gov.nist.javax.sip.address.AddressImpl) r5     // Catch:{ all -> 0x00e9 }
            r4.setAddress(r5)     // Catch:{ all -> 0x00e9 }
            gov.nist.core.NameValueList r6 = r3.getParameters()     // Catch:{ all -> 0x00e9 }
            java.lang.Object r6 = r6.clone()     // Catch:{ all -> 0x00e9 }
            gov.nist.core.NameValueList r6 = (gov.nist.core.NameValueList) r6     // Catch:{ all -> 0x00e9 }
            r4.setParameters(r6)     // Catch:{ all -> 0x00e9 }
            gov.nist.javax.sip.header.RouteList r6 = r7.routeList     // Catch:{ all -> 0x00e9 }
            r6.add(r4)     // Catch:{ all -> 0x00e9 }
        L_0x004d:
            goto L_0x0019
        L_0x004e:
            goto L_0x0090
        L_0x004f:
            gov.nist.javax.sip.header.RouteList r1 = new gov.nist.javax.sip.header.RouteList     // Catch:{ all -> 0x00e9 }
            r1.<init>()     // Catch:{ all -> 0x00e9 }
            r7.routeList = r1     // Catch:{ all -> 0x00e9 }
            java.util.ListIterator r1 = r8.listIterator()     // Catch:{ all -> 0x00e9 }
        L_0x005b:
            boolean r3 = r1.hasNext()     // Catch:{ all -> 0x00e9 }
            if (r3 == 0) goto L_0x0090
            java.lang.Object r3 = r1.next()     // Catch:{ all -> 0x00e9 }
            gov.nist.javax.sip.header.RecordRoute r3 = (gov.nist.javax.sip.header.RecordRoute) r3     // Catch:{ all -> 0x00e9 }
            if (r2 == 0) goto L_0x008f
            gov.nist.javax.sip.header.Route r4 = new gov.nist.javax.sip.header.Route     // Catch:{ all -> 0x00e9 }
            r4.<init>()     // Catch:{ all -> 0x00e9 }
            javax.sip.address.Address r5 = r3.getAddress()     // Catch:{ all -> 0x00e9 }
            gov.nist.javax.sip.address.AddressImpl r5 = (gov.nist.javax.sip.address.AddressImpl) r5     // Catch:{ all -> 0x00e9 }
            java.lang.Object r5 = r5.clone()     // Catch:{ all -> 0x00e9 }
            gov.nist.javax.sip.address.AddressImpl r5 = (gov.nist.javax.sip.address.AddressImpl) r5     // Catch:{ all -> 0x00e9 }
            r4.setAddress(r5)     // Catch:{ all -> 0x00e9 }
            gov.nist.core.NameValueList r6 = r3.getParameters()     // Catch:{ all -> 0x00e9 }
            java.lang.Object r6 = r6.clone()     // Catch:{ all -> 0x00e9 }
            gov.nist.core.NameValueList r6 = (gov.nist.core.NameValueList) r6     // Catch:{ all -> 0x00e9 }
            r4.setParameters(r6)     // Catch:{ all -> 0x00e9 }
            gov.nist.javax.sip.header.RouteList r6 = r7.routeList     // Catch:{ all -> 0x00e9 }
            r6.add(r4)     // Catch:{ all -> 0x00e9 }
        L_0x008f:
            goto L_0x005b
        L_0x0090:
            gov.nist.javax.sip.stack.SIPTransactionStack r1 = r7.sipStack
            gov.nist.core.StackLogger r1 = r1.getStackLogger()
            boolean r1 = r1.isLoggingEnabled()
            if (r1 == 0) goto L_0x00e8
            gov.nist.javax.sip.header.RouteList r1 = r7.routeList
            java.util.Iterator r1 = r1.iterator()
        L_0x00a2:
            boolean r2 = r1.hasNext()
            if (r2 == 0) goto L_0x00e8
            java.lang.Object r2 = r1.next()
            gov.nist.javax.sip.header.Route r2 = (gov.nist.javax.sip.header.Route) r2
            javax.sip.address.Address r2 = r2.getAddress()
            javax.sip.address.URI r2 = r2.getURI()
            javax.sip.address.SipURI r2 = (javax.sip.address.SipURI) r2
            boolean r3 = r2.hasLrParam()
            if (r3 != 0) goto L_0x00e7
            gov.nist.javax.sip.stack.SIPTransactionStack r3 = r7.sipStack
            boolean r3 = r3.isLoggingEnabled()
            if (r3 == 0) goto L_0x00e7
            gov.nist.javax.sip.stack.SIPTransactionStack r3 = r7.sipStack
            gov.nist.core.StackLogger r3 = r3.getStackLogger()
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            r4.append(r0)
            r4.append(r7)
            java.lang.String r4 = r4.toString()
            r3.logWarning(r4)
            gov.nist.javax.sip.stack.SIPTransactionStack r3 = r7.sipStack
            gov.nist.core.StackLogger r3 = r3.getStackLogger()
            r3.logStackTrace()
        L_0x00e7:
            goto L_0x00a2
        L_0x00e8:
            return
        L_0x00e9:
            r1 = move-exception
            gov.nist.javax.sip.stack.SIPTransactionStack r2 = r7.sipStack
            gov.nist.core.StackLogger r2 = r2.getStackLogger()
            boolean r2 = r2.isLoggingEnabled()
            if (r2 == 0) goto L_0x0142
            gov.nist.javax.sip.header.RouteList r2 = r7.routeList
            java.util.Iterator r2 = r2.iterator()
        L_0x00fc:
            boolean r3 = r2.hasNext()
            if (r3 == 0) goto L_0x0142
            java.lang.Object r3 = r2.next()
            gov.nist.javax.sip.header.Route r3 = (gov.nist.javax.sip.header.Route) r3
            javax.sip.address.Address r3 = r3.getAddress()
            javax.sip.address.URI r3 = r3.getURI()
            javax.sip.address.SipURI r3 = (javax.sip.address.SipURI) r3
            boolean r4 = r3.hasLrParam()
            if (r4 != 0) goto L_0x0141
            gov.nist.javax.sip.stack.SIPTransactionStack r4 = r7.sipStack
            boolean r4 = r4.isLoggingEnabled()
            if (r4 == 0) goto L_0x0141
            gov.nist.javax.sip.stack.SIPTransactionStack r4 = r7.sipStack
            gov.nist.core.StackLogger r4 = r4.getStackLogger()
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            r5.append(r0)
            r5.append(r7)
            java.lang.String r5 = r5.toString()
            r4.logWarning(r5)
            gov.nist.javax.sip.stack.SIPTransactionStack r4 = r7.sipStack
            gov.nist.core.StackLogger r4 = r4.getStackLogger()
            r4.logStackTrace()
        L_0x0141:
            goto L_0x00fc
        L_0x0142:
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: gov.nist.javax.sip.stack.SIPDialog.addRoute(gov.nist.javax.sip.header.RecordRouteList):void");
    }

    /* access modifiers changed from: package-private */
    public void setRemoteTarget(ContactHeader contact) {
        this.remoteTarget = contact.getAddress();
        if (this.sipStack.isLoggingEnabled()) {
            StackLogger stackLogger = this.sipStack.getStackLogger();
            stackLogger.logDebug("Dialog.setRemoteTarget: " + this.remoteTarget);
            this.sipStack.getStackLogger().logStackTrace();
        }
    }

    private synchronized void addRoute(SIPResponse sipResponse) {
        ContactList contactList;
        try {
            if (this.sipStack.isLoggingEnabled()) {
                try {
                    StackLogger stackLogger = this.sipStack.getStackLogger();
                    stackLogger.logDebug("setContact: dialogState: " + this + "state = " + getState());
                } catch (Throwable th) {
                    th = th;
                }
            }
            if (sipResponse.getStatusCode() == 100) {
                if (this.sipStack.isLoggingEnabled()) {
                    this.sipStack.getStackLogger().logStackTrace();
                }
            } else if (this.dialogState == TERMINATED_STATE) {
                if (this.sipStack.isLoggingEnabled()) {
                    this.sipStack.getStackLogger().logStackTrace();
                }
            } else if (this.dialogState == CONFIRMED_STATE) {
                if (sipResponse.getStatusCode() / 100 == 2 && !isServer() && (contactList = sipResponse.getContactHeaders()) != null && SIPRequest.isTargetRefresh(sipResponse.getCSeq().getMethod())) {
                    setRemoteTarget((ContactHeader) contactList.getFirst());
                }
                if (this.sipStack.isLoggingEnabled()) {
                    this.sipStack.getStackLogger().logStackTrace();
                }
            } else {
                if (!isServer()) {
                    if (!(getState() == DialogState.CONFIRMED || getState() == DialogState.TERMINATED)) {
                        RecordRouteList rrlist = sipResponse.getRecordRouteHeaders();
                        if (rrlist != null) {
                            addRoute(rrlist);
                        } else {
                            this.routeList = new RouteList();
                        }
                    }
                    ContactList contactList2 = sipResponse.getContactHeaders();
                    if (contactList2 != null) {
                        setRemoteTarget((ContactHeader) contactList2.getFirst());
                    }
                }
                if (this.sipStack.isLoggingEnabled()) {
                    this.sipStack.getStackLogger().logStackTrace();
                }
            }
        } catch (Throwable th2) {
            th = th2;
            if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logStackTrace();
            }
            throw th;
        }
    }

    private synchronized RouteList getRouteList() {
        RouteList retval;
        if (this.sipStack.isLoggingEnabled()) {
            StackLogger stackLogger = this.sipStack.getStackLogger();
            stackLogger.logDebug("getRouteList " + this);
        }
        new RouteList();
        retval = new RouteList();
        if (this.routeList != null) {
            ListIterator li = this.routeList.listIterator();
            while (li.hasNext()) {
                retval.add((SIPHeader) ((Route) ((Route) li.next()).clone()));
            }
        }
        if (this.sipStack.isLoggingEnabled()) {
            this.sipStack.getStackLogger().logDebug("----- ");
            StackLogger stackLogger2 = this.sipStack.getStackLogger();
            stackLogger2.logDebug("getRouteList for " + this);
            StackLogger stackLogger3 = this.sipStack.getStackLogger();
            stackLogger3.logDebug("RouteList = " + retval.encode());
            if (this.routeList != null) {
                StackLogger stackLogger4 = this.sipStack.getStackLogger();
                stackLogger4.logDebug("myRouteList = " + this.routeList.encode());
            }
            this.sipStack.getStackLogger().logDebug("----- ");
        }
        return retval;
    }

    /* access modifiers changed from: package-private */
    public void setRouteList(RouteList routeList2) {
        this.routeList = routeList2;
    }

    /* JADX WARNING: Removed duplicated region for block: B:64:0x0252  */
    private void sendAck(Request request, boolean throwIOExceptionAsSipException) throws SipException {
        DialogDeleteTask dialogDeleteTask2;
        SIPRequest ackRequest = (SIPRequest) request;
        if (this.sipStack.isLoggingEnabled()) {
            StackLogger stackLogger = this.sipStack.getStackLogger();
            stackLogger.logDebug("sendAck" + this);
        }
        if (!ackRequest.getMethod().equals("ACK")) {
            throw new SipException("Bad request method -- should be ACK");
        } else if (getState() == null || getState().getValue() == EARLY_STATE) {
            if (this.sipStack.isLoggingEnabled()) {
                StackLogger stackLogger2 = this.sipStack.getStackLogger();
                stackLogger2.logError("Bad Dialog State for " + this + " dialogID = " + getDialogId());
            }
            throw new SipException("Bad dialog state " + getState());
        } else if (!getCallId().getCallId().equals(((SIPRequest) request).getCallId().getCallId())) {
            if (this.sipStack.isLoggingEnabled()) {
                StackLogger stackLogger3 = this.sipStack.getStackLogger();
                stackLogger3.logError("CallID " + getCallId());
                StackLogger stackLogger4 = this.sipStack.getStackLogger();
                stackLogger4.logError("RequestCallID = " + ackRequest.getCallId().getCallId());
                StackLogger stackLogger5 = this.sipStack.getStackLogger();
                stackLogger5.logError("dialog =  " + this);
            }
            throw new SipException("Bad call ID in request");
        } else {
            try {
                if (this.sipStack.isLoggingEnabled()) {
                    StackLogger stackLogger6 = this.sipStack.getStackLogger();
                    stackLogger6.logDebug("setting from tag For outgoing ACK= " + getLocalTag());
                    StackLogger stackLogger7 = this.sipStack.getStackLogger();
                    stackLogger7.logDebug("setting To tag for outgoing ACK = " + getRemoteTag());
                    StackLogger stackLogger8 = this.sipStack.getStackLogger();
                    stackLogger8.logDebug("ack = " + ackRequest);
                }
                if (getLocalTag() != null) {
                    ackRequest.getFrom().setTag(getLocalTag());
                }
                if (getRemoteTag() != null) {
                    ackRequest.getTo().setTag(getRemoteTag());
                }
                Hop hop = this.sipStack.getNextHop(ackRequest);
                if (hop != null) {
                    try {
                        if (this.sipStack.isLoggingEnabled()) {
                            StackLogger stackLogger9 = this.sipStack.getStackLogger();
                            stackLogger9.logDebug("hop = " + hop);
                        }
                        ListeningPointImpl lp = (ListeningPointImpl) this.sipProvider.getListeningPoint(hop.getTransport());
                        if (lp != null) {
                            MessageChannel messageChannel = lp.getMessageProcessor().createMessageChannel(InetAddress.getByName(hop.getHost()), hop.getPort());
                            boolean releaseAckSem = false;
                            if (!isAckSent(((SIPRequest) request).getCSeq().getSeqNumber())) {
                                releaseAckSem = true;
                            }
                            setLastAckSent(ackRequest);
                            messageChannel.sendMessage(ackRequest);
                            this.isAcknowledged = true;
                            this.highestSequenceNumberAcknowledged = Math.max(this.highestSequenceNumberAcknowledged, ackRequest.getCSeq().getSeqNumber());
                            if (!releaseAckSem || !this.isBackToBackUserAgent) {
                                if (this.sipStack.isLoggingEnabled()) {
                                    StackLogger stackLogger10 = this.sipStack.getStackLogger();
                                    stackLogger10.logDebug("Not releasing ack sem for " + this + " isAckSent " + releaseAckSem);
                                }
                                dialogDeleteTask2 = this.dialogDeleteTask;
                                if (dialogDeleteTask2 != null) {
                                    dialogDeleteTask2.cancel();
                                    this.dialogDeleteTask = null;
                                }
                                this.ackSeen = true;
                                return;
                            }
                            releaseAckSem();
                            dialogDeleteTask2 = this.dialogDeleteTask;
                            if (dialogDeleteTask2 != null) {
                            }
                            this.ackSeen = true;
                            return;
                        }
                        throw new SipException("No listening point for this provider registered at " + hop);
                    } catch (IOException ex) {
                        if (!throwIOExceptionAsSipException) {
                            raiseIOException(hop.getHost(), hop.getPort(), hop.getTransport());
                        } else {
                            throw new SipException("Could not send ack", ex);
                        }
                    } catch (SipException ex2) {
                        if (this.sipStack.isLoggingEnabled()) {
                            this.sipStack.getStackLogger().logException(ex2);
                        }
                        throw ex2;
                    } catch (Exception ex3) {
                        if (this.sipStack.isLoggingEnabled()) {
                            this.sipStack.getStackLogger().logException(ex3);
                        }
                        throw new SipException("Could not create message channel", ex3);
                    }
                } else {
                    throw new SipException("No route!");
                }
            } catch (ParseException ex4) {
                throw new SipException(ex4.getMessage());
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setStack(SIPTransactionStack sipStack2) {
        this.sipStack = sipStack2;
    }

    /* access modifiers changed from: package-private */
    public SIPTransactionStack getStack() {
        return this.sipStack;
    }

    /* access modifiers changed from: package-private */
    public boolean isTerminatedOnBye() {
        return this.terminateOnBye;
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: package-private */
    public void ackReceived(SIPRequest sipRequest) {
        SIPServerTransaction tr;
        if (!this.ackSeen && (tr = getInviteTransaction()) != null && tr.getCSeq() == sipRequest.getCSeq().getSeqNumber()) {
            acquireTimerTaskSem();
            try {
                if (this.timerTask != null) {
                    this.timerTask.cancel();
                    this.timerTask = null;
                }
                releaseTimerTaskSem();
                this.ackSeen = true;
                DialogDeleteTask dialogDeleteTask2 = this.dialogDeleteTask;
                if (dialogDeleteTask2 != null) {
                    dialogDeleteTask2.cancel();
                    this.dialogDeleteTask = null;
                }
                setLastAckReceived(sipRequest);
                if (this.sipStack.isLoggingEnabled()) {
                    StackLogger stackLogger = this.sipStack.getStackLogger();
                    stackLogger.logDebug("ackReceived for " + tr.getMethod());
                    this.ackLine = this.sipStack.getStackLogger().getLineCount();
                    printDebugInfo();
                }
                if (this.isBackToBackUserAgent) {
                    releaseAckSem();
                }
                setState(CONFIRMED_STATE);
            } catch (Throwable th) {
                releaseTimerTaskSem();
                throw th;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public synchronized boolean testAndSetIsDialogTerminatedEventDelivered() {
        boolean retval;
        retval = this.dialogTerminatedEventDelivered;
        this.dialogTerminatedEventDelivered = true;
        return retval;
    }

    public void addEventListener(SIPDialogEventListener newListener) {
        this.eventListeners.add(newListener);
    }

    public void removeEventListener(SIPDialogEventListener oldListener) {
        this.eventListeners.remove(oldListener);
    }

    @Override // javax.sip.Dialog
    public void setApplicationData(Object applicationData2) {
        this.applicationData = applicationData2;
    }

    @Override // javax.sip.Dialog
    public Object getApplicationData() {
        return this.applicationData;
    }

    public synchronized void requestConsumed() {
        this.nextSeqno = Long.valueOf(getRemoteSeqNumber() + 1);
        if (this.sipStack.isLoggingEnabled()) {
            StackLogger stackLogger = this.sipStack.getStackLogger();
            stackLogger.logDebug("Request Consumed -- next consumable Request Seqno = " + this.nextSeqno);
        }
    }

    public synchronized boolean isRequestConsumable(SIPRequest dialogRequest) {
        if (!dialogRequest.getMethod().equals("ACK")) {
            boolean z = true;
            if (!isSequnceNumberValidation()) {
                return true;
            }
            if (this.remoteSequenceNumber >= dialogRequest.getCSeq().getSeqNumber()) {
                z = false;
            }
            return z;
        }
        throw new RuntimeException("Illegal method");
    }

    public void doDeferredDelete() {
        if (this.sipStack.getTimer() == null) {
            setState(TERMINATED_STATE);
            return;
        }
        this.dialogDeleteTask = new DialogDeleteTask();
        this.sipStack.getTimer().schedule(this.dialogDeleteTask, 32000);
    }

    public void setState(int state) {
        if (this.sipStack.isLoggingEnabled()) {
            StackLogger stackLogger = this.sipStack.getStackLogger();
            stackLogger.logDebug("Setting dialog state for " + this + "newState = " + state);
            this.sipStack.getStackLogger().logStackTrace();
            if (!(state == -1 || state == this.dialogState || !this.sipStack.isLoggingEnabled())) {
                StackLogger stackLogger2 = this.sipStack.getStackLogger();
                stackLogger2.logDebug(this + "  old dialog state is " + getState());
                StackLogger stackLogger3 = this.sipStack.getStackLogger();
                stackLogger3.logDebug(this + "  New dialog state is " + DialogState.getObject(state));
            }
        }
        this.dialogState = state;
        if (state == TERMINATED_STATE) {
            if (this.sipStack.getTimer() != null) {
                this.sipStack.getTimer().schedule(new LingerTimer(), 8000);
            }
            stopTimer();
        }
    }

    public void printDebugInfo() {
        if (this.sipStack.isLoggingEnabled()) {
            StackLogger stackLogger = this.sipStack.getStackLogger();
            stackLogger.logDebug("isServer = " + isServer());
            StackLogger stackLogger2 = this.sipStack.getStackLogger();
            stackLogger2.logDebug("localTag = " + getLocalTag());
            StackLogger stackLogger3 = this.sipStack.getStackLogger();
            stackLogger3.logDebug("remoteTag = " + getRemoteTag());
            StackLogger stackLogger4 = this.sipStack.getStackLogger();
            stackLogger4.logDebug("localSequenceNumer = " + getLocalSeqNumber());
            StackLogger stackLogger5 = this.sipStack.getStackLogger();
            stackLogger5.logDebug("remoteSequenceNumer = " + getRemoteSeqNumber());
            StackLogger stackLogger6 = this.sipStack.getStackLogger();
            stackLogger6.logDebug("ackLine:" + getRemoteTag() + Separators.SP + this.ackLine);
        }
    }

    public boolean isAckSeen() {
        return this.ackSeen;
    }

    public SIPRequest getLastAckSent() {
        return this.lastAckSent;
    }

    public boolean isAckSent(long cseqNo) {
        if (getLastTransaction() == null || !(getLastTransaction() instanceof ClientTransaction)) {
            return true;
        }
        if (getLastAckSent() == null) {
            return false;
        }
        if (cseqNo <= getLastAckSent().getCSeq().getSeqNumber()) {
            return true;
        }
        return false;
    }

    @Override // javax.sip.Dialog
    public Transaction getFirstTransaction() {
        return this.firstTransaction;
    }

    @Override // javax.sip.Dialog
    public Iterator getRouteSet() {
        if (this.routeList == null) {
            return new LinkedList().listIterator();
        }
        return getRouteList().listIterator();
    }

    public synchronized void addRoute(SIPRequest sipRequest) {
        if (this.sipStack.isLoggingEnabled()) {
            StackLogger stackLogger = this.sipStack.getStackLogger();
            stackLogger.logDebug("setContact: dialogState: " + this + "state = " + getState());
        }
        if (this.dialogState == CONFIRMED_STATE && SIPRequest.isTargetRefresh(sipRequest.getMethod())) {
            doTargetRefresh(sipRequest);
        }
        if (this.dialogState != CONFIRMED_STATE) {
            if (this.dialogState != TERMINATED_STATE) {
                if (sipRequest.getToTag() == null) {
                    RecordRouteList rrlist = sipRequest.getRecordRouteHeaders();
                    if (rrlist != null) {
                        addRoute(rrlist);
                    } else {
                        this.routeList = new RouteList();
                    }
                    ContactList contactList = sipRequest.getContactHeaders();
                    if (contactList != null) {
                        setRemoteTarget((ContactHeader) contactList.getFirst());
                    }
                }
            }
        }
    }

    public void setDialogId(String dialogId2) {
        this.dialogId = dialogId2;
    }

    public static SIPDialog createFromNOTIFY(SIPClientTransaction subscribeTx, SIPTransaction notifyST) {
        SIPDialog d = new SIPDialog(notifyST);
        d.serverTransactionFlag = false;
        d.lastTransaction = subscribeTx;
        storeFirstTransactionInfo(d, subscribeTx);
        d.terminateOnBye = false;
        d.localSequenceNumber = subscribeTx.getCSeq();
        SIPRequest not = (SIPRequest) notifyST.getRequest();
        d.remoteSequenceNumber = not.getCSeq().getSeqNumber();
        d.setDialogId(not.getDialogId(true));
        d.setLocalTag(not.getToTag());
        d.setRemoteTag(not.getFromTag());
        d.setLastResponse(subscribeTx, subscribeTx.getLastResponse());
        d.localParty = not.getTo().getAddress();
        d.remoteParty = not.getFrom().getAddress();
        d.addRoute(not);
        d.setState(CONFIRMED_STATE);
        return d;
    }

    @Override // javax.sip.Dialog
    public boolean isServer() {
        if (!this.firstTransactionSeen) {
            return this.serverTransactionFlag;
        }
        return this.firstTransactionIsServerTransaction;
    }

    /* access modifiers changed from: protected */
    public boolean isReInvite() {
        return this.reInviteFlag;
    }

    @Override // javax.sip.Dialog
    public String getDialogId() {
        SIPResponse sIPResponse;
        if (this.dialogId == null && (sIPResponse = this.lastResponse) != null) {
            this.dialogId = sIPResponse.getDialogId(isServer());
        }
        return this.dialogId;
    }

    private static void storeFirstTransactionInfo(SIPDialog dialog, SIPTransaction transaction) {
        dialog.firstTransaction = transaction;
        dialog.firstTransactionSeen = true;
        dialog.firstTransactionIsServerTransaction = transaction.isServerTransaction();
        dialog.firstTransactionSecure = transaction.getRequest().getRequestURI().getScheme().equalsIgnoreCase("sips");
        dialog.firstTransactionPort = transaction.getPort();
        dialog.firstTransactionId = transaction.getBranchId();
        dialog.firstTransactionMethod = transaction.getMethod();
        if (dialog.isServer()) {
            SIPResponse response = ((SIPServerTransaction) transaction).getLastResponse();
            dialog.contactHeader = response != null ? response.getContactHeader() : null;
            return;
        }
        dialog.contactHeader = ((SIPClientTransaction) transaction).getOriginalRequest().getContactHeader();
    }

    public void addTransaction(SIPTransaction transaction) {
        SIPRequest sipRequest = transaction.getOriginalRequest();
        if (this.firstTransactionSeen && !this.firstTransactionId.equals(transaction.getBranchId()) && transaction.getMethod().equals(this.firstTransactionMethod)) {
            this.reInviteFlag = true;
        }
        if (!this.firstTransactionSeen) {
            storeFirstTransactionInfo(this, transaction);
            if (sipRequest.getMethod().equals("SUBSCRIBE")) {
                this.eventHeader = (EventHeader) sipRequest.getHeader("Event");
            }
            setLocalParty(sipRequest);
            setRemoteParty(sipRequest);
            setCallId(sipRequest);
            if (this.originalRequest == null) {
                this.originalRequest = sipRequest;
            }
            if (this.method == null) {
                this.method = sipRequest.getMethod();
            }
            if (transaction instanceof SIPServerTransaction) {
                this.hisTag = sipRequest.getFrom().getTag();
            } else {
                setLocalSequenceNumber(sipRequest.getCSeq().getSeqNumber());
                this.originalLocalSequenceNumber = this.localSequenceNumber;
                this.myTag = sipRequest.getFrom().getTag();
                if (this.myTag == null && this.sipStack.isLoggingEnabled()) {
                    this.sipStack.getStackLogger().logError("The request's From header is missing the required Tag parameter.");
                }
            }
        } else if (transaction.getMethod().equals(this.firstTransactionMethod) && this.firstTransactionIsServerTransaction != transaction.isServerTransaction()) {
            storeFirstTransactionInfo(this, transaction);
            setLocalParty(sipRequest);
            setRemoteParty(sipRequest);
            setCallId(sipRequest);
            this.originalRequest = sipRequest;
            this.method = sipRequest.getMethod();
        }
        if (transaction instanceof SIPServerTransaction) {
            setRemoteSequenceNumber(sipRequest.getCSeq().getSeqNumber());
        }
        this.lastTransaction = transaction;
        if (this.sipStack.isLoggingEnabled()) {
            StackLogger stackLogger = this.sipStack.getStackLogger();
            stackLogger.logDebug("Transaction Added " + this + this.myTag + Separators.SLASH + this.hisTag);
            StackLogger stackLogger2 = this.sipStack.getStackLogger();
            stackLogger2.logDebug("TID = " + transaction.getTransactionId() + Separators.SLASH + transaction.isServerTransaction());
            this.sipStack.getStackLogger().logStackTrace();
        }
    }

    private void setRemoteTag(String hisTag2) {
        if (this.sipStack.isLoggingEnabled()) {
            StackLogger stackLogger = this.sipStack.getStackLogger();
            stackLogger.logDebug("setRemoteTag(): " + this + " remoteTag = " + this.hisTag + " new tag = " + hisTag2);
        }
        String str = this.hisTag;
        if (str == null || hisTag2 == null || hisTag2.equals(str)) {
            if (hisTag2 != null) {
                this.hisTag = hisTag2;
            } else if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logWarning("setRemoteTag : called with null argument ");
            }
        } else if (getState() != DialogState.EARLY) {
            if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logDebug("Dialog is already established -- ignoring remote tag re-assignment");
            }
        } else if (this.sipStack.isRemoteTagReassignmentAllowed()) {
            if (this.sipStack.isLoggingEnabled()) {
                StackLogger stackLogger2 = this.sipStack.getStackLogger();
                stackLogger2.logDebug("UNSAFE OPERATION !  tag re-assignment " + this.hisTag + " trying to set to " + hisTag2 + " can cause unexpected effects ");
            }
            boolean removed = false;
            if (this.sipStack.getDialog(this.dialogId) == this) {
                this.sipStack.removeDialog(this.dialogId);
                removed = true;
            }
            this.dialogId = null;
            this.hisTag = hisTag2;
            if (removed) {
                if (this.sipStack.isLoggingEnabled()) {
                    this.sipStack.getStackLogger().logDebug("ReInserting Dialog");
                }
                this.sipStack.putDialog(this);
            }
        }
    }

    public SIPTransaction getLastTransaction() {
        return this.lastTransaction;
    }

    public SIPServerTransaction getInviteTransaction() {
        DialogTimerTask t = this.timerTask;
        if (t != null) {
            return t.transaction;
        }
        return null;
    }

    private void setLocalSequenceNumber(long lCseq) {
        if (this.sipStack.isLoggingEnabled()) {
            StackLogger stackLogger = this.sipStack.getStackLogger();
            stackLogger.logDebug("setLocalSequenceNumber: original  " + this.localSequenceNumber + " new  = " + lCseq);
        }
        if (lCseq > this.localSequenceNumber) {
            this.localSequenceNumber = lCseq;
            return;
        }
        throw new RuntimeException("Sequence number should not decrease !");
    }

    public void setRemoteSequenceNumber(long rCseq) {
        if (this.sipStack.isLoggingEnabled()) {
            StackLogger stackLogger = this.sipStack.getStackLogger();
            stackLogger.logDebug("setRemoteSeqno " + this + Separators.SLASH + rCseq);
        }
        this.remoteSequenceNumber = rCseq;
    }

    @Override // javax.sip.Dialog
    public void incrementLocalSequenceNumber() {
        this.localSequenceNumber++;
    }

    @Override // javax.sip.Dialog
    public int getRemoteSequenceNumber() {
        return (int) this.remoteSequenceNumber;
    }

    @Override // javax.sip.Dialog
    public int getLocalSequenceNumber() {
        return (int) this.localSequenceNumber;
    }

    public long getOriginalLocalSequenceNumber() {
        return this.originalLocalSequenceNumber;
    }

    @Override // javax.sip.Dialog
    public long getLocalSeqNumber() {
        return this.localSequenceNumber;
    }

    @Override // javax.sip.Dialog
    public long getRemoteSeqNumber() {
        return this.remoteSequenceNumber;
    }

    @Override // javax.sip.Dialog
    public String getLocalTag() {
        return this.myTag;
    }

    @Override // javax.sip.Dialog
    public String getRemoteTag() {
        return this.hisTag;
    }

    private void setLocalTag(String mytag) {
        if (this.sipStack.isLoggingEnabled()) {
            StackLogger stackLogger = this.sipStack.getStackLogger();
            stackLogger.logDebug("set Local tag " + mytag + Separators.SP + this.dialogId);
            this.sipStack.getStackLogger().logStackTrace();
        }
        this.myTag = mytag;
    }

    @Override // javax.sip.Dialog
    public void delete() {
        setState(TERMINATED_STATE);
    }

    @Override // javax.sip.Dialog
    public CallIdHeader getCallId() {
        return this.callIdHeader;
    }

    private void setCallId(SIPRequest sipRequest) {
        this.callIdHeader = sipRequest.getCallId();
    }

    @Override // javax.sip.Dialog
    public Address getLocalParty() {
        return this.localParty;
    }

    private void setLocalParty(SIPMessage sipMessage) {
        if (!isServer()) {
            this.localParty = sipMessage.getFrom().getAddress();
        } else {
            this.localParty = sipMessage.getTo().getAddress();
        }
    }

    @Override // javax.sip.Dialog
    public Address getRemoteParty() {
        if (this.sipStack.isLoggingEnabled()) {
            StackLogger stackLogger = this.sipStack.getStackLogger();
            stackLogger.logDebug("gettingRemoteParty " + this.remoteParty);
        }
        return this.remoteParty;
    }

    @Override // javax.sip.Dialog
    public Address getRemoteTarget() {
        return this.remoteTarget;
    }

    @Override // javax.sip.Dialog
    public DialogState getState() {
        int i = this.dialogState;
        if (i == -1) {
            return null;
        }
        return DialogState.getObject(i);
    }

    @Override // javax.sip.Dialog
    public boolean isSecure() {
        return this.firstTransactionSecure;
    }

    @Override // javax.sip.Dialog
    public void sendAck(Request request) throws SipException {
        sendAck(request, true);
    }

    @Override // javax.sip.Dialog
    public Request createRequest(String method2) throws SipException {
        if (method2.equals("ACK") || method2.equals(Request.PRACK)) {
            throw new SipException("Invalid method specified for createRequest:" + method2);
        }
        SIPResponse sIPResponse = this.lastResponse;
        if (sIPResponse != null) {
            return createRequest(method2, sIPResponse);
        }
        throw new SipException("Dialog not yet established -- no response!");
    }

    private Request createRequest(String method2, SIPResponse sipResponse) throws SipException {
        SipUri sipUri;
        EventHeader eventHeader2;
        if (method2 == null || sipResponse == null) {
            throw new NullPointerException("null argument");
        } else if (method2.equals(Request.CANCEL)) {
            throw new SipException("Dialog.createRequest(): Invalid request");
        } else if (getState() == null || ((getState().getValue() == TERMINATED_STATE && !method2.equalsIgnoreCase("BYE")) || (isServer() && getState().getValue() == EARLY_STATE && method2.equalsIgnoreCase("BYE")))) {
            throw new SipException("Dialog  " + getDialogId() + " not yet established or terminated " + getState());
        } else {
            if (getRemoteTarget() != null) {
                sipUri = (SipUri) getRemoteTarget().getURI().clone();
            } else {
                sipUri = (SipUri) getRemoteParty().getURI().clone();
                sipUri.clearUriParms();
            }
            CSeq cseq = new CSeq();
            try {
                cseq.setMethod(method2);
                cseq.setSeqNumber(getLocalSeqNumber());
            } catch (Exception ex) {
                if (this.sipStack.isLoggingEnabled()) {
                    this.sipStack.getStackLogger().logError("Unexpected error");
                }
                InternalErrorHandler.handleException(ex);
            }
            ListeningPointImpl lp = (ListeningPointImpl) this.sipProvider.getListeningPoint(sipResponse.getTopmostVia().getTransport());
            if (lp == null) {
                if (this.sipStack.isLoggingEnabled()) {
                    StackLogger stackLogger = this.sipStack.getStackLogger();
                    stackLogger.logError("Cannot find listening point for transport " + sipResponse.getTopmostVia().getTransport());
                }
                throw new SipException("Cannot find listening point for transport " + sipResponse.getTopmostVia().getTransport());
            }
            Via via = lp.getViaHeader();
            From from = new From();
            from.setAddress(this.localParty);
            To to = new To();
            to.setAddress(this.remoteParty);
            SIPRequest sipRequest = sipResponse.createRequest(sipUri, via, cseq, from, to);
            if (SIPRequest.isTargetRefresh(method2)) {
                ContactHeader contactHeader2 = ((ListeningPointImpl) this.sipProvider.getListeningPoint(lp.getTransport())).createContactHeader();
                ((SipURI) contactHeader2.getAddress().getURI()).setSecure(isSecure());
                sipRequest.setHeader(contactHeader2);
            }
            try {
                ((CSeq) sipRequest.getCSeq()).setSeqNumber(this.localSequenceNumber + 1);
            } catch (InvalidArgumentException ex2) {
                InternalErrorHandler.handleException(ex2);
            }
            if (method2.equals("SUBSCRIBE") && (eventHeader2 = this.eventHeader) != null) {
                sipRequest.addHeader(eventHeader2);
            }
            try {
                if (getLocalTag() != null) {
                    from.setTag(getLocalTag());
                } else {
                    from.removeTag();
                }
                if (getRemoteTag() != null) {
                    to.setTag(getRemoteTag());
                } else {
                    to.removeTag();
                }
            } catch (ParseException ex3) {
                InternalErrorHandler.handleException(ex3);
            }
            updateRequest(sipRequest);
            return sipRequest;
        }
    }

    @Override // javax.sip.Dialog
    public void sendRequest(ClientTransaction clientTransactionId) throws TransactionDoesNotExistException, SipException {
        sendRequest(clientTransactionId, !this.isBackToBackUserAgent);
    }

    public void sendRequest(ClientTransaction clientTransactionId, boolean allowInterleaving) throws TransactionDoesNotExistException, SipException {
        if (allowInterleaving || !clientTransactionId.getRequest().getMethod().equals("INVITE")) {
            SIPRequest dialogRequest = ((SIPClientTransaction) clientTransactionId).getOriginalRequest();
            if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logDebug("dialog.sendRequest  dialog = " + this + "\ndialogRequest = \n" + dialogRequest);
            }
            if (clientTransactionId == null) {
                throw new NullPointerException("null parameter");
            } else if (dialogRequest.getMethod().equals("ACK") || dialogRequest.getMethod().equals(Request.CANCEL)) {
                throw new SipException("Bad Request Method. " + dialogRequest.getMethod());
            } else if (!this.byeSent || !isTerminatedOnBye() || dialogRequest.getMethod().equals("BYE")) {
                if (dialogRequest.getTopmostVia() == null) {
                    dialogRequest.addHeader(((SIPClientTransaction) clientTransactionId).getOutgoingViaHeader());
                }
                if (!getCallId().getCallId().equalsIgnoreCase(dialogRequest.getCallId().getCallId())) {
                    if (this.sipStack.isLoggingEnabled()) {
                        this.sipStack.getStackLogger().logError("CallID " + getCallId());
                        this.sipStack.getStackLogger().logError("RequestCallID = " + dialogRequest.getCallId().getCallId());
                        this.sipStack.getStackLogger().logError("dialog =  " + this);
                    }
                    throw new SipException("Bad call ID in request");
                }
                ((SIPClientTransaction) clientTransactionId).setDialog(this, this.dialogId);
                addTransaction((SIPTransaction) clientTransactionId);
                ((SIPClientTransaction) clientTransactionId).isMapped = true;
                From from = (From) dialogRequest.getFrom();
                To to = (To) dialogRequest.getTo();
                if (getLocalTag() == null || from.getTag() == null || from.getTag().equals(getLocalTag())) {
                    if (getRemoteTag() != null && to.getTag() != null && !to.getTag().equals(getRemoteTag()) && this.sipStack.isLoggingEnabled()) {
                        this.sipStack.getStackLogger().logWarning("To header tag mismatch expecting " + getRemoteTag());
                    }
                    if (getLocalTag() == null && dialogRequest.getMethod().equals("NOTIFY")) {
                        if (getMethod().equals("SUBSCRIBE")) {
                            setLocalTag(from.getTag());
                        } else {
                            throw new SipException("Trying to send NOTIFY without SUBSCRIBE Dialog!");
                        }
                    }
                    try {
                        if (getLocalTag() != null) {
                            from.setTag(getLocalTag());
                        }
                        if (getRemoteTag() != null) {
                            to.setTag(getRemoteTag());
                        }
                    } catch (ParseException ex) {
                        InternalErrorHandler.handleException(ex);
                    }
                    Hop hop = ((SIPClientTransaction) clientTransactionId).getNextHop();
                    if (this.sipStack.isLoggingEnabled()) {
                        this.sipStack.getStackLogger().logDebug("Using hop = " + hop.getHost() + " : " + hop.getPort());
                    }
                    try {
                        MessageChannel messageChannel = this.sipStack.createRawMessageChannel(getSipProvider().getListeningPoint(hop.getTransport()).getIPAddress(), this.firstTransactionPort, hop);
                        MessageChannel oldChannel = ((SIPClientTransaction) clientTransactionId).getMessageChannel();
                        oldChannel.uncache();
                        if (!this.sipStack.cacheClientConnections) {
                            oldChannel.useCount--;
                            if (this.sipStack.isLoggingEnabled()) {
                                this.sipStack.getStackLogger().logDebug("oldChannel: useCount " + oldChannel.useCount);
                            }
                        }
                        if (messageChannel == null) {
                            if (this.sipStack.isLoggingEnabled()) {
                                this.sipStack.getStackLogger().logDebug("Null message channel using outbound proxy !");
                            }
                            Hop outboundProxy = this.sipStack.getRouter(dialogRequest).getOutboundProxy();
                            if (outboundProxy != null) {
                                messageChannel = this.sipStack.createRawMessageChannel(getSipProvider().getListeningPoint(outboundProxy.getTransport()).getIPAddress(), this.firstTransactionPort, outboundProxy);
                                if (messageChannel != null) {
                                    ((SIPClientTransaction) clientTransactionId).setEncapsulatedChannel(messageChannel);
                                }
                            } else {
                                throw new SipException("No route found! hop=" + hop);
                            }
                        } else {
                            ((SIPClientTransaction) clientTransactionId).setEncapsulatedChannel(messageChannel);
                            if (this.sipStack.isLoggingEnabled()) {
                                this.sipStack.getStackLogger().logDebug("using message channel " + messageChannel);
                            }
                        }
                        if (messageChannel != null) {
                            messageChannel.useCount++;
                        }
                        if (!this.sipStack.cacheClientConnections && oldChannel.useCount <= 0) {
                            oldChannel.close();
                        }
                        try {
                            this.localSequenceNumber++;
                            dialogRequest.getCSeq().setSeqNumber(getLocalSeqNumber());
                        } catch (InvalidArgumentException ex2) {
                            this.sipStack.getStackLogger().logFatalError(ex2.getMessage());
                        }
                        try {
                            ((SIPClientTransaction) clientTransactionId).sendMessage(dialogRequest);
                            if (dialogRequest.getMethod().equals("BYE")) {
                                this.byeSent = true;
                                if (isTerminatedOnBye()) {
                                    setState(DialogState._TERMINATED);
                                }
                            }
                        } catch (IOException ex3) {
                            throw new SipException("error sending message", ex3);
                        }
                    } catch (Exception ex4) {
                        if (this.sipStack.isLoggingEnabled()) {
                            this.sipStack.getStackLogger().logException(ex4);
                        }
                        throw new SipException("Could not create message channel", ex4);
                    }
                } else {
                    throw new SipException("From tag mismatch expecting  " + getLocalTag());
                }
            } else {
                if (this.sipStack.isLoggingEnabled()) {
                    this.sipStack.getStackLogger().logError("BYE already sent for " + this);
                }
                throw new SipException("Cannot send request; BYE already sent");
            }
        } else {
            new Thread(new ReInviteSender(clientTransactionId)).start();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean toRetransmitFinalResponse(int T2) {
        int i = this.retransmissionTicksLeft - 1;
        this.retransmissionTicksLeft = i;
        if (i != 0) {
            return false;
        }
        int i2 = this.prevRetransmissionTicks;
        if (i2 * 2 <= T2) {
            this.retransmissionTicksLeft = i2 * 2;
        } else {
            this.retransmissionTicksLeft = i2;
        }
        this.prevRetransmissionTicks = this.retransmissionTicksLeft;
        return true;
    }

    /* access modifiers changed from: protected */
    public void setRetransmissionTicks() {
        this.retransmissionTicksLeft = 1;
        this.prevRetransmissionTicks = 1;
    }

    public void resendAck() throws SipException {
        if (getLastAckSent() != null) {
            if (getLastAckSent().getHeader("Timestamp") != null && this.sipStack.generateTimeStampHeader) {
                TimeStamp ts = new TimeStamp();
                try {
                    ts.setTimeStamp((float) System.currentTimeMillis());
                    getLastAckSent().setHeader(ts);
                } catch (InvalidArgumentException e) {
                }
            }
            sendAck(getLastAckSent(), false);
        }
    }

    public String getMethod() {
        return this.method;
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: protected */
    public void startTimer(SIPServerTransaction transaction) {
        DialogTimerTask dialogTimerTask = this.timerTask;
        if (dialogTimerTask == null || dialogTimerTask.transaction != transaction) {
            if (this.sipStack.isLoggingEnabled()) {
                StackLogger stackLogger = this.sipStack.getStackLogger();
                stackLogger.logDebug("Starting dialog timer for " + getDialogId());
            }
            this.ackSeen = false;
            acquireTimerTaskSem();
            try {
                if (this.timerTask != null) {
                    this.timerTask.transaction = transaction;
                } else {
                    this.timerTask = new DialogTimerTask(transaction);
                    this.sipStack.getTimer().schedule(this.timerTask, 500, 500);
                }
                releaseTimerTaskSem();
                setRetransmissionTicks();
            } catch (Throwable th) {
                releaseTimerTaskSem();
                throw th;
            }
        } else if (this.sipStack.isLoggingEnabled()) {
            StackLogger stackLogger2 = this.sipStack.getStackLogger();
            stackLogger2.logDebug("Timer already running for " + getDialogId());
        }
    }

    /* access modifiers changed from: protected */
    public void stopTimer() {
        try {
            acquireTimerTaskSem();
            try {
                if (this.timerTask != null) {
                    this.timerTask.cancel();
                    this.timerTask = null;
                }
            } finally {
                releaseTimerTaskSem();
            }
        } catch (Exception e) {
        }
    }

    @Override // javax.sip.Dialog
    public Request createPrack(Response relResponse) throws DialogDoesNotExistException, SipException {
        if (getState() == null || getState().equals(DialogState.TERMINATED)) {
            throw new DialogDoesNotExistException("Dialog not initialized or terminated");
        } else if (((RSeq) relResponse.getHeader("RSeq")) != null) {
            try {
                SIPResponse sipResponse = (SIPResponse) relResponse;
                SIPRequest sipRequest = (SIPRequest) createRequest(Request.PRACK, (SIPResponse) relResponse);
                sipRequest.setToTag(sipResponse.getTo().getTag());
                RAck rack = new RAck();
                rack.setMethod(sipResponse.getCSeq().getMethod());
                rack.setCSequenceNumber((long) ((int) sipResponse.getCSeq().getSeqNumber()));
                rack.setRSequenceNumber(((RSeq) relResponse.getHeader("RSeq")).getSeqNumber());
                sipRequest.setHeader(rack);
                return sipRequest;
            } catch (Exception ex) {
                InternalErrorHandler.handleException(ex);
                return null;
            }
        } else {
            throw new SipException("Missing RSeq Header");
        }
    }

    private void updateRequest(SIPRequest sipRequest) {
        RouteList rl = getRouteList();
        if (rl.size() > 0) {
            sipRequest.setHeader((Header) rl);
        } else {
            sipRequest.removeHeader("Route");
        }
        if (MessageFactoryImpl.getDefaultUserAgentHeader() != null) {
            sipRequest.setHeader(MessageFactoryImpl.getDefaultUserAgentHeader());
        }
    }

    @Override // javax.sip.Dialog
    public Request createAck(long cseqno) throws InvalidArgumentException, SipException {
        SipURI uri4transport;
        Authorization authorization;
        NameValueList originalRequestParameters;
        if (!this.method.equals("INVITE")) {
            throw new SipException("Dialog was not created with an INVITE" + this.method);
        } else if (cseqno <= 0) {
            throw new InvalidArgumentException("bad cseq <= 0 ");
        } else if (cseqno > 4294967295L) {
            throw new InvalidArgumentException("bad cseq > 4294967295");
        } else if (this.remoteTarget != null) {
            if (this.sipStack.isLoggingEnabled()) {
                StackLogger stackLogger = this.sipStack.getStackLogger();
                stackLogger.logDebug("createAck " + this + " cseqno " + cseqno);
            }
            if (this.lastInviteOkReceived < cseqno) {
                if (this.sipStack.isLoggingEnabled()) {
                    StackLogger stackLogger2 = this.sipStack.getStackLogger();
                    stackLogger2.logDebug("WARNING : Attempt to crete ACK without OK " + this);
                    StackLogger stackLogger3 = this.sipStack.getStackLogger();
                    stackLogger3.logDebug("LAST RESPONSE = " + this.lastResponse);
                }
                throw new SipException("Dialog not yet established -- no OK response!");
            }
            try {
                if (this.routeList == null || this.routeList.isEmpty()) {
                    uri4transport = (SipURI) this.remoteTarget.getURI();
                } else {
                    uri4transport = (SipURI) ((Route) this.routeList.getFirst()).getAddress().getURI();
                }
                String transport = uri4transport.getTransportParam();
                if (transport == null) {
                    transport = uri4transport.isSecure() ? ListeningPoint.TLS : ListeningPoint.UDP;
                }
                if (((ListeningPointImpl) this.sipProvider.getListeningPoint(transport)) == null) {
                    if (this.sipStack.isLoggingEnabled()) {
                        StackLogger stackLogger4 = this.sipStack.getStackLogger();
                        stackLogger4.logError("remoteTargetURI " + this.remoteTarget.getURI());
                        StackLogger stackLogger5 = this.sipStack.getStackLogger();
                        stackLogger5.logError("uri4transport = " + uri4transport);
                        StackLogger stackLogger6 = this.sipStack.getStackLogger();
                        stackLogger6.logError("No LP found for transport=" + transport);
                    }
                    throw new SipException("Cannot create ACK - no ListeningPoint for transport towards next hop found:" + transport);
                }
                SIPRequest sipRequest = new SIPRequest();
                sipRequest.setMethod("ACK");
                sipRequest.setRequestURI((SipUri) getRemoteTarget().getURI().clone());
                sipRequest.setCallId(this.callIdHeader);
                sipRequest.setCSeq(new CSeq(cseqno, "ACK"));
                List<Via> vias = new ArrayList<>();
                Via via = this.lastResponse.getTopmostVia();
                via.removeParameters();
                if (!(this.originalRequest == null || this.originalRequest.getTopmostVia() == null || (originalRequestParameters = this.originalRequest.getTopmostVia().getParameters()) == null || originalRequestParameters.size() <= 0)) {
                    via.setParameters((NameValueList) originalRequestParameters.clone());
                }
                via.setBranch(Utils.getInstance().generateBranchId());
                vias.add(via);
                sipRequest.setVia(vias);
                From from = new From();
                from.setAddress(this.localParty);
                from.setTag(this.myTag);
                sipRequest.setFrom(from);
                To to = new To();
                to.setAddress(this.remoteParty);
                if (this.hisTag != null) {
                    to.setTag(this.hisTag);
                }
                sipRequest.setTo(to);
                sipRequest.setMaxForwards(new MaxForwards(70));
                if (!(this.originalRequest == null || (authorization = this.originalRequest.getAuthorization()) == null)) {
                    sipRequest.setHeader(authorization);
                }
                updateRequest(sipRequest);
                return sipRequest;
            } catch (Exception ex) {
                InternalErrorHandler.handleException(ex);
                throw new SipException("unexpected exception ", ex);
            }
        } else {
            throw new SipException("Cannot create ACK - no remote Target!");
        }
    }

    @Override // javax.sip.Dialog, gov.nist.javax.sip.DialogExt
    public SipProviderImpl getSipProvider() {
        return this.sipProvider;
    }

    public void setSipProvider(SipProviderImpl sipProvider2) {
        this.sipProvider = sipProvider2;
    }

    public void setResponseTags(SIPResponse sipResponse) {
        if (getLocalTag() == null && getRemoteTag() == null) {
            String responseFromTag = sipResponse.getFromTag();
            if (responseFromTag != null) {
                if (responseFromTag.equals(getLocalTag())) {
                    sipResponse.setToTag(getRemoteTag());
                } else if (responseFromTag.equals(getRemoteTag())) {
                    sipResponse.setToTag(getLocalTag());
                }
            } else if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logWarning("No from tag in response! Not RFC 3261 compatible.");
            }
        }
    }

    public void setLastResponse(SIPTransaction transaction, SIPResponse sipResponse) {
        SIPRequest sIPRequest;
        RecordRouteList rrList;
        this.callIdHeader = sipResponse.getCallId();
        int statusCode = sipResponse.getStatusCode();
        if (statusCode != 100) {
            this.lastResponse = sipResponse;
            setAssigned();
            if (this.sipStack.isLoggingEnabled()) {
                StackLogger stackLogger = this.sipStack.getStackLogger();
                stackLogger.logDebug("sipDialog: setLastResponse:" + this + " lastResponse = " + this.lastResponse.getFirstLine());
            }
            if (getState() == DialogState.TERMINATED) {
                if (this.sipStack.isLoggingEnabled()) {
                    this.sipStack.getStackLogger().logDebug("sipDialog: setLastResponse -- dialog is terminated - ignoring ");
                }
                if (sipResponse.getCSeq().getMethod().equals("INVITE") && statusCode == 200) {
                    this.lastInviteOkReceived = Math.max(sipResponse.getCSeq().getSeqNumber(), this.lastInviteOkReceived);
                    return;
                }
                return;
            }
            String cseqMethod = sipResponse.getCSeq().getMethod();
            if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logStackTrace();
                StackLogger stackLogger2 = this.sipStack.getStackLogger();
                stackLogger2.logDebug("cseqMethod = " + cseqMethod);
                StackLogger stackLogger3 = this.sipStack.getStackLogger();
                stackLogger3.logDebug("dialogState = " + getState());
                StackLogger stackLogger4 = this.sipStack.getStackLogger();
                stackLogger4.logDebug("method = " + getMethod());
                StackLogger stackLogger5 = this.sipStack.getStackLogger();
                stackLogger5.logDebug("statusCode = " + statusCode);
                StackLogger stackLogger6 = this.sipStack.getStackLogger();
                stackLogger6.logDebug("transaction = " + transaction);
            }
            if (transaction == null || (transaction instanceof ClientTransaction)) {
                SIPTransactionStack sIPTransactionStack = this.sipStack;
                if (SIPTransactionStack.isDialogCreated(cseqMethod)) {
                    if (getState() == null && statusCode / 100 == 1) {
                        setState(EARLY_STATE);
                        if ((sipResponse.getToTag() != null || this.sipStack.rfc2543Supported) && getRemoteTag() == null) {
                            setRemoteTag(sipResponse.getToTag());
                            setDialogId(sipResponse.getDialogId(false));
                            this.sipStack.putDialog(this);
                            addRoute(sipResponse);
                        }
                    } else if (getState() == null || !getState().equals(DialogState.EARLY) || statusCode / 100 != 1) {
                        if (statusCode / 100 == 2) {
                            if (cseqMethod.equals(getMethod()) && ((sipResponse.getToTag() != null || this.sipStack.rfc2543Supported) && getState() != DialogState.CONFIRMED)) {
                                setRemoteTag(sipResponse.getToTag());
                                setDialogId(sipResponse.getDialogId(false));
                                this.sipStack.putDialog(this);
                                addRoute(sipResponse);
                                setState(CONFIRMED_STATE);
                            }
                            if (cseqMethod.equals("INVITE")) {
                                this.lastInviteOkReceived = Math.max(sipResponse.getCSeq().getSeqNumber(), this.lastInviteOkReceived);
                            }
                        } else if (statusCode >= 300 && statusCode <= 699 && (getState() == null || (cseqMethod.equals(getMethod()) && getState().getValue() == EARLY_STATE))) {
                            setState(TERMINATED_STATE);
                        }
                    } else if (cseqMethod.equals(getMethod()) && transaction != null && (sipResponse.getToTag() != null || this.sipStack.rfc2543Supported)) {
                        setRemoteTag(sipResponse.getToTag());
                        setDialogId(sipResponse.getDialogId(false));
                        this.sipStack.putDialog(this);
                        addRoute(sipResponse);
                    }
                    if (getState() != DialogState.CONFIRMED && getState() != DialogState.TERMINATED && (sIPRequest = this.originalRequest) != null && (rrList = sIPRequest.getRecordRouteHeaders()) != null) {
                        ListIterator<RecordRoute> it = rrList.listIterator(rrList.size());
                        while (it.hasPrevious()) {
                            RecordRoute rr = it.previous();
                            Route route = (Route) this.routeList.getFirst();
                            if (route != null && rr.getAddress().equals(route.getAddress())) {
                                this.routeList.removeFirst();
                            } else {
                                return;
                            }
                        }
                    }
                } else if (cseqMethod.equals("NOTIFY") && ((getMethod().equals("SUBSCRIBE") || getMethod().equals(Request.REFER)) && sipResponse.getStatusCode() / 100 == 2 && getState() == null)) {
                    setDialogId(sipResponse.getDialogId(true));
                    this.sipStack.putDialog(this);
                    setState(CONFIRMED_STATE);
                } else if (cseqMethod.equals("BYE") && statusCode / 100 == 2 && isTerminatedOnBye()) {
                    setState(TERMINATED_STATE);
                }
            } else if (!cseqMethod.equals("BYE") || statusCode / 100 != 2 || !isTerminatedOnBye()) {
                boolean doPutDialog = false;
                if (getLocalTag() == null && sipResponse.getTo().getTag() != null) {
                    SIPTransactionStack sIPTransactionStack2 = this.sipStack;
                    if (SIPTransactionStack.isDialogCreated(cseqMethod) && cseqMethod.equals(getMethod())) {
                        setLocalTag(sipResponse.getTo().getTag());
                        doPutDialog = true;
                    }
                }
                if (statusCode / 100 == 2) {
                    if (this.dialogState <= EARLY_STATE && (cseqMethod.equals("INVITE") || cseqMethod.equals("SUBSCRIBE") || cseqMethod.equals(Request.REFER))) {
                        setState(CONFIRMED_STATE);
                    }
                    if (doPutDialog) {
                        setDialogId(sipResponse.getDialogId(true));
                        this.sipStack.putDialog(this);
                    }
                    if (transaction.getState() != TransactionState.TERMINATED && sipResponse.getStatusCode() == 200 && cseqMethod.equals("INVITE") && this.isBackToBackUserAgent && !takeAckSem()) {
                        if (this.sipStack.isLoggingEnabled()) {
                            this.sipStack.getStackLogger().logDebug("Delete dialog -- cannot acquire ackSem");
                        }
                        delete();
                    }
                } else if (statusCode / 100 == 1) {
                    if (doPutDialog) {
                        setState(EARLY_STATE);
                        setDialogId(sipResponse.getDialogId(true));
                        this.sipStack.putDialog(this);
                    }
                } else if (statusCode != 489 || (!cseqMethod.equals("NOTIFY") && !cseqMethod.equals("SUBSCRIBE"))) {
                    if (!isReInvite() && getState() != DialogState.CONFIRMED) {
                        setState(TERMINATED_STATE);
                    }
                } else if (this.sipStack.isLoggingEnabled()) {
                    this.sipStack.getStackLogger().logDebug("RFC 3265 : Not setting dialog to TERMINATED for 489");
                }
            } else {
                setState(TERMINATED_STATE);
            }
        } else if (this.sipStack.isLoggingEnabled()) {
            this.sipStack.getStackLogger().logWarning("Invalid status code - 100 in setLastResponse - ignoring");
        }
    }

    public void startRetransmitTimer(SIPServerTransaction sipServerTx, Response response) {
        if (sipServerTx.getRequest().getMethod().equals("INVITE") && response.getStatusCode() / 100 == 2) {
            startTimer(sipServerTx);
        }
    }

    public SIPResponse getLastResponse() {
        return this.lastResponse;
    }

    private void doTargetRefresh(SIPMessage sipMessage) {
        ContactList contactList = sipMessage.getContactHeaders();
        if (contactList != null) {
            setRemoteTarget((Contact) contactList.getFirst());
        }
    }

    private static final boolean optionPresent(ListIterator l, String option) {
        while (l.hasNext()) {
            OptionTag opt = (OptionTag) l.next();
            if (opt != null && option.equalsIgnoreCase(opt.getOptionTag())) {
                return true;
            }
        }
        return false;
    }

    @Override // javax.sip.Dialog
    public Response createReliableProvisionalResponse(int statusCode) throws InvalidArgumentException, SipException {
        ListIterator<SIPHeader> list;
        if (!this.firstTransactionIsServerTransaction) {
            throw new SipException("Not a Server Dialog!");
        } else if (statusCode <= 100 || statusCode > 199) {
            throw new InvalidArgumentException("Bad status code ");
        } else {
            SIPRequest request = this.originalRequest;
            if (request.getMethod().equals("INVITE")) {
                ListIterator<SIPHeader> list2 = request.getHeaders("Supported");
                if ((list2 == null || !optionPresent(list2, "100rel")) && ((list = request.getHeaders("Require")) == null || !optionPresent(list, "100rel"))) {
                    throw new SipException("No Supported/Require 100rel header in the request");
                }
                SIPResponse response = request.createResponse(statusCode);
                Require require = new Require();
                try {
                    require.setOptionTag("100rel");
                } catch (Exception ex) {
                    InternalErrorHandler.handleException(ex);
                }
                response.addHeader(require);
                new RSeq().setSeqNumber(1);
                RecordRouteList rrl = request.getRecordRouteHeaders();
                if (rrl != null) {
                    response.setHeader((Header) ((RecordRouteList) rrl.clone()));
                }
                return response;
            }
            throw new SipException("Bad method");
        }
    }

    public boolean handlePrack(SIPRequest prackRequest) {
        if (!isServer()) {
            if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logDebug("Dropping Prack -- not a server Dialog");
            }
            return false;
        }
        SIPServerTransaction sipServerTransaction = (SIPServerTransaction) getFirstTransaction();
        SIPResponse sipResponse = sipServerTransaction.getReliableProvisionalResponse();
        if (sipResponse == null) {
            if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logDebug("Dropping Prack -- ReliableResponse not found");
            }
            return false;
        }
        RAck rack = (RAck) prackRequest.getHeader("RAck");
        if (rack == null) {
            if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logDebug("Dropping Prack -- rack header not found");
            }
            return false;
        }
        CSeq cseq = (CSeq) sipResponse.getCSeq();
        if (!rack.getMethod().equals(cseq.getMethod())) {
            if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logDebug("Dropping Prack -- CSeq Header does not match PRACK");
            }
            return false;
        } else if (rack.getCSeqNumberLong() != cseq.getSeqNumber()) {
            if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logDebug("Dropping Prack -- CSeq Header does not match PRACK");
            }
            return false;
        } else if (rack.getRSequenceNumber() == ((RSeq) sipResponse.getHeader("RSeq")).getSeqNumber()) {
            return sipServerTransaction.prackRecieved();
        } else {
            if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logDebug("Dropping Prack -- RSeq Header does not match PRACK");
            }
            return false;
        }
    }

    @Override // javax.sip.Dialog
    public void sendReliableProvisionalResponse(Response relResponse) throws SipException {
        if (isServer()) {
            SIPResponse sipResponse = (SIPResponse) relResponse;
            if (relResponse.getStatusCode() == 100) {
                throw new SipException("Cannot send 100 as a reliable provisional response");
            } else if (relResponse.getStatusCode() / 100 > 2) {
                throw new SipException("Response code is not a 1xx response - should be in the range 101 to 199 ");
            } else if (sipResponse.getToTag() != null) {
                ListIterator requireList = relResponse.getHeaders("Require");
                boolean found = false;
                found = false;
                if (requireList != null) {
                    while (requireList.hasNext() && !found) {
                        if (((RequireHeader) requireList.next()).getOptionTag().equalsIgnoreCase("100rel")) {
                            found = true;
                        }
                    }
                }
                if (!found) {
                    relResponse.addHeader(new Require("100rel"));
                    if (this.sipStack.isLoggingEnabled()) {
                        this.sipStack.getStackLogger().logDebug("Require header with optionTag 100rel is needed -- adding one");
                    }
                }
                SIPServerTransaction serverTransaction = (SIPServerTransaction) getFirstTransaction();
                setLastResponse(serverTransaction, sipResponse);
                setDialogId(sipResponse.getDialogId(true));
                serverTransaction.sendReliableProvisionalResponse(relResponse);
                startRetransmitTimer(serverTransaction, relResponse);
            } else {
                throw new SipException("Badly formatted response -- To tag mandatory for Reliable Provisional Response");
            }
        } else {
            throw new SipException("Not a Server Dialog");
        }
    }

    @Override // javax.sip.Dialog
    public void terminateOnBye(boolean terminateFlag) throws SipException {
        this.terminateOnBye = terminateFlag;
    }

    public void setAssigned() {
        this.isAssigned = true;
    }

    public boolean isAssigned() {
        return this.isAssigned;
    }

    public Contact getMyContactHeader() {
        return this.contactHeader;
    }

    public boolean handleAck(SIPServerTransaction ackTransaction) {
        SIPRequest sipRequest = ackTransaction.getOriginalRequest();
        SIPResponse sipResponse = null;
        if (isAckSeen() && getRemoteSeqNumber() == sipRequest.getCSeq().getSeqNumber()) {
            if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logDebug("ACK already seen by dialog -- dropping Ack retransmission");
            }
            acquireTimerTaskSem();
            try {
                if (this.timerTask != null) {
                    this.timerTask.cancel();
                    this.timerTask = null;
                }
                return false;
            } finally {
                releaseTimerTaskSem();
            }
        } else if (getState() == DialogState.TERMINATED) {
            if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logDebug("Dialog is terminated -- dropping ACK");
            }
            return false;
        } else {
            SIPServerTransaction tr = getInviteTransaction();
            if (tr != null) {
                sipResponse = tr.getLastResponse();
            }
            if (tr == null || sipResponse == null || sipResponse.getStatusCode() / 100 != 2 || !sipResponse.getCSeq().getMethod().equals("INVITE") || sipResponse.getCSeq().getSeqNumber() != sipRequest.getCSeq().getSeqNumber()) {
                if (this.sipStack.isLoggingEnabled()) {
                    this.sipStack.getStackLogger().logDebug(" INVITE transaction not found  -- Discarding ACK");
                }
                return false;
            }
            ackTransaction.setDialog(this, sipResponse.getDialogId(false));
            ackReceived(sipRequest);
            if (!this.sipStack.isLoggingEnabled()) {
                return true;
            }
            this.sipStack.getStackLogger().logDebug("ACK for 2XX response --- sending to TU ");
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public void setEarlyDialogId(String earlyDialogId2) {
        this.earlyDialogId = earlyDialogId2;
    }

    /* access modifiers changed from: package-private */
    public String getEarlyDialogId() {
        return this.earlyDialogId;
    }

    /* access modifiers changed from: package-private */
    public void releaseAckSem() {
        if (this.isBackToBackUserAgent) {
            if (this.sipStack.isLoggingEnabled()) {
                StackLogger stackLogger = this.sipStack.getStackLogger();
                stackLogger.logDebug("releaseAckSem]" + this);
            }
            this.ackSem.release();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean takeAckSem() {
        if (this.sipStack.isLoggingEnabled()) {
            StackLogger stackLogger = this.sipStack.getStackLogger();
            stackLogger.logDebug("[takeAckSem " + this);
        }
        try {
            if (!this.ackSem.tryAcquire(2, TimeUnit.SECONDS)) {
                if (this.sipStack.isLoggingEnabled()) {
                    this.sipStack.getStackLogger().logError("Cannot aquire ACK semaphore");
                }
                if (this.sipStack.isLoggingEnabled()) {
                    StackLogger stackLogger2 = this.sipStack.getStackLogger();
                    stackLogger2.logDebug("Semaphore previously acquired at " + this.stackTrace);
                    this.sipStack.getStackLogger().logStackTrace();
                }
                return false;
            } else if (!this.sipStack.isLoggingEnabled()) {
                return true;
            } else {
                recordStackTrace();
                return true;
            }
        } catch (InterruptedException e) {
            this.sipStack.getStackLogger().logError("Cannot aquire ACK semaphore");
            return false;
        }
    }

    private void setLastAckReceived(SIPRequest lastAckReceived2) {
        this.lastAckReceived = lastAckReceived2;
    }

    /* access modifiers changed from: protected */
    public SIPRequest getLastAckReceived() {
        return this.lastAckReceived;
    }

    private void setLastAckSent(SIPRequest lastAckSent2) {
        this.lastAckSent = lastAckSent2;
    }

    public boolean isAtleastOneAckSent() {
        return this.isAcknowledged;
    }

    public boolean isBackToBackUserAgent() {
        return this.isBackToBackUserAgent;
    }

    public synchronized void doDeferredDeleteIfNoAckSent(long seqno) {
        if (this.sipStack.getTimer() == null) {
            setState(TERMINATED_STATE);
        } else if (this.dialogDeleteIfNoAckSentTask == null) {
            this.dialogDeleteIfNoAckSentTask = new DialogDeleteIfNoAckSentTask(seqno);
            this.sipStack.getTimer().schedule(this.dialogDeleteIfNoAckSentTask, 32000);
        }
    }

    @Override // javax.sip.Dialog, gov.nist.javax.sip.DialogExt
    public void setBackToBackUserAgent() {
        this.isBackToBackUserAgent = true;
    }

    /* access modifiers changed from: package-private */
    public EventHeader getEventHeader() {
        return this.eventHeader;
    }

    /* access modifiers changed from: package-private */
    public void setEventHeader(EventHeader eventHeader2) {
        this.eventHeader = eventHeader2;
    }

    /* access modifiers changed from: package-private */
    public void setServerTransactionFlag(boolean serverTransactionFlag2) {
        this.serverTransactionFlag = serverTransactionFlag2;
    }

    /* access modifiers changed from: package-private */
    public void setReInviteFlag(boolean reInviteFlag2) {
        this.reInviteFlag = reInviteFlag2;
    }

    public boolean isSequnceNumberValidation() {
        return this.sequenceNumberValidation;
    }

    @Override // gov.nist.javax.sip.DialogExt
    public void disableSequenceNumberValidation() {
        this.sequenceNumberValidation = false;
    }

    public void acquireTimerTaskSem() {
        boolean acquired;
        try {
            acquired = this.timerTaskLock.tryAcquire(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            acquired = false;
        }
        if (!acquired) {
            throw new IllegalStateException("Impossible to acquire the dialog timer task lock");
        }
    }

    public void releaseTimerTaskSem() {
        this.timerTaskLock.release();
    }
}
