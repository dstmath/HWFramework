package com.android.server.om;

import com.android.server.om.OverlayManagerSettings;
import java.util.function.ToIntFunction;

/* renamed from: com.android.server.om.-$$Lambda$OverlayManagerSettings$vXm2C4y9Q-F5yYZNimB-Lr6w-oI  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$OverlayManagerSettings$vXm2C4y9QF5yYZNimBLr6woI implements ToIntFunction {
    public static final /* synthetic */ $$Lambda$OverlayManagerSettings$vXm2C4y9QF5yYZNimBLr6woI INSTANCE = new $$Lambda$OverlayManagerSettings$vXm2C4y9QF5yYZNimBLr6woI();

    private /* synthetic */ $$Lambda$OverlayManagerSettings$vXm2C4y9QF5yYZNimBLr6woI() {
    }

    @Override // java.util.function.ToIntFunction
    public final int applyAsInt(Object obj) {
        return ((OverlayManagerSettings.SettingsItem) obj).getUserId();
    }
}
