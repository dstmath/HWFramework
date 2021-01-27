package ohos.aafwk.utils.dfx.time;

import java.util.function.Consumer;

/* renamed from: ohos.aafwk.utils.dfx.time.-$$Lambda$TimeCost$WgCRqXPN5elkWmMJx6vUYdueZGU  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TimeCost$WgCRqXPN5elkWmMJx6vUYdueZGU implements Consumer {
    public static final /* synthetic */ $$Lambda$TimeCost$WgCRqXPN5elkWmMJx6vUYdueZGU INSTANCE = new $$Lambda$TimeCost$WgCRqXPN5elkWmMJx6vUYdueZGU();

    private /* synthetic */ $$Lambda$TimeCost$WgCRqXPN5elkWmMJx6vUYdueZGU() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((TimeCost) obj).forceDoneAllPools();
    }
}
