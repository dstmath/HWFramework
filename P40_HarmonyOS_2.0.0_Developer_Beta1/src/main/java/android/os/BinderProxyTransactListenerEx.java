package android.os;

public interface BinderProxyTransactListenerEx {
    void onTransactEnded(Object obj);

    Object onTransactStarted(IBinder iBinder, int i);
}
