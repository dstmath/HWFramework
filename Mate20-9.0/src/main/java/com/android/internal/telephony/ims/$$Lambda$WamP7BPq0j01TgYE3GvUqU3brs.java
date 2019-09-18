package com.android.internal.telephony.ims;

import android.content.Context;
import com.android.internal.telephony.ims.ImsResolver;
import com.android.internal.telephony.ims.ImsServiceFeatureQueryManager;

/* renamed from: com.android.internal.telephony.ims.-$$Lambda$WamP7BPq0j01TgYE3GvUqU3b-rs  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$WamP7BPq0j01TgYE3GvUqU3brs implements ImsResolver.ImsDynamicQueryManagerFactory {
    public static final /* synthetic */ $$Lambda$WamP7BPq0j01TgYE3GvUqU3brs INSTANCE = new $$Lambda$WamP7BPq0j01TgYE3GvUqU3brs();

    private /* synthetic */ $$Lambda$WamP7BPq0j01TgYE3GvUqU3brs() {
    }

    public final ImsServiceFeatureQueryManager create(Context context, ImsServiceFeatureQueryManager.Listener listener) {
        return new ImsServiceFeatureQueryManager(context, listener);
    }
}
