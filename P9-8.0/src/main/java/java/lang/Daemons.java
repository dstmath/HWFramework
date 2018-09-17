package java.lang;

import android.system.Os;
import android.system.OsConstants;
import dalvik.system.VMRuntime;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.ref.FinalizerReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import libcore.util.EmptyArray;

public final class Daemons {
    private static final long MAX_FINALIZE_NANOS = 10000000000L;
    private static final int NANOS_PER_MILLI = 1000000;
    private static final int NANOS_PER_SECOND = 1000000000;

    private static abstract class Daemon implements Runnable {
        private String name;
        private boolean postZygoteFork;
        private Thread thread;

        public abstract void runInternal();

        protected Daemon(String name) {
            this.name = name;
        }

        public synchronized void start() {
            startInternal();
        }

        public synchronized void startPostZygoteFork() {
            this.postZygoteFork = true;
            startInternal();
        }

        public void startInternal() {
            if (this.thread != null) {
                throw new IllegalStateException("already running");
            }
            this.thread = new Thread(ThreadGroup.systemThreadGroup, this, this.name);
            this.thread.setDaemon(true);
            this.thread.start();
        }

        public void run() {
            if (this.postZygoteFork) {
                VMRuntime.getRuntime();
                VMRuntime.setSystemDaemonThreadPriority();
            }
            runInternal();
        }

        protected synchronized boolean isRunning() {
            return this.thread != null;
        }

        public synchronized void interrupt() {
            interrupt(this.thread);
        }

        public synchronized void interrupt(Thread thread) {
            if (thread == null) {
                throw new IllegalStateException("not running");
            }
            thread.interrupt();
        }

        public void stop() {
            Thread threadToStop;
            synchronized (this) {
                threadToStop = this.thread;
                this.thread = null;
            }
            if (threadToStop == null) {
                throw new IllegalStateException("not running");
            }
            interrupt(threadToStop);
            while (true) {
                try {
                    threadToStop.join();
                    return;
                } catch (InterruptedException e) {
                } catch (OutOfMemoryError e2) {
                }
            }
        }

        public synchronized StackTraceElement[] getStackTrace() {
            return this.thread != null ? this.thread.getStackTrace() : EmptyArray.STACK_TRACE_ELEMENT;
        }
    }

    private static class FinalizerDaemon extends Daemon {
        private static final FinalizerDaemon INSTANCE = new FinalizerDaemon();
        private Object finalizingObject = null;
        private volatile Object oldFinalizingObject;
        private final AtomicInteger progressCounter = new AtomicInteger(0);
        private final ReferenceQueue<Object> queue = FinalizerReference.queue;

        FinalizerDaemon() {
            super("FinalizerDaemon");
        }

        public void runInternal() {
            int localProgressCounter = this.progressCounter.get();
            while (isRunning()) {
                try {
                    FinalizerReference<?> finalizingReference = (FinalizerReference) this.queue.poll();
                    if (finalizingReference != null) {
                        this.finalizingObject = finalizingReference.get();
                        localProgressCounter++;
                        this.progressCounter.lazySet(localProgressCounter);
                    } else {
                        this.finalizingObject = null;
                        localProgressCounter++;
                        this.progressCounter.lazySet(localProgressCounter);
                        FinalizerWatchdogDaemon.INSTANCE.goToSleep();
                        finalizingReference = (FinalizerReference) this.queue.remove();
                        this.finalizingObject = finalizingReference.get();
                        localProgressCounter++;
                        this.progressCounter.set(localProgressCounter);
                        FinalizerWatchdogDaemon.INSTANCE.wakeUp();
                    }
                    doFinalize(finalizingReference);
                } catch (InterruptedException e) {
                } catch (OutOfMemoryError e2) {
                }
            }
        }

        @FindBugsSuppressWarnings({"FI_EXPLICIT_INVOCATION"})
        private void doFinalize(FinalizerReference<?> reference) {
            FinalizerReference.remove(reference);
            Object object = reference.get();
            reference.clear();
            try {
                object.finalize();
            } catch (Throwable th) {
                this.finalizingObject = null;
            }
            this.finalizingObject = null;
        }
    }

    private static class FinalizerWatchdogDaemon extends Daemon {
        private static final FinalizerWatchdogDaemon INSTANCE = new FinalizerWatchdogDaemon();
        private boolean needToWork = true;

        FinalizerWatchdogDaemon() {
            super("FinalizerWatchdogDaemon");
        }

        public void runInternal() {
            while (isRunning()) {
                if (sleepUntilNeeded()) {
                    Object finalizing = waitForFinalization();
                    if (!(finalizing == null || (VMRuntime.getRuntime().isDebuggerActive() ^ 1) == 0)) {
                        finalizerTimedOut(finalizing);
                        return;
                    }
                }
            }
        }

        private synchronized boolean sleepUntilNeeded() {
            while (!this.needToWork) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    return false;
                } catch (OutOfMemoryError e2) {
                    return false;
                }
            }
            return true;
        }

        private synchronized void goToSleep() {
            this.needToWork = false;
        }

        private synchronized void wakeUp() {
            this.needToWork = true;
            notify();
        }

        private synchronized boolean getNeedToWork() {
            return this.needToWork;
        }

        private boolean sleepFor(long durationNanos) {
            long startNanos = System.nanoTime();
            while (true) {
                long sleepMills = (durationNanos - (System.nanoTime() - startNanos)) / 1000000;
                if (sleepMills <= 0) {
                    return true;
                }
                try {
                    Thread.sleep(sleepMills);
                } catch (InterruptedException e) {
                    if (!isRunning()) {
                        return false;
                    }
                } catch (OutOfMemoryError e2) {
                    if (!isRunning()) {
                        return false;
                    }
                }
            }
        }

        private Object waitForFinalization() {
            long startCount = (long) FinalizerDaemon.INSTANCE.progressCounter.get();
            FinalizerDaemon.INSTANCE.oldFinalizingObject = FinalizerDaemon.INSTANCE.finalizingObject;
            if (sleepFor(Daemons.MAX_FINALIZE_NANOS) && getNeedToWork() && ((long) FinalizerDaemon.INSTANCE.progressCounter.get()) == startCount) {
                Object finalizing = FinalizerDaemon.INSTANCE.finalizingObject;
                sleepFor(500000000);
                if (getNeedToWork() && ((long) FinalizerDaemon.INSTANCE.progressCounter.get()) == startCount) {
                    return finalizing;
                }
                return null;
            }
            return null;
        }

        private static void finalizerTimedOut(Object object) {
            String message = object.getClass().getName() + ".finalize() timed out after " + 10 + " seconds";
            Exception syntheticException = new TimeoutException(message);
            syntheticException.setStackTrace(FinalizerDaemon.INSTANCE.getStackTrace());
            UncaughtExceptionHandler h = Thread.getDefaultUncaughtExceptionHandler();
            try {
                Os.kill(Os.getpid(), OsConstants.SIGQUIT);
                Thread.sleep(5000);
            } catch (Exception e) {
                System.logE("failed to send SIGQUIT", e);
            } catch (OutOfMemoryError e2) {
            }
            if (h == null) {
                System.logE(message, syntheticException);
                System.exit(2);
            }
            if (FinalizerDaemon.INSTANCE.oldFinalizingObject == FinalizerDaemon.INSTANCE.finalizingObject) {
                h.uncaughtException(Thread.currentThread(), syntheticException);
            }
        }
    }

    private static class HeapTaskDaemon extends Daemon {
        private static final HeapTaskDaemon INSTANCE = new HeapTaskDaemon();

        HeapTaskDaemon() {
            super("HeapTaskDaemon");
        }

        public synchronized void interrupt(Thread thread) {
            VMRuntime.getRuntime().stopHeapTaskProcessor();
        }

        public void runInternal() {
            synchronized (this) {
                if (isRunning()) {
                    VMRuntime.getRuntime().startHeapTaskProcessor();
                }
            }
            VMRuntime.getRuntime().runHeapTasks();
        }
    }

    private static class ReferenceQueueDaemon extends Daemon {
        private static final ReferenceQueueDaemon INSTANCE = new ReferenceQueueDaemon();

        ReferenceQueueDaemon() {
            super("ReferenceQueueDaemon");
        }

        public void runInternal() {
            while (isRunning()) {
                try {
                    Reference<?> list;
                    synchronized (ReferenceQueue.class) {
                        while (ReferenceQueue.unenqueued == null) {
                            ReferenceQueue.class.wait();
                        }
                        list = ReferenceQueue.unenqueued;
                        ReferenceQueue.unenqueued = null;
                    }
                    ReferenceQueue.enqueuePending(list);
                } catch (InterruptedException e) {
                } catch (OutOfMemoryError e2) {
                }
            }
        }
    }

    public static void start() {
        ReferenceQueueDaemon.INSTANCE.start();
        FinalizerDaemon.INSTANCE.start();
        FinalizerWatchdogDaemon.INSTANCE.start();
        HeapTaskDaemon.INSTANCE.start();
    }

    public static void startPostZygoteFork() {
        ReferenceQueueDaemon.INSTANCE.startPostZygoteFork();
        FinalizerDaemon.INSTANCE.startPostZygoteFork();
        FinalizerWatchdogDaemon.INSTANCE.startPostZygoteFork();
        HeapTaskDaemon.INSTANCE.startPostZygoteFork();
    }

    public static void stop() {
        HeapTaskDaemon.INSTANCE.stop();
        ReferenceQueueDaemon.INSTANCE.stop();
        FinalizerDaemon.INSTANCE.stop();
        FinalizerWatchdogDaemon.INSTANCE.stop();
    }

    public static void requestHeapTrim() {
        VMRuntime.getRuntime().requestHeapTrim();
    }

    public static void requestGC() {
        VMRuntime.getRuntime().requestConcurrentGC();
    }
}
