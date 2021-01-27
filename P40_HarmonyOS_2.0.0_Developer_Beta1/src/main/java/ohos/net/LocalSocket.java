package ohos.net;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class LocalSocket {
    private static final HiLogLabel LOG_LABEL = new HiLogLabel(3, 218109360, "LocalSocket");
    private final LocalSocketImplAdapter mImpl;
    private boolean mIsBound;
    private boolean mIsConnected;

    public LocalSocket(String str, boolean z) throws IOException {
        this(new LocalSocketImplAdapter(), str, z, false);
    }

    private LocalSocket(LocalSocketImplAdapter localSocketImplAdapter, String str, boolean z, boolean z2) throws IOException {
        if (z2) {
            this.mImpl = localSocketImplAdapter;
            this.mIsConnected = false;
            this.mIsBound = false;
        } else if (str == null || str.equals("")) {
            throw new IllegalArgumentException("LocalSocket name can't be empty");
        } else {
            this.mImpl = localSocketImplAdapter;
            this.mImpl.createSocket(str, z);
            if (z) {
                this.mIsBound = true;
            }
            HiLog.debug(LOG_LABEL, "LocalSocket constructor, name = %s, isServer = %s", str, String.valueOf(z));
        }
    }

    public synchronized void connectSocket() throws IOException {
        if (!this.mIsConnected) {
            this.mImpl.connectSocket();
            this.mIsConnected = true;
            this.mIsBound = true;
        } else {
            throw new IOException("socket already connected");
        }
    }

    public LocalSocket acceptSocket() throws IOException {
        LocalSocket localSocket = new LocalSocket(this.mImpl.acceptSocket(), null, false, true);
        localSocket.mIsConnected = true;
        localSocket.mIsBound = true;
        return localSocket;
    }

    public synchronized void closeSocket() throws IOException {
        this.mImpl.closeSocket();
        this.mIsConnected = false;
    }

    public InputStream getInputStream() throws IOException {
        return this.mImpl.getInputStream();
    }

    public OutputStream getOutputStream() throws IOException {
        return this.mImpl.getOutputStream();
    }

    public void closeInputStream() throws IOException {
        this.mImpl.closeInputStream();
    }

    public void closeOutputStream() throws IOException {
        this.mImpl.closeOutputStream();
    }

    public FileDescriptor getSocketFd() {
        return this.mImpl.getSocketFd();
    }

    public String getSocketName() {
        return this.mImpl.getSocketName();
    }

    public SocketCertificates getPeerSocketInfo() throws IOException {
        return this.mImpl.getPeerSocketInfo();
    }

    public synchronized boolean isSocketConnected() {
        return this.mIsConnected;
    }

    public void setSocketReadTimeout(int i) throws IOException {
        this.mImpl.setSocketReadTimeout(i);
    }
}
