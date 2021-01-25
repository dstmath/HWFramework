package ohos.aafwk.utils.dfx.time;

import java.util.function.Function;

/* renamed from: ohos.aafwk.utils.dfx.time.-$$Lambda$TimeRecord$LmCrKbpkX1DMUMNAHC3GfRBjJmM  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TimeRecord$LmCrKbpkX1DMUMNAHC3GfRBjJmM implements Function {
    public static final /* synthetic */ $$Lambda$TimeRecord$LmCrKbpkX1DMUMNAHC3GfRBjJmM INSTANCE = new $$Lambda$TimeRecord$LmCrKbpkX1DMUMNAHC3GfRBjJmM();

    private /* synthetic */ $$Lambda$TimeRecord$LmCrKbpkX1DMUMNAHC3GfRBjJmM() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return Integer.valueOf(((TimeRecord) ((TimeEvent) obj)).getDuration());
    }
}
