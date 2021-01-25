package android.os;

import android.os.Looper;

public class LooperObserverBridge implements Looper.Observer {
    private LooperObserverEx looperObserverEx;

    public void setLooperObserverEx(LooperObserverEx looperObserverEx2) {
        this.looperObserverEx = looperObserverEx2;
    }

    public LooperObserverBridge(LooperObserverEx looperObserverEx2) {
        this.looperObserverEx = looperObserverEx2;
    }

    @Override // android.os.Looper.Observer
    public Object messageDispatchStarting() {
        return this.looperObserverEx.messageDispatchStarting();
    }

    @Override // android.os.Looper.Observer
    public void messageDispatched(Object token, Message msg) {
        this.looperObserverEx.messageDispatched(token, msg);
    }

    @Override // android.os.Looper.Observer
    public void dispatchingThrewException(Object token, Message msg, Exception exception) {
        this.looperObserverEx.dispatchingThrewException(token, msg, exception);
    }
}
