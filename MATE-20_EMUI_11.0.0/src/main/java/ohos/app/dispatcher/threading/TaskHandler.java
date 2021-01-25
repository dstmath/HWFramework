package ohos.app.dispatcher.threading;

public interface TaskHandler {
    boolean dispatch(Runnable runnable);

    boolean dispatch(Runnable runnable, long j);
}
