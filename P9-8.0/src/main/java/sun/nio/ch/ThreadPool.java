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
        return new -$Lambda$MfIm8har2wOVFJJtpwtPJYPKn8o();
    }

    static /* synthetic */ Thread lambda$-sun_nio_ch_ThreadPool_2676(Runnable r) {
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

    public static ThreadPool wrap(ExecutorService executor, int initialSize) {
        if (executor == null) {
            throw new NullPointerException("'executor' is null");
        }
        if (executor instanceof ThreadPoolExecutor) {
            if (((ThreadPoolExecutor) executor).getMaximumPoolSize() == Integer.MAX_VALUE) {
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
