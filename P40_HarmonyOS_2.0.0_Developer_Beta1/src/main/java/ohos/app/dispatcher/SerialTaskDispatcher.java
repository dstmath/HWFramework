package ohos.app.dispatcher;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import ohos.app.dispatcher.task.Revocable;
import ohos.app.dispatcher.task.SyncTask;
import ohos.app.dispatcher.task.Task;
import ohos.app.dispatcher.task.TaskListener;
import ohos.app.dispatcher.task.TaskPriority;
import ohos.app.dispatcher.task.TaskStage;
import ohos.appexecfwk.utils.AppLog;
import ohos.hiviewdfx.HiTraceId;

public class SerialTaskDispatcher extends BaseTaskDispatcher {
    private static final String ASYNC_DISPATCHER_TAG = "SerialTaskDispatcher::asyncDispatch";
    private static final String DELAY_DISPATCHER_TAG = "SerialTaskDispatcher::delayDispatch";
    private static final String DISPATCHER_TAG = "SerialTaskDispatcher";
    private static final String SYNC_DISPATCHER_TAG = "SerialTaskDispatcher::syncDispatch";
    private final TaskExecutor executor;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final Queue<Task> workingTasks = new ConcurrentLinkedQueue();

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

    SerialTaskDispatcher(String str, TaskPriority taskPriority, TaskExecutor taskExecutor) {
        super(str, taskPriority);
        this.executor = taskExecutor;
    }

    public int getWorkingTasksSize() {
        return this.workingTasks.size();
    }

    public String getDispatcherName() {
        return this.dispatcherName;
    }

    @Override // ohos.app.dispatcher.BaseTaskDispatcher, ohos.app.dispatcher.TaskDispatcher
    public void syncDispatch(Runnable runnable) {
        check(runnable);
        SyncTask syncTask = new SyncTask(runnable, getPriority());
        HiTraceId tracePointBeforePost = tracePointBeforePost(syncTask, false, SYNC_DISPATCHER_TAG);
        onNewTaskIn(syncTask);
        syncTask.waitTask();
        tracePointAfterPost(tracePointBeforePost, syncTask, false, DISPATCHER_TAG);
    }

    @Override // ohos.app.dispatcher.BaseTaskDispatcher, ohos.app.dispatcher.TaskDispatcher
    public Revocable asyncDispatch(Runnable runnable) {
        check(runnable);
        Task task = new Task(runnable, getPriority());
        tracePointBeforePost(task, true, ASYNC_DISPATCHER_TAG);
        onNewTaskIn(task);
        return task;
    }

    @Override // ohos.app.dispatcher.BaseTaskDispatcher, ohos.app.dispatcher.TaskDispatcher
    public Revocable delayDispatch(Runnable runnable, long j) {
        check(runnable);
        final Task task = new Task(runnable, getPriority());
        tracePointBeforePost(task, true, DELAY_DISPATCHER_TAG);
        this.executor.delayExecute(new Runnable() {
            /* class ohos.app.dispatcher.SerialTaskDispatcher.AnonymousClass1 */

            @Override // java.lang.Runnable
            public void run() {
                SerialTaskDispatcher.this.onNewTaskIn(task);
            }
        }, j);
        return task;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onNewTaskIn(Task task) {
        boolean z;
        prepare(task);
        if (this.workingTasks.isEmpty()) {
            synchronized (this.workingTasks) {
                z = this.workingTasks.offer(task);
            }
        } else {
            z = this.workingTasks.offer(task);
        }
        if (!z) {
            AppLog.w("SerialTaskDispatcher.onNewTaskIn exceed the maximum capacity of Queue (Integer.MAX_VALUE)", new Object[0]);
        }
        schedule();
    }

    private void prepare(Task task) {
        task.addTaskListener(new TaskListener() {
            /* class ohos.app.dispatcher.SerialTaskDispatcher.AnonymousClass2 */

            @Override // ohos.app.dispatcher.task.TaskListener
            public void onChanged(TaskStage taskStage) {
                if (taskStage.isDone()) {
                    SerialTaskDispatcher.this.onTaskDone();
                }
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onTaskDone() {
        doNext(this.workingTasks.poll() == null);
    }

    private boolean schedule() {
        if (this.running.compareAndSet(false, true)) {
            return doNext(false);
        }
        AppLog.d("SerialTaskDispatcher::schedule already running", new Object[0]);
        return false;
    }

    private boolean doNext(boolean z) {
        Task peek = z ? null : this.workingTasks.peek();
        if (peek == null) {
            synchronized (this.workingTasks) {
                peek = this.workingTasks.peek();
                if (peek == null) {
                    this.running.set(false);
                    return false;
                }
            }
        }
        doWork(peek);
        return true;
    }

    private void doWork(Task task) {
        this.executor.execute(task);
    }
}
