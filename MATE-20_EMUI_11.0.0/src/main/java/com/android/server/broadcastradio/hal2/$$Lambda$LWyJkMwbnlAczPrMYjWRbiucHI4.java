package com.android.server.broadcastradio.hal2;

import android.hardware.broadcastradio.V2_0.ProgramIdentifier;
import java.util.function.Function;

/* renamed from: com.android.server.broadcastradio.hal2.-$$Lambda$LWyJkMwbnlAczPrMYjWRbiucHI4  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$LWyJkMwbnlAczPrMYjWRbiucHI4 implements Function {
    public static final /* synthetic */ $$Lambda$LWyJkMwbnlAczPrMYjWRbiucHI4 INSTANCE = new $$Lambda$LWyJkMwbnlAczPrMYjWRbiucHI4();

    private /* synthetic */ $$Lambda$LWyJkMwbnlAczPrMYjWRbiucHI4() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return Convert.programIdentifierFromHal((ProgramIdentifier) obj);
    }
}
