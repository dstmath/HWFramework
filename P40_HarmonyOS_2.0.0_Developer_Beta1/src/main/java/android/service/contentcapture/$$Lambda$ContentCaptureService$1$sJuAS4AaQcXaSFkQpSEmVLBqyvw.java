package android.service.contentcapture;

import android.view.contentcapture.DataRemovalRequest;
import java.util.function.BiConsumer;

/* renamed from: android.service.contentcapture.-$$Lambda$ContentCaptureService$1$sJuAS4AaQcXaSFkQpSEmVLBqyvw  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ContentCaptureService$1$sJuAS4AaQcXaSFkQpSEmVLBqyvw implements BiConsumer {
    public static final /* synthetic */ $$Lambda$ContentCaptureService$1$sJuAS4AaQcXaSFkQpSEmVLBqyvw INSTANCE = new $$Lambda$ContentCaptureService$1$sJuAS4AaQcXaSFkQpSEmVLBqyvw();

    private /* synthetic */ $$Lambda$ContentCaptureService$1$sJuAS4AaQcXaSFkQpSEmVLBqyvw() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((ContentCaptureService) obj).handleOnDataRemovalRequest((DataRemovalRequest) obj2);
    }
}
