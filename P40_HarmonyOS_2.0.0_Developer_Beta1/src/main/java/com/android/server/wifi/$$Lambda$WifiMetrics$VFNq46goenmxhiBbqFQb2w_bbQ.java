package com.android.server.wifi;

import com.android.server.wifi.util.ObjectCounter;

/* renamed from: com.android.server.wifi.-$$Lambda$WifiMetrics$VFNq-46goenmxhiBbqFQb2w_bbQ  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$WifiMetrics$VFNq46goenmxhiBbqFQb2w_bbQ implements ObjectCounter.ProtobufConverter {
    public static final /* synthetic */ $$Lambda$WifiMetrics$VFNq46goenmxhiBbqFQb2w_bbQ INSTANCE = new $$Lambda$WifiMetrics$VFNq46goenmxhiBbqFQb2w_bbQ();

    private /* synthetic */ $$Lambda$WifiMetrics$VFNq46goenmxhiBbqFQb2w_bbQ() {
    }

    @Override // com.android.server.wifi.util.ObjectCounter.ProtobufConverter
    public final Object convert(Object obj, int i) {
        return WifiMetrics.lambda$consolidateProto$1((String) obj, i);
    }
}
