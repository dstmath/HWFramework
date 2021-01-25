package android.arch.core.executor;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import java.util.concurrent.Executor;

@RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
public class ArchTaskExecutor extends TaskExecutor {
    @NonNull
    private static final Executor sIOThreadExecutor = new Executor() {
        /* class android.arch.core.executor.ArchTaskExecutor.AnonymousClass2 */

        @Override // java.util.concurrent.Executor
        public void execute(Runnable command) {
            ArchTaskExecutor.getInstance().executeOnDiskIO(command);
        }
    };
    private static volatile ArchTaskExecutor sInstance;
    @NonNull
    private static final Executor sMainThreadExecutor = new Executor() {
        /* class android.arch.core.executor.ArchTaskExecutor.AnonymousClass1 */

        @Override // java.util.concurrent.Executor
        public void execute(Runnable command) {
            ArchTaskExecutor.getInstance().postToMainThread(command);
        }
    };
    @NonNull
    private TaskExecutor mDefaultTaskExecutor = new DefaultTaskExecutor();
    @NonNull
    private TaskExecutor mDelegate = this.mDefaultTaskExecutor;

    private ArchTaskExecutor() {
    }

    @NonNull
    public static ArchTaskExecutor getInstance() {
        if (sInstance != null) {
            return sInstance;
        }
        synchronized (ArchTaskExecutor.class) {
            if (sInstance == null) {
                sInstance = new ArchTaskExecutor();
            }
        }
        return sInstance;
    }

    public void setDelegate(@Nullable TaskExecutor taskExecutor) {
        this.mDelegate = taskExecutor == null ? this.mDefaultTaskExecutor : taskExecutor;
    }

    @Override // android.arch.core.executor.TaskExecutor
    public void executeOnDiskIO(Runnable runnable) {
        this.mDelegate.executeOnDiskIO(runnable);
    }

    @Override // android.arch.core.executor.TaskExecutor
    public void postToMainThread(Runnable runnable) {
        this.mDelegate.postToMainThread(runnable);
    }

    @NonNull
    public static Executor getMainThreadExecutor() {
        return sMainThreadExecutor;
    }

    @NonNull
    public static Executor getIOThreadExecutor() {
        return sIOThreadExecutor;
    }

    @Override // android.arch.core.executor.TaskExecutor
    public boolean isMainThread() {
        return this.mDelegate.isMainThread();
    }
}
