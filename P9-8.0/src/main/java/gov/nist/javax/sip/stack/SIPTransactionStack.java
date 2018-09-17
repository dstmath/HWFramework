package gov.nist.javax.sip.stack;

import gov.nist.core.Host;
import gov.nist.core.HostPort;
import gov.nist.core.Separators;
import gov.nist.core.ServerLogger;
import gov.nist.core.StackLogger;
import gov.nist.core.ThreadAuditor;
import gov.nist.core.ThreadAuditor.ThreadHandle;
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

    class PingTimer extends SIPStackTimerTask {
        ThreadHandle threadHandle;

        public PingTimer(ThreadHandle a_oThreadHandle) {
            this.threadHandle = a_oThreadHandle;
        }

        protected void runTask() {
            if (SIPTransactionStack.this.getTimer() != null) {
                if (this.threadHandle == null) {
                    this.threadHandle = SIPTransactionStack.this.getThreadAuditor().addCurrentThread();
                }
                this.threadHandle.ping();
                SIPTransactionStack.this.getTimer().schedule(new PingTimer(this.threadHandle), this.threadHandle.getPingIntervalInMillisecs());
            }
        }
    }

    class RemoveForkedTransactionTimerTask extends SIPStackTimerTask {
        private SIPClientTransaction clientTransaction;

        public RemoveForkedTransactionTimerTask(SIPClientTransaction sipClientTransaction) {
            this.clientTransaction = sipClientTransaction;
        }

        protected void runTask() {
            SIPTransactionStack.this.forkedClientTransactionTable.remove(this.clientTransaction.getTransactionId());
        }
    }

    static {
        dialogCreatingMethods.add(Request.REFER);
        dialogCreatingMethods.add("INVITE");
        dialogCreatingMethods.add("SUBSCRIBE");
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
        this.forkedEvents = new HashSet();
        this.threadPoolSize = -1;
        this.cacheServerConnections = true;
        this.cacheClientConnections = true;
        this.maxConnections = -1;
        this.messageProcessors = new ArrayList();
        this.ioHandler = new IOHandler(this);
        this.readTimeout = -1;
        this.maxListenerResponseTime = -1;
        this.addressResolver = new DefaultAddressResolver();
        this.dialogTable = new ConcurrentHashMap();
        this.earlyDialogTable = new ConcurrentHashMap();
        this.clientTransactionTable = new ConcurrentHashMap();
        this.serverTransactionTable = new ConcurrentHashMap();
        this.terminatedServerTransactionsPendingAck = new ConcurrentHashMap();
        this.mergeTable = new ConcurrentHashMap();
        this.retransmissionAlertTransactions = new ConcurrentHashMap();
        this.timer = new Timer();
        this.pendingTransactions = new ConcurrentHashMap();
        this.forkedClientTransactionTable = new ConcurrentHashMap();
        if (getThreadAuditor().isEnabled()) {
            this.timer.schedule(new PingTimer(null), 0);
        }
    }

    protected void reInit() {
        if (this.stackLogger.isLoggingEnabled()) {
            this.stackLogger.logDebug("Re-initializing !");
        }
        this.messageProcessors = new ArrayList();
        this.ioHandler = new IOHandler(this);
        this.pendingTransactions = new ConcurrentHashMap();
        this.clientTransactionTable = new ConcurrentHashMap();
        this.serverTransactionTable = new ConcurrentHashMap();
        this.retransmissionAlertTransactions = new ConcurrentHashMap();
        this.mergeTable = new ConcurrentHashMap();
        this.dialogTable = new ConcurrentHashMap();
        this.earlyDialogTable = new ConcurrentHashMap();
        this.terminatedServerTransactionsPendingAck = new ConcurrentHashMap();
        this.forkedClientTransactionTable = new ConcurrentHashMap();
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
            getStackLogger().logDebug("dialog table  = " + this.dialogTable);
            System.out.println("dialog table = " + this.dialogTable);
        }
    }

    public SIPServerTransaction getRetransmissionAlertTransaction(String dialogId) {
        return (SIPServerTransaction) this.retransmissionAlertTransactions.get(dialogId);
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
        if (this.dialogTable.containsKey(dialogId)) {
            if (this.stackLogger.isLoggingEnabled()) {
                this.stackLogger.logDebug("putDialog: dialog already exists" + dialogId + " in table = " + this.dialogTable.get(dialogId));
            }
            return;
        }
        if (this.stackLogger.isLoggingEnabled()) {
            this.stackLogger.logDebug("putDialog dialogId=" + dialogId + " dialog = " + dialog);
        }
        dialog.setStack(this);
        if (this.stackLogger.isLoggingEnabled()) {
            this.stackLogger.logStackTrace();
        }
        this.dialogTable.put(dialogId, dialog);
    }

    public SIPDialog createDialog(SIPTransaction transaction) {
        if (!(transaction instanceof SIPClientTransaction)) {
            return new SIPDialog(transaction);
        }
        String dialogId = ((SIPRequest) transaction.getRequest()).getDialogId(false);
        SIPDialog retval;
        if (this.earlyDialogTable.get(dialogId) != null) {
            SIPDialog dialog = (SIPDialog) this.earlyDialogTable.get(dialogId);
            if (dialog.getState() == null || dialog.getState() == DialogState.EARLY) {
                return dialog;
            }
            retval = new SIPDialog(transaction);
            this.earlyDialogTable.put(dialogId, retval);
            return retval;
        }
        retval = new SIPDialog(transaction);
        this.earlyDialogTable.put(dialogId, retval);
        return retval;
    }

    public SIPDialog createDialog(SIPClientTransaction transaction, SIPResponse sipResponse) {
        String dialogId = ((SIPRequest) transaction.getRequest()).getDialogId(false);
        if (this.earlyDialogTable.get(dialogId) == null) {
            return new SIPDialog(transaction, sipResponse);
        }
        SIPDialog retval = (SIPDialog) this.earlyDialogTable.get(dialogId);
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
        SIPDialog sipDialog = (SIPDialog) this.dialogTable.get(dialogId);
        if (this.stackLogger.isLoggingEnabled()) {
            this.stackLogger.logDebug("getDialog(" + dialogId + ") : returning " + sipDialog);
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
        SIPClientTransaction sIPClientTransaction = null;
        SIPClientTransaction retval = null;
        try {
            if (this.stackLogger.isLoggingEnabled()) {
                this.stackLogger.logDebug("ct table size = " + this.clientTransactionTable.size());
            }
            String thisToTag = notifyMessage.getTo().getTag();
            if (thisToTag == null) {
                return sIPClientTransaction;
            }
            Event eventHdr = (Event) notifyMessage.getHeader("Event");
            if (eventHdr == null) {
                if (this.stackLogger.isLoggingEnabled()) {
                    this.stackLogger.logDebug("event Header is null -- returning null");
                }
                if (this.stackLogger.isLoggingEnabled()) {
                    this.stackLogger.logDebug("findSubscribeTransaction : returning " + null);
                }
                return null;
            }
            for (SIPClientTransaction ct : this.clientTransactionTable.values()) {
                if (ct.getMethod().equals("SUBSCRIBE")) {
                    String fromTag = ct.from.getTag();
                    Event hisEvent = ct.event;
                    if (hisEvent == null) {
                        continue;
                    } else {
                        if (this.stackLogger.isLoggingEnabled()) {
                            this.stackLogger.logDebug("ct.fromTag = " + fromTag);
                            this.stackLogger.logDebug("thisToTag = " + thisToTag);
                            this.stackLogger.logDebug("hisEvent = " + hisEvent);
                            this.stackLogger.logDebug("eventHdr " + eventHdr);
                        }
                        if (fromTag.equalsIgnoreCase(thisToTag) && hisEvent != null && eventHdr.match(hisEvent) && notifyMessage.getCallId().getCallId().equalsIgnoreCase(ct.callId.getCallId())) {
                            if (ct.acquireSem()) {
                                retval = ct;
                            }
                            if (this.stackLogger.isLoggingEnabled()) {
                                this.stackLogger.logDebug("findSubscribeTransaction : returning " + retval);
                            }
                            return retval;
                        }
                    }
                }
            }
            if (this.stackLogger.isLoggingEnabled()) {
                this.stackLogger.logDebug("findSubscribeTransaction : returning " + null);
            }
            return null;
        } finally {
            if (this.stackLogger.isLoggingEnabled()) {
                sIPClientTransaction = "findSubscribeTransaction : returning ";
                this.stackLogger.logDebug(sIPClientTransaction + null);
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
        return (SIPServerTransaction) this.terminatedServerTransactionsPendingAck.get(ackMessage.getTopmostVia().getBranch());
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

    /* JADX WARNING: Removed duplicated region for block: B:45:0x0174  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public SIPTransaction findTransaction(SIPMessage sipMessage, boolean isServer) {
        SIPTransaction retval = null;
        String key;
        if (isServer) {
            try {
                if (sipMessage.getTopmostVia().getBranch() != null) {
                    key = sipMessage.getTransactionId();
                    retval = (SIPTransaction) this.serverTransactionTable.get(key);
                    if (this.stackLogger.isLoggingEnabled()) {
                        getStackLogger().logDebug("serverTx: looking for key " + key + " existing=" + this.serverTransactionTable);
                    }
                    if (key.startsWith(SIPConstants.BRANCH_MAGIC_COOKIE_LOWER_CASE)) {
                    }
                }
                for (SIPTransaction sipServerTransaction : this.serverTransactionTable.values()) {
                    if (sipServerTransaction.isMessagePartOfTransaction(sipMessage)) {
                        retval = sipServerTransaction;
                        if (getStackLogger().isLoggingEnabled()) {
                            getStackLogger().logDebug("findTransaction: returning  : " + sipServerTransaction);
                        }
                        return sipServerTransaction;
                    }
                }
                if (getStackLogger().isLoggingEnabled()) {
                    getStackLogger().logDebug("findTransaction: returning  : " + retval);
                }
                return retval;
            } finally {
                if (getStackLogger().isLoggingEnabled()) {
                    getStackLogger().logDebug("findTransaction: returning  : " + retval);
                }
            }
        } else {
            if (sipMessage.getTopmostVia().getBranch() != null) {
                key = sipMessage.getTransactionId();
                if (this.stackLogger.isLoggingEnabled()) {
                    getStackLogger().logDebug("clientTx: looking for key " + key);
                }
                retval = (SIPTransaction) this.clientTransactionTable.get(key);
                if (key.startsWith(SIPConstants.BRANCH_MAGIC_COOKIE_LOWER_CASE)) {
                    if (getStackLogger().isLoggingEnabled()) {
                        getStackLogger().logDebug("findTransaction: returning  : " + retval);
                    }
                    return retval;
                }
            }
            for (SIPClientTransaction clientTransaction : this.clientTransactionTable.values()) {
                if (clientTransaction.isMessagePartOfTransaction(sipMessage)) {
                    SIPClientTransaction sIPClientTransaction = clientTransaction;
                    if (getStackLogger().isLoggingEnabled()) {
                        getStackLogger().logDebug("findTransaction: returning  : " + clientTransaction);
                    }
                    return clientTransaction;
                }
            }
            if (getStackLogger().isLoggingEnabled()) {
            }
            return retval;
        }
        return retval;
    }

    public SIPTransaction findCancelTransaction(SIPRequest cancelRequest, boolean isServer) {
        if (this.stackLogger.isLoggingEnabled()) {
            this.stackLogger.logDebug("findCancelTransaction request= \n" + cancelRequest + "\nfindCancelRequest isServer=" + isServer);
        }
        if (isServer) {
            for (SIPTransaction transaction : this.serverTransactionTable.values()) {
                SIPServerTransaction sipServerTransaction = (SIPServerTransaction) transaction;
                if (sipServerTransaction.doesCancelMatchTransaction(cancelRequest)) {
                    return sipServerTransaction;
                }
            }
        }
        for (SIPTransaction transaction2 : this.clientTransactionTable.values()) {
            SIPClientTransaction sipClientTransaction = (SIPClientTransaction) transaction2;
            if (sipClientTransaction.doesCancelMatchTransaction(cancelRequest)) {
                return sipClientTransaction;
            }
        }
        if (this.stackLogger.isLoggingEnabled()) {
            this.stackLogger.logDebug("Could not find transaction for cancel request");
        }
        return null;
    }

    protected SIPTransactionStack(StackMessageFactory messageFactory) {
        this();
        this.sipMessageFactory = messageFactory;
    }

    public SIPServerTransaction findPendingTransaction(SIPRequest requestReceived) {
        if (this.stackLogger.isLoggingEnabled()) {
            this.stackLogger.logDebug("looking for pending tx for :" + requestReceived.getTransactionId());
        }
        return (SIPServerTransaction) this.pendingTransactions.get(requestReceived.getTransactionId());
    }

    public SIPServerTransaction findMergedTransaction(SIPRequest sipRequest) {
        if (!sipRequest.getMethod().equals("INVITE")) {
            return null;
        }
        String mergeId = sipRequest.getMergeId();
        SIPServerTransaction mergedTransaction = (SIPServerTransaction) this.mergeTable.get(mergeId);
        if (mergeId == null) {
            return null;
        }
        if (mergedTransaction != null && (mergedTransaction.isMessagePartOfTransaction(sipRequest) ^ 1) != 0) {
            return mergedTransaction;
        }
        for (SIPDialog sipDialog : this.dialogTable.values()) {
            if (sipDialog.getFirstTransaction() != null && (sipDialog.getFirstTransaction() instanceof ServerTransaction)) {
                SIPServerTransaction serverTransaction = (SIPServerTransaction) sipDialog.getFirstTransaction();
                SIPRequest transactionRequest = ((SIPServerTransaction) sipDialog.getFirstTransaction()).getOriginalRequest();
                if (!serverTransaction.isMessagePartOfTransaction(sipRequest) && sipRequest.getMergeId().equals(transactionRequest.getMergeId())) {
                    return (SIPServerTransaction) sipDialog.getFirstTransaction();
                }
            }
        }
        return null;
    }

    public void removePendingTransaction(SIPServerTransaction tr) {
        if (this.stackLogger.isLoggingEnabled()) {
            this.stackLogger.logDebug("removePendingTx: " + tr.getTransactionId());
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
        SIPServerTransaction currentTransaction = (SIPServerTransaction) this.serverTransactionTable.get(key);
        if (currentTransaction == null || (currentTransaction.isMessagePartOfTransaction(requestReceived) ^ 1) != 0) {
            Iterator<SIPServerTransaction> transactionIterator = this.serverTransactionTable.values().iterator();
            currentTransaction = null;
            if (!key.toLowerCase().startsWith(SIPConstants.BRANCH_MAGIC_COOKIE_LOWER_CASE)) {
                while (transactionIterator.hasNext() && currentTransaction == null) {
                    SIPServerTransaction nextTransaction = (SIPServerTransaction) transactionIterator.next();
                    if (nextTransaction.isMessagePartOfTransaction(requestReceived)) {
                        currentTransaction = nextTransaction;
                    }
                }
            }
            if (currentTransaction == null) {
                currentTransaction = findPendingTransaction(requestReceived);
                if (currentTransaction != null) {
                    requestReceived.setTransaction(currentTransaction);
                    if (currentTransaction == null || !currentTransaction.acquireSem()) {
                        return null;
                    }
                    return currentTransaction;
                }
                currentTransaction = createServerTransaction(requestMessageChannel);
                if (currentTransaction != null) {
                    currentTransaction.setOriginalRequest(requestReceived);
                    requestReceived.setTransaction(currentTransaction);
                }
            }
        }
        if (this.stackLogger.isLoggingEnabled()) {
            this.stackLogger.logDebug("newSIPServerRequest( " + requestReceived.getMethod() + Separators.COLON + requestReceived.getTopmostVia().getBranch() + "):" + currentTransaction);
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
        Object currentTransaction = (SIPClientTransaction) this.clientTransactionTable.get(key);
        if (currentTransaction == null || !(currentTransaction.isMessagePartOfTransaction(responseReceived) || (key.startsWith(SIPConstants.BRANCH_MAGIC_COOKIE_LOWER_CASE) ^ 1) == 0)) {
            Iterator<SIPClientTransaction> transactionIterator = this.clientTransactionTable.values().iterator();
            currentTransaction = null;
            while (transactionIterator.hasNext() && currentTransaction == null) {
                SIPClientTransaction nextTransaction = (SIPClientTransaction) transactionIterator.next();
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
        if (Math.random() > 1.0d - ((double) (((float) (this.serverTransactionTable.size() - this.serverTransactionTableLowaterMark)) / ((float) (this.serverTransactionTableHighwaterMark - this.serverTransactionTableLowaterMark))))) {
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
            this.stackLogger.logDebug("added transaction " + clientTransaction);
        }
        addTransactionHash(clientTransaction);
    }

    public void removeTransaction(SIPTransaction sipTransaction) {
        if (this.stackLogger.isLoggingEnabled()) {
            this.stackLogger.logDebug("Removing Transaction = " + sipTransaction.getTransactionId() + " transaction = " + sipTransaction);
        }
        Object sipProvider;
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
            sipProvider = sipTransaction.getSipProvider();
            if (removed != null && sipTransaction.testAndSetTransactionTerminatedEvent()) {
                sipProvider.handleEvent(new TransactionTerminatedEvent(sipProvider, (ServerTransaction) sipTransaction), sipTransaction);
                return;
            }
            return;
        }
        String key = sipTransaction.getTransactionId();
        SIPClientTransaction removed2 = this.clientTransactionTable.remove(key);
        if (this.stackLogger.isLoggingEnabled()) {
            this.stackLogger.logDebug("REMOVED client tx " + removed2 + " KEY = " + key);
            if (removed2 != null) {
                SIPClientTransaction clientTx = removed2;
                if (clientTx.getMethod().equals("INVITE") && this.maxForkTime != 0) {
                    this.timer.schedule(new RemoveForkedTransactionTimerTask(clientTx), (long) (this.maxForkTime * 1000));
                }
            }
        }
        if (removed2 != null && sipTransaction.testAndSetTransactionTerminatedEvent()) {
            sipProvider = sipTransaction.getSipProvider();
            sipProvider.handleEvent(new TransactionTerminatedEvent(sipProvider, (ClientTransaction) sipTransaction), sipTransaction);
        }
    }

    public void addTransaction(SIPServerTransaction serverTransaction) throws IOException {
        if (this.stackLogger.isLoggingEnabled()) {
            this.stackLogger.logDebug("added transaction " + serverTransaction);
        }
        serverTransaction.map();
        addTransactionHash(serverTransaction);
    }

    private void addTransactionHash(SIPTransaction sipTransaction) {
        SIPRequest sipRequest = sipTransaction.getOriginalRequest();
        String key;
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
            key = sipRequest.getTransactionId();
            this.clientTransactionTable.put(key, (SIPClientTransaction) sipTransaction);
            if (this.stackLogger.isLoggingEnabled()) {
                this.stackLogger.logDebug(" putTransactionHash :  key = " + key);
                return;
            }
            return;
        }
        key = sipRequest.getTransactionId();
        if (this.stackLogger.isLoggingEnabled()) {
            this.stackLogger.logDebug(" putTransactionHash :  key = " + key);
        }
        this.serverTransactionTable.put(key, (SIPServerTransaction) sipTransaction);
    }

    protected void decrementActiveClientTransactionCount() {
        if (this.activeClientTransactionCount.decrementAndGet() <= this.clientTransactionTableLowaterMark && (this.unlimitedClientTransactionTableSize ^ 1) != 0) {
            synchronized (this.clientTransactionTable) {
                this.clientTransactionTable.notify();
            }
        }
    }

    protected void removeTransactionHash(SIPTransaction sipTransaction) {
        if (sipTransaction.getOriginalRequest() != null) {
            String key;
            if (sipTransaction instanceof SIPClientTransaction) {
                key = sipTransaction.getTransactionId();
                if (this.stackLogger.isLoggingEnabled()) {
                    this.stackLogger.logStackTrace();
                    this.stackLogger.logDebug("removing client Tx : " + key);
                }
                this.clientTransactionTable.remove(key);
            } else if (sipTransaction instanceof SIPServerTransaction) {
                key = sipTransaction.getTransactionId();
                this.serverTransactionTable.remove(key);
                if (this.stackLogger.isLoggingEnabled()) {
                    this.stackLogger.logDebug("removing server Tx : " + key);
                }
            }
        }
    }

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

    public synchronized void dialogErrorEvent(SIPDialogErrorEvent dialogErrorEvent) {
        SIPDialog sipDialog = (SIPDialog) dialogErrorEvent.getSource();
        SipListener sipListener = ((SipStackImpl) this).getSipListener();
        if (!(sipDialog == null || ((sipListener instanceof SipListenerExt) ^ 1) == 0)) {
            sipDialog.delete();
        }
    }

    public void stopStack() {
        if (this.timer != null) {
            this.timer.cancel();
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
            MessageProcessor[] processorList = getMessageProcessors();
            for (MessageProcessor removeMessageProcessor : processorList) {
                removeMessageProcessor(removeMessageProcessor);
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
            this.stackLogger.logDebug("putPendingTransaction: " + tr);
        }
        this.pendingTransactions.put(tr.getTransactionId(), tr);
    }

    public NetworkLayer getNetworkLayer() {
        if (this.networkLayer == null) {
            return DefaultNetworkLayer.SINGLETON;
        }
        return this.networkLayer;
    }

    public boolean isLoggingEnabled() {
        return this.stackLogger == null ? false : this.stackLogger.isLoggingEnabled();
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
            if (this.router != null) {
                return this.router.getNextHop(sipRequest);
            }
            return null;
        } else if (sipRequest.getRequestURI().isSipURI() || sipRequest.getRouteHeaders() != null) {
            return this.defaultRouter.getNextHop(sipRequest);
        } else {
            if (this.router != null) {
                return this.router.getNextHop(sipRequest);
            }
            return null;
        }
    }

    public void setStackName(String stackName) {
        this.stackName = stackName;
    }

    protected void setHostAddress(String stackAddress) throws UnknownHostException {
        if (stackAddress.indexOf(58) == stackAddress.lastIndexOf(58) || stackAddress.trim().charAt(0) == '[') {
            this.stackAddress = stackAddress;
        } else {
            this.stackAddress = '[' + stackAddress + ']';
        }
        this.stackInetAddress = InetAddress.getByName(stackAddress);
    }

    public String getHostAddress() {
        return this.stackAddress;
    }

    protected void setRouter(Router router) {
        this.router = router;
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
        if (this.router != null) {
            return this.router;
        }
        return this.defaultRouter;
    }

    public Router getRouter() {
        return this.router;
    }

    public boolean isAlive() {
        return this.toExit ^ 1;
    }

    protected void addMessageProcessor(MessageProcessor newMessageProcessor) throws IOException {
        synchronized (this.messageProcessors) {
            this.messageProcessors.add(newMessageProcessor);
        }
    }

    protected void removeMessageProcessor(MessageProcessor oldMessageProcessor) {
        synchronized (this.messageProcessors) {
            if (this.messageProcessors.remove(oldMessageProcessor)) {
                oldMessageProcessor.stop();
            }
        }
    }

    protected MessageProcessor[] getMessageProcessors() {
        MessageProcessor[] messageProcessorArr;
        synchronized (this.messageProcessors) {
            messageProcessorArr = (MessageProcessor[]) this.messageProcessors.toArray(new MessageProcessor[0]);
        }
        return messageProcessorArr;
    }

    protected MessageProcessor createMessageProcessor(InetAddress ipAddress, int port, String transport) throws IOException {
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

    protected void setMessageFactory(StackMessageFactory messageFactory) {
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
            MessageProcessor nextProcessor = (MessageProcessor) processorIterator.next();
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
            this.stackLogger.logDebug("isEventForked: " + ename + " returning " + this.forkedEvents.contains(ename));
        }
        return this.forkedEvents.contains(ename);
    }

    public AddressResolver getAddressResolver() {
        return this.addressResolver;
    }

    public void setAddressResolver(AddressResolver addressResolver) {
        this.addressResolver = addressResolver;
    }

    public void setLogRecordFactory(LogRecordFactory logRecordFactory) {
        this.logRecordFactory = logRecordFactory;
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
        StringBuilder append = new StringBuilder().append("SIP Stack Audit:\n");
        if (leakedDialogs == null) {
            leakedDialogs = "";
        }
        append = append.append(leakedDialogs);
        if (leakedServerTransactions == null) {
            leakedServerTransactions = "";
        }
        append = append.append(leakedServerTransactions);
        if (leakedClientTransactions == null) {
            leakedClientTransactions = "";
        }
        return append.append(leakedClientTransactions).toString();
    }

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
            CallIdHeader callIdHeader = itDialog != null ? itDialog.getCallId() : null;
            Object callID = callIdHeader != null ? callIdHeader.getCallId() : null;
            if (!(itDialog == null || callID == null || (activeCallIDs.contains(callID) ^ 1) == 0)) {
                if (itDialog.auditTag == 0) {
                    itDialog.auditTag = currentTime;
                } else if (currentTime - itDialog.auditTag >= leakedDialogTimer) {
                    leakedDialogs++;
                    DialogState dialogState = itDialog.getState();
                    String dialogReport = "dialog id: " + itDialog.getDialogId() + ", dialog state: " + (dialogState != null ? dialogState.toString() : "null");
                    auditReport = auditReport + "    " + dialogReport + Separators.RETURN;
                    itDialog.setState(SIPDialog.TERMINATED_STATE);
                    if (this.stackLogger.isLoggingEnabled()) {
                        this.stackLogger.logDebug("auditDialogs: leaked " + dialogReport);
                    }
                }
            }
        }
        if (leakedDialogs > 0) {
            return auditReport + "    Total: " + Integer.toString(leakedDialogs) + " leaked dialogs detected and removed.\n";
        }
        return null;
    }

    private String auditTransactions(ConcurrentHashMap transactionsMap, long a_nLeakedTransactionTimer) {
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
                    String origRequestMethod;
                    String transactionState;
                    leakedTransactions++;
                    TransactionState transactionState2 = sipTransaction.getState();
                    SIPRequest origRequest = sipTransaction.getOriginalRequest();
                    if (origRequest != null) {
                        origRequestMethod = origRequest.getMethod();
                    } else {
                        origRequestMethod = null;
                    }
                    StringBuilder append = new StringBuilder().append(sipTransaction.getClass().getName()).append(", state: ");
                    if (transactionState2 != null) {
                        transactionState = transactionState2.toString();
                    } else {
                        transactionState = "null";
                    }
                    StringBuilder append2 = append.append(transactionState).append(", OR: ");
                    if (origRequestMethod == null) {
                        origRequestMethod = "null";
                    }
                    String transactionReport = append2.append(origRequestMethod).toString();
                    auditReport = auditReport + "    " + transactionReport + Separators.RETURN;
                    removeTransaction(sipTransaction);
                    if (isLoggingEnabled()) {
                        this.stackLogger.logDebug("auditTransactions: leaked " + transactionReport);
                    }
                }
            }
        }
        if (leakedTransactions > 0) {
            return auditReport + "    Total: " + Integer.toString(leakedTransactions) + " leaked transactions detected and removed.\n";
        }
        return null;
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
        HashSet<Dialog> dialogs = new HashSet();
        dialogs.addAll(this.dialogTable.values());
        dialogs.addAll(this.earlyDialogTable.values());
        return dialogs;
    }

    public Collection<Dialog> getDialogs(DialogState state) {
        HashSet<Dialog> matchingDialogs = new HashSet();
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
            this.stackLogger.logDebug("Looking for dialog " + did);
        }
        Dialog replacesDialog = (Dialog) this.dialogTable.get(did);
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
        return (Dialog) this.dialogTable.get(retval.toString().toLowerCase());
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
    }

    public Timer getTimer() {
        return this.timer;
    }

    public int getReceiveUdpBufferSize() {
        return this.receiveUdpBufferSize;
    }

    public void setReceiveUdpBufferSize(int receiveUdpBufferSize) {
        this.receiveUdpBufferSize = receiveUdpBufferSize;
    }

    public int getSendUdpBufferSize() {
        return this.sendUdpBufferSize;
    }

    public void setSendUdpBufferSize(int sendUdpBufferSize) {
        this.sendUdpBufferSize = sendUdpBufferSize;
    }

    public void setStackLogger(StackLogger stackLogger) {
        this.stackLogger = stackLogger;
    }

    public boolean checkBranchId() {
        return this.checkBranchId;
    }

    public void setLogStackTraceOnMessageSend(boolean logStackTraceOnMessageSend) {
        this.logStackTraceOnMessageSend = logStackTraceOnMessageSend;
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
        return (SIPClientTransaction) this.forkedClientTransactionTable.get(transactionId);
    }
}
