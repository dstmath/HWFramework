package ohos.net;

import android.net.Credentials;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import ohos.hiviewdfx.HiLogLabel;

class LocalSocketImplAdapter {
    private static final HiLogLabel LOG_LABLE = new HiLogLabel(3, 218109360, "LocalSocketImplAdapter");
    private static final int SOCKET_DGRAM = 1;
    private static final int SOCKET_STREAM = 2;
    private LocalSocket mClientSocket;
    private boolean mIsServerSocket;
    private LocalServerSocket mServerSocket;
    private String mSocketName;

    LocalSocketImplAdapter() {
    }

    /* access modifiers changed from: package-private */
    public void createSocket(String str, boolean z) throws IOException {
        this.mSocketName = str;
        if (z) {
            this.mServerSocket = new LocalServerSocket(str);
            this.mIsServerSocket = true;
            return;
        }
        this.mClientSocket = new LocalSocket(2);
        this.mIsServerSocket = false;
    }

    /* access modifiers changed from: package-private */
    public void connectSocket() throws IOException {
        LocalSocket localSocket = this.mClientSocket;
        if (localSocket != null) {
            localSocket.connect(new LocalSocketAddress(this.mSocketName));
            return;
        }
        throw new IOException("connectSocket, Client socket not created");
    }

    /* access modifiers changed from: package-private */
    public LocalSocketImplAdapter acceptSocket() throws IOException {
        if (this.mServerSocket != null) {
            LocalSocketImplAdapter localSocketImplAdapter = new LocalSocketImplAdapter();
            localSocketImplAdapter.mClientSocket = this.mServerSocket.accept();
            localSocketImplAdapter.mSocketName = this.mSocketName;
            localSocketImplAdapter.mIsServerSocket = false;
            return localSocketImplAdapter;
        }
        throw new IOException("acceptSocket, Server socket not created");
    }

    /* access modifiers changed from: package-private */
    public void closeSocket() throws IOException {
        if (this.mIsServerSocket) {
            this.mServerSocket.close();
        } else {
            this.mClientSocket.close();
        }
    }

    /* access modifiers changed from: package-private */
    public InputStream getInputStream() throws IOException {
        LocalSocket localSocket = this.mClientSocket;
        if (localSocket != null) {
            return localSocket.getInputStream();
        }
        throw new IOException("getInputStream, Client socket not created");
    }

    /* access modifiers changed from: package-private */
    public OutputStream getOutputStream() throws IOException {
        LocalSocket localSocket = this.mClientSocket;
        if (localSocket != null) {
            return localSocket.getOutputStream();
        }
        throw new IOException("getOutputStream, Client socket not created");
    }

    /* access modifiers changed from: package-private */
    public void closeInputStream() throws IOException {
        LocalSocket localSocket = this.mClientSocket;
        if (localSocket != null) {
            localSocket.shutdownInput();
            return;
        }
        throw new IOException("closeInputStream, Client socket not created");
    }

    /* access modifiers changed from: package-private */
    public void closeOutputStream() throws IOException {
        LocalSocket localSocket = this.mClientSocket;
        if (localSocket != null) {
            localSocket.shutdownOutput();
            return;
        }
        throw new IOException("closeOutputStream, Client socket not created");
    }

    /* access modifiers changed from: package-private */
    public FileDescriptor getSocketFd() {
        if (this.mIsServerSocket) {
            return this.mServerSocket.getFileDescriptor();
        }
        return this.mClientSocket.getFileDescriptor();
    }

    /* access modifiers changed from: package-private */
    public String getSocketName() {
        LocalSocketAddress localSocketAddress;
        if (this.mIsServerSocket) {
            localSocketAddress = this.mServerSocket.getLocalSocketAddress();
        } else {
            localSocketAddress = this.mClientSocket.getLocalSocketAddress();
        }
        return localSocketAddress != null ? localSocketAddress.getName() : this.mSocketName;
    }

    /* access modifiers changed from: package-private */
    public SocketCertificates getPeerSocketInfo() throws IOException {
        LocalSocket localSocket = this.mClientSocket;
        if (localSocket != null) {
            Credentials peerCredentials = localSocket.getPeerCredentials();
            return new SocketCertificates(peerCredentials.getPid(), peerCredentials.getUid(), peerCredentials.getGid());
        }
        throw new IOException("getPeerSocketInfo, Client socket not created");
    }

    /* access modifiers changed from: package-private */
    public void setSocketReadTimeout(int i) throws IOException {
        LocalSocket localSocket = this.mClientSocket;
        if (localSocket != null) {
            localSocket.setSoTimeout(i);
            return;
        }
        throw new IOException("setSocketReadTimeout, Client socket not created");
    }
}
