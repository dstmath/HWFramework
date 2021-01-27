package ohos.ai.engine.handler;

public class TaskInfo {
    private int argOne;
    private int argTwo;
    private Object object;
    private int taskId;

    private TaskInfo(Builder builder) {
        this.taskId = builder.taskId;
        this.argOne = builder.argOne;
        this.argTwo = builder.argTwo;
        this.object = builder.object;
    }

    public int getTaskId() {
        return this.taskId;
    }

    public int getArgOne() {
        return this.argOne;
    }

    public int getArgTwo() {
        return this.argTwo;
    }

    public Object getObject() {
        return this.object;
    }

    public static class Builder {
        private int argOne;
        private int argTwo;
        private Object object;
        private int taskId;

        public Builder(int i) {
            this.taskId = i;
        }

        public Builder setArgOne(int i) {
            this.argOne = i;
            return this;
        }

        public Builder setObject(Object obj) {
            this.object = obj;
            return this;
        }

        public TaskInfo build() {
            return new TaskInfo(this);
        }
    }
}
