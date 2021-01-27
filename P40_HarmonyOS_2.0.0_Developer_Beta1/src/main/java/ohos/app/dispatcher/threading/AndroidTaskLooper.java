package ohos.app.dispatcher.threading;

import android.os.Looper;

public class AndroidTaskLooper implements TaskLooper {
    private final Looper looper;

    public AndroidTaskLooper(Looper looper2) {
        this.looper = looper2;
    }

    @Override // ohos.app.dispatcher.threading.TaskLooper
    public TaskHandler createHandler() {
        return new TaskHandlerAndroidAdapter(this.looper);
    }
}
