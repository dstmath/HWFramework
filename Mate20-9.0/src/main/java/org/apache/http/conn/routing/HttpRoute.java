package org.apache.http.conn.routing;

import java.net.InetAddress;
import org.apache.http.HttpHost;
import org.apache.http.conn.routing.RouteInfo;

@Deprecated
public final class HttpRoute implements RouteInfo, Cloneable {
    private final RouteInfo.LayerType layered;
    private final InetAddress localAddress;
    private final HttpHost[] proxyChain;
    private final boolean secure;
    private final HttpHost targetHost;
    private final RouteInfo.TunnelType tunnelled;

    private HttpRoute(InetAddress local, HttpHost target, HttpHost[] proxies, boolean secure2, RouteInfo.TunnelType tunnelled2, RouteInfo.LayerType layered2) {
        if (target == null) {
            throw new IllegalArgumentException("Target host may not be null.");
        } else if (tunnelled2 == RouteInfo.TunnelType.TUNNELLED && proxies == null) {
            throw new IllegalArgumentException("Proxy required if tunnelled.");
        } else {
            tunnelled2 = tunnelled2 == null ? RouteInfo.TunnelType.PLAIN : tunnelled2;
            layered2 = layered2 == null ? RouteInfo.LayerType.PLAIN : layered2;
            this.targetHost = target;
            this.localAddress = local;
            this.proxyChain = proxies;
            this.secure = secure2;
            this.tunnelled = tunnelled2;
            this.layered = layered2;
        }
    }

    public HttpRoute(HttpHost target, InetAddress local, HttpHost[] proxies, boolean secure2, RouteInfo.TunnelType tunnelled2, RouteInfo.LayerType layered2) {
        this(local, target, toChain(proxies), secure2, tunnelled2, layered2);
    }

    public HttpRoute(HttpHost target, InetAddress local, HttpHost proxy, boolean secure2, RouteInfo.TunnelType tunnelled2, RouteInfo.LayerType layered2) {
        this(local, target, toChain(proxy), secure2, tunnelled2, layered2);
    }

    public HttpRoute(HttpHost target, InetAddress local, boolean secure2) {
        this(local, target, (HttpHost[]) null, secure2, RouteInfo.TunnelType.PLAIN, RouteInfo.LayerType.PLAIN);
    }

    public HttpRoute(HttpHost target) {
        this((InetAddress) null, target, (HttpHost[]) null, false, RouteInfo.TunnelType.PLAIN, RouteInfo.LayerType.PLAIN);
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public HttpRoute(HttpHost target, InetAddress local, HttpHost proxy, boolean secure2) {
        this(local, target, toChain(proxy), secure2, secure2 ? RouteInfo.TunnelType.TUNNELLED : RouteInfo.TunnelType.PLAIN, secure2 ? RouteInfo.LayerType.LAYERED : RouteInfo.LayerType.PLAIN);
        if (proxy == null) {
            throw new IllegalArgumentException("Proxy host may not be null.");
        }
    }

    private static HttpHost[] toChain(HttpHost proxy) {
        if (proxy == null) {
            return null;
        }
        return new HttpHost[]{proxy};
    }

    private static HttpHost[] toChain(HttpHost[] proxies) {
        if (proxies == null || proxies.length < 1) {
            return null;
        }
        int length = proxies.length;
        int i = 0;
        while (i < length) {
            if (proxies[i] != null) {
                i++;
            } else {
                throw new IllegalArgumentException("Proxy chain may not contain null elements.");
            }
        }
        HttpHost[] result = new HttpHost[proxies.length];
        System.arraycopy(proxies, 0, result, 0, proxies.length);
        return result;
    }

    public final HttpHost getTargetHost() {
        return this.targetHost;
    }

    public final InetAddress getLocalAddress() {
        return this.localAddress;
    }

    public final int getHopCount() {
        if (this.proxyChain == null) {
            return 1;
        }
        return 1 + this.proxyChain.length;
    }

    public final HttpHost getHopTarget(int hop) {
        if (hop >= 0) {
            int hopcount = getHopCount();
            if (hop >= hopcount) {
                throw new IllegalArgumentException("Hop index " + hop + " exceeds route length " + hopcount);
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

    public final boolean equals(Object o) {
        boolean z = true;
        if (o == this) {
            return true;
        }
        int i = 0;
        if (!(o instanceof HttpRoute)) {
            return false;
        }
        HttpRoute that = (HttpRoute) o;
        boolean equal = this.targetHost.equals(that.targetHost) & (this.localAddress == that.localAddress || (this.localAddress != null && this.localAddress.equals(that.localAddress))) & (this.proxyChain == that.proxyChain || !(this.proxyChain == null || that.proxyChain == null || this.proxyChain.length != that.proxyChain.length));
        if (!(this.secure == that.secure && this.tunnelled == that.tunnelled && this.layered == that.layered)) {
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
            for (HttpHost aProxyChain : this.proxyChain) {
                hc ^= aProxyChain.hashCode();
            }
        }
        if (this.secure) {
            hc ^= 286331153;
        }
        return (hc ^ this.tunnelled.hashCode()) ^ this.layered.hashCode();
    }

    public final String toString() {
        StringBuilder cab = new StringBuilder(50 + (getHopCount() * 30));
        cab.append("HttpRoute[");
        if (this.localAddress != null) {
            cab.append(this.localAddress);
            cab.append("->");
        }
        cab.append('{');
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
            for (HttpHost aProxyChain : this.proxyChain) {
                cab.append(aProxyChain);
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
