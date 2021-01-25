package ohos.app.dispatcher.threading;

import android.os.Handler;
import android.os.Looper;

public class TaskHandlerAndroidAdapter extends Handler implements TaskHandler {
    public TaskHandlerAndroidAdapter(Looper looper) {
        super(looper);
    }

    @Override // ohos.app.dispatcher.threading.TaskHandler
    public boolean dispatch(Runnable runnable) {
        return super.post(runnable);
    }

    @Override // ohos.app.dispatcher.threading.TaskHandler
    public boolean dispatch(Runnable runnable, long j) {
        return super.postDelayed(runnable, j);
    }
}
