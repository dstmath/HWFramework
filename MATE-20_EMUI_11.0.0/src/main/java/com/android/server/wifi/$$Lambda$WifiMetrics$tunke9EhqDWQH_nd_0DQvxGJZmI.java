package com.android.server.wifi;

import com.android.server.wifi.util.IntCounter;

/* renamed from: com.android.server.wifi.-$$Lambda$WifiMetrics$tunke9EhqDWQH_nd_0DQvxGJZmI  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$WifiMetrics$tunke9EhqDWQH_nd_0DQvxGJZmI implements IntCounter.ProtobufConverter {
    public static final /* synthetic */ $$Lambda$WifiMetrics$tunke9EhqDWQH_nd_0DQvxGJZmI INSTANCE = new $$Lambda$WifiMetrics$tunke9EhqDWQH_nd_0DQvxGJZmI();

    private /* synthetic */ $$Lambda$WifiMetrics$tunke9EhqDWQH_nd_0DQvxGJZmI() {
    }

    @Override // com.android.server.wifi.util.IntCounter.ProtobufConverter
    public final Object convert(int i, int i2) {
        return WifiMetrics.lambda$consolidateProto$2(i, i2);
    }
}
