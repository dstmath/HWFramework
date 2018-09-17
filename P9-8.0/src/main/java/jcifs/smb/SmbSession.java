package jcifs.smb;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Vector;
import jcifs.Config;
import jcifs.UniAddress;
import jcifs.netbios.NbtAddress;
import jcifs.util.LogStream;

public final class SmbSession {
    private static final int CACHE_POLICY = (Config.getInt("jcifs.netbios.cachePolicy", 600) * 60);
    private static final String DOMAIN = Config.getProperty("jcifs.smb.client.domain", null);
    private static final String LOGON_SHARE = Config.getProperty("jcifs.smb.client.logonShare", null);
    private static final int LOOKUP_RESP_LIMIT = Config.getInt("jcifs.netbios.lookupRespLimit", 3);
    private static final String USERNAME = Config.getProperty("jcifs.smb.client.username", null);
    static NbtAddress[] dc_list = null;
    static int dc_list_counter;
    static long dc_list_expiration;
    private UniAddress address;
    NtlmPasswordAuthentication auth;
    int connectionState;
    long expiration;
    private InetAddress localAddr;
    private int localPort;
    String netbiosName = null;
    private int port;
    SmbTransport transport = null;
    Vector trees;
    int uid;

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

    /* JADX WARNING: Missing block: B:23:0x0058, code:
            return r9;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static NtlmChallenge getChallengeForDomain() throws SmbException, UnknownHostException {
        if (DOMAIN == null) {
            throw new SmbException("A domain was not specified");
        }
        synchronized (DOMAIN) {
            long now = System.currentTimeMillis();
            int retry = 1;
            loop0:
            while (true) {
                LogStream logStream;
                int retry2 = retry;
                if (dc_list_expiration < now) {
                    NbtAddress[] list = NbtAddress.getAllByName(DOMAIN, 28, null, null);
                    dc_list_expiration = (((long) CACHE_POLICY) * 1000) + now;
                    if (list == null || list.length <= 0) {
                        dc_list_expiration = 900000 + now;
                        logStream = SmbTransport.log;
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
                    int i = dc_list_counter;
                    dc_list_counter = i + 1;
                    int i2 = i % max;
                    if (dc_list[i2] != null) {
                        try {
                            NtlmChallenge interrogate = interrogate(dc_list[i2]);
                            break loop0;
                        } catch (SmbException se) {
                            logStream = SmbTransport.log;
                            if (LogStream.level >= 2) {
                                SmbTransport.log.println("Failed validate DC: " + dc_list[i2]);
                                logStream = SmbTransport.log;
                                if (LogStream.level > 2) {
                                    se.printStackTrace(SmbTransport.log);
                                }
                            }
                            dc_list[i2] = null;
                        }
                    } else {
                        j++;
                    }
                }
                dc_list_expiration = 0;
                retry = retry2 - 1;
                if (retry2 <= 0) {
                    dc_list_expiration = 900000 + now;
                    throw new UnknownHostException("Failed to negotiate with a suitable domain controller for " + DOMAIN);
                }
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
        return t3;
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

    /* JADX WARNING: Removed duplicated region for block: B:77:0x019f A:{Splitter: B:32:0x00a3, ExcHandler: all (th java.lang.Throwable), Catch:{ SmbAuthException -> 0x01cb, SmbException -> 0x01cd, all -> 0x019f, SmbException -> 0x00c0, all -> 0x019f }} */
    /* JADX WARNING: Missing block: B:77:0x019f, code:
            r14 = th;
     */
    /* JADX WARNING: Missing block: B:78:0x01a0, code:
            r5 = r6;
     */
    /* JADX WARNING: Missing block: B:164:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void sessionSetup(ServerMessageBlock andx, ServerMessageBlock andxResponse) throws SmbException {
        SmbException se;
        synchronized (transport()) {
            NtlmContext nctx = null;
            SmbException ex = null;
            byte[] token = new byte[0];
            int state = 10;
            while (this.connectionState != 0) {
                if (this.connectionState == 2 || this.connectionState == 3) {
                } else {
                    try {
                        this.transport.wait();
                    } catch (Throwable ie) {
                        throw new SmbException(ie.getMessage(), ie);
                    }
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
                        if (this.auth == NtlmPasswordAuthentication.ANONYMOUS || !this.transport.hasCapability(Integer.MIN_VALUE)) {
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
                            }
                            if (response.isLoggedInAsGuest && !"GUEST".equalsIgnoreCase(this.auth.username) && this.transport.server.security != 0 && this.auth != NtlmPasswordAuthentication.ANONYMOUS) {
                                throw new SmbAuthException(NtStatus.NT_STATUS_LOGON_FAILURE);
                            } else if (ex != null) {
                                throw ex;
                            } else {
                                this.uid = response.uid;
                                if (request.digest != null) {
                                    this.transport.digest = request.digest;
                                }
                                this.connectionState = 2;
                                state = 0;
                                nctx = nctx2;
                                continue;
                            }
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
                        try {
                            logStream = SmbTransport.log;
                            if (LogStream.level >= 4) {
                                SmbTransport.log.println(nctx);
                            }
                            if (nctx.isEstablished()) {
                                this.netbiosName = nctx.getNetbiosName();
                                this.connectionState = 2;
                                state = 0;
                                continue;
                            } else {
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
                                    if (response.isLoggedInAsGuest && !"GUEST".equalsIgnoreCase(this.auth.username)) {
                                        throw new SmbAuthException(NtStatus.NT_STATUS_LOGON_FAILURE);
                                    } else if (ex != null) {
                                        throw ex;
                                    } else {
                                        this.uid = response.uid;
                                        if (request.digest != null) {
                                            this.transport.digest = request.digest;
                                        }
                                        token = response.blob;
                                        continue;
                                    }
                                } else {
                                    continue;
                                }
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
                        break;
                    default:
                        throw new SmbException("Unexpected session setup state: " + state);
                }
                try {
                    logoff(true);
                    this.connectionState = 0;
                    throw se222;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    this.transport.notifyAll();
                    throw th3;
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
