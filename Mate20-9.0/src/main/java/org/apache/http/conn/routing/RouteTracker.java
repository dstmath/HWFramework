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
        } else if (!this.connected) {
            throw new IllegalStateException("No tunnel unless connected.");
        } else if (this.proxyChain != null) {
            HttpHost[] proxies = new HttpHost[(this.proxyChain.length + 1)];
            System.arraycopy(this.proxyChain, 0, proxies, 0, this.proxyChain.length);
            proxies[proxies.length - 1] = proxy;
            this.proxyChain = proxies;
            this.secure = secure2;
        } else {
            throw new IllegalStateException("No proxy tunnel without proxy.");
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

    public final HttpHost getTargetHost() {
        return this.targetHost;
    }

    public final InetAddress getLocalAddress() {
        return this.localAddress;
    }

    public final int getHopCount() {
        if (!this.connected) {
            return 0;
        }
        if (this.proxyChain == null) {
            return 1;
        }
        return this.proxyChain.length + 1;
    }

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

    public final HttpHost getProxyHost() {
        if (this.proxyChain == null) {
            return null;
        }
        return this.proxyChain[0];
    }

    public final boolean isConnected() {
        return this.connected;
    }

    public final RouteInfo.TunnelType getTunnelType() {
        return this.tunnelled;
    }

    public final boolean isTunnelled() {
        return this.tunnelled == RouteInfo.TunnelType.TUNNELLED;
    }

    public final RouteInfo.LayerType getLayerType() {
        return this.layered;
    }

    public final boolean isLayered() {
        return this.layered == RouteInfo.LayerType.LAYERED;
    }

    public final boolean isSecure() {
        return this.secure;
    }

    public final HttpRoute toRoute() {
        if (!this.connected) {
            return null;
        }
        HttpRoute httpRoute = new HttpRoute(this.targetHost, this.localAddress, this.proxyChain, this.secure, this.tunnelled, this.layered);
        return httpRoute;
    }

    public final boolean equals(Object o) {
        boolean z = true;
        if (o == this) {
            return true;
        }
        int i = 0;
        if (!(o instanceof RouteTracker)) {
            return false;
        }
        RouteTracker that = (RouteTracker) o;
        boolean equal = this.targetHost.equals(that.targetHost) & (this.localAddress == that.localAddress || (this.localAddress != null && this.localAddress.equals(that.localAddress))) & (this.proxyChain == that.proxyChain || !(this.proxyChain == null || that.proxyChain == null || this.proxyChain.length != that.proxyChain.length));
        if (!(this.connected == that.connected && this.secure == that.secure && this.tunnelled == that.tunnelled && this.layered == that.layered)) {
            z = false;
        }
        boolean equal2 = z & equal;
        if (equal2 && this.proxyChain != null) {
            while (equal2 && i < this.proxyChain.length) {
                equal2 = this.proxyChain[i].equals(that.proxyChain[i]);
                i++;
            }
        }
        return equal2;
    }

    public final int hashCode() {
        int hc;
        int hc2 = this.targetHost.hashCode();
        if (this.localAddress != null) {
            hc2 ^= this.localAddress.hashCode();
        }
        if (this.proxyChain != null) {
            hc ^= this.proxyChain.length;
            for (HttpHost hashCode : this.proxyChain) {
                hc ^= hashCode.hashCode();
            }
        }
        if (this.connected != 0) {
            hc ^= 286331153;
        }
        if (this.secure) {
            hc ^= 572662306;
        }
        return (hc ^ this.tunnelled.hashCode()) ^ this.layered.hashCode();
    }

    public final String toString() {
        StringBuilder cab = new StringBuilder(50 + (getHopCount() * 30));
        cab.append("RouteTracker[");
        if (this.localAddress != null) {
            cab.append(this.localAddress);
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
            for (HttpHost append : this.proxyChain) {
                cab.append(append);
                cab.append("->");
            }
        }
        cab.append(this.targetHost);
        cab.append(']');
        return cab.toString();
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
