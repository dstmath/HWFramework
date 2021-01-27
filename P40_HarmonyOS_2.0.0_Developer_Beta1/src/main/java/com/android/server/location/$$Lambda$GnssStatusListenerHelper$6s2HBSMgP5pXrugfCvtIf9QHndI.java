package com.android.server.location;

import android.location.IGnssStatusListener;
import android.os.IInterface;
import com.android.server.location.RemoteListenerHelper;

/* renamed from: com.android.server.location.-$$Lambda$GnssStatusListenerHelper$6s2HBSMgP5pXrugfCvtIf9QHndI  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$GnssStatusListenerHelper$6s2HBSMgP5pXrugfCvtIf9QHndI implements RemoteListenerHelper.ListenerOperation {
    public static final /* synthetic */ $$Lambda$GnssStatusListenerHelper$6s2HBSMgP5pXrugfCvtIf9QHndI INSTANCE = new $$Lambda$GnssStatusListenerHelper$6s2HBSMgP5pXrugfCvtIf9QHndI();

    private /* synthetic */ $$Lambda$GnssStatusListenerHelper$6s2HBSMgP5pXrugfCvtIf9QHndI() {
    }

    @Override // com.android.server.location.RemoteListenerHelper.ListenerOperation
    public final void execute(IInterface iInterface, CallerIdentity callerIdentity) {
        ((IGnssStatusListener) iInterface).onGnssStopped();
    }
}
