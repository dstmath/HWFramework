package java.nio.channels.spi;

import java.io.IOException;
import java.net.ProtocolFamily;
import java.nio.channels.Channel;
import java.nio.channels.DatagramChannel;
import java.nio.channels.Pipe;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import sun.nio.ch.DefaultSelectorProvider;

public abstract class SelectorProvider {
    private static final Object lock = new Object();
    private static SelectorProvider provider = null;

    public abstract DatagramChannel openDatagramChannel() throws IOException;

    public abstract DatagramChannel openDatagramChannel(ProtocolFamily protocolFamily) throws IOException;

    public abstract Pipe openPipe() throws IOException;

    public abstract AbstractSelector openSelector() throws IOException;

    public abstract ServerSocketChannel openServerSocketChannel() throws IOException;

    public abstract SocketChannel openSocketChannel() throws IOException;

    protected SelectorProvider() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("selectorProvider"));
        }
    }

    private static boolean loadProviderFromProperty() {
        String cn = System.getProperty("java.nio.channels.spi.SelectorProvider");
        if (cn == null) {
            return false;
        }
        try {
            provider = (SelectorProvider) Class.forName(cn, true, ClassLoader.getSystemClassLoader()).newInstance();
            return true;
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

    private static boolean loadProviderAsService() {
        Iterator<SelectorProvider> i = ServiceLoader.load(SelectorProvider.class, ClassLoader.getSystemClassLoader()).iterator();
        do {
            try {
                if (!i.hasNext()) {
                    return false;
                }
                provider = (SelectorProvider) i.next();
                return true;
            } catch (ServiceConfigurationError sce) {
                if (!(sce.getCause() instanceof SecurityException)) {
                    throw sce;
                }
            }
        } while (sce.getCause() instanceof SecurityException);
        throw sce;
    }

    public static SelectorProvider provider() {
        synchronized (lock) {
            SelectorProvider selectorProvider;
            if (provider != null) {
                selectorProvider = provider;
                return selectorProvider;
            }
            selectorProvider = (SelectorProvider) AccessController.doPrivileged(new PrivilegedAction<SelectorProvider>() {
                public SelectorProvider run() {
                    if (SelectorProvider.loadProviderFromProperty()) {
                        return SelectorProvider.provider;
                    }
                    if (SelectorProvider.loadProviderAsService()) {
                        return SelectorProvider.provider;
                    }
                    SelectorProvider.provider = DefaultSelectorProvider.create();
                    return SelectorProvider.provider;
                }
            });
            return selectorProvider;
        }
    }

    public Channel inheritedChannel() throws IOException {
        return null;
    }
}
