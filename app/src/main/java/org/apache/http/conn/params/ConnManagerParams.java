package org.apache.http.conn.params;

import org.apache.http.params.HttpParams;

@Deprecated
public final class ConnManagerParams implements ConnManagerPNames {
    private static final ConnPerRoute DEFAULT_CONN_PER_ROUTE = null;
    public static final int DEFAULT_MAX_TOTAL_CONNECTIONS = 20;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: org.apache.http.conn.params.ConnManagerParams.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: org.apache.http.conn.params.ConnManagerParams.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: org.apache.http.conn.params.ConnManagerParams.<clinit>():void");
    }

    public static long getTimeout(HttpParams params) {
        if (params != null) {
            return params.getLongParameter(ConnManagerPNames.TIMEOUT, 0);
        }
        throw new IllegalArgumentException("HTTP parameters may not be null");
    }

    public static void setTimeout(HttpParams params, long timeout) {
        if (params == null) {
            throw new IllegalArgumentException("HTTP parameters may not be null");
        }
        params.setLongParameter(ConnManagerPNames.TIMEOUT, timeout);
    }

    public static void setMaxConnectionsPerRoute(HttpParams params, ConnPerRoute connPerRoute) {
        if (params == null) {
            throw new IllegalArgumentException("HTTP parameters must not be null.");
        }
        params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE, connPerRoute);
    }

    public static ConnPerRoute getMaxConnectionsPerRoute(HttpParams params) {
        if (params == null) {
            throw new IllegalArgumentException("HTTP parameters must not be null.");
        }
        ConnPerRoute connPerRoute = (ConnPerRoute) params.getParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE);
        if (connPerRoute == null) {
            return DEFAULT_CONN_PER_ROUTE;
        }
        return connPerRoute;
    }

    public static void setMaxTotalConnections(HttpParams params, int maxTotalConnections) {
        if (params == null) {
            throw new IllegalArgumentException("HTTP parameters must not be null.");
        }
        params.setIntParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, maxTotalConnections);
    }

    public static int getMaxTotalConnections(HttpParams params) {
        if (params != null) {
            return params.getIntParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, DEFAULT_MAX_TOTAL_CONNECTIONS);
        }
        throw new IllegalArgumentException("HTTP parameters must not be null.");
    }
}
