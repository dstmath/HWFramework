package ohos.agp.utils;

import java.util.function.Predicate;

/* renamed from: ohos.agp.utils.-$$Lambda$1_OzCeVcv7csvH-b6C43NEdIblg  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$1_OzCeVcv7csvHb6C43NEdIblg implements Predicate {
    public static final /* synthetic */ $$Lambda$1_OzCeVcv7csvHb6C43NEdIblg INSTANCE = new $$Lambda$1_OzCeVcv7csvHb6C43NEdIblg();

    private /* synthetic */ $$Lambda$1_OzCeVcv7csvHb6C43NEdIblg() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return TextTool.isValidTextSize(((Integer) obj).intValue());
    }
}
