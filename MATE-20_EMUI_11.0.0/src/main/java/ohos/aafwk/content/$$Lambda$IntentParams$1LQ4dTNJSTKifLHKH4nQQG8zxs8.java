package ohos.aafwk.content;

import java.util.function.BiFunction;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

/* renamed from: ohos.aafwk.content.-$$Lambda$IntentParams$1LQ4dTNJSTKifLHKH4nQQG8zxs8  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$IntentParams$1LQ4dTNJSTKifLHKH4nQQG8zxs8 implements BiFunction {
    public static final /* synthetic */ $$Lambda$IntentParams$1LQ4dTNJSTKifLHKH4nQQG8zxs8 INSTANCE = new $$Lambda$IntentParams$1LQ4dTNJSTKifLHKH4nQQG8zxs8();

    private /* synthetic */ $$Lambda$IntentParams$1LQ4dTNJSTKifLHKH4nQQG8zxs8() {
    }

    @Override // java.util.function.BiFunction
    public final Object apply(Object obj, Object obj2) {
        return ((Parcel) obj).writeTypedSequenceable((Sequenceable) obj2);
    }
}
