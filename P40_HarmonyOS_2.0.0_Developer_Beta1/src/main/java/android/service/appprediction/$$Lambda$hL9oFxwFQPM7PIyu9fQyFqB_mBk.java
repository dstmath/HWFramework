package android.service.appprediction;

import android.app.prediction.AppPredictionSessionId;
import android.os.CancellationSignal;
import android.service.appprediction.AppPredictionService;
import com.android.internal.util.function.QuintConsumer;
import java.util.List;

/* renamed from: android.service.appprediction.-$$Lambda$hL9oFxwFQPM7PIyu9fQyFqB_mBk  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$hL9oFxwFQPM7PIyu9fQyFqB_mBk implements QuintConsumer {
    public static final /* synthetic */ $$Lambda$hL9oFxwFQPM7PIyu9fQyFqB_mBk INSTANCE = new $$Lambda$hL9oFxwFQPM7PIyu9fQyFqB_mBk();

    private /* synthetic */ $$Lambda$hL9oFxwFQPM7PIyu9fQyFqB_mBk() {
    }

    @Override // com.android.internal.util.function.QuintConsumer
    public final void accept(Object obj, Object obj2, Object obj3, Object obj4, Object obj5) {
        ((AppPredictionService) obj).onSortAppTargets((AppPredictionSessionId) obj2, (List) obj3, (CancellationSignal) obj4, (AppPredictionService.CallbackWrapper) obj5);
    }
}
