package com.android.server.location;

import android.content.Context;
import android.location.GnssMeasurementsEvent;
import android.location.IGnssMeasurementsListener;
import android.os.Handler;
import android.os.IInterface;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.location.RemoteListenerHelper;

public abstract class GnssMeasurementsProvider extends RemoteListenerHelper<IGnssMeasurementsListener> {
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final String TAG = "GnssMeasurementsProvider";
    private final Context mContext;
    private boolean mEnableFullTracking;
    private boolean mIsCollectionStarted;
    private final GnssMeasurementProviderNative mNative;

    @VisibleForTesting
    static class GnssMeasurementProviderNative {
        GnssMeasurementProviderNative() {
        }

        public boolean isMeasurementSupported() {
            return GnssMeasurementsProvider.native_is_measurement_supported();
        }

        public boolean startMeasurementCollection(boolean enableFullTracking) {
            return GnssMeasurementsProvider.native_start_measurement_collection(enableFullTracking);
        }

        public boolean stopMeasurementCollection() {
            return GnssMeasurementsProvider.native_stop_measurement_collection();
        }
    }

    private static class StatusChangedOperation implements RemoteListenerHelper.ListenerOperation<IGnssMeasurementsListener> {
        private final int mStatus;

        public StatusChangedOperation(int status) {
            this.mStatus = status;
        }

        public void execute(IGnssMeasurementsListener listener) throws RemoteException {
            listener.onStatusChanged(this.mStatus);
        }
    }

    /* access modifiers changed from: private */
    public static native boolean native_is_measurement_supported();

    /* access modifiers changed from: private */
    public static native boolean native_start_measurement_collection(boolean z);

    /* access modifiers changed from: private */
    public static native boolean native_stop_measurement_collection();

    public /* bridge */ /* synthetic */ boolean isRegistered() {
        return super.isRegistered();
    }

    protected GnssMeasurementsProvider(Context context, Handler handler) {
        this(context, handler, new GnssMeasurementProviderNative());
    }

    @VisibleForTesting
    GnssMeasurementsProvider(Context context, Handler handler, GnssMeasurementProviderNative aNative) {
        super(handler, TAG);
        this.mContext = context;
        this.mNative = aNative;
    }

    /* access modifiers changed from: package-private */
    public void resumeIfStarted() {
        if (DEBUG) {
            Log.d(TAG, "resumeIfStarted");
        }
        if (this.mIsCollectionStarted) {
            this.mNative.startMeasurementCollection(this.mEnableFullTracking);
        }
    }

    public boolean isAvailableInPlatform() {
        return this.mNative.isMeasurementSupported();
    }

    /* access modifiers changed from: protected */
    public int registerWithService() {
        boolean enableFullTracking = Settings.Secure.getInt(this.mContext.getContentResolver(), "development_settings_enabled", 0) == 1 && Settings.Global.getInt(this.mContext.getContentResolver(), "enable_gnss_raw_meas_full_tracking", 0) == 1;
        if (!this.mNative.startMeasurementCollection(enableFullTracking)) {
            return 4;
        }
        this.mIsCollectionStarted = true;
        this.mEnableFullTracking = enableFullTracking;
        return 0;
    }

    /* access modifiers changed from: protected */
    public void unregisterFromService() {
        if (this.mNative.stopMeasurementCollection()) {
            this.mIsCollectionStarted = false;
        }
    }

    public void onMeasurementsAvailable(GnssMeasurementsEvent event) {
        foreach(new RemoteListenerHelper.ListenerOperation(event) {
            private final /* synthetic */ GnssMeasurementsEvent f$0;

            {
                this.f$0 = r1;
            }

            public final void execute(IInterface iInterface) {
                ((IGnssMeasurementsListener) iInterface).onGnssMeasurementsReceived(this.f$0);
            }
        });
    }

    public void onCapabilitiesUpdated(boolean isGnssMeasurementsSupported) {
        setSupported(isGnssMeasurementsSupported);
        updateResult();
    }

    public void onGpsEnabledChanged() {
        tryUpdateRegistrationWithService();
        updateResult();
    }

    /* access modifiers changed from: protected */
    public RemoteListenerHelper.ListenerOperation<IGnssMeasurementsListener> getHandlerOperation(int result) {
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
            case 6:
                status = 3;
                break;
            default:
                Log.v(TAG, "Unhandled addListener result: " + result);
                return null;
        }
        return new StatusChangedOperation(status);
    }
}
