package jcifs.netbios;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.StringTokenizer;
import jcifs.Config;
import jcifs.util.Hexdump;
import jcifs.util.LogStream;

class NameServiceClient implements Runnable {
    static final int DEFAULT_RCV_BUF_SIZE = 576;
    static final int DEFAULT_RETRY_COUNT = 2;
    static final int DEFAULT_RETRY_TIMEOUT = 3000;
    static final int DEFAULT_SND_BUF_SIZE = 576;
    static final int DEFAULT_SO_TIMEOUT = 5000;
    private static final InetAddress LADDR = null;
    private static final int LPORT = 0;
    static final int NAME_SERVICE_UDP_PORT = 137;
    private static final int RCV_BUF_SIZE = 0;
    static final int RESOLVER_BCAST = 2;
    static final int RESOLVER_LMHOSTS = 1;
    static final int RESOLVER_WINS = 3;
    private static final int RETRY_COUNT = 0;
    private static final int RETRY_TIMEOUT = 0;
    private static final String RO = null;
    private static final int SND_BUF_SIZE = 0;
    private static final int SO_TIMEOUT = 0;
    private static LogStream log;
    private final Object LOCK;
    InetAddress baddr;
    private int closeTimeout;
    private DatagramPacket in;
    InetAddress laddr;
    private int lport;
    private int nextNameTrnId;
    private DatagramPacket out;
    private byte[] rcv_buf;
    private int[] resolveOrder;
    private HashMap responseTable;
    private byte[] snd_buf;
    private DatagramSocket socket;
    private Thread thread;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: jcifs.netbios.NameServiceClient.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: jcifs.netbios.NameServiceClient.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: jcifs.netbios.NameServiceClient.<clinit>():void");
    }

    NameServiceClient() {
        this(LPORT, LADDR);
    }

    NameServiceClient(int lport, InetAddress laddr) {
        this.LOCK = new Object();
        this.responseTable = new HashMap();
        this.nextNameTrnId = RCV_BUF_SIZE;
        this.lport = lport;
        this.laddr = laddr;
        try {
            this.baddr = Config.getInetAddress("jcifs.netbios.baddr", InetAddress.getByName("255.255.255.255"));
        } catch (UnknownHostException e) {
        }
        this.snd_buf = new byte[SND_BUF_SIZE];
        this.rcv_buf = new byte[RCV_BUF_SIZE];
        this.out = new DatagramPacket(this.snd_buf, SND_BUF_SIZE, this.baddr, NAME_SERVICE_UDP_PORT);
        this.in = new DatagramPacket(this.rcv_buf, RCV_BUF_SIZE);
        if (RO != null && RO.length() != 0) {
            int[] tmp = new int[RESOLVER_WINS];
            StringTokenizer st = new StringTokenizer(RO, ",");
            int i = RCV_BUF_SIZE;
            while (st.hasMoreTokens()) {
                String s = st.nextToken().trim();
                int i2;
                if (s.equalsIgnoreCase("LMHOSTS")) {
                    i2 = i + RESOLVER_LMHOSTS;
                    tmp[i] = RESOLVER_LMHOSTS;
                    i = i2;
                } else if (s.equalsIgnoreCase("WINS")) {
                    if (NbtAddress.getWINSAddress() == null) {
                        r5 = log;
                        if (LogStream.level > RESOLVER_LMHOSTS) {
                            log.println("NetBIOS resolveOrder specifies WINS however the jcifs.netbios.wins property has not been set");
                        }
                    } else {
                        i2 = i + RESOLVER_LMHOSTS;
                        tmp[i] = RESOLVER_WINS;
                        i = i2;
                    }
                } else if (s.equalsIgnoreCase("BCAST")) {
                    i2 = i + RESOLVER_LMHOSTS;
                    tmp[i] = RESOLVER_BCAST;
                    i = i2;
                } else if (!s.equalsIgnoreCase("DNS")) {
                    r5 = log;
                    if (LogStream.level > RESOLVER_LMHOSTS) {
                        log.println("unknown resolver method: " + s);
                    }
                }
            }
            this.resolveOrder = new int[i];
            System.arraycopy(tmp, RCV_BUF_SIZE, this.resolveOrder, RCV_BUF_SIZE, i);
        } else if (NbtAddress.getWINSAddress() == null) {
            this.resolveOrder = new int[RESOLVER_BCAST];
            this.resolveOrder[RCV_BUF_SIZE] = RESOLVER_LMHOSTS;
            this.resolveOrder[RESOLVER_LMHOSTS] = RESOLVER_BCAST;
        } else {
            this.resolveOrder = new int[RESOLVER_WINS];
            this.resolveOrder[RCV_BUF_SIZE] = RESOLVER_LMHOSTS;
            this.resolveOrder[RESOLVER_LMHOSTS] = RESOLVER_WINS;
            this.resolveOrder[RESOLVER_BCAST] = RESOLVER_BCAST;
        }
    }

    int getNextNameTrnId() {
        int i = this.nextNameTrnId + RESOLVER_LMHOSTS;
        this.nextNameTrnId = i;
        if ((i & 65535) == 0) {
            this.nextNameTrnId = RESOLVER_LMHOSTS;
        }
        return this.nextNameTrnId;
    }

    void ensureOpen(int timeout) throws IOException {
        this.closeTimeout = RCV_BUF_SIZE;
        if (SO_TIMEOUT != 0) {
            this.closeTimeout = Math.max(SO_TIMEOUT, timeout);
        }
        if (this.socket == null) {
            this.socket = new DatagramSocket(this.lport, this.laddr);
            this.thread = new Thread(this, "JCIFS-NameServiceClient");
            this.thread.setDaemon(true);
            this.thread.start();
        }
    }

    void tryClose() {
        synchronized (this.LOCK) {
            if (this.socket != null) {
                this.socket.close();
                this.socket = null;
            }
            this.thread = null;
            this.responseTable.clear();
        }
    }

    public void run() {
        while (this.thread == Thread.currentThread()) {
            LogStream logStream;
            try {
                this.in.setLength(RCV_BUF_SIZE);
                this.socket.setSoTimeout(this.closeTimeout);
                this.socket.receive(this.in);
                logStream = log;
                if (LogStream.level > RESOLVER_WINS) {
                    log.println("NetBIOS: new data read from socket");
                }
                NameServicePacket response = (NameServicePacket) this.responseTable.get(new Integer(NameServicePacket.readNameTrnId(this.rcv_buf, RCV_BUF_SIZE)));
                if (!(response == null || response.received)) {
                    synchronized (response) {
                        response.readWireFormat(this.rcv_buf, RCV_BUF_SIZE);
                        response.received = true;
                        logStream = log;
                        if (LogStream.level > RESOLVER_WINS) {
                            log.println(response);
                            Hexdump.hexdump(log, this.rcv_buf, RCV_BUF_SIZE, this.in.getLength());
                        }
                        response.notify();
                    }
                }
            } catch (SocketTimeoutException e) {
                tryClose();
                return;
            } catch (Exception ex) {
                try {
                    logStream = log;
                    if (LogStream.level > RESOLVER_BCAST) {
                        ex.printStackTrace(log);
                    }
                    tryClose();
                    return;
                } catch (Throwable th) {
                    tryClose();
                }
            }
        }
        tryClose();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void send(NameServicePacket request, NameServicePacket response, int timeout) throws IOException {
        Throwable th;
        InterruptedException ie;
        int max = NbtAddress.NBNS.length;
        if (max == 0) {
            max = RESOLVER_LMHOSTS;
        }
        synchronized (response) {
            int max2 = max;
            Integer nid = null;
            while (true) {
                max = max2 - 1;
                if (max2 <= 0) {
                    break;
                }
                Integer num;
                try {
                    synchronized (this.LOCK) {
                        try {
                            request.nameTrnId = getNextNameTrnId();
                            num = new Integer(request.nameTrnId);
                            try {
                                this.out.setAddress(request.addr);
                                this.out.setLength(request.writeWireFormat(this.snd_buf, RCV_BUF_SIZE));
                                response.received = false;
                                this.responseTable.put(num, response);
                                ensureOpen(timeout + 1000);
                                this.socket.send(this.out);
                                LogStream logStream = log;
                                if (LogStream.level > RESOLVER_WINS) {
                                    log.println(request);
                                    Hexdump.hexdump(log, this.snd_buf, RCV_BUF_SIZE, this.out.getLength());
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                throw th;
                            }
                            try {
                                long start = System.currentTimeMillis();
                                while (timeout > 0) {
                                    response.wait((long) timeout);
                                    if (response.received && request.questionType == response.recordType) {
                                        this.responseTable.remove(num);
                                        return;
                                    }
                                    response.received = false;
                                    timeout = (int) (((long) timeout) - (System.currentTimeMillis() - start));
                                }
                                this.responseTable.remove(num);
                                synchronized (this.LOCK) {
                                    if (!NbtAddress.isWINS(request.addr)) {
                                        break;
                                    }
                                    if (request.addr == NbtAddress.getWINSAddress()) {
                                        NbtAddress.switchWINS();
                                    }
                                    request.addr = NbtAddress.getWINSAddress();
                                    max2 = max;
                                    nid = num;
                                }
                            } catch (InterruptedException e) {
                                ie = e;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            num = nid;
                        }
                    }
                } catch (InterruptedException e2) {
                    ie = e2;
                    num = nid;
                } catch (Throwable th4) {
                    th = th4;
                    num = nid;
                }
            }
        }
    }

    NbtAddress[] getAllByName(Name name, InetAddress addr) throws UnknownHostException {
        boolean z;
        int n;
        NameQueryRequest request = new NameQueryRequest(name);
        NameQueryResponse response = new NameQueryResponse();
        if (addr == null) {
            addr = NbtAddress.getWINSAddress();
        }
        request.addr = addr;
        if (request.addr == null) {
            z = true;
        } else {
            z = false;
        }
        request.isBroadcast = z;
        if (request.isBroadcast) {
            request.addr = this.baddr;
            n = RETRY_COUNT;
        } else {
            request.isBroadcast = false;
            n = RESOLVER_LMHOSTS;
        }
        do {
            try {
                send(request, response, RETRY_TIMEOUT);
                if (!response.received || response.resultCode != 0) {
                    n--;
                    if (n <= 0) {
                        break;
                    }
                } else {
                    return response.addrEntry;
                }
            } catch (IOException ioe) {
                LogStream logStream = log;
                if (LogStream.level > RESOLVER_LMHOSTS) {
                    ioe.printStackTrace(log);
                }
                throw new UnknownHostException(name.name);
            }
        } while (request.isBroadcast);
        throw new UnknownHostException(name.name);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    NbtAddress getByName(Name name, InetAddress addr) throws UnknownHostException {
        boolean z = false;
        NameQueryRequest request = new NameQueryRequest(name);
        NameQueryResponse response = new NameQueryResponse();
        int n;
        if (addr != null) {
            request.addr = addr;
            if (addr.getAddress()[RESOLVER_WINS] == -1) {
                z = true;
            }
            request.isBroadcast = z;
            n = RETRY_COUNT;
            do {
                try {
                    send(request, response, RETRY_TIMEOUT);
                    if (!response.received || response.resultCode != 0) {
                        n--;
                        if (n <= 0) {
                            break;
                        }
                    } else {
                        int last = response.addrEntry.length - 1;
                        response.addrEntry[last].hostName.srcHashCode = addr.hashCode();
                        return response.addrEntry[last];
                    }
                } catch (IOException ioe) {
                    LogStream logStream = log;
                    if (LogStream.level > RESOLVER_LMHOSTS) {
                        ioe.printStackTrace(log);
                    }
                    throw new UnknownHostException(name.name);
                }
            } while (request.isBroadcast);
            throw new UnknownHostException(name.name);
        }
        for (int i = RCV_BUF_SIZE; i < this.resolveOrder.length; i += RESOLVER_LMHOSTS) {
            switch (this.resolveOrder[i]) {
                case RESOLVER_LMHOSTS /*1*/:
                    NbtAddress ans = Lmhosts.getByName(name);
                    if (ans == null) {
                        break;
                    }
                    ans.hostName.srcHashCode = RCV_BUF_SIZE;
                    return ans;
                case RESOLVER_BCAST /*2*/:
                case RESOLVER_WINS /*3*/:
                    if (this.resolveOrder[i] != RESOLVER_WINS || name.name == NbtAddress.MASTER_BROWSER_NAME || name.hexCode == 29) {
                        request.addr = this.baddr;
                        request.isBroadcast = true;
                    } else {
                        request.addr = NbtAddress.getWINSAddress();
                        request.isBroadcast = false;
                    }
                    int n2 = RETRY_COUNT;
                    while (true) {
                        n = n2 - 1;
                        if (n2 > 0) {
                            try {
                                send(request, response, RETRY_TIMEOUT);
                                if (!response.received || response.resultCode != 0) {
                                    if (this.resolveOrder[i] == RESOLVER_WINS) {
                                        break;
                                    }
                                    n2 = n;
                                } else {
                                    response.addrEntry[RCV_BUF_SIZE].hostName.srcHashCode = request.addr.hashCode();
                                    return response.addrEntry[RCV_BUF_SIZE];
                                }
                            } catch (IOException ioe2) {
                                logStream = log;
                                if (LogStream.level > RESOLVER_LMHOSTS) {
                                    ioe2.printStackTrace(log);
                                }
                                throw new UnknownHostException(name.name);
                            } catch (IOException e) {
                                break;
                            }
                        }
                        continue;
                    }
                    break;
                default:
                    continue;
            }
        }
        throw new UnknownHostException(name.name);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    NbtAddress[] getNodeStatus(NbtAddress addr) throws UnknownHostException {
        NodeStatusResponse response = new NodeStatusResponse(addr);
        NodeStatusRequest request = new NodeStatusRequest(new Name("*\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000", RCV_BUF_SIZE, null));
        request.addr = addr.getInetAddress();
        int n = RETRY_COUNT;
        while (true) {
            int n2 = n - 1;
            if (n <= 0) {
                break;
            }
            try {
                send(request, response, RETRY_TIMEOUT);
                if (response.received && response.resultCode == 0) {
                    break;
                }
                n = n2;
            } catch (IOException ioe) {
                LogStream logStream = log;
                if (LogStream.level > RESOLVER_LMHOSTS) {
                    ioe.printStackTrace(log);
                }
                throw new UnknownHostException(addr.toString());
            }
        }
        throw new UnknownHostException(addr.hostName.name);
    }
}
