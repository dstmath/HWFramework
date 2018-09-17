package com.android.okhttp.internal;

import com.android.okhttp.Call;
import com.android.okhttp.Callback;
import com.android.okhttp.Connection;
import com.android.okhttp.ConnectionPool;
import com.android.okhttp.ConnectionSpec;
import com.android.okhttp.Headers.Builder;
import com.android.okhttp.HttpUrl;
import com.android.okhttp.OkHttpClient;
import com.android.okhttp.Protocol;
import com.android.okhttp.Request;
import com.android.okhttp.internal.http.HttpEngine;
import com.android.okhttp.internal.http.RouteException;
import com.android.okhttp.internal.http.Transport;
import com.android.okhttp.okio.BufferedSink;
import com.android.okhttp.okio.BufferedSource;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.logging.Logger;
import javax.net.ssl.SSLSocket;

public abstract class Internal {
    public static Internal instance;
    public static final Logger logger = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.okhttp.internal.Internal.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.okhttp.internal.Internal.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.okhttp.internal.Internal.<clinit>():void");
    }

    public abstract void addLenient(Builder builder, String str);

    public abstract void addLenient(Builder builder, String str, String str2);

    public abstract void apply(ConnectionSpec connectionSpec, SSLSocket sSLSocket, boolean z);

    public abstract Connection callEngineGetConnection(Call call);

    public abstract void callEngineReleaseConnection(Call call) throws IOException;

    public abstract void callEnqueue(Call call, Callback callback, boolean z);

    public abstract boolean clearOwner(Connection connection);

    public abstract void closeIfOwnedBy(Connection connection, Object obj) throws IOException;

    public abstract void connectAndSetOwner(OkHttpClient okHttpClient, Connection connection, HttpEngine httpEngine, Request request) throws RouteException;

    public abstract BufferedSink connectionRawSink(Connection connection);

    public abstract BufferedSource connectionRawSource(Connection connection);

    public abstract void connectionSetOwner(Connection connection, Object obj);

    public abstract HttpUrl getHttpUrlChecked(String str) throws MalformedURLException, UnknownHostException;

    public abstract InternalCache internalCache(OkHttpClient okHttpClient);

    public abstract boolean isReadable(Connection connection);

    public abstract Network network(OkHttpClient okHttpClient);

    public abstract Transport newTransport(Connection connection, HttpEngine httpEngine) throws IOException;

    public abstract void recycle(ConnectionPool connectionPool, Connection connection);

    public abstract int recycleCount(Connection connection);

    public abstract RouteDatabase routeDatabase(OkHttpClient okHttpClient);

    public abstract void setCache(OkHttpClient okHttpClient, InternalCache internalCache);

    public abstract void setNetwork(OkHttpClient okHttpClient, Network network);

    public abstract void setOwner(Connection connection, HttpEngine httpEngine);

    public abstract void setProtocol(Connection connection, Protocol protocol);

    public static void initializeInstanceForTests() {
        OkHttpClient okHttpClient = new OkHttpClient();
    }
}
