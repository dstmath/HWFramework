package gov.nist.javax.sip;

import gov.nist.core.Separators;
import gov.nist.core.ServerLogger;
import gov.nist.core.StackLogger;
import gov.nist.core.net.AddressResolver;
import gov.nist.core.net.NetworkLayer;
import gov.nist.core.net.SslNetworkLayer;
import gov.nist.javax.sip.clientauthutils.AccountManager;
import gov.nist.javax.sip.clientauthutils.AuthenticationHelper;
import gov.nist.javax.sip.clientauthutils.AuthenticationHelperImpl;
import gov.nist.javax.sip.clientauthutils.SecureAccountManager;
import gov.nist.javax.sip.parser.StringMsgParser;
import gov.nist.javax.sip.stack.DefaultMessageLogFactory;
import gov.nist.javax.sip.stack.DefaultRouter;
import gov.nist.javax.sip.stack.MessageProcessor;
import gov.nist.javax.sip.stack.SIPTransactionStack;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import javax.sip.InvalidArgumentException;
import javax.sip.ListeningPoint;
import javax.sip.ObjectInUseException;
import javax.sip.PeerUnavailableException;
import javax.sip.ProviderDoesNotExistException;
import javax.sip.SipException;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import javax.sip.TransportNotSupportedException;
import javax.sip.address.Router;
import javax.sip.header.HeaderFactory;
import org.ccil.cowan.tagsoup.HTMLModels;

public class SipStackImpl extends SIPTransactionStack implements SipStack, SipStackExt {
    public static final Integer MAX_DATAGRAM_SIZE = Integer.valueOf(HTMLModels.M_LEGEND);
    private String[] cipherSuites;
    boolean deliverTerminatedEventForAck;
    boolean deliverUnsolicitedNotify;
    private String[] enabledProtocols;
    private EventScanner eventScanner;
    private Hashtable<String, ListeningPointImpl> listeningPoints;
    boolean reEntrantListener;
    SipListener sipListener;
    private LinkedList<SipProviderImpl> sipProviders;
    private Semaphore stackSemaphore;

    protected SipStackImpl() {
        this.deliverTerminatedEventForAck = false;
        this.deliverUnsolicitedNotify = false;
        this.stackSemaphore = new Semaphore(1);
        this.cipherSuites = new String[]{"TLS_RSA_WITH_AES_128_CBC_SHA", "SSL_RSA_WITH_3DES_EDE_CBC_SHA", "TLS_DH_anon_WITH_AES_128_CBC_SHA", "SSL_DH_anon_WITH_3DES_EDE_CBC_SHA"};
        this.enabledProtocols = new String[]{"SSLv3", "SSLv2Hello", "TLSv1"};
        super.setMessageFactory(new NistSipMessageFactoryImpl(this));
        this.eventScanner = new EventScanner(this);
        this.listeningPoints = new Hashtable();
        this.sipProviders = new LinkedList();
    }

    private void reInitialize() {
        super.reInit();
        this.eventScanner = new EventScanner(this);
        this.listeningPoints = new Hashtable();
        this.sipProviders = new LinkedList();
        this.sipListener = null;
    }

    boolean isAutomaticDialogSupportEnabled() {
        return this.isAutomaticDialogSupportEnabled;
    }

    public SipStackImpl(Properties configurationProperties) throws PeerUnavailableException {
        this();
        String address = configurationProperties.getProperty("javax.sip.IP_ADDRESS");
        if (address != null) {
            try {
                super.setHostAddress(address);
            } catch (UnknownHostException e) {
                throw new PeerUnavailableException("bad address " + address);
            }
        }
        String name = configurationProperties.getProperty("javax.sip.STACK_NAME");
        if (name == null) {
            throw new PeerUnavailableException("stack name is missing");
        }
        super.setStackName(name);
        String stackLoggerClassName = configurationProperties.getProperty("gov.nist.javax.sip.STACK_LOGGER");
        if (stackLoggerClassName == null) {
            stackLoggerClassName = "gov.nist.core.LogWriter";
        }
        try {
            StackLogger stackLogger = (StackLogger) Class.forName(stackLoggerClassName).getConstructor(new Class[0]).newInstance(new Object[0]);
            stackLogger.setStackProperties(configurationProperties);
            super.setStackLogger(stackLogger);
            String serverLoggerClassName = configurationProperties.getProperty("gov.nist.javax.sip.SERVER_LOGGER");
            if (serverLoggerClassName == null) {
                serverLoggerClassName = "gov.nist.javax.sip.stack.ServerLog";
            }
            try {
                this.serverLogger = (ServerLogger) Class.forName(serverLoggerClassName).getConstructor(new Class[0]).newInstance(new Object[0]);
                this.serverLogger.setSipStack(this);
                this.serverLogger.setStackProperties(configurationProperties);
                this.outboundProxy = configurationProperties.getProperty("javax.sip.OUTBOUND_PROXY");
                this.defaultRouter = new DefaultRouter(this, this.outboundProxy);
                String routerPath = configurationProperties.getProperty("javax.sip.ROUTER_PATH");
                if (routerPath == null) {
                    routerPath = "gov.nist.javax.sip.stack.DefaultRouter";
                }
                try {
                    StringTokenizer stringTokenizer;
                    String path;
                    super.setRouter((Router) Class.forName(routerPath).getConstructor(new Class[]{SipStack.class, String.class}).newInstance(new Object[]{this, this.outboundProxy}));
                    String useRouterForAll = configurationProperties.getProperty("javax.sip.USE_ROUTER_FOR_ALL_URIS");
                    this.useRouterForAll = true;
                    if (useRouterForAll != null) {
                        this.useRouterForAll = "true".equalsIgnoreCase(useRouterForAll);
                    }
                    String extensionMethods = configurationProperties.getProperty("javax.sip.EXTENSION_METHODS");
                    if (extensionMethods != null) {
                        stringTokenizer = new StringTokenizer(extensionMethods);
                        while (stringTokenizer.hasMoreTokens()) {
                            String em = stringTokenizer.nextToken(Separators.COLON);
                            if (em.equalsIgnoreCase("BYE") || em.equalsIgnoreCase("INVITE") || em.equalsIgnoreCase("SUBSCRIBE") || em.equalsIgnoreCase("NOTIFY") || em.equalsIgnoreCase("ACK") || em.equalsIgnoreCase("OPTIONS")) {
                                throw new PeerUnavailableException("Bad extension method " + em);
                            }
                            addExtensionMethod(em);
                        }
                    }
                    String keyStoreFile = configurationProperties.getProperty("javax.net.ssl.keyStore");
                    String trustStoreFile = configurationProperties.getProperty("javax.net.ssl.trustStore");
                    if (keyStoreFile != null) {
                        if (trustStoreFile == null) {
                            trustStoreFile = keyStoreFile;
                        }
                        try {
                            this.networkLayer = new SslNetworkLayer(trustStoreFile, keyStoreFile, configurationProperties.getProperty("javax.net.ssl.keyStorePassword").toCharArray(), configurationProperties.getProperty("javax.net.ssl.keyStoreType"));
                        } catch (Exception e1) {
                            getStackLogger().logError("could not instantiate SSL networking", e1);
                        }
                    }
                    this.isAutomaticDialogSupportEnabled = configurationProperties.getProperty("javax.sip.AUTOMATIC_DIALOG_SUPPORT", "on").equalsIgnoreCase("on");
                    this.isAutomaticDialogErrorHandlingEnabled = configurationProperties.getProperty("gov.nist.javax.sip.AUTOMATIC_DIALOG_ERROR_HANDLING", "true").equals(Boolean.TRUE.toString());
                    if (this.isAutomaticDialogSupportEnabled) {
                        this.isAutomaticDialogErrorHandlingEnabled = true;
                    }
                    if (configurationProperties.getProperty("gov.nist.javax.sip.MAX_LISTENER_RESPONSE_TIME") != null) {
                        this.maxListenerResponseTime = Integer.parseInt(configurationProperties.getProperty("gov.nist.javax.sip.MAX_LISTENER_RESPONSE_TIME"));
                        if (this.maxListenerResponseTime <= 0) {
                            throw new PeerUnavailableException("Bad configuration parameter gov.nist.javax.sip.MAX_LISTENER_RESPONSE_TIME : should be positive");
                        }
                    }
                    this.maxListenerResponseTime = -1;
                    this.deliverTerminatedEventForAck = configurationProperties.getProperty("gov.nist.javax.sip.DELIVER_TERMINATED_EVENT_FOR_ACK", "false").equalsIgnoreCase("true");
                    this.deliverUnsolicitedNotify = configurationProperties.getProperty("gov.nist.javax.sip.DELIVER_UNSOLICITED_NOTIFY", "false").equalsIgnoreCase("true");
                    String forkedSubscriptions = configurationProperties.getProperty("javax.sip.FORKABLE_EVENTS");
                    if (forkedSubscriptions != null) {
                        stringTokenizer = new StringTokenizer(forkedSubscriptions);
                        while (stringTokenizer.hasMoreTokens()) {
                            this.forkedEvents.add(stringTokenizer.nextToken());
                        }
                    }
                    String NETWORK_LAYER_KEY = "gov.nist.javax.sip.NETWORK_LAYER";
                    if (configurationProperties.containsKey("gov.nist.javax.sip.NETWORK_LAYER")) {
                        path = configurationProperties.getProperty("gov.nist.javax.sip.NETWORK_LAYER");
                        try {
                            this.networkLayer = (NetworkLayer) Class.forName(path).getConstructor(new Class[0]).newInstance(new Object[0]);
                        } catch (Exception e2) {
                            throw new PeerUnavailableException("can't find or instantiate NetworkLayer implementation: " + path);
                        }
                    }
                    String ADDRESS_RESOLVER_KEY = "gov.nist.javax.sip.ADDRESS_RESOLVER";
                    if (configurationProperties.containsKey("gov.nist.javax.sip.ADDRESS_RESOLVER")) {
                        path = configurationProperties.getProperty("gov.nist.javax.sip.ADDRESS_RESOLVER");
                        try {
                            this.addressResolver = (AddressResolver) Class.forName(path).getConstructor(new Class[0]).newInstance(new Object[0]);
                        } catch (Exception e3) {
                            throw new PeerUnavailableException("can't find or instantiate AddressResolver implementation: " + path);
                        }
                    }
                    String maxConnections = configurationProperties.getProperty("gov.nist.javax.sip.MAX_CONNECTIONS");
                    if (maxConnections != null) {
                        try {
                            this.maxConnections = new Integer(maxConnections).intValue();
                        } catch (NumberFormatException ex) {
                            if (isLoggingEnabled()) {
                                getStackLogger().logError("max connections - bad value " + ex.getMessage());
                            }
                        }
                    }
                    String threadPoolSize = configurationProperties.getProperty("gov.nist.javax.sip.THREAD_POOL_SIZE");
                    if (threadPoolSize != null) {
                        try {
                            this.threadPoolSize = new Integer(threadPoolSize).intValue();
                        } catch (NumberFormatException ex2) {
                            if (isLoggingEnabled()) {
                                getStackLogger().logError("thread pool size - bad value " + ex2.getMessage());
                            }
                        }
                    }
                    String serverTransactionTableSize = configurationProperties.getProperty("gov.nist.javax.sip.MAX_SERVER_TRANSACTIONS");
                    if (serverTransactionTableSize != null) {
                        try {
                            this.serverTransactionTableHighwaterMark = new Integer(serverTransactionTableSize).intValue();
                            this.serverTransactionTableLowaterMark = (this.serverTransactionTableHighwaterMark * 80) / 100;
                        } catch (NumberFormatException ex22) {
                            if (isLoggingEnabled()) {
                                getStackLogger().logError("transaction table size - bad value " + ex22.getMessage());
                            }
                        }
                    } else {
                        this.unlimitedServerTransactionTableSize = true;
                    }
                    String clientTransactionTableSize = configurationProperties.getProperty("gov.nist.javax.sip.MAX_CLIENT_TRANSACTIONS");
                    if (clientTransactionTableSize != null) {
                        try {
                            this.clientTransactionTableHiwaterMark = new Integer(clientTransactionTableSize).intValue();
                            this.clientTransactionTableLowaterMark = (this.clientTransactionTableLowaterMark * 80) / 100;
                        } catch (NumberFormatException ex222) {
                            if (isLoggingEnabled()) {
                                getStackLogger().logError("transaction table size - bad value " + ex222.getMessage());
                            }
                        }
                    } else {
                        this.unlimitedClientTransactionTableSize = true;
                    }
                    this.cacheServerConnections = true;
                    String flag = configurationProperties.getProperty("gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS");
                    if (flag != null && "false".equalsIgnoreCase(flag.trim())) {
                        this.cacheServerConnections = false;
                    }
                    this.cacheClientConnections = true;
                    String cacheflag = configurationProperties.getProperty("gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS");
                    if (cacheflag != null && "false".equalsIgnoreCase(cacheflag.trim())) {
                        this.cacheClientConnections = false;
                    }
                    String readTimeout = configurationProperties.getProperty("gov.nist.javax.sip.READ_TIMEOUT");
                    if (readTimeout != null) {
                        try {
                            int rt = Integer.parseInt(readTimeout);
                            if (rt >= 100) {
                                this.readTimeout = rt;
                            } else {
                                System.err.println("Value too low " + readTimeout);
                            }
                        } catch (NumberFormatException e4) {
                            if (isLoggingEnabled()) {
                                getStackLogger().logError("Bad read timeout " + readTimeout);
                            }
                        }
                    }
                    if (configurationProperties.getProperty("gov.nist.javax.sip.STUN_SERVER") != null) {
                        getStackLogger().logWarning("Ignoring obsolete property gov.nist.javax.sip.STUN_SERVER");
                    }
                    String maxMsgSize = configurationProperties.getProperty("gov.nist.javax.sip.MAX_MESSAGE_SIZE");
                    if (maxMsgSize != null) {
                        try {
                            this.maxMessageSize = new Integer(maxMsgSize).intValue();
                            if (this.maxMessageSize < 4096) {
                                this.maxMessageSize = 4096;
                            }
                        } catch (NumberFormatException ex2222) {
                            if (isLoggingEnabled()) {
                                getStackLogger().logError("maxMessageSize - bad value " + ex2222.getMessage());
                            }
                        }
                    } else {
                        this.maxMessageSize = 0;
                    }
                    String rel = configurationProperties.getProperty("gov.nist.javax.sip.REENTRANT_LISTENER");
                    this.reEntrantListener = rel != null ? "true".equalsIgnoreCase(rel) : false;
                    String interval = configurationProperties.getProperty("gov.nist.javax.sip.THREAD_AUDIT_INTERVAL_IN_MILLISECS");
                    if (interval != null) {
                        try {
                            getThreadAuditor().setPingIntervalInMillisecs(Long.valueOf(interval).longValue() / 2);
                        } catch (NumberFormatException ex22222) {
                            if (isLoggingEnabled()) {
                                getStackLogger().logError("THREAD_AUDIT_INTERVAL_IN_MILLISECS - bad value [" + interval + "] " + ex22222.getMessage());
                            }
                        }
                    }
                    setNon2XXAckPassedToListener(Boolean.valueOf(configurationProperties.getProperty("gov.nist.javax.sip.PASS_INVITE_NON_2XX_ACK_TO_LISTENER", "false")).booleanValue());
                    this.generateTimeStampHeader = Boolean.valueOf(configurationProperties.getProperty("gov.nist.javax.sip.AUTO_GENERATE_TIMESTAMP", "false")).booleanValue();
                    String messageLogFactoryClasspath = configurationProperties.getProperty("gov.nist.javax.sip.LOG_FACTORY");
                    if (messageLogFactoryClasspath != null) {
                        try {
                            this.logRecordFactory = (LogRecordFactory) Class.forName(messageLogFactoryClasspath).getConstructor(new Class[0]).newInstance(new Object[0]);
                        } catch (Exception e5) {
                            if (isLoggingEnabled()) {
                                getStackLogger().logError("Bad configuration value for LOG_FACTORY -- using default logger");
                            }
                            this.logRecordFactory = new DefaultMessageLogFactory();
                        }
                    } else {
                        this.logRecordFactory = new DefaultMessageLogFactory();
                    }
                    StringMsgParser.setComputeContentLengthFromMessage(configurationProperties.getProperty("gov.nist.javax.sip.COMPUTE_CONTENT_LENGTH_FROM_MESSAGE_BODY", "false").equalsIgnoreCase("true"));
                    String tlsClientProtocols = configurationProperties.getProperty("gov.nist.javax.sip.TLS_CLIENT_PROTOCOLS");
                    if (tlsClientProtocols != null) {
                        stringTokenizer = new StringTokenizer(tlsClientProtocols, " ,");
                        String[] protocols = new String[stringTokenizer.countTokens()];
                        int i = 0;
                        while (stringTokenizer.hasMoreTokens()) {
                            int i2 = i + 1;
                            protocols[i] = stringTokenizer.nextToken();
                            i = i2;
                        }
                        this.enabledProtocols = protocols;
                    }
                    this.rfc2543Supported = configurationProperties.getProperty("gov.nist.javax.sip.RFC_2543_SUPPORT_ENABLED", "true").equalsIgnoreCase("true");
                    this.cancelClientTransactionChecked = configurationProperties.getProperty("gov.nist.javax.sip.CANCEL_CLIENT_TRANSACTION_CHECKED", "true").equalsIgnoreCase("true");
                    this.logStackTraceOnMessageSend = configurationProperties.getProperty("gov.nist.javax.sip.LOG_STACK_TRACE_ON_MESSAGE_SEND", "false").equalsIgnoreCase("true");
                    if (isLoggingEnabled()) {
                        getStackLogger().logDebug("created Sip stack. Properties = " + configurationProperties);
                    }
                    InputStream in = getClass().getResourceAsStream("/TIMESTAMP");
                    if (in != null) {
                        try {
                            String buildTimeStamp = new BufferedReader(new InputStreamReader(in)).readLine();
                            if (in != null) {
                                in.close();
                            }
                            getStackLogger().setBuildTimeStamp(buildTimeStamp);
                        } catch (IOException e6) {
                            getStackLogger().logError("Could not open build timestamp.");
                        }
                    }
                    super.setReceiveUdpBufferSize(new Integer(configurationProperties.getProperty("gov.nist.javax.sip.RECEIVE_UDP_BUFFER_SIZE", MAX_DATAGRAM_SIZE.toString())).intValue());
                    super.setSendUdpBufferSize(new Integer(configurationProperties.getProperty("gov.nist.javax.sip.SEND_UDP_BUFFER_SIZE", MAX_DATAGRAM_SIZE.toString())).intValue());
                    this.stackDoesCongestionControl = Boolean.parseBoolean(configurationProperties.getProperty("gov.nist.javax.sip.CONGESTION_CONTROL_ENABLED", Boolean.TRUE.toString()));
                    this.isBackToBackUserAgent = Boolean.parseBoolean(configurationProperties.getProperty("gov.nist.javax.sip.IS_BACK_TO_BACK_USER_AGENT", Boolean.FALSE.toString()));
                    this.checkBranchId = Boolean.parseBoolean(configurationProperties.getProperty("gov.nist.javax.sip.REJECT_STRAY_RESPONSES", Boolean.FALSE.toString()));
                    this.isDialogTerminatedEventDeliveredForNullDialog = Boolean.parseBoolean(configurationProperties.getProperty("gov.nist.javax.sip.DELIVER_TERMINATED_EVENT_FOR_NULL_DIALOG", Boolean.FALSE.toString()));
                    this.maxForkTime = Integer.parseInt(configurationProperties.getProperty("gov.nist.javax.sip.MAX_FORK_TIME_SECONDS", "0"));
                } catch (Throwable ex1) {
                    getStackLogger().logError("could not instantiate router -- invocation target problem", (Exception) ex1.getCause());
                    throw new PeerUnavailableException("Cound not instantiate router - check constructor", ex1);
                } catch (Throwable ex3) {
                    getStackLogger().logError("could not instantiate router", (Exception) ex3.getCause());
                    throw new PeerUnavailableException("Could not instantiate router", ex3);
                }
            } catch (Throwable ex12) {
                throw new IllegalArgumentException("Cound not instantiate server logger " + stackLoggerClassName + "- check that it is present on the classpath and that there is a no-args constructor defined", ex12);
            } catch (Throwable ex32) {
                throw new IllegalArgumentException("Cound not instantiate server logger " + stackLoggerClassName + "- check that it is present on the classpath and that there is a no-args constructor defined", ex32);
            }
        } catch (Throwable ex122) {
            throw new IllegalArgumentException("Cound not instantiate stack logger " + stackLoggerClassName + "- check that it is present on the classpath and that there is a no-args constructor defined", ex122);
        } catch (Throwable ex322) {
            throw new IllegalArgumentException("Cound not instantiate stack logger " + stackLoggerClassName + "- check that it is present on the classpath and that there is a no-args constructor defined", ex322);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:48:0x0116  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized ListeningPoint createListeningPoint(String address, int port, String transport) throws TransportNotSupportedException, InvalidArgumentException {
        IOException ex;
        if (isLoggingEnabled()) {
            getStackLogger().logDebug("createListeningPoint : address = " + address + " port = " + port + " transport = " + transport);
        }
        if (address == null) {
            throw new NullPointerException("Address for listening point is null!");
        } else if (transport == null) {
            throw new NullPointerException("null transport");
        } else if (port <= 0) {
            throw new InvalidArgumentException("bad port");
        } else if (transport.equalsIgnoreCase(ListeningPoint.UDP) || (transport.equalsIgnoreCase(ListeningPoint.TLS) ^ 1) == 0 || (transport.equalsIgnoreCase(ListeningPoint.TCP) ^ 1) == 0 || (transport.equalsIgnoreCase(ListeningPoint.SCTP) ^ 1) == 0) {
            if (!isAlive()) {
                this.toExit = false;
                reInitialize();
            }
            String key = ListeningPointImpl.makeKey(address, port, transport);
            ListeningPointImpl lip = (ListeningPointImpl) this.listeningPoints.get(key);
            if (lip != null) {
                return lip;
            }
            try {
                MessageProcessor messageProcessor = createMessageProcessor(InetAddress.getByName(address), port, transport);
                if (isLoggingEnabled()) {
                    getStackLogger().logDebug("Created Message Processor: " + address + " port = " + port + " transport = " + transport);
                }
                ListeningPointImpl lip2 = new ListeningPointImpl(this, port, transport);
                try {
                    lip2.messageProcessor = messageProcessor;
                    messageProcessor.setListeningPoint(lip2);
                    this.listeningPoints.put(key, lip2);
                    messageProcessor.start();
                    return lip2;
                } catch (IOException e) {
                    ex = e;
                    lip = lip2;
                    if (isLoggingEnabled()) {
                        getStackLogger().logError("Invalid argument address = " + address + " port = " + port + " transport = " + transport);
                    }
                    throw new InvalidArgumentException(ex.getMessage(), ex);
                }
            } catch (IOException e2) {
                ex = e2;
                if (isLoggingEnabled()) {
                }
                throw new InvalidArgumentException(ex.getMessage(), ex);
            }
        } else {
            throw new TransportNotSupportedException("bad transport " + transport);
        }
    }

    public SipProvider createSipProvider(ListeningPoint listeningPoint) throws ObjectInUseException {
        if (listeningPoint == null) {
            throw new NullPointerException("null listeningPoint");
        }
        if (isLoggingEnabled()) {
            getStackLogger().logDebug("createSipProvider: " + listeningPoint);
        }
        ListeningPointImpl listeningPointImpl = (ListeningPointImpl) listeningPoint;
        if (listeningPointImpl.sipProvider != null) {
            throw new ObjectInUseException("Provider already attached!");
        }
        SipProviderImpl provider = new SipProviderImpl(this);
        provider.setListeningPoint(listeningPointImpl);
        listeningPointImpl.sipProvider = provider;
        this.sipProviders.add(provider);
        return provider;
    }

    public void deleteListeningPoint(ListeningPoint listeningPoint) throws ObjectInUseException {
        if (listeningPoint == null) {
            throw new NullPointerException("null listeningPoint arg");
        }
        ListeningPointImpl lip = (ListeningPointImpl) listeningPoint;
        super.removeMessageProcessor(lip.messageProcessor);
        this.listeningPoints.remove(lip.getKey());
    }

    public void deleteSipProvider(SipProvider sipProvider) throws ObjectInUseException {
        if (sipProvider == null) {
            throw new NullPointerException("null provider arg");
        }
        SipProviderImpl sipProviderImpl = (SipProviderImpl) sipProvider;
        if (sipProviderImpl.getSipListener() != null) {
            throw new ObjectInUseException("SipProvider still has an associated SipListener!");
        }
        sipProviderImpl.removeListeningPoints();
        sipProviderImpl.stop();
        this.sipProviders.remove(sipProvider);
        if (this.sipProviders.isEmpty()) {
            stopStack();
        }
    }

    public String getIPAddress() {
        return super.getHostAddress();
    }

    public Iterator getListeningPoints() {
        return this.listeningPoints.values().iterator();
    }

    public boolean isRetransmissionFilterActive() {
        return true;
    }

    public Iterator<SipProviderImpl> getSipProviders() {
        return this.sipProviders.iterator();
    }

    public String getStackName() {
        return this.stackName;
    }

    protected void finalize() {
        stopStack();
    }

    public ListeningPoint createListeningPoint(int port, String transport) throws TransportNotSupportedException, InvalidArgumentException {
        if (this.stackAddress != null) {
            return createListeningPoint(this.stackAddress, port, transport);
        }
        throw new NullPointerException("Stack does not have a default IP Address!");
    }

    public void stop() {
        if (isLoggingEnabled()) {
            getStackLogger().logDebug("stopStack -- stoppping the stack");
        }
        stopStack();
        this.sipProviders = new LinkedList();
        this.listeningPoints = new Hashtable();
        if (this.eventScanner != null) {
            this.eventScanner.forceStop();
        }
        this.eventScanner = null;
    }

    public void start() throws ProviderDoesNotExistException, SipException {
        if (this.eventScanner == null) {
            this.eventScanner = new EventScanner(this);
        }
    }

    public SipListener getSipListener() {
        return this.sipListener;
    }

    public LogRecordFactory getLogRecordFactory() {
        return this.logRecordFactory;
    }

    @Deprecated
    public EventScanner getEventScanner() {
        return this.eventScanner;
    }

    public AuthenticationHelper getAuthenticationHelper(AccountManager accountManager, HeaderFactory headerFactory) {
        return new AuthenticationHelperImpl(this, accountManager, headerFactory);
    }

    public AuthenticationHelper getSecureAuthenticationHelper(SecureAccountManager accountManager, HeaderFactory headerFactory) {
        return new AuthenticationHelperImpl(this, accountManager, headerFactory);
    }

    public void setEnabledCipherSuites(String[] newCipherSuites) {
        this.cipherSuites = newCipherSuites;
    }

    public String[] getEnabledCipherSuites() {
        return this.cipherSuites;
    }

    public void setEnabledProtocols(String[] newProtocols) {
        this.enabledProtocols = newProtocols;
    }

    public String[] getEnabledProtocols() {
        return this.enabledProtocols;
    }

    public void setIsBackToBackUserAgent(boolean flag) {
        this.isBackToBackUserAgent = flag;
    }

    public boolean isBackToBackUserAgent() {
        return this.isBackToBackUserAgent;
    }

    public boolean isAutomaticDialogErrorHandlingEnabled() {
        return this.isAutomaticDialogErrorHandlingEnabled;
    }

    public boolean acquireSem() {
        try {
            return this.stackSemaphore.tryAcquire(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return false;
        }
    }

    public void releaseSem() {
        this.stackSemaphore.release();
    }
}
