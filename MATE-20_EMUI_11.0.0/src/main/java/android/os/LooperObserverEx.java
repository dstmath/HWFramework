package android.os;

public interface LooperObserverEx {
    void dispatchingThrewException(Object obj, Message message, Exception exc);

    Object messageDispatchStarting();

    void messageDispatched(Object obj, Message message);
}
