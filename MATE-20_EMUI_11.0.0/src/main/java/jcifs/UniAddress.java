package jcifs;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.StringTokenizer;
import jcifs.netbios.Lmhosts;
import jcifs.netbios.NbtAddress;
import jcifs.util.LogStream;

public class UniAddress {
    private static final int RESOLVER_BCAST = 1;
    private static final int RESOLVER_DNS = 2;
    private static final int RESOLVER_LMHOSTS = 3;
    private static final int RESOLVER_WINS = 0;
    private static InetAddress baddr;
    private static LogStream log = LogStream.getInstance();
    private static int[] resolveOrder;
    Object addr;
    String calledName;

    static {
        String ro = Config.getProperty("jcifs.resolveOrder");
        InetAddress nbns = NbtAddress.getWINSAddress();
        try {
            baddr = Config.getInetAddress("jcifs.netbios.baddr", InetAddress.getByName("255.255.255.255"));
        } catch (UnknownHostException e) {
        }
        if (ro != null && ro.length() != 0) {
            int[] tmp = new int[4];
            StringTokenizer st = new StringTokenizer(ro, ",");
            int i = 0;
            while (st.hasMoreTokens()) {
                String s = st.nextToken().trim();
                if (s.equalsIgnoreCase("LMHOSTS")) {
                    tmp[i] = 3;
                    i++;
                } else if (s.equalsIgnoreCase("WINS")) {
                    if (nbns == null) {
                        LogStream logStream = log;
                        if (LogStream.level > 1) {
                            log.println("UniAddress resolveOrder specifies WINS however the jcifs.netbios.wins property has not been set");
                        }
                    } else {
                        tmp[i] = 0;
                        i++;
                    }
                } else if (s.equalsIgnoreCase("BCAST")) {
                    tmp[i] = 1;
                    i++;
                } else if (s.equalsIgnoreCase("DNS")) {
                    tmp[i] = 2;
                    i++;
                } else {
                    LogStream logStream2 = log;
                    if (LogStream.level > 1) {
                        log.println("unknown resolver method: " + s);
                    }
                }
            }
            resolveOrder = new int[i];
            System.arraycopy(tmp, 0, resolveOrder, 0, i);
        } else if (nbns == null) {
            resolveOrder = new int[3];
            resolveOrder[0] = 3;
            resolveOrder[1] = 2;
            resolveOrder[2] = 1;
        } else {
            resolveOrder = new int[4];
            resolveOrder[0] = 3;
            resolveOrder[1] = 0;
            resolveOrder[2] = 2;
            resolveOrder[3] = 1;
        }
    }

    /* access modifiers changed from: package-private */
    public static class Sem {
        int count;

        Sem(int count2) {
            this.count = count2;
        }
    }

    /* access modifiers changed from: package-private */
    public static class QueryThread extends Thread {
        NbtAddress ans = null;
        String host;
        String scope;
        Sem sem;
        InetAddress svr;
        int type;
        UnknownHostException uhe;

        QueryThread(Sem sem2, String host2, int type2, String scope2, InetAddress svr2) {
            super("JCIFS-QueryThread: " + host2);
            this.sem = sem2;
            this.host = host2;
            this.type = type2;
            this.scope = scope2;
            this.svr = svr2;
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            try {
                this.ans = NbtAddress.getByName(this.host, this.type, this.scope, this.svr);
                synchronized (this.sem) {
                    Sem sem2 = this.sem;
                    sem2.count--;
                    this.sem.notify();
                }
            } catch (UnknownHostException uhe2) {
                this.uhe = uhe2;
                synchronized (this.sem) {
                    Sem sem3 = this.sem;
                    sem3.count--;
                    this.sem.notify();
                }
            } catch (Exception ex) {
                this.uhe = new UnknownHostException(ex.getMessage());
                synchronized (this.sem) {
                    Sem sem4 = this.sem;
                    sem4.count--;
                    this.sem.notify();
                }
            } catch (Throwable th) {
                synchronized (this.sem) {
                    Sem sem5 = this.sem;
                    sem5.count--;
                    this.sem.notify();
                    throw th;
                }
            }
        }
    }

    static NbtAddress lookupServerOrWorkgroup(String name, InetAddress svr) throws UnknownHostException {
        Sem sem = new Sem(2);
        QueryThread q1x = new QueryThread(sem, name, NbtAddress.isWINS(svr) ? 27 : 29, null, svr);
        QueryThread q20 = new QueryThread(sem, name, 32, null, svr);
        q1x.setDaemon(true);
        q20.setDaemon(true);
        try {
            synchronized (sem) {
                q1x.start();
                q20.start();
                while (sem.count > 0 && q1x.ans == null && q20.ans == null) {
                    sem.wait();
                }
            }
            if (q1x.ans != null) {
                return q1x.ans;
            }
            if (q20.ans != null) {
                return q20.ans;
            }
            throw q1x.uhe;
        } catch (InterruptedException e) {
            throw new UnknownHostException(name);
        }
    }

    public static UniAddress getByName(String hostname) throws UnknownHostException {
        return getByName(hostname, false);
    }

    static boolean isDotQuadIP(String hostname) {
        if (!Character.isDigit(hostname.charAt(0))) {
            return false;
        }
        int dots = 0;
        int len = hostname.length();
        char[] data = hostname.toCharArray();
        int i = 0;
        while (i < len) {
            int i2 = i + 1;
            if (!Character.isDigit(data[i])) {
                return false;
            }
            if (i2 == len && dots == 3) {
                return true;
            }
            if (i2 >= len || data[i2] != '.') {
                i = i2;
            } else {
                dots++;
                i = i2 + 1;
            }
        }
        return false;
    }

    static boolean isAllDigits(String hostname) {
        for (int i = 0; i < hostname.length(); i++) {
            if (!Character.isDigit(hostname.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static UniAddress getByName(String hostname, boolean possibleNTDomainOrWorkgroup) throws UnknownHostException {
        return getAllByName(hostname, possibleNTDomainOrWorkgroup)[0];
    }

    public static UniAddress[] getAllByName(String hostname, boolean possibleNTDomainOrWorkgroup) throws UnknownHostException {
        NbtAddress addr2;
        if (hostname == null || hostname.length() == 0) {
            throw new UnknownHostException();
        } else if (isDotQuadIP(hostname)) {
            return new UniAddress[]{new UniAddress(NbtAddress.getByName(hostname))};
        } else {
            for (int i = 0; i < resolveOrder.length; i++) {
                try {
                    switch (resolveOrder[i]) {
                        case 0:
                            if (hostname != NbtAddress.MASTER_BROWSER_NAME && hostname.length() <= 15) {
                                if (possibleNTDomainOrWorkgroup) {
                                    addr2 = lookupServerOrWorkgroup(hostname, NbtAddress.getWINSAddress());
                                } else {
                                    addr2 = NbtAddress.getByName(hostname, 32, null, NbtAddress.getWINSAddress());
                                }
                                return new UniAddress[]{new UniAddress(addr2)};
                            }
                        case 1:
                            if (hostname.length() <= 15) {
                                if (possibleNTDomainOrWorkgroup) {
                                    addr2 = lookupServerOrWorkgroup(hostname, baddr);
                                } else {
                                    addr2 = NbtAddress.getByName(hostname, 32, null, baddr);
                                }
                                return new UniAddress[]{new UniAddress(addr2)};
                            }
                            continue;
                        case 2:
                            if (isAllDigits(hostname)) {
                                throw new UnknownHostException(hostname);
                            }
                            InetAddress[] iaddrs = InetAddress.getAllByName(hostname);
                            UniAddress[] addrs = new UniAddress[iaddrs.length];
                            for (int ii = 0; ii < iaddrs.length; ii++) {
                                addrs[ii] = new UniAddress(iaddrs[ii]);
                            }
                            return addrs;
                        case 3:
                            addr2 = Lmhosts.getByName(hostname);
                            if (addr2 != null) {
                                return new UniAddress[]{new UniAddress(addr2)};
                            }
                            continue;
                        default:
                            throw new UnknownHostException(hostname);
                    }
                } catch (IOException e) {
                }
            }
            throw new UnknownHostException(hostname);
        }
    }

    public UniAddress(Object addr2) {
        if (addr2 == null) {
            throw new IllegalArgumentException();
        }
        this.addr = addr2;
    }

    public int hashCode() {
        return this.addr.hashCode();
    }

    public boolean equals(Object obj) {
        return (obj instanceof UniAddress) && this.addr.equals(((UniAddress) obj).addr);
    }

    public String firstCalledName() {
        if (this.addr instanceof NbtAddress) {
            return ((NbtAddress) this.addr).firstCalledName();
        }
        this.calledName = ((InetAddress) this.addr).getHostName();
        if (isDotQuadIP(this.calledName)) {
            this.calledName = NbtAddress.SMBSERVER_NAME;
        } else {
            int i = this.calledName.indexOf(46);
            if (i > 1 && i < 15) {
                this.calledName = this.calledName.substring(0, i).toUpperCase();
            } else if (this.calledName.length() > 15) {
                this.calledName = NbtAddress.SMBSERVER_NAME;
            } else {
                this.calledName = this.calledName.toUpperCase();
            }
        }
        return this.calledName;
    }

    public String nextCalledName() {
        if (this.addr instanceof NbtAddress) {
            return ((NbtAddress) this.addr).nextCalledName();
        }
        if (this.calledName == NbtAddress.SMBSERVER_NAME) {
            return null;
        }
        this.calledName = NbtAddress.SMBSERVER_NAME;
        return this.calledName;
    }

    public Object getAddress() {
        return this.addr;
    }

    public String getHostName() {
        if (this.addr instanceof NbtAddress) {
            return ((NbtAddress) this.addr).getHostName();
        }
        return ((InetAddress) this.addr).getHostName();
    }

    public String getHostAddress() {
        if (this.addr instanceof NbtAddress) {
            return ((NbtAddress) this.addr).getHostAddress();
        }
        return ((InetAddress) this.addr).getHostAddress();
    }

    public String toString() {
        return this.addr.toString();
    }
}
