package ohos.telephony;

import java.lang.ref.WeakReference;
import java.util.List;
import ohos.eventhandler.EventHandler;

public class RadioStateObserverCallback extends RadioStateObserverSkeleton {
    private EventHandler eventHandler;
    private WeakReference<RadioStateObserver> phoneStateObserverWeakRef;

    public RadioStateObserverCallback(RadioStateObserver radioStateObserver, EventHandler eventHandler2) {
        this.phoneStateObserverWeakRef = new WeakReference<>(radioStateObserver);
        this.eventHandler = eventHandler2;
    }

    @Override // ohos.telephony.IRadioStateObserver
    public void onNetworkStateUpdated(NetworkState networkState) {
        RadioStateObserver radioStateObserver = this.phoneStateObserverWeakRef.get();
        if (radioStateObserver != null) {
            this.eventHandler.postTask(new Runnable(networkState) {
                /* class ohos.telephony.$$Lambda$RadioStateObserverCallback$8slGLybGBTjdgRPImMaL2LMN1Ro */
                private final /* synthetic */ NetworkState f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    RadioStateObserver.this.onNetworkStateUpdated(this.f$1);
                }
            });
        }
    }

    @Override // ohos.telephony.IRadioStateObserver
    public void onSignalInfoUpdated(List<SignalInformation> list) {
        RadioStateObserver radioStateObserver = this.phoneStateObserverWeakRef.get();
        if (radioStateObserver != null) {
            this.eventHandler.postTask(new Runnable(list) {
                /* class ohos.telephony.$$Lambda$RadioStateObserverCallback$xTAvzcPFpWgsYxEfnOv1v97r4gs */
                private final /* synthetic */ List f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    RadioStateObserver.this.onSignalInfoUpdated(this.f$1);
                }
            });
        }
    }

    @Override // ohos.telephony.IRadioStateObserver
    public void onCellInfoUpdated(List<CellInformation> list) {
        RadioStateObserver radioStateObserver = this.phoneStateObserverWeakRef.get();
        if (radioStateObserver != null) {
            this.eventHandler.postTask(new Runnable(list) {
                /* class ohos.telephony.$$Lambda$RadioStateObserverCallback$ftCl__7VAKw9owHiCSticu1wLCI */
                private final /* synthetic */ List f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    RadioStateObserver.this.onCellInfoUpdated(this.f$1);
                }
            });
        }
    }
}
