package java.net;

public final class DatagramPacket {
    InetAddress address;
    byte[] buf;
    int bufLength;
    int length;
    int offset;
    int port;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.net.DatagramPacket.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.net.DatagramPacket.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.net.DatagramPacket.<clinit>():void");
    }

    private static native void init();

    public DatagramPacket(byte[] buf, int offset, int length) {
        setData(buf, offset, length);
        this.address = null;
        this.port = -1;
    }

    public DatagramPacket(byte[] buf, int length) {
        this(buf, 0, length);
    }

    public DatagramPacket(byte[] buf, int offset, int length, InetAddress address, int port) {
        setData(buf, offset, length);
        setAddress(address);
        setPort(port);
    }

    public DatagramPacket(byte[] buf, int offset, int length, SocketAddress address) throws SocketException {
        setData(buf, offset, length);
        setSocketAddress(address);
    }

    public DatagramPacket(byte[] buf, int length, InetAddress address, int port) {
        this(buf, 0, length, address, port);
    }

    public DatagramPacket(byte[] buf, int length, SocketAddress address) throws SocketException {
        this(buf, 0, length, address);
    }

    public synchronized InetAddress getAddress() {
        return this.address;
    }

    public synchronized int getPort() {
        return this.port;
    }

    public synchronized byte[] getData() {
        return this.buf;
    }

    public synchronized int getOffset() {
        return this.offset;
    }

    public synchronized int getLength() {
        return this.length;
    }

    public synchronized void setData(byte[] buf, int offset, int length) {
        if (length >= 0 && offset >= 0 && length + offset >= 0) {
            if (length + offset <= buf.length) {
                this.buf = buf;
                this.length = length;
                this.bufLength = length;
                this.offset = offset;
            }
        }
        throw new IllegalArgumentException("illegal length or offset");
    }

    public synchronized void setAddress(InetAddress iaddr) {
        this.address = iaddr;
    }

    public void setReceivedLength(int length) {
        this.length = length;
    }

    public synchronized void setPort(int iport) {
        if (iport < 0 || iport > 65535) {
            throw new IllegalArgumentException("Port out of range:" + iport);
        }
        this.port = iport;
    }

    public synchronized void setSocketAddress(SocketAddress address) {
        if (address != null) {
            if (address instanceof InetSocketAddress) {
                InetSocketAddress addr = (InetSocketAddress) address;
                if (addr.isUnresolved()) {
                    throw new IllegalArgumentException("unresolved address");
                }
                setAddress(addr.getAddress());
                setPort(addr.getPort());
            }
        }
        throw new IllegalArgumentException("unsupported address type");
    }

    public synchronized SocketAddress getSocketAddress() {
        return new InetSocketAddress(getAddress(), getPort());
    }

    public synchronized void setData(byte[] buf) {
        if (buf == null) {
            throw new NullPointerException("null packet buffer");
        }
        this.buf = buf;
        this.offset = 0;
        this.length = buf.length;
        this.bufLength = buf.length;
    }

    public synchronized void setLength(int length) {
        if (this.offset + length <= this.buf.length && length >= 0) {
            if (this.offset + length >= 0) {
                this.length = length;
                this.bufLength = this.length;
            }
        }
        throw new IllegalArgumentException("illegal length");
    }
}
