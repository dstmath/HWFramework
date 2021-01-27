package org.apache.http.conn.routing;

import java.net.InetAddress;
import org.apache.http.HttpHost;
import org.apache.http.conn.routing.RouteInfo;

@Deprecated
public final class RouteTracker implements RouteInfo, Cloneable {
    private boolean connected;
    private RouteInfo.LayerType layered;
    private final InetAddress localAddress;
    private HttpHost[] proxyChain;
    private boolean secure;
    private final HttpHost targetHost;
    private RouteInfo.TunnelType tunnelled;

    public RouteTracker(HttpHost target, InetAddress local) {
        if (target != null) {
            this.targetHost = target;
            this.localAddress = local;
            this.tunnelled = RouteInfo.TunnelType.PLAIN;
            this.layered = RouteInfo.LayerType.PLAIN;
            return;
        }
        throw new IllegalArgumentException("Target host may not be null.");
    }

    public RouteTracker(HttpRoute route) {
        this(route.getTargetHost(), route.getLocalAddress());
    }

    public final void connectTarget(boolean secure2) {
        if (!this.connected) {
            this.connected = true;
            this.secure = secure2;
            return;
        }
        throw new IllegalStateException("Already connected.");
    }

    public final void connectProxy(HttpHost proxy, boolean secure2) {
        if (proxy == null) {
            throw new IllegalArgumentException("Proxy host may not be null.");
        } else if (!this.connected) {
            this.connected = true;
            this.proxyChain = new HttpHost[]{proxy};
            this.secure = secure2;
        } else {
            throw new IllegalStateException("Already connected.");
        }
    }

    public final void tunnelTarget(boolean secure2) {
        if (!this.connected) {
            throw new IllegalStateException("No tunnel unless connected.");
        } else if (this.proxyChain != null) {
            this.tunnelled = RouteInfo.TunnelType.TUNNELLED;
            this.secure = secure2;
        } else {
            throw new IllegalStateException("No tunnel without proxy.");
        }
    }

    public final void tunnelProxy(HttpHost proxy, boolean secure2) {
        if (proxy == null) {
            throw new IllegalArgumentException("Proxy host may not be null.");
        } else if (this.connected) {
            HttpHost[] httpHostArr = this.proxyChain;
            if (httpHostArr != null) {
                HttpHost[] proxies = new HttpHost[(httpHostArr.length + 1)];
                System.arraycopy(httpHostArr, 0, proxies, 0, httpHostArr.length);
                proxies[proxies.length - 1] = proxy;
                this.proxyChain = proxies;
                this.secure = secure2;
                return;
            }
            throw new IllegalStateException("No proxy tunnel without proxy.");
        } else {
            throw new IllegalStateException("No tunnel unless connected.");
        }
    }

    public final void layerProtocol(boolean secure2) {
        if (this.connected) {
            this.layered = RouteInfo.LayerType.LAYERED;
            this.secure = secure2;
            return;
        }
        throw new IllegalStateException("No layered protocol unless connected.");
    }

    @Override // org.apache.http.conn.routing.RouteInfo
    public final HttpHost getTargetHost() {
        return this.targetHost;
    }

    @Override // org.apache.http.conn.routing.RouteInfo
    public final InetAddress getLocalAddress() {
        return this.localAddress;
    }

    @Override // org.apache.http.conn.routing.RouteInfo
    public final int getHopCount() {
        if (!this.connected) {
            return 0;
        }
        HttpHost[] httpHostArr = this.proxyChain;
        if (httpHostArr == null) {
            return 1;
        }
        return httpHostArr.length + 1;
    }

    @Override // org.apache.http.conn.routing.RouteInfo
    public final HttpHost getHopTarget(int hop) {
        if (hop >= 0) {
            int hopcount = getHopCount();
            if (hop >= hopcount) {
                throw new IllegalArgumentException("Hop index " + hop + " exceeds tracked route length " + hopcount + ".");
            } else if (hop < hopcount - 1) {
                return this.proxyChain[hop];
            } else {
                return this.targetHost;
            }
        } else {
            throw new IllegalArgumentException("Hop index must not be negative: " + hop);
        }
    }

    @Override // org.apache.http.conn.routing.RouteInfo
    public final HttpHost getProxyHost() {
        HttpHost[] httpHostArr = this.proxyChain;
        if (httpHostArr == null) {
            return null;
        }
        return httpHostArr[0];
    }

    public final boolean isConnected() {
        return this.connected;
    }

    @Override // org.apache.http.conn.routing.RouteInfo
    public final RouteInfo.TunnelType getTunnelType() {
        return this.tunnelled;
    }

    @Override // org.apache.http.conn.routing.RouteInfo
    public final boolean isTunnelled() {
        return this.tunnelled == RouteInfo.TunnelType.TUNNELLED;
    }

    @Override // org.apache.http.conn.routing.RouteInfo
    public final RouteInfo.LayerType getLayerType() {
        return this.layered;
    }

    @Override // org.apache.http.conn.routing.RouteInfo
    public final boolean isLayered() {
        return this.layered == RouteInfo.LayerType.LAYERED;
    }

    @Override // org.apache.http.conn.routing.RouteInfo
    public final boolean isSecure() {
        return this.secure;
    }

    public final HttpRoute toRoute() {
        if (!this.connected) {
            return null;
        }
        return new HttpRoute(this.targetHost, this.localAddress, this.proxyChain, this.secure, this.tunnelled, this.layered);
    }

    @Override // java.lang.Object
    public final boolean equals(Object o) {
        boolean z = true;
        if (o == this) {
            return true;
        }
        if (!(o instanceof RouteTracker)) {
            return false;
        }
        RouteTracker that = (RouteTracker) o;
        boolean equal = this.targetHost.equals(that.targetHost);
        InetAddress inetAddress = this.localAddress;
        InetAddress inetAddress2 = that.localAddress;
        boolean equal2 = equal & (inetAddress == inetAddress2 || (inetAddress != null && inetAddress.equals(inetAddress2)));
        HttpHost[] httpHostArr = this.proxyChain;
        HttpHost[] httpHostArr2 = that.proxyChain;
        boolean equal3 = equal2 & (httpHostArr == httpHostArr2 || !(httpHostArr == null || httpHostArr2 == null || httpHostArr.length != httpHostArr2.length));
        if (!(this.connected == that.connected && this.secure == that.secure && this.tunnelled == that.tunnelled && this.layered == that.layered)) {
            z = false;
        }
        boolean equal4 = z & equal3;
        if (equal4 && this.proxyChain != null) {
            int i = 0;
            while (equal4) {
                HttpHost[] httpHostArr3 = this.proxyChain;
                if (i >= httpHostArr3.length) {
                    break;
                }
                equal4 = httpHostArr3[i].equals(that.proxyChain[i]);
                i++;
            }
        }
        return equal4;
    }

    @Override // java.lang.Object
    public final int hashCode() {
        int hc = this.targetHost.hashCode();
        InetAddress inetAddress = this.localAddress;
        if (inetAddress != null) {
            hc ^= inetAddress.hashCode();
        }
        HttpHost[] httpHostArr = this.proxyChain;
        if (httpHostArr != null) {
            hc ^= httpHostArr.length;
            int i = 0;
            while (true) {
                HttpHost[] httpHostArr2 = this.proxyChain;
                if (i >= httpHostArr2.length) {
                    break;
                }
                hc ^= httpHostArr2[i].hashCode();
                i++;
            }
        }
        if (this.connected) {
            hc ^= 286331153;
        }
        if (this.secure) {
            hc ^= 572662306;
        }
        return (hc ^ this.tunnelled.hashCode()) ^ this.layered.hashCode();
    }

    @Override // java.lang.Object
    public final String toString() {
        StringBuilder cab = new StringBuilder((getHopCount() * 30) + 50);
        cab.append("RouteTracker[");
        InetAddress inetAddress = this.localAddress;
        if (inetAddress != null) {
            cab.append(inetAddress);
            cab.append("->");
        }
        cab.append('{');
        if (this.connected) {
            cab.append('c');
        }
        if (this.tunnelled == RouteInfo.TunnelType.TUNNELLED) {
            cab.append('t');
        }
        if (this.layered == RouteInfo.LayerType.LAYERED) {
            cab.append('l');
        }
        if (this.secure) {
            cab.append('s');
        }
        cab.append("}->");
        if (this.proxyChain != null) {
            int i = 0;
            while (true) {
                HttpHost[] httpHostArr = this.proxyChain;
                if (i >= httpHostArr.length) {
                    break;
                }
                cab.append(httpHostArr[i]);
                cab.append("->");
                i++;
            }
        }
        cab.append(this.targetHost);
        cab.append(']');
        return cab.toString();
    }

    @Override // java.lang.Object
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
