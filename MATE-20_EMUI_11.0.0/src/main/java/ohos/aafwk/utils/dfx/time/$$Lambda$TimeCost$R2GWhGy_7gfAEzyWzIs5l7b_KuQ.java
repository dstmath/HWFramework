package ohos.aafwk.utils.dfx.time;

import java.util.function.BiConsumer;

/* renamed from: ohos.aafwk.utils.dfx.time.-$$Lambda$TimeCost$R2GWhGy_7gfAEzyWzIs5l7b_KuQ  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TimeCost$R2GWhGy_7gfAEzyWzIs5l7b_KuQ implements BiConsumer {
    public static final /* synthetic */ $$Lambda$TimeCost$R2GWhGy_7gfAEzyWzIs5l7b_KuQ INSTANCE = new $$Lambda$TimeCost$R2GWhGy_7gfAEzyWzIs5l7b_KuQ();

    private /* synthetic */ $$Lambda$TimeCost$R2GWhGy_7gfAEzyWzIs5l7b_KuQ() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        TimeEventType timeEventType = (TimeEventType) obj;
        ((RecordPool) obj2).clear();
    }
}
