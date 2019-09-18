package android.location;

import android.content.Context;
import android.location.GnssNavigationMessage;
import android.location.IGnssNavigationMessageListener;
import android.location.LocalListenerHelper;
import android.os.RemoteException;

class GnssNavigationMessageCallbackTransport extends LocalListenerHelper<GnssNavigationMessage.Callback> {
    private final IGnssNavigationMessageListener mListenerTransport = new ListenerTransport();
    private final ILocationManager mLocationManager;

    private class ListenerTransport extends IGnssNavigationMessageListener.Stub {
        private ListenerTransport() {
        }

        public void onGnssNavigationMessageReceived(final GnssNavigationMessage event) {
            GnssNavigationMessageCallbackTransport.this.foreach(new LocalListenerHelper.ListenerOperation<GnssNavigationMessage.Callback>() {
                public void execute(GnssNavigationMessage.Callback callback) throws RemoteException {
                    callback.onGnssNavigationMessageReceived(event);
                }
            });
        }

        public void onStatusChanged(final int status) {
            GnssNavigationMessageCallbackTransport.this.foreach(new LocalListenerHelper.ListenerOperation<GnssNavigationMessage.Callback>() {
                public void execute(GnssNavigationMessage.Callback callback) throws RemoteException {
                    callback.onStatusChanged(status);
                }
            });
        }
    }

    public GnssNavigationMessageCallbackTransport(Context context, ILocationManager locationManager) {
        super(context, "GnssNavigationMessageCallbackTransport");
        this.mLocationManager = locationManager;
    }

    /* access modifiers changed from: protected */
    public boolean registerWithServer() throws RemoteException {
        return this.mLocationManager.addGnssNavigationMessageListener(this.mListenerTransport, getContext().getPackageName());
    }

    /* access modifiers changed from: protected */
    public void unregisterFromServer() throws RemoteException {
        this.mLocationManager.removeGnssNavigationMessageListener(this.mListenerTransport);
    }
}
