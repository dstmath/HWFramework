package ohos.app.dispatcher.task;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import ohos.app.dispatcher.HiTraceHelper;
import ohos.appexecfwk.utils.AppLog;
import ohos.hiviewdfx.HiTrace;
import ohos.hiviewdfx.HiTraceId;

public class Task implements Revocable, Runnable {
    private static final int EXECUTED = 1;
    private static final int REVOKED = 2;
    private final TaskPriority priority;
    protected Runnable runnable;
    private long sequence;
    private final AtomicInteger state = new AtomicInteger(0);
    private HiTraceId taskHiTraceId;
    private final ConcurrentLinkedQueue<TaskListener> taskListeners = new ConcurrentLinkedQueue<>();

    /* access modifiers changed from: private */
    public enum RevokeResult {
        FAIL,
        SUCCESS,
        ALREADY_REVOKED
    }

    public Task(Runnable runnable2, TaskPriority taskPriority) {
        this.runnable = runnable2;
        this.priority = taskPriority;
    }

    @Override // java.lang.Runnable
    public void run() {
        HiTraceId taskHiTraceId2 = getTaskHiTraceId();
        boolean isValid = HiTraceHelper.isValid(taskHiTraceId2);
        tracePointBeforeRunning(taskHiTraceId2, isValid);
        if (this.runnable != null) {
            if (enterExecute()) {
                this.runnable.run();
            }
            tracePointAfterRunning(taskHiTraceId2, isValid);
        }
    }

    public TaskPriority getPriority() {
        return this.priority;
    }

    public void setSequence(long j) {
        this.sequence = j;
    }

    public long getSequence() {
        return this.sequence;
    }

    @Override // ohos.app.dispatcher.task.Revocable
    public boolean revoke() {
        if (this.runnable == null) {
            return false;
        }
        RevokeResult revoked = setRevoked();
        AppLog.d("Task.revoke result: %{public}s", revoked.name());
        if (revoked == RevokeResult.SUCCESS) {
            onTaskCanceled();
        }
        if (revoked == RevokeResult.SUCCESS || revoked == RevokeResult.ALREADY_REVOKED) {
            return true;
        }
        return false;
    }

    public void addTaskListener(TaskListener taskListener) {
        this.taskListeners.add(taskListener);
    }

    public void beforeTaskExecute() {
        if ((this.state.get() & 2) != 2) {
            this.taskListeners.forEach($$Lambda$Task$h4JGBtaRJXOBvmSoGDHDeZn5nk.INSTANCE);
        }
    }

    public void afterTaskExecute() {
        if ((this.state.get() & 1) == 1) {
            this.taskListeners.forEach($$Lambda$Task$oPLtQ24oGQQ1u0DiqEPczEcYLZw.INSTANCE);
        }
    }

    public void onTaskCanceled() {
        this.taskListeners.forEach($$Lambda$Task$hQRk0pQw14b3b5vvcU_x_W74utY.INSTANCE);
    }

    private boolean enterExecute() {
        int i;
        do {
            i = this.state.get();
            if ((this.state.get() & 3) != 0) {
                return false;
            }
        } while (!this.state.compareAndSet(i, 1));
        return true;
    }

    private RevokeResult setRevoked() {
        int i;
        do {
            i = this.state.get();
            if ((i & 2) == 2) {
                return RevokeResult.ALREADY_REVOKED;
            }
            if ((i & 1) != 0) {
                return RevokeResult.FAIL;
            }
        } while (!this.state.compareAndSet(i, i | 2));
        return RevokeResult.SUCCESS;
    }

    public void setTaskHiTraceId(HiTraceId hiTraceId) {
        this.taskHiTraceId = hiTraceId;
    }

    private HiTraceId getTaskHiTraceId() {
        return this.taskHiTraceId;
    }

    private void tracePointBeforeRunning(HiTraceId hiTraceId, boolean z) {
        if (z) {
            HiTrace.setId(hiTraceId);
            HiTraceHelper.tracePointBeforeRunning(hiTraceId, this);
        }
    }

    private void tracePointAfterRunning(HiTraceId hiTraceId, boolean z) {
        if (z) {
            HiTraceHelper.tracePointAfterRunning(hiTraceId, this);
        }
        HiTrace.clearId();
    }
}
