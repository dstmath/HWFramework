package android.location;

import android.content.Context;
import android.location.IBatchedLocationCallback.Stub;
import android.os.RemoteException;
import java.util.List;

class BatchedLocationCallbackTransport extends LocalListenerHelper<BatchedLocationCallback> {
    private final IBatchedLocationCallback mCallbackTransport = new CallbackTransport(this, null);
    private final ILocationManager mLocationManager;

    private class CallbackTransport extends Stub {
        /* synthetic */ CallbackTransport(BatchedLocationCallbackTransport this$0, CallbackTransport -this1) {
            this();
        }

        private CallbackTransport() {
        }

        public void onLocationBatch(final List<Location> locations) {
            BatchedLocationCallbackTransport.this.foreach(new ListenerOperation<BatchedLocationCallback>() {
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

    protected boolean registerWithServer() throws RemoteException {
        return this.mLocationManager.addGnssBatchingCallback(this.mCallbackTransport, getContext().getPackageName());
    }

    protected void unregisterFromServer() throws RemoteException {
        this.mLocationManager.removeGnssBatchingCallback();
    }
}
