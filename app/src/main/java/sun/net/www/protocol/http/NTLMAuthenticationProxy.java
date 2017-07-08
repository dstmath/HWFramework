package sun.net.www.protocol.http;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.PasswordAuthentication;
import java.net.URL;

class NTLMAuthenticationProxy {
    private static final String clazzStr = "sun.net.www.protocol.http.ntlm.NTLMAuthentication";
    private static Method isTrustedSite = null;
    private static final String isTrustedSiteStr = "isTrustedSite";
    static final NTLMAuthenticationProxy proxy = null;
    static final boolean supported = false;
    private static Method supportsTA = null;
    private static final String supportsTAStr = "supportsTransparentAuth";
    static final boolean supportsTransparentAuth = false;
    private final Constructor<? extends AuthenticationInfo> fiveArgCtr;
    private final Constructor<? extends AuthenticationInfo> threeArgCtr;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.net.www.protocol.http.NTLMAuthenticationProxy.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.net.www.protocol.http.NTLMAuthenticationProxy.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: sun.net.www.protocol.http.NTLMAuthenticationProxy.<clinit>():void");
    }

    private NTLMAuthenticationProxy(Constructor<? extends AuthenticationInfo> threeArgCtr, Constructor<? extends AuthenticationInfo> fiveArgCtr) {
        this.threeArgCtr = threeArgCtr;
        this.fiveArgCtr = fiveArgCtr;
    }

    AuthenticationInfo create(boolean isProxy, URL url, PasswordAuthentication pw) {
        try {
            return (AuthenticationInfo) this.threeArgCtr.newInstance(Boolean.valueOf(isProxy), url, pw);
        } catch (ReflectiveOperationException roe) {
            finest(roe);
            return null;
        }
    }

    AuthenticationInfo create(boolean isProxy, String host, int port, PasswordAuthentication pw) {
        try {
            return (AuthenticationInfo) this.fiveArgCtr.newInstance(Boolean.valueOf(isProxy), host, Integer.valueOf(port), pw);
        } catch (ReflectiveOperationException roe) {
            finest(roe);
            return null;
        }
    }

    private static boolean supportsTransparentAuth() {
        try {
            return ((Boolean) supportsTA.invoke(null, new Object[0])).booleanValue();
        } catch (ReflectiveOperationException roe) {
            finest(roe);
            return supported;
        }
    }

    public static boolean isTrustedSite(URL url) {
        try {
            return ((Boolean) isTrustedSite.invoke(null, url)).booleanValue();
        } catch (ReflectiveOperationException roe) {
            finest(roe);
            return supported;
        }
    }

    private static NTLMAuthenticationProxy tryLoadNTLMAuthentication() {
        try {
            Class<? extends AuthenticationInfo> cl = Class.forName(clazzStr, true, null);
            if (cl != null) {
                Constructor<? extends AuthenticationInfo> threeArg = cl.getConstructor(Boolean.TYPE, URL.class, PasswordAuthentication.class);
                Constructor<? extends AuthenticationInfo> fiveArg = cl.getConstructor(Boolean.TYPE, String.class, Integer.TYPE, PasswordAuthentication.class);
                supportsTA = cl.getDeclaredMethod(supportsTAStr, new Class[0]);
                isTrustedSite = cl.getDeclaredMethod(isTrustedSiteStr, URL.class);
                return new NTLMAuthenticationProxy(threeArg, fiveArg);
            }
        } catch (ClassNotFoundException cnfe) {
            finest(cnfe);
        } catch (Object roe) {
            throw new AssertionError(roe);
        }
        return null;
    }

    static void finest(Exception e) {
        HttpURLConnection.getHttpLogger().finest("NTLMAuthenticationProxy: " + e);
    }
}
