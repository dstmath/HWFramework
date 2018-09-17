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
    private static final String[] JVM_VENDOR_DEFAULT_PKGS = new String[]{"sun.net.www.protocol"};
    private static final Map PROTOCOL_HANDLERS = new HashMap();
    private static URLStreamHandlerFactory factory;

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
        return 80;
    }

    protected URLConnection openConnection(URL url) throws IOException {
        return new NtlmHttpURLConnection((HttpURLConnection) new URL(url, url.toExternalForm(), getDefaultStreamHandler(url.getProtocol())).openConnection());
    }

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
                for (String str : JVM_VENDOR_DEFAULT_PKGS) {
                    className = str + "." + protocol + ".Handler";
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
