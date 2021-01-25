package ohos.data.rdb;

import ohos.eventhandler.EventHandler;

public class DataObserverAsyncWrapper implements DataObserver {
    private DataObserver dataObserver;
    private EventHandler eventHandler;

    public DataObserverAsyncWrapper(DataObserver dataObserver2, EventHandler eventHandler2) {
        this.dataObserver = dataObserver2;
        this.eventHandler = eventHandler2;
    }

    @Override // ohos.data.rdb.DataObserver
    public void onChange() {
        EventHandler eventHandler2;
        if (this.dataObserver != null && (eventHandler2 = this.eventHandler) != null) {
            eventHandler2.postTask(new Runnable() {
                /* class ohos.data.rdb.$$Lambda$DataObserverAsyncWrapper$cazBHzXEJ8iuLa1A52rDLGJ31h4 */

                @Override // java.lang.Runnable
                public final void run() {
                    DataObserverAsyncWrapper.this.lambda$onChange$0$DataObserverAsyncWrapper();
                }
            });
        }
    }

    public /* synthetic */ void lambda$onChange$0$DataObserverAsyncWrapper() {
        this.dataObserver.onChange();
    }
}
