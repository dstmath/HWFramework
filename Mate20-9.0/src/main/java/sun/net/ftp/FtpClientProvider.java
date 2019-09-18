package sun.net.ftp;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ServiceConfigurationError;
import sun.net.ftp.impl.DefaultFtpClientProvider;

public abstract class FtpClientProvider {
    private static final Object lock = new Object();
    /* access modifiers changed from: private */
    public static FtpClientProvider provider = null;

    public abstract FtpClient createFtpClient();

    protected FtpClientProvider() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("ftpClientProvider"));
        }
    }

    /* access modifiers changed from: private */
    public static boolean loadProviderFromProperty() {
        String cm = System.getProperty("sun.net.ftpClientProvider");
        if (cm == null) {
            return false;
        }
        try {
            provider = (FtpClientProvider) Class.forName(cm, true, null).newInstance();
            return true;
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | SecurityException x) {
            throw new ServiceConfigurationError(x.toString());
        }
    }

    /* access modifiers changed from: private */
    public static boolean loadProviderAsService() {
        return false;
    }

    public static FtpClientProvider provider() {
        synchronized (lock) {
            if (provider != null) {
                FtpClientProvider ftpClientProvider = provider;
                return ftpClientProvider;
            }
            FtpClientProvider ftpClientProvider2 = (FtpClientProvider) AccessController.doPrivileged(new PrivilegedAction<Object>() {
                public Object run() {
                    if (FtpClientProvider.loadProviderFromProperty()) {
                        return FtpClientProvider.provider;
                    }
                    if (FtpClientProvider.loadProviderAsService()) {
                        return FtpClientProvider.provider;
                    }
                    FtpClientProvider unused = FtpClientProvider.provider = new DefaultFtpClientProvider();
                    return FtpClientProvider.provider;
                }
            });
            return ftpClientProvider2;
        }
    }
}
