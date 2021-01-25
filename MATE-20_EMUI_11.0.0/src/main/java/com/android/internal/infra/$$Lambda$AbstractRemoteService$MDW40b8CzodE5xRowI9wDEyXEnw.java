package com.android.internal.infra;

import java.util.function.Consumer;

/* renamed from: com.android.internal.infra.-$$Lambda$AbstractRemoteService$MDW40b8CzodE5xRowI9wDEyXEnw  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AbstractRemoteService$MDW40b8CzodE5xRowI9wDEyXEnw implements Consumer {
    public static final /* synthetic */ $$Lambda$AbstractRemoteService$MDW40b8CzodE5xRowI9wDEyXEnw INSTANCE = new $$Lambda$AbstractRemoteService$MDW40b8CzodE5xRowI9wDEyXEnw();

    private /* synthetic */ $$Lambda$AbstractRemoteService$MDW40b8CzodE5xRowI9wDEyXEnw() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((AbstractRemoteService) obj).handleUnbind();
    }
}
