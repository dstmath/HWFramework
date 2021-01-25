package ohos.app.dispatcher.threading;

import ohos.eventhandler.EventRunner;

public class LibeventTaskLooper implements TaskLooper {
    private final EventRunner runner;

    public LibeventTaskLooper(EventRunner eventRunner) {
        this.runner = eventRunner;
    }

    @Override // ohos.app.dispatcher.threading.TaskLooper
    public TaskHandler createHandler() {
        return new TaskHandlerLibeventAdapter(this.runner);
    }
}
