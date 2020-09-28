package com.huawei.internal.telephony.dataconnection;

import com.android.internal.telephony.dataconnection.ApnContext;
import java.util.function.Consumer;

/* renamed from: com.huawei.internal.telephony.dataconnection.-$$Lambda$DcTrackerEx$oYBRqVUcAH9G7X58w04FCRO7FZQ  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$DcTrackerEx$oYBRqVUcAH9G7X58w04FCRO7FZQ implements Consumer {
    public static final /* synthetic */ $$Lambda$DcTrackerEx$oYBRqVUcAH9G7X58w04FCRO7FZQ INSTANCE = new $$Lambda$DcTrackerEx$oYBRqVUcAH9G7X58w04FCRO7FZQ();

    private /* synthetic */ $$Lambda$DcTrackerEx$oYBRqVUcAH9G7X58w04FCRO7FZQ() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        DcTrackerEx.lambda$resetDefaultApnRetryCount$0((ApnContext) obj);
    }
}
