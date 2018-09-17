package com.android.server.location;

import android.location.IGnssStatusListener;
import android.os.Handler;
import android.os.RemoteException;

abstract class GnssStatusListenerHelper extends RemoteListenerHelper<IGnssStatusListener> {

    private interface Operation extends ListenerOperation<IGnssStatusListener> {
    }

    /* renamed from: com.android.server.location.GnssStatusListenerHelper.3 */
    class AnonymousClass3 implements Operation {
        final /* synthetic */ int val$timeToFirstFix;

        AnonymousClass3(int val$timeToFirstFix) {
            this.val$timeToFirstFix = val$timeToFirstFix;
        }

        public void execute(IGnssStatusListener listener) throws RemoteException {
            listener.onFirstFix(this.val$timeToFirstFix);
        }
    }

    /* renamed from: com.android.server.location.GnssStatusListenerHelper.4 */
    class AnonymousClass4 implements Operation {
        final /* synthetic */ float[] val$azimuths;
        final /* synthetic */ float[] val$cn0s;
        final /* synthetic */ float[] val$elevations;
        final /* synthetic */ int[] val$prnWithFlags;
        final /* synthetic */ int val$svCount;

        AnonymousClass4(int val$svCount, int[] val$prnWithFlags, float[] val$cn0s, float[] val$elevations, float[] val$azimuths) {
            this.val$svCount = val$svCount;
            this.val$prnWithFlags = val$prnWithFlags;
            this.val$cn0s = val$cn0s;
            this.val$elevations = val$elevations;
            this.val$azimuths = val$azimuths;
        }

        public void execute(IGnssStatusListener listener) throws RemoteException {
            listener.onSvStatusChanged(this.val$svCount, this.val$prnWithFlags, this.val$cn0s, this.val$elevations, this.val$azimuths);
        }
    }

    /* renamed from: com.android.server.location.GnssStatusListenerHelper.5 */
    class AnonymousClass5 implements Operation {
        final /* synthetic */ String val$nmea;
        final /* synthetic */ long val$timestamp;

        AnonymousClass5(long val$timestamp, String val$nmea) {
            this.val$timestamp = val$timestamp;
            this.val$nmea = val$nmea;
        }

        public void execute(IGnssStatusListener listener) throws RemoteException {
            listener.onNmeaReceived(this.val$timestamp, this.val$nmea);
        }
    }

    protected GnssStatusListenerHelper(Handler handler) {
        super(handler, "GnssStatusListenerHelper");
        setSupported(GnssLocationProvider.isSupported());
    }

    protected boolean registerWithService() {
        return true;
    }

    protected void unregisterFromService() {
    }

    protected ListenerOperation<IGnssStatusListener> getHandlerOperation(int result) {
        return null;
    }

    public void onStatusChanged(boolean isNavigating) {
        Operation operation;
        if (isNavigating) {
            operation = new Operation() {
                public void execute(IGnssStatusListener listener) throws RemoteException {
                    listener.onGnssStarted();
                }
            };
        } else {
            operation = new Operation() {
                public void execute(IGnssStatusListener listener) throws RemoteException {
                    listener.onGnssStopped();
                }
            };
        }
        foreach(operation);
    }

    public void onFirstFix(int timeToFirstFix) {
        foreach(new AnonymousClass3(timeToFirstFix));
    }

    public void onSvStatusChanged(int svCount, int[] prnWithFlags, float[] cn0s, float[] elevations, float[] azimuths) {
        foreachDirect(new AnonymousClass4(svCount, prnWithFlags, cn0s, elevations, azimuths));
    }

    public void onNmeaReceived(long timestamp, String nmea) {
        foreachDirect(new AnonymousClass5(timestamp, nmea));
    }
}
