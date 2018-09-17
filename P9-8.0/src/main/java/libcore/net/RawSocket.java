package libcore.net;

import android.icu.text.DateTimePatternGenerator;
import dalvik.system.CloseGuard;
import java.io.Closeable;
import java.io.FileDescriptor;
import java.io.IOException;
import java.net.SocketException;
import java.util.Arrays;
import libcore.io.IoBridge;

public class RawSocket implements Closeable {
    public static final short ETH_P_ARP = (short) 2054;
    public static final short ETH_P_IP = (short) 2048;
    private final FileDescriptor fd;
    private final CloseGuard guard = CloseGuard.get();
    private final String mInterfaceName;
    private final short mProtocolType;

    private static native void create(FileDescriptor fileDescriptor, short s, String str) throws SocketException;

    private static native int recvPacket(FileDescriptor fileDescriptor, byte[] bArr, int i, int i2, int i3, int i4);

    private static native int sendPacket(FileDescriptor fileDescriptor, String str, short s, byte[] bArr, byte[] bArr2, int i, int i2);

    public RawSocket(String interfaceName, short protocolType) throws SocketException {
        this.mInterfaceName = interfaceName;
        this.mProtocolType = protocolType;
        this.fd = new FileDescriptor();
        create(this.fd, this.mProtocolType, this.mInterfaceName);
        this.guard.open("close");
    }

    public int read(byte[] packet, int offset, int byteCount, int destPort, int timeoutMillis) {
        if (packet == null) {
            throw new NullPointerException("packet == null");
        }
        Arrays.checkOffsetAndCount(packet.length, offset, byteCount);
        if (destPort <= DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH) {
            return recvPacket(this.fd, packet, offset, byteCount, destPort, timeoutMillis);
        }
        throw new IllegalArgumentException("Port out of range: " + destPort);
    }

    public int write(byte[] destMac, byte[] packet, int offset, int byteCount) {
        if (destMac == null) {
            throw new NullPointerException("destMac == null");
        } else if (packet == null) {
            throw new NullPointerException("packet == null");
        } else {
            Arrays.checkOffsetAndCount(packet.length, offset, byteCount);
            if (destMac.length == 6) {
                return sendPacket(this.fd, this.mInterfaceName, this.mProtocolType, destMac, packet, offset, byteCount);
            }
            throw new IllegalArgumentException("MAC length must be 6: " + destMac.length);
        }
    }

    public void close() throws IOException {
        this.guard.close();
        IoBridge.closeAndSignalBlockedThreads(this.fd);
    }

    protected void finalize() throws Throwable {
        try {
            if (this.guard != null) {
                this.guard.warnIfOpen();
            }
            close();
        } finally {
            super.finalize();
        }
    }
}
