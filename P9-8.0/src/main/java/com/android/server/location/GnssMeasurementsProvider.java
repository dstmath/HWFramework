package com.android.server.location;

import android.location.GnssMeasurementsEvent;
import android.location.IGnssMeasurementsListener;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;

public abstract class GnssMeasurementsProvider extends RemoteListenerHelper<IGnssMeasurementsListener> {
    private static final String TAG = "GnssMeasurementsProvider";

    private static class StatusChangedOperation implements ListenerOperation<IGnssMeasurementsListener> {
        private final int mStatus;

        public StatusChangedOperation(int status) {
            this.mStatus = status;
        }

        public void execute(IGnssMeasurementsListener listener) throws RemoteException {
            listener.onStatusChanged(this.mStatus);
        }
    }

    protected GnssMeasurementsProvider(Handler handler) {
        super(handler, TAG);
    }

    public void onMeasurementsAvailable(final GnssMeasurementsEvent event) {
        foreach(new ListenerOperation<IGnssMeasurementsListener>() {
            public void execute(IGnssMeasurementsListener listener) throws RemoteException {
                listener.onGnssMeasurementsReceived(event);
            }
        });
    }

    public void onCapabilitiesUpdated(boolean isGnssMeasurementsSupported) {
        setSupported(isGnssMeasurementsSupported);
        updateResult();
    }

    public void onGpsEnabledChanged() {
        if (tryUpdateRegistrationWithService()) {
            updateResult();
        }
    }

    protected ListenerOperation<IGnssMeasurementsListener> getHandlerOperation(int result) {
        int status;
        switch (result) {
            case 0:
                status = 1;
                break;
            case 1:
            case 2:
            case 4:
                status = 0;
                break;
            case 3:
                status = 2;
                break;
            case 5:
                return null;
            default:
                Log.v(TAG, "Unhandled addListener result: " + result);
                return null;
        }
        return new StatusChangedOperation(status);
    }
}
