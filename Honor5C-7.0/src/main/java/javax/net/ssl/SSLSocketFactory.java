package javax.net.ssl;

import java.io.IOException;
import java.net.Socket;
import java.security.AccessController;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedAction;
import java.security.Security;
import javax.net.SocketFactory;

public abstract class SSLSocketFactory extends SocketFactory {
    static final boolean DEBUG = false;
    private static SSLSocketFactory defaultSocketFactory;
    private static int lastVersion;

    /* renamed from: javax.net.ssl.SSLSocketFactory.1 */
    static class AnonymousClass1 implements PrivilegedAction<String> {
        final /* synthetic */ String val$name;

        AnonymousClass1(String val$name) {
            this.val$name = val$name;
        }

        public String run() {
            String s = Security.getProperty(this.val$name);
            if (s == null) {
                return s;
            }
            s = s.trim();
            if (s.length() == 0) {
                return null;
            }
            return s;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: javax.net.ssl.SSLSocketFactory.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: javax.net.ssl.SSLSocketFactory.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.net.ssl.SSLSocketFactory.<clinit>():void");
    }

    public abstract Socket createSocket(Socket socket, String str, int i, boolean z) throws IOException;

    public abstract String[] getDefaultCipherSuites();

    public abstract String[] getSupportedCipherSuites();

    private static void log(String msg) {
        if (DEBUG) {
            System.out.println(msg);
        }
    }

    public static synchronized SocketFactory getDefault() {
        synchronized (SSLSocketFactory.class) {
            SocketFactory socketFactory;
            if (defaultSocketFactory == null || lastVersion != Security.getVersion()) {
                lastVersion = Security.getVersion();
                SSLSocketFactory previousDefaultSocketFactory = defaultSocketFactory;
                defaultSocketFactory = null;
                String clsName = getSecurityProperty("ssl.SocketFactory.provider");
                if (clsName != null) {
                    if (previousDefaultSocketFactory == null || !clsName.equals(previousDefaultSocketFactory.getClass().getName())) {
                        log("setting up default SSLSocketFactory");
                        Class cls = null;
                        try {
                            cls = Class.forName(clsName);
                        } catch (ClassNotFoundException e) {
                            ClassLoader cl = Thread.currentThread().getContextClassLoader();
                            if (cl == null) {
                                cl = ClassLoader.getSystemClassLoader();
                            }
                            if (cl != null) {
                                cls = Class.forName(clsName, true, cl);
                            }
                        }
                        try {
                            log("class " + clsName + " is loaded");
                            defaultSocketFactory = (SSLSocketFactory) cls.newInstance();
                            log("instantiated an instance of class " + clsName);
                            if (defaultSocketFactory != null) {
                                socketFactory = defaultSocketFactory;
                                return socketFactory;
                            }
                        } catch (Exception e2) {
                            log("SSLSocketFactory instantiation failed: " + e2.toString());
                        }
                    } else {
                        defaultSocketFactory = previousDefaultSocketFactory;
                        socketFactory = defaultSocketFactory;
                        return socketFactory;
                    }
                }
                try {
                    SSLContext context = SSLContext.getDefault();
                    if (context != null) {
                        defaultSocketFactory = context.getSocketFactory();
                    }
                } catch (NoSuchAlgorithmException e3) {
                }
                if (defaultSocketFactory == null) {
                    defaultSocketFactory = new DefaultSSLSocketFactory(new IllegalStateException("No factory found."));
                }
                socketFactory = defaultSocketFactory;
                return socketFactory;
            }
            socketFactory = defaultSocketFactory;
            return socketFactory;
        }
    }

    static String getSecurityProperty(String name) {
        return (String) AccessController.doPrivileged(new AnonymousClass1(name));
    }
}
