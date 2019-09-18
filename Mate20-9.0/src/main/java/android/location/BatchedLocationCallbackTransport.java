package android.location;

import android.content.Context;
import android.location.IBatchedLocationCallback;
import android.location.LocalListenerHelper;
import android.os.RemoteException;
import java.util.List;

class BatchedLocationCallbackTransport extends LocalListenerHelper<BatchedLocationCallback> {
    private final IBatchedLocationCallback mCallbackTransport = new CallbackTransport();
    private final ILocationManager mLocationManager;

    private class CallbackTransport extends IBatchedLocationCallback.Stub {
        private CallbackTransport() {
        }

        public void onLocationBatch(final List<Location> locations) {
            BatchedLocationCallbackTransport.this.foreach(new LocalListenerHelper.ListenerOperation<BatchedLocationCallback>() {
                public void execute(BatchedLocationCallback callback) throws RemoteException {
                    callback.onLocationBatch(locations);
                }
            });
        }
    }

    public BatchedLocationCallbackTransport(Context context, ILocationManager locationManager) {
        super(context, "BatchedLocationCallbackTransport");
        this.mLocationManager = locationManager;
    }

    /* access modifiers changed from: protected */
    public boolean registerWithServer() throws RemoteException {
        return this.mLocationManager.addGnssBatchingCallback(this.mCallbackTransport, getContext().getPackageName());
    }

    /* access modifiers changed from: protected */
    public void unregisterFromServer() throws RemoteException {
        this.mLocationManager.removeGnssBatchingCallback();
    }
}
