package ohos.net;

import android.net.NetworkUtils;
import android.system.ErrnoException;
import android.system.OsConstants;
import com.android.okhttp.internalandroidapi.Dns;
import com.android.okhttp.internalandroidapi.HttpURLConnectionFactory;
import java.io.FileDescriptor;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.net.SocketFactory;
import libcore.io.IoUtils;
import libcore.io.Libcore;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.RemoteException;
import ohos.tools.C0000Bytrace;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class NetHandle implements Sequenceable {
    private static final long KEEP_ALIVE_DURATIONS_MS = 300000;
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109360, "NETMGRKIT");
    private static final int MAX_HTTP_CONNECTIONS = 5;
    private static final long NET_ID_MAGIC = 3405697037L;
    private static final int NET_ID_MAGIC_SIZE = 32;
    private final Object mLock;
    private volatile NetBoundSocketFactory mNetBoundSocketFactory;
    private final transient boolean mPrivateDnsBypass;
    private volatile HttpURLConnectionFactory mUrlConnectionFactory;
    public int netId;

    public NetHandle(int i, boolean z) {
        this.mLock = new Object();
        this.mNetBoundSocketFactory = null;
        this.netId = i;
        this.mPrivateDnsBypass = z;
    }

    public NetHandle(int i) {
        this.mLock = new Object();
        this.mNetBoundSocketFactory = null;
        this.netId = i;
        this.mPrivateDnsBypass = false;
    }

    public NetHandle() {
        this.mLock = new Object();
        this.mNetBoundSocketFactory = null;
        this.netId = 0;
        this.mPrivateDnsBypass = false;
    }

    public InetAddress[] getAllByName(String str) throws UnknownHostException {
        return InetAddress.getAllByNameOnNet(str, getNetIdForResolv());
    }

    public InetAddress getByName(String str) throws UnknownHostException {
        return InetAddress.getByNameOnNet(str, getNetIdForResolv());
    }

    public void bindSocket(DatagramSocket datagramSocket) throws IOException {
        if (datagramSocket != null) {
            datagramSocket.getReuseAddress();
            bindSocket(datagramSocket.getFileDescriptor$());
        }
    }

    public void bindSocket(Socket socket) throws IOException {
        if (socket != null) {
            socket.getReuseAddress();
            bindSocket(socket.getFileDescriptor$());
        }
    }

    public void bindSocket(FileDescriptor fileDescriptor) throws IOException {
        if (fileDescriptor != null) {
            try {
                SocketAddress socketAddress = Libcore.os.getpeername(fileDescriptor);
                if (socketAddress instanceof InetSocketAddress) {
                    if (!((InetSocketAddress) socketAddress).getAddress().isAnyLocalAddress()) {
                        throw new SocketException("Socket is connected");
                    }
                }
            } catch (ErrnoException e) {
                if (e.errno != OsConstants.ENOTCONN) {
                    throw e.rethrowAsSocketException();
                }
            } catch (ClassCastException unused) {
                throw new SocketException("Only AF_INET/AF_INET6 sockets supported");
            }
            if (NetworkUtils.bindSocketToNetwork(fileDescriptor.getInt$(), this.netId) != 0) {
                throw new SocketException("Binding socket to network " + this.netId);
            }
        }
    }

    /* access modifiers changed from: private */
    public class NetBoundSocketFactory extends SocketFactory {
        private NetBoundSocketFactory() {
        }

        private Socket connectToHost(String str, int i, SocketAddress socketAddress) throws IOException {
            InetAddress[] allByName = NetHandle.this.getAllByName(str);
            for (int i2 = 0; i2 < allByName.length; i2++) {
                try {
                    Socket createSocket = createSocket();
                    if (socketAddress != null) {
                        try {
                            createSocket.bind(socketAddress);
                        } catch (Throwable th) {
                            IoUtils.closeQuietly(createSocket);
                            throw th;
                        }
                    }
                    createSocket.connect(new InetSocketAddress(allByName[i2], i));
                    return createSocket;
                } catch (IOException e) {
                    if (i2 == allByName.length - 1) {
                        throw e;
                    }
                }
            }
            throw new UnknownHostException(str);
        }

        @Override // javax.net.SocketFactory
        public Socket createSocket(String str, int i, InetAddress inetAddress, int i2) throws IOException {
            return connectToHost(str, i, new InetSocketAddress(inetAddress, i2));
        }

        @Override // javax.net.SocketFactory
        public Socket createSocket(InetAddress inetAddress, int i, InetAddress inetAddress2, int i2) throws IOException {
            Socket createSocket = createSocket();
            try {
                createSocket.bind(new InetSocketAddress(inetAddress2, i2));
                createSocket.connect(new InetSocketAddress(inetAddress, i));
                return createSocket;
            } catch (Throwable th) {
                IoUtils.closeQuietly(createSocket);
                throw th;
            }
        }

        @Override // javax.net.SocketFactory
        public Socket createSocket(InetAddress inetAddress, int i) throws IOException {
            Socket createSocket = createSocket();
            try {
                createSocket.connect(new InetSocketAddress(inetAddress, i));
                return createSocket;
            } catch (Throwable th) {
                IoUtils.closeQuietly(createSocket);
                throw th;
            }
        }

        @Override // javax.net.SocketFactory
        public Socket createSocket(String str, int i) throws IOException {
            return connectToHost(str, i, null);
        }

        @Override // javax.net.SocketFactory
        public Socket createSocket() throws IOException {
            Socket socket = new Socket();
            try {
                NetHandle.this.bindSocket(socket);
                return socket;
            } catch (Throwable th) {
                IoUtils.closeQuietly(socket);
                throw th;
            }
        }
    }

    private void maybeInitUrlConnectionFactory() {
        synchronized (this.mLock) {
            if (this.mUrlConnectionFactory == null) {
                $$Lambda$NetHandle$EiRDuaUQ9y7sS0dZXtTDWbH9Xc r1 = new Dns() {
                    /* class ohos.net.$$Lambda$NetHandle$EiRDuaUQ9y7sS0dZXtTDWbH9Xc */

                    public final List lookup(String str) {
                        return NetHandle.this.lambda$maybeInitUrlConnectionFactory$0$NetHandle(str);
                    }
                };
                HttpURLConnectionFactory httpURLConnectionFactory = new HttpURLConnectionFactory();
                httpURLConnectionFactory.setDns(r1);
                httpURLConnectionFactory.setNewConnectionPool(5, (long) KEEP_ALIVE_DURATIONS_MS, TimeUnit.MILLISECONDS);
                this.mUrlConnectionFactory = httpURLConnectionFactory;
            }
        }
    }

    public /* synthetic */ List lambda$maybeInitUrlConnectionFactory$0$NetHandle(String str) throws UnknownHostException {
        return Arrays.asList(getAllByName(str));
    }

    private SocketFactory getSocketFactory() {
        if (this.mNetBoundSocketFactory == null) {
            synchronized (this.mLock) {
                if (this.mNetBoundSocketFactory == null) {
                    this.mNetBoundSocketFactory = new NetBoundSocketFactory();
                }
            }
        }
        return this.mNetBoundSocketFactory;
    }

    public URLConnection openConnection(URL url) throws IOException {
        Proxy proxy;
        NetManagerProxy instance = NetManagerProxy.getInstance();
        if (instance == null || url == null) {
            throw new IOException("No NetworkManager yet constructed, please construct one");
        }
        try {
            HttpProxy httpProxyForNet = instance.getHttpProxyForNet(this);
            if (httpProxyForNet != null) {
                Proxy proxy2 = Proxy.NO_PROXY;
                if (httpProxyForNet.host != null) {
                    try {
                        proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(httpProxyForNet.host, httpProxyForNet.port));
                    } catch (IllegalArgumentException unused) {
                        HiLog.error(LABEL, "openConnection proxy error", new Object[0]);
                    }
                }
                proxy = proxy2;
            } else {
                proxy = Proxy.NO_PROXY;
            }
        } catch (RemoteException unused2) {
            proxy = Proxy.NO_PROXY;
        }
        return openConnection(url, proxy);
    }

    public URLConnection openConnection(URL url, Proxy proxy) throws IOException {
        if (url == null) {
            throw new IllegalArgumentException("invalid url");
        } else if (proxy != null) {
            maybeInitUrlConnectionFactory();
            return this.mUrlConnectionFactory.openConnection(url, getSocketFactory(), proxy);
        } else {
            throw new IllegalArgumentException("proxy is null");
        }
    }

    public long getNetHandle() {
        int i = this.netId;
        if (i == 0) {
            return 0;
        }
        return (((long) i) << 32) | NET_ID_MAGIC;
    }

    public boolean equals(Object obj) {
        if ((obj instanceof NetHandle) && this.netId == ((NetHandle) obj).netId) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return this.netId * 11;
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        this.netId = parcel.readInt();
        return true;
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        parcel.writeInt(this.netId);
        return true;
    }

    private int getNetIdForResolv() {
        if (this.mPrivateDnsBypass) {
            return (int) (C0000Bytrace.BYTRACE_TAG_ABILITY_MANAGER | ((long) this.netId));
        }
        return this.netId;
    }
}
