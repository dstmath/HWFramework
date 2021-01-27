package ohos.app.dispatcher;

import java.util.function.Consumer;
import ohos.app.dispatcher.task.Revocable;

public interface TaskDispatcher {
    void applyDispatch(Consumer<Long> consumer, long j);

    Revocable asyncDispatch(Runnable runnable);

    void asyncDispatchBarrier(Runnable runnable);

    Revocable asyncGroupDispatch(Group group, Runnable runnable);

    Group createDispatchGroup();

    Revocable delayDispatch(Runnable runnable, long j);

    void groupDispatchNotify(Group group, Runnable runnable);

    boolean groupDispatchWait(Group group, long j);

    void syncDispatch(Runnable runnable);

    void syncDispatchBarrier(Runnable runnable);
}
