package jcifs.smb;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import jcifs.Config;
import jcifs.UniAddress;
import jcifs.dcerpc.DcerpcHandle;
import jcifs.dcerpc.msrpc.MsrpcDfsRootEnum;
import jcifs.dcerpc.msrpc.MsrpcShareEnum;
import jcifs.dcerpc.msrpc.MsrpcShareGetInfo;
import jcifs.netbios.NbtAddress;
import jcifs.util.LogStream;

public class SmbFile extends URLConnection implements SmbConstants {
    public static final int ATTR_ARCHIVE = 32;
    static final int ATTR_COMPRESSED = 2048;
    public static final int ATTR_DIRECTORY = 16;
    static final int ATTR_GET_MASK = 32767;
    public static final int ATTR_HIDDEN = 2;
    static final int ATTR_NORMAL = 128;
    public static final int ATTR_READONLY = 1;
    static final int ATTR_SET_MASK = 12455;
    public static final int ATTR_SYSTEM = 4;
    static final int ATTR_TEMPORARY = 256;
    public static final int ATTR_VOLUME = 8;
    static final int DEFAULT_ATTR_EXPIRATION_PERIOD = 5000;
    public static final int FILE_NO_SHARE = 0;
    public static final int FILE_SHARE_DELETE = 4;
    public static final int FILE_SHARE_READ = 1;
    public static final int FILE_SHARE_WRITE = 2;
    static final int HASH_DOT = ".".hashCode();
    static final int HASH_DOT_DOT = "..".hashCode();
    static final int O_APPEND = 4;
    static final int O_CREAT = 16;
    static final int O_EXCL = 32;
    static final int O_RDONLY = 1;
    static final int O_RDWR = 3;
    static final int O_TRUNC = 64;
    static final int O_WRONLY = 2;
    public static final int TYPE_COMM = 64;
    public static final int TYPE_FILESYSTEM = 1;
    public static final int TYPE_NAMED_PIPE = 16;
    public static final int TYPE_PRINTER = 32;
    public static final int TYPE_SERVER = 4;
    public static final int TYPE_SHARE = 8;
    public static final int TYPE_WORKGROUP = 2;
    static long attrExpirationPeriod = Config.getLong("jcifs.smb.client.attrExpirationPeriod", 5000);
    protected static Dfs dfs = new Dfs();
    static boolean ignoreCopyToException = Config.getBoolean("jcifs.smb.client.ignoreCopyToException", true);
    static LogStream log = LogStream.getInstance();
    int addressIndex;
    UniAddress[] addresses;
    private long attrExpiration;
    private int attributes;
    NtlmPasswordAuthentication auth;
    private SmbComBlankResponse blank_resp;
    private String canon;
    private long createTime;
    private DfsReferral dfsReferral;
    int fid;
    private boolean isExists;
    private long lastModified;
    boolean opened;
    private String share;
    private int shareAccess;
    private long size;
    private long sizeExpiration;
    SmbTree tree;
    int tree_num;
    int type;
    String unc;

    class WriterThread extends Thread {
        byte[] b;
        SmbFile dest;
        SmbException e = null;
        int n;
        long off;
        boolean ready;
        SmbComWrite req;
        SmbComWriteAndX reqx;
        ServerMessageBlock resp;
        boolean useNTSmbs;

        WriterThread() throws SmbException {
            super("JCIFS-WriterThread");
            this.useNTSmbs = SmbFile.this.tree.session.transport.hasCapability(16);
            if (this.useNTSmbs) {
                this.reqx = new SmbComWriteAndX();
                this.resp = new SmbComWriteAndXResponse();
            } else {
                this.req = new SmbComWrite();
                this.resp = new SmbComWriteResponse();
            }
            this.ready = false;
        }

        /* access modifiers changed from: package-private */
        public synchronized void write(byte[] b2, int n2, SmbFile dest2, long off2) {
            this.b = b2;
            this.n = n2;
            this.dest = dest2;
            this.off = off2;
            this.ready = false;
            notify();
        }

        public void run() {
            synchronized (this) {
                while (true) {
                    try {
                        notify();
                        this.ready = true;
                        while (this.ready) {
                            wait();
                        }
                        if (this.n != -1) {
                            if (this.useNTSmbs) {
                                this.reqx.setParam(this.dest.fid, this.off, this.n, this.b, 0, this.n);
                                this.dest.send(this.reqx, this.resp);
                            } else {
                                this.req.setParam(this.dest.fid, this.off, this.n, this.b, 0, this.n);
                                this.dest.send(this.req, this.resp);
                            }
                        } else {
                            return;
                        }
                    } catch (SmbException e2) {
                        this.e = e2;
                        notify();
                        return;
                    } catch (Exception x) {
                        this.e = new SmbException("WriterThread", (Throwable) x);
                        notify();
                        return;
                    }
                }
            }
        }
    }

    static {
        try {
            Class.forName("jcifs.Config");
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }
    }

    public SmbFile(String url) throws MalformedURLException {
        this(new URL(null, url, Handler.SMB_HANDLER));
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public SmbFile(SmbFile context, String name) throws MalformedURLException, UnknownHostException {
        this(context.isWorkgroup0() ? new URL(null, "smb://" + name, Handler.SMB_HANDLER) : new URL(context.url, name, Handler.SMB_HANDLER), context.auth);
    }

    public SmbFile(String context, String name) throws MalformedURLException {
        this(new URL(new URL(null, context, Handler.SMB_HANDLER), name, Handler.SMB_HANDLER));
    }

    public SmbFile(String url, NtlmPasswordAuthentication auth2) throws MalformedURLException {
        this(new URL(null, url, Handler.SMB_HANDLER), auth2);
    }

    public SmbFile(String url, NtlmPasswordAuthentication auth2, int shareAccess2) throws MalformedURLException {
        this(new URL(null, url, Handler.SMB_HANDLER), auth2);
        if ((shareAccess2 & -8) != 0) {
            throw new RuntimeException("Illegal shareAccess parameter");
        }
        this.shareAccess = shareAccess2;
    }

    public SmbFile(String context, String name, NtlmPasswordAuthentication auth2) throws MalformedURLException {
        this(new URL(new URL(null, context, Handler.SMB_HANDLER), name, Handler.SMB_HANDLER), auth2);
    }

    public SmbFile(String context, String name, NtlmPasswordAuthentication auth2, int shareAccess2) throws MalformedURLException {
        this(new URL(new URL(null, context, Handler.SMB_HANDLER), name, Handler.SMB_HANDLER), auth2);
        if ((shareAccess2 & -8) != 0) {
            throw new RuntimeException("Illegal shareAccess parameter");
        }
        this.shareAccess = shareAccess2;
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public SmbFile(SmbFile context, String name, int shareAccess2) throws MalformedURLException, UnknownHostException {
        this(context.isWorkgroup0() ? new URL(null, "smb://" + name, Handler.SMB_HANDLER) : new URL(context.url, name, Handler.SMB_HANDLER), context.auth);
        if ((shareAccess2 & -8) != 0) {
            throw new RuntimeException("Illegal shareAccess parameter");
        }
        this.shareAccess = shareAccess2;
    }

    public SmbFile(URL url) {
        this(url, new NtlmPasswordAuthentication(url.getUserInfo()));
    }

    public SmbFile(URL url, NtlmPasswordAuthentication auth2) {
        super(url);
        this.shareAccess = 7;
        this.blank_resp = null;
        this.dfsReferral = null;
        this.tree = null;
        this.auth = auth2 == null ? new NtlmPasswordAuthentication(url.getUserInfo()) : auth2;
        getUncPath0();
    }

    /* JADX WARNING: Illegal instructions before constructor call */
    SmbFile(SmbFile context, String name, int type2, int attributes2, long createTime2, long lastModified2, long size2) throws MalformedURLException, UnknownHostException {
        this(r1);
        URL url;
        if (context.isWorkgroup0()) {
            url = new URL(null, "smb://" + name + "/", Handler.SMB_HANDLER);
        } else {
            url = new URL(context.url, name + ((attributes2 & 16) > 0 ? "/" : ""));
        }
        this.auth = context.auth;
        if (context.share != null) {
            this.tree = context.tree;
            this.dfsReferral = context.dfsReferral;
        }
        int last = name.length() - 1;
        name = name.charAt(last) == '/' ? name.substring(0, last) : name;
        if (context.share == null) {
            this.unc = "\\";
        } else if (context.unc.equals("\\")) {
            this.unc = '\\' + name;
        } else {
            this.unc = context.unc + '\\' + name;
        }
        this.type = type2;
        this.attributes = attributes2;
        this.createTime = createTime2;
        this.lastModified = lastModified2;
        this.size = size2;
        this.isExists = true;
        long currentTimeMillis = System.currentTimeMillis() + attrExpirationPeriod;
        this.sizeExpiration = currentTimeMillis;
        this.attrExpiration = currentTimeMillis;
    }

    private SmbComBlankResponse blank_resp() {
        if (this.blank_resp == null) {
            this.blank_resp = new SmbComBlankResponse();
        }
        return this.blank_resp;
    }

    /* access modifiers changed from: package-private */
    public void resolveDfs(ServerMessageBlock request) throws SmbException {
        SmbException se;
        if (!(request instanceof SmbComClose)) {
            connect0();
            DfsReferral dr = dfs.resolve(this.tree.session.transport.tconHostName, this.tree.share, this.unc, this.auth);
            if (dr != null) {
                String service = null;
                if (request != null) {
                    switch (request.command) {
                        case 37:
                        case 50:
                            switch (((SmbComTransaction) request).subCommand & 255) {
                                case 16:
                                    break;
                                default:
                                    service = "A:";
                                    break;
                            }
                        default:
                            service = "A:";
                            break;
                    }
                }
                DfsReferral start = dr;
                while (true) {
                    try {
                        LogStream logStream = log;
                        if (LogStream.level >= 2) {
                            log.println("DFS redirect: " + dr);
                        }
                        SmbTransport trans = SmbTransport.getSmbTransport(UniAddress.getByName(dr.server), this.url.getPort());
                        trans.connect();
                        this.tree = trans.getSmbSession(this.auth).getSmbTree(dr.share, service);
                        if (!(dr == start || dr.key == null)) {
                            dr.map.put(dr.key, dr);
                        }
                        se = null;
                    } catch (IOException ioe) {
                        if (ioe instanceof SmbException) {
                            se = (SmbException) ioe;
                        } else {
                            se = new SmbException(dr.server, (Throwable) ioe);
                        }
                        dr = dr.next;
                        if (dr == start) {
                        }
                    }
                }
                if (se != null) {
                    throw se;
                }
                LogStream logStream2 = log;
                if (LogStream.level >= 3) {
                    log.println(dr);
                }
                this.dfsReferral = dr;
                if (dr.pathConsumed < 0) {
                    dr.pathConsumed = 0;
                } else if (dr.pathConsumed > this.unc.length()) {
                    dr.pathConsumed = this.unc.length();
                }
                String dunc = this.unc.substring(dr.pathConsumed);
                if (dunc.equals("")) {
                    dunc = "\\";
                }
                if (!dr.path.equals("")) {
                    dunc = "\\" + dr.path + dunc;
                }
                this.unc = dunc;
                if (request != null && request.path != null && request.path.endsWith("\\") && !dunc.endsWith("\\")) {
                    dunc = dunc + "\\";
                }
                if (request != null) {
                    request.path = dunc;
                    request.flags2 |= 4096;
                }
            } else if (this.tree.inDomainDfs && !(request instanceof NtTransQuerySecurityDesc) && !(request instanceof SmbComClose) && !(request instanceof SmbComFindClose2)) {
                throw new SmbException((int) NtStatus.NT_STATUS_NOT_FOUND, false);
            } else if (request != null) {
                request.flags2 &= -4097;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void send(ServerMessageBlock request, ServerMessageBlock response) throws SmbException {
        while (true) {
            resolveDfs(request);
            try {
                this.tree.send(request, response);
                return;
            } catch (DfsReferral dre) {
                if (dre.resolveHashes) {
                    throw dre;
                }
                request.reset();
            }
        }
    }

    static String queryLookup(String query, String param) {
        char[] in = query.toCharArray();
        int eq = 0;
        int st = 0;
        for (int i = 0; i < in.length; i++) {
            char ch = in[i];
            if (ch == '&') {
                if (eq <= st || !new String(in, st, eq - st).equalsIgnoreCase(param)) {
                    st = i + 1;
                } else {
                    int eq2 = eq + 1;
                    return new String(in, eq2, i - eq2);
                }
            } else if (ch == '=') {
                eq = i;
            }
        }
        if (eq <= st || !new String(in, st, eq - st).equalsIgnoreCase(param)) {
            return null;
        }
        int eq3 = eq + 1;
        return new String(in, eq3, in.length - eq3);
    }

    /* access modifiers changed from: package-private */
    public UniAddress getAddress() throws UnknownHostException {
        if (this.addressIndex == 0) {
            return getFirstAddress();
        }
        return this.addresses[this.addressIndex - 1];
    }

    /* access modifiers changed from: package-private */
    public UniAddress getFirstAddress() throws UnknownHostException {
        this.addressIndex = 0;
        String host = this.url.getHost();
        String path = this.url.getPath();
        String query = this.url.getQuery();
        if (query != null) {
            String server = queryLookup(query, "server");
            if (server == null || server.length() <= 0) {
                String address = queryLookup(query, "address");
                if (address != null && address.length() > 0) {
                    byte[] ip = InetAddress.getByName(address).getAddress();
                    this.addresses = new UniAddress[1];
                    this.addresses[0] = new UniAddress(InetAddress.getByAddress(host, ip));
                    return getNextAddress();
                }
            } else {
                this.addresses = new UniAddress[1];
                this.addresses[0] = UniAddress.getByName(server);
                return getNextAddress();
            }
        }
        if (host.length() == 0) {
            try {
                NbtAddress addr = NbtAddress.getByName(NbtAddress.MASTER_BROWSER_NAME, 1, null);
                this.addresses = new UniAddress[1];
                this.addresses[0] = UniAddress.getByName(addr.getHostAddress());
            } catch (UnknownHostException uhe) {
                NtlmPasswordAuthentication.initDefaults();
                if (NtlmPasswordAuthentication.DEFAULT_DOMAIN.equals("?")) {
                    throw uhe;
                }
                this.addresses = UniAddress.getAllByName(NtlmPasswordAuthentication.DEFAULT_DOMAIN, true);
            }
        } else if (path.length() == 0 || path.equals("/")) {
            this.addresses = UniAddress.getAllByName(host, true);
        } else {
            this.addresses = UniAddress.getAllByName(host, false);
        }
        return getNextAddress();
    }

    /* access modifiers changed from: package-private */
    public UniAddress getNextAddress() {
        if (this.addressIndex >= this.addresses.length) {
            return null;
        }
        UniAddress[] uniAddressArr = this.addresses;
        int i = this.addressIndex;
        this.addressIndex = i + 1;
        return uniAddressArr[i];
    }

    /* access modifiers changed from: package-private */
    public boolean hasNextAddress() {
        return this.addressIndex < this.addresses.length;
    }

    /* access modifiers changed from: package-private */
    public void connect0() throws SmbException {
        try {
            connect();
        } catch (UnknownHostException uhe) {
            throw new SmbException("Failed to connect to server", (Throwable) uhe);
        } catch (SmbException se) {
            throw se;
        } catch (IOException ioe) {
            throw new SmbException("Failed to connect to server", (Throwable) ioe);
        }
    }

    /* access modifiers changed from: package-private */
    public void doConnect() throws IOException {
        SmbTransport trans;
        boolean z;
        boolean z2 = true;
        UniAddress addr = getAddress();
        if (this.tree != null) {
            trans = this.tree.session.transport;
        } else {
            trans = SmbTransport.getSmbTransport(addr, this.url.getPort());
            this.tree = trans.getSmbSession(this.auth).getSmbTree(this.share, null);
        }
        String hostName = getServerWithDfs();
        SmbTree smbTree = this.tree;
        if (dfs.resolve(hostName, this.tree.share, null, this.auth) != null) {
            z = true;
        } else {
            z = false;
        }
        smbTree.inDomainDfs = z;
        if (this.tree.inDomainDfs) {
            this.tree.connectionState = 2;
        }
        try {
            LogStream logStream = log;
            if (LogStream.level >= 3) {
                log.println("doConnect: " + addr);
            }
            this.tree.treeConnect(null, null);
        } catch (SmbAuthException sae) {
            if (this.share == null) {
                this.tree = trans.getSmbSession(NtlmPasswordAuthentication.NULL).getSmbTree(null, null);
                this.tree.treeConnect(null, null);
                return;
            }
            NtlmPasswordAuthentication a = NtlmAuthenticator.requestNtlmPasswordAuthentication(this.url.toString(), sae);
            if (a != null) {
                this.auth = a;
                this.tree = trans.getSmbSession(this.auth).getSmbTree(this.share, null);
                SmbTree smbTree2 = this.tree;
                if (dfs.resolve(hostName, this.tree.share, null, this.auth) == null) {
                    z2 = false;
                }
                smbTree2.inDomainDfs = z2;
                if (this.tree.inDomainDfs) {
                    this.tree.connectionState = 2;
                }
                this.tree.treeConnect(null, null);
                return;
            }
            LogStream logStream2 = log;
            if (LogStream.level >= 1 && hasNextAddress()) {
                sae.printStackTrace(log);
            }
            throw sae;
        }
    }

    public void connect() throws IOException {
        if (!isConnected()) {
            getUncPath0();
            getFirstAddress();
            while (true) {
                try {
                    doConnect();
                    return;
                } catch (SmbAuthException sae) {
                    throw sae;
                } catch (SmbException se) {
                    if (getNextAddress() == null) {
                        throw se;
                    }
                    LogStream logStream = log;
                    if (LogStream.level >= 3) {
                        se.printStackTrace(log);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isConnected() {
        return this.tree != null && this.tree.connectionState == 2;
    }

    /* access modifiers changed from: package-private */
    public int open0(int flags, int access, int attrs, int options) throws SmbException {
        connect0();
        LogStream logStream = log;
        if (LogStream.level >= 3) {
            log.println("open0: " + this.unc);
        }
        if (this.tree.session.transport.hasCapability(16)) {
            SmbComNTCreateAndXResponse response = new SmbComNTCreateAndXResponse();
            SmbComNTCreateAndX request = new SmbComNTCreateAndX(this.unc, flags, access, this.shareAccess, attrs, options, null);
            if (this instanceof SmbNamedPipe) {
                request.flags0 |= 22;
                request.desiredAccess |= 131072;
                response.isExtended = true;
            }
            send(request, response);
            int f = response.fid;
            this.attributes = response.extFileAttributes & ATTR_GET_MASK;
            this.attrExpiration = System.currentTimeMillis() + attrExpirationPeriod;
            this.isExists = true;
            return f;
        }
        SmbComOpenAndXResponse response2 = new SmbComOpenAndXResponse();
        send(new SmbComOpenAndX(this.unc, access, flags, null), response2);
        return response2.fid;
    }

    /* access modifiers changed from: package-private */
    public void open(int flags, int access, int attrs, int options) throws SmbException {
        if (!isOpen()) {
            this.fid = open0(flags, access, attrs, options);
            this.opened = true;
            this.tree_num = this.tree.tree_num;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isOpen() {
        return this.opened && isConnected() && this.tree_num == this.tree.tree_num;
    }

    /* access modifiers changed from: package-private */
    public void close(int f, long lastWriteTime) throws SmbException {
        LogStream logStream = log;
        if (LogStream.level >= 3) {
            log.println("close: " + f);
        }
        send(new SmbComClose(f, lastWriteTime), blank_resp());
    }

    /* access modifiers changed from: package-private */
    public void close(long lastWriteTime) throws SmbException {
        if (isOpen()) {
            close(this.fid, lastWriteTime);
            this.opened = false;
        }
    }

    /* access modifiers changed from: package-private */
    public void close() throws SmbException {
        close(0);
    }

    public Principal getPrincipal() {
        return this.auth;
    }

    public String getName() {
        getUncPath0();
        if (this.canon.length() > 1) {
            int i = this.canon.length() - 2;
            while (this.canon.charAt(i) != '/') {
                i--;
            }
            return this.canon.substring(i + 1);
        } else if (this.share != null) {
            return this.share + '/';
        } else {
            if (this.url.getHost().length() > 0) {
                return this.url.getHost() + '/';
            }
            return "smb://";
        }
    }

    public String getParent() {
        String str = this.url.getAuthority();
        if (str.length() <= 0) {
            return "smb://";
        }
        StringBuffer sb = new StringBuffer("smb://");
        sb.append(str);
        getUncPath0();
        if (this.canon.length() > 1) {
            sb.append(this.canon);
        } else {
            sb.append('/');
        }
        String str2 = sb.toString();
        int i = str2.length() - 2;
        while (str2.charAt(i) != '/') {
            i--;
        }
        return str2.substring(0, i + 1);
    }

    public String getPath() {
        return this.url.toString();
    }

    /* access modifiers changed from: package-private */
    public String getUncPath0() {
        int o;
        if (this.unc == null) {
            char[] in = this.url.getPath().toCharArray();
            char[] out = new char[in.length];
            int length = in.length;
            int state = 0;
            int i = 0;
            int o2 = 0;
            while (i < length) {
                switch (state) {
                    case 0:
                        if (in[i] == '/') {
                            o = o2 + 1;
                            out[o2] = in[i];
                            state = 1;
                            break;
                        } else {
                            return null;
                        }
                    case 1:
                        if (in[i] != '/') {
                            if (in[i] != '.' || (i + 1 < length && in[i + 1] != '/')) {
                                if (i + 1 < length && in[i] == '.' && in[i + 1] == '.' && (i + 2 >= length || in[i + 2] == '/')) {
                                    i += 2;
                                    if (o2 != 1) {
                                        o = o2;
                                        do {
                                            o--;
                                            if (o <= 1) {
                                                break;
                                            }
                                        } while (out[o - 1] != '/');
                                        break;
                                    } else {
                                        o = o2;
                                        break;
                                    }
                                } else {
                                    state = 2;
                                }
                            } else {
                                i++;
                                o = o2;
                                break;
                            }
                        } else {
                            o = o2;
                            break;
                        }
                        break;
                    case 2:
                        if (in[i] == '/') {
                            state = 1;
                        }
                        o = o2 + 1;
                        out[o2] = in[i];
                        break;
                    default:
                        o = o2;
                        break;
                }
                i++;
                o2 = o;
            }
            this.canon = new String(out, 0, o2);
            if (o2 > 1) {
                int o3 = o2 - 1;
                int i2 = this.canon.indexOf(47, 1);
                if (i2 < 0) {
                    this.share = this.canon.substring(1);
                    this.unc = "\\";
                } else if (i2 == o3) {
                    this.share = this.canon.substring(1, i2);
                    this.unc = "\\";
                } else {
                    this.share = this.canon.substring(1, i2);
                    String str = this.canon;
                    if (out[o3] != '/') {
                        o3++;
                    }
                    this.unc = str.substring(i2, o3);
                    this.unc = this.unc.replace('/', '\\');
                }
            } else {
                this.share = null;
                this.unc = "\\";
            }
        }
        return this.unc;
    }

    public String getUncPath() {
        getUncPath0();
        if (this.share == null) {
            return "\\\\" + this.url.getHost();
        }
        return "\\\\" + this.url.getHost() + this.canon.replace('/', '\\');
    }

    public String getCanonicalPath() {
        String str = this.url.getAuthority();
        getUncPath0();
        if (str.length() > 0) {
            return "smb://" + this.url.getAuthority() + this.canon;
        }
        return "smb://";
    }

    public String getShare() {
        return this.share;
    }

    /* access modifiers changed from: package-private */
    public String getServerWithDfs() {
        if (this.dfsReferral != null) {
            return this.dfsReferral.server;
        }
        return getServer();
    }

    public String getServer() {
        String str = this.url.getHost();
        if (str.length() == 0) {
            return null;
        }
        return str;
    }

    public int getType() throws SmbException {
        if (this.type == 0) {
            if (getUncPath0().length() > 1) {
                this.type = 1;
            } else if (this.share != null) {
                connect0();
                if (this.share.equals("IPC$")) {
                    this.type = 16;
                } else if (this.tree.service.equals("LPT1:")) {
                    this.type = 32;
                } else if (this.tree.service.equals("COMM")) {
                    this.type = 64;
                } else {
                    this.type = 8;
                }
            } else if (this.url.getAuthority() == null || this.url.getAuthority().length() == 0) {
                this.type = 2;
            } else {
                try {
                    UniAddress addr = getAddress();
                    if (addr.getAddress() instanceof NbtAddress) {
                        int code = ((NbtAddress) addr.getAddress()).getNameType();
                        if (code == 29 || code == 27) {
                            this.type = 2;
                            return this.type;
                        }
                    }
                    this.type = 4;
                } catch (UnknownHostException uhe) {
                    throw new SmbException(this.url.toString(), (Throwable) uhe);
                }
            }
        }
        return this.type;
    }

    /* access modifiers changed from: package-private */
    public boolean isWorkgroup0() throws UnknownHostException {
        if (this.type == 2 || this.url.getHost().length() == 0) {
            this.type = 2;
            return true;
        }
        getUncPath0();
        if (this.share == null) {
            UniAddress addr = getAddress();
            if (addr.getAddress() instanceof NbtAddress) {
                int code = ((NbtAddress) addr.getAddress()).getNameType();
                if (code == 29 || code == 27) {
                    this.type = 2;
                    return true;
                }
            }
            this.type = 4;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public Info queryPath(String path, int infoLevel) throws SmbException {
        connect0();
        LogStream logStream = log;
        if (LogStream.level >= 3) {
            log.println("queryPath: " + path);
        }
        if (this.tree.session.transport.hasCapability(16)) {
            Trans2QueryPathInformationResponse response = new Trans2QueryPathInformationResponse(infoLevel);
            send(new Trans2QueryPathInformation(path, infoLevel), response);
            return response.info;
        }
        SmbComQueryInformationResponse response2 = new SmbComQueryInformationResponse(((long) (this.tree.session.transport.server.serverTimeZone * 1000)) * 60);
        send(new SmbComQueryInformation(path), response2);
        return response2;
    }

    public boolean exists() throws SmbException {
        if (this.attrExpiration > System.currentTimeMillis()) {
            return this.isExists;
        }
        this.attributes = 17;
        this.createTime = 0;
        this.lastModified = 0;
        this.isExists = false;
        try {
            if (this.url.getHost().length() != 0) {
                if (this.share == null) {
                    if (getType() == 2) {
                        UniAddress.getByName(this.url.getHost(), true);
                    } else {
                        UniAddress.getByName(this.url.getHost()).getHostName();
                    }
                } else if (getUncPath0().length() == 1 || this.share.equalsIgnoreCase("IPC$")) {
                    connect0();
                } else {
                    Info info = queryPath(getUncPath0(), 257);
                    this.attributes = info.getAttributes();
                    this.createTime = info.getCreateTime();
                    this.lastModified = info.getLastWriteTime();
                }
            }
            this.isExists = true;
        } catch (UnknownHostException e) {
        } catch (SmbException se) {
            switch (se.getNtStatus()) {
                case NtStatus.NT_STATUS_NO_SUCH_FILE:
                case NtStatus.NT_STATUS_OBJECT_NAME_INVALID:
                case NtStatus.NT_STATUS_OBJECT_NAME_NOT_FOUND:
                case NtStatus.NT_STATUS_OBJECT_PATH_NOT_FOUND:
                    break;
                default:
                    throw se;
            }
        }
        this.attrExpiration = System.currentTimeMillis() + attrExpirationPeriod;
        return this.isExists;
    }

    public boolean canRead() throws SmbException {
        if (getType() == 16) {
            return true;
        }
        return exists();
    }

    public boolean canWrite() throws SmbException {
        if (getType() == 16) {
            return true;
        }
        if (!exists() || (this.attributes & 1) != 0) {
            return false;
        }
        return true;
    }

    public boolean isDirectory() throws SmbException {
        if (getUncPath0().length() == 1) {
            return true;
        }
        if (!exists()) {
            return false;
        }
        if ((this.attributes & 16) != 16) {
            return false;
        }
        return true;
    }

    public boolean isFile() throws SmbException {
        boolean z = true;
        if (getUncPath0().length() == 1) {
            return false;
        }
        exists();
        if ((this.attributes & 16) != 0) {
            z = false;
        }
        return z;
    }

    public boolean isHidden() throws SmbException {
        boolean z = true;
        if (this.share == null) {
            return false;
        }
        if (getUncPath0().length() != 1) {
            exists();
            if ((this.attributes & 2) != 2) {
                z = false;
            }
            return z;
        } else if (this.share.endsWith("$")) {
            return true;
        } else {
            return false;
        }
    }

    public String getDfsPath() throws SmbException {
        resolveDfs(null);
        if (this.dfsReferral == null) {
            return null;
        }
        String path = ("smb:/" + this.dfsReferral.server + "/" + this.dfsReferral.share + this.unc).replace('\\', '/');
        if (isDirectory()) {
            return path + '/';
        }
        return path;
    }

    public long createTime() throws SmbException {
        if (getUncPath0().length() <= 1) {
            return 0;
        }
        exists();
        return this.createTime;
    }

    public long lastModified() throws SmbException {
        if (getUncPath0().length() <= 1) {
            return 0;
        }
        exists();
        return this.lastModified;
    }

    public String[] list() throws SmbException {
        return list("*", 22, null, null);
    }

    public String[] list(SmbFilenameFilter filter) throws SmbException {
        return list("*", 22, filter, null);
    }

    public SmbFile[] listFiles() throws SmbException {
        return listFiles("*", 22, null, null);
    }

    public SmbFile[] listFiles(String wildcard) throws SmbException {
        return listFiles(wildcard, 22, null, null);
    }

    public SmbFile[] listFiles(SmbFilenameFilter filter) throws SmbException {
        return listFiles("*", 22, filter, null);
    }

    public SmbFile[] listFiles(SmbFileFilter filter) throws SmbException {
        return listFiles("*", 22, null, filter);
    }

    /* access modifiers changed from: package-private */
    public String[] list(String wildcard, int searchAttributes, SmbFilenameFilter fnf, SmbFileFilter ff) throws SmbException {
        ArrayList list = new ArrayList();
        doEnum(list, false, wildcard, searchAttributes, fnf, ff);
        return (String[]) list.toArray(new String[list.size()]);
    }

    /* access modifiers changed from: package-private */
    public SmbFile[] listFiles(String wildcard, int searchAttributes, SmbFilenameFilter fnf, SmbFileFilter ff) throws SmbException {
        ArrayList list = new ArrayList();
        doEnum(list, true, wildcard, searchAttributes, fnf, ff);
        return (SmbFile[]) list.toArray(new SmbFile[list.size()]);
    }

    /* access modifiers changed from: package-private */
    public void doEnum(ArrayList list, boolean files, String wildcard, int searchAttributes, SmbFilenameFilter fnf, SmbFileFilter ff) throws SmbException {
        if (ff != null && (ff instanceof DosFileFilter)) {
            DosFileFilter dff = (DosFileFilter) ff;
            if (dff.wildcard != null) {
                wildcard = dff.wildcard;
            }
            searchAttributes = dff.attributes;
        }
        try {
            if (this.url.getHost().length() == 0 || getType() == 2) {
                doNetServerEnum(list, files, wildcard, searchAttributes, fnf, ff);
            } else if (this.share == null) {
                doShareEnum(list, files, wildcard, searchAttributes, fnf, ff);
            } else {
                doFindFirstNext(list, files, wildcard, searchAttributes, fnf, ff);
            }
        } catch (UnknownHostException uhe) {
            throw new SmbException(this.url.toString(), (Throwable) uhe);
        } catch (MalformedURLException mue) {
            throw new SmbException(this.url.toString(), (Throwable) mue);
        }
    }

    /* access modifiers changed from: package-private */
    public void doShareEnum(ArrayList list, boolean files, String wildcard, int searchAttributes, SmbFilenameFilter fnf, SmbFileFilter ff) throws SmbException, UnknownHostException, MalformedURLException {
        FileEntry[] entries;
        IOException last = null;
        if (this.url.getPath().lastIndexOf(47) != this.url.getPath().length() - 1) {
            throw new SmbException(this.url.toString() + " directory must end with '/'");
        } else if (getType() != 4) {
            throw new SmbException("The requested list operations is invalid: " + this.url.toString());
        } else {
            HashMap map = new HashMap();
            if (dfs.isTrustedDomain(getServer(), this.auth)) {
                try {
                    FileEntry[] entries2 = doDfsRootEnum();
                    for (FileEntry e : entries2) {
                        if (!map.containsKey(e)) {
                            map.put(e, e);
                        }
                    }
                } catch (IOException ioe) {
                    LogStream logStream = log;
                    if (LogStream.level >= 4) {
                        ioe.printStackTrace(log);
                    }
                }
            }
            UniAddress addr = getFirstAddress();
            loop1:
            while (addr != null) {
                try {
                    doConnect();
                    try {
                        entries = doMsrpcShareEnum();
                    } catch (IOException ioe2) {
                        LogStream logStream2 = log;
                        if (LogStream.level >= 3) {
                            ioe2.printStackTrace(log);
                        }
                        entries = doNetShareEnum();
                    }
                    for (FileEntry e2 : entries) {
                        if (!map.containsKey(e2)) {
                            map.put(e2, e2);
                        }
                    }
                    break loop1;
                } catch (IOException ioe3) {
                    LogStream logStream3 = log;
                    if (LogStream.level >= 3) {
                        ioe3.printStackTrace(log);
                    }
                    last = ioe3;
                    addr = getNextAddress();
                }
            }
            if (last == null || !map.isEmpty()) {
                for (FileEntry e3 : map.keySet()) {
                    String name = e3.getName();
                    if ((fnf == null || fnf.accept(this, name)) && name.length() > 0) {
                        SmbFile f = new SmbFile(this, name, e3.getType(), 17, 0, 0, 0);
                        if (ff == null || ff.accept(f)) {
                            if (files) {
                                list.add(f);
                            } else {
                                list.add(name);
                            }
                        }
                    }
                }
            } else if (!(last instanceof SmbException)) {
                throw new SmbException(this.url.toString(), (Throwable) last);
            } else {
                throw ((SmbException) last);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public FileEntry[] doDfsRootEnum() throws IOException {
        DcerpcHandle handle = DcerpcHandle.getHandle("ncacn_np:" + getAddress().getHostAddress() + "[\\PIPE\\netdfs]", this.auth);
        try {
            MsrpcDfsRootEnum rpc = new MsrpcDfsRootEnum(getServer());
            handle.sendrecv(rpc);
            if (rpc.retval != 0) {
                throw new SmbException(rpc.retval, true);
            }
            FileEntry[] entries = rpc.getEntries();
            try {
            } catch (IOException ioe) {
                LogStream logStream = log;
                if (LogStream.level >= 4) {
                    ioe.printStackTrace(log);
                }
            }
            return entries;
        } finally {
            try {
                handle.close();
            } catch (IOException ioe2) {
                LogStream logStream2 = log;
                if (LogStream.level >= 4) {
                    ioe2.printStackTrace(log);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public FileEntry[] doMsrpcShareEnum() throws IOException {
        MsrpcShareEnum rpc = new MsrpcShareEnum(this.url.getHost());
        DcerpcHandle handle = DcerpcHandle.getHandle("ncacn_np:" + getAddress().getHostAddress() + "[\\PIPE\\srvsvc]", this.auth);
        try {
            handle.sendrecv(rpc);
            if (rpc.retval != 0) {
                throw new SmbException(rpc.retval, true);
            }
            FileEntry[] entries = rpc.getEntries();
            try {
            } catch (IOException ioe) {
                LogStream logStream = log;
                if (LogStream.level >= 4) {
                    ioe.printStackTrace(log);
                }
            }
            return entries;
        } finally {
            try {
                handle.close();
            } catch (IOException ioe2) {
                LogStream logStream2 = log;
                if (LogStream.level >= 4) {
                    ioe2.printStackTrace(log);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public FileEntry[] doNetShareEnum() throws SmbException {
        SmbComTransaction req = new NetShareEnum();
        SmbComTransactionResponse resp = new NetShareEnumResponse();
        send(req, resp);
        if (resp.status == 0) {
            return resp.results;
        }
        throw new SmbException(resp.status, true);
    }

    /* access modifiers changed from: package-private */
    public void doNetServerEnum(ArrayList list, boolean files, String wildcard, int searchAttributes, SmbFilenameFilter fnf, SmbFileFilter ff) throws SmbException, UnknownHostException, MalformedURLException {
        NetServerEnum2Response netServerEnum2Response;
        NetServerEnum2 netServerEnum2;
        boolean more;
        int listType = this.url.getHost().length() == 0 ? 0 : getType();
        if (listType == 0) {
            connect0();
            netServerEnum2 = new NetServerEnum2(this.tree.session.transport.server.oemDomainName, Integer.MIN_VALUE);
            netServerEnum2Response = new NetServerEnum2Response();
        } else if (listType == 2) {
            netServerEnum2 = new NetServerEnum2(this.url.getHost(), -1);
            netServerEnum2Response = new NetServerEnum2Response();
        } else {
            throw new SmbException("The requested list operations is invalid: " + this.url.toString());
        }
        do {
            send(netServerEnum2, netServerEnum2Response);
            if (netServerEnum2Response.status == 0 || netServerEnum2Response.status == 234) {
                more = netServerEnum2Response.status == 234;
                int n = more ? netServerEnum2Response.numEntries - 1 : netServerEnum2Response.numEntries;
                for (int i = 0; i < n; i++) {
                    FileEntry e = netServerEnum2Response.results[i];
                    String name = e.getName();
                    if ((fnf == null || fnf.accept(this, name)) && name.length() > 0) {
                        SmbFile f = new SmbFile(this, name, e.getType(), 17, 0, 0, 0);
                        if (ff == null || ff.accept(f)) {
                            if (files) {
                                list.add(f);
                            } else {
                                list.add(name);
                            }
                        }
                    }
                }
                if (getType() == 2) {
                    netServerEnum2.subCommand = -41;
                    netServerEnum2.reset(0, netServerEnum2Response.lastName);
                    netServerEnum2Response.reset();
                } else {
                    return;
                }
            } else {
                throw new SmbException(netServerEnum2Response.status, true);
            }
        } while (more);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x00c0, code lost:
        if (r16 != HASH_DOT_DOT) goto L_0x00d5;
     */
    public void doFindFirstNext(ArrayList list, boolean files, String wildcard, int searchAttributes, SmbFilenameFilter fnf, SmbFileFilter ff) throws SmbException, UnknownHostException, MalformedURLException {
        String path = getUncPath0();
        String p = this.url.getPath();
        if (p.lastIndexOf(47) != p.length() - 1) {
            throw new SmbException(this.url.toString() + " directory must end with '/'");
        }
        Trans2FindFirst2 trans2FindFirst2 = new Trans2FindFirst2(path, wildcard, searchAttributes);
        Trans2FindFirst2Response resp = new Trans2FindFirst2Response();
        LogStream logStream = log;
        if (LogStream.level >= 3) {
            log.println("doFindFirstNext: " + trans2FindFirst2.path);
        }
        send(trans2FindFirst2, resp);
        int sid = resp.sid;
        Trans2FindNext2 trans2FindNext2 = new Trans2FindNext2(sid, resp.resumeKey, resp.lastName);
        resp.subCommand = 2;
        while (true) {
            for (int i = 0; i < resp.numEntries; i++) {
                FileEntry e = resp.results[i];
                String name = e.getName();
                if (name.length() < 3) {
                    int h = name.hashCode();
                    if (h != HASH_DOT) {
                    }
                    if (!name.equals(".")) {
                        if (name.equals("..")) {
                        }
                    }
                }
                if ((fnf == null || fnf.accept(this, name)) && name.length() > 0) {
                    SmbFile f = new SmbFile(this, name, 1, e.getAttributes(), e.createTime(), e.lastModified(), e.length());
                    if (ff == null || ff.accept(f)) {
                        if (files) {
                            list.add(f);
                        } else {
                            list.add(name);
                        }
                    }
                }
            }
            if (resp.isEndOfSearch || resp.numEntries == 0) {
                try {
                    send(new SmbComFindClose2(sid), blank_resp());
                    return;
                } catch (SmbException se) {
                    LogStream logStream2 = log;
                    if (LogStream.level >= 4) {
                        se.printStackTrace(log);
                        return;
                    }
                    return;
                }
            } else {
                trans2FindNext2.reset(resp.resumeKey, resp.lastName);
                resp.reset();
                send(trans2FindNext2, resp);
            }
        }
    }

    public void renameTo(SmbFile dest) throws SmbException {
        if (getUncPath0().length() == 1 || dest.getUncPath0().length() == 1) {
            throw new SmbException("Invalid operation for workgroups, servers, or shares");
        }
        resolveDfs(null);
        dest.resolveDfs(null);
        if (!this.tree.equals(dest.tree)) {
            throw new SmbException("Invalid operation for workgroups, servers, or shares");
        }
        LogStream logStream = log;
        if (LogStream.level >= 3) {
            log.println("renameTo: " + this.unc + " -> " + dest.unc);
        }
        this.sizeExpiration = 0;
        this.attrExpiration = 0;
        dest.attrExpiration = 0;
        send(new SmbComRename(this.unc, dest.unc), blank_resp());
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:89:0x0210, code lost:
        if (r18 != 1) goto L_0x021c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:90:0x0212, code lost:
        r18 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:95:0x021c, code lost:
        r18 = 1;
     */
    public void copyTo0(SmbFile dest, byte[][] b, int bsize, WriterThread w, SmbComReadAndX req, SmbComReadAndXResponse resp) throws SmbException {
        if (this.attrExpiration < System.currentTimeMillis()) {
            this.attributes = 17;
            this.createTime = 0;
            this.lastModified = 0;
            this.isExists = false;
            Info info = queryPath(getUncPath0(), 257);
            this.attributes = info.getAttributes();
            this.createTime = info.getCreateTime();
            this.lastModified = info.getLastWriteTime();
            this.isExists = true;
            this.attrExpiration = System.currentTimeMillis() + attrExpirationPeriod;
        }
        if (isDirectory()) {
            if (dest.getUncPath0().length() > 1) {
                try {
                    dest.mkdir();
                    dest.setPathInformation(this.attributes, this.createTime, this.lastModified);
                } catch (SmbException se) {
                    if (!(se.getNtStatus() == -1073741790 || se.getNtStatus() == -1073741771)) {
                        throw se;
                    }
                }
            }
            SmbFile[] files = listFiles("*", 22, null, null);
            int i = 0;
            while (i < files.length) {
                try {
                    files[i].copyTo0(new SmbFile(dest, files[i].getName(), files[i].type, files[i].attributes, files[i].createTime, files[i].lastModified, files[i].size), b, bsize, w, req, resp);
                    i++;
                } catch (UnknownHostException uhe) {
                    throw new SmbException(this.url.toString(), (Throwable) uhe);
                } catch (MalformedURLException mue) {
                    throw new SmbException(this.url.toString(), (Throwable) mue);
                }
            }
            return;
        }
        try {
            open(1, 0, 128, 0);
            dest.open(82, 258, this.attributes, 0);
        } catch (InterruptedException ie) {
            throw new SmbException(dest.url.toString(), (Throwable) ie);
        } catch (SmbAuthException sae) {
            if ((dest.attributes & 1) != 0) {
                dest.setPathInformation(dest.attributes & -2, 0, 0);
                dest.open(82, 258, this.attributes, 0);
            } else {
                throw sae;
            }
        } catch (SmbException se2) {
            try {
                if (!ignoreCopyToException) {
                    throw new SmbException("Failed to copy file from [" + toString() + "] to [" + dest.toString() + "]", (Throwable) se2);
                }
                LogStream logStream = log;
                if (LogStream.level > 1) {
                    se2.printStackTrace(log);
                }
                return;
            } finally {
                close();
            }
        }
        int i2 = 0;
        long off = 0;
        loop1:
        while (true) {
            req.setParam(this.fid, off, bsize);
            resp.setParam(b[i2], 0);
            send(req, resp);
            synchronized (w) {
                if (w.e != null) {
                    throw w.e;
                }
                while (!w.ready) {
                    w.wait();
                }
                if (w.e != null) {
                    throw w.e;
                } else if (resp.dataLength <= 0) {
                    dest.send(new Trans2SetFileInformation(dest.fid, this.attributes, this.createTime, this.lastModified), new Trans2SetFileInformationResponse());
                    dest.close(0);
                    close();
                    return;
                } else {
                    w.write(b[i2], resp.dataLength, dest, off);
                }
            }
            off += (long) resp.dataLength;
        }
    }

    public void copyTo(SmbFile dest) throws SmbException {
        if (this.share == null || dest.share == null) {
            throw new SmbException("Invalid operation for workgroups or servers");
        }
        SmbComReadAndX req = new SmbComReadAndX();
        SmbComReadAndXResponse resp = new SmbComReadAndXResponse();
        connect0();
        dest.connect0();
        resolveDfs(null);
        try {
            if (getAddress().equals(dest.getAddress()) && this.canon.regionMatches(true, 0, dest.canon, 0, Math.min(this.canon.length(), dest.canon.length()))) {
                throw new SmbException("Source and destination paths overlap.");
            }
        } catch (UnknownHostException e) {
        }
        WriterThread w = new WriterThread();
        w.setDaemon(true);
        w.start();
        SmbTransport t1 = this.tree.session.transport;
        SmbTransport t2 = dest.tree.session.transport;
        if (t1.snd_buf_size < t2.snd_buf_size) {
            t2.snd_buf_size = t1.snd_buf_size;
        } else {
            t1.snd_buf_size = t2.snd_buf_size;
        }
        int bsize = Math.min(t1.rcv_buf_size - 70, t1.snd_buf_size - 70);
        try {
            copyTo0(dest, (byte[][]) Array.newInstance(Byte.TYPE, new int[]{2, bsize}), bsize, w, req, resp);
        } finally {
            w.write(null, -1, null, 0);
        }
    }

    public void delete() throws SmbException {
        exists();
        getUncPath0();
        delete(this.unc);
    }

    /* access modifiers changed from: package-private */
    public void delete(String fileName) throws SmbException {
        if (getUncPath0().length() == 1) {
            throw new SmbException("Invalid operation for workgroups, servers, or shares");
        }
        if (System.currentTimeMillis() > this.attrExpiration) {
            this.attributes = 17;
            this.createTime = 0;
            this.lastModified = 0;
            this.isExists = false;
            Info info = queryPath(getUncPath0(), 257);
            this.attributes = info.getAttributes();
            this.createTime = info.getCreateTime();
            this.lastModified = info.getLastWriteTime();
            this.attrExpiration = System.currentTimeMillis() + attrExpirationPeriod;
            this.isExists = true;
        }
        if ((this.attributes & 1) != 0) {
            setReadWrite();
        }
        LogStream logStream = log;
        if (LogStream.level >= 3) {
            log.println("delete: " + fileName);
        }
        if ((this.attributes & 16) != 0) {
            try {
                SmbFile[] l = listFiles("*", 22, null, null);
                for (SmbFile delete : l) {
                    delete.delete();
                }
            } catch (SmbException se) {
                if (se.getNtStatus() != -1073741809) {
                    throw se;
                }
            }
            send(new SmbComDeleteDirectory(fileName), blank_resp());
        } else {
            send(new SmbComDelete(fileName), blank_resp());
        }
        this.sizeExpiration = 0;
        this.attrExpiration = 0;
    }

    public long length() throws SmbException {
        if (this.sizeExpiration > System.currentTimeMillis()) {
            return this.size;
        }
        if (getType() == 8) {
            Trans2QueryFSInformationResponse response = new Trans2QueryFSInformationResponse(1);
            send(new Trans2QueryFSInformation(1), response);
            this.size = response.info.getCapacity();
        } else if (getUncPath0().length() <= 1 || this.type == 16) {
            this.size = 0;
        } else {
            this.size = queryPath(getUncPath0(), 258).getSize();
        }
        this.sizeExpiration = System.currentTimeMillis() + attrExpirationPeriod;
        return this.size;
    }

    public long getDiskFreeSpace() throws SmbException {
        if (getType() != 8 && this.type != 1) {
            return 0;
        }
        try {
            return queryFSInformation(1007);
        } catch (SmbException ex) {
            switch (ex.getNtStatus()) {
                case NtStatus.NT_STATUS_UNSUCCESSFUL:
                case NtStatus.NT_STATUS_INVALID_INFO_CLASS:
                    return queryFSInformation(1);
                default:
                    throw ex;
            }
        }
    }

    private long queryFSInformation(int level) throws SmbException {
        Trans2QueryFSInformationResponse response = new Trans2QueryFSInformationResponse(level);
        send(new Trans2QueryFSInformation(level), response);
        if (this.type == 8) {
            this.size = response.info.getCapacity();
            this.sizeExpiration = System.currentTimeMillis() + attrExpirationPeriod;
        }
        return response.info.getFree();
    }

    public void mkdir() throws SmbException {
        String path = getUncPath0();
        if (path.length() == 1) {
            throw new SmbException("Invalid operation for workgroups, servers, or shares");
        }
        LogStream logStream = log;
        if (LogStream.level >= 3) {
            log.println("mkdir: " + path);
        }
        send(new SmbComCreateDirectory(path), blank_resp());
        this.sizeExpiration = 0;
        this.attrExpiration = 0;
    }

    public void mkdirs() throws SmbException {
        try {
            SmbFile parent = new SmbFile(getParent(), this.auth);
            if (!parent.exists()) {
                parent.mkdirs();
            }
            mkdir();
        } catch (IOException e) {
        }
    }

    public void createNewFile() throws SmbException {
        if (getUncPath0().length() == 1) {
            throw new SmbException("Invalid operation for workgroups, servers, or shares");
        }
        close(open0(51, 0, 128, 0), 0);
    }

    /* access modifiers changed from: package-private */
    public void setPathInformation(int attrs, long ctime, long mtime) throws SmbException {
        exists();
        int dir = this.attributes & 16;
        int f = open0(1, 256, dir, dir != 0 ? 1 : 64);
        send(new Trans2SetFileInformation(f, attrs | dir, ctime, mtime), new Trans2SetFileInformationResponse());
        close(f, 0);
        this.attrExpiration = 0;
    }

    public void setCreateTime(long time) throws SmbException {
        if (getUncPath0().length() == 1) {
            throw new SmbException("Invalid operation for workgroups, servers, or shares");
        }
        setPathInformation(0, time, 0);
    }

    public void setLastModified(long time) throws SmbException {
        if (getUncPath0().length() == 1) {
            throw new SmbException("Invalid operation for workgroups, servers, or shares");
        }
        setPathInformation(0, 0, time);
    }

    public int getAttributes() throws SmbException {
        if (getUncPath0().length() == 1) {
            return 0;
        }
        exists();
        return this.attributes & ATTR_GET_MASK;
    }

    public void setAttributes(int attrs) throws SmbException {
        if (getUncPath0().length() == 1) {
            throw new SmbException("Invalid operation for workgroups, servers, or shares");
        }
        setPathInformation(attrs & ATTR_SET_MASK, 0, 0);
    }

    public void setReadOnly() throws SmbException {
        setAttributes(getAttributes() | 1);
    }

    public void setReadWrite() throws SmbException {
        setAttributes(getAttributes() & -2);
    }

    public URL toURL() throws MalformedURLException {
        return this.url;
    }

    public int hashCode() {
        int hash;
        try {
            hash = getAddress().hashCode();
        } catch (UnknownHostException e) {
            hash = getServer().toUpperCase().hashCode();
        }
        getUncPath0();
        return this.canon.toUpperCase().hashCode() + hash;
    }

    /* access modifiers changed from: protected */
    public boolean pathNamesPossiblyEqual(String path1, String path2) {
        int p1 = path1.lastIndexOf(47);
        int p2 = path2.lastIndexOf(47);
        int l1 = path1.length() - p1;
        int l2 = path2.length() - p2;
        if (l1 > 1 && path1.charAt(p1 + 1) == '.') {
            return true;
        }
        if (l2 > 1 && path2.charAt(p2 + 1) == '.') {
            return true;
        }
        if (l1 != l2 || !path1.regionMatches(true, p1, path2, p2, l1)) {
            return false;
        }
        return true;
    }

    /* JADX WARNING: type inference failed for: r6v0, types: [java.lang.Object] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public boolean equals(Object r6) {
        if (r6 instanceof SmbFile) {
            SmbFile f = r6;
            if (this == f) {
                return true;
            }
            if (pathNamesPossiblyEqual(this.url.getPath(), f.url.getPath())) {
                getUncPath0();
                f.getUncPath0();
                if (this.canon.equalsIgnoreCase(f.canon)) {
                    try {
                        return getAddress().equals(f.getAddress());
                    } catch (UnknownHostException e) {
                        return getServer().equalsIgnoreCase(f.getServer());
                    }
                }
            }
        }
        return false;
    }

    public String toString() {
        return this.url.toString();
    }

    public int getContentLength() {
        try {
            return (int) (length() & 4294967295L);
        } catch (SmbException e) {
            return 0;
        }
    }

    public long getDate() {
        try {
            return lastModified();
        } catch (SmbException e) {
            return 0;
        }
    }

    public long getLastModified() {
        try {
            return lastModified();
        } catch (SmbException e) {
            return 0;
        }
    }

    public InputStream getInputStream() throws IOException {
        return new SmbFileInputStream(this);
    }

    public OutputStream getOutputStream() throws IOException {
        return new SmbFileOutputStream(this);
    }

    private void processAces(ACE[] aces, boolean resolveSids) throws IOException {
        String server = getServerWithDfs();
        if (resolveSids) {
            SID[] sids = new SID[aces.length];
            for (int ai = 0; ai < aces.length; ai++) {
                sids[ai] = aces[ai].sid;
            }
            for (int off = 0; off < sids.length; off += 64) {
                int len = sids.length - off;
                if (len > 64) {
                    len = 64;
                }
                SID.resolveSids(server, this.auth, sids, off, len);
            }
            return;
        }
        for (int ai2 = 0; ai2 < aces.length; ai2++) {
            aces[ai2].sid.origin_server = server;
            aces[ai2].sid.origin_auth = this.auth;
        }
    }

    /* JADX INFO: finally extract failed */
    public ACE[] getSecurity(boolean resolveSids) throws IOException {
        int i;
        if (isDirectory()) {
            i = 1;
        } else {
            i = 0;
        }
        int f = open0(1, 131072, 0, i);
        NtTransQuerySecurityDesc request = new NtTransQuerySecurityDesc(f, 4);
        NtTransQuerySecurityDescResponse response = new NtTransQuerySecurityDescResponse();
        try {
            send(request, response);
            close(f, 0);
            ACE[] aces = response.securityDescriptor.aces;
            if (aces != null) {
                processAces(aces, resolveSids);
            }
            return aces;
        } catch (Throwable th) {
            close(f, 0);
            throw th;
        }
    }

    public ACE[] getShareSecurity(boolean resolveSids) throws IOException {
        String path = this.url.getPath();
        resolveDfs(null);
        String server = getServerWithDfs();
        MsrpcShareGetInfo rpc = new MsrpcShareGetInfo(server, this.tree.share);
        DcerpcHandle handle = DcerpcHandle.getHandle("ncacn_np:" + server + "[\\PIPE\\srvsvc]", this.auth);
        try {
            handle.sendrecv(rpc);
            if (rpc.retval != 0) {
                throw new SmbException(rpc.retval, true);
            }
            ACE[] aces = rpc.getSecurity();
            if (aces != null) {
                processAces(aces, resolveSids);
            }
            try {
            } catch (IOException ioe) {
                LogStream logStream = log;
                if (LogStream.level >= 1) {
                    ioe.printStackTrace(log);
                }
            }
            return aces;
        } finally {
            try {
                handle.close();
            } catch (IOException ioe2) {
                LogStream logStream2 = log;
                if (LogStream.level >= 1) {
                    ioe2.printStackTrace(log);
                }
            }
        }
    }

    public ACE[] getSecurity() throws IOException {
        return getSecurity(false);
    }
}
