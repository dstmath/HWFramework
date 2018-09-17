package android.app;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.ExponentiallyBucketedHistogram;
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
    private static final LinkedList<Runnable> sFinishers = new LinkedList();
    @GuardedBy("sLock")
    private static Handler sHandler = null;
    private static final Object sLock = new Object();
    private static Object sProcessingWork = new Object();
    @GuardedBy("sLock")
    private static final LinkedList<Runnable> sWork = new LinkedList();

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
        long startTime = System.currentTimeMillis();
        Handler handler = getHandler();
        synchronized (sLock) {
            if (handler.hasMessages(1)) {
                handler.removeMessages(1);
            }
            sCanDelay = false;
        }
        ThreadPolicy oldPolicy = StrictMode.allowThreadDiskWrites();
        try {
            processPendingWork();
            while (true) {
                try {
                    Runnable finisher;
                    synchronized (sLock) {
                        finisher = (Runnable) sFinishers.poll();
                    }
                    if (finisher == null) {
                        break;
                    }
                    finisher.run();
                } finally {
                    sCanDelay = true;
                }
            }
            synchronized (sLock) {
                long waitTime = System.currentTimeMillis() - startTime;
                if (waitTime > 0) {
                    mWaitTimes.add(Long.valueOf(waitTime).intValue());
                    mNumWaits++;
                    if (mNumWaits % 1024 == 0 || waitTime > 512) {
                        mWaitTimes.log(LOG_TAG, "waited: ");
                    }
                }
            }
        } finally {
            StrictMode.setThreadPolicy(oldPolicy);
        }
    }

    public static void queue(Runnable work, boolean shouldDelay) {
        Handler handler = getHandler();
        synchronized (sLock) {
            sWork.add(work);
            if (shouldDelay && sCanDelay) {
                handler.sendEmptyMessageDelayed(1, DELAY);
            } else {
                handler.sendEmptyMessage(1);
            }
        }
    }

    public static boolean hasPendingWork() {
        boolean isEmpty;
        synchronized (sLock) {
            isEmpty = sWork.isEmpty() ^ 1;
        }
        return isEmpty;
    }

    private static void processPendingWork() {
        synchronized (sProcessingWork) {
            LinkedList<Runnable> work;
            synchronized (sLock) {
                work = (LinkedList) sWork.clone();
                sWork.clear();
                getHandler().removeMessages(1);
            }
            if (work.size() > 0) {
                for (Runnable w : work) {
                    w.run();
                }
            }
        }
    }
}
