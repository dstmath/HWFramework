package ohos.app.dispatcher.threading;

public interface ThreadFactory {
    Thread create(Runnable runnable);
}
