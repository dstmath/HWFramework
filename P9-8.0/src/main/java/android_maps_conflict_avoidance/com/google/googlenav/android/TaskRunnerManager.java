package android_maps_conflict_avoidance.com.google.googlenav.android;

import android_maps_conflict_avoidance.com.google.common.lang.BackgroundThreadFactory;
import android_maps_conflict_avoidance.com.google.common.lang.BaseThreadFactory;
import android_maps_conflict_avoidance.com.google.common.task.TaskRunner;

public class TaskRunnerManager {

    private static class TaskRunnerHolder {
        private static final TaskRunner instance = new TaskRunner(new BackgroundThreadFactory(new BaseThreadFactory()));

        private TaskRunnerHolder() {
        }
    }

    private TaskRunnerManager() {
    }

    public static TaskRunner getTaskRunner() {
        return TaskRunnerHolder.instance;
    }
}
