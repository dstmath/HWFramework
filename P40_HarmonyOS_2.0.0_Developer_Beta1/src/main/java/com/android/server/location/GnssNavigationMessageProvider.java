package com.android.server.location;

import android.content.Context;
import android.location.GnssNavigationMessage;
import android.location.IGnssNavigationMessageListener;
import android.os.Handler;
import android.os.IInterface;
import android.os.RemoteException;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.location.RemoteListenerHelper;

public abstract class GnssNavigationMessageProvider extends RemoteListenerHelper<IGnssNavigationMessageListener> {
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final String TAG = "GnssNavigationMessageProvider";
    private boolean mCollectionStarted;
    private final GnssNavigationMessageProviderNative mNative;

    /* access modifiers changed from: private */
    public static native boolean native_is_navigation_message_supported();

    /* access modifiers changed from: private */
    public static native boolean native_start_navigation_message_collection();

    /* access modifiers changed from: private */
    public static native boolean native_stop_navigation_message_collection();

    protected GnssNavigationMessageProvider(Context context, Handler handler) {
        this(context, handler, new GnssNavigationMessageProviderNative());
    }

    @VisibleForTesting
    GnssNavigationMessageProvider(Context context, Handler handler, GnssNavigationMessageProviderNative aNative) {
        super(context, handler, TAG);
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
    @Override // com.android.server.location.RemoteListenerHelper
    public boolean isAvailableInPlatform() {
        return this.mNative.isNavigationMessageSupported();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.location.RemoteListenerHelper
    public int registerWithService() {
        if (!this.mNative.startNavigationMessageCollection()) {
            return 4;
        }
        this.mCollectionStarted = true;
        return 0;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.location.RemoteListenerHelper
    public void unregisterFromService() {
        if (this.mNative.stopNavigationMessageCollection()) {
            this.mCollectionStarted = false;
        }
    }

    public void onNavigationMessageAvailable(GnssNavigationMessage event) {
        foreach(new RemoteListenerHelper.ListenerOperation(event) {
            /* class com.android.server.location.$$Lambda$GnssNavigationMessageProvider$FPgP5DRMyheqM1CQ4z7jkoJwIFg */
            private final /* synthetic */ GnssNavigationMessage f$0;

            {
                this.f$0 = r1;
            }

            @Override // com.android.server.location.RemoteListenerHelper.ListenerOperation
            public final void execute(IInterface iInterface, CallerIdentity callerIdentity) {
                ((IGnssNavigationMessageListener) iInterface).onGnssNavigationMessageReceived(this.f$0);
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
    @Override // com.android.server.location.RemoteListenerHelper
    public RemoteListenerHelper.ListenerOperation<IGnssNavigationMessageListener> getHandlerOperation(int result) {
        int status;
        if (result != 0) {
            if (!(result == 1 || result == 2)) {
                if (result == 3) {
                    status = 2;
                } else if (result != 4) {
                    if (result == 5) {
                        return null;
                    }
                    Log.v(TAG, "Unhandled addListener result: " + result);
                    return null;
                }
            }
            status = 0;
        } else {
            status = 1;
        }
        return new StatusChangedOperation(status);
    }

    private static class StatusChangedOperation implements RemoteListenerHelper.ListenerOperation<IGnssNavigationMessageListener> {
        private final int mStatus;

        public StatusChangedOperation(int status) {
            this.mStatus = status;
        }

        public void execute(IGnssNavigationMessageListener listener, CallerIdentity callerIdentity) throws RemoteException {
            listener.onStatusChanged(this.mStatus);
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public static class GnssNavigationMessageProviderNative {
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
}
