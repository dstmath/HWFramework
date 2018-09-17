package android.hardware.camera2.utils;

import android.hardware.camera2.utils.TaskDrainer.DrainListener;
import android.os.Handler;

public class TaskSingleDrainer {
    private final Object mSingleTask;
    private final TaskDrainer<Object> mTaskDrainer;

    public TaskSingleDrainer(Handler handler, DrainListener listener) {
        this.mSingleTask = new Object();
        this.mTaskDrainer = new TaskDrainer(handler, listener);
    }

    public TaskSingleDrainer(Handler handler, DrainListener listener, String name) {
        this.mSingleTask = new Object();
        this.mTaskDrainer = new TaskDrainer(handler, listener, name);
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
