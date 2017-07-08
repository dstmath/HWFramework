package java.net;

import android.system.OsConstants;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Enumeration;
import java.util.regex.Pattern;
import libcore.io.Libcore;

public final class Inet6Address extends InetAddress {
    public static final InetAddress ANY = null;
    static final int INADDRSZ = 16;
    private static final int INT16SZ = 2;
    public static final InetAddress LOOPBACK = null;
    private static final long serialVersionUID = 6880410070516793377L;
    private String ifname;
    byte[] ipaddress;
    private int scope_id;
    private boolean scope_id_set;
    private transient NetworkInterface scope_ifname;
    private boolean scope_ifname_set;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.net.Inet6Address.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.net.Inet6Address.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.net.Inet6Address.<clinit>():void");
    }

    Inet6Address() {
        this.scope_id = 0;
        this.scope_id_set = false;
        this.scope_ifname = null;
        this.scope_ifname_set = false;
        holder().hostName = null;
        this.ipaddress = new byte[INADDRSZ];
        holder().family = OsConstants.AF_INET6;
    }

    Inet6Address(String hostName, byte[] addr, int scope_id) {
        this.scope_id = 0;
        this.scope_id_set = false;
        this.scope_ifname = null;
        this.scope_ifname_set = false;
        holder().hostName = hostName;
        if (addr.length == INADDRSZ) {
            holder().family = OsConstants.AF_INET6;
            this.ipaddress = (byte[]) addr.clone();
        }
        if (scope_id > 0) {
            this.scope_id = scope_id;
            this.scope_id_set = true;
        }
    }

    Inet6Address(String hostName, byte[] addr) {
        this.scope_id = 0;
        this.scope_id_set = false;
        this.scope_ifname = null;
        this.scope_ifname_set = false;
        try {
            initif(hostName, addr, null);
        } catch (UnknownHostException e) {
        }
    }

    Inet6Address(String hostName, byte[] addr, NetworkInterface nif) throws UnknownHostException {
        this.scope_id = 0;
        this.scope_id_set = false;
        this.scope_ifname = null;
        this.scope_ifname_set = false;
        initif(hostName, addr, nif);
    }

    Inet6Address(String hostName, byte[] addr, String ifname) throws UnknownHostException {
        this.scope_id = 0;
        this.scope_id_set = false;
        this.scope_ifname = null;
        this.scope_ifname_set = false;
        initstr(hostName, addr, ifname);
    }

    public static Inet6Address getByAddress(String host, byte[] addr, NetworkInterface nif) throws UnknownHostException {
        if (host != null && host.length() > 0 && host.charAt(0) == '[' && host.charAt(host.length() - 1) == ']') {
            host = host.substring(1, host.length() - 1);
        }
        if (addr != null && addr.length == INADDRSZ) {
            return new Inet6Address(host, addr, nif);
        }
        throw new UnknownHostException("addr is of illegal length");
    }

    public static Inet6Address getByAddress(String host, byte[] addr, int scope_id) throws UnknownHostException {
        if (host != null && host.length() > 0 && host.charAt(0) == '[' && host.charAt(host.length() - 1) == ']') {
            host = host.substring(1, host.length() - 1);
        }
        if (addr != null && addr.length == INADDRSZ) {
            return new Inet6Address(host, addr, scope_id);
        }
        throw new UnknownHostException("addr is of illegal length");
    }

    private void initstr(String hostName, byte[] addr, String ifname) throws UnknownHostException {
        try {
            NetworkInterface nif = NetworkInterface.getByName(ifname);
            if (nif == null) {
                throw new UnknownHostException("no such interface " + ifname);
            }
            initif(hostName, addr, nif);
        } catch (SocketException e) {
            throw new UnknownHostException("SocketException thrown" + ifname);
        }
    }

    private void initif(String hostName, byte[] addr, NetworkInterface nif) throws UnknownHostException {
        holder().hostName = hostName;
        if (addr.length == INADDRSZ) {
            holder().family = OsConstants.AF_INET6;
            this.ipaddress = (byte[]) addr.clone();
        }
        if (nif != null) {
            this.scope_ifname = nif;
            this.scope_ifname_set = true;
            this.scope_id = deriveNumericScope(nif);
            this.scope_id_set = true;
        }
    }

    private boolean differentLocalAddressTypes(Inet6Address other) {
        if (isLinkLocalAddress() && !other.isLinkLocalAddress()) {
            return false;
        }
        if (!isSiteLocalAddress() || other.isSiteLocalAddress()) {
            return true;
        }
        return false;
    }

    private int deriveNumericScope(NetworkInterface ifc) throws UnknownHostException {
        Enumeration<InetAddress> addresses = ifc.getInetAddresses();
        while (addresses.hasMoreElements()) {
            InetAddress addr = (InetAddress) addresses.nextElement();
            if (addr instanceof Inet6Address) {
                Inet6Address ia6_addr = (Inet6Address) addr;
                if (differentLocalAddressTypes(ia6_addr)) {
                    return ia6_addr.scope_id;
                }
            }
        }
        throw new UnknownHostException("no scope_id found");
    }

    private int deriveNumericScope(String ifname) throws UnknownHostException {
        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements()) {
                NetworkInterface ifc = (NetworkInterface) en.nextElement();
                if (ifc.getName().equals(ifname)) {
                    Enumeration addresses = ifc.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress addr = (InetAddress) addresses.nextElement();
                        if (addr instanceof Inet6Address) {
                            Inet6Address ia6_addr = (Inet6Address) addr;
                            if (differentLocalAddressTypes(ia6_addr)) {
                                return ia6_addr.scope_id;
                            }
                        }
                    }
                    continue;
                }
            }
            throw new UnknownHostException("No matching address found for interface : " + ifname);
        } catch (SocketException e) {
            throw new UnknownHostException("could not enumerate local network interfaces");
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        this.scope_ifname = null;
        this.scope_ifname_set = false;
        if (getClass().getClassLoader() != Class.class.getClassLoader()) {
            throw new SecurityException("invalid address type");
        }
        s.defaultReadObject();
        if (!(this.ifname == null || "".equals(this.ifname))) {
            try {
                this.scope_ifname = NetworkInterface.getByName(this.ifname);
                if (this.scope_ifname == null) {
                    this.scope_id_set = false;
                    this.scope_ifname_set = false;
                    this.scope_id = 0;
                } else {
                    try {
                        this.scope_id = deriveNumericScope(this.scope_ifname);
                    } catch (UnknownHostException e) {
                    }
                }
            } catch (SocketException e2) {
            }
        }
        this.ipaddress = (byte[]) this.ipaddress.clone();
        if (this.ipaddress.length != INADDRSZ) {
            throw new InvalidObjectException("invalid address length: " + this.ipaddress.length);
        } else if (holder().getFamily() != OsConstants.AF_INET6) {
            throw new InvalidObjectException("invalid address family type");
        }
    }

    public boolean isMulticastAddress() {
        return (this.ipaddress[0] & 255) == 255;
    }

    public boolean isAnyLocalAddress() {
        byte test = (byte) 0;
        for (int i = 0; i < INADDRSZ; i++) {
            test = (byte) (this.ipaddress[i] | test);
        }
        if (test == null) {
            return true;
        }
        return false;
    }

    public boolean isLoopbackAddress() {
        byte test = (byte) 0;
        for (int i = 0; i < 15; i++) {
            test = (byte) (this.ipaddress[i] | test);
        }
        if (test == null && this.ipaddress[15] == (byte) 1) {
            return true;
        }
        return false;
    }

    public boolean isLinkLocalAddress() {
        if ((this.ipaddress[0] & 255) == 254) {
            return (this.ipaddress[1] & 192) == Pattern.CANON_EQ;
        } else {
            return false;
        }
    }

    public boolean isSiteLocalAddress() {
        if ((this.ipaddress[0] & 255) == 254) {
            return (this.ipaddress[1] & 192) == 192;
        } else {
            return false;
        }
    }

    public boolean isMCGlobal() {
        if ((this.ipaddress[0] & 255) == 255) {
            return (this.ipaddress[1] & 15) == 14;
        } else {
            return false;
        }
    }

    public boolean isMCNodeLocal() {
        if ((this.ipaddress[0] & 255) == 255) {
            return (this.ipaddress[1] & 15) == 1;
        } else {
            return false;
        }
    }

    public boolean isMCLinkLocal() {
        if ((this.ipaddress[0] & 255) == 255) {
            return (this.ipaddress[1] & 15) == INT16SZ;
        } else {
            return false;
        }
    }

    public boolean isMCSiteLocal() {
        if ((this.ipaddress[0] & 255) == 255) {
            return (this.ipaddress[1] & 15) == 5;
        } else {
            return false;
        }
    }

    public boolean isMCOrgLocal() {
        if ((this.ipaddress[0] & 255) == 255) {
            return (this.ipaddress[1] & 15) == 8;
        } else {
            return false;
        }
    }

    public byte[] getAddress() {
        return (byte[]) this.ipaddress.clone();
    }

    public int getScopeId() {
        return this.scope_id;
    }

    public NetworkInterface getScopedInterface() {
        return this.scope_ifname;
    }

    public String getHostAddress() {
        return Libcore.os.getnameinfo(this, OsConstants.NI_NUMERICHOST);
    }

    public int hashCode() {
        if (this.ipaddress == null) {
            return 0;
        }
        int hash = 0;
        int i = 0;
        while (i < INADDRSZ) {
            int j = 0;
            int component = 0;
            while (j < 4 && i < INADDRSZ) {
                component = (component << 8) + this.ipaddress[i];
                j++;
                i++;
            }
            hash += component;
        }
        return hash;
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Inet6Address)) {
            return false;
        }
        Inet6Address inetAddr = (Inet6Address) obj;
        for (int i = 0; i < INADDRSZ; i++) {
            if (this.ipaddress[i] != inetAddr.ipaddress[i]) {
                return false;
            }
        }
        return true;
    }

    public boolean isIPv4CompatibleAddress() {
        return this.ipaddress[0] == null && this.ipaddress[1] == null && this.ipaddress[INT16SZ] == null && this.ipaddress[3] == null && this.ipaddress[4] == null && this.ipaddress[5] == null && this.ipaddress[6] == null && this.ipaddress[7] == null && this.ipaddress[8] == null && this.ipaddress[9] == null && this.ipaddress[10] == null && this.ipaddress[11] == null;
    }

    static String numericToTextFormat(byte[] src) {
        StringBuffer sb = new StringBuffer(39);
        for (int i = 0; i < 8; i++) {
            sb.append(Integer.toHexString(((src[i << 1] << 8) & 65280) | (src[(i << 1) + 1] & 255)));
            if (i < 7) {
                sb.append(":");
            }
        }
        return sb.toString();
    }

    private synchronized void writeObject(ObjectOutputStream s) throws IOException {
        if (this.scope_ifname_set) {
            this.ifname = this.scope_ifname.getName();
        }
        s.defaultWriteObject();
    }
}
