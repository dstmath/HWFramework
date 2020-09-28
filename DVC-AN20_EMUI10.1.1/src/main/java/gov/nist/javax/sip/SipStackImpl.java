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
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
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
    public static final Integer MAX_DATAGRAM_SIZE = Integer.valueOf((int) HTMLModels.M_LEGEND);
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
        this.listeningPoints = new Hashtable<>();
        this.sipProviders = new LinkedList<>();
    }

    private void reInitialize() {
        super.reInit();
        this.eventScanner = new EventScanner(this);
        this.listeningPoints = new Hashtable<>();
        this.sipProviders = new LinkedList<>();
        this.sipListener = null;
    }

    /* access modifiers changed from: package-private */
    public boolean isAutomaticDialogSupportEnabled() {
        return this.isAutomaticDialogSupportEnabled;
    }

    /* JADX WARNING: Removed duplicated region for block: B:182:0x044d  */
    /* JADX WARNING: Removed duplicated region for block: B:183:0x0468  */
    /* JADX WARNING: Removed duplicated region for block: B:187:0x0478  */
    /* JADX WARNING: Removed duplicated region for block: B:190:0x048a A[SYNTHETIC, Splitter:B:190:0x048a] */
    /* JADX WARNING: Removed duplicated region for block: B:199:0x04a6 A[Catch:{ NumberFormatException -> 0x04b0 }] */
    /* JADX WARNING: Removed duplicated region for block: B:204:0x04b7  */
    /* JADX WARNING: Removed duplicated region for block: B:205:0x04d8  */
    /* JADX WARNING: Removed duplicated region for block: B:208:0x04e5  */
    /* JADX WARNING: Removed duplicated region for block: B:214:0x04f9 A[SYNTHETIC, Splitter:B:214:0x04f9] */
    /* JADX WARNING: Removed duplicated region for block: B:225:0x051c  */
    /* JADX WARNING: Removed duplicated region for block: B:226:0x0543  */
    /* JADX WARNING: Removed duplicated region for block: B:227:0x0546  */
    /* JADX WARNING: Removed duplicated region for block: B:230:0x0576 A[SYNTHETIC, Splitter:B:230:0x0576] */
    /* JADX WARNING: Removed duplicated region for block: B:237:0x05a4  */
    /* JADX WARNING: Removed duplicated region for block: B:240:0x05c1  */
    /* JADX WARNING: Removed duplicated region for block: B:245:0x05e6  */
    /* JADX WARNING: Removed duplicated region for block: B:248:0x0613  */
    /* JADX WARNING: Removed duplicated region for block: B:251:0x0637  */
    public SipStackImpl(Properties configurationProperties) throws PeerUnavailableException {
        this();
        String stackLoggerClassName;
        String serverLoggerClassName;
        String routerPath;
        boolean z;
        String maxMsgSize;
        String interval;
        String messageLogFactoryClasspath;
        String tlsClientProtocols;
        InputStream in;
        String trustStoreFile;
        String address = configurationProperties.getProperty("javax.sip.IP_ADDRESS");
        if (address != null) {
            try {
                super.setHostAddress(address);
            } catch (UnknownHostException e) {
                throw new PeerUnavailableException("bad address " + address);
            }
        }
        String name = configurationProperties.getProperty("javax.sip.STACK_NAME");
        if (name != null) {
            super.setStackName(name);
            String stackLoggerClassName2 = configurationProperties.getProperty("gov.nist.javax.sip.STACK_LOGGER");
            if (stackLoggerClassName2 == null) {
                stackLoggerClassName = "gov.nist.core.LogWriter";
            } else {
                stackLoggerClassName = stackLoggerClassName2;
            }
            try {
                StackLogger stackLogger = (StackLogger) Class.forName(stackLoggerClassName).getConstructor(new Class[0]).newInstance(new Object[0]);
                stackLogger.setStackProperties(configurationProperties);
                super.setStackLogger(stackLogger);
                String serverLoggerClassName2 = configurationProperties.getProperty("gov.nist.javax.sip.SERVER_LOGGER");
                if (serverLoggerClassName2 == null) {
                    serverLoggerClassName = "gov.nist.javax.sip.stack.ServerLog";
                } else {
                    serverLoggerClassName = serverLoggerClassName2;
                }
                try {
                    this.serverLogger = (ServerLogger) Class.forName(serverLoggerClassName).getConstructor(new Class[0]).newInstance(new Object[0]);
                    this.serverLogger.setSipStack(this);
                    this.serverLogger.setStackProperties(configurationProperties);
                    this.outboundProxy = configurationProperties.getProperty("javax.sip.OUTBOUND_PROXY");
                    this.defaultRouter = new DefaultRouter(this, this.outboundProxy);
                    String routerPath2 = configurationProperties.getProperty("javax.sip.ROUTER_PATH");
                    if (routerPath2 == null) {
                        routerPath = "gov.nist.javax.sip.stack.DefaultRouter";
                    } else {
                        routerPath = routerPath2;
                    }
                    try {
                        super.setRouter((Router) Class.forName(routerPath).getConstructor(SipStack.class, String.class).newInstance(this, this.outboundProxy));
                        String useRouterForAll = configurationProperties.getProperty("javax.sip.USE_ROUTER_FOR_ALL_URIS");
                        this.useRouterForAll = true;
                        if (useRouterForAll != null) {
                            this.useRouterForAll = "true".equalsIgnoreCase(useRouterForAll);
                        }
                        String extensionMethods = configurationProperties.getProperty("javax.sip.EXTENSION_METHODS");
                        if (extensionMethods != null) {
                            StringTokenizer st = new StringTokenizer(extensionMethods);
                            while (st.hasMoreTokens()) {
                                String em = st.nextToken(Separators.COLON);
                                if (em.equalsIgnoreCase("BYE") || em.equalsIgnoreCase("INVITE") || em.equalsIgnoreCase("SUBSCRIBE") || em.equalsIgnoreCase("NOTIFY") || em.equalsIgnoreCase("ACK") || em.equalsIgnoreCase("OPTIONS")) {
                                    throw new PeerUnavailableException("Bad extension method " + em);
                                }
                                addExtensionMethod(em);
                            }
                        }
                        String keyStoreFile = configurationProperties.getProperty("javax.net.ssl.keyStore");
                        String trustStoreFile2 = configurationProperties.getProperty("javax.net.ssl.trustStore");
                        if (keyStoreFile != null) {
                            if (trustStoreFile2 == null) {
                                trustStoreFile = keyStoreFile;
                            } else {
                                trustStoreFile = trustStoreFile2;
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
                        } else {
                            this.maxListenerResponseTime = -1;
                        }
                        this.deliverTerminatedEventForAck = configurationProperties.getProperty("gov.nist.javax.sip.DELIVER_TERMINATED_EVENT_FOR_ACK", "false").equalsIgnoreCase("true");
                        this.deliverUnsolicitedNotify = configurationProperties.getProperty("gov.nist.javax.sip.DELIVER_UNSOLICITED_NOTIFY", "false").equalsIgnoreCase("true");
                        String forkedSubscriptions = configurationProperties.getProperty("javax.sip.FORKABLE_EVENTS");
                        if (forkedSubscriptions != null) {
                            for (StringTokenizer st2 = new StringTokenizer(forkedSubscriptions); st2.hasMoreTokens(); st2 = st2) {
                                this.forkedEvents.add(st2.nextToken());
                            }
                        }
                        if (configurationProperties.containsKey("gov.nist.javax.sip.NETWORK_LAYER")) {
                            String path = configurationProperties.getProperty("gov.nist.javax.sip.NETWORK_LAYER");
                            try {
                                try {
                                    this.networkLayer = (NetworkLayer) Class.forName(path).getConstructor(new Class[0]).newInstance(new Object[0]);
                                } catch (Exception e2) {
                                }
                            } catch (Exception e3) {
                                throw new PeerUnavailableException("can't find or instantiate NetworkLayer implementation: " + path);
                            }
                        }
                        if (configurationProperties.containsKey("gov.nist.javax.sip.ADDRESS_RESOLVER")) {
                            String path2 = configurationProperties.getProperty("gov.nist.javax.sip.ADDRESS_RESOLVER");
                            try {
                                try {
                                    this.addressResolver = (AddressResolver) Class.forName(path2).getConstructor(new Class[0]).newInstance(new Object[0]);
                                } catch (Exception e4) {
                                }
                            } catch (Exception e5) {
                                throw new PeerUnavailableException("can't find or instantiate AddressResolver implementation: " + path2);
                            }
                        }
                        String maxConnections = configurationProperties.getProperty("gov.nist.javax.sip.MAX_CONNECTIONS");
                        if (maxConnections != null) {
                            try {
                                this.maxConnections = new Integer(maxConnections).intValue();
                            } catch (NumberFormatException ex) {
                                if (isLoggingEnabled()) {
                                    StackLogger stackLogger2 = getStackLogger();
                                    stackLogger2.logError("max connections - bad value " + ex.getMessage());
                                }
                            }
                        }
                        String threadPoolSize = configurationProperties.getProperty("gov.nist.javax.sip.THREAD_POOL_SIZE");
                        if (threadPoolSize != null) {
                            try {
                                this.threadPoolSize = new Integer(threadPoolSize).intValue();
                            } catch (NumberFormatException ex2) {
                                if (isLoggingEnabled()) {
                                    StackLogger stackLogger3 = getStackLogger();
                                    stackLogger3.logError("thread pool size - bad value " + ex2.getMessage());
                                }
                            }
                        }
                        String serverTransactionTableSize = configurationProperties.getProperty("gov.nist.javax.sip.MAX_SERVER_TRANSACTIONS");
                        if (serverTransactionTableSize != null) {
                            try {
                                this.serverTransactionTableHighwaterMark = new Integer(serverTransactionTableSize).intValue();
                                this.serverTransactionTableLowaterMark = (this.serverTransactionTableHighwaterMark * 80) / 100;
                            } catch (NumberFormatException ex3) {
                                if (isLoggingEnabled()) {
                                    StackLogger stackLogger4 = getStackLogger();
                                    stackLogger4.logError("transaction table size - bad value " + ex3.getMessage());
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
                            } catch (NumberFormatException ex4) {
                                if (isLoggingEnabled()) {
                                    StackLogger stackLogger5 = getStackLogger();
                                    stackLogger5.logError("transaction table size - bad value " + ex4.getMessage());
                                }
                            }
                            z = true;
                        } else {
                            z = true;
                            this.unlimitedClientTransactionTableSize = true;
                        }
                        this.cacheServerConnections = z;
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
                                    try {
                                        this.readTimeout = rt;
                                    } catch (NumberFormatException e6) {
                                        nfe = e6;
                                        if (isLoggingEnabled()) {
                                        }
                                        if (configurationProperties.getProperty("gov.nist.javax.sip.STUN_SERVER") != null) {
                                        }
                                        maxMsgSize = configurationProperties.getProperty("gov.nist.javax.sip.MAX_MESSAGE_SIZE");
                                        if (maxMsgSize == null) {
                                        }
                                        String rel = configurationProperties.getProperty("gov.nist.javax.sip.REENTRANT_LISTENER");
                                        this.reEntrantListener = rel == null && "true".equalsIgnoreCase(rel);
                                        interval = configurationProperties.getProperty("gov.nist.javax.sip.THREAD_AUDIT_INTERVAL_IN_MILLISECS");
                                        if (interval == null) {
                                        }
                                        setNon2XXAckPassedToListener(Boolean.valueOf(configurationProperties.getProperty("gov.nist.javax.sip.PASS_INVITE_NON_2XX_ACK_TO_LISTENER", "false")).booleanValue());
                                        this.generateTimeStampHeader = Boolean.valueOf(configurationProperties.getProperty("gov.nist.javax.sip.AUTO_GENERATE_TIMESTAMP", "false")).booleanValue();
                                        messageLogFactoryClasspath = configurationProperties.getProperty("gov.nist.javax.sip.LOG_FACTORY");
                                        if (messageLogFactoryClasspath == null) {
                                        }
                                        StringMsgParser.setComputeContentLengthFromMessage(configurationProperties.getProperty("gov.nist.javax.sip.COMPUTE_CONTENT_LENGTH_FROM_MESSAGE_BODY", "false").equalsIgnoreCase("true"));
                                        tlsClientProtocols = configurationProperties.getProperty("gov.nist.javax.sip.TLS_CLIENT_PROTOCOLS");
                                        if (tlsClientProtocols == null) {
                                        }
                                        this.rfc2543Supported = configurationProperties.getProperty("gov.nist.javax.sip.RFC_2543_SUPPORT_ENABLED", "true").equalsIgnoreCase("true");
                                        this.cancelClientTransactionChecked = configurationProperties.getProperty("gov.nist.javax.sip.CANCEL_CLIENT_TRANSACTION_CHECKED", "true").equalsIgnoreCase("true");
                                        this.logStackTraceOnMessageSend = configurationProperties.getProperty("gov.nist.javax.sip.LOG_STACK_TRACE_ON_MESSAGE_SEND", "false").equalsIgnoreCase("true");
                                        if (isLoggingEnabled()) {
                                        }
                                        in = getClass().getResourceAsStream("/TIMESTAMP");
                                        if (in != null) {
                                        }
                                        super.setReceiveUdpBufferSize(new Integer(configurationProperties.getProperty("gov.nist.javax.sip.RECEIVE_UDP_BUFFER_SIZE", MAX_DATAGRAM_SIZE.toString())).intValue());
                                        super.setSendUdpBufferSize(new Integer(configurationProperties.getProperty("gov.nist.javax.sip.SEND_UDP_BUFFER_SIZE", MAX_DATAGRAM_SIZE.toString())).intValue());
                                        this.stackDoesCongestionControl = Boolean.parseBoolean(configurationProperties.getProperty("gov.nist.javax.sip.CONGESTION_CONTROL_ENABLED", Boolean.TRUE.toString()));
                                        this.isBackToBackUserAgent = Boolean.parseBoolean(configurationProperties.getProperty("gov.nist.javax.sip.IS_BACK_TO_BACK_USER_AGENT", Boolean.FALSE.toString()));
                                        this.checkBranchId = Boolean.parseBoolean(configurationProperties.getProperty("gov.nist.javax.sip.REJECT_STRAY_RESPONSES", Boolean.FALSE.toString()));
                                        this.isDialogTerminatedEventDeliveredForNullDialog = Boolean.parseBoolean(configurationProperties.getProperty("gov.nist.javax.sip.DELIVER_TERMINATED_EVENT_FOR_NULL_DIALOG", Boolean.FALSE.toString()));
                                        this.maxForkTime = Integer.parseInt(configurationProperties.getProperty("gov.nist.javax.sip.MAX_FORK_TIME_SECONDS", "0"));
                                    }
                                } else {
                                    try {
                                        PrintStream printStream = System.err;
                                        StringBuilder sb = new StringBuilder();
                                        try {
                                            sb.append("Value too low ");
                                            sb.append(readTimeout);
                                            printStream.println(sb.toString());
                                        } catch (NumberFormatException e7) {
                                            nfe = e7;
                                        }
                                    } catch (NumberFormatException e8) {
                                        nfe = e8;
                                        if (isLoggingEnabled()) {
                                        }
                                        if (configurationProperties.getProperty("gov.nist.javax.sip.STUN_SERVER") != null) {
                                        }
                                        maxMsgSize = configurationProperties.getProperty("gov.nist.javax.sip.MAX_MESSAGE_SIZE");
                                        if (maxMsgSize == null) {
                                        }
                                        String rel2 = configurationProperties.getProperty("gov.nist.javax.sip.REENTRANT_LISTENER");
                                        this.reEntrantListener = rel2 == null && "true".equalsIgnoreCase(rel2);
                                        interval = configurationProperties.getProperty("gov.nist.javax.sip.THREAD_AUDIT_INTERVAL_IN_MILLISECS");
                                        if (interval == null) {
                                        }
                                        setNon2XXAckPassedToListener(Boolean.valueOf(configurationProperties.getProperty("gov.nist.javax.sip.PASS_INVITE_NON_2XX_ACK_TO_LISTENER", "false")).booleanValue());
                                        this.generateTimeStampHeader = Boolean.valueOf(configurationProperties.getProperty("gov.nist.javax.sip.AUTO_GENERATE_TIMESTAMP", "false")).booleanValue();
                                        messageLogFactoryClasspath = configurationProperties.getProperty("gov.nist.javax.sip.LOG_FACTORY");
                                        if (messageLogFactoryClasspath == null) {
                                        }
                                        StringMsgParser.setComputeContentLengthFromMessage(configurationProperties.getProperty("gov.nist.javax.sip.COMPUTE_CONTENT_LENGTH_FROM_MESSAGE_BODY", "false").equalsIgnoreCase("true"));
                                        tlsClientProtocols = configurationProperties.getProperty("gov.nist.javax.sip.TLS_CLIENT_PROTOCOLS");
                                        if (tlsClientProtocols == null) {
                                        }
                                        this.rfc2543Supported = configurationProperties.getProperty("gov.nist.javax.sip.RFC_2543_SUPPORT_ENABLED", "true").equalsIgnoreCase("true");
                                        this.cancelClientTransactionChecked = configurationProperties.getProperty("gov.nist.javax.sip.CANCEL_CLIENT_TRANSACTION_CHECKED", "true").equalsIgnoreCase("true");
                                        this.logStackTraceOnMessageSend = configurationProperties.getProperty("gov.nist.javax.sip.LOG_STACK_TRACE_ON_MESSAGE_SEND", "false").equalsIgnoreCase("true");
                                        if (isLoggingEnabled()) {
                                        }
                                        in = getClass().getResourceAsStream("/TIMESTAMP");
                                        if (in != null) {
                                        }
                                        super.setReceiveUdpBufferSize(new Integer(configurationProperties.getProperty("gov.nist.javax.sip.RECEIVE_UDP_BUFFER_SIZE", MAX_DATAGRAM_SIZE.toString())).intValue());
                                        super.setSendUdpBufferSize(new Integer(configurationProperties.getProperty("gov.nist.javax.sip.SEND_UDP_BUFFER_SIZE", MAX_DATAGRAM_SIZE.toString())).intValue());
                                        this.stackDoesCongestionControl = Boolean.parseBoolean(configurationProperties.getProperty("gov.nist.javax.sip.CONGESTION_CONTROL_ENABLED", Boolean.TRUE.toString()));
                                        this.isBackToBackUserAgent = Boolean.parseBoolean(configurationProperties.getProperty("gov.nist.javax.sip.IS_BACK_TO_BACK_USER_AGENT", Boolean.FALSE.toString()));
                                        this.checkBranchId = Boolean.parseBoolean(configurationProperties.getProperty("gov.nist.javax.sip.REJECT_STRAY_RESPONSES", Boolean.FALSE.toString()));
                                        this.isDialogTerminatedEventDeliveredForNullDialog = Boolean.parseBoolean(configurationProperties.getProperty("gov.nist.javax.sip.DELIVER_TERMINATED_EVENT_FOR_NULL_DIALOG", Boolean.FALSE.toString()));
                                        this.maxForkTime = Integer.parseInt(configurationProperties.getProperty("gov.nist.javax.sip.MAX_FORK_TIME_SECONDS", "0"));
                                    }
                                }
                            } catch (NumberFormatException e9) {
                                nfe = e9;
                                if (isLoggingEnabled()) {
                                    StackLogger stackLogger6 = getStackLogger();
                                    stackLogger6.logError("Bad read timeout " + readTimeout);
                                }
                                if (configurationProperties.getProperty("gov.nist.javax.sip.STUN_SERVER") != null) {
                                }
                                maxMsgSize = configurationProperties.getProperty("gov.nist.javax.sip.MAX_MESSAGE_SIZE");
                                if (maxMsgSize == null) {
                                }
                                String rel22 = configurationProperties.getProperty("gov.nist.javax.sip.REENTRANT_LISTENER");
                                this.reEntrantListener = rel22 == null && "true".equalsIgnoreCase(rel22);
                                interval = configurationProperties.getProperty("gov.nist.javax.sip.THREAD_AUDIT_INTERVAL_IN_MILLISECS");
                                if (interval == null) {
                                }
                                setNon2XXAckPassedToListener(Boolean.valueOf(configurationProperties.getProperty("gov.nist.javax.sip.PASS_INVITE_NON_2XX_ACK_TO_LISTENER", "false")).booleanValue());
                                this.generateTimeStampHeader = Boolean.valueOf(configurationProperties.getProperty("gov.nist.javax.sip.AUTO_GENERATE_TIMESTAMP", "false")).booleanValue();
                                messageLogFactoryClasspath = configurationProperties.getProperty("gov.nist.javax.sip.LOG_FACTORY");
                                if (messageLogFactoryClasspath == null) {
                                }
                                StringMsgParser.setComputeContentLengthFromMessage(configurationProperties.getProperty("gov.nist.javax.sip.COMPUTE_CONTENT_LENGTH_FROM_MESSAGE_BODY", "false").equalsIgnoreCase("true"));
                                tlsClientProtocols = configurationProperties.getProperty("gov.nist.javax.sip.TLS_CLIENT_PROTOCOLS");
                                if (tlsClientProtocols == null) {
                                }
                                this.rfc2543Supported = configurationProperties.getProperty("gov.nist.javax.sip.RFC_2543_SUPPORT_ENABLED", "true").equalsIgnoreCase("true");
                                this.cancelClientTransactionChecked = configurationProperties.getProperty("gov.nist.javax.sip.CANCEL_CLIENT_TRANSACTION_CHECKED", "true").equalsIgnoreCase("true");
                                this.logStackTraceOnMessageSend = configurationProperties.getProperty("gov.nist.javax.sip.LOG_STACK_TRACE_ON_MESSAGE_SEND", "false").equalsIgnoreCase("true");
                                if (isLoggingEnabled()) {
                                }
                                in = getClass().getResourceAsStream("/TIMESTAMP");
                                if (in != null) {
                                }
                                super.setReceiveUdpBufferSize(new Integer(configurationProperties.getProperty("gov.nist.javax.sip.RECEIVE_UDP_BUFFER_SIZE", MAX_DATAGRAM_SIZE.toString())).intValue());
                                super.setSendUdpBufferSize(new Integer(configurationProperties.getProperty("gov.nist.javax.sip.SEND_UDP_BUFFER_SIZE", MAX_DATAGRAM_SIZE.toString())).intValue());
                                this.stackDoesCongestionControl = Boolean.parseBoolean(configurationProperties.getProperty("gov.nist.javax.sip.CONGESTION_CONTROL_ENABLED", Boolean.TRUE.toString()));
                                this.isBackToBackUserAgent = Boolean.parseBoolean(configurationProperties.getProperty("gov.nist.javax.sip.IS_BACK_TO_BACK_USER_AGENT", Boolean.FALSE.toString()));
                                this.checkBranchId = Boolean.parseBoolean(configurationProperties.getProperty("gov.nist.javax.sip.REJECT_STRAY_RESPONSES", Boolean.FALSE.toString()));
                                this.isDialogTerminatedEventDeliveredForNullDialog = Boolean.parseBoolean(configurationProperties.getProperty("gov.nist.javax.sip.DELIVER_TERMINATED_EVENT_FOR_NULL_DIALOG", Boolean.FALSE.toString()));
                                this.maxForkTime = Integer.parseInt(configurationProperties.getProperty("gov.nist.javax.sip.MAX_FORK_TIME_SECONDS", "0"));
                            }
                        }
                        if (configurationProperties.getProperty("gov.nist.javax.sip.STUN_SERVER") != null) {
                            getStackLogger().logWarning("Ignoring obsolete property gov.nist.javax.sip.STUN_SERVER");
                        }
                        maxMsgSize = configurationProperties.getProperty("gov.nist.javax.sip.MAX_MESSAGE_SIZE");
                        if (maxMsgSize == null) {
                            try {
                                this.maxMessageSize = new Integer(maxMsgSize).intValue();
                                if (this.maxMessageSize < 4096) {
                                    try {
                                        this.maxMessageSize = 4096;
                                    } catch (NumberFormatException e10) {
                                        ex = e10;
                                        if (!isLoggingEnabled()) {
                                        }
                                        String rel222 = configurationProperties.getProperty("gov.nist.javax.sip.REENTRANT_LISTENER");
                                        this.reEntrantListener = rel222 == null && "true".equalsIgnoreCase(rel222);
                                        interval = configurationProperties.getProperty("gov.nist.javax.sip.THREAD_AUDIT_INTERVAL_IN_MILLISECS");
                                        if (interval == null) {
                                        }
                                        setNon2XXAckPassedToListener(Boolean.valueOf(configurationProperties.getProperty("gov.nist.javax.sip.PASS_INVITE_NON_2XX_ACK_TO_LISTENER", "false")).booleanValue());
                                        this.generateTimeStampHeader = Boolean.valueOf(configurationProperties.getProperty("gov.nist.javax.sip.AUTO_GENERATE_TIMESTAMP", "false")).booleanValue();
                                        messageLogFactoryClasspath = configurationProperties.getProperty("gov.nist.javax.sip.LOG_FACTORY");
                                        if (messageLogFactoryClasspath == null) {
                                        }
                                        StringMsgParser.setComputeContentLengthFromMessage(configurationProperties.getProperty("gov.nist.javax.sip.COMPUTE_CONTENT_LENGTH_FROM_MESSAGE_BODY", "false").equalsIgnoreCase("true"));
                                        tlsClientProtocols = configurationProperties.getProperty("gov.nist.javax.sip.TLS_CLIENT_PROTOCOLS");
                                        if (tlsClientProtocols == null) {
                                        }
                                        this.rfc2543Supported = configurationProperties.getProperty("gov.nist.javax.sip.RFC_2543_SUPPORT_ENABLED", "true").equalsIgnoreCase("true");
                                        this.cancelClientTransactionChecked = configurationProperties.getProperty("gov.nist.javax.sip.CANCEL_CLIENT_TRANSACTION_CHECKED", "true").equalsIgnoreCase("true");
                                        this.logStackTraceOnMessageSend = configurationProperties.getProperty("gov.nist.javax.sip.LOG_STACK_TRACE_ON_MESSAGE_SEND", "false").equalsIgnoreCase("true");
                                        if (isLoggingEnabled()) {
                                        }
                                        in = getClass().getResourceAsStream("/TIMESTAMP");
                                        if (in != null) {
                                        }
                                        super.setReceiveUdpBufferSize(new Integer(configurationProperties.getProperty("gov.nist.javax.sip.RECEIVE_UDP_BUFFER_SIZE", MAX_DATAGRAM_SIZE.toString())).intValue());
                                        super.setSendUdpBufferSize(new Integer(configurationProperties.getProperty("gov.nist.javax.sip.SEND_UDP_BUFFER_SIZE", MAX_DATAGRAM_SIZE.toString())).intValue());
                                        this.stackDoesCongestionControl = Boolean.parseBoolean(configurationProperties.getProperty("gov.nist.javax.sip.CONGESTION_CONTROL_ENABLED", Boolean.TRUE.toString()));
                                        this.isBackToBackUserAgent = Boolean.parseBoolean(configurationProperties.getProperty("gov.nist.javax.sip.IS_BACK_TO_BACK_USER_AGENT", Boolean.FALSE.toString()));
                                        this.checkBranchId = Boolean.parseBoolean(configurationProperties.getProperty("gov.nist.javax.sip.REJECT_STRAY_RESPONSES", Boolean.FALSE.toString()));
                                        this.isDialogTerminatedEventDeliveredForNullDialog = Boolean.parseBoolean(configurationProperties.getProperty("gov.nist.javax.sip.DELIVER_TERMINATED_EVENT_FOR_NULL_DIALOG", Boolean.FALSE.toString()));
                                        this.maxForkTime = Integer.parseInt(configurationProperties.getProperty("gov.nist.javax.sip.MAX_FORK_TIME_SECONDS", "0"));
                                    }
                                }
                            } catch (NumberFormatException e11) {
                                ex = e11;
                                if (!isLoggingEnabled()) {
                                    StackLogger stackLogger7 = getStackLogger();
                                    stackLogger7.logError("maxMessageSize - bad value " + ex.getMessage());
                                }
                                String rel2222 = configurationProperties.getProperty("gov.nist.javax.sip.REENTRANT_LISTENER");
                                this.reEntrantListener = rel2222 == null && "true".equalsIgnoreCase(rel2222);
                                interval = configurationProperties.getProperty("gov.nist.javax.sip.THREAD_AUDIT_INTERVAL_IN_MILLISECS");
                                if (interval == null) {
                                }
                                setNon2XXAckPassedToListener(Boolean.valueOf(configurationProperties.getProperty("gov.nist.javax.sip.PASS_INVITE_NON_2XX_ACK_TO_LISTENER", "false")).booleanValue());
                                this.generateTimeStampHeader = Boolean.valueOf(configurationProperties.getProperty("gov.nist.javax.sip.AUTO_GENERATE_TIMESTAMP", "false")).booleanValue();
                                messageLogFactoryClasspath = configurationProperties.getProperty("gov.nist.javax.sip.LOG_FACTORY");
                                if (messageLogFactoryClasspath == null) {
                                }
                                StringMsgParser.setComputeContentLengthFromMessage(configurationProperties.getProperty("gov.nist.javax.sip.COMPUTE_CONTENT_LENGTH_FROM_MESSAGE_BODY", "false").equalsIgnoreCase("true"));
                                tlsClientProtocols = configurationProperties.getProperty("gov.nist.javax.sip.TLS_CLIENT_PROTOCOLS");
                                if (tlsClientProtocols == null) {
                                }
                                this.rfc2543Supported = configurationProperties.getProperty("gov.nist.javax.sip.RFC_2543_SUPPORT_ENABLED", "true").equalsIgnoreCase("true");
                                this.cancelClientTransactionChecked = configurationProperties.getProperty("gov.nist.javax.sip.CANCEL_CLIENT_TRANSACTION_CHECKED", "true").equalsIgnoreCase("true");
                                this.logStackTraceOnMessageSend = configurationProperties.getProperty("gov.nist.javax.sip.LOG_STACK_TRACE_ON_MESSAGE_SEND", "false").equalsIgnoreCase("true");
                                if (isLoggingEnabled()) {
                                }
                                in = getClass().getResourceAsStream("/TIMESTAMP");
                                if (in != null) {
                                }
                                super.setReceiveUdpBufferSize(new Integer(configurationProperties.getProperty("gov.nist.javax.sip.RECEIVE_UDP_BUFFER_SIZE", MAX_DATAGRAM_SIZE.toString())).intValue());
                                super.setSendUdpBufferSize(new Integer(configurationProperties.getProperty("gov.nist.javax.sip.SEND_UDP_BUFFER_SIZE", MAX_DATAGRAM_SIZE.toString())).intValue());
                                this.stackDoesCongestionControl = Boolean.parseBoolean(configurationProperties.getProperty("gov.nist.javax.sip.CONGESTION_CONTROL_ENABLED", Boolean.TRUE.toString()));
                                this.isBackToBackUserAgent = Boolean.parseBoolean(configurationProperties.getProperty("gov.nist.javax.sip.IS_BACK_TO_BACK_USER_AGENT", Boolean.FALSE.toString()));
                                this.checkBranchId = Boolean.parseBoolean(configurationProperties.getProperty("gov.nist.javax.sip.REJECT_STRAY_RESPONSES", Boolean.FALSE.toString()));
                                this.isDialogTerminatedEventDeliveredForNullDialog = Boolean.parseBoolean(configurationProperties.getProperty("gov.nist.javax.sip.DELIVER_TERMINATED_EVENT_FOR_NULL_DIALOG", Boolean.FALSE.toString()));
                                this.maxForkTime = Integer.parseInt(configurationProperties.getProperty("gov.nist.javax.sip.MAX_FORK_TIME_SECONDS", "0"));
                            }
                        } else {
                            this.maxMessageSize = 0;
                        }
                        String rel22222 = configurationProperties.getProperty("gov.nist.javax.sip.REENTRANT_LISTENER");
                        this.reEntrantListener = rel22222 == null && "true".equalsIgnoreCase(rel22222);
                        interval = configurationProperties.getProperty("gov.nist.javax.sip.THREAD_AUDIT_INTERVAL_IN_MILLISECS");
                        if (interval == null) {
                            try {
                                try {
                                    getThreadAuditor().setPingIntervalInMillisecs(Long.valueOf(interval).longValue() / 2);
                                } catch (NumberFormatException e12) {
                                    ex = e12;
                                    if (!isLoggingEnabled()) {
                                    }
                                    setNon2XXAckPassedToListener(Boolean.valueOf(configurationProperties.getProperty("gov.nist.javax.sip.PASS_INVITE_NON_2XX_ACK_TO_LISTENER", "false")).booleanValue());
                                    this.generateTimeStampHeader = Boolean.valueOf(configurationProperties.getProperty("gov.nist.javax.sip.AUTO_GENERATE_TIMESTAMP", "false")).booleanValue();
                                    messageLogFactoryClasspath = configurationProperties.getProperty("gov.nist.javax.sip.LOG_FACTORY");
                                    if (messageLogFactoryClasspath == null) {
                                    }
                                    StringMsgParser.setComputeContentLengthFromMessage(configurationProperties.getProperty("gov.nist.javax.sip.COMPUTE_CONTENT_LENGTH_FROM_MESSAGE_BODY", "false").equalsIgnoreCase("true"));
                                    tlsClientProtocols = configurationProperties.getProperty("gov.nist.javax.sip.TLS_CLIENT_PROTOCOLS");
                                    if (tlsClientProtocols == null) {
                                    }
                                    this.rfc2543Supported = configurationProperties.getProperty("gov.nist.javax.sip.RFC_2543_SUPPORT_ENABLED", "true").equalsIgnoreCase("true");
                                    this.cancelClientTransactionChecked = configurationProperties.getProperty("gov.nist.javax.sip.CANCEL_CLIENT_TRANSACTION_CHECKED", "true").equalsIgnoreCase("true");
                                    this.logStackTraceOnMessageSend = configurationProperties.getProperty("gov.nist.javax.sip.LOG_STACK_TRACE_ON_MESSAGE_SEND", "false").equalsIgnoreCase("true");
                                    if (isLoggingEnabled()) {
                                    }
                                    in = getClass().getResourceAsStream("/TIMESTAMP");
                                    if (in != null) {
                                    }
                                    super.setReceiveUdpBufferSize(new Integer(configurationProperties.getProperty("gov.nist.javax.sip.RECEIVE_UDP_BUFFER_SIZE", MAX_DATAGRAM_SIZE.toString())).intValue());
                                    super.setSendUdpBufferSize(new Integer(configurationProperties.getProperty("gov.nist.javax.sip.SEND_UDP_BUFFER_SIZE", MAX_DATAGRAM_SIZE.toString())).intValue());
                                    this.stackDoesCongestionControl = Boolean.parseBoolean(configurationProperties.getProperty("gov.nist.javax.sip.CONGESTION_CONTROL_ENABLED", Boolean.TRUE.toString()));
                                    this.isBackToBackUserAgent = Boolean.parseBoolean(configurationProperties.getProperty("gov.nist.javax.sip.IS_BACK_TO_BACK_USER_AGENT", Boolean.FALSE.toString()));
                                    this.checkBranchId = Boolean.parseBoolean(configurationProperties.getProperty("gov.nist.javax.sip.REJECT_STRAY_RESPONSES", Boolean.FALSE.toString()));
                                    this.isDialogTerminatedEventDeliveredForNullDialog = Boolean.parseBoolean(configurationProperties.getProperty("gov.nist.javax.sip.DELIVER_TERMINATED_EVENT_FOR_NULL_DIALOG", Boolean.FALSE.toString()));
                                    this.maxForkTime = Integer.parseInt(configurationProperties.getProperty("gov.nist.javax.sip.MAX_FORK_TIME_SECONDS", "0"));
                                }
                            } catch (NumberFormatException e13) {
                                ex = e13;
                                if (!isLoggingEnabled()) {
                                    StackLogger stackLogger8 = getStackLogger();
                                    stackLogger8.logError("THREAD_AUDIT_INTERVAL_IN_MILLISECS - bad value [" + interval + "] " + ex.getMessage());
                                }
                                setNon2XXAckPassedToListener(Boolean.valueOf(configurationProperties.getProperty("gov.nist.javax.sip.PASS_INVITE_NON_2XX_ACK_TO_LISTENER", "false")).booleanValue());
                                this.generateTimeStampHeader = Boolean.valueOf(configurationProperties.getProperty("gov.nist.javax.sip.AUTO_GENERATE_TIMESTAMP", "false")).booleanValue();
                                messageLogFactoryClasspath = configurationProperties.getProperty("gov.nist.javax.sip.LOG_FACTORY");
                                if (messageLogFactoryClasspath == null) {
                                }
                                StringMsgParser.setComputeContentLengthFromMessage(configurationProperties.getProperty("gov.nist.javax.sip.COMPUTE_CONTENT_LENGTH_FROM_MESSAGE_BODY", "false").equalsIgnoreCase("true"));
                                tlsClientProtocols = configurationProperties.getProperty("gov.nist.javax.sip.TLS_CLIENT_PROTOCOLS");
                                if (tlsClientProtocols == null) {
                                }
                                this.rfc2543Supported = configurationProperties.getProperty("gov.nist.javax.sip.RFC_2543_SUPPORT_ENABLED", "true").equalsIgnoreCase("true");
                                this.cancelClientTransactionChecked = configurationProperties.getProperty("gov.nist.javax.sip.CANCEL_CLIENT_TRANSACTION_CHECKED", "true").equalsIgnoreCase("true");
                                this.logStackTraceOnMessageSend = configurationProperties.getProperty("gov.nist.javax.sip.LOG_STACK_TRACE_ON_MESSAGE_SEND", "false").equalsIgnoreCase("true");
                                if (isLoggingEnabled()) {
                                }
                                in = getClass().getResourceAsStream("/TIMESTAMP");
                                if (in != null) {
                                }
                                super.setReceiveUdpBufferSize(new Integer(configurationProperties.getProperty("gov.nist.javax.sip.RECEIVE_UDP_BUFFER_SIZE", MAX_DATAGRAM_SIZE.toString())).intValue());
                                super.setSendUdpBufferSize(new Integer(configurationProperties.getProperty("gov.nist.javax.sip.SEND_UDP_BUFFER_SIZE", MAX_DATAGRAM_SIZE.toString())).intValue());
                                this.stackDoesCongestionControl = Boolean.parseBoolean(configurationProperties.getProperty("gov.nist.javax.sip.CONGESTION_CONTROL_ENABLED", Boolean.TRUE.toString()));
                                this.isBackToBackUserAgent = Boolean.parseBoolean(configurationProperties.getProperty("gov.nist.javax.sip.IS_BACK_TO_BACK_USER_AGENT", Boolean.FALSE.toString()));
                                this.checkBranchId = Boolean.parseBoolean(configurationProperties.getProperty("gov.nist.javax.sip.REJECT_STRAY_RESPONSES", Boolean.FALSE.toString()));
                                this.isDialogTerminatedEventDeliveredForNullDialog = Boolean.parseBoolean(configurationProperties.getProperty("gov.nist.javax.sip.DELIVER_TERMINATED_EVENT_FOR_NULL_DIALOG", Boolean.FALSE.toString()));
                                this.maxForkTime = Integer.parseInt(configurationProperties.getProperty("gov.nist.javax.sip.MAX_FORK_TIME_SECONDS", "0"));
                            }
                        }
                        setNon2XXAckPassedToListener(Boolean.valueOf(configurationProperties.getProperty("gov.nist.javax.sip.PASS_INVITE_NON_2XX_ACK_TO_LISTENER", "false")).booleanValue());
                        this.generateTimeStampHeader = Boolean.valueOf(configurationProperties.getProperty("gov.nist.javax.sip.AUTO_GENERATE_TIMESTAMP", "false")).booleanValue();
                        messageLogFactoryClasspath = configurationProperties.getProperty("gov.nist.javax.sip.LOG_FACTORY");
                        if (messageLogFactoryClasspath == null) {
                            try {
                                this.logRecordFactory = (LogRecordFactory) Class.forName(messageLogFactoryClasspath).getConstructor(new Class[0]).newInstance(new Object[0]);
                            } catch (Exception e14) {
                                if (isLoggingEnabled()) {
                                    getStackLogger().logError("Bad configuration value for LOG_FACTORY -- using default logger");
                                }
                                this.logRecordFactory = new DefaultMessageLogFactory();
                            }
                        } else {
                            this.logRecordFactory = new DefaultMessageLogFactory();
                        }
                        StringMsgParser.setComputeContentLengthFromMessage(configurationProperties.getProperty("gov.nist.javax.sip.COMPUTE_CONTENT_LENGTH_FROM_MESSAGE_BODY", "false").equalsIgnoreCase("true"));
                        tlsClientProtocols = configurationProperties.getProperty("gov.nist.javax.sip.TLS_CLIENT_PROTOCOLS");
                        if (tlsClientProtocols == null) {
                            StringTokenizer st3 = new StringTokenizer(tlsClientProtocols, " ,");
                            String[] protocols = new String[st3.countTokens()];
                            int i = 0;
                            while (st3.hasMoreTokens()) {
                                protocols[i] = st3.nextToken();
                                i++;
                            }
                            this.enabledProtocols = protocols;
                        }
                        this.rfc2543Supported = configurationProperties.getProperty("gov.nist.javax.sip.RFC_2543_SUPPORT_ENABLED", "true").equalsIgnoreCase("true");
                        this.cancelClientTransactionChecked = configurationProperties.getProperty("gov.nist.javax.sip.CANCEL_CLIENT_TRANSACTION_CHECKED", "true").equalsIgnoreCase("true");
                        this.logStackTraceOnMessageSend = configurationProperties.getProperty("gov.nist.javax.sip.LOG_STACK_TRACE_ON_MESSAGE_SEND", "false").equalsIgnoreCase("true");
                        if (isLoggingEnabled()) {
                            StackLogger stackLogger9 = getStackLogger();
                            stackLogger9.logDebug("created Sip stack. Properties = " + configurationProperties);
                        }
                        in = getClass().getResourceAsStream("/TIMESTAMP");
                        if (in != null) {
                            try {
                                String buildTimeStamp = new BufferedReader(new InputStreamReader(in)).readLine();
                                in.close();
                                getStackLogger().setBuildTimeStamp(buildTimeStamp);
                            } catch (IOException e15) {
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
                    } catch (InvocationTargetException ex1) {
                        getStackLogger().logError("could not instantiate router -- invocation target problem", (Exception) ex1.getCause());
                        throw new PeerUnavailableException("Cound not instantiate router - check constructor", ex1);
                    } catch (Exception ex5) {
                        getStackLogger().logError("could not instantiate router", (Exception) ex5.getCause());
                        throw new PeerUnavailableException("Could not instantiate router", ex5);
                    }
                } catch (InvocationTargetException ex12) {
                    throw new IllegalArgumentException("Cound not instantiate server logger " + stackLoggerClassName + "- check that it is present on the classpath and that there is a no-args constructor defined", ex12);
                } catch (Exception ex6) {
                    throw new IllegalArgumentException("Cound not instantiate server logger " + stackLoggerClassName + "- check that it is present on the classpath and that there is a no-args constructor defined", ex6);
                }
            } catch (InvocationTargetException ex13) {
                throw new IllegalArgumentException("Cound not instantiate stack logger " + stackLoggerClassName + "- check that it is present on the classpath and that there is a no-args constructor defined", ex13);
            } catch (Exception ex7) {
                throw new IllegalArgumentException("Cound not instantiate stack logger " + stackLoggerClassName + "- check that it is present on the classpath and that there is a no-args constructor defined", ex7);
            }
        } else {
            throw new PeerUnavailableException("stack name is missing");
        }
    }

    @Override // javax.sip.SipStack
    public synchronized ListeningPoint createListeningPoint(String address, int port, String transport) throws TransportNotSupportedException, InvalidArgumentException {
        if (isLoggingEnabled()) {
            StackLogger stackLogger = getStackLogger();
            stackLogger.logDebug("createListeningPoint : address = " + address + " port = " + port + " transport = " + transport);
        }
        if (address == null) {
            throw new NullPointerException("Address for listening point is null!");
        } else if (transport == null) {
            throw new NullPointerException("null transport");
        } else if (port > 0) {
            if (!transport.equalsIgnoreCase(ListeningPoint.UDP) && !transport.equalsIgnoreCase(ListeningPoint.TLS) && !transport.equalsIgnoreCase(ListeningPoint.TCP)) {
                if (!transport.equalsIgnoreCase(ListeningPoint.SCTP)) {
                    throw new TransportNotSupportedException("bad transport " + transport);
                }
            }
            if (!isAlive()) {
                this.toExit = false;
                reInitialize();
            }
            String key = ListeningPointImpl.makeKey(address, port, transport);
            ListeningPointImpl lip = this.listeningPoints.get(key);
            if (lip != null) {
                return lip;
            }
            try {
                MessageProcessor messageProcessor = createMessageProcessor(InetAddress.getByName(address), port, transport);
                if (isLoggingEnabled()) {
                    StackLogger stackLogger2 = getStackLogger();
                    stackLogger2.logDebug("Created Message Processor: " + address + " port = " + port + " transport = " + transport);
                }
                ListeningPointImpl lip2 = new ListeningPointImpl(this, port, transport);
                lip2.messageProcessor = messageProcessor;
                messageProcessor.setListeningPoint(lip2);
                this.listeningPoints.put(key, lip2);
                messageProcessor.start();
                return lip2;
            } catch (IOException ex) {
                if (isLoggingEnabled()) {
                    StackLogger stackLogger3 = getStackLogger();
                    stackLogger3.logError("Invalid argument address = " + address + " port = " + port + " transport = " + transport);
                }
                throw new InvalidArgumentException(ex.getMessage(), ex);
            }
        } else {
            throw new InvalidArgumentException("bad port");
        }
    }

    @Override // javax.sip.SipStack
    public SipProvider createSipProvider(ListeningPoint listeningPoint) throws ObjectInUseException {
        if (listeningPoint != null) {
            if (isLoggingEnabled()) {
                StackLogger stackLogger = getStackLogger();
                stackLogger.logDebug("createSipProvider: " + listeningPoint);
            }
            ListeningPointImpl listeningPointImpl = (ListeningPointImpl) listeningPoint;
            if (listeningPointImpl.sipProvider == null) {
                SipProviderImpl provider = new SipProviderImpl(this);
                provider.setListeningPoint(listeningPointImpl);
                listeningPointImpl.sipProvider = provider;
                this.sipProviders.add(provider);
                return provider;
            }
            throw new ObjectInUseException("Provider already attached!");
        }
        throw new NullPointerException("null listeningPoint");
    }

    @Override // javax.sip.SipStack
    public void deleteListeningPoint(ListeningPoint listeningPoint) throws ObjectInUseException {
        if (listeningPoint != null) {
            ListeningPointImpl lip = (ListeningPointImpl) listeningPoint;
            super.removeMessageProcessor(lip.messageProcessor);
            this.listeningPoints.remove(lip.getKey());
            return;
        }
        throw new NullPointerException("null listeningPoint arg");
    }

    @Override // javax.sip.SipStack
    public void deleteSipProvider(SipProvider sipProvider) throws ObjectInUseException {
        if (sipProvider != null) {
            SipProviderImpl sipProviderImpl = (SipProviderImpl) sipProvider;
            if (sipProviderImpl.getSipListener() == null) {
                sipProviderImpl.removeListeningPoints();
                sipProviderImpl.stop();
                this.sipProviders.remove(sipProvider);
                if (this.sipProviders.isEmpty()) {
                    stopStack();
                    return;
                }
                return;
            }
            throw new ObjectInUseException("SipProvider still has an associated SipListener!");
        }
        throw new NullPointerException("null provider arg");
    }

    @Override // javax.sip.SipStack
    public String getIPAddress() {
        return super.getHostAddress();
    }

    @Override // javax.sip.SipStack
    public Iterator getListeningPoints() {
        return this.listeningPoints.values().iterator();
    }

    @Override // javax.sip.SipStack
    public boolean isRetransmissionFilterActive() {
        return true;
    }

    @Override // javax.sip.SipStack
    public Iterator<SipProviderImpl> getSipProviders() {
        return this.sipProviders.iterator();
    }

    @Override // javax.sip.SipStack
    public String getStackName() {
        return this.stackName;
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public void finalize() {
        stopStack();
    }

    @Override // javax.sip.SipStack
    public ListeningPoint createListeningPoint(int port, String transport) throws TransportNotSupportedException, InvalidArgumentException {
        if (this.stackAddress != null) {
            return createListeningPoint(this.stackAddress, port, transport);
        }
        throw new NullPointerException("Stack does not have a default IP Address!");
    }

    @Override // javax.sip.SipStack
    public void stop() {
        if (isLoggingEnabled()) {
            getStackLogger().logDebug("stopStack -- stoppping the stack");
        }
        stopStack();
        this.sipProviders = new LinkedList<>();
        this.listeningPoints = new Hashtable<>();
        EventScanner eventScanner2 = this.eventScanner;
        if (eventScanner2 != null) {
            eventScanner2.forceStop();
        }
        this.eventScanner = null;
    }

    @Override // javax.sip.SipStack
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

    @Override // gov.nist.javax.sip.SipStackExt
    public AuthenticationHelper getAuthenticationHelper(AccountManager accountManager, HeaderFactory headerFactory) {
        return new AuthenticationHelperImpl(this, accountManager, headerFactory);
    }

    @Override // gov.nist.javax.sip.SipStackExt
    public AuthenticationHelper getSecureAuthenticationHelper(SecureAccountManager accountManager, HeaderFactory headerFactory) {
        return new AuthenticationHelperImpl(this, accountManager, headerFactory);
    }

    @Override // gov.nist.javax.sip.SipStackExt
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
