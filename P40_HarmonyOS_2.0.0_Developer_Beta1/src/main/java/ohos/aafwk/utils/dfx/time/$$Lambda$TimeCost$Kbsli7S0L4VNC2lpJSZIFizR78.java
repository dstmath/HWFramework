package ohos.aafwk.utils.dfx.time;

import java.util.function.BiConsumer;

/* renamed from: ohos.aafwk.utils.dfx.time.-$$Lambda$TimeCost$Kbsli7S0L4VNC2l-pJSZIFizR78  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TimeCost$Kbsli7S0L4VNC2lpJSZIFizR78 implements BiConsumer {
    public static final /* synthetic */ $$Lambda$TimeCost$Kbsli7S0L4VNC2lpJSZIFizR78 INSTANCE = new $$Lambda$TimeCost$Kbsli7S0L4VNC2lpJSZIFizR78();

    private /* synthetic */ $$Lambda$TimeCost$Kbsli7S0L4VNC2lpJSZIFizR78() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        TimeEventType timeEventType = (TimeEventType) obj;
        ((RecordPool) obj2).clear();
    }
}
