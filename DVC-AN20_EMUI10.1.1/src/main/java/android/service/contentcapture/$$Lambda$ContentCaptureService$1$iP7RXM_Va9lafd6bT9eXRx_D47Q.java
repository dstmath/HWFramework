package android.service.contentcapture;

import android.os.IBinder;
import java.util.function.BiConsumer;

/* renamed from: android.service.contentcapture.-$$Lambda$ContentCaptureService$1$iP7RXM_Va9lafd6bT9eXRx_D47Q  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ContentCaptureService$1$iP7RXM_Va9lafd6bT9eXRx_D47Q implements BiConsumer {
    public static final /* synthetic */ $$Lambda$ContentCaptureService$1$iP7RXM_Va9lafd6bT9eXRx_D47Q INSTANCE = new $$Lambda$ContentCaptureService$1$iP7RXM_Va9lafd6bT9eXRx_D47Q();

    private /* synthetic */ $$Lambda$ContentCaptureService$1$iP7RXM_Va9lafd6bT9eXRx_D47Q() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((ContentCaptureService) obj).handleOnConnected((IBinder) obj2);
    }
}
