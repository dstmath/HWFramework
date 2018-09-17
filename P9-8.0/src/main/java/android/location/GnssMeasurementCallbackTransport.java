package android.location;

import android.content.Context;
import android.location.GnssMeasurementsEvent.Callback;
import android.location.IGnssMeasurementsListener.Stub;
import android.os.RemoteException;

class GnssMeasurementCallbackTransport extends LocalListenerHelper<Callback> {
    private final IGnssMeasurementsListener mListenerTransport = new ListenerTransport(this, null);
    private final ILocationManager mLocationManager;

    private class ListenerTransport extends Stub {
        /* synthetic */ ListenerTransport(GnssMeasurementCallbackTransport this$0, ListenerTransport -this1) {
            this();
        }

        private ListenerTransport() {
        }

        public void onGnssMeasurementsReceived(final GnssMeasurementsEvent event) {
            GnssMeasurementCallbackTransport.this.foreach(new ListenerOperation<Callback>() {
                public void execute(Callback callback) throws RemoteException {
                    callback.onGnssMeasurementsReceived(event);
                }
            });
        }

        public void onStatusChanged(final int status) {
            GnssMeasurementCallbackTransport.this.foreach(new ListenerOperation<Callback>() {
                public void execute(Callback callback) throws RemoteException {
                    callback.onStatusChanged(status);
                }
            });
        }
    }

    public GnssMeasurementCallbackTransport(Context context, ILocationManager locationManager) {
        super(context, "GnssMeasurementListenerTransport");
        this.mLocationManager = locationManager;
    }

    protected boolean registerWithServer() throws RemoteException {
        return this.mLocationManager.addGnssMeasurementsListener(this.mListenerTransport, getContext().getPackageName());
    }

    protected void unregisterFromServer() throws RemoteException {
        this.mLocationManager.removeGnssMeasurementsListener(this.mListenerTransport);
    }
}
