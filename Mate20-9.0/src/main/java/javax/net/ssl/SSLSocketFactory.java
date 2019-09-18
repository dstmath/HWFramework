package javax.net.ssl;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.security.AccessController;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedAction;
import java.security.Security;
import java.util.Locale;
import javax.net.SocketFactory;
import sun.security.action.GetPropertyAction;

public abstract class SSLSocketFactory extends SocketFactory {
    static final boolean DEBUG;
    private static SSLSocketFactory defaultSocketFactory;
    private static int lastVersion = -1;

    public abstract Socket createSocket(Socket socket, String str, int i, boolean z) throws IOException;

    public abstract String[] getDefaultCipherSuites();

    public abstract String[] getSupportedCipherSuites();

    static {
        String s = ((String) AccessController.doPrivileged(new GetPropertyAction("javax.net.debug", ""))).toLowerCase(Locale.ENGLISH);
        DEBUG = s.contains("all") || s.contains("ssl");
    }

    private static void log(String msg) {
        if (DEBUG) {
            System.out.println(msg);
        }
    }

    public static synchronized SocketFactory getDefault() {
        synchronized (SSLSocketFactory.class) {
            if (defaultSocketFactory == null || lastVersion != Security.getVersion()) {
                lastVersion = Security.getVersion();
                SSLSocketFactory previousDefaultSocketFactory = defaultSocketFactory;
                Class<?> cls = null;
                defaultSocketFactory = null;
                String clsName = getSecurityProperty("ssl.SocketFactory.provider");
                if (clsName != null) {
                    if (previousDefaultSocketFactory == null || !clsName.equals(previousDefaultSocketFactory.getClass().getName())) {
                        log("setting up default SSLSocketFactory");
                        try {
                            cls = Class.forName(clsName);
                        } catch (ClassNotFoundException e) {
                            try {
                                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                                if (cl == null) {
                                    cl = ClassLoader.getSystemClassLoader();
                                }
                                if (cl != null) {
                                    cls = Class.forName(clsName, true, cl);
                                }
                            } catch (Exception e2) {
                                log("SSLSocketFactory instantiation failed: " + e2.toString());
                            }
                        }
                        log("class " + clsName + " is loaded");
                        SSLSocketFactory fac = (SSLSocketFactory) cls.newInstance();
                        log("instantiated an instance of class " + clsName);
                        defaultSocketFactory = fac;
                        return fac;
                    }
                    defaultSocketFactory = previousDefaultSocketFactory;
                    SSLSocketFactory sSLSocketFactory = defaultSocketFactory;
                    return sSLSocketFactory;
                }
                try {
                    SSLContext context = SSLContext.getDefault();
                    if (context != null) {
                        defaultSocketFactory = context.getSocketFactory();
                    } else {
                        defaultSocketFactory = new DefaultSSLSocketFactory(new IllegalStateException("No factory found."));
                    }
                    SSLSocketFactory sSLSocketFactory2 = defaultSocketFactory;
                    return sSLSocketFactory2;
                } catch (NoSuchAlgorithmException e3) {
                    return new DefaultSSLSocketFactory(e3);
                }
            } else {
                SSLSocketFactory sSLSocketFactory3 = defaultSocketFactory;
                return sSLSocketFactory3;
            }
        }
    }

    static String getSecurityProperty(final String name) {
        return (String) AccessController.doPrivileged(new PrivilegedAction<String>() {
            public String run() {
                String s = Security.getProperty(String.this);
                if (s == null) {
                    return s;
                }
                String s2 = s.trim();
                if (s2.length() == 0) {
                    return null;
                }
                return s2;
            }
        });
    }

    public Socket createSocket(Socket s, InputStream consumed, boolean autoClose) throws IOException {
        throw new UnsupportedOperationException();
    }
}
