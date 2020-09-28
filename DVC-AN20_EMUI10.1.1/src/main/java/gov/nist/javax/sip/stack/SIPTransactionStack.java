package gov.nist.javax.sip.stack;

import gov.nist.core.Host;
import gov.nist.core.HostPort;
import gov.nist.core.Separators;
import gov.nist.core.ServerLogger;
import gov.nist.core.StackLogger;
import gov.nist.core.ThreadAuditor;
import gov.nist.core.net.AddressResolver;
import gov.nist.core.net.DefaultNetworkLayer;
import gov.nist.core.net.NetworkLayer;
import gov.nist.javax.sip.DefaultAddressResolver;
import gov.nist.javax.sip.ListeningPointImpl;
import gov.nist.javax.sip.LogRecordFactory;
import gov.nist.javax.sip.SIPConstants;
import gov.nist.javax.sip.SipListenerExt;
import gov.nist.javax.sip.SipProviderImpl;
import gov.nist.javax.sip.SipStackImpl;
import gov.nist.javax.sip.address.ParameterNames;
import gov.nist.javax.sip.header.Event;
import gov.nist.javax.sip.header.extensions.JoinHeader;
import gov.nist.javax.sip.header.extensions.ReplacesHeader;
import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.message.SIPResponse;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.DialogState;
import javax.sip.DialogTerminatedEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.SipListener;
import javax.sip.TransactionState;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.address.Hop;
import javax.sip.address.Router;
import javax.sip.header.CallIdHeader;
import javax.sip.message.Request;

public abstract class SIPTransactionStack implements SIPTransactionEventListener, SIPDialogEventListener {
    public static final int BASE_TIMER_INTERVAL = 500;
    public static final int CONNECTION_LINGER_TIME = 8;
    protected static final Set<String> dialogCreatingMethods = new HashSet();
    private AtomicInteger activeClientTransactionCount;
    protected AddressResolver addressResolver;
    protected boolean cacheClientConnections;
    protected boolean cacheServerConnections;
    protected boolean cancelClientTransactionChecked;
    protected boolean checkBranchId;
    private ConcurrentHashMap<String, SIPClientTransaction> clientTransactionTable;
    protected int clientTransactionTableHiwaterMark;
    protected int clientTransactionTableLowaterMark;
    protected DefaultRouter defaultRouter;
    protected ConcurrentHashMap<String, SIPDialog> dialogTable;
    protected ConcurrentHashMap<String, SIPDialog> earlyDialogTable;
    private ConcurrentHashMap<String, SIPClientTransaction> forkedClientTransactionTable;
    protected HashSet<String> forkedEvents;
    protected boolean generateTimeStampHeader;
    protected IOHandler ioHandler;
    protected boolean isAutomaticDialogErrorHandlingEnabled;
    protected boolean isAutomaticDialogSupportEnabled;
    protected boolean isBackToBackUserAgent;
    protected boolean isDialogTerminatedEventDeliveredForNullDialog;
    protected LogRecordFactory logRecordFactory;
    protected boolean logStackTraceOnMessageSend;
    protected int maxConnections;
    protected int maxContentLength;
    protected int maxForkTime;
    protected int maxListenerResponseTime;
    protected int maxMessageSize;
    private ConcurrentHashMap<String, SIPServerTransaction> mergeTable;
    private Collection<MessageProcessor> messageProcessors;
    protected boolean needsLogging;
    protected NetworkLayer networkLayer;
    private boolean non2XXAckPassedToListener;
    protected String outboundProxy;
    private ConcurrentHashMap<String, SIPServerTransaction> pendingTransactions;
    protected int readTimeout;
    protected int receiveUdpBufferSize;
    protected boolean remoteTagReassignmentAllowed;
    protected ConcurrentHashMap<String, SIPServerTransaction> retransmissionAlertTransactions;
    protected boolean rfc2543Supported;
    protected Router router;
    protected String routerPath;
    protected int sendUdpBufferSize;
    protected ServerLogger serverLogger;
    private ConcurrentHashMap<String, SIPServerTransaction> serverTransactionTable;
    protected int serverTransactionTableHighwaterMark;
    protected int serverTransactionTableLowaterMark;
    protected StackMessageFactory sipMessageFactory;
    protected String stackAddress;
    protected boolean stackDoesCongestionControl;
    protected InetAddress stackInetAddress;
    private StackLogger stackLogger;
    protected String stackName;
    private ConcurrentHashMap<String, SIPServerTransaction> terminatedServerTransactionsPendingAck;
    protected ThreadAuditor threadAuditor;
    protected int threadPoolSize;
    private Timer timer;
    protected boolean toExit;
    boolean udpFlag;
    protected boolean unlimitedClientTransactionTableSize;
    protected boolean unlimitedServerTransactionTableSize;
    protected boolean useRouterForAll;

    static {
        dialogCreatingMethods.add(Request.REFER);
        dialogCreatingMethods.add("INVITE");
        dialogCreatingMethods.add("SUBSCRIBE");
    }

    class PingTimer extends SIPStackTimerTask {
        ThreadAuditor.ThreadHandle threadHandle;

        public PingTimer(ThreadAuditor.ThreadHandle a_oThreadHandle) {
            this.threadHandle = a_oThreadHandle;
        }

        /* access modifiers changed from: protected */
        @Override // gov.nist.javax.sip.stack.SIPStackTimerTask
        public void runTask() {
            if (SIPTransactionStack.this.getTimer() != null) {
                if (this.threadHandle == null) {
                    this.threadHandle = SIPTransactionStack.this.getThreadAuditor().addCurrentThread();
                }
                this.threadHandle.ping();
                SIPTransactionStack.this.getTimer().schedule(new PingTimer(this.threadHandle), this.threadHandle.getPingIntervalInMillisecs());
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class RemoveForkedTransactionTimerTask extends SIPStackTimerTask {
        private SIPClientTransaction clientTransaction;

        public RemoveForkedTransactionTimerTask(SIPClientTransaction sipClientTransaction) {
            this.clientTransaction = sipClientTransaction;
        }

        /* access modifiers changed from: protected */
        @Override // gov.nist.javax.sip.stack.SIPStackTimerTask
        public void runTask() {
            SIPTransactionStack.this.forkedClientTransactionTable.remove(this.clientTransaction.getTransactionId());
        }
    }

    protected SIPTransactionStack() {
        this.unlimitedServerTransactionTableSize = true;
        this.unlimitedClientTransactionTableSize = true;
        this.serverTransactionTableHighwaterMark = 5000;
        this.serverTransactionTableLowaterMark = 4000;
        this.clientTransactionTableHiwaterMark = 1000;
        this.clientTransactionTableLowaterMark = 800;
        this.activeClientTransactionCount = new AtomicInteger(0);
        this.rfc2543Supported = true;
        this.threadAuditor = new ThreadAuditor();
        this.cancelClientTransactionChecked = true;
        this.remoteTagReassignmentAllowed = true;
        this.logStackTraceOnMessageSend = true;
        this.stackDoesCongestionControl = true;
        this.isBackToBackUserAgent = false;
        this.isAutomaticDialogErrorHandlingEnabled = true;
        this.isDialogTerminatedEventDeliveredForNullDialog = false;
        this.maxForkTime = 0;
        this.toExit = false;
        this.forkedEvents = new HashSet<>();
        this.threadPoolSize = -1;
        this.cacheServerConnections = true;
        this.cacheClientConnections = true;
        this.maxConnections = -1;
        this.messageProcessors = new ArrayList();
        this.ioHandler = new IOHandler(this);
        this.readTimeout = -1;
        this.maxListenerResponseTime = -1;
        this.addressResolver = new DefaultAddressResolver();
        this.dialogTable = new ConcurrentHashMap<>();
        this.earlyDialogTable = new ConcurrentHashMap<>();
        this.clientTransactionTable = new ConcurrentHashMap<>();
        this.serverTransactionTable = new ConcurrentHashMap<>();
        this.terminatedServerTransactionsPendingAck = new ConcurrentHashMap<>();
        this.mergeTable = new ConcurrentHashMap<>();
        this.retransmissionAlertTransactions = new ConcurrentHashMap<>();
        this.timer = new Timer();
        this.pendingTransactions = new ConcurrentHashMap<>();
        this.forkedClientTransactionTable = new ConcurrentHashMap<>();
        if (getThreadAuditor().isEnabled()) {
            this.timer.schedule(new PingTimer(null), 0);
        }
    }

    /* access modifiers changed from: protected */
    public void reInit() {
        if (this.stackLogger.isLoggingEnabled()) {
            this.stackLogger.logDebug("Re-initializing !");
        }
        this.messageProcessors = new ArrayList();
        this.ioHandler = new IOHandler(this);
        this.pendingTransactions = new ConcurrentHashMap<>();
        this.clientTransactionTable = new ConcurrentHashMap<>();
        this.serverTransactionTable = new ConcurrentHashMap<>();
        this.retransmissionAlertTransactions = new ConcurrentHashMap<>();
        this.mergeTable = new ConcurrentHashMap<>();
        this.dialogTable = new ConcurrentHashMap<>();
        this.earlyDialogTable = new ConcurrentHashMap<>();
        this.terminatedServerTransactionsPendingAck = new ConcurrentHashMap<>();
        this.forkedClientTransactionTable = new ConcurrentHashMap<>();
        this.timer = new Timer();
        this.activeClientTransactionCount = new AtomicInteger(0);
    }

    public SocketAddress obtainLocalAddress(InetAddress dst, int dstPort, InetAddress localAddress, int localPort) throws IOException {
        return this.ioHandler.obtainLocalAddress(dst, dstPort, localAddress, localPort);
    }

    public void disableLogging() {
        getStackLogger().disableLogging();
    }

    public void enableLogging() {
        getStackLogger().enableLogging();
    }

    public void printDialogTable() {
        if (isLoggingEnabled()) {
            StackLogger stackLogger2 = getStackLogger();
            stackLogger2.logDebug("dialog table  = " + this.dialogTable);
            PrintStream printStream = System.out;
            printStream.println("dialog table = " + this.dialogTable);
        }
    }

    public SIPServerTransaction getRetransmissionAlertTransaction(String dialogId) {
        return this.retransmissionAlertTransactions.get(dialogId);
    }

    public static boolean isDialogCreated(String method) {
        return dialogCreatingMethods.contains(method);
    }

    public void addExtensionMethod(String extensionMethod) {
        if (!extensionMethod.equals("NOTIFY")) {
            dialogCreatingMethods.add(extensionMethod.trim().toUpperCase());
        } else if (this.stackLogger.isLoggingEnabled()) {
            this.stackLogger.logDebug("NOTIFY Supported Natively");
        }
    }

    public void putDialog(SIPDialog dialog) {
        String dialogId = dialog.getDialogId();
        if (!this.dialogTable.containsKey(dialogId)) {
            if (this.stackLogger.isLoggingEnabled()) {
                StackLogger stackLogger2 = this.stackLogger;
                stackLogger2.logDebug("putDialog dialogId=" + dialogId + " dialog = " + dialog);
            }
            dialog.setStack(this);
            if (this.stackLogger.isLoggingEnabled()) {
                this.stackLogger.logStackTrace();
            }
            this.dialogTable.put(dialogId, dialog);
        } else if (this.stackLogger.isLoggingEnabled()) {
            StackLogger stackLogger3 = this.stackLogger;
            stackLogger3.logDebug("putDialog: dialog already exists" + dialogId + " in table = " + this.dialogTable.get(dialogId));
        }
    }

    public SIPDialog createDialog(SIPTransaction transaction) {
        if (!(transaction instanceof SIPClientTransaction)) {
            return new SIPDialog(transaction);
        }
        String dialogId = ((SIPRequest) transaction.getRequest()).getDialogId(false);
        if (this.earlyDialogTable.get(dialogId) != null) {
            SIPDialog dialog = this.earlyDialogTable.get(dialogId);
            if (dialog.getState() == null || dialog.getState() == DialogState.EARLY) {
                return dialog;
            }
            SIPDialog retval = new SIPDialog(transaction);
            this.earlyDialogTable.put(dialogId, retval);
            return retval;
        }
        SIPDialog retval2 = new SIPDialog(transaction);
        this.earlyDialogTable.put(dialogId, retval2);
        return retval2;
    }

    public SIPDialog createDialog(SIPClientTransaction transaction, SIPResponse sipResponse) {
        String dialogId = ((SIPRequest) transaction.getRequest()).getDialogId(false);
        if (this.earlyDialogTable.get(dialogId) == null) {
            return new SIPDialog(transaction, sipResponse);
        }
        SIPDialog retval = this.earlyDialogTable.get(dialogId);
        if (!sipResponse.isFinalResponse()) {
            return retval;
        }
        this.earlyDialogTable.remove(dialogId);
        return retval;
    }

    public SIPDialog createDialog(SipProviderImpl sipProvider, SIPResponse sipResponse) {
        return new SIPDialog(sipProvider, sipResponse);
    }

    public void removeDialog(SIPDialog dialog) {
        String id = dialog.getDialogId();
        String earlyId = dialog.getEarlyDialogId();
        if (earlyId != null) {
            this.earlyDialogTable.remove(earlyId);
            this.dialogTable.remove(earlyId);
        }
        if (id != null) {
            if (this.dialogTable.get(id) == dialog) {
                this.dialogTable.remove(id);
            }
            if (!dialog.testAndSetIsDialogTerminatedEventDelivered()) {
                dialog.getSipProvider().handleEvent(new DialogTerminatedEvent(dialog.getSipProvider(), dialog), null);
            }
        } else if (this.isDialogTerminatedEventDeliveredForNullDialog && !dialog.testAndSetIsDialogTerminatedEventDelivered()) {
            dialog.getSipProvider().handleEvent(new DialogTerminatedEvent(dialog.getSipProvider(), dialog), null);
        }
    }

    public SIPDialog getDialog(String dialogId) {
        SIPDialog sipDialog = this.dialogTable.get(dialogId);
        if (this.stackLogger.isLoggingEnabled()) {
            StackLogger stackLogger2 = this.stackLogger;
            stackLogger2.logDebug("getDialog(" + dialogId + ") : returning " + sipDialog);
        }
        return sipDialog;
    }

    public void removeDialog(String dialogId) {
        if (this.stackLogger.isLoggingEnabled()) {
            this.stackLogger.logWarning("Silently removing dialog from table");
        }
        this.dialogTable.remove(dialogId);
    }

    public SIPClientTransaction findSubscribeTransaction(SIPRequest notifyMessage, ListeningPointImpl listeningPoint) {
        SIPClientTransaction retval = null;
        try {
            if (this.stackLogger.isLoggingEnabled()) {
                StackLogger stackLogger2 = this.stackLogger;
                stackLogger2.logDebug("ct table size = " + this.clientTransactionTable.size());
            }
            String thisToTag = notifyMessage.getTo().getTag();
            if (thisToTag == null) {
                return retval;
            }
            Event eventHdr = (Event) notifyMessage.getHeader("Event");
            if (eventHdr == null) {
                if (this.stackLogger.isLoggingEnabled()) {
                    this.stackLogger.logDebug("event Header is null -- returning null");
                }
                if (this.stackLogger.isLoggingEnabled()) {
                    StackLogger stackLogger3 = this.stackLogger;
                    stackLogger3.logDebug("findSubscribeTransaction : returning " + retval);
                }
                return retval;
            }
            for (SIPClientTransaction ct : this.clientTransactionTable.values()) {
                if (ct.getMethod().equals("SUBSCRIBE")) {
                    String fromTag = ct.from.getTag();
                    Event hisEvent = ct.event;
                    if (hisEvent != null) {
                        if (this.stackLogger.isLoggingEnabled()) {
                            StackLogger stackLogger4 = this.stackLogger;
                            stackLogger4.logDebug("ct.fromTag = " + fromTag);
                            StackLogger stackLogger5 = this.stackLogger;
                            stackLogger5.logDebug("thisToTag = " + thisToTag);
                            StackLogger stackLogger6 = this.stackLogger;
                            stackLogger6.logDebug("hisEvent = " + hisEvent);
                            StackLogger stackLogger7 = this.stackLogger;
                            stackLogger7.logDebug("eventHdr " + eventHdr);
                        }
                        if (fromTag.equalsIgnoreCase(thisToTag) && eventHdr.match(hisEvent) && notifyMessage.getCallId().getCallId().equalsIgnoreCase(ct.callId.getCallId())) {
                            if (ct.acquireSem()) {
                                retval = ct;
                            }
                            if (this.stackLogger.isLoggingEnabled()) {
                                StackLogger stackLogger8 = this.stackLogger;
                                stackLogger8.logDebug("findSubscribeTransaction : returning " + retval);
                            }
                            return retval;
                        }
                    } else {
                        continue;
                    }
                }
            }
            if (this.stackLogger.isLoggingEnabled()) {
                StackLogger stackLogger9 = this.stackLogger;
                stackLogger9.logDebug("findSubscribeTransaction : returning " + retval);
            }
            return retval;
        } finally {
            if (this.stackLogger.isLoggingEnabled()) {
                StackLogger stackLogger10 = this.stackLogger;
                stackLogger10.logDebug("findSubscribeTransaction : returning " + retval);
            }
        }
    }

    public void addTransactionPendingAck(SIPServerTransaction serverTransaction) {
        String branchId = ((SIPRequest) serverTransaction.getRequest()).getTopmostVia().getBranch();
        if (branchId != null) {
            this.terminatedServerTransactionsPendingAck.put(branchId, serverTransaction);
        }
    }

    public SIPServerTransaction findTransactionPendingAck(SIPRequest ackMessage) {
        return this.terminatedServerTransactionsPendingAck.get(ackMessage.getTopmostVia().getBranch());
    }

    public boolean removeTransactionPendingAck(SIPServerTransaction serverTransaction) {
        String branchId = ((SIPRequest) serverTransaction.getRequest()).getTopmostVia().getBranch();
        if (branchId == null || !this.terminatedServerTransactionsPendingAck.containsKey(branchId)) {
            return false;
        }
        this.terminatedServerTransactionsPendingAck.remove(branchId);
        return true;
    }

    public boolean isTransactionPendingAck(SIPServerTransaction serverTransaction) {
        return this.terminatedServerTransactionsPendingAck.contains(((SIPRequest) serverTransaction.getRequest()).getTopmostVia().getBranch());
    }

    public SIPTransaction findTransaction(SIPMessage sipMessage, boolean isServer) {
        SIPTransaction retval = null;
        if (isServer) {
            try {
                if (sipMessage.getTopmostVia().getBranch() != null) {
                    String key = sipMessage.getTransactionId();
                    retval = this.serverTransactionTable.get(key);
                    if (this.stackLogger.isLoggingEnabled()) {
                        StackLogger stackLogger2 = getStackLogger();
                        stackLogger2.logDebug("serverTx: looking for key " + key + " existing=" + this.serverTransactionTable);
                    }
                    if (key.startsWith(SIPConstants.BRANCH_MAGIC_COOKIE_LOWER_CASE)) {
                        return retval;
                    }
                }
                for (SIPServerTransaction sipServerTransaction : this.serverTransactionTable.values()) {
                    if (sipServerTransaction.isMessagePartOfTransaction(sipMessage)) {
                        if (getStackLogger().isLoggingEnabled()) {
                            StackLogger stackLogger3 = getStackLogger();
                            stackLogger3.logDebug("findTransaction: returning  : " + sipServerTransaction);
                        }
                        return sipServerTransaction;
                    }
                }
            } finally {
                if (getStackLogger().isLoggingEnabled()) {
                    StackLogger stackLogger4 = getStackLogger();
                    stackLogger4.logDebug("findTransaction: returning  : " + retval);
                }
            }
        } else {
            if (sipMessage.getTopmostVia().getBranch() != null) {
                String key2 = sipMessage.getTransactionId();
                if (this.stackLogger.isLoggingEnabled()) {
                    StackLogger stackLogger5 = getStackLogger();
                    stackLogger5.logDebug("clientTx: looking for key " + key2);
                }
                retval = this.clientTransactionTable.get(key2);
                if (key2.startsWith(SIPConstants.BRANCH_MAGIC_COOKIE_LOWER_CASE)) {
                    if (getStackLogger().isLoggingEnabled()) {
                        StackLogger stackLogger6 = getStackLogger();
                        stackLogger6.logDebug("findTransaction: returning  : " + retval);
                    }
                    return retval;
                }
            }
            for (SIPClientTransaction clientTransaction : this.clientTransactionTable.values()) {
                if (clientTransaction.isMessagePartOfTransaction(sipMessage)) {
                    if (getStackLogger().isLoggingEnabled()) {
                        StackLogger stackLogger7 = getStackLogger();
                        stackLogger7.logDebug("findTransaction: returning  : " + clientTransaction);
                    }
                    return clientTransaction;
                }
            }
        }
        if (getStackLogger().isLoggingEnabled()) {
            StackLogger stackLogger8 = getStackLogger();
            stackLogger8.logDebug("findTransaction: returning  : " + retval);
        }
        return retval;
    }

    public SIPTransaction findCancelTransaction(SIPRequest cancelRequest, boolean isServer) {
        if (this.stackLogger.isLoggingEnabled()) {
            StackLogger stackLogger2 = this.stackLogger;
            stackLogger2.logDebug("findCancelTransaction request= \n" + cancelRequest + "\nfindCancelRequest isServer=" + isServer);
        }
        if (isServer) {
            for (SIPServerTransaction sipServerTransaction : this.serverTransactionTable.values()) {
                if (sipServerTransaction.doesCancelMatchTransaction(cancelRequest)) {
                    return sipServerTransaction;
                }
            }
        } else {
            for (SIPClientTransaction sipClientTransaction : this.clientTransactionTable.values()) {
                if (sipClientTransaction.doesCancelMatchTransaction(cancelRequest)) {
                    return sipClientTransaction;
                }
            }
        }
        if (!this.stackLogger.isLoggingEnabled()) {
            return null;
        }
        this.stackLogger.logDebug("Could not find transaction for cancel request");
        return null;
    }

    protected SIPTransactionStack(StackMessageFactory messageFactory) {
        this();
        this.sipMessageFactory = messageFactory;
    }

    public SIPServerTransaction findPendingTransaction(SIPRequest requestReceived) {
        if (this.stackLogger.isLoggingEnabled()) {
            StackLogger stackLogger2 = this.stackLogger;
            stackLogger2.logDebug("looking for pending tx for :" + requestReceived.getTransactionId());
        }
        return this.pendingTransactions.get(requestReceived.getTransactionId());
    }

    public SIPServerTransaction findMergedTransaction(SIPRequest sipRequest) {
        if (!sipRequest.getMethod().equals("INVITE")) {
            return null;
        }
        String mergeId = sipRequest.getMergeId();
        SIPServerTransaction mergedTransaction = this.mergeTable.get(mergeId);
        if (mergeId == null) {
            return null;
        }
        if (!(mergedTransaction == null || mergedTransaction.isMessagePartOfTransaction(sipRequest))) {
            return mergedTransaction;
        }
        for (SIPDialog sipDialog : this.dialogTable.values()) {
            if (sipDialog.getFirstTransaction() != null && (sipDialog.getFirstTransaction() instanceof ServerTransaction)) {
                SIPRequest transactionRequest = ((SIPServerTransaction) sipDialog.getFirstTransaction()).getOriginalRequest();
                if (!((SIPServerTransaction) sipDialog.getFirstTransaction()).isMessagePartOfTransaction(sipRequest) && sipRequest.getMergeId().equals(transactionRequest.getMergeId())) {
                    return (SIPServerTransaction) sipDialog.getFirstTransaction();
                }
            }
        }
        return null;
    }

    public void removePendingTransaction(SIPServerTransaction tr) {
        if (this.stackLogger.isLoggingEnabled()) {
            StackLogger stackLogger2 = this.stackLogger;
            stackLogger2.logDebug("removePendingTx: " + tr.getTransactionId());
        }
        this.pendingTransactions.remove(tr.getTransactionId());
    }

    public void removeFromMergeTable(SIPServerTransaction tr) {
        if (this.stackLogger.isLoggingEnabled()) {
            this.stackLogger.logDebug("Removing tx from merge table ");
        }
        String key = ((SIPRequest) tr.getRequest()).getMergeId();
        if (key != null) {
            this.mergeTable.remove(key);
        }
    }

    public void putInMergeTable(SIPServerTransaction sipTransaction, SIPRequest sipRequest) {
        String mergeKey = sipRequest.getMergeId();
        if (mergeKey != null) {
            this.mergeTable.put(mergeKey, sipTransaction);
        }
    }

    public void mapTransaction(SIPServerTransaction transaction) {
        if (!transaction.isMapped) {
            addTransactionHash(transaction);
            transaction.isMapped = true;
        }
    }

    public ServerRequestInterface newSIPServerRequest(SIPRequest requestReceived, MessageChannel requestMessageChannel) {
        String key = requestReceived.getTransactionId();
        requestReceived.setMessageChannel(requestMessageChannel);
        SIPServerTransaction currentTransaction = this.serverTransactionTable.get(key);
        if (currentTransaction == null || !currentTransaction.isMessagePartOfTransaction(requestReceived)) {
            Iterator<SIPServerTransaction> transactionIterator = this.serverTransactionTable.values().iterator();
            currentTransaction = null;
            if (!key.toLowerCase().startsWith(SIPConstants.BRANCH_MAGIC_COOKIE_LOWER_CASE)) {
                while (transactionIterator.hasNext() && currentTransaction == null) {
                    SIPServerTransaction nextTransaction = transactionIterator.next();
                    if (nextTransaction.isMessagePartOfTransaction(requestReceived)) {
                        currentTransaction = nextTransaction;
                    }
                }
            }
            if (currentTransaction == null) {
                SIPServerTransaction currentTransaction2 = findPendingTransaction(requestReceived);
                if (currentTransaction2 != null) {
                    requestReceived.setTransaction(currentTransaction2);
                    if (currentTransaction2.acquireSem()) {
                        return currentTransaction2;
                    }
                    return null;
                }
                currentTransaction = createServerTransaction(requestMessageChannel);
                if (currentTransaction != null) {
                    currentTransaction.setOriginalRequest(requestReceived);
                    requestReceived.setTransaction(currentTransaction);
                }
            }
        }
        if (this.stackLogger.isLoggingEnabled()) {
            StackLogger stackLogger2 = this.stackLogger;
            stackLogger2.logDebug("newSIPServerRequest( " + requestReceived.getMethod() + Separators.COLON + requestReceived.getTopmostVia().getBranch() + "):" + currentTransaction);
        }
        if (currentTransaction != null) {
            currentTransaction.setRequestInterface(this.sipMessageFactory.newSIPServerRequest(requestReceived, currentTransaction));
        }
        if (currentTransaction != null && currentTransaction.acquireSem()) {
            return currentTransaction;
        }
        if (currentTransaction == null) {
            return null;
        }
        try {
            if (currentTransaction.isMessagePartOfTransaction(requestReceived) && currentTransaction.getMethod().equals(requestReceived.getMethod())) {
                SIPResponse trying = requestReceived.createResponse(100);
                trying.removeContent();
                currentTransaction.getMessageChannel().sendMessage(trying);
            }
        } catch (Exception e) {
            if (isLoggingEnabled()) {
                this.stackLogger.logError("Exception occured sending TRYING");
            }
        }
        return null;
    }

    public ServerResponseInterface newSIPServerResponse(SIPResponse responseReceived, MessageChannel responseMessageChannel) {
        String key = responseReceived.getTransactionId();
        SIPClientTransaction currentTransaction = this.clientTransactionTable.get(key);
        if (currentTransaction == null || (!currentTransaction.isMessagePartOfTransaction(responseReceived) && !key.startsWith(SIPConstants.BRANCH_MAGIC_COOKIE_LOWER_CASE))) {
            Iterator<SIPClientTransaction> transactionIterator = this.clientTransactionTable.values().iterator();
            currentTransaction = null;
            while (transactionIterator.hasNext() && currentTransaction == null) {
                SIPClientTransaction nextTransaction = transactionIterator.next();
                if (nextTransaction.isMessagePartOfTransaction(responseReceived)) {
                    currentTransaction = nextTransaction;
                }
            }
            if (currentTransaction == null) {
                if (this.stackLogger.isLoggingEnabled(16)) {
                    responseMessageChannel.logResponse(responseReceived, System.currentTimeMillis(), "before processing");
                }
                return this.sipMessageFactory.newSIPServerResponse(responseReceived, responseMessageChannel);
            }
        }
        boolean acquired = currentTransaction.acquireSem();
        if (this.stackLogger.isLoggingEnabled(16)) {
            currentTransaction.logResponse(responseReceived, System.currentTimeMillis(), "before processing");
        }
        if (acquired) {
            ServerResponseInterface sri = this.sipMessageFactory.newSIPServerResponse(responseReceived, currentTransaction);
            if (sri != null) {
                currentTransaction.setResponseInterface(sri);
            } else {
                if (this.stackLogger.isLoggingEnabled()) {
                    this.stackLogger.logDebug("returning null - serverResponseInterface is null!");
                }
                currentTransaction.releaseSem();
                return null;
            }
        } else if (this.stackLogger.isLoggingEnabled()) {
            this.stackLogger.logDebug("Could not aquire semaphore !!");
        }
        if (acquired) {
            return currentTransaction;
        }
        return null;
    }

    public MessageChannel createMessageChannel(SIPRequest request, MessageProcessor mp, Hop nextHop) throws IOException {
        Host targetHost = new Host();
        targetHost.setHostname(nextHop.getHost());
        HostPort targetHostPort = new HostPort();
        targetHostPort.setHost(targetHost);
        targetHostPort.setPort(nextHop.getPort());
        MessageChannel mc = mp.createMessageChannel(targetHostPort);
        if (mc == null) {
            return null;
        }
        SIPTransaction returnChannel = createClientTransaction(request, mc);
        ((SIPClientTransaction) returnChannel).setViaPort(nextHop.getPort());
        ((SIPClientTransaction) returnChannel).setViaHost(nextHop.getHost());
        addTransactionHash(returnChannel);
        return returnChannel;
    }

    public SIPClientTransaction createClientTransaction(SIPRequest sipRequest, MessageChannel encapsulatedMessageChannel) {
        SIPClientTransaction ct = new SIPClientTransaction(this, encapsulatedMessageChannel);
        ct.setOriginalRequest(sipRequest);
        return ct;
    }

    public SIPServerTransaction createServerTransaction(MessageChannel encapsulatedMessageChannel) {
        if (this.unlimitedServerTransactionTableSize) {
            return new SIPServerTransaction(this, encapsulatedMessageChannel);
        }
        int size = this.serverTransactionTable.size();
        int i = this.serverTransactionTableLowaterMark;
        if (Math.random() > 1.0d - ((double) (((float) (size - i)) / ((float) (this.serverTransactionTableHighwaterMark - i))))) {
            return null;
        }
        return new SIPServerTransaction(this, encapsulatedMessageChannel);
    }

    public int getClientTransactionTableSize() {
        return this.clientTransactionTable.size();
    }

    public int getServerTransactionTableSize() {
        return this.serverTransactionTable.size();
    }

    public void addTransaction(SIPClientTransaction clientTransaction) {
        if (this.stackLogger.isLoggingEnabled()) {
            StackLogger stackLogger2 = this.stackLogger;
            stackLogger2.logDebug("added transaction " + clientTransaction);
        }
        addTransactionHash(clientTransaction);
    }

    public void removeTransaction(SIPTransaction sipTransaction) {
        if (this.stackLogger.isLoggingEnabled()) {
            StackLogger stackLogger2 = this.stackLogger;
            stackLogger2.logDebug("Removing Transaction = " + sipTransaction.getTransactionId() + " transaction = " + sipTransaction);
        }
        if (sipTransaction instanceof SIPServerTransaction) {
            if (this.stackLogger.isLoggingEnabled()) {
                this.stackLogger.logStackTrace();
            }
            Object removed = this.serverTransactionTable.remove(sipTransaction.getTransactionId());
            String method = sipTransaction.getMethod();
            removePendingTransaction((SIPServerTransaction) sipTransaction);
            removeTransactionPendingAck((SIPServerTransaction) sipTransaction);
            if (method.equalsIgnoreCase("INVITE")) {
                removeFromMergeTable((SIPServerTransaction) sipTransaction);
            }
            SipProviderImpl sipProvider = sipTransaction.getSipProvider();
            if (removed != null && sipTransaction.testAndSetTransactionTerminatedEvent()) {
                sipProvider.handleEvent(new TransactionTerminatedEvent(sipProvider, (ServerTransaction) sipTransaction), sipTransaction);
                return;
            }
            return;
        }
        String key = sipTransaction.getTransactionId();
        Object removed2 = this.clientTransactionTable.remove(key);
        if (this.stackLogger.isLoggingEnabled()) {
            StackLogger stackLogger3 = this.stackLogger;
            stackLogger3.logDebug("REMOVED client tx " + removed2 + " KEY = " + key);
            if (removed2 != null) {
                SIPClientTransaction clientTx = (SIPClientTransaction) removed2;
                if (clientTx.getMethod().equals("INVITE") && this.maxForkTime != 0) {
                    this.timer.schedule(new RemoveForkedTransactionTimerTask(clientTx), (long) (this.maxForkTime * 1000));
                }
            }
        }
        if (removed2 != null && sipTransaction.testAndSetTransactionTerminatedEvent()) {
            SipProviderImpl sipProvider2 = sipTransaction.getSipProvider();
            sipProvider2.handleEvent(new TransactionTerminatedEvent(sipProvider2, (ClientTransaction) sipTransaction), sipTransaction);
        }
    }

    public void addTransaction(SIPServerTransaction serverTransaction) throws IOException {
        if (this.stackLogger.isLoggingEnabled()) {
            StackLogger stackLogger2 = this.stackLogger;
            stackLogger2.logDebug("added transaction " + serverTransaction);
        }
        serverTransaction.map();
        addTransactionHash(serverTransaction);
    }

    private void addTransactionHash(SIPTransaction sipTransaction) {
        SIPRequest sipRequest = sipTransaction.getOriginalRequest();
        if (sipTransaction instanceof SIPClientTransaction) {
            if (this.unlimitedClientTransactionTableSize) {
                this.activeClientTransactionCount.incrementAndGet();
            } else if (this.activeClientTransactionCount.get() > this.clientTransactionTableHiwaterMark) {
                try {
                    synchronized (this.clientTransactionTable) {
                        this.clientTransactionTable.wait();
                        this.activeClientTransactionCount.incrementAndGet();
                    }
                } catch (Exception ex) {
                    if (this.stackLogger.isLoggingEnabled()) {
                        this.stackLogger.logError("Exception occured while waiting for room", ex);
                    }
                }
            }
            String key = sipRequest.getTransactionId();
            this.clientTransactionTable.put(key, (SIPClientTransaction) sipTransaction);
            if (this.stackLogger.isLoggingEnabled()) {
                StackLogger stackLogger2 = this.stackLogger;
                stackLogger2.logDebug(" putTransactionHash :  key = " + key);
                return;
            }
            return;
        }
        String key2 = sipRequest.getTransactionId();
        if (this.stackLogger.isLoggingEnabled()) {
            StackLogger stackLogger3 = this.stackLogger;
            stackLogger3.logDebug(" putTransactionHash :  key = " + key2);
        }
        this.serverTransactionTable.put(key2, (SIPServerTransaction) sipTransaction);
    }

    /* access modifiers changed from: protected */
    public void decrementActiveClientTransactionCount() {
        if (this.activeClientTransactionCount.decrementAndGet() <= this.clientTransactionTableLowaterMark && !this.unlimitedClientTransactionTableSize) {
            synchronized (this.clientTransactionTable) {
                this.clientTransactionTable.notify();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void removeTransactionHash(SIPTransaction sipTransaction) {
        if (sipTransaction.getOriginalRequest() != null) {
            if (sipTransaction instanceof SIPClientTransaction) {
                String key = sipTransaction.getTransactionId();
                if (this.stackLogger.isLoggingEnabled()) {
                    this.stackLogger.logStackTrace();
                    StackLogger stackLogger2 = this.stackLogger;
                    stackLogger2.logDebug("removing client Tx : " + key);
                }
                this.clientTransactionTable.remove(key);
            } else if (sipTransaction instanceof SIPServerTransaction) {
                String key2 = sipTransaction.getTransactionId();
                this.serverTransactionTable.remove(key2);
                if (this.stackLogger.isLoggingEnabled()) {
                    StackLogger stackLogger3 = this.stackLogger;
                    stackLogger3.logDebug("removing server Tx : " + key2);
                }
            }
        }
    }

    @Override // gov.nist.javax.sip.stack.SIPTransactionEventListener
    public synchronized void transactionErrorEvent(SIPTransactionErrorEvent transactionErrorEvent) {
        SIPTransaction transaction = (SIPTransaction) transactionErrorEvent.getSource();
        if (transactionErrorEvent.getErrorID() == 2) {
            transaction.setState(SIPTransaction.TERMINATED_STATE);
            if (transaction instanceof SIPServerTransaction) {
                ((SIPServerTransaction) transaction).collectionTime = 0;
            }
            transaction.disableTimeoutTimer();
            transaction.disableRetransmissionTimer();
        }
    }

    @Override // gov.nist.javax.sip.stack.SIPDialogEventListener
    public synchronized void dialogErrorEvent(SIPDialogErrorEvent dialogErrorEvent) {
        SIPDialog sipDialog = (SIPDialog) dialogErrorEvent.getSource();
        SipListener sipListener = ((SipStackImpl) this).getSipListener();
        if (sipDialog != null && !(sipListener instanceof SipListenerExt)) {
            sipDialog.delete();
        }
    }

    public void stopStack() {
        MessageProcessor[] processorList;
        Timer timer2 = this.timer;
        if (timer2 != null) {
            timer2.cancel();
        }
        this.timer = null;
        this.pendingTransactions.clear();
        this.toExit = true;
        synchronized (this) {
            notifyAll();
        }
        synchronized (this.clientTransactionTable) {
            this.clientTransactionTable.notifyAll();
        }
        synchronized (this.messageProcessors) {
            for (MessageProcessor messageProcessor : getMessageProcessors()) {
                removeMessageProcessor(messageProcessor);
            }
            this.ioHandler.closeAll();
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        this.clientTransactionTable.clear();
        this.serverTransactionTable.clear();
        this.dialogTable.clear();
        this.serverLogger.closeLogFile();
    }

    public void putPendingTransaction(SIPServerTransaction tr) {
        if (this.stackLogger.isLoggingEnabled()) {
            StackLogger stackLogger2 = this.stackLogger;
            stackLogger2.logDebug("putPendingTransaction: " + tr);
        }
        this.pendingTransactions.put(tr.getTransactionId(), tr);
    }

    public NetworkLayer getNetworkLayer() {
        NetworkLayer networkLayer2 = this.networkLayer;
        if (networkLayer2 == null) {
            return DefaultNetworkLayer.SINGLETON;
        }
        return networkLayer2;
    }

    public boolean isLoggingEnabled() {
        StackLogger stackLogger2 = this.stackLogger;
        if (stackLogger2 == null) {
            return false;
        }
        return stackLogger2.isLoggingEnabled();
    }

    public StackLogger getStackLogger() {
        return this.stackLogger;
    }

    public ServerLogger getServerLogger() {
        return this.serverLogger;
    }

    public int getMaxMessageSize() {
        return this.maxMessageSize;
    }

    public void setSingleThreaded() {
        this.threadPoolSize = 1;
    }

    public void setThreadPoolSize(int size) {
        this.threadPoolSize = size;
    }

    public void setMaxConnections(int nconnections) {
        this.maxConnections = nconnections;
    }

    public Hop getNextHop(SIPRequest sipRequest) throws SipException {
        if (this.useRouterForAll) {
            Router router2 = this.router;
            if (router2 != null) {
                return router2.getNextHop(sipRequest);
            }
            return null;
        } else if (sipRequest.getRequestURI().isSipURI() || sipRequest.getRouteHeaders() != null) {
            return this.defaultRouter.getNextHop(sipRequest);
        } else {
            Router router3 = this.router;
            if (router3 != null) {
                return router3.getNextHop(sipRequest);
            }
            return null;
        }
    }

    public void setStackName(String stackName2) {
        this.stackName = stackName2;
    }

    /* access modifiers changed from: protected */
    public void setHostAddress(String stackAddress2) throws UnknownHostException {
        if (stackAddress2.indexOf(58) == stackAddress2.lastIndexOf(58) || stackAddress2.trim().charAt(0) == '[') {
            this.stackAddress = stackAddress2;
        } else {
            this.stackAddress = '[' + stackAddress2 + ']';
        }
        this.stackInetAddress = InetAddress.getByName(stackAddress2);
    }

    public String getHostAddress() {
        return this.stackAddress;
    }

    /* access modifiers changed from: protected */
    public void setRouter(Router router2) {
        this.router = router2;
    }

    public Router getRouter(SIPRequest request) {
        if (request.getRequestLine() == null) {
            return this.defaultRouter;
        }
        if (this.useRouterForAll) {
            return this.router;
        }
        if (request.getRequestURI().getScheme().equals("sip") || request.getRequestURI().getScheme().equals("sips")) {
            return this.defaultRouter;
        }
        Router router2 = this.router;
        if (router2 != null) {
            return router2;
        }
        return this.defaultRouter;
    }

    public Router getRouter() {
        return this.router;
    }

    public boolean isAlive() {
        return !this.toExit;
    }

    /* access modifiers changed from: protected */
    public void addMessageProcessor(MessageProcessor newMessageProcessor) throws IOException {
        synchronized (this.messageProcessors) {
            this.messageProcessors.add(newMessageProcessor);
        }
    }

    /* access modifiers changed from: protected */
    public void removeMessageProcessor(MessageProcessor oldMessageProcessor) {
        synchronized (this.messageProcessors) {
            if (this.messageProcessors.remove(oldMessageProcessor)) {
                oldMessageProcessor.stop();
            }
        }
    }

    /* access modifiers changed from: protected */
    public MessageProcessor[] getMessageProcessors() {
        MessageProcessor[] messageProcessorArr;
        synchronized (this.messageProcessors) {
            messageProcessorArr = (MessageProcessor[]) this.messageProcessors.toArray(new MessageProcessor[0]);
        }
        return messageProcessorArr;
    }

    /* access modifiers changed from: protected */
    public MessageProcessor createMessageProcessor(InetAddress ipAddress, int port, String transport) throws IOException {
        if (transport.equalsIgnoreCase(ParameterNames.UDP)) {
            UDPMessageProcessor udpMessageProcessor = new UDPMessageProcessor(ipAddress, this, port);
            addMessageProcessor(udpMessageProcessor);
            this.udpFlag = true;
            return udpMessageProcessor;
        } else if (transport.equalsIgnoreCase(ParameterNames.TCP)) {
            TCPMessageProcessor tcpMessageProcessor = new TCPMessageProcessor(ipAddress, this, port);
            addMessageProcessor(tcpMessageProcessor);
            return tcpMessageProcessor;
        } else if (transport.equalsIgnoreCase(ParameterNames.TLS)) {
            TLSMessageProcessor tlsMessageProcessor = new TLSMessageProcessor(ipAddress, this, port);
            addMessageProcessor(tlsMessageProcessor);
            return tlsMessageProcessor;
        } else if (transport.equalsIgnoreCase("sctp")) {
            try {
                MessageProcessor mp = (MessageProcessor) ClassLoader.getSystemClassLoader().loadClass("gov.nist.javax.sip.stack.sctp.SCTPMessageProcessor").newInstance();
                mp.initialize(ipAddress, port, this);
                addMessageProcessor(mp);
                return mp;
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("SCTP not supported (needs Java 7 and SCTP jar in classpath)");
            } catch (InstantiationException ie) {
                throw new IllegalArgumentException("Error initializing SCTP", ie);
            } catch (IllegalAccessException ie2) {
                throw new IllegalArgumentException("Error initializing SCTP", ie2);
            }
        } else {
            throw new IllegalArgumentException("bad transport");
        }
    }

    /* access modifiers changed from: protected */
    public void setMessageFactory(StackMessageFactory messageFactory) {
        this.sipMessageFactory = messageFactory;
    }

    public MessageChannel createRawMessageChannel(String sourceIpAddress, int sourcePort, Hop nextHop) throws UnknownHostException {
        Host targetHost = new Host();
        targetHost.setHostname(nextHop.getHost());
        HostPort targetHostPort = new HostPort();
        targetHostPort.setHost(targetHost);
        targetHostPort.setPort(nextHop.getPort());
        MessageChannel newChannel = null;
        Iterator processorIterator = this.messageProcessors.iterator();
        while (processorIterator.hasNext() && newChannel == null) {
            MessageProcessor nextProcessor = processorIterator.next();
            if (nextHop.getTransport().equalsIgnoreCase(nextProcessor.getTransport()) && sourceIpAddress.equals(nextProcessor.getIpAddress().getHostAddress()) && sourcePort == nextProcessor.getPort()) {
                try {
                    newChannel = nextProcessor.createMessageChannel(targetHostPort);
                } catch (UnknownHostException ex) {
                    if (this.stackLogger.isLoggingEnabled()) {
                        this.stackLogger.logException(ex);
                    }
                    throw ex;
                } catch (IOException e) {
                    if (this.stackLogger.isLoggingEnabled()) {
                        this.stackLogger.logException(e);
                    }
                }
            }
        }
        return newChannel;
    }

    public boolean isEventForked(String ename) {
        if (this.stackLogger.isLoggingEnabled()) {
            StackLogger stackLogger2 = this.stackLogger;
            stackLogger2.logDebug("isEventForked: " + ename + " returning " + this.forkedEvents.contains(ename));
        }
        return this.forkedEvents.contains(ename);
    }

    public AddressResolver getAddressResolver() {
        return this.addressResolver;
    }

    public void setAddressResolver(AddressResolver addressResolver2) {
        this.addressResolver = addressResolver2;
    }

    public void setLogRecordFactory(LogRecordFactory logRecordFactory2) {
        this.logRecordFactory = logRecordFactory2;
    }

    public ThreadAuditor getThreadAuditor() {
        return this.threadAuditor;
    }

    public String auditStack(Set activeCallIDs, long leakedDialogTimer, long leakedTransactionTimer) {
        String leakedDialogs = auditDialogs(activeCallIDs, leakedDialogTimer);
        String leakedServerTransactions = auditTransactions(this.serverTransactionTable, leakedTransactionTimer);
        String leakedClientTransactions = auditTransactions(this.clientTransactionTable, leakedTransactionTimer);
        if (leakedDialogs == null && leakedServerTransactions == null && leakedClientTransactions == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("SIP Stack Audit:\n");
        String str = "";
        sb.append(leakedDialogs != null ? leakedDialogs : str);
        sb.append(leakedServerTransactions != null ? leakedServerTransactions : str);
        if (leakedClientTransactions != null) {
            str = leakedClientTransactions;
        }
        sb.append(str);
        return sb.toString();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00ec, code lost:
        r0 = th;
     */
    private String auditDialogs(Set activeCallIDs, long leakedDialogTimer) {
        LinkedList dialogs;
        String auditReport = "  Leaked dialogs:\n";
        int leakedDialogs = 0;
        long currentTime = System.currentTimeMillis();
        synchronized (this.dialogTable) {
            dialogs = new LinkedList(this.dialogTable.values());
        }
        Iterator it = dialogs.iterator();
        while (it.hasNext()) {
            SIPDialog itDialog = (SIPDialog) it.next();
            String callID = null;
            CallIdHeader callIdHeader = itDialog != null ? itDialog.getCallId() : null;
            if (callIdHeader != null) {
                callID = callIdHeader.getCallId();
            }
            if (itDialog != null && callID != null) {
                if (!activeCallIDs.contains(callID)) {
                    if (itDialog.auditTag == 0) {
                        itDialog.auditTag = currentTime;
                    } else if (currentTime - itDialog.auditTag >= leakedDialogTimer) {
                        leakedDialogs++;
                        DialogState dialogState = itDialog.getState();
                        StringBuilder sb = new StringBuilder();
                        sb.append("dialog id: ");
                        sb.append(itDialog.getDialogId());
                        sb.append(", dialog state: ");
                        sb.append(dialogState != null ? dialogState.toString() : "null");
                        String dialogReport = sb.toString();
                        auditReport = auditReport + "    " + dialogReport + Separators.RETURN;
                        itDialog.setState(SIPDialog.TERMINATED_STATE);
                        if (this.stackLogger.isLoggingEnabled()) {
                            this.stackLogger.logDebug("auditDialogs: leaked " + dialogReport);
                        }
                    }
                }
            }
        }
        if (leakedDialogs <= 0) {
            return null;
        }
        return auditReport + "    Total: " + Integer.toString(leakedDialogs) + " leaked dialogs detected and removed.\n";
        while (true) {
        }
    }

    private String auditTransactions(ConcurrentHashMap transactionsMap, long a_nLeakedTransactionTimer) {
        String origRequestMethod;
        String str;
        String auditReport = "  Leaked transactions:\n";
        int leakedTransactions = 0;
        long currentTime = System.currentTimeMillis();
        Iterator it = new LinkedList(transactionsMap.values()).iterator();
        while (it.hasNext()) {
            SIPTransaction sipTransaction = (SIPTransaction) it.next();
            if (sipTransaction != null) {
                if (sipTransaction.auditTag == 0) {
                    sipTransaction.auditTag = currentTime;
                } else if (currentTime - sipTransaction.auditTag >= a_nLeakedTransactionTimer) {
                    leakedTransactions++;
                    TransactionState transactionState = sipTransaction.getState();
                    SIPRequest origRequest = sipTransaction.getOriginalRequest();
                    if (origRequest != null) {
                        origRequestMethod = origRequest.getMethod();
                    } else {
                        origRequestMethod = null;
                    }
                    StringBuilder sb = new StringBuilder();
                    sb.append(sipTransaction.getClass().getName());
                    sb.append(", state: ");
                    String str2 = "null";
                    if (transactionState != null) {
                        str = transactionState.toString();
                    } else {
                        str = str2;
                    }
                    sb.append(str);
                    sb.append(", OR: ");
                    if (origRequestMethod != null) {
                        str2 = origRequestMethod;
                    }
                    sb.append(str2);
                    String transactionReport = sb.toString();
                    auditReport = auditReport + "    " + transactionReport + Separators.RETURN;
                    removeTransaction(sipTransaction);
                    if (isLoggingEnabled()) {
                        this.stackLogger.logDebug("auditTransactions: leaked " + transactionReport);
                    }
                }
            }
        }
        if (leakedTransactions <= 0) {
            return null;
        }
        return auditReport + "    Total: " + Integer.toString(leakedTransactions) + " leaked transactions detected and removed.\n";
    }

    public void setNon2XXAckPassedToListener(boolean passToListener) {
        this.non2XXAckPassedToListener = passToListener;
    }

    public boolean isNon2XXAckPassedToListener() {
        return this.non2XXAckPassedToListener;
    }

    public int getActiveClientTransactionCount() {
        return this.activeClientTransactionCount.get();
    }

    public boolean isRfc2543Supported() {
        return this.rfc2543Supported;
    }

    public boolean isCancelClientTransactionChecked() {
        return this.cancelClientTransactionChecked;
    }

    public boolean isRemoteTagReassignmentAllowed() {
        return this.remoteTagReassignmentAllowed;
    }

    public Collection<Dialog> getDialogs() {
        HashSet<Dialog> dialogs = new HashSet<>();
        dialogs.addAll(this.dialogTable.values());
        dialogs.addAll(this.earlyDialogTable.values());
        return dialogs;
    }

    public Collection<Dialog> getDialogs(DialogState state) {
        HashSet<Dialog> matchingDialogs = new HashSet<>();
        if (DialogState.EARLY.equals(state)) {
            matchingDialogs.addAll(this.earlyDialogTable.values());
        } else {
            for (SIPDialog dialog : this.dialogTable.values()) {
                if (dialog.getState() != null && dialog.getState().equals(state)) {
                    matchingDialogs.add(dialog);
                }
            }
        }
        return matchingDialogs;
    }

    public Dialog getReplacesDialog(ReplacesHeader replacesHeader) {
        String cid = replacesHeader.getCallId();
        String fromTag = replacesHeader.getFromTag();
        String toTag = replacesHeader.getToTag();
        StringBuffer dialogId = new StringBuffer(cid);
        if (toTag != null) {
            dialogId.append(Separators.COLON);
            dialogId.append(toTag);
        }
        if (fromTag != null) {
            dialogId.append(Separators.COLON);
            dialogId.append(fromTag);
        }
        String did = dialogId.toString().toLowerCase();
        if (this.stackLogger.isLoggingEnabled()) {
            StackLogger stackLogger2 = this.stackLogger;
            stackLogger2.logDebug("Looking for dialog " + did);
        }
        Dialog replacesDialog = this.dialogTable.get(did);
        if (replacesDialog != null) {
            return replacesDialog;
        }
        for (SIPClientTransaction ctx : this.clientTransactionTable.values()) {
            if (ctx.getDialog(did) != null) {
                return ctx.getDialog(did);
            }
        }
        return replacesDialog;
    }

    public Dialog getJoinDialog(JoinHeader joinHeader) {
        String cid = joinHeader.getCallId();
        String fromTag = joinHeader.getFromTag();
        String toTag = joinHeader.getToTag();
        StringBuffer retval = new StringBuffer(cid);
        if (toTag != null) {
            retval.append(Separators.COLON);
            retval.append(toTag);
        }
        if (fromTag != null) {
            retval.append(Separators.COLON);
            retval.append(fromTag);
        }
        return this.dialogTable.get(retval.toString().toLowerCase());
    }

    public void setTimer(Timer timer2) {
        this.timer = timer2;
    }

    public Timer getTimer() {
        return this.timer;
    }

    public int getReceiveUdpBufferSize() {
        return this.receiveUdpBufferSize;
    }

    public void setReceiveUdpBufferSize(int receiveUdpBufferSize2) {
        this.receiveUdpBufferSize = receiveUdpBufferSize2;
    }

    public int getSendUdpBufferSize() {
        return this.sendUdpBufferSize;
    }

    public void setSendUdpBufferSize(int sendUdpBufferSize2) {
        this.sendUdpBufferSize = sendUdpBufferSize2;
    }

    public void setStackLogger(StackLogger stackLogger2) {
        this.stackLogger = stackLogger2;
    }

    public boolean checkBranchId() {
        return this.checkBranchId;
    }

    public void setLogStackTraceOnMessageSend(boolean logStackTraceOnMessageSend2) {
        this.logStackTraceOnMessageSend = logStackTraceOnMessageSend2;
    }

    public boolean isLogStackTraceOnMessageSend() {
        return this.logStackTraceOnMessageSend;
    }

    public void setDeliverDialogTerminatedEventForNullDialog() {
        this.isDialogTerminatedEventDeliveredForNullDialog = true;
    }

    public void addForkedClientTransaction(SIPClientTransaction clientTransaction) {
        this.forkedClientTransactionTable.put(clientTransaction.getTransactionId(), clientTransaction);
    }

    public SIPClientTransaction getForkedTransaction(String transactionId) {
        return this.forkedClientTransactionTable.get(transactionId);
    }
}
