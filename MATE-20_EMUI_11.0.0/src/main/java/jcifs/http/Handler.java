package jcifs.http;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class Handler extends URLStreamHandler {
    public static final int DEFAULT_HTTP_PORT = 80;
    private static final String HANDLER_PKGS_PROPERTY = "java.protocol.handler.pkgs";
    private static final String[] JVM_VENDOR_DEFAULT_PKGS = {"sun.net.www.protocol"};
    private static final Map PROTOCOL_HANDLERS = new HashMap();
    private static URLStreamHandlerFactory factory;

    public static void setURLStreamHandlerFactory(URLStreamHandlerFactory factory2) {
        synchronized (PROTOCOL_HANDLERS) {
            if (factory != null) {
                throw new IllegalStateException("URLStreamHandlerFactory already set.");
            }
            PROTOCOL_HANDLERS.clear();
            factory = factory2;
        }
    }

    /* access modifiers changed from: protected */
    @Override // java.net.URLStreamHandler
    public int getDefaultPort() {
        return 80;
    }

    /* access modifiers changed from: protected */
    @Override // java.net.URLStreamHandler
    public URLConnection openConnection(URL url) throws IOException {
        return new NtlmHttpURLConnection((HttpURLConnection) new URL(url, url.toExternalForm(), getDefaultStreamHandler(url.getProtocol())).openConnection());
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x006b, code lost:
        r2 = (java.net.URLStreamHandler) r4.newInstance();
     */
    private static URLStreamHandler getDefaultStreamHandler(String protocol) throws IOException {
        synchronized (PROTOCOL_HANDLERS) {
            URLStreamHandler handler = (URLStreamHandler) PROTOCOL_HANDLERS.get(protocol);
            if (handler != null) {
                return handler;
            }
            if (factory != null) {
                handler = factory.createURLStreamHandler(protocol);
            }
            if (handler == null) {
                StringTokenizer tokenizer = new StringTokenizer(System.getProperty(HANDLER_PKGS_PROPERTY), "|");
                while (true) {
                    if (!tokenizer.hasMoreTokens()) {
                        break;
                    }
                    String provider = tokenizer.nextToken().trim();
                    if (!provider.equals("jcifs")) {
                        String className = provider + "." + protocol + ".Handler";
                        Class handlerClass = null;
                        try {
                            handlerClass = Class.forName(className);
                        } catch (Exception e) {
                        }
                        if (handlerClass != null) {
                            break;
                        }
                        try {
                            handlerClass = ClassLoader.getSystemClassLoader().loadClass(className);
                            break;
                        } catch (Exception e2) {
                        }
                    }
                }
            }
            if (handler == null) {
                for (int i = 0; i < JVM_VENDOR_DEFAULT_PKGS.length; i++) {
                    String className2 = JVM_VENDOR_DEFAULT_PKGS[i] + "." + protocol + ".Handler";
                    Class handlerClass2 = null;
                    try {
                        handlerClass2 = Class.forName(className2);
                    } catch (Exception e3) {
                    }
                    if (handlerClass2 == null) {
                        try {
                            handlerClass2 = ClassLoader.getSystemClassLoader().loadClass(className2);
                        } catch (Exception e4) {
                        }
                    }
                    handler = (URLStreamHandler) handlerClass2.newInstance();
                    if (handler != null) {
                        break;
                    }
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
