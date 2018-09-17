package com.android.server.location;

import android.location.GnssMeasurementsEvent;
import android.location.IGnssMeasurementsListener;
import android.os.Handler;
import android.os.IInterface;
import android.os.RemoteException;
import android.util.Log;
import com.android.server.wm.WindowManagerService.H;
import com.android.server.wm.WindowState;

public abstract class GnssMeasurementsProvider extends RemoteListenerHelper<IGnssMeasurementsListener> {
    private static final String TAG = "GnssMeasurementsProvider";

    /* renamed from: com.android.server.location.GnssMeasurementsProvider.1 */
    class AnonymousClass1 implements ListenerOperation<IGnssMeasurementsListener> {
        final /* synthetic */ GnssMeasurementsEvent val$event;

        AnonymousClass1(GnssMeasurementsEvent val$event) {
            this.val$event = val$event;
        }

        public void execute(IGnssMeasurementsListener listener) throws RemoteException {
            listener.onGnssMeasurementsReceived(this.val$event);
        }
    }

    private static class StatusChangedOperation implements ListenerOperation<IGnssMeasurementsListener> {
        private final int mStatus;

        public StatusChangedOperation(int status) {
            this.mStatus = status;
        }

        public void execute(IGnssMeasurementsListener listener) throws RemoteException {
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

    protected GnssMeasurementsProvider(Handler handler) {
        super(handler, TAG);
    }

    public void onMeasurementsAvailable(GnssMeasurementsEvent event) {
        foreach(new AnonymousClass1(event));
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
