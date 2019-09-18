package android.app;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.ExponentiallyBucketedHistogram;
import java.util.Iterator;
import java.util.LinkedList;

public class QueuedWork {
    private static final boolean DEBUG = false;
    private static final long DELAY = 100;
    private static final String LOG_TAG = QueuedWork.class.getSimpleName();
    private static final long MAX_WAIT_TIME_MILLIS = 512;
    private static int mNumWaits = 0;
    @GuardedBy("sLock")
    private static final ExponentiallyBucketedHistogram mWaitTimes = new ExponentiallyBucketedHistogram(16);
    @GuardedBy("sLock")
    private static boolean sCanDelay = true;
    @GuardedBy("sLock")
    private static final LinkedList<Runnable> sFinishers = new LinkedList<>();
    @GuardedBy("sLock")
    private static Handler sHandler = null;
    private static final Object sLock = new Object();
    private static Object sProcessingWork = new Object();
    @GuardedBy("sLock")
    private static final LinkedList<Runnable> sWork = new LinkedList<>();

    private static class QueuedWorkHandler extends Handler {
        static final int MSG_RUN = 1;

        QueuedWorkHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                QueuedWork.processPendingWork();
            }
        }
    }

    private static Handler getHandler() {
        Handler handler;
        synchronized (sLock) {
            if (sHandler == null) {
                HandlerThread handlerThread = new HandlerThread("queued-work-looper", -2);
                handlerThread.start();
                sHandler = new QueuedWorkHandler(handlerThread.getLooper());
            }
            handler = sHandler;
        }
        return handler;
    }

    public static void addFinisher(Runnable finisher) {
        synchronized (sLock) {
            sFinishers.add(finisher);
        }
    }

    public static void removeFinisher(Runnable finisher) {
        synchronized (sLock) {
            sFinishers.remove(finisher);
        }
    }

    public static void waitToFinish() {
        Runnable finisher;
        long startTime = System.currentTimeMillis();
        Handler handler = getHandler();
        synchronized (sLock) {
            if (handler.hasMessages(1)) {
                handler.removeMessages(1);
            }
            sCanDelay = false;
        }
        StrictMode.ThreadPolicy oldPolicy = StrictMode.allowThreadDiskWrites();
        try {
            processPendingWork();
            while (true) {
                try {
                    synchronized (sLock) {
                        finisher = sFinishers.poll();
                    }
                    if (finisher == null) {
                        sCanDelay = true;
                        synchronized (sLock) {
                            long waitTime = System.currentTimeMillis() - startTime;
                            if (waitTime > 0 || 0 != 0) {
                                mWaitTimes.add(Long.valueOf(waitTime).intValue());
                                mNumWaits++;
                                if (mNumWaits % 1024 == 0 || waitTime > 512) {
                                    mWaitTimes.log(LOG_TAG, "waited: ");
                                }
                            }
                        }
                        return;
                    }
                    finisher.run();
                } catch (Throwable th) {
                    sCanDelay = true;
                    throw th;
                }
            }
            while (true) {
            }
        } finally {
            StrictMode.setThreadPolicy(oldPolicy);
        }
    }

    public static void queue(Runnable work, boolean shouldDelay) {
        Handler handler = getHandler();
        synchronized (sLock) {
            sWork.add(work);
            if (!shouldDelay || !sCanDelay) {
                handler.sendEmptyMessage(1);
            } else {
                handler.sendEmptyMessageDelayed(1, DELAY);
            }
        }
    }

    public static boolean hasPendingWork() {
        boolean z;
        synchronized (sLock) {
            z = !sWork.isEmpty();
        }
        return z;
    }

    /* access modifiers changed from: private */
    public static void processPendingWork() {
        LinkedList<Runnable> work;
        synchronized (sProcessingWork) {
            synchronized (sLock) {
                work = (LinkedList) sWork.clone();
                sWork.clear();
                getHandler().removeMessages(1);
            }
            if (work.size() > 0) {
                Iterator it = work.iterator();
                while (it.hasNext()) {
                    ((Runnable) it.next()).run();
                }
            }
        }
    }
}
