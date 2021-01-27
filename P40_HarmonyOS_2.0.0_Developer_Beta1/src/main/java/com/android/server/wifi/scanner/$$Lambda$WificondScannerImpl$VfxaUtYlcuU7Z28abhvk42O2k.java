package com.android.server.wifi.scanner;

import com.android.server.wifi.ScanDetail;
import java.util.function.Function;

/* renamed from: com.android.server.wifi.scanner.-$$Lambda$WificondScannerImpl$VfxaUtYlcuU7--Z28abhvk42O2k  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$WificondScannerImpl$VfxaUtYlcuU7Z28abhvk42O2k implements Function {
    public static final /* synthetic */ $$Lambda$WificondScannerImpl$VfxaUtYlcuU7Z28abhvk42O2k INSTANCE = new $$Lambda$WificondScannerImpl$VfxaUtYlcuU7Z28abhvk42O2k();

    private /* synthetic */ $$Lambda$WificondScannerImpl$VfxaUtYlcuU7Z28abhvk42O2k() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return ((ScanDetail) obj).getScanResult();
    }
}
