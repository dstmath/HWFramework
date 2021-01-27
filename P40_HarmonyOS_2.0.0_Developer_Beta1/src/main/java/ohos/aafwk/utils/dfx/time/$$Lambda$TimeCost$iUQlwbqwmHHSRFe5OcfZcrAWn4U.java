package ohos.aafwk.utils.dfx.time;

import java.util.function.Consumer;

/* renamed from: ohos.aafwk.utils.dfx.time.-$$Lambda$TimeCost$iUQlwbqwmHHSRFe5OcfZcrAWn4U  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TimeCost$iUQlwbqwmHHSRFe5OcfZcrAWn4U implements Consumer {
    public static final /* synthetic */ $$Lambda$TimeCost$iUQlwbqwmHHSRFe5OcfZcrAWn4U INSTANCE = new $$Lambda$TimeCost$iUQlwbqwmHHSRFe5OcfZcrAWn4U();

    private /* synthetic */ $$Lambda$TimeCost$iUQlwbqwmHHSRFe5OcfZcrAWn4U() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((TimeCost) obj).clearDeadRecords();
    }
}
