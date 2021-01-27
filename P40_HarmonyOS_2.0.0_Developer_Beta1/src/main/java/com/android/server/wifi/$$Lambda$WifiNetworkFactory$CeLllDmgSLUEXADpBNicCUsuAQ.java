package com.android.server.wifi;

import android.net.wifi.ScanResult;
import java.util.function.Function;

/* renamed from: com.android.server.wifi.-$$Lambda$WifiNetworkFactory$CeLllDmgSLUEXAD-pBNicCUsuAQ  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$WifiNetworkFactory$CeLllDmgSLUEXADpBNicCUsuAQ implements Function {
    public static final /* synthetic */ $$Lambda$WifiNetworkFactory$CeLllDmgSLUEXADpBNicCUsuAQ INSTANCE = new $$Lambda$WifiNetworkFactory$CeLllDmgSLUEXADpBNicCUsuAQ();

    private /* synthetic */ $$Lambda$WifiNetworkFactory$CeLllDmgSLUEXADpBNicCUsuAQ() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return Integer.valueOf(((ScanResult) obj).level);
    }
}
