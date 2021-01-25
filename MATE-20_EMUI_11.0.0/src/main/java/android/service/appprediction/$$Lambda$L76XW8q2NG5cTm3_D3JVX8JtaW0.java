package android.service.appprediction;

import android.app.prediction.AppPredictionSessionId;
import android.app.prediction.AppTargetEvent;
import com.android.internal.util.function.TriConsumer;

/* renamed from: android.service.appprediction.-$$Lambda$L76XW8q2NG5cTm3_D3JVX8JtaW0  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$L76XW8q2NG5cTm3_D3JVX8JtaW0 implements TriConsumer {
    public static final /* synthetic */ $$Lambda$L76XW8q2NG5cTm3_D3JVX8JtaW0 INSTANCE = new $$Lambda$L76XW8q2NG5cTm3_D3JVX8JtaW0();

    private /* synthetic */ $$Lambda$L76XW8q2NG5cTm3_D3JVX8JtaW0() {
    }

    @Override // com.android.internal.util.function.TriConsumer
    public final void accept(Object obj, Object obj2, Object obj3) {
        ((AppPredictionService) obj).onAppTargetEvent((AppPredictionSessionId) obj2, (AppTargetEvent) obj3);
    }
}
