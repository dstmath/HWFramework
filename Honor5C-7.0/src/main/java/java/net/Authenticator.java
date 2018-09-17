package java.net;

public abstract class Authenticator {
    private static Authenticator theAuthenticator;
    private RequestorType requestingAuthType;
    private String requestingHost;
    private int requestingPort;
    private String requestingPrompt;
    private String requestingProtocol;
    private String requestingScheme;
    private InetAddress requestingSite;
    private URL requestingURL;

    public enum RequestorType {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.net.Authenticator.RequestorType.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.net.Authenticator.RequestorType.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: java.net.Authenticator.RequestorType.<clinit>():void");
        }
    }

    public Authenticator() {
    }

    private void reset() {
        this.requestingHost = null;
        this.requestingSite = null;
        this.requestingPort = -1;
        this.requestingProtocol = null;
        this.requestingPrompt = null;
        this.requestingScheme = null;
        this.requestingURL = null;
        this.requestingAuthType = RequestorType.SERVER;
    }

    public static synchronized void setDefault(Authenticator a) {
        synchronized (Authenticator.class) {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                sm.checkPermission(new NetPermission("setDefaultAuthenticator"));
            }
            theAuthenticator = a;
        }
    }

    public static PasswordAuthentication requestPasswordAuthentication(InetAddress addr, int port, String protocol, String prompt, String scheme) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new NetPermission("requestPasswordAuthentication"));
        }
        Authenticator a = theAuthenticator;
        if (a == null) {
            return null;
        }
        PasswordAuthentication passwordAuthentication;
        synchronized (a) {
            a.reset();
            a.requestingSite = addr;
            a.requestingPort = port;
            a.requestingProtocol = protocol;
            a.requestingPrompt = prompt;
            a.requestingScheme = scheme;
            passwordAuthentication = a.getPasswordAuthentication();
        }
        return passwordAuthentication;
    }

    public static PasswordAuthentication requestPasswordAuthentication(String host, InetAddress addr, int port, String protocol, String prompt, String scheme) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new NetPermission("requestPasswordAuthentication"));
        }
        Authenticator a = theAuthenticator;
        if (a == null) {
            return null;
        }
        PasswordAuthentication passwordAuthentication;
        synchronized (a) {
            a.reset();
            a.requestingHost = host;
            a.requestingSite = addr;
            a.requestingPort = port;
            a.requestingProtocol = protocol;
            a.requestingPrompt = prompt;
            a.requestingScheme = scheme;
            passwordAuthentication = a.getPasswordAuthentication();
        }
        return passwordAuthentication;
    }

    public static PasswordAuthentication requestPasswordAuthentication(String host, InetAddress addr, int port, String protocol, String prompt, String scheme, URL url, RequestorType reqType) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new NetPermission("requestPasswordAuthentication"));
        }
        Authenticator a = theAuthenticator;
        if (a == null) {
            return null;
        }
        PasswordAuthentication passwordAuthentication;
        synchronized (a) {
            a.reset();
            a.requestingHost = host;
            a.requestingSite = addr;
            a.requestingPort = port;
            a.requestingProtocol = protocol;
            a.requestingPrompt = prompt;
            a.requestingScheme = scheme;
            a.requestingURL = url;
            a.requestingAuthType = reqType;
            passwordAuthentication = a.getPasswordAuthentication();
        }
        return passwordAuthentication;
    }

    protected final String getRequestingHost() {
        return this.requestingHost;
    }

    protected final InetAddress getRequestingSite() {
        return this.requestingSite;
    }

    protected final int getRequestingPort() {
        return this.requestingPort;
    }

    protected final String getRequestingProtocol() {
        return this.requestingProtocol;
    }

    protected final String getRequestingPrompt() {
        return this.requestingPrompt;
    }

    protected final String getRequestingScheme() {
        return this.requestingScheme;
    }

    protected PasswordAuthentication getPasswordAuthentication() {
        return null;
    }

    protected URL getRequestingURL() {
        return this.requestingURL;
    }

    protected RequestorType getRequestorType() {
        return this.requestingAuthType;
    }
}
