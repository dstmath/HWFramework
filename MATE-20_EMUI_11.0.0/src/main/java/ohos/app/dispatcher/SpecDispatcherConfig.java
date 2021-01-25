package ohos.app.dispatcher;

import ohos.app.dispatcher.task.TaskPriority;

public enum SpecDispatcherConfig {
    MAIN("DispatcherMain", TaskPriority.HIGH),
    UI("DispatcherUI", TaskPriority.HIGH);
    
    private String name;
    private TaskPriority priority;

    private SpecDispatcherConfig(String str, TaskPriority taskPriority) {
        this.name = str;
        this.priority = taskPriority;
    }

    public String getName() {
        return this.name;
    }

    public TaskPriority getPriority() {
        return this.priority;
    }
}
