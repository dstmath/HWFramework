package java.net;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.sql.Types;
import sun.security.action.GetPropertyAction;
import sun.security.x509.GeneralNameInterface;
import sun.util.calendar.BaseCalendar;

class SocksSocketImpl extends PlainSocketImpl implements SocksConsts {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private boolean applicationSetProxy;
    private InputStream cmdIn;
    private OutputStream cmdOut;
    private Socket cmdsock;
    private InetSocketAddress external_address;
    private String server;
    private int serverPort;
    private boolean useV4;

    /* renamed from: java.net.SocksSocketImpl.1 */
    class AnonymousClass1 implements PrivilegedExceptionAction<Void> {
        final /* synthetic */ String val$host;
        final /* synthetic */ int val$port;
        final /* synthetic */ int val$timeout;

        AnonymousClass1(String val$host, int val$port, int val$timeout) {
            this.val$host = val$host;
            this.val$port = val$port;
            this.val$timeout = val$timeout;
        }

        public Void run() throws IOException {
            SocksSocketImpl.this.superConnectServer(this.val$host, this.val$port, this.val$timeout);
            SocksSocketImpl.this.cmdIn = SocksSocketImpl.this.getInputStream();
            SocksSocketImpl.this.cmdOut = SocksSocketImpl.this.getOutputStream();
            return null;
        }
    }

    /* renamed from: java.net.SocksSocketImpl.2 */
    class AnonymousClass2 implements PrivilegedAction<PasswordAuthentication> {
        final /* synthetic */ InetAddress val$addr;

        AnonymousClass2(InetAddress val$addr) {
            this.val$addr = val$addr;
        }

        public PasswordAuthentication run() {
            return Authenticator.requestPasswordAuthentication(SocksSocketImpl.this.server, this.val$addr, SocksSocketImpl.this.serverPort, "SOCKS5", "SOCKS authentication", null);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.net.SocksSocketImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.net.SocksSocketImpl.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.net.SocksSocketImpl.<clinit>():void");
    }

    SocksSocketImpl() {
        this.server = null;
        this.serverPort = SocksConsts.DEFAULT_PORT;
        this.useV4 = false;
        this.cmdsock = null;
        this.cmdIn = null;
        this.cmdOut = null;
    }

    SocksSocketImpl(String server, int port) {
        this.server = null;
        this.serverPort = SocksConsts.DEFAULT_PORT;
        this.useV4 = false;
        this.cmdsock = null;
        this.cmdIn = null;
        this.cmdOut = null;
        this.server = server;
        if (port == -1) {
            port = SocksConsts.DEFAULT_PORT;
        }
        this.serverPort = port;
    }

    SocksSocketImpl(Proxy proxy) {
        this.server = null;
        this.serverPort = SocksConsts.DEFAULT_PORT;
        this.useV4 = false;
        this.cmdsock = null;
        this.cmdIn = null;
        this.cmdOut = null;
        SocketAddress a = proxy.address();
        if (a instanceof InetSocketAddress) {
            InetSocketAddress ad = (InetSocketAddress) a;
            this.server = ad.getHostString();
            this.serverPort = ad.getPort();
        }
    }

    void setV4() {
        this.useV4 = true;
    }

    private synchronized void privilegedConnect(String host, int port, int timeout) throws IOException {
        try {
            AccessController.doPrivileged(new AnonymousClass1(host, port, timeout));
        } catch (PrivilegedActionException pae) {
            throw ((IOException) pae.getException());
        }
    }

    private void superConnectServer(String host, int port, int timeout) throws IOException {
        super.connect(new InetSocketAddress(host, port), timeout);
    }

    private static int remainingMillis(long deadlineMillis) throws IOException {
        if (deadlineMillis == 0) {
            return 0;
        }
        long remaining = deadlineMillis - System.currentTimeMillis();
        if (remaining > 0) {
            return (int) remaining;
        }
        throw new SocketTimeoutException();
    }

    private int readSocksReply(InputStream in, byte[] data) throws IOException {
        return readSocksReply(in, data, 0);
    }

    private int readSocksReply(InputStream in, byte[] data, long deadlineMillis) throws IOException {
        int len = data.length;
        int received = 0;
        int attempts = 0;
        while (received < len && attempts < 3) {
            try {
                int count = ((SocketInputStream) in).read(data, received, len - received, remainingMillis(deadlineMillis));
                if (count < 0) {
                    throw new SocketException("Malformed reply from SOCKS server");
                }
                received += count;
                attempts++;
            } catch (SocketTimeoutException e) {
                throw new SocketTimeoutException("Connect timed out");
            }
        }
        return received;
    }

    private boolean authenticate(byte method, InputStream in, BufferedOutputStream out) throws IOException {
        return authenticate(method, in, out, 0);
    }

    private boolean authenticate(byte method, InputStream in, BufferedOutputStream out, long deadlineMillis) throws IOException {
        if (method == null) {
            return true;
        }
        if (method != 2) {
            return false;
        }
        String userName;
        String str = null;
        PasswordAuthentication pw = (PasswordAuthentication) AccessController.doPrivileged(new AnonymousClass2(InetAddress.getByName(this.server)));
        if (pw != null) {
            userName = pw.getUserName();
            str = new String(pw.getPassword());
        } else {
            userName = (String) AccessController.doPrivileged(new GetPropertyAction("user.name"));
        }
        if (userName == null) {
            return false;
        }
        out.write(1);
        out.write(userName.length());
        try {
            out.write(userName.getBytes("ISO-8859-1"));
        } catch (UnsupportedEncodingException e) {
            if (!-assertionsDisabled) {
                throw new AssertionError();
            }
        }
        if (str != null) {
            out.write(str.length());
            try {
                out.write(str.getBytes("ISO-8859-1"));
            } catch (UnsupportedEncodingException e2) {
                if (!-assertionsDisabled) {
                    throw new AssertionError();
                }
            }
        }
        out.write(0);
        out.flush();
        byte[] data = new byte[2];
        if (readSocksReply(in, data, deadlineMillis) == 2 && data[1] == null) {
            return true;
        }
        out.close();
        in.close();
        return false;
    }

    private void connectV4(InputStream in, OutputStream out, InetSocketAddress endpoint, long deadlineMillis) throws IOException {
        if (endpoint.getAddress() instanceof Inet4Address) {
            out.write(4);
            out.write(1);
            out.write((endpoint.getPort() >> 8) & 255);
            out.write((endpoint.getPort() >> 0) & 255);
            out.write(endpoint.getAddress().getAddress());
            try {
                out.write(getUserName().getBytes("ISO-8859-1"));
            } catch (UnsupportedEncodingException e) {
                if (!-assertionsDisabled) {
                    throw new AssertionError();
                }
            }
            out.write(0);
            out.flush();
            byte[] data = new byte[8];
            int n = readSocksReply(in, data, deadlineMillis);
            if (n != 8) {
                throw new SocketException("Reply from SOCKS server has bad length: " + n);
            } else if (data[0] == null || data[0] == (byte) 4) {
                SocketException socketException = null;
                switch (data[1]) {
                    case (byte) 90:
                        this.external_address = endpoint;
                        break;
                    case Types.DATE /*91*/:
                        socketException = new SocketException("SOCKS request rejected");
                        break;
                    case Types.TIME /*92*/:
                        socketException = new SocketException("SOCKS server couldn't reach destination");
                        break;
                    case Types.TIMESTAMP /*93*/:
                        socketException = new SocketException("SOCKS authentication failed");
                        break;
                    default:
                        socketException = new SocketException("Reply from SOCKS server contains bad status");
                        break;
                }
                if (socketException != null) {
                    in.close();
                    out.close();
                    throw socketException;
                }
                return;
            } else {
                throw new SocketException("Reply from SOCKS server has bad version");
            }
        }
        throw new SocketException("SOCKS V4 requires IPv4 only addresses");
    }

    protected void connect(SocketAddress endpoint, int timeout) throws IOException {
        long deadlineMillis;
        if (timeout == 0) {
            deadlineMillis = 0;
        } else {
            long finish = System.currentTimeMillis() + ((long) timeout);
            deadlineMillis = finish < 0 ? Long.MAX_VALUE : finish;
        }
        SecurityManager security = System.getSecurityManager();
        if (endpoint == null || !(endpoint instanceof InetSocketAddress)) {
            throw new IllegalArgumentException("Unsupported address type");
        }
        InetSocketAddress epoint = (InetSocketAddress) endpoint;
        if (security != null) {
            if (epoint.isUnresolved()) {
                security.checkConnect(epoint.getHostName(), epoint.getPort());
            } else {
                security.checkConnect(epoint.getAddress().getHostAddress(), epoint.getPort());
            }
        }
        if (this.server == null) {
            super.connect((SocketAddress) epoint, remainingMillis(deadlineMillis));
            return;
        }
        try {
            privilegedConnect(this.server, this.serverPort, remainingMillis(deadlineMillis));
            BufferedOutputStream out = new BufferedOutputStream(this.cmdOut, Modifier.INTERFACE);
            InputStream in = this.cmdIn;
            if (!this.useV4) {
                out.write(5);
                out.write(2);
                out.write(0);
                out.write(2);
                out.flush();
                byte[] data = new byte[2];
                if (readSocksReply(in, data, deadlineMillis) == 2 && data[0] == 5) {
                    if (data[1] == -1) {
                        throw new SocketException("SOCKS : No acceptable methods");
                    } else if (authenticate(data[1], in, out, deadlineMillis)) {
                        out.write(5);
                        out.write(1);
                        out.write(0);
                        if (epoint.isUnresolved()) {
                            out.write(3);
                            out.write(epoint.getHostName().length());
                            try {
                                out.write(epoint.getHostName().getBytes("ISO-8859-1"));
                            } catch (UnsupportedEncodingException e) {
                                if (!-assertionsDisabled) {
                                    throw new AssertionError();
                                }
                            }
                            out.write((epoint.getPort() >> 8) & 255);
                            out.write((epoint.getPort() >> 0) & 255);
                        } else if (epoint.getAddress() instanceof Inet6Address) {
                            out.write(4);
                            out.write(epoint.getAddress().getAddress());
                            out.write((epoint.getPort() >> 8) & 255);
                            out.write((epoint.getPort() >> 0) & 255);
                        } else {
                            out.write(1);
                            out.write(epoint.getAddress().getAddress());
                            out.write((epoint.getPort() >> 8) & 255);
                            out.write((epoint.getPort() >> 0) & 255);
                        }
                        out.flush();
                        data = new byte[4];
                        if (readSocksReply(in, data, deadlineMillis) != 4) {
                            throw new SocketException("Reply from SOCKS server has bad length");
                        }
                        SocketException ex = null;
                        SocketException socketException;
                        switch (data[1]) {
                            case GeneralNameInterface.NAME_MATCH /*0*/:
                                int len;
                                switch (data[3]) {
                                    case BaseCalendar.SUNDAY /*1*/:
                                        if (readSocksReply(in, new byte[4], deadlineMillis) != 4) {
                                            throw new SocketException("Reply from SOCKS server badly formatted");
                                        } else if (readSocksReply(in, new byte[2], deadlineMillis) != 2) {
                                            throw new SocketException("Reply from SOCKS server badly formatted");
                                        }
                                        break;
                                    case BaseCalendar.TUESDAY /*3*/:
                                        len = data[1];
                                        if (readSocksReply(in, new byte[len], deadlineMillis) != len) {
                                            throw new SocketException("Reply from SOCKS server badly formatted");
                                        } else if (readSocksReply(in, new byte[2], deadlineMillis) != 2) {
                                            throw new SocketException("Reply from SOCKS server badly formatted");
                                        }
                                        break;
                                    case BaseCalendar.WEDNESDAY /*4*/:
                                        len = data[1];
                                        if (readSocksReply(in, new byte[len], deadlineMillis) != len) {
                                            throw new SocketException("Reply from SOCKS server badly formatted");
                                        } else if (readSocksReply(in, new byte[2], deadlineMillis) != 2) {
                                            throw new SocketException("Reply from SOCKS server badly formatted");
                                        }
                                        break;
                                    default:
                                        socketException = new SocketException("Reply from SOCKS server contains wrong code");
                                        break;
                                }
                            case BaseCalendar.SUNDAY /*1*/:
                                socketException = new SocketException("SOCKS server general failure");
                                break;
                            case BaseCalendar.MONDAY /*2*/:
                                socketException = new SocketException("SOCKS: Connection not allowed by ruleset");
                                break;
                            case BaseCalendar.TUESDAY /*3*/:
                                socketException = new SocketException("SOCKS: Network unreachable");
                                break;
                            case BaseCalendar.WEDNESDAY /*4*/:
                                socketException = new SocketException("SOCKS: Host unreachable");
                                break;
                            case BaseCalendar.THURSDAY /*5*/:
                                socketException = new SocketException("SOCKS: Connection refused");
                                break;
                            case BaseCalendar.JUNE /*6*/:
                                socketException = new SocketException("SOCKS: TTL expired");
                                break;
                            case BaseCalendar.SATURDAY /*7*/:
                                socketException = new SocketException("SOCKS: Command not supported");
                                break;
                            case BaseCalendar.AUGUST /*8*/:
                                socketException = new SocketException("SOCKS: address type not supported");
                                break;
                        }
                        if (ex != null) {
                            in.close();
                            out.close();
                            throw ex;
                        }
                        this.external_address = epoint;
                    } else {
                        throw new SocketException("SOCKS : authentication failed");
                    }
                } else if (epoint.isUnresolved()) {
                    throw new UnknownHostException(epoint.toString());
                } else {
                    connectV4(in, out, epoint, deadlineMillis);
                }
            } else if (epoint.isUnresolved()) {
                throw new UnknownHostException(epoint.toString());
            } else {
                connectV4(in, out, epoint, deadlineMillis);
            }
        } catch (IOException e2) {
            throw new SocketException(e2.getMessage());
        }
    }

    private void bindV4(InputStream in, OutputStream out, InetAddress baddr, int lport) throws IOException {
        if (baddr instanceof Inet4Address) {
            super.bind(baddr, lport);
            byte[] addr1 = baddr.getAddress();
            InetAddress naddr = baddr;
            if (baddr.isAnyLocalAddress()) {
                addr1 = ((InetAddress) AccessController.doPrivileged(new PrivilegedAction<InetAddress>() {
                    public InetAddress run() {
                        return SocksSocketImpl.this.cmdsock.getLocalAddress();
                    }
                })).getAddress();
            }
            out.write(4);
            out.write(2);
            out.write((super.getLocalPort() >> 8) & 255);
            out.write((super.getLocalPort() >> 0) & 255);
            out.write(addr1);
            try {
                out.write(getUserName().getBytes("ISO-8859-1"));
            } catch (UnsupportedEncodingException e) {
                if (!-assertionsDisabled) {
                    throw new AssertionError();
                }
            }
            out.write(0);
            out.flush();
            byte[] data = new byte[8];
            int n = readSocksReply(in, data);
            if (n != 8) {
                throw new SocketException("Reply from SOCKS server has bad length: " + n);
            } else if (data[0] == null || data[0] == (byte) 4) {
                SocketException socketException = null;
                switch (data[1]) {
                    case (byte) 90:
                        this.external_address = new InetSocketAddress(baddr, lport);
                        break;
                    case Types.DATE /*91*/:
                        socketException = new SocketException("SOCKS request rejected");
                        break;
                    case Types.TIME /*92*/:
                        socketException = new SocketException("SOCKS server couldn't reach destination");
                        break;
                    case Types.TIMESTAMP /*93*/:
                        socketException = new SocketException("SOCKS authentication failed");
                        break;
                    default:
                        socketException = new SocketException("Reply from SOCKS server contains bad status");
                        break;
                }
                if (socketException != null) {
                    in.close();
                    out.close();
                    throw socketException;
                }
                return;
            } else {
                throw new SocketException("Reply from SOCKS server has bad version");
            }
        }
        throw new SocketException("SOCKS V4 requires IPv4 only addresses");
    }

    protected synchronized void socksBind(java.net.InetSocketAddress r28) throws java.io.IOException {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Exception block dominator not found, method:java.net.SocksSocketImpl.socksBind(java.net.InetSocketAddress):void. bs: [B:19:0x0041, B:77:0x01e4, B:109:0x02f4]
	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.searchTryCatchDominators(ProcessTryCatchRegions.java:86)
	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.process(ProcessTryCatchRegions.java:45)
	at jadx.core.dex.visitors.regions.RegionMakerVisitor.postProcessRegions(RegionMakerVisitor.java:57)
	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:52)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r27 = this;
        monitor-enter(r27);
        r0 = r27;	 Catch:{ all -> 0x00d5 }
        r0 = r0.socket;	 Catch:{ all -> 0x00d5 }
        r24 = r0;	 Catch:{ all -> 0x00d5 }
        if (r24 == 0) goto L_0x000b;
    L_0x0009:
        monitor-exit(r27);
        return;
    L_0x000b:
        r0 = r27;	 Catch:{ all -> 0x00d5 }
        r0 = r0.server;	 Catch:{ all -> 0x00d5 }
        r24 = r0;	 Catch:{ all -> 0x00d5 }
        if (r24 != 0) goto L_0x01e4;	 Catch:{ all -> 0x00d5 }
    L_0x0013:
        r24 = new java.net.SocksSocketImpl$4;	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r1 = r27;	 Catch:{ all -> 0x00d5 }
        r0.<init>();	 Catch:{ all -> 0x00d5 }
        r21 = java.security.AccessController.doPrivileged(r24);	 Catch:{ all -> 0x00d5 }
        r21 = (java.net.ProxySelector) r21;	 Catch:{ all -> 0x00d5 }
        if (r21 != 0) goto L_0x0026;
    L_0x0024:
        monitor-exit(r27);
        return;
    L_0x0026:
        r10 = r28.getHostString();	 Catch:{ all -> 0x00d5 }
        r24 = r28.getAddress();	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r0 = r0 instanceof java.net.Inet6Address;	 Catch:{ all -> 0x00d5 }
        r24 = r0;	 Catch:{ all -> 0x00d5 }
        if (r24 == 0) goto L_0x0041;	 Catch:{ all -> 0x00d5 }
    L_0x0036:
        r24 = "[";	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r24 = r10.startsWith(r0);	 Catch:{ all -> 0x00d5 }
        if (r24 == 0) goto L_0x009e;
    L_0x0041:
        r23 = new java.net.URI;	 Catch:{ URISyntaxException -> 0x00c8 }
        r24 = new java.lang.StringBuilder;	 Catch:{ URISyntaxException -> 0x00c8 }
        r24.<init>();	 Catch:{ URISyntaxException -> 0x00c8 }
        r25 = "serversocket://";	 Catch:{ URISyntaxException -> 0x00c8 }
        r24 = r24.append(r25);	 Catch:{ URISyntaxException -> 0x00c8 }
        r25 = sun.net.www.ParseUtil.encodePath(r10);	 Catch:{ URISyntaxException -> 0x00c8 }
        r24 = r24.append(r25);	 Catch:{ URISyntaxException -> 0x00c8 }
        r25 = ":";	 Catch:{ URISyntaxException -> 0x00c8 }
        r24 = r24.append(r25);	 Catch:{ URISyntaxException -> 0x00c8 }
        r25 = r28.getPort();	 Catch:{ URISyntaxException -> 0x00c8 }
        r24 = r24.append(r25);	 Catch:{ URISyntaxException -> 0x00c8 }
        r24 = r24.toString();	 Catch:{ URISyntaxException -> 0x00c8 }
        r23.<init>(r24);	 Catch:{ URISyntaxException -> 0x00c8 }
    L_0x006d:
        r19 = 0;
        r20 = 0;
        r13 = 0;
        r0 = r21;	 Catch:{ all -> 0x00d5 }
        r1 = r23;	 Catch:{ all -> 0x00d5 }
        r24 = r0.select(r1);	 Catch:{ all -> 0x00d5 }
        r13 = r24.iterator();	 Catch:{ all -> 0x00d5 }
        if (r13 == 0) goto L_0x00db;	 Catch:{ all -> 0x00d5 }
    L_0x0080:
        r24 = r13.hasNext();	 Catch:{ all -> 0x00d5 }
        if (r24 == 0) goto L_0x00db;	 Catch:{ all -> 0x00d5 }
    L_0x0086:
        r24 = r13.hasNext();	 Catch:{ all -> 0x00d5 }
        if (r24 == 0) goto L_0x01b6;	 Catch:{ all -> 0x00d5 }
    L_0x008c:
        r19 = r13.next();	 Catch:{ all -> 0x00d5 }
        r19 = (java.net.Proxy) r19;	 Catch:{ all -> 0x00d5 }
        if (r19 == 0) goto L_0x009c;	 Catch:{ all -> 0x00d5 }
    L_0x0094:
        r24 = java.net.Proxy.NO_PROXY;	 Catch:{ all -> 0x00d5 }
        r0 = r19;
        r1 = r24;
        if (r0 != r1) goto L_0x00dd;
    L_0x009c:
        monitor-exit(r27);
        return;
    L_0x009e:
        r24 = ":";	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r24 = r10.indexOf(r0);	 Catch:{ all -> 0x00d5 }
        if (r24 < 0) goto L_0x0041;	 Catch:{ all -> 0x00d5 }
    L_0x00a9:
        r24 = new java.lang.StringBuilder;	 Catch:{ all -> 0x00d5 }
        r24.<init>();	 Catch:{ all -> 0x00d5 }
        r25 = "[";	 Catch:{ all -> 0x00d5 }
        r24 = r24.append(r25);	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r24 = r0.append(r10);	 Catch:{ all -> 0x00d5 }
        r25 = "]";	 Catch:{ all -> 0x00d5 }
        r24 = r24.append(r25);	 Catch:{ all -> 0x00d5 }
        r10 = r24.toString();	 Catch:{ all -> 0x00d5 }
        goto L_0x0041;	 Catch:{ all -> 0x00d5 }
    L_0x00c8:
        r8 = move-exception;	 Catch:{ all -> 0x00d5 }
        r24 = -assertionsDisabled;	 Catch:{ all -> 0x00d5 }
        if (r24 != 0) goto L_0x00d8;	 Catch:{ all -> 0x00d5 }
    L_0x00cd:
        r24 = new java.lang.AssertionError;	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r0.<init>(r8);	 Catch:{ all -> 0x00d5 }
        throw r24;	 Catch:{ all -> 0x00d5 }
    L_0x00d5:
        r24 = move-exception;
        monitor-exit(r27);
        throw r24;
    L_0x00d8:
        r23 = 0;
        goto L_0x006d;
    L_0x00db:
        monitor-exit(r27);
        return;
    L_0x00dd:
        r24 = r19.type();	 Catch:{ all -> 0x00d5 }
        r25 = java.net.Proxy.Type.SOCKS;	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r1 = r25;	 Catch:{ all -> 0x00d5 }
        if (r0 == r1) goto L_0x0107;	 Catch:{ all -> 0x00d5 }
    L_0x00e9:
        r24 = new java.net.SocketException;	 Catch:{ all -> 0x00d5 }
        r25 = new java.lang.StringBuilder;	 Catch:{ all -> 0x00d5 }
        r25.<init>();	 Catch:{ all -> 0x00d5 }
        r26 = "Unknown proxy type : ";	 Catch:{ all -> 0x00d5 }
        r25 = r25.append(r26);	 Catch:{ all -> 0x00d5 }
        r26 = r19.type();	 Catch:{ all -> 0x00d5 }
        r25 = r25.append(r26);	 Catch:{ all -> 0x00d5 }
        r25 = r25.toString();	 Catch:{ all -> 0x00d5 }
        r24.<init>(r25);	 Catch:{ all -> 0x00d5 }
        throw r24;	 Catch:{ all -> 0x00d5 }
    L_0x0107:
        r24 = r19.address();	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r0 = r0 instanceof java.net.InetSocketAddress;	 Catch:{ all -> 0x00d5 }
        r24 = r0;	 Catch:{ all -> 0x00d5 }
        if (r24 != 0) goto L_0x0131;	 Catch:{ all -> 0x00d5 }
    L_0x0113:
        r24 = new java.net.SocketException;	 Catch:{ all -> 0x00d5 }
        r25 = new java.lang.StringBuilder;	 Catch:{ all -> 0x00d5 }
        r25.<init>();	 Catch:{ all -> 0x00d5 }
        r26 = "Unknow address type for proxy: ";	 Catch:{ all -> 0x00d5 }
        r25 = r25.append(r26);	 Catch:{ all -> 0x00d5 }
        r0 = r25;	 Catch:{ all -> 0x00d5 }
        r1 = r19;	 Catch:{ all -> 0x00d5 }
        r25 = r0.append(r1);	 Catch:{ all -> 0x00d5 }
        r25 = r25.toString();	 Catch:{ all -> 0x00d5 }
        r24.<init>(r25);	 Catch:{ all -> 0x00d5 }
        throw r24;	 Catch:{ all -> 0x00d5 }
    L_0x0131:
        r24 = r19.address();	 Catch:{ all -> 0x00d5 }
        r24 = (java.net.InetSocketAddress) r24;	 Catch:{ all -> 0x00d5 }
        r24 = r24.getHostString();	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r1 = r27;	 Catch:{ all -> 0x00d5 }
        r1.server = r0;	 Catch:{ all -> 0x00d5 }
        r24 = r19.address();	 Catch:{ all -> 0x00d5 }
        r24 = (java.net.InetSocketAddress) r24;	 Catch:{ all -> 0x00d5 }
        r24 = r24.getPort();	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r1 = r27;	 Catch:{ all -> 0x00d5 }
        r1.serverPort = r0;	 Catch:{ all -> 0x00d5 }
        r0 = r19;	 Catch:{ all -> 0x00d5 }
        r0 = r0 instanceof sun.net.SocksProxy;	 Catch:{ all -> 0x00d5 }
        r24 = r0;	 Catch:{ all -> 0x00d5 }
        if (r24 == 0) goto L_0x0173;	 Catch:{ all -> 0x00d5 }
    L_0x0159:
        r0 = r19;	 Catch:{ all -> 0x00d5 }
        r0 = (sun.net.SocksProxy) r0;	 Catch:{ all -> 0x00d5 }
        r24 = r0;	 Catch:{ all -> 0x00d5 }
        r24 = r24.protocolVersion();	 Catch:{ all -> 0x00d5 }
        r25 = 4;	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r1 = r25;	 Catch:{ all -> 0x00d5 }
        if (r0 != r1) goto L_0x0173;	 Catch:{ all -> 0x00d5 }
    L_0x016b:
        r24 = 1;	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r1 = r27;	 Catch:{ all -> 0x00d5 }
        r1.useV4 = r0;	 Catch:{ all -> 0x00d5 }
    L_0x0173:
        r24 = new java.net.SocksSocketImpl$5;	 Catch:{ Exception -> 0x0181 }
        r0 = r24;	 Catch:{ Exception -> 0x0181 }
        r1 = r27;	 Catch:{ Exception -> 0x0181 }
        r0.<init>();	 Catch:{ Exception -> 0x0181 }
        java.security.AccessController.doPrivileged(r24);	 Catch:{ Exception -> 0x0181 }
        goto L_0x0086;
    L_0x0181:
        r7 = move-exception;
        r24 = r19.address();	 Catch:{ all -> 0x00d5 }
        r25 = new java.net.SocketException;	 Catch:{ all -> 0x00d5 }
        r26 = r7.getMessage();	 Catch:{ all -> 0x00d5 }
        r25.<init>(r26);	 Catch:{ all -> 0x00d5 }
        r0 = r21;	 Catch:{ all -> 0x00d5 }
        r1 = r23;	 Catch:{ all -> 0x00d5 }
        r2 = r24;	 Catch:{ all -> 0x00d5 }
        r3 = r25;	 Catch:{ all -> 0x00d5 }
        r0.connectFailed(r1, r2, r3);	 Catch:{ all -> 0x00d5 }
        r24 = 0;	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r1 = r27;	 Catch:{ all -> 0x00d5 }
        r1.server = r0;	 Catch:{ all -> 0x00d5 }
        r24 = -1;	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r1 = r27;	 Catch:{ all -> 0x00d5 }
        r1.serverPort = r0;	 Catch:{ all -> 0x00d5 }
        r24 = 0;	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r1 = r27;	 Catch:{ all -> 0x00d5 }
        r1.cmdsock = r0;	 Catch:{ all -> 0x00d5 }
        r20 = r7;	 Catch:{ all -> 0x00d5 }
        goto L_0x0086;	 Catch:{ all -> 0x00d5 }
    L_0x01b6:
        r0 = r27;	 Catch:{ all -> 0x00d5 }
        r0 = r0.server;	 Catch:{ all -> 0x00d5 }
        r24 = r0;	 Catch:{ all -> 0x00d5 }
        if (r24 == 0) goto L_0x01c6;	 Catch:{ all -> 0x00d5 }
    L_0x01be:
        r0 = r27;	 Catch:{ all -> 0x00d5 }
        r0 = r0.cmdsock;	 Catch:{ all -> 0x00d5 }
        r24 = r0;	 Catch:{ all -> 0x00d5 }
        if (r24 != 0) goto L_0x01f0;	 Catch:{ all -> 0x00d5 }
    L_0x01c6:
        r24 = new java.net.SocketException;	 Catch:{ all -> 0x00d5 }
        r25 = new java.lang.StringBuilder;	 Catch:{ all -> 0x00d5 }
        r25.<init>();	 Catch:{ all -> 0x00d5 }
        r26 = "Can't connect to SOCKS proxy:";	 Catch:{ all -> 0x00d5 }
        r25 = r25.append(r26);	 Catch:{ all -> 0x00d5 }
        r26 = r20.getMessage();	 Catch:{ all -> 0x00d5 }
        r25 = r25.append(r26);	 Catch:{ all -> 0x00d5 }
        r25 = r25.toString();	 Catch:{ all -> 0x00d5 }
        r24.<init>(r25);	 Catch:{ all -> 0x00d5 }
        throw r24;	 Catch:{ all -> 0x00d5 }
    L_0x01e4:
        r24 = new java.net.SocksSocketImpl$6;	 Catch:{ Exception -> 0x0224 }
        r0 = r24;	 Catch:{ Exception -> 0x0224 }
        r1 = r27;	 Catch:{ Exception -> 0x0224 }
        r0.<init>();	 Catch:{ Exception -> 0x0224 }
        java.security.AccessController.doPrivileged(r24);	 Catch:{ Exception -> 0x0224 }
    L_0x01f0:
        r18 = new java.io.BufferedOutputStream;	 Catch:{ all -> 0x00d5 }
        r0 = r27;	 Catch:{ all -> 0x00d5 }
        r0 = r0.cmdOut;	 Catch:{ all -> 0x00d5 }
        r24 = r0;	 Catch:{ all -> 0x00d5 }
        r25 = 512; // 0x200 float:7.175E-43 double:2.53E-321;	 Catch:{ all -> 0x00d5 }
        r0 = r18;	 Catch:{ all -> 0x00d5 }
        r1 = r24;	 Catch:{ all -> 0x00d5 }
        r2 = r25;	 Catch:{ all -> 0x00d5 }
        r0.<init>(r1, r2);	 Catch:{ all -> 0x00d5 }
        r0 = r27;	 Catch:{ all -> 0x00d5 }
        r14 = r0.cmdIn;	 Catch:{ all -> 0x00d5 }
        r0 = r27;	 Catch:{ all -> 0x00d5 }
        r0 = r0.useV4;	 Catch:{ all -> 0x00d5 }
        r24 = r0;	 Catch:{ all -> 0x00d5 }
        if (r24 == 0) goto L_0x022f;	 Catch:{ all -> 0x00d5 }
    L_0x020f:
        r24 = r28.getAddress();	 Catch:{ all -> 0x00d5 }
        r25 = r28.getPort();	 Catch:{ all -> 0x00d5 }
        r0 = r27;	 Catch:{ all -> 0x00d5 }
        r1 = r18;	 Catch:{ all -> 0x00d5 }
        r2 = r24;	 Catch:{ all -> 0x00d5 }
        r3 = r25;	 Catch:{ all -> 0x00d5 }
        r0.bindV4(r14, r1, r2, r3);	 Catch:{ all -> 0x00d5 }
        monitor-exit(r27);
        return;
    L_0x0224:
        r7 = move-exception;
        r24 = new java.net.SocketException;	 Catch:{ all -> 0x00d5 }
        r25 = r7.getMessage();	 Catch:{ all -> 0x00d5 }
        r24.<init>(r25);	 Catch:{ all -> 0x00d5 }
        throw r24;	 Catch:{ all -> 0x00d5 }
    L_0x022f:
        r24 = 5;	 Catch:{ all -> 0x00d5 }
        r0 = r18;	 Catch:{ all -> 0x00d5 }
        r1 = r24;	 Catch:{ all -> 0x00d5 }
        r0.write(r1);	 Catch:{ all -> 0x00d5 }
        r24 = 2;	 Catch:{ all -> 0x00d5 }
        r0 = r18;	 Catch:{ all -> 0x00d5 }
        r1 = r24;	 Catch:{ all -> 0x00d5 }
        r0.write(r1);	 Catch:{ all -> 0x00d5 }
        r24 = 0;	 Catch:{ all -> 0x00d5 }
        r0 = r18;	 Catch:{ all -> 0x00d5 }
        r1 = r24;	 Catch:{ all -> 0x00d5 }
        r0.write(r1);	 Catch:{ all -> 0x00d5 }
        r24 = 2;	 Catch:{ all -> 0x00d5 }
        r0 = r18;	 Catch:{ all -> 0x00d5 }
        r1 = r24;	 Catch:{ all -> 0x00d5 }
        r0.write(r1);	 Catch:{ all -> 0x00d5 }
        r18.flush();	 Catch:{ all -> 0x00d5 }
        r24 = 2;	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r6 = new byte[r0];	 Catch:{ all -> 0x00d5 }
        r0 = r27;	 Catch:{ all -> 0x00d5 }
        r12 = r0.readSocksReply(r14, r6);	 Catch:{ all -> 0x00d5 }
        r24 = 2;	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        if (r12 != r0) goto L_0x0274;	 Catch:{ all -> 0x00d5 }
    L_0x0268:
        r24 = 0;	 Catch:{ all -> 0x00d5 }
        r24 = r6[r24];	 Catch:{ all -> 0x00d5 }
        r25 = 5;	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r1 = r25;	 Catch:{ all -> 0x00d5 }
        if (r0 == r1) goto L_0x0289;	 Catch:{ all -> 0x00d5 }
    L_0x0274:
        r24 = r28.getAddress();	 Catch:{ all -> 0x00d5 }
        r25 = r28.getPort();	 Catch:{ all -> 0x00d5 }
        r0 = r27;	 Catch:{ all -> 0x00d5 }
        r1 = r18;	 Catch:{ all -> 0x00d5 }
        r2 = r24;	 Catch:{ all -> 0x00d5 }
        r3 = r25;	 Catch:{ all -> 0x00d5 }
        r0.bindV4(r14, r1, r2, r3);	 Catch:{ all -> 0x00d5 }
        monitor-exit(r27);
        return;
    L_0x0289:
        r24 = 1;
        r24 = r6[r24];	 Catch:{ all -> 0x00d5 }
        r25 = -1;	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r1 = r25;	 Catch:{ all -> 0x00d5 }
        if (r0 != r1) goto L_0x029e;	 Catch:{ all -> 0x00d5 }
    L_0x0295:
        r24 = new java.net.SocketException;	 Catch:{ all -> 0x00d5 }
        r25 = "SOCKS : No acceptable methods";	 Catch:{ all -> 0x00d5 }
        r24.<init>(r25);	 Catch:{ all -> 0x00d5 }
        throw r24;	 Catch:{ all -> 0x00d5 }
    L_0x029e:
        r24 = 1;	 Catch:{ all -> 0x00d5 }
        r24 = r6[r24];	 Catch:{ all -> 0x00d5 }
        r0 = r27;	 Catch:{ all -> 0x00d5 }
        r1 = r24;	 Catch:{ all -> 0x00d5 }
        r2 = r18;	 Catch:{ all -> 0x00d5 }
        r24 = r0.authenticate(r1, r14, r2);	 Catch:{ all -> 0x00d5 }
        if (r24 != 0) goto L_0x02b7;	 Catch:{ all -> 0x00d5 }
    L_0x02ae:
        r24 = new java.net.SocketException;	 Catch:{ all -> 0x00d5 }
        r25 = "SOCKS : authentication failed";	 Catch:{ all -> 0x00d5 }
        r24.<init>(r25);	 Catch:{ all -> 0x00d5 }
        throw r24;	 Catch:{ all -> 0x00d5 }
    L_0x02b7:
        r24 = 5;	 Catch:{ all -> 0x00d5 }
        r0 = r18;	 Catch:{ all -> 0x00d5 }
        r1 = r24;	 Catch:{ all -> 0x00d5 }
        r0.write(r1);	 Catch:{ all -> 0x00d5 }
        r24 = 2;	 Catch:{ all -> 0x00d5 }
        r0 = r18;	 Catch:{ all -> 0x00d5 }
        r1 = r24;	 Catch:{ all -> 0x00d5 }
        r0.write(r1);	 Catch:{ all -> 0x00d5 }
        r24 = 0;	 Catch:{ all -> 0x00d5 }
        r0 = r18;	 Catch:{ all -> 0x00d5 }
        r1 = r24;	 Catch:{ all -> 0x00d5 }
        r0.write(r1);	 Catch:{ all -> 0x00d5 }
        r16 = r28.getPort();	 Catch:{ all -> 0x00d5 }
        r24 = r28.isUnresolved();	 Catch:{ all -> 0x00d5 }
        if (r24 == 0) goto L_0x035d;	 Catch:{ all -> 0x00d5 }
    L_0x02dc:
        r24 = 3;	 Catch:{ all -> 0x00d5 }
        r0 = r18;	 Catch:{ all -> 0x00d5 }
        r1 = r24;	 Catch:{ all -> 0x00d5 }
        r0.write(r1);	 Catch:{ all -> 0x00d5 }
        r24 = r28.getHostName();	 Catch:{ all -> 0x00d5 }
        r24 = r24.length();	 Catch:{ all -> 0x00d5 }
        r0 = r18;	 Catch:{ all -> 0x00d5 }
        r1 = r24;	 Catch:{ all -> 0x00d5 }
        r0.write(r1);	 Catch:{ all -> 0x00d5 }
        r24 = r28.getHostName();	 Catch:{ UnsupportedEncodingException -> 0x0352 }
        r25 = "ISO-8859-1";	 Catch:{ UnsupportedEncodingException -> 0x0352 }
        r24 = r24.getBytes(r25);	 Catch:{ UnsupportedEncodingException -> 0x0352 }
        r0 = r18;	 Catch:{ UnsupportedEncodingException -> 0x0352 }
        r1 = r24;	 Catch:{ UnsupportedEncodingException -> 0x0352 }
        r0.write(r1);	 Catch:{ UnsupportedEncodingException -> 0x0352 }
    L_0x0306:
        r24 = r16 >> 8;
        r0 = r24;
        r0 = r0 & 255;
        r24 = r0;
        r0 = r18;	 Catch:{ all -> 0x00d5 }
        r1 = r24;	 Catch:{ all -> 0x00d5 }
        r0.write(r1);	 Catch:{ all -> 0x00d5 }
        r24 = r16 >> 0;	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r0 = r0 & 255;	 Catch:{ all -> 0x00d5 }
        r24 = r0;	 Catch:{ all -> 0x00d5 }
        r0 = r18;	 Catch:{ all -> 0x00d5 }
        r1 = r24;	 Catch:{ all -> 0x00d5 }
        r0.write(r1);	 Catch:{ all -> 0x00d5 }
    L_0x0324:
        r24 = 4;	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r6 = new byte[r0];	 Catch:{ all -> 0x00d5 }
        r0 = r27;	 Catch:{ all -> 0x00d5 }
        r12 = r0.readSocksReply(r14, r6);	 Catch:{ all -> 0x00d5 }
        r9 = 0;	 Catch:{ all -> 0x00d5 }
        r24 = 1;	 Catch:{ all -> 0x00d5 }
        r24 = r6[r24];	 Catch:{ all -> 0x00d5 }
        switch(r24) {
            case 0: goto L_0x040d;
            case 1: goto L_0x0550;
            case 2: goto L_0x055c;
            case 3: goto L_0x0568;
            case 4: goto L_0x0574;
            case 5: goto L_0x0580;
            case 6: goto L_0x058c;
            case 7: goto L_0x0598;
            case 8: goto L_0x05a4;
            default: goto L_0x0338;
        };	 Catch:{ all -> 0x00d5 }
    L_0x0338:
        if (r9 == 0) goto L_0x05b0;	 Catch:{ all -> 0x00d5 }
    L_0x033a:
        r14.close();	 Catch:{ all -> 0x00d5 }
        r18.close();	 Catch:{ all -> 0x00d5 }
        r0 = r27;	 Catch:{ all -> 0x00d5 }
        r0 = r0.cmdsock;	 Catch:{ all -> 0x00d5 }
        r24 = r0;	 Catch:{ all -> 0x00d5 }
        r24.close();	 Catch:{ all -> 0x00d5 }
        r24 = 0;	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r1 = r27;	 Catch:{ all -> 0x00d5 }
        r1.cmdsock = r0;	 Catch:{ all -> 0x00d5 }
        throw r9;	 Catch:{ all -> 0x00d5 }
    L_0x0352:
        r22 = move-exception;	 Catch:{ all -> 0x00d5 }
        r24 = -assertionsDisabled;	 Catch:{ all -> 0x00d5 }
        if (r24 != 0) goto L_0x0306;	 Catch:{ all -> 0x00d5 }
    L_0x0357:
        r24 = new java.lang.AssertionError;	 Catch:{ all -> 0x00d5 }
        r24.<init>();	 Catch:{ all -> 0x00d5 }
        throw r24;	 Catch:{ all -> 0x00d5 }
    L_0x035d:
        r24 = r28.getAddress();	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r0 = r0 instanceof java.net.Inet4Address;	 Catch:{ all -> 0x00d5 }
        r24 = r0;	 Catch:{ all -> 0x00d5 }
        if (r24 == 0) goto L_0x03a1;	 Catch:{ all -> 0x00d5 }
    L_0x0369:
        r24 = r28.getAddress();	 Catch:{ all -> 0x00d5 }
        r5 = r24.getAddress();	 Catch:{ all -> 0x00d5 }
        r24 = 1;	 Catch:{ all -> 0x00d5 }
        r0 = r18;	 Catch:{ all -> 0x00d5 }
        r1 = r24;	 Catch:{ all -> 0x00d5 }
        r0.write(r1);	 Catch:{ all -> 0x00d5 }
        r0 = r18;	 Catch:{ all -> 0x00d5 }
        r0.write(r5);	 Catch:{ all -> 0x00d5 }
        r24 = r16 >> 8;	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r0 = r0 & 255;	 Catch:{ all -> 0x00d5 }
        r24 = r0;	 Catch:{ all -> 0x00d5 }
        r0 = r18;	 Catch:{ all -> 0x00d5 }
        r1 = r24;	 Catch:{ all -> 0x00d5 }
        r0.write(r1);	 Catch:{ all -> 0x00d5 }
        r24 = r16 >> 0;	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r0 = r0 & 255;	 Catch:{ all -> 0x00d5 }
        r24 = r0;	 Catch:{ all -> 0x00d5 }
        r0 = r18;	 Catch:{ all -> 0x00d5 }
        r1 = r24;	 Catch:{ all -> 0x00d5 }
        r0.write(r1);	 Catch:{ all -> 0x00d5 }
        r18.flush();	 Catch:{ all -> 0x00d5 }
        goto L_0x0324;	 Catch:{ all -> 0x00d5 }
    L_0x03a1:
        r24 = r28.getAddress();	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r0 = r0 instanceof java.net.Inet6Address;	 Catch:{ all -> 0x00d5 }
        r24 = r0;	 Catch:{ all -> 0x00d5 }
        if (r24 == 0) goto L_0x03e6;	 Catch:{ all -> 0x00d5 }
    L_0x03ad:
        r24 = r28.getAddress();	 Catch:{ all -> 0x00d5 }
        r5 = r24.getAddress();	 Catch:{ all -> 0x00d5 }
        r24 = 4;	 Catch:{ all -> 0x00d5 }
        r0 = r18;	 Catch:{ all -> 0x00d5 }
        r1 = r24;	 Catch:{ all -> 0x00d5 }
        r0.write(r1);	 Catch:{ all -> 0x00d5 }
        r0 = r18;	 Catch:{ all -> 0x00d5 }
        r0.write(r5);	 Catch:{ all -> 0x00d5 }
        r24 = r16 >> 8;	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r0 = r0 & 255;	 Catch:{ all -> 0x00d5 }
        r24 = r0;	 Catch:{ all -> 0x00d5 }
        r0 = r18;	 Catch:{ all -> 0x00d5 }
        r1 = r24;	 Catch:{ all -> 0x00d5 }
        r0.write(r1);	 Catch:{ all -> 0x00d5 }
        r24 = r16 >> 0;	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r0 = r0 & 255;	 Catch:{ all -> 0x00d5 }
        r24 = r0;	 Catch:{ all -> 0x00d5 }
        r0 = r18;	 Catch:{ all -> 0x00d5 }
        r1 = r24;	 Catch:{ all -> 0x00d5 }
        r0.write(r1);	 Catch:{ all -> 0x00d5 }
        r18.flush();	 Catch:{ all -> 0x00d5 }
        goto L_0x0324;	 Catch:{ all -> 0x00d5 }
    L_0x03e6:
        r0 = r27;	 Catch:{ all -> 0x00d5 }
        r0 = r0.cmdsock;	 Catch:{ all -> 0x00d5 }
        r24 = r0;	 Catch:{ all -> 0x00d5 }
        r24.close();	 Catch:{ all -> 0x00d5 }
        r24 = new java.net.SocketException;	 Catch:{ all -> 0x00d5 }
        r25 = new java.lang.StringBuilder;	 Catch:{ all -> 0x00d5 }
        r25.<init>();	 Catch:{ all -> 0x00d5 }
        r26 = "unsupported address type : ";	 Catch:{ all -> 0x00d5 }
        r25 = r25.append(r26);	 Catch:{ all -> 0x00d5 }
        r0 = r25;	 Catch:{ all -> 0x00d5 }
        r1 = r28;	 Catch:{ all -> 0x00d5 }
        r25 = r0.append(r1);	 Catch:{ all -> 0x00d5 }
        r25 = r25.toString();	 Catch:{ all -> 0x00d5 }
        r24.<init>(r25);	 Catch:{ all -> 0x00d5 }
        throw r24;	 Catch:{ all -> 0x00d5 }
    L_0x040d:
        r24 = 3;	 Catch:{ all -> 0x00d5 }
        r24 = r6[r24];	 Catch:{ all -> 0x00d5 }
        switch(r24) {
            case 1: goto L_0x0416;
            case 2: goto L_0x0414;
            case 3: goto L_0x0483;
            case 4: goto L_0x04e7;
            default: goto L_0x0414;
        };	 Catch:{ all -> 0x00d5 }
    L_0x0414:
        goto L_0x0338;	 Catch:{ all -> 0x00d5 }
    L_0x0416:
        r24 = 4;	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r4 = new byte[r0];	 Catch:{ all -> 0x00d5 }
        r0 = r27;	 Catch:{ all -> 0x00d5 }
        r12 = r0.readSocksReply(r14, r4);	 Catch:{ all -> 0x00d5 }
        r24 = 4;	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        if (r12 == r0) goto L_0x0431;	 Catch:{ all -> 0x00d5 }
    L_0x0428:
        r24 = new java.net.SocketException;	 Catch:{ all -> 0x00d5 }
        r25 = "Reply from SOCKS server badly formatted";	 Catch:{ all -> 0x00d5 }
        r24.<init>(r25);	 Catch:{ all -> 0x00d5 }
        throw r24;	 Catch:{ all -> 0x00d5 }
    L_0x0431:
        r24 = 2;	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r6 = new byte[r0];	 Catch:{ all -> 0x00d5 }
        r0 = r27;	 Catch:{ all -> 0x00d5 }
        r12 = r0.readSocksReply(r14, r6);	 Catch:{ all -> 0x00d5 }
        r24 = 2;	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        if (r12 == r0) goto L_0x044c;	 Catch:{ all -> 0x00d5 }
    L_0x0443:
        r24 = new java.net.SocketException;	 Catch:{ all -> 0x00d5 }
        r25 = "Reply from SOCKS server badly formatted";	 Catch:{ all -> 0x00d5 }
        r24.<init>(r25);	 Catch:{ all -> 0x00d5 }
        throw r24;	 Catch:{ all -> 0x00d5 }
    L_0x044c:
        r24 = 0;	 Catch:{ all -> 0x00d5 }
        r24 = r6[r24];	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r0 = r0 & 255;	 Catch:{ all -> 0x00d5 }
        r24 = r0;	 Catch:{ all -> 0x00d5 }
        r17 = r24 << 8;	 Catch:{ all -> 0x00d5 }
        r24 = 1;	 Catch:{ all -> 0x00d5 }
        r24 = r6[r24];	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r0 = r0 & 255;	 Catch:{ all -> 0x00d5 }
        r24 = r0;	 Catch:{ all -> 0x00d5 }
        r17 = r17 + r24;	 Catch:{ all -> 0x00d5 }
        r24 = new java.net.InetSocketAddress;	 Catch:{ all -> 0x00d5 }
        r25 = new java.net.Inet4Address;	 Catch:{ all -> 0x00d5 }
        r26 = "";	 Catch:{ all -> 0x00d5 }
        r0 = r25;	 Catch:{ all -> 0x00d5 }
        r1 = r26;	 Catch:{ all -> 0x00d5 }
        r0.<init>(r1, r4);	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r1 = r25;	 Catch:{ all -> 0x00d5 }
        r2 = r17;	 Catch:{ all -> 0x00d5 }
        r0.<init>(r1, r2);	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r1 = r27;	 Catch:{ all -> 0x00d5 }
        r1.external_address = r0;	 Catch:{ all -> 0x00d5 }
        goto L_0x0338;	 Catch:{ all -> 0x00d5 }
    L_0x0483:
        r24 = 1;	 Catch:{ all -> 0x00d5 }
        r15 = r6[r24];	 Catch:{ all -> 0x00d5 }
        r11 = new byte[r15];	 Catch:{ all -> 0x00d5 }
        r0 = r27;	 Catch:{ all -> 0x00d5 }
        r12 = r0.readSocksReply(r14, r11);	 Catch:{ all -> 0x00d5 }
        if (r12 == r15) goto L_0x049a;	 Catch:{ all -> 0x00d5 }
    L_0x0491:
        r24 = new java.net.SocketException;	 Catch:{ all -> 0x00d5 }
        r25 = "Reply from SOCKS server badly formatted";	 Catch:{ all -> 0x00d5 }
        r24.<init>(r25);	 Catch:{ all -> 0x00d5 }
        throw r24;	 Catch:{ all -> 0x00d5 }
    L_0x049a:
        r24 = 2;	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r6 = new byte[r0];	 Catch:{ all -> 0x00d5 }
        r0 = r27;	 Catch:{ all -> 0x00d5 }
        r12 = r0.readSocksReply(r14, r6);	 Catch:{ all -> 0x00d5 }
        r24 = 2;	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        if (r12 == r0) goto L_0x04b5;	 Catch:{ all -> 0x00d5 }
    L_0x04ac:
        r24 = new java.net.SocketException;	 Catch:{ all -> 0x00d5 }
        r25 = "Reply from SOCKS server badly formatted";	 Catch:{ all -> 0x00d5 }
        r24.<init>(r25);	 Catch:{ all -> 0x00d5 }
        throw r24;	 Catch:{ all -> 0x00d5 }
    L_0x04b5:
        r24 = 0;	 Catch:{ all -> 0x00d5 }
        r24 = r6[r24];	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r0 = r0 & 255;	 Catch:{ all -> 0x00d5 }
        r24 = r0;	 Catch:{ all -> 0x00d5 }
        r17 = r24 << 8;	 Catch:{ all -> 0x00d5 }
        r24 = 1;	 Catch:{ all -> 0x00d5 }
        r24 = r6[r24];	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r0 = r0 & 255;	 Catch:{ all -> 0x00d5 }
        r24 = r0;	 Catch:{ all -> 0x00d5 }
        r17 = r17 + r24;	 Catch:{ all -> 0x00d5 }
        r24 = new java.net.InetSocketAddress;	 Catch:{ all -> 0x00d5 }
        r25 = new java.lang.String;	 Catch:{ all -> 0x00d5 }
        r0 = r25;	 Catch:{ all -> 0x00d5 }
        r0.<init>(r11);	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r1 = r25;	 Catch:{ all -> 0x00d5 }
        r2 = r17;	 Catch:{ all -> 0x00d5 }
        r0.<init>(r1, r2);	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r1 = r27;	 Catch:{ all -> 0x00d5 }
        r1.external_address = r0;	 Catch:{ all -> 0x00d5 }
        goto L_0x0338;	 Catch:{ all -> 0x00d5 }
    L_0x04e7:
        r24 = 1;	 Catch:{ all -> 0x00d5 }
        r15 = r6[r24];	 Catch:{ all -> 0x00d5 }
        r4 = new byte[r15];	 Catch:{ all -> 0x00d5 }
        r0 = r27;	 Catch:{ all -> 0x00d5 }
        r12 = r0.readSocksReply(r14, r4);	 Catch:{ all -> 0x00d5 }
        if (r12 == r15) goto L_0x04fe;	 Catch:{ all -> 0x00d5 }
    L_0x04f5:
        r24 = new java.net.SocketException;	 Catch:{ all -> 0x00d5 }
        r25 = "Reply from SOCKS server badly formatted";	 Catch:{ all -> 0x00d5 }
        r24.<init>(r25);	 Catch:{ all -> 0x00d5 }
        throw r24;	 Catch:{ all -> 0x00d5 }
    L_0x04fe:
        r24 = 2;	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r6 = new byte[r0];	 Catch:{ all -> 0x00d5 }
        r0 = r27;	 Catch:{ all -> 0x00d5 }
        r12 = r0.readSocksReply(r14, r6);	 Catch:{ all -> 0x00d5 }
        r24 = 2;	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        if (r12 == r0) goto L_0x0519;	 Catch:{ all -> 0x00d5 }
    L_0x0510:
        r24 = new java.net.SocketException;	 Catch:{ all -> 0x00d5 }
        r25 = "Reply from SOCKS server badly formatted";	 Catch:{ all -> 0x00d5 }
        r24.<init>(r25);	 Catch:{ all -> 0x00d5 }
        throw r24;	 Catch:{ all -> 0x00d5 }
    L_0x0519:
        r24 = 0;	 Catch:{ all -> 0x00d5 }
        r24 = r6[r24];	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r0 = r0 & 255;	 Catch:{ all -> 0x00d5 }
        r24 = r0;	 Catch:{ all -> 0x00d5 }
        r17 = r24 << 8;	 Catch:{ all -> 0x00d5 }
        r24 = 1;	 Catch:{ all -> 0x00d5 }
        r24 = r6[r24];	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r0 = r0 & 255;	 Catch:{ all -> 0x00d5 }
        r24 = r0;	 Catch:{ all -> 0x00d5 }
        r17 = r17 + r24;	 Catch:{ all -> 0x00d5 }
        r24 = new java.net.InetSocketAddress;	 Catch:{ all -> 0x00d5 }
        r25 = new java.net.Inet6Address;	 Catch:{ all -> 0x00d5 }
        r26 = "";	 Catch:{ all -> 0x00d5 }
        r0 = r25;	 Catch:{ all -> 0x00d5 }
        r1 = r26;	 Catch:{ all -> 0x00d5 }
        r0.<init>(r1, r4);	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r1 = r25;	 Catch:{ all -> 0x00d5 }
        r2 = r17;	 Catch:{ all -> 0x00d5 }
        r0.<init>(r1, r2);	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r1 = r27;	 Catch:{ all -> 0x00d5 }
        r1.external_address = r0;	 Catch:{ all -> 0x00d5 }
        goto L_0x0338;	 Catch:{ all -> 0x00d5 }
    L_0x0550:
        r9 = new java.net.SocketException;	 Catch:{ all -> 0x00d5 }
        r24 = "SOCKS server general failure";	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r9.<init>(r0);	 Catch:{ all -> 0x00d5 }
        goto L_0x0338;	 Catch:{ all -> 0x00d5 }
    L_0x055c:
        r9 = new java.net.SocketException;	 Catch:{ all -> 0x00d5 }
        r24 = "SOCKS: Bind not allowed by ruleset";	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r9.<init>(r0);	 Catch:{ all -> 0x00d5 }
        goto L_0x0338;	 Catch:{ all -> 0x00d5 }
    L_0x0568:
        r9 = new java.net.SocketException;	 Catch:{ all -> 0x00d5 }
        r24 = "SOCKS: Network unreachable";	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r9.<init>(r0);	 Catch:{ all -> 0x00d5 }
        goto L_0x0338;	 Catch:{ all -> 0x00d5 }
    L_0x0574:
        r9 = new java.net.SocketException;	 Catch:{ all -> 0x00d5 }
        r24 = "SOCKS: Host unreachable";	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r9.<init>(r0);	 Catch:{ all -> 0x00d5 }
        goto L_0x0338;	 Catch:{ all -> 0x00d5 }
    L_0x0580:
        r9 = new java.net.SocketException;	 Catch:{ all -> 0x00d5 }
        r24 = "SOCKS: Connection refused";	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r9.<init>(r0);	 Catch:{ all -> 0x00d5 }
        goto L_0x0338;	 Catch:{ all -> 0x00d5 }
    L_0x058c:
        r9 = new java.net.SocketException;	 Catch:{ all -> 0x00d5 }
        r24 = "SOCKS: TTL expired";	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r9.<init>(r0);	 Catch:{ all -> 0x00d5 }
        goto L_0x0338;	 Catch:{ all -> 0x00d5 }
    L_0x0598:
        r9 = new java.net.SocketException;	 Catch:{ all -> 0x00d5 }
        r24 = "SOCKS: Command not supported";	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r9.<init>(r0);	 Catch:{ all -> 0x00d5 }
        goto L_0x0338;	 Catch:{ all -> 0x00d5 }
    L_0x05a4:
        r9 = new java.net.SocketException;	 Catch:{ all -> 0x00d5 }
        r24 = "SOCKS: address type not supported";	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r9.<init>(r0);	 Catch:{ all -> 0x00d5 }
        goto L_0x0338;	 Catch:{ all -> 0x00d5 }
    L_0x05b0:
        r0 = r27;	 Catch:{ all -> 0x00d5 }
        r0.cmdIn = r14;	 Catch:{ all -> 0x00d5 }
        r0 = r18;	 Catch:{ all -> 0x00d5 }
        r1 = r27;	 Catch:{ all -> 0x00d5 }
        r1.cmdOut = r0;	 Catch:{ all -> 0x00d5 }
        monitor-exit(r27);
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: java.net.SocksSocketImpl.socksBind(java.net.InetSocketAddress):void");
    }

    protected InetAddress getInetAddress() {
        if (this.external_address != null) {
            return this.external_address.getAddress();
        }
        return super.getInetAddress();
    }

    protected int getPort() {
        if (this.external_address != null) {
            return this.external_address.getPort();
        }
        return super.getPort();
    }

    protected int getLocalPort() {
        if (this.socket != null) {
            return super.getLocalPort();
        }
        if (this.external_address != null) {
            return this.external_address.getPort();
        }
        return super.getLocalPort();
    }

    protected void close() throws IOException {
        if (this.cmdsock != null) {
            this.cmdsock.close();
        }
        this.cmdsock = null;
        super.close();
    }

    private String getUserName() {
        String userName = "";
        if (!this.applicationSetProxy) {
            return (String) AccessController.doPrivileged(new GetPropertyAction("user.name"));
        }
        try {
            return System.getProperty("user.name");
        } catch (SecurityException e) {
            return userName;
        }
    }
}
