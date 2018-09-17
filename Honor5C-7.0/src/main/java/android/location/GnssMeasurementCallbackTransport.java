package android.location;

import android.content.Context;
import android.location.GnssMeasurementsEvent.Callback;
import android.location.IGnssMeasurementsListener.Stub;
import android.os.RemoteException;

class GnssMeasurementCallbackTransport extends LocalListenerHelper<Callback> {
    private final IGnssMeasurementsListener mListenerTransport;
    private final ILocationManager mLocationManager;

    private class ListenerTransport extends Stub {

        /* renamed from: android.location.GnssMeasurementCallbackTransport.ListenerTransport.1 */
        class AnonymousClass1 implements ListenerOperation<Callback> {
            final /* synthetic */ GnssMeasurementsEvent val$event;

            AnonymousClass1(GnssMeasurementsEvent val$event) {
                this.val$event = val$event;
            }

            public void execute(Callback callback) throws RemoteException {
                callback.onGnssMeasurementsReceived(this.val$event);
            }
        }

        /* renamed from: android.location.GnssMeasurementCallbackTransport.ListenerTransport.2 */
        class AnonymousClass2 implements ListenerOperation<Callback> {
            final /* synthetic */ int val$status;

            AnonymousClass2(int val$status) {
                this.val$status = val$status;
            }

            public void execute(Callback callback) throws RemoteException {
                callback.onStatusChanged(this.val$status);
            }
        }

        private ListenerTransport() {
        }

        public void onGnssMeasurementsReceived(GnssMeasurementsEvent event) {
            GnssMeasurementCallbackTransport.this.foreach(new AnonymousClass1(event));
        }

        public void onStatusChanged(int status) {
            GnssMeasurementCallbackTransport.this.foreach(new AnonymousClass2(status));
        }
    }

    public GnssMeasurementCallbackTransport(Context context, ILocationManager locationManager) {
        super(context, "GnssMeasurementListenerTransport");
        this.mListenerTransport = new ListenerTransport();
        this.mLocationManager = locationManager;
    }

    protected boolean registerWithServer() throws RemoteException {
        return this.mLocationManager.addGnssMeasurementsListener(this.mListenerTransport, getContext().getPackageName());
    }

    protected void unregisterFromServer() throws RemoteException {
        this.mLocationManager.removeGnssMeasurementsListener(this.mListenerTransport);
    }
}
