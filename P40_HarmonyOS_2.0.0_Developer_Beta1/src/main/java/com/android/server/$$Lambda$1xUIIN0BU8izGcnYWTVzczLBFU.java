package com.android.server;

import com.android.server.NsdService;

/* renamed from: com.android.server.-$$Lambda$1xUIIN0BU8izGcnYWT-VzczLBFU  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$1xUIIN0BU8izGcnYWTVzczLBFU implements NsdService.DaemonConnectionSupplier {
    public static final /* synthetic */ $$Lambda$1xUIIN0BU8izGcnYWTVzczLBFU INSTANCE = new $$Lambda$1xUIIN0BU8izGcnYWTVzczLBFU();

    private /* synthetic */ $$Lambda$1xUIIN0BU8izGcnYWTVzczLBFU() {
    }

    @Override // com.android.server.NsdService.DaemonConnectionSupplier
    public final NsdService.DaemonConnection get(NsdService.NativeCallbackReceiver nativeCallbackReceiver) {
        return new NsdService.DaemonConnection(nativeCallbackReceiver);
    }
}
