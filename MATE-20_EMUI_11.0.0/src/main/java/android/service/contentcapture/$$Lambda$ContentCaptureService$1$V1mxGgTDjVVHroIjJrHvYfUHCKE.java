package android.service.contentcapture;

import java.util.function.BiConsumer;

/* renamed from: android.service.contentcapture.-$$Lambda$ContentCaptureService$1$V1mxGgTDjVVHroIjJrHvYfUHCKE  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ContentCaptureService$1$V1mxGgTDjVVHroIjJrHvYfUHCKE implements BiConsumer {
    public static final /* synthetic */ $$Lambda$ContentCaptureService$1$V1mxGgTDjVVHroIjJrHvYfUHCKE INSTANCE = new $$Lambda$ContentCaptureService$1$V1mxGgTDjVVHroIjJrHvYfUHCKE();

    private /* synthetic */ $$Lambda$ContentCaptureService$1$V1mxGgTDjVVHroIjJrHvYfUHCKE() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((ContentCaptureService) obj).handleOnActivityEvent((ActivityEvent) obj2);
    }
}
