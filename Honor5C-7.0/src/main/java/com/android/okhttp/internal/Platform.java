package com.android.okhttp.internal;

import com.android.okhttp.Protocol;
import com.android.okhttp.okio.Buffer;
import dalvik.system.SocketTagger;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import javax.net.ssl.SSLSocket;

public final class Platform {
    private static final OptionalMethod<Socket> GET_ALPN_SELECTED_PROTOCOL = null;
    private static final Platform PLATFORM = null;
    private static final OptionalMethod<Socket> SET_ALPN_PROTOCOLS = null;
    private static final OptionalMethod<Socket> SET_HOSTNAME = null;
    private static final OptionalMethod<Socket> SET_USE_SESSION_TICKETS = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.okhttp.internal.Platform.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.okhttp.internal.Platform.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.okhttp.internal.Platform.<clinit>():void");
    }

    public static Platform get() {
        return PLATFORM;
    }

    public void logW(String warning) {
        System.logW(warning);
    }

    public void tagSocket(Socket socket) throws SocketException {
        SocketTagger.get().tag(socket);
    }

    public void untagSocket(Socket socket) throws SocketException {
        SocketTagger.get().untag(socket);
    }

    public void configureTlsExtensions(SSLSocket sslSocket, String hostname, List<Protocol> protocols) {
        if (hostname != null) {
            SET_USE_SESSION_TICKETS.invokeOptionalWithoutCheckedException(sslSocket, Boolean.valueOf(true));
            SET_HOSTNAME.invokeOptionalWithoutCheckedException(sslSocket, hostname);
        }
        boolean alpnSupported = SET_ALPN_PROTOCOLS.isSupported(sslSocket);
        if (alpnSupported) {
            Object[] parameters = new Object[]{concatLengthPrefixed(protocols)};
            if (alpnSupported) {
                SET_ALPN_PROTOCOLS.invokeWithoutCheckedException(sslSocket, parameters);
            }
        }
    }

    public void afterHandshake(SSLSocket sslSocket) {
    }

    public String getSelectedProtocol(SSLSocket socket) {
        if (!GET_ALPN_SELECTED_PROTOCOL.isSupported(socket)) {
            return null;
        }
        byte[] alpnResult = (byte[]) GET_ALPN_SELECTED_PROTOCOL.invokeWithoutCheckedException(socket, new Object[0]);
        if (alpnResult != null) {
            return new String(alpnResult, Util.UTF_8);
        }
        return null;
    }

    public void connectSocket(Socket socket, InetSocketAddress address, int connectTimeout) throws IOException {
        socket.connect(address, connectTimeout);
    }

    public String getPrefix() {
        return "X-Android";
    }

    static byte[] concatLengthPrefixed(List<Protocol> protocols) {
        Buffer result = new Buffer();
        int size = protocols.size();
        for (int i = 0; i < size; i++) {
            Protocol protocol = (Protocol) protocols.get(i);
            if (protocol != Protocol.HTTP_1_0) {
                result.writeByte(protocol.toString().length());
                result.writeUtf8(protocol.toString());
            }
        }
        return result.readByteArray();
    }
}
