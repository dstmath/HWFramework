package android.location;

import android.content.Context;
import android.location.GnssMeasurementsEvent;
import android.location.IGnssMeasurementsListener;
import android.location.LocalListenerHelper;
import android.os.RemoteException;

class GnssMeasurementCallbackTransport extends LocalListenerHelper<GnssMeasurementsEvent.Callback> {
    private final IGnssMeasurementsListener mListenerTransport = new ListenerTransport();
    private final ILocationManager mLocationManager;

    private class ListenerTransport extends IGnssMeasurementsListener.Stub {
        private ListenerTransport() {
        }

        public void onGnssMeasurementsReceived(final GnssMeasurementsEvent event) {
            GnssMeasurementCallbackTransport.this.foreach(new LocalListenerHelper.ListenerOperation<GnssMeasurementsEvent.Callback>() {
                public void execute(GnssMeasurementsEvent.Callback callback) throws RemoteException {
                    callback.onGnssMeasurementsReceived(event);
                }
            });
        }

        public void onStatusChanged(final int status) {
            GnssMeasurementCallbackTransport.this.foreach(new LocalListenerHelper.ListenerOperation<GnssMeasurementsEvent.Callback>() {
                public void execute(GnssMeasurementsEvent.Callback callback) throws RemoteException {
                    callback.onStatusChanged(status);
                }
            });
        }
    }

    public GnssMeasurementCallbackTransport(Context context, ILocationManager locationManager) {
        super(context, "GnssMeasurementListenerTransport");
        this.mLocationManager = locationManager;
    }

    /* access modifiers changed from: protected */
    public boolean registerWithServer() throws RemoteException {
        return this.mLocationManager.addGnssMeasurementsListener(this.mListenerTransport, getContext().getPackageName());
    }

    /* access modifiers changed from: protected */
    public void unregisterFromServer() throws RemoteException {
        this.mLocationManager.removeGnssMeasurementsListener(this.mListenerTransport);
    }
}
