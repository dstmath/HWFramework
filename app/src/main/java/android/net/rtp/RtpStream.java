package android.net.rtp;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.SocketException;

public class RtpStream {
    private static final int MODE_LAST = 2;
    public static final int MODE_NORMAL = 0;
    public static final int MODE_RECEIVE_ONLY = 2;
    public static final int MODE_SEND_ONLY = 1;
    private final InetAddress mLocalAddress;
    private final int mLocalPort;
    private int mMode;
    private InetAddress mRemoteAddress;
    private int mRemotePort;
    private int mSocket;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.rtp.RtpStream.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.rtp.RtpStream.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.net.rtp.RtpStream.<clinit>():void");
    }

    private native void close();

    private native int create(String str) throws SocketException;

    RtpStream(InetAddress address) throws SocketException {
        this.mRemotePort = -1;
        this.mMode = MODE_NORMAL;
        this.mSocket = -1;
        this.mLocalPort = create(address.getHostAddress());
        this.mLocalAddress = address;
    }

    public InetAddress getLocalAddress() {
        return this.mLocalAddress;
    }

    public int getLocalPort() {
        return this.mLocalPort;
    }

    public InetAddress getRemoteAddress() {
        return this.mRemoteAddress;
    }

    public int getRemotePort() {
        return this.mRemotePort;
    }

    public boolean isBusy() {
        return false;
    }

    public int getMode() {
        return this.mMode;
    }

    public void setMode(int mode) {
        if (isBusy()) {
            throw new IllegalStateException("Busy");
        } else if (mode < 0 || mode > MODE_RECEIVE_ONLY) {
            throw new IllegalArgumentException("Invalid mode");
        } else {
            this.mMode = mode;
        }
    }

    public void associate(InetAddress address, int port) {
        boolean z = false;
        if (isBusy()) {
            throw new IllegalStateException("Busy");
        }
        if (address instanceof Inet4Address) {
            z = this.mLocalAddress instanceof Inet4Address;
        }
        if (!z && (!(address instanceof Inet6Address) || !(this.mLocalAddress instanceof Inet6Address))) {
            throw new IllegalArgumentException("Unsupported address");
        } else if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("Invalid port");
        } else {
            this.mRemoteAddress = address;
            this.mRemotePort = port;
        }
    }

    int getSocket() {
        return this.mSocket;
    }

    public void release() {
        synchronized (this) {
            if (isBusy()) {
                throw new IllegalStateException("Busy");
            }
            close();
        }
    }

    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }
}
