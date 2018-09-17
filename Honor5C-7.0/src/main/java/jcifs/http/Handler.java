package jcifs.http;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.Map;
import java.util.StringTokenizer;

public class Handler extends URLStreamHandler {
    public static final int DEFAULT_HTTP_PORT = 80;
    private static final String HANDLER_PKGS_PROPERTY = "java.protocol.handler.pkgs";
    private static final String[] JVM_VENDOR_DEFAULT_PKGS = null;
    private static final Map PROTOCOL_HANDLERS = null;
    private static URLStreamHandlerFactory factory;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: jcifs.http.Handler.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: jcifs.http.Handler.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: jcifs.http.Handler.<clinit>():void");
    }

    public static void setURLStreamHandlerFactory(URLStreamHandlerFactory factory) {
        synchronized (PROTOCOL_HANDLERS) {
            if (factory != null) {
                throw new IllegalStateException("URLStreamHandlerFactory already set.");
            }
            PROTOCOL_HANDLERS.clear();
            factory = factory;
        }
    }

    protected int getDefaultPort() {
        return DEFAULT_HTTP_PORT;
    }

    protected URLConnection openConnection(URL url) throws IOException {
        return new NtlmHttpURLConnection((HttpURLConnection) new URL(url, url.toExternalForm(), getDefaultStreamHandler(url.getProtocol())).openConnection());
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static URLStreamHandler getDefaultStreamHandler(String protocol) throws IOException {
        synchronized (PROTOCOL_HANDLERS) {
            URLStreamHandler handler = (URLStreamHandler) PROTOCOL_HANDLERS.get(protocol);
            if (handler != null) {
                return handler;
            }
            String className;
            Class handlerClass;
            if (factory != null) {
                handler = factory.createURLStreamHandler(protocol);
            }
            if (handler == null) {
                StringTokenizer tokenizer = new StringTokenizer(System.getProperty(HANDLER_PKGS_PROPERTY), "|");
                while (tokenizer.hasMoreTokens()) {
                    String provider = tokenizer.nextToken().trim();
                    if (!provider.equals("jcifs")) {
                        className = provider + "." + protocol + ".Handler";
                        handlerClass = null;
                        try {
                            handlerClass = Class.forName(className);
                        } catch (Exception e) {
                        }
                        if (handlerClass == null) {
                            try {
                                handlerClass = ClassLoader.getSystemClassLoader().loadClass(className);
                            } catch (Exception e2) {
                            }
                        }
                        handler = (URLStreamHandler) handlerClass.newInstance();
                        break;
                    }
                }
            }
            if (handler == null) {
                int i = 0;
                while (true) {
                    if (i >= JVM_VENDOR_DEFAULT_PKGS.length) {
                        break;
                    }
                    className = JVM_VENDOR_DEFAULT_PKGS[i] + "." + protocol + ".Handler";
                    handlerClass = null;
                    try {
                        handlerClass = Class.forName(className);
                    } catch (Exception e3) {
                    }
                    if (handlerClass == null) {
                        try {
                            handlerClass = ClassLoader.getSystemClassLoader().loadClass(className);
                        } catch (Exception e4) {
                        }
                    }
                    handler = (URLStreamHandler) handlerClass.newInstance();
                    if (handler != null) {
                        break;
                    }
                    i++;
                }
            }
            if (handler == null) {
                throw new IOException("Unable to find default handler for protocol: " + protocol);
            }
            PROTOCOL_HANDLERS.put(protocol, handler);
            return handler;
        }
    }
}
