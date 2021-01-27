package ohos.app.dispatcher.task;

import java.io.Serializable;
import java.util.Comparator;
import ohos.appexecfwk.utils.AppLog;

public class TaskPriorityComparator implements Comparator<Task>, Serializable {
    private static final int DEFAULT_PRIORITY_WEIGHT = 1;
    private static final int HIGH_PRIORITY_WEIGHT = 2;
    private static final int LOW_PRIORITY_WEIGHT = 0;
    private static final long serialVersionUID = -4352745781101560609L;

    /* access modifiers changed from: package-private */
    /* renamed from: ohos.app.dispatcher.task.TaskPriorityComparator$1  reason: invalid class name */
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

    private static int getPriorityWeight(TaskPriority taskPriority) {
        int i = AnonymousClass1.$SwitchMap$ohos$app$dispatcher$task$TaskPriority[taskPriority.ordinal()];
        if (i == 1) {
            return 2;
        }
        if (i == 2) {
            return 1;
        }
        if (i == 3) {
            return 0;
        }
        AppLog.w("TaskPriorityComparator.getPriorityWeight unhandled priority: %{public}s", taskPriority);
        return 1;
    }

    public int compare(Task task, Task task2) {
        if (task == null || task2 == null) {
            throw new NullPointerException("Comparable is null");
        } else if (task == task2) {
            return 0;
        } else {
            int priorityWeight = getPriorityWeight(task.getPriority());
            int priorityWeight2 = getPriorityWeight(task2.getPriority());
            if (priorityWeight == priorityWeight2) {
                return Long.compare(task.getSequence(), task2.getSequence());
            }
            return Integer.compare(priorityWeight2, priorityWeight);
        }
    }
}
