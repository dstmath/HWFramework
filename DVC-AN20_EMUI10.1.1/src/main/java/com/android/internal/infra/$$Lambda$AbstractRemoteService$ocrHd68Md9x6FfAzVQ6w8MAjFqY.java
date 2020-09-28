package com.android.internal.infra;

import java.util.function.Consumer;

/* renamed from: com.android.internal.infra.-$$Lambda$AbstractRemoteService$ocrHd68Md9x6FfAzVQ6w8MAjFqY  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AbstractRemoteService$ocrHd68Md9x6FfAzVQ6w8MAjFqY implements Consumer {
    public static final /* synthetic */ $$Lambda$AbstractRemoteService$ocrHd68Md9x6FfAzVQ6w8MAjFqY INSTANCE = new $$Lambda$AbstractRemoteService$ocrHd68Md9x6FfAzVQ6w8MAjFqY();

    private /* synthetic */ $$Lambda$AbstractRemoteService$ocrHd68Md9x6FfAzVQ6w8MAjFqY() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((AbstractRemoteService) obj).handleBinderDied();
    }
}
