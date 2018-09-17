package gov.nist.javax.sip.stack;

import gov.nist.core.InternalErrorHandler;
import gov.nist.core.NameValueList;
import gov.nist.core.Separators;
import gov.nist.javax.sip.DialogExt;
import gov.nist.javax.sip.ListeningPointImpl;
import gov.nist.javax.sip.SIPConstants;
import gov.nist.javax.sip.SipListenerExt;
import gov.nist.javax.sip.SipProviderImpl;
import gov.nist.javax.sip.Utils;
import gov.nist.javax.sip.address.AddressImpl;
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
import gov.nist.javax.sip.parser.TokenNames;
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
import javax.sip.header.RAckHeader;
import javax.sip.header.RSeqHeader;
import javax.sip.header.ReasonHeader;
import javax.sip.header.RequireHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.SupportedHeader;
import javax.sip.header.TimeStampHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;
import org.ccil.cowan.tagsoup.HTMLModels;

public class SIPDialog implements Dialog, DialogExt {
    public static final int CONFIRMED_STATE = 0;
    private static final int DIALOG_LINGER_TIME = 8;
    public static final int EARLY_STATE = 0;
    public static final int NULL_STATE = -1;
    public static final int TERMINATED_STATE = 0;
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

    class DialogDeleteIfNoAckSentTask extends SIPStackTimerTask implements Serializable {
        private long seqno;

        public DialogDeleteIfNoAckSentTask(long seqno) {
            this.seqno = seqno;
        }

        protected void runTask() {
            if (SIPDialog.this.highestSequenceNumberAcknowledged < this.seqno) {
                SIPDialog.this.dialogDeleteIfNoAckSentTask = null;
                if (SIPDialog.this.isBackToBackUserAgent) {
                    if (SIPDialog.this.sipStack.isLoggingEnabled()) {
                        SIPDialog.this.sipStack.getStackLogger().logError("ACK Was not sent. Sending BYE");
                    }
                    if (SIPDialog.this.sipProvider.getSipListener() instanceof SipListenerExt) {
                        SIPDialog.this.raiseErrorEvent(2);
                    } else {
                        try {
                            Request byeRequest = SIPDialog.this.createRequest(TokenNames.BYE);
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
                } else {
                    if (SIPDialog.this.sipStack.isLoggingEnabled()) {
                        SIPDialog.this.sipStack.getStackLogger().logError("ACK Was not sent. killing dialog");
                    }
                    if (SIPDialog.this.sipProvider.getSipListener() instanceof SipListenerExt) {
                        SIPDialog.this.raiseErrorEvent(2);
                    } else {
                        SIPDialog.this.delete();
                    }
                }
            }
        }
    }

    class DialogDeleteTask extends SIPStackTimerTask implements Serializable {
        DialogDeleteTask() {
        }

        protected void runTask() {
            SIPDialog.this.delete();
        }
    }

    class DialogTimerTask extends SIPStackTimerTask implements Serializable {
        int nRetransmissions;
        SIPServerTransaction transaction;

        protected void runTask() {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x00ef in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:42)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:58)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r10 = this;
            r7 = 1;
            r9 = 0;
            r0 = gov.nist.javax.sip.stack.SIPDialog.this;
            r5 = gov.nist.javax.sip.stack.SIPDialog.this;
            r5 = r5.sipStack;
            r5 = r5.isLoggingEnabled();
            if (r5 == 0) goto L_0x0020;
        L_0x0010:
            r5 = gov.nist.javax.sip.stack.SIPDialog.this;
            r5 = r5.sipStack;
            r5 = r5.getStackLogger();
            r6 = "Running dialog timer";
            r5.logDebug(r6);
        L_0x0020:
            r5 = r10.nRetransmissions;
            r5 = r5 + 1;
            r10.nRetransmissions = r5;
            r4 = r10.transaction;
            r5 = r10.nRetransmissions;
            r6 = 64;
            if (r5 <= r6) goto L_0x0072;
        L_0x002e:
            r5 = gov.nist.javax.sip.stack.SIPDialog.this;
            r5 = r5.sipProvider;
            r5 = r5.getSipListener();
            if (r5 == 0) goto L_0x006e;
        L_0x003a:
            r5 = gov.nist.javax.sip.stack.SIPDialog.this;
            r5 = r5.sipProvider;
            r5 = r5.getSipListener();
            r5 = r5 instanceof gov.nist.javax.sip.SipListenerExt;
            if (r5 == 0) goto L_0x006e;
        L_0x0048:
            r5 = gov.nist.javax.sip.stack.SIPDialog.this;
            r5.raiseErrorEvent(r7);
        L_0x004d:
            if (r4 == 0) goto L_0x005a;
        L_0x004f:
            r5 = r4.getState();
            r6 = javax.sip.TransactionState.TERMINATED;
            if (r5 == r6) goto L_0x005a;
        L_0x0057:
            r4.raiseErrorEvent(r7);
        L_0x005a:
            r5 = r0.isAckSeen();
            if (r5 != 0) goto L_0x0068;
        L_0x0060:
            r5 = r0.dialogState;
            r6 = gov.nist.javax.sip.stack.SIPDialog.TERMINATED_STATE;
            if (r5 != r6) goto L_0x006d;
        L_0x0068:
            r10.transaction = r9;
            r10.cancel();
        L_0x006d:
            return;
        L_0x006e:
            r0.delete();
            goto L_0x004d;
        L_0x0072:
            r5 = r0.ackSeen;
            if (r5 != 0) goto L_0x005a;
        L_0x0076:
            if (r4 == 0) goto L_0x005a;
        L_0x0078:
            r2 = r4.getLastResponse();
            r5 = r2.getStatusCode();
            r6 = 200; // 0xc8 float:2.8E-43 double:9.9E-322;
            if (r5 != r6) goto L_0x005a;
        L_0x0084:
            r5 = r4.T2;	 Catch:{ IOException -> 0x00b8, all -> 0x00f4 }
            r5 = r0.toRetransmitFinalResponse(r5);	 Catch:{ IOException -> 0x00b8, all -> 0x00f4 }
            if (r5 == 0) goto L_0x008f;	 Catch:{ IOException -> 0x00b8, all -> 0x00f4 }
        L_0x008c:
            r4.sendMessage(r2);	 Catch:{ IOException -> 0x00b8, all -> 0x00f4 }
        L_0x008f:
            r3 = r0.sipStack;
            r5 = r3.isLoggingEnabled();
            if (r5 == 0) goto L_0x00b4;
        L_0x0099:
            r5 = r3.getStackLogger();
            r6 = new java.lang.StringBuilder;
            r6.<init>();
            r7 = "resend 200 response from ";
            r6 = r6.append(r7);
            r6 = r6.append(r0);
            r6 = r6.toString();
            r5.logDebug(r6);
        L_0x00b4:
            r4.fireTimer();
            goto L_0x005a;
        L_0x00b8:
            r1 = move-exception;
            r5 = gov.nist.javax.sip.stack.SIPDialog.this;	 Catch:{ IOException -> 0x00b8, all -> 0x00f4 }
            r6 = r4.getPeerAddress();	 Catch:{ IOException -> 0x00b8, all -> 0x00f4 }
            r7 = r4.getPeerPort();	 Catch:{ IOException -> 0x00b8, all -> 0x00f4 }
            r8 = r4.getPeerProtocol();	 Catch:{ IOException -> 0x00b8, all -> 0x00f4 }
            r5.raiseIOException(r6, r7, r8);	 Catch:{ IOException -> 0x00b8, all -> 0x00f4 }
            r3 = r0.sipStack;
            r5 = r3.isLoggingEnabled();
            if (r5 == 0) goto L_0x00ef;
        L_0x00d4:
            r5 = r3.getStackLogger();
            r6 = new java.lang.StringBuilder;
            r6.<init>();
            r7 = "resend 200 response from ";
            r6 = r6.append(r7);
            r6 = r6.append(r0);
            r6 = r6.toString();
            r5.logDebug(r6);
        L_0x00ef:
            r4.fireTimer();
            goto L_0x005a;
        L_0x00f4:
            r5 = move-exception;
            r3 = r0.sipStack;
            r6 = r3.isLoggingEnabled();
            if (r6 == 0) goto L_0x011a;
        L_0x00ff:
            r6 = r3.getStackLogger();
            r7 = new java.lang.StringBuilder;
            r7.<init>();
            r8 = "resend 200 response from ";
            r7 = r7.append(r8);
            r7 = r7.append(r0);
            r7 = r7.toString();
            r6.logDebug(r7);
        L_0x011a:
            r4.fireTimer();
            throw r5;
            */
            throw new UnsupportedOperationException("Method not decompiled: gov.nist.javax.sip.stack.SIPDialog.DialogTimerTask.runTask():void");
        }

        public DialogTimerTask(SIPServerTransaction transaction) {
            this.transaction = transaction;
            this.nRetransmissions = SIPDialog.TERMINATED_STATE;
        }
    }

    class LingerTimer extends SIPStackTimerTask implements Serializable {
        protected void runTask() {
            SIPDialog dialog = SIPDialog.this;
            if (SIPDialog.this.eventListeners != null) {
                SIPDialog.this.eventListeners.clear();
            }
            SIPDialog.this.timerTaskLock = null;
            SIPDialog.this.sipStack.removeDialog(dialog);
        }
    }

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

        public ReInviteSender(ClientTransaction ctx) {
            this.ctx = ctx;
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
                        Request byeRequest = SIPDialog.this.createRequest(TokenNames.BYE);
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
            }
            this.ctx = null;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: gov.nist.javax.sip.stack.SIPDialog.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: gov.nist.javax.sip.stack.SIPDialog.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: gov.nist.javax.sip.stack.SIPDialog.<clinit>():void");
    }

    private SIPDialog(SipProviderImpl provider) {
        this.auditTag = 0;
        this.ackSem = new Semaphore(1);
        this.reInviteWaitTime = 100;
        this.highestSequenceNumberAcknowledged = -1;
        this.sequenceNumberValidation = true;
        this.timerTaskLock = new Semaphore(1);
        this.firstTransactionPort = SIPConstants.DEFAULT_PORT;
        this.terminateOnBye = true;
        this.routeList = new RouteList();
        this.dialogState = NULL_STATE;
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
        if (transaction == null) {
            throw new NullPointerException("Null tx");
        }
        this.sipStack = transaction.sipStack;
        this.sipProvider = transaction.getSipProvider();
        if (this.sipProvider == null) {
            throw new NullPointerException("Null Provider!");
        }
        addTransaction(transaction);
        if (this.sipStack.isLoggingEnabled()) {
            this.sipStack.getStackLogger().logDebug("Creating a dialog : " + this);
            this.sipStack.getStackLogger().logDebug("provider port = " + this.sipProvider.getListeningPoint().getPort());
            this.sipStack.getStackLogger().logStackTrace();
        }
        this.isBackToBackUserAgent = this.sipStack.isBackToBackUserAgent;
        addEventListener(this.sipStack);
    }

    public SIPDialog(SIPClientTransaction transaction, SIPResponse sipResponse) {
        this((SIPTransaction) transaction);
        if (sipResponse == null) {
            throw new NullPointerException("Null SipResponse");
        }
        setLastResponse(transaction, sipResponse);
        this.isBackToBackUserAgent = this.sipStack.isBackToBackUserAgent;
    }

    public SIPDialog(SipProviderImpl sipProvider, SIPResponse sipResponse) {
        this(sipProvider);
        this.sipStack = (SIPTransactionStack) sipProvider.getSipStack();
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
            this.sipStack.getStackLogger().logDebug("Creating a dialog : " + this);
            this.sipStack.getStackLogger().logStackTrace();
        }
        this.isBackToBackUserAgent = this.sipStack.isBackToBackUserAgent;
        addEventListener(this.sipStack);
    }

    private void printRouteList() {
        if (this.sipStack.isLoggingEnabled()) {
            this.sipStack.getStackLogger().logDebug("this : " + this);
            this.sipStack.getStackLogger().logDebug("printRouteList : " + this.routeList.encode());
        }
    }

    private boolean isClientDialog() {
        return ((SIPTransaction) getFirstTransaction()) instanceof SIPClientTransaction;
    }

    private void raiseIOException(String host, int port, String protocol) {
        this.sipProvider.handleEvent(new IOExceptionEvent(this, host, port, protocol), null);
        setState(TERMINATED_STATE);
    }

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
        if (isServer()) {
            this.remoteParty = sipMessage.getFrom().getAddress();
        } else {
            this.remoteParty = sipMessage.getTo().getAddress();
        }
        if (this.sipStack.isLoggingEnabled()) {
            this.sipStack.getStackLogger().logDebug("settingRemoteParty " + this.remoteParty);
        }
    }

    private void addRoute(RecordRouteList recordRouteList) {
        Iterator it;
        try {
            ListIterator li;
            RecordRoute rr;
            Route route;
            if (isClientDialog()) {
                this.routeList = new RouteList();
                li = recordRouteList.listIterator(recordRouteList.size());
                while (li.hasPrevious()) {
                    rr = (RecordRoute) li.previous();
                    if (true) {
                        route = new Route();
                        route.setAddress((AddressImpl) ((AddressImpl) rr.getAddress()).clone());
                        route.setParameters((NameValueList) rr.getParameters().clone());
                        this.routeList.add((SIPHeader) route);
                    }
                }
            } else {
                this.routeList = new RouteList();
                li = recordRouteList.listIterator();
                while (li.hasNext()) {
                    rr = (RecordRoute) li.next();
                    if (true) {
                        route = new Route();
                        route.setAddress((AddressImpl) ((AddressImpl) rr.getAddress()).clone());
                        route.setParameters((NameValueList) rr.getParameters().clone());
                        this.routeList.add((SIPHeader) route);
                    }
                }
            }
            if (this.sipStack.getStackLogger().isLoggingEnabled()) {
                it = this.routeList.iterator();
                while (it.hasNext()) {
                    if (!((SipURI) ((Route) it.next()).getAddress().getURI()).hasLrParam() && this.sipStack.isLoggingEnabled()) {
                        this.sipStack.getStackLogger().logWarning("NON LR route in Route set detected for dialog : " + this);
                        this.sipStack.getStackLogger().logStackTrace();
                    }
                }
            }
        } catch (Throwable th) {
            Throwable th2 = th;
            if (this.sipStack.getStackLogger().isLoggingEnabled()) {
                it = this.routeList.iterator();
                while (it.hasNext()) {
                    if (!((SipURI) ((Route) it.next()).getAddress().getURI()).hasLrParam() && this.sipStack.isLoggingEnabled()) {
                        this.sipStack.getStackLogger().logWarning("NON LR route in Route set detected for dialog : " + this);
                        this.sipStack.getStackLogger().logStackTrace();
                    }
                }
            }
        }
    }

    void setRemoteTarget(ContactHeader contact) {
        this.remoteTarget = contact.getAddress();
        if (this.sipStack.isLoggingEnabled()) {
            this.sipStack.getStackLogger().logDebug("Dialog.setRemoteTarget: " + this.remoteTarget);
            this.sipStack.getStackLogger().logStackTrace();
        }
    }

    private synchronized void addRoute(SIPResponse sipResponse) {
        try {
            if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logDebug("setContact: dialogState: " + this + "state = " + getState());
            }
            if (sipResponse.getStatusCode() == 100) {
                if (this.sipStack.isLoggingEnabled()) {
                    this.sipStack.getStackLogger().logStackTrace();
                }
                return;
            } else if (this.dialogState == TERMINATED_STATE) {
                if (this.sipStack.isLoggingEnabled()) {
                    this.sipStack.getStackLogger().logStackTrace();
                }
                return;
            } else if (this.dialogState == CONFIRMED_STATE) {
                if (sipResponse.getStatusCode() / 100 == 2 && !isServer()) {
                    contactList = sipResponse.getContactHeaders();
                    if (contactList != null && SIPRequest.isTargetRefresh(sipResponse.getCSeq().getMethod())) {
                        setRemoteTarget((ContactHeader) contactList.getFirst());
                    }
                }
                if (this.sipStack.isLoggingEnabled()) {
                    this.sipStack.getStackLogger().logStackTrace();
                }
                return;
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
                    contactList = sipResponse.getContactHeaders();
                    if (contactList != null) {
                        setRemoteTarget((ContactHeader) contactList.getFirst());
                    }
                }
                if (this.sipStack.isLoggingEnabled()) {
                    this.sipStack.getStackLogger().logStackTrace();
                }
                return;
            }
        } catch (Throwable th) {
            if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logStackTrace();
            }
        }
    }

    private synchronized RouteList getRouteList() {
        RouteList routeList;
        if (this.sipStack.isLoggingEnabled()) {
            this.sipStack.getStackLogger().logDebug("getRouteList " + this);
        }
        routeList = new RouteList();
        routeList = new RouteList();
        if (this.routeList != null) {
            ListIterator li = this.routeList.listIterator();
            while (li.hasNext()) {
                routeList.add((SIPHeader) (Route) ((Route) li.next()).clone());
            }
        }
        if (this.sipStack.isLoggingEnabled()) {
            this.sipStack.getStackLogger().logDebug("----- ");
            this.sipStack.getStackLogger().logDebug("getRouteList for " + this);
            if (routeList != null) {
                this.sipStack.getStackLogger().logDebug("RouteList = " + routeList.encode());
            }
            if (this.routeList != null) {
                this.sipStack.getStackLogger().logDebug("myRouteList = " + this.routeList.encode());
            }
            this.sipStack.getStackLogger().logDebug("----- ");
        }
        return routeList;
    }

    void setRouteList(RouteList routeList) {
        this.routeList = routeList;
    }

    private void sendAck(Request request, boolean throwIOExceptionAsSipException) throws SipException {
        SIPRequest ackRequest = (SIPRequest) request;
        if (this.sipStack.isLoggingEnabled()) {
            this.sipStack.getStackLogger().logDebug("sendAck" + this);
        }
        if (!ackRequest.getMethod().equals(TokenNames.ACK)) {
            throw new SipException("Bad request method -- should be ACK");
        } else if (getState() == null || getState().getValue() == EARLY_STATE) {
            if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logError("Bad Dialog State for " + this + " dialogID = " + getDialogId());
            }
            throw new SipException("Bad dialog state " + getState());
        } else if (getCallId().getCallId().equals(((SIPRequest) request).getCallId().getCallId())) {
            try {
                if (this.sipStack.isLoggingEnabled()) {
                    this.sipStack.getStackLogger().logDebug("setting from tag For outgoing ACK= " + getLocalTag());
                    this.sipStack.getStackLogger().logDebug("setting To tag for outgoing ACK = " + getRemoteTag());
                    this.sipStack.getStackLogger().logDebug("ack = " + ackRequest);
                }
                if (getLocalTag() != null) {
                    ackRequest.getFrom().setTag(getLocalTag());
                }
                if (getRemoteTag() != null) {
                    ackRequest.getTo().setTag(getRemoteTag());
                }
                Hop hop = this.sipStack.getNextHop(ackRequest);
                if (hop == null) {
                    throw new SipException("No route!");
                }
                try {
                    if (this.sipStack.isLoggingEnabled()) {
                        this.sipStack.getStackLogger().logDebug("hop = " + hop);
                    }
                    ListeningPointImpl lp = (ListeningPointImpl) this.sipProvider.getListeningPoint(hop.getTransport());
                    if (lp == null) {
                        throw new SipException("No listening point for this provider registered at " + hop);
                    }
                    MessageChannel messageChannel = lp.getMessageProcessor().createMessageChannel(InetAddress.getByName(hop.getHost()), hop.getPort());
                    boolean releaseAckSem = false;
                    if (!isAckSent(((SIPRequest) request).getCSeq().getSeqNumber())) {
                        releaseAckSem = true;
                    }
                    setLastAckSent(ackRequest);
                    messageChannel.sendMessage(ackRequest);
                    this.isAcknowledged = true;
                    this.highestSequenceNumberAcknowledged = Math.max(this.highestSequenceNumberAcknowledged, ackRequest.getCSeq().getSeqNumber());
                    if (releaseAckSem && this.isBackToBackUserAgent) {
                        releaseAckSem();
                        if (this.dialogDeleteTask != null) {
                            this.dialogDeleteTask.cancel();
                            this.dialogDeleteTask = null;
                        }
                        this.ackSeen = true;
                    }
                    if (this.sipStack.isLoggingEnabled()) {
                        this.sipStack.getStackLogger().logDebug("Not releasing ack sem for " + this + " isAckSent " + releaseAckSem);
                    }
                    if (this.dialogDeleteTask != null) {
                        this.dialogDeleteTask.cancel();
                        this.dialogDeleteTask = null;
                    }
                    this.ackSeen = true;
                } catch (IOException ex) {
                    if (throwIOExceptionAsSipException) {
                        throw new SipException("Could not send ack", ex);
                    }
                    raiseIOException(hop.getHost(), hop.getPort(), hop.getTransport());
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
            } catch (ParseException ex4) {
                throw new SipException(ex4.getMessage());
            }
        } else {
            if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logError("CallID " + getCallId());
                this.sipStack.getStackLogger().logError("RequestCallID = " + ackRequest.getCallId().getCallId());
                this.sipStack.getStackLogger().logError("dialog =  " + this);
            }
            throw new SipException("Bad call ID in request");
        }
    }

    void setStack(SIPTransactionStack sipStack) {
        this.sipStack = sipStack;
    }

    SIPTransactionStack getStack() {
        return this.sipStack;
    }

    boolean isTerminatedOnBye() {
        return this.terminateOnBye;
    }

    void ackReceived(SIPRequest sipRequest) {
        if (!this.ackSeen) {
            SIPServerTransaction tr = getInviteTransaction();
            if (tr != null && tr.getCSeq() == sipRequest.getCSeq().getSeqNumber()) {
                acquireTimerTaskSem();
                try {
                    if (this.timerTask != null) {
                        this.timerTask.cancel();
                        this.timerTask = null;
                    }
                    releaseTimerTaskSem();
                    this.ackSeen = true;
                    if (this.dialogDeleteTask != null) {
                        this.dialogDeleteTask.cancel();
                        this.dialogDeleteTask = null;
                    }
                    setLastAckReceived(sipRequest);
                    if (this.sipStack.isLoggingEnabled()) {
                        this.sipStack.getStackLogger().logDebug("ackReceived for " + tr.getMethod());
                        this.ackLine = this.sipStack.getStackLogger().getLineCount();
                        printDebugInfo();
                    }
                    if (this.isBackToBackUserAgent) {
                        releaseAckSem();
                    }
                    setState(CONFIRMED_STATE);
                } catch (Throwable th) {
                    releaseTimerTaskSem();
                }
            }
        }
    }

    synchronized boolean testAndSetIsDialogTerminatedEventDelivered() {
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

    public void setApplicationData(Object applicationData) {
        this.applicationData = applicationData;
    }

    public Object getApplicationData() {
        return this.applicationData;
    }

    public synchronized void requestConsumed() {
        this.nextSeqno = Long.valueOf(getRemoteSeqNumber() + 1);
        if (this.sipStack.isLoggingEnabled()) {
            this.sipStack.getStackLogger().logDebug("Request Consumed -- next consumable Request Seqno = " + this.nextSeqno);
        }
    }

    public synchronized boolean isRequestConsumable(SIPRequest dialogRequest) {
        boolean z = true;
        synchronized (this) {
            if (dialogRequest.getMethod().equals(TokenNames.ACK)) {
                throw new RuntimeException("Illegal method");
            } else if (isSequnceNumberValidation()) {
                if (this.remoteSequenceNumber >= dialogRequest.getCSeq().getSeqNumber()) {
                    z = false;
                }
                return z;
            } else {
                return true;
            }
        }
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
            this.sipStack.getStackLogger().logDebug("Setting dialog state for " + this + "newState = " + state);
            this.sipStack.getStackLogger().logStackTrace();
            if (!(state == NULL_STATE || state == this.dialogState || !this.sipStack.isLoggingEnabled())) {
                this.sipStack.getStackLogger().logDebug(this + "  old dialog state is " + getState());
                this.sipStack.getStackLogger().logDebug(this + "  New dialog state is " + DialogState.getObject(state));
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
            this.sipStack.getStackLogger().logDebug("isServer = " + isServer());
            this.sipStack.getStackLogger().logDebug("localTag = " + getLocalTag());
            this.sipStack.getStackLogger().logDebug("remoteTag = " + getRemoteTag());
            this.sipStack.getStackLogger().logDebug("localSequenceNumer = " + getLocalSeqNumber());
            this.sipStack.getStackLogger().logDebug("remoteSequenceNumer = " + getRemoteSeqNumber());
            this.sipStack.getStackLogger().logDebug("ackLine:" + getRemoteTag() + Separators.SP + this.ackLine);
        }
    }

    public boolean isAckSeen() {
        return this.ackSeen;
    }

    public SIPRequest getLastAckSent() {
        return this.lastAckSent;
    }

    public boolean isAckSent(long cseqNo) {
        boolean z = true;
        if (getLastTransaction() == null || !(getLastTransaction() instanceof ClientTransaction)) {
            return true;
        }
        if (getLastAckSent() == null) {
            return false;
        }
        if (cseqNo > getLastAckSent().getCSeq().getSeqNumber()) {
            z = false;
        }
        return z;
    }

    public Transaction getFirstTransaction() {
        return this.firstTransaction;
    }

    public Iterator getRouteSet() {
        if (this.routeList == null) {
            return new LinkedList().listIterator();
        }
        return getRouteList().listIterator();
    }

    public synchronized void addRoute(SIPRequest sipRequest) {
        if (this.sipStack.isLoggingEnabled()) {
            this.sipStack.getStackLogger().logDebug("setContact: dialogState: " + this + "state = " + getState());
        }
        if (this.dialogState == CONFIRMED_STATE && SIPRequest.isTargetRefresh(sipRequest.getMethod())) {
            doTargetRefresh(sipRequest);
        }
        if (this.dialogState != CONFIRMED_STATE && this.dialogState != TERMINATED_STATE) {
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

    public void setDialogId(String dialogId) {
        this.dialogId = dialogId;
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

    public boolean isServer() {
        if (this.firstTransactionSeen) {
            return this.firstTransactionIsServerTransaction;
        }
        return this.serverTransactionFlag;
    }

    protected boolean isReInvite() {
        return this.reInviteFlag;
    }

    public String getDialogId() {
        if (this.dialogId == null && this.lastResponse != null) {
            this.dialogId = this.lastResponse.getDialogId(isServer());
        }
        return this.dialogId;
    }

    private static void storeFirstTransactionInfo(SIPDialog dialog, SIPTransaction transaction) {
        Contact contact = null;
        dialog.firstTransaction = transaction;
        dialog.firstTransactionSeen = true;
        dialog.firstTransactionIsServerTransaction = transaction.isServerTransaction();
        dialog.firstTransactionSecure = transaction.getRequest().getRequestURI().getScheme().equalsIgnoreCase(TokenNames.SIPS);
        dialog.firstTransactionPort = transaction.getPort();
        dialog.firstTransactionId = transaction.getBranchId();
        dialog.firstTransactionMethod = transaction.getMethod();
        if (dialog.isServer()) {
            SIPResponse response = ((SIPServerTransaction) transaction).getLastResponse();
            if (response != null) {
                contact = response.getContactHeader();
            }
            dialog.contactHeader = contact;
            return;
        }
        SIPClientTransaction ct = (SIPClientTransaction) transaction;
        if (ct != null) {
            dialog.contactHeader = ct.getOriginalRequest().getContactHeader();
        }
    }

    public void addTransaction(SIPTransaction transaction) {
        SIPRequest sipRequest = transaction.getOriginalRequest();
        if (this.firstTransactionSeen && !this.firstTransactionId.equals(transaction.getBranchId()) && transaction.getMethod().equals(this.firstTransactionMethod)) {
            this.reInviteFlag = true;
        }
        if (!this.firstTransactionSeen) {
            storeFirstTransactionInfo(this, transaction);
            if (sipRequest.getMethod().equals(TokenNames.SUBSCRIBE)) {
                this.eventHeader = (EventHeader) sipRequest.getHeader(EventHeader.NAME);
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
            this.sipStack.getStackLogger().logDebug("Transaction Added " + this + this.myTag + Separators.SLASH + this.hisTag);
            this.sipStack.getStackLogger().logDebug("TID = " + transaction.getTransactionId() + Separators.SLASH + transaction.isServerTransaction());
            this.sipStack.getStackLogger().logStackTrace();
        }
    }

    private void setRemoteTag(String hisTag) {
        if (this.sipStack.isLoggingEnabled()) {
            this.sipStack.getStackLogger().logDebug("setRemoteTag(): " + this + " remoteTag = " + this.hisTag + " new tag = " + hisTag);
        }
        if (this.hisTag == null || hisTag == null || hisTag.equals(this.hisTag)) {
            if (hisTag != null) {
                this.hisTag = hisTag;
            } else if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logWarning("setRemoteTag : called with null argument ");
            }
        } else if (getState() != DialogState.EARLY) {
            if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logDebug("Dialog is already established -- ignoring remote tag re-assignment");
            }
        } else if (this.sipStack.isRemoteTagReassignmentAllowed()) {
            if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logDebug("UNSAFE OPERATION !  tag re-assignment " + this.hisTag + " trying to set to " + hisTag + " can cause unexpected effects ");
            }
            boolean removed = false;
            if (this.sipStack.getDialog(this.dialogId) == this) {
                this.sipStack.removeDialog(this.dialogId);
                removed = true;
            }
            this.dialogId = null;
            this.hisTag = hisTag;
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
            this.sipStack.getStackLogger().logDebug("setLocalSequenceNumber: original  " + this.localSequenceNumber + " new  = " + lCseq);
        }
        if (lCseq <= this.localSequenceNumber) {
            throw new RuntimeException("Sequence number should not decrease !");
        }
        this.localSequenceNumber = lCseq;
    }

    public void setRemoteSequenceNumber(long rCseq) {
        if (this.sipStack.isLoggingEnabled()) {
            this.sipStack.getStackLogger().logDebug("setRemoteSeqno " + this + Separators.SLASH + rCseq);
        }
        this.remoteSequenceNumber = rCseq;
    }

    public void incrementLocalSequenceNumber() {
        this.localSequenceNumber++;
    }

    public int getRemoteSequenceNumber() {
        return (int) this.remoteSequenceNumber;
    }

    public int getLocalSequenceNumber() {
        return (int) this.localSequenceNumber;
    }

    public long getOriginalLocalSequenceNumber() {
        return this.originalLocalSequenceNumber;
    }

    public long getLocalSeqNumber() {
        return this.localSequenceNumber;
    }

    public long getRemoteSeqNumber() {
        return this.remoteSequenceNumber;
    }

    public String getLocalTag() {
        return this.myTag;
    }

    public String getRemoteTag() {
        return this.hisTag;
    }

    private void setLocalTag(String mytag) {
        if (this.sipStack.isLoggingEnabled()) {
            this.sipStack.getStackLogger().logDebug("set Local tag " + mytag + Separators.SP + this.dialogId);
            this.sipStack.getStackLogger().logStackTrace();
        }
        this.myTag = mytag;
    }

    public void delete() {
        setState(TERMINATED_STATE);
    }

    public CallIdHeader getCallId() {
        return this.callIdHeader;
    }

    private void setCallId(SIPRequest sipRequest) {
        this.callIdHeader = sipRequest.getCallId();
    }

    public Address getLocalParty() {
        return this.localParty;
    }

    private void setLocalParty(SIPMessage sipMessage) {
        if (isServer()) {
            this.localParty = sipMessage.getTo().getAddress();
        } else {
            this.localParty = sipMessage.getFrom().getAddress();
        }
    }

    public Address getRemoteParty() {
        if (this.sipStack.isLoggingEnabled()) {
            this.sipStack.getStackLogger().logDebug("gettingRemoteParty " + this.remoteParty);
        }
        return this.remoteParty;
    }

    public Address getRemoteTarget() {
        return this.remoteTarget;
    }

    public DialogState getState() {
        if (this.dialogState == NULL_STATE) {
            return null;
        }
        return DialogState.getObject(this.dialogState);
    }

    public boolean isSecure() {
        return this.firstTransactionSecure;
    }

    public void sendAck(Request request) throws SipException {
        sendAck(request, true);
    }

    public Request createRequest(String method) throws SipException {
        if (method.equals(TokenNames.ACK) || method.equals(Request.PRACK)) {
            throw new SipException("Invalid method specified for createRequest:" + method);
        } else if (this.lastResponse != null) {
            return createRequest(method, this.lastResponse);
        } else {
            throw new SipException("Dialog not yet established -- no response!");
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private Request createRequest(String method, SIPResponse sipResponse) throws SipException {
        if (method == null || sipResponse == null) {
            throw new NullPointerException("null argument");
        }
        if (method.equals(Request.CANCEL)) {
            throw new SipException("Dialog.createRequest(): Invalid request");
        }
        if (getState() != null) {
            SipUri sipUri;
            if (getState().getValue() == TERMINATED_STATE) {
            }
            if (isServer() && getState().getValue() == EARLY_STATE) {
            }
            if (getRemoteTarget() != null) {
                sipUri = (SipUri) getRemoteTarget().getURI().clone();
            } else {
                sipUri = (SipUri) getRemoteParty().getURI().clone();
                sipUri.clearUriParms();
            }
            CSeq cseq = new CSeq();
            try {
                cseq.setMethod(method);
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
                    this.sipStack.getStackLogger().logError("Cannot find listening point for transport " + sipResponse.getTopmostVia().getTransport());
                }
                throw new SipException("Cannot find listening point for transport " + sipResponse.getTopmostVia().getTransport());
            }
            Via via = lp.getViaHeader();
            From from = new From();
            from.setAddress(this.localParty);
            To to = new To();
            to.setAddress(this.remoteParty);
            SIPRequest sipRequest = sipResponse.createRequest(sipUri, via, cseq, from, to);
            if (SIPRequest.isTargetRefresh(method)) {
                ContactHeader contactHeader = ((ListeningPointImpl) this.sipProvider.getListeningPoint(lp.getTransport())).createContactHeader();
                ((SipURI) contactHeader.getAddress().getURI()).setSecure(isSecure());
                sipRequest.setHeader((Header) contactHeader);
            }
            try {
                ((CSeq) sipRequest.getCSeq()).setSeqNumber(this.localSequenceNumber + 1);
            } catch (Exception ex2) {
                InternalErrorHandler.handleException(ex2);
            }
            if (method.equals(TokenNames.SUBSCRIBE) && this.eventHeader != null) {
                sipRequest.addHeader(this.eventHeader);
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
            } catch (Exception ex3) {
                InternalErrorHandler.handleException(ex3);
            }
            updateRequest(sipRequest);
            return sipRequest;
        }
        throw new SipException("Dialog  " + getDialogId() + " not yet established or terminated " + getState());
    }

    public void sendRequest(ClientTransaction clientTransactionId) throws TransactionDoesNotExistException, SipException {
        sendRequest(clientTransactionId, !this.isBackToBackUserAgent);
    }

    public void sendRequest(ClientTransaction clientTransactionId, boolean allowInterleaving) throws TransactionDoesNotExistException, SipException {
        if (allowInterleaving || !clientTransactionId.getRequest().getMethod().equals(TokenNames.INVITE)) {
            SIPRequest dialogRequest = ((SIPClientTransaction) clientTransactionId).getOriginalRequest();
            if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logDebug("dialog.sendRequest  dialog = " + this + "\ndialogRequest = \n" + dialogRequest);
            }
            if (clientTransactionId == null) {
                throw new NullPointerException("null parameter");
            } else if (dialogRequest.getMethod().equals(TokenNames.ACK) || dialogRequest.getMethod().equals(Request.CANCEL)) {
                throw new SipException("Bad Request Method. " + dialogRequest.getMethod());
            } else if (this.byeSent && isTerminatedOnBye() && !dialogRequest.getMethod().equals(TokenNames.BYE)) {
                if (this.sipStack.isLoggingEnabled()) {
                    this.sipStack.getStackLogger().logError("BYE already sent for " + this);
                }
                throw new SipException("Cannot send request; BYE already sent");
            } else {
                if (dialogRequest.getTopmostVia() == null) {
                    dialogRequest.addHeader((Header) ((SIPClientTransaction) clientTransactionId).getOutgoingViaHeader());
                }
                if (getCallId().getCallId().equalsIgnoreCase(dialogRequest.getCallId().getCallId())) {
                    ((SIPClientTransaction) clientTransactionId).setDialog(this, this.dialogId);
                    addTransaction((SIPTransaction) clientTransactionId);
                    ((SIPClientTransaction) clientTransactionId).isMapped = true;
                    From from = (From) dialogRequest.getFrom();
                    To to = (To) dialogRequest.getTo();
                    if (getLocalTag() == null || from.getTag() == null || from.getTag().equals(getLocalTag())) {
                        if (!(getRemoteTag() == null || to.getTag() == null || to.getTag().equals(getRemoteTag()) || !this.sipStack.isLoggingEnabled())) {
                            this.sipStack.getStackLogger().logWarning("To header tag mismatch expecting " + getRemoteTag());
                        }
                        if (getLocalTag() == null && dialogRequest.getMethod().equals(TokenNames.NOTIFY)) {
                            if (getMethod().equals(TokenNames.SUBSCRIBE)) {
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
                        } catch (Exception ex) {
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
                                oldChannel.useCount += NULL_STATE;
                                if (this.sipStack.isLoggingEnabled()) {
                                    this.sipStack.getStackLogger().logDebug("oldChannel: useCount " + oldChannel.useCount);
                                }
                            }
                            if (messageChannel == null) {
                                if (this.sipStack.isLoggingEnabled()) {
                                    this.sipStack.getStackLogger().logDebug("Null message channel using outbound proxy !");
                                }
                                Hop outboundProxy = this.sipStack.getRouter(dialogRequest).getOutboundProxy();
                                if (outboundProxy == null) {
                                    throw new SipException("No route found! hop=" + hop);
                                }
                                messageChannel = this.sipStack.createRawMessageChannel(getSipProvider().getListeningPoint(outboundProxy.getTransport()).getIPAddress(), this.firstTransactionPort, outboundProxy);
                                if (messageChannel != null) {
                                    ((SIPClientTransaction) clientTransactionId).setEncapsulatedChannel(messageChannel);
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
                            if (!(this.sipStack.cacheClientConnections || oldChannel == null || oldChannel.useCount > 0)) {
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
                                if (dialogRequest.getMethod().equals(TokenNames.BYE)) {
                                    this.byeSent = true;
                                    if (isTerminatedOnBye()) {
                                        setState(DialogState._TERMINATED);
                                    }
                                }
                                return;
                            } catch (IOException ex3) {
                                throw new SipException("error sending message", ex3);
                            }
                        } catch (Exception ex4) {
                            if (this.sipStack.isLoggingEnabled()) {
                                this.sipStack.getStackLogger().logException(ex4);
                            }
                            throw new SipException("Could not create message channel", ex4);
                        }
                    }
                    throw new SipException("From tag mismatch expecting  " + getLocalTag());
                }
                if (this.sipStack.isLoggingEnabled()) {
                    this.sipStack.getStackLogger().logError("CallID " + getCallId());
                    this.sipStack.getStackLogger().logError("RequestCallID = " + dialogRequest.getCallId().getCallId());
                    this.sipStack.getStackLogger().logError("dialog =  " + this);
                }
                throw new SipException("Bad call ID in request");
            }
        }
        new Thread(new ReInviteSender(clientTransactionId)).start();
    }

    private boolean toRetransmitFinalResponse(int T2) {
        int i = this.retransmissionTicksLeft + NULL_STATE;
        this.retransmissionTicksLeft = i;
        if (i != 0) {
            return false;
        }
        if (this.prevRetransmissionTicks * 2 <= T2) {
            this.retransmissionTicksLeft = this.prevRetransmissionTicks * 2;
        } else {
            this.retransmissionTicksLeft = this.prevRetransmissionTicks;
        }
        this.prevRetransmissionTicks = this.retransmissionTicksLeft;
        return true;
    }

    protected void setRetransmissionTicks() {
        this.retransmissionTicksLeft = 1;
        this.prevRetransmissionTicks = 1;
    }

    public void resendAck() throws SipException {
        if (getLastAckSent() != null) {
            if (getLastAckSent().getHeader(TimeStampHeader.NAME) != null && this.sipStack.generateTimeStampHeader) {
                TimeStamp ts = new TimeStamp();
                try {
                    ts.setTimeStamp((float) System.currentTimeMillis());
                    getLastAckSent().setHeader((Header) ts);
                } catch (InvalidArgumentException e) {
                }
            }
            sendAck(getLastAckSent(), false);
        }
    }

    public String getMethod() {
        return this.method;
    }

    protected void startTimer(SIPServerTransaction transaction) {
        if (this.timerTask == null || this.timerTask.transaction != transaction) {
            if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logDebug("Starting dialog timer for " + getDialogId());
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
            }
        } else {
            if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logDebug("Timer already running for " + getDialogId());
            }
        }
    }

    protected void stopTimer() {
        try {
            acquireTimerTaskSem();
            if (this.timerTask != null) {
                this.timerTask.cancel();
                this.timerTask = null;
            }
            releaseTimerTaskSem();
        } catch (Exception e) {
        } catch (Throwable th) {
            releaseTimerTaskSem();
        }
    }

    public Request createPrack(Response relResponse) throws DialogDoesNotExistException, SipException {
        if (getState() == null || getState().equals(DialogState.TERMINATED)) {
            throw new DialogDoesNotExistException("Dialog not initialized or terminated");
        } else if (((RSeq) relResponse.getHeader(RSeqHeader.NAME)) == null) {
            throw new SipException("Missing RSeq Header");
        } else {
            try {
                SIPResponse sipResponse = (SIPResponse) relResponse;
                SIPRequest sipRequest = (SIPRequest) createRequest(Request.PRACK, (SIPResponse) relResponse);
                sipRequest.setToTag(sipResponse.getTo().getTag());
                RAck rack = new RAck();
                RSeq rseq = (RSeq) relResponse.getHeader(RSeqHeader.NAME);
                rack.setMethod(sipResponse.getCSeq().getMethod());
                rack.setCSequenceNumber((long) ((int) sipResponse.getCSeq().getSeqNumber()));
                rack.setRSequenceNumber(rseq.getSeqNumber());
                sipRequest.setHeader((Header) rack);
                return sipRequest;
            } catch (Exception ex) {
                InternalErrorHandler.handleException(ex);
                return null;
            }
        }
    }

    private void updateRequest(SIPRequest sipRequest) {
        RouteList rl = getRouteList();
        if (rl.size() > 0) {
            sipRequest.setHeader((Header) rl);
        } else {
            sipRequest.removeHeader(RouteHeader.NAME);
        }
        if (MessageFactoryImpl.getDefaultUserAgentHeader() != null) {
            sipRequest.setHeader(MessageFactoryImpl.getDefaultUserAgentHeader());
        }
    }

    public Request createAck(long cseqno) throws InvalidArgumentException, SipException {
        if (!this.method.equals(TokenNames.INVITE)) {
            throw new SipException("Dialog was not created with an INVITE" + this.method);
        } else if (cseqno <= 0) {
            throw new InvalidArgumentException("bad cseq <= 0 ");
        } else if (cseqno > 4294967295L) {
            throw new InvalidArgumentException("bad cseq > 4294967295");
        } else if (this.remoteTarget == null) {
            throw new SipException("Cannot create ACK - no remote Target!");
        } else {
            if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logDebug("createAck " + this + " cseqno " + cseqno);
            }
            if (this.lastInviteOkReceived < cseqno) {
                if (this.sipStack.isLoggingEnabled()) {
                    this.sipStack.getStackLogger().logDebug("WARNING : Attempt to crete ACK without OK " + this);
                    this.sipStack.getStackLogger().logDebug("LAST RESPONSE = " + this.lastResponse);
                }
                throw new SipException("Dialog not yet established -- no OK response!");
            }
            try {
                SipURI uri4transport;
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
                        this.sipStack.getStackLogger().logError("remoteTargetURI " + this.remoteTarget.getURI());
                        this.sipStack.getStackLogger().logError("uri4transport = " + uri4transport);
                        this.sipStack.getStackLogger().logError("No LP found for transport=" + transport);
                    }
                    throw new SipException("Cannot create ACK - no ListeningPoint for transport towards next hop found:" + transport);
                }
                SIPRequest sipRequest = new SIPRequest();
                sipRequest.setMethod(TokenNames.ACK);
                sipRequest.setRequestURI((SipUri) getRemoteTarget().getURI().clone());
                sipRequest.setCallId(this.callIdHeader);
                sipRequest.setCSeq(new CSeq(cseqno, TokenNames.ACK));
                List<Via> vias = new ArrayList();
                Via via = this.lastResponse.getTopmostVia();
                via.removeParameters();
                if (!(this.originalRequest == null || this.originalRequest.getTopmostVia() == null)) {
                    NameValueList originalRequestParameters = this.originalRequest.getTopmostVia().getParameters();
                    if (originalRequestParameters != null && originalRequestParameters.size() > 0) {
                        via.setParameters((NameValueList) originalRequestParameters.clone());
                    }
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
                if (this.originalRequest != null) {
                    Authorization authorization = this.originalRequest.getAuthorization();
                    if (authorization != null) {
                        sipRequest.setHeader((Header) authorization);
                    }
                }
                updateRequest(sipRequest);
                return sipRequest;
            } catch (Exception ex) {
                InternalErrorHandler.handleException(ex);
                throw new SipException("unexpected exception ", ex);
            }
        }
    }

    public SipProviderImpl getSipProvider() {
        return this.sipProvider;
    }

    public void setSipProvider(SipProviderImpl sipProvider) {
        this.sipProvider = sipProvider;
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setLastResponse(SIPTransaction transaction, SIPResponse sipResponse) {
        this.callIdHeader = sipResponse.getCallId();
        int statusCode = sipResponse.getStatusCode();
        if (statusCode == 100) {
            if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logWarning("Invalid status code - 100 in setLastResponse - ignoring");
            }
            return;
        }
        this.lastResponse = sipResponse;
        setAssigned();
        if (this.sipStack.isLoggingEnabled()) {
            this.sipStack.getStackLogger().logDebug("sipDialog: setLastResponse:" + this + " lastResponse = " + this.lastResponse.getFirstLine());
        }
        if (getState() == DialogState.TERMINATED) {
            if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logDebug("sipDialog: setLastResponse -- dialog is terminated - ignoring ");
            }
            if (sipResponse.getCSeq().getMethod().equals(TokenNames.INVITE) && statusCode == Response.OK) {
                this.lastInviteOkReceived = Math.max(sipResponse.getCSeq().getSeqNumber(), this.lastInviteOkReceived);
            }
            return;
        }
        String cseqMethod = sipResponse.getCSeq().getMethod();
        if (this.sipStack.isLoggingEnabled()) {
            this.sipStack.getStackLogger().logStackTrace();
            this.sipStack.getStackLogger().logDebug("cseqMethod = " + cseqMethod);
            this.sipStack.getStackLogger().logDebug("dialogState = " + getState());
            this.sipStack.getStackLogger().logDebug("method = " + getMethod());
            this.sipStack.getStackLogger().logDebug("statusCode = " + statusCode);
            this.sipStack.getStackLogger().logDebug("transaction = " + transaction);
        }
        SIPTransactionStack sIPTransactionStack;
        if (transaction == null || (transaction instanceof ClientTransaction)) {
            sIPTransactionStack = this.sipStack;
            if (SIPTransactionStack.isDialogCreated(cseqMethod)) {
                if (getState() == null && statusCode / 100 == 1) {
                    setState(EARLY_STATE);
                    if ((sipResponse.getToTag() != null || this.sipStack.rfc2543Supported) && getRemoteTag() == null) {
                        setRemoteTag(sipResponse.getToTag());
                        setDialogId(sipResponse.getDialogId(false));
                        this.sipStack.putDialog(this);
                        addRoute(sipResponse);
                    }
                } else if (getState() != null && getState().equals(DialogState.EARLY) && statusCode / 100 == 1) {
                    if (cseqMethod.equals(getMethod()) && transaction != null && (sipResponse.getToTag() != null || this.sipStack.rfc2543Supported)) {
                        setRemoteTag(sipResponse.getToTag());
                        setDialogId(sipResponse.getDialogId(false));
                        this.sipStack.putDialog(this);
                        addRoute(sipResponse);
                    }
                } else if (statusCode / 100 == 2) {
                    if (cseqMethod.equals(getMethod()) && ((sipResponse.getToTag() != null || this.sipStack.rfc2543Supported) && getState() != DialogState.CONFIRMED)) {
                        setRemoteTag(sipResponse.getToTag());
                        setDialogId(sipResponse.getDialogId(false));
                        this.sipStack.putDialog(this);
                        addRoute(sipResponse);
                        setState(CONFIRMED_STATE);
                    }
                    if (cseqMethod.equals(TokenNames.INVITE)) {
                        this.lastInviteOkReceived = Math.max(sipResponse.getCSeq().getSeqNumber(), this.lastInviteOkReceived);
                    }
                } else if (statusCode >= Response.MULTIPLE_CHOICES && statusCode <= 699 && (getState() == null || (cseqMethod.equals(getMethod()) && getState().getValue() == EARLY_STATE))) {
                    setState(TERMINATED_STATE);
                }
                if (!(getState() == DialogState.CONFIRMED || getState() == DialogState.TERMINATED || this.originalRequest == null)) {
                    RecordRouteList rrList = this.originalRequest.getRecordRouteHeaders();
                    if (rrList != null) {
                        ListIterator<RecordRoute> it = rrList.listIterator(rrList.size());
                        while (it.hasPrevious()) {
                            RecordRoute rr = (RecordRoute) it.previous();
                            Route route = (Route) this.routeList.getFirst();
                            if (route != null && rr.getAddress().equals(route.getAddress())) {
                                this.routeList.removeFirst();
                            }
                        }
                    }
                }
            } else if (cseqMethod.equals(TokenNames.NOTIFY) && ((getMethod().equals(TokenNames.SUBSCRIBE) || getMethod().equals(Request.REFER)) && sipResponse.getStatusCode() / 100 == 2 && getState() == null)) {
                setDialogId(sipResponse.getDialogId(true));
                this.sipStack.putDialog(this);
                setState(CONFIRMED_STATE);
            } else if (cseqMethod.equals(TokenNames.BYE) && statusCode / 100 == 2 && isTerminatedOnBye()) {
                setState(TERMINATED_STATE);
            }
        } else if (cseqMethod.equals(TokenNames.BYE) && statusCode / 100 == 2 && isTerminatedOnBye()) {
            setState(TERMINATED_STATE);
        } else {
            boolean doPutDialog = false;
            if (getLocalTag() == null && sipResponse.getTo().getTag() != null) {
                sIPTransactionStack = this.sipStack;
                if (SIPTransactionStack.isDialogCreated(cseqMethod) && cseqMethod.equals(getMethod())) {
                    setLocalTag(sipResponse.getTo().getTag());
                    doPutDialog = true;
                }
            }
            if (statusCode / 100 == 2) {
                if (this.dialogState <= EARLY_STATE && (cseqMethod.equals(TokenNames.INVITE) || cseqMethod.equals(TokenNames.SUBSCRIBE) || cseqMethod.equals(Request.REFER))) {
                    setState(CONFIRMED_STATE);
                }
                if (doPutDialog) {
                    setDialogId(sipResponse.getDialogId(true));
                    this.sipStack.putDialog(this);
                }
                if (transaction.getState() != TransactionState.TERMINATED && sipResponse.getStatusCode() == Response.OK && cseqMethod.equals(TokenNames.INVITE) && this.isBackToBackUserAgent && !takeAckSem()) {
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
            } else if (statusCode == Response.BAD_EVENT && (cseqMethod.equals(TokenNames.NOTIFY) || cseqMethod.equals(TokenNames.SUBSCRIBE))) {
                if (this.sipStack.isLoggingEnabled()) {
                    this.sipStack.getStackLogger().logDebug("RFC 3265 : Not setting dialog to TERMINATED for 489");
                }
            } else if (!(isReInvite() || getState() == DialogState.CONFIRMED)) {
                setState(TERMINATED_STATE);
            }
        }
    }

    public void startRetransmitTimer(SIPServerTransaction sipServerTx, Response response) {
        if (sipServerTx.getRequest().getMethod().equals(TokenNames.INVITE) && response.getStatusCode() / 100 == 2) {
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

    public Response createReliableProvisionalResponse(int statusCode) throws InvalidArgumentException, SipException {
        if (!this.firstTransactionIsServerTransaction) {
            throw new SipException("Not a Server Dialog!");
        } else if (statusCode <= 100 || statusCode > 199) {
            throw new InvalidArgumentException("Bad status code ");
        } else {
            SIPRequest request = this.originalRequest;
            if (request.getMethod().equals(TokenNames.INVITE)) {
                ListIterator<SIPHeader> list = request.getHeaders(SupportedHeader.NAME);
                if (list == null || !optionPresent(list, "100rel")) {
                    list = request.getHeaders(RequireHeader.NAME);
                    if (list == null || !optionPresent(list, "100rel")) {
                        throw new SipException("No Supported/Require 100rel header in the request");
                    }
                }
                SIPResponse response = request.createResponse(statusCode);
                Require require = new Require();
                try {
                    require.setOptionTag("100rel");
                } catch (Exception ex) {
                    InternalErrorHandler.handleException(ex);
                }
                response.addHeader((Header) require);
                new RSeq().setSeqNumber(1);
                RecordRouteList rrl = request.getRecordRouteHeaders();
                if (rrl != null) {
                    response.setHeader((Header) (RecordRouteList) rrl.clone());
                }
                return response;
            }
            throw new SipException("Bad method");
        }
    }

    public boolean handlePrack(SIPRequest prackRequest) {
        if (isServer()) {
            SIPServerTransaction sipServerTransaction = (SIPServerTransaction) getFirstTransaction();
            SIPResponse sipResponse = sipServerTransaction.getReliableProvisionalResponse();
            if (sipResponse == null) {
                if (this.sipStack.isLoggingEnabled()) {
                    this.sipStack.getStackLogger().logDebug("Dropping Prack -- ReliableResponse not found");
                }
                return false;
            }
            RAck rack = (RAck) prackRequest.getHeader(RAckHeader.NAME);
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
            } else {
                if (rack.getRSequenceNumber() == ((RSeq) sipResponse.getHeader(RSeqHeader.NAME)).getSeqNumber()) {
                    return sipServerTransaction.prackRecieved();
                }
                if (this.sipStack.isLoggingEnabled()) {
                    this.sipStack.getStackLogger().logDebug("Dropping Prack -- RSeq Header does not match PRACK");
                }
                return false;
            }
        }
        if (this.sipStack.isLoggingEnabled()) {
            this.sipStack.getStackLogger().logDebug("Dropping Prack -- not a server Dialog");
        }
        return false;
    }

    public void sendReliableProvisionalResponse(Response relResponse) throws SipException {
        if (isServer()) {
            SIPResponse sipResponse = (SIPResponse) relResponse;
            if (relResponse.getStatusCode() == 100) {
                throw new SipException("Cannot send 100 as a reliable provisional response");
            } else if (relResponse.getStatusCode() / 100 > 2) {
                throw new SipException("Response code is not a 1xx response - should be in the range 101 to 199 ");
            } else if (sipResponse.getToTag() == null) {
                throw new SipException("Badly formatted response -- To tag mandatory for Reliable Provisional Response");
            } else {
                ListIterator requireList = relResponse.getHeaders(RequireHeader.NAME);
                boolean found = false;
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
                return;
            }
        }
        throw new SipException("Not a Server Dialog");
    }

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
                releaseTimerTaskSem();
                return false;
            } catch (Throwable th) {
                releaseTimerTaskSem();
            }
        } else if (getState() == DialogState.TERMINATED) {
            if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logDebug("Dialog is terminated -- dropping ACK");
            }
            return false;
        } else {
            SIPServerTransaction tr = getInviteTransaction();
            SIPResponse lastResponse = tr != null ? tr.getLastResponse() : null;
            if (tr != null && lastResponse != null && lastResponse.getStatusCode() / 100 == 2 && lastResponse.getCSeq().getMethod().equals(TokenNames.INVITE) && lastResponse.getCSeq().getSeqNumber() == sipRequest.getCSeq().getSeqNumber()) {
                ackTransaction.setDialog(this, lastResponse.getDialogId(false));
                ackReceived(sipRequest);
                if (this.sipStack.isLoggingEnabled()) {
                    this.sipStack.getStackLogger().logDebug("ACK for 2XX response --- sending to TU ");
                }
                return true;
            }
            if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logDebug(" INVITE transaction not found  -- Discarding ACK");
            }
            return false;
        }
    }

    void setEarlyDialogId(String earlyDialogId) {
        this.earlyDialogId = earlyDialogId;
    }

    String getEarlyDialogId() {
        return this.earlyDialogId;
    }

    void releaseAckSem() {
        if (this.isBackToBackUserAgent) {
            if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logDebug("releaseAckSem]" + this);
            }
            this.ackSem.release();
        }
    }

    boolean takeAckSem() {
        if (this.sipStack.isLoggingEnabled()) {
            this.sipStack.getStackLogger().logDebug("[takeAckSem " + this);
        }
        try {
            if (this.ackSem.tryAcquire(2, TimeUnit.SECONDS)) {
                if (this.sipStack.isLoggingEnabled()) {
                    recordStackTrace();
                }
                return true;
            }
            if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logError("Cannot aquire ACK semaphore");
            }
            if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logDebug("Semaphore previously acquired at " + this.stackTrace);
                this.sipStack.getStackLogger().logStackTrace();
            }
            return false;
        } catch (InterruptedException e) {
            this.sipStack.getStackLogger().logError("Cannot aquire ACK semaphore");
            return false;
        }
    }

    private void setLastAckReceived(SIPRequest lastAckReceived) {
        this.lastAckReceived = lastAckReceived;
    }

    protected SIPRequest getLastAckReceived() {
        return this.lastAckReceived;
    }

    private void setLastAckSent(SIPRequest lastAckSent) {
        this.lastAckSent = lastAckSent;
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

    public void setBackToBackUserAgent() {
        this.isBackToBackUserAgent = true;
    }

    EventHeader getEventHeader() {
        return this.eventHeader;
    }

    void setEventHeader(EventHeader eventHeader) {
        this.eventHeader = eventHeader;
    }

    void setServerTransactionFlag(boolean serverTransactionFlag) {
        this.serverTransactionFlag = serverTransactionFlag;
    }

    void setReInviteFlag(boolean reInviteFlag) {
        this.reInviteFlag = reInviteFlag;
    }

    public boolean isSequnceNumberValidation() {
        return this.sequenceNumberValidation;
    }

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
