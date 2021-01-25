package ohos.app.dispatcher.threading;

import ohos.eventhandler.EventRunner;

public class TaskLooperFactory {
    public static TaskLooper create(EventRunner eventRunner) {
        return new LibeventTaskLooper(eventRunner);
    }
}
