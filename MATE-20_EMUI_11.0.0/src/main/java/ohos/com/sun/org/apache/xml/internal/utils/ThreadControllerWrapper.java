package ohos.com.sun.org.apache.xml.internal.utils;

public class ThreadControllerWrapper {
    private static ThreadController m_tpool = new ThreadController();

    public static Thread runThread(Runnable runnable, int i) {
        return m_tpool.run(runnable, i);
    }

    public static void waitThread(Thread thread, Runnable runnable) throws InterruptedException {
        m_tpool.waitThread(thread, runnable);
    }

    public static class ThreadController {

        /* access modifiers changed from: package-private */
        public final class SafeThread extends Thread {
            private volatile boolean ran = false;

            public SafeThread(Runnable runnable) {
                super(runnable);
            }

            @Override // java.lang.Thread, java.lang.Runnable
            public final void run() {
                if (Thread.currentThread() == this) {
                    synchronized (this) {
                        if (!this.ran) {
                            this.ran = true;
                        } else {
                            throw new IllegalStateException("The run() method in a SafeThread cannot be called more than once.");
                        }
                    }
                    super.run();
                    return;
                }
                throw new IllegalStateException("The run() method in a SafeThread cannot be called from another thread.");
            }
        }

        public Thread run(Runnable runnable, int i) {
            SafeThread safeThread = new SafeThread(runnable);
            safeThread.start();
            return safeThread;
        }

        public void waitThread(Thread thread, Runnable runnable) throws InterruptedException {
            thread.join();
        }
    }
}
