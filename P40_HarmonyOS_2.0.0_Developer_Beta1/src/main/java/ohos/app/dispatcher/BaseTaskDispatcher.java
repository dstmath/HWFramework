package ohos.app.dispatcher;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import ohos.app.dispatcher.task.Revocable;
import ohos.app.dispatcher.task.Task;
import ohos.app.dispatcher.task.TaskExecuteInterceptor;
import ohos.app.dispatcher.task.TaskPriority;
import ohos.appexecfwk.utils.AppLog;
import ohos.hiviewdfx.HiTrace;
import ohos.hiviewdfx.HiTraceId;

/* access modifiers changed from: package-private */
public abstract class BaseTaskDispatcher implements TaskDispatcher {
    private static final AtomicInteger SEQUENCE = new AtomicInteger(0);
    protected final String dispatcherName;
    protected final TaskPriority taskPriority;

    @Override // ohos.app.dispatcher.TaskDispatcher
    /* renamed from: asyncDispatch */
    public abstract Revocable lambda$groupDispatchNotify$0$BaseTaskDispatcher(Runnable runnable);

    @Override // ohos.app.dispatcher.TaskDispatcher
    public abstract Revocable delayDispatch(Runnable runnable, long j);

    /* access modifiers changed from: protected */
    public TaskExecuteInterceptor getInterceptor() {
        return null;
    }

    @Override // ohos.app.dispatcher.TaskDispatcher
    public abstract void syncDispatch(Runnable runnable);

    BaseTaskDispatcher(String str, TaskPriority taskPriority2) {
        if (str == null) {
            str = "Dispatcher-" + SEQUENCE.getAndIncrement();
        }
        this.dispatcherName = str;
        this.taskPriority = taskPriority2;
    }

    @Override // ohos.app.dispatcher.TaskDispatcher
    public void syncDispatchBarrier(Runnable runnable) {
        syncDispatch(runnable);
    }

    @Override // ohos.app.dispatcher.TaskDispatcher
    public void asyncDispatchBarrier(Runnable runnable) {
        lambda$groupDispatchNotify$0$BaseTaskDispatcher(runnable);
    }

    @Override // ohos.app.dispatcher.TaskDispatcher
    public Group createDispatchGroup() {
        return new GroupImpl();
    }

    @Override // ohos.app.dispatcher.TaskDispatcher
    public Revocable asyncGroupDispatch(Group group, Runnable runnable) {
        return lambda$groupDispatchNotify$0$BaseTaskDispatcher(runnable);
    }

    @Override // ohos.app.dispatcher.TaskDispatcher
    public boolean groupDispatchWait(Group group, long j) {
        return castToGroupImpl(group).awaitAllTasks(j);
    }

    @Override // ohos.app.dispatcher.TaskDispatcher
    public void groupDispatchNotify(Group group, Runnable runnable) {
        castToGroupImpl(group).addNotification(new Runnable(runnable) {
            /* class ohos.app.dispatcher.$$Lambda$BaseTaskDispatcher$v4D2Y11ww6mpcpPPtB6kuc4kjh4 */
            private final /* synthetic */ Runnable f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                BaseTaskDispatcher.this.lambda$groupDispatchNotify$0$BaseTaskDispatcher(this.f$1);
            }
        });
    }

    /* access modifiers changed from: private */
    public static class IteratorTask implements Runnable {
        private final long index;
        private final Consumer<Long> innerTask;

        IteratorTask(long j, Consumer<Long> consumer) {
            this.index = j;
            this.innerTask = consumer;
        }

        @Override // java.lang.Runnable
        public void run() {
            this.innerTask.accept(Long.valueOf(this.index));
        }
    }

    @Override // ohos.app.dispatcher.TaskDispatcher
    public void applyDispatch(Consumer<Long> consumer, long j) {
        if (consumer != null) {
            if (j > 0) {
                for (long j2 = 0; j2 < j; j2++) {
                    lambda$groupDispatchNotify$0$BaseTaskDispatcher(new IteratorTask(j2, consumer));
                }
                return;
            }
            throw new IllegalArgumentException("iterations must giant than 0");
        }
        throw new NullPointerException("task cannot be null");
    }

    /* access modifiers changed from: protected */
    public void check(Runnable runnable) {
        if (runnable == null) {
            throw new NullPointerException("dispatch task cannot be null.");
        }
    }

    /* access modifiers changed from: protected */
    public GroupImpl castToGroupImpl(Group group) {
        if (group == null) {
            throw new NullPointerException("group cannot be null.");
        } else if (group instanceof GroupImpl) {
            return (GroupImpl) group;
        } else {
            throw new ClassCastException("Group is not target type");
        }
    }

    public TaskPriority getPriority() {
        return this.taskPriority;
    }

    public HiTraceId tracePointBeforePost(Task task, boolean z, String str) {
        HiTraceId id = HiTrace.getId();
        if (!HiTraceHelper.isValid(id)) {
            AppLog.d("tracePointBeforePost id is illegal", new Object[0]);
            return null;
        } else if (z && !id.isFlagEnabled(1)) {
            AppLog.d("tracePointBeforePost the async flag is not enabled", new Object[0]);
            return null;
        } else if (task == null) {
            AppLog.d("tracePointBeforePost the task is null", new Object[0]);
            return null;
        } else {
            task.setTaskHiTraceId(HiTrace.createSpan());
            HiTraceHelper.tracePointBeforePost(id, task, z, str);
            return id;
        }
    }

    public void tracePointAfterPost(HiTraceId hiTraceId, Task task, boolean z, String str) {
        if (HiTraceHelper.isValid(hiTraceId)) {
            HiTraceHelper.tracePointAfterPost(hiTraceId, task, z, str);
        }
    }
}
