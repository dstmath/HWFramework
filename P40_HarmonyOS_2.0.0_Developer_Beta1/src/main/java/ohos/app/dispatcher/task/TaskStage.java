package ohos.app.dispatcher.task;

public enum TaskStage {
    BEFORE_EXECUTE(0),
    AFTER_EXECUTE(1),
    REVOKED(2);
    
    private final int index;

    private TaskStage(int i) {
        this.index = i;
    }

    public int getIndex() {
        return this.index;
    }

    public boolean isDone() {
        return this.index == AFTER_EXECUTE.getIndex() || this.index == REVOKED.getIndex();
    }
}
