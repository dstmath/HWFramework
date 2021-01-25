package android.hardware.camera2.utils;

import android.hardware.camera2.utils.TaskDrainer;
import java.util.concurrent.Executor;

public class TaskSingleDrainer {
    private final Object mSingleTask = new Object();
    private final TaskDrainer<Object> mTaskDrainer;

    public TaskSingleDrainer(Executor executor, TaskDrainer.DrainListener listener) {
        this.mTaskDrainer = new TaskDrainer<>(executor, listener);
    }

    public TaskSingleDrainer(Executor executor, TaskDrainer.DrainListener listener, String name) {
        this.mTaskDrainer = new TaskDrainer<>(executor, listener, name);
    }

    public void taskStarted() {
        this.mTaskDrainer.taskStarted(this.mSingleTask);
    }

    public void beginDrain() {
        this.mTaskDrainer.beginDrain();
    }

    public void taskFinished() {
        this.mTaskDrainer.taskFinished(this.mSingleTask);
    }
}
