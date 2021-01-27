package ohos.telephony;

import java.lang.ref.WeakReference;
import ohos.eventhandler.EventHandler;

public class CellularDataStateObserverCallback extends CellularDataStateObserverSkeleton {
    private WeakReference<CellularDataStateObserver> dataStateObserverWeakRef;
    private EventHandler eventHandler;

    public CellularDataStateObserverCallback(CellularDataStateObserver cellularDataStateObserver, EventHandler eventHandler2) {
        this.dataStateObserverWeakRef = new WeakReference<>(cellularDataStateObserver);
        this.eventHandler = eventHandler2;
    }

    @Override // ohos.telephony.ICellularDataStateObserver
    public void onCellularDataFlow(int i) {
        CellularDataStateObserver cellularDataStateObserver = this.dataStateObserverWeakRef.get();
        if (cellularDataStateObserver != null) {
            this.eventHandler.postTask(new Runnable(i) {
                /* class ohos.telephony.$$Lambda$CellularDataStateObserverCallback$i9xJvMb2Z6xIQBhfZqb6oFDCkQA */
                private final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    CellularDataStateObserver.this.onCellularDataFlow(this.f$1);
                }
            });
        }
    }

    @Override // ohos.telephony.ICellularDataStateObserver
    public void onCellularDataConnectStateUpdated(int i, int i2) {
        CellularDataStateObserver cellularDataStateObserver = this.dataStateObserverWeakRef.get();
        if (cellularDataStateObserver != null) {
            this.eventHandler.postTask(new Runnable(i, i2) {
                /* class ohos.telephony.$$Lambda$CellularDataStateObserverCallback$kuCVDDAoErxxclKjtYyyylucA */
                private final /* synthetic */ int f$1;
                private final /* synthetic */ int f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    CellularDataStateObserver.this.onCellularDataConnectStateUpdated(this.f$1, this.f$2);
                }
            });
        }
    }
}
