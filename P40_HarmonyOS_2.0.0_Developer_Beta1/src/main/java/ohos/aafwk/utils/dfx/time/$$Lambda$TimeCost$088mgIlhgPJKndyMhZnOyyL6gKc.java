package ohos.aafwk.utils.dfx.time;

import java.util.function.BiConsumer;

/* renamed from: ohos.aafwk.utils.dfx.time.-$$Lambda$TimeCost$088mgIlhgPJKndyMhZnOyyL6gKc  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TimeCost$088mgIlhgPJKndyMhZnOyyL6gKc implements BiConsumer {
    public static final /* synthetic */ $$Lambda$TimeCost$088mgIlhgPJKndyMhZnOyyL6gKc INSTANCE = new $$Lambda$TimeCost$088mgIlhgPJKndyMhZnOyyL6gKc();

    private /* synthetic */ $$Lambda$TimeCost$088mgIlhgPJKndyMhZnOyyL6gKc() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        TimeEventType timeEventType = (TimeEventType) obj;
        ((RecordPool) obj2).clearDeadRecords();
    }
}
