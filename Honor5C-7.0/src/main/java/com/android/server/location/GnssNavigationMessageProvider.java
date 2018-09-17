package com.android.server.location;

import android.location.GnssNavigationMessage;
import android.location.IGnssNavigationMessageListener;
import android.os.Handler;
import android.os.IInterface;
import android.os.RemoteException;
import android.util.Log;
import com.android.server.wm.WindowManagerService.H;
import com.android.server.wm.WindowState;

public abstract class GnssNavigationMessageProvider extends RemoteListenerHelper<IGnssNavigationMessageListener> {
    private static final String TAG = "GnssNavigationMessageProvider";

    /* renamed from: com.android.server.location.GnssNavigationMessageProvider.1 */
    class AnonymousClass1 implements ListenerOperation<IGnssNavigationMessageListener> {
        final /* synthetic */ GnssNavigationMessage val$event;

        AnonymousClass1(GnssNavigationMessage val$event) {
            this.val$event = val$event;
        }

        public void execute(IGnssNavigationMessageListener listener) throws RemoteException {
            listener.onGnssNavigationMessageReceived(this.val$event);
        }
    }

    private static class StatusChangedOperation implements ListenerOperation<IGnssNavigationMessageListener> {
        private final int mStatus;

        public StatusChangedOperation(int status) {
            this.mStatus = status;
        }

        public void execute(IGnssNavigationMessageListener listener) throws RemoteException {
            listener.onStatusChanged(this.mStatus);
        }
    }

    public /* bridge */ /* synthetic */ boolean addListener(IInterface listener) {
        return super.addListener(listener);
    }

    public /* bridge */ /* synthetic */ boolean addListener(IInterface listener, String pkgName) {
        return super.addListener(listener, pkgName);
    }

    public /* bridge */ /* synthetic */ void removeListener(IInterface listener) {
        super.removeListener(listener);
    }

    protected GnssNavigationMessageProvider(Handler handler) {
        super(handler, TAG);
    }

    public void onNavigationMessageAvailable(GnssNavigationMessage event) {
        foreach(new AnonymousClass1(event));
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
            case WindowState.LOW_RESOLUTION_FEATURE_OFF /*0*/:
                status = 1;
                break;
            case WindowState.LOW_RESOLUTION_COMPOSITION_OFF /*1*/:
            case WindowState.LOW_RESOLUTION_COMPOSITION_ON /*2*/:
            case H.DO_TRAVERSAL /*4*/:
                status = 0;
                break;
            case H.REPORT_LOSING_FOCUS /*3*/:
                status = 2;
                break;
            case H.ADD_STARTING /*5*/:
                return null;
            default:
                Log.v(TAG, "Unhandled addListener result: " + result);
                return null;
        }
        return new StatusChangedOperation(status);
    }
}
