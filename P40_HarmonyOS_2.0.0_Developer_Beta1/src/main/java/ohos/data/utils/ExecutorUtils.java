package ohos.data.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class ExecutorUtils {
    private static final ConcurrentHashMap<String, ExecutorService> EXECUTOR_SERVICE_MAP = new ConcurrentHashMap<>();
    private static final int INT_1 = 1;
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109520, "ExecutorUtils");

    private ExecutorUtils() {
    }

    public static ExecutorService getExecutorService(String str, int i, int i2, long j, Integer num) {
        int i3;
        HiLog.debug(LABEL, "start. param task serviceName is %{public}s, corePoolSize is %{public}d, maxPoolSize is %{public}d, keepAliveTime is %{public}d, blockQueueSize is %{public}d", new Object[]{str, Integer.valueOf(i), Integer.valueOf(i2), Long.valueOf(j), num});
        if (num == null) {
            i3 = Integer.MAX_VALUE;
        } else {
            i3 = num.intValue();
        }
        return EXECUTOR_SERVICE_MAP.computeIfAbsent(str, new Function(i, i2, j, i3, str) {
            /* class ohos.data.utils.$$Lambda$ExecutorUtils$LWs8qfYy2rrYyhNfrThZ0kCMB9E */
            private final /* synthetic */ int f$0;
            private final /* synthetic */ int f$1;
            private final /* synthetic */ long f$2;
            private final /* synthetic */ int f$3;
            private final /* synthetic */ String f$4;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r5;
                this.f$4 = r6;
            }

            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                return ExecutorUtils.lambda$getExecutorService$1(this.f$0, this.f$1, this.f$2, this.f$3, this.f$4, (String) obj);
            }
        });
    }

    static /* synthetic */ ExecutorService lambda$getExecutorService$1(int i, int i2, long j, int i3, String str, String str2) {
        return new ThreadPoolExecutor(i, i2, j, TimeUnit.SECONDS, new LinkedBlockingQueue(i3), new ThreadFactory(str) {
            /* class ohos.data.utils.$$Lambda$ExecutorUtils$RMf0CJlaRMkDhzSeM7zIiKx_3TA */
            private final /* synthetic */ String f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.concurrent.ThreadFactory
            public final Thread newThread(Runnable runnable) {
                return ExecutorUtils.lambda$getExecutorService$0(this.f$0, runnable);
            }
        }, new ThreadPoolExecutor.DiscardPolicy());
    }

    static /* synthetic */ Thread lambda$getExecutorService$0(String str, Runnable runnable) {
        AtomicInteger atomicInteger = new AtomicInteger(1);
        return new Thread(runnable, str + atomicInteger.getAndIncrement());
    }
}
