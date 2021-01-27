package ohos.bluetooth;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import java.util.UUID;
import ohos.hiviewdfx.HiLogLabel;

public class SppClientSocket {
    public static final int INVALID_PACKET_SIZE_VALUE = -1;
    private static final int PARAM_LEN = 20;
    private static final int PORT_LEN = 4;
    private static final int SHORT_MASK = 65535;
    public static final int SOCKET_L2CAP = 3;
    public static final int SOCKET_L2CAP_BREDR = 3;
    public static final int SOCKET_L2CAP_LE = 4;
    public static final int SOCKET_RFCOMM = 1;
    public static final int SOCKET_SCO = 2;
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogHelper.BT_DOMAIN_ID, "SppClientSocket");
    private int mConnectionType;
    private BluetoothRemoteDevice mDevice;
    private boolean mIsAuthNeeded = false;
    private boolean mIsEncryptNeeded = false;
    private boolean mIsSocketConnected;
    private int mPort;
    private SppSocket mSppSocket;
    private UUID mUuid;

    public SppClientSocket(BluetoothRemoteDevice bluetoothRemoteDevice, int i, UUID uuid, int i2) {
        this.mConnectionType = i;
        this.mDevice = bluetoothRemoteDevice;
        this.mUuid = uuid;
        this.mPort = i2;
        this.mIsSocketConnected = false;
    }

    SppClientSocket(int i, UUID uuid, int i2, SppSocket sppSocket) {
        this.mConnectionType = i;
        this.mUuid = uuid;
        this.mPort = i2;
        this.mIsSocketConnected = true;
        this.mSppSocket = sppSocket;
    }

    /* access modifiers changed from: package-private */
    public void setAuth(boolean z) {
        this.mIsAuthNeeded = z;
    }

    /* access modifiers changed from: package-private */
    public void setEncrypt(boolean z) {
        this.mIsEncryptNeeded = z;
    }

    public void connectSppClient() throws IOException {
        if (this.mDevice != null) {
            SppSocketProxy sppSocketProxy = new SppSocketProxy();
            if (this.mUuid == null) {
                this.mUuid = new UUID(0, 0);
            }
            Optional<FileDescriptor> sppConnectSocket = sppSocketProxy.sppConnectSocket(this.mDevice, this.mConnectionType, this.mUuid, this.mPort, 0);
            if (sppConnectSocket.isPresent()) {
                this.mSppSocket = new SppSocket(this.mConnectionType);
                this.mSppSocket.createSppSocket(sppConnectSocket.get());
                this.mIsSocketConnected = true;
                return;
            }
            throw new IOException("connect remote device error");
        }
        throw new IOException("No remote device");
    }

    public void closeSppClient() {
        this.mIsSocketConnected = false;
    }

    public int getSppClientConnectionType() {
        return this.mConnectionType;
    }

    public InputStream getSppClientInputStream() {
        SppSocket sppSocket = this.mSppSocket;
        if (sppSocket == null) {
            return null;
        }
        return sppSocket.getInputStream();
    }

    public OutputStream getSppClientOutputStream() {
        SppSocket sppSocket = this.mSppSocket;
        if (sppSocket == null) {
            return null;
        }
        return sppSocket.getOutputStream();
    }

    public int getSppClientMaxTransmitPacketSize() {
        SppSocket sppSocket = this.mSppSocket;
        if (sppSocket == null) {
            return -1;
        }
        return sppSocket.getMaxTransmitPacketSize();
    }

    public int getSppClientMaxReceivePacketSize() {
        SppSocket sppSocket = this.mSppSocket;
        if (sppSocket == null) {
            return -1;
        }
        return sppSocket.getMaxReceivePacketSize();
    }

    public BluetoothRemoteDevice getSppClientRemoteDevice() {
        return this.mDevice;
    }

    public boolean isSppClientConnected() {
        return this.mIsSocketConnected;
    }
}
