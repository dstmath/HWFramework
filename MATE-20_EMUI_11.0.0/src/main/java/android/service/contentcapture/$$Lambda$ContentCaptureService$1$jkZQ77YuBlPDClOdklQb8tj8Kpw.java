package android.service.contentcapture;

import java.util.function.BiConsumer;

/* renamed from: android.service.contentcapture.-$$Lambda$ContentCaptureService$1$jkZQ77YuBlPDClOdklQb8tj8Kpw  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ContentCaptureService$1$jkZQ77YuBlPDClOdklQb8tj8Kpw implements BiConsumer {
    public static final /* synthetic */ $$Lambda$ContentCaptureService$1$jkZQ77YuBlPDClOdklQb8tj8Kpw INSTANCE = new $$Lambda$ContentCaptureService$1$jkZQ77YuBlPDClOdklQb8tj8Kpw();

    private /* synthetic */ $$Lambda$ContentCaptureService$1$jkZQ77YuBlPDClOdklQb8tj8Kpw() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((ContentCaptureService) obj).handleFinishSession(((Integer) obj2).intValue());
    }
}
