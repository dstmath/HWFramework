package android.service.contentcapture;

import android.view.contentcapture.ContentCaptureContext;
import com.android.internal.os.IResultReceiver;
import com.android.internal.util.function.HexConsumer;

/* renamed from: android.service.contentcapture.-$$Lambda$ContentCaptureService$1$PaMsQkJwdUJ1lCgOOaLG9Bm09t8  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ContentCaptureService$1$PaMsQkJwdUJ1lCgOOaLG9Bm09t8 implements HexConsumer {
    public static final /* synthetic */ $$Lambda$ContentCaptureService$1$PaMsQkJwdUJ1lCgOOaLG9Bm09t8 INSTANCE = new $$Lambda$ContentCaptureService$1$PaMsQkJwdUJ1lCgOOaLG9Bm09t8();

    private /* synthetic */ $$Lambda$ContentCaptureService$1$PaMsQkJwdUJ1lCgOOaLG9Bm09t8() {
    }

    @Override // com.android.internal.util.function.HexConsumer
    public final void accept(Object obj, Object obj2, Object obj3, Object obj4, Object obj5, Object obj6) {
        ((ContentCaptureService) obj).handleOnCreateSession((ContentCaptureContext) obj2, ((Integer) obj3).intValue(), ((Integer) obj4).intValue(), (IResultReceiver) obj5, ((Integer) obj6).intValue());
    }
}
