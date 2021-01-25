package android.service.appprediction;

import android.app.prediction.AppPredictionSessionId;
import java.util.function.BiConsumer;

/* renamed from: android.service.appprediction.-$$Lambda$AppPredictionService$1$oaGU8LD9Stlihi_KoW_pb0jZjQk  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AppPredictionService$1$oaGU8LD9Stlihi_KoW_pb0jZjQk implements BiConsumer {
    public static final /* synthetic */ $$Lambda$AppPredictionService$1$oaGU8LD9Stlihi_KoW_pb0jZjQk INSTANCE = new $$Lambda$AppPredictionService$1$oaGU8LD9Stlihi_KoW_pb0jZjQk();

    private /* synthetic */ $$Lambda$AppPredictionService$1$oaGU8LD9Stlihi_KoW_pb0jZjQk() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((AppPredictionService) obj).doRequestPredictionUpdate((AppPredictionSessionId) obj2);
    }
}
