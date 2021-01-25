package android.service.appprediction;

import android.app.prediction.AppPredictionSessionId;
import android.app.prediction.IPredictionCallback;
import com.android.internal.util.function.TriConsumer;

/* renamed from: android.service.appprediction.-$$Lambda$AppPredictionService$1$3o4A2wryMBwv4mIbcQKrEaoUyik  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AppPredictionService$1$3o4A2wryMBwv4mIbcQKrEaoUyik implements TriConsumer {
    public static final /* synthetic */ $$Lambda$AppPredictionService$1$3o4A2wryMBwv4mIbcQKrEaoUyik INSTANCE = new $$Lambda$AppPredictionService$1$3o4A2wryMBwv4mIbcQKrEaoUyik();

    private /* synthetic */ $$Lambda$AppPredictionService$1$3o4A2wryMBwv4mIbcQKrEaoUyik() {
    }

    @Override // com.android.internal.util.function.TriConsumer
    public final void accept(Object obj, Object obj2, Object obj3) {
        ((AppPredictionService) obj).doUnregisterPredictionUpdates((AppPredictionSessionId) obj2, (IPredictionCallback) obj3);
    }
}
