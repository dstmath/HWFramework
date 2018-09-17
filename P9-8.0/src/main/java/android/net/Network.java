package android.net;

import android.content.IntentFilter;
import android.net.wifi.WifiScanLog;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import com.android.okhttp.ConnectionPool;
import com.android.okhttp.Dns;
import com.android.okhttp.HttpHandler;
import com.android.okhttp.HttpsHandler;
import com.android.okhttp.OkHttpClient;
import com.android.okhttp.OkUrlFactory;
import java.io.FileDescriptor;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
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

public class Network implements Parcelable {
    public static final Creator<Network> CREATOR = new Creator<Network>() {
        public Network createFromParcel(Parcel in) {
            return new Network(in.readInt());
        }

        public Network[] newArray(int size) {
            return new Network[size];
        }
    };
    private static final boolean httpKeepAlive = Boolean.parseBoolean(System.getProperty("http.keepAlive", "true"));
    private static final long httpKeepAliveDurationMs = Long.parseLong(System.getProperty("http.keepAliveDuration", "300000"));
    private static final int httpMaxConnections = (httpKeepAlive ? Integer.parseInt(System.getProperty("http.maxConnections", WifiScanLog.EVENT_KEY5)) : 0);
    private volatile ConnectionPool mConnectionPool = null;
    private volatile Dns mDns = null;
    private final Object mLock = new Object();
    private volatile NetworkBoundSocketFactory mNetworkBoundSocketFactory = null;
    public final int netId;

    private class NetworkBoundSocketFactory extends SocketFactory {
        private final int mNetId;

        public NetworkBoundSocketFactory(int netId) {
            this.mNetId = netId;
        }

        private Socket connectToHost(String host, int port, SocketAddress localAddress) throws IOException {
            InetAddress[] hostAddresses = Network.this.getAllByName(host);
            int i = 0;
            while (i < hostAddresses.length) {
                try {
                    Socket socket = createSocket();
                    if (localAddress != null) {
                        socket.bind(localAddress);
                    }
                    socket.connect(new InetSocketAddress(hostAddresses[i], port));
                    return socket;
                } catch (IOException e) {
                    if (i == hostAddresses.length - 1) {
                        throw e;
                    }
                    i++;
                }
            }
            throw new UnknownHostException(host);
        }

        public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
            return connectToHost(host, port, new InetSocketAddress(localHost, localPort));
        }

        public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
            Socket socket = createSocket();
            socket.bind(new InetSocketAddress(localAddress, localPort));
            socket.connect(new InetSocketAddress(address, port));
            return socket;
        }

        public Socket createSocket(InetAddress host, int port) throws IOException {
            Socket socket = createSocket();
            socket.connect(new InetSocketAddress(host, port));
            return socket;
        }

        public Socket createSocket(String host, int port) throws IOException {
            return connectToHost(host, port, null);
        }

        public Socket createSocket() throws IOException {
            Socket socket = new Socket();
            Network.this.bindSocket(socket);
            return socket;
        }
    }

    public Network(int netId) {
        this.netId = netId;
    }

    public Network(Network that) {
        this.netId = that.netId;
    }

    public InetAddress[] getAllByName(String host) throws UnknownHostException {
        return InetAddress.getAllByNameOnNet(host, this.netId);
    }

    public InetAddress getByName(String host) throws UnknownHostException {
        return InetAddress.getByNameOnNet(host, this.netId);
    }

    public SocketFactory getSocketFactory() {
        if (this.mNetworkBoundSocketFactory == null) {
            synchronized (this.mLock) {
                if (this.mNetworkBoundSocketFactory == null) {
                    this.mNetworkBoundSocketFactory = new NetworkBoundSocketFactory(this.netId);
                }
            }
        }
        return this.mNetworkBoundSocketFactory;
    }

    private void maybeInitHttpClient() {
        synchronized (this.mLock) {
            if (this.mDns == null) {
                this.mDns = new Dns() {
                    public List<InetAddress> lookup(String hostname) throws UnknownHostException {
                        return Arrays.asList(Network.this.getAllByName(hostname));
                    }
                };
            }
            if (this.mConnectionPool == null) {
                this.mConnectionPool = new ConnectionPool(httpMaxConnections, httpKeepAliveDurationMs, TimeUnit.MILLISECONDS);
            }
        }
    }

    public URLConnection openConnection(URL url) throws IOException {
        ConnectivityManager cm = ConnectivityManager.getInstanceOrNull();
        if (cm == null) {
            throw new IOException("No ConnectivityManager yet constructed, please construct one");
        }
        Proxy proxy;
        ProxyInfo proxyInfo = cm.getProxyForNetwork(this);
        if (proxyInfo != null) {
            proxy = proxyInfo.makeProxy();
        } else {
            proxy = Proxy.NO_PROXY;
        }
        return openConnection(url, proxy);
    }

    public URLConnection openConnection(URL url, Proxy proxy) throws IOException {
        if (proxy == null) {
            throw new IllegalArgumentException("proxy is null");
        }
        OkUrlFactory okUrlFactory;
        maybeInitHttpClient();
        String protocol = url.getProtocol();
        if (protocol.equals(IntentFilter.SCHEME_HTTP)) {
            okUrlFactory = HttpHandler.createHttpOkUrlFactory(proxy);
        } else if (protocol.equals(IntentFilter.SCHEME_HTTPS)) {
            okUrlFactory = HttpsHandler.createHttpsOkUrlFactory(proxy);
        } else {
            throw new MalformedURLException("Invalid URL or unrecognized protocol " + protocol);
        }
        OkHttpClient client = okUrlFactory.client();
        client.setSocketFactory(getSocketFactory()).setConnectionPool(this.mConnectionPool);
        client.setDns(this.mDns);
        return okUrlFactory.open(url);
    }

    public void bindSocket(DatagramSocket socket) throws IOException {
        socket.getReuseAddress();
        bindSocket(socket.getFileDescriptor$());
    }

    public void bindSocket(Socket socket) throws IOException {
        socket.getReuseAddress();
        bindSocket(socket.getFileDescriptor$());
    }

    public void bindSocket(FileDescriptor fd) throws IOException {
        try {
            if (!((InetSocketAddress) Os.getpeername(fd)).getAddress().isAnyLocalAddress()) {
                throw new SocketException("Socket is connected");
            }
        } catch (ErrnoException e) {
            if (e.errno != OsConstants.ENOTCONN) {
                throw e.rethrowAsSocketException();
            }
        } catch (ClassCastException e2) {
            throw new SocketException("Only AF_INET/AF_INET6 sockets supported");
        }
        int err = NetworkUtils.bindSocketToNetwork(fd.getInt$(), this.netId);
        if (err != 0) {
            throw new ErrnoException("Binding socket to network " + this.netId, -err).rethrowAsSocketException();
        }
    }

    public long getNetworkHandle() {
        if (this.netId == 0) {
            return 0;
        }
        return (((long) this.netId) << 32) | 16435934;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.netId);
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!(obj instanceof Network)) {
            return false;
        }
        if (this.netId == ((Network) obj).netId) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return this.netId * 11;
    }

    public String toString() {
        return Integer.toString(this.netId);
    }
}
