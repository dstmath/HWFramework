package java.net;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectInputStream.GetField;
import java.io.ObjectOutputStream;
import java.io.ObjectOutputStream.PutField;
import java.io.ObjectStreamException;
import java.io.ObjectStreamField;
import sun.misc.Unsafe;

public class InetSocketAddress extends SocketAddress {
    private static final long FIELDS_OFFSET;
    private static final Unsafe UNSAFE;
    private static final ObjectStreamField[] serialPersistentFields = new ObjectStreamField[]{new ObjectStreamField("hostname", String.class), new ObjectStreamField("addr", InetAddress.class), new ObjectStreamField("port", Integer.TYPE)};
    private static final long serialVersionUID = 5076001401234631237L;
    private final transient InetSocketAddressHolder holder;

    private static class InetSocketAddressHolder {
        private InetAddress addr;
        private String hostname;
        private int port;

        /* synthetic */ InetSocketAddressHolder(String hostname, InetAddress addr, int port, InetSocketAddressHolder -this3) {
            this(hostname, addr, port);
        }

        private InetSocketAddressHolder(String hostname, InetAddress addr, int port) {
            this.hostname = hostname;
            this.addr = addr;
            this.port = port;
        }

        private int getPort() {
            return this.port;
        }

        private InetAddress getAddress() {
            return this.addr;
        }

        private String getHostName() {
            if (this.hostname != null) {
                return this.hostname;
            }
            if (this.addr != null) {
                return this.addr.getHostName();
            }
            return null;
        }

        private String getHostString() {
            if (this.hostname != null) {
                return this.hostname;
            }
            if (this.addr == null) {
                return null;
            }
            if (this.addr.holder().getHostName() != null) {
                return this.addr.holder().getHostName();
            }
            return this.addr.getHostAddress();
        }

        private boolean isUnresolved() {
            return this.addr == null;
        }

        public String toString() {
            if (isUnresolved()) {
                return this.hostname + ":" + this.port;
            }
            return this.addr.toString() + ":" + this.port;
        }

        public final boolean equals(Object obj) {
            boolean z = false;
            if (obj == null || ((obj instanceof InetSocketAddressHolder) ^ 1) != 0) {
                return false;
            }
            InetSocketAddressHolder that = (InetSocketAddressHolder) obj;
            boolean sameIP = this.addr != null ? this.addr.equals(that.addr) : this.hostname != null ? that.addr == null ? this.hostname.equalsIgnoreCase(that.hostname) : false : that.addr == null && that.hostname == null;
            if (sameIP && this.port == that.port) {
                z = true;
            }
            return z;
        }

        public final int hashCode() {
            if (this.addr != null) {
                return this.addr.hashCode() + this.port;
            }
            if (this.hostname != null) {
                return this.hostname.toLowerCase().hashCode() + this.port;
            }
            return this.port;
        }
    }

    private static int checkPort(int port) {
        if (port >= 0 && port <= 65535) {
            return port;
        }
        throw new IllegalArgumentException("port out of range:" + port);
    }

    private static String checkHost(String hostname) {
        if (hostname != null) {
            return hostname;
        }
        throw new IllegalArgumentException("hostname can't be null");
    }

    public InetSocketAddress() {
        this.holder = new InetSocketAddressHolder(null, null, 0, null);
    }

    public InetSocketAddress(int port) {
        this((InetAddress) null, port);
    }

    public InetSocketAddress(InetAddress addr, int port) {
        if (addr == null) {
            addr = Inet6Address.ANY;
        }
        this.holder = new InetSocketAddressHolder(null, addr, checkPort(port), null);
    }

    public InetSocketAddress(String hostname, int port) {
        checkHost(hostname);
        InetAddress addr = null;
        String host = null;
        try {
            addr = InetAddress.getByName(hostname);
        } catch (UnknownHostException e) {
            host = hostname;
        }
        this.holder = new InetSocketAddressHolder(host, addr, checkPort(port), null);
    }

    private InetSocketAddress(int port, String hostname) {
        this.holder = new InetSocketAddressHolder(hostname, null, port, null);
    }

    public static InetSocketAddress createUnresolved(String host, int port) {
        return new InetSocketAddress(checkPort(port), checkHost(host));
    }

    static {
        try {
            Unsafe unsafe = Unsafe.getUnsafe();
            FIELDS_OFFSET = unsafe.objectFieldOffset(InetSocketAddress.class.getDeclaredField("holder"));
            UNSAFE = unsafe;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        PutField pfields = out.putFields();
        pfields.put("hostname", this.holder.hostname);
        pfields.put("addr", this.holder.addr);
        pfields.put("port", this.holder.port);
        out.writeFields();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        GetField oisFields = in.readFields();
        String oisHostname = (String) oisFields.get("hostname", null);
        InetAddress oisAddr = (InetAddress) oisFields.get("addr", null);
        int oisPort = oisFields.get("port", -1);
        checkPort(oisPort);
        if (oisHostname == null && oisAddr == null) {
            throw new InvalidObjectException("hostname and addr can't both be null");
        }
        UNSAFE.putObject(this, FIELDS_OFFSET, new InetSocketAddressHolder(oisHostname, oisAddr, oisPort, null));
    }

    private void readObjectNoData() throws ObjectStreamException {
        throw new InvalidObjectException("Stream data required");
    }

    public final int getPort() {
        return this.holder.getPort();
    }

    public final InetAddress getAddress() {
        return this.holder.getAddress();
    }

    public final String getHostName() {
        return this.holder.getHostName();
    }

    public final String getHostString() {
        return this.holder.getHostString();
    }

    public final boolean isUnresolved() {
        return this.holder.isUnresolved();
    }

    public String toString() {
        return this.holder.toString();
    }

    public final boolean equals(Object obj) {
        if (obj == null || ((obj instanceof InetSocketAddress) ^ 1) != 0) {
            return false;
        }
        return this.holder.equals(((InetSocketAddress) obj).holder);
    }

    public final int hashCode() {
        return this.holder.hashCode();
    }
}
