package com.android.server.location;

import android.location.IGnssStatusListener;
import android.os.Handler;
import android.os.RemoteException;
import com.android.server.location.RemoteListenerHelper;

abstract class GnssStatusListenerHelper extends RemoteListenerHelper<IGnssStatusListener> {

    private interface Operation extends RemoteListenerHelper.ListenerOperation<IGnssStatusListener> {
    }

    protected GnssStatusListenerHelper(Handler handler) {
        super(handler, "GnssStatusListenerHelper");
        setSupported(GnssLocationProvider.isSupported());
    }

    /* access modifiers changed from: protected */
    public int registerWithService() {
        return 0;
    }

    /* access modifiers changed from: protected */
    public void unregisterFromService() {
    }

    /* access modifiers changed from: protected */
    public RemoteListenerHelper.ListenerOperation<IGnssStatusListener> getHandlerOperation(int result) {
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

    public void onFirstFix(final int timeToFirstFix) {
        foreach(new Operation() {
            public void execute(IGnssStatusListener listener) throws RemoteException {
                listener.onFirstFix(timeToFirstFix);
            }
        });
    }

    public void onSvStatusChanged(int svCount, int[] prnWithFlags, float[] cn0s, float[] elevations, float[] azimuths, float[] carrierFreqs) {
        final int i = svCount;
        final int[] iArr = prnWithFlags;
        final float[] fArr = cn0s;
        final float[] fArr2 = elevations;
        final float[] fArr3 = azimuths;
        final float[] fArr4 = carrierFreqs;
        AnonymousClass4 r0 = new Operation() {
            public void execute(IGnssStatusListener listener) throws RemoteException {
                listener.onSvStatusChanged(i, iArr, fArr, fArr2, fArr3, fArr4);
            }
        };
        foreachDirect(r0);
    }

    public void onNmeaReceived(final long timestamp, final String nmea) {
        foreachDirect(new Operation() {
            public void execute(IGnssStatusListener listener) throws RemoteException {
                listener.onNmeaReceived(timestamp, nmea);
            }
        });
    }
}
