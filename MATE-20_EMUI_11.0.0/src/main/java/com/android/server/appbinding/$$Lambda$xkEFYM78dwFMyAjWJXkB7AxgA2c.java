package com.android.server.appbinding;

import com.android.server.appbinding.finders.AppServiceFinder;
import java.util.function.Consumer;

/* renamed from: com.android.server.appbinding.-$$Lambda$xkEFYM78dwFMyAjWJXkB7AxgA2c  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$xkEFYM78dwFMyAjWJXkB7AxgA2c implements Consumer {
    public static final /* synthetic */ $$Lambda$xkEFYM78dwFMyAjWJXkB7AxgA2c INSTANCE = new $$Lambda$xkEFYM78dwFMyAjWJXkB7AxgA2c();

    private /* synthetic */ $$Lambda$xkEFYM78dwFMyAjWJXkB7AxgA2c() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((AppServiceFinder) obj).startMonitoring();
    }
}
