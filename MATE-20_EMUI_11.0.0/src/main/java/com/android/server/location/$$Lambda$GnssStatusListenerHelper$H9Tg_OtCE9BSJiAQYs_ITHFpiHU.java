package com.android.server.location;

import android.location.IGnssStatusListener;
import android.os.IInterface;
import com.android.server.location.RemoteListenerHelper;

/* renamed from: com.android.server.location.-$$Lambda$GnssStatusListenerHelper$H9Tg_OtCE9BSJiAQYs_ITHFpiHU  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$GnssStatusListenerHelper$H9Tg_OtCE9BSJiAQYs_ITHFpiHU implements RemoteListenerHelper.ListenerOperation {
    public static final /* synthetic */ $$Lambda$GnssStatusListenerHelper$H9Tg_OtCE9BSJiAQYs_ITHFpiHU INSTANCE = new $$Lambda$GnssStatusListenerHelper$H9Tg_OtCE9BSJiAQYs_ITHFpiHU();

    private /* synthetic */ $$Lambda$GnssStatusListenerHelper$H9Tg_OtCE9BSJiAQYs_ITHFpiHU() {
    }

    @Override // com.android.server.location.RemoteListenerHelper.ListenerOperation
    public final void execute(IInterface iInterface, CallerIdentity callerIdentity) {
        ((IGnssStatusListener) iInterface).onGnssStarted();
    }
}
