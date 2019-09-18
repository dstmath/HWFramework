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

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    /* JADX WARNING: Removed duplicated region for block: B:116:0x02e1 A[SYNTHETIC, Splitter:B:116:0x02e1] */
    /* JADX WARNING: Removed duplicated region for block: B:126:0x0322 A[SYNTHETIC, Splitter:B:126:0x0322] */
    /* JADX WARNING: Removed duplicated region for block: B:134:0x0361  */
    /* JADX WARNING: Removed duplicated region for block: B:137:0x036e A[SYNTHETIC, Splitter:B:137:0x036e] */
    /* JADX WARNING: Removed duplicated region for block: B:146:0x03b0  */
    /* JADX WARNING: Removed duplicated region for block: B:159:0x03f0 A[SYNTHETIC, Splitter:B:159:0x03f0] */
    /* JADX WARNING: Removed duplicated region for block: B:180:0x042f  */
    /* JADX WARNING: Removed duplicated region for block: B:181:0x044a  */
    /* JADX WARNING: Removed duplicated region for block: B:184:0x0456  */
    /* JADX WARNING: Removed duplicated region for block: B:187:0x0468 A[SYNTHETIC, Splitter:B:187:0x0468] */
    /* JADX WARNING: Removed duplicated region for block: B:195:0x0482 A[Catch:{ NumberFormatException -> 0x048d }] */
    /* JADX WARNING: Removed duplicated region for block: B:200:0x0494  */
    /* JADX WARNING: Removed duplicated region for block: B:201:0x04b5  */
    /* JADX WARNING: Removed duplicated region for block: B:204:0x04c1  */
    /* JADX WARNING: Removed duplicated region for block: B:210:0x04d6 A[SYNTHETIC, Splitter:B:210:0x04d6] */
    /* JADX WARNING: Removed duplicated region for block: B:221:0x04fa  */
    /* JADX WARNING: Removed duplicated region for block: B:222:0x0521  */
    /* JADX WARNING: Removed duplicated region for block: B:223:0x0524  */
    /* JADX WARNING: Removed duplicated region for block: B:226:0x0555 A[SYNTHETIC, Splitter:B:226:0x0555] */
    /* JADX WARNING: Removed duplicated region for block: B:238:0x0588  */
    /* JADX WARNING: Removed duplicated region for block: B:241:0x05a9  */
    /* JADX WARNING: Removed duplicated region for block: B:246:0x05ce  */
    /* JADX WARNING: Removed duplicated region for block: B:249:0x0606  */
    /* JADX WARNING: Removed duplicated region for block: B:250:0x0621  */
    /* JADX WARNING: Removed duplicated region for block: B:253:0x062f  */
    /* JADX WARNING: Removed duplicated region for block: B:268:0x0663  */
    public SipStackImpl(Properties configurationProperties) throws PeerUnavailableException {
        this();
        String threadPoolSize;
        String serverTransactionTableSize;
        String clientTransactionTableSize;
        boolean z;
        String readTimeout;
        String stunAddr;
        String maxMsgSize;
        String interval;
        String messageLogFactoryClasspath;
        String tlsClientProtocols;
        InputStream in;
        Properties properties = configurationProperties;
        String address = properties.getProperty("javax.sip.IP_ADDRESS");
        if (address != null) {
            try {
                super.setHostAddress(address);
            } catch (UnknownHostException e) {
                UnknownHostException unknownHostException = e;
                throw new PeerUnavailableException("bad address " + address);
            }
        }
        String name = properties.getProperty("javax.sip.STACK_NAME");
        if (name != null) {
            super.setStackName(name);
            String stackLoggerClassName = properties.getProperty("gov.nist.javax.sip.STACK_LOGGER") == null ? "gov.nist.core.LogWriter" : properties.getProperty("gov.nist.javax.sip.STACK_LOGGER");
            try {
                StackLogger stackLogger = (StackLogger) Class.forName(stackLoggerClassName).getConstructor(new Class[0]).newInstance(new Object[0]);
                stackLogger.setStackProperties(properties);
                super.setStackLogger(stackLogger);
                String serverLoggerClassName = properties.getProperty("gov.nist.javax.sip.SERVER_LOGGER") == null ? "gov.nist.javax.sip.stack.ServerLog" : properties.getProperty("gov.nist.javax.sip.SERVER_LOGGER");
                try {
                    this.serverLogger = (ServerLogger) Class.forName(serverLoggerClassName).getConstructor(new Class[0]).newInstance(new Object[0]);
                    this.serverLogger.setSipStack(this);
                    this.serverLogger.setStackProperties(properties);
                    this.outboundProxy = properties.getProperty("javax.sip.OUTBOUND_PROXY");
                    this.defaultRouter = new DefaultRouter(this, this.outboundProxy);
                    try {
                        super.setRouter((Router) Class.forName(properties.getProperty("javax.sip.ROUTER_PATH") == null ? "gov.nist.javax.sip.stack.DefaultRouter" : properties.getProperty("javax.sip.ROUTER_PATH")).getConstructor(new Class[]{SipStack.class, String.class}).newInstance(new Object[]{this, this.outboundProxy}));
                        String useRouterForAll = properties.getProperty("javax.sip.USE_ROUTER_FOR_ALL_URIS");
                        this.useRouterForAll = true;
                        if (useRouterForAll != null) {
                            this.useRouterForAll = "true".equalsIgnoreCase(useRouterForAll);
                        }
                        String extensionMethods = properties.getProperty("javax.sip.EXTENSION_METHODS");
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
                        String keyStoreFile = properties.getProperty("javax.net.ssl.keyStore");
                        String trustStoreFile = properties.getProperty("javax.net.ssl.trustStore");
                        if (keyStoreFile != null) {
                            try {
                                this.networkLayer = new SslNetworkLayer(trustStoreFile == null ? keyStoreFile : trustStoreFile, keyStoreFile, properties.getProperty("javax.net.ssl.keyStorePassword").toCharArray(), properties.getProperty("javax.net.ssl.keyStoreType"));
                            } catch (Exception e1) {
                                getStackLogger().logError("could not instantiate SSL networking", e1);
                            }
                        }
                        this.isAutomaticDialogSupportEnabled = properties.getProperty("javax.sip.AUTOMATIC_DIALOG_SUPPORT", "on").equalsIgnoreCase("on");
                        this.isAutomaticDialogErrorHandlingEnabled = properties.getProperty("gov.nist.javax.sip.AUTOMATIC_DIALOG_ERROR_HANDLING", "true").equals(Boolean.TRUE.toString());
                        if (this.isAutomaticDialogSupportEnabled) {
                            this.isAutomaticDialogErrorHandlingEnabled = true;
                        }
                        if (properties.getProperty("gov.nist.javax.sip.MAX_LISTENER_RESPONSE_TIME") != null) {
                            this.maxListenerResponseTime = Integer.parseInt(properties.getProperty("gov.nist.javax.sip.MAX_LISTENER_RESPONSE_TIME"));
                            if (this.maxListenerResponseTime <= 0) {
                                throw new PeerUnavailableException("Bad configuration parameter gov.nist.javax.sip.MAX_LISTENER_RESPONSE_TIME : should be positive");
                            }
                        } else {
                            this.maxListenerResponseTime = -1;
                        }
                        this.deliverTerminatedEventForAck = properties.getProperty("gov.nist.javax.sip.DELIVER_TERMINATED_EVENT_FOR_ACK", "false").equalsIgnoreCase("true");
                        this.deliverUnsolicitedNotify = properties.getProperty("gov.nist.javax.sip.DELIVER_UNSOLICITED_NOTIFY", "false").equalsIgnoreCase("true");
                        String forkedSubscriptions = properties.getProperty("javax.sip.FORKABLE_EVENTS");
                        if (forkedSubscriptions != null) {
                            StringTokenizer st2 = new StringTokenizer(forkedSubscriptions);
                            while (st2.hasMoreTokens()) {
                                this.forkedEvents.add(st2.nextToken());
                            }
                        }
                        if (properties.containsKey("gov.nist.javax.sip.NETWORK_LAYER")) {
                            String path = properties.getProperty("gov.nist.javax.sip.NETWORK_LAYER");
                            try {
                                Class<?> clazz = Class.forName(path);
                                String str = address;
                                try {
                                    Class<?> cls = clazz;
                                    this.networkLayer = (NetworkLayer) clazz.getConstructor(new Class[0]).newInstance(new Object[0]);
                                } catch (Exception e2) {
                                    e = e2;
                                }
                            } catch (Exception e3) {
                                e = e3;
                                String str2 = address;
                                StringBuilder sb = new StringBuilder();
                                Exception exc = e;
                                sb.append("can't find or instantiate NetworkLayer implementation: ");
                                sb.append(path);
                                throw new PeerUnavailableException(sb.toString());
                            }
                        }
                        if (properties.containsKey("gov.nist.javax.sip.ADDRESS_RESOLVER")) {
                            String path2 = properties.getProperty("gov.nist.javax.sip.ADDRESS_RESOLVER");
                            try {
                                Class<?> clazz2 = Class.forName(path2);
                                Object obj = "gov.nist.javax.sip.ADDRESS_RESOLVER";
                                try {
                                    Class<?> cls2 = clazz2;
                                    this.addressResolver = (AddressResolver) clazz2.getConstructor(new Class[0]).newInstance(new Object[0]);
                                } catch (Exception e4) {
                                    e = e4;
                                }
                            } catch (Exception e5) {
                                e = e5;
                                Object obj2 = "gov.nist.javax.sip.ADDRESS_RESOLVER";
                                StringBuilder sb2 = new StringBuilder();
                                Exception exc2 = e;
                                sb2.append("can't find or instantiate AddressResolver implementation: ");
                                sb2.append(path2);
                                throw new PeerUnavailableException(sb2.toString());
                            }
                        } else {
                            String ADDRESS_RESOLVER_KEY = "gov.nist.javax.sip.ADDRESS_RESOLVER";
                        }
                        String maxConnections = properties.getProperty("gov.nist.javax.sip.MAX_CONNECTIONS");
                        if (maxConnections != null) {
                            try {
                                this.maxConnections = new Integer(maxConnections).intValue();
                                String str3 = maxConnections;
                            } catch (NumberFormatException ex) {
                                if (isLoggingEnabled()) {
                                    StackLogger stackLogger2 = getStackLogger();
                                    StringBuilder sb3 = new StringBuilder();
                                    String str4 = maxConnections;
                                    sb3.append("max connections - bad value ");
                                    sb3.append(ex.getMessage());
                                    stackLogger2.logError(sb3.toString());
                                }
                            }
                            threadPoolSize = properties.getProperty("gov.nist.javax.sip.THREAD_POOL_SIZE");
                            if (threadPoolSize != null) {
                                try {
                                    this.threadPoolSize = new Integer(threadPoolSize).intValue();
                                    String str5 = threadPoolSize;
                                } catch (NumberFormatException ex2) {
                                    if (isLoggingEnabled()) {
                                        StackLogger stackLogger3 = getStackLogger();
                                        StringBuilder sb4 = new StringBuilder();
                                        String str6 = threadPoolSize;
                                        sb4.append("thread pool size - bad value ");
                                        sb4.append(ex2.getMessage());
                                        stackLogger3.logError(sb4.toString());
                                    }
                                }
                                serverTransactionTableSize = properties.getProperty("gov.nist.javax.sip.MAX_SERVER_TRANSACTIONS");
                                if (serverTransactionTableSize != null) {
                                    try {
                                        this.serverTransactionTableHighwaterMark = new Integer(serverTransactionTableSize).intValue();
                                        this.serverTransactionTableLowaterMark = (this.serverTransactionTableHighwaterMark * 80) / 100;
                                        String str7 = serverTransactionTableSize;
                                    } catch (NumberFormatException ex3) {
                                        if (isLoggingEnabled()) {
                                            StackLogger stackLogger4 = getStackLogger();
                                            StringBuilder sb5 = new StringBuilder();
                                            String str8 = serverTransactionTableSize;
                                            sb5.append("transaction table size - bad value ");
                                            sb5.append(ex3.getMessage());
                                            stackLogger4.logError(sb5.toString());
                                        }
                                    }
                                } else {
                                    this.unlimitedServerTransactionTableSize = true;
                                }
                                clientTransactionTableSize = properties.getProperty("gov.nist.javax.sip.MAX_CLIENT_TRANSACTIONS");
                                if (clientTransactionTableSize != null) {
                                    try {
                                        this.clientTransactionTableHiwaterMark = new Integer(clientTransactionTableSize).intValue();
                                        this.clientTransactionTableLowaterMark = (this.clientTransactionTableLowaterMark * 80) / 100;
                                        String str9 = clientTransactionTableSize;
                                    } catch (NumberFormatException ex4) {
                                        if (isLoggingEnabled()) {
                                            StackLogger stackLogger5 = getStackLogger();
                                            StringBuilder sb6 = new StringBuilder();
                                            String str10 = clientTransactionTableSize;
                                            sb6.append("transaction table size - bad value ");
                                            sb6.append(ex4.getMessage());
                                            stackLogger5.logError(sb6.toString());
                                        }
                                    }
                                    z = true;
                                } else {
                                    z = true;
                                    this.unlimitedClientTransactionTableSize = true;
                                }
                                this.cacheServerConnections = z;
                                String flag = properties.getProperty("gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS");
                                if (flag != null && "false".equalsIgnoreCase(flag.trim())) {
                                    this.cacheServerConnections = false;
                                }
                                this.cacheClientConnections = true;
                                String cacheflag = properties.getProperty("gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS");
                                if (cacheflag != null && "false".equalsIgnoreCase(cacheflag.trim())) {
                                    this.cacheClientConnections = false;
                                }
                                readTimeout = properties.getProperty("gov.nist.javax.sip.READ_TIMEOUT");
                                if (readTimeout != null) {
                                    try {
                                        int rt = Integer.parseInt(readTimeout);
                                        String str11 = flag;
                                        if (rt >= 100) {
                                            try {
                                                this.readTimeout = rt;
                                                String str12 = name;
                                            } catch (NumberFormatException e6) {
                                                nfe = e6;
                                                String str13 = name;
                                                if (isLoggingEnabled()) {
                                                }
                                                stunAddr = properties.getProperty("gov.nist.javax.sip.STUN_SERVER");
                                                if (stunAddr != null) {
                                                }
                                                maxMsgSize = properties.getProperty("gov.nist.javax.sip.MAX_MESSAGE_SIZE");
                                                if (maxMsgSize == null) {
                                                }
                                                String str14 = maxMsgSize;
                                                String str15 = forkedSubscriptions;
                                                String rel = properties.getProperty("gov.nist.javax.sip.REENTRANT_LISTENER");
                                                this.reEntrantListener = rel == null && "true".equalsIgnoreCase(rel);
                                                interval = properties.getProperty("gov.nist.javax.sip.THREAD_AUDIT_INTERVAL_IN_MILLISECS");
                                                if (interval == null) {
                                                }
                                                setNon2XXAckPassedToListener(Boolean.valueOf(properties.getProperty("gov.nist.javax.sip.PASS_INVITE_NON_2XX_ACK_TO_LISTENER", "false")).booleanValue());
                                                this.generateTimeStampHeader = Boolean.valueOf(properties.getProperty("gov.nist.javax.sip.AUTO_GENERATE_TIMESTAMP", "false")).booleanValue();
                                                messageLogFactoryClasspath = properties.getProperty("gov.nist.javax.sip.LOG_FACTORY");
                                                if (messageLogFactoryClasspath == null) {
                                                }
                                                StringMsgParser.setComputeContentLengthFromMessage(properties.getProperty("gov.nist.javax.sip.COMPUTE_CONTENT_LENGTH_FROM_MESSAGE_BODY", "false").equalsIgnoreCase("true"));
                                                tlsClientProtocols = properties.getProperty("gov.nist.javax.sip.TLS_CLIENT_PROTOCOLS");
                                                if (tlsClientProtocols != null) {
                                                }
                                                this.rfc2543Supported = properties.getProperty("gov.nist.javax.sip.RFC_2543_SUPPORT_ENABLED", "true").equalsIgnoreCase("true");
                                                this.cancelClientTransactionChecked = properties.getProperty("gov.nist.javax.sip.CANCEL_CLIENT_TRANSACTION_CHECKED", "true").equalsIgnoreCase("true");
                                                this.logStackTraceOnMessageSend = properties.getProperty("gov.nist.javax.sip.LOG_STACK_TRACE_ON_MESSAGE_SEND", "false").equalsIgnoreCase("true");
                                                if (isLoggingEnabled()) {
                                                }
                                                in = getClass().getResourceAsStream("/TIMESTAMP");
                                                if (in != null) {
                                                }
                                                String bufferSize = properties.getProperty("gov.nist.javax.sip.RECEIVE_UDP_BUFFER_SIZE", MAX_DATAGRAM_SIZE.toString());
                                                super.setReceiveUdpBufferSize(new Integer(bufferSize).intValue());
                                                String str16 = bufferSize;
                                                String bufferSize2 = properties.getProperty("gov.nist.javax.sip.SEND_UDP_BUFFER_SIZE", MAX_DATAGRAM_SIZE.toString());
                                                super.setSendUdpBufferSize(new Integer(bufferSize2).intValue());
                                                String str17 = bufferSize2;
                                                boolean congetstionControlEnabled = Boolean.parseBoolean(properties.getProperty("gov.nist.javax.sip.CONGESTION_CONTROL_ENABLED", Boolean.TRUE.toString()));
                                                this.stackDoesCongestionControl = congetstionControlEnabled;
                                                boolean z2 = congetstionControlEnabled;
                                                this.isBackToBackUserAgent = Boolean.parseBoolean(properties.getProperty("gov.nist.javax.sip.IS_BACK_TO_BACK_USER_AGENT", Boolean.FALSE.toString()));
                                                this.checkBranchId = Boolean.parseBoolean(properties.getProperty("gov.nist.javax.sip.REJECT_STRAY_RESPONSES", Boolean.FALSE.toString()));
                                                this.isDialogTerminatedEventDeliveredForNullDialog = Boolean.parseBoolean(properties.getProperty("gov.nist.javax.sip.DELIVER_TERMINATED_EVENT_FOR_NULL_DIALOG", Boolean.FALSE.toString()));
                                                this.maxForkTime = Integer.parseInt(properties.getProperty("gov.nist.javax.sip.MAX_FORK_TIME_SECONDS", "0"));
                                            }
                                        } else {
                                            try {
                                                PrintStream printStream = System.err;
                                                int i = rt;
                                                StringBuilder sb7 = new StringBuilder();
                                                String str18 = name;
                                                try {
                                                    sb7.append("Value too low ");
                                                    sb7.append(readTimeout);
                                                    printStream.println(sb7.toString());
                                                } catch (NumberFormatException e7) {
                                                    nfe = e7;
                                                }
                                            } catch (NumberFormatException e8) {
                                                nfe = e8;
                                                String str19 = name;
                                                if (isLoggingEnabled()) {
                                                }
                                                stunAddr = properties.getProperty("gov.nist.javax.sip.STUN_SERVER");
                                                if (stunAddr != null) {
                                                }
                                                maxMsgSize = properties.getProperty("gov.nist.javax.sip.MAX_MESSAGE_SIZE");
                                                if (maxMsgSize == null) {
                                                }
                                                String str142 = maxMsgSize;
                                                String str152 = forkedSubscriptions;
                                                String rel2 = properties.getProperty("gov.nist.javax.sip.REENTRANT_LISTENER");
                                                this.reEntrantListener = rel2 == null && "true".equalsIgnoreCase(rel2);
                                                interval = properties.getProperty("gov.nist.javax.sip.THREAD_AUDIT_INTERVAL_IN_MILLISECS");
                                                if (interval == null) {
                                                }
                                                setNon2XXAckPassedToListener(Boolean.valueOf(properties.getProperty("gov.nist.javax.sip.PASS_INVITE_NON_2XX_ACK_TO_LISTENER", "false")).booleanValue());
                                                this.generateTimeStampHeader = Boolean.valueOf(properties.getProperty("gov.nist.javax.sip.AUTO_GENERATE_TIMESTAMP", "false")).booleanValue();
                                                messageLogFactoryClasspath = properties.getProperty("gov.nist.javax.sip.LOG_FACTORY");
                                                if (messageLogFactoryClasspath == null) {
                                                }
                                                StringMsgParser.setComputeContentLengthFromMessage(properties.getProperty("gov.nist.javax.sip.COMPUTE_CONTENT_LENGTH_FROM_MESSAGE_BODY", "false").equalsIgnoreCase("true"));
                                                tlsClientProtocols = properties.getProperty("gov.nist.javax.sip.TLS_CLIENT_PROTOCOLS");
                                                if (tlsClientProtocols != null) {
                                                }
                                                this.rfc2543Supported = properties.getProperty("gov.nist.javax.sip.RFC_2543_SUPPORT_ENABLED", "true").equalsIgnoreCase("true");
                                                this.cancelClientTransactionChecked = properties.getProperty("gov.nist.javax.sip.CANCEL_CLIENT_TRANSACTION_CHECKED", "true").equalsIgnoreCase("true");
                                                this.logStackTraceOnMessageSend = properties.getProperty("gov.nist.javax.sip.LOG_STACK_TRACE_ON_MESSAGE_SEND", "false").equalsIgnoreCase("true");
                                                if (isLoggingEnabled()) {
                                                }
                                                in = getClass().getResourceAsStream("/TIMESTAMP");
                                                if (in != null) {
                                                }
                                                String bufferSize3 = properties.getProperty("gov.nist.javax.sip.RECEIVE_UDP_BUFFER_SIZE", MAX_DATAGRAM_SIZE.toString());
                                                super.setReceiveUdpBufferSize(new Integer(bufferSize3).intValue());
                                                String str162 = bufferSize3;
                                                String bufferSize22 = properties.getProperty("gov.nist.javax.sip.SEND_UDP_BUFFER_SIZE", MAX_DATAGRAM_SIZE.toString());
                                                super.setSendUdpBufferSize(new Integer(bufferSize22).intValue());
                                                String str172 = bufferSize22;
                                                boolean congetstionControlEnabled2 = Boolean.parseBoolean(properties.getProperty("gov.nist.javax.sip.CONGESTION_CONTROL_ENABLED", Boolean.TRUE.toString()));
                                                this.stackDoesCongestionControl = congetstionControlEnabled2;
                                                boolean z22 = congetstionControlEnabled2;
                                                this.isBackToBackUserAgent = Boolean.parseBoolean(properties.getProperty("gov.nist.javax.sip.IS_BACK_TO_BACK_USER_AGENT", Boolean.FALSE.toString()));
                                                this.checkBranchId = Boolean.parseBoolean(properties.getProperty("gov.nist.javax.sip.REJECT_STRAY_RESPONSES", Boolean.FALSE.toString()));
                                                this.isDialogTerminatedEventDeliveredForNullDialog = Boolean.parseBoolean(properties.getProperty("gov.nist.javax.sip.DELIVER_TERMINATED_EVENT_FOR_NULL_DIALOG", Boolean.FALSE.toString()));
                                                this.maxForkTime = Integer.parseInt(properties.getProperty("gov.nist.javax.sip.MAX_FORK_TIME_SECONDS", "0"));
                                            }
                                        }
                                    } catch (NumberFormatException e9) {
                                        nfe = e9;
                                        String str20 = flag;
                                        String str21 = name;
                                        if (isLoggingEnabled()) {
                                            StackLogger stackLogger6 = getStackLogger();
                                            StringBuilder sb8 = new StringBuilder();
                                            NumberFormatException numberFormatException = nfe;
                                            sb8.append("Bad read timeout ");
                                            sb8.append(readTimeout);
                                            stackLogger6.logError(sb8.toString());
                                        }
                                        stunAddr = properties.getProperty("gov.nist.javax.sip.STUN_SERVER");
                                        if (stunAddr != null) {
                                        }
                                        maxMsgSize = properties.getProperty("gov.nist.javax.sip.MAX_MESSAGE_SIZE");
                                        if (maxMsgSize == null) {
                                        }
                                        String str1422 = maxMsgSize;
                                        String str1522 = forkedSubscriptions;
                                        String rel22 = properties.getProperty("gov.nist.javax.sip.REENTRANT_LISTENER");
                                        this.reEntrantListener = rel22 == null && "true".equalsIgnoreCase(rel22);
                                        interval = properties.getProperty("gov.nist.javax.sip.THREAD_AUDIT_INTERVAL_IN_MILLISECS");
                                        if (interval == null) {
                                        }
                                        setNon2XXAckPassedToListener(Boolean.valueOf(properties.getProperty("gov.nist.javax.sip.PASS_INVITE_NON_2XX_ACK_TO_LISTENER", "false")).booleanValue());
                                        this.generateTimeStampHeader = Boolean.valueOf(properties.getProperty("gov.nist.javax.sip.AUTO_GENERATE_TIMESTAMP", "false")).booleanValue();
                                        messageLogFactoryClasspath = properties.getProperty("gov.nist.javax.sip.LOG_FACTORY");
                                        if (messageLogFactoryClasspath == null) {
                                        }
                                        StringMsgParser.setComputeContentLengthFromMessage(properties.getProperty("gov.nist.javax.sip.COMPUTE_CONTENT_LENGTH_FROM_MESSAGE_BODY", "false").equalsIgnoreCase("true"));
                                        tlsClientProtocols = properties.getProperty("gov.nist.javax.sip.TLS_CLIENT_PROTOCOLS");
                                        if (tlsClientProtocols != null) {
                                        }
                                        this.rfc2543Supported = properties.getProperty("gov.nist.javax.sip.RFC_2543_SUPPORT_ENABLED", "true").equalsIgnoreCase("true");
                                        this.cancelClientTransactionChecked = properties.getProperty("gov.nist.javax.sip.CANCEL_CLIENT_TRANSACTION_CHECKED", "true").equalsIgnoreCase("true");
                                        this.logStackTraceOnMessageSend = properties.getProperty("gov.nist.javax.sip.LOG_STACK_TRACE_ON_MESSAGE_SEND", "false").equalsIgnoreCase("true");
                                        if (isLoggingEnabled()) {
                                        }
                                        in = getClass().getResourceAsStream("/TIMESTAMP");
                                        if (in != null) {
                                        }
                                        String bufferSize32 = properties.getProperty("gov.nist.javax.sip.RECEIVE_UDP_BUFFER_SIZE", MAX_DATAGRAM_SIZE.toString());
                                        super.setReceiveUdpBufferSize(new Integer(bufferSize32).intValue());
                                        String str1622 = bufferSize32;
                                        String bufferSize222 = properties.getProperty("gov.nist.javax.sip.SEND_UDP_BUFFER_SIZE", MAX_DATAGRAM_SIZE.toString());
                                        super.setSendUdpBufferSize(new Integer(bufferSize222).intValue());
                                        String str1722 = bufferSize222;
                                        boolean congetstionControlEnabled22 = Boolean.parseBoolean(properties.getProperty("gov.nist.javax.sip.CONGESTION_CONTROL_ENABLED", Boolean.TRUE.toString()));
                                        this.stackDoesCongestionControl = congetstionControlEnabled22;
                                        boolean z222 = congetstionControlEnabled22;
                                        this.isBackToBackUserAgent = Boolean.parseBoolean(properties.getProperty("gov.nist.javax.sip.IS_BACK_TO_BACK_USER_AGENT", Boolean.FALSE.toString()));
                                        this.checkBranchId = Boolean.parseBoolean(properties.getProperty("gov.nist.javax.sip.REJECT_STRAY_RESPONSES", Boolean.FALSE.toString()));
                                        this.isDialogTerminatedEventDeliveredForNullDialog = Boolean.parseBoolean(properties.getProperty("gov.nist.javax.sip.DELIVER_TERMINATED_EVENT_FOR_NULL_DIALOG", Boolean.FALSE.toString()));
                                        this.maxForkTime = Integer.parseInt(properties.getProperty("gov.nist.javax.sip.MAX_FORK_TIME_SECONDS", "0"));
                                    }
                                } else {
                                    String str22 = name;
                                }
                                stunAddr = properties.getProperty("gov.nist.javax.sip.STUN_SERVER");
                                if (stunAddr != null) {
                                    getStackLogger().logWarning("Ignoring obsolete property gov.nist.javax.sip.STUN_SERVER");
                                }
                                maxMsgSize = properties.getProperty("gov.nist.javax.sip.MAX_MESSAGE_SIZE");
                                if (maxMsgSize == null) {
                                    try {
                                        this.maxMessageSize = new Integer(maxMsgSize).intValue();
                                        String str23 = stunAddr;
                                        if (this.maxMessageSize < 4096) {
                                            try {
                                                this.maxMessageSize = 4096;
                                            } catch (NumberFormatException e10) {
                                                ex = e10;
                                                if (!isLoggingEnabled()) {
                                                }
                                                String rel222 = properties.getProperty("gov.nist.javax.sip.REENTRANT_LISTENER");
                                                this.reEntrantListener = rel222 == null && "true".equalsIgnoreCase(rel222);
                                                interval = properties.getProperty("gov.nist.javax.sip.THREAD_AUDIT_INTERVAL_IN_MILLISECS");
                                                if (interval == null) {
                                                }
                                                setNon2XXAckPassedToListener(Boolean.valueOf(properties.getProperty("gov.nist.javax.sip.PASS_INVITE_NON_2XX_ACK_TO_LISTENER", "false")).booleanValue());
                                                this.generateTimeStampHeader = Boolean.valueOf(properties.getProperty("gov.nist.javax.sip.AUTO_GENERATE_TIMESTAMP", "false")).booleanValue();
                                                messageLogFactoryClasspath = properties.getProperty("gov.nist.javax.sip.LOG_FACTORY");
                                                if (messageLogFactoryClasspath == null) {
                                                }
                                                StringMsgParser.setComputeContentLengthFromMessage(properties.getProperty("gov.nist.javax.sip.COMPUTE_CONTENT_LENGTH_FROM_MESSAGE_BODY", "false").equalsIgnoreCase("true"));
                                                tlsClientProtocols = properties.getProperty("gov.nist.javax.sip.TLS_CLIENT_PROTOCOLS");
                                                if (tlsClientProtocols != null) {
                                                }
                                                this.rfc2543Supported = properties.getProperty("gov.nist.javax.sip.RFC_2543_SUPPORT_ENABLED", "true").equalsIgnoreCase("true");
                                                this.cancelClientTransactionChecked = properties.getProperty("gov.nist.javax.sip.CANCEL_CLIENT_TRANSACTION_CHECKED", "true").equalsIgnoreCase("true");
                                                this.logStackTraceOnMessageSend = properties.getProperty("gov.nist.javax.sip.LOG_STACK_TRACE_ON_MESSAGE_SEND", "false").equalsIgnoreCase("true");
                                                if (isLoggingEnabled()) {
                                                }
                                                in = getClass().getResourceAsStream("/TIMESTAMP");
                                                if (in != null) {
                                                }
                                                String bufferSize322 = properties.getProperty("gov.nist.javax.sip.RECEIVE_UDP_BUFFER_SIZE", MAX_DATAGRAM_SIZE.toString());
                                                super.setReceiveUdpBufferSize(new Integer(bufferSize322).intValue());
                                                String str16222 = bufferSize322;
                                                String bufferSize2222 = properties.getProperty("gov.nist.javax.sip.SEND_UDP_BUFFER_SIZE", MAX_DATAGRAM_SIZE.toString());
                                                super.setSendUdpBufferSize(new Integer(bufferSize2222).intValue());
                                                String str17222 = bufferSize2222;
                                                boolean congetstionControlEnabled222 = Boolean.parseBoolean(properties.getProperty("gov.nist.javax.sip.CONGESTION_CONTROL_ENABLED", Boolean.TRUE.toString()));
                                                this.stackDoesCongestionControl = congetstionControlEnabled222;
                                                boolean z2222 = congetstionControlEnabled222;
                                                this.isBackToBackUserAgent = Boolean.parseBoolean(properties.getProperty("gov.nist.javax.sip.IS_BACK_TO_BACK_USER_AGENT", Boolean.FALSE.toString()));
                                                this.checkBranchId = Boolean.parseBoolean(properties.getProperty("gov.nist.javax.sip.REJECT_STRAY_RESPONSES", Boolean.FALSE.toString()));
                                                this.isDialogTerminatedEventDeliveredForNullDialog = Boolean.parseBoolean(properties.getProperty("gov.nist.javax.sip.DELIVER_TERMINATED_EVENT_FOR_NULL_DIALOG", Boolean.FALSE.toString()));
                                                this.maxForkTime = Integer.parseInt(properties.getProperty("gov.nist.javax.sip.MAX_FORK_TIME_SECONDS", "0"));
                                            }
                                        }
                                    } catch (NumberFormatException e11) {
                                        ex = e11;
                                        String str24 = stunAddr;
                                        if (!isLoggingEnabled()) {
                                            StackLogger stackLogger7 = getStackLogger();
                                            String str25 = maxMsgSize;
                                            StringBuilder sb9 = new StringBuilder();
                                            String str26 = forkedSubscriptions;
                                            sb9.append("maxMessageSize - bad value ");
                                            sb9.append(ex.getMessage());
                                            stackLogger7.logError(sb9.toString());
                                        } else {
                                            String str27 = forkedSubscriptions;
                                        }
                                        String rel2222 = properties.getProperty("gov.nist.javax.sip.REENTRANT_LISTENER");
                                        this.reEntrantListener = rel2222 == null && "true".equalsIgnoreCase(rel2222);
                                        interval = properties.getProperty("gov.nist.javax.sip.THREAD_AUDIT_INTERVAL_IN_MILLISECS");
                                        if (interval == null) {
                                        }
                                        setNon2XXAckPassedToListener(Boolean.valueOf(properties.getProperty("gov.nist.javax.sip.PASS_INVITE_NON_2XX_ACK_TO_LISTENER", "false")).booleanValue());
                                        this.generateTimeStampHeader = Boolean.valueOf(properties.getProperty("gov.nist.javax.sip.AUTO_GENERATE_TIMESTAMP", "false")).booleanValue();
                                        messageLogFactoryClasspath = properties.getProperty("gov.nist.javax.sip.LOG_FACTORY");
                                        if (messageLogFactoryClasspath == null) {
                                        }
                                        StringMsgParser.setComputeContentLengthFromMessage(properties.getProperty("gov.nist.javax.sip.COMPUTE_CONTENT_LENGTH_FROM_MESSAGE_BODY", "false").equalsIgnoreCase("true"));
                                        tlsClientProtocols = properties.getProperty("gov.nist.javax.sip.TLS_CLIENT_PROTOCOLS");
                                        if (tlsClientProtocols != null) {
                                        }
                                        this.rfc2543Supported = properties.getProperty("gov.nist.javax.sip.RFC_2543_SUPPORT_ENABLED", "true").equalsIgnoreCase("true");
                                        this.cancelClientTransactionChecked = properties.getProperty("gov.nist.javax.sip.CANCEL_CLIENT_TRANSACTION_CHECKED", "true").equalsIgnoreCase("true");
                                        this.logStackTraceOnMessageSend = properties.getProperty("gov.nist.javax.sip.LOG_STACK_TRACE_ON_MESSAGE_SEND", "false").equalsIgnoreCase("true");
                                        if (isLoggingEnabled()) {
                                        }
                                        in = getClass().getResourceAsStream("/TIMESTAMP");
                                        if (in != null) {
                                        }
                                        String bufferSize3222 = properties.getProperty("gov.nist.javax.sip.RECEIVE_UDP_BUFFER_SIZE", MAX_DATAGRAM_SIZE.toString());
                                        super.setReceiveUdpBufferSize(new Integer(bufferSize3222).intValue());
                                        String str162222 = bufferSize3222;
                                        String bufferSize22222 = properties.getProperty("gov.nist.javax.sip.SEND_UDP_BUFFER_SIZE", MAX_DATAGRAM_SIZE.toString());
                                        super.setSendUdpBufferSize(new Integer(bufferSize22222).intValue());
                                        String str172222 = bufferSize22222;
                                        boolean congetstionControlEnabled2222 = Boolean.parseBoolean(properties.getProperty("gov.nist.javax.sip.CONGESTION_CONTROL_ENABLED", Boolean.TRUE.toString()));
                                        this.stackDoesCongestionControl = congetstionControlEnabled2222;
                                        boolean z22222 = congetstionControlEnabled2222;
                                        this.isBackToBackUserAgent = Boolean.parseBoolean(properties.getProperty("gov.nist.javax.sip.IS_BACK_TO_BACK_USER_AGENT", Boolean.FALSE.toString()));
                                        this.checkBranchId = Boolean.parseBoolean(properties.getProperty("gov.nist.javax.sip.REJECT_STRAY_RESPONSES", Boolean.FALSE.toString()));
                                        this.isDialogTerminatedEventDeliveredForNullDialog = Boolean.parseBoolean(properties.getProperty("gov.nist.javax.sip.DELIVER_TERMINATED_EVENT_FOR_NULL_DIALOG", Boolean.FALSE.toString()));
                                        this.maxForkTime = Integer.parseInt(properties.getProperty("gov.nist.javax.sip.MAX_FORK_TIME_SECONDS", "0"));
                                    }
                                } else {
                                    this.maxMessageSize = 0;
                                }
                                String str14222 = maxMsgSize;
                                String str15222 = forkedSubscriptions;
                                String rel22222 = properties.getProperty("gov.nist.javax.sip.REENTRANT_LISTENER");
                                this.reEntrantListener = rel22222 == null && "true".equalsIgnoreCase(rel22222);
                                interval = properties.getProperty("gov.nist.javax.sip.THREAD_AUDIT_INTERVAL_IN_MILLISECS");
                                if (interval == null) {
                                    try {
                                        String str28 = serverLoggerClassName;
                                        try {
                                            getThreadAuditor().setPingIntervalInMillisecs(Long.valueOf(interval).longValue() / 2);
                                            String str29 = rel22222;
                                        } catch (NumberFormatException e12) {
                                            ex = e12;
                                            if (!isLoggingEnabled()) {
                                                StackLogger stackLogger8 = getStackLogger();
                                                StringBuilder sb10 = new StringBuilder();
                                                String str30 = rel22222;
                                                sb10.append("THREAD_AUDIT_INTERVAL_IN_MILLISECS - bad value [");
                                                sb10.append(interval);
                                                sb10.append("] ");
                                                sb10.append(ex.getMessage());
                                                stackLogger8.logError(sb10.toString());
                                            }
                                            setNon2XXAckPassedToListener(Boolean.valueOf(properties.getProperty("gov.nist.javax.sip.PASS_INVITE_NON_2XX_ACK_TO_LISTENER", "false")).booleanValue());
                                            this.generateTimeStampHeader = Boolean.valueOf(properties.getProperty("gov.nist.javax.sip.AUTO_GENERATE_TIMESTAMP", "false")).booleanValue();
                                            messageLogFactoryClasspath = properties.getProperty("gov.nist.javax.sip.LOG_FACTORY");
                                            if (messageLogFactoryClasspath == null) {
                                            }
                                            StringMsgParser.setComputeContentLengthFromMessage(properties.getProperty("gov.nist.javax.sip.COMPUTE_CONTENT_LENGTH_FROM_MESSAGE_BODY", "false").equalsIgnoreCase("true"));
                                            tlsClientProtocols = properties.getProperty("gov.nist.javax.sip.TLS_CLIENT_PROTOCOLS");
                                            if (tlsClientProtocols != null) {
                                            }
                                            this.rfc2543Supported = properties.getProperty("gov.nist.javax.sip.RFC_2543_SUPPORT_ENABLED", "true").equalsIgnoreCase("true");
                                            this.cancelClientTransactionChecked = properties.getProperty("gov.nist.javax.sip.CANCEL_CLIENT_TRANSACTION_CHECKED", "true").equalsIgnoreCase("true");
                                            this.logStackTraceOnMessageSend = properties.getProperty("gov.nist.javax.sip.LOG_STACK_TRACE_ON_MESSAGE_SEND", "false").equalsIgnoreCase("true");
                                            if (isLoggingEnabled()) {
                                            }
                                            in = getClass().getResourceAsStream("/TIMESTAMP");
                                            if (in != null) {
                                            }
                                            String bufferSize32222 = properties.getProperty("gov.nist.javax.sip.RECEIVE_UDP_BUFFER_SIZE", MAX_DATAGRAM_SIZE.toString());
                                            super.setReceiveUdpBufferSize(new Integer(bufferSize32222).intValue());
                                            String str1622222 = bufferSize32222;
                                            String bufferSize222222 = properties.getProperty("gov.nist.javax.sip.SEND_UDP_BUFFER_SIZE", MAX_DATAGRAM_SIZE.toString());
                                            super.setSendUdpBufferSize(new Integer(bufferSize222222).intValue());
                                            String str1722222 = bufferSize222222;
                                            boolean congetstionControlEnabled22222 = Boolean.parseBoolean(properties.getProperty("gov.nist.javax.sip.CONGESTION_CONTROL_ENABLED", Boolean.TRUE.toString()));
                                            this.stackDoesCongestionControl = congetstionControlEnabled22222;
                                            boolean z222222 = congetstionControlEnabled22222;
                                            this.isBackToBackUserAgent = Boolean.parseBoolean(properties.getProperty("gov.nist.javax.sip.IS_BACK_TO_BACK_USER_AGENT", Boolean.FALSE.toString()));
                                            this.checkBranchId = Boolean.parseBoolean(properties.getProperty("gov.nist.javax.sip.REJECT_STRAY_RESPONSES", Boolean.FALSE.toString()));
                                            this.isDialogTerminatedEventDeliveredForNullDialog = Boolean.parseBoolean(properties.getProperty("gov.nist.javax.sip.DELIVER_TERMINATED_EVENT_FOR_NULL_DIALOG", Boolean.FALSE.toString()));
                                            this.maxForkTime = Integer.parseInt(properties.getProperty("gov.nist.javax.sip.MAX_FORK_TIME_SECONDS", "0"));
                                        }
                                    } catch (NumberFormatException e13) {
                                        ex = e13;
                                        String str31 = serverLoggerClassName;
                                        if (!isLoggingEnabled()) {
                                        }
                                        setNon2XXAckPassedToListener(Boolean.valueOf(properties.getProperty("gov.nist.javax.sip.PASS_INVITE_NON_2XX_ACK_TO_LISTENER", "false")).booleanValue());
                                        this.generateTimeStampHeader = Boolean.valueOf(properties.getProperty("gov.nist.javax.sip.AUTO_GENERATE_TIMESTAMP", "false")).booleanValue();
                                        messageLogFactoryClasspath = properties.getProperty("gov.nist.javax.sip.LOG_FACTORY");
                                        if (messageLogFactoryClasspath == null) {
                                        }
                                        StringMsgParser.setComputeContentLengthFromMessage(properties.getProperty("gov.nist.javax.sip.COMPUTE_CONTENT_LENGTH_FROM_MESSAGE_BODY", "false").equalsIgnoreCase("true"));
                                        tlsClientProtocols = properties.getProperty("gov.nist.javax.sip.TLS_CLIENT_PROTOCOLS");
                                        if (tlsClientProtocols != null) {
                                        }
                                        this.rfc2543Supported = properties.getProperty("gov.nist.javax.sip.RFC_2543_SUPPORT_ENABLED", "true").equalsIgnoreCase("true");
                                        this.cancelClientTransactionChecked = properties.getProperty("gov.nist.javax.sip.CANCEL_CLIENT_TRANSACTION_CHECKED", "true").equalsIgnoreCase("true");
                                        this.logStackTraceOnMessageSend = properties.getProperty("gov.nist.javax.sip.LOG_STACK_TRACE_ON_MESSAGE_SEND", "false").equalsIgnoreCase("true");
                                        if (isLoggingEnabled()) {
                                        }
                                        in = getClass().getResourceAsStream("/TIMESTAMP");
                                        if (in != null) {
                                        }
                                        String bufferSize322222 = properties.getProperty("gov.nist.javax.sip.RECEIVE_UDP_BUFFER_SIZE", MAX_DATAGRAM_SIZE.toString());
                                        super.setReceiveUdpBufferSize(new Integer(bufferSize322222).intValue());
                                        String str16222222 = bufferSize322222;
                                        String bufferSize2222222 = properties.getProperty("gov.nist.javax.sip.SEND_UDP_BUFFER_SIZE", MAX_DATAGRAM_SIZE.toString());
                                        super.setSendUdpBufferSize(new Integer(bufferSize2222222).intValue());
                                        String str17222222 = bufferSize2222222;
                                        boolean congetstionControlEnabled222222 = Boolean.parseBoolean(properties.getProperty("gov.nist.javax.sip.CONGESTION_CONTROL_ENABLED", Boolean.TRUE.toString()));
                                        this.stackDoesCongestionControl = congetstionControlEnabled222222;
                                        boolean z2222222 = congetstionControlEnabled222222;
                                        this.isBackToBackUserAgent = Boolean.parseBoolean(properties.getProperty("gov.nist.javax.sip.IS_BACK_TO_BACK_USER_AGENT", Boolean.FALSE.toString()));
                                        this.checkBranchId = Boolean.parseBoolean(properties.getProperty("gov.nist.javax.sip.REJECT_STRAY_RESPONSES", Boolean.FALSE.toString()));
                                        this.isDialogTerminatedEventDeliveredForNullDialog = Boolean.parseBoolean(properties.getProperty("gov.nist.javax.sip.DELIVER_TERMINATED_EVENT_FOR_NULL_DIALOG", Boolean.FALSE.toString()));
                                        this.maxForkTime = Integer.parseInt(properties.getProperty("gov.nist.javax.sip.MAX_FORK_TIME_SECONDS", "0"));
                                    }
                                } else {
                                    String str32 = serverLoggerClassName;
                                }
                                setNon2XXAckPassedToListener(Boolean.valueOf(properties.getProperty("gov.nist.javax.sip.PASS_INVITE_NON_2XX_ACK_TO_LISTENER", "false")).booleanValue());
                                this.generateTimeStampHeader = Boolean.valueOf(properties.getProperty("gov.nist.javax.sip.AUTO_GENERATE_TIMESTAMP", "false")).booleanValue();
                                messageLogFactoryClasspath = properties.getProperty("gov.nist.javax.sip.LOG_FACTORY");
                                if (messageLogFactoryClasspath == null) {
                                    try {
                                        Class<?> clazz3 = Class.forName(messageLogFactoryClasspath);
                                        try {
                                            Class<?> cls3 = clazz3;
                                            this.logRecordFactory = (LogRecordFactory) clazz3.getConstructor(new Class[0]).newInstance(new Object[0]);
                                        } catch (Exception e14) {
                                        }
                                    } catch (Exception e15) {
                                        if (isLoggingEnabled()) {
                                            getStackLogger().logError("Bad configuration value for LOG_FACTORY -- using default logger");
                                        }
                                        this.logRecordFactory = new DefaultMessageLogFactory();
                                        StringMsgParser.setComputeContentLengthFromMessage(properties.getProperty("gov.nist.javax.sip.COMPUTE_CONTENT_LENGTH_FROM_MESSAGE_BODY", "false").equalsIgnoreCase("true"));
                                        tlsClientProtocols = properties.getProperty("gov.nist.javax.sip.TLS_CLIENT_PROTOCOLS");
                                        if (tlsClientProtocols != null) {
                                        }
                                        this.rfc2543Supported = properties.getProperty("gov.nist.javax.sip.RFC_2543_SUPPORT_ENABLED", "true").equalsIgnoreCase("true");
                                        this.cancelClientTransactionChecked = properties.getProperty("gov.nist.javax.sip.CANCEL_CLIENT_TRANSACTION_CHECKED", "true").equalsIgnoreCase("true");
                                        this.logStackTraceOnMessageSend = properties.getProperty("gov.nist.javax.sip.LOG_STACK_TRACE_ON_MESSAGE_SEND", "false").equalsIgnoreCase("true");
                                        if (isLoggingEnabled()) {
                                        }
                                        in = getClass().getResourceAsStream("/TIMESTAMP");
                                        if (in != null) {
                                        }
                                        String bufferSize3222222 = properties.getProperty("gov.nist.javax.sip.RECEIVE_UDP_BUFFER_SIZE", MAX_DATAGRAM_SIZE.toString());
                                        super.setReceiveUdpBufferSize(new Integer(bufferSize3222222).intValue());
                                        String str162222222 = bufferSize3222222;
                                        String bufferSize22222222 = properties.getProperty("gov.nist.javax.sip.SEND_UDP_BUFFER_SIZE", MAX_DATAGRAM_SIZE.toString());
                                        super.setSendUdpBufferSize(new Integer(bufferSize22222222).intValue());
                                        String str172222222 = bufferSize22222222;
                                        boolean congetstionControlEnabled2222222 = Boolean.parseBoolean(properties.getProperty("gov.nist.javax.sip.CONGESTION_CONTROL_ENABLED", Boolean.TRUE.toString()));
                                        this.stackDoesCongestionControl = congetstionControlEnabled2222222;
                                        boolean z22222222 = congetstionControlEnabled2222222;
                                        this.isBackToBackUserAgent = Boolean.parseBoolean(properties.getProperty("gov.nist.javax.sip.IS_BACK_TO_BACK_USER_AGENT", Boolean.FALSE.toString()));
                                        this.checkBranchId = Boolean.parseBoolean(properties.getProperty("gov.nist.javax.sip.REJECT_STRAY_RESPONSES", Boolean.FALSE.toString()));
                                        this.isDialogTerminatedEventDeliveredForNullDialog = Boolean.parseBoolean(properties.getProperty("gov.nist.javax.sip.DELIVER_TERMINATED_EVENT_FOR_NULL_DIALOG", Boolean.FALSE.toString()));
                                        this.maxForkTime = Integer.parseInt(properties.getProperty("gov.nist.javax.sip.MAX_FORK_TIME_SECONDS", "0"));
                                    }
                                } else {
                                    this.logRecordFactory = new DefaultMessageLogFactory();
                                }
                                StringMsgParser.setComputeContentLengthFromMessage(properties.getProperty("gov.nist.javax.sip.COMPUTE_CONTENT_LENGTH_FROM_MESSAGE_BODY", "false").equalsIgnoreCase("true"));
                                tlsClientProtocols = properties.getProperty("gov.nist.javax.sip.TLS_CLIENT_PROTOCOLS");
                                if (tlsClientProtocols != null) {
                                    String str33 = messageLogFactoryClasspath;
                                    StringTokenizer st3 = new StringTokenizer(tlsClientProtocols, " ,");
                                    String[] protocols = new String[st3.countTokens()];
                                    int i2 = 0;
                                    while (true) {
                                        int i3 = i2;
                                        if (!st3.hasMoreTokens()) {
                                            break;
                                        }
                                        i2 = i3 + 1;
                                        protocols[i3] = st3.nextToken();
                                    }
                                    this.enabledProtocols = protocols;
                                }
                                this.rfc2543Supported = properties.getProperty("gov.nist.javax.sip.RFC_2543_SUPPORT_ENABLED", "true").equalsIgnoreCase("true");
                                this.cancelClientTransactionChecked = properties.getProperty("gov.nist.javax.sip.CANCEL_CLIENT_TRANSACTION_CHECKED", "true").equalsIgnoreCase("true");
                                this.logStackTraceOnMessageSend = properties.getProperty("gov.nist.javax.sip.LOG_STACK_TRACE_ON_MESSAGE_SEND", "false").equalsIgnoreCase("true");
                                if (isLoggingEnabled()) {
                                    StackLogger stackLogger9 = getStackLogger();
                                    StringBuilder sb11 = new StringBuilder();
                                    String str34 = interval;
                                    sb11.append("created Sip stack. Properties = ");
                                    sb11.append(properties);
                                    stackLogger9.logDebug(sb11.toString());
                                }
                                in = getClass().getResourceAsStream("/TIMESTAMP");
                                if (in != null) {
                                    try {
                                        String buildTimeStamp = new BufferedReader(new InputStreamReader(in)).readLine();
                                        if (in != null) {
                                            try {
                                                in.close();
                                            } catch (IOException e16) {
                                                ex = e16;
                                                InputStream inputStream = in;
                                            }
                                        }
                                        InputStream inputStream2 = in;
                                        try {
                                            getStackLogger().setBuildTimeStamp(buildTimeStamp);
                                        } catch (IOException e17) {
                                            ex = e17;
                                        }
                                    } catch (IOException e18) {
                                        ex = e18;
                                        InputStream inputStream3 = in;
                                        IOException iOException = ex;
                                        getStackLogger().logError("Could not open build timestamp.");
                                        String bufferSize32222222 = properties.getProperty("gov.nist.javax.sip.RECEIVE_UDP_BUFFER_SIZE", MAX_DATAGRAM_SIZE.toString());
                                        super.setReceiveUdpBufferSize(new Integer(bufferSize32222222).intValue());
                                        String str1622222222 = bufferSize32222222;
                                        String bufferSize222222222 = properties.getProperty("gov.nist.javax.sip.SEND_UDP_BUFFER_SIZE", MAX_DATAGRAM_SIZE.toString());
                                        super.setSendUdpBufferSize(new Integer(bufferSize222222222).intValue());
                                        String str1722222222 = bufferSize222222222;
                                        boolean congetstionControlEnabled22222222 = Boolean.parseBoolean(properties.getProperty("gov.nist.javax.sip.CONGESTION_CONTROL_ENABLED", Boolean.TRUE.toString()));
                                        this.stackDoesCongestionControl = congetstionControlEnabled22222222;
                                        boolean z222222222 = congetstionControlEnabled22222222;
                                        this.isBackToBackUserAgent = Boolean.parseBoolean(properties.getProperty("gov.nist.javax.sip.IS_BACK_TO_BACK_USER_AGENT", Boolean.FALSE.toString()));
                                        this.checkBranchId = Boolean.parseBoolean(properties.getProperty("gov.nist.javax.sip.REJECT_STRAY_RESPONSES", Boolean.FALSE.toString()));
                                        this.isDialogTerminatedEventDeliveredForNullDialog = Boolean.parseBoolean(properties.getProperty("gov.nist.javax.sip.DELIVER_TERMINATED_EVENT_FOR_NULL_DIALOG", Boolean.FALSE.toString()));
                                        this.maxForkTime = Integer.parseInt(properties.getProperty("gov.nist.javax.sip.MAX_FORK_TIME_SECONDS", "0"));
                                    }
                                }
                                String bufferSize322222222 = properties.getProperty("gov.nist.javax.sip.RECEIVE_UDP_BUFFER_SIZE", MAX_DATAGRAM_SIZE.toString());
                                super.setReceiveUdpBufferSize(new Integer(bufferSize322222222).intValue());
                                String str16222222222 = bufferSize322222222;
                                String bufferSize2222222222 = properties.getProperty("gov.nist.javax.sip.SEND_UDP_BUFFER_SIZE", MAX_DATAGRAM_SIZE.toString());
                                super.setSendUdpBufferSize(new Integer(bufferSize2222222222).intValue());
                                String str17222222222 = bufferSize2222222222;
                                boolean congetstionControlEnabled222222222 = Boolean.parseBoolean(properties.getProperty("gov.nist.javax.sip.CONGESTION_CONTROL_ENABLED", Boolean.TRUE.toString()));
                                this.stackDoesCongestionControl = congetstionControlEnabled222222222;
                                boolean z2222222222 = congetstionControlEnabled222222222;
                                this.isBackToBackUserAgent = Boolean.parseBoolean(properties.getProperty("gov.nist.javax.sip.IS_BACK_TO_BACK_USER_AGENT", Boolean.FALSE.toString()));
                                this.checkBranchId = Boolean.parseBoolean(properties.getProperty("gov.nist.javax.sip.REJECT_STRAY_RESPONSES", Boolean.FALSE.toString()));
                                this.isDialogTerminatedEventDeliveredForNullDialog = Boolean.parseBoolean(properties.getProperty("gov.nist.javax.sip.DELIVER_TERMINATED_EVENT_FOR_NULL_DIALOG", Boolean.FALSE.toString()));
                                this.maxForkTime = Integer.parseInt(properties.getProperty("gov.nist.javax.sip.MAX_FORK_TIME_SECONDS", "0"));
                            }
                            serverTransactionTableSize = properties.getProperty("gov.nist.javax.sip.MAX_SERVER_TRANSACTIONS");
                            if (serverTransactionTableSize != null) {
                            }
                            clientTransactionTableSize = properties.getProperty("gov.nist.javax.sip.MAX_CLIENT_TRANSACTIONS");
                            if (clientTransactionTableSize != null) {
                            }
                            this.cacheServerConnections = z;
                            String flag2 = properties.getProperty("gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS");
                            this.cacheServerConnections = false;
                            this.cacheClientConnections = true;
                            String cacheflag2 = properties.getProperty("gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS");
                            this.cacheClientConnections = false;
                            readTimeout = properties.getProperty("gov.nist.javax.sip.READ_TIMEOUT");
                            if (readTimeout != null) {
                            }
                            stunAddr = properties.getProperty("gov.nist.javax.sip.STUN_SERVER");
                            if (stunAddr != null) {
                            }
                            maxMsgSize = properties.getProperty("gov.nist.javax.sip.MAX_MESSAGE_SIZE");
                            if (maxMsgSize == null) {
                            }
                            String str142222 = maxMsgSize;
                            String str152222 = forkedSubscriptions;
                            String rel222222 = properties.getProperty("gov.nist.javax.sip.REENTRANT_LISTENER");
                            this.reEntrantListener = rel222222 == null && "true".equalsIgnoreCase(rel222222);
                            interval = properties.getProperty("gov.nist.javax.sip.THREAD_AUDIT_INTERVAL_IN_MILLISECS");
                            if (interval == null) {
                            }
                            setNon2XXAckPassedToListener(Boolean.valueOf(properties.getProperty("gov.nist.javax.sip.PASS_INVITE_NON_2XX_ACK_TO_LISTENER", "false")).booleanValue());
                            this.generateTimeStampHeader = Boolean.valueOf(properties.getProperty("gov.nist.javax.sip.AUTO_GENERATE_TIMESTAMP", "false")).booleanValue();
                            messageLogFactoryClasspath = properties.getProperty("gov.nist.javax.sip.LOG_FACTORY");
                            if (messageLogFactoryClasspath == null) {
                            }
                            StringMsgParser.setComputeContentLengthFromMessage(properties.getProperty("gov.nist.javax.sip.COMPUTE_CONTENT_LENGTH_FROM_MESSAGE_BODY", "false").equalsIgnoreCase("true"));
                            tlsClientProtocols = properties.getProperty("gov.nist.javax.sip.TLS_CLIENT_PROTOCOLS");
                            if (tlsClientProtocols != null) {
                            }
                            this.rfc2543Supported = properties.getProperty("gov.nist.javax.sip.RFC_2543_SUPPORT_ENABLED", "true").equalsIgnoreCase("true");
                            this.cancelClientTransactionChecked = properties.getProperty("gov.nist.javax.sip.CANCEL_CLIENT_TRANSACTION_CHECKED", "true").equalsIgnoreCase("true");
                            this.logStackTraceOnMessageSend = properties.getProperty("gov.nist.javax.sip.LOG_STACK_TRACE_ON_MESSAGE_SEND", "false").equalsIgnoreCase("true");
                            if (isLoggingEnabled()) {
                            }
                            in = getClass().getResourceAsStream("/TIMESTAMP");
                            if (in != null) {
                            }
                            String bufferSize3222222222 = properties.getProperty("gov.nist.javax.sip.RECEIVE_UDP_BUFFER_SIZE", MAX_DATAGRAM_SIZE.toString());
                            super.setReceiveUdpBufferSize(new Integer(bufferSize3222222222).intValue());
                            String str162222222222 = bufferSize3222222222;
                            String bufferSize22222222222 = properties.getProperty("gov.nist.javax.sip.SEND_UDP_BUFFER_SIZE", MAX_DATAGRAM_SIZE.toString());
                            super.setSendUdpBufferSize(new Integer(bufferSize22222222222).intValue());
                            String str172222222222 = bufferSize22222222222;
                            boolean congetstionControlEnabled2222222222 = Boolean.parseBoolean(properties.getProperty("gov.nist.javax.sip.CONGESTION_CONTROL_ENABLED", Boolean.TRUE.toString()));
                            this.stackDoesCongestionControl = congetstionControlEnabled2222222222;
                            boolean z22222222222 = congetstionControlEnabled2222222222;
                            this.isBackToBackUserAgent = Boolean.parseBoolean(properties.getProperty("gov.nist.javax.sip.IS_BACK_TO_BACK_USER_AGENT", Boolean.FALSE.toString()));
                            this.checkBranchId = Boolean.parseBoolean(properties.getProperty("gov.nist.javax.sip.REJECT_STRAY_RESPONSES", Boolean.FALSE.toString()));
                            this.isDialogTerminatedEventDeliveredForNullDialog = Boolean.parseBoolean(properties.getProperty("gov.nist.javax.sip.DELIVER_TERMINATED_EVENT_FOR_NULL_DIALOG", Boolean.FALSE.toString()));
                            this.maxForkTime = Integer.parseInt(properties.getProperty("gov.nist.javax.sip.MAX_FORK_TIME_SECONDS", "0"));
                        }
                        threadPoolSize = properties.getProperty("gov.nist.javax.sip.THREAD_POOL_SIZE");
                        if (threadPoolSize != null) {
                        }
                        serverTransactionTableSize = properties.getProperty("gov.nist.javax.sip.MAX_SERVER_TRANSACTIONS");
                        if (serverTransactionTableSize != null) {
                        }
                        clientTransactionTableSize = properties.getProperty("gov.nist.javax.sip.MAX_CLIENT_TRANSACTIONS");
                        if (clientTransactionTableSize != null) {
                        }
                        this.cacheServerConnections = z;
                        String flag22 = properties.getProperty("gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS");
                        this.cacheServerConnections = false;
                        this.cacheClientConnections = true;
                        String cacheflag22 = properties.getProperty("gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS");
                        this.cacheClientConnections = false;
                        readTimeout = properties.getProperty("gov.nist.javax.sip.READ_TIMEOUT");
                        if (readTimeout != null) {
                        }
                        stunAddr = properties.getProperty("gov.nist.javax.sip.STUN_SERVER");
                        if (stunAddr != null) {
                        }
                        maxMsgSize = properties.getProperty("gov.nist.javax.sip.MAX_MESSAGE_SIZE");
                        if (maxMsgSize == null) {
                        }
                        String str1422222 = maxMsgSize;
                        String str1522222 = forkedSubscriptions;
                        String rel2222222 = properties.getProperty("gov.nist.javax.sip.REENTRANT_LISTENER");
                        this.reEntrantListener = rel2222222 == null && "true".equalsIgnoreCase(rel2222222);
                        interval = properties.getProperty("gov.nist.javax.sip.THREAD_AUDIT_INTERVAL_IN_MILLISECS");
                        if (interval == null) {
                        }
                        setNon2XXAckPassedToListener(Boolean.valueOf(properties.getProperty("gov.nist.javax.sip.PASS_INVITE_NON_2XX_ACK_TO_LISTENER", "false")).booleanValue());
                        this.generateTimeStampHeader = Boolean.valueOf(properties.getProperty("gov.nist.javax.sip.AUTO_GENERATE_TIMESTAMP", "false")).booleanValue();
                        messageLogFactoryClasspath = properties.getProperty("gov.nist.javax.sip.LOG_FACTORY");
                        if (messageLogFactoryClasspath == null) {
                        }
                        StringMsgParser.setComputeContentLengthFromMessage(properties.getProperty("gov.nist.javax.sip.COMPUTE_CONTENT_LENGTH_FROM_MESSAGE_BODY", "false").equalsIgnoreCase("true"));
                        tlsClientProtocols = properties.getProperty("gov.nist.javax.sip.TLS_CLIENT_PROTOCOLS");
                        if (tlsClientProtocols != null) {
                        }
                        this.rfc2543Supported = properties.getProperty("gov.nist.javax.sip.RFC_2543_SUPPORT_ENABLED", "true").equalsIgnoreCase("true");
                        this.cancelClientTransactionChecked = properties.getProperty("gov.nist.javax.sip.CANCEL_CLIENT_TRANSACTION_CHECKED", "true").equalsIgnoreCase("true");
                        this.logStackTraceOnMessageSend = properties.getProperty("gov.nist.javax.sip.LOG_STACK_TRACE_ON_MESSAGE_SEND", "false").equalsIgnoreCase("true");
                        if (isLoggingEnabled()) {
                        }
                        in = getClass().getResourceAsStream("/TIMESTAMP");
                        if (in != null) {
                        }
                        String bufferSize32222222222 = properties.getProperty("gov.nist.javax.sip.RECEIVE_UDP_BUFFER_SIZE", MAX_DATAGRAM_SIZE.toString());
                        super.setReceiveUdpBufferSize(new Integer(bufferSize32222222222).intValue());
                        String str1622222222222 = bufferSize32222222222;
                        String bufferSize222222222222 = properties.getProperty("gov.nist.javax.sip.SEND_UDP_BUFFER_SIZE", MAX_DATAGRAM_SIZE.toString());
                        super.setSendUdpBufferSize(new Integer(bufferSize222222222222).intValue());
                        String str1722222222222 = bufferSize222222222222;
                        boolean congetstionControlEnabled22222222222 = Boolean.parseBoolean(properties.getProperty("gov.nist.javax.sip.CONGESTION_CONTROL_ENABLED", Boolean.TRUE.toString()));
                        this.stackDoesCongestionControl = congetstionControlEnabled22222222222;
                        boolean z222222222222 = congetstionControlEnabled22222222222;
                        this.isBackToBackUserAgent = Boolean.parseBoolean(properties.getProperty("gov.nist.javax.sip.IS_BACK_TO_BACK_USER_AGENT", Boolean.FALSE.toString()));
                        this.checkBranchId = Boolean.parseBoolean(properties.getProperty("gov.nist.javax.sip.REJECT_STRAY_RESPONSES", Boolean.FALSE.toString()));
                        this.isDialogTerminatedEventDeliveredForNullDialog = Boolean.parseBoolean(properties.getProperty("gov.nist.javax.sip.DELIVER_TERMINATED_EVENT_FOR_NULL_DIALOG", Boolean.FALSE.toString()));
                        this.maxForkTime = Integer.parseInt(properties.getProperty("gov.nist.javax.sip.MAX_FORK_TIME_SECONDS", "0"));
                    } catch (InvocationTargetException ex1) {
                        String str35 = address;
                        String str36 = name;
                        String str37 = serverLoggerClassName;
                        getStackLogger().logError("could not instantiate router -- invocation target problem", (Exception) ex1.getCause());
                        throw new PeerUnavailableException("Cound not instantiate router - check constructor", ex1);
                    } catch (Exception ex5) {
                        String str38 = address;
                        String str39 = name;
                        String str40 = serverLoggerClassName;
                        getStackLogger().logError("could not instantiate router", (Exception) ex5.getCause());
                        throw new PeerUnavailableException("Could not instantiate router", ex5);
                    }
                } catch (InvocationTargetException ex12) {
                    String str41 = address;
                    String str42 = name;
                    String str43 = serverLoggerClassName;
                    throw new IllegalArgumentException("Cound not instantiate server logger " + stackLoggerClassName + "- check that it is present on the classpath and that there is a no-args constructor defined", ex12);
                } catch (Exception ex6) {
                    String str44 = address;
                    String str45 = name;
                    String str46 = serverLoggerClassName;
                    throw new IllegalArgumentException("Cound not instantiate server logger " + stackLoggerClassName + "- check that it is present on the classpath and that there is a no-args constructor defined", ex6);
                }
            } catch (InvocationTargetException ex13) {
                String str47 = address;
                String str48 = name;
                throw new IllegalArgumentException("Cound not instantiate stack logger " + stackLoggerClassName + "- check that it is present on the classpath and that there is a no-args constructor defined", ex13);
            } catch (Exception ex7) {
                String str49 = address;
                String str50 = name;
                throw new IllegalArgumentException("Cound not instantiate stack logger " + stackLoggerClassName + "- check that it is present on the classpath and that there is a no-args constructor defined", ex7);
            }
        } else {
            String str51 = name;
            throw new PeerUnavailableException("stack name is missing");
        }
    }

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

    public void deleteListeningPoint(ListeningPoint listeningPoint) throws ObjectInUseException {
        if (listeningPoint != null) {
            ListeningPointImpl lip = (ListeningPointImpl) listeningPoint;
            super.removeMessageProcessor(lip.messageProcessor);
            this.listeningPoints.remove(lip.getKey());
            return;
        }
        throw new NullPointerException("null listeningPoint arg");
    }

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

    /* access modifiers changed from: protected */
    public void finalize() {
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
        this.sipProviders = new LinkedList<>();
        this.listeningPoints = new Hashtable<>();
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
