package ohos.media.sessioncore;

import java.util.function.Function;
import ohos.media.sessioncore.adapter.AVControllerAdapter;

/* renamed from: ohos.media.sessioncore.-$$Lambda$AVSessionManager$ZK59LGZFMiotlWBhy7PITyocZEY  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AVSessionManager$ZK59LGZFMiotlWBhy7PITyocZEY implements Function {
    public static final /* synthetic */ $$Lambda$AVSessionManager$ZK59LGZFMiotlWBhy7PITyocZEY INSTANCE = new $$Lambda$AVSessionManager$ZK59LGZFMiotlWBhy7PITyocZEY();

    private /* synthetic */ $$Lambda$AVSessionManager$ZK59LGZFMiotlWBhy7PITyocZEY() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return AVSessionManager.lambda$getActiveAVControllers$0((AVControllerAdapter) obj);
    }
}
