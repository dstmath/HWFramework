package android.service.appprediction;

import android.app.prediction.AppPredictionContext;
import android.app.prediction.AppPredictionSessionId;
import com.android.internal.util.function.TriConsumer;

/* renamed from: android.service.appprediction.-$$Lambda$AppPredictionService$1$dlPwi16n_6u5po2eN8wlW4I1bRw  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AppPredictionService$1$dlPwi16n_6u5po2eN8wlW4I1bRw implements TriConsumer {
    public static final /* synthetic */ $$Lambda$AppPredictionService$1$dlPwi16n_6u5po2eN8wlW4I1bRw INSTANCE = new $$Lambda$AppPredictionService$1$dlPwi16n_6u5po2eN8wlW4I1bRw();

    private /* synthetic */ $$Lambda$AppPredictionService$1$dlPwi16n_6u5po2eN8wlW4I1bRw() {
    }

    @Override // com.android.internal.util.function.TriConsumer
    public final void accept(Object obj, Object obj2, Object obj3) {
        ((AppPredictionService) obj).doCreatePredictionSession((AppPredictionContext) obj2, (AppPredictionSessionId) obj3);
    }
}
