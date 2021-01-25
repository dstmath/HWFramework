package android.os;

import android.view.InputEvent;

public class DefaultBlockMonitorEx implements IBlockMonitor, LooperObserverEx, BinderProxyTransactListenerEx {
    @Override // android.os.BinderProxyTransactListenerEx
    public Object onTransactStarted(IBinder binder, int transactionCode) {
        return null;
    }

    @Override // android.os.BinderProxyTransactListenerEx
    public void onTransactEnded(Object session) {
    }

    @Override // android.os.IBlockMonitor
    public void checkInputTime(long startTime) {
    }

    @Override // android.os.IBlockMonitor
    public void checkBinderTime(long startTime) {
    }

    @Override // android.os.IBlockMonitor
    public void initialize() {
    }

    @Override // android.os.IBlockMonitor
    public void checkInputReceiveTime(int seq, long eventTime) {
    }

    @Override // android.os.LooperObserverEx
    public Object messageDispatchStarting() {
        return null;
    }

    @Override // android.os.LooperObserverEx
    public void messageDispatched(Object token, Message msg) {
    }

    @Override // android.os.LooperObserverEx
    public void dispatchingThrewException(Object token, Message msg, Exception exception) {
    }

    public void setObserver(DefaultBlockMonitorEx defaultBlockMonitor) {
        Looper.setObserver(new LooperObserverBridge(defaultBlockMonitor));
    }

    public void setProxyTransactListener(DefaultBlockMonitorEx defaultBlockMonitor) {
        Binder.setProxyTransactListener(new BinderProxyTransactListenerBridge(defaultBlockMonitor));
    }

    @Override // android.os.IBlockMonitor
    public void notifyInputEvent(InputEvent event) {
    }
}
