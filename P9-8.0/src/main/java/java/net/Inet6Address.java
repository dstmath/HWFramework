package java.net;

import android.system.OsConstants;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectInputStream.GetField;
import java.io.ObjectOutputStream;
import java.io.ObjectOutputStream.PutField;
import java.io.ObjectStreamField;
import java.util.Arrays;
import java.util.Enumeration;
import libcore.io.Libcore;
import sun.misc.Unsafe;

public final class Inet6Address extends InetAddress {
    public static final InetAddress ANY = new Inet6Address("::", new byte[]{(byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, 0);
    private static final long FIELDS_OFFSET;
    static final int INADDRSZ = 16;
    private static final int INT16SZ = 2;
    public static final InetAddress LOOPBACK = new Inet6Address("ip6-localhost", new byte[]{(byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 1}, 0);
    private static final Unsafe UNSAFE;
    private static final ObjectStreamField[] serialPersistentFields = new ObjectStreamField[]{new ObjectStreamField("ipaddress", byte[].class), new ObjectStreamField("scope_id", Integer.TYPE), new ObjectStreamField("scope_id_set", Boolean.TYPE), new ObjectStreamField("scope_ifname_set", Boolean.TYPE), new ObjectStreamField("ifname", String.class)};
    private static final long serialVersionUID = 6880410070516793377L;
    private final transient Inet6AddressHolder holder6 = new Inet6AddressHolder(this, null);

    private class Inet6AddressHolder {
        byte[] ipaddress;
        int scope_id;
        boolean scope_id_set;
        NetworkInterface scope_ifname;
        boolean scope_ifname_set;

        /* synthetic */ Inet6AddressHolder(Inet6Address this$0, Inet6AddressHolder -this1) {
            this();
        }

        /* synthetic */ Inet6AddressHolder(Inet6Address this$0, byte[] ipaddress, int scope_id, boolean scope_id_set, NetworkInterface ifname, boolean scope_ifname_set, Inet6AddressHolder -this6) {
            this(ipaddress, scope_id, scope_id_set, ifname, scope_ifname_set);
        }

        private Inet6AddressHolder() {
            this.ipaddress = new byte[16];
        }

        private Inet6AddressHolder(byte[] ipaddress, int scope_id, boolean scope_id_set, NetworkInterface ifname, boolean scope_ifname_set) {
            this.ipaddress = ipaddress;
            this.scope_id = scope_id;
            this.scope_id_set = scope_id_set;
            this.scope_ifname_set = scope_ifname_set;
            this.scope_ifname = ifname;
        }

        void setAddr(byte[] addr) {
            if (addr.length == 16) {
                System.arraycopy(addr, 0, this.ipaddress, 0, 16);
            }
        }

        void init(byte[] addr, int scope_id) {
            setAddr(addr);
            if (scope_id > 0) {
                this.scope_id = scope_id;
                this.scope_id_set = true;
            }
        }

        void init(byte[] addr, NetworkInterface nif) throws UnknownHostException {
            setAddr(addr);
            if (nif != null) {
                this.scope_id = Inet6Address.deriveNumericScope(this.ipaddress, nif);
                this.scope_id_set = true;
                this.scope_ifname = nif;
                this.scope_ifname_set = true;
            }
        }

        public boolean equals(Object o) {
            if (!(o instanceof Inet6AddressHolder)) {
                return false;
            }
            return Arrays.equals(this.ipaddress, ((Inet6AddressHolder) o).ipaddress);
        }

        public int hashCode() {
            if (this.ipaddress == null) {
                return 0;
            }
            int hash = 0;
            int i = 0;
            while (i < 16) {
                int j = 0;
                int component = 0;
                while (j < 4 && i < 16) {
                    component = (component << 8) + this.ipaddress[i];
                    j++;
                    i++;
                }
                hash += component;
            }
            return hash;
        }

        boolean isIPv4CompatibleAddress() {
            return this.ipaddress[0] == (byte) 0 && this.ipaddress[1] == (byte) 0 && this.ipaddress[2] == (byte) 0 && this.ipaddress[3] == (byte) 0 && this.ipaddress[4] == (byte) 0 && this.ipaddress[5] == (byte) 0 && this.ipaddress[6] == (byte) 0 && this.ipaddress[7] == (byte) 0 && this.ipaddress[8] == (byte) 0 && this.ipaddress[9] == (byte) 0 && this.ipaddress[10] == (byte) 0 && this.ipaddress[11] == (byte) 0;
        }

        boolean isMulticastAddress() {
            return (this.ipaddress[0] & 255) == 255;
        }

        boolean isAnyLocalAddress() {
            byte test = (byte) 0;
            for (int i = 0; i < 16; i++) {
                test = (byte) (this.ipaddress[i] | test);
            }
            if (test == (byte) 0) {
                return true;
            }
            return false;
        }

        boolean isLoopbackAddress() {
            byte test = (byte) 0;
            for (int i = 0; i < 15; i++) {
                test = (byte) (this.ipaddress[i] | test);
            }
            if (test == (byte) 0 && this.ipaddress[15] == (byte) 1) {
                return true;
            }
            return false;
        }

        boolean isLinkLocalAddress() {
            if ((this.ipaddress[0] & 255) == 254) {
                return (this.ipaddress[1] & 192) == 128;
            } else {
                return false;
            }
        }

        boolean isSiteLocalAddress() {
            if ((this.ipaddress[0] & 255) == 254) {
                return (this.ipaddress[1] & 192) == 192;
            } else {
                return false;
            }
        }

        boolean isMCGlobal() {
            if ((this.ipaddress[0] & 255) == 255) {
                return (this.ipaddress[1] & 15) == 14;
            } else {
                return false;
            }
        }

        boolean isMCNodeLocal() {
            if ((this.ipaddress[0] & 255) == 255) {
                return (this.ipaddress[1] & 15) == 1;
            } else {
                return false;
            }
        }

        boolean isMCLinkLocal() {
            if ((this.ipaddress[0] & 255) == 255) {
                return (this.ipaddress[1] & 15) == 2;
            } else {
                return false;
            }
        }

        boolean isMCSiteLocal() {
            if ((this.ipaddress[0] & 255) == 255) {
                return (this.ipaddress[1] & 15) == 5;
            } else {
                return false;
            }
        }

        boolean isMCOrgLocal() {
            if ((this.ipaddress[0] & 255) == 255) {
                return (this.ipaddress[1] & 15) == 8;
            } else {
                return false;
            }
        }
    }

    static {
        try {
            Unsafe unsafe = Unsafe.getUnsafe();
            FIELDS_OFFSET = unsafe.objectFieldOffset(Inet6Address.class.getDeclaredField("holder6"));
            UNSAFE = unsafe;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    Inet6Address() {
        this.holder.init(null, OsConstants.AF_INET6);
    }

    Inet6Address(String hostName, byte[] addr, int scope_id) {
        this.holder.init(hostName, OsConstants.AF_INET6);
        this.holder6.init(addr, scope_id);
    }

    Inet6Address(String hostName, byte[] addr) {
        try {
            initif(hostName, addr, null);
        } catch (UnknownHostException e) {
        }
    }

    Inet6Address(String hostName, byte[] addr, NetworkInterface nif) throws UnknownHostException {
        initif(hostName, addr, nif);
    }

    Inet6Address(String hostName, byte[] addr, String ifname) throws UnknownHostException {
        initstr(hostName, addr, ifname);
    }

    public static Inet6Address getByAddress(String host, byte[] addr, NetworkInterface nif) throws UnknownHostException {
        if (host != null && host.length() > 0 && host.charAt(0) == '[' && host.charAt(host.length() - 1) == ']') {
            host = host.substring(1, host.length() - 1);
        }
        if (addr != null && addr.length == 16) {
            return new Inet6Address(host, addr, nif);
        }
        throw new UnknownHostException("addr is of illegal length");
    }

    public static Inet6Address getByAddress(String host, byte[] addr, int scope_id) throws UnknownHostException {
        if (host != null && host.length() > 0 && host.charAt(0) == '[' && host.charAt(host.length() - 1) == ']') {
            host = host.substring(1, host.length() - 1);
        }
        if (addr != null && addr.length == 16) {
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
        int family = -1;
        this.holder6.init(addr, nif);
        if (addr.length == 16) {
            family = OsConstants.AF_INET6;
        }
        this.holder.init(hostName, family);
    }

    private static boolean isDifferentLocalAddressType(byte[] thisAddr, byte[] otherAddr) {
        if (isLinkLocalAddress(thisAddr) && (isLinkLocalAddress(otherAddr) ^ 1) != 0) {
            return false;
        }
        if (!isSiteLocalAddress(thisAddr) || (isSiteLocalAddress(otherAddr) ^ 1) == 0) {
            return true;
        }
        return false;
    }

    private static int deriveNumericScope(byte[] thisAddr, NetworkInterface ifc) throws UnknownHostException {
        Enumeration<InetAddress> addresses = ifc.getInetAddresses();
        while (addresses.hasMoreElements()) {
            InetAddress addr = (InetAddress) addresses.nextElement();
            if (addr instanceof Inet6Address) {
                Inet6Address ia6_addr = (Inet6Address) addr;
                if (isDifferentLocalAddressType(thisAddr, ia6_addr.getAddress())) {
                    return ia6_addr.getScopeId();
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
                    return deriveNumericScope(this.holder6.ipaddress, ifc);
                }
            }
            throw new UnknownHostException("No matching address found for interface : " + ifname);
        } catch (SocketException e) {
            throw new UnknownHostException("could not enumerate local network interfaces");
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        NetworkInterface scope_ifname = null;
        if (getClass().getClassLoader() != Class.class.getClassLoader()) {
            throw new SecurityException("invalid address type");
        }
        GetField gf = s.readFields();
        byte[] ipaddress = (byte[]) gf.get("ipaddress", null);
        int scope_id = gf.get("scope_id", -1);
        boolean scope_id_set = gf.get("scope_id_set", false);
        boolean scope_ifname_set = gf.get("scope_ifname_set", false);
        String ifname = (String) gf.get("ifname", null);
        if (!(ifname == null || ("".equals(ifname) ^ 1) == 0)) {
            try {
                scope_ifname = NetworkInterface.getByName(ifname);
                if (scope_ifname == null) {
                    scope_id_set = false;
                    scope_ifname_set = false;
                    scope_id = 0;
                } else {
                    scope_ifname_set = true;
                    try {
                        scope_id = deriveNumericScope(ipaddress, scope_ifname);
                    } catch (UnknownHostException e) {
                    }
                }
            } catch (SocketException e2) {
            }
        }
        ipaddress = (byte[]) ipaddress.clone();
        if (ipaddress.length != 16) {
            throw new InvalidObjectException("invalid address length: " + ipaddress.length);
        } else if (holder().getFamily() != OsConstants.AF_INET6) {
            throw new InvalidObjectException("invalid address family type");
        } else {
            UNSAFE.putObject(this, FIELDS_OFFSET, new Inet6AddressHolder(this, ipaddress, scope_id, scope_id_set, scope_ifname, scope_ifname_set, null));
        }
    }

    private synchronized void writeObject(ObjectOutputStream s) throws IOException {
        Object ifname = null;
        if (this.holder6.scope_ifname != null) {
            ifname = this.holder6.scope_ifname.getName();
            this.holder6.scope_ifname_set = true;
        }
        PutField pfields = s.putFields();
        pfields.put("ipaddress", this.holder6.ipaddress);
        pfields.put("scope_id", this.holder6.scope_id);
        pfields.put("scope_id_set", this.holder6.scope_id_set);
        pfields.put("scope_ifname_set", this.holder6.scope_ifname_set);
        pfields.put("ifname", ifname);
        s.writeFields();
    }

    public boolean isMulticastAddress() {
        return this.holder6.isMulticastAddress();
    }

    public boolean isAnyLocalAddress() {
        return this.holder6.isAnyLocalAddress();
    }

    public boolean isLoopbackAddress() {
        return this.holder6.isLoopbackAddress();
    }

    public boolean isLinkLocalAddress() {
        return this.holder6.isLinkLocalAddress();
    }

    static boolean isLinkLocalAddress(byte[] ipaddress) {
        if ((ipaddress[0] & 255) == 254) {
            return (ipaddress[1] & 192) == 128;
        } else {
            return false;
        }
    }

    public boolean isSiteLocalAddress() {
        return this.holder6.isSiteLocalAddress();
    }

    static boolean isSiteLocalAddress(byte[] ipaddress) {
        if ((ipaddress[0] & 255) == 254) {
            return (ipaddress[1] & 192) == 192;
        } else {
            return false;
        }
    }

    public boolean isMCGlobal() {
        return this.holder6.isMCGlobal();
    }

    public boolean isMCNodeLocal() {
        return this.holder6.isMCNodeLocal();
    }

    public boolean isMCLinkLocal() {
        return this.holder6.isMCLinkLocal();
    }

    public boolean isMCSiteLocal() {
        return this.holder6.isMCSiteLocal();
    }

    public boolean isMCOrgLocal() {
        return this.holder6.isMCOrgLocal();
    }

    public byte[] getAddress() {
        return (byte[]) this.holder6.ipaddress.clone();
    }

    public int getScopeId() {
        return this.holder6.scope_id;
    }

    public NetworkInterface getScopedInterface() {
        return this.holder6.scope_ifname;
    }

    public String getHostAddress() {
        return Libcore.os.getnameinfo(this, OsConstants.NI_NUMERICHOST);
    }

    public int hashCode() {
        return this.holder6.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj == null || ((obj instanceof Inet6Address) ^ 1) != 0) {
            return false;
        }
        return this.holder6.equals(((Inet6Address) obj).holder6);
    }

    public boolean isIPv4CompatibleAddress() {
        return this.holder6.isIPv4CompatibleAddress();
    }

    static String numericToTextFormat(byte[] src) {
        StringBuilder sb = new StringBuilder(39);
        for (int i = 0; i < 8; i++) {
            sb.append(Integer.toHexString(((src[i << 1] << 8) & 65280) | (src[(i << 1) + 1] & 255)));
            if (i < 7) {
                sb.append(":");
            }
        }
        return sb.toString();
    }
}
