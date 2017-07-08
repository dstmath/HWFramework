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
    private static final long FIELDS_OFFSET = 0;
    private static final Unsafe UNSAFE = null;
    private static final ObjectStreamField[] serialPersistentFields = null;
    private static final long serialVersionUID = 5076001401234631237L;
    private final transient InetSocketAddressHolder holder;

    private static class InetSocketAddressHolder {
        private InetAddress addr;
        private String hostname;
        private int port;

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
            if (obj == null || !(obj instanceof InetSocketAddressHolder)) {
                return false;
            }
            InetSocketAddressHolder that = (InetSocketAddressHolder) obj;
            boolean equals = this.addr != null ? this.addr.equals(that.addr) : this.hostname != null ? that.addr == null ? this.hostname.equalsIgnoreCase(that.hostname) : false : that.addr == null && that.hostname == null;
            if (equals && this.port == that.port) {
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.net.InetSocketAddress.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.net.InetSocketAddress.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.net.InetSocketAddress.<clinit>():void");
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
        this.holder = new InetSocketAddressHolder(null, 0, null);
    }

    public InetSocketAddress(int port) {
        this((InetAddress) null, port);
    }

    public InetSocketAddress(InetAddress addr, int port) {
        if (addr == null) {
            addr = Inet6Address.ANY;
        }
        this.holder = new InetSocketAddressHolder(addr, checkPort(port), null);
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
        this.holder = new InetSocketAddressHolder(addr, checkPort(port), null);
    }

    private InetSocketAddress(int port, String hostname) {
        this.holder = new InetSocketAddressHolder(null, port, null);
    }

    public static InetSocketAddress createUnresolved(String host, int port) {
        return new InetSocketAddress(checkPort(port), checkHost(host));
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
        UNSAFE.putObject(this, FIELDS_OFFSET, new InetSocketAddressHolder(oisAddr, oisPort, null));
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
        if (obj == null || !(obj instanceof InetSocketAddress)) {
            return false;
        }
        return this.holder.equals(((InetSocketAddress) obj).holder);
    }

    public final int hashCode() {
        return this.holder.hashCode();
    }
}
