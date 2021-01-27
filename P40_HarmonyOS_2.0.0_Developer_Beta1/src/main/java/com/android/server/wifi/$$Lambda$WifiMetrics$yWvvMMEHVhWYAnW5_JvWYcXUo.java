package com.android.server.wifi;

import com.android.server.wifi.util.IntCounter;

/* renamed from: com.android.server.wifi.-$$Lambda$WifiMetrics$yWvvMMEHVhWYAnW5_JvWY-c-XUo  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$WifiMetrics$yWvvMMEHVhWYAnW5_JvWYcXUo implements IntCounter.ProtobufConverter {
    public static final /* synthetic */ $$Lambda$WifiMetrics$yWvvMMEHVhWYAnW5_JvWYcXUo INSTANCE = new $$Lambda$WifiMetrics$yWvvMMEHVhWYAnW5_JvWYcXUo();

    private /* synthetic */ $$Lambda$WifiMetrics$yWvvMMEHVhWYAnW5_JvWYcXUo() {
    }

    @Override // com.android.server.wifi.util.IntCounter.ProtobufConverter
    public final Object convert(int i, int i2) {
        return WifiMetrics.lambda$convertPasspointProfilesToProto$3(i, i2);
    }
}
