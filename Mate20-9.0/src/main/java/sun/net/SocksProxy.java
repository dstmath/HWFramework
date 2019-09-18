package sun.net;

import java.net.Proxy;
import java.net.SocketAddress;

public final class SocksProxy extends Proxy {
    private final int version;

    private SocksProxy(SocketAddress addr, int version2) {
        super(Proxy.Type.SOCKS, addr);
        this.version = version2;
    }

    public static SocksProxy create(SocketAddress addr, int version2) {
        return new SocksProxy(addr, version2);
    }

    public int protocolVersion() {
        return this.version;
    }
}
