package ohos.app.dispatcher;

import java.util.function.Consumer;
import ohos.app.dispatcher.task.Revocable;
import ohos.app.dispatcher.task.SyncTask;
import ohos.app.dispatcher.task.Task;
import ohos.app.dispatcher.task.TaskPriority;
import ohos.app.dispatcher.threading.TaskHandler;
import ohos.app.dispatcher.threading.TaskLooper;
import ohos.hiviewdfx.HiTraceId;

public class SpecTaskDispatcher extends BaseTaskDispatcher {
    private static final String ASYNC_DISPATCHER_TAG = "SpecTaskDispatcher::asyncDispatch";
    private static final String DELAY_DISPATCHER_TAG = "SpecTaskDispatcher::delayDispatch";
    private static final String DISPATCHER_TAG = "SpecTaskDispatcher";
    private static final String SYNC_DISPATCHER_TAG = "SpecTaskDispatcher::syncDispatch";
    private TaskHandler handler;

    @Override // ohos.app.dispatcher.BaseTaskDispatcher, ohos.app.dispatcher.TaskDispatcher
    public /* bridge */ /* synthetic */ void applyDispatch(Consumer consumer, long j) {
        super.applyDispatch(consumer, j);
    }

    @Override // ohos.app.dispatcher.BaseTaskDispatcher, ohos.app.dispatcher.TaskDispatcher
    public /* bridge */ /* synthetic */ void asyncDispatchBarrier(Runnable runnable) {
        super.asyncDispatchBarrier(runnable);
    }

    @Override // ohos.app.dispatcher.BaseTaskDispatcher, ohos.app.dispatcher.TaskDispatcher
    public /* bridge */ /* synthetic */ Revocable asyncGroupDispatch(Group group, Runnable runnable) {
        return super.asyncGroupDispatch(group, runnable);
    }

    @Override // ohos.app.dispatcher.BaseTaskDispatcher, ohos.app.dispatcher.TaskDispatcher
    public /* bridge */ /* synthetic */ Group createDispatchGroup() {
        return super.createDispatchGroup();
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

    public SpecTaskDispatcher(SpecDispatcherConfig specDispatcherConfig, TaskLooper taskLooper) {
        super(specDispatcherConfig.getName(), specDispatcherConfig.getPriority());
        this.handler = taskLooper.createHandler();
    }

    @Override // ohos.app.dispatcher.BaseTaskDispatcher, ohos.app.dispatcher.TaskDispatcher
    public void syncDispatch(Runnable runnable) {
        check(runnable);
        SyncTask syncTask = new SyncTask(runnable, getPriority());
        HiTraceId tracePointBeforePost = tracePointBeforePost(syncTask, false, SYNC_DISPATCHER_TAG);
        this.handler.dispatch(syncTask);
        syncTask.waitTask();
        tracePointAfterPost(tracePointBeforePost, syncTask, false, SYNC_DISPATCHER_TAG);
    }

    @Override // ohos.app.dispatcher.BaseTaskDispatcher, ohos.app.dispatcher.TaskDispatcher
    public Revocable asyncDispatch(Runnable runnable) {
        check(runnable);
        Task task = new Task(runnable, getPriority());
        tracePointBeforePost(task, true, ASYNC_DISPATCHER_TAG);
        this.handler.dispatch(task);
        return task;
    }

    @Override // ohos.app.dispatcher.BaseTaskDispatcher, ohos.app.dispatcher.TaskDispatcher
    public Revocable delayDispatch(Runnable runnable, long j) {
        check(runnable);
        Task task = new Task(runnable, getPriority());
        tracePointBeforePost(task, true, DELAY_DISPATCHER_TAG);
        this.handler.dispatch(task, j);
        return task;
    }
}
