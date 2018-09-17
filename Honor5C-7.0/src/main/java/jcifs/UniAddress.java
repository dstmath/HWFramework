package jcifs;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import jcifs.netbios.Lmhosts;
import jcifs.netbios.NbtAddress;
import jcifs.smb.SmbFile;
import jcifs.util.LogStream;

public class UniAddress {
    private static final int RESOLVER_BCAST = 1;
    private static final int RESOLVER_DNS = 2;
    private static final int RESOLVER_LMHOSTS = 3;
    private static final int RESOLVER_WINS = 0;
    private static InetAddress baddr;
    private static LogStream log;
    private static int[] resolveOrder;
    Object addr;
    String calledName;

    static class QueryThread extends Thread {
        NbtAddress ans;
        String host;
        String scope;
        Sem sem;
        InetAddress svr;
        int type;
        UnknownHostException uhe;

        QueryThread(Sem sem, String host, int type, String scope, InetAddress svr) {
            super("JCIFS-QueryThread: " + host);
            this.ans = null;
            this.sem = sem;
            this.host = host;
            this.type = type;
            this.scope = scope;
            this.svr = svr;
        }

        public void run() {
            Sem sem;
            try {
                this.ans = NbtAddress.getByName(this.host, this.type, this.scope, this.svr);
                synchronized (this.sem) {
                    sem = this.sem;
                    sem.count--;
                    this.sem.notify();
                }
            } catch (UnknownHostException uhe) {
                this.uhe = uhe;
                synchronized (this.sem) {
                }
                sem = this.sem;
                sem.count--;
                this.sem.notify();
            } catch (Exception ex) {
                this.uhe = new UnknownHostException(ex.getMessage());
                synchronized (this.sem) {
                }
                sem = this.sem;
                sem.count--;
                this.sem.notify();
            } catch (Throwable th) {
                synchronized (this.sem) {
                }
                Sem sem2 = this.sem;
                sem2.count--;
                this.sem.notify();
            }
        }
    }

    static class Sem {
        int count;

        Sem(int count) {
            this.count = count;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: jcifs.UniAddress.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: jcifs.UniAddress.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: jcifs.UniAddress.<clinit>():void");
    }

    static NbtAddress lookupServerOrWorkgroup(String name, InetAddress svr) throws UnknownHostException {
        Sem sem = new Sem(RESOLVER_DNS);
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
        int i = 0;
        int len = hostname.length();
        char[] data = hostname.toCharArray();
        int i2 = i;
        while (i2 < len) {
            i = i2 + RESOLVER_BCAST;
            if (!Character.isDigit(data[i2])) {
                return false;
            }
            if (i == len && dots == RESOLVER_LMHOSTS) {
                return true;
            }
            if (i >= len || data[i] != '.') {
                i2 = i;
            } else {
                dots += RESOLVER_BCAST;
                i2 = i + RESOLVER_BCAST;
            }
        }
        return false;
    }

    static boolean isAllDigits(String hostname) {
        for (int i = 0; i < hostname.length(); i += RESOLVER_BCAST) {
            if (!Character.isDigit(hostname.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static UniAddress getByName(String hostname, boolean possibleNTDomainOrWorkgroup) throws UnknownHostException {
        return getAllByName(hostname, possibleNTDomainOrWorkgroup)[0];
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static UniAddress[] getAllByName(String hostname, boolean possibleNTDomainOrWorkgroup) throws UnknownHostException {
        if (hostname == null || hostname.length() == 0) {
            throw new UnknownHostException();
        } else if (isDotQuadIP(hostname)) {
            addrs = new UniAddress[RESOLVER_BCAST];
            addrs[0] = new UniAddress(NbtAddress.getByName(hostname));
            return addrs;
        } else {
            int i = 0;
            while (i < resolveOrder.length) {
                try {
                    NbtAddress addr;
                    switch (resolveOrder[i]) {
                        case SmbFile.FILE_NO_SHARE /*0*/:
                            if (hostname != NbtAddress.MASTER_BROWSER_NAME && hostname.length() <= 15) {
                                if (!possibleNTDomainOrWorkgroup) {
                                    addr = NbtAddress.getByName(hostname, 32, null, NbtAddress.getWINSAddress());
                                    break;
                                }
                                addr = lookupServerOrWorkgroup(hostname, NbtAddress.getWINSAddress());
                                break;
                            }
                        case RESOLVER_BCAST /*1*/:
                            if (hostname.length() <= 15) {
                                if (!possibleNTDomainOrWorkgroup) {
                                    addr = NbtAddress.getByName(hostname, 32, null, baddr);
                                    break;
                                }
                                addr = lookupServerOrWorkgroup(hostname, baddr);
                                break;
                            }
                            continue;
                        case RESOLVER_DNS /*2*/:
                            if (isAllDigits(hostname)) {
                                throw new UnknownHostException(hostname);
                            }
                            InetAddress[] iaddrs = InetAddress.getAllByName(hostname);
                            addrs = new UniAddress[iaddrs.length];
                            for (int ii = 0; ii < iaddrs.length; ii += RESOLVER_BCAST) {
                                addrs[ii] = new UniAddress(iaddrs[ii]);
                            }
                            return addrs;
                        case RESOLVER_LMHOSTS /*3*/:
                            addr = Lmhosts.getByName(hostname);
                            if (addr != null) {
                                break;
                            }
                            continue;
                        default:
                            throw new UnknownHostException(hostname);
                    }
                    addrs = new UniAddress[RESOLVER_BCAST];
                    addrs[0] = new UniAddress(addr);
                    return addrs;
                } catch (IOException e) {
                }
            }
            throw new UnknownHostException(hostname);
        }
    }

    public UniAddress(Object addr) {
        if (addr == null) {
            throw new IllegalArgumentException();
        }
        this.addr = addr;
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
            if (i > RESOLVER_BCAST && i < 15) {
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
