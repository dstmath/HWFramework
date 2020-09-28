package com.android.internal.telephony;

import android.telephony.CellInfo;
import java.util.function.Function;

/* renamed from: com.android.internal.telephony.-$$Lambda$OXXtpNvVeJw7E7y9hLioSYgFy9A  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$OXXtpNvVeJw7E7y9hLioSYgFy9A implements Function {
    public static final /* synthetic */ $$Lambda$OXXtpNvVeJw7E7y9hLioSYgFy9A INSTANCE = new $$Lambda$OXXtpNvVeJw7E7y9hLioSYgFy9A();

    private /* synthetic */ $$Lambda$OXXtpNvVeJw7E7y9hLioSYgFy9A() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return ((CellInfo) obj).sanitizeLocationInfo();
    }
}
