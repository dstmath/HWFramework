package com.android.internal.infra;

import java.util.function.Consumer;

/* renamed from: com.android.internal.infra.-$$Lambda$AbstractRemoteService$9IBVTCLLZgndvH7fu1P14PW1_1o  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AbstractRemoteService$9IBVTCLLZgndvH7fu1P14PW1_1o implements Consumer {
    public static final /* synthetic */ $$Lambda$AbstractRemoteService$9IBVTCLLZgndvH7fu1P14PW1_1o INSTANCE = new $$Lambda$AbstractRemoteService$9IBVTCLLZgndvH7fu1P14PW1_1o();

    private /* synthetic */ $$Lambda$AbstractRemoteService$9IBVTCLLZgndvH7fu1P14PW1_1o() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((AbstractRemoteService) obj).handleDestroy();
    }
}
