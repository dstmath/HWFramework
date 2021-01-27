package com.android.server.wifi;

import com.android.server.wifi.util.IntCounter;

/* renamed from: com.android.server.wifi.-$$Lambda$WifiMetrics$2FAdjVJtGK2Wuyu18kKp1OyQsI4  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$WifiMetrics$2FAdjVJtGK2Wuyu18kKp1OyQsI4 implements IntCounter.ProtobufConverter {
    public static final /* synthetic */ $$Lambda$WifiMetrics$2FAdjVJtGK2Wuyu18kKp1OyQsI4 INSTANCE = new $$Lambda$WifiMetrics$2FAdjVJtGK2Wuyu18kKp1OyQsI4();

    private /* synthetic */ $$Lambda$WifiMetrics$2FAdjVJtGK2Wuyu18kKp1OyQsI4() {
    }

    @Override // com.android.server.wifi.util.IntCounter.ProtobufConverter
    public final Object convert(int i, int i2) {
        return WifiMetrics.lambda$consolidateProto$0(i, i2);
    }
}
