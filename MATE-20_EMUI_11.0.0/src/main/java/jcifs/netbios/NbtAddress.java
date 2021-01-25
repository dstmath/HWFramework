package jcifs.netbios;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import jcifs.Config;
import jcifs.util.Hexdump;

public final class NbtAddress {
    private static final HashMap ADDRESS_CACHE = new HashMap();
    static final String ANY_HOSTS_NAME = "*\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000";
    public static final int B_NODE = 0;
    private static final int CACHE_POLICY = Config.getInt("jcifs.netbios.cachePolicy", DEFAULT_CACHE_POLICY);
    private static final NameServiceClient CLIENT = new NameServiceClient();
    private static final int DEFAULT_CACHE_POLICY = 30;
    private static final int FOREVER = -1;
    public static final int H_NODE = 3;
    private static final HashMap LOOKUP_TABLE = new HashMap();
    public static final String MASTER_BROWSER_NAME = "\u0001\u0002__MSBROWSE__\u0002";
    public static final int M_NODE = 2;
    static final InetAddress[] NBNS = Config.getInetAddressArray("jcifs.netbios.wins", ",", new InetAddress[0]);
    public static final int P_NODE = 1;
    public static final String SMBSERVER_NAME = "*SMBSERVER     ";
    static final NbtAddress UNKNOWN_ADDRESS = new NbtAddress(UNKNOWN_NAME, 0, false, 0);
    static final byte[] UNKNOWN_MAC_ADDRESS = {0, 0, 0, 0, 0, 0};
    static final Name UNKNOWN_NAME = new Name("0.0.0.0", 0, null);
    static NbtAddress localhost;
    private static int nbnsIndex = 0;
    int address;
    String calledName;
    boolean groupName;
    Name hostName;
    boolean isActive;
    boolean isBeingDeleted;
    boolean isDataFromNodeStatus;
    boolean isInConflict;
    boolean isPermanent;
    byte[] macAddress;
    int nodeType;

    static {
        ADDRESS_CACHE.put(UNKNOWN_NAME, new CacheEntry(UNKNOWN_NAME, UNKNOWN_ADDRESS, -1));
        InetAddress localInetAddress = CLIENT.laddr;
        if (localInetAddress == null) {
            try {
                localInetAddress = InetAddress.getLocalHost();
            } catch (UnknownHostException e) {
                try {
                    localInetAddress = InetAddress.getByName("127.0.0.1");
                } catch (UnknownHostException e2) {
                }
            }
        }
        String localHostname = Config.getProperty("jcifs.netbios.hostname", null);
        if (localHostname == null || localHostname.length() == 0) {
            byte[] addr = localInetAddress.getAddress();
            localHostname = "JCIFS" + (addr[2] & 255) + "_" + (addr[3] & 255) + "_" + Hexdump.toHexString((int) (Math.random() * 255.0d), 2);
        }
        Name localName = new Name(localHostname, 0, Config.getProperty("jcifs.netbios.scope", null));
        localhost = new NbtAddress(localName, localInetAddress.hashCode(), false, 0, false, false, true, false, UNKNOWN_MAC_ADDRESS);
        cacheAddress(localName, localhost, -1);
    }

    /* access modifiers changed from: package-private */
    public static final class CacheEntry {
        NbtAddress address;
        long expiration;
        Name hostName;

        CacheEntry(Name hostName2, NbtAddress address2, long expiration2) {
            this.hostName = hostName2;
            this.address = address2;
            this.expiration = expiration2;
        }
    }

    static void cacheAddress(Name hostName2, NbtAddress addr) {
        if (CACHE_POLICY != 0) {
            long expiration = -1;
            if (CACHE_POLICY != -1) {
                expiration = System.currentTimeMillis() + ((long) (CACHE_POLICY * 1000));
            }
            cacheAddress(hostName2, addr, expiration);
        }
    }

    static void cacheAddress(Name hostName2, NbtAddress addr, long expiration) {
        if (CACHE_POLICY != 0) {
            synchronized (ADDRESS_CACHE) {
                CacheEntry entry = (CacheEntry) ADDRESS_CACHE.get(hostName2);
                if (entry == null) {
                    ADDRESS_CACHE.put(hostName2, new CacheEntry(hostName2, addr, expiration));
                } else {
                    entry.address = addr;
                    entry.expiration = expiration;
                }
            }
        }
    }

    static void cacheAddressArray(NbtAddress[] addrs) {
        if (CACHE_POLICY != 0) {
            long expiration = -1;
            if (CACHE_POLICY != -1) {
                expiration = System.currentTimeMillis() + ((long) (CACHE_POLICY * 1000));
            }
            synchronized (ADDRESS_CACHE) {
                for (int i = 0; i < addrs.length; i++) {
                    CacheEntry entry = (CacheEntry) ADDRESS_CACHE.get(addrs[i].hostName);
                    if (entry == null) {
                        ADDRESS_CACHE.put(addrs[i].hostName, new CacheEntry(addrs[i].hostName, addrs[i], expiration));
                    } else {
                        entry.address = addrs[i];
                        entry.expiration = expiration;
                    }
                }
            }
        }
    }

    static NbtAddress getCachedAddress(Name hostName2) {
        NbtAddress nbtAddress = null;
        if (CACHE_POLICY != 0) {
            synchronized (ADDRESS_CACHE) {
                CacheEntry entry = (CacheEntry) ADDRESS_CACHE.get(hostName2);
                if (entry != null && entry.expiration < System.currentTimeMillis() && entry.expiration >= 0) {
                    entry = null;
                }
                if (entry != null) {
                    nbtAddress = entry.address;
                }
            }
        }
        return nbtAddress;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:8:0x001a, code lost:
        r0 = (jcifs.netbios.NbtAddress) checkLookupTable(r4);
     */
    static NbtAddress doNameQuery(Name name, InetAddress svr) throws UnknownHostException {
        if (name.hexCode == 29 && svr == null) {
            svr = CLIENT.baddr;
        }
        name.srcHashCode = svr != null ? svr.hashCode() : 0;
        NbtAddress addr = getCachedAddress(name);
        if (addr == null && addr == null) {
            try {
                addr = CLIENT.getByName(name, svr);
            } catch (UnknownHostException e) {
                addr = UNKNOWN_ADDRESS;
            } finally {
                cacheAddress(name, addr);
                updateLookupTable(name);
            }
        }
        if (addr != UNKNOWN_ADDRESS) {
            return addr;
        }
        throw new UnknownHostException(name.toString());
    }

    private static Object checkLookupTable(Name name) {
        NbtAddress obj;
        synchronized (LOOKUP_TABLE) {
            if (!LOOKUP_TABLE.containsKey(name)) {
                LOOKUP_TABLE.put(name, name);
                obj = null;
            } else {
                while (LOOKUP_TABLE.containsKey(name)) {
                    try {
                        LOOKUP_TABLE.wait();
                    } catch (InterruptedException e) {
                    }
                }
                obj = getCachedAddress(name);
                if (obj == null) {
                    synchronized (LOOKUP_TABLE) {
                        LOOKUP_TABLE.put(name, name);
                    }
                }
            }
        }
        return obj;
    }

    private static void updateLookupTable(Name name) {
        synchronized (LOOKUP_TABLE) {
            LOOKUP_TABLE.remove(name);
            LOOKUP_TABLE.notifyAll();
        }
    }

    public static NbtAddress getLocalHost() throws UnknownHostException {
        return localhost;
    }

    public static Name getLocalName() {
        return localhost.hostName;
    }

    public static NbtAddress getByName(String host) throws UnknownHostException {
        return getByName(host, 0, null);
    }

    public static NbtAddress getByName(String host, int type, String scope) throws UnknownHostException {
        return getByName(host, type, scope, null);
    }

    public static NbtAddress getByName(String host, int type, String scope, InetAddress svr) throws UnknownHostException {
        if (host == null || host.length() == 0) {
            return getLocalHost();
        }
        if (!Character.isDigit(host.charAt(0))) {
            return doNameQuery(new Name(host, type, scope), svr);
        }
        int IP = 0;
        int hitDots = 0;
        char[] data = host.toCharArray();
        int i = 0;
        while (i < data.length) {
            char c = data[i];
            if (c < '0' || c > '9') {
                return doNameQuery(new Name(host, type, scope), svr);
            }
            int b = 0;
            while (c != '.') {
                if (c < '0' || c > '9') {
                    return doNameQuery(new Name(host, type, scope), svr);
                }
                b = ((b * 10) + c) - 48;
                i++;
                if (i >= data.length) {
                    break;
                }
                c = data[i];
            }
            if (b > 255) {
                return doNameQuery(new Name(host, type, scope), svr);
            }
            IP = (IP << 8) + b;
            hitDots++;
            i++;
        }
        if (hitDots != 4 || host.endsWith(".")) {
            return doNameQuery(new Name(host, type, scope), svr);
        }
        return new NbtAddress(UNKNOWN_NAME, IP, false, 0);
    }

    public static NbtAddress[] getAllByName(String host, int type, String scope, InetAddress svr) throws UnknownHostException {
        return CLIENT.getAllByName(new Name(host, type, scope), svr);
    }

    public static NbtAddress[] getAllByAddress(String host) throws UnknownHostException {
        return getAllByAddress(getByName(host, 0, null));
    }

    public static NbtAddress[] getAllByAddress(String host, int type, String scope) throws UnknownHostException {
        return getAllByAddress(getByName(host, type, scope));
    }

    public static NbtAddress[] getAllByAddress(NbtAddress addr) throws UnknownHostException {
        try {
            NbtAddress[] addrs = CLIENT.getNodeStatus(addr);
            cacheAddressArray(addrs);
            return addrs;
        } catch (UnknownHostException e) {
            throw new UnknownHostException("no name with type 0x" + Hexdump.toHexString(addr.hostName.hexCode, 2) + ((addr.hostName.scope == null || addr.hostName.scope.length() == 0) ? " with no scope" : " with scope " + addr.hostName.scope) + " for host " + addr.getHostAddress());
        }
    }

    public static InetAddress getWINSAddress() {
        if (NBNS.length == 0) {
            return null;
        }
        return NBNS[nbnsIndex];
    }

    public static boolean isWINS(InetAddress svr) {
        int i = 0;
        while (svr != null && i < NBNS.length) {
            if (svr.hashCode() == NBNS[i].hashCode()) {
                return true;
            }
            i++;
        }
        return false;
    }

    static InetAddress switchWINS() {
        nbnsIndex = nbnsIndex + 1 < NBNS.length ? nbnsIndex + 1 : 0;
        if (NBNS.length == 0) {
            return null;
        }
        return NBNS[nbnsIndex];
    }

    NbtAddress(Name hostName2, int address2, boolean groupName2, int nodeType2) {
        this.hostName = hostName2;
        this.address = address2;
        this.groupName = groupName2;
        this.nodeType = nodeType2;
    }

    NbtAddress(Name hostName2, int address2, boolean groupName2, int nodeType2, boolean isBeingDeleted2, boolean isInConflict2, boolean isActive2, boolean isPermanent2, byte[] macAddress2) {
        this.hostName = hostName2;
        this.address = address2;
        this.groupName = groupName2;
        this.nodeType = nodeType2;
        this.isBeingDeleted = isBeingDeleted2;
        this.isInConflict = isInConflict2;
        this.isActive = isActive2;
        this.isPermanent = isPermanent2;
        this.macAddress = macAddress2;
        this.isDataFromNodeStatus = true;
    }

    public String firstCalledName() {
        this.calledName = this.hostName.name;
        if (!Character.isDigit(this.calledName.charAt(0))) {
            switch (this.hostName.hexCode) {
                case 27:
                case 28:
                case 29:
                    this.calledName = SMBSERVER_NAME;
                    break;
            }
        } else {
            int dots = 0;
            int len = this.calledName.length();
            char[] data = this.calledName.toCharArray();
            int i = 0;
            while (true) {
                if (i >= len) {
                    break;
                }
                int i2 = i + 1;
                if (Character.isDigit(data[i])) {
                    if (i2 == len && dots == 3) {
                        this.calledName = SMBSERVER_NAME;
                        break;
                    } else if (i2 >= len || data[i2] != '.') {
                        i = i2;
                    } else {
                        dots++;
                        i = i2 + 1;
                    }
                } else {
                    break;
                }
            }
        }
        return this.calledName;
    }

    public String nextCalledName() {
        if (this.calledName == this.hostName.name) {
            this.calledName = SMBSERVER_NAME;
        } else if (this.calledName == SMBSERVER_NAME) {
            try {
                NbtAddress[] addrs = CLIENT.getNodeStatus(this);
                if (this.hostName.hexCode == 29) {
                    for (int i = 0; i < addrs.length; i++) {
                        if (addrs[i].hostName.hexCode == 32) {
                            return addrs[i].hostName.name;
                        }
                    }
                    return null;
                } else if (this.isDataFromNodeStatus) {
                    this.calledName = null;
                    return this.hostName.name;
                }
            } catch (UnknownHostException e) {
                this.calledName = null;
            }
        } else {
            this.calledName = null;
        }
        return this.calledName;
    }

    /* access modifiers changed from: package-private */
    public void checkData() throws UnknownHostException {
        if (this.hostName == UNKNOWN_NAME) {
            getAllByAddress(this);
        }
    }

    /* access modifiers changed from: package-private */
    public void checkNodeStatusData() throws UnknownHostException {
        if (!this.isDataFromNodeStatus) {
            getAllByAddress(this);
        }
    }

    public boolean isGroupAddress() throws UnknownHostException {
        checkData();
        return this.groupName;
    }

    public int getNodeType() throws UnknownHostException {
        checkData();
        return this.nodeType;
    }

    public boolean isBeingDeleted() throws UnknownHostException {
        checkNodeStatusData();
        return this.isBeingDeleted;
    }

    public boolean isInConflict() throws UnknownHostException {
        checkNodeStatusData();
        return this.isInConflict;
    }

    public boolean isActive() throws UnknownHostException {
        checkNodeStatusData();
        return this.isActive;
    }

    public boolean isPermanent() throws UnknownHostException {
        checkNodeStatusData();
        return this.isPermanent;
    }

    public byte[] getMacAddress() throws UnknownHostException {
        checkNodeStatusData();
        return this.macAddress;
    }

    public String getHostName() {
        if (this.hostName == UNKNOWN_NAME) {
            return getHostAddress();
        }
        return this.hostName.name;
    }

    public byte[] getAddress() {
        return new byte[]{(byte) ((this.address >>> 24) & 255), (byte) ((this.address >>> 16) & 255), (byte) ((this.address >>> 8) & 255), (byte) (this.address & 255)};
    }

    public InetAddress getInetAddress() throws UnknownHostException {
        return InetAddress.getByName(getHostAddress());
    }

    public String getHostAddress() {
        return ((this.address >>> 24) & 255) + "." + ((this.address >>> 16) & 255) + "." + ((this.address >>> 8) & 255) + "." + ((this.address >>> 0) & 255);
    }

    public int getNameType() {
        return this.hostName.hexCode;
    }

    public int hashCode() {
        return this.address;
    }

    public boolean equals(Object obj) {
        return obj != null && (obj instanceof NbtAddress) && ((NbtAddress) obj).address == this.address;
    }

    public String toString() {
        return this.hostName.toString() + "/" + getHostAddress();
    }
}
