package com.android.server.location;

import android.location.GnssNavigationMessage;
import android.location.IGnssNavigationMessageListener;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;

public abstract class GnssNavigationMessageProvider extends RemoteListenerHelper<IGnssNavigationMessageListener> {
    private static final String TAG = "GnssNavigationMessageProvider";

    private static class StatusChangedOperation implements ListenerOperation<IGnssNavigationMessageListener> {
        private final int mStatus;

        public StatusChangedOperation(int status) {
            this.mStatus = status;
        }

        public void execute(IGnssNavigationMessageListener listener) throws RemoteException {
            listener.onStatusChanged(this.mStatus);
        }
    }

    protected GnssNavigationMessageProvider(Handler handler) {
        super(handler, TAG);
    }

    public void onNavigationMessageAvailable(final GnssNavigationMessage event) {
        foreach(new ListenerOperation<IGnssNavigationMessageListener>() {
            public void execute(IGnssNavigationMessageListener listener) throws RemoteException {
                listener.onGnssNavigationMessageReceived(event);
            }
        });
    }

    public void onCapabilitiesUpdated(boolean isGnssNavigationMessageSupported) {
        setSupported(isGnssNavigationMessageSupported);
        updateResult();
    }

    public void onGpsEnabledChanged() {
        if (tryUpdateRegistrationWithService()) {
            updateResult();
        }
    }

    protected ListenerOperation<IGnssNavigationMessageListener> getHandlerOperation(int result) {
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
