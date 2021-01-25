package com.android.server.wifi;

import android.net.wifi.ScanResult;
import java.util.function.Function;

/* renamed from: com.android.server.wifi.-$$Lambda$Sgsg9Ml_dxoj_SCBslbH-6YHea8  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$Sgsg9Ml_dxoj_SCBslbH6YHea8 implements Function {
    public static final /* synthetic */ $$Lambda$Sgsg9Ml_dxoj_SCBslbH6YHea8 INSTANCE = new $$Lambda$Sgsg9Ml_dxoj_SCBslbH6YHea8();

    private /* synthetic */ $$Lambda$Sgsg9Ml_dxoj_SCBslbH6YHea8() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return ScanResultMatchInfo.fromScanResult((ScanResult) obj);
    }
}
