package com.android.server.location;

import android.location.IGnssStatusListener;
import android.os.Handler;
import android.os.RemoteException;

abstract class GnssStatusListenerHelper extends RemoteListenerHelper<IGnssStatusListener> {

    private interface Operation extends ListenerOperation<IGnssStatusListener> {
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
        foreachDirect(new Operation() {
            public void execute(IGnssStatusListener listener) throws RemoteException {
                listener.onSvStatusChanged(i, iArr, fArr, fArr2, fArr3, fArr4);
            }
        });
    }

    public void onNmeaReceived(final long timestamp, final String nmea) {
        foreachDirect(new Operation() {
            public void execute(IGnssStatusListener listener) throws RemoteException {
                listener.onNmeaReceived(timestamp, nmea);
            }
        });
    }
}
