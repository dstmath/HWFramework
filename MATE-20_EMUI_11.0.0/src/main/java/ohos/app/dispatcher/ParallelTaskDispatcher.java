package ohos.app.dispatcher;

import ohos.app.dispatcher.task.SyncTask;
import ohos.app.dispatcher.task.Task;
import ohos.app.dispatcher.task.TaskExecuteInterceptor;
import ohos.app.dispatcher.task.TaskPriority;
import ohos.hiviewdfx.HiTraceId;

/* access modifiers changed from: package-private */
public class ParallelTaskDispatcher extends ParallelTaskDispatcherBase {
    private static final String ASYNC_DISPATCHER_BARRIER_TAG = "ParallelTaskDispatcher::asyncDispatchBarrier";
    private static final String DISPATCHER_TAG = "ParallelTaskDispatcher";
    private static final String SYNC_DISPATCHER_BARRIER_TAG = "ParallelTaskDispatcher::syncDispatchBarrier";
    private final BarrierHandler barrierHandler;

    ParallelTaskDispatcher(String str, TaskPriority taskPriority, TaskExecutor taskExecutor) {
        super(taskPriority, taskExecutor, str);
        this.barrierHandler = new BarrierHandler(taskExecutor);
    }

    @Override // ohos.app.dispatcher.BaseTaskDispatcher, ohos.app.dispatcher.TaskDispatcher
    public void syncDispatchBarrier(Runnable runnable) {
        check(runnable);
        SyncTask syncTask = new SyncTask(runnable, getPriority());
        HiTraceId tracePointBeforePost = tracePointBeforePost(syncTask, false, SYNC_DISPATCHER_BARRIER_TAG);
        this.barrierHandler.addBarrier(syncTask);
        syncTask.waitTask();
        tracePointAfterPost(tracePointBeforePost, syncTask, false, SYNC_DISPATCHER_BARRIER_TAG);
    }

    @Override // ohos.app.dispatcher.BaseTaskDispatcher, ohos.app.dispatcher.TaskDispatcher
    public void asyncDispatchBarrier(Runnable runnable) {
        check(runnable);
        Task task = new Task(runnable, getPriority());
        tracePointBeforePost(task, true, ASYNC_DISPATCHER_BARRIER_TAG);
        this.barrierHandler.addBarrier(task);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.app.dispatcher.ParallelTaskDispatcherBase, ohos.app.dispatcher.BaseTaskDispatcher
    public TaskExecuteInterceptor getInterceptor() {
        return this.barrierHandler;
    }
}
