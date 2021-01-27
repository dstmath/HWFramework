package com.android.server.location;

import android.content.Context;
import android.location.IGnssStatusListener;
import android.os.Handler;
import android.os.IInterface;
import android.os.RemoteException;
import android.util.Log;
import com.android.server.location.RemoteListenerHelper;

public abstract class GnssStatusListenerHelper extends RemoteListenerHelper<IGnssStatusListener> {
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final String TAG = "GnssStatusListenerHelper";

    protected GnssStatusListenerHelper(Context context, Handler handler) {
        super(context, handler, TAG);
        setSupported(GnssLocationProvider.isSupported());
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.location.RemoteListenerHelper
    public int registerWithService() {
        return 0;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.location.RemoteListenerHelper
    public void unregisterFromService() {
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.location.RemoteListenerHelper
    public RemoteListenerHelper.ListenerOperation<IGnssStatusListener> getHandlerOperation(int result) {
        return null;
    }

    public void onStatusChanged(boolean isNavigating) {
        if (isNavigating) {
            foreach($$Lambda$GnssStatusListenerHelper$H9Tg_OtCE9BSJiAQYs_ITHFpiHU.INSTANCE);
        } else {
            foreach($$Lambda$GnssStatusListenerHelper$6s2HBSMgP5pXrugfCvtIf9QHndI.INSTANCE);
        }
    }

    public void onFirstFix(int timeToFirstFix) {
        foreach(new RemoteListenerHelper.ListenerOperation(timeToFirstFix) {
            /* class com.android.server.location.$$Lambda$GnssStatusListenerHelper$0MNjUouf1HJVcFD10rzoJIkzCrw */
            private final /* synthetic */ int f$0;

            {
                this.f$0 = r1;
            }

            @Override // com.android.server.location.RemoteListenerHelper.ListenerOperation
            public final void execute(IInterface iInterface, CallerIdentity callerIdentity) {
                ((IGnssStatusListener) iInterface).onFirstFix(this.f$0);
            }
        });
    }

    public void onSvStatusChanged(int svCount, int[] prnWithFlags, float[] cn0s, float[] elevations, float[] azimuths, float[] carrierFreqs) {
        foreach(new RemoteListenerHelper.ListenerOperation(svCount, prnWithFlags, cn0s, elevations, azimuths, carrierFreqs) {
            /* class com.android.server.location.$$Lambda$GnssStatusListenerHelper$68FOYPQxCAVSdtoWmmZNfYGGIJE */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ int[] f$2;
            private final /* synthetic */ float[] f$3;
            private final /* synthetic */ float[] f$4;
            private final /* synthetic */ float[] f$5;
            private final /* synthetic */ float[] f$6;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
                this.f$5 = r6;
                this.f$6 = r7;
            }

            @Override // com.android.server.location.RemoteListenerHelper.ListenerOperation
            public final void execute(IInterface iInterface, CallerIdentity callerIdentity) {
                GnssStatusListenerHelper.this.lambda$onSvStatusChanged$3$GnssStatusListenerHelper(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, (IGnssStatusListener) iInterface, callerIdentity);
            }
        });
    }

    public /* synthetic */ void lambda$onSvStatusChanged$3$GnssStatusListenerHelper(int svCount, int[] prnWithFlags, float[] cn0s, float[] elevations, float[] azimuths, float[] carrierFreqs, IGnssStatusListener listener, CallerIdentity callerIdentity) throws RemoteException {
        if (!hasPermission(this.mContext, callerIdentity)) {
            logPermissionDisabledEventNotReported(TAG, callerIdentity.mPackageName, "GNSS status");
        } else {
            listener.onSvStatusChanged(svCount, prnWithFlags, cn0s, elevations, azimuths, carrierFreqs);
        }
    }

    public void onNmeaReceived(long timestamp, String nmea) {
        foreach(new RemoteListenerHelper.ListenerOperation(timestamp, nmea) {
            /* class com.android.server.location.$$Lambda$GnssStatusListenerHelper$AtHI8E6PAjonHH1N0ZGabW0VF6c */
            private final /* synthetic */ long f$1;
            private final /* synthetic */ String f$2;

            {
                this.f$1 = r2;
                this.f$2 = r4;
            }

            @Override // com.android.server.location.RemoteListenerHelper.ListenerOperation
            public final void execute(IInterface iInterface, CallerIdentity callerIdentity) {
                GnssStatusListenerHelper.this.lambda$onNmeaReceived$4$GnssStatusListenerHelper(this.f$1, this.f$2, (IGnssStatusListener) iInterface, callerIdentity);
            }
        });
    }

    public /* synthetic */ void lambda$onNmeaReceived$4$GnssStatusListenerHelper(long timestamp, String nmea, IGnssStatusListener listener, CallerIdentity callerIdentity) throws RemoteException {
        if (!hasPermission(this.mContext, callerIdentity)) {
            logPermissionDisabledEventNotReported(TAG, callerIdentity.mPackageName, "NMEA");
        } else {
            listener.onNmeaReceived(timestamp, nmea);
        }
    }
}
