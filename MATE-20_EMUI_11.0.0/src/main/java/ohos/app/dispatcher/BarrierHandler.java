package ohos.app.dispatcher;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.function.Consumer;
import ohos.app.dispatcher.task.Task;
import ohos.app.dispatcher.task.TaskExecuteInterceptor;
import ohos.app.dispatcher.task.TaskListener;
import ohos.app.dispatcher.task.TaskStage;
import ohos.appexecfwk.utils.AppLog;

/* access modifiers changed from: package-private */
public class BarrierHandler implements TaskExecuteInterceptor {
    private final Object barrierLock = new Object();
    private final LinkedList<BarrierPair> barrierQueue = new LinkedList<>();
    private final TaskExecutor executor;

    /* access modifiers changed from: private */
    public static class BarrierPair {
        Task barrier;
        Set<Task> tasks;

        BarrierPair(Set<Task> set, Task task) {
            this.tasks = set;
            this.barrier = task;
        }
    }

    public BarrierHandler(TaskExecutor taskExecutor) {
        this.executor = taskExecutor;
    }

    @Override // ohos.app.dispatcher.task.TaskExecuteInterceptor
    public boolean intercept(Task task) {
        listenToTask(task);
        boolean addTaskAfterBarrier = addTaskAfterBarrier(task);
        if (addTaskAfterBarrier) {
            AppLog.d("Barrier.intercept intercepted a task.", new Object[0]);
        }
        return addTaskAfterBarrier;
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x0024  */
    public void addBarrier(Task task) {
        boolean z;
        listenToTask(task);
        synchronized (this.barrierLock) {
            BarrierPair peekLast = this.barrierQueue.peekLast();
            if (peekLast != null) {
                if (hasTask(peekLast.tasks) || peekLast.barrier != null) {
                    z = false;
                    if (peekLast != null) {
                        if (peekLast.barrier == null) {
                            peekLast.barrier = task;
                        }
                    }
                    this.barrierQueue.offerLast(new BarrierPair(null, task));
                }
            }
            z = true;
            if (peekLast != null) {
            }
            this.barrierQueue.offerLast(new BarrierPair(null, task));
        }
        AppLog.d("Barrier.addBarrier need execute now: %{public}b", Boolean.valueOf(z));
        if (z) {
            this.executor.execute(task);
        }
    }

    private void listenToTask(final Task task) {
        task.addTaskListener(new TaskListener() {
            /* class ohos.app.dispatcher.BarrierHandler.AnonymousClass1 */

            @Override // ohos.app.dispatcher.task.TaskListener
            public void onChanged(TaskStage taskStage) {
                if (taskStage.isDone()) {
                    BarrierHandler.this.onTaskDone(task);
                }
            }
        });
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x003b  */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x003c  */
    private boolean addTaskAfterBarrier(Task task) {
        boolean z;
        synchronized (this.barrierLock) {
            BarrierPair peekLast = this.barrierQueue.peekLast();
            if (peekLast != null) {
                if (peekLast.barrier == null) {
                    if (peekLast.tasks == null) {
                        peekLast.tasks = createTaskSet(task);
                    } else {
                        peekLast.tasks.add(task);
                    }
                    z = true;
                    if (this.barrierQueue.size() > 1) {
                        z = false;
                    }
                }
            }
            this.barrierQueue.offerLast(new BarrierPair(createTaskSet(task), null));
            z = true;
            if (this.barrierQueue.size() > 1) {
            }
        }
        return z;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onTaskDone(Task task) {
        boolean z;
        synchronized (this.barrierLock) {
            BarrierPair peekFirst = this.barrierQueue.peekFirst();
            z = true;
            if (peekFirst != null) {
                if (hasTask(peekFirst.tasks)) {
                    z = peekFirst.tasks.remove(task);
                    if (peekFirst.tasks.isEmpty() && peekFirst.barrier != null) {
                        AppLog.d("Barrier.onTaskDone execute barrier task after task done.", new Object[0]);
                        this.executor.execute(peekFirst.barrier);
                    }
                } else if (task.equals(peekFirst.barrier)) {
                    AppLog.d("Barrier.onTaskDone remove a barrier.", new Object[0]);
                    peekFirst.barrier = null;
                    if (this.barrierQueue.size() > 1) {
                        this.barrierQueue.pollFirst();
                        BarrierPair peekFirst2 = this.barrierQueue.peekFirst();
                        if (hasTask(peekFirst2.tasks)) {
                            peekFirst2.tasks.forEach(new Consumer() {
                                /* class ohos.app.dispatcher.$$Lambda$BarrierHandler$BiXQbGmM1CKYY2mzwEK3984nj4 */

                                @Override // java.util.function.Consumer
                                public final void accept(Object obj) {
                                    BarrierHandler.this.lambda$onTaskDone$0$BarrierHandler((Task) obj);
                                }
                            });
                        } else if (peekFirst2.barrier != null) {
                            AppLog.d("Barrier.onTaskDone execute barrier task after barrier done.", new Object[0]);
                            this.executor.execute(peekFirst2.barrier);
                        } else {
                            AppLog.w("Barrier.onTaskDone: Detected an empty node.", new Object[0]);
                        }
                    }
                }
            }
            z = false;
        }
        if (!z) {
            AppLog.w("Barrier.onTaskDone: Task remove failed.", new Object[0]);
        }
    }

    public /* synthetic */ void lambda$onTaskDone$0$BarrierHandler(Task task) {
        this.executor.execute(task);
    }

    private boolean hasTask(Set<Task> set) {
        return set != null && !set.isEmpty();
    }

    private Set<Task> createTaskSet(Task task) {
        LinkedHashSet linkedHashSet = new LinkedHashSet();
        linkedHashSet.add(task);
        return linkedHashSet;
    }
}
