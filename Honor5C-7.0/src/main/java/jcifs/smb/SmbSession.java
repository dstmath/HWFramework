package jcifs.smb;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Vector;
import jcifs.UniAddress;
import jcifs.netbios.NbtAddress;
import jcifs.util.LogStream;

public final class SmbSession {
    private static final int CACHE_POLICY = 0;
    private static final String DOMAIN = null;
    private static final String LOGON_SHARE = null;
    private static final int LOOKUP_RESP_LIMIT = 0;
    private static final String USERNAME = null;
    static NbtAddress[] dc_list;
    static int dc_list_counter;
    static long dc_list_expiration;
    private UniAddress address;
    NtlmPasswordAuthentication auth;
    int connectionState;
    long expiration;
    private InetAddress localAddr;
    private int localPort;
    String netbiosName;
    private int port;
    SmbTransport transport;
    Vector trees;
    int uid;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: jcifs.smb.SmbSession.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: jcifs.smb.SmbSession.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: jcifs.smb.SmbSession.<clinit>():void");
    }

    private static NtlmChallenge interrogate(NbtAddress addr) throws SmbException {
        UniAddress dc = new UniAddress(addr);
        SmbTransport trans = SmbTransport.getSmbTransport(dc, 0);
        if (USERNAME == null) {
            trans.connect();
            LogStream logStream = SmbTransport.log;
            if (LogStream.level >= 3) {
                SmbTransport.log.println("Default credentials (jcifs.smb.client.username/password) not specified. SMB signing may not work propertly.  Skipping DC interrogation.");
            }
        } else {
            trans.getSmbSession(NtlmPasswordAuthentication.DEFAULT).getSmbTree(LOGON_SHARE, null).treeConnect(null, null);
        }
        return new NtlmChallenge(trans.server.encryptionKey, dc);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static NtlmChallenge getChallengeForDomain() throws SmbException, UnknownHostException {
        if (DOMAIN == null) {
            throw new SmbException("A domain was not specified");
        }
        synchronized (DOMAIN) {
            int i;
            long now = System.currentTimeMillis();
            int retry = 1;
            loop0:
            while (true) {
                if (dc_list_expiration < now) {
                    NbtAddress[] list = NbtAddress.getAllByName(DOMAIN, 28, null, null);
                    dc_list_expiration = (((long) CACHE_POLICY) * 1000) + now;
                    if (list == null || list.length <= 0) {
                        dc_list_expiration = 900000 + now;
                        r9 = SmbTransport.log;
                        if (LogStream.level >= 2) {
                            SmbTransport.log.println("Failed to retrieve DC list from WINS");
                        }
                    } else {
                        dc_list = list;
                    }
                }
                int max = Math.min(dc_list.length, LOOKUP_RESP_LIMIT);
                int j = 0;
                while (j < max) {
                    int i2 = dc_list_counter;
                    dc_list_counter = i2 + 1;
                    i = i2 % max;
                    if (dc_list[i] != null) {
                        try {
                            NtlmChallenge interrogate = interrogate(dc_list[i]);
                            break loop0;
                        } catch (SmbException se) {
                            r9 = SmbTransport.log;
                            if (LogStream.level >= 2) {
                                LogStream logStream;
                                SmbTransport.log.println("Failed validate DC: " + dc_list[i]);
                                logStream = SmbTransport.log;
                                if (LogStream.level > 2) {
                                    se.printStackTrace(SmbTransport.log);
                                }
                            }
                            dc_list[i] = null;
                        }
                    } else {
                        j++;
                    }
                }
                dc_list_expiration = 0;
                int retry2 = retry - 1;
                if (retry <= 0) {
                    break;
                }
                retry = retry2;
            }
        }
    }

    public static byte[] getChallenge(UniAddress dc) throws SmbException, UnknownHostException {
        return getChallenge(dc, 0);
    }

    public static byte[] getChallenge(UniAddress dc, int port) throws SmbException, UnknownHostException {
        SmbTransport trans = SmbTransport.getSmbTransport(dc, port);
        trans.connect();
        return trans.server.encryptionKey;
    }

    public static void logon(UniAddress dc, NtlmPasswordAuthentication auth) throws SmbException {
        logon(dc, 0, auth);
    }

    public static void logon(UniAddress dc, int port, NtlmPasswordAuthentication auth) throws SmbException {
        SmbTree tree = SmbTransport.getSmbTransport(dc, port).getSmbSession(auth).getSmbTree(LOGON_SHARE, null);
        if (LOGON_SHARE == null) {
            tree.treeConnect(null, null);
        } else {
            tree.send(new Trans2FindFirst2("\\", "*", 16), new Trans2FindFirst2Response());
        }
    }

    SmbSession(UniAddress address, int port, InetAddress localAddr, int localPort, NtlmPasswordAuthentication auth) {
        this.transport = null;
        this.netbiosName = null;
        this.address = address;
        this.port = port;
        this.localAddr = localAddr;
        this.localPort = localPort;
        this.auth = auth;
        this.trees = new Vector();
        this.connectionState = 0;
    }

    synchronized SmbTree getSmbTree(String share, String service) {
        Object t;
        SmbTree t2;
        if (share == null) {
            share = "IPC$";
        }
        Enumeration e = this.trees.elements();
        while (e.hasMoreElements()) {
            t2 = (SmbTree) e.nextElement();
            if (t2.matches(share, service)) {
                t = t2;
                break;
            }
        }
        t2 = new SmbTree(this, share, service);
        this.trees.addElement(t2);
        SmbTree t3 = t2;
        return t;
    }

    boolean matches(NtlmPasswordAuthentication auth) {
        return this.auth == auth || this.auth.equals(auth);
    }

    synchronized SmbTransport transport() {
        if (this.transport == null) {
            this.transport = SmbTransport.getSmbTransport(this.address, this.port, this.localAddr, this.localPort, null);
        }
        return this.transport;
    }

    void send(ServerMessageBlock request, ServerMessageBlock response) throws SmbException {
        synchronized (transport()) {
            if (response != null) {
                response.received = false;
            }
            this.expiration = System.currentTimeMillis() + ((long) SmbTransport.SO_TIMEOUT);
            sessionSetup(request, response);
            if (response == null || !response.received) {
                if (request instanceof SmbComTreeConnectAndX) {
                    SmbComTreeConnectAndX tcax = (SmbComTreeConnectAndX) request;
                    if (this.netbiosName != null && tcax.path.endsWith("\\IPC$")) {
                        tcax.path = "\\\\" + this.netbiosName + "\\IPC$";
                    }
                }
                request.uid = this.uid;
                request.auth = this.auth;
                try {
                    this.transport.send(request, response);
                    return;
                } catch (SmbException se) {
                    if (request instanceof SmbComTreeConnectAndX) {
                        logoff(true);
                    }
                    request.digest = null;
                    throw se;
                }
            }
        }
    }

    void sessionSetup(ServerMessageBlock andx, ServerMessageBlock andxResponse) throws SmbException {
        SmbException se;
        synchronized (transport()) {
            NtlmContext nctx = null;
            SmbException ex = null;
            byte[] token = new byte[0];
            int state = 10;
            while (this.connectionState != 0) {
                if (this.connectionState == 2 || this.connectionState == 3) {
                    return;
                }
                try {
                    this.transport.wait();
                } catch (Throwable ie) {
                    throw new SmbException(ie.getMessage(), ie);
                }
            }
            this.connectionState = 1;
            this.transport.connect();
            SmbTransport smbTransport = this.transport;
            LogStream logStream = SmbTransport.log;
            if (LogStream.level >= 4) {
                smbTransport = this.transport;
                SmbTransport.log.println("sessionSetup: accountName=" + this.auth.username + ",primaryDomain=" + this.auth.domain);
            }
            this.uid = 0;
            do {
                NtlmContext nctx2 = nctx;
                SmbComSessionSetupAndX request;
                SmbComSessionSetupAndXResponse response;
                switch (state) {
                    case SmbConstants.DEFAULT_MAX_MPX_COUNT /*10*/:
                        if (this.auth == NtlmPasswordAuthentication.ANONYMOUS || !this.transport.hasCapability(SmbConstants.GENERIC_READ)) {
                            try {
                                request = new SmbComSessionSetupAndX(this, andx, this.auth);
                                response = new SmbComSessionSetupAndXResponse(andxResponse);
                                if (this.transport.isSignatureSetupRequired(this.auth)) {
                                    if (!this.auth.hashesExternal || NtlmPasswordAuthentication.DEFAULT_PASSWORD == "") {
                                        request.digest = new SigningDigest(this.auth.getSigningKey(this.transport.server.encryptionKey), false);
                                    } else {
                                        this.transport.getSmbSession(NtlmPasswordAuthentication.DEFAULT).getSmbTree(LOGON_SHARE, null).treeConnect(null, null);
                                    }
                                }
                                request.auth = this.auth;
                                this.transport.send(request, response);
                            } catch (SmbAuthException sae) {
                                throw sae;
                            } catch (SmbException se2) {
                                ex = se2;
                            } catch (SmbException e) {
                                se2 = e;
                                nctx = nctx2;
                                break;
                            } catch (Throwable th) {
                                Throwable th2 = th;
                                nctx = nctx2;
                                break;
                            }
                            if (response.isLoggedInAsGuest) {
                                if (!("GUEST".equalsIgnoreCase(this.auth.username) || this.transport.server.security == 0 || this.auth == NtlmPasswordAuthentication.ANONYMOUS)) {
                                    throw new SmbAuthException(NtStatus.NT_STATUS_LOGON_FAILURE);
                                }
                            }
                            if (ex != null) {
                                throw ex;
                            }
                            this.uid = response.uid;
                            if (request.digest != null) {
                                this.transport.digest = request.digest;
                            }
                            this.connectionState = 2;
                            state = 0;
                            nctx = nctx2;
                            continue;
                        } else {
                            state = 20;
                            nctx = nctx2;
                            continue;
                        }
                    case 20:
                        if (nctx2 == null) {
                            nctx = new NtlmContext(this.auth, (this.transport.flags2 & 4) != 0);
                        } else {
                            nctx = nctx2;
                        }
                        logStream = SmbTransport.log;
                        if (LogStream.level >= 4) {
                            SmbTransport.log.println(nctx);
                        }
                        if (!nctx.isEstablished()) {
                            try {
                                token = nctx.initSecContext(token, 0, token.length);
                                if (token != null) {
                                    request = new SmbComSessionSetupAndX(this, null, token);
                                    response = new SmbComSessionSetupAndXResponse(null);
                                    if (this.transport.isSignatureSetupRequired(this.auth)) {
                                        byte[] signingKey = nctx.getSigningKey();
                                        if (signingKey != null) {
                                            request.digest = new SigningDigest(signingKey, true);
                                        }
                                    }
                                    request.uid = this.uid;
                                    this.uid = 0;
                                    this.transport.send(request, response);
                                    if (response.isLoggedInAsGuest) {
                                        if (!"GUEST".equalsIgnoreCase(this.auth.username)) {
                                            throw new SmbAuthException(NtStatus.NT_STATUS_LOGON_FAILURE);
                                        }
                                    }
                                    if (ex != null) {
                                        throw ex;
                                    }
                                    this.uid = response.uid;
                                    if (request.digest != null) {
                                        this.transport.digest = request.digest;
                                    }
                                    token = response.blob;
                                    continue;
                                } else {
                                    continue;
                                }
                            } catch (SmbAuthException sae2) {
                                throw sae2;
                            } catch (SmbException se22) {
                                ex = se22;
                                try {
                                    this.transport.disconnect(true);
                                } catch (Exception e2) {
                                }
                            } catch (SmbException se222) {
                                try {
                                    this.transport.disconnect(true);
                                } catch (IOException e3) {
                                }
                                this.uid = 0;
                                throw se222;
                            } catch (SmbException e4) {
                                se222 = e4;
                                break;
                            }
                        }
                        this.netbiosName = nctx.getNetbiosName();
                        this.connectionState = 2;
                        state = 0;
                        continue;
                    default:
                        throw new SmbException("Unexpected session setup state: " + state);
                }
                try {
                    logoff(true);
                    this.connectionState = 0;
                    throw se222;
                } catch (Throwable th3) {
                    th2 = th3;
                    this.transport.notifyAll();
                    throw th2;
                }
            } while (state != 0);
            this.transport.notifyAll();
        }
    }

    void logoff(boolean inError) {
        synchronized (transport()) {
            if (this.connectionState != 2) {
                return;
            }
            this.connectionState = 3;
            this.netbiosName = null;
            Enumeration e = this.trees.elements();
            while (e.hasMoreElements()) {
                ((SmbTree) e.nextElement()).treeDisconnect(inError);
            }
            if (!inError) {
                if (this.transport.server.security != 0) {
                    SmbComLogoffAndX request = new SmbComLogoffAndX(null);
                    request.uid = this.uid;
                    try {
                        this.transport.send(request, null);
                    } catch (SmbException e2) {
                    }
                    this.uid = 0;
                }
            }
            this.connectionState = 0;
            this.transport.notifyAll();
        }
    }

    public String toString() {
        return "SmbSession[accountName=" + this.auth.username + ",primaryDomain=" + this.auth.domain + ",uid=" + this.uid + ",connectionState=" + this.connectionState + "]";
    }
}
