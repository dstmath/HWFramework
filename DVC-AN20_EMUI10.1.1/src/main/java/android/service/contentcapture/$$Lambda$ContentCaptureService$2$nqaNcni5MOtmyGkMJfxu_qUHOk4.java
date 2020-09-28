package android.service.contentcapture;

import android.content.ContentCaptureOptions;
import android.content.pm.ParceledListSlice;
import com.android.internal.util.function.QuintConsumer;

/* renamed from: android.service.contentcapture.-$$Lambda$ContentCaptureService$2$nqaNcni5MOtmyGkMJfxu_qUHOk4  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ContentCaptureService$2$nqaNcni5MOtmyGkMJfxu_qUHOk4 implements QuintConsumer {
    public static final /* synthetic */ $$Lambda$ContentCaptureService$2$nqaNcni5MOtmyGkMJfxu_qUHOk4 INSTANCE = new $$Lambda$ContentCaptureService$2$nqaNcni5MOtmyGkMJfxu_qUHOk4();

    private /* synthetic */ $$Lambda$ContentCaptureService$2$nqaNcni5MOtmyGkMJfxu_qUHOk4() {
    }

    @Override // com.android.internal.util.function.QuintConsumer
    public final void accept(Object obj, Object obj2, Object obj3, Object obj4, Object obj5) {
        ((ContentCaptureService) obj).handleSendEvents(((Integer) obj2).intValue(), (ParceledListSlice) obj3, ((Integer) obj4).intValue(), (ContentCaptureOptions) obj5);
    }
}
