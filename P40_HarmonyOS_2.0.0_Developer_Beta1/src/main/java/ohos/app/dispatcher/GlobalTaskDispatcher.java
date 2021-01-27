package ohos.app.dispatcher;

import java.util.function.Consumer;
import ohos.app.dispatcher.task.Revocable;
import ohos.app.dispatcher.task.Task;
import ohos.app.dispatcher.task.TaskPriority;
import ohos.hiviewdfx.HiTraceId;

public class GlobalTaskDispatcher extends ParallelTaskDispatcherBase {
    private static final String DISPATCHER_NAME = "GlobalDispatcher";

    @Override // ohos.app.dispatcher.BaseTaskDispatcher, ohos.app.dispatcher.TaskDispatcher
    public /* bridge */ /* synthetic */ void applyDispatch(Consumer consumer, long j) {
        super.applyDispatch(consumer, j);
    }

    @Override // ohos.app.dispatcher.ParallelTaskDispatcherBase, ohos.app.dispatcher.BaseTaskDispatcher, ohos.app.dispatcher.TaskDispatcher
    public /* bridge */ /* synthetic */ Revocable asyncDispatch(Runnable runnable) {
        return super.asyncDispatch(runnable);
    }

    @Override // ohos.app.dispatcher.BaseTaskDispatcher, ohos.app.dispatcher.TaskDispatcher
    public /* bridge */ /* synthetic */ void asyncDispatchBarrier(Runnable runnable) {
        super.asyncDispatchBarrier(runnable);
    }

    @Override // ohos.app.dispatcher.ParallelTaskDispatcherBase, ohos.app.dispatcher.BaseTaskDispatcher, ohos.app.dispatcher.TaskDispatcher
    public /* bridge */ /* synthetic */ Revocable asyncGroupDispatch(Group group, Runnable runnable) {
        return super.asyncGroupDispatch(group, runnable);
    }

    @Override // ohos.app.dispatcher.BaseTaskDispatcher, ohos.app.dispatcher.TaskDispatcher
    public /* bridge */ /* synthetic */ Group createDispatchGroup() {
        return super.createDispatchGroup();
    }

    @Override // ohos.app.dispatcher.ParallelTaskDispatcherBase, ohos.app.dispatcher.BaseTaskDispatcher, ohos.app.dispatcher.TaskDispatcher
    public /* bridge */ /* synthetic */ Revocable delayDispatch(Runnable runnable, long j) {
        return super.delayDispatch(runnable, j);
    }

    @Override // ohos.app.dispatcher.BaseTaskDispatcher
    public /* bridge */ /* synthetic */ TaskPriority getPriority() {
        return super.getPriority();
    }

    @Override // ohos.app.dispatcher.BaseTaskDispatcher, ohos.app.dispatcher.TaskDispatcher
    public /* bridge */ /* synthetic */ void groupDispatchNotify(Group group, Runnable runnable) {
        super.groupDispatchNotify(group, runnable);
    }

    @Override // ohos.app.dispatcher.BaseTaskDispatcher, ohos.app.dispatcher.TaskDispatcher
    public /* bridge */ /* synthetic */ boolean groupDispatchWait(Group group, long j) {
        return super.groupDispatchWait(group, j);
    }

    @Override // ohos.app.dispatcher.ParallelTaskDispatcherBase, ohos.app.dispatcher.BaseTaskDispatcher, ohos.app.dispatcher.TaskDispatcher
    public /* bridge */ /* synthetic */ void syncDispatch(Runnable runnable) {
        super.syncDispatch(runnable);
    }

    @Override // ohos.app.dispatcher.BaseTaskDispatcher, ohos.app.dispatcher.TaskDispatcher
    public /* bridge */ /* synthetic */ void syncDispatchBarrier(Runnable runnable) {
        super.syncDispatchBarrier(runnable);
    }

    @Override // ohos.app.dispatcher.BaseTaskDispatcher
    public /* bridge */ /* synthetic */ void tracePointAfterPost(HiTraceId hiTraceId, Task task, boolean z, String str) {
        super.tracePointAfterPost(hiTraceId, task, z, str);
    }

    @Override // ohos.app.dispatcher.BaseTaskDispatcher
    public /* bridge */ /* synthetic */ HiTraceId tracePointBeforePost(Task task, boolean z, String str) {
        return super.tracePointBeforePost(task, z, str);
    }

    GlobalTaskDispatcher(TaskPriority taskPriority, TaskExecutor taskExecutor) {
        super(taskPriority, taskExecutor, DISPATCHER_NAME);
    }
}
