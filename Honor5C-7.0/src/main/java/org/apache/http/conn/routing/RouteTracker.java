package org.apache.http.conn.routing;

import java.net.InetAddress;
import org.apache.http.HttpHost;
import org.apache.http.conn.routing.RouteInfo.LayerType;
import org.apache.http.conn.routing.RouteInfo.TunnelType;

@Deprecated
public final class RouteTracker implements RouteInfo, Cloneable {
    private boolean connected;
    private LayerType layered;
    private final InetAddress localAddress;
    private HttpHost[] proxyChain;
    private boolean secure;
    private final HttpHost targetHost;
    private TunnelType tunnelled;

    public RouteTracker(HttpHost target, InetAddress local) {
        if (target == null) {
            throw new IllegalArgumentException("Target host may not be null.");
        }
        this.targetHost = target;
        this.localAddress = local;
        this.tunnelled = TunnelType.PLAIN;
        this.layered = LayerType.PLAIN;
    }

    public RouteTracker(HttpRoute route) {
        this(route.getTargetHost(), route.getLocalAddress());
    }

    public final void connectTarget(boolean secure) {
        if (this.connected) {
            throw new IllegalStateException("Already connected.");
        }
        this.connected = true;
        this.secure = secure;
    }

    public final void connectProxy(HttpHost proxy, boolean secure) {
        if (proxy == null) {
            throw new IllegalArgumentException("Proxy host may not be null.");
        } else if (this.connected) {
            throw new IllegalStateException("Already connected.");
        } else {
            this.connected = true;
            this.proxyChain = new HttpHost[]{proxy};
            this.secure = secure;
        }
    }

    public final void tunnelTarget(boolean secure) {
        if (!this.connected) {
            throw new IllegalStateException("No tunnel unless connected.");
        } else if (this.proxyChain == null) {
            throw new IllegalStateException("No tunnel without proxy.");
        } else {
            this.tunnelled = TunnelType.TUNNELLED;
            this.secure = secure;
        }
    }

    public final void tunnelProxy(HttpHost proxy, boolean secure) {
        if (proxy == null) {
            throw new IllegalArgumentException("Proxy host may not be null.");
        } else if (!this.connected) {
            throw new IllegalStateException("No tunnel unless connected.");
        } else if (this.proxyChain == null) {
            throw new IllegalStateException("No proxy tunnel without proxy.");
        } else {
            HttpHost[] proxies = new HttpHost[(this.proxyChain.length + 1)];
            System.arraycopy(this.proxyChain, 0, proxies, 0, this.proxyChain.length);
            proxies[proxies.length - 1] = proxy;
            this.proxyChain = proxies;
            this.secure = secure;
        }
    }

    public final void layerProtocol(boolean secure) {
        if (this.connected) {
            this.layered = LayerType.LAYERED;
            this.secure = secure;
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
        if (hop < 0) {
            throw new IllegalArgumentException("Hop index must not be negative: " + hop);
        }
        int hopcount = getHopCount();
        if (hop >= hopcount) {
            throw new IllegalArgumentException("Hop index " + hop + " exceeds tracked route length " + hopcount + ".");
        } else if (hop < hopcount - 1) {
            return this.proxyChain[hop];
        } else {
            return this.targetHost;
        }
    }

    public final HttpHost getProxyHost() {
        return this.proxyChain == null ? null : this.proxyChain[0];
    }

    public final boolean isConnected() {
        return this.connected;
    }

    public final TunnelType getTunnelType() {
        return this.tunnelled;
    }

    public final boolean isTunnelled() {
        return this.tunnelled == TunnelType.TUNNELLED;
    }

    public final LayerType getLayerType() {
        return this.layered;
    }

    public final boolean isLayered() {
        return this.layered == LayerType.LAYERED;
    }

    public final boolean isSecure() {
        return this.secure;
    }

    public final HttpRoute toRoute() {
        return !this.connected ? null : new HttpRoute(this.targetHost, this.localAddress, this.proxyChain, this.secure, this.tunnelled, this.layered);
    }

    public final boolean equals(Object o) {
        int i = 1;
        if (o == this) {
            return true;
        }
        if (!(o instanceof RouteTracker)) {
            return false;
        }
        int i2;
        RouteTracker that = (RouteTracker) o;
        boolean equal = this.targetHost.equals(that.targetHost);
        if (this.localAddress == that.localAddress) {
            i2 = 1;
        } else if (this.localAddress != null) {
            i2 = this.localAddress.equals(that.localAddress);
        } else {
            i2 = 0;
        }
        equal &= i2;
        i2 = this.proxyChain != that.proxyChain ? (this.proxyChain == null || that.proxyChain == null) ? 0 : this.proxyChain.length == that.proxyChain.length ? 1 : 0 : 1;
        equal &= i2;
        if (this.connected != that.connected || this.secure != that.secure || this.tunnelled != that.tunnelled) {
            i = 0;
        } else if (this.layered != that.layered) {
            i = 0;
        }
        equal &= i;
        if (equal && this.proxyChain != null) {
            int i3 = 0;
            while (equal && i3 < this.proxyChain.length) {
                equal = this.proxyChain[i3].equals(that.proxyChain[i3]);
                i3++;
            }
        }
        return equal;
    }

    public final int hashCode() {
        int hc = this.targetHost.hashCode();
        if (this.localAddress != null) {
            hc ^= this.localAddress.hashCode();
        }
        if (this.proxyChain != null) {
            hc ^= this.proxyChain.length;
            for (HttpHost hashCode : this.proxyChain) {
                hc ^= hashCode.hashCode();
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

    public final String toString() {
        StringBuilder cab = new StringBuilder((getHopCount() * 30) + 50);
        cab.append("RouteTracker[");
        if (this.localAddress != null) {
            cab.append(this.localAddress);
            cab.append("->");
        }
        cab.append('{');
        if (this.connected) {
            cab.append('c');
        }
        if (this.tunnelled == TunnelType.TUNNELLED) {
            cab.append('t');
        }
        if (this.layered == LayerType.LAYERED) {
            cab.append('l');
        }
        if (this.secure) {
            cab.append('s');
        }
        cab.append("}->");
        if (this.proxyChain != null) {
            for (Object append : this.proxyChain) {
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
