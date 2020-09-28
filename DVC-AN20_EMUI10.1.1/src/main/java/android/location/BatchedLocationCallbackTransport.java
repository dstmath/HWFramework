package android.location;

import android.content.Context;
import android.location.IBatchedLocationCallback;
import android.location.LocalListenerHelper;
import android.os.RemoteException;
import java.util.List;

/* access modifiers changed from: package-private */
public class BatchedLocationCallbackTransport extends LocalListenerHelper<BatchedLocationCallback> {
    private final IBatchedLocationCallback mCallbackTransport = new CallbackTransport();
    private final ILocationManager mLocationManager;

    public BatchedLocationCallbackTransport(Context context, ILocationManager locationManager) {
        super(context, "BatchedLocationCallbackTransport");
        this.mLocationManager = locationManager;
    }

    /* access modifiers changed from: protected */
    @Override // android.location.LocalListenerHelper
    public boolean registerWithServer() throws RemoteException {
        return this.mLocationManager.addGnssBatchingCallback(this.mCallbackTransport, getContext().getPackageName());
    }

    /* access modifiers changed from: protected */
    @Override // android.location.LocalListenerHelper
    public void unregisterFromServer() throws RemoteException {
        this.mLocationManager.removeGnssBatchingCallback();
    }

    private class CallbackTransport extends IBatchedLocationCallback.Stub {
        private CallbackTransport() {
        }

        @Override // android.location.IBatchedLocationCallback
        public void onLocationBatch(final List<Location> locations) {
            BatchedLocationCallbackTransport.this.foreach(new LocalListenerHelper.ListenerOperation<BatchedLocationCallback>() {
                /* class android.location.BatchedLocationCallbackTransport.CallbackTransport.AnonymousClass1 */

                public void execute(BatchedLocationCallback callback) throws RemoteException {
                    callback.onLocationBatch(locations);
                }
            });
        }
    }
}
