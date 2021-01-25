package ohos.app.dispatcher;

import ohos.app.dispatcher.task.Revocable;
import ohos.app.dispatcher.task.SyncTask;
import ohos.app.dispatcher.task.Task;
import ohos.app.dispatcher.task.TaskExecuteInterceptor;
import ohos.app.dispatcher.task.TaskListener;
import ohos.app.dispatcher.task.TaskPriority;
import ohos.app.dispatcher.task.TaskStage;
import ohos.hiviewdfx.HiTraceId;

/* access modifiers changed from: package-private */
public class ParallelTaskDispatcherBase extends BaseTaskDispatcher {
    private static final String ASYNC_DISPATCHER_TAG = "ParallelTaskDispatcherBase::asyncDispatch";
    private static final String ASYNC_GROUP_DISPATCHER_TAG = "ParallelTaskDispatcherBase::asyncGroupDispatch";
    private static final String DELAY_DISPATCHER_TAG = "ParallelTaskDispatcherBase::delayDispatch";
    private static final String DISPATCHER_TAG = "ParallelTaskDispatcherBase";
    private static final String SYNC_DISPATCHER_TAG = "ParallelTaskDispatcherBase::syncDispatch";
    protected final TaskExecutor executor;

    /* access modifiers changed from: protected */
    @Override // ohos.app.dispatcher.BaseTaskDispatcher
    public TaskExecuteInterceptor getInterceptor() {
        return null;
    }

    ParallelTaskDispatcherBase(TaskPriority taskPriority, TaskExecutor taskExecutor, String str) {
        super(str, taskPriority);
        if (taskExecutor != null) {
            this.executor = taskExecutor;
            return;
        }
        throw new NullPointerException("TaskExecutor for TaskDispatcher cannot be null.");
    }

    @Override // ohos.app.dispatcher.BaseTaskDispatcher, ohos.app.dispatcher.TaskDispatcher
    public void syncDispatch(Runnable runnable) {
        check(runnable);
        SyncTask syncTask = new SyncTask(runnable, getPriority());
        HiTraceId tracePointBeforePost = tracePointBeforePost(syncTask, false, SYNC_DISPATCHER_TAG);
        interceptedExecute(syncTask);
        syncTask.waitTask();
        tracePointAfterPost(tracePointBeforePost, syncTask, false, SYNC_DISPATCHER_TAG);
    }

    @Override // ohos.app.dispatcher.BaseTaskDispatcher, ohos.app.dispatcher.TaskDispatcher
    public Revocable asyncDispatch(Runnable runnable) {
        check(runnable);
        Task task = new Task(runnable, getPriority());
        tracePointBeforePost(task, true, ASYNC_DISPATCHER_TAG);
        interceptedExecute(task);
        return task;
    }

    @Override // ohos.app.dispatcher.BaseTaskDispatcher, ohos.app.dispatcher.TaskDispatcher
    public Revocable delayDispatch(Runnable runnable, long j) {
        check(runnable);
        final Task task = new Task(runnable, getPriority());
        tracePointBeforePost(task, true, DELAY_DISPATCHER_TAG);
        this.executor.delayExecute(new Runnable() {
            /* class ohos.app.dispatcher.ParallelTaskDispatcherBase.AnonymousClass1 */

            @Override // java.lang.Runnable
            public void run() {
                ParallelTaskDispatcherBase.this.interceptedExecute(task);
            }
        }, j);
        return task;
    }

    @Override // ohos.app.dispatcher.BaseTaskDispatcher, ohos.app.dispatcher.TaskDispatcher
    public Revocable asyncGroupDispatch(Group group, Runnable runnable) {
        check(runnable);
        final GroupImpl castToGroupImpl = castToGroupImpl(group);
        castToGroupImpl.associate();
        Task task = new Task(runnable, getPriority());
        tracePointBeforePost(task, true, ASYNC_GROUP_DISPATCHER_TAG);
        task.addTaskListener(new TaskListener() {
            /* class ohos.app.dispatcher.ParallelTaskDispatcherBase.AnonymousClass2 */

            @Override // ohos.app.dispatcher.task.TaskListener
            public void onChanged(TaskStage taskStage) {
                if (taskStage.isDone()) {
                    castToGroupImpl.notifyTaskDone();
                }
            }
        });
        interceptedExecute(task);
        return task;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void interceptedExecute(Task task) {
        if (getInterceptor() == null || !getInterceptor().intercept(task)) {
            this.executor.execute(task);
        }
    }
}
