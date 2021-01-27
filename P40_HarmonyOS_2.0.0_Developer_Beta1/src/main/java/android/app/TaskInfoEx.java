package android.app;

import android.content.res.Configuration;
import android.graphics.Rect;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class TaskInfoEx {
    private TaskInfo mTaskInfo;

    public TaskInfoEx(TaskInfo taskInfo) {
        this.mTaskInfo = taskInfo;
    }

    public TaskInfo getTaskInfo() {
        return this.mTaskInfo;
    }

    public boolean isEmpty() {
        return this.mTaskInfo == null;
    }

    public void setCombinedTaskIds(int[] combinedTaskIds) {
        TaskInfo taskInfo = this.mTaskInfo;
        if (taskInfo != null) {
            taskInfo.combinedTaskIds = combinedTaskIds;
        }
    }

    public void setWindowMode(int windowMode) {
        TaskInfo taskInfo = this.mTaskInfo;
        if (taskInfo != null) {
            taskInfo.windowMode = windowMode;
        }
    }

    public int getWindowMode() {
        TaskInfo taskInfo = this.mTaskInfo;
        if (taskInfo != null) {
            return taskInfo.windowMode;
        }
        return 0;
    }

    public void setBounds(Rect bounds) {
        TaskInfo taskInfo = this.mTaskInfo;
        if (taskInfo != null) {
            taskInfo.bounds = bounds;
        }
    }

    public void setSupportsSplitScreenMultiWindow(boolean isSupport) {
        TaskInfo taskInfo = this.mTaskInfo;
        if (taskInfo != null) {
            taskInfo.supportsSplitScreenMultiWindow = isSupport;
        }
    }

    public Configuration getConfiguration() {
        TaskInfo taskInfo = this.mTaskInfo;
        if (taskInfo != null) {
            return taskInfo.configuration;
        }
        return null;
    }
}
