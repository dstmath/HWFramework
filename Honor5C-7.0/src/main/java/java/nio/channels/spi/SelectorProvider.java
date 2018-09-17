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
    private static final Object lock = null;
    private static SelectorProvider provider;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.nio.channels.spi.SelectorProvider.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.nio.channels.spi.SelectorProvider.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.nio.channels.spi.SelectorProvider.<clinit>():void");
    }

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
            if (provider != null) {
                SelectorProvider selectorProvider = provider;
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
