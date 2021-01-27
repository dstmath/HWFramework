package ohos.agp.utils;

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
import ohos.tools.Bytrace;

public class MemoryCleanerRegistry {
    private static final BlockingQueue<WeakReference<?>> GC_LIST = new LinkedBlockingQueue();
    private static final MemoryCleanerRegistry INSTANCE = new MemoryCleanerRegistry();
    private static final HiLogLabel LOG_TAG = new HiLogLabel(3, (int) LogDomain.END, "MemoryCleanerRegistry");
    private static final int MAX_CAPACITY_ONE_FRAME = 10000;
    private static final int MAX_DURATION_ONE_FRAME_IN_MICRO = 5000;
    private static final ReferenceQueue<Object> QUEUE = new ReferenceQueue<>();
    private final Map<WeakReference<?>, MemoryCleaner> mCleanerMap = new ConcurrentHashMap();
    private final UIReleaseThread mReleaseThread = new UIReleaseThread("UI Release Thread");

    private static native int nativeGetRefCount();

    private MemoryCleanerRegistry() {
        this.mReleaseThread.setUncaughtExceptionHandler(new UncaughtExceptionLogger());
        this.mReleaseThread.start();
        VsyncScheduler.getInstance().addFrameCallbackForSystem(new VsyncScheduler.FrameCallback() {
            /* class ohos.agp.utils.$$Lambda$MemoryCleanerRegistry$EUoElZgtBC5mU4cmrMhEMHVzIV0 */

            @Override // ohos.agp.vsync.VsyncScheduler.FrameCallback
            public final void doFrame(long j) {
                MemoryCleanerRegistry.this.lambda$new$0$MemoryCleanerRegistry(j);
            }
        });
    }

    public /* synthetic */ void lambda$new$0$MemoryCleanerRegistry(long j) {
        runCleaner();
    }

    public static MemoryCleanerRegistry getInstance() {
        return INSTANCE;
    }

    public void register(Object obj, MemoryCleaner memoryCleaner) {
        if (obj == null || memoryCleaner == null) {
            HiLog.error(LOG_TAG, "Can't register MemoryCleaner!", new Object[0]);
            return;
        }
        this.mCleanerMap.put(new WeakReference<>(obj, QUEUE), memoryCleaner);
    }

    public void registerWithNativeBind(Object obj, MemoryCleaner memoryCleaner, long j) {
        if (obj == null || memoryCleaner == null || j == 0) {
            HiLog.error(LOG_TAG, "Can't register MemoryCleaner with native bind!", new Object[0]);
            return;
        }
        WeakReference<?> weakReference = new WeakReference<>(obj, QUEUE);
        this.mCleanerMap.put(weakReference, memoryCleaner);
        CallbackHelper.add(j, weakReference);
    }

    private void runCleaner() {
        Bytrace.startTrace(274877906944L, "MemoryCleaner");
        long nanoTime = System.nanoTime();
        long j = nanoTime;
        int i = 0;
        int i2 = 0;
        while (i < 10000) {
            i++;
            WeakReference<?> poll = GC_LIST.poll();
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
            HiLog.debug(LOG_TAG, "Do ui memory clean, count: %{public}d, duration: %{public}l, remaining for clean: %{public}d, total remaining: %{public}d, count in callback helper: %{public}d, ref count in jni: %{public}d", new Object[]{Integer.valueOf(i2), Long.valueOf((j - nanoTime) / 1000), Integer.valueOf(GC_LIST.size()), Integer.valueOf(this.mCleanerMap.size()), Integer.valueOf(CallbackHelper.getCount()), Integer.valueOf(nativeGetRefCount())});
            if (this.mCleanerMap.size() > 10000) {
                HiLog.debug(LOG_TAG, "Too many agp ui objects, requiring GC", new Object[0]);
                System.gc();
            }
        }
        Bytrace.finishTrace(274877906944L, "MemoryCleaner");
    }

    private static class UIReleaseThread extends Thread {
        public UIReleaseThread(String str) {
            super(str);
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            while (true) {
                try {
                    Reference remove = MemoryCleanerRegistry.QUEUE.remove();
                    if (remove instanceof WeakReference) {
                        MemoryCleanerRegistry.GC_LIST.put((WeakReference) remove);
                        VsyncScheduler.getInstance().postRequestVsync(null);
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
