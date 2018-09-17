package sun.net.ftp;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ServiceConfigurationError;
import sun.net.ftp.impl.DefaultFtpClientProvider;

public abstract class FtpClientProvider {
    private static final Object lock = null;
    private static FtpClientProvider provider;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.net.ftp.FtpClientProvider.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.net.ftp.FtpClientProvider.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: sun.net.ftp.FtpClientProvider.<clinit>():void");
    }

    public abstract FtpClient createFtpClient();

    protected FtpClientProvider() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("ftpClientProvider"));
        }
    }

    private static boolean loadProviderFromProperty() {
        String cm = System.getProperty("sun.net.ftpClientProvider");
        if (cm == null) {
            return false;
        }
        try {
            provider = (FtpClientProvider) Class.forName(cm, true, null).newInstance();
            return true;
        } catch (ClassNotFoundException x) {
            throw new ServiceConfigurationError(x.toString());
        } catch (IllegalAccessException x2) {
            throw new ServiceConfigurationError(x2.toString());
        } catch (InstantiationException x3) {
            throw new ServiceConfigurationError(x3.toString());
        } catch (SecurityException x4) {
            throw new ServiceConfigurationError(x4.toString());
        }
    }

    private static boolean loadProviderAsService() {
        return false;
    }

    public static FtpClientProvider provider() {
        synchronized (lock) {
            if (provider != null) {
                FtpClientProvider ftpClientProvider = provider;
                return ftpClientProvider;
            }
            ftpClientProvider = (FtpClientProvider) AccessController.doPrivileged(new PrivilegedAction<Object>() {
                public Object run() {
                    if (FtpClientProvider.loadProviderFromProperty()) {
                        return FtpClientProvider.provider;
                    }
                    if (FtpClientProvider.loadProviderAsService()) {
                        return FtpClientProvider.provider;
                    }
                    FtpClientProvider.provider = new DefaultFtpClientProvider();
                    return FtpClientProvider.provider;
                }
            });
            return ftpClientProvider;
        }
    }
}
