package sun.nio.ch;

import java.security.AccessController;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import sun.security.action.GetPropertyAction;
import sun.util.logging.PlatformLogger;

public class ThreadPool {
    private static final String DEFAULT_THREAD_POOL_INITIAL_SIZE = "java.nio.channels.DefaultThreadPool.initialSize";
    private static final String DEFAULT_THREAD_POOL_THREAD_FACTORY = "java.nio.channels.DefaultThreadPool.threadFactory";
    private static final ThreadFactory defaultThreadFactory = null;
    private final ExecutorService executor;
    private final boolean isFixed;
    private final int poolSize;

    private static class DefaultThreadPoolHolder {
        static final ThreadPool defaultThreadPool = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.nio.ch.ThreadPool.DefaultThreadPoolHolder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.nio.ch.ThreadPool.DefaultThreadPoolHolder.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.ThreadPool.DefaultThreadPoolHolder.<clinit>():void");
        }

        private DefaultThreadPoolHolder() {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.nio.ch.ThreadPool.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.nio.ch.ThreadPool.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.ThreadPool.<clinit>():void");
    }

    private ThreadPool(ExecutorService executor, boolean isFixed, int poolSize) {
        this.executor = executor;
        this.isFixed = isFixed;
        this.poolSize = poolSize;
    }

    ExecutorService executor() {
        return this.executor;
    }

    boolean isFixedThreadPool() {
        return this.isFixed;
    }

    int poolSize() {
        return this.poolSize;
    }

    static ThreadFactory defaultThreadFactory() {
        return defaultThreadFactory;
    }

    static ThreadPool getDefault() {
        return DefaultThreadPoolHolder.defaultThreadPool;
    }

    static ThreadPool createDefault() {
        int initialSize = getDefaultThreadPoolInitialSize();
        if (initialSize < 0) {
            initialSize = Runtime.getRuntime().availableProcessors();
        }
        ThreadFactory threadFactory = getDefaultThreadPoolThreadFactory();
        if (threadFactory == null) {
            threadFactory = defaultThreadFactory;
        }
        return new ThreadPool(new ThreadPoolExecutor(0, PlatformLogger.OFF, Long.MAX_VALUE, TimeUnit.MILLISECONDS, new SynchronousQueue(), threadFactory), false, initialSize);
    }

    static ThreadPool create(int nThreads, ThreadFactory factory) {
        if (nThreads > 0) {
            return new ThreadPool(Executors.newFixedThreadPool(nThreads, factory), true, nThreads);
        }
        throw new IllegalArgumentException("'nThreads' must be > 0");
    }

    public static ThreadPool wrap(ExecutorService executor, int initialSize) {
        if (executor == null) {
            throw new NullPointerException("'executor' is null");
        }
        if (executor instanceof ThreadPoolExecutor) {
            if (((ThreadPoolExecutor) executor).getMaximumPoolSize() == PlatformLogger.OFF) {
                if (initialSize < 0) {
                    initialSize = Runtime.getRuntime().availableProcessors();
                } else {
                    initialSize = 0;
                }
            }
        } else if (initialSize < 0) {
            initialSize = 0;
        }
        return new ThreadPool(executor, false, initialSize);
    }

    private static int getDefaultThreadPoolInitialSize() {
        String propValue = (String) AccessController.doPrivileged(new GetPropertyAction(DEFAULT_THREAD_POOL_INITIAL_SIZE));
        if (propValue == null) {
            return -1;
        }
        try {
            return Integer.parseInt(propValue);
        } catch (Object x) {
            throw new Error("Value of property 'java.nio.channels.DefaultThreadPool.initialSize' is invalid: " + x);
        }
    }

    private static ThreadFactory getDefaultThreadPoolThreadFactory() {
        String propValue = (String) AccessController.doPrivileged(new GetPropertyAction(DEFAULT_THREAD_POOL_THREAD_FACTORY));
        if (propValue == null) {
            return null;
        }
        try {
            return (ThreadFactory) Class.forName(propValue, true, ClassLoader.getSystemClassLoader()).newInstance();
        } catch (Throwable x) {
            throw new Error(x);
        } catch (Throwable x2) {
            throw new Error(x2);
        } catch (Throwable x3) {
            throw new Error(x3);
        }
    }
}
