package android.location;

import android.content.Context;
import android.location.GnssNavigationMessage.Callback;
import android.location.IGnssNavigationMessageListener.Stub;
import android.os.RemoteException;

class GnssNavigationMessageCallbackTransport extends LocalListenerHelper<Callback> {
    private final IGnssNavigationMessageListener mListenerTransport = new ListenerTransport(this, null);
    private final ILocationManager mLocationManager;

    private class ListenerTransport extends Stub {
        /* synthetic */ ListenerTransport(GnssNavigationMessageCallbackTransport this$0, ListenerTransport -this1) {
            this();
        }

        private ListenerTransport() {
        }

        public void onGnssNavigationMessageReceived(final GnssNavigationMessage event) {
            GnssNavigationMessageCallbackTransport.this.foreach(new ListenerOperation<Callback>() {
                public void execute(Callback callback) throws RemoteException {
                    callback.onGnssNavigationMessageReceived(event);
                }
            });
        }

        public void onStatusChanged(final int status) {
            GnssNavigationMessageCallbackTransport.this.foreach(new ListenerOperation<Callback>() {
                public void execute(Callback callback) throws RemoteException {
                    callback.onStatusChanged(status);
                }
            });
        }
    }

    public GnssNavigationMessageCallbackTransport(Context context, ILocationManager locationManager) {
        super(context, "GnssNavigationMessageCallbackTransport");
        this.mLocationManager = locationManager;
    }

    protected boolean registerWithServer() throws RemoteException {
        return this.mLocationManager.addGnssNavigationMessageListener(this.mListenerTransport, getContext().getPackageName());
    }

    protected void unregisterFromServer() throws RemoteException {
        this.mLocationManager.removeGnssNavigationMessageListener(this.mListenerTransport);
    }
}
