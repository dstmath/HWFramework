package android.service.autofill;

import android.os.CancellationSignal;
import com.android.internal.util.function.QuadConsumer;

/* renamed from: android.service.autofill.-$$Lambda$I0gCKFrBTO70VZfSZTq2fj-wyG8  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$I0gCKFrBTO70VZfSZTq2fjwyG8 implements QuadConsumer {
    public static final /* synthetic */ $$Lambda$I0gCKFrBTO70VZfSZTq2fjwyG8 INSTANCE = new $$Lambda$I0gCKFrBTO70VZfSZTq2fjwyG8();

    private /* synthetic */ $$Lambda$I0gCKFrBTO70VZfSZTq2fjwyG8() {
    }

    public final void accept(Object obj, Object obj2, Object obj3, Object obj4) {
        ((AutofillService) obj).onFillRequest((FillRequest) obj2, (CancellationSignal) obj3, (FillCallback) obj4);
    }
}
