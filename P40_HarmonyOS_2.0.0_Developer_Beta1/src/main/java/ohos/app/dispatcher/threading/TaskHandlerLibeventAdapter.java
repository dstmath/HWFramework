package ohos.app.dispatcher.threading;

import ohos.eventhandler.EventHandler;
import ohos.eventhandler.EventRunner;

class TaskHandlerLibeventAdapter extends EventHandler implements TaskHandler {
    public TaskHandlerLibeventAdapter(EventRunner eventRunner) {
        super(eventRunner);
    }

    @Override // ohos.app.dispatcher.threading.TaskHandler
    public boolean dispatch(Runnable runnable) {
        super.postTask(runnable);
        return true;
    }

    @Override // ohos.app.dispatcher.threading.TaskHandler
    public boolean dispatch(Runnable runnable, long j) {
        super.postTask(runnable, j);
        return true;
    }
}
