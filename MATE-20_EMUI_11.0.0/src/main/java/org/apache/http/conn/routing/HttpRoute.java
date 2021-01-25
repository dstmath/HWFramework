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
        for (HttpHost proxy : proxies) {
            if (proxy == null) {
                throw new IllegalArgumentException("Proxy chain may not contain null elements.");
            }
        }
        HttpHost[] result = new HttpHost[proxies.length];
        System.arraycopy(proxies, 0, result, 0, proxies.length);
        return result;
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
        HttpHost[] httpHostArr = this.proxyChain;
        if (httpHostArr == null) {
            return 1;
        }
        return 1 + httpHostArr.length;
    }

    @Override // org.apache.http.conn.routing.RouteInfo
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

    @Override // org.apache.http.conn.routing.RouteInfo
    public final HttpHost getProxyHost() {
        HttpHost[] httpHostArr = this.proxyChain;
        if (httpHostArr == null) {
            return null;
        }
        return httpHostArr[0];
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

    @Override // java.lang.Object
    public final boolean equals(Object o) {
        boolean z = true;
        if (o == this) {
            return true;
        }
        if (!(o instanceof HttpRoute)) {
            return false;
        }
        HttpRoute that = (HttpRoute) o;
        boolean equal = this.targetHost.equals(that.targetHost);
        InetAddress inetAddress = this.localAddress;
        InetAddress inetAddress2 = that.localAddress;
        boolean equal2 = equal & (inetAddress == inetAddress2 || (inetAddress != null && inetAddress.equals(inetAddress2)));
        HttpHost[] httpHostArr = this.proxyChain;
        HttpHost[] httpHostArr2 = that.proxyChain;
        boolean equal3 = equal2 & (httpHostArr == httpHostArr2 || !(httpHostArr == null || httpHostArr2 == null || httpHostArr.length != httpHostArr2.length));
        if (!(this.secure == that.secure && this.tunnelled == that.tunnelled && this.layered == that.layered)) {
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
            for (HttpHost aProxyChain : httpHostArr) {
                hc ^= aProxyChain.hashCode();
            }
        }
        if (this.secure) {
            hc ^= 286331153;
        }
        return (hc ^ this.tunnelled.hashCode()) ^ this.layered.hashCode();
    }

    @Override // java.lang.Object
    public final String toString() {
        StringBuilder cab = new StringBuilder((getHopCount() * 30) + 50);
        cab.append("HttpRoute[");
        InetAddress inetAddress = this.localAddress;
        if (inetAddress != null) {
            cab.append(inetAddress);
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
        HttpHost[] httpHostArr = this.proxyChain;
        if (httpHostArr != null) {
            for (HttpHost aProxyChain : httpHostArr) {
                cab.append(aProxyChain);
                cab.append("->");
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
