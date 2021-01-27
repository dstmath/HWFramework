package ohos.app.dispatcher;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicReferenceArray;
import ohos.app.dispatcher.task.TaskPriority;
import ohos.app.dispatcher.threading.WorkerPoolConfig;
import ohos.appexecfwk.utils.AppLog;

public class TaskDispatcherContext {
    private static final int DEFAULT_PRIORITY_INDEX = 1;
    private static final int HIGH_PRIORITY_INDEX = 0;
    private static final int LOW_PRIORITY_INDEX = 2;
    private final WorkerPoolConfig config;
    private final TaskExecutor executor;
    private final AtomicReferenceArray<TaskDispatcher> globalDispatchers;
    private final Object instanceLock;
    private final Map<SerialTaskDispatcher, String> serialDispatchers;

    public TaskDispatcherContext() {
        this.globalDispatchers = new AtomicReferenceArray<>(TaskPriority.values().length);
        this.serialDispatchers = new WeakHashMap();
        this.config = new DefaultWorkerPoolConfig();
        this.instanceLock = new Object();
        this.executor = new TaskExecutor(this.config);
    }

    public TaskDispatcherContext(TaskExecutor taskExecutor) {
        this.globalDispatchers = new AtomicReferenceArray<>(TaskPriority.values().length);
        this.serialDispatchers = new WeakHashMap();
        this.config = new DefaultWorkerPoolConfig();
        this.instanceLock = new Object();
        this.executor = taskExecutor;
    }

    public WorkerPoolConfig getWorkerPoolConfig() {
        return this.config;
    }

    public Map<String, Long> getWorkerThreadsInfo() {
        TaskExecutor taskExecutor = this.executor;
        if (taskExecutor != null) {
            return taskExecutor.getWorkerThreadsInfo();
        }
        return new HashMap();
    }

    public Map<SerialTaskDispatcher, String> getSerialDispatchers() {
        return this.serialDispatchers;
    }

    public int getWaitingTasksCount() {
        TaskExecutor taskExecutor = this.executor;
        if (taskExecutor != null) {
            return taskExecutor.getPendingTasksSize();
        }
        return 0;
    }

    public long getTaskCounter() {
        return this.executor.getTaskCounter();
    }

    public SerialTaskDispatcher createSerialDispatcher(String str, TaskPriority taskPriority) {
        SerialTaskDispatcher serialTaskDispatcher = new SerialTaskDispatcher(str, taskPriority, this.executor);
        this.serialDispatchers.put(serialTaskDispatcher, str);
        return serialTaskDispatcher;
    }

    public ParallelTaskDispatcher createParallelDispatcher(String str, TaskPriority taskPriority) {
        return new ParallelTaskDispatcher(str, taskPriority, this.executor);
    }

    /* access modifiers changed from: package-private */
    /* renamed from: ohos.app.dispatcher.TaskDispatcherContext$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$ohos$app$dispatcher$task$TaskPriority = new int[TaskPriority.values().length];

        static {
            try {
                $SwitchMap$ohos$app$dispatcher$task$TaskPriority[TaskPriority.HIGH.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$ohos$app$dispatcher$task$TaskPriority[TaskPriority.DEFAULT.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$ohos$app$dispatcher$task$TaskPriority[TaskPriority.LOW.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
        }
    }

    private static int mapPriorityIndex(TaskPriority taskPriority) {
        int i = AnonymousClass1.$SwitchMap$ohos$app$dispatcher$task$TaskPriority[taskPriority.ordinal()];
        if (i == 1) {
            return 0;
        }
        if (i == 2) {
            return 1;
        }
        if (i == 3) {
            return 2;
        }
        AppLog.w("TaskDispatcherContext.mapPriorityIndex unhandled priority: %{public}s", taskPriority);
        return 1;
    }

    public TaskDispatcher getGlobalTaskDispatcher(TaskPriority taskPriority) {
        int mapPriorityIndex = mapPriorityIndex(taskPriority);
        TaskDispatcher taskDispatcher = this.globalDispatchers.get(mapPriorityIndex);
        if (taskDispatcher != null) {
            return taskDispatcher;
        }
        GlobalTaskDispatcher globalTaskDispatcher = new GlobalTaskDispatcher(taskPriority, this.executor);
        return !this.globalDispatchers.compareAndSet(mapPriorityIndex, null, globalTaskDispatcher) ? this.globalDispatchers.get(mapPriorityIndex) : globalTaskDispatcher;
    }

    public void shutdown(boolean z) {
        this.executor.terminate(z);
    }
}
