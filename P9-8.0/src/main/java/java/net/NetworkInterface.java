package java.net;

import android.system.ErrnoException;
import android.system.OsConstants;
import android.system.StructIfaddrs;
import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import libcore.io.IoUtils;
import libcore.io.Libcore;

public final class NetworkInterface {
    private static final int defaultIndex;
    private static final NetworkInterface defaultInterface = DefaultInterface.getDefault();
    private InetAddress[] addrs;
    private InterfaceAddress[] bindings;
    private List<NetworkInterface> childs;
    private String displayName;
    private byte[] hardwareAddr;
    private int index;
    private String name;
    private NetworkInterface parent = null;
    private boolean virtual = false;

    static {
        if (defaultInterface != null) {
            defaultIndex = defaultInterface.getIndex();
        } else {
            defaultIndex = 0;
        }
    }

    NetworkInterface() {
    }

    NetworkInterface(String name, int index, InetAddress[] addrs) {
        this.name = name;
        this.index = index;
        this.addrs = addrs;
    }

    public String getName() {
        return this.name;
    }

    public Enumeration<InetAddress> getInetAddresses() {
        return new Enumeration<InetAddress>() {
            private int count = 0;
            private int i = 0;
            private InetAddress[] local_addrs;

            {
                this.local_addrs = new InetAddress[NetworkInterface.this.addrs.length];
                boolean trusted = true;
                SecurityManager sec = System.getSecurityManager();
                if (sec != null) {
                    try {
                        sec.checkPermission(new NetPermission("getNetworkInformation"));
                    } catch (SecurityException e) {
                        trusted = false;
                    }
                }
                for (int j = 0; j < NetworkInterface.this.addrs.length; j++) {
                    if (!(sec == null || (trusted ^ 1) == 0)) {
                        try {
                            sec.checkConnect(NetworkInterface.this.addrs[j].getHostAddress(), -1);
                        } catch (SecurityException e2) {
                        }
                    }
                    InetAddress[] inetAddressArr = this.local_addrs;
                    int i = this.count;
                    this.count = i + 1;
                    inetAddressArr[i] = NetworkInterface.this.addrs[j];
                }
            }

            public InetAddress nextElement() {
                if (this.i < this.count) {
                    InetAddress[] inetAddressArr = this.local_addrs;
                    int i = this.i;
                    this.i = i + 1;
                    return inetAddressArr[i];
                }
                throw new NoSuchElementException();
            }

            public boolean hasMoreElements() {
                return this.i < this.count;
            }
        };
    }

    public List<InterfaceAddress> getInterfaceAddresses() {
        List<InterfaceAddress> lst = new ArrayList(1);
        if (this.bindings != null) {
            SecurityManager sec = System.getSecurityManager();
            for (int j = 0; j < this.bindings.length; j++) {
                if (sec != null) {
                    try {
                        sec.checkConnect(this.bindings[j].getAddress().getHostAddress(), -1);
                    } catch (SecurityException e) {
                    }
                }
                lst.add(this.bindings[j]);
            }
        }
        return lst;
    }

    public Enumeration<NetworkInterface> getSubInterfaces() {
        return Collections.enumeration(this.childs);
    }

    public NetworkInterface getParent() {
        return this.parent;
    }

    public int getIndex() {
        return this.index;
    }

    public String getDisplayName() {
        return "".equals(this.displayName) ? null : this.displayName;
    }

    public static NetworkInterface getByName(String name) throws SocketException {
        if (name == null) {
            throw new NullPointerException();
        }
        for (NetworkInterface ni : getAll()) {
            if (ni.getName().equals(name)) {
                return ni;
            }
        }
        return null;
    }

    public static NetworkInterface getByIndex(int index) throws SocketException {
        if (index < 0) {
            throw new IllegalArgumentException("Interface index can't be negative");
        }
        for (NetworkInterface ni : getAll()) {
            if (ni.getIndex() == index) {
                return ni;
            }
        }
        return null;
    }

    public static NetworkInterface getByInetAddress(InetAddress addr) throws SocketException {
        if (addr == null) {
            throw new NullPointerException();
        }
        if (!(addr instanceof Inet4Address) ? addr instanceof Inet6Address : true) {
            for (NetworkInterface ni : getAll()) {
                for (InetAddress inetAddress : Collections.list(ni.getInetAddresses())) {
                    if (inetAddress.equals(addr)) {
                        return ni;
                    }
                }
            }
            return null;
        }
        throw new IllegalArgumentException("invalid address type");
    }

    public static Enumeration<NetworkInterface> getNetworkInterfaces() throws SocketException {
        NetworkInterface[] netifs = getAll();
        if (netifs.length == 0) {
            return null;
        }
        return Collections.enumeration(Arrays.asList(netifs));
    }

    private static NetworkInterface[] getAll() throws SocketException {
        Map<String, List<StructIfaddrs>> inetMap = new HashMap();
        try {
            String name;
            for (StructIfaddrs ifa : Libcore.os.getifaddrs()) {
                name = ifa.ifa_name;
                List<StructIfaddrs> ifas = (List) inetMap.get(name);
                if (ifas == null) {
                    ifas = new ArrayList();
                    inetMap.put(name, ifas);
                }
                ifas.add(ifa);
            }
            Map<String, NetworkInterface> hashMap = new HashMap(inetMap.size());
            for (Entry<String, List<StructIfaddrs>> e : inetMap.entrySet()) {
                name = (String) e.getKey();
                int index = Libcore.os.if_nametoindex((String) e.getKey());
                if (index != 0) {
                    NetworkInterface networkInterface = new NetworkInterface(name, index, null);
                    networkInterface.displayName = name;
                    List<InetAddress> addrs = new ArrayList();
                    List<InterfaceAddress> binds = new ArrayList();
                    for (StructIfaddrs ifa2 : (List) e.getValue()) {
                        if (ifa2.ifa_addr != null) {
                            addrs.add(ifa2.ifa_addr);
                            binds.add(new InterfaceAddress(ifa2.ifa_addr, (Inet4Address) ifa2.ifa_broadaddr, ifa2.ifa_netmask));
                        }
                        if (ifa2.hwaddr != null) {
                            networkInterface.hardwareAddr = ifa2.hwaddr;
                        }
                    }
                    networkInterface.addrs = (InetAddress[]) addrs.toArray(new InetAddress[addrs.size()]);
                    networkInterface.bindings = (InterfaceAddress[]) binds.toArray(new InterfaceAddress[binds.size()]);
                    networkInterface.childs = new ArrayList(0);
                    hashMap.put(name, networkInterface);
                }
            }
            for (Entry<String, NetworkInterface> e2 : hashMap.entrySet()) {
                NetworkInterface ni = (NetworkInterface) e2.getValue();
                String niName = ni.getName();
                int colonIdx = niName.indexOf(58);
                if (colonIdx != -1) {
                    NetworkInterface parent = (NetworkInterface) hashMap.get(niName.substring(0, colonIdx));
                    ni.virtual = true;
                    ni.parent = parent;
                    parent.childs.add(ni);
                }
            }
            return (NetworkInterface[]) hashMap.values().toArray(new NetworkInterface[hashMap.size()]);
        } catch (ErrnoException e3) {
            throw e3.rethrowAsSocketException();
        }
    }

    public boolean isUp() throws SocketException {
        return (getFlags() & OsConstants.IFF_UP) != 0;
    }

    public boolean isLoopback() throws SocketException {
        return (getFlags() & OsConstants.IFF_LOOPBACK) != 0;
    }

    public boolean isPointToPoint() throws SocketException {
        return (getFlags() & OsConstants.IFF_POINTOPOINT) != 0;
    }

    public boolean supportsMulticast() throws SocketException {
        return (getFlags() & OsConstants.IFF_MULTICAST) != 0;
    }

    public byte[] getHardwareAddress() throws SocketException {
        NetworkInterface ni = getByName(this.name);
        if (ni != null) {
            return ni.hardwareAddr;
        }
        throw new SocketException("NetworkInterface doesn't exist anymore");
    }

    public int getMTU() throws SocketException {
        FileDescriptor fileDescriptor = null;
        try {
            fileDescriptor = Libcore.rawOs.socket(OsConstants.AF_INET, OsConstants.SOCK_DGRAM, 0);
            int ioctlMTU = Libcore.rawOs.ioctlMTU(fileDescriptor, this.name);
            IoUtils.closeQuietly(fileDescriptor);
            return ioctlMTU;
        } catch (ErrnoException e) {
            throw e.rethrowAsSocketException();
        } catch (Throwable ex) {
            throw new SocketException(ex);
        } catch (Throwable th) {
            IoUtils.closeQuietly(fileDescriptor);
        }
    }

    public boolean isVirtual() {
        return this.virtual;
    }

    private int getFlags() throws SocketException {
        FileDescriptor fileDescriptor = null;
        try {
            fileDescriptor = Libcore.rawOs.socket(OsConstants.AF_INET, OsConstants.SOCK_DGRAM, 0);
            int ioctlFlags = Libcore.rawOs.ioctlFlags(fileDescriptor, this.name);
            IoUtils.closeQuietly(fileDescriptor);
            return ioctlFlags;
        } catch (ErrnoException e) {
            throw e.rethrowAsSocketException();
        } catch (Throwable ex) {
            throw new SocketException(ex);
        } catch (Throwable th) {
            IoUtils.closeQuietly(fileDescriptor);
        }
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (!(obj instanceof NetworkInterface)) {
            return false;
        }
        NetworkInterface that = (NetworkInterface) obj;
        if (this.name != null) {
            if (!this.name.equals(that.name)) {
                return false;
            }
        } else if (that.name != null) {
            return false;
        }
        if (this.addrs == null) {
            if (that.addrs != null) {
                z = false;
            }
            return z;
        } else if (that.addrs == null || this.addrs.length != that.addrs.length) {
            return false;
        } else {
            for (int i = 0; i < count; i++) {
                boolean found = false;
                for (Object equals : that.addrs) {
                    if (this.addrs[i].equals(equals)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    return false;
                }
            }
            return true;
        }
    }

    public int hashCode() {
        return this.name == null ? 0 : this.name.hashCode();
    }

    public String toString() {
        String result = "name:" + (this.name == null ? "null" : this.name);
        if (this.displayName != null) {
            return result + " (" + this.displayName + ")";
        }
        return result;
    }

    static NetworkInterface getDefault() {
        return defaultInterface;
    }
}
