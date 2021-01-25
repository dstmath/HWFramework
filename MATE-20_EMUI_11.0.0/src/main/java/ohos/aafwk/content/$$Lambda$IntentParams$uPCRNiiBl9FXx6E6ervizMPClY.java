package ohos.aafwk.content;

import java.util.function.BiFunction;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

/* renamed from: ohos.aafwk.content.-$$Lambda$IntentParams$uPCRNiiBl9F-Xx6E6ervizMPClY  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$IntentParams$uPCRNiiBl9FXx6E6ervizMPClY implements BiFunction {
    public static final /* synthetic */ $$Lambda$IntentParams$uPCRNiiBl9FXx6E6ervizMPClY INSTANCE = new $$Lambda$IntentParams$uPCRNiiBl9FXx6E6ervizMPClY();

    private /* synthetic */ $$Lambda$IntentParams$uPCRNiiBl9FXx6E6ervizMPClY() {
    }

    @Override // java.util.function.BiFunction
    public final Object apply(Object obj, Object obj2) {
        return ((Parcel) obj).writeTypedSequenceableArray((Sequenceable[]) obj2);
    }
}
