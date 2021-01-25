package ohos.bluetooth;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import ohos.ai.engine.pluginlabel.PluginLabelConstants;
import ohos.hiviewdfx.HiLogLabel;

public class SppServerSocket {
    private static final int SEC_FLAG_AUTH = 2;
    private static final int SEC_FLAG_ENCRYPT = 1;
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogHelper.BT_DOMAIN_ID, "SppServerSocket");
    private int mConnectionType;
    private FileDescriptor mFd;
    private boolean mIsAuthNeeded = false;
    private boolean mIsEncryptNeeded = false;
    private int mPort;
    private String mServiceName;
    private SppSocket mSppSocket;
    private UUID mUuid;

    public SppServerSocket(String str, int i, UUID uuid, int i2) {
        this.mServiceName = str;
        this.mConnectionType = i;
        this.mUuid = uuid;
        this.mPort = i2;
    }

    /* access modifiers changed from: package-private */
    public void setAuth(boolean z) {
        this.mIsAuthNeeded = z;
    }

    /* access modifiers changed from: package-private */
    public void setEncrypt(boolean z) {
        this.mIsEncryptNeeded = z;
    }

    private int getSecurityFlags() {
        int i = this.mIsAuthNeeded ? 2 : 0;
        return this.mIsEncryptNeeded ? i | 1 : i;
    }

    public SppClientSocket acceptSppServer() throws IOException {
        this.mSppSocket = new SppSocket(this.mConnectionType);
        this.mSppSocket.createSppSocket(this.mFd);
        return new SppClientSocket(this.mConnectionType, this.mUuid, this.mPort, this.mSppSocket);
    }

    public void closeSppServer() throws IOException {
        SppSocket sppSocket = this.mSppSocket;
        if (sppSocket != null) {
            sppSocket.close();
        }
    }

    public int getServerSocketPsmInfo() {
        return this.mPort;
    }

    public String getStringTag() {
        String str;
        String str2 = "";
        if (this.mServiceName != null) {
            str2 = (str2 + this.mServiceName) + " ";
        }
        int i = this.mConnectionType;
        if (i == 1) {
            str = str2 + "TYPE_RFCOMM";
        } else if (i == 2) {
            str = str2 + "TYPE_SCO";
        } else if (i == 3) {
            str = str2 + "TYPE_L2CAP";
        } else if (i != 4) {
            str = str2 + PluginLabelConstants.REMOTE_EXCEPTION_DEFAULT;
        } else {
            str = str2 + "TYPE_L2CAP_LE";
        }
        return str + ":" + this.mPort;
    }

    /* access modifiers changed from: package-private */
    public void bindListenSppServer() throws IOException {
        SppSocketProxy sppSocketProxy = new SppSocketProxy();
        if (this.mUuid == null) {
            this.mUuid = new UUID(0, 0);
        }
        Optional<FileDescriptor> sppCreateSocketServer = sppSocketProxy.sppCreateSocketServer(this.mServiceName, this.mConnectionType, this.mUuid, this.mPort, getSecurityFlags());
        if (sppCreateSocketServer.isPresent()) {
            this.mFd = sppCreateSocketServer.get();
            return;
        }
        throw new IOException("make spp server error");
    }

    /* access modifiers changed from: package-private */
    public boolean isFileDescriptorValid() {
        FileDescriptor fileDescriptor = this.mFd;
        if (fileDescriptor == null) {
            return false;
        }
        return fileDescriptor.valid();
    }
}
