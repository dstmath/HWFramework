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
    private boolean mEnableFullTracking;
    private boolean mIsCollectionStarted;
    private final GnssMeasurementProviderNative mNative;

    /* access modifiers changed from: private */
    public static native boolean native_is_measurement_supported();

    /* access modifiers changed from: private */
    public static native boolean native_start_measurement_collection(boolean z);

    /* access modifiers changed from: private */
    public static native boolean native_stop_measurement_collection();

    protected GnssMeasurementsProvider(Context context, Handler handler) {
        this(context, handler, new GnssMeasurementProviderNative());
    }

    @VisibleForTesting
    GnssMeasurementsProvider(Context context, Handler handler, GnssMeasurementProviderNative aNative) {
        super(context, handler, TAG);
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

    @Override // com.android.server.location.RemoteListenerHelper
    public boolean isAvailableInPlatform() {
        return this.mNative.isMeasurementSupported();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.location.RemoteListenerHelper
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
    @Override // com.android.server.location.RemoteListenerHelper
    public void unregisterFromService() {
        if (this.mNative.stopMeasurementCollection()) {
            this.mIsCollectionStarted = false;
        }
    }

    public void onMeasurementsAvailable(GnssMeasurementsEvent event) {
        foreach(new RemoteListenerHelper.ListenerOperation(event) {
            /* class com.android.server.location.$$Lambda$GnssMeasurementsProvider$QlkbfzzYggD17FlZmrylRJr2vE */
            private final /* synthetic */ GnssMeasurementsEvent f$1;

            {
                this.f$1 = r2;
            }

            @Override // com.android.server.location.RemoteListenerHelper.ListenerOperation
            public final void execute(IInterface iInterface, CallerIdentity callerIdentity) {
                GnssMeasurementsProvider.this.lambda$onMeasurementsAvailable$0$GnssMeasurementsProvider(this.f$1, (IGnssMeasurementsListener) iInterface, callerIdentity);
            }
        });
    }

    public /* synthetic */ void lambda$onMeasurementsAvailable$0$GnssMeasurementsProvider(GnssMeasurementsEvent event, IGnssMeasurementsListener listener, CallerIdentity callerIdentity) throws RemoteException {
        if (!hasPermission(this.mContext, callerIdentity)) {
            logPermissionDisabledEventNotReported(TAG, callerIdentity.mPackageName, "GNSS measurements");
        } else {
            listener.onGnssMeasurementsReceived(event);
        }
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
    @Override // com.android.server.location.RemoteListenerHelper
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

    private static class StatusChangedOperation implements RemoteListenerHelper.ListenerOperation<IGnssMeasurementsListener> {
        private final int mStatus;

        public StatusChangedOperation(int status) {
            this.mStatus = status;
        }

        public void execute(IGnssMeasurementsListener listener, CallerIdentity callerIdentity) throws RemoteException {
            listener.onStatusChanged(this.mStatus);
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public static class GnssMeasurementProviderNative {
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
}
