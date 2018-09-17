package java.nio.channels.spi;

import java.io.IOException;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import sun.nio.ch.DefaultAsynchronousChannelProvider;

public abstract class AsynchronousChannelProvider {

    private static class ProviderHolder {
        static final AsynchronousChannelProvider provider = load();

        private ProviderHolder() {
        }

        private static AsynchronousChannelProvider load() {
            return (AsynchronousChannelProvider) AccessController.doPrivileged(new PrivilegedAction<AsynchronousChannelProvider>() {
                public AsynchronousChannelProvider run() {
                    AsynchronousChannelProvider p = ProviderHolder.loadProviderFromProperty();
                    if (p != null) {
                        return p;
                    }
                    p = ProviderHolder.loadProviderAsService();
                    if (p != null) {
                        return p;
                    }
                    return DefaultAsynchronousChannelProvider.create();
                }
            });
        }

        private static AsynchronousChannelProvider loadProviderFromProperty() {
            String cn = System.getProperty("java.nio.channels.spi.AsynchronousChannelProvider");
            if (cn == null) {
                return null;
            }
            try {
                return (AsynchronousChannelProvider) Class.forName(cn, true, ClassLoader.getSystemClassLoader()).newInstance();
            } catch (ClassNotFoundException x) {
                throw new ServiceConfigurationError(null, x);
            } catch (IllegalAccessException x2) {
                throw new ServiceConfigurationError(null, x2);
            } catch (InstantiationException x3) {
                throw new ServiceConfigurationError(null, x3);
            } catch (SecurityException x4) {
                throw new ServiceConfigurationError(null, x4);
            }
        }

        private static AsynchronousChannelProvider loadProviderAsService() {
            Iterator<AsynchronousChannelProvider> i = ServiceLoader.load(AsynchronousChannelProvider.class, ClassLoader.getSystemClassLoader()).iterator();
            do {
                try {
                    return i.hasNext() ? (AsynchronousChannelProvider) i.next() : null;
                } catch (ServiceConfigurationError sce) {
                    if (!(sce.getCause() instanceof SecurityException)) {
                        throw sce;
                    }
                }
            } while (sce.getCause() instanceof SecurityException);
            throw sce;
        }
    }

    public abstract AsynchronousChannelGroup openAsynchronousChannelGroup(int i, ThreadFactory threadFactory) throws IOException;

    public abstract AsynchronousChannelGroup openAsynchronousChannelGroup(ExecutorService executorService, int i) throws IOException;

    public abstract AsynchronousServerSocketChannel openAsynchronousServerSocketChannel(AsynchronousChannelGroup asynchronousChannelGroup) throws IOException;

    public abstract AsynchronousSocketChannel openAsynchronousSocketChannel(AsynchronousChannelGroup asynchronousChannelGroup) throws IOException;

    private static Void checkPermission() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("asynchronousChannelProvider"));
        }
        return null;
    }

    private AsynchronousChannelProvider(Void ignore) {
    }

    protected AsynchronousChannelProvider() {
        this(checkPermission());
    }

    public static AsynchronousChannelProvider provider() {
        return ProviderHolder.provider;
    }
}
