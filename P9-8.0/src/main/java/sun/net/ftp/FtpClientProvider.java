package sun.net.ftp;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ServiceConfigurationError;
import sun.net.ftp.impl.DefaultFtpClientProvider;

public abstract class FtpClientProvider {
    private static final Object lock = new Object();
    private static FtpClientProvider provider = null;

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
            FtpClientProvider ftpClientProvider;
            if (provider != null) {
                ftpClientProvider = provider;
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
