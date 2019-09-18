package sun.nio.ch;

import java.security.AccessController;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import sun.security.action.GetPropertyAction;

public class ThreadPool {
    private static final String DEFAULT_THREAD_POOL_INITIAL_SIZE = "java.nio.channels.DefaultThreadPool.initialSize";
    private static final String DEFAULT_THREAD_POOL_THREAD_FACTORY = "java.nio.channels.DefaultThreadPool.threadFactory";
    private final ExecutorService executor;
    private final boolean isFixed;
    private final int poolSize;

    private static class DefaultThreadPoolHolder {
        static final ThreadPool defaultThreadPool = ThreadPool.createDefault();

        private DefaultThreadPoolHolder() {
        }
    }

    private ThreadPool(ExecutorService executor2, boolean isFixed2, int poolSize2) {
        this.executor = executor2;
        this.isFixed = isFixed2;
        this.poolSize = poolSize2;
    }

    /* access modifiers changed from: package-private */
    public ExecutorService executor() {
        return this.executor;
    }

    /* access modifiers changed from: package-private */
    public boolean isFixedThreadPool() {
        return this.isFixed;
    }

    /* access modifiers changed from: package-private */
    public int poolSize() {
        return this.poolSize;
    }

    static ThreadFactory defaultThreadFactory() {
        return $$Lambda$ThreadPool$N88rfRTSpCtnK5fgJOWA6OwVQM.INSTANCE;
    }

    static /* synthetic */ Thread lambda$defaultThreadFactory$0(Runnable r) {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
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
            threadFactory = defaultThreadFactory();
        }
        return new ThreadPool(Executors.newCachedThreadPool(threadFactory), false, initialSize);
    }

    static ThreadPool create(int nThreads, ThreadFactory factory) {
        if (nThreads > 0) {
            return new ThreadPool(Executors.newFixedThreadPool(nThreads, factory), true, nThreads);
        }
        throw new IllegalArgumentException("'nThreads' must be > 0");
    }

    public static ThreadPool wrap(ExecutorService executor2, int initialSize) {
        if (executor2 != null) {
            if (executor2 instanceof ThreadPoolExecutor) {
                if (((ThreadPoolExecutor) executor2).getMaximumPoolSize() == Integer.MAX_VALUE) {
                    if (initialSize < 0) {
                        initialSize = Runtime.getRuntime().availableProcessors();
                    } else {
                        initialSize = 0;
                    }
                }
            } else if (initialSize < 0) {
                initialSize = 0;
            }
            return new ThreadPool(executor2, false, initialSize);
        }
        throw new NullPointerException("'executor' is null");
    }

    private static int getDefaultThreadPoolInitialSize() {
        String propValue = (String) AccessController.doPrivileged(new GetPropertyAction(DEFAULT_THREAD_POOL_INITIAL_SIZE));
        if (propValue == null) {
            return -1;
        }
        try {
            return Integer.parseInt(propValue);
        } catch (NumberFormatException x) {
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
        } catch (ClassNotFoundException x) {
            throw new Error((Throwable) x);
        } catch (InstantiationException x2) {
            throw new Error((Throwable) x2);
        } catch (IllegalAccessException x3) {
            throw new Error((Throwable) x3);
        }
    }
}
