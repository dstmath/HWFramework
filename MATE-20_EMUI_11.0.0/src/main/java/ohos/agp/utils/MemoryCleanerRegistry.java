package ohos.agp.utils;

import android.os.Trace;
import android.util.Log;
import java.lang.Thread;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.vsync.VsyncScheduler;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class MemoryCleanerRegistry {
    private static final MemoryCleanerRegistry INSTANCE = new MemoryCleanerRegistry();
    private static final HiLogLabel LOG_TAG = new HiLogLabel(3, (int) LogDomain.END, "MemoryCleanerRegistry");
    private static final int MAX_CAPACITY_ONE_FRAME = 10000;
    private static final int MAX_DURATION_ONE_FRAME_IN_MICRO = 5000;
    private static BlockingQueue<WeakReference<?>> sGcList = new LinkedBlockingQueue();
    private static ReferenceQueue<Object> sQueue = new ReferenceQueue<>();
    private Map<WeakReference<?>, MemoryCleaner> mCleanerMap = new ConcurrentHashMap();
    private UIReleaseThread mReleaseThread = new UIReleaseThread("UI Release Thread");

    private static native int nativeGetRefCount();

    private MemoryCleanerRegistry() {
        this.mReleaseThread.setUncaughtExceptionHandler(new UncaughtExceptionLogger());
        this.mReleaseThread.start();
        VsyncScheduler.getInstance().addFrameCallbackForSystem(new VsyncScheduler.FrameCallback() {
            /* class ohos.agp.utils.MemoryCleanerRegistry.AnonymousClass1 */

            @Override // ohos.agp.vsync.VsyncScheduler.FrameCallback
            public void doFrame(long j) {
                MemoryCleanerRegistry.this.runCleaner();
            }
        });
    }

    public static MemoryCleanerRegistry getInstance() {
        return INSTANCE;
    }

    public void register(Object obj, MemoryCleaner memoryCleaner) {
        if (obj == null || memoryCleaner == null) {
            HiLog.error(LOG_TAG, "Can't register MemoryCleaner!", new Object[0]);
            return;
        }
        this.mCleanerMap.put(new WeakReference<>(obj, sQueue), memoryCleaner);
    }

    public void registerWithNativeBind(Object obj, MemoryCleaner memoryCleaner, long j) {
        if (obj == null || memoryCleaner == null || j == 0) {
            HiLog.error(LOG_TAG, "Can't register MemoryCleaner with native bind!", new Object[0]);
            return;
        }
        WeakReference<?> weakReference = new WeakReference<>(obj, sQueue);
        this.mCleanerMap.put(weakReference, memoryCleaner);
        CallbackHelper.add(j, weakReference);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void runCleaner() {
        Trace.traceBegin(2, "MemoryCleaner");
        long nanoTime = System.nanoTime();
        int i = 0;
        long j = nanoTime;
        int i2 = 0;
        while (i < 10000) {
            i++;
            WeakReference<?> poll = sGcList.poll();
            if (poll == null) {
                break;
            }
            MemoryCleaner remove = this.mCleanerMap.remove(poll);
            CallbackHelper.remove(poll);
            if (remove != null) {
                remove.run();
                i2++;
                j = System.nanoTime();
                if ((j - nanoTime) / 1000 > 5000) {
                    break;
                }
            }
        }
        if (i2 > 0) {
            Log.d("MemoryCleanerRegistry", "Do ui memory clean, count: " + i2 + ", duration: " + ((j - nanoTime) / 1000) + ", remaining for clean: " + sGcList.size() + ", total remaining: " + this.mCleanerMap.size() + ", count in callback helper: " + CallbackHelper.getCount() + ", ref count in jni: " + nativeGetRefCount());
            if (this.mCleanerMap.size() > 10000) {
                Log.d("MemoryCleanerRegistry", "Too many agp ui objects, requiring GC");
                System.gc();
            }
        }
        Trace.traceEnd(2);
    }

    private static class UIReleaseThread extends Thread {
        public UIReleaseThread(String str) {
            super(str);
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            while (true) {
                try {
                    Reference remove = MemoryCleanerRegistry.sQueue.remove();
                    if (remove instanceof WeakReference) {
                        MemoryCleanerRegistry.sGcList.put((WeakReference) remove);
                    }
                } catch (InterruptedException e) {
                    HiLog.error(MemoryCleanerRegistry.LOG_TAG, "UI Release Thread exception in queue: %{public}s", new Object[]{e.getMessage()});
                }
            }
        }
    }

    private static class UncaughtExceptionLogger implements Thread.UncaughtExceptionHandler {
        private UncaughtExceptionLogger() {
        }

        @Override // java.lang.Thread.UncaughtExceptionHandler
        public void uncaughtException(Thread thread, Throwable th) {
            HiLog.error(MemoryCleanerRegistry.LOG_TAG, "UI Release Thread exception: %{public}s", new Object[]{th.getMessage()});
        }
    }
}
