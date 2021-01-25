package ohos.app.dispatcher.threading;

import java.util.concurrent.atomic.AtomicInteger;

class DefaultThreadFactory implements ThreadFactory {
    private final AtomicInteger index = new AtomicInteger(1);

    DefaultThreadFactory() {
    }

    @Override // ohos.app.dispatcher.threading.ThreadFactory
    public Thread create(Runnable runnable) {
        Thread thread = new Thread(runnable, "PoolThread-" + this.index.getAndIncrement());
        if (thread.isDaemon()) {
            thread.setDaemon(false);
        }
        return thread;
    }
}
