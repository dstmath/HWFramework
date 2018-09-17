package android.location;

import android.content.Context;
import android.location.GnssNavigationMessage.Callback;
import android.location.IGnssNavigationMessageListener.Stub;
import android.os.RemoteException;

class GnssNavigationMessageCallbackTransport extends LocalListenerHelper<Callback> {
    private final IGnssNavigationMessageListener mListenerTransport;
    private final ILocationManager mLocationManager;

    private class ListenerTransport extends Stub {

        /* renamed from: android.location.GnssNavigationMessageCallbackTransport.ListenerTransport.1 */
        class AnonymousClass1 implements ListenerOperation<Callback> {
            final /* synthetic */ GnssNavigationMessage val$event;

            AnonymousClass1(GnssNavigationMessage val$event) {
                this.val$event = val$event;
            }

            public void execute(Callback callback) throws RemoteException {
                callback.onGnssNavigationMessageReceived(this.val$event);
            }
        }

        /* renamed from: android.location.GnssNavigationMessageCallbackTransport.ListenerTransport.2 */
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

        public void onGnssNavigationMessageReceived(GnssNavigationMessage event) {
            GnssNavigationMessageCallbackTransport.this.foreach(new AnonymousClass1(event));
        }

        public void onStatusChanged(int status) {
            GnssNavigationMessageCallbackTransport.this.foreach(new AnonymousClass2(status));
        }
    }

    public GnssNavigationMessageCallbackTransport(Context context, ILocationManager locationManager) {
        super(context, "GnssNavigationMessageCallbackTransport");
        this.mListenerTransport = new ListenerTransport();
        this.mLocationManager = locationManager;
    }

    protected boolean registerWithServer() throws RemoteException {
        return this.mLocationManager.addGnssNavigationMessageListener(this.mListenerTransport, getContext().getPackageName());
    }

    protected void unregisterFromServer() throws RemoteException {
        this.mLocationManager.removeGnssNavigationMessageListener(this.mListenerTransport);
    }
}
