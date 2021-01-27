package com.huawei.okhttp3.internal.http2;

import com.huawei.okhttp3.Call;
import com.huawei.okhttp3.EventListener;
import com.huawei.okhttp3.Route;
import com.huawei.okhttp3.internal.connection.ConcurrentConnect;
import com.huawei.okhttp3.internal.connection.RealConnectionPool;
import com.huawei.okhttp3.internal.connection.RouteSelector;
import com.huawei.okhttp3.internal.connection.Transmitter;
import java.io.IOException;
import java.lang.ref.Reference;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

@Deprecated
public class RealConnectionBase {
    public static final long MAX_RESERVE_DURATION_NS = 1000000000;
    public int allocationLimit = 1;
    public ConcurrentConnect concurrentConnect = null;
    public Route connectedRoute = null;
    public RealConnectionPool connectionPool;
    public long keepaliveTimestampNs = 0;
    public boolean noNewExchanges;
    protected Socket rawSocket;
    protected Route route;
    public RouteSelector.Selection routeSelection = null;
    public int successCount;
    public List<Reference<Transmitter>> transmitters = new ArrayList();

    public void prepareConcurrentConnect(ArrayList<InetSocketAddress> concurrentInetSocketAddresses, int connectionAttemptDelay) {
        if (concurrentInetSocketAddresses != null) {
            this.concurrentConnect = new ConcurrentConnect(concurrentInetSocketAddresses, connectionAttemptDelay);
        }
    }

    public void setRouteSelection(RouteSelector.Selection routeSelection2) {
        this.routeSelection = routeSelection2;
    }

    public boolean getNoNewExchanges() {
        boolean isAllOldExchangesInfo;
        synchronized (this.connectionPool) {
            isAllOldExchangesInfo = this.noNewExchanges;
        }
        return isAllOldExchangesInfo;
    }

    public int successCount() {
        return this.successCount;
    }

    public int allocationLimit() {
        return this.allocationLimit;
    }

    public List<Reference<Transmitter>> getTransmitters() {
        return this.transmitters;
    }

    public void concurrentConnectSocket(int connectTimeout, int readTimeout, Call call, EventListener eventListener) throws IOException {
        Route route2;
        ConcurrentConnect concurrentConnect2 = this.concurrentConnect;
        if (concurrentConnect2 != null && (route2 = this.route) != null) {
            this.rawSocket = concurrentConnect2.getConnectedSocket((long) connectTimeout, route2.proxy(), call, eventListener);
            RouteSelector.Selection selection = this.routeSelection;
            if (selection != null) {
                selection.setFailedTcpAddresses(this.concurrentConnect.failedAddressList());
                Socket socket = this.rawSocket;
                if (socket != null) {
                    SocketAddress socketAddr = socket.getRemoteSocketAddress();
                    if (socketAddr instanceof InetSocketAddress) {
                        this.routeSelection.setConnectedTcpAddress((InetSocketAddress) socketAddr);
                    }
                }
            }
            Socket socket2 = this.rawSocket;
            if (socket2 != null) {
                SocketAddress socketAddr2 = socket2.getRemoteSocketAddress();
                if (socketAddr2 instanceof InetSocketAddress) {
                    this.connectedRoute = new Route(this.route.address(), this.route.proxy(), (InetSocketAddress) socketAddr2);
                    this.route = this.connectedRoute;
                }
                this.rawSocket.setSoTimeout(readTimeout);
                return;
            }
            throw new ConnectException("Failed to connect to host " + this.route.address().url().host());
        }
    }
}
