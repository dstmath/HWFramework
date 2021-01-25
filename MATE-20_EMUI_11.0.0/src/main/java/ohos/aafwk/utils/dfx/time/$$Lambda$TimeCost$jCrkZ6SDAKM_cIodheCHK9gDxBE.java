package ohos.aafwk.utils.dfx.time;

import java.util.function.Consumer;

/* renamed from: ohos.aafwk.utils.dfx.time.-$$Lambda$TimeCost$jCrkZ6SDAKM_cIodheCHK9gDxBE  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TimeCost$jCrkZ6SDAKM_cIodheCHK9gDxBE implements Consumer {
    public static final /* synthetic */ $$Lambda$TimeCost$jCrkZ6SDAKM_cIodheCHK9gDxBE INSTANCE = new $$Lambda$TimeCost$jCrkZ6SDAKM_cIodheCHK9gDxBE();

    private /* synthetic */ $$Lambda$TimeCost$jCrkZ6SDAKM_cIodheCHK9gDxBE() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((RecordPool) obj).forceDone();
    }
}
