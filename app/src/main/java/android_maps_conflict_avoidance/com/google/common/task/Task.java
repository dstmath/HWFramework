package android_maps_conflict_avoidance.com.google.common.task;

public class Task extends AbstractTask {
    private int priority;

    public Task(TaskRunner runner, Runnable runnable, String name) {
        this(runner, runnable, name, runner.getDefaultPriority());
    }

    public Task(TaskRunner runner, Runnable runnable, String name, int priority) {
        super(runner, runnable, name);
        setPriorityInternal(priority);
    }

    int cancelInternal() {
        return !this.runner.cancelTaskInternal(this) ? 0 : 1;
    }

    public synchronized int getPriority() {
        return this.priority;
    }

    private void setPriorityInternal(int priority) {
        this.priority = priority;
    }

    void scheduleInternal() {
        this.runner.schedulePriorityTaskInternal(this);
    }
}
