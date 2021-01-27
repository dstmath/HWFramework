package android.os;

import android.os.Binder;

public class BinderProxyTransactListenerBridge implements Binder.ProxyTransactListener {
    private BinderProxyTransactListenerEx binderProxyTransactListenerEx;

    public void setBinderProxyTransactListenerEx(BinderProxyTransactListenerEx listenerEx) {
        this.binderProxyTransactListenerEx = listenerEx;
    }

    public BinderProxyTransactListenerBridge(BinderProxyTransactListenerEx listenerEx) {
        this.binderProxyTransactListenerEx = listenerEx;
    }

    @Override // android.os.Binder.ProxyTransactListener
    public Object onTransactStarted(IBinder binder, int transactionCode) {
        return this.binderProxyTransactListenerEx.onTransactStarted(binder, transactionCode);
    }

    @Override // android.os.Binder.ProxyTransactListener
    public void onTransactEnded(Object session) {
        this.binderProxyTransactListenerEx.onTransactEnded(session);
    }
}
