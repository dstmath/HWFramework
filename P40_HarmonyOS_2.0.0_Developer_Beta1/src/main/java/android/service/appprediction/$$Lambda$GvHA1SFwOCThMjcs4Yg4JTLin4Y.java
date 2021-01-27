package android.service.appprediction;

import android.app.prediction.AppPredictionSessionId;
import com.android.internal.util.function.QuadConsumer;
import java.util.List;

/* renamed from: android.service.appprediction.-$$Lambda$GvHA1SFwOCThMjcs4Yg4JTLin4Y  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$GvHA1SFwOCThMjcs4Yg4JTLin4Y implements QuadConsumer {
    public static final /* synthetic */ $$Lambda$GvHA1SFwOCThMjcs4Yg4JTLin4Y INSTANCE = new $$Lambda$GvHA1SFwOCThMjcs4Yg4JTLin4Y();

    private /* synthetic */ $$Lambda$GvHA1SFwOCThMjcs4Yg4JTLin4Y() {
    }

    @Override // com.android.internal.util.function.QuadConsumer
    public final void accept(Object obj, Object obj2, Object obj3, Object obj4) {
        ((AppPredictionService) obj).onLaunchLocationShown((AppPredictionSessionId) obj2, (String) obj3, (List) obj4);
    }
}
