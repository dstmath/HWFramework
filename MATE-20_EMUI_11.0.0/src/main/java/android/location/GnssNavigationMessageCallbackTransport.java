package android.location;

import android.content.Context;
import android.location.GnssNavigationMessage;
import android.location.IGnssNavigationMessageListener;
import android.location.LocalListenerHelper;
import android.os.RemoteException;

/* access modifiers changed from: package-private */
public class GnssNavigationMessageCallbackTransport extends LocalListenerHelper<GnssNavigationMessage.Callback> {
    private final IGnssNavigationMessageListener mListenerTransport = new ListenerTransport();
    private final ILocationManager mLocationManager;

    public GnssNavigationMessageCallbackTransport(Context context, ILocationManager locationManager) {
        super(context, "GnssNavigationMessageCallbackTransport");
        this.mLocationManager = locationManager;
    }

    /* access modifiers changed from: protected */
    @Override // android.location.LocalListenerHelper
    public boolean registerWithServer() throws RemoteException {
        return this.mLocationManager.addGnssNavigationMessageListener(this.mListenerTransport, getContext().getPackageName());
    }

    /* access modifiers changed from: protected */
    @Override // android.location.LocalListenerHelper
    public void unregisterFromServer() throws RemoteException {
        this.mLocationManager.removeGnssNavigationMessageListener(this.mListenerTransport);
    }

    private class ListenerTransport extends IGnssNavigationMessageListener.Stub {
        private ListenerTransport() {
        }

        @Override // android.location.IGnssNavigationMessageListener
        public void onGnssNavigationMessageReceived(final GnssNavigationMessage event) {
            GnssNavigationMessageCallbackTransport.this.foreach(new LocalListenerHelper.ListenerOperation<GnssNavigationMessage.Callback>() {
                /* class android.location.GnssNavigationMessageCallbackTransport.ListenerTransport.AnonymousClass1 */

                public void execute(GnssNavigationMessage.Callback callback) throws RemoteException {
                    callback.onGnssNavigationMessageReceived(event);
                }
            });
        }

        @Override // android.location.IGnssNavigationMessageListener
        public void onStatusChanged(final int status) {
            GnssNavigationMessageCallbackTransport.this.foreach(new LocalListenerHelper.ListenerOperation<GnssNavigationMessage.Callback>() {
                /* class android.location.GnssNavigationMessageCallbackTransport.ListenerTransport.AnonymousClass2 */

                public void execute(GnssNavigationMessage.Callback callback) throws RemoteException {
                    callback.onStatusChanged(status);
                }
            });
        }
    }
}
