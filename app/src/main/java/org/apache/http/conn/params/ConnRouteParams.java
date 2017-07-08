package org.apache.http.conn.params;

import java.net.InetAddress;
import org.apache.http.HttpHost;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.params.HttpParams;

@Deprecated
public class ConnRouteParams implements ConnRoutePNames {
    public static final HttpHost NO_HOST = null;
    public static final HttpRoute NO_ROUTE = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: org.apache.http.conn.params.ConnRouteParams.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: org.apache.http.conn.params.ConnRouteParams.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.http.conn.params.ConnRouteParams.<clinit>():void");
    }

    private ConnRouteParams() {
    }

    public static HttpHost getDefaultProxy(HttpParams params) {
        if (params == null) {
            throw new IllegalArgumentException("Parameters must not be null.");
        }
        HttpHost proxy = (HttpHost) params.getParameter(ConnRoutePNames.DEFAULT_PROXY);
        if (proxy == null || !NO_HOST.equals(proxy)) {
            return proxy;
        }
        return null;
    }

    public static void setDefaultProxy(HttpParams params, HttpHost proxy) {
        if (params == null) {
            throw new IllegalArgumentException("Parameters must not be null.");
        }
        params.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
    }

    public static HttpRoute getForcedRoute(HttpParams params) {
        if (params == null) {
            throw new IllegalArgumentException("Parameters must not be null.");
        }
        HttpRoute route = (HttpRoute) params.getParameter(ConnRoutePNames.FORCED_ROUTE);
        if (route == null || !NO_ROUTE.equals(route)) {
            return route;
        }
        return null;
    }

    public static void setForcedRoute(HttpParams params, HttpRoute route) {
        if (params == null) {
            throw new IllegalArgumentException("Parameters must not be null.");
        }
        params.setParameter(ConnRoutePNames.FORCED_ROUTE, route);
    }

    public static InetAddress getLocalAddress(HttpParams params) {
        if (params != null) {
            return (InetAddress) params.getParameter(ConnRoutePNames.LOCAL_ADDRESS);
        }
        throw new IllegalArgumentException("Parameters must not be null.");
    }

    public static void setLocalAddress(HttpParams params, InetAddress local) {
        if (params == null) {
            throw new IllegalArgumentException("Parameters must not be null.");
        }
        params.setParameter(ConnRoutePNames.LOCAL_ADDRESS, local);
    }
}
