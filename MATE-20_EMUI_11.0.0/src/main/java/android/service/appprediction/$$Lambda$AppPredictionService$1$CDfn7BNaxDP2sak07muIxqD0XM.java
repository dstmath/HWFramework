package android.service.appprediction;

import android.app.prediction.AppPredictionSessionId;
import android.app.prediction.IPredictionCallback;
import com.android.internal.util.function.TriConsumer;

/* renamed from: android.service.appprediction.-$$Lambda$AppPredictionService$1$CDfn7BNaxDP2sak-07muIxqD0XM  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AppPredictionService$1$CDfn7BNaxDP2sak07muIxqD0XM implements TriConsumer {
    public static final /* synthetic */ $$Lambda$AppPredictionService$1$CDfn7BNaxDP2sak07muIxqD0XM INSTANCE = new $$Lambda$AppPredictionService$1$CDfn7BNaxDP2sak07muIxqD0XM();

    private /* synthetic */ $$Lambda$AppPredictionService$1$CDfn7BNaxDP2sak07muIxqD0XM() {
    }

    @Override // com.android.internal.util.function.TriConsumer
    public final void accept(Object obj, Object obj2, Object obj3) {
        ((AppPredictionService) obj).doRegisterPredictionUpdates((AppPredictionSessionId) obj2, (IPredictionCallback) obj3);
    }
}
