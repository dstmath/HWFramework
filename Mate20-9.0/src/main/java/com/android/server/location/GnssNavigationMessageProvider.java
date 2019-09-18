package com.android.server.location;

import android.location.GnssNavigationMessage;
import android.location.IGnssNavigationMessageListener;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.location.RemoteListenerHelper;

public abstract class GnssNavigationMessageProvider extends RemoteListenerHelper<IGnssNavigationMessageListener> {
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final String TAG = "GnssNavigationMessageProvider";
    private boolean mCollectionStarted;
    private final GnssNavigationMessageProviderNative mNative;

    @VisibleForTesting
    static class GnssNavigationMessageProviderNative {
        GnssNavigationMessageProviderNative() {
        }

        public boolean isNavigationMessageSupported() {
            return GnssNavigationMessageProvider.native_is_navigation_message_supported();
        }

        public boolean startNavigationMessageCollection() {
            return GnssNavigationMessageProvider.native_start_navigation_message_collection();
        }

        public boolean stopNavigationMessageCollection() {
            return GnssNavigationMessageProvider.native_stop_navigation_message_collection();
        }
    }

    private static class StatusChangedOperation implements RemoteListenerHelper.ListenerOperation<IGnssNavigationMessageListener> {
        private final int mStatus;

        public StatusChangedOperation(int status) {
            this.mStatus = status;
        }

        public void execute(IGnssNavigationMessageListener listener) throws RemoteException {
            listener.onStatusChanged(this.mStatus);
        }
    }

    /* access modifiers changed from: private */
    public static native boolean native_is_navigation_message_supported();

    /* access modifiers changed from: private */
    public static native boolean native_start_navigation_message_collection();

    /* access modifiers changed from: private */
    public static native boolean native_stop_navigation_message_collection();

    public /* bridge */ /* synthetic */ boolean isRegistered() {
        return super.isRegistered();
    }

    protected GnssNavigationMessageProvider(Handler handler) {
        this(handler, new GnssNavigationMessageProviderNative());
    }

    @VisibleForTesting
    GnssNavigationMessageProvider(Handler handler, GnssNavigationMessageProviderNative aNative) {
        super(handler, TAG);
        this.mNative = aNative;
    }

    /* access modifiers changed from: package-private */
    public void resumeIfStarted() {
        if (DEBUG) {
            Log.d(TAG, "resumeIfStarted");
        }
        if (this.mCollectionStarted) {
            this.mNative.startNavigationMessageCollection();
        }
    }

    /* access modifiers changed from: protected */
    public boolean isAvailableInPlatform() {
        return this.mNative.isNavigationMessageSupported();
    }

    /* access modifiers changed from: protected */
    public int registerWithService() {
        if (!this.mNative.startNavigationMessageCollection()) {
            return 4;
        }
        this.mCollectionStarted = true;
        return 0;
    }

    /* access modifiers changed from: protected */
    public void unregisterFromService() {
        if (this.mNative.stopNavigationMessageCollection()) {
            this.mCollectionStarted = false;
        }
    }

    public void onNavigationMessageAvailable(final GnssNavigationMessage event) {
        foreach(new RemoteListenerHelper.ListenerOperation<IGnssNavigationMessageListener>() {
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
        tryUpdateRegistrationWithService();
        updateResult();
    }

    /* access modifiers changed from: protected */
    public RemoteListenerHelper.ListenerOperation<IGnssNavigationMessageListener> getHandlerOperation(int result) {
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
